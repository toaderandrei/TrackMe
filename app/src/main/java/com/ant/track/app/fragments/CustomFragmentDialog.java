package com.ant.track.app.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

/**
 * A reusable generic dialog fragment class
 */
public class CustomFragmentDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String DIALOG_TITLE = "title";
    private static final String DIALOG_MESSAGE = "message";
    private static final String DIALOG_POSITIVE_BUTTON_TEXT = "positive_button_text";
    private static final String DIALOG_NEGATIVE_BUTTON_TEXT = "negative_button_text";
    private static final String DIALOG_BUNDLE = "bundle";
    private Callback callback;

    /**
     * A callback interface for button events
     */
    public interface Callback {
        /**
         * Used for the onClick event of the positive button
         */
        void onPositiveButtonClicked(Bundle bundle);

        /**
         * Used for the onClick event of the negative button
         */
        void onNegativeButtonClicked(Bundle bundle);
    }

    /**
     * Creates a new instance of the dialog fragment
     *
     * @param title              The title of the message dialog.
     * @param message            The message of the message dialog
     * @param positiveButtonText Text for the positive dialog button.
     * @param negativeButtonText Text for the negative dialog button. Pass null to display only one button.
     * @param callback           Called when the user taps on either of the buttons.
     * @return Returns the instance.
     */
    public static CustomFragmentDialog newInstance(String title, String message, String positiveButtonText, String negativeButtonText, Callback callback) {
        return newInstance(title, message, positiveButtonText, negativeButtonText, callback, null);
    }

    /**
     * Creates a new instance of the dialog fragment
     *
     * @param title              The title of the message dialog.
     * @param message            The message of the message dialog
     * @param positiveButtonText Text for the positive dialog button.
     * @param negativeButtonText Text for the negative dialog button. Pass null to display only one button.
     * @param callback           Called when the user taps on either of the buttons.
     * @param bundle             A bundle that will be returned in the callback
     * @return Returns the instance.
     */
    public static CustomFragmentDialog newInstance(String title, String message, String positiveButtonText, String negativeButtonText, Callback callback, Bundle bundle) {
        final CustomFragmentDialog dialog = new CustomFragmentDialog();
        final Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putString(DIALOG_MESSAGE, message);
        args.putString(DIALOG_POSITIVE_BUTTON_TEXT, positiveButtonText);
        args.putString(DIALOG_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        args.putBundle(DIALOG_BUNDLE, bundle);
        dialog.setArguments(args);
        dialog.callback = callback;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(DIALOG_TITLE);
        String message = getArguments().getString(DIALOG_MESSAGE);
        CharSequence positiveButtonText = getArguments().getString(DIALOG_POSITIVE_BUTTON_TEXT);
        CharSequence negativeButtonText = getArguments().getString(DIALOG_NEGATIVE_BUTTON_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), AlertDialog.THEME_HOLO_LIGHT)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, this);

        if (!TextUtils.isEmpty(negativeButtonText)) {
            builder.setNegativeButton(negativeButtonText, this);
        }

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Callback callback = getCallback();
        if (callback != null) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    callback.onPositiveButtonClicked(getArguments().getBundle(DIALOG_BUNDLE));
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE: {
                    callback.onNegativeButtonClicked(getArguments().getBundle(DIALOG_BUNDLE));
                    break;
                }
            }
        }
        this.dismiss();
    }


    private Callback getCallback() {
        return callback;
    }

}
