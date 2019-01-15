package ru.snowy_owl.testservicesphinx.preferences;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

public class NumberPreferenceDialogFragmentCompat extends NotEmptyEditTextPreferenceDialogFragmentCompat {
    public static NumberPreferenceDialogFragmentCompat newInstance(String key) {
        final NumberPreferenceDialogFragmentCompat
                fragment = new NumberPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ((EditText) view.findViewById(android.R.id.edit)).setInputType(InputType.TYPE_CLASS_NUMBER);
    }
}
