package com.niznash.remind.content;

import com.niznash.remind.content.RemindProvider.ReminderColumns;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

/**
 * Loads reminders using a {@link Cursor}.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class ReminderLoader extends CursorLoader {

    private final long mReminderId;

    /**
     * Constructor.
     *
     * <p>Loads all reminders using {@link ReminderColumns#PROJECTION}.
     *
     * @param context The context
     */
    public ReminderLoader(Context context) {
        this(context, Intents.NO_VALUE);
    }

    /**
     * Constructor.
     *
     * <p>Loads a single reminder using {@link ReminderColumns#PROJECTION}.
     *
     * @param context The context
     * @param reminderId The ID of the reminder to load
     */
    public ReminderLoader(Context context, long reminderId) {
        super(context);
        mReminderId = reminderId;
    }

    @Override
    public Cursor loadInBackground() {
        String selection = mReminderId != Intents.NO_VALUE ? ReminderColumns._ID + "=?" : null;
        String[] args =
            mReminderId != Intents.NO_VALUE ? new String[] { Long.toString(mReminderId) } : null;
        return RemindData.queryReminders(getContext(), ReminderColumns.PROJECTION, selection, args);
    }
}
