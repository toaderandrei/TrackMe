package com.ant.track.application;

import com.ant.lib.application.TrackLibApplication;
import com.ant.track.provider.DataProvider;
import com.ant.track.provider.IDataProvider;

/**
 * Extension of the TrackLibApplication which in turn extends the Android Application.
 */
public class GPSLiveTrackerApplication extends TrackLibApplication {


    public GPSLiveTrackerApplication() {
        TrackLibApplication.instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public IDataProvider getDataProvider() {
        return DataProvider.getInstance();
    }
}
