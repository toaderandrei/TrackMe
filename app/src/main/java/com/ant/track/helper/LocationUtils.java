package com.ant.track.helper;

import android.location.Location;

/**
 * Created by Toader on 6/3/2015.
 */
public class LocationUtils {

    public static final int RECORDING_GPS_ACCURACY_DEFAULT = 50;
    public static final int RECORDING_GPS_ACCURACY_EXCELLENT = 10;
    public static final int RECORDING_GPS_ACCURACY_POOR = 2000;

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