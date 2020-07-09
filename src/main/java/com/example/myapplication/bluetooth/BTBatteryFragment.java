package com.example.myapplication.bluetooth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.PrefStatusUtil;
import com.example.myapplication.R;
import com.example.myapplication.wifi.ListenWifiStateService;

public class BTBatteryFragment extends Fragment {
    private Button btCheckBattery;
    boolean isCheckBattery = false;
    Context thisActivity;
    private static final String TAG = "Silent_BTBatteryFragment";
    PrefStatusUtil pf;
    Intent intent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity = getActivity();
        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1234);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1234: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(thisActivity, "Exit with Permission Denied!!!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_bt_battery, container, false);

        btCheckBattery = root.findViewById(R.id.btnCheckBTBattery);
        intent = new Intent(thisActivity, ListenBTBatteryService.class);
        pf = new PrefStatusUtil(thisActivity);
        isCheckBattery = pf.getBooleanStatus("BTBatteryStatus");
        String tmp = isCheckBattery?"Stop checking BT Battery":"Start checking BT Battery";
        btCheckBattery.setText(tmp);
        btCheckBattery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!isCheckBattery){
                    btCheckBattery.setText("Stop checking BT Battery");
                    thisActivity.startService(intent);
                    isCheckBattery = true;
                }else {
                    btCheckBattery.setText("Start checking BT Battery");
                    thisActivity.stopService(intent);
                    isCheckBattery = false;

                }
                pf.putBooleanStatus("BTBatteryStatus", isCheckBattery);
            }
        });
        return root;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
