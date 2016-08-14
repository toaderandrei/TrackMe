package com.ant.track.app.widget;

import android.appwidget.AppWidgetManager;

import com.ant.track.app.widget.AppWidget;

/**
 * Implementation of appwidget
 */
public class AppWidgetImpl implements AppWidget {


    protected static final int HOME_SCREEN_DEFAULT_SIZE = 2;

    @Override
    public int getAppWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId) {
        return HOME_SCREEN_DEFAULT_SIZE;
    }

    @Override
    public void setAppWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId, int size) {
        // Do nothing
    }
}
