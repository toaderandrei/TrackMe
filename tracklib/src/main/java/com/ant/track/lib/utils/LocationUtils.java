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

    public static double getLatitudeFromLatitude1E6(int latitude1E6) {
        return latitude1E6 / 1E6;
    }

    public static int getLatitude1E6FromDouble(double latitude) {
        return (int) (latitude * 1E6);
    }

    public static double getLongitudeFromLongitude1E6(int longitude1E6) {
        return longitude1E6 / 1E6;
    }

    public static int getLongitude1E6FromDouble(double longitude) {
        return (int) (longitude * 1E6);
    }


    public static double getElevationFromElevation1E6(int elevation1E6) {
        return elevation1E6 / 1E6;
    }

    public static int getElevation1E6FromDouble(double elevation) {
        return (int) (elevation * 1E6);
    }

    public static boolean isValidLatitude(double latitude) {
        if (Math.abs(latitude) <= 90) {
            return true;
        }
        return false;
    }

    public static boolean isValidLongitude(double longitude) {
        if (Math.abs(longitude) <= 180) {
            return true;
        }
        return false;
    }

    public static boolean isValidElevation(double elevationMax) {
        return elevationMax < Double.POSITIVE_INFINITY && elevationMax > Double.NEGATIVE_INFINITY;
    }
}

