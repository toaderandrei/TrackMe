package com.ant.track.lib.stats;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.model.RoutePoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Route stats test.
 */
@RunWith(AndroidJUnit4.class)
public class RouteStatsTest {

    public static final double DEFAULT_MAX_SPEED = 34.4d;

    public static final double DEFAULT_MIN_SPEED = 14.4d;

    public static final double DEFAULT_SPEED = 24.4d;
    private List<RouteCheckPoint> routeCheckPoints;
    private List<RoutePoint> routePointList;

    private RouteStats routeStats;
    private static long now;

    @Before
    public void setup() {
        now = System.currentTimeMillis();
        routeStats = new RouteStats(now);
        routePointList = new ArrayList<>();
        routeCheckPoints = new ArrayList<>();
    }

    @After
    public void tearDown() {
        this.routeStats = null;
        this.routeCheckPoints = null;
        this.routePointList = null;
    }

    @Test
    public void testParcelable() {
        RouteStats routeStatsLocal = new RouteStats(now);
        Parcel parcel = Parcel.obtain();
        long stopTime = System.currentTimeMillis();

        routeStatsLocal.setStopTime(stopTime);
        final double maxSpeed = DEFAULT_MAX_SPEED;
        final double minSpeed = DEFAULT_MIN_SPEED;
        final double speed = DEFAULT_SPEED;

        routeStatsLocal.setMaxSpeed(maxSpeed);
        routeStatsLocal.setMinSpeed(minSpeed);
        routeStatsLocal.setAvgSpeed(speed);

        routeStatsLocal.writeToParcel(parcel, 0);
        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        RouteStats createdFromParcel = RouteStats.CREATOR.createFromParcel(parcel);
        assertEquals(routeStatsLocal.getAvgSpeed(), createdFromParcel.getAvgSpeed());
        assertEquals(routeStatsLocal.getStopTime(), createdFromParcel.getStopTime());
        assertEquals(routeStatsLocal.getMaxSpeed(), createdFromParcel.getMaxSpeed());
        assertEquals(routeStatsLocal.getMinSpeed(), createdFromParcel.getMinSpeed());
    }
}
