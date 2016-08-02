package com.ant.track.lib.stats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data proximity used for latitude, longitude and altitude..
 */
public class LocationProximitiesManager implements Parcelable {

    private static final double POSITIVE_INFINITY = Double.POSITIVE_INFINITY;

    private static final double NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY;
    private double min;

    private double max;

    public LocationProximitiesManager() {
        initData();
    }

    private void initData() {
        this.max = NEGATIVE_INFINITY;
        this.min = POSITIVE_INFINITY;
    }

    private LocationProximitiesManager(Parcel in) {
        min = in.readDouble();
        max = in.readDouble();
    }

    public static final Creator<LocationProximitiesManager> CREATOR = new Creator<LocationProximitiesManager>() {
        @Override
        public LocationProximitiesManager createFromParcel(Parcel in) {
            return new LocationProximitiesManager(in);
        }

        @Override
        public LocationProximitiesManager[] newArray(int size) {
            return new LocationProximitiesManager[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(min);
        parcel.writeDouble(max);
    }

    public boolean update(double data) {
        boolean updated = false;
        if (data < min) {
            min = data;
            updated = true;
        }

        if (data >= max) {
            max = data;
            updated = true;
        }

        return updated;

    }

    /**
     * Returns true if has data.
     */
    public boolean hasData() {
        return min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public void setMinMax(double minLat, double maxLat) {
        this.min = minLat;
        this.max = maxLat;
    }
}
