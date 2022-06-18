package com.example.dz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RebootAppMonitor extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, AppMonitor.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
