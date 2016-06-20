package com.ant.track.lib.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describes a route/track on the map. A route can contain
 * points and check points.
 */
public class Route implements Parcelable{

    private RouteStats routeStats;

    public Route(RouteStats stats){
        routeStats = stats;
    }

    protected Route(Parcel in) {
        routeStats = in.readParcelable(RouteStats.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
}
