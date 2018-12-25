package ru.snowy_owl.testservicesphinx;

import java.util.HashMap;

final class Consts {
    //final static String PREF_FIRST_RUN = "first_run";
    final static String FILE_PREF_NAME = "SphinxPref";
    final static String PREF_SPHINX_PATH = "am_path";
    final static String PREF_KEYPHRASE = "keyphrase";
    final static String PREF_TIMEOUT_RECOGNITION ="timeout_recognition";
    final static String PREF_VIBRO_RECOGNIZED="vibro_recognized";
    final static String PREF_SOUND_RECOGNIZED="sound_recognized";
    final static String PREF_SAMPLE_RATE="sample_rate";
    final static String PREF_REMOVE_NOISE="remove_noise";
    final static String PREF_ENABLE_RAW_LOG="enable_raw_log";
    //final static String PREF_RAW_LOG_DIR="raw_log_dir";
    final static String DEFAULT_KEYPHRASE = "окей робот";
    final static String DEFAULT_TIMEOUT_RECOGNITION = "10000";
    final static String DEFAULT_SPHINX_PATH = "/storage/sdcard0/Sphinx/";
    final static boolean DEFAULT_VIBRO_RECOGNIZED = true;
    final static boolean DEFAULT_SOUND_RECOGNIZER = true;
    final static String DEFAULT_SAMPLE_RATE = "8000";
    final static boolean DEFAULT_REMOVE_NOISE=true;
    final static boolean DEFAULT_ENABLE_RAW_LOG = false;

    final static String BROADCAST_ACTION = "ru.snowy_owl.testservicesphinx.broadcast";
    final static String BROADCAST_PARAM_STATUS = "status";
    final static String BROADCAST_PARAM_DATA = "data";
    final static String BROADCAST_PARAM_CONFIDENCE="confidence";
    static final String BROADCAST_STATUS_START_INIT = "start_init";
    static final String BROADCAST_STATUS_STOP = "stop";
    static final String BROADCAST_STATUS_ERROR_INIT = "error_init";
    static final String BROADCAST_STATUS_INIT_COMPLETE = "init_complete";
    static final String BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE = "start_recognize_keyphrase";
    static final String BROADCAST_STATUS_START_RECOGNIZE_COMMAND = "start_recognize_command";
    static final String BROADCAST_STATUS_KEYPHRASE_RECOGNIZED = "keyphrase_recognized";
    static final String BROADCAST_STATUS_COMMAND_RECOGNIZED = "command_recognized";
    static final String BROADCAST_STATUS_RECOGNIZE_COMMAND_TIMEOUT="recognize_command_timeout";

    final static String LOG_TAG = "TestServiceSphinx";

    final static HashMap<String,Integer> BroadcastStatusLabels;

    static {
        BroadcastStatusLabels= new HashMap<>();
        BroadcastStatusLabels.put(BROADCAST_STATUS_ERROR_INIT,R.string.service_error_init);
        BroadcastStatusLabels.put(BROADCAST_STATUS_INIT_COMPLETE,R.string.service_init_complete);
        BroadcastStatusLabels.put(BROADCAST_STATUS_KEYPHRASE_RECOGNIZED,R.string.service_keyphrase_recognized);
        BroadcastStatusLabels.put(BROADCAST_STATUS_COMMAND_RECOGNIZED,R.string.service_command_recognized);
        BroadcastStatusLabels.put(BROADCAST_STATUS_START_INIT,R.string.service_start_init);
        BroadcastStatusLabels.put(BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE,R.string.service_start_recognize_keyphrase);
        BroadcastStatusLabels.put(BROADCAST_STATUS_START_RECOGNIZE_COMMAND,R.string.service_start_recognize_command);
        BroadcastStatusLabels.put(BROADCAST_STATUS_RECOGNIZE_COMMAND_TIMEOUT,R.string.service_recognize_command_timeout);
        BroadcastStatusLabels.put(BROADCAST_STATUS_STOP,R.string.service_stop);
    }

    private Consts(){
        throw new AssertionError();
    }
}
