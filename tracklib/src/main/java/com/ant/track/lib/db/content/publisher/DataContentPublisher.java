package com.ant.track.lib.db.content.publisher;

import com.ant.track.lib.db.content.datasource.RouteDataListener;
import com.ant.track.lib.db.content.datasource.RouteType;

import java.util.EnumSet;
import java.util.Set;

/**
 * interface that defines the DataContentPublisher methods.
 */
public interface DataContentPublisher {

    /**
     * registers listener
     *
     * @param listener - to be registered
     */
    void registerListener(RouteDataListener listener, EnumSet<RouteType> types);

    /**
     * unregisters a listener
     *
     * @param listener - to be unregistered.
     */
    void unregisterListener(RouteDataListener listener);

    /**
     * retrieves a list of all the listeners
     *
     * @return all the listeners.
     */
    Set<RouteDataListener> getListeners();

    /**
     * retrieves a list of all the types for a specific listener
     *
     * @param listener the type for which we want to retrieve the types.
     * @return the list of rype.
     */
    EnumSet<RouteType> getRouteTypes(final RouteDataListener listener);


    /**
     * retrieves all teh route tytpes from the cache.
     *
     * @return all the route typs are retrieved.
     */
    EnumSet<RouteType> getAllRouteTypes();

    /**
     * retrieves a list of all the listener for a specific type
     *
     * @param type the type for which we want to retrieve the listeners.
     * @return the list of listeners.
     */
    Set<RouteDataListener> getListenersByType(final RouteType type);

    /**
     * Retrieves the number of listeners.
     *
     * @return the number of listeners.
     */
    int getRouteTypesCount();

    /**
     * retrieves all the route listeners.
     *
     * @return all the listeners.
     */
    int getRouteListenersCount();

    /**
     * clears all the listeners.
     */
    void clearAll();
}
