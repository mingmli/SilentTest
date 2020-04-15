package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.GraphicalView;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    
    private BluetoothConnectionEventManager mBtConnectionManager;
    
    TextView tx;
    //SilentTools tool;
    EditText silentDB;
    EditText issueTime;
    Button btStart;
    Boolean isRecord = false;
    private final Handler handler = new Handler();
    private static final String TAG="MainActivity";
    Activity thisActivity;
    MediaPlayer mediaPlayer;
    MediaPlayer preMP;
    BluetoothReceiver btReceiver;
    TextView btState;
    public static final String BUG2OG_INTENT_BUGREPORT_START = "motorola.intent.action.BUG2GO.BUGREPORT.START";
    private static final String WIFI_DISCONNECT_BUG2GO = "WIFI_CLASS_D_DIAGNOSTICS" ;
    public static final String BUG2GO_AUTO_UPLOAD_EVENT_START = "bug2go_attach_start";
    boolean isStart = false;
    private Handler DBhandler;
    private FbChartline mService;
    private float degree = 0.0f;
    private GraphicalView mView;
    private LinearLayout dbChart;
    private DataReceiver dataReceiver;
    private int t = 0;
    private Intent intent;

    BluetoothUtils btUtils;

    @Override
    protected void onStart()
    {
        dataReceiver = new DataReceiver();
        IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
        filter.addAction("com.example.myapplication.service");
        registerReceiver(dataReceiver, filter);// 注册Broadcast Receiver
        super.onStart();
    }

    @Override
    protected void onResume() {
        //Click notification to resume
        Log.i(TAG,"onResume");
        if(isStart){
            btStart.setText("Stop Listening");
        }else if(isStart&&isRecord) {
            btStart.setText("Listening, click to stop");
        }else{
            btStart.setText("START Listening");
        }
        super.onResume();

    }

    private class DataReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mBtConnectionManager = new BluetoothConnectionEventManager(getApplicationContext());
        mBtConnectionManager.registerEvents();
        
        thisActivity = MainActivity.this;
        tx = findViewById(R.id.btsm);
        btStart = findViewById(R.id.btnStart);
        silentDB = findViewById(R.id.silentDB);
        issueTime = findViewById(R.id.issueTime);
        btState = findViewById(R.id.btState);
        dbChart = findViewById(R.id.dbChart);
        setChartLineView();

        mediaPlayer = new MediaPlayer();
        btReceiver = new BluetoothReceiver();
        btUtils = new BluetoothUtils();

        btState.setText(btUtils.getConnectionState());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(btReceiver, intentFilter);

        DBhandler=new Handler(){

            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case Constant.MESSAGE_DB:
                        String DB = msg.obj.toString();
                        Log.i(TAG,"DB:"+DB);
                        tx.setText(DB+"");
                        if ("-Infinity".equals( msg.obj.toString())) {
                            degree=0f;
                        }else {
                            degree =(Float.parseFloat( msg.obj.toString()));
                        }
                        //mService.updateChart(t, degree); //update chart
                        t+=1;
                        Log.i("updateChart:", "degree:"+msg.obj.toString()+" t:"+t);
                        break;
                     case Constant.MESSAGE_ISSILENT:
                         //Vibrate, alert, toast, bugreport
                         Vibrator vibrator = (Vibrator)thisActivity.getSystemService(thisActivity.VIBRATOR_SERVICE);
                         vibrator.vibrate(VibrationEffect.createOneShot(200,255));
                         startAlert();
                         Toast.makeText(getApplicationContext(), "Silent Issue!!! Stop Listen", Toast.LENGTH_LONG).show();
                         MyUtils.startBug2go("silent issue",thisActivity);
                         //tool.stopGetVoice();
                         btStart.setText("START Listening");
                         isStart = false;
                    default:
                        break;
                }
            }
        };

        try {
            AssetFileDescriptor fd = getAssets().openFd("alert.mp3");
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),fd.getLength());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                preMP = mp;
            }
        });

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1234);
        } else {
            //Already Granted
            isRecord = true;
        }
        //tool = new SilentTools(DBhandler,thisActivity);

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btStart
                if(!isStart) {
                    btStart.setText("Stop Listening");
                    isStart = true;
                    String silentDBText = silentDB.getText() + "";
                    String issueTimeText = issueTime.getText() + "";
                    int silentDB = 50;
                    int issueTime = 10000;
                    try {
                        if (!"".equals(silentDBText))
                            //tool.setSilentDB(Integer.parseInt(silentDBText));
                            silentDB= Integer.parseInt(silentDBText);
                        if (!"".equals(issueTimeText))
                            //tool.setIssueTime(Integer.parseInt(issueTimeText));
                            issueTime=Integer.parseInt(issueTimeText);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    //handler.postDelayed(task, 1000);
                    Log.i(TAG, "isRecord"+isRecord + "");
                    if (isRecord) {
                        btStart.setText("Listening, click to stop");
                        //tool.startGetNoise();
                        intent = new Intent(thisActivity, NotificationService.class);
                        intent.putExtra(Constant.INTENT_EXTRA_ISSUE_TIME,issueTime);
                        intent.putExtra(Constant.INTENT_EXTRA_SILENT_DB,silentDB);
                        intent.putExtra("messenger", new Messenger(DBhandler));
                        startService(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Record Permission Denied!!!", Toast.LENGTH_SHORT).show();
                    }
                }//btStop
                else{
                    btStart.setText("Start Listening");
                    isStart = false;
                    stopService(intent);
                    //tool.stopGetVoice();
                }
            }
        });

    }

    private void startAlert(){
        preMP.start();
    }

    private void setChartLineView() {
        mService=new FbChartline(thisActivity);
        mService.setXYMultipleSeriesDataset("DB Chart");
        mService.setXYMultipleSeriesRenderer(100, 100, "Chart", "time", "DB",
                Color.BLACK, Color.BLACK, Color.RED, Color.BLACK);
        mView = mService.getGraphicalView();
        dbChart.addView(mView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1234: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isRecord = true;

                } else {
                    Log.d("MainActivity", "permission denied by user");
                    Toast.makeText(getApplicationContext(), "Exit with Permission Denied!!!", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mBtConnectionManager.cleanup();

        mediaPlayer.release();
        preMP.release();
        //tool.stopGetVoice();
        unregisterReceiver(btReceiver);
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action){
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    btState.setText("Connected");
                    Toast.makeText(context,"Device connected",Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    btState.setText("Disconnected");
                    btState.setTextColor(Color.RED);
                    Toast.makeText(context,"Device disconnected",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
