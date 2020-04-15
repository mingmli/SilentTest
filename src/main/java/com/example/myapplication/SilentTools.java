package com.example.myapplication;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import java.net.ConnectException;
import java.security.MessageDigest;
import java.util.logging.LogRecord;

public class SilentTools {

    private static final String TAG = "silent_Tools";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetVoiceRun;
    Object mLock;
    Thread mThread;
    double mVolume;
    long startSilentTime;
    long stopSilentTime;
    long silentTime;
    boolean isStartSilent;
    int mSilentDB;
    int mIssueTime;
    private static final boolean DEBUG = false;
    private Context mContext;
    private Messenger myHandler;
    AudioManager musicManager;


    public void setMyHandler(Messenger handle){
        myHandler = handle;
    }

    public SilentTools(Messenger handle, Context context, AudioRecord audioRecord) {
        mLock = new Object();
        mVolume = -1;
        mSilentDB = 50;
        mIssueTime = 10000;
        myHandler = handle;
        mContext = context;
        musicManager =(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioRecord = audioRecord;
        resetTime();
    }

    public boolean isSilent(double volume)
    {
        if(volume < mSilentDB && !"-Infinity".equals(volume+"")){
            return true;
        }else{
            return false;
        }
    }

    public void setSilentDB(int silentDB){
        mSilentDB = silentDB;
    }

    public int getSilentDB(){
        return mSilentDB;
    }

    public void setIssueTime(int issueTime){
        mIssueTime = issueTime;
    }

    private boolean isSilentIssue()
    {
        boolean isMusicAlive = musicManager.isMusicActive();
        if(DEBUG)Log.i(TAG,"isStartSilent:"+isStartSilent+" stopSilentTime:"+stopSilentTime+" startSilentTime:"+startSilentTime+" isMusicAlive:"+isMusicAlive);
        if(isStartSilent && stopSilentTime!=-100 && startSilentTime !=-100  && stopSilentTime - startSilentTime > mIssueTime && isMusicAlive) return true;
        return false;
    }


    public void startGetNoise() {
        Log.i(TAG,"startGetNoise");
        if (isGetVoiceRun) {
            Log.e(TAG, "isGetVocieRun:"+isGetVoiceRun);
            return;
        }
        if (mAudioRecord == null) {
            Log.e("sound", "mAudioRecord failed to init");
        }
        isGetVoiceRun = true;

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
                    mVolume = volume;
                    if(isSilent(volume) && !isStartSilent){
                        isStartSilent= true;
                        startSilentTime = System.currentTimeMillis();
                    }else if(isSilent(volume)&&isStartSilent){
                         stopSilentTime = System.currentTimeMillis();
                    } else if(!isSilent(volume)){
                        isStartSilent = false;
                        resetTime();
                    }
                    if(DEBUG)Log.d(TAG, "DB:" + volume);
                    Message msg = Message.obtain();
                    msg.what=Constant.MESSAGE_DB;
                    msg.obj=volume;
                    if(myHandler!=null) {
                        try {
                            myHandler.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    // 大概一秒一次
                    synchronized (mLock) {
                        try {
                            mLock.wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(isSilentIssue()){
                        Message msg1 = Message.obtain();
                        msg1.what = Constant.MESSAGE_ISSILENT;
                        try {
                            myHandler.send(msg1);
                            isGetVoiceRun = false;//Avoid trigger many times
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        });
        mThread.start();
    }

    public double getVolume(){
        return mVolume;
    }

    private void resetTime(){
        silentTime = 0;
        startSilentTime = -100;
        stopSilentTime = -100;
    }
    public void stopGetVoice(){
        isGetVoiceRun = false;
        isStartSilent = false;
        resetTime();
    }
}
