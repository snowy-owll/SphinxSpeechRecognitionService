package ru.snowy_owl.testservicesphinx.preferences;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.ls.directoryselector.DirectoryPreference;
import com.ls.directoryselector.DirectoryPreferenceDialogFragmentCompat;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import ru.snowy_owl.testservicesphinx.Consts;
import ru.snowy_owl.testservicesphinx.R;
import ru.snowy_owl.testservicesphinx.activities.MainActivity;
import ru.snowy_owl.testservicesphinx.helpers.PermissionsHelper;

public class ServicePreferenceFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";

    private DirectoryPreference mPrefPathToAmModel;
    private NotEmptyEditTextPreference mPrefKeyphrase;
    private NumberPreference mPrefTimeoutRecognition;
    private NumberPreference mPrefSampleRate;
    private ListPreference mPrefLanguage;
    private AppPreferences mAppPreferences;
    private String mPreferenceDialogKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppPreferences = AppPreferences.getInstance();

        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName(AppPreferences.FILE_PREF_NAME);

        addPreferencesFromResource(R.xml.pref);

        PreferenceScreen screen = getPreferenceScreen();

        mPrefPathToAmModel = screen.findPreference(AppPreferences.PREF_SPHINX_PATH);
        String value = mAppPreferences.getAcousticModelPath();
        mPrefPathToAmModel.setSummary(value);

        mPrefKeyphrase = screen.findPreference(AppPreferences.PREF_KEYPHRASE);
        value = mAppPreferences.getKeyphrase();
        mPrefKeyphrase.setSummary(value);
        mPrefKeyphrase.setText(value);

        mPrefTimeoutRecognition = screen.findPreference(AppPreferences.PREF_TIMEOUT_RECOGNITION);
        value = mAppPreferences.getTimeoutRecognition().toString();
        mPrefTimeoutRecognition.setSummary(value);
        mPrefTimeoutRecognition.setText(value);

        mPrefSampleRate = screen.findPreference(AppPreferences.PREF_SAMPLE_RATE);
        value = mAppPreferences.getSampleRate().toString();
        mPrefSampleRate.setSummary(value);
        mPrefSampleRate.setText(value);

        CheckBoxPreference checkBoxPreference = screen.findPreference(AppPreferences.PREF_VIBRO_RECOGNIZED);
        boolean b = mAppPreferences.getVibroRecognized();
        checkBoxPreference.setChecked(b);

        checkBoxPreference = screen.findPreference(AppPreferences.PREF_SOUND_RECOGNIZED);
        b = mAppPreferences.getSoundRecognized();
        checkBoxPreference.setChecked(b);

        checkBoxPreference = screen.findPreference(AppPreferences.PREF_REMOVE_NOISE);
        b = mAppPreferences.getRemoveNoise();
        checkBoxPreference.setChecked(b);

        checkBoxPreference = screen.findPreference(AppPreferences.PREF_ENABLE_RAW_LOG);
        b = mAppPreferences.getEnableRawLog();
        checkBoxPreference.setChecked(b);

        mPrefLanguage = screen.findPreference(AppPreferences.PREF_LANGUAGE);
        String lang = mAppPreferences.getLanguage();
        int langIndex = Consts.SUPPORTED_LANGUAGES.indexOf(lang);
        mPrefLanguage.setSummary(getResources()
                .getStringArray(R.array.language_list_labels)[langIndex]);
        mPrefLanguage.setValueIndex(langIndex);

        Preference prefVersion = screen.findPreference("version");
        String version = "";
        try {
            version = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(Consts.LOG_TAG, e.getMessage());
        }
        prefVersion.setSummary(version);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof NumberPreference) {
            dialogFragment = NumberPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        } else if (preference instanceof NotEmptyEditTextPreference) {
            dialogFragment = NotEmptyEditTextPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        } else if (preference instanceof DirectoryPreference) {
            if (PermissionsHelper.checkExternalStoragePermission(getContext())) {
                dialogFragment = DirectoryPreferenceDialogFragmentCompat
                        .newInstance(preference.getKey());
            } else {
                mPreferenceDialogKey = preference.getKey();
                PermissionsHelper.requestExternalStoragePermission(this);
                return;
            }
        }
        if (dialogFragment != null) {
            showPreferenceDialog(dialogFragment);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case AppPreferences.PREF_SPHINX_PATH:
                String path = mAppPreferences.getAcousticModelPath();
                mPrefPathToAmModel.setSummary(path);
                break;
            case AppPreferences.PREF_KEYPHRASE:
                String keyphrase = mAppPreferences.getKeyphrase();
                mPrefKeyphrase.setSummary(keyphrase);
                break;
            case AppPreferences.PREF_TIMEOUT_RECOGNITION:
                String timeout = mAppPreferences.getTimeoutRecognition().toString();
                mPrefTimeoutRecognition.setSummary(timeout);
                break;
            case AppPreferences.PREF_SAMPLE_RATE:
                String sample_rate = mAppPreferences.getSampleRate().toString();
                mPrefSampleRate.setSummary(sample_rate);
            case AppPreferences.PREF_LANGUAGE:
                String lang = mAppPreferences.getLanguage();
                int langIndex = Consts.SUPPORTED_LANGUAGES.indexOf(lang);
                mPrefLanguage.setSummary(getResources()
                        .getStringArray(R.array.language_list_labels)[langIndex]);

                mAppPreferences.commitLanguage(lang);

                restartApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionsHelper.REQUEST_WRITE_STORAGE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (mPreferenceDialogKey == null) {
                    return;
                }
                showPreferenceDialog(
                        DirectoryPreferenceDialogFragmentCompat.newInstance(mPreferenceDialogKey));
                mPreferenceDialogKey = null;
            }
        }
    }

    private void restartApp() {
        Intent intent = new Intent(this.getActivity(), MainActivity.class);
        ComponentName cn = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(cn);
        startActivity(mainIntent);
        System.exit(0);
    }

    private void showPreferenceDialog(DialogFragment dialogFragment) {
        dialogFragment.setTargetFragment(this, 0);
        dialogFragment.show(this.getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }
}
