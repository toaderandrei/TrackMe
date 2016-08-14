package com.ant.track.app.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;

/**
 * AppWidget for api16
 */
@TargetApi(16)
public class AppWidgetImplApi16 extends AppWidgetImpl {

    private static final String APP_WIDGET_SIZE_KEY = "app_widget_size_key";

    @Override
    public int getAppWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle bundle = appWidgetManager.getAppWidgetOptions(appWidgetId);
        return bundle.getInt(APP_WIDGET_SIZE_KEY, getAppWidgetSizeDefault(bundle));
    }

    @SuppressWarnings("unused")
    protected int getAppWidgetSizeDefault(Bundle bundle) {
        return HOME_SCREEN_DEFAULT_SIZE;
    }

    @Override
    public void setAppWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId, int size) {
        Bundle bundle = new Bundle();
        bundle.putInt(APP_WIDGET_SIZE_KEY, size);
        appWidgetManager.updateAppWidgetOptions(appWidgetId, bundle);
    }
}
