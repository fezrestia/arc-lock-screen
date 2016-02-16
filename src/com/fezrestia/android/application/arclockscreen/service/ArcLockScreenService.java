package com.fezrestia.android.application.arclockscreen.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.fezrestia.android.application.arclockscreen.ArcLockScreenApplication;
import com.fezrestia.android.application.arclockscreen.controller.ArcLockScreenController;

public class ArcLockScreenService extends Service {
    // Local receiver.
    ScreenOnReceiver screenOnReceiver = new ScreenOnReceiver();

    @Override
    public IBinder onBind(Intent intent) {
        // NOP.
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (intent == null) {
            // NOP. This service may be re-started.
android.util.Log.e("TraceLog", "### ArcLockScreenService.onStart():[IN] [Intent==null]");
            ArcLockScreenController.getInstance().disableKeyguard(this);
            return;
        }
android.util.Log.e("TraceLog", "### ArcLockScreenService.onStart():[IN] [ACTION=" + intent.getAction() + "]");

        // Get preference.
        SharedPreferences sp = getSharedPreferences(
                ArcLockScreenApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean isEnabled = sp.getBoolean(
                ArcLockScreenApplication.KEY_IS_ENABLED, false);

        if (!isEnabled) {
            // NOP.
            return;
        }

        if (isEnabled) {
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action)) {

                // Notify.
                ArcLockScreenController.getInstance().onScreenOn();

            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                ArcLockScreenController.getInstance().disableKeyguard(this);

                // Enable extended key guard screen.
                ArcLockScreenController.getInstance().lock(ArcLockScreenService.this);

                // Notify.
                ArcLockScreenController.getInstance().onScreenOff();

            } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                // Vanilla keyguard is always disabled to handle key event in device sleep.
                ArcLockScreenController.getInstance().disableKeyguard(this);
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                // Lockscreen is still available.
                ArcLockScreenController.getInstance().disableKeyguard(this);
            } else {
                // NOP. Unexpected sequence.
            }

        } else {
            // NOP. Unexpected sequence.
            return;
        }

        // Register SCREEN_ON receiver.
//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        this.getApplicationContext().registerReceiver(screenOnReceiver, filter);
    }

    class ScreenOnReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
android.util.Log.e("TraceLog", "### ArcLockScreenService.ScreenOnReceiver.onReceive():[IN] [ACTION=" + intent.getAction() + "]");
            // Unlock standard keyguard.
            ArcLockScreenController.getInstance().disableKeyguard(ArcLockScreenService.this);

            // Enable extended key guard screen.
            ArcLockScreenController.getInstance().lock(ArcLockScreenService.this);

            // Unregister SCREEN_ON receiver.
            context.getApplicationContext().unregisterReceiver(screenOnReceiver);
        }
    }
}
