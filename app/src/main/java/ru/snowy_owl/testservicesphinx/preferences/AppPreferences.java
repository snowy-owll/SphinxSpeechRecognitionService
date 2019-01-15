package ru.snowy_owl.testservicesphinx.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import ru.snowy_owl.testservicesphinx.R;

public class AppPreferences {
    private static final AppPreferences ourInstance = new AppPreferences();

    final static String FILE_PREF_NAME = "SphinxPref";
    final static String PREF_SPHINX_PATH = "am_path";
    final static String PREF_KEYPHRASE = "keyphrase";
    final static String PREF_TIMEOUT_RECOGNITION = "timeout_recognition";
    final static String PREF_VIBRO_RECOGNIZED = "vibro_recognized";
    final static String PREF_SOUND_RECOGNIZED = "sound_recognized";
    final static String PREF_SAMPLE_RATE = "sample_rate";
    final static String PREF_REMOVE_NOISE = "remove_noise";
    final static String PREF_ENABLE_RAW_LOG = "enable_raw_log";
    final static String PREF_LANGUAGE = "language";

    public static AppPreferences getInstance() {
        return ourInstance;
    }

    private SharedPreferences mPreferences;

    private AppPreferences() {
    }

    public void init(Context context) {
        mPreferences = context.getSharedPreferences(FILE_PREF_NAME, Context.MODE_PRIVATE);
        PreferenceManager.setDefaultValues(context, FILE_PREF_NAME, Context.MODE_PRIVATE, R.xml.pref, false);
    }

    public String getAcousticModelPath() {
        return mPreferences.getString(PREF_SPHINX_PATH, null);
    }

    public void setAcousticModelPath(String path) {
        setPreference(PREF_SPHINX_PATH, path);
    }

    public String getKeyphrase() {
        return mPreferences.getString(PREF_KEYPHRASE, null);
    }

    public void setKeyphrase(String keyphrase) {
        setPreference(PREF_KEYPHRASE, keyphrase);
    }

    public String getLanguage() {
        return mPreferences.getString(PREF_LANGUAGE, null);
    }

    public void setLanguage(String language) {
        setPreference(PREF_LANGUAGE, language);
    }

    @SuppressLint("ApplySharedPref")
    public void commitLanguage(String language) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_LANGUAGE, language);
        editor.commit();
    }

    public Integer getTimeoutRecognition() {
        return Integer.parseInt(mPreferences.getString(PREF_TIMEOUT_RECOGNITION, null));
    }

    public void setTimeoutRecognition(Integer timeoutRecognition) {
        setPreference(PREF_TIMEOUT_RECOGNITION, timeoutRecognition);
    }

    public Integer getSampleRate() {
        return Integer.parseInt(mPreferences.getString(PREF_SAMPLE_RATE, null));
    }

    public void setSampleRate(Integer sampleRate) {
        setPreference(PREF_SAMPLE_RATE, sampleRate);
    }

    public Boolean getVibroRecognized() {
        return mPreferences.getBoolean(PREF_VIBRO_RECOGNIZED, false);
    }

    public void setVibroRecognized(Boolean vibroRecognized) {
        setPreference(PREF_VIBRO_RECOGNIZED, vibroRecognized);
    }

    public Boolean getSoundRecognized() {
        return mPreferences.getBoolean(PREF_SOUND_RECOGNIZED, false);
    }

    public void setSoundRecognized(Boolean soundRecognized) {
        setPreference(PREF_SOUND_RECOGNIZED, soundRecognized);
    }

    public Boolean getRemoveNoise() {
        return mPreferences.getBoolean(PREF_REMOVE_NOISE, false);
    }

    public void setRemoveNoise(Boolean removeNoise) {
        setPreference(PREF_REMOVE_NOISE, removeNoise);
    }

    public Boolean getEnableRawLog() {
        return mPreferences.getBoolean(PREF_ENABLE_RAW_LOG, false);
    }

    public void setEnableRawLog(Boolean enableRawLog) {
        setPreference(PREF_ENABLE_RAW_LOG, enableRawLog);
    }

    private void setPreference(String key, Object value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else {
            editor.putString(key, value.toString());
        }
        editor.apply();
    }
}
