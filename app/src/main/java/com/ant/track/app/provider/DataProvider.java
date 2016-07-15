package com.ant.track.app.provider;

import android.location.Location;

import com.ant.track.lib.model.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Toader on 6/3/2015.
 */
public class DataProvider implements IDataProvider {


    private static final String TAG = DataProvider.class.getSimpleName();
    private static DataProvider instance = null;
    List<Route> userList = new ArrayList<>();

    public static DataProvider getInstance() {
        if (instance == null) {
            instance = new DataProvider();
        }
        return instance;
    }

    @Override
    public void updateLocation(Location location) {

    }
}
