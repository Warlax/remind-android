package com.niznash.remind.content;

import android.content.Context;
import android.content.Intent;

import com.niznash.remind.ReminderActivity;

/**
 * Defines {@link Intent}s that are used across the app.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class Intents {
    /**
     * Place-holder for when an extra that's normally a Long or an Integer has no value or was not
     * set.
     */
    public static final int NO_VALUE = -1;

    // Extras
    public static final String EXTRA_OP = "op";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_ID = "id";

    /**
     * Builds an {@link Intent} that, when fired, starts {@link ReminderActivity} in the "create
     * new" mode.
     *
     * @param context The context
     * @return The {@code Intent}
     */
    public static Intent newCreateReminderIntent(Context context) {
        return new Intent(context, ReminderActivity.class);
    }

    /**
     * Builds an {@link Intent} that, when fired, starts {@link ReminderActivity} in the "edit
     * existing reminder" mode.
     *
     * @param context The context
     * @param reminderId The ID of the reminder to edit
     * @return The {@code Intent}
     */
    public static Intent newEditReminderIntent(Context context, long reminderId) {
        Intent intent = new Intent(context, ReminderActivity.class);
        intent.putExtra(Intents.EXTRA_ID, reminderId);
        return intent;
    }
}
