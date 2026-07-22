package com.campusconnect.app.classroom.util;

import android.content.Context;
import android.graphics.Color;

import com.campusconnect.app.R;

/**
 * Maps resource type keys (PDF/PPT/DOC/VID) to display labels and badge colors.
 */
public final class ResourceTypes {

    private ResourceTypes() {}

    public static String labelFor(Context context, String key) {
        String[] keys = context.getResources().getStringArray(R.array.resource_type_keys);
        String[] labels = context.getResources().getStringArray(R.array.resource_types);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) return labels[i];
        }
        return key != null ? key : "";
    }

    public static int indexOf(Context context, String key) {
        String[] keys = context.getResources().getStringArray(R.array.resource_type_keys);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) return i;
        }
        return 0;
    }

    public static String keyAt(Context context, int position) {
        String[] keys = context.getResources().getStringArray(R.array.resource_type_keys);
        if (position >= 0 && position < keys.length) return keys[position];
        return keys[0];
    }

    public static int colorFor(String key) {
        if ("PDF".equals(key)) return Color.parseColor("#F87171");
        if ("PPT".equals(key)) return Color.parseColor("#22D3EE");
        if ("DOC".equals(key)) return Color.parseColor("#A855F7");
        if ("VID".equals(key)) return Color.parseColor("#F59E0B");
        return Color.parseColor("#9CA3AF");
    }
}
