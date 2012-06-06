package com.niznash.remind;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Fragment to pick a time.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class TimePickerDialogFragment extends DialogFragment {

    /**
     * Shows the "time picker" dialog.
     *
     * @param fm The {@link FragmentManager}
     * @param listener The listener to notify of the user's choice
     * @param hours The initial value for hours (in 24 hour format)
     * @param minutes The initial value for the minutes
     */
    public static void show(FragmentManager fm, TimePickerDialog.OnTimeSetListener listener,
            int hours, int minutes) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        TimePickerDialogFragment dialog = new TimePickerDialogFragment(listener, hours, minutes);
        dialog.show(ft, "dialog");
    }

    private final TimePickerDialog.OnTimeSetListener mListener;
    private final int mHours;
    private final int mMinutes;

    /**
     * Constructor.
     *
     * @param listener The listener
     * @param hours The initial value for hours
     * @param minutes The initial value for minutes
     */
    private TimePickerDialogFragment(TimePickerDialog.OnTimeSetListener listener, int hours,
            int minutes) {
        mListener = listener;
        mHours = hours;
        mMinutes = minutes;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), mListener, mHours, mMinutes, false);
    }
}
