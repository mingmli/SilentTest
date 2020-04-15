package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {
    private static final String TAG = "Silent_Noti";
    private NotificationManager notificationManager;
    private Messenger mMessenger;
    private SilentTools tool;
    private int NOTIFICATION = 1;
    private int issueTime = 10000;
    private int silentDB = 50;
    private AudioRecord mAudioRecord;
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    Thread mThread;
    private boolean isStartListen = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mMessenger ==null)
            mMessenger = (Messenger)intent.getExtras().get("messenger");
        issueTime = intent.getIntExtra(Constant.INTENT_EXTRA_ISSUE_TIME,10000);
        silentDB = intent.getIntExtra(Constant.INTENT_EXTRA_SILENT_DB,50);
        Log.i(TAG, "issueTime:"+issueTime+" silentDB:"+ silentDB);

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null) {
            Log.e("sound", "mAudioRecord failed to init");
        }
        mAudioRecord.startRecording();
        if(tool==null)
            tool = new SilentTools(mMessenger,this,mAudioRecord);
        tool.setIssueTime(issueTime);
        tool.setSilentDB(silentDB);
        tool.startGetNoise();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification("listening...");
    }
    private void showNotification(String db){
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationChannel mChannel = new NotificationChannel("mychannel","mychannel", NotificationManager.IMPORTANCE_HIGH);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"mychannel").setContentTitle("SilentAPP").setContentText(db)
                .setSmallIcon(R.drawable.ic_launcher_background).setContentIntent(pendingIntent);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.notify(NOTIFICATION,builder.build());
        startForeground(NOTIFICATION,builder.build());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        isStartListen = false;
        if(tool!=null)
           tool.stopGetVoice();
        if(notificationManager!=null)
            notificationManager.cancel(NOTIFICATION);
    }

    public void onClearNotify(){
        stopForeground(true);
    }


}
