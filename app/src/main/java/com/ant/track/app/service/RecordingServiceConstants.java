package com.ant.track.app.service;

/**
 * Constants used for communicating from Service to listeners
 * and from listeners to Service.
 */
public class RecordingServiceConstants {

    public static final int MSG_REGISTER_CLIENT = 100;

    public static final int MSG_START_TRACKING = 101;
    public static final int MSG_STOP_TRACKING = 102;

    public static final int MSG_PAUSE_TRACKING = 103;

    public static final int MSG_RESUME_TRACKING = 105;

    public static final int MSG_UPDATE_ROUTE_ID = 200;

    public static final int MSG_NOT_ALLOWED = 300;

    public static final int MSG_STOP_NOTIFICATIONS = 400;

    public static final int MSG_SHOW_NOTIFICATIONS = 401;
    public static final int MSG_CLIENT_CONNECTED = 500;
    public static final int MSG_EXCEPTION = 600;
    public static final int MSG_CLIENT_DISCONNECTED = 501;
}
