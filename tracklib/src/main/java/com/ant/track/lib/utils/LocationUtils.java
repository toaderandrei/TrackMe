package com.ant.track.lib.utils;

import android.location.Location;

/**
 * Location utils
 */
public class LocationUtils {

    /**
     * the accuracy is in meters
     */
    public static final int RECORDING_GPS_ACCURACY_DEFAULT = 50;
    public static final int RECORDING_GPS_ACCURACY_EXCELLENT = 10;
    public static final int RECORDING_GPS_ACCURACY_POOR = 200;


    public static final double PAUSE_LATITUDE = 100;

    public static final double PAUSE_LONGITUDE = 200;

    /**
     * Checks if a given location is a valid (i.e. physically possible) location
     * on Earth. Note: The special separator locations (which have latitude = 100)
     * will not qualify as valid. Neither will locations with lat=0 and lng=0 as
     * these are most likely "bad" measurements which often cause trouble.
     *
     * @param location the location to test
     * @return true if the location is a valid location.
     */
    public static boolean isValidLocation(Location location) {
        return location != null && Math.abs(location.getLatitude()) <= 90
                && Math.abs(location.getLongitude()) <= 180;
    }
}
