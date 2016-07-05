package com.ant.track.lib.stats;

import java.util.LinkedList;
import java.util.List;

/**
 * Buffer that collects data in order to compute statistics
 * for the trip.
 */
public class DataBufferImpl implements DataBuffer {

    private boolean isFull = false;

    private List<Double> buffer;
    private int currentIndex = 0;

    public DataBufferImpl(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size is less than 1.");
        }
        buffer = new LinkedList<>();
    }

    @Override
    public void setNext(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be less than 0");
        }
        buffer.add(value);
        currentIndex++;
    }

    @Override
    public double getAverage() {
        int numEntries = isFull ? buffer.size() : currentIndex;
        double average = 0;
        for (int k = 0; k < numEntries; k++) {
            average += buffer.get(k);
        }

        return average / numEntries;
    }

    @Override
    public double getVariance() {

        int numEntries = isFull ? buffer.size() : currentIndex;
        double variance = 0;
        for (int k = 0; k < numEntries; k++) {
            variance += Math.pow(buffer.get(k), 2);
        }

        return variance / numEntries;
    }

    @Override
    public void reset() {
        isFull = false;
        currentIndex = 0;
        buffer.clear();
    }

    @Override
    public boolean isFull() {
        return isFull;
    }
}
