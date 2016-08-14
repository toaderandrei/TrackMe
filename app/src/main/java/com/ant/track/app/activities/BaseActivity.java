package com.ant.track.app.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ant.track.app.R;
import com.ant.track.app.fragments.LocationFragment;
import com.ant.track.app.helper.ResourceHelper;
import com.ant.track.lib.constants.Constants;

/**
 * This is in charge of initializing the fragments and also the toolbars.
 */
public abstract class BaseActivity extends AppCompatActivity implements RecordStateListener {

    protected LocationFragment mMapFragment;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private boolean mToolbarInitialized;

    private int itemToOpenWhenDrawerCloses = -1;

    private FragmentManager.OnBackStackChangedListener mBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            updateDrawerToggle();
        }
    };

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerClosed(View drawerView) {
            if (mDrawerToggle != null) {
                mDrawerToggle.onDrawerClosed(drawerView);
            }
            if (itemToOpenWhenDrawerCloses >= 0) {
                Bundle extras = ActivityOptionsCompat.makeCustomAnimation(BaseActivity.this, R.anim.fade_in, R.anim.fade_out).toBundle();

                Class activityClass;
                switch (itemToOpenWhenDrawerCloses) {
                    case R.id.navigation_map:
                        activityClass = MainActivity.class;
                        break;
                    case R.id.navigation_list:
                        activityClass = RouteDetailsActivity.class;
                        break;
                    default:
                        activityClass = MainActivity.class;
                        break;
                }

                Intent mIntent = new Intent(BaseActivity.this, activityClass);
                if (activityClass.equals(RouteDetailsActivity.class)) {
                    extras.putBoolean(Constants.EXTRA_VIEW_ROUTE_DETAILS, false);
                }
                mIntent.putExtras(extras);
                itemToOpenWhenDrawerCloses = -1;
                startActivity(mIntent);
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerStateChanged(newState);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if (mDrawerToggle != null) {
                mDrawerToggle.onDrawerOpened(drawerView);
            }
            setActionBarTitle();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initGoogleMapsIfNotAlready() {
        if (mMapFragment == null) {
            mMapFragment = LocationFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, mMapFragment);
            ft.commit();
        }
    }

    protected void initGoogleMaps() {
        if (mMapFragment == null) {
            mMapFragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        }
        if (mMapFragment == null) {
            initGoogleMapsIfNotAlready();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initGoogleMaps();
        if (!mToolbarInitialized) {
            throw new IllegalStateException("You must run super.initializeToolbar at " +
                    "the end of your onCreate method");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Whenever the fragment back stack changes, we may need to update the
        // action bar toggle: only top level screens show the hamburger-like icon, inner
        // screens - either Activities or fragments - show the "Up" icon instead.
        getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // If not handled by drawerToggle, home needs to be handled by returning to previous
        if (item != null && item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the drawer is open, back will close it
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        // Otherwise, it may return to the previous fragment stack
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            // Lastly, it will rely on the system behavior for back
            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mToolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mToolbar.setTitle(titleId);
    }

    protected void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id " +
                    "'toolbar'");
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (mDrawerLayout != null) {
            NavigationView navigationView = (NavigationView) this.findViewById(R.id.nav_view);
            if (navigationView == null) {
                throw new IllegalStateException("A layout with a drawerLayout is required to" +
                        "include a ListView with id 'drawerList'");
            }

            // Create an ActionBarDrawerToggle that will handle opening/closing of the drawer:
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    mToolbar, R.string.open_content_drawer, R.string.close_content_drawer);
            mDrawerLayout.addDrawerListener(mDrawerListener);
            mDrawerLayout.setStatusBarBackgroundColor(ResourceHelper.getThemeColor(this, R.attr.colorPrimary, android.R.color.black));
            populateDrawerItems(navigationView);
            setSupportActionBar(mToolbar);
            updateDrawerToggle();
        } else {
            setSupportActionBar(mToolbar);
        }

        mToolbarInitialized = true;
    }

    private void populateDrawerItems(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        itemToOpenWhenDrawerCloses = menuItem.getItemId();
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
        if (MainActivity.class.isAssignableFrom(getClass())) {
            navigationView.setCheckedItem(R.id.navigation_map);
        } else if (RouteDetailsActivity.class.isAssignableFrom(getClass())) {
            navigationView.setCheckedItem(R.id.navigation_list);
        }
    }


    protected void updateDrawerToggle() {
        if (mDrawerToggle == null) {
            return;
        }
        boolean isRoot = getFragmentManager().getBackStackEntryCount() == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if (isRoot) {
            mDrawerToggle.syncState();
        }
    }

    protected void setActionBarTitle() {
        getSupportActionBar().setTitle(R.string.app_name);
    }
}
