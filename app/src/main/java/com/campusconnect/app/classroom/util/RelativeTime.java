package com.campusconnect.app.classroom.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Formats an ISO UTC timestamp as a short relative label: "20m", "1h", "3d", or a date once old. */
public final class RelativeTime {

    private RelativeTime() {}

    public static String format(String isoUtc) {
        if (isoUtc == null || isoUtc.length() < 19) return "";
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date then = iso.parse(isoUtc.substring(0, 19));
            long diffMs = System.currentTimeMillis() - then.getTime();
            if (diffMs < 0) diffMs = 0;

            long minutes = diffMs / (60 * 1000);
            if (minutes < 1) return "now";
            if (minutes < 60) return minutes + "m";
            long hours = minutes / 60;
            if (hours < 24) return hours + "h";
            long days = hours / 24;
            if (days < 7) return days + "d";
            return new SimpleDateFormat("MMM d", Locale.US).format(then);
        } catch (Exception e) {
            return "";
        }
    }
}
