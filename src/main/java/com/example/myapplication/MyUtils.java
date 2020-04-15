package com.example.myapplication;

import android.content.Context;
import android.os.DropBoxManager;
import android.util.Log;

import java.util.Date;

public class MyUtils {
    private static final String TAG = "Silent_utils";
    private static final String BUG2GO_AUTO_UPLOAD_EVENT_START = "BUG2GO_USER_REPORT";
    public static void startBug2go(String description, Context context){
        Log.i(TAG,"startBug2go");
        //https://sse.am.mot.com/q_source/xref/mq-r-6125/motorola/packages/apps/Bug2Go/application/res/raw/deam.xml#3149
        //String evtType = WIFI_DISCONNECT_BUG2GO;
        String evtType = BUG2GO_AUTO_UPLOAD_EVENT_START;
        String value = "ts :" + new Date(System.currentTimeMillis()) + ", "+description;
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
        Log.d(TAG, "Added Drop box entry - " + evtType + " " + value );
    }
}