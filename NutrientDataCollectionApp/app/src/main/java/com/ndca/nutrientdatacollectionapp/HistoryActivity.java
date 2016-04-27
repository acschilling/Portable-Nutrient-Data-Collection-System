package com.ndca.nutrientdatacollectionapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ndca.nutrientdatacollectionapp.util.UserDBHelper;

public class HistoryActivity extends AppCompatActivity {

    UserDBHelper dbHelper;
    Context context = this;
    SQLiteDatabase db;
    ListView listView;
    Cursor cursor;
    ListDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        listView = (ListView) findViewById(R.id.sample_list);
        adapter = new ListDataAdapter(getApplicationContext(),R.layout.activity_row);
        listView.setAdapter(adapter);
        dbHelper = new UserDBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        cursor = dbHelper.getInformation(db);
        if(cursor.moveToFirst()){
            do{

                String name,id,location;
                name = cursor.getString(0);
                id = cursor.getString(1);
                location = cursor.getString(2);
                DataProvidor dataProvidor = new DataProvidor(name,id,location);
                adapter.add(dataProvidor);

            }while(cursor.moveToNext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
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

    /** Called when the user clicks the New Sample button */
    public void goToMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    //Create method that creates database/tables and inserts 3 values.
    //Put as a button
    public void createDatabase(View view){

        //Button button = (Button) view;
        dbHelper = new UserDBHelper(context);
        db = dbHelper.getWritableDatabase();
        /*dbHelper.addInformation("Example1","1","Lake Laverne",db);
        dbHelper.addInformation("Example2","2","Lake Laverne",db);
        dbHelper.addInformation("Example3","3","Lake Laverne",db);*/
        dbHelper.addInformation("Experiment_One","004","Coover Lab",db);
        Toast.makeText(getBaseContext(),"Data Created", Toast.LENGTH_LONG).show();
        dbHelper.close();

    }

   /* public void deleteDatabase(View view){

        dbHelper = new UserDBHelper(context);
        db = dbHelper.getWritableDatabase();
        dbHelper.deleteTable(db);
        Toast.makeText(getBaseContext(),"Table Deleted", Toast.LENGTH_LONG).show();
        dbHelper.close();

    }*/
}
