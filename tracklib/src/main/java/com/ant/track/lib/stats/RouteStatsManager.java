package com.ant.track.lib.stats;

import android.location.Location;

import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.utils.LocationUtils;

/**
 * Route stats manager. Updates, modifies the route stats.
 * The entire class is based on the MyTracks application with
 * slight modifications.
 */
public class RouteStatsManager {


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

    private LocationProximitiesManager latitudeProximityManager = new LocationProximitiesManager();

    private LocationProximitiesManager longitudeProximityManager = new LocationProximitiesManager();

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
            return;
        }

        updateLatitudeStats(location);
        updateLongitudeStats(location);

        double distanceToLastMovingLocation = lastValidLocation.distanceTo(location);
        double movingTime = location.getTime() - lastValidLocation.getTime();

        if ((distanceToLastMovingLocation < minRecordingDistance) ||
                (!location.hasSpeed() || location.getSpeed() < Constants.MAX_SPEED_NO_MOVEMENT)) {
            lastLocation = location;
            return;
        }

        currentRouteStats.addNewDistanceToStats(distanceToLastMovingLocation);
        currentSegmentStats.addNewMovingTimeToStats(movingTime);
        if (location.hasSpeed() && lastValidLocation.hasSpeed()) {
            updateMaxSpeed(location.getTime(), location.getSpeed(), lastValidLocation.getTime(), lastValidLocation.getSpeed());
        }
    }


    private boolean isPausedLocation(Location location) {
        if (location.getLatitude() == LocationUtils.PAUSE_LATITUDE || location.getLongitude() == LocationUtils.PAUSE_LONGITUDE) {
            return true;
        }
        return false;
    }


    public RouteStatsManager(RouteStatsManager other) {
        //todo this behaves like a copy constructor
    }

    private void updateLatitudeStats(Location currentLoc) {
        latitudeProximityManager.add(currentLoc.getLatitude());
    }

    private void updateLongitudeStats(Location currentLocation) {
        longitudeProximityManager.add(currentLocation.getLongitude());
    }

    /**
     * updates the max speed for the current route.
     */
    protected void updateMaxSpeed(long time, double speed, long lastTime, double lastSpeed) {
        if (speed > 0) {

            if (isValidSpeed(time, speed, lastTime, lastSpeed)) {
                speedBuffer.setNext(speed);
            }
            currentSegmentStats.setMaxSpeed(speedBuffer.getMax());
            currentSegmentStats.setAvgSpeed(speedBuffer.getAverage());
            currentSegmentStats.setMinSpeed(speedBuffer.getMin());
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
        * we exclude weird readings like speed = 0;
        */
        if (speed == 0) {
            return false;
        }

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

        if (speedBuffer.hasSufficientReadings()) {

            return speed >= speedBuffer.getMin() &&
                    speed <= speedBuffer.getMax() &&
                    speed <= speedBuffer.getAverage() &&
                    speed <= Constants.MAX_ACCELERATION * timeDiff;
        }
        return true;
    }
}
