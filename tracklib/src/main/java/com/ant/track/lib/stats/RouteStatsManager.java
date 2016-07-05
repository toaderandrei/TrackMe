package com.ant.track.lib.stats;

import android.location.Location;
import android.support.annotation.VisibleForTesting;

import com.ant.track.lib.utils.LocationUtils;

/**
 * Route stats manager. Updates, modifies the route stats.
 */
public class RouteStatsManager {

    /**
     * The number of speed reading to smooth to get a somewhat accurate signal.
     */
    @VisibleForTesting
    static final int SPEED_DEFAULT_FACTOR = 25;

    /**
     * the entire route from start to stop
     */
    private RouteStats currentRouteStats;
    /**
     * Current route stats, from start to any pause.
     * Once the route was paused it will end this route
     * and add it to the main route stats.
     */
    private RouteStats currentSegmentStats;

    private Location lastLocation;
    private Location lastValidLocation;

    private DataBuffer speedBuffer = new DataBufferImpl(SPEED_DEFAULT_FACTOR);

    public RouteStatsManager(long time) {
        init(time);
    }

    public void addLocationToStats(Location location) {

        if (!LocationUtils.isValidLocation(location)) {
            //pause location - update.
            if (lastLocation != null && lastValidLocation != null && lastValidLocation != lastLocation) {

            }
        } else {
            //todo add location to all the stats.
        }
    }

    private void init(long time) {
        currentRouteStats = new RouteStats(time);
        currentSegmentStats = new RouteStats(time);
        lastLocation = new Location("GPS");
        lastLocation.setLatitude(LocationUtils.PAUSE_LATITUDE);
    }

    public RouteStatsManager(RouteStatsManager other) {
        //todo this behaves like a copy constructor
    }

    /**
     * updates the max speed for the current route.
     */
    private void updateMaxSpeed(long time, double speed, long lastTime, double lastSpeed) {
        if (speed > 0) {
            if (speed != lastSpeed && time > lastTime) {
                speedBuffer.setNext(speed);
            }
            speedBuffer.setNext(speed);
            //double maxSpeed = speedBuffer.get
            //currentRouteStats.setMaxSpeed(speedBuffer.getAverage());
        }

    }
}
