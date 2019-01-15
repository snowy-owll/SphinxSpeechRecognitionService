package ru.snowy_owl.testservicesphinx.services;

import android.app.Service;
import android.content.Context;

import ru.snowy_owl.testservicesphinx.helpers.LocaleHelper;

public abstract class LocalizedService extends Service {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapWithLocale(newBase));
    }
}
