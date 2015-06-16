package com.ant.track.service.utils;

/**
 * Created by Toader on 6/3/2015.
 */
public class UnitConversions {

    // Time
    // multiplication factor to convert seconds to milliseconds
    public static final double S_TO_MS = 1000.0;
    // multiplication factor to convert milliseconds to seconds
    public static final double MS_TO_S = 1 / S_TO_MS;
    // multiplication factor to convert minutes to seconds
    public static final double MIN_TO_S = 60.0;
    // multiplication factor to convert seconds to minutes
    public static final double S_TO_MIN = 1 / MIN_TO_S;
    // multiplication factor to convert hours to minutes
    public static final double HR_TO_MIN = 60.0;
    // multiplication factor to convert minutes to hours
    public static final double MIN_TO_HR = 1 / HR_TO_MIN;
    // multiplication factor to convert kilometers to miles
    public static final double KM_TO_MI = 0.621371192;

    // Distance
    // multiplication factor to convert miles to kilometers
    public static final double MI_TO_KM = 1 / KM_TO_MI;
    // multiplication factor to convert miles to feet
    public static final double MI_TO_FT = 5280.0;
    // multiplication factor to convert feet to miles
    public static final double FT_TO_MI = 1 / MI_TO_FT;
    // multiplication factor to covert kilometers to meters
    public static final double KM_TO_M = 1000.0;
    // multiplication factor to convert meters to kilometers
    public static final double M_TO_KM = 1 / KM_TO_M;
    // multiplication factor to convert meters to miles
    public static final double M_TO_MI = M_TO_KM * KM_TO_MI;
    // multiplication factor to convert meters to feet
    public static final double M_TO_FT = M_TO_MI * MI_TO_FT;

    private UnitConversions() {
    }
}
