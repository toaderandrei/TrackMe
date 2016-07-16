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
    private String name;
    private String description;

    @VisibleForTesting
    public RouteCheckPoint() {
    }

    public RouteCheckPoint(long id,
                           String name,
                           String description,
                           float speed,
                           float location_alt,
                           float location_bearing,
                           float location_lat,
                           float location_long,
                           String activityMode,
                           String markerColor) {
        super(id, speed, location_alt, location_bearing, location_lat, location_long, activityMode);
        this.name = name;
        this.description = description;
        this.markerColor = markerColor;
    }

    protected RouteCheckPoint(Parcel in) {
        super(in);
        markerColor = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(description);
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

    public String getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(String markerColor) {
        this.markerColor = markerColor;
    }

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

}
