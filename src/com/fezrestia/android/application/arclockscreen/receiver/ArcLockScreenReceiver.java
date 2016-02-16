package com.fezrestia.android.application.arclockscreen.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fezrestia.android.application.arclockscreen.service.ArcLockScreenService;

public class ArcLockScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start service and send intent.
        Intent service = new Intent(context, ArcLockScreenService.class);
        service.setAction(intent.getAction());
        context.startService(service);
    }
}
