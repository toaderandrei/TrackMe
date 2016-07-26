package com.ant.track.lib.db.content.datasource;

import android.os.Handler;

import com.ant.track.lib.db.content.publisher.RouteDataSourceListener;

/**
 * RouteCheckPoint data source manager.
 */
public class RouteCheckPointDataSourceObserverImpl extends AbstractDataSourceObserverImpl {

    public RouteCheckPointDataSourceObserverImpl(Handler handler, RouteDataSourceListener routeDataListener) {
        super(handler, routeDataListener);
    }

    @Override
    protected void notifyUpdate() {
        getListener().notifyRouteCheckPointUpdate();
    }
}
