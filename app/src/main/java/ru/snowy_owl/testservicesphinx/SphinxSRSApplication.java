package ru.snowy_owl.testservicesphinx;

import android.app.Application;

import ru.snowy_owl.testservicesphinx.preferences.AppPreferences;

public class SphinxSRSApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppPreferences.getInstance().init(this);
    }
}
