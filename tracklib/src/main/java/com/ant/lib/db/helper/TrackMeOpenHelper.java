package com.ant.lib.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.ant.lib.db.columns.GenericColumn;
import com.ant.lib.db.tables.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Extension of the SQLiteOpenHelper
 */
public class TrackMeOpenHelper extends SQLiteOpenHelper {

    private static final String DROP_START_QUERY = "DROP TABLE IF EXISTS ";
    /**
     * database name
     */
    private static final String DATABASE_NAME = "trackme.db";

    // default statements used for different queries
    public static final String PRIMARY_KEY_STATEMENT = "PRIMARY KEY, ";
    public static final String PRIMARY_KEY_AUTOINCREMENT_STATEMENT = " PRIMARY KEY AUTOINCREMENT, ";

    public static final String COMMA_CHARACTER = ",";
    public static final String END_CREATE_TABLE_QUERY = ")\n";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    private static final String LEFT_PARANTHESIS = "( \n";
    private static final String RIGHT_PARANTHESIS = ")";

    private static final String SPACE_CHARACTER = " ";
    private static final String CONFLICT_REPALCE_TEXT = ") ON CONFLICT IGNORE ";
    public static final String UNIQUE_KEY_STATEMENT = " UNIQUE (";
    // end of default queries

    private static final int DATABASE_VERSION = 1;

    public TrackMeOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    void createTables(SQLiteDatabase db) {
        Collection<String> scripts = createScripts();
        for (String query : scripts) {
            db.execSQL(query);
        }
    }

    private Collection<String> createScripts() {
        Collection<String> scripts = new ArrayList<>();

        for (DatabaseTable table : DatabaseTable.values()) {
            String sqlQuery = CREATE_TABLE + table.getName() + LEFT_PARANTHESIS;
            String primaryKeyQuery = getPrimaryKeyScript(table);
            sqlQuery += primaryKeyQuery;
            scripts.add(primaryKeyQuery);
            //Collection<String> secondKeyScripts =
        }
        return scripts;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop tables
        List<String> dropScripts = createDropScripts();
        for (String query : dropScripts) {
            db.execSQL(query);
        }
    }

    private String getPrimaryKeyScript(DatabaseTable table) {
        String query = "";
        for (GenericColumn column : table.getColumns()) {
            if (column.isPrimaryKey() && column.isAutoIncremented()) {
                query += column.getFieldName();
                query += PRIMARY_KEY_AUTOINCREMENT_STATEMENT;
            } else if (column.isPrimaryKey()) {
                query += PRIMARY_KEY_STATEMENT;
            } else if (column.isUnique()) {
                query += UNIQUE_KEY_STATEMENT + column.getFieldName() + CONFLICT_REPALCE_TEXT;
            }
        }

        return query;
    }

/**

 // Set up the location column as a foreign key to location table.
 " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
 LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), "

 */


    /**
     * creates a list of all the drop table queries and add them
     * to a list so we can delete them in a simple way.
     *
     * @return array list containing the drop table queries.
     */
    private List<String> createDropScripts() {
        List<String> dropScripts = new ArrayList<>();
        DatabaseTable[] tables = DatabaseTable.values();
        for (DatabaseTable table : tables) {
            String query_script = DROP_START_QUERY + table.getName() + " ;";
            dropScripts.add(query_script);
        }
        return dropScripts;
    }
}
