package com.campusconnect.app.classroom.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Buckets an upload timestamp into the Saturday→Friday week it falls in, for
 * grouping resources. Uses the date portion of the ISO created_at string.
 */
public final class Weeks {

    private Weeks() {}

    private static final SimpleDateFormat ISO_DATE =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat KEY_FMT =
            new SimpleDateFormat("yyyyMMdd", Locale.US);
    private static final SimpleDateFormat LABEL_FMT =
            new SimpleDateFormat("MMM d", Locale.US);

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
