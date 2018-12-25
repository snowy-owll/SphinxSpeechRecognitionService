package ru.snowy_owl.testservicesphinx;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.*;
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
import java.util.HashMap;
import java.util.Locale;

import static ru.snowy_owl.testservicesphinx.Consts.*;

public class MainActivity extends Activity {

    private Button mBtnManageService;
    private TextView mTxtViewServiceState;
    private SharedPreferences mPref;
    private BroadcastReceiver mBr;
    private LastMessagesList mLastMessagesList;
    private HashMap<String, Integer> mBroadcastStatusLabels;
    private ScrollView mScrollViewLastMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScrollViewLastMessages = (ScrollView) findViewById(R.id.scrollView_lastMessages);

        mBroadcastStatusLabels = BroadcastStatusLabels;
        mLastMessagesList = new LastMessagesList(Consts.LAST_MESSAGES_LIST_SIZE);

        mPref = getSharedPreferences(FILE_PREF_NAME, MODE_PRIVATE);

        mBtnManageService = (Button) findViewById(R.id.btn_manageService);
        mBtnManageService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSphinxServiceState()) {
                    stopService(new Intent(MainActivity.this, SphinxService.class));
                    setServiceStateToUI(false);
                    return;
                }
                startService(new Intent(MainActivity.this, SphinxService.class));
                setServiceStateToUI(true);
            }
        });

        final TextView txtView_lastData = (TextView) findViewById(R.id.txtView_lastData);
        final TextView txtView_lastMessages = (TextView) findViewById(R.id.txtView_lastMessages);
        final TextView txtView_serviceStateDetail = (TextView) findViewById(R.id.txtView_serviceStateDetail);

        mBr = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra(BROADCAST_PARAM_STATUS);
                String data = intent.getStringExtra(BROADCAST_PARAM_DATA);

                StringBuilder message = new StringBuilder();
                DateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                message.append(df.format(Calendar.getInstance().getTime()));
                String broadcastLabel = getResources().getString(
                        mBroadcastStatusLabels.get(status));
                message.append(" - ").append(broadcastLabel);
                switch (status) {
                    case BROADCAST_STATUS_STOP:
                        setServiceStateToUI(false);
                    case BROADCAST_STATUS_START_INIT:
                    case BROADCAST_STATUS_START_RECOGNIZE_COMMAND:
                    case BROADCAST_STATUS_START_RECOGNIZE_KEYPHRASE:
                        txtView_serviceStateDetail.setText(broadcastLabel);
                        break;
                    case BROADCAST_STATUS_INIT_COMPLETE:
                        Toast.makeText(MainActivity.this,
                                "Сервис готов к распознаванию", Toast.LENGTH_LONG).show();
                        break;
                    case BROADCAST_STATUS_KEYPHRASE_RECOGNIZED:
                        notifyUser();
                        break;
                    case BROADCAST_STATUS_ERROR_INIT:
                        Toast.makeText(MainActivity.this,
                                "Произошла ошибка при инициализации распознавания: " + data,
                                Toast.LENGTH_LONG).show();
                        txtView_lastData.setText(data);
                        message.append(" (").append(data).append(")");
                        break;
                    case BROADCAST_STATUS_COMMAND_RECOGNIZED:
                        notifyUser();
                        txtView_lastData.setText(data);
                        float confidence = intent.getFloatExtra(BROADCAST_PARAM_CONFIDENCE, 0);
                        message.append(" (").append(data).append(" | ").append(confidence).append(")");
                        break;
                }
                mLastMessagesList.add(message.toString());
                txtView_lastMessages.setText(mLastMessagesList.toString());
                scrollViewToBottom();
            }
        };
        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(mBr, filter);

        mTxtViewServiceState = (TextView) findViewById(R.id.txtView_serviceState);

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
        if (mPref.getBoolean(PREF_SOUND_RECOGNIZED, DEFAULT_SOUND_RECOGNIZER)) {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
            toneGenerator.release();
        }
        if (mPref.getBoolean(PREF_VIBRO_RECOGNIZED, DEFAULT_VIBRO_RECOGNIZED)) {
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
