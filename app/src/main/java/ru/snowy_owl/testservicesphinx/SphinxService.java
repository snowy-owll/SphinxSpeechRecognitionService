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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static ru.snowy_owl.testservicesphinx.Consts.*;

public class SphinxService extends Service implements RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    private static final String COMMAND_SEARCH = "commands";
    private static final int NOTIFY_ID = 1;

    private SharedPreferences mPref;
    private SpeechRecognizer mRecognizer;
    private NotificationManager mNotificationManager;
    private HashMap<String, Integer> mBroadcastStatusLabels;
    private long mTimer;
    private boolean mStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Создание сервиса");
        mPref = getSharedPreferences(FILE_PREF_NAME, MODE_PRIVATE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mBroadcastStatusLabels = BroadcastStatusLabels;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mStarted) {
            return super.onStartCommand(intent, flags, startId);
        }
        mStarted = true;
        startForeground(NOTIFY_ID, buildNotify(""));
        sendBroadcast(BROADCAST_STATUS_START_INIT);
        Log.d(LOG_TAG, "Запуск сервиса");
        Log.d(LOG_TAG, "Начата инициализация распознавания.");
        //TODO: move the async task in a static inner class for prevent memory leaks
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    File sphinxDir = new File(mPref.getString(PREF_SPHINX_PATH, DEFAULT_SPHINX_PATH));
                    setupRecognizer(sphinxDir);
                } catch (RuntimeException | IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.d(LOG_TAG, "Произошла ошибка при инициализации: " + result);

                    String error = result.getMessage();
                    if (result.getClass() == RuntimeException.class)
                        error += " (возможно неверно указана директория с акустической " +
                                "моделью или отсутствуют некоторые файлы)";
                    sendBroadcast(BROADCAST_STATUS_ERROR_INIT, error);
                    stopSelf();
                    return;
                }
                Log.d(LOG_TAG, "Инициализация распознавания выполнена.");
                sendBroadcast(BROADCAST_STATUS_INIT_COMPLETE);
                switchSearch(KWS_SEARCH);
            }
        }.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    private void setupRecognizer(File sphinxDir) throws IOException, RuntimeException {
        if (!sphinxDir.exists()) {
            Log.d(LOG_TAG, "Невозможно получить доступ к директории по пути (или не существует): "
                    + sphinxDir.getAbsolutePath());
            throw new RuntimeException("Can not access directory by path (or does not exist): "
                    + sphinxDir.getAbsolutePath());
        }

        int sample_rate = Integer.parseInt(mPref.getString(PREF_SAMPLE_RATE, DEFAULT_SAMPLE_RATE));

        SpeechRecognizerSetup recognizerSetup = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(sphinxDir, "ptm"))
                .setDictionary(new File(sphinxDir, "dict.dic"))
                .setSampleRate(sample_rate)
                .setBoolean("-remove_noise", mPref.getBoolean(PREF_REMOVE_NOISE, DEFAULT_REMOVE_NOISE));
        if (mPref.getBoolean(PREF_ENABLE_RAW_LOG, DEFAULT_ENABLE_RAW_LOG)) {
            File logDir = new File(sphinxDir, "log_raw");
            boolean success = true;
            if (!logDir.exists()) {
                success = logDir.mkdir();
            }
            if (success) {
                recognizerSetup.setRawLogDir(logDir);
            } else {
                String message = "Не удалось создать директорию для логов распознавателя. Логирование не включено";
                sendBroadcast(BROADCAST_STATUS_ERROR_INIT, message);
                Log.d(LOG_TAG, message);
            }
        }
        mRecognizer = recognizerSetup.getRecognizer();
        mRecognizer.addListener(this);

        mRecognizer.addKeyphraseSearch(KWS_SEARCH, mPref.getString(PREF_KEYPHRASE, DEFAULT_KEYPHRASE));
        mRecognizer.addGrammarSearch(COMMAND_SEARCH,
                new File(sphinxDir, "command.gram"));
    }

    @Override
    public void onDestroy() {
        sendBroadcast(BROADCAST_STATUS_STOP);
        super.onDestroy();
        Log.d(LOG_TAG, "Уничтожение сервиса");
        if (mRecognizer != null) {
            mRecognizer.cancel();
            mRecognizer.shutdown();
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
        mTimer = System.currentTimeMillis();
    }

    @Override
    public void onEndOfSpeech() {
        if (!mRecognizer.getSearchName().equals(KWS_SEARCH)) {
            switchSearch(KWS_SEARCH);
            return;
        }
        Log.d(LOG_TAG, "Звук закончился");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if (text.equals(mPref.getString(PREF_KEYPHRASE, DEFAULT_KEYPHRASE))) {
            Log.d(LOG_TAG, "Ключевая фраза распознана");
            sendBroadcast(BROADCAST_STATUS_KEYPHRASE_RECOGNIZED);
            switchSearch(COMMAND_SEARCH);
        } else {
            Log.d(LOG_TAG, "Частичный результат распознавания: " + text);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }
        String text = hypothesis.getHypstr();
        //test score
        int bestScore = hypothesis.getBestScore();
        long sec = System.currentTimeMillis() - mTimer;
        float confidence = ((float) bestScore) / ((float) sec);
        //
        Log.d(LOG_TAG, "Окончательный результат распознавания: " + text
                + ". Доверие: " + confidence);
        if (!text.equals(mPref.getString(PREF_KEYPHRASE, DEFAULT_KEYPHRASE))) {
            sendBroadcast(BROADCAST_STATUS_COMMAND_RECOGNIZED, text, confidence);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.d(LOG_TAG, "Ошибка распознавания: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        sendBroadcast(BROADCAST_STATUS_RECOGNIZE_COMMAND_TIMEOUT);
        switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        mRecognizer.stop();

        if (searchName.equals(KWS_SEARCH)) {
            mRecognizer.startListening(searchName);
            sendBroadcast(BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE);
            Log.d(LOG_TAG, "Начато распознавание по поиску \"" + searchName + "\"");
        } else {
            int timeout = Integer.parseInt(mPref.getString(PREF_TIMEOUT_RECOGNITION, DEFAULT_TIMEOUT_RECOGNITION));
            mRecognizer.startListening(searchName, timeout);
            sendBroadcast(BROADCAST_STATUS_START_RECOGNIZE_COMMAND);
            Log.d(LOG_TAG, "Начато распознавание по поиску \"" + searchName + "\" c таймаутом " + (timeout / 1000) + " секунд");
        }
    }

    private void sendBroadcast(String status) {
        sendBroadcast(status, null);
    }

    private void sendBroadcast(String status, String data) {
        sendBroadcast(status, data, null);
    }

    private void sendBroadcast(String status, String data, Float confidence) {
        if (!status.equals(BROADCAST_STATUS_STOP)) {
            String label = getResources().getString(mBroadcastStatusLabels.get(status));
            if (status.equals(BROADCAST_STATUS_START_INIT) ||
                    status.equals(BROADCAST_STATUS_START_RECOGNIZE_COMMAND) ||
                    status.equals(BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE)) {
                sendNotify(label);
            }
        }

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(BROADCAST_PARAM_STATUS, status);
        if (data != null && !data.isEmpty()) {
            intent.putExtra(BROADCAST_PARAM_DATA, data);
        }
        if (confidence != null) {
            intent.putExtra(BROADCAST_PARAM_CONFIDENCE, confidence);
        }
        sendBroadcast(intent);
    }

    private void sendNotify(String message) {
        mNotificationManager.notify(NOTIFY_ID, buildNotify(message));
    }

    private Notification buildNotify(String message) {
        Intent startActivity = new Intent(this, MainActivity.class);
        startActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                startActivity,
                0);
        String title = getResources().getString(R.string.notify_title);
        return new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentIntent(pendingIntent).build();
    }
}
