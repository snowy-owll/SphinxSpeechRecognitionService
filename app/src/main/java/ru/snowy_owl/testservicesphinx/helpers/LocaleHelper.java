package ru.snowy_owl.testservicesphinx.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

import ru.snowy_owl.testservicesphinx.preferences.AppPreferences;

import static ru.snowy_owl.testservicesphinx.Consts.SUPPORTED_LANGUAGES;

public class LocaleHelper {
    public static ContextWrapper wrapWithLocale(Context context) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();
        String lang = configuration.locale.getLanguage();
        String prefLang = AppPreferences.getInstance().getLanguage();
        if ("".equals(prefLang)) {
            if (SUPPORTED_LANGUAGES.indexOf(lang) > -1) {
                AppPreferences.getInstance().setLanguage(lang);
            } else {
                AppPreferences.getInstance().setLanguage("en");
            }
            prefLang = lang;
        }
        Locale newLocale = new Locale(prefLang);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale);
            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context = context.createConfigurationContext(configuration);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(newLocale);
            context = context.createConfigurationContext(configuration);
        } else {
            configuration.locale = newLocale;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }
        return new ContextWrapper(context);
    }
}
