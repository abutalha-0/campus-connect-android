package com.campusconnect.app.notifications.model;

/** NEW (Notifications feature): shape of GET /api/notifications/unread-count/. */
public class UnreadCountResponse {
    private int unread_count;
    public int getUnreadCount() { return unread_count; }
}
