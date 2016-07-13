package com.ant.track.lib.stats;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Route stats test.
 */
@RunWith(AndroidJUnit4.class)
public class RouteStatsTest {

    private RouteStats routeStats;
    private static long now;

    @Before
    public void setup() {
        now = System.currentTimeMillis();
        routeStats = new RouteStats(now);
    }

    @After
    public void tearDown() {
        routeStats = null;
    }

    @Test
    public void testParcelable() {
        //todo do the parcelable test.
    }
}
