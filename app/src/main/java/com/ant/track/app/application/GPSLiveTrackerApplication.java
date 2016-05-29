package com.ant.track.app.application;

import com.ant.track.app.provider.DataProvider;
import com.ant.track.app.provider.IDataProvider;
import com.ant.track.lib.application.TrackLibApplication;

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
