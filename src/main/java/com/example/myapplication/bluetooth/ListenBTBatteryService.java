package com.example.myapplication.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ListenBTBatteryService extends Service {
    private BTBatteryLevelManager bblManager;
    private static final String BTAddress = "4C:3D:6F";
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
        bblManager = new BTBatteryLevelManager();
        bblManager.startGetBTBattery(BTAddress);
    }
    @Override
    public void onDestroy() {
        bblManager.stopGetBTBattery();
    }

}
