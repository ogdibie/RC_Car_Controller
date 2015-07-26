package com.example.ogheneruona.rc_car_controller;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AutonomousControl extends ActionBarActivity {

    private final static String TAG = "AutonomousControl";      // tag for logging data
    private TextView distance_to_object;
    private Button Start_button, Stop_button;
    private String address;
    private cBluetooth b1 = null;

    //control bit used in controlling the car
    private static final String START = "4";
    private static final String STOP = "5";

    //used when retrieving messages
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autonomous_control);

        //connect the reference to the widgets
        Start_button = (Button)findViewById(R.id.start);
        Stop_button = (Button)findViewById(R.id.stop);
        distance_to_object = (TextView)findViewById(R.id.distance_viewer1);

        //set the action listeners on the buttons

        Start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               b1.sendData(START);
            }
        });

        Stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b1.sendData(STOP);
            }
        });

        address = "10:14:05:22:04:97";

        b1 = new cBluetooth(this,mHandler);
        b1.checkBTState();
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
                        distance_to_object.setText("Distance = " + sbprint);         // update TextView
                    }
                    Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        b1.BT_Connect(address);
    }

    @Override
    protected void onPause() {
        super.onPause();
        b1.BT_onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_autonomous_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
