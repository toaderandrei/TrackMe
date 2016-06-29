package com.ant.track.app.service;

/**
 * Recording state
 * */
public enum RecordingState {

    STARTED("started"),

    STOPPED("stopped"),

    PAUSED("paused"),

    RESUMED("resumed");

    private String state;

    private RecordingState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
