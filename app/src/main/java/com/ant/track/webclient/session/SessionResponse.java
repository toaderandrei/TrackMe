package com.ant.track.webclient.session;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Toader on 6/4/2015.
 */
public class SessionResponse {

    private static final String STATUS_OK = "ok";


    @SerializedName("status")
    public String status;
    @SerializedName("message")
    public String message;
    public BaseSessionResponse model;

    protected SessionResponse() {

    }

    public String getMessage() {
        return message;
    }

    public boolean isStatusOK() {
        return status != null && status.equals(STATUS_OK);
    }

    public BaseSessionResponse getModel() {
        return model;
    }
}
