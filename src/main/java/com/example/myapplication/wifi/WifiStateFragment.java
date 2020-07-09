package com.example.myapplication.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.PrefStatusUtil;
import com.example.myapplication.R;
import com.example.myapplication.StatedFragment;


public class WifiStateFragment extends StatedFragment {
    private WifiConnectionEventManager mWifiConnectionManager;
    private Button btCheckWifi;
    boolean isCheckWifi = false;
    Context thisActivity;
    Intent intent;
    PrefStatusUtil pf;
    private static final String TAG = "Silent_WifiStateFragment";


    @Override
    protected void onFirstTimeLaunched() {
        Log.i(TAG,"onFirstTime");
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG,"onCreateView");
        super.onCreate(savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_wifi, container, false);
        thisActivity = getActivity();
        mWifiConnectionManager = new WifiConnectionEventManager(thisActivity);
        btCheckWifi = root.findViewById(R.id.btnCheckWifiStatus);
        intent = new Intent(thisActivity, ListenWifiStateService.class);
        pf = new PrefStatusUtil(thisActivity);
        isCheckWifi = pf.getBooleanStatus("WifiStatus");
        setWifiStatus(isCheckWifi);
        btCheckWifi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!isCheckWifi){
                    btCheckWifi.setText("Stop checking Wifi status");
                    isCheckWifi = true;
                    thisActivity.startService(intent);
                }else {
                    btCheckWifi.setText("Start checking Wifi status");
                    isCheckWifi = false;
                    thisActivity.stopService(intent);
                }
                pf.putBooleanStatus("WifiStatus", isCheckWifi);
            }
        });
        return root;
    }

    private void setWifiStatus(boolean isCheck){
        Log.i(TAG,"setWifiStatus"+isCheck+"btCheckWifi:"+btCheckWifi);
        if(btCheckWifi!=null){
            Log.i(TAG,"isCheck"+isCheck);
            if(isCheck){
                btCheckWifi.setText("Stop checking Wifi status");
            }else{
                btCheckWifi.setText("Start checking Wifi status");
            }

        }
    }

    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
        Log.i(TAG,"onRestoreState");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestory");
    }



}
