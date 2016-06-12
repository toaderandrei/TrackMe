package com.ant.track.lib.db.content;

import android.content.ContentValues;
import android.net.Uri;

import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RoutePoint;

/**
 * utility class used for creating route objects out
 * of database objects and vice-versa.
 */
public interface TrackMeDatabaseUtils {
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
    void deleteRouteTrack(int id);

    /**
     * updates a route from the database.
     *
     * @param route to be updated
     */
    void updateRouteTrack(Route route);

    /**
     * updates a route from the db based on its id
     * and the new values.
     *
     * @param id     the id of the route to be updated
     * @param values the values used for update
     */
    void updateRouteTrack(int id, ContentValues values);

    /**
     * Inserts a routepoint into the db.
     *
     * @param routePoint the new point to be inserted.
     */
    void insertRoutePoint(RoutePoint routePoint);

    /**
     * delets a routepoint based on its id.
     *
     * @param id the id of the route point to be deleted.
     */
    void deleteRoutePoint(int id);

    /**
     * Updates a route point based on its id and the new values.
     *
     * @param id     the id of the routepoint
     * @param values the new values.
     */
    void updateRoutePoint(int id, ContentValues values);

    /**
     * updates a routepoint from the database.
     *
     * @param routePoint the routepoint to be updated.
     */
    void updateRoutePoint(RoutePoint routePoint);
}
