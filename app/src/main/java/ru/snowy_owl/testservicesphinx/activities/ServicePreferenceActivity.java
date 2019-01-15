package ru.snowy_owl.testservicesphinx.activities;

import android.os.Bundle;
import android.view.MenuItem;

import ru.snowy_owl.testservicesphinx.R;
import ru.snowy_owl.testservicesphinx.preferences.ServicePreferenceFragment;

public class ServicePreferenceActivity extends LocalizedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                new ServicePreferenceFragment()).commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
