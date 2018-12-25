package ru.snowy_owl.testservicesphinx;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import com.ls.directoryselector.DirectoryPreference;

import static android.content.Context.MODE_PRIVATE;
import static ru.snowy_owl.testservicesphinx.Consts.*;

public class ServicePreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private DirectoryPreference _pref_pathToAmModel;
    private EditTextPreference _pref_keyphrase;
    private EditTextPreference _pref_timeoutRecognition;
    private EditTextPreference _pref_sampleRate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName(FILE_PREF_NAME);

        addPreferencesFromResource(R.xml.pref);

        PreferenceScreen screen = getPreferenceScreen();

        _pref_pathToAmModel = (DirectoryPreference) screen.findPreference(PREF_SPHINX_PATH);
        String value = screen.getSharedPreferences().getString(PREF_SPHINX_PATH,DEFAULT_SPHINX_PATH);
        _pref_pathToAmModel.setSummary(value);

        _pref_keyphrase = (EditTextPreference)screen.findPreference(PREF_KEYPHRASE);
        value = screen.getSharedPreferences().getString(PREF_KEYPHRASE,DEFAULT_KEYPHRASE);
        _pref_keyphrase.setSummary(value);
        _pref_keyphrase.setText(value);
        _pref_keyphrase.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = false;
                if(s.toString().equals("")){
                    isEmpty=true;
                }
                Dialog dialog=_pref_keyphrase.getDialog();
                if(dialog instanceof AlertDialog){
                    Button button= ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setEnabled(!isEmpty);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //TODO: перенести реализацию диалога ввода численных параметров в отдельный класс
        _pref_timeoutRecognition = (EditTextPreference)screen.findPreference(PREF_TIMEOUT_RECOGNITION);
        value = screen.getSharedPreferences()
                .getString(PREF_TIMEOUT_RECOGNITION, DEFAULT_TIMEOUT_RECOGNITION);
        _pref_timeoutRecognition.setSummary(value);
        _pref_timeoutRecognition.setText(value);
        _pref_timeoutRecognition.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = false;
                if(s.toString().equals("") || ( _isNumeric(s.toString()) && Integer.parseInt(s.toString())==0)){
                    isEmpty=true;
                }
                Dialog dialog=_pref_timeoutRecognition.getDialog();
                if(dialog instanceof AlertDialog){
                    Button button= ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setEnabled(!isEmpty);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        _pref_sampleRate=(EditTextPreference)screen.findPreference(PREF_SAMPLE_RATE);
        value=screen.getSharedPreferences().getString(PREF_SAMPLE_RATE,DEFAULT_SAMPLE_RATE);
        _pref_sampleRate.setSummary(value);
        _pref_sampleRate.setText(value);
        _pref_sampleRate.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = false;
                if(s.toString().equals("") || ( _isNumeric(s.toString()) && Integer.parseInt(s.toString())==0)){
                    isEmpty=true;
                }
                Dialog dialog=_pref_sampleRate.getDialog();
                if(dialog instanceof AlertDialog){
                    Button button= ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setEnabled(!isEmpty);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        CheckBoxPreference checkBoxPreference = (CheckBoxPreference)
                screen.findPreference(PREF_VIBRO_RECOGNIZED);
        boolean b = screen.getSharedPreferences().getBoolean(PREF_VIBRO_RECOGNIZED, DEFAULT_VIBRO_RECOGNIZED);
        checkBoxPreference.setChecked(b);

        checkBoxPreference = (CheckBoxPreference)
            screen.findPreference(PREF_SOUND_RECOGNIZED);
        b = screen.getSharedPreferences().getBoolean(PREF_SOUND_RECOGNIZED, DEFAULT_SOUND_RECOGNIZER);
        checkBoxPreference.setChecked(b);

        checkBoxPreference = (CheckBoxPreference) screen.findPreference(PREF_REMOVE_NOISE);
        b = screen.getSharedPreferences().getBoolean(PREF_REMOVE_NOISE,DEFAULT_REMOVE_NOISE);
        checkBoxPreference.setChecked(b);

        checkBoxPreference = (CheckBoxPreference) screen.findPreference(PREF_ENABLE_RAW_LOG);
        b = screen.getSharedPreferences().getBoolean(PREF_ENABLE_RAW_LOG,DEFAULT_ENABLE_RAW_LOG);
        checkBoxPreference.setChecked(b);

        Preference prefVersion = screen.findPreference("version");
        String version = "";
        try {
            version = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        prefVersion.setSummary(version);
    }

    @Override
    public void onStart(){
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case PREF_SPHINX_PATH:
                String path = sharedPreferences.getString(PREF_SPHINX_PATH, DEFAULT_SPHINX_PATH);
                _pref_pathToAmModel.setSummary(path);
                break;
            case  PREF_KEYPHRASE:
                String keyphrase = sharedPreferences.getString(PREF_KEYPHRASE, DEFAULT_KEYPHRASE);
                _pref_keyphrase.setSummary(keyphrase);
                break;
            case PREF_TIMEOUT_RECOGNITION:
                String timeout = sharedPreferences
                        .getString(PREF_TIMEOUT_RECOGNITION, DEFAULT_TIMEOUT_RECOGNITION);
                _pref_timeoutRecognition.setSummary(timeout);
                break;
            case PREF_SAMPLE_RATE:
                String sample_rate = sharedPreferences.getString(PREF_SAMPLE_RATE, DEFAULT_SAMPLE_RATE);
                _pref_sampleRate.setSummary(sample_rate);
        }
    }

    private boolean _isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }
}
