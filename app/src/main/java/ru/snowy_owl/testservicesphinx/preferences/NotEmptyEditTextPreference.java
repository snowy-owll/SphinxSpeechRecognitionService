package ru.snowy_owl.testservicesphinx.preferences;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

public class NotEmptyEditTextPreference extends EditTextPreference {

    public NotEmptyEditTextPreference(Context context) {
        super(context);
    }

    public NotEmptyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotEmptyEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NotEmptyEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);


    }

}
