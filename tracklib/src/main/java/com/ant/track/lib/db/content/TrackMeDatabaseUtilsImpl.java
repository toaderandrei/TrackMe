package com.ant.track.lib.db.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.db.DatabaseConstants;
import com.ant.track.lib.db.provider.TrackMeContract;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.utils.LocationUtils;
import com.ant.track.lib.utils.SpeedUtils;

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


    //===========================================start of public methods===========================================//

    @Override
    public Route getRouteById(long routeId) {

        if (routeId < 0) {
            return null;
        }
        Route route = null;
        Cursor cursor = null;
        try {

            cursor = getRouteCursor(routeId);
            if (cursor != null && cursor.moveToFirst()) {
                route = createRouteFromCursor(cursor);
            }
        } catch (Exception exc) {
            route = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return route;
    }

    @Override
    public Uri insertRoutePoint(long routeId, Location location) {
        return getApp().getContentResolver().insert(TrackMeContract.RoutePointEntry.CONTENT_URI,
                createContentValues(routeId, location));
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
        return getApp().getContentResolver().insert(TrackMeContract.RouteEntry.CONTENT_URI, createContentValues(route));
    }

    @Override
    public void deleteRouteTrack(long id) {
        deleteAllRoutePoints(id);

        String where = getWhereCloseForRouteId();
        String[] selectionArgs = new String[]{String.valueOf(id)};

        getContentResolver().delete(TrackMeContract.RouteEntry.CONTENT_URI, where, selectionArgs);
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

    //====================================end of public methods==================================================//

    //====================================private methods========================================================//

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

        if (location.hasBearing()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_BEARING, location.getBearing());
        }

        return contentValues;
    }


    private ContentValues createContentValues(Route route) {
        ContentValues contentValues = new ContentValues();
        RouteStats routeStats = route.getRouteStats();
        if (route.getRouteId() >= 0) {
            contentValues.put(TrackMeContract.RouteEntry._ID, route.getRouteId());
        }

        contentValues.put(TrackMeContract.RouteEntry.TOTAL_TIME, routeStats.getTotalDuration());
        //speed part
        if (SpeedUtils.isSpeedValid(routeStats.getAvgSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.AVG_SPEED, routeStats.getAvgSpeed());
        }
        if (SpeedUtils.isSpeedValid(routeStats.getMinSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_SPEED, routeStats.getMinSpeed());
        }
        if (SpeedUtils.isSpeedValid(routeStats.getMaxSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_SPEED, routeStats.getMaxSpeed());
        }
        if (LocationUtils.isValidLongitude(routeStats.getLongitudeMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(routeStats.getLongitudeMin()));
        }
        if (LocationUtils.isValidLongitude(routeStats.getLongitudeMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(routeStats.getLongitudeMax()));
        }

        if (LocationUtils.isValidLatitude(routeStats.getLatitudeMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_LATITUDE, LocationUtils.getLatitude1E6FromDouble(routeStats.getLatitudeMin()));
        }

        if (LocationUtils.isValidLatitude(routeStats.getLatitudeMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_LATITUDE, LocationUtils.getLatitude1E6FromDouble(routeStats.getLatitudeMax()));
        }

        if (LocationUtils.isValidElevation(routeStats.getElevationMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_ELEVATION, LocationUtils.getElevation1E6FromDouble(routeStats.getElevationMin()));
        }
        if (LocationUtils.isValidElevation(routeStats.getElevationMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_ELEVATION, LocationUtils.getElevation1E6FromDouble(routeStats.getElevationMax()));
        }
        contentValues.put(TrackMeContract.RouteEntry.ELEVATION_GAIN, routeStats.getTotalElevationGain());

        contentValues.put(TrackMeContract.RouteEntry.NAME, route.getRouteName());
        contentValues.put(TrackMeContract.RouteEntry.NUM_ROUTE_POINTS, route.getNumberOfPoints());
        contentValues.put(TrackMeContract.RouteEntry.TOTAL_DISTANCE, routeStats.getTotalDistance());
        contentValues.put(TrackMeContract.RouteEntry.TOTAL_TIME, routeStats.getTotalDuration());
        contentValues.put(TrackMeContract.RouteEntry.START_POINT_ID, route.getStartPointId());
        contentValues.put(TrackMeContract.RouteEntry.STOP_POINT_ID, route.getStopPointId());
        contentValues.put(TrackMeContract.RouteEntry.START_TIME, routeStats.getStartTime());
        contentValues.put(TrackMeContract.RouteEntry.STOP_TIME, routeStats.getStopTime());

        return contentValues;
    }

    private void deleteAllRoutePoints(long id) {
        //delete route points first
        String where = TrackMeContract.RoutePointEntry.ROUTE_ID + " = ";
        String[] selectArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RoutePointEntry.CONTENT_URI, where, selectArgs);
        //route check point
        where = TrackMeContract.RouteCheckPointEntry.ROUTE_ID + " = ";
        selectArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, where, selectArgs);

    }

    private Route createRouteFromCursor(Cursor cursor) {
        int idxId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry._ID);
        int idxName = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.NAME);
        int idxDescription = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.DESCRIPTION);

        int idxAvgSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.AVG_SPEED);
        int idxMinSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_SPEED);
        int idxMaxSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_SPEED);

        int idxLocMinLat = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_LATITUDE);
        int idxLocMaxLat = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_LATITUDE);

        int idxLocMinLong = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_LONGITUDE);
        int idxLocMaxLong = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_LONGITUDE);

        int idxLocMinElev = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_ELEVATION);
        int idxLocMaxElev = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_ELEVATION);

        int idxNumPoints = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.NUM_ROUTE_POINTS);
        int idxTotalTime = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.TOTAL_TIME);
        int idxTotalMovingTime = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.TOTAL_MOVING_TIME);

        int idxDistance = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.TOTAL_DISTANCE);

        int idxStartPointId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.START_POINT_ID);
        int idxStopPointId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.STOP_POINT_ID);
        Route route = new Route();
        RouteStats routeStats = new RouteStats();
        if (!isNull(cursor, idxId)) {
            route.setRouteId(cursor.getLong(idxId));
        }
        //name
        if (!isNull(cursor, idxName)) {
            route.setRouteName(cursor.getString(idxName));
        }
        //desc
        if (!isNull(cursor, idxDescription)) {
            route.setDescription(cursor.getString(idxDescription));
        }
        //stats
        if (!isNull(cursor, idxAvgSpeed)) {
            routeStats.setAvgSpeed(cursor.getDouble(idxAvgSpeed));
        }
        if (!isNull(cursor, idxMinSpeed)) {
            routeStats.setMinSpeed(cursor.getDouble(idxMinSpeed));
        }

        if (!isNull(cursor, idxMaxSpeed)) {
            routeStats.setMaxSpeed(cursor.getDouble(idxMaxSpeed));
        }

        if (!isNull(cursor, idxLocMinLat) &&
                !isNull(cursor, idxLocMaxLat) &&
                !isNull(cursor, idxLocMinLong) &&
                !isNull(cursor, idxLocMaxLong)) {
            routeStats.updateMinMaxLatitudeStats(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocMinLat)), LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocMaxLat)));
            routeStats.updateMinMaxLongStats(LocationUtils.getLongitudeFromLongitude1E6(cursor.getInt(idxLocMinLong)), LocationUtils.getLongitudeFromLongitude1E6(cursor.getInt(idxLocMaxLong)));
        }

        if (!isNull(cursor, idxLocMinElev) && !isNull(cursor, idxLocMaxElev)) {
            routeStats.updateMinMaxElevation(LocationUtils.getElevationFromElevation1E6(cursor.getInt(idxLocMinElev)), LocationUtils.getElevationFromElevation1E6(cursor.getInt(idxLocMaxElev)));
        }

        if (!isNull(cursor, idxStartPointId)) {
            route.setStartPointId(cursor.getLong(idxStartPointId));
        }

        if (!isNull(cursor, idxStopPointId)) {
            route.setStopPointId(cursor.getLong(idxStopPointId));
        }

        if (!isNull(cursor, idxTotalTime)) {
            routeStats.setTotalTime(cursor.getInt(idxTotalTime));
        }

        if (!isNull(cursor, idxNumPoints)) {
            route.setNumberOfPoints(cursor.getInt(idxNumPoints));
        }

        if (!isNull(cursor, idxDistance)) {
            routeStats.setTotalDistance(cursor.getDouble(idxDistance));
        }

        if (!isNull(cursor, idxTotalMovingTime)) {
            routeStats.setTotalMovingTime(cursor.getLong(idxTotalMovingTime));
        }
        route.setRouteStats(routeStats);
        return route;

    }

    private boolean isNull(Cursor cursor, int index) {
        return cursor.isNull(index);
    }


    private Cursor getRouteCursor(long routeId) {
        return getRouteCursor(routeId, null);
    }

    private Cursor getRouteCursor(long routeId, String[] projection) {
        String where = TrackMeContract.RouteEntry._ID + " = ";
        String[] selectArgs = new String[]{String.valueOf(routeId)};
        return getContentResolver().query(TrackMeContract.RouteEntry.CONTENT_URI, projection, where, selectArgs, DatabaseConstants.DEFAULT_ORDER_COLUMN);
    }

    private ContentResolver getContentResolver() {
        return TrackLibApplication.getInstance().getContentResolver();
    }

    @NonNull
    private String getWhereCloseForRouteId() {
        return TrackMeContract.RouteEntry._ID + " = ";
    }

    //====================================end of private methods===============================================//

    private TrackLibApplication getApp() {
        return TrackLibApplication.getInstance();
    }

    public static void reset() {
        instance = null;
    }
}
