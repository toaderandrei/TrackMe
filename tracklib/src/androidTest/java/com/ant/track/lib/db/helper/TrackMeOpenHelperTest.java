package com.ant.track.lib.db.helper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.ant.track.lib.base.BaseTest;
import com.ant.track.lib.db.provider.TrackMeContract;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.utils.TestDbUtils;
import com.ant.track.lib.utils.TestUtils;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertTrue;

/**
 * Test class for the SqliteOpenHelper.
 */
public class TrackMeOpenHelperTest extends BaseTest {

    private static final long DEFAULT_LONG_ID = 100;
    private static final String DEFAULT_NAME_1 = "DEFAULT_NAME_1";
    private static final String DEFAULT_NAME_2 = "DEFAULT_NAME_2";
    private TrackMeOpenHelper trackMeOpenHelper;

    @Override
    protected void init() {
        getTargetContext().deleteDatabase(TrackMeOpenHelper.DATABASE_NAME);
        trackMeOpenHelper = new TrackMeOpenHelper(getTargetContext());
    }

    @Override
    protected void destroy() {
        trackMeOpenHelper.close();
        trackMeOpenHelper = null;
    }

    @Test
    public void testOnCreate() {
        trackMeOpenHelper.onCreate(trackMeOpenHelper.getWritableDatabase());
        assertTrue(trackMeOpenHelper.getWritableDatabase().isOpen());
    }


    @Test
    public void testInsert() {
        trackMeOpenHelper.onCreate(trackMeOpenHelper.getWritableDatabase());
        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_NAME_1);

        long inserted = trackMeOpenHelper.getWritableDatabase().insert(TrackMeContract.RouteEntry.TABLE_NAME,
                null,
                contentValues);
        assertTrue(inserted > 0);
    }


    @Test
    public void testUpdate() {
        long now = System.currentTimeMillis();
        trackMeOpenHelper.onCreate(trackMeOpenHelper.getWritableDatabase());
        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_NAME_1);
        RouteStats routeStats = TestUtils.createRouteStats(now);

        long inserted = trackMeOpenHelper.getWritableDatabase().insert(TrackMeContract.RouteEntry.TABLE_NAME,
                null,
                contentValues);
        assertTrue(inserted > 0);

        ContentValues contentValues2 = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_NAME_2, routeStats);
        String where = "";
        where += "_id = ?";
        String idQuery = String.valueOf(DEFAULT_LONG_ID);
        long updated = trackMeOpenHelper.getWritableDatabase().update(TrackMeContract.RouteEntry.TABLE_NAME,
                contentValues2,
                where,
                new String[]{idQuery});
        assertTrue(updated > 0);
    }

    @Test
    public void testOnDelete() {
        long now = System.currentTimeMillis();
        trackMeOpenHelper.onCreate(trackMeOpenHelper.getWritableDatabase());


        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_NAME_2, routeStats);

        long inserted = trackMeOpenHelper.getWritableDatabase().insert(TrackMeContract.RouteEntry.TABLE_NAME,
                null,
                contentValues);
        assertTrue(inserted > 0);
        String where = "";
        where += "_id = ?";
        String idQuery = String.valueOf(DEFAULT_LONG_ID);

        int deleted = trackMeOpenHelper.getWritableDatabase().delete(TrackMeContract.RouteEntry.TABLE_NAME,
                where,
                new String[]{idQuery});
        assertTrue(deleted > 0);
    }

    @Test
    public void testInsertSame() {
        trackMeOpenHelper.onCreate(trackMeOpenHelper.getWritableDatabase());
        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_NAME_1);

        long inserted = trackMeOpenHelper.getWritableDatabase().insert(TrackMeContract.RouteEntry.TABLE_NAME,
                null,
                contentValues);
        assertTrue(inserted > 0);
        //insert the same value twice
        contentValues = TestDbUtils.getRouteContentValues(DEFAULT_NAME_2);
        long inserted2 = trackMeOpenHelper.getWritableDatabase().insertWithOnConflict(TrackMeContract.RouteEntry.TABLE_NAME,
                null,
                contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);
        assertTrue(inserted2 > 0);
    }
}
