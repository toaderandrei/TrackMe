package com.ant.track.lib.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ant.track.lib.db.columns.GenericColumn;
import com.ant.track.lib.db.tables.DatabaseTable;

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
    public static final String DATABASE_NAME = "trackme.db";

    // default statements used for different queries
    public static final String PRIMARY_KEY_STATEMENT = " PRIMARY KEY ";
    public static final String PRIMARY_KEY_AUTOINCREMENT_STATEMENT = " PRIMARY KEY AUTOINCREMENT ";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    private static final String LEFT_PARENTHESES = "( \n";

    private static final String SPACE_CHARACTER = " ";
    private static final String CONFLICT_REPLACE_TEXT = ") ON CONFLICT IGNORE ";
    public static final String UNIQUE_KEY_STATEMENT = " UNIQUE (";
    // end of default queries

    private static final int DATABASE_VERSION = 19;
    public static final String COMMA_STRING = ",";
    public static final String EMPTY_STRING = "";
    public static final String TAB_STRING = "\n";

    public TrackMeOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        Collection<String> scripts = createScripts();
        for (String query : scripts) {
            db.execSQL(query);
        }
    }

    private Collection<String> createScripts() {
        Collection<String> scripts = new ArrayList<>();

        for (DatabaseTable table : DatabaseTable.values()) {
            String sqlQuery = CREATE_TABLE + table.getName() + LEFT_PARENTHESES;
            String primaryKeyQuery = getQueryScript(table);
            sqlQuery += primaryKeyQuery;
            sqlQuery += ")\n";
            scripts.add(sqlQuery);
        }
        return scripts;
    }

    private String getQueryScript(DatabaseTable table) {
        String query = "";
        for (int k = 0; k < table.getColumns().length; k++) {
            GenericColumn column = table.getColumns()[k];
            query += column.getFieldName() + SPACE_CHARACTER + column.getDataType().getRowType();
            if (column.isPrimaryKey() && column.isAutoIncremented()) {
                query += PRIMARY_KEY_AUTOINCREMENT_STATEMENT;
            } else if (column.isPrimaryKey()) {
                query += PRIMARY_KEY_STATEMENT;
            } else if (column.isUnique()) {
                query += UNIQUE_KEY_STATEMENT + column.getFieldName() + CONFLICT_REPLACE_TEXT;
            }
            query += (k == table.getColumns().length - 1 ? EMPTY_STRING : COMMA_STRING) + TAB_STRING;
        }
        return query;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop tables
        List<String> dropScripts = createDropScripts();
        for (String query : dropScripts) {
            db.execSQL(query);
        }
    }

    /**
     * creates a list of all the drop table queries and update them
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
