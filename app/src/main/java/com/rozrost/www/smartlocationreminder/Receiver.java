package com.rozrost.www.smartlocationreminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class Receiver extends Activity {
    private MediaPlayer mMediaPlayer;
    Button stopAlarm, snoozeAlarm;
    private PowerManager.WakeLock mWakeLock;
    String primaryKey;
    TextView tv,TVtaskview;
    DatabaseHelper mDatabaseHelper;
    private Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKELOCK");
        mWakeLock.acquire();

        setContentView(R.layout.activity_receiver);
        mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        testSound();
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = { 0, 200, 500};
        vib.vibrate(pattern,0);
        mDatabaseHelper = new DatabaseHelper(this);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            primaryKey = extras.getString("PrimaryKey");
            tv = (TextView) findViewById(R.id.titleTextView);
            tv.setText(mDatabaseHelper.getNameFromDatabase(primaryKey));
            TVtaskview = (TextView) findViewById(R.id.recieverTaskView);
            TVtaskview.setText(mDatabaseHelper.getTaskFromDatabase(primaryKey));
        }

        stopAlarm = (Button) findViewById(R.id.btnStopAlarm);
        snoozeAlarm = (Button) findViewById(R.id.snoozebutton);
        onStopClick();

        snoozeAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.setLooping(false);
                mMediaPlayer.stop();
                vib.cancel();
                finish();
            }
        });



    }

    public void onStopClick(){
        stopAlarm.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                vib.cancel();
                mDatabaseHelper.changeOnOffInDatabase(primaryKey, false);
                mMediaPlayer.setLooping(false);
                mMediaPlayer.stop();
                Intent newAppIntent;
                PackageManager packageManager = getPackageManager();
                newAppIntent = packageManager.getLaunchIntentForPackage("com.rozrost.www.smartlocationreminder");
                newAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(newAppIntent);
                finish();
            }
        });
    }

    private void testSound(){
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    protected void onStop(){
        super.onStop();
        vib.cancel();
        if (mWakeLock.isHeld())
            mWakeLock.release();
    }
}

