package com.campusconnect.app.core.utils;

import android.content.Context;
import android.content.Intent;

import com.campusconnect.app.faculty.FacultyPublicProfileActivity;
import com.campusconnect.app.profile.PublicProfileActivity;

/**
 * Single place that decides where tapping a profile icon/name goes: faculty
 * users open the read-only faculty profile, everyone else (students, CRs —
 * a CR is still a student account) opens the student public profile.
 */
public final class ProfileNavigator {

    private ProfileNavigator() {}

    public static void open(Context context, int userId, String role) {
        if (userId <= 0) return;

        if ("FACULTY".equals(role)) {
            FacultyPublicProfileActivity.start(context, userId);
        } else {
            Intent intent = new Intent(context, PublicProfileActivity.class);
            intent.putExtra("user_id", userId);
            context.startActivity(intent);
        }
    }
}
