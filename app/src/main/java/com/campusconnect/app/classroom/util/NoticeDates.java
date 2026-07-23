package com.campusconnect.app.classroom.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Formats a notice's structured event_date ("YYYY-MM-DD") and optional
 * event_time ("HH:MM:SS") into the display string shown in the highlighted
 * callout, e.g. "Jul 20, 2026" or "Jul 20, 2026 · 10:00 AM".
 */
public final class NoticeDates {

    private NoticeDates() {}

    private static final SimpleDateFormat DATE_LABEL = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private static final SimpleDateFormat TIME_LABEL = new SimpleDateFormat("h:mm a", Locale.US);

    /** Formats a bare "HH:MM:SS" time string as "h:mm a", or null if unparsable. */
    public static String formatTimeOnly(String eventTime) {
        if (eventTime == null || eventTime.length() < 5) return null;
        try {
            String[] timeParts = eventTime.split(":");
            Calendar t = Calendar.getInstance();
            t.clear();
            t.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            t.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            return TIME_LABEL.format(t.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static String format(String eventDate, String eventTime) {
        if (eventDate == null || eventDate.length() < 10) return null;
        try {
            String[] dateParts = eventDate.substring(0, 10).split("-");
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1,
                    Integer.parseInt(dateParts[2]));
            String label = DATE_LABEL.format(cal.getTime());

            if (eventTime != null && eventTime.length() >= 5) {
                String[] timeParts = eventTime.split(":");
                Calendar t = Calendar.getInstance();
                t.clear();
                t.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                t.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                label += " · " + TIME_LABEL.format(t.getTime());
            }
            return label;
        } catch (Exception e) {
            return eventDate;
        }
    }
}
