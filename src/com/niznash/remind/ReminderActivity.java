package com.niznash.remind;

import android.app.ActionBar;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.niznash.remind.ConfirmDialogFragment.OnConfirmDialogResultListener;
import com.niznash.remind.content.Intents;
import com.niznash.remind.content.ReminderLoader;
import com.niznash.remind.util.TimeUtil;

/**
 * Creates or updates a reminder.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class ReminderActivity extends FragmentActivity implements LoaderCallbacks<Cursor>,
        OnConfirmDialogResultListener {

    private static final int DIALOG_ID_CONFIRM_DELETE = 0;

    // Some current reminder data
    private long mReminderId;
    private long mTime;

    
    // Views
    private EditText mTitleEditText;
    private TextView mTimeTextView;
    private TextView mPositiveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_activity);

        // Get handles to the relevant views
        mTitleEditText = (EditText) findViewById(R.id.edit_title);
        mTimeTextView = (TextView) findViewById(R.id.time);
        mPositiveButton = (TextView) findViewById(android.R.id.button1);

        // Add a listener to text editing of the title so we can enable/disable the positive button
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            public void afterTextChanged(Editable s) {
                mPositiveButton.setEnabled(s.length() > 0);
            }
        });

        // Check if we were restarted
        if (savedInstanceState != null) {
            // Activity was restarted, get the reminder data and update the UI
            mReminderId = savedInstanceState.getLong(Intents.EXTRA_ID);
            String title = savedInstanceState.getString(Intents.EXTRA_TITLE);
            mTime = savedInstanceState.getLong(Intents.EXTRA_TIME);
            mTitleEditText.setText(title);
            mTimeTextView.setText(TimeUtil.toText(mTime));
        } else {
            /*
             * Activity is new, get the reminder ID we'll use to load the correct reminder.
             *
             * For now, setup temporary defaults for the title and the time of the reminder.
             */
            mReminderId = getIntent().getLongExtra(Intents.EXTRA_ID, Intents.NO_VALUE);
            mTime = System.currentTimeMillis() + 1000 * 60 * 60;
            mTitleEditText.setText(null);
            mTimeTextView.setText(TimeUtil.toText(mTime));
        }

        // Check if we're updating an existing reminder or creating a brand-new one
        if (mReminderId != Intents.NO_VALUE) {
            // Updating an existing reminder, positive button should say "Save"
            mPositiveButton.setText(R.string.action_save);
        } else {
            // Creating a new reminder, positive button should say "Create"
            mPositiveButton.setText(R.string.action_create);
        }

        // On newer devices (HC and up) turn on the "up affordance" in the ActionBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we're updating an existing reminder, load it from the DB
        if (mReminderId != Intents.NO_VALUE) {
            getSupportLoaderManager().initLoader(0, null, this);
        }

        /*
         * We should show the soft keyboard so the user can start typing into the title field
         * immediately
         */
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    protected void onPause() {
        // Hide the keyboard, if it was shown, so it does not stick around in the previous activity
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTitleEditText.getWindowToken(), 0);

        super.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save reminder data
        outState.putLong(Intents.EXTRA_ID, mReminderId);
        outState.putLong(Intents.EXTRA_TIME, mTime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.reminder, menu);

        /*
         * The "delete" menu item should be inaccessible when creating a new reminder: can't delete
         * a reminder that hasn't been created yet.
         */
        menu.findItem(R.id.menu_item_delete).setVisible(mReminderId != Intents.NO_VALUE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // User has hit the "home" icon in the ActionBar, finish this Activity
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_item_delete) {
            // User has hit the "delete" menu item - bring up a confirmation dialog
            ConfirmDialogFragment.show(getSupportFragmentManager(), this, DIALOG_ID_CONFIRM_DELETE,
                    getString(R.string.action_delete));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Notifies that the time button has been touched; see the XML file for the declaration of this
     * callback.
     *
     * @param view The view that was touched
     */
    public void onEditTimeButtonClicked(View view) {
        // Get the current time in hours (in a day) and minutes
        final int hours = TimeUtil.getHours(mTime);
        final int minutes = TimeUtil.getMinutes(mTime);

        // Show the time picker dialog
        TimePickerDialogFragment.show(getSupportFragmentManager(),
                new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Time was set in the dialog, update it locally and refresh the UI
                        mTime = TimeUtil.toTime(hourOfDay, minute);
                        mTimeTextView.setText(TimeUtil.toText(mTime));
                    }
        }, hours, minutes);
    }

    /**
     * Notifies that the positive button has been touched; see the XML file for the declaration of
     * this callback.
     *
     * @param view The view that was touched
     */
    public void onPositiveButtonClicked(View view) {
        // Get the current title from the UI
        final String title = mTitleEditText.getText().toString();

        // Check if we need to update an existing reminder or create a brand-new one
        if (mReminderId != Intents.NO_VALUE) {
            // update the reminder
            RemindService.updateReminder(this, mReminderId, title, mTime);
            Toast.makeText(this, R.string.message_reminder_updated, Toast.LENGTH_SHORT).show();
        } else {
            // create a new reminder
            RemindService.createReminder(this, title, mTime);
            Toast.makeText(this, R.string.message_reminder_created, Toast.LENGTH_SHORT).show();
        }

        // After touching the positive button, we finish this Activity
        finish();
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new ReminderLoader(this, mReminderId);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            // We loaded some new data in this cursor

            // Move to the first (and hopefully, the only) row in the Cursor
            cursor.moveToFirst();

            /*
             * Get the title and time from the cursor and update the UI accordingly.
             *
             * We can have magic numbers here (e.g. 1, 2) because we're making the grand assumption
             * that everything is using the ReminderColumns.PROJECTION projection for DB queries
             */
            String title = cursor.getString(1);
            mTime = cursor.getLong(2);
            mTitleEditText.setText(title);
            mTimeTextView.setText(TimeUtil.toText(mTime));
        } else {
            // TODO(niznash): display error message
        }
    }

    public void onLoaderReset(Loader<Cursor> cursor) {
    }

    public void onConfirmDialogPositive(int dialogId) {
        switch (dialogId) {
            case DIALOG_ID_CONFIRM_DELETE:
                // The "Are you sure you wish to delete this reminder?" dialog was confirmed, delete
                deleteReminder();
                break;
        }
    }

    public void onConfirmDialogNegative(int dialogId) {
    }

    /**
     * Asks the {@link RemindService} to delete the current reminder, shows a Toast, and finishes
     * the Activity
     */
    private void deleteReminder() {
        RemindService.deleteReminders(this, new Long[] { Long.valueOf(mReminderId) });
        Toast.makeText(this, R.string.message_reminder_deleted, Toast.LENGTH_SHORT).show();
        finish();
    }
}
