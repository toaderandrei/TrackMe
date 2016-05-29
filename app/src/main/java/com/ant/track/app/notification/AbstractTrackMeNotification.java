package com.ant.track.app.notification;

import android.content.Context;

/**
 * Abstract implementation of the Notification system.
 */
public abstract class AbstractTrackMeNotification {

    private Context context;

    public AbstractTrackMeNotification(Context service) {
        this.context = context;
    }

    public abstract void showNotification();

    public abstract void startForegroundService();


    protected Context getContext() {
        return context;
    }
}
