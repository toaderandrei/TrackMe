package com.ant.track.lib.model;

import android.os.Parcel;
import android.os.Parcelable;

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
    private int maxLongitude;
    private int minLongitude;
    private int maxLatitude;
    private int minLatitude;
    private int minAltitude;
    private int maxAltitude;
    private int totalDuration;
    private int maxSpeed;
    private int minSpeed;
    private int avgSpeed;
    private float minElevation;
    private float maxElevation;
    private List<RoutePoint> routePointList;
    private List<RouteCheckPoint> routeCheckPoints;
    private long currentTime;

    public RouteStats(long currentTime) {
        this.currentTime = currentTime;
    }

    protected RouteStats(Parcel in) {
        currentTime = in.readLong();
        name = in.readString();
        description = in.readString();
        start_pointId = in.readLong();
        stop_pointId = in.readLong();
        startTime = in.readLong();
        stopTime = in.readLong();
        maxLongitude = in.readInt();
        minLongitude = in.readInt();
        maxLatitude = in.readInt();
        minLatitude = in.readInt();
        minAltitude = in.readInt();
        maxAltitude = in.readInt();
        totalDuration = in.readInt();
        maxSpeed = in.readInt();
        minSpeed = in.readInt();
        avgSpeed = in.readInt();
        minElevation = in.readFloat();
        maxElevation = in.readFloat();
        routePointList = in.createTypedArrayList(RoutePoint.CREATOR);
        routeCheckPoints = in.createTypedArrayList(RouteCheckPoint.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(currentTime);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(start_pointId);
        dest.writeLong(stop_pointId);
        dest.writeLong(startTime);
        dest.writeLong(stopTime);
        dest.writeInt(maxLongitude);
        dest.writeInt(minLongitude);
        dest.writeInt(maxLatitude);
        dest.writeInt(minLatitude);
        dest.writeInt(minAltitude);
        dest.writeInt(maxAltitude);
        dest.writeInt(totalDuration);
        dest.writeInt(maxSpeed);
        dest.writeInt(minSpeed);
        dest.writeInt(avgSpeed);
        dest.writeFloat(minElevation);
        dest.writeFloat(maxElevation);
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

    public int getMaxLongitude() {
        return maxLongitude;
    }

    public void setMaxLongitude(int maxLongitude) {
        this.maxLongitude = maxLongitude;
    }

    public int getMinLongitude() {
        return minLongitude;
    }

    public void setMinLongitude(int minLongitude) {
        this.minLongitude = minLongitude;
    }

    public int getMaxLatitude() {
        return maxLatitude;
    }

    public void setMaxLatitude(int maxLatitude) {
        this.maxLatitude = maxLatitude;
    }

    public int getMinLatitude() {
        return minLatitude;
    }

    public void setMinLatitude(int minLatitude) {
        this.minLatitude = minLatitude;
    }

    public int getMinAltitude() {
        return minAltitude;
    }

    public void setMinAltitude(int minAltitude) {
        this.minAltitude = minAltitude;
    }

    public int getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(int maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(int minSpeed) {
        this.minSpeed = minSpeed;
    }

    public int getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(int avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public float getMinElevation() {
        return minElevation;
    }

    public void setMinElevation(float minElevation) {
        this.minElevation = minElevation;
    }

    public float getMaxElevation() {
        return maxElevation;
    }

    public void setMaxElevation(float maxElevation) {
        this.maxElevation = maxElevation;
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
}


