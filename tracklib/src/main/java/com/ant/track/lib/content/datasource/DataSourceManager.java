package com.ant.track.lib.content.datasource;

import android.os.Handler;

import com.ant.track.lib.content.publisher.DataContentObserver;
import com.ant.track.lib.content.publisher.RouteDataSourceListener;
import com.ant.track.lib.db.provider.TrackMeContract;

import java.util.EnumSet;
import java.util.Set;

/**
 * DataSource class that registers observers to the content provider, via uri and the
 * types provided.
 */
public class DataSourceManager {

    private DataContentObserver dataObserver;
    private AbstractDataSourceObserverImpl routeSourceManager;

    private AbstractDataSourceObserverImpl routePointSourceManager;

    private AbstractDataSourceObserverImpl routeCheckPointSourceManager;

    private PreferenceSourceObserverImpl preferenceObserver;

    private Set<RouteType> registeredTypes = EnumSet.noneOf(RouteType.class);

    public DataSourceManager(Handler handler, RouteDataSourceListener routeDataSourceListener) {
        routeSourceManager = new RouteDataSourceObserverImpl(handler, routeDataSourceListener);
        routePointSourceManager = new RoutePointDataSourceObserverImpl(handler, routeDataSourceListener);
        routeCheckPointSourceManager = new RouteCheckPointDataSourceObserverImpl(handler, routeDataSourceListener);
        dataObserver = new DataContentObserver();
        preferenceObserver = new PreferenceSourceObserverImpl(routeDataSourceListener);
    }

    public void updateListeners(EnumSet<RouteType> neededListeners) {

        Set<RouteType> listenersToRegisterObserver = EnumSet.copyOf(neededListeners);

        Set<RouteType> copy = EnumSet.copyOf(registeredTypes);
        copy.removeAll(listenersToRegisterObserver);

        listenersToRegisterObserver.removeAll(registeredTypes);
        for (RouteType type : listenersToRegisterObserver) {
            registerObserver(type);
        }

        registeredTypes.clear();
        registeredTypes.addAll(neededListeners);
    }

    public void registerObserver(final RouteType type) {

        switch (type) {
            case ROUTE: {
                dataObserver.registerObserver(TrackMeContract.RouteEntry.CONTENT_URI, routeSourceManager);
                break;
            }
            case ROUTE_POINT: {
                dataObserver.registerObserver(TrackMeContract.RoutePointEntry.CONTENT_URI, routePointSourceManager);
                break;
            }
            case ROUTE_CHECK_POINT: {
                dataObserver.registerObserver(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, routeCheckPointSourceManager);
                break;
            }
            case PREFERENCE: {

                dataObserver.registerOnSharedPreferenceChangeListener(preferenceObserver);
                break;
            }
            default:
                throw new UnsupportedOperationException("type does not exist");
        }
    }

    public void unregisterObserver(final RouteType type) {

        switch (type) {
            case ROUTE: {
                dataObserver.unregisterObserver(routeSourceManager);
                break;
            }
            case ROUTE_POINT: {
                dataObserver.unregisterObserver(routePointSourceManager);
                break;
            }
            case ROUTE_CHECK_POINT: {
                dataObserver.unregisterObserver(routeCheckPointSourceManager);
                break;
            }
            case PREFERENCE: {
                dataObserver.unregisterOnSharedPreferenceChangeListener(preferenceObserver);
                break;
            }
            default:
                throw new UnsupportedOperationException("type does not exist");
        }
    }

    public void clear() {
        for (RouteType type : RouteType.values()) {
            unregisterObserver(type);
        }
        routeSourceManager = null;
        routeCheckPointSourceManager = null;
        routePointSourceManager = null;
        preferenceObserver = null;
    }
}
