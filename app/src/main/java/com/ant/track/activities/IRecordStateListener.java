package com.ant.track.activities;

import android.location.Location;

/**
 * Created by Toader on 6/2/2015.
 */
public interface IRecordStateListener {
    void updateRecordState(boolean update);

    boolean isRecording();

    Location getLastLocation();
}
