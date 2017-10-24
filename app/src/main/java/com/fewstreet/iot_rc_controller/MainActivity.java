package com.fewstreet.iot_rc_controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.MobileAnarchy.Android.Widgets.Joystick.DualJoystickView;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.Service.UDPService;
import com.Service.UDPServiceConnection;
import com.google.gson.Gson;

/*
This app should be used under USB tethering mode
After a little while, there is an other wired connection appears with a new IP begin with 192.168.42.
this IP is the tethering mode IP of the PC.
*/

public class MainActivity extends AppCompatActivity {
    private DualJoystickView joystick;
    private String TAG = "MainActivity";
    private float throttle_range;
    CarControl carControl = new CarControl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joystick = (DualJoystickView)findViewById(R.id.dualjoystickView);
        joystick.setOnJostickMovedListener(_listenerRight,_listenerLeft);

        startUDPService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        throttle_range = SettingsActivity.getThrottleRange(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    private JoystickMovedListener _listenerLeft = new JoystickMovedListener() {
        float i = (float) 0.0;

        @Override
        // send only when mapped changes
        public void OnMoved(int pan, int tilt) {
            float mapped = ((float)tilt*-1*throttle_range + 100)/200;
            if(mapped!=i) {
                sendThrottle(mapped);
                Log.d(TAG, "sendThrottleOnMove");
            }
            i = mapped;
        }

        @Override
        public void OnReleased() {
            Log.d(TAG, "Released");
        }

        public void OnReturnedToCenter() {
            Log.d(TAG, "stopped");
            sendThrottle(0.5f);
            Log.d(TAG,"sendThrottleOnReturnedToCenter");
        };
    };

    private JoystickMovedListener _listenerRight = new JoystickMovedListener() {
        float i = (float) 0.0;

        @Override
        //send only when mapped changes
        public void OnMoved(int pan, int tilt) {
            float mapped = ((float)pan + 100)/200;
            if(mapped!=i) {
                sendSteering(mapped);
            }
            i = mapped;
        }

        @Override
        public void OnReleased() {
            Log.d(TAG, "Released");
        }

        public void OnReturnedToCenter() {
            Log.d(TAG, "stopped");
            sendSteering(0.5f);
        };
    };

    protected void onDestroy() {
        super.onDestroy();
        stopUDPService();
    }

    //initial UDPConnetion
    private static Intent mUDP = null;
    private static UDPServiceConnection mUDPConnection = null;

    private void startUDPService() {
        Log.d(TAG, "startUDPService");
        mUDP = new Intent(this, UDPService.class);
        mUDPConnection = new UDPServiceConnection();
        bindService(mUDP, mUDPConnection, Context.BIND_AUTO_CREATE);
        startService(mUDP);
    }

    private void stopUDPService() {
        if(mUDP != null && mUDPConnection != null) {
            unbindService(mUDPConnection);
            stopService(mUDP);
            mUDP = null;
            mUDPConnection = null;
        }
    }

    //send typed gson controllor order
    private void sendThrottle(float val) {

        Gson gson = new Gson();
        Log.d(TAG,"sendThrottle" + gson.toJson(carControl));
        carControl.throttle_=val;
        String json = gson.toJson(carControl);

        if(mUDP != null && mUDPConnection != null) {
            mUDPConnection.sendData(json);
        }
    }
    private void sendSteering(float val) {
        Gson gson = new Gson();
        Log.d(TAG,"sendSteering" + gson.toJson(carControl));
        carControl.steering_=val;
        String json = gson.toJson(carControl);

        Log.d(TAG,json);
        if(mUDP != null && mUDPConnection != null) {
            mUDPConnection.sendData(json);        }
    }

}



