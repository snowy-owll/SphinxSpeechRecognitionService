package ru.snowy_owl.testservicesphinx.activities;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import ru.snowy_owl.testservicesphinx.helpers.LocaleHelper;

public abstract class LocalizedActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapWithLocale(newBase));
    }
}
