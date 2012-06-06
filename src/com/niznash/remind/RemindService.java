package com.niznash.remind;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.niznash.remind.content.Intents;
import com.niznash.remind.content.RemindData;
import com.niznash.remind.content.RemindServiceListener;

/**
 * Service that performs long-running operations for the application.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class RemindService extends IntentService {

    // Reminder operation IDs
    private static final int OP_CREATE_REMINDER = 0;
    private static final int OP_UPDATE_REMINDER = 1;
    private static final int OP_DELETE_REMINDER = 2;

    // Registered listeners to notify of the completion of operations
    private static final ArrayList<RemindServiceListener> sListeners =
        new ArrayList<RemindServiceListener>();

    /*
     * Handler on the main/UI thread, used to make sure we notify our listeners on the UI thread
     * only.
     */
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    /**
     * Registers a listener with the service.
     *
     * @param listener The listener to register
     */
    public static void registerListener(RemindServiceListener listener) {
        synchronized (sListeners) {
            sListeners.add(listener);
        }
    }

    /**
     * Unregisters a (previously registered) listener.
     *
     * @param listener The listener to unregister
     */
    public static void unregisterListener(RemindServiceListener listener) {
        synchronized (sListeners) {
            sListeners.remove(listener);
        }
    }

    /**
     * Create a new reminder.
     *
     * @param context The context
     * @param title The title of the reminder
     * @param time The time to schedule the reminder for
     */
    public static void createReminder(Context context, String title, long time) {
        Intent intent = newIntent(context, OP_CREATE_REMINDER);
        intent.putExtra(Intents.EXTRA_TITLE, title);
        intent.putExtra(Intents.EXTRA_TIME, time);
        context.startService(intent);
    }

    /**
     * Update an existing reminder.
     *
     * @param context The context
     * @param id The ID of the reminder to modify
     * @param title The new title for the reminder
     * @param time The new time for the reminder
     */
    public static void updateReminder(Context context, long id, String title, long time) {
        Intent intent = newIntent(context, OP_UPDATE_REMINDER);
        intent.putExtra(Intents.EXTRA_ID, id);
        intent.putExtra(Intents.EXTRA_TITLE, title);
        intent.putExtra(Intents.EXTRA_TIME, time);
        context.startService(intent);
    }

    /**
     * Deletes an existing set of reminders.
     *
     * @param context The context
     * @param ids Array of IDs of reminders to delete
     */
    public static void deleteReminders(Context context, Long... ids) {
        for (Long id : ids) {
            Intent intent = newIntent(context, OP_DELETE_REMINDER);
            intent.putExtra(Intents.EXTRA_ID, id.longValue());
            context.startService(intent);
        }
    }

    /**
     * Helper that generates a new {@link Intent} with the correct target and OP-Code already added.
     *
     * @param context The context
     * @param opCode The operaion code as defined in {@link RemindService}
     * @return An {@link Intent} pre-populated with the target class and op-code
     */
    private static Intent newIntent(Context context, int opCode) {
        Intent intent = new Intent(context, RemindService.class);
        intent.putExtra(Intents.EXTRA_OP, opCode);
        return intent;
    }

    /**
     * Constructor.
     */
    public RemindService() {
	    super("RemindService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        // Process the intent
        processIntent(intent);

        // Notify listeners on the UI thread
        sHandler.post(new Runnable() {
            public void run() {
                onIntentProcessed(intent);
            }
        });
    }

    /**
     * Notifies to listeners that an {@link Intent} was processed. This means that an operation was
     * finished. This method must be called on the UI thread.
     *
     * @param intent The {@link Intent} that was processed, can have additional extras added to it
     *     by {@link #processIntent(Intent)} to be used to extract result data in this method
     */
    private void onIntentProcessed(Intent intent) {
        switch (intent.getIntExtra(Intents.EXTRA_OP, Intents.NO_VALUE)) {
            case OP_CREATE_REMINDER: {
                final long id = intent.getLongExtra(Intents.EXTRA_ID, Intents.NO_VALUE);
                synchronized (sListeners) {
                    for (RemindServiceListener listener : sListeners) {
                        listener.onReminderCreated(id);
                    }
                }
                break;
            }

            case OP_UPDATE_REMINDER: {
                final long id = intent.getLongExtra(Intents.EXTRA_ID, Intents.NO_VALUE);
                synchronized (sListeners) {
                    for (RemindServiceListener listener : sListeners) {
                        listener.onReminderUpdated(id);
                    }
                }
                break;
            }

            case OP_DELETE_REMINDER: {
                final long id = intent.getLongExtra(Intents.EXTRA_ID, Intents.NO_VALUE);
                synchronized (sListeners) {
                    for (RemindServiceListener listener : sListeners) {
                        listener.onReminderDeleted(id);
                    }
                }
                break;
            }

            default:
                throw new IllegalArgumentException("No OP code found!");
        }
    }

    /**
     * Processes an {@link Intent}. Does the heavy-lifting required for the given operation.
     *
     * <p>This blocks all newer {@code Intent}s from being processed so make sure it does not run
     * too long for any single {@code Intent}.
     *
     * @param intent The {@code Intent} to process
     */
    private void processIntent(Intent intent) {
        switch (intent.getIntExtra(Intents.EXTRA_OP, Intents.NO_VALUE)) {
            case OP_CREATE_REMINDER: {
                final String title = intent.getStringExtra(Intents.EXTRA_TITLE);
                final long time = intent.getLongExtra(Intents.EXTRA_TIME, Intents.NO_VALUE);
                long id = RemindData.createReminder(this, title, time);
                intent.putExtra(Intents.EXTRA_ID, id);
                break;
            }

            case OP_UPDATE_REMINDER: {
                final long id = intent.getLongExtra(Intents.EXTRA_ID, Intents.NO_VALUE);
                final String title = intent.getStringExtra(Intents.EXTRA_TITLE);
                final long time = intent.getLongExtra(Intents.EXTRA_TIME, Intents.NO_VALUE);
                RemindData.updateReminder(this, id, title, time);
                break;
            }

            case OP_DELETE_REMINDER: {
                final long id = intent.getLongExtra(Intents.EXTRA_ID, Intents.NO_VALUE);
                RemindData.deleteReminder(this, id);
                break;
            }

            default:
                throw new IllegalArgumentException("No OP code found!");
        }
    }
}
