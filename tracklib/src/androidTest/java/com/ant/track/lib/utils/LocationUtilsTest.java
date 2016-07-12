package com.ant.track.lib.utils;

import android.location.Location;
import android.support.annotation.NonNull;

import java.util.Random;

/**
 * Utility class for test.
 */
public class LocationUtilsTest {

    @NonNull
    public static Location getRandomLocation() {
        Location loc1 = new Location("GPS");
        double latitude = (new Random()).nextDouble() + 79;
        double longitude = (new Random()).nextDouble() + 169;
        float speed = 1 + (new Random()).nextFloat() + 40;
        long time = System.currentTimeMillis();
        loc1.setLatitude(latitude);
        loc1.setLongitude(longitude);
        loc1.setSpeed(speed);
        loc1.setTime(time);
        return loc1;
    }

    public static float getRandomSpeed(int min, int max) {
        int range = max - min;

        float speed = (new Random()).nextFloat() * range;
        speed += min;
        return speed;
    }

    public static float getCurrentTime() {
        return System.currentTimeMillis();
    }
}
