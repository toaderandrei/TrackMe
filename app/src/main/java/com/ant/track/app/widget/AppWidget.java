package com.ant.track.app.widget;

import android.appwidget.AppWidgetManager;

/**
 * base class for Appwidget.
 */
public interface AppWidget {

    /**
     * Gets the app widget size.
     * <p>
     * Due to changes in API level 16.
     *
     * @param appWidgetManager the app widget manager
     * @param appWidgetId      the app widget id
     */
    int getAppWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId);

    /**
     * Sets the app widget size.
     * <p>
     * Due to changes in API level 16.
     *
     * @param appWidgetManager the app widget manager.
     * @param appWidgetId      the app widgit id
     * @param size             the size
     */
    void setAppWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId, int size);
}
