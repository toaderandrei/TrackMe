package com.ant.track.lib.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ant.track.lib.stats.RouteStats;

/**
 * Describes a route/track on the map. A route can contain
 * points and check points.
 */
public class Route implements Parcelable {

    private RouteStats routeStats = new RouteStats();
    private String routeName;
    private String description = "";
    private long routeId = -1L;
    private long startPointId;
    private long stopPointId;
    private int numberOfPoints;

    public Route(RouteStats stats) {
        routeStats = stats;
    }

    public Route() {
    }

    protected Route(Parcel in) {
        routeName = in.readString();
        description = in.readString();
        routeId = in.readLong();
        startPointId = in.readLong();
        stopPointId = in.readLong();
        numberOfPoints = in.readInt();
        routeStats = in.readParcelable(RouteStats.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(routeName);
        dest.writeString(description);
        dest.writeLong(routeId);
        dest.writeLong(startPointId);
        dest.writeLong(stopPointId);
        dest.writeInt(numberOfPoints);
        dest.writeParcelable(routeStats, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public RouteStats getRouteStats() {
        return routeStats;
    }

    public void setRouteStats(RouteStats routeStats) {
        this.routeStats = routeStats;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public long getStartPointId() {
        return startPointId;
    }

    public void setStartPointId(long startPointId) {
        this.startPointId = startPointId;
    }

    public long getStopPointId() {
        return stopPointId;
    }

    public void setStopPointId(long stopPointId) {
        this.stopPointId = stopPointId;
    }

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    public void setNumberOfPoints(int numberOfPoints) {
        this.numberOfPoints = numberOfPoints;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return description;
    }
}
