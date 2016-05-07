package com.ant.track.activities;

import android.location.Location;

import com.ant.track.fragments.LocationFragment;

/**
 * Created by Toader on 6/2/2015.
 */
public interface TrackStateListener {

    void updateRecordState(boolean update);

    boolean isRecording();
}
