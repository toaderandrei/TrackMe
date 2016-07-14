package com.ant.track.lib.publisher;

import com.ant.track.lib.model.Route;

/**
 * interface for the content publisher.
 */
public interface ContentPublisher<T> {
    /**
     * register listener
     *
     * @param listener - to be registered
     */
    void registerListener(T listener);

    /**
     * unregister listener
     *
     * @param listener to be unregistered
     */
    void unregisterListener(T listener);

    /**
     * notify listeners.
     *
     * @param route to be notified about
     */
    void notifyListeners(Route route);
}
