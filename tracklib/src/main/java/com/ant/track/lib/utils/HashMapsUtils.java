package com.ant.track.lib.utils;

import android.util.ArrayMap;

import java.util.HashMap;

/**
 * Provides static methods for creating mutable {@code Maps} instances easily.
 */
public class HashMapsUtils {

    /**
     * Creates a {@code HashMap} instance.
     *
     * @return a newly-created, initially-empty {@code HashMap}
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * Creates a {@code ArrayMap} instance.
     */
    public static <K, V> ArrayMap<K, V> newArrayMap() {
        return new ArrayMap<K, V>();
    }
}

