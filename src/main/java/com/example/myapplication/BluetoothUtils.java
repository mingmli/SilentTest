package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;

public class BluetoothUtils {
    BluetoothAdapter btAdapter;
    int A2DP;
    int HFP;

    public BluetoothUtils(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        A2DP = -1;
        HFP = -1;
    }

    public boolean getBTstate() {
        return btAdapter.isEnabled();
    }

    public int getA2DPState(){
        if(getBTstate()) {
            A2DP = btAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        }
       return A2DP;
    }

    public int getHFPState(){
        if(getBTstate()) {
            HFP = btAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        }
        return HFP;
    }

    public String getConnectionState(){
        if(getA2DPState()==BluetoothProfile.STATE_CONNECTED || getHFPState()==BluetoothProfile.STATE_CONNECTED){
            return "Connected";
        }
        return "Disconnected";
    }

}
