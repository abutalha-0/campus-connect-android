package com.campusconnect.app.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusconnect.app.R;
import com.campusconnect.app.core.api.PageResponse;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.NavShellActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.NotificationNavigator;
import com.campusconnect.app.notifications.model.Notification;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * NEW (Notifications feature): full notification list. Tapping a
 * notification marks it read (if it wasn't already) and navigates to
 * whatever it's about, via NotificationNavigator + action_url.
 */
public class NotificationsActivity extends NavShellActivity {

    public static Intent createIntent(Context ctx) {
        return new Intent(ctx, NotificationsActivity.class);
    }

    private TextView tvStatus;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(R.string.notifications_title));
        findViewById(R.id.btnSave).setVisibility(View.GONE);
        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        tvStatus = findViewById(R.id.tvStatus);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NotificationApiService.class)
                .getNotifications(token, null)
                .enqueue(new Callback<PageResponse<Notification>>() {
                    @Override
                    public void onResponse(Call<PageResponse<Notification>> call, Response<PageResponse<Notification>> response) {
                        if (isFinishing()) return;

                        if (!response.isSuccessful() || response.body() == null || response.body().getResults() == null) {
                            tvStatus.setText(getString(R.string.error_network));
                            tvStatus.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        List<Notification> notifications = response.body().getResults();
                        if (notifications.isEmpty()) {
                            tvStatus.setText(getString(R.string.notifications_empty));
                            tvStatus.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        tvStatus.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        if (adapter == null) {
                            adapter = new NotificationAdapter(notifications, NotificationsActivity.this::onNotificationTapped);
                            recyclerView.setAdapter(adapter);
                        } else {
                            adapter.setNotifications(notifications);
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<Notification>> call, Throwable t) {
                        if (isFinishing()) return;
                        tvStatus.setText(getString(R.string.error_network));
                        tvStatus.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private void onNotificationTapped(Notification notification) {
        NotificationNavigator.open(this, notification.getActionUrl());

        if (notification.isRead()) return;

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NotificationApiService.class)
                .markRead(token, notification.getId())
                .enqueue(new Callback<Notification>() {
                    @Override
                    public void onResponse(Call<Notification> call, Response<Notification> response) {
                        if (isFinishing()) return;
                        loadNotifications();
                    }

                    @Override
                    public void onFailure(Call<Notification> call, Throwable t) {}
                });
    }
}
