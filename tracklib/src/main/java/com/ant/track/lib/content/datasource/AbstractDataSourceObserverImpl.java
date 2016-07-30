package com.ant.track.lib.content.datasource;

import android.database.ContentObserver;
import android.os.Handler;

import com.ant.track.lib.content.publisher.RouteDataSourceListener;

/**
 * Data content manager.
 */
public abstract class AbstractDataSourceObserverImpl extends ContentObserver {

    private RouteDataSourceListener listener;

    public AbstractDataSourceObserverImpl(Handler handler, RouteDataSourceListener listener) {
        super(handler);
        this.listener = listener;
    }

    @Override
    public void onChange(boolean selfChange) {
        notifyUpdate();
    }

    /**
     * notify about an an update
     */
    protected abstract void notifyUpdate();

    protected RouteDataSourceListener getListener() {
        return listener;
    }
}
