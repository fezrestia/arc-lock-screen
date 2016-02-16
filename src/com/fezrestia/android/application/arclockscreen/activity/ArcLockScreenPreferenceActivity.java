package com.fezrestia.android.application.arclockscreen.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.fezrestia.android.application.arclockscreen.ArcLockScreenApplication;
import com.fezrestia.android.application.arclockscreen.R;
import com.fezrestia.android.application.arclockscreen.controller.ArcLockScreenController;

public class ArcLockScreenPreferenceActivity extends Activity
        implements
                OnClickListener,
                OnCheckedChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout.
        setContentView(R.layout.arc_lock_screen_preference);

        // Add on click listener.
        findViewById(R.id.button_finish).setOnClickListener(this);
        findViewById(R.id.button_test).setOnClickListener(this);

        // Set check box.
        SharedPreferences sp = getSharedPreferences(
                ArcLockScreenApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        CheckBox checkIsEnabled = (CheckBox) 
                findViewById(R.id.check_is_enabled);
        checkIsEnabled.setChecked(sp.getBoolean(
                ArcLockScreenApplication.KEY_IS_ENABLED, false));
        CheckBox checkIsRapidBootEnabled = (CheckBox)
                findViewById(R.id.check_is_rapid_boot_enabled);
        checkIsRapidBootEnabled.setChecked(sp.getBoolean(
                ArcLockScreenApplication.KEY_IS_RAPID_BOOT_ENABLED, false));

        // Set on check listener.
        checkIsEnabled.setOnCheckedChangeListener(this);
        checkIsRapidBootEnabled.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_finish:
                finish();
                break;

            case R.id.button_test:
                ArcLockScreenController.getInstance().lock(getApplicationContext());
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Get editor.
        SharedPreferences sp = getSharedPreferences(
                ArcLockScreenApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();

        // Write setting.
        switch (buttonView.getId()) {
            case R.id.check_is_enabled:
                editor.putBoolean(ArcLockScreenApplication.KEY_IS_ENABLED, isChecked);
                editor.apply();
                break;

            case R.id.check_is_rapid_boot_enabled:
                editor.putBoolean(ArcLockScreenApplication.KEY_IS_RAPID_BOOT_ENABLED, isChecked);
                editor.apply();
                break;
        }
    }
}
