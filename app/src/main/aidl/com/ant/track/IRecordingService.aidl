// IRecordingService.aidl
package com.ant.track;


// Declare any non-default types here with import statements

interface IRecordingService {
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
        * Returns true if currently recording a track.
        */
       boolean isRecording();

       /**
        * Returns true if the current recording track is paused. Returns true if not recording.
        */
       boolean isStopped();

       Location getLocation();
}
