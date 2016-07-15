package com.ant.track.lib.db.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.SQLException;
import android.net.Uri;

import com.ant.track.lib.base.BaseProviderTest;
import com.ant.track.lib.db.TrackMeContract;
import com.ant.track.lib.db.TrackMeDbProvider;
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
        ContentValues contentValues = TestDbUtils.getRouteContentValues(24, "route22", routeStats);
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

        ContentValues contentValues = TestDbUtils.getRouteContentValues(24, "route22", routeStats);

        long insertedUri = ContentUris.parseId(cp.insert(TrackMeContract.RouteEntry.CONTENT_URI, contentValues));
        assertTrue("created ok", insertedUri > 0);
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
}
