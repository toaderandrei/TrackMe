package com.ant.track.lib.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;

/**
 * Point that is part of a route. This type of point is not shown on the
 * map but it is used as part of a route.
 */
public class RoutePoint implements Parcelable {

    private long _id;
    private double speed;
    private float location_lat;
    private float location_alt;
    private float location_long;
    private float location_bearing;
    private long time;
    private String activityMode;


    @VisibleForTesting
    public RoutePoint() {

    }

    public RoutePoint(long id,
                      float speed,
                      float location_alt,
                      float location_bearing,
                      float location_lat,
                      float location_long,
                      String activityMode) {
        this._id = id;

        this.speed = speed;
        this.location_alt = location_alt;
        this.location_bearing = location_bearing;
        this.location_long = location_long;
        this.location_lat = location_lat;
        this.activityMode = activityMode;
    }

    protected RoutePoint(Parcel in) {
        _id = in.readLong();

        speed = in.readDouble();
        location_lat = in.readFloat();
        location_alt = in.readFloat();
        location_long = in.readFloat();
        location_bearing = in.readFloat();
        time = in.readLong();
        activityMode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);

        dest.writeDouble(speed);
        dest.writeFloat(location_lat);
        dest.writeFloat(location_alt);
        dest.writeFloat(location_long);
        dest.writeFloat(location_bearing);
        dest.writeLong(time);
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public float getLocation_lat() {
        return location_lat;
    }

    public void setLocation_lat(float location_lat) {
        this.location_lat = location_lat;
    }

    public float getLocation_alt() {
        return location_alt;
    }

    public void setLocation_alt(float location_alt) {
        this.location_alt = location_alt;
    }

    public float getLocation_long() {
        return location_long;
    }

    public void setLocation_long(float location_long) {
        this.location_long = location_long;
    }

    public float getLocationBearing() {
        return location_bearing;
    }

    public void setLocationBearing(float location_bearing) {
        this.location_bearing = location_bearing;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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
}
