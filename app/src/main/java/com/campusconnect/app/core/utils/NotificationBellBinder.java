package com.campusconnect.app.core.utils;

import android.content.Context;
import android.view.View;

import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.notifications.NotificationApiService;
import com.campusconnect.app.notifications.NotificationsActivity;
import com.campusconnect.app.notifications.model.UnreadCountResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Wires the shared top_bar_main.xml bell (btnNotifications + notificationDot)
 * to open NotificationsActivity and reflect the current unread count. Used
 * by Home/Discover/Profile (each includes the bar separately) and
 * CrewActivity (its own bell, same shape, same behavior).
 */
public final class NotificationBellBinder {

    /** Lets callers gate the async unread-count callback on their own
     *  lifecycle (Fragment.isAdded(), Activity.isFinishing(), etc.) without
     *  this class needing to know which kind of caller it has. */
    public interface StillValidCheck {
        boolean isStillValid();
    }

    private NotificationBellBinder() {}

    public static void bindClick(View barRoot, Context context) {
        barRoot.findViewById(R.id.btnNotifications).setOnClickListener(v ->
                context.startActivity(NotificationsActivity.createIntent(context)));
    }

    public static void refreshUnreadDot(View barRoot, TokenManager tokenManager, StillValidCheck stillValid) {
        View dot = barRoot.findViewById(R.id.notificationDot);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NotificationApiService.class)
                .getUnreadCount(token)
                .enqueue(new Callback<UnreadCountResponse>() {
                    @Override
                    public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                        if (!stillValid.isStillValid()) return;
                        boolean hasUnread = response.isSuccessful() && response.body() != null
                                && response.body().getUnreadCount() > 0;
                        dot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<UnreadCountResponse> call, Throwable t) {}
                });
    }
}
