package com.example.myapplication.wifi;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class WifiStateFragment extends Fragment {
    private WifiConnectionEventManager mWifiConnectionManager;
    private Button btCheckWifi;
    boolean isCheckWifi = false;
    Context thisActivity;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_wifi, container, false);
        thisActivity = getActivity();
        mWifiConnectionManager = new WifiConnectionEventManager(thisActivity);
        btCheckWifi = root.findViewById(R.id.btnCheckWifiStatus);
        btCheckWifi.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(!isCheckWifi){
                    btCheckWifi.setText("Stop checking Wifi status");
                    isCheckWifi = true;
                    mWifiConnectionManager.registerEvents();
                }else {
                    btCheckWifi.setText("Start checking Wifi status");
                    isCheckWifi = false;
                    mWifiConnectionManager.cleanup();
                }
            }
        });
        return root;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiConnectionManager.cleanup();
    }

}
