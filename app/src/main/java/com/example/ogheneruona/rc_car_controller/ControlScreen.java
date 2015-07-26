package com.example.ogheneruona.rc_car_controller;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Model for the control screen with 4 major buttons to control
 * the movement of the car through bluetooth control
 */
public class ControlScreen extends ActionBarActivity {
    public final static String TAG = "ControlScreen";   //Tag for logging data
    private TextView distance_viewer;
    private cBluetooth bl = null;
    private Button btn_forward, btn_backward, btn_left, btn_right;
    private String address;     //MAC address

    //control values to send the arduino for controlling the RC car
    private static final String FORWARD = "0";
    private static final String BACKWARD = "1";
    private static final String LEFT = "2";
    private static final String RIGHT = "3";
    private static final String STOP = "5";

    //to receiving messages
    private StringBuilder sb = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_screen);
        //set up controls for the widgets
        btn_forward = (Button)findViewById(R.id.forward);
        btn_backward = (Button)findViewById(R.id.backward);
        btn_left = (Button)findViewById(R.id.left);
        btn_right = (Button)findViewById(R.id.right);
        distance_viewer = (TextView) findViewById(R.id.distance_viewer);

        //actions to be taken when either one of the buttons are connected
        btn_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bl.sendData(FORWARD);
            }
        });

        btn_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bl.sendData(BACKWARD);
            }
        });
        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bl.sendData(LEFT);
            }
        });
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bl.sendData(RIGHT);
            }
        });
        //mac_address of the Bluetooth set_up by default
        address = (String) getResources().getText(R.string.default_MAC);

        bl = new cBluetooth(this,mHandler);
        bl.checkBTState();
    }

    /**
     * Create a handler to handle to what messages (or even when a message is recieved
     */
    private final Handler mHandler =  new Handler() {
        public void handleMessage(android.os.Message msg) {
            //handle different WHAT messages
            switch (msg.what) {
                case cBluetooth.BL_NOT_AVAILABLE:
                    Log.d(cBluetooth.TAG, "Bluetooth is not available. Exit");
                    Toast.makeText(getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case cBluetooth.BL_INCORRECT_ADDRESS:
                    Log.d(cBluetooth.TAG, "Incorrect MAC address");
                    Toast.makeText(getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
                    break;
                case cBluetooth.BL_REQUEST_ENABLE:
                    Log.d(cBluetooth.TAG, "Request Bluetooth Enable");
                    BluetoothAdapter.getDefaultAdapter();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                    break;
                case cBluetooth.BL_SOCKET_FAILED:
                    Toast.makeText(getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case cBluetooth.RECIEVE_MESSAGE:                                 // if receive massage
                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, msg.arg1);          // create string from bytes array
                    sb.append(strIncom);                                         // append string
                    int endOfLineIndex = sb.indexOf("\r\n");                     // determine the end-of-line
                    if (endOfLineIndex > 0) {                                    // if end-of-line,
                        String sbprint = sb.substring(0, endOfLineIndex);        // extract string
                        sb.delete(0, sb.length());                               // and clear
                        distance_viewer.setText("Distance = " + sbprint);         // update TextView
                    }
                    Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        bl.BT_Connect(address);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bl.BT_onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

