package com.ndca.nutrientdatacollectionapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
import android.support.v7.app.AppCompatActivity;
=======
import android.util.Log;
>>>>>>> refs/remotes/origin/master
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class NewSampleActivity extends AppCompatActivity {

    bt bluetooth = new bt(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sample);
        bluetooth.initialize();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!bluetooth.isBTOn())
        {
            bluetooth.btOn();
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

    public void goToSample(View view) {
        Intent intent = new Intent(this, SampleActivity.class);
        startActivity(intent);
    }

    /**
     * Called by button to start system
     */
    public void startSystem(View view){
        bluetooth.notifyConnectedDevices();
        Log.i("button_pressed", "System started");
    }
}
