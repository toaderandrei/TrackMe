package com.ant.track.lib.constants;

import android.support.annotation.VisibleForTesting;

/**
 * Constants
 * */
public class Constants {


    public static final int MAX_LONGITUDE = 180;
    public static final int MAX_LATITUDE = 90;

    public static double PAUSE_LATITUDE = 100.0d;

    public static double PAUSE_LONGITUDE = 200.0d;

    /**
     * The number of speed reading to smooth to get a somewhat accurate signal.
     */
    @VisibleForTesting
    public static final int SPEED_DEFAULT_FACTOR = 40;
    public static final String GPS = "GPS";

    /**
     * Ignore any acceleration faster than this. Will ignore any speeds that imply
     * acceleration greater than 2g's 2g = 19.6 m/s^2 = 0.0002 m/ms^2 = 0.02
     * m/(m*ms)
     */
    public static final double MAX_ACCELERATION = 0.02;
    public static final String EXTRA_RECORDING_ID = "recording_id";

    public static final String SETTINGS_KEY = "settings_key";

    public static final double MAX_SPEED_NO_MOVEMENT = 0.22;
}
