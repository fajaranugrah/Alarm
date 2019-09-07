package com.example.fajarramadhan.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "ALARM!! ALARM!!", Toast.LENGTH_SHORT).show();

        //Stop sound service to play sound for alarm
        if (Build.VERSION.SDK_INT >= 26){
            try {
                context.startForegroundService(new Intent(context, AlarmSoundService.class));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        } else {
            context.startService(new Intent(context, AlarmSoundService.class));
        }

        //This will send a notification message and show notification in notification tray
        ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmNotificationService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));

    }
}
