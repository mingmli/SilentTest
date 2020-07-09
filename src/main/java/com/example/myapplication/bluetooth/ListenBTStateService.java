package com.example.myapplication.bluetooth;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.myapplication.PrefStatusUtil;
import com.example.myapplication.R;
import com.example.myapplication.StartActivity;
import com.example.myapplication.wifi.WifiConnectionEventManager;

public class ListenBTStateService extends Service {
    private static final String TAG = "Silent_ListenBTStateService";
    private NotificationManager notificationManager;
    private BluetoothConnectionEventManager mBTConnectionManager;
    private int NOTIFICATION = 2;

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
        super.onCreate();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mBTConnectionManager = new BluetoothConnectionEventManager(this);
        mBTConnectionManager.registerEvents();
        showNotification("Listen BT State...");
    }
    private void showNotification(String db){
        Bundle bundle = new Bundle();
        //bundle.putBoolean("isCheckBT",true);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = new NavDeepLinkBuilder(this).setComponentName(StartActivity.class).setGraph(R.navigation.mobile_navigation).setDestination(R.id.nav_bt_state).setArguments(bundle).createPendingIntent();
        NotificationChannel mChannel = new NotificationChannel("mychannel","mychannel", NotificationManager.IMPORTANCE_HIGH);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"mychannel").setContentTitle("Listening...").setContentText(db)
                .setSmallIcon(R.drawable.ic_launcher_background).setContentIntent(pendingIntent);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.notify(NOTIFICATION,builder.build());
        startForeground(NOTIFICATION,builder.build());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        PrefStatusUtil pf = new PrefStatusUtil(this);
        pf.putBooleanStatus("BTStatus",false);
        if(mBTConnectionManager!=null)
            mBTConnectionManager.cleanup();
        if(notificationManager!=null)
            notificationManager.cancel(NOTIFICATION);
    }

    public void onClearNotify(){
        stopForeground(true);
    }


}
