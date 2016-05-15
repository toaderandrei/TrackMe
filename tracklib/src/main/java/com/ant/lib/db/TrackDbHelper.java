package com.ant.lib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that deals with creating the database.
 */
public class TrackDbHelper extends SQLiteOpenHelper {


    private Context context;
    private static final String DATABASE_NAME = "trackme.db";
    private static final int DATABASE_VERSION = 1;

    public TrackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        List<String> scripts = createScripts();
        for (String query : scripts) {
            db.execSQL(query);
        }
    }

    private List<String> createScripts(){
        List<String> scripts = new ArrayList<>();


        return scripts;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
