package com.ant.track.location;

/**
 * Created by Toader on 6/3/2015.
 */
public class AbsoluteLocationListenerRestrictions implements LocationListenerRestrictions {
    private final long interval;

    /**
     * Constructor.
     *
     * @param interval the interval to request for gps signal
     */
    public AbsoluteLocationListenerRestrictions(long interval) {
        this.interval = interval;
    }

    @Override
    public long getDesiredPollingInterval() {
        return interval;
    }

    @Override
    public int getMinDistance() {
        return 0;
    }

    @Override
    public void updateIdleTime(long idleTime) {
        // Ignore
    }
}
