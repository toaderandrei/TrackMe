package com.ant.track.webclient.session;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Toader on 6/4/2015.
 */
public class BaseSessionResponse extends BaseSession {

    @SerializedName("_id")
    public String _id;

    @SerializedName("started")
    public String started;

    @SerializedName("auth_token")
    public String auth_token;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("family_id")
    public String family_id;

    public String get_id() {
        return _id;
    }

    public String getStarted() {
        return started;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getFamily_id() {
        return family_id;
    }

    public String getAuth_token() {
        return auth_token;
    }
}
