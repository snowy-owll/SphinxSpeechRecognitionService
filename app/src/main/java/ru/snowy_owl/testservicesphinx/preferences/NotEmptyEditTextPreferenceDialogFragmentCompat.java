package ru.snowy_owl.testservicesphinx.preferences;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;

public class NotEmptyEditTextPreferenceDialogFragmentCompat extends EditTextPreferenceDialogFragmentCompat {

    public static NotEmptyEditTextPreferenceDialogFragmentCompat newInstance(String key) {
        final NotEmptyEditTextPreferenceDialogFragmentCompat
                fragment = new NotEmptyEditTextPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        ((EditText) view.findViewById(android.R.id.edit)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AlertDialog dialog = (AlertDialog) NotEmptyEditTextPreferenceDialogFragmentCompat.
                        this.getDialog();
                if (dialog != null) {
                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setEnabled(!s.toString().equals(""));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
