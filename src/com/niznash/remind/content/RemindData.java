package com.niznash.remind.content;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;

import com.niznash.remind.content.RemindProvider.ReminderColumns;

/**
 * Helps access reminder data.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class RemindData {

    /**
     * If called on the UI/main thread, throws a runtime exception
     */
    private static void failOnMainThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new IllegalStateException("Cannot call this method from the main thread!");
        }
    }

    /**
     * Creates a reminder in the database.
     *
     * <p>Never call on the UI thread.
     *
     * @param context The context
     * @param title The title of the reminder to create
     * @param time The time to schedule the new reminder for
     * @return  The ID of the newly created reminder
     */
    public static long createReminder(Context context, String title, long time) {
        failOnMainThread();

        ContentValues values = new ContentValues();
        values.put(ReminderColumns.TITLE, title);
        values.put(ReminderColumns.TIME, time);
        return ContentUris.parseId(context.getContentResolver().insert(
                RemindProvider.getContentUri(RemindProvider.REMINDER_TABLE), values));
    }

    /**
     * Gets all reminders from the database.
     *
     * <p>Never call on the UI thread.
     *
     * @param context The context
     * @param projection The column names to retrieve
     * @param selection The SQL "WHERE" argument, without the "WHERE"
     * @param selectionArgs The arguments for the given selection
     * @return A {@link Cursor} with the queried rows loaded, if any
     */
    public static Cursor queryReminders(Context context, String[] projection, String selection,
            String[] selectionArgs) {
        failOnMainThread();

        return context.getContentResolver().query(
                RemindProvider.getContentUri(RemindProvider.REMINDER_TABLE), projection, selection,
                selectionArgs, null);
    }

    /**
     * Queries a single reminder from the database.
     *
     * <p>Never call on the UI thread.
     *
     * @param context The context
     * @param id The ID of the reminder to retrieve
     * @param projection The column names to retrieve
     * @return A {@link Cursor} with the queried row loaded, if found
     */
    public static Cursor queryReminder(Context context, long id, String[] projection) {
        failOnMainThread();

        return context.getContentResolver().query(ContentUris.withAppendedId(
                RemindProvider.getContentUri(RemindProvider.REMINDER_TABLE), id), projection, null,
                null, null);
    }

    /**
     * Updates an existing reminder in the database.
     *
     * <p>Never call on the UI thread.
     *
     * @param context The context
     * @param id The ID of the reminder to update
     * @param title The new title of the reminder
     * @param time The new time to schedule the reminder for
     */
    public static void updateReminder(Context context, long id, String title, long time) {
        failOnMainThread();

        ContentValues values = new ContentValues();
        values.put(ReminderColumns.TITLE, title);
        values.put(ReminderColumns.TIME, time);
        context.getContentResolver().update(ContentUris.withAppendedId(
                RemindProvider.getContentUri(RemindProvider.REMINDER_TABLE), id), values, null,
                null);
    }

    /**
     * Deletes an existing reminder from the database.
     *
     * <p>Never call on the UI thread.
     *
     * @param context The context
     * @param id The ID of the reminder to delete
     */
    public static void deleteReminder(Context context, long id) {
        failOnMainThread();

        context.getContentResolver().delete(ContentUris.withAppendedId(
                RemindProvider.getContentUri(RemindProvider.REMINDER_TABLE), id), null, null);
    }
}
