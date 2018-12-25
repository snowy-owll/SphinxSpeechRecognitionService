package ru.snowy_owl.testservicesphinx;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static ru.snowy_owl.testservicesphinx.Consts.*;

public class SphinxService extends Service implements RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    private static final String COMMAND_SEARCH = "commands";
    private static final int NOTIFY_ID = 1;

    private SharedPreferences _pref;
    private SpeechRecognizer _recognizer;
    private NotificationManager _nm;
    private HashMap<String,Integer> _broadcastStatusLabels;
    private long _timer;
    private boolean _started = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Создание сервиса");
        _pref = getSharedPreferences(FILE_PREF_NAME, MODE_PRIVATE);
        _nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        _broadcastStatusLabels = BroadcastStatusLabels;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(_started){
            return super.onStartCommand(intent, flags, startId);
        }
        _started = true;
        startForeground(NOTIFY_ID,_buildNotify(""));
        _sendBroadcast(BROADCAST_STATUS_START_INIT);
        Log.d(LOG_TAG, "Запуск сервиса");
        Log.d(LOG_TAG, "Начата инициализация распознавания.");
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params){
                try {
                    File sphinxDir = new File(_pref.getString(PREF_SPHINX_PATH,DEFAULT_SPHINX_PATH));
                    _setupRecognizer(sphinxDir);
                } catch (RuntimeException | IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if(result!=null){
                    Log.d(LOG_TAG,"Произошла ошибка при инициализации: "+result);

                    String error = result.getMessage();
                    if(result.getClass()== RuntimeException.class)
                        error+=" (возможно неверно указана директория с акустической " +
                                "моделью или отсутствуют некоторые файлы)";
                    _sendBroadcast(BROADCAST_STATUS_ERROR_INIT,error);
                    stopSelf();
                    return;
                }
                Log.d(LOG_TAG, "Инициализация распознавания выполнена.");
                _sendBroadcast(BROADCAST_STATUS_INIT_COMPLETE);
                _switchSearch(KWS_SEARCH);
            }
        }.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    private void _setupRecognizer(File sphinxDir) throws IOException {
        if(!sphinxDir.exists()){
            Log.d(LOG_TAG, "Невозможно получить доступ к директории по пути (или не существует): "
                    +sphinxDir.getAbsolutePath());
            throw new RuntimeException("Can not access directory by path (or does not exist): "
                    +sphinxDir.getAbsolutePath());
        }

        int sample_rate = Integer.parseInt(_pref.getString(PREF_SAMPLE_RATE,DEFAULT_SAMPLE_RATE));

        SpeechRecognizerSetup recognizerSetup = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(sphinxDir,"ptm"))
                .setDictionary(new File(sphinxDir, "dict.dic"))
                .setSampleRate(sample_rate)
                .setBoolean("-remove_noise",_pref.getBoolean(PREF_REMOVE_NOISE,DEFAULT_REMOVE_NOISE));
        if(_pref.getBoolean(PREF_ENABLE_RAW_LOG,DEFAULT_ENABLE_RAW_LOG)){
            File logDir = new File(sphinxDir, "log_raw");
            boolean success = true;
            if(!logDir.exists()){
                success = logDir.mkdir();
            }
            if(success){
                recognizerSetup.setRawLogDir(logDir);
            }
            else {
                String message = "Не удалось создать директорию для логов распознавателя. Логирование не включено";
                _sendBroadcast(BROADCAST_STATUS_ERROR_INIT, message);
                Log.d(LOG_TAG, message);
            }
        }
        _recognizer = recognizerSetup.getRecognizer();
        _recognizer.addListener(this);

        _recognizer.addKeyphraseSearch(KWS_SEARCH,_pref.getString(PREF_KEYPHRASE,DEFAULT_KEYPHRASE));
        _recognizer.addGrammarSearch(COMMAND_SEARCH,
                new File(sphinxDir, "command.gram"));
    }

    @Override
    public void onDestroy() {
        _sendBroadcast(BROADCAST_STATUS_STOP);
        super.onDestroy();
        Log.d(LOG_TAG, "Уничтожение сервиса");
        if(_recognizer!=null){
            _recognizer.cancel();
            _recognizer.shutdown();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Подключение к сервису");
        return null;
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(LOG_TAG, "Обнаружен звук (возможно речь)");
        _timer = System.currentTimeMillis();
    }

    @Override
    public void onEndOfSpeech() {
        if(!_recognizer.getSearchName().equals(KWS_SEARCH)){
            _switchSearch(KWS_SEARCH);
            return;
        }
        Log.d(LOG_TAG, "Звук закончился");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if(hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if(text.equals(_pref.getString(PREF_KEYPHRASE, DEFAULT_KEYPHRASE))) {
            Log.d(LOG_TAG, "Ключевая фраза распознана");
            _sendBroadcast(BROADCAST_STATUS_KEYPHRASE_RECOGNIZED);
            _switchSearch(COMMAND_SEARCH);
        }
        else {
            Log.d(LOG_TAG, "Частичный результат распознавания: " + text);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis==null){
            return;
        }
        String text = hypothesis.getHypstr();
        //test score
        int bestScore = hypothesis.getBestScore();
        long sec = System.currentTimeMillis()-_timer;
        float confidence = ((float)bestScore)/((float)sec);
        //
        Log.d(LOG_TAG, "Окончательный результат распознавания: "+text
                        +". Доверие: " + confidence);
        if(!text.equals(_pref.getString(PREF_KEYPHRASE,DEFAULT_KEYPHRASE))) {
            _sendBroadcast(BROADCAST_STATUS_COMMAND_RECOGNIZED, text, confidence);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.d(LOG_TAG, "Ошибка распознавания: "+e.getMessage());
    }

    @Override
    public void onTimeout() {
        _sendBroadcast(BROADCAST_STATUS_RECOGNIZE_COMMAND_TIMEOUT);
        _switchSearch(KWS_SEARCH);
    }

    private void _switchSearch(String searchName) {
        _recognizer.stop();

        if(searchName.equals(KWS_SEARCH)){
            _recognizer.startListening(searchName);
            _sendBroadcast(BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE);
            Log.d(LOG_TAG, "Начато распознавание по поиску \""+searchName+"\"");
        }
        else {
            int timeout = Integer.parseInt(_pref.getString(PREF_TIMEOUT_RECOGNITION, DEFAULT_TIMEOUT_RECOGNITION));
            _recognizer.startListening(searchName, timeout);
            _sendBroadcast(BROADCAST_STATUS_START_RECOGNIZE_COMMAND);
            Log.d(LOG_TAG, "Начато распознавание по поиску \""+searchName+"\" c таймаутом "+(timeout/1000)+" секунд");
        }
    }

    private void _sendBroadcast(String status){_sendBroadcast(status,null);}
    private void _sendBroadcast(String status, String data){
        _sendBroadcast(status, data, null);
    }
    private void _sendBroadcast(String status, String data, Float confidence){
        if(!status.equals(BROADCAST_STATUS_STOP)) {
            String label = getResources().getString(_broadcastStatusLabels.get(status));
            if(status.equals(BROADCAST_STATUS_START_INIT)||
                    status.equals(BROADCAST_STATUS_START_RECOGNIZE_COMMAND)||
                    status.equals(BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE)) {
                _sendNotify(label);
            }
        }

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(BROADCAST_PARAM_STATUS, status);
        if(data!=null && !data.isEmpty()){
            intent.putExtra(BROADCAST_PARAM_DATA,data);
        }
        if(confidence!=null) {
            intent.putExtra(BROADCAST_PARAM_CONFIDENCE, confidence);
        }
        sendBroadcast(intent);
    }

    private void _sendNotify(String message){
        _nm.notify(NOTIFY_ID, _buildNotify(message));
    }

    private Notification _buildNotify(String message) {
        Intent startActivity=new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                startActivity,
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String title = getResources().getString(R.string.notify_title);
        return new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentIntent(pendingIntent).build();
    }
}
