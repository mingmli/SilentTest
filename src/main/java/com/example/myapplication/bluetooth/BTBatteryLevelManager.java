package com.example.myapplication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.example.myapplication.Constant;
import com.example.myapplication.MyUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;

public class BTBatteryLevelManager {
    private static final String TAG = "Silent_BTBatteryLevelManager";
    private Context mContext;
    private boolean isGetBTBatteryRun = false;
    private Thread mThread;
    private Object mLock;
    IntentFilter mFilter;
    private static final int interval = 60000;
    private static final String path = "/storage/emulated/0/BatteryRecord.txt";
    public BTBatteryLevelManager() {
        mLock = new Object();
    }

    public void stopGetBTBattery(){
        isGetBTBatteryRun = false;
    }

    public boolean isStartGetBTBattery(){
        return isGetBTBatteryRun;
    }

    public void startGetBTBattery(final String address) {
        Log.i(TAG, "startRecordBTDeviceBattery");
        if (isGetBTBatteryRun) {
            Log.e(TAG, "isGetBTBatteryRun:" + isGetBTBatteryRun);
            return;
        }
        isGetBTBatteryRun = true;

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGetBTBatteryRun) {
                    getBluetoothDeviceBattery(address);
                    // check once per 10min
                    synchronized (mLock) {
                        try {
                            mLock.wait(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mThread.start();
    }

    private int getBluetoothDeviceBattery(String address){
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        int level = -1;
        //获取BluetoothAdapter的Class对象
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        try {
            //反射获取蓝牙连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开使用这个方法的权限
            method.setAccessible(true);
            int state = (int) method.invoke(btAdapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {
                //获取在系统蓝牙的配对列表中的设备--！已连接设备包含在其中
                Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
                for (BluetoothDevice device : devices) {
                    String deviceAd = device.getAddress();
                    if(!address.equals(deviceAd)) continue;
                    Method batteryMethod = BluetoothDevice.class.getDeclaredMethod("getBatteryLevel", (Class[]) null);
                    batteryMethod.setAccessible(true);
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    isConnectedMethod.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    level = (int) batteryMethod.invoke(device, (Object[]) null);
                    if (device != null && level > 0 && isConnected) {
                        String deviceName = device .getName();
                        Log.d(TAG, deviceName + "    Battery:  " + level);
                        //Write it to SDCARD /storage/emulated/0
                        String tmp = "time:"+new Date(System.currentTimeMillis())+" battery:"+level;
                        MyUtils.writeFileData(path,tmp);
                    }else if(!isConnected){
                        //disconnected
                        String tmp = "time:"+new Date(System.currentTimeMillis())+" Disconnected";
                        MyUtils.writeFileData(path,tmp);
                    }
                }
            } else {
                //No connected device, stop
                String tmp = "time:"+new Date(System.currentTimeMillis())+" Disconnected";
                MyUtils.writeFileData(path,tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return level;
    }
}

