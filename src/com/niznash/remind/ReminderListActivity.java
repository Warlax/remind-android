package com.niznash.remind;

import java.util.HashSet;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.niznash.remind.ConfirmDialogFragment.OnConfirmDialogResultListener;
import com.niznash.remind.content.Intents;
import com.niznash.remind.content.RemindServiceListener;
import com.niznash.remind.content.ReminderLoader;

/**
 * Displays a list of current reminders.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class ReminderListActivity extends FragmentActivity implements LoaderCallbacks<Cursor>,
        OnItemClickListener {

    private static final int DIALOG_ID_CONFIRM_DELETE = 0;

    /*
     * Service listener that reloads the list of reminders every time a reminder is created,
     * updated, or deleted.
     */
    private RemindServiceListener mRemindServiceListener = new RemindServiceListener() {

        @Override
        public void onReminderCreated(long id) {
            reload();
        }

        @Override
        public void onReminderUpdated(long id) {
            reload();
        }

        @Override
        public void onReminderDeleted(long id) {
            reload();
        }
    };

    /**
     * The {@link ListFragment} used to display the {@link ListView} of all reminders in the app
     */
    private ListFragment mListFragment;

    /**
     * Collection of reminder IDs that are currently selected, used to pass multiple items to the
     * {@link RemindService#deleteReminders(android.content.Context, Long...)} method
     */
    private final HashSet<Long> mSelectedIds = new HashSet<Long>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add the ListFragment
        final FragmentManager gm = getSupportFragmentManager();
        mListFragment = (ListFragment) gm.findFragmentById(android.R.id.content);
        if (mListFragment == null) {
            mListFragment = new ListFragment();
            getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mListFragment)
                .commit();
        }

        // Load the data for the first time
        reload();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the ListFragment
        mListFragment.setListAdapter(new ReminderCursorAdapter(this));
        mListFragment.setEmptyText(getString(R.string.no_reminders));

        // Add an item-click listener to the ListView so we can open a reminder once clicked
        ListView listView = mListFragment.getListView();
        listView.setOnItemClickListener(this);

        /*
         * On HC and up, we have the ability to use the CAB (contextual action bar) to select
         * multiple items for bulk processing. This means that if the user long-presses an item, the
         * ActionBar changes its UI and then, subsequent item clicks add or remove items from the
         * collection of currently selected items.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

                private ActionMode mMode;

                private OnConfirmDialogResultListener mOnConfirmListener =
                    new OnConfirmDialogResultListener() {
                    
                    public void onConfirmDialogPositive(int dialogId) {
                        /*
                         * We just confirmed that we wish to delete the currently selected items:
                         * Schedule them for deletion on the ReminderService, show a toast, and
                         * finish the CAB mode
                         */
                        RemindService.deleteReminders(ReminderListActivity.this,
                                mSelectedIds.toArray(new Long[0]));
                        Toast.makeText(ReminderListActivity.this, R.string.message_reminder_deleted,
                                Toast.LENGTH_SHORT).show();
                        mMode.finish();
                    }
                    
                    public void onConfirmDialogNegative(int dialogId) {
                    }
                };

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                    // CAB mode destroyed, clear the current selected set
                    mSelectedIds.clear();
                    mMode = null;
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mMode = mode;
                    new MenuInflater(ReminderListActivity.this).inflate(
                            R.menu.reminder_list_cab, menu);
                    return true;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.menu_item_delete) {
                        // The user wishes to delete the currently selected items, confirm it first
                        ConfirmDialogFragment.show(getSupportFragmentManager(), mOnConfirmListener,
                                DIALOG_ID_CONFIRM_DELETE, getString(R.string.action_delete));
                        return true;
                    }

                    return false;
                }

                public void onItemCheckedStateChanged(ActionMode mode,
                        int position, long id, boolean checked) {
                    if (checked) {
                        mSelectedIds.add(id);
                    } else {
                        mSelectedIds.remove(id);
                    }
                    /*
                     * TODO(niznash): there is no way to alter the UI to show what items are
                     *     selected.
                     */

                    // Update the title of the CAB to reflect the number of selected items
                    mode.setTitle(getString(R.string.selected,
                            Integer.valueOf(mSelectedIds.size())));
                }
                
            });
        } else {
            /*
             * On devices with APIs lower than HC, we use a popup context menu to act on a single
             * reminder at a time
             */
            registerForContextMenu(listView);
        }

        /*
         * Our Activity was resumed, register a listener with the RemindService so we are notified
         * when service operations finish
         */
        RemindService.registerListener(mRemindServiceListener);

        // Reload the data
        reload();
    }

    @Override
    protected void onPause() {
        /*
         * Our Activity was paused, its UI is no longer visible. Unregister the listener so the
         * RemindService does not notify us when operations finish and does not keep a reference to
         * this instance
         */
        RemindService.unregisterListener(mRemindServiceListener);

        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // Create the context menu and mark the long-pressed item as selected
        mSelectedIds.add(((AdapterView.AdapterContextMenuInfo) menuInfo).id);
        new MenuInflater(ReminderListActivity.this).inflate(
                R.menu.reminder_list_cab, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete) {
            // The user selected the "delete" item from the context menu, confirm it first
            ConfirmDialogFragment.show(getSupportFragmentManager(),
                    new OnConfirmDialogResultListener() {
                public void onConfirmDialogPositive(int dialogId) {
                    // Confirmed, delete the reminder, show a toast, clear the selection
                    RemindService.deleteReminders(ReminderListActivity.this,
                            mSelectedIds.toArray(new Long[0]));
                    Toast.makeText(ReminderListActivity.this, R.string.message_reminder_deleted,
                            Toast.LENGTH_SHORT).show();
                    mSelectedIds.clear();
                }

                public void onConfirmDialogNegative(int dialogId) {
                    // The user canceled the deletion, clear the selection
                    mSelectedIds.clear();
                }
            }, DIALOG_ID_CONFIRM_DELETE, getString(R.string.action_delete));
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.reminder_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_add) {
            // User clicked the "create" options item, start the ReminderActivity to create one
            createNewReminder();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new ReminderLoader(this);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Finished loading our reminders, update the existing ListAdapter or create a new one
        CursorAdapter adapter = (CursorAdapter) mListFragment.getListAdapter();
        if (adapter == null) {
            adapter = new ReminderCursorAdapter(this);
            mListFragment.setListAdapter(adapter);
        }

        adapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        ((CursorAdapter) mListFragment.getListAdapter()).swapCursor(null);
    }

    /**
     * If no loader was created, creates one.
     *
     * <p>(Re)starts the current loader.
     */
    private void reload() {
        final LoaderManager lm = getSupportLoaderManager();
        lm.restartLoader(0, null, this);
    }

    /**
     * Starts {@code ReminderActivity} to create a new reminder
     */
    private void createNewReminder() {
        startActivity(Intents.newCreateReminderIntent(this));
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        // A reminder item from the list was clicked, start ReminderActivity to edit that reminder
        long reminderId =
            ((ReminderCursorAdapter) mListFragment.getListAdapter()).getItemId(position);
        startActivity(Intents.newEditReminderIntent(this, reminderId));
    }
}
