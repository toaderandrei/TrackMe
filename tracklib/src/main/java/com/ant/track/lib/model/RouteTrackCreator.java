package com.ant.track.lib.model;

import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.ant.track.lib.constants.Constants;

/**
 * An utility class that creates default route points and route check points.
 */
public final class RouteTrackCreator implements Parcelable {

    private static final String DEFAULT_NAME = "route point name";
    private static final String DEFAULT_DESC = "route point desc";
    private static final String DEFAULT_LAT_KEY = "LAT_KEY";
    private static final String DEFAULT_LONG_KEY = "LONG_KEY";
    public static final RouteTrackCreator DEFAULT_ROUTE_TRACK_BUILDER = new RouteTrackCreator.Builder().addName(DEFAULT_NAME)
            .addDescription(DEFAULT_DESC)
            .addLocation(Constants.PAUSE_LATITUDE, Constants.PAUSE_LONGITUDE)
            .build();
    private static final String NAME_KEY = "NAME_KEY";
    private static final String DESC_KEY = "DESC_KEY";

    private Bundle bundle;

    public RouteTrackCreator(Bundle bundle) {
        this.bundle = bundle;
    }

    protected RouteTrackCreator(Parcel in) {
        bundle = in.readBundle();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(bundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RouteTrackCreator> CREATOR = new Creator<RouteTrackCreator>() {
        @Override
        public RouteTrackCreator createFromParcel(Parcel in) {
            return new RouteTrackCreator(in);
        }

        @Override
        public RouteTrackCreator[] newArray(int size) {
            return new RouteTrackCreator[size];
        }
    };

    public String getName() {
        if (bundle.containsKey(NAME_KEY)) {
            return bundle.getString(NAME_KEY);
        }
        return DEFAULT_NAME;
    }


    public String getDescription() {
        if (bundle.containsKey(DESC_KEY)) {
            return bundle.getString(DESC_KEY);
        }
        return DEFAULT_DESC;
    }

    public Location getLocation() {
        Location location = new Location("gps");
        if (bundle.containsKey(DEFAULT_LAT_KEY)) {
            location.setLongitude(bundle.getDouble(DEFAULT_LAT_KEY));
        } else {
            location.setLatitude(Constants.PAUSE_LATITUDE);
        }

        if (bundle.containsKey(DEFAULT_LONG_KEY)) {
            location.setLongitude(bundle.getDouble(DEFAULT_LONG_KEY));
        } else {
            location.setLatitude(Constants.PAUSE_LONGITUDE);
        }
        return location;
    }

    public static class Builder {

        private Bundle bundle = new Bundle();

        public Builder addName(String name) {
            this.bundle.putString(NAME_KEY, name);
            return this;
        }


        public Builder addDescription(String description) {
            this.bundle.putString(DESC_KEY, description);
            return this;
        }

        public Builder addLocation(double lat, double longitude) {
            this.bundle.putDouble(DEFAULT_LAT_KEY, lat);
            this.bundle.putDouble(DEFAULT_LONG_KEY, longitude);
            return this;
        }

        public RouteTrackCreator build() {
            return new RouteTrackCreator(bundle);
        }
    }

}
