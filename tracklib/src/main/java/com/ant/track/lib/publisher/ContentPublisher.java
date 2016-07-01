package com.ant.track.lib.publisher;

/**
 * interface for the content publisher.
 */
public interface ContentPublisher<U, T> {
    /**
     * register listener
     *
     * @param listener
     */
    void registerListener(T listener);

    /**
     * unregister listener
     *
     * @param listener
     */
    void unregisterListener(T listener);

    /**
     * notify listeners.
     *
     * @param
     */
    void notifyListeners(U data);
}
