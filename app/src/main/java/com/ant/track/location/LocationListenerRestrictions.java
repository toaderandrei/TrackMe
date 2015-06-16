package com.ant.track.location;

/**
 * Created by Toader on 6/3/2015.
 */
public interface LocationListenerRestrictions {
    /**
     * Returns the polling interval for receiveing update.
     *
     * @return the polling interval
     */
    long getDesiredPollingInterval();

    /**
     * Returns the minimum distance between updates.
     */
    int getMinDistance();

    /**
     * Notifies the amount of time the user has been idle at his current location.
     *
     * @param idleTime the time that the user has been idle at his current
     *                 location
     */
    void updateIdleTime(long idleTime);
}
