package com.ant.track.lib.utils;

/**
 * Utility class about speed.
 */
public class SpeedUtils {

    public static boolean isSpeedValid(double speed) {
        if (speed >= -1 && speed != Double.MIN_VALUE && speed != Double.MAX_VALUE) {
            return true;
        }
        return false;
    }
}
