package com.example.ogheneruona.rc_car_controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Main Activity control the home page of the Rc Car Controller
 * Three Screen: SCREEN CONTROL, ACCELEROMETER CONTROL AND AUTOMATIC CONTROL
 */
public class MainActivity extends ActionBarActivity {

    //defines what the controller controls in its domain
    private Button on_screen_button;
    private Button on_accelerometer_button;
    private Button on_autonomous_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //connect to the actual widgets
        on_screen_button = (Button) findViewById(R.id.ScreenControl);
        on_accelerometer_button = (Button) findViewById(R.id.AccelerometerControl);
        on_autonomous_button = (Button) findViewById(R.id.AutomaticControl);

        on_screen_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ControlScreen.class);
                //@todo add the resource from measured distance to the rc car
                //@todo send the bluetooth control alongside with it
                startActivity(intent);
            }
        });

        on_accelerometer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ControlScreen.class);
                //@todo add the resource from measured distance to the rc car
                //@todo send the bluetooth control alongside with it
            }
        });

        // Starts up the autonomous control intmotion
        on_autonomous_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AutonomousControl.class);
                //@todo add the resource from measured distance to the rc car
                //@todo send the bluetooth control alongside with it
                startActivity(intent);
            }
        });




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
