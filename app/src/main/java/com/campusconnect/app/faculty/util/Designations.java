package com.campusconnect.app.faculty.util;

import android.content.Context;

import com.campusconnect.app.R;

/**
 * Maps between the faculty designation keys stored by the backend
 * (e.g. ASSOCIATE_PROFESSOR) and their human-readable labels
 * (e.g. "Associate Professor").
 */
public final class Designations {

    private Designations() {}

    public static String labelFor(Context context, String key) {
        String[] keys = context.getResources().getStringArray(R.array.faculty_designation_keys);
        String[] labels = context.getResources().getStringArray(R.array.faculty_designations);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) return labels[i];
        }
        return key != null ? key : "";
    }

    /** Spinner position for a given key, defaulting to Assistant Professor (1). */
    public static int indexOf(Context context, String key) {
        String[] keys = context.getResources().getStringArray(R.array.faculty_designation_keys);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) return i;
        }
        return 1;
    }

    public static String keyAt(Context context, int position) {
        String[] keys = context.getResources().getStringArray(R.array.faculty_designation_keys);
        if (position >= 0 && position < keys.length) return keys[position];
        return keys[1];
    }
}
