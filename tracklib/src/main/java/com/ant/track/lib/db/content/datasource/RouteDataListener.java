package com.ant.track.lib.db.content.datasource;

/**
 * interface used to define the operations on the route path.
 * */
public interface RouteDataListener {

    void onRecordingGpsAccuracyChanged(int newRecording);

    void onRecordingDistanceIntervalChanged(int newDistance);
}
