package com.example.myapplication;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Date;

public class BluetoothConnectionEventManager {
    private static final String TAG = "BluetoothConnectionEventManager";
    private static final String DEFAULT_BLUETOOTH_BUG2GO_TYPE = "BUG2GO_USER_REPORT";
    private static final int MSG_ACL_DISCONNECTED = 0;
    private static final int MSG_A2DP_CONNECTION_STATE_CHANGED = 1;
    private static final int MSG_HFP_CONNECTION_STATE_CHANGED = 2;

    private static final long GENERATE_BUG2GO_TIME_INTERVAL = 30000;

    private Handler mHandler = null;
    private long mLastGenerateTimestamp;
    IntentFilter mFilter;

    private Context mContext;

    public BluetoothConnectionEventManager(Context context) {
        mContext = context;
        mHandler = new BluetoothConnectionStateHandler(Looper.getMainLooper());
        mFilter = new IntentFilter();
        mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        mFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
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

            switch (action) {
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mHandler.obtainMessage(MSG_ACL_DISCONNECTED).sendToTarget();
                    break;
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    mHandler.obtainMessage(MSG_A2DP_CONNECTION_STATE_CHANGED,
                            nextState).sendToTarget();
                    break;
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    mHandler.obtainMessage(MSG_HFP_CONNECTION_STATE_CHANGED,
                            nextState).sendToTarget();
                    break;
                default:
                    Log.e(TAG, "Received unexpected intent, action=" + action);
                    break;
            }
        }
    };

    private class BluetoothConnectionStateHandler extends Handler{
        BluetoothConnectionStateHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_ACL_DISCONNECTED:
                    triggerBug2go(" Generate the bug2go for bt ACL disconnection");
                    break;
                case MSG_A2DP_CONNECTION_STATE_CHANGED: {
                    int nextState = (int) msg.obj;
                    if (nextState == BluetoothProfile.STATE_DISCONNECTED) {
                        // enter disconnected state
                        triggerBug2go(" Generate the bug2go for bt A2DP disconnection");
                    }
                }
                break;
                case MSG_HFP_CONNECTION_STATE_CHANGED: {
                    int nextState = (int) msg.obj;
                    if (nextState == BluetoothProfile.STATE_DISCONNECTED) {
                        // enter disconnected state
                        triggerBug2go(" Generate the bug2go for bt HFP disconnection");
                    }
                }
                break;
            }
        }
    }

    private void triggerBug2go(String description){
        long currentGenerateTimestamp = System.currentTimeMillis();
        if (currentGenerateTimestamp - mLastGenerateTimestamp < GENERATE_BUG2GO_TIME_INTERVAL){
            Log.d(TAG, description + " time interval is too short ");
            return;
        }

        //MyUtil.startBug2go(description, mContext);

        String evtType = DEFAULT_BLUETOOTH_BUG2GO_TYPE;
        final DropBoxManager dbox = (DropBoxManager)
                mContext.getSystemService(Context.DROPBOX_SERVICE);
        if (dbox == null || !dbox.isTagEnabled(evtType)) {
            Log.d(TAG, "Dropbox tag - " + evtType + " is NOT configured to accept this report type");
            return;
        }

        String value = "ts : " + new Date(currentGenerateTimestamp) + description;
        StringBuilder sb = new StringBuilder(2048);
        sb.append(value + "\n\n");

        dbox.addText(evtType, sb.toString());
        Log.d(TAG, "Added DropBox entry - " + evtType + " " + value );

        mLastGenerateTimestamp = currentGenerateTimestamp;
    }

    public void cleanup(){
        mContext.unregisterReceiver(mReceiver);
    }
}
