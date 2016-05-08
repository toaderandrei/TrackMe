package com.ant.track.service;

/**
 * Constants used for communicating from Service to listeners
 * and from listeners to Service.
 */
public class RecordingServiceConstants {

    public static final int MSG_SERVICE_CALL = 100;

    public static final int MSG_START_TRACKING = 101;
    public static final int MSG_END_TRACKING = 102;

    public static final int MSG_PAUSE_TRACKING = 103;

    public static final int MSG_STOP_SERVICE = 104;

    public static final int MSG_RESUME_TRACKING = 105;

    public static final int MSG_UPDATE_LOCATION = 200;

    public static final int MSG_NOT_ALLOWED = 300;
}
