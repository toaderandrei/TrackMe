package com.ant.track.lib.db.content.factory;

import android.location.Location;

import java.util.Iterator;

/**
 * iterator for location inside database.
 */
public interface LocationIterator extends Iterator<Location> {

    /**
     * Gets the most recently retrieved track point id by {@link #next()}.
     */
    public long getLocationId();

    /**
     * Closes the iterator.
     */
    public void close();
}

