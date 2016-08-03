package com.ant.track.lib.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by toaderandrei on 03/08/16.
 */
public class TimeUtils {

    public static final String LOCAL_FORMAT = "%d min, %d sec";

    public static String getTotalTime(long millis) {
        return String.format(LOCAL_FORMAT,
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }
}
