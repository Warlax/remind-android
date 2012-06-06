package com.niznash.remind;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Dialog fragment that builds "are you sure?" type dialogs with a custom title.
 *
 * <p>The message is always "Are you sure?".
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class ConfirmDialogFragment extends DialogFragment {

    /**
     * Defines interface for a listener to be notified when the user confirms or rejects the dialog.
     */
    public interface OnConfirmDialogResultListener {
        /**
         * Notifies that the user confirmed the prompt.
         *
         * @param dialogId The ID of the dialog for which this is being called
         */
        void onConfirmDialogPositive(int dialogId);

        /**
         * Notifies that the user rejected the prompt.
         *
         * @param dialogId The ID of the dialog for which this is being called
         */
        void onConfirmDialogNegative(int dialogId);
    }

    /**
     * Shows a confirmation dialog.
     *
     * @param fm The {@link FragmentManager}, you can get it using
     *     {@code Activity#getSupportFragmentManager()}
     * @param listener The {@link OnConfirmDialogResultListener} to be notified when the user hits
     *     "OK" or "Cancel".
     * @param dialogId The ID of the dialog to show, this is used in the callbacks of
     *     {@link OnConfirmDialogResultListener} to identify the dialog.
     * @param title The title to use for the dialog
     */
    public static void show(FragmentManager fm, OnConfirmDialogResultListener listener,
            int dialogId, String title) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ConfirmDialogFragment dialog = new ConfirmDialogFragment(listener, dialogId, title);
        dialog.show(ft, "dialog");
    }

    private final OnConfirmDialogResultListener mListener;
    private final int mDialogId;
    private final String mTitle;

    private ConfirmDialogFragment(OnConfirmDialogResultListener listener, int dialogId,
            String title) {
        mListener = listener;
        mDialogId = dialogId;
        mTitle = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle(mTitle)
            .setMessage(R.string.message_confirm_message)
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onConfirmDialogPositive(mDialogId);
                }
            })
            .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onConfirmDialogNegative(mDialogId);
                }
            })
            .create();
    }
}
