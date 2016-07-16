package com.ant.track.lib.stats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Route stats during tracking.
 */
public class RouteStats implements Parcelable {

    private long startTime = -1L;
    private long stopTime = -1L;
    private long totalDuration;
    private double maxSpeed = Double.MIN_VALUE;
    private double minSpeed = Double.MAX_VALUE;
    private double avgSpeed = Double.MAX_VALUE;
    private double totalDistance;
    private double totalElevationGain;
    private long totalMovingTime;

    private LocationProximitiesManager latitudeProximityManager = new LocationProximitiesManager();

    private LocationProximitiesManager longitudeProximityManager = new LocationProximitiesManager();

    private LocationProximitiesManager elevationProximityManager = new LocationProximitiesManager();
    private long totalTime;

    public RouteStats() {

    }

    public RouteStats(long currentTime) {
        this.startTime = currentTime;
        this.stopTime = currentTime;
    }

    public RouteStats(RouteStats other) {

        this.startTime = other.startTime;
        this.stopTime = other.stopTime;
        this.totalMovingTime = other.getMovingTime();

        this.minSpeed = other.minSpeed;
        this.maxSpeed = other.maxSpeed;
        this.avgSpeed = other.avgSpeed;

        this.totalDistance = other.totalDistance;
        this.totalDuration = other.totalDuration;

        this.latitudeProximityManager = other.latitudeProximityManager;
        this.longitudeProximityManager = other.longitudeProximityManager;
        this.elevationProximityManager = other.elevationProximityManager;
    }

    protected RouteStats(Parcel in) {

        startTime = in.readLong();
        stopTime = in.readLong();

        totalMovingTime = in.readLong();
        maxSpeed = in.readDouble();
        minSpeed = in.readDouble();
        avgSpeed = in.readDouble();

        latitudeProximityManager = in.readParcelable(LocationProximitiesManager.class.getClassLoader());
        longitudeProximityManager = in.readParcelable(LocationProximitiesManager.class.getClassLoader());
        elevationProximityManager = in.readParcelable(LocationProximitiesManager.class.getClassLoader());

        totalDuration = in.readLong();

        totalDistance = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(startTime);
        dest.writeLong(stopTime);
        dest.writeLong(totalMovingTime);

        dest.writeDouble(maxSpeed);
        dest.writeDouble(minSpeed);
        dest.writeDouble(avgSpeed);

        dest.writeParcelable(latitudeProximityManager, flags);
        dest.writeParcelable(longitudeProximityManager, flags);
        dest.writeParcelable(elevationProximityManager, flags);

        dest.writeLong(totalDuration);

        dest.writeDouble(totalDistance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RouteStats> CREATOR = new Creator<RouteStats>() {
        @Override
        public RouteStats createFromParcel(Parcel in) {
            return new RouteStats(in);
        }

        @Override
        public RouteStats[] newArray(int size) {
            return new RouteStats[size];
        }
    };

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(double minSpeed) {
        this.minSpeed = minSpeed;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public void addNewDistanceToStats(final double distance) {
        totalDistance += distance;

    }

    public void updateLatitudeStats(double latitude) {
        latitudeProximityManager.update(latitude);
    }

    public void updateMinMaxLatitudeStats(double minLat, double maxLat) {
        latitudeProximityManager.setMinMax(minLat, maxLat);
    }

    public void updateMinMaxLongStats(double mingLong, double maxLong) {
        longitudeProximityManager.setMinMax(mingLong, maxLong);
    }

    public void updateLongitudeStats(double longitude) {
        longitudeProximityManager.update(longitude);
    }

    public void updateElevation(double altitude) {
        elevationProximityManager.update(altitude);
    }

    public void updateMinMaxElevation(double minElev, double maxElev) {
        elevationProximityManager.setMinMax(minElev, maxElev);
    }

    public double getLongitudeMax() {
        return longitudeProximityManager.getMax();
    }

    public double getLongitudeMin() {
        return longitudeProximityManager.getMin();
    }

    public double getLatitudeMin() {
        return latitudeProximityManager.getMin();
    }

    public double getLatitudeMax() {
        return latitudeProximityManager.getMax();
    }

    public double getElevationMin() {
        return elevationProximityManager.getMin();
    }

    public double getElevationMax() {
        return elevationProximityManager.getMax();
    }

    public void addElevationGain(double elevation) {
        this.totalElevationGain += elevation;
    }

    public double getTotalElevationGain() {
        return totalElevationGain;
    }

    /**
     * merges the current stats with stats received
     *
     * @param currentSegmentStats the stats received to be merged with the current stats.
     */
    public void merge(RouteStats currentSegmentStats) {
        //todo merge
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void addNewMovingTimeToStats(double movingTime) {
        this.totalDuration += movingTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long time) {
        this.totalTime = time;
    }

    public void setTotalMovingTime(long movingTime) {
        this.totalMovingTime = movingTime;
    }

    public long getMovingTime() {
        return totalMovingTime;
    }
}


