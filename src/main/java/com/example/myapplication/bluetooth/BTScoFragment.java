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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BTScoFragment extends Fragment {
    private Button btStartSco;
    private Button btChangeRoute;
    private static final String TAG = "Silent_BTSco";
    private MediaPlayer mMPlayerA2DP;
    private MediaPlayer mMplayerSCO;
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
        mMplayerSCO = new MediaPlayer();
        mMPlayerA2DP = new MediaPlayer();
        try{
            mMplayerSCO.setDataSource(new FileInputStream("/storage/emulated/0/sampleSCO.mp3").getFD());
            mMplayerSCO.setLooping(true);
            AudioAttributes audioAttributesSCO = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            mMplayerSCO.setAudioAttributes(audioAttributesSCO);
            //mMPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mMplayerSCO.prepare();

            mMPlayerA2DP.setDataSource(new FileInputStream("/storage/emulated/0/sampleA2DP.mp3").getFD());
            mMPlayerA2DP.setLooping(true);
            AudioAttributes audioAttributesA2DP = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
            mMPlayerA2DP.setAudioAttributes(audioAttributesA2DP);
            mMPlayerA2DP.prepare();
        }catch(FileNotFoundException e){
            Log.i(TAG,e.getMessage());
            Toast.makeText(thisActivity,"mp3 Files not found!!", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
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
                    txRoute.setText(getResources().getText(R.string.cur_route_sco));
                    btStartSco.setText("STOP playing");
                    isA2DP = false;
                    isStart = true;
                    mMplayerSCO.start();
                }else{
                    isStart = false;
                    isA2DP = true;
                    btChangeRoute.setText(getResources().getText(R.string.bt_route_sco));
                    txRoute.setText(getResources().getText(R.string.cur_route_a2dp));
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
                        txRoute.setText(getResources().getText(R.string.cur_route_sco));
                        btChangeRoute.setText(getResources().getText(R.string.bt_route_a2dp));
                        startSco();
                        reStart(false);
                    }else{
                        isA2DP = true;
                        txRoute.setText(getResources().getText(R.string.cur_route_a2dp));
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
        if(mMplayerSCO!=null && mMplayerSCO.isPlaying())mMplayerSCO.stop();
        if(mMPlayerA2DP!=null && mMPlayerA2DP.isPlaying())mMPlayerA2DP.stop();
    }
    private void reStart(boolean isA2dp) {
        if(isA2dp){
            if(mMPlayerA2DP.isPlaying()) return;
            if(mMplayerSCO!=null && mMplayerSCO.isPlaying())mMplayerSCO.pause();
            mMPlayerA2DP.start();
        }else{
            if(mMPlayerA2DP!=null && mMPlayerA2DP.isPlaying())mMPlayerA2DP.pause();
            if(mMplayerSCO.isPlaying())return;
            mMplayerSCO.start();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
        stopSco();
    }
}
