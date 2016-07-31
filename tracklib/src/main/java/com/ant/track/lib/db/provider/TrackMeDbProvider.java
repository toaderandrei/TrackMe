package com.ant.track.lib.db.provider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.db.DatabaseConstants;
import com.ant.track.lib.db.exceptions.TrackMeProviderExceptions;
import com.ant.track.lib.db.helper.TrackMeOpenHelper;

/**
 * Data provider for the TrackMe application.
 */
public class TrackMeDbProvider extends ContentProvider {

    private static final String PATH_SEPARATOR_PROVIDER = "/#";

    private TrackMeOpenHelper trackMeOpenHelper;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        //com.ant.track
        final String authority = TrackMeContract.CONTENT_AUTHORITY;

        //com.ant.track/route/0
        matcher.addURI(authority, TrackMeContract.RouteEntry.TABLE_NAME, UriType.ROUTE.ordinal());
        matcher.addURI(authority, TrackMeContract.RouteEntry.TABLE_NAME + PATH_SEPARATOR_PROVIDER, UriType.ROUTE_ID.ordinal());

        matcher.addURI(authority, TrackMeContract.RoutePointEntry.TABLE_NAME, UriType.ROUTE_POINT.ordinal());
        matcher.addURI(authority, TrackMeContract.RoutePointEntry.TABLE_NAME + PATH_SEPARATOR_PROVIDER, UriType.ROUTE_POINT_ID.ordinal());

        matcher.addURI(authority, TrackMeContract.RouteCheckPointEntry.TABLE_NAME, UriType.ROUTE_CHECK_POINT.ordinal());
        matcher.addURI(authority, TrackMeContract.RouteCheckPointEntry.TABLE_NAME + PATH_SEPARATOR_PROVIDER, UriType.ROUTE_CHECK_POINT_ID.ordinal());

        return matcher;
    }

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        trackMeOpenHelper = new TrackMeOpenHelper(getContext());
        return true;
    }


    private UriType getUriType(Uri url) {
        int id = sUriMatcher.match(url);
        return UriType.values()[id];
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final UriType match = getUriType(uri);

        switch (match) {
            case ROUTE:
                return TrackMeContract.RouteEntry.CONTENT_TYPE;
            case ROUTE_ID:
                return TrackMeContract.RouteEntry.CONTENT_ITEMTYPE;
            case ROUTE_POINT:
                return TrackMeContract.RoutePointEntry.CONTENT_TYPE;
            case ROUTE_POINT_ID:
                return TrackMeContract.RoutePointEntry.CONTENT_ITEMTYPE;
            case ROUTE_CHECK_POINT:
                return TrackMeContract.RouteCheckPointEntry.CONTENT_TYPE;
            case ROUTE_CHECK_POINT_ID:
                return TrackMeContract.RouteCheckPointEntry.CONTENT_ITEMTYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor queryCursor;

        UriType match = getUriType(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String sort;
        switch (match) {
            case ROUTE: {
                queryBuilder.setTables(TrackMeContract.RouteEntry.TABLE_NAME);
                sort = sortOrder == null ? DatabaseConstants.DEFAULT_ORDER_COLUMN : sortOrder;
                break;
            }
            case ROUTE_ID:
                queryBuilder.setTables(TrackMeContract.RouteEntry.TABLE_NAME);
                queryBuilder.appendWhere(TrackMeContract.RouteEntry._ID + "=" + uri.getPathSegments().get(1));
                sort = sortOrder == null ? DatabaseConstants.DEFAULT_ORDER_COLUMN : sortOrder;
                break;
            case ROUTE_POINT: {
                sort = sortOrder == null ? DatabaseConstants.DEFAULT_ORDER_COLUMN : sortOrder;
                queryBuilder.setTables(TrackMeContract.RoutePointEntry.TABLE_NAME);
                break;
            }
            case ROUTE_POINT_ID:
                queryBuilder.setTables(TrackMeContract.RoutePointEntry.TABLE_NAME);
                queryBuilder.appendWhere(TrackMeContract.RoutePointEntry._ID + "=" + uri.getPathSegments().get(1));
                sort = sortOrder == null ? DatabaseConstants.DEFAULT_ORDER_COLUMN : sortOrder;
                break;
            case ROUTE_CHECK_POINT: {
                queryBuilder.setTables(TrackMeContract.RouteCheckPointEntry.TABLE_NAME);
                sort = sortOrder == null ? DatabaseConstants.DEFAULT_ORDER_COLUMN : sortOrder;
                break;
            }
            case ROUTE_CHECK_POINT_ID:
                queryBuilder.setTables(TrackMeContract.RouteCheckPointEntry.TABLE_NAME);
                queryBuilder.appendWhere(TrackMeContract.RouteCheckPointEntry._ID + "=" + uri.getPathSegments().get(1));
                sort = sortOrder == null ? DatabaseConstants.DEFAULT_ORDER_COLUMN : sortOrder;
                break;

            default:
                throw new UnsupportedOperationException("Query for data failed!");
        }

        queryCursor = queryBuilder.query(getDb(), projection, selection, selectionArgs, null, null, sort);
        queryCursor.setNotificationUri(getContentResolver(), uri);
        return queryCursor;
    }

    private SQLiteDatabase getDb() {
        return trackMeOpenHelper.getReadableDatabase();
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final UriType match = getUriType(uri);
        Uri returnUri;
        try {
            db.beginTransaction();
            switch (match) {
                case ROUTE: {
                    boolean hasStartTime = values.containsKey(TrackMeContract.RouteEntry.START_TIME);
                    boolean hasStartId = values.containsKey(TrackMeContract.RouteEntry.START_POINT_ID);
                    if (!hasStartTime || !hasStartId) {
                        throw new IllegalArgumentException(TrackMeProviderExceptions.MISSING_IMPORTANT_INFORMATION);
                    }

                    long _id = db.insert(TrackMeContract.RouteEntry.TABLE_NAME, TrackMeContract.RouteEntry._ID, values);
                    if (_id >= 0) {
                        returnUri = TrackMeContract.RouteEntry.buildRouteUri(_id);
                    } else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case ROUTE_POINT: {
                    long _id = db.insert(TrackMeContract.RoutePointEntry.TABLE_NAME, null, values);
                    if (_id > 0) {
                        returnUri = TrackMeContract.RoutePointEntry.buildRoutePointUri(_id);
                    } else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case ROUTE_CHECK_POINT: {
                    long _id = db.insert(TrackMeContract.RouteCheckPointEntry.TABLE_NAME, null, values);
                    if (_id > 0) {
                        returnUri = TrackMeContract.RouteCheckPointEntry.buildRouteCheckPointUri(_id);
                    } else {
                        throw new android.database.SQLException("failed to insert row into: " + uri);
                    }
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        notifyChange(returnUri);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final UriType match = getUriType(uri);
        int rowsDeleted;
        switch (match) {
            case ROUTE: {
                rowsDeleted = db.delete(TrackMeContract.RouteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case ROUTE_POINT: {
                rowsDeleted = db.delete(TrackMeContract.RoutePointEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case ROUTE_CHECK_POINT: {
                rowsDeleted = db.delete(TrackMeContract.RouteCheckPointEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri when trying to delete");
        }
        if (selection == null || rowsDeleted != 0) {
            notifyChange(uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final UriType match = getUriType(uri);
        int rowsUpdated;
        switch (match) {
            case ROUTE: {
                rowsUpdated = db.update(TrackMeContract.RouteEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case ROUTE_ID: {
                String query = TrackMeContract.RouteEntry._ID + "=" + uri.getPathSegments().get(1);
                if (!TextUtils.isEmpty(selection)) {
                    query += " AND (" + selection + " )";
                }
                rowsUpdated = db.update(TrackMeContract.RouteEntry.TABLE_NAME, values, query, selectionArgs);
                break;
            }
            case ROUTE_POINT: {
                rowsUpdated = db.update(TrackMeContract.RoutePointEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case ROUTE_POINT_ID: {
                String query = TrackMeContract.RoutePointEntry._ID + "=" + uri.getPathSegments().get(1);
                if (!TextUtils.isEmpty(selection)) {
                    query += " AND (" + selection + " )";
                }
                rowsUpdated = db.update(TrackMeContract.RoutePointEntry.TABLE_NAME, values, query, selectionArgs);
                break;
            }
            case ROUTE_CHECK_POINT: {
                rowsUpdated = db.update(TrackMeContract.RouteCheckPointEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case ROUTE_CHECK_POINT_ID: {
                String query = TrackMeContract.RouteCheckPointEntry._ID + "=" + uri.getPathSegments().get(1);
                if (!TextUtils.isEmpty(selection)) {
                    query += " AND (" + selection + " )";
                }
                rowsUpdated = db.update(TrackMeContract.RouteCheckPointEntry.TABLE_NAME, values, query, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri when trying to delete");
        }
        if (rowsUpdated != 0) {
            notifyChange(uri);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final UriType match = getUriType(uri);
        switch (match) {
            case ROUTE: {
                db.beginTransaction();
                int count = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TrackMeContract.RouteEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            count++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                notifyChange(uri);
                return count;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    //used for testing more below:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        trackMeOpenHelper.close();
        super.shutdown();
    }

    private void notifyChange(Uri returnUri) {
        getContentResolver().notifyChange(returnUri, null);
    }

    private ContentResolver getContentResolver() {
        if (getContext() != null) {
            return getContext().getContentResolver();
        } else {
            return TrackLibApplication.getInstance().getContentResolver();
        }
    }

    enum UriType {
        ROUTE,
        ROUTE_ID,
        ROUTE_POINT,
        ROUTE_POINT_ID,
        ROUTE_CHECK_POINT,
        ROUTE_CHECK_POINT_ID
    }
}
