package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.example.myapplication.bluetooth.BluetoothStateFragment;
import com.example.myapplication.wifi.WifiStateFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StartActivity extends AppCompatActivity implements PassDataToActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private WifiStateFragment mWifiFragment;
    private String TAG = "Silent_StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //FloatingActionButton fab = findViewById(R.id.fab);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_bt_state, R.id.nav_bt_battery, R.id.nav_wifi_state)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        checkIfGoBtStateFrag(savedInstanceState);
        if(getFragment(BluetoothStateFragment.class)!=null) {
            Log.i(TAG,"not null");
            ((BluetoothStateFragment) getFragment(BluetoothStateFragment.class)).setPassDataToActivityListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent!=null)
            checkIfGoBtStateFrag(intent.getExtras());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void checkIfGoBtStateFrag(Bundle bundle){
        if(bundle == null) return;
        int messageType = bundle.getInt("Type");

        Log.i(TAG,"checkIfGoBtStateFra: messageType:"+messageType);
        switch (messageType){
            case 0:
                //navController.navigate(R.id.nav_bt_state);
                break;
            case 2:
                //From notification
                Boolean isCheckWifi = bundle.getBoolean("isCheckWifi",false);
                Bundle b = new Bundle();
                b.putBoolean("isCheckWifi",true);
                //navController.navigate(R.id.nav_wifi_state, bundle);
                break;
            default:
                break;
        }

    }

    @Override
    public void passDateToActivity(String key, String value) {
        Log.i(TAG,"key:"+key);
    }

    private Fragment getFragment(Class<?> cls){
        NavHostFragment navHF =  (NavHostFragment)this.getSupportFragmentManager().getFragments().get(0);
        Log.i(TAG,"f:size:"+navHF.getChildFragmentManager().getFragments().size());
        for(Fragment f :  navHF.getChildFragmentManager().getFragments()){
            if(cls.isAssignableFrom(f.getClass())){
                return f;
            }
        }
        return null;
    }
}
