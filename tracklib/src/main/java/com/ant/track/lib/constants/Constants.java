package com.ant.track.lib.constants;

import android.support.annotation.VisibleForTesting;

/**
 * Constants
 */
public class Constants {


    /**
     * The settings file name.
     */
    public static final String SETTINGS_NAME = "SettingsActivity";

    public static final String EXTRA_VIEW_ROUTE_DETAILS = "extra_view_route_details";

    public static final String STOPS_THE_TRACKING_OR_PAUSES_IT = "Stops the tracking or pauses it.";
    public static final String STOP_OR_PAUSE = "Stop or pause?";

    public static final int MAX_LONGITUDE = 180;
    public static final int MAX_LATITUDE = 90000000;
    public static final String EMPTY_STRING = "";
    public static final int DEFAULT_MAX_NUMBER_OF_POINTS = 5000;
    public static final String EXTRA_ROUTE_ID = "route_id";
    public static final float DEFAULT_POLYLINE_POINT_WIDTH = 5;
    public static final String EXTRA_ROUTE_ID_KEY = "route_id_key";

    public static double PAUSE_LATITUDE = 100.0;

    public static double PAUSE_LONGITUDE = 200.0;

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
