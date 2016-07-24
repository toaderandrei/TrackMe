package com.ant.track.lib.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;

/**
 * Point that is part of a route. This type of point is not shown on the
 * map but it is used as part of a route.
 */
public class RoutePoint implements Parcelable {

    private long _id;
    private long routeId;
    private Location location;
    private String activityMode;

    @VisibleForTesting
    public RoutePoint() {

    }

    public RoutePoint(long routeId,
                      Location location,
                      String activityMode) {
        this.routeId = routeId;
        this.location = location;
        this.activityMode = activityMode;
    }

    public RoutePoint(long id,
                      long routeId,
                      Location location,
                      String activityMode) {
        this._id = id;
        this.routeId = routeId;
        this.location = location;
        this.activityMode = activityMode;
    }

    protected RoutePoint(Parcel in) {
        _id = in.readLong();
        in.readLong();
        location = in.readParcelable(Location.class.getClassLoader());
        activityMode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeLong(routeId);
        dest.writeParcelable(location, flags);
        dest.writeString(activityMode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoutePoint> CREATOR = new Creator<RoutePoint>() {
        @Override
        public RoutePoint createFromParcel(Parcel in) {
            return new RoutePoint(in);
        }

        @Override
        public RoutePoint[] newArray(int size) {
            return new RoutePoint[size];
        }
    };

    public float getSpeed() {
        if (!hasLocation()) {
            return -1f;
        }
        return location.getSpeed();
    }

    public void setSpeed(float speed) {
        if (!hasLocation()) {
            return;
        }
        this.location.setSpeed(speed);
    }

    public double getLatitude() {
        if (!hasLocation()) {
            return Double.NEGATIVE_INFINITY;
        }
        return location.getLatitude();
    }

    public void setLocation_lat(double location_lat) {
        if (!hasLocation()) {
            return;
        }
        this.location.setLatitude(location_lat);
    }

    public double getAltitude() {
        if (!hasLocation()) {
            return Double.NEGATIVE_INFINITY;
        }
        return location.getAltitude();
    }

    public void setLocation_alt(float location_alt) {
        this.location.setAltitude(location_alt);
    }

    public double getLongitude() {
        if (!hasLocation()) {
            return Double.NEGATIVE_INFINITY;
        }
        return location.getLongitude();
    }

    public void setLocation_long(double location_long) {
        this.location.setLongitude(location_long);
    }

    public float getLocationBearing() {
        return location.getBearing();
    }

    public void setLocationBearing(float location_bearing) {
        this.location.setBearing(location_bearing);
    }

    public long getTime() {
        return location.getTime();
    }

    public void setTime(long time) {
        if (!hasLocation()) {
            return;
        }
        this.location.setTime(time);
    }

    public String getActivityMode() {
        return activityMode;
    }

    public void setActivityMode(String activityMode) {
        this.activityMode = activityMode;
    }

    public void setId(long id) {
        this._id = id;
    }

    public long getId() {
        return _id;
    }

    public boolean hasSpeed() {
        if (!hasLocation()) {
            return false;
        }
        return location.hasSpeed();
    }

    public boolean hasAccuracy() {
        if (!hasLocation()) {
            return false;
        }
        return location.hasAccuracy();
    }

    public float getAccuracy() {
        if (!hasLocation()) {
            return -1f;
        }
        return location.getAccuracy();
    }

    public boolean hasAltitude() {
        if (!hasLocation()) {
            return false;
        }
        return location.hasAltitude();
    }

    public boolean hasBearing() {
        if (!hasLocation()) {
            return false;
        }
        return location.hasBearing();
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeid) {
        this.routeId = routeid;
    }

    private boolean hasLocation() {
        return location != null;
    }

    public float getBearing() {
        if (!hasLocation()) {
            return Float.NEGATIVE_INFINITY;
        }
        return location.getBearing();
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
