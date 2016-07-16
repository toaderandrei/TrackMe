package com.ant.track.lib.stats;

import android.location.Location;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;

import com.ant.track.lib.utils.LocationUtils;
import com.ant.track.lib.utils.LocationUtilsTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

/**
 * Unit test for RouteStatsManager.
 */
@RunWith(AndroidJUnit4.class)
public class RouteStatsManagerTest extends AndroidJUnitRunner {

    private static final int SMALL_TIME_DIFF = 10;
    private RouteStatsManager manager;

    private static Location DEFAULT_INVALID_LOCATION = new Location("GPS");

    private static Location DEFAULT_LINZ_LOC = new Location("GPS");
    private static Location DEFAULT_LINZ_LOC_2 = new Location("GPS");
    private static Location DEFAULT_LINZ_LOC_3 = new Location("GPS");

    private static final long now = System.currentTimeMillis();

    @Before
    public void setup() {
        initDefaultLocations();
        manager = new RouteStatsManager(now);
    }

    @After
    public void tearDown() {
        manager = null;
    }

    @Test
    public void testConstructorInit() {
        assertNotNull(manager);
        long time = manager.getCurrentRouteStats().getStartTime();
        assertEquals(now, time);
    }

    @Test
    public void testGetAverage() {
        assertNotNull(manager);
        double speed = manager.getAvgSpeed();
        //it is not define first.
        assertEquals(0.0, speed);
    }

    @Test
    public void testAddPausedLocation() {
        Location location = new Location("GPS");
        location.setLatitude(LocationUtils.PAUSE_LATITUDE);
        manager.addLocationToStats(location, 0.1);
        assertEquals(0.0, manager.getAvgSpeed());
    }

    @Test
    public void testAddValidPausedLocation() {

        Location loc1 = DEFAULT_LINZ_LOC;
        manager.addLocationToStats(loc1, 100);
        //we have one location still nothing
        assertEquals(0.0, manager.getAvgSpeed());
        Location loc2 = DEFAULT_LINZ_LOC_2;
        loc2.setSpeed(loc1.getSpeed() + 3);

        loc2.setTime(loc1.getTime() + 100 * SMALL_TIME_DIFF);

        manager.addLocationToStats(loc2, 1000);
        assertTrue(manager.getAvgSpeed() > 0);
        Location loc3 = DEFAULT_LINZ_LOC_3;
        double avgSpeed = manager.getAvgSpeed();

        loc3.setTime(loc2.getTime() + 100 * SMALL_TIME_DIFF);
        manager.addLocationToStats(loc3, 1000);
        assertNotEquals(avgSpeed, manager.getAvgSpeed());
    }


    @Test
    public void testAddPausedLocAfterTwoValidLocation() {

        Location loc1 = DEFAULT_LINZ_LOC;
        manager.addLocationToStats(loc1, 100);
        //we have one location still nothing
        assertEquals(0.0, manager.getAvgSpeed());
        Location loc2 = DEFAULT_LINZ_LOC_2;
        loc2.setSpeed(loc1.getSpeed() + 8);

        loc2.setTime(loc1.getTime() + 100 * SMALL_TIME_DIFF);

        manager.addLocationToStats(loc2, 1000);
        assertTrue(manager.getAvgSpeed() > 0);
        double distance = manager.getCurrentSegmentStats().getTotalDistance();
        Location loc3 = DEFAULT_LINZ_LOC_3;
        loc3.setLatitude(LocationUtils.PAUSE_LATITUDE);
        loc3.setLongitude(LocationUtils.PAUSE_LONGITUDE);

        loc3.setTime(loc2.getTime() + 100 * SMALL_TIME_DIFF);
        manager.addLocationToStats(loc3, 1000);
        assertNotEquals(distance, manager.getCurrentRouteStats().getTotalDistance());
    }

    @Test
    public void testLocationData() {
        Location loc1 = DEFAULT_LINZ_LOC;
        manager.addLocationToStats(loc1, 100);
        //we have one location still nothing
        assertEquals(0.0, manager.getAvgSpeed());
        Location loc2 = DEFAULT_LINZ_LOC_2;
        loc2.setSpeed(loc1.getSpeed() + 8);

        loc2.setTime(loc1.getTime() + 100 * SMALL_TIME_DIFF);

        manager.addLocationToStats(loc2, 1000);
        assertTrue(manager.getAvgSpeed() > 0);
        //we check to see that in the current segment stats we only have one location
        assertEquals(loc2.getLatitude(), manager.getCurrentSegmentStats().getLatitudeMin());
        assertEquals(loc2.getLatitude(), manager.getCurrentSegmentStats().getLatitudeMax());
        assertEquals(loc2.getLongitude(), manager.getCurrentSegmentStats().getLongitudeMin());
        //we now update a third one
        Location loc3 = DEFAULT_LINZ_LOC_3;
        loc3.setTime(loc2.getTime() + 100 * SMALL_TIME_DIFF);
        manager.addLocationToStats(loc3, 1000);
        assertEquals(loc2.getLatitude(), manager.getCurrentSegmentStats().getLatitudeMin());
        assertEquals(loc3.getLongitude(), manager.getCurrentSegmentStats().getLongitudeMax());
    }

    @Test
    public void testAddSpeedZero() {

        Location loc1 = DEFAULT_LINZ_LOC;
        loc1.setSpeed(0.0f);
        manager.addLocationToStats(loc1, 100);

        Location loc2 = DEFAULT_LINZ_LOC_2;
        loc2.setSpeed(loc1.getSpeed());
        loc2.setTime(loc1.getTime() + 100 * SMALL_TIME_DIFF);

        manager.addLocationToStats(loc2, 100);
        assertEquals(0.0, manager.getAvgSpeed());
    }


    @Test
    public void testAddInvalidAndNotPausedLocation() {

        Location loc1 = DEFAULT_LINZ_LOC;
        loc1.setSpeed(0.0f);
        manager.addLocationToStats(loc1, 100);

        Location loc2 = DEFAULT_INVALID_LOCATION;
        loc2.setSpeed(loc1.getSpeed());
        loc2.setTime(loc1.getTime() + 100 * SMALL_TIME_DIFF);

        manager.addLocationToStats(loc2, 100);
        assertEquals(0.0, manager.getAvgSpeed());
    }

    @Test
    public void testAddGodSpeed() {

        Location loc1 = DEFAULT_LINZ_LOC;
        loc1.setSpeed(0.0f);
        manager.addLocationToStats(loc1, 100);

        Location loc2 = DEFAULT_LINZ_LOC_3;
        loc2.setSpeed(loc1.getSpeed() * 1000);
        loc2.setTime(loc1.getTime() + 100 * SMALL_TIME_DIFF);

        manager.addLocationToStats(loc2, 100);
        assertEquals(0.0, manager.getAvgSpeed());
    }

    @Test
    public void testAddSpeedErrorSpeed() {

        Location loc1 = DEFAULT_LINZ_LOC;
        loc1.setSpeed(0.0f);
        manager.addLocationToStats(loc1, 100);

        Location loc2 = DEFAULT_LINZ_LOC_2;
        loc2.setSpeed(loc1.getSpeed());
        loc2.setTime(loc1.getTime() + 100 * SMALL_TIME_DIFF);

        loc2.setSpeed(128.30f);
        manager.addLocationToStats(loc2, 100);

        assertEquals(0.0, manager.getAvgSpeed());
    }

    @Test
    public void testAddSpeedZeroAfterGood() {

        Location loc1 = DEFAULT_LINZ_LOC;
        manager.addLocationToStats(loc1, 100);
        //we have one location still nothing
        assertEquals(0.0, manager.getAvgSpeed());
    }

    @Test
    public void testTotalTime() {
        Location loc1 = DEFAULT_LINZ_LOC;
        long time = System.currentTimeMillis();
        long time2 = time + 100;
        long time3 = time + 200;
        manager = new RouteStatsManager(time);
        loc1.setTime(time2);

        manager.addLocationToStats(loc1, 100);
        Location loc2 = DEFAULT_LINZ_LOC_2;
        loc2.setTime(time3);
        manager.addLocationToStats(loc2, 200);
        assertEquals(200, manager.getCurrentSegmentStats().getTotalTime());
        //check for stop time and start time
        assertEquals(time, manager.getCurrentSegmentStats().getStartTime());
        //stop time
        assertEquals(time + 200, manager.getCurrentSegmentStats().getStopTime());
    }


    private void initDefaultLocations() {
        DEFAULT_INVALID_LOCATION.setLatitude(95.01);
        DEFAULT_INVALID_LOCATION.setLongitude(200.01);
        DEFAULT_LINZ_LOC.setLatitude(48.3014);
        DEFAULT_LINZ_LOC.setLongitude(14.2925);
        DEFAULT_LINZ_LOC.setSpeed(LocationUtilsTest.getRandomSpeed(15, 20));

        DEFAULT_LINZ_LOC_2.setLatitude(48.3018);
        DEFAULT_LINZ_LOC_2.setLongitude(14.2943);
        DEFAULT_LINZ_LOC_2.setSpeed(LocationUtilsTest.getRandomSpeed(20, 25));

        DEFAULT_LINZ_LOC_3.setLatitude(48.3022);
        DEFAULT_LINZ_LOC_3.setLongitude(14.2954);
        DEFAULT_LINZ_LOC_3.setSpeed(LocationUtilsTest.getRandomSpeed(25, 30));
    }
}
