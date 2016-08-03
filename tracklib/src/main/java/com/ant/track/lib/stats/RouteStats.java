package com.ant.track.lib.stats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Route stats during tracking.
 */
public class RouteStats implements Parcelable {

    private long startTime = -1L;
    private long stopTime = -1L;
    private double maxSpeed = Double.MIN_VALUE;
    private double minSpeed = Double.MAX_VALUE;
    private double avgSpeed = Double.MAX_VALUE;
    private double totalDistance;
    private double totalElevationGain;
    private long totalMovingTime;
    private long totalTime;

    private LocationProximitiesManager latitudeProximityManager = new LocationProximitiesManager();

    private LocationProximitiesManager longitudeProximityManager = new LocationProximitiesManager();

    private LocationProximitiesManager elevationProximityManager = new LocationProximitiesManager();

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
        this.totalTime = other.totalTime;
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

        totalTime = in.readLong();

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

        dest.writeLong(totalTime);

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
     * @param other the stats received to be merged with the current stats.
     */
    public void merge(RouteStats other) {
        startTime = Math.min(startTime, other.startTime);
        stopTime = Math.max(stopTime, other.stopTime);
        totalDistance += other.totalDistance;
        totalTime += other.totalTime;
        totalMovingTime += other.getMovingTime();
        if (other.latitudeProximityManager.hasData()) {
            latitudeProximityManager.update(other.latitudeProximityManager.getMin());
            latitudeProximityManager.update(other.latitudeProximityManager.getMax());
        }
        if (other.longitudeProximityManager.hasData()) {
            longitudeProximityManager.update(other.longitudeProximityManager.getMin());
            longitudeProximityManager.update(other.longitudeProximityManager.getMax());
        }
        maxSpeed = Math.max(maxSpeed, other.maxSpeed);
        if (other.elevationProximityManager.hasData()) {
            elevationProximityManager.update(other.elevationProximityManager.getMin());
            elevationProximityManager.update(other.elevationProximityManager.getMax());
        }
        totalElevationGain += other.totalElevationGain;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void addNewMovingTimeToStats(double movingTime) {
        this.totalTime += movingTime;
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

    public int getTop() {
        return (int) (latitudeProximityManager.getMax() * 1E6);
    }

    public int getBottom() {
        return (int) (latitudeProximityManager.getMin() * 1E6);
    }

    /**
     * Gets the leftmost position (lowest longitude) of the track, in signed
     * millions of degrees.
     */
    public int getLeft() {
        return (int) (longitudeProximityManager.getMin() * 1E6);
    }


    /**
     * Gets the topmost position (highest latitude) of the track, in signed
     * degrees.
     */
    public double getTopDegrees() {
        return latitudeProximityManager.getMax();
    }

    /**
     * Gets the rightmost position (highest longitude) of the track, in signed
     * millions of degrees.
     */
    public int getRight() {
        return (int) (longitudeProximityManager.getMax() * 1E6);
    }

    /**
     * Gets the leftmost position (lowest longitude) of the track, in signed
     * degrees.
     */
    public double getLeftDegrees() {
        return longitudeProximityManager.getMin();
    }

    /**
     * Gets the bottommost position (lowest latitude) of the track, in signed
     * degrees.
     */
    public double getBottomDegrees() {
        return latitudeProximityManager.getMin();
    }

    /**
     * Gets the rightmost position (highest longitude) of the track, in signed
     * degrees.
     */
    public double getRightDegrees() {
        return longitudeProximityManager.getMax();
    }
}


