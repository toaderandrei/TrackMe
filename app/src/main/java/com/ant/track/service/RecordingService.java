package com.ant.track.service;

import android.location.Location;

/**
 * Created by andrei on 05/05/16.
 */
public interface RecordingService {
    /**
     * Starts recording a new track.
     *
     * @return the track ID of the new track.
     */
    void startNewTracking();

    /**
     * Ends the current track.
     */
    void endCurrentTracking();

    /**
     * Ends the current recording track.
     */
    void stopRecording();

    /**
     * Returns true if the current recording track is paused. Returns true if not recording.
     */
    boolean isStopped();
}
