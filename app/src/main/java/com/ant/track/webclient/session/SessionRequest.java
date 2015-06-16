package com.ant.track.webclient.session;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Toader on 6/4/2015.
 */
public class SessionRequest extends BaseSession {


    @SerializedName("password")
    public String password;

    protected SessionRequest() {

    }

    public SessionRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void setDeviceId(String deviceId) {
        this.device_id = deviceId;
    }

    public void setLanguage(String lang) {
        this.language = lang;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAppVersion(String _app_version) {
        this.app_version = _app_version;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setOsVersion(String osVersion) {
        this.os_version = osVersion;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public void setApnToken(String apnToken) {
        this.apn_token = apnToken;
    }

    public void setGcmRid(String gcmRid) {
        this.gcm_rid = gcmRid;
    }

}
