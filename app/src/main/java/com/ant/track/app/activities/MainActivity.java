package com.ant.track.app.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ant.track.app.R;
import com.google.android.gms.maps.GoogleMap;

/**
 * This is the main activity that it is started every time and it is in charge of showing distinct UI stuff, menus, and also here we initialize the content view.
 */
public class MainActivity extends ServiceConnectActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeToolbar();
        initRecordingFragment();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setActionBarTitle();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (getGoogleMap() != null) {
            int id;
            switch (getGoogleMap().getMapType()) {
                case GoogleMap.MAP_TYPE_NORMAL:
                    id = R.id.normal_view;
                    break;
                case GoogleMap.MAP_TYPE_SATELLITE:
                    id = R.id.satellite_view;
                    break;
                default:
                    id = R.id.normal_view;
                    break;
            }
            MenuItem item = menu.findItem(id);
            if (item != null) {
                item.setChecked(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int type;
        if (this.getGoogleMap() != null) {
            type = this.getGoogleMap().getMapType();
        }

        if (item.getItemId() == R.id.normal_view) {
            type = GoogleMap.MAP_TYPE_NORMAL;
        } else if (item.getItemId() == R.id.satellite_view) {
            type = GoogleMap.MAP_TYPE_SATELLITE;
        } else {
            return super.onOptionsItemSelected(item);
        }
        if (getGoogleMap() != null) {
            this.getGoogleMap().setMapType(type);
            item.setChecked(true);
        }
        return true;
    }

    protected GoogleMap getGoogleMap() {
        if (mMapFragment != null) {
            return mMapFragment.getGoogleMap();
        }
        return null;
    }

}
