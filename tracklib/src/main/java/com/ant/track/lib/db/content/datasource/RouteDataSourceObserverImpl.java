package com.ant.track.lib.db.content.datasource;

import android.os.Handler;

import com.ant.track.lib.db.content.publisher.RouteDataSourceListener;

/**
 * Implementation of the DataSourceManager for the route.
 */
public class RouteDataSourceObserverImpl extends AbstractDataSourceObserverImpl {

    public RouteDataSourceObserverImpl(Handler handler, RouteDataSourceListener routeDataListener) {
        super(handler, routeDataListener);
    }

    @Override
    protected void notifyUpdate() {
        getListener().notifyRouteUpdate();
    }
}
