package com.example.ogheneruona.rc_car_controller;

/**
 * Created by Ogheneruona on 3/23/2015.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


/**
 * Created by Ogheneruona on 11/2/2014.
 * Models a android bluetooth communication between as a client with an Open Remote Bluetooth (Server)
 */
public class cBluetooth {
    public final static String TAG = "cBluetooth";   //Tag for logging data

    //variables
    private static BluetoothAdapter bluetoothAdapter;   //local Bluetooth device
    private BluetoothSocket bluetoothSocket;            //Entrance from Open server
    private OutputStream outputStream;                  //Stream to send out
    private ConnectedThread mConnectedThread;           //thread used managing connections

    // SPP UUID services
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Handler mHandler;

    //flags for every handler on each activity to handle
    public final static int BL_NOT_AVAILABLE = 1;
    public final static int BL_INCORRECT_ADDRESS = 2;
    public final static int BL_REQUEST_ENABLE = 3;
    public final static int BL_SOCKET_FAILED = 4;
    public final static int RECIEVE_MESSAGE = 5;

    public cBluetooth(Context context,Handler mHandler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = mHandler;

        //If local device is not found
        if (bluetoothAdapter==null){
            this.mHandler.sendEmptyMessage(BL_NOT_AVAILABLE);
            return;
        }
    }

    /**
     * Check to see the state of the bluetooth (logging also done for debugging purposes)
     */
    public void checkBTState() {
        //if the bluetooth is not available
        if(bluetoothAdapter == null) {
            mHandler.sendEmptyMessage(BL_NOT_AVAILABLE);
        } else {
            if (bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth ON");
            } else {
                mHandler.sendEmptyMessage(BL_REQUEST_ENABLE);   //if bluetooth available but not on
            }
        }
    }

    /**
     * It is connecting a local Bluetooth Adaptor to an external bluetooth device
     * @param address MAC Address for remote device
     */
    public void BT_Connect(String address) {
        Log.d(TAG, "...On Resume...");

        //check to see if the MAC address of remote bluetooth device is a valid entry
        if(!BluetoothAdapter.checkBluetoothAddress(address)){
            mHandler.sendEmptyMessage(BL_INCORRECT_ADDRESS);
            return;
        }
        //if it's a correct address
        //  Create Bluetooth device, create RFcomm Socket,, then try to connect
        else{
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d(TAG, "In onResume() and socket create failed: " + e.getMessage());
                mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
                return;
            }

            //cancel discovery as a precaution
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "...Connecting...");
            try {
                bluetoothSocket.connect();
                Log.d(TAG, "...Connection ok...");
            } catch (IOException e) {
                try {
                    bluetoothSocket.close();
                } catch (IOException e2) {
                    Log.d(TAG, "In onResume() and unable to close socket during connection failure" + e2.getMessage());
                    mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
                    return;
                }
            }

            // Create a data stream so we can talk to server.
            Log.d(TAG, "...Create outputstream...");

            try {
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "In onResume() and output stream creation failed:" + e.getMessage());
                mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
                return;
            }

            mConnectedThread = new ConnectedThread();
            mConnectedThread.start();
        }
    }

    /**
     * define how to handle app on the on_pause call i.e flush output stream and close socket
     */
    public void BT_onPause() {
        Log.d(TAG, "...On Pause...");
        //if output stream is not clear, flush the screen
        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (IOException e) {
                Log.d(TAG, "In onPause() and failed to flush output stream: " + e.getMessage());
                mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
                return;
            }
        }

        //if socket is still avialable
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {
                Log.d(TAG, "In onPause() and failed to close socket." + e2.getMessage());
                mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
                return;
            }
        }
    }

    /**
     * Send message to the remote bluetooth with a socket
     * @param message
     */
    public void sendData(String message) {

        //get bytes
        byte[] msgBuffer = message.getBytes();

        Log.i(TAG, "Send data: " + message);

        //if output stream is availabel
        if (outputStream != null) {
            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "In onResume() and an exception occurred during write: " + e.getMessage());
                mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
                return;
            }
        } else Log.d(TAG, "Error Send data: outStream is Null");
    }

    /**
     * Model used for reading input streams
     * needs a seperate thread for reading streams
     */
    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;

        public ConnectedThread() {
            InputStream tmpIn = null;

            // Get the input  using temp objects because
            // member streams are final
            try {
                tmpIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {//@TODO Add what is needed later
            }

            mmInStream = tmpIn;
        }

        //Main method for the new thread created here
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    mHandler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

}
