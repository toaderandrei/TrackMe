package com.ant.track.webclient;

import android.location.Location;
import android.os.Build;

import com.ant.track.constants.Constants;
import com.ant.track.helper.SystemUtils;
import com.ant.track.models.User;
import com.ant.track.models.UserAddress;
import com.ant.track.provider.ICheckinListener;
import com.ant.track.webclient.checkin.BaseCheckin;
import com.ant.track.webclient.checkin.CheckinPostResponse;
import com.ant.track.webclient.session.SessionRequest;

/**
 * Created by Toader on 6/4/2015.
 */
public class ContentTranslator {

    public static SessionRequest getSessionRequest() {
        SessionRequest request = new SessionRequest(Constants.EMAIL, Constants.PASSWORD);
        request.setDeviceId(SystemUtils.getDeviceId());
        request.setOs(Constants.OS);
        request.setAuto(false);
        request.setLanguage(Constants.LANGUAGE);
        int sdkVersion = Build.VERSION.SDK_INT;
        request.setOsVersion("" + sdkVersion);
        request.setRegion(Constants.REGION);
        request.setApnToken(Constants.APN_TOKEN);
        request.setAppVersion(Constants.APP_VERSION);
        return request;
    }

    public static BaseCheckin getCheckinForUserToServer(User user) {
        if (user == null || user.getUser_id() == null || user.getAuth_token() == null) {
            return null;
        }
        BaseCheckin checkin = new BaseCheckin();
        checkin.setUser_id(user.getUser_id());
        if (user.getUserAddress().getLocation() != null) {
            Location loc = user.getUserAddress().getLocation();
            checkin.setLatitude((float) loc.getLatitude());

            checkin.setLongitude((float) loc.getLongitude());

            checkin.setAccuracy((int) loc.getAccuracy());

            checkin.setSpeed(loc.getSpeed());

            checkin.setCourse(loc.getBearing());
        }
        checkin.setAnnotation("running late");
        checkin.setPoi("123456789");

        checkin.setWifi(SystemUtils.getMacAddress());
        checkin.setOptimized(true);
        return checkin;
    }

    public static void translateResponse(ICheckinListener listener, User user, CheckinPostResponse response) {
        Location serverLoc = new Location("");
        if (response.getModel().getLatitude() != -1.0f) {
            serverLoc.setLatitude(Double.valueOf(response.getModel().getLatitude()));
        }

        if (response.getModel().getLongitude() != -1.0f) {
            serverLoc.setLongitude(Double.valueOf(response.getModel().getLongitude()));
        }

        if (response.getModel().getAccuracy() != -1.0f) {
            serverLoc.setAccuracy(Float.valueOf(response.getModel().getAccuracy()));
        }

        if (response.getModel().getSpeed() != 0) {
            serverLoc.setSpeed(Float.valueOf(response.getModel().getSpeed()));
        }
        if (response.getModel().getCourse() != -1.0f) {
            serverLoc.setBearing(Float.valueOf(response.getModel().getCourse()));
        }

        UserAddress userAddress = new UserAddress(serverLoc, response.getModel().getTitle(), response.getModel().getAddress(), response.getModel().getAdress_medium(), response.getModel().getZona_id(), response.getModel().getDate(), response.getModel().getPoi());
        user.setUserAddress(userAddress);
        listener.updateCheckin(user);
    }
}
