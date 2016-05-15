package com.ant.lib.db.tables;

import com.ant.lib.db.TrackMeContract;
import com.ant.lib.db.columns.GenericColumn;
import com.ant.lib.db.columns.GenericColumnImpl;
import com.ant.lib.db.type.SqliteDataType;

/**
 * Created by toaderandrei on 15/05/16.
 */
public enum DatabaseTable {

    ROUTE(new GenericColumnImpl(true, TrackMeContract.RouteEntry._ID, SqliteDataType.String),
            new GenericColumnImpl(false, true, TrackMeContract.RouteEntry.TOTAL_TIME, SqliteDataType.Float),
            new GenericColumnImpl(TrackMeContract.RouteEntry.NAME, SqliteDataType.String),
            new GenericColumnImpl(TrackMeContract.RouteEntry.DESCRIPTION, SqliteDataType.String),
            new GenericColumnImpl(TrackMeContract.RouteEntry.START_POINT_ID, SqliteDataType.Integer),
            new GenericColumnImpl(TrackMeContract.RouteEntry.AVG_SPEED, SqliteDataType.Float),
            new GenericColumnImpl(TrackMeContract.RouteEntry.MAX_SPEED, SqliteDataType.Float),
            new GenericColumnImpl(TrackMeContract.RouteEntry.STOP_POINT_ID, SqliteDataType.Integer),
            new GenericColumnImpl(TrackMeContract.RouteEntry.TOTAL_DISTANCE, SqliteDataType.Float)
            ),

    ROUTE_POINT(new GenericColumnImpl(true, TrackMeContract.RoutePointEntry._ID, SqliteDataType.String),
            new GenericColumnImpl(false, true, TrackMeContract.RoutePointEntry.ROUTE_ID, SqliteDataType.String),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.START_ROUTE_ID, SqliteDataType.Integer),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.STOP_ROUTE_ID, SqliteDataType.Integer),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.DESCRIPTION, SqliteDataType.String),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.NAME, SqliteDataType.String),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.TIME, SqliteDataType.Integer),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.MAX_SPEED, SqliteDataType.Float),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.DURATION, SqliteDataType.Float),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.LOCATION_ALT, SqliteDataType.Float),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.LOCATION_ACCURACY, SqliteDataType.Float),
            new GenericColumnImpl(TrackMeContract.RoutePointEntry.LOCATION_LAT, SqliteDataType.Float));

    private GenericColumn[] genericColumns;

    private DatabaseTable(GenericColumn... columns) {
        this.genericColumns = columns;
    }

    public GenericColumn[] getColumns() {
        return genericColumns;
    }

    public String getName() {
        return this.name();
    }
}
