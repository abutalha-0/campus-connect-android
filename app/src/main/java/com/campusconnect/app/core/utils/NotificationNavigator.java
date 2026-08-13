package com.campusconnect.app.core.utils;

import android.content.Context;

import com.campusconnect.app.classroom.FeedPostDetailActivity;
import com.campusconnect.app.classroom.SubjectDetailActivity;
import com.campusconnect.app.crew.PostDetailActivity;
import com.campusconnect.app.home.HomeActivity;

/**
 * Single place that decides where tapping a Notification goes, based on
 * its action_url — a simple "{app}/..." convention set by each source
 * app's signals.py on the backend (crew/signals.py, route_mate/signals.py,
 * classroom/notices/signals.py, classroom/resources/signals.py,
 * classroom/feed/signals.py). Same shape as ProfileNavigator.open(...).
 */
public final class NotificationNavigator {

    private NotificationNavigator() {}

    public static void open(Context context, String actionUrl) {
        if (actionUrl == null || actionUrl.isEmpty()) return;

        String[] parts = actionUrl.split("/");
        if (parts.length == 0) return;

        try {
            switch (parts[0]) {
                case "crew":
                    openCrew(context, parts);
                    break;
                case "routemate":
                    // No standalone route-detail screen reachable from just
                    // an id yet (RouteDetailBottomSheet needs a full Route
                    // object) — open the Route Mate list for v1.
                    context.startActivity(HomeActivity.createRouteMateIntent(context));
                    break;
                case "classroom":
                    openClassroom(context, parts);
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException ignored) {
            // malformed id segment in action_url — no-op, notification
            // still gets marked read by the caller regardless.
        }
    }

    private static void openCrew(Context context, String[] parts) {
        // crew/posts/{slug}
        if (parts.length >= 3 && "posts".equals(parts[1])) {
            context.startActivity(PostDetailActivity.createIntent(context, parts[2]));
        }
    }

    private static void openClassroom(Context context, String[] parts) {
        // classroom/subjects/{subjectId}/notices/{noticeId}
        // classroom/subjects/{subjectId}/resources/{resourceId}
        if (parts.length >= 5 && "subjects".equals(parts[1])) {
            int subjectId = Integer.parseInt(parts[2]);
            if ("notices".equals(parts[3])) {
                int noticeId = Integer.parseInt(parts[4]);
                SubjectDetailActivity.startAtNotice(context, subjectId, "", noticeId);
            } else {
                // Resources (or anything else under a subject): no
                // deep-link target for the specific item yet — open the
                // subject itself so it's at least one tap away.
                SubjectDetailActivity.start(context, subjectId, "", "");
            }
            return;
        }

        // classroom/feed/{postId}
        if (parts.length >= 3 && "feed".equals(parts[1])) {
            int postId = Integer.parseInt(parts[2]);
            FeedPostDetailActivity.start(context, postId);
        }
    }
}
