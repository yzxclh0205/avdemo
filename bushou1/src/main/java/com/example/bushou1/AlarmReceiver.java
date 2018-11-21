package com.example.bushou1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, LongRunningService.class));
            } else {
                context.startService(new Intent(context, LongRunningService.class));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}