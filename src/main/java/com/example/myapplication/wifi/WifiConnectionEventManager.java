package com.example.myapplication.wifi;

import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.example.myapplication.MyUtils;

public class WifiConnectionEventManager {
    private static final String TAG = "Silent_WifiConnectionEventManager";

    private Handler mHandler = null;
    private long mLastGenerateTimestamp;
    IntentFilter mFilter;

    private Context mContext;

    public WifiConnectionEventManager(Context context) {
        mContext = context;
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public void registerEvents(){
        mContext.registerReceiver(mReceiver, mFilter);
    }

    // Broadcast receiver for all changes
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Log.e(TAG, "Received intent with null action");
                return;
            }
            Log.d(TAG, "Received intent with action: " + action);

            //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //int prevState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
            int nextState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
            Log.d(TAG, "Received connection state changed with nextState: " + nextState);

            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    Log.i(TAG, "wifi disconnected");
                    MyUtils.startBug2go("In silent test, wifi disconnected",mContext);
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    Log.i(TAG, "wifi reconnected");
                }
            }
        }
    };



    public void cleanup(){
        try {
            mContext.unregisterReceiver(mReceiver);
        }catch (IllegalArgumentException e){
            Log.e(TAG, "clean up alredy have been done before");
        }
    }
}
