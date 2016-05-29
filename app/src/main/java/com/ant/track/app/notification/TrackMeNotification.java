package com.ant.track.app.notification;

import android.content.Context;

/**
 * Notification system for when the track is in background
 * and the tracking has started.
 */
public class TrackMeNotification extends AbstractTrackMeNotification {

    TrackMeNotification(Context context) {
        super(context);
    }

    @Override
    public void startForegroundService() {

    }

    @Override
    public void showNotification() {

    }
}
