package com.ant.track.app.location;

/**
 * Adaptive location listener.
 */
public class LocationListenerRestrictionsImpl implements LocationListenerRestrictions {

    private long maxInterval;
    private long minInterval = 0;
    private long idleTime;
    private int minDistance;

    public LocationListenerRestrictionsImpl(long minInterval, long maxInterval, int minDistance) {
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.minDistance = minDistance;
    }

    public LocationListenerRestrictionsImpl(long minInterval) {
        this(minInterval, 0, 0);
    }

    @Override
    public long getDesiredPollingInterval() {
        long desiredInterval = idleTime / 2;
        desiredInterval = (desiredInterval / 1000) * 1000;
        return Math.max(Math.min(desiredInterval, maxInterval), minInterval);
    }

    @Override
    public int getMinDistance() {
        return minDistance;
    }

    @Override
    public void updateIdleTime(long idleTime) {
        //it does not make sense to update the interval unless
        //it is not the default one, meaning it has a min and max
        if (maxInterval != 0L) {
            this.idleTime = idleTime;
        }
    }
}
