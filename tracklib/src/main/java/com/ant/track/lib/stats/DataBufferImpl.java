package com.ant.track.lib.stats;

import java.util.LinkedList;
import java.util.List;

/**
 * Buffer that collects data in order to compute statistics
 * for the trip.
 */
public class DataBufferImpl implements DataBuffer {

    private boolean hasSufficientReadings = false;

    private List<Double> buffer;
    private int currentIndex = 0;
    private double max = 0;
    private double min = 0;

    private int maxSize = 25;

    public DataBufferImpl(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Size is less than 1.");
        }
        this.maxSize = maxSize;
        buffer = new LinkedList<>();
    }

    @Override
    public void setNext(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be less than 0");
        }
        if (value > max) {
            max = value;
        }
        if (value < min) {
            min = value;
        }
        buffer.add(value);
        currentIndex++;
        if (currentIndex >= maxSize) {
            hasSufficientReadings = true;
        }
    }

    @Override
    public double getAverage() {
        int numEntries = hasSufficientReadings() ? buffer.size() : currentIndex;
        double average = 0;
        for (int k = 0; k < numEntries; k++) {
            average += buffer.get(k);
        }
        return average / numEntries;
    }

    @Override
    public double getVariance() {

        int numEntries = hasSufficientReadings() ? buffer.size() : currentIndex;
        double variance = 0;
        for (int k = 0; k < numEntries; k++) {
            variance += Math.pow(buffer.get(k), 2);
        }

        return variance / numEntries;
    }

    @Override
    public void reset() {
        hasSufficientReadings = false;
        currentIndex = 0;
        buffer.clear();
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
    public boolean hasSufficientReadings() {
        return hasSufficientReadings;
    }
}
