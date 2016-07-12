package com.ant.track.lib.stats;

import android.util.Log;

/**
 * Buffer that collects data in order to compute statistics
 * for the trip.
 */
public class DataBufferImpl implements DataBuffer {

    private boolean isFull = false;
    private Double[] buffer;
    private int currentIndex = 0;
    private double max = 0;
    private double min = 0;
    private static final String TAG = DataBufferImpl.class.getCanonicalName();

    public DataBufferImpl(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Size is less than 1.");
        }
        buffer = new Double[maxSize];
    }

    @Override
    public void setNext(double value, boolean force) {
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be less than 0");
        }
        if (isFull && !force) {
            Log.d(TAG, "it is full, please free some space.");
            return;
        }
        if (value >= max) {
            max = value;
        }
        if (value < min) {
            min = value;
        }
        if (currentIndex == buffer.length) {
            currentIndex = 0;
        }

        buffer[currentIndex] = value;
        currentIndex++;
        if (currentIndex == buffer.length) {
            isFull = true;
        }
    }

    @Override
    public double getAverage() {
        int numEntries = isFull() ? buffer.length : currentIndex;
        if (numEntries == 0) {
            return 0;
        }

        double average = 0;
        for (int k = 0; k < numEntries; k++) {
            average += buffer[k];
        }
        return average / numEntries;
    }

    @Override
    public double getVariance() {

        int numEntries = isFull() ? buffer.length : currentIndex;
        if (numEntries == 0) {
            return 0;
        }
        double variance = 0;
        for (int k = 0; k < numEntries; k++) {
            variance += Math.pow(buffer[k], 2);
        }

        return variance / numEntries;
    }

    @Override
    public void reset() {
        isFull = false;
        currentIndex = 0;
        buffer = null;
    }

    @Override
    public double getMax() {
        return max;
    }

    @Override
    public double getMin() {
        return min;
    }

    @Override
    public boolean isFull() {
        return isFull;
    }
}
