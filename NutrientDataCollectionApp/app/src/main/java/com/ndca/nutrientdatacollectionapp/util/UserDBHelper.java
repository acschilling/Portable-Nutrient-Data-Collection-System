package com.ndca.nutrientdatacollectionapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by polar_cyclone12 on 4/14/2016.
 */
public class UserDBHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "sample_data.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_QUERY = "CREATE TABLE " + SampleData.SampleInfo.TABLE_NAME+"("+
            SampleData.SampleInfo.SAMPLE_NAME+" TEXT,"+SampleData.SampleInfo.ID+" INTEGER,"+ SampleData.SampleInfo.LOCATION+" TEXT);";

    public UserDBHelper(Context context){

        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        Log.e("DATABASE OPERATIONS","Database Created");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_QUERY);
        Log.e("DATABASE OPERATIONS", "Table Created");

    }

    public void addInformation(String name, String id, String location, SQLiteDatabase db){

        ContentValues contentValues = new ContentValues();

        contentValues.put(SampleData.SampleInfo.SAMPLE_NAME, name);
        contentValues.put(SampleData.SampleInfo.ID, id);
        contentValues.put(SampleData.SampleInfo.LOCATION, location);
        db.insert(SampleData.SampleInfo.TABLE_NAME, null, contentValues);

    }

    public Cursor getInformation(SQLiteDatabase db){

        Cursor cursor;
        String[] projections = {SampleData.SampleInfo.SAMPLE_NAME, SampleData.SampleInfo.ID, SampleData.SampleInfo.LOCATION};
        cursor = db.query(SampleData.SampleInfo.TABLE_NAME,projections,null,null,null,null,null);
        return cursor;

    }

    /*public void deleteTable(SQLiteDatabase db){
        db.execSQL("DROP TABLE " + SampleData.SampleInfo.TABLE_NAME);
    }*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
