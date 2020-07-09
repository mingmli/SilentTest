package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefStatusUtil {
    private Context mContext;
    private SharedPreferences mSpf;
    public PrefStatusUtil(Context context){
        mSpf = context.getSharedPreferences("Status",Context.MODE_PRIVATE);
    }
    public void putBooleanStatus(String key, Boolean value){
        SharedPreferences.Editor editor = mSpf.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public boolean getBooleanStatus(String key){
        return mSpf.getBoolean(key,false);
    }
}
