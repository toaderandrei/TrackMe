package com.ant.track.lib.db.content;

import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.db.TrackMeContract;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.utils.LocationUtils;

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
    public Uri insertRoutePoint(long routeId, Location location) {
        return TrackLibApplication.getInstance().getContentResolver().insert(TrackMeContract.RoutePointEntry.CONTENT_URI,
                createContentValues(routeId, location));
    }

    private ContentValues createContentValues(long routeId, Location location) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TrackMeContract.RoutePointEntry.ROUTE_ID, routeId);
        contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LAT, LocationUtils.getLatitude1E6FromDouble(location.getLatitude()));
        contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LONG, LocationUtils.getLongitude1E6FromDouble(location.getLongitude()));

        long time;
        if (location.getTime() == 0) {
            time = System.currentTimeMillis();
        } else {
            time = location.getTime();
        }
        contentValues.put(TrackMeContract.RoutePointEntry.TIME, time);

        if (location.hasSpeed()) {
            contentValues.put(TrackMeContract.RoutePointEntry.SPEED, location.getSpeed());
        }

        if (location.hasAccuracy()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ACCURACY, location.getAccuracy());
        }

        if (location.hasAltitude()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ALT, location.getAltitude());
        }
        return contentValues;
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
