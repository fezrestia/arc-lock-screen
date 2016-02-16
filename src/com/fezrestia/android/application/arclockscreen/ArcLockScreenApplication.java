package com.fezrestia.android.application.arclockscreen;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.fezrestia.android.application.arclockscreen.receiver.ArcLockScreenReceiver;

public class ArcLockScreenApplication extends Application {
    private ArcLockScreenReceiver mReceiver = null;

    // Shared preferences keys.
    public static final String SHARED_PREFERENCES_NAME
            = "sp-arclockscreen";
    public static final String KEY_IS_ENABLED
            = "key-is-enabled";
    public static final String KEY_IS_RAPID_BOOT_ENABLED
            = "key-is-rapid-boot-enabled";

    @Override
    public void onCreate() {
        super.onCreate();

        // Filter to detect screen off.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        // Register.
        mReceiver = new ArcLockScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // Unregister.
        unregisterReceiver(mReceiver);
    }
}
