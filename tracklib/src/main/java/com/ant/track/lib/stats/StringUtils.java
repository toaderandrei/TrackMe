package com.ant.track.lib.stats;

import android.content.Context;
import android.text.TextUtils;

import com.ant.track.lib.R;

import java.util.Locale;

/**
 * Utility class for strings.
 */
public class StringUtils {

    /**
     * Formats the elapsed time in the form "H:MM:SS".
     *
     * @param time the time in milliseconds
     */
    public static String formatElapsedTimeWithHour(long time) {
        String value = formatElapsedTime(time);
        return TextUtils.split(value, ":").length == 2 ? "0:" + value : value;
    }

    /**
     * Formats the elapsed timed in the form "MM:SS" or "H:MM:SS".
     *
     * @param time the time in milliseconds
     */
    public static String formatElapsedTime(long time) {
        //todo format time.
        return null;
    }


    public static String[] getDistanceParts(Context context, double distance) {
        String[] result = new String[2];
        if (Double.isNaN(distance) || Double.isInfinite(distance)) {
            result[0] = null;
            result[1] = context.getString(R.string.unit_meter);
            return result;
        }

        int unitId = R.string.unit_meter;

        result[0] = formatDecimal(distance);
        result[1] = context.getString(unitId);
        return result;
    }

    public static String formatDecimal(double value) {
        return formatDecimal(value, 2);
    }

    private static String formatDecimal(double value, int precision) {
        String result = String.format(Locale.getDefault(), "%1$,." + precision + "f", value);
        return result.replaceAll("[0]*$", "").replaceAll("\\.$", "");
    }

    public static String[] getSpeedParts(Context context, Double speed) {
        String[] result = new String[2];
        int unitId = R.string.unit_kilometer_per_hour;
        result[1] = context.getString(unitId);
        if (Double.isNaN(speed) || Double.isInfinite(speed)) {
            result[0] = null;
            return result;
        }
        speed *= UnitConversions.MS_TO_KMH;

        result[0] = StringUtils.formatDecimal(speed);

        return result;
    }
}
