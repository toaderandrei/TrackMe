package com.ant.track.app.helper;

import android.content.Context;
import android.content.Intent;

/**
 * Utility class for starting intents.
 */
public class IntentUtils {
    public static final String MAIN_ACTIVITY_ACTION = "com.ant.lib.activity";

    /**
     * Creates an intent with {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} and
     * {@link Intent#FLAG_ACTIVITY_NEW_TASK}.
     *
     * @param context the context
     * @param cls     the class
     */
    public static final Intent newIntent(Context context, Class<?> cls) {
        return new Intent(context, cls).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static final Intent newIntentWithAction(String action) {
        Intent intent = new Intent(action);
        return intent;
    }
}
