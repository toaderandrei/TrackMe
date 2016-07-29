package com.ant.track.app.activities;

import com.ant.track.lib.service.RecordingState;

/**
 * listener for communicating with the activity.
 */
public interface RecordStateListener {

    /**
     * updates the recording state
     *
     * @param update
     */
    void updateServiceState(RecordingState update);

    long getRouteId();
}
