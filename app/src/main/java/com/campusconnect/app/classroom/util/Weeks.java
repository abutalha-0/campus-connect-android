package com.campusconnect.app.classroom.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Buckets an upload timestamp into the Saturday→Friday week it falls in, for
 * grouping resources (and, via the relative-label helpers below, Schedule).
 * Uses the date portion of the ISO created_at string.
 */
public final class Weeks {

    private Weeks() {}

    private static final SimpleDateFormat ISO_DATE =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat KEY_FMT =
            new SimpleDateFormat("yyyyMMdd", Locale.US);
    private static final SimpleDateFormat LABEL_FMT =
            new SimpleDateFormat("MMM d", Locale.US);
    private static final SimpleDateFormat DAY_FMT =
            new SimpleDateFormat("EEE", Locale.US);

    /** Stable key for the Saturday that starts this timestamp's week. */
    public static String weekKey(String isoDateTime) {
        Calendar saturday = saturdayOf(isoDateTime);
        return saturday != null ? KEY_FMT.format(saturday.getTime()) : "unknown";
    }

    /** Human label for the week, e.g. "Jul 11 – Jul 17". */
    public static String weekLabel(String isoDateTime) {
        Calendar saturday = saturdayOf(isoDateTime);
        if (saturday == null) return "Unknown week";
        Date start = saturday.getTime();
        saturday.add(Calendar.DAY_OF_MONTH, 6);
        Date end = saturday.getTime();
        return LABEL_FMT.format(start) + " – " + LABEL_FMT.format(end);
    }

    /** Short day-of-week label, e.g. "Sat", "Mon". */
    public static String dayLabel(String isoDateTime) {
        Calendar saturday = saturdayOf(isoDateTime);
        // saturdayOf() rewinds to the week's Saturday; parse the original
        // date directly instead so the label reflects the actual day.
        if (isoDateTime == null || isoDateTime.length() < 10) return "";
        try {
            Date d = ISO_DATE.parse(isoDateTime.substring(0, 10));
            return DAY_FMT.format(d);
        } catch (Exception e) {
            return "";
        }
    }

    /** "This Week", "Next Week", or null if further out (caller supplies a fallback). */
    public static String relativeWeekLabel(String isoDateTime) {
        String key = weekKey(isoDateTime);
        String todayKey = weekKey(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        if (key.equals(todayKey)) return "This Week";

        Calendar todaySaturday = saturdayOf(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        if (todaySaturday != null) {
            todaySaturday.add(Calendar.DAY_OF_MONTH, 7);
            if (key.equals(KEY_FMT.format(todaySaturday.getTime()))) return "Next Week";
        }
        return null;
    }

    /** The Calendar (set to midnight) for the Saturday that starts this date's week. */
    public static Calendar weekStart(String isoDateTime) {
        return saturdayOf(isoDateTime);
    }

    private static Calendar saturdayOf(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.length() < 10) return null;
        try {
            Date d = ISO_DATE.parse(isoDateTime.substring(0, 10));
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            int daysSinceSaturday = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7;
            cal.add(Calendar.DAY_OF_MONTH, -daysSinceSaturday);
            return cal;
        } catch (Exception e) {
            return null;
        }
    }
}
