package com.fezrestia.android.application.arclockscreen.controller;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.fezrestia.android.application.arclockscreen.R;
import com.fezrestia.android.application.arclockscreen.view.ArcLockScreenBaseView;

public class ArcLockScreenController {
    // Master context.
    private static Context mContext;

    // Lock screen view.
    private ArcLockScreenBaseView mLockScreen = null;
    private WindowManager mWindowManager = null;

    private OnUnlockListener mUnlockListener;

    private static final int SCREEN_TIME_OUT = 60000;

    // Lock instance
    private static final ArcLockScreenController instance = new ArcLockScreenController();

    // Keyguard.
    private KeyguardManager mKeyguardManager;
    private KeyguardManager.KeyguardLock mKeyguardLock;

    // CONSTRUCTOR.
    private ArcLockScreenController() {
        // NOP.
    }

    public static synchronized ArcLockScreenController getInstance() {
        return instance;
    }

    public void lock(Context context) {
        if (mLockScreen != null) {
            // Device is already locked.
            // NOP.
            return;
        }

        // Store caller context.
        mContext = context;

        // Create lock screen view.
        mLockScreen = (ArcLockScreenBaseView)
                LayoutInflater.from(context).inflate(R.layout.arc_lock_screen, null);
        // Set unlock listener.
        mUnlockListener = new OnUnlockListener();
        mLockScreen.setOnUnlockedListener(mUnlockListener);

        // Set to Window.
        setLockScreenToWindow(context, mLockScreen);

        // Set screen timeout.
        setScreenBackLightTimeOut(0);
    }

    private void setLockScreenToWindow(Context context, ArcLockScreenBaseView lockScreen) {
        // Setup window parameters.
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.FILL_PARENT;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;

        // Add to WindowManager.
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        mWindowManager.addView(lockScreen, params);
    }

    public void unlock(){
        if (mLockScreen == null) {
            // Device is already unlocked.
            // NOP.
            return;
        }

        // Reset screen out timeout.
        setScreenBackLightTimeOut(SCREEN_TIME_OUT);

        // Release unlock listener.
        mLockScreen.setOnUnlockedListener(null);
        mUnlockListener = null;

        // Remove from WindowManager.
        mWindowManager.removeView(mLockScreen);
        mLockScreen = null;
    }

    private void setScreenBackLightTimeOut(int millisec) {
        // Reset screen out timeout.
        Settings.System.putInt(
                mContext.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT,
                millisec);
    }

    private class OnUnlockListener implements ArcLockScreenBaseView.OnUnlockedListener {
        @Override
        public void onUnlock() {
            unlock();
        }
    }

    public void disableKeyguard(Context context) {
        // Initialize.
        if (mKeyguardLock == null) {
            mKeyguardManager = (KeyguardManager)
                    context.getSystemService(Context.KEYGUARD_SERVICE);
            mKeyguardLock = mKeyguardManager.newKeyguardLock("ArcLockScreen");
        }

        // Disable key guard.
        mKeyguardLock.disableKeyguard();
    }

    public void enableKeyguard(Context context) {
        // Initialize.
        if (mKeyguardLock == null) {
            mKeyguardManager = (KeyguardManager)
                    context.getSystemService(Context.KEYGUARD_SERVICE);
            mKeyguardLock = mKeyguardManager.newKeyguardLock("ArcLockScreen");
        }

        // Enable key guard.
        mKeyguardLock.reenableKeyguard();
    }
}
