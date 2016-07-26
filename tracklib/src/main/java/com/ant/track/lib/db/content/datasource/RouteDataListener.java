package com.ant.track.lib.db.content.datasource;

/**
 * interface used to define the operations on the route path.
 */
public interface RouteDataListener {

    /**
     * callback for when the accuracy has changed via a prefrence change.
     *
     * @param newValue the new value.
     */
    void onRecordingGpsAccuracyChanged(int newValue);

    /**
     * callback for when the min recording interval preference changed.
     *
     * @param newDistance the new distance
     */
    void onRecordingDistanceIntervalChanged(int newDistance);


    /**
     * callback for when the max distance preference changed.
     *
     * @param newDistance the new distance.
     */
    void onRecordingMaxDistanceChanged(int newDistance);
}
