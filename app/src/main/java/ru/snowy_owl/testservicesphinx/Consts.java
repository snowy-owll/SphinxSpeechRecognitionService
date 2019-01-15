package ru.snowy_owl.testservicesphinx;

import java.util.ArrayList;

import androidx.collection.SimpleArrayMap;

public final class Consts {
    public final static String BROADCAST_ACTION = "ru.snowy_owl.testservicesphinx.broadcast";
    public final static String BROADCAST_PARAM_STATUS = "status";
    public final static String BROADCAST_PARAM_DATA = "data";
    public final static String BROADCAST_PARAM_CONFIDENCE = "confidence";
    public static final String BROADCAST_STATUS_START_INIT = "start_init";
    public static final String BROADCAST_STATUS_STOP = "stop";
    public static final String BROADCAST_STATUS_ERROR_INIT = "error_init";
    public static final String BROADCAST_STATUS_INIT_COMPLETE = "init_complete";
    public static final String BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE = "start_recognize_keyphrase";
    public static final String BROADCAST_STATUS_START_RECOGNIZE_COMMAND = "start_recognize_command";
    public static final String BROADCAST_STATUS_KEYPHRASE_RECOGNIZED = "keyphrase_recognized";
    public static final String BROADCAST_STATUS_COMMAND_RECOGNIZED = "command_recognized";
    public static final String BROADCAST_STATUS_RECOGNIZE_COMMAND_TIMEOUT = "recognize_command_timeout";

    public final static String LOG_TAG = "TestServiceSphinx";

    public final static SimpleArrayMap<String, Integer> BROADCAST_STATUS_LABELS;
    public final static ArrayList<String> SUPPORTED_LANGUAGES;

    static {
        BROADCAST_STATUS_LABELS = new SimpleArrayMap<>();
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_ERROR_INIT, R.string.service_error_init);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_INIT_COMPLETE, R.string.service_init_complete);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_KEYPHRASE_RECOGNIZED, R.string.service_keyphrase_recognized);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_COMMAND_RECOGNIZED, R.string.service_command_recognized);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_START_INIT, R.string.service_start_init);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE, R.string.service_start_recognize_keyphrase);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_START_RECOGNIZE_COMMAND, R.string.service_start_recognize_command);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_RECOGNIZE_COMMAND_TIMEOUT, R.string.service_recognize_command_timeout);
        BROADCAST_STATUS_LABELS.put(BROADCAST_STATUS_STOP, R.string.service_stop);

        SUPPORTED_LANGUAGES = new ArrayList<>();
        SUPPORTED_LANGUAGES.add("en");
        SUPPORTED_LANGUAGES.add("ru");
    }

    private Consts() {
        throw new AssertionError();
    }
}
