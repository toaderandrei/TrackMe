package com.ant.track.lib.stats;

import android.os.Parcel;
import android.os.Parcelable;

import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.model.RoutePoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Route stats during tracking.
 */
public class RouteStats implements Parcelable {
    private String name;
    private String description;
    private long start_pointId;
    private long stop_pointId;
    private long startTime;
    private long stopTime;
    private int totalDuration;
    private double maxSpeed;
    private double minSpeed;
    private double avgSpeed;
    private double totalDistance;
    private double totalElevationGain;
    private List<RoutePoint> routePointList;
    private List<RouteCheckPoint> routeCheckPoints;

    private LocationProximitiesManager latitudeProximityManager = new LocationProximitiesManager();

    private LocationProximitiesManager longitudeProximityManager = new LocationProximitiesManager();

    private LocationProximitiesManager elevationProximityManager = new LocationProximitiesManager();
    private long totalTime;


    public RouteStats(long currentTime) {
        this.startTime = currentTime;
        this.stopTime = currentTime;
    }

    public RouteStats(RouteStats other) {
        this.name = other.name;
        this.description = other.description;

        this.startTime = other.startTime;
        this.stopTime = other.stopTime;

        this.start_pointId = other.start_pointId;
        this.stop_pointId = other.stop_pointId;

        this.minSpeed = other.minSpeed;
        this.maxSpeed = other.maxSpeed;
        this.avgSpeed = other.avgSpeed;

        this.totalDistance = other.totalDistance;
        this.totalDuration = other.totalDuration;

        this.routeCheckPoints = other.routeCheckPoints;
        this.routePointList = other.routePointList;

        this.latitudeProximityManager = other.latitudeProximityManager;
        this.longitudeProximityManager = other.longitudeProximityManager;
        this.elevationProximityManager = other.elevationProximityManager;
    }

    protected RouteStats(Parcel in) {
        name = in.readString();
        description = in.readString();
        start_pointId = in.readLong();
        stop_pointId = in.readLong();
        startTime = in.readLong();
        stopTime = in.readLong();

        latitudeProximityManager = in.readParcelable(LocationProximitiesManager.class.getClassLoader());
        longitudeProximityManager = in.readParcelable(LocationProximitiesManager.class.getClassLoader());
        elevationProximityManager = in.readParcelable(LocationProximitiesManager.class.getClassLoader());

        totalDuration = in.readInt();
        maxSpeed = in.readDouble();
        minSpeed = in.readDouble();
        avgSpeed = in.readDouble();
        totalDistance = in.readDouble();
        routePointList = in.createTypedArrayList(RoutePoint.CREATOR);
        routeCheckPoints = in.createTypedArrayList(RouteCheckPoint.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(start_pointId);
        dest.writeLong(stop_pointId);
        dest.writeLong(startTime);
        dest.writeLong(stopTime);

        dest.writeParcelable(latitudeProximityManager, flags);
        dest.writeParcelable(longitudeProximityManager, flags);
        dest.writeParcelable(elevationProximityManager, flags);

        dest.writeInt(totalDuration);
        dest.writeDouble(maxSpeed);
        dest.writeDouble(minSpeed);
        dest.writeDouble(avgSpeed);

        dest.writeDouble(totalDistance);
        dest.writeTypedList(routePointList);
        dest.writeTypedList(routeCheckPoints);
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStart_pointId() {
        return start_pointId;
    }

    public void setStart_pointId(long start_pointId) {
        this.start_pointId = start_pointId;
    }

    public long getStop_pointId() {
        return stop_pointId;
    }

    public void setStop_pointId(long stop_pointId) {
        this.stop_pointId = stop_pointId;
    }

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

    public int getTotalDuration() {
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

    public List<RouteCheckPoint> getRouteCheckPoints() {
        return routeCheckPoints;
    }

    public void setRouteCheckPoints(List<RouteCheckPoint> routeCheckPoints) {
        this.routeCheckPoints = routeCheckPoints;
    }

    public List<RoutePoint> getRoutePointList() {
        return routePointList;
    }

    public void setRoutePointList(List<RoutePoint> routePointList) {
        this.routePointList = routePointList;
    }

    public void addRoutePoint(final RoutePoint routePoint) {
        if (routePointList == null || routePointList.isEmpty()) {
            routePointList = new ArrayList<>();
        }

        if (routeCheckPoints == null || routeCheckPoints.isEmpty()) {
            routePointList = new ArrayList<>();
        }

        if (routePoint != null) {
            routePointList.add(routePoint);
        }
        if (routePoint instanceof RouteCheckPoint) {
            routeCheckPoints.add((RouteCheckPoint) routePoint);
        }
    }

    public void addNewDistanceToStats(final double distance) {
        totalDistance += distance;

    }

    public void updateLatitudeStats(double latitude) {
        latitudeProximityManager.add(latitude);
    }

    public void updateLongitudeStats(double longitude) {
        longitudeProximityManager.add(longitude);
    }

    public void updateElevation(double altitude) {
        elevationProximityManager.add(altitude);
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

    public void addNewMovingTimeToStats(double movingTime) {
        this.totalDuration += movingTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long time) {
        this.totalTime = time;
    }
}


