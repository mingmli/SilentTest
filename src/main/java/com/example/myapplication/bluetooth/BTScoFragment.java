package com.example.myapplication.bluetooth;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import java.io.FileInputStream;
import java.io.IOException;

public class BTScoFragment extends Fragment {
    private Button btStartSco;
    private Button btChangeRoute;
    private static final String TAG = "Silent_BTSco";
    private MediaPlayer mMPlayer;
    private MediaPlayer mMplayer2;
    private AudioManager mAManager;
    private Context thisActivity;
    private boolean isStart = false;
    private boolean isA2DP = true;
    private TextView txRoute;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_bt_sco, container, false);
        thisActivity = getActivity();
        mAManager =(AudioManager)(thisActivity.getSystemService(Context.AUDIO_SERVICE));
        mMPlayer = new MediaPlayer();
        try{
            mMPlayer.setDataSource(new FileInputStream("/storage/emulated/0/sample.mp3").getFD());
            mMPlayer.setLooping(true);
            AudioAttributes audioAttributesSCO = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            mMPlayer.setAudioAttributes(audioAttributesSCO);
            //mMPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }catch(Exception e){
            Log.i(TAG,e.getMessage());
        }
        btStartSco = root.findViewById(R.id.btnStartSco);
        btChangeRoute = root.findViewById(R.id.btnChangeRoute);
        txRoute = root.findViewById(R.id.txRoute);
        btStartSco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isStart) {
                    btChangeRoute.setText(getResources().getText(R.string.bt_route_a2dp));
                    txRoute.setText(getResources().getText(R.string.bt_route_sco));
                    btStartSco.setText("STOP playing");
                    isA2DP = false;
                    try {
                        isStart = true;
                        startSco();
                        mMPlayer.prepare();
                        mMPlayer.start();
                    } catch (IOException e) {
                        Log.i(TAG, e.getMessage());
                    }
                }else{
                    isStart = false;
                    isA2DP = true;
                    btChangeRoute.setText(getResources().getText(R.string.bt_route_sco));
                    txRoute.setText(getResources().getText(R.string.bt_route_a2dp));
                    btStartSco.setText("START playing");
                    stopPlayer();
                    stopSco();
                }
            }
        });
        btChangeRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStart){
                    if(isA2DP) {
                        isA2DP = false;
                        txRoute.setText(getResources().getText(R.string.bt_route_sco));
                        btChangeRoute.setText(getResources().getText(R.string.bt_route_a2dp));
                        startSco();
                        reStart(false);
                    }else{
                        isA2DP = true;
                        txRoute.setText(getResources().getText(R.string.bt_route_a2dp));
                        btChangeRoute.setText(getResources().getText(R.string.bt_route_sco));
                        stopSco();
                        reStart(true);
                    }
                }
            }
        });
        return root;
    }
    private void stopSco(){
        mAManager.setBluetoothScoOn(false);
        mAManager.stopBluetoothSco();
        mAManager.setMode(AudioManager.MODE_NORMAL);


    }
    private void startSco(){
        mAManager.setBluetoothScoOn(true);
        mAManager.startBluetoothSco();
        mAManager.setMode(AudioManager.MODE_IN_CALL);
    }
    private void stopPlayer(){
        if(mMPlayer!=null && mMPlayer.isPlaying())mMPlayer.stop();
    }
    private void reStart(boolean isA2dp){
        stopPlayer();
        //Need reset to reset the streamType, or it will be failed
        mMPlayer.reset();
        try {
            mMPlayer.setDataSource(new FileInputStream("/storage/emulated/0/sample.mp3").getFD());
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        if(isA2dp){
            AudioAttributes audioAttributesA2DP = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
            mMPlayer.setAudioAttributes(audioAttributesA2DP);
        }else{
            AudioAttributes audioAttributesSCO = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            mMPlayer.setAudioAttributes(audioAttributesSCO);
        }
        mMPlayer.setLooping(true);
        try{
            mMPlayer.prepare();
            mMPlayer.start();
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
        stopSco();
    }
}
