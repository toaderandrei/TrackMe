package com.ant.track.lib.publisher;

/**
 * interface for the content publisher.
 */
public interface ContentPublisher<U, T> {
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
     * @param
     */
    void notifyListeners(U data);
}
