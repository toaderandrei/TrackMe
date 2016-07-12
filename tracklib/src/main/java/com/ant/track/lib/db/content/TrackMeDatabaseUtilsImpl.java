package com.ant.track.lib.db.content;

import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RoutePoint;

/**
 * Track me database utility class. It deals with insertion, deletion,
 * updating and deleting of data from db.
 */
public class TrackMeDatabaseUtilsImpl implements TrackMeDatabaseUtils {

    private static TrackMeDatabaseUtils instance;

    public static TrackMeDatabaseUtils getInstance() {
        if (instance == null) {
            instance = new TrackMeDatabaseUtilsImpl();
        }
        return instance;
    }

    public Route getRouteById(long routeId) {
        Route route = new Route();


        return route;
    }

    @Override
    public void insertRoutePoint(Route route, Location location ) {

    }

    @Override
    public Location getLastValidLocation(long routeId) {
        return null;
    }

    @Override
    public Location getLastValidRouteTrack(long routeId) {
        return null;
    }

    @Override
    public Uri insertRouteTrack(Route route) {
        return null;
    }

    @Override
    public void deleteRouteTrack(int id) {

    }

    @Override
    public void updateRouteTrack(Route route) {

    }

    @Override
    public void updateRouteTrack(int id, ContentValues values) {

    }

    @Override
    public void insertRoutePoint(RoutePoint routePoint) {

    }

    @Override
    public void deleteRoutePoint(int id) {

    }

    @Override
    public void updateRoutePoint(int id, ContentValues values) {

    }

    @Override
    public void updateRoutePoint(RoutePoint routePoint) {

    }

    private TrackLibApplication getApp() {
        return TrackLibApplication.getInstance();
    }

    public static void reset() {
        instance = null;
    }
}
