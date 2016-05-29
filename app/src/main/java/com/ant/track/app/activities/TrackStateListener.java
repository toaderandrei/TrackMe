package com.ant.track.app.activities;

/**
 * Created by Toader on 6/2/2015.
 */
public interface TrackStateListener {

    void updateRecordState(boolean update);

    boolean isRecording();
}
