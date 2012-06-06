package com.niznash.remind.content;

/**
 * Listens to completion of operations done in the service.
 *
 * @author niznash@gmail.com (Alejandro Nijamkin)
 */
public class RemindServiceListener {

    /**
     * Notifies that a reminder was created.
     *
     * @param id ID of the new reminder
     */
    public void onReminderCreated(long id) {
    }

    /**
     * Notifies that a reminder was updated.
     *
     * @param id ID of the updated reminder
     */
    public void onReminderUpdated(long id) {
    }

    /**
     * Notifies that a reminder was deleted.
     *
     * @param id ID of the deleted reminder
     */
    public void onReminderDeleted(long id) {
    }
}
