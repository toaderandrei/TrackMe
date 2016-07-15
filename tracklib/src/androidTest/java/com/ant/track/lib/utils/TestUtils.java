package com.ant.track.lib.utils;

import com.ant.track.lib.stats.RouteStats;

/**
 * Utility class for creating data for test
 */
public final class TestUtils {

    private static final double DEFAULT_AVG_SPEED = 10.0d;
    private static final double DEFAULT_MIN_SPEED = 2.0d;
    private static final long DEFAULT_TOTAL_TIME = 20000;

    public static RouteStats createRouteStats(long now,
                                              long start,
                                              long end,
                                              double avgSpeed,
                                              double minSpeed,
                                              long totalTime) {
        RouteStats routeStats = new RouteStats(now);
        routeStats.setStartTime(start);
        routeStats.setStopTime(end);
        routeStats.setAvgSpeed(avgSpeed);
        routeStats.setMinSpeed(minSpeed);
        routeStats.setTotalTime(totalTime);
        return routeStats;
    }

    public static RouteStats createRouteStats(long now) {
        RouteStats routeStats = new RouteStats(now);
        routeStats.setStartTime(now);
        routeStats.setStopTime(now);
        routeStats.setAvgSpeed(DEFAULT_AVG_SPEED);
        routeStats.setMinSpeed(DEFAULT_MIN_SPEED);
        routeStats.setTotalTime(DEFAULT_TOTAL_TIME);
        return routeStats;
    }

}
