package ru.snowy_owl.testservicesphinx.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import ru.snowy_owl.testservicesphinx.Consts;
import ru.snowy_owl.testservicesphinx.LastMessagesList;
import ru.snowy_owl.testservicesphinx.R;
import ru.snowy_owl.testservicesphinx.helpers.PermissionsHelper;
import ru.snowy_owl.testservicesphinx.preferences.AppPreferences;
import ru.snowy_owl.testservicesphinx.services.SphinxService;

public class MainActivity extends LocalizedActivity {

    private final static int LAST_MESSAGES_LIST_SIZE = 15;

    private Button mBtnManageService;
    private TextView mTxtViewServiceState;
    private BroadcastReceiver mBr;
    private LastMessagesList mLastMessagesList;
    private ScrollView mScrollViewLastMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScrollViewLastMessages = findViewById(R.id.scrollView_lastMessages);

        mLastMessagesList = new LastMessagesList(LAST_MESSAGES_LIST_SIZE);

        mBtnManageService = findViewById(R.id.btn_manageService);
        mBtnManageService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSphinxServiceState()) {
                    stopService(new Intent(MainActivity.this, SphinxService.class));
                    setServiceStateToUI(false);
                    return;
                }
                if (PermissionsHelper.checkExternalStoragePermission(MainActivity.this)
                        && PermissionsHelper.checkRecordAudioPermission(MainActivity.this)) {
                    startService(new Intent(MainActivity.this, SphinxService.class));
                    setServiceStateToUI(true);
                } else {
                    PermissionsHelper.requestAllNeededPermissions(MainActivity.this);
                }
            }
        });

        final TextView txtView_lastData = findViewById(R.id.txtView_lastData);
        final TextView txtView_lastMessages = findViewById(R.id.txtView_lastMessages);
        final TextView txtView_serviceStateDetail = findViewById(R.id.txtView_serviceStateDetail);

        //TODO: move BroadcastReceiver into separate class
        mBr = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra(Consts.BROADCAST_PARAM_STATUS);
                String data = intent.getStringExtra(Consts.BROADCAST_PARAM_DATA);

                StringBuilder message = new StringBuilder();
                DateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                message.append(df.format(Calendar.getInstance().getTime()));
                String broadcastLabel = getString(Consts.BROADCAST_STATUS_LABELS.get(status));
                message.append(" - ").append(broadcastLabel);
                switch (status) {
                    case Consts.BROADCAST_STATUS_STOP:
                        setServiceStateToUI(false);
                    case Consts.BROADCAST_STATUS_START_INIT:
                    case Consts.BROADCAST_STATUS_START_RECOGNIZE_COMMAND:
                    case Consts.BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE:
                        txtView_serviceStateDetail.setText(broadcastLabel);
                        break;
                    case Consts.BROADCAST_STATUS_INIT_COMPLETE:
                        Toast.makeText(MainActivity.this,
                                R.string.service_ready_for_recognition, Toast.LENGTH_LONG).show();
                        break;
                    case Consts.BROADCAST_STATUS_KEYPHRASE_RECOGNIZED:
                        notifyUser();
                        break;
                    case Consts.BROADCAST_STATUS_ERROR_INIT:
                        Toast.makeText(MainActivity.this,
                                String.format(getString(R.string.error_occurred_while_initializing_recognition), data),
                                Toast.LENGTH_LONG).show();
                        txtView_lastData.setText(data);
                        message.append(" (").append(data).append(")");
                        break;
                    case Consts.BROADCAST_STATUS_COMMAND_RECOGNIZED:
                        notifyUser();
                        txtView_lastData.setText(data);
                        float confidence = intent.getFloatExtra(Consts.BROADCAST_PARAM_CONFIDENCE, 0);
                        message.append(" (").append(data).append(" | ").append(confidence).append(")");
                        break;
                }
                mLastMessagesList.add(message.toString());
                txtView_lastMessages.setText(mLastMessagesList.toString());
                scrollViewToBottom();
            }
        };
        IntentFilter filter = new IntentFilter(Consts.BROADCAST_ACTION);
        registerReceiver(mBr, filter);

        mTxtViewServiceState = findViewById(R.id.txtView_serviceState);

        setServiceStateToUI(getSphinxServiceState());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_settings:
                Intent intent = new Intent(this, ServicePreferenceActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionsHelper.REQUEST_ALL_NEEDED_PERMISSIONS) {
            if (grantResults.length > 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, SphinxService.class));
                setServiceStateToUI(true);
            } else {
                Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void setServiceStateToUI(boolean state) {
        int btnTextId;
        int txtViewId;
        if (state) {
            btnTextId = R.string.stop_service;
            txtViewId = R.string.service_on;
        } else {
            btnTextId = R.string.start_service;
            txtViewId = R.string.service_off;
        }
        mBtnManageService.setText(btnTextId);
        mTxtViewServiceState.setText(txtViewId);
    }

    private boolean getSphinxServiceState() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : am.getRunningServices(Integer.MAX_VALUE)) {
            if (SphinxService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void notifyUser() {
        if (AppPreferences.getInstance().getSoundRecognized()) {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
            toneGenerator.release();
        }
        if (AppPreferences.getInstance().getVibroRecognized()) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(400);
            }
        }
    }

    private void scrollViewToBottom() {
        mScrollViewLastMessages.post(new Runnable() {
            @Override
            public void run() {
                mScrollViewLastMessages.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
