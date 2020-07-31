package com.example.myapplication;

import android.content.Context;
import android.os.DropBoxManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.util.Date;

public class MyUtils {
    private static final String TAG = "Silent_utils";
    private static final String BUG2GO_AUTO_UPLOAD_EVENT_START = "BUG2GO_USER_REPORT";
    private static long mLastGenerateTimestamp;
    private static final long GENERATE_BUG2GO_TIME_INTERVAL = 60000;
    public static void startBug2go(String description, Context context){
        Log.i(TAG,"startBug2go");
        long currentGenerateTimestamp = System.currentTimeMillis();
        if (currentGenerateTimestamp - mLastGenerateTimestamp < GENERATE_BUG2GO_TIME_INTERVAL){
            Log.d(TAG, description + " time interval is too short ");
            return;
        }
        //https://sse.am.mot.com/q_source/xref/mq-r-6125/motorola/packages/apps/Bug2Go/application/res/raw/deam.xml#3149
        //String evtType = WIFI_DISCONNECT_BUG2GO;
        String evtType = BUG2GO_AUTO_UPLOAD_EVENT_START;
        String value = "ts :" + new Date(currentGenerateTimestamp) + ", "+description;
        final DropBoxManager dbox = (DropBoxManager)
                context.getSystemService(Context.DROPBOX_SERVICE);
        // Exit early if the dropbox isn't configured to accept this report type.
        if (dbox == null || !dbox.isTagEnabled(evtType)) {
            Log.d(TAG, "Dropbox tag - " + evtType + " not enabled");
            return;
        }
        StringBuilder sb = new StringBuilder(2048);
        sb.append(value + "\n\n");
        dbox.addText(evtType, sb.toString());
        mLastGenerateTimestamp = currentGenerateTimestamp;
        Log.d(TAG, "Added Drop box entry - " + evtType + " " + value );
    }

    //Write
    public static void writeFileData(String filename, String content, boolean isAppend){
        try {
            FileOutputStream fos = new FileOutputStream(filename,isAppend);
            fos.write(content.getBytes());
            fos.write("\r\n".getBytes());
            fos.close();
        } catch (Exception e) {
            Log.i(TAG,e.getMessage());
        }
    }

    public static int getFileSize(String fileName) {
        int size = -1;
        try {
            FileInputStream f = new FileInputStream(fileName);
            //Log.i("Silent",f.available()+"");
            size = f.available()/1024/1024/1024;//G
        }catch (Exception e) {
            Log.i(TAG,e.getMessage());
        }
        return size;
    }

    //READ
    public static String readFileData(String fileName){
        String result="";
        try{
            FileInputStream fis = new FileInputStream(fileName);
            //getLength
            int lenght = fis.available();
            byte[] buffer = new byte[lenght];
            fis.read(buffer);
            result = new String(buffer, "UTF-8");

        } catch (Exception e) {
            Log.i(TAG,e.getMessage());
        }
        return  result;
    }
}