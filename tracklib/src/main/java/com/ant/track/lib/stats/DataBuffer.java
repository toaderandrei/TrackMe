package com.ant.track.lib.stats;

/**
 * Interface for the data collected.
 */
public interface DataBuffer {

    void setNext(double value, boolean force);

    /**
     * gets the average of values.
     *
     * @return the average
     */
    double getAverage();

    /**
     * gets the variance of values.
     *
     * @return
     */
    double getVariance();

    double getMin();

    double getMax();

    void reset();

    boolean isFull();
}
