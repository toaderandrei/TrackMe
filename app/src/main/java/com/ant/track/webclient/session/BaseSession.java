package com.ant.track.webclient.session;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Toader on 6/4/2015.
 */
public class BaseSession {

    @SerializedName("email")
    public String email;

    @SerializedName("apn_token")
    public String apn_token;

    @SerializedName("gcm_rid")
    public String gcm_rid;

    @SerializedName("device")
    public String device;

    @SerializedName("device_id")
    public String device_id;

    @SerializedName("region")
    public String region;

    @SerializedName("os")
    public String os;

    @SerializedName("os_version")
    public String os_version;

    @SerializedName("app_version")
    public String app_version;

    @SerializedName("auto")
    public boolean auto;

    @SerializedName("language")
    public String language;


    public String getApnToken() {
        return apn_token;
    }

    public String getGcmRid() {
        return gcm_rid;
    }

    public String getRegion() {
        return region;
    }

    public String getOs() {
        return os;
    }

    public String getOsVersion() {
        return os_version;
    }

    public String getDeviceId() {
        return device_id;
    }

    public boolean isAuto() {
        return auto;
    }

    public String getAppVersion() {
        return app_version;
    }

    public String getEmail() {
        return email;
    }

    public String getLanguage() {
        return language;
    }

    public String getDevice() {
        return device;
    }

}
