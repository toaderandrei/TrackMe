package com.ant.track.lib.model;

import android.os.Parcel;

import com.ant.track.lib.base.BaseTest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for RouteCheckPoint
 */
public class RouteCheckPointTest extends BaseTest {

    private static final float DEFAULT_SPEED = 10.0f;

    private RouteCheckPoint test;

    @Override
    protected void init() {
        test = new RouteCheckPoint();
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
        test.setSpeed(DEFAULT_SPEED);
        test.setTime(now);
        test.writeToParcel(parcel, 0);
        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        RouteCheckPoint createdFromParcel = RouteCheckPoint.CREATOR.createFromParcel(parcel);
        //assertEquals(test, createdFromParcel);
        assertEquals(now, createdFromParcel.getTime());
        assertEquals(DEFAULT_SPEED, createdFromParcel.getSpeed());
    }
}
