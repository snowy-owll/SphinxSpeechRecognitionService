package ru.snowy_owl.testservicesphinx.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import androidx.core.app.NotificationManagerCompat;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import ru.snowy_owl.testservicesphinx.Consts;
import ru.snowy_owl.testservicesphinx.R;
import ru.snowy_owl.testservicesphinx.activities.MainActivity;
import ru.snowy_owl.testservicesphinx.helpers.PermissionsHelper;
import ru.snowy_owl.testservicesphinx.preferences.AppPreferences;

public class SphinxService extends LocalizedService implements RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    private static final String COMMAND_SEARCH = "commands";
    private static final int NOTIFY_ID = 1;
    private final static String CHANNEL_ID = "service_notification_channel";

    private AppPreferences mAppPreferences;
    private SpeechRecognizer mRecognizer;
    private NotificationManagerCompat mNotificationManager;
    private long mTimer;
    private boolean mStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(Consts.LOG_TAG, getResources().getConfiguration().locale.getLanguage());

        Log.d(Consts.LOG_TAG, "Creating a service");
        mAppPreferences = AppPreferences.getInstance();

        createNotificationChannel();
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mStarted) {
            return super.onStartCommand(intent, flags, startId);
        }
        if (!PermissionsHelper.checkRecordAudioPermission(getApplicationContext())
                || !PermissionsHelper.checkExternalStoragePermission(getApplicationContext())) {
            String error = getString(R.string.service_does_not_have_permissions);
            sendBroadcast(Consts.BROADCAST_STATUS_ERROR_INIT, error);
            stopSelf();
            return START_NOT_STICKY;
        }
        mStarted = true;
        startForeground(NOTIFY_ID, buildNotify(""));
        sendBroadcast(Consts.BROADCAST_STATUS_START_INIT);
        Log.d(Consts.LOG_TAG, "Start service");
        Log.d(Consts.LOG_TAG, "Recognition initialization started.");
        //TODO: move the async task in a static inner class for prevent memory leaks
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    File sphinxDir = new File(mAppPreferences.getAcousticModelPath());
                    setupRecognizer(sphinxDir);
                } catch (RuntimeException | IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.d(Consts.LOG_TAG, "An error occurred during initialization: " + result);

                    String error = result.getMessage();
                    if (result.getClass() == RuntimeException.class)
                        error += getString(R.string.perhaps_incorrectly_directory);
                    sendBroadcast(Consts.BROADCAST_STATUS_ERROR_INIT, error);
                    stopSelf();
                    return;
                }
                Log.d(Consts.LOG_TAG, "Recognition initialization completed.");
                sendBroadcast(Consts.BROADCAST_STATUS_INIT_COMPLETE);
                switchSearch(KWS_SEARCH);
            }
        }.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    private void setupRecognizer(File sphinxDir) throws IOException, RuntimeException {
        if (!sphinxDir.exists()) {
            Log.d(Consts.LOG_TAG, "Unable to access directory on path (or does not exist): "
                    + sphinxDir.getAbsolutePath());
            throw new RuntimeException(getString(R.string.can_not_access_directory)
                    + sphinxDir.getAbsolutePath());
        }

        int sample_rate = mAppPreferences.getSampleRate();

        SpeechRecognizerSetup recognizerSetup = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(sphinxDir, "ptm"))
                .setDictionary(new File(sphinxDir, "dict.dic"))
                .setSampleRate(sample_rate)
                .setBoolean("-remove_noise", mAppPreferences.getRemoveNoise());
        if (mAppPreferences.getEnableRawLog()) {
            File logDir = new File(sphinxDir, "log_raw");
            boolean success = true;
            if (!logDir.exists()) {
                success = logDir.mkdir();
            }
            if (success) {
                recognizerSetup.setRawLogDir(logDir);
            } else {
                String message = getString(R.string.unable_create_directory_for_logs);
                sendBroadcast(Consts.BROADCAST_STATUS_ERROR_INIT, message);
                Log.d(Consts.LOG_TAG, message);
            }
        }
        mRecognizer = recognizerSetup.getRecognizer();
        mRecognizer.addListener(this);

        mRecognizer.addKeyphraseSearch(KWS_SEARCH, mAppPreferences.getKeyphrase());
        mRecognizer.addGrammarSearch(COMMAND_SEARCH,
                new File(sphinxDir, "command.gram"));
    }

    @Override
    public void onDestroy() {
        sendBroadcast(Consts.BROADCAST_STATUS_STOP);
        super.onDestroy();
        Log.d(Consts.LOG_TAG, "Service destruction");
        if (mRecognizer != null) {
            mRecognizer.cancel();
            mRecognizer.shutdown();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Consts.LOG_TAG, "Connect to the service");
        return null;
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(Consts.LOG_TAG, "Sound detected (speech possible)");
        mTimer = System.currentTimeMillis();
    }

    @Override
    public void onEndOfSpeech() {
        if (!mRecognizer.getSearchName().equals(KWS_SEARCH)) {
            switchSearch(KWS_SEARCH);
            return;
        }
        Log.d(Consts.LOG_TAG, "Sound ended");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if (text.equals(mAppPreferences.getKeyphrase())) {
            Log.d(Consts.LOG_TAG, "Key phrase recognized");
            sendBroadcast(Consts.BROADCAST_STATUS_KEYPHRASE_RECOGNIZED);
            switchSearch(COMMAND_SEARCH);
        } else {
            Log.d(Consts.LOG_TAG, "Partial recognition result: " + text);
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
        Log.d(Consts.LOG_TAG, "The final recognition result: " + text
                + ". Confidence: " + confidence);
        if (!text.equals(mAppPreferences.getKeyphrase())) {
            sendBroadcast(Consts.BROADCAST_STATUS_COMMAND_RECOGNIZED, text, confidence);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.d(Consts.LOG_TAG, "Recognition error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        sendBroadcast(Consts.BROADCAST_STATUS_RECOGNIZE_COMMAND_TIMEOUT);
        switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        mRecognizer.stop();

        if (searchName.equals(KWS_SEARCH)) {
            mRecognizer.startListening(searchName);
            sendBroadcast(Consts.BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE);
            Log.d(Consts.LOG_TAG, "Started recognition by search \"" + searchName + "\"");
        } else {
            int timeout = mAppPreferences.getTimeoutRecognition();
            mRecognizer.startListening(searchName, timeout);
            sendBroadcast(Consts.BROADCAST_STATUS_START_RECOGNIZE_COMMAND);
            Log.d(Consts.LOG_TAG, "Started recognition by search \"" + searchName
                    + "\" with a timeout of " + (timeout / 1000) + " seconds");
        }
    }

    private void sendBroadcast(String status) {
        sendBroadcast(status, null);
    }

    private void sendBroadcast(String status, String data) {
        sendBroadcast(status, data, null);
    }

    private void sendBroadcast(String status, String data, Float confidence) {
        if (!status.equals(Consts.BROADCAST_STATUS_STOP)) {
            String label = getString(Consts.BROADCAST_STATUS_LABELS.get(status));
            if (status.equals(Consts.BROADCAST_STATUS_START_INIT) ||
                    status.equals(Consts.BROADCAST_STATUS_START_RECOGNIZE_COMMAND) ||
                    status.equals(Consts.BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE)) {
                sendNotify(label);
            }
        }

        Intent intent = new Intent(Consts.BROADCAST_ACTION);
        intent.putExtra(Consts.BROADCAST_PARAM_STATUS, status);
        if (data != null && !data.isEmpty()) {
            intent.putExtra(Consts.BROADCAST_PARAM_DATA, data);
        }
        if (confidence != null) {
            intent.putExtra(Consts.BROADCAST_PARAM_CONFIDENCE, confidence);
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
        String title = getString(R.string.notify_title);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
