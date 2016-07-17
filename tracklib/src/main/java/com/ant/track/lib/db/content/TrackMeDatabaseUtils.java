package com.ant.track.lib.db.content;

import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;

import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.model.RoutePoint;

/**
 * utility class used for creating route objects out
 * of database objects and vice-versa.
 */
public interface TrackMeDatabaseUtils {
    /**
     * gets the id of the first route point.
     *
     * @param trackId the route id.
     * @return the id of the route point.
     */
    long getFirstRoutePointId(long trackId);

    /**
     * gets the last valid route point id.
     *
     * @param routeId the id of the route.
     * @return the id
     */
    long getLastValidPointId(long routeId);

    /**
     * gets the first location for route.
     *
     * @param routeid the id of the route.
     * @return the first valid location.
     */
    Location getFirstValidRoutePointForRoute(long routeid);

    /**
     * gets the last valid location for route.
     *
     * @param routeId the id of the route.
     * @return the last valid location.
     */
    Location getLastValidLoctionForRoute(long routeId);

    /**
     * inserts a new route into the database
     *
     * @param route the route to be inserted.
     */
    Uri insertRouteTrack(Route route);

    /**
     * deletes a route by id.
     *
     * @param id of the route to be deleted.
     */
    void deleteRouteTrack(long id);

    /**
     * updates a route from the database.
     *
     * @param route to be updated
     */
    int updateRouteTrack(Route route);

    /**
     * updates a route from the db based on its id
     * and the new values.
     *
     * @param id     the id of the route to be updated
     * @param values the values used for update
     * @return an integer, which, if bigger than 0 means, the update was successful, otherwise
     * not.
     */
    int updateRouteTrack(long id, ContentValues values);

    /**
     * Inserts a routepoint into the db.
     *
     * @param routeCheckPoint the new point to be inserted.
     * @return the uri of the inserted value.
     */
    Uri insertRouteCheckPoint(RouteCheckPoint routeCheckPoint);

    /**
     * Inserts a routepoint into the db.
     *
     * @param routePoint the new point to be inserted.
     * @return the uri of the inserted value.
     */
    Uri insertRoutePoint(RoutePoint routePoint);

    /**
     * deletes a routepoint based on its id.
     *
     * @param id the id of the route point to be deleted.
     */
    void deleteRoutePoint(long id);

    /**
     * deletes a route check point by id.
     *
     * @param id the id of the routecheckpoint.
     */
    void deleteRouteCheckPoint(long id);

    /**
     * Updates a route point based on its id and the new values.
     *
     * @param id     the id of the routepoint
     * @param values the new values.
     */
    int updateRoutePointById(long id, ContentValues values);

    /**
     * updates a routepoint from the database.
     *
     * @param routePoint the routepoint to be updated.
     * @return a positive number if the update was successful, otherwise a negative number.
     */
    int updateRoutePoint(RoutePoint routePoint);

    /**
     * gets a route by id.
     *
     * @param routeId the id of the route
     * @return the Route by id.
     */
    Route getRouteById(long routeId);

    Uri insertRoutePoint(long routeId, Location location);
}
