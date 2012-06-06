package com.niznash.remind.content;

import com.niznash.remind.content.RemindProvider.ReminderColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates and/or opens the database as needed.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class RemindDatabaseHelper extends SQLiteOpenHelper {

    private static RemindDatabaseHelper sInstance;

    private static final String DATABASE_NAME = "remind.db";
    private static final int VERSION = 1;

    static synchronized RemindDatabaseHelper get(Context context) {
        if (sInstance == null) {
            sInstance = new RemindDatabaseHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    public RemindDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = new StringBuilder()
            .append("CREATE TABLE " + RemindProvider.REMINDER_TABLE + " (")
            .append(ReminderColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ")
            .append(ReminderColumns.TITLE + " TEXT NOT NULL, ")
            .append(ReminderColumns.TIME + " INTEGER NOT NULL);")
            .toString();
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = new StringBuilder()
            .append("DROP TABLE " + RemindProvider.REMINDER_TABLE + " IF EXISTS")
            .toString();
        db.execSQL(sql);
        onCreate(db);
    }
}
