package com.niznash.remind.content;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Content provider for the application.
 *
 * <ul>
 * <li>{@code com.niznash.reminder/reminder} gets all reminders.
 * <li>{@code com.niznash.reminder/reminder/#}, where {@code #} is the reminder ID, gets the reminder
 *     with that ID.
 * </ul>
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class RemindProvider extends ContentProvider {

    static final String AUTHORITY = "com.niznash.remind";
    static final String REMINDER_TABLE = "reminder";

    /**
     * Defines the columns of the Reminder table.
     */
    public static class ReminderColumns {
        /**
         * Defines the typical projection to use when loading reminders.
         */
        public static final String[] PROJECTION = new String[] {
            ReminderColumns._ID,
            ReminderColumns.TITLE,
            ReminderColumns.TIME
        };

        /**
         * ID of the reminder.
         *
         * <p>Auto-generated
         */
        public static final String _ID = "_id";

        /**
         * Time for the reminder, Unix time.
         *
         * <p>User supplied
         */
        public static final String TIME = "time";

        /**
         * Title for reminder.
         *
         * <p>User supplied
         */
        public static final String TITLE = "title";
    }

    // URI matcher, used to parse the Uris
    private static final UriMatcher URI_MATCHER;

    // URI types
    private static final int TYPE_ALL_REMINDERS = 0;
    private static final int TYPE_SPECIFIC_REMINDER = 1;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, REMINDER_TABLE, TYPE_ALL_REMINDERS);
        URI_MATCHER.addURI(AUTHORITY, REMINDER_TABLE + "/#", TYPE_SPECIFIC_REMINDER);
    }

    static Uri getContentUri(String tableName) {
        return new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .path(tableName)
            .build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_ALL_REMINDERS:
                return "vnd.android.cursor.dir/reminder";
            case TYPE_SPECIFIC_REMINDER:
                return "vnd.android.cursor.item.reminder";
            default:
                throw new IllegalArgumentException("Unknown URI type: " + uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_ALL_REMINDERS:
                return insertReminder(values);
            default:
                throw new IllegalArgumentException("Cannot insert into: " + uri.toString());
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_ALL_REMINDERS:
                return getAllReminders(uri, projection, selection, selectionArgs, sortOrder);
            case TYPE_SPECIFIC_REMINDER:
                return getReminder(ContentUris.parseId(uri), projection);
            default:
                throw new IllegalArgumentException("Cannot query: " + uri.toString());
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_SPECIFIC_REMINDER:
                return updateReminder(ContentUris.parseId(uri), values);
            default:
                throw new IllegalArgumentException("Cannot update: " + uri.toString());
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_ALL_REMINDERS:
                return deleteAllReminders();
            case TYPE_SPECIFIC_REMINDER:
                return deleteReminder(ContentUris.parseId(uri));
            default:
                throw new IllegalArgumentException("Cannot delete: " + uri.toString());
        }
    }

    private Uri insertReminder(ContentValues values) {
        SQLiteDatabase db = RemindDatabaseHelper.get(getContext()).getWritableDatabase();
        return ContentUris.withAppendedId(getContentUri(REMINDER_TABLE),
                db.insert(REMINDER_TABLE, null, values));
    }

    private Cursor getAllReminders(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = RemindDatabaseHelper.get(getContext()).getWritableDatabase();
        return db.query(REMINDER_TABLE, projection, selection, selectionArgs, null, null,
                sortOrder);
    }

    private Cursor getReminder(long id, String[] projection) {
        SQLiteDatabase db = RemindDatabaseHelper.get(getContext()).getWritableDatabase();
        return db.query(REMINDER_TABLE, projection, ReminderColumns._ID + "=?",
                new String[] { Long.toString(id) }, null, null, null);
    }

    private int updateReminder(long parseId, ContentValues values) {
        SQLiteDatabase db = RemindDatabaseHelper.get(getContext()).getWritableDatabase();
        return db.update(REMINDER_TABLE, values, ReminderColumns._ID + "=?",
                new String[] { Long.toString(parseId) });
    }

    private int deleteAllReminders() {
        SQLiteDatabase db = RemindDatabaseHelper.get(getContext()).getWritableDatabase();
        return db.delete(REMINDER_TABLE, null, null);
    }

    private int deleteReminder(long parseId) {
        SQLiteDatabase db = RemindDatabaseHelper.get(getContext()).getWritableDatabase();
        return db.delete(REMINDER_TABLE, ReminderColumns._ID + "=?",
                new String[] { Long.toString(parseId) });
    }
}
