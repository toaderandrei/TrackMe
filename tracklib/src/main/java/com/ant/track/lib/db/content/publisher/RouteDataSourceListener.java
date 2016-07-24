package com.ant.track.lib.db.content.publisher;


/**
 * Listener to be invoked when {@link} changes.
 */
public interface RouteDataSourceListener {

    /**
     * Notifies when the tracks table is updated.
     */
    void notifyRouteUpdate();

    /**
     * Notifies when the route check points table is updated.
     */
    void notifyRouteCheckPointUpdate();

    /**
     * Notifies when the route points table is updated.
     */
    void notifyRoutePointsUpdate();

    /**
     * Notifies when a preference changes.
     *
     * @param key the preference key
     */
    void notifyPreferenceChanged(String key);
}
