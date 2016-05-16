package com.ant.lib.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of the SQLiteOpenHelper
 */
public class TrackMeOpenHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "trackme.db";
    private static final int DATABASE_VERSION = 1;

    public TrackMeOpenHelper(Context context) {
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

    private List<String> createScripts() {
        List<String> scripts = new ArrayList<>();


        return scripts;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
