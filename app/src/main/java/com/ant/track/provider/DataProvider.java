package com.ant.track.provider;

import android.location.Location;
import android.util.Log;

import com.ant.track.application.GPSLiveTrackerApplication;
import com.ant.track.models.User;
import com.ant.track.models.UserAddress;
import com.ant.track.publisher.ContentPublisher;
import com.ant.track.webclient.HttpClient;
import com.ant.track.webclient.session.SessionResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Toader on 6/3/2015.
 */
public class DataProvider implements IDataProvider, ISessionCreated, ICheckinListener {


    private static final String TAG = DataProvider.class.getSimpleName();
    private static DataProvider instance = null;
    List<User> userList = new ArrayList<>();
    private SessionResponse response;

    public static DataProvider getInstance() {
        if (instance == null) {
            instance = new DataProvider();
        }
        return instance;
    }

    @Override
    public void updateLocation(Location location) {
        //TODO assemble everything required for the server
        if (location == null) {
            Log.wtf(TAG, "it should not be null.");
            return;
        }
        User user = getLastAuthUser();
        UserAddress userAddress = new UserAddress(location, null, null, null, null, null, null);
        user.setUserAddress(userAddress);
        getHttpClient().sendCheckIn(this, user);
        Log.d(TAG, "we got a new location:" + location);
    }

    private HttpClient getHttpClient() {
        return GPSLiveTrackerApplication.getInstance().getHttpClient();
    }

    @Override
    public void createSession() {
        getHttpClient().createSession(this);
    }

    @Override
    public void sessionCreated(SessionResponse _response) {
        User user = new User(_response.getModel().getUser_id(), _response.getModel().getAuth_token(), null);
        if (user != null && user.getUser_id() != null && user.getAuth_token() != null) {
            userList.add(user);
        }
    }

    @Override
    public User getLastAuthUser() {
        if (userList == null || userList.isEmpty()) {
            return null;
        }
        return userList.get(userList.size() - 1);
    }

    @Override
    public void updateCheckin(User user) {
        Log.d(TAG, "updating about a new checkin that was made");
        ContentPublisher.getInstance().notifyListeners(user);
    }
}
