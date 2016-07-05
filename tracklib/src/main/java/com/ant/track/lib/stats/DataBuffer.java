package com.ant.track.lib.stats;

/**
 * Interface for the data collected.
 */
public interface DataBuffer {

    void setNext(double value);

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

    void reset();

    boolean isFull();
}
