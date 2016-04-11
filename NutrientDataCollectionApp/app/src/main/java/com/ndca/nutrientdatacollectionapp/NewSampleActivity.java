package com.ndca.nutrientdatacollectionapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class NewSampleActivity extends AppCompatActivity {

    public AlertDialog alertDialog;
    public BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sample);

        alertDialog = new AlertDialog.Builder(this).create();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null){
            alertDialog.setTitle("Bluetooth Not Supported");
            alertDialog.setMessage("This app requires Bluetooth to retrieve Data");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    goToHistory(NewSampleActivity.this);

                }

            });
            alertDialog.show();
        }
        else{
            if (mBluetoothAdapter.isEnabled()){
                if(mBluetoothAdapter.isDiscovering()){
                    alertDialog.setTitle("Bluetooth is currently discovering devices");
                    alertDialog.setMessage("Please connect to (insert device name here) and return to this app");
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
                //Only needed for debugging reasons.
                else{
                    alertDialog.setTitle("Bluetooth is enabled");
                    alertDialog.setMessage("You are good to go");
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }

                    });
                    alertDialog.show();
                }
            }
            else{
                alertDialog.setTitle("Bluetooth is not enabled");
                alertDialog.setMessage("Please enable Bluetooth as this app requires it in order to collect data");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        goToHistory(NewSampleActivity.this);

                    }
                });
                alertDialog.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_sample, menu);
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

    public void goToHistory(Activity activity) {
            Intent intent = new Intent(activity, HomeActivity.class);
            startActivity(intent);
    }
}
