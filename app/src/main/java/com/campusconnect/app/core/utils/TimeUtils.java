package com.campusconnect.app.core.utils;

import android.text.format.DateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    public static String getRelativeTime(String isoTime) {
        if (isoTime == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(isoTime);
            if (date == null) return "";
            long now = System.currentTimeMillis();
            return DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
        } catch (ParseException e) {
            return isoTime;
        }
    }

    public static String formatDate(String dateStr) {
        if (dateStr == null) return "";
        SimpleDateFormat fromSdf = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.US);
        SimpleDateFormat toSdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        try {
            Date date = fromSdf.parse(dateStr);
            if (date == null) return dateStr;
            return toSdf.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }
}
