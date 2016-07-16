package com.ant.track.lib.db.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.ant.track.lib.base.BaseProviderTest;
import com.ant.track.lib.db.DatabaseConstants;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.utils.TestDbUtils;
import com.ant.track.lib.utils.TestUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test ckass for the TrackMeProvider.
 */
public class TrackMeDbProviderTest extends BaseProviderTest {

    public static final int DEFAULT_LONG_ID = 24;
    public static final String DEFAULT_ROUTE_22 = "route22";
    private static final String DEFAULT_ROUTE_33 = "route33";
    private static final long DEFAULT_TOTAL_TIME = 8000;
    private static final double DEFAULT_SPEED = 12;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public TrackMeDbProviderTest() {
        super(TrackMeDbProvider.class, TrackMeContract.CONTENT_AUTHORITY);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void destroy() {
        getProvider().shutdown();
    }

    @Test
    public void testGetTypes() {
        ContentProvider cp = getProvider();
        Uri uriItemSingle = TrackMeContract.RouteEntry.buildRouteUri(12);
        Uri uriRoute = Uri.parse("content://com.ant.track/route");
        //routes
        assertEquals(TrackMeContract.RouteEntry.CONTENT_ITEMTYPE, cp.getType(uriItemSingle));
        assertEquals(TrackMeContract.RouteEntry.CONTENT_TYPE, cp.getType(uriRoute));
        //route points
        Uri uriRoutePointItem = TrackMeContract.RoutePointEntry.buildRoutePointUri(12);
        assertEquals(TrackMeContract.RoutePointEntry.CONTENT_ITEMTYPE, cp.getType(uriRoutePointItem));
        Uri uriRoutePoint = Uri.parse("content://com.ant.track/route_point");
        assertEquals(TrackMeContract.RoutePointEntry.CONTENT_TYPE, cp.getType(uriRoutePoint));

        Uri uriRouteCheckPointItem = TrackMeContract.RouteCheckPointEntry.buildRouteCheckPointUri(12);
        assertEquals(TrackMeContract.RouteCheckPointEntry.CONTENT_ITEMTYPE, cp.getType(uriRouteCheckPointItem));
        Uri uriRouteCheckPoint = Uri.parse("content://com.ant.track/route_check_point");
        assertEquals(TrackMeContract.RouteCheckPointEntry.CONTENT_TYPE, cp.getType(uriRouteCheckPoint));
    }

    @Test
    public void testInsertException() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);
        ContentValues contentValues = TestDbUtils.getRouteContentValues(24, DEFAULT_ROUTE_22, routeStats);
        contentValues.remove(TrackMeContract.RouteEntry.START_TIME);
        try {
            cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues);
            fail("expected exception to be thrown");
        } catch (IllegalArgumentException illex) {
            //
        }
    }

    @Test
    public void testInsert() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(24, DEFAULT_ROUTE_22, routeStats);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);
    }

    @Test
    public void testInsertRouteAndQuery() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_ROUTE_22, routeStats);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        String[] projection = new String[]{TrackMeContract.RouteEntry._ID, TrackMeContract.RouteEntry.NAME};
        String selection = TrackMeContract.RouteEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};
        String sortOrder = DatabaseConstants.DEFAULT_ORDER_COLUMN;
        Cursor cursor = cp.query(TrackMeContract.RouteEntry.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        String name = cursor.getString(cursor.getColumnIndex(TrackMeContract.RouteEntry.NAME));
        assertEquals(DEFAULT_ROUTE_22, name);
    }

    @Test
    public void testInsertRouteAndQueryUsingUri() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_ROUTE_22, routeStats);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        Uri insertUriToQuery = TrackMeContract.RouteEntry.buildRouteUri(insertedUri);
        String[] projection = new String[]{TrackMeContract.RouteEntry._ID, TrackMeContract.RouteEntry.NAME};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = DatabaseConstants.DEFAULT_ORDER_COLUMN;
        Cursor cursor = cp.query(insertUriToQuery, projection, selection, selectionArgs, sortOrder);

        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        String name = cursor.getString(cursor.getColumnIndex(TrackMeContract.RouteEntry.NAME));
        assertEquals(DEFAULT_ROUTE_22, name);
    }


    @Test
    public void testInsertRoutePointAndQueryUsingUri() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RoutePoint routePoint = TestUtils.createRoutePoint(DEFAULT_LONG_ID, now);
        ContentValues contentValues = TestDbUtils.getRoutePointContentValues(routePoint);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RoutePointEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        String selection = TrackMeContract.RoutePointEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};

        routePoint.setSpeed(DEFAULT_SPEED);
        contentValues = TestDbUtils.getRoutePointContentValues(routePoint);
        int updated = cp.update(TrackMeContract.RoutePointEntry.CONTENT_URI, contentValues, selection, selectionArgs);
        assertTrue(updated > 0);


        Uri insertUriToQuery = TrackMeContract.RoutePointEntry.buildRoutePointUri(insertedUri);
        String[] projection = new String[]{TrackMeContract.RouteEntry._ID, TrackMeContract.RoutePointEntry.SPEED};
        selection = null;
        selectionArgs = null;
        String sortOrder = DatabaseConstants.DEFAULT_ORDER_COLUMN;
        Cursor cursor = cp.query(insertUriToQuery, projection, selection, selectionArgs, sortOrder);

        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        double speed = cursor.getDouble(cursor.getColumnIndex(TrackMeContract.RoutePointEntry.SPEED));
        assertEquals(DEFAULT_SPEED, speed);
    }

    @Test
    public void testInsertRoutPointAndDelete() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RoutePoint routePoint = TestUtils.createRoutePoint(DEFAULT_LONG_ID, now);

        ContentValues contentValues = TestDbUtils.getRoutePointContentValues(routePoint);

        Uri insertedUri = cp.insert(TrackMeContract.RoutePointEntry.CONTENT_URI, contentValues);
        long insertedUriLong = ContentUris.parseId(insertedUri);
        assertTrue("created ok", insertedUriLong > 0);

        String selection = TrackMeContract.RoutePointEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(25)};
        int deleted = cp.delete(TrackMeContract.RoutePointEntry.CONTENT_URI, selection, selectionArgs);
        assertTrue(deleted <= 0);

        //delete the right value
        selection = TrackMeContract.RoutePointEntry._ID + "=?";
        selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};
        deleted = cp.delete(TrackMeContract.RoutePointEntry.CONTENT_URI, selection, selectionArgs);
        assertTrue(deleted > 0);
    }

    @Test
    public void testInsertRouteAndDelete() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_ROUTE_22, routeStats);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        String selection = TrackMeContract.RouteEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};
        int deleted = cp.delete(TrackMeContract.RouteEntry.CONTENT_URI, selection, selectionArgs);
        assertTrue(deleted > 0);
    }

    @Test
    public void testInsertRouteAndUpdate() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_ROUTE_22, routeStats);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        String selection = TrackMeContract.RouteEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};
        routeStats.setTotalTime(DEFAULT_TOTAL_TIME);
        contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_ROUTE_33, routeStats);
        int updated = cp.update(TrackMeContract.RouteEntry.CONTENT_URI, contentValues, selection, selectionArgs);
        assertTrue(updated > 0);

        //check update
        String[] projection = new String[]{TrackMeContract.RouteEntry._ID, TrackMeContract.RouteEntry.NAME};
        selection = TrackMeContract.RouteEntry._ID + "=?";
        selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};
        String sortOrder = DatabaseConstants.DEFAULT_ORDER_COLUMN;
        Cursor cursor = cp.query(TrackMeContract.RouteEntry.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        //check the values
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        String name = cursor.getString(cursor.getColumnIndex(TrackMeContract.RouteEntry.NAME));
        assertEquals(DEFAULT_ROUTE_33, name);
    }

    @Test
    public void testInsertRouteUsingUriAndUpdate() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_ROUTE_22, routeStats);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        Uri uri = TrackMeContract.RouteEntry.buildRouteUri(insertedUri);

        String selection = null;
        String[] selectionArgs = null;
        routeStats.setTotalTime(DEFAULT_TOTAL_TIME);
        routeStats.setAvgSpeed(DEFAULT_SPEED);
        contentValues = TestDbUtils.getRouteContentValues(DEFAULT_LONG_ID, DEFAULT_ROUTE_33, routeStats);
        int updated = cp.update(uri, contentValues, selection, selectionArgs);
        assertTrue(updated > 0);
    }


    @Test
    public void testInsertRoutPointAndUpdate() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RoutePoint routePoint = TestUtils.createRoutePoint(DEFAULT_LONG_ID, now);
        ContentValues contentValues = TestDbUtils.getRoutePointContentValues(routePoint);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RoutePointEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        String selection = TrackMeContract.RoutePointEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};

        routePoint.setSpeed(DEFAULT_SPEED);
        contentValues = TestDbUtils.getRoutePointContentValues(routePoint);
        int updated = cp.update(TrackMeContract.RoutePointEntry.CONTENT_URI, contentValues, selection, selectionArgs);
        assertTrue(updated > 0);

        //check update
        String[] projection = new String[]{TrackMeContract.RoutePointEntry._ID, TrackMeContract.RoutePointEntry.SPEED};
        selection = TrackMeContract.RoutePointEntry._ID + "=?";
        selectionArgs = new String[]{String.valueOf(DEFAULT_LONG_ID)};
        String sortOrder = DatabaseConstants.DEFAULT_ORDER_COLUMN;
        Cursor cursor = cp.query(TrackMeContract.RoutePointEntry.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        //check the values
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        double speed = cursor.getDouble(cursor.getColumnIndex(TrackMeContract.RoutePointEntry.SPEED));
        assertEquals(DEFAULT_SPEED, speed);
    }

    @Test
    public void testInsertRoutPointFromUriAndUpdate() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RoutePoint routePoint = TestUtils.createRoutePoint(DEFAULT_LONG_ID, now);
        ContentValues contentValues = TestDbUtils.getRoutePointContentValues(routePoint);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RoutePointEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);

        Uri updatingUri = TrackMeContract.RoutePointEntry.buildRoutePointUri(insertedUri);
        String selection = null;
        String[] selectionArgs = null;

        routePoint.setSpeed(DEFAULT_SPEED);
        contentValues = TestDbUtils.getRoutePointContentValues(routePoint);
        int updated = cp.update(updatingUri, contentValues, selection, selectionArgs);
        assertTrue(updated > 0);
    }


    @Test
    public void testInsertNull() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);

        ContentValues contentValues = TestDbUtils.getRouteContentValues(24, "route22", routeStats);
        cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues);
        try {
            cp.insert(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, null);
            fail("expected exception to be thrown");
        } catch (SQLException sqex) {

        }
    }

    @Test
    public void testBulkInsert() {

        long now = System.currentTimeMillis();
        ContentProvider cp = getProvider();
        RouteStats routeStats = TestUtils.createRouteStats(now);


        ContentValues contentValues = TestDbUtils.getRouteContentValues(24, DEFAULT_ROUTE_22, routeStats);

        RouteStats routeStats2 = TestUtils.createRouteStats(System.currentTimeMillis());

        ContentValues contentValues2 = TestDbUtils.getRouteContentValues(25, DEFAULT_ROUTE_33, routeStats2);
        ContentValues[] values = new ContentValues[2];
        values[0] = contentValues;
        values[1] = contentValues2;

        int insertedBulk = cp.bulkInsert(TrackMeContract.RouteEntry.CONTENT_URI, values);
        assertTrue("created ok", insertedBulk > 0);
    }
}
