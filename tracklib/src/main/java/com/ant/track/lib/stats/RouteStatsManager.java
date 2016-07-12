package com.ant.track.lib.stats;

import android.location.Location;
import android.util.Log;

import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.utils.LocationUtils;

/**
 * Route stats manager. Updates, modifies the route stats.
 * The entire class is based on the MyTracks application with
 * slight modifications.
 */
public class RouteStatsManager {

    private static final String TAG = RouteStatsManager.class.getCanonicalName();
    public static final int DEFAULT_MULTIPLIER = 8;
    public static final double MAXIMUM_TIME_NO_UPDATE = 0.001;

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

    private DataBuffer speedBuffer = new DataBufferImpl(Constants.SPEED_DEFAULT_FACTOR);

    public RouteStatsManager(long time) {
        init(time);
    }

    private void init(long time) {
        currentRouteStats = new RouteStats(time);
        currentSegmentStats = new RouteStats(time);
        lastLocation = new Location(Constants.GPS);
        lastLocation.setLatitude(LocationUtils.PAUSE_LATITUDE);
    }

    public void addLocationToStats(Location location, double minRecordingDistance) {

        if (!LocationUtils.isValidLocation(location)) {
            //pause location - update the current stats and add them
            //to the big route
            if (isPausedLocation(location)) {
                if (lastLocation != null && lastValidLocation != null && lastValidLocation != lastLocation) {
                    currentSegmentStats.addNewDistanceToStats(lastValidLocation.distanceTo(lastLocation));
                }
            }

            currentRouteStats.merge(currentSegmentStats);
            lastLocation = null;
            lastValidLocation = null;
            currentSegmentStats = new RouteStats(location.getTime());
            speedBuffer.reset();
            return;
        }
        //location is valid somehow.
        //we need to validate the location
        if (lastValidLocation == null || lastLocation == null) {
            lastLocation = location;
            lastValidLocation = location;
            return;
        }

        updateLatitudeStats(location);
        updateLongitudeStats(location);
        updateAltitudeStats(location);

        double distanceToLastMovingLocation = lastValidLocation.distanceTo(location);
        double movingTime = location.getTime() - lastValidLocation.getTime();

        if ((distanceToLastMovingLocation < minRecordingDistance) &&
                (!location.hasSpeed() || location.getSpeed() < Constants.MAX_SPEED_NO_MOVEMENT)) {
            lastLocation = location;
            return;
        }

        double timeDiff = location.getTime() - lastLocation.getTime();
        if (timeDiff <= MAXIMUM_TIME_NO_UPDATE) {
            lastLocation = location;
            return;
        }

        currentRouteStats.addNewDistanceToStats(distanceToLastMovingLocation);
        currentSegmentStats.addNewMovingTimeToStats(movingTime);
        if (location.hasSpeed() && lastValidLocation.hasSpeed()) {
            updateMaxSpeed(location.getTime(), location.getSpeed(), lastValidLocation.getTime(), lastValidLocation.getSpeed());
        }

        lastValidLocation = location;
        lastLocation = location;
    }


    private boolean isPausedLocation(Location location) {
        if (location.getLatitude() == LocationUtils.PAUSE_LATITUDE || location.getLongitude() == LocationUtils.PAUSE_LONGITUDE) {
            return true;
        }
        return false;
    }


    private void updateAltitudeStats(Location location) {
        currentSegmentStats.updateAltitudeStats(location.getAltitude());
    }

    private void updateLatitudeStats(Location currentLoc) {
        currentSegmentStats.updateLatitudeStats(currentLoc.getLatitude());
    }

    private void updateLongitudeStats(Location currentLocation) {
        currentSegmentStats.updateLongitudeStats(currentLocation.getLongitude());
    }

    /**
     * updates the max speed for the current route.
     */
    private void updateMaxSpeed(long time, double speed, long lastTime, double lastSpeed) {

        if (speed < Constants.MAX_SPEED_NO_MOVEMENT) {
            speedBuffer.reset();
        } else {

            if (isValidSpeed(time, speed, lastTime, lastSpeed)) {
                if (!speedBuffer.isFull()) {
                    speedBuffer.setNext(speed, false);
                }
                currentSegmentStats.setMaxSpeed(speedBuffer.getMax());
                currentSegmentStats.setAvgSpeed(speedBuffer.getAverage());
                currentSegmentStats.setMinSpeed(speedBuffer.getMin());
                if (speedBuffer.isFull()) {
                    speedBuffer.setNext(speed, true);
                }

            } else {
                Log.d(TAG, "invalid speed");
            }
        }
    }

    /**
     * checks if the current time and speed is valid
     * in respect to the old ones, valid.
     *
     * @param time      the current time of location
     * @param speed     the speed
     * @param lastTime  the last time registered
     * @param lastSpeed the last speed recorded
     * @return true if the new speed is valid, false otherwise
     */
    private boolean isValidSpeed(long time, double speed, long lastTime, double lastSpeed) {

        /*
         * The following code will ignore unlikely readings. 128 m/s seems to be an
         * internal android error code.
         */
        if (Math.abs(speed - 128) < 1) {
            return false;
        }

        long timeDiff = Math.abs(lastTime - time);
        double speedDif = Math.abs(lastSpeed - speed);

        if (speedDif > Constants.MAX_ACCELERATION * timeDiff) {
            return false;
        }

        if (speedBuffer.isFull()) {
            double average = speedBuffer.getAverage();
            double speedDifAvg = Math.abs(speed - average);
            return (speed < average * DEFAULT_MULTIPLIER &&
                    (speedDifAvg < Constants.MAX_ACCELERATION * timeDiff) &&
                    speedDifAvg < (speedBuffer.getMax() - speedBuffer.getMin()));
        } else {
            return true;
        }
    }

    public RouteStats getCurrentRouteStats() {
        //make a copy and return it;
        RouteStats routeStats = new RouteStats(currentRouteStats);
        routeStats.merge(currentSegmentStats);
        return routeStats;
    }

    public RouteStats getCurrentSegmentStats() {
        return currentSegmentStats;
    }

    public double getAvgSpeed() {
        return speedBuffer.getAverage();
    }
}
