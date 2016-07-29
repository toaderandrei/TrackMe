package com.ant.track.lib.db.content.datasource;

import android.location.Location;

import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteCheckPoint;

/**
 * interface used to define the operations on the route path.
 */
public interface RouteDataListener {

    /**
     * callback for when the accuracy has changed via a prefrence change.
     *
     * @param newValue the new value.
     */
    void onRecordingGpsAccuracyChanged(int newValue);

    /**
     * callback for when the min recording interval preference changed.
     *
     * @param newDistance the new distance
     */
    void onRecordingDistanceIntervalChanged(int newDistance);


    /**
     * callback for when the max distance preference changed.
     *
     * @param newDistance the new distance.
     */
    void onRecordingMaxDistanceChanged(int newDistance);

    /**
     * clear route points from the map.
     */
    void clearPoints();

    /**
     * update of the new point.
     */
    void onNewRoutePointUpdateDone();

    /**
     * adds a location to queue for later processing.
     *
     * @param location location to be added.
     */
    void addLocationToQueue(Location location);

    /**
     * addds the current location to map
     *
     * @param location the location to show on the map.
     */
    void addPendingLocation(Location location);

    /**
     * sets the last shown location.
     *
     * @param location
     */
    void setLastLocation(Location location);

    /**
     * updates in case there is a route update.
     *
     * @param route - the route to be updated.
     */
    void onNewRouteUpdate(Route route);

    /**
     * add a new route check point to the map.
     *
     * @param routeCheckPoint the check point to be added.
     */
    void addRouteCheckPointToMap(RouteCheckPoint routeCheckPoint);

    /**
     * notify the adding of point is finished.
     */
    void onNewRouteCheckPointUpdate();

}
