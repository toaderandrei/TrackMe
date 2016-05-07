package com.ant.track.provider;

import android.location.Location;
import android.util.Log;

import com.ant.track.application.GPSLiveTrackerApplication;
import com.ant.track.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Toader on 6/3/2015.
 */
public class DataProvider implements IDataProvider {


    private static final String TAG = DataProvider.class.getSimpleName();
    private static DataProvider instance = null;
    List<User> userList = new ArrayList<>();

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
