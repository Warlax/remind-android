package com.niznash.remind;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.niznash.remind.content.RemindProvider.ReminderColumns;
import com.niznash.remind.util.TimeUtil;

/**
 * Adapter between a reminder {@link Cursor} and the UI. Assumes the use of
 * {@link ReminderColumns#PROJECTION}.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class ReminderCursorAdapter extends CursorAdapter {

    /**
     * View-holder to cache child views of a list item and speed up the time to bind views.
     */
    private static class Holder {
        public final TextView title;
        public final TextView time;

        public Holder(View view) {
            title = (TextView) view.findViewById(android.R.id.text1);
            time = (TextView) view.findViewById(android.R.id.text2);
        }
    }

    /**
     * Constructor.
     *
     * @param context The context
     */
    public ReminderCursorAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final Holder holder = (Holder) view.getTag();
        holder.title.setText(cursor.getString(1));
        holder.time.setText(TimeUtil.toText(cursor.getLong(2)));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup container) {
        final View view = LayoutInflater.from(context).inflate(
                R.layout.reminder_list_item, container, false);
        view.setTag(new Holder(view));
        return view;
    }
}
