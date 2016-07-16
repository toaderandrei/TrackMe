package com.ant.track.lib.stats;

import android.os.Parcel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for Location proximities
 */
@RunWith(JUnit4.class)
public class LocationProximitiesManagerTest {

    private static final double DEFAULT_DOUBLE = 10.0d;

    private static final double DEFAULT_DOUBLE_2 = 20.0d;
    public static final double DEFAULT_MIN = 10.0;
    public static final double DEFAULT_MAX = 100.0;

    private LocationProximitiesManager locationProximitiesManager;

    @Before
    public void setup() {
        locationProximitiesManager = new LocationProximitiesManager();
    }

    @After
    public void tearDown() {
        locationProximitiesManager = null;
    }

    @Test
    public void testDefault() {
        locationProximitiesManager.update(DEFAULT_DOUBLE);
        assertEquals(DEFAULT_DOUBLE, locationProximitiesManager.getMax());
        assertEquals(DEFAULT_DOUBLE, locationProximitiesManager.getMin());
    }

    @Test
    public void testMinMax() {
        locationProximitiesManager.update(DEFAULT_DOUBLE);
        locationProximitiesManager.update(DEFAULT_DOUBLE_2);
        assertEquals(DEFAULT_DOUBLE_2, locationProximitiesManager.getMax());
        assertEquals(DEFAULT_DOUBLE, locationProximitiesManager.getMin());
    }

    @Test
    public void testParcelable() {
        LocationProximitiesManager test = new LocationProximitiesManager();
        // Obtain a Parcel object and write the parcelable object to it:
        Parcel parcel = Parcel.obtain();
        test.update(DEFAULT_MAX);
        test.update(DEFAULT_MIN);
        test.writeToParcel(parcel, 0);
        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        LocationProximitiesManager createdFromParcel = LocationProximitiesManager.CREATOR.createFromParcel(parcel);
        //assertEquals(test, createdFromParcel);
        assertEquals(DEFAULT_MIN, createdFromParcel.getMin());
        assertEquals(DEFAULT_MAX, createdFromParcel.getMax());
    }
}
