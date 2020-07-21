package com.example.myapplication.bluetooth;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.myapplication.R;
import com.example.myapplication.StartActivity;
import com.example.myapplication.wifi.WifiConnectionEventManager;

public class ListenBTBatteryService extends Service {
    private BTBatteryLevelManager bblManager;
    private static final String BTName = "Motobuds";
    private NotificationManager notificationManager;
    private int NOTIFICATION = 3;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //startService will call onStartCommand again but won't call onCreate
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        bblManager = new BTBatteryLevelManager();
        showNotification("Listen BT Battery...");
        bblManager.startGetBTBattery(BTName);
    }
    private void showNotification(String db){
        PendingIntent pendingIntent = new NavDeepLinkBuilder(this).setComponentName(StartActivity.class).setGraph(R.navigation.mobile_navigation).setDestination(R.id.nav_bt_battery).createPendingIntent();
        NotificationChannel mChannel = new NotificationChannel("mychannel3","mychannel3", NotificationManager.IMPORTANCE_HIGH);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"mychannel3").setContentTitle("Listening...").setContentText(db)
                .setSmallIcon(R.drawable.ic_launcher_background).setContentIntent(pendingIntent);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.notify(NOTIFICATION,builder.build());
        startForeground(NOTIFICATION,builder.build());
    }
    @Override
    public void onDestroy() {
        bblManager.stopGetBTBattery();
        if(notificationManager!=null)
            notificationManager.cancel(NOTIFICATION);
    }

}
