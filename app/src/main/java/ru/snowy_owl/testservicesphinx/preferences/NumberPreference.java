package ru.snowy_owl.testservicesphinx.preferences;

import android.content.Context;
import android.util.AttributeSet;

public class NumberPreference extends NotEmptyEditTextPreference {
    public NumberPreference(Context context) {
        super(context);
    }

    public NumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NumberPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
