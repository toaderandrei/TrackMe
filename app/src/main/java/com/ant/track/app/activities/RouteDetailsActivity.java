package com.ant.track.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ant.track.app.R;
import com.ant.track.app.fragments.RouteDetailsFragment;
import com.ant.track.app.fragments.RouteListFragment;
import com.ant.track.lib.constants.Constants;

/**
 * Activity for showing details of routes.
 */
public class RouteDetailsActivity extends AppCompatActivity implements RouteDetailsFragment.Callback, RouteListFragment.Callback {

    private static final String TAG = RouteDetailsActivity.class.getCanonicalName();
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.route_details_view);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                boolean viewDetails = extras.getBoolean(Constants.EXTRA_VIEW_ROUTE_DETAILS, true);
                if (viewDetails) {
                    long routeid = extras.getLong(Constants.EXTRA_ROUTE_ID_KEY);
                    if (routeid != -1L) {
                        showRouteDetails(routeid);
                    }
                } else {
                    //we see all routes
                    showRouteListFragment();
                }
            }
        }
    }

    private void showRouteDetails(long routeid) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment == null || (fragment instanceof RouteListFragment)) {
            RouteDetailsFragment routeDetailsFragment = RouteDetailsFragment.newInstance(routeid);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, routeDetailsFragment, TAG);
            transaction.commit();
        }
    }

    @Override
    public void onSaved() {
        showRouteListFragment();
    }

    private void showRouteListFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment == null || (fragment instanceof RouteDetailsFragment)) {
            RouteListFragment routeDetailsFragment = RouteListFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, routeDetailsFragment, TAG);
            transaction.commit();
        }
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void onViewDetails(long routeid) {
        showRouteDetails(routeid);
    }
}
