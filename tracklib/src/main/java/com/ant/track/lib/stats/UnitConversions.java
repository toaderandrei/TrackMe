package com.ant.track.lib.stats;

/**
 * Created by toaderandrei on 11/08/16.
 */
public class UnitConversions {


    public static final double KM_TO_M = 1000;
    public static final double M_TO_KM = 1 / (KM_TO_M);

    public static final double MIN_TO_S = 60.0;
    public static final double S_TO_MIN = (double) (1 / MIN_TO_S);

    public static final double HR_TO_MIN = 60.0;
    public static final double MIN_TO_HR = (double) (1 / HR_TO_MIN);
    /**
     * convers meters to km/h
     */
    public static final double MS_TO_KMH = (M_TO_KM) / (S_TO_MIN * MIN_TO_HR);
}
