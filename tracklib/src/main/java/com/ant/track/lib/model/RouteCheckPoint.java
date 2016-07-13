package com.ant.track.lib.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;

/**
 * A specific point on the map. This point is used together with a marker on
 * the map.
 */
public class RouteCheckPoint extends RoutePoint implements Parcelable {

    private String markerColor;

    @VisibleForTesting
    public RouteCheckPoint() {
    }

    public RouteCheckPoint(String name,
                           String description,
                           float speed,
                           float location_alt,
                           float location_bearing,
                           float location_lat,
                           float location_long,
                           String activityMode,
                           String markerColor) {
        super(name, description, speed, location_alt, location_bearing, location_lat, location_long, activityMode);
        this.markerColor = markerColor;
    }

    protected RouteCheckPoint(Parcel in) {
        super(in);
        markerColor = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(markerColor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RouteCheckPoint> CREATOR = new Creator<RouteCheckPoint>() {
        @Override
        public RouteCheckPoint createFromParcel(Parcel in) {
            return new RouteCheckPoint(in);
        }

        @Override
        public RouteCheckPoint[] newArray(int size) {
            return new RouteCheckPoint[size];
        }
    };
}
