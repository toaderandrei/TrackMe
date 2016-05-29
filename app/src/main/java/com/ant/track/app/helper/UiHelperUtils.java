package com.ant.track.app.helper;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by andrei on 08/05/16.
 */
public class UiHelperUtils {

    public static void showErrToast(Context context, String errMessage) {
        Toast.makeText(context, errMessage, Toast.LENGTH_SHORT).show();

    }
}
