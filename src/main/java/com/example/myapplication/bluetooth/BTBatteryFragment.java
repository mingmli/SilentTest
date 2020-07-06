package com.example.myapplication.bluetooth;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class BTBatteryFragment extends Fragment {
    private Button btCheckBattery;
    boolean isCheckBattery = false;
    Context thisActivity;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_bt_battery, container, false);
        thisActivity = getActivity();
        return root;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
