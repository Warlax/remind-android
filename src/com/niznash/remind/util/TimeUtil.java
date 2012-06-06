package com.niznash.remind.util;

import java.util.Calendar;
import java.util.Locale;

import android.text.format.DateFormat;

/**
 * Converts {@code long}s to readable dates.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class TimeUtil {

    private static final String FORMAT = "MMM dd h:mmaa";

    /**
     * Converts a time (in Posix time} to readable text.
     *
     * @param time The time
     * @return A user readable string for that time
     */
    public static String toText(long time) {
        return (String) DateFormat.format(FORMAT, time);
    }

    /**
     * Extracts the hours a day from the given Posix time.
     *
     * @param time The time
     * @return The hours a day
     */
    public static int getHours(long time) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTimeInMillis(time);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Extracts the minutes from the given Posix time.
     *
     * @param time The time
     * @return The minutes
     */
    public static int getMinutes(long time) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTimeInMillis(time);
        return c.get(Calendar.MINUTE);
    }

    /**
     * Converts hours and minutes to a Posix time either today or tomorrow, depending on the current
     * time such that: if a time of day was specified that's earlier than now, the returned time
     * would be tomorrow. Otherwise, the time would be today.
     *
     * @param hourOfDay Hour in the day (24 hour format)
     * @param minute Minutes
     * @return Time (Posix time)
     */
    public static long toTime(int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        long result = c.getTimeInMillis();
        if (result < System.currentTimeMillis()) {
            result += 24 * 60 * 60 * 1000;
        }

        return result;
    }
}
