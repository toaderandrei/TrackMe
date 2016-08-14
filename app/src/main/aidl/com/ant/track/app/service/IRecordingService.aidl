// IRecordingService.aidl
package com.ant.track.app.service;

// Declare any non-default types here with import statements

interface IRecordingService {
    /**
     * starts the service and returns the id of the route
     */
    long startNewRoute();

    /**
    * pauses the current route
    **/
    void pauseCurrentRoute();

    /**
    * stops the current route tracking.
    **/
    void stopCurrentRoute();

    /**
    * resumes the current route
    **/
    void resumeRecordingService();

    /**
    * checks if the recording is paused.
    **/
    boolean isPaused();

    /**
    * checks if it is recording
    **/
    boolean isRecording();

    /**
    * returns the current route id.
    **/
    long getRouteId();
}
