package com.ant.track.app.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.ant.track.app.R;
import com.ant.track.lib.prefs.PreferenceUtils;

/**
 * Activity widget.
 */
public class RouteWidgetActivity extends Activity {

    private int appWidgetId;
    private TextView item1;
    private TextView item2;
    private TextView item3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    /*
     * Set the result to CANCELED. This will cause the widget host to cancel out
     * of the widget placement if they press the back button.
     */
        setResult(RESULT_CANCELED);

        setContentView(R.layout.layout_widget);
        item1 = (TextView) findViewById(R.id.widget_config_item1);
        item2 = (TextView) findViewById(R.id.widget_config_item2);
        item3 = (TextView) findViewById(R.id.widget_config_item3);


        setItem(item1, R.string.stats_distance);
        setItem(item2, R.string.stats_total_time);
        setItem(item3, R.string.stats_moving_time);


        findViewById(R.id.track_widget_config_add).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Push widget update to surface with newly set prefix
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(RouteWidgetActivity.this);

                RouteWidgetProvider.updateAppWidget(RouteWidgetActivity.this, appWidgetManager, appWidgetId, -1L);
                PreferenceUtils.setInt(RouteWidgetActivity.this, R.string.widget_item1, R.string.stats_distance);
                PreferenceUtils.setInt(RouteWidgetActivity.this, R.string.widget_item2, R.string.stats_total_time);
                PreferenceUtils.setInt(RouteWidgetActivity.this, R.string.widget_item3, R.string.stats_moving_time);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        findViewById(R.id.widget_config_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Find the app widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        appWidgetId = extras != null ? extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                : AppWidgetManager.INVALID_APPWIDGET_ID;

        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    @NonNull
    private int getValueAsInt(String value) {
        return Integer.parseInt(value);
    }

    private void setItem(TextView view, int id) {
        if (view == null) {
            throw new IllegalArgumentException("view cannot be null");
        }
        if (id < 0) {
            throw new IllegalArgumentException("id cannoe be less than 0");
        }

        view.setText(getString(id));
    }
}
