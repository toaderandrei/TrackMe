package com.ant.track.lib.model;

import android.os.Parcel;

import com.ant.track.lib.base.BaseTest;
import com.ant.track.lib.stats.RouteStats;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for Route
 * */
public class RouteTest extends BaseTest{


    private static final String DEFAULT_ROUTE = "default_route";
    public static final int NUMBER_OF_POINTS = 20;

    private Route test;

    @Override
    protected void init() {
        test = new Route();
    }

    @Override
    protected void destroy() {
        test = null;
    }

    @Test
    public void testParcelable() {
        long now = System.currentTimeMillis();
        // Obtain a Parcel object and write the parcelable object to it:
        Parcel parcel = Parcel.obtain();
        RouteStats routeStats = new RouteStats(now);
        test.setRouteStats(routeStats);
        test.setRouteName(DEFAULT_ROUTE);
        test.setNumberOfPoints(NUMBER_OF_POINTS);
        test.writeToParcel(parcel, 0);
        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        Route createdFromParcel = Route.CREATOR.createFromParcel(parcel);
        //assertEquals(test, createdFromParcel);
        assertEquals(NUMBER_OF_POINTS, createdFromParcel.getNumberOfPoints());
        assertEquals(DEFAULT_ROUTE, createdFromParcel.getRouteName());
    }
}
