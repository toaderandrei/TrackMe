package com.ant.track.lib.db.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.db.DatabaseConstants;
import com.ant.track.lib.db.provider.TrackMeContract;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.utils.LocationUtils;
import com.ant.track.lib.utils.SpeedUtils;

/**
 * Track me database utility class. It deals with insertion, deletion,
 * updating and deleting of data from db. It is used for assembling data
 * from db rows and vice-versa. The code is based on the MyTracks app.
 * Some things have been changed some are the same.
 */
public class TrackMeDatabaseUtilsImpl implements TrackMeDatabaseUtils {

    public static final String PARAM_SELECT_MAX = "=(select max(";
    public static final String PARAM_FROM = ") from ";
    public static final String PARAM_WHERE = " WHERE ";
    public static final String PARAM_AND = "=? AND ";
    public static final String PARAM_LESS_OR_EQUAL = "<=";
    public static final String PARAM_COMMA_CLOSE = ")";
    public static final String PARAM_SELECT_MIN = "=(select min(";
    public static final String PARAM_EQUAL_QUESTION_MARK = "=?)";
    public static final String PARAM_EQUAL_STRING = " =?";
    private static TrackMeDatabaseUtils instance;
    private static final String TAG = TrackMeDatabaseUtilsImpl.class.getCanonicalName();

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
        RoutePoint routePoint = new RoutePoint();
        routePoint.setLocation(location);

        return insertRoutePoint(routePoint);
    }

    @Override
    public long getFirstRoutePointId(long trackId) {
        if (trackId < 0) {
            return -1L;
        }
        Cursor cursor = null;
        try {
            String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MIN + TrackMeContract.RoutePointEntry._ID
                    + PARAM_FROM + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID
                    + PARAM_EQUAL_QUESTION_MARK;
            String[] selectionArgs = new String[]{Long.toString(trackId)};
            cursor = getRoutePointCursor(new String[]{TrackMeContract.RoutePointEntry._ID},
                    selection,
                    selectionArgs,
                    TrackMeContract.RoutePointEntry._ID);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1L;
    }

    @Override
    public long getLastValidPointId(long routeId) {
        if (routeId < 0) {
            return -1L;
        }
        Cursor cursor = null;
        try {
            String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MAX + TrackMeContract.RoutePointEntry._ID
                    + TrackMeContract.RoutePointEntry.TABLE_NAME + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID
                    + PARAM_EQUAL_QUESTION_MARK;
            String[] selectionArgs = new String[]{Long.toString(routeId)};
            cursor = getRoutePointCursor(new String[]{TrackMeContract.RoutePointEntry._ID},
                    selection,
                    selectionArgs,
                    TrackMeContract.RoutePointEntry._ID);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1L;
    }

    @Override
    public RouteCheckPoint getLastCheckPoint(long routeId) {
        if (routeId < 0) {
            return null;
        }
        Cursor cursor;

        try {
            String selection = TrackMeContract.RouteCheckPointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
            String[] args = new String[]{String.valueOf(routeId)};
            cursor = getContentResolver().query(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, null, selection, args, DatabaseConstants.DEFAULT_ORDER_COLUMN);
            return createRouteCheckPointFromCursor(cursor);
        } catch (Exception exc) {
            Log.e(TAG, "exception:" + exc);
        }
        return null;

    }

    private Cursor getRoutePointCursor(String[] strings, String selection, String[] selectionArgs, String sortOrder) {
        return getContentResolver().query(TrackMeContract.RoutePointEntry.CONTENT_URI, strings, selection, selectionArgs, sortOrder);
    }


    @Override
    public Location getFirstValidRoutePointForRoute(long routeid) {
        if (routeid < 0) {
            return null;
        }
        String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MIN + TrackMeContract.RoutePointEntry._ID + PARAM_FROM
                + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_AND
                + TrackMeContract.RoutePointEntry.LOCATION_LAT + PARAM_LESS_OR_EQUAL + Constants.MAX_LATITUDE + PARAM_COMMA_CLOSE;
        String[] selectionArgs = new String[]{Long.toString(routeid)};
        return findTrackPointBy(selection, selectionArgs);
    }

    @Override
    public Location getLastValidLocationForRoute(long routeId) {
        if (routeId < 0) {
            return null;
        }
        String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MAX + TrackMeContract.RoutePointEntry._ID + PARAM_FROM
                + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_AND
                + TrackMeContract.RoutePointEntry.LOCATION_LAT + PARAM_LESS_OR_EQUAL + Constants.MAX_LATITUDE + PARAM_COMMA_CLOSE;
        String[] selectionArgs = new String[]{Long.toString(routeId)};
        return findTrackPointBy(selection, selectionArgs);
    }

    private Location findTrackPointBy(String selection, String[] selectionArgs) {
        return null;
    }


    @Override
    public Uri insertRouteTrack(Route route) {
        return getApp().getContentResolver().insert(TrackMeContract.RouteEntry.CONTENT_URI, createContentValues(route));
    }

    @Override
    public void deleteRouteTrack(long id) {
        deleteAllRoutePoints(id);
        String where = getWhereCloseForRoute();
        String[] selectionArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RouteEntry.CONTENT_URI, where, selectionArgs);
    }

    @Override
    public int updateRouteTrack(Route route) {
        String where = TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
        String[] args = new String[]{String.valueOf(route.getRouteId())};
        ContentValues values = createContentValues(route);
        return getContentResolver().update(TrackMeContract.RouteEntry.CONTENT_URI, values, where, args);
    }

    @Override
    public int updateRouteTrack(long id, ContentValues values) {
        String where = TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
        String[] args = new String[]{String.valueOf(id)};
        return getContentResolver().update(TrackMeContract.RouteEntry.CONTENT_URI, values, where, args);
    }

    @Override
    public Uri insertRoutePoint(RoutePoint routePoint) {
        return getApp().getContentResolver().insert(TrackMeContract.RoutePointEntry.CONTENT_URI,
                createContentValues(routePoint));
    }

    @Override
    public Uri insertRouteCheckPoint(RouteCheckPoint routeCheckPoint) {
        return getApp().getContentResolver().insert(TrackMeContract.RoutePointEntry.CONTENT_URI,
                createContentValues(routeCheckPoint));
    }

    @Override
    public void deleteRoutePoint(long id) {
        String where = getWhereCloseForRoutePoint();
        String[] args = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RoutePointEntry.CONTENT_URI, where, args);
    }


    @Override
    public void deleteRouteCheckPoint(long id) {
        String where = getWhereCloseForRouteCheckPoint();
        String[] args = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, where, args);
    }

    @Override
    public int updateRoutePointById(long id, ContentValues values) {
        String where = getWhereCloseForRouteCheckPoint();
        String[] args = new String[]{String.valueOf(id)};
        return getContentResolver().update(TrackMeContract.RoutePointEntry.CONTENT_URI, values, where, args);
    }

    @Override
    public int updateRoutePoint(RoutePoint routePoint) {
        String where = TrackMeContract.RouteEntry._ID + " =?";
        String[] args = new String[]{String.valueOf(routePoint.getId())};
        ContentValues values = createContentValues(routePoint);
        return getContentResolver().update(TrackMeContract.RouteEntry.CONTENT_URI, values, where, args);
    }

    //====================================end of public methods==================================================//

    //====================================private methods========================================================//

    private ContentValues createContentValues(RoutePoint routePoint) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TrackMeContract.RoutePointEntry.ROUTE_ID, routePoint.getId());
        if (LocationUtils.isValidLatitude(routePoint.getLatitude())) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LAT, LocationUtils.getLatitude1E6FromDouble(routePoint.getLatitude()));
        }
        if (LocationUtils.isValidLongitude(routePoint.getLongitude())) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LONG, LocationUtils.getLongitude1E6FromDouble(routePoint.getLongitude()));
        }

        long time;
        if (routePoint.getTime() == 0) {
            time = System.currentTimeMillis();
        } else {
            time = routePoint.getTime();
        }
        contentValues.put(TrackMeContract.RoutePointEntry.TIME, time);

        if (routePoint.hasSpeed()) {
            contentValues.put(TrackMeContract.RoutePointEntry.SPEED, routePoint.getSpeed());
        }

        if (routePoint.hasAccuracy()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ACCURACY, routePoint.getAccuracy());
        }

        if (routePoint.hasAltitude()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ALT, routePoint.getAltitude());
        }

        if (routePoint.hasBearing()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_BEARING, routePoint.getBearing());
        }

        return contentValues;
    }


    private ContentValues createContentValues(RouteCheckPoint routeCheckPoint) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TrackMeContract.RouteCheckPointEntry.NAME, routeCheckPoint.getName());
        contentValues.put(TrackMeContract.RouteCheckPointEntry.DESCRIPTION, routeCheckPoint.getDescription());
        contentValues.put(TrackMeContract.RouteCheckPointEntry.MARKER_COLOR, routeCheckPoint.getMarkerColor());
        contentValues.put(TrackMeContract.RoutePointEntry.ROUTE_ID, routeCheckPoint.getId());
        if (LocationUtils.isValidLatitude(routeCheckPoint.getLatitude())) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_LAT, LocationUtils.getLatitude1E6FromDouble(routeCheckPoint.getLatitude()));
        }
        if (LocationUtils.isValidLongitude(routeCheckPoint.getLongitude())) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_LONG, LocationUtils.getLongitude1E6FromDouble(routeCheckPoint.getLongitude()));
        }

        long time;
        if (routeCheckPoint.getTime() == 0) {
            time = System.currentTimeMillis();
        } else {
            time = routeCheckPoint.getTime();
        }
        contentValues.put(TrackMeContract.RouteCheckPointEntry.TIME, time);

        if (routeCheckPoint.hasSpeed()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.SPEED, routeCheckPoint.getSpeed());
        }

        if (routeCheckPoint.hasAccuracy()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_ACCURACY, routeCheckPoint.getAccuracy());
        }

        if (routeCheckPoint.hasAltitude()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_ALT, routeCheckPoint.getAltitude());
        }

        if (routeCheckPoint.hasBearing()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_BEARING, routeCheckPoint.getBearing());
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
        String where = TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
        String[] selectArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RoutePointEntry.CONTENT_URI, where, selectArgs);
        //route check point
        where = TrackMeContract.RouteCheckPointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
        selectArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, where, selectArgs);

    }

    private RouteCheckPoint createRouteCheckPointFromCursor(Cursor cursor) {
        int idxId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry._ID);
        int idxName = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.NAME);
        int idxDescription = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.DESCRIPTION);
        int idxRouteId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.ROUTE_ID);
        int idxSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.SPEED);
        int idxTime = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.TIME);

        int idxLocAccuracy = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_ACCURACY);
        int idxLocLatitude = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_LAT);
        int idxLocLongitude = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_LONG);
        int idxLocBearing = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_BEARING);
        int idxLocAlt = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_ALT);
        int idxMarkerColor = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.MARKER_COLOR);

        RouteCheckPoint routeCheckPoint = new RouteCheckPoint();
        if (!isNull(cursor, idxId)) {
            routeCheckPoint.setId(cursor.getLong(idxId));
        }
        //name
        if (!isNull(cursor, idxName)) {
            routeCheckPoint.setName(cursor.getString(idxName));
        }
        //desc
        if (!isNull(cursor, idxDescription)) {
            routeCheckPoint.setDescription(cursor.getString(idxDescription));
        }
        //stats
        if (!isNull(cursor, idxSpeed)) {
            routeCheckPoint.setSpeed(cursor.getFloat(idxSpeed));
        }
        if (!isNull(cursor, idxTime)) {
            routeCheckPoint.setTime(cursor.getLong(idxTime));
        }

        if (!isNull(cursor, idxLocAccuracy)) {
            routeCheckPoint.setLocationBearing(cursor.getFloat(idxLocBearing));
        }

        if (!isNull(cursor, idxLocAlt)) {
            routeCheckPoint.setLocation_alt(cursor.getFloat(idxLocAlt));
        }

        if (!isNull(cursor, idxLocLatitude) &&
                !isNull(cursor, idxLocLongitude)) {
            routeCheckPoint.setLocation_lat(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocLatitude)));
            routeCheckPoint.setLocation_long(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocLongitude)));
        }

        if (!isNull(cursor, idxMarkerColor)) {
            routeCheckPoint.setLocationBearing(cursor.getInt(idxMarkerColor));
        }

        if (!isNull(cursor, idxRouteId)) {
            routeCheckPoint.setRouteId(cursor.getLong(idxLocBearing));
        }
        return routeCheckPoint;

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
    private String getWhereCloseForRoute() {
        return TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
    }

    @NonNull
    private String getWhereCloseForRoutePoint() {
        return TrackMeContract.RoutePointEntry._ID + PARAM_EQUAL_STRING;
    }

    @NonNull
    private String getWhereCloseForRouteCheckPoint() {
        return TrackMeContract.RouteCheckPointEntry._ID + PARAM_EQUAL_STRING;
    }


    //====================================end of private methods===============================================//

    private TrackLibApplication getApp() {
        return TrackLibApplication.getInstance();
    }

    public static void reset() {
        instance = null;
    }
}
