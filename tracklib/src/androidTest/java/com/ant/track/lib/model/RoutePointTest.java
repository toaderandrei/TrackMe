package com.ant.track.lib.model;

import android.os.Parcel;

import com.ant.track.lib.base.BaseTest;
import com.ant.track.lib.utils.LocationUtilsTest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for RoutePoint.
 */
public class RoutePointTest extends BaseTest {


    private static final float DEFAULT_SPEED = 10.0f;

    private RoutePoint test;

    @Override
    protected void init() {
        test = new RoutePoint();
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
        test.setLocation(LocationUtilsTest.getRandomLocation());
        test.setSpeed(DEFAULT_SPEED);
        test.setTime(now);
        test.writeToParcel(parcel, 0);
        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        RoutePoint createdFromParcel = RoutePoint.CREATOR.createFromParcel(parcel);
        //assertEquals(test, createdFromParcel);
        assertEquals(now, createdFromParcel.getTime());
        assertEquals(DEFAULT_SPEED, createdFromParcel.getSpeed());
    }
}
