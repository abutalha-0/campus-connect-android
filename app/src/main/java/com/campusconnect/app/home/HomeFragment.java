package com.campusconnect.app.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.PageResponse;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.NotificationBellBinder;
import com.campusconnect.app.core.utils.NotificationNavigator;
import com.campusconnect.app.core.utils.SkeletonAnimator;
import com.campusconnect.app.core.utils.TimeUtils;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.crew.CrewApiService;
import com.campusconnect.app.crew.model.Post;
import com.campusconnect.app.lostfound.api.LostFoundApiService;
import com.campusconnect.app.lostfound.model.LostFoundItem;
import com.campusconnect.app.notifications.NotificationApiService;
import com.campusconnect.app.notifications.model.Notification;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.routemate.api.RouteMateApiService;
import com.campusconnect.app.routemate.model.Route;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard tab: greeting + feature grid + recent activity. Grid badges and
 * the activity feed are backed by real data (Crew/Lost&Found/Route-Mate
 * counts, and the shared notifications feed) — see loadXBadge()/
 * loadRecentActivity() below.
 */
public class HomeFragment extends Fragment {

    // Keeps the dashboard preview short — the full list is one tap away via
    // the notification bell.
    private static final int MAX_ACTIVITY_ITEMS = 5;

    private TokenManager tokenManager;

    private TextView tvGreeting, tvUserName, tvAvatarInitials;
    private ImageView ivAvatar;
    private LinearLayout activityContainer;
    private View skeletonNameLine;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        activityContainer = view.findViewById(R.id.activityContainer);
        skeletonNameLine = view.findViewById(R.id.skeletonNameLine);

        tvGreeting.setText(greetingForCurrentTime());

        view.findViewById(R.id.btnMenu).setOnClickListener(v ->
                ((HomeActivity) requireActivity()).openDrawer());
        NotificationBellBinder.bindClick(view, requireContext());

        setUpBlock(view, R.id.ivClassroomIcon, R.id.tvClassroomBadge, R.drawable.ic_classroom, R.color.color_cyan);
        setUpBlock(view, R.id.ivCrewIcon, R.id.tvCrewBadge, R.drawable.ic_crew, R.color.color_purple);
        setUpBlock(view, R.id.ivLostIcon, R.id.tvLostBadge, R.drawable.ic_lost, R.color.color_amber);
        setUpBlock(view, R.id.ivRouteIcon, R.id.tvRouteBadge, R.drawable.ic_route, R.color.color_indigo);

        view.findViewById(R.id.blockClassroom).setOnClickListener(v ->
                startActivity(new android.content.Intent(getActivity(),
                        com.campusconnect.app.classroom.ClassroomActivity.class)));
        view.findViewById(R.id.blockCrew).setOnClickListener(v ->
                startActivity(com.campusconnect.app.crew.CrewActivity.createIntent(requireContext())));
        view.findViewById(R.id.blockLost).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.campusconnect.app.lostfound.LostFoundListFragment())
                        .addToBackStack(null)
                        .commit());
        view.findViewById(R.id.blockRoute).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.campusconnect.app.routemate.RouteMateListFragment())
                        .addToBackStack(null)
                        .commit());

        loadDashboardData(view);
        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            NotificationBellBinder.refreshUnreadDot(getView(), tokenManager, this::isAdded);
            loadDashboardData(getView());
        }
    }

    private void loadDashboardData(View view) {
        loadClassroomBadge(view.findViewById(R.id.tvClassroomBadge));
        loadCrewBadge(view.findViewById(R.id.tvCrewBadge));
        loadLostFoundBadge(view.findViewById(R.id.tvLostBadge));
        loadRouteMateBadge(view.findViewById(R.id.tvRouteBadge));
        loadRecentActivity();
    }

    // ── Greeting ──────────────────────────────────────────────────────────

    private String greetingForCurrentTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return getString(R.string.greeting_morning);
        if (hour < 17) return getString(R.string.greeting_afternoon);
        return getString(R.string.greeting_evening);
    }

    // ── Profile (greeting name + avatar) ─────────────────────────────────

    private void loadProfile() {
        SkeletonAnimator.showLoading(skeletonNameLine, tvUserName);
        SkeletonAnimator.pulse(tvAvatarInitials);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (!isAdded()) return;
                        SkeletonAnimator.showContent(skeletonNameLine, tvUserName);
                        SkeletonAnimator.stop(tvAvatarInitials);
                        if (response.isSuccessful() && response.body() != null) {
                            populateProfile(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        // greeting/avatar just stay blank until next load
                        if (!isAdded()) return;
                        SkeletonAnimator.showContent(skeletonNameLine, tvUserName);
                        SkeletonAnimator.stop(tvAvatarInitials);
                    }
                });
    }

    private void populateProfile(Profile profile) {
        String fullName = profile.getUser() != null ? profile.getUser().getFullName() : null;
        String username = profile.getUser() != null ? profile.getUser().getUsername() : null;
        tvUserName.setText(fullName != null ? fullName : "");
        tvAvatarInitials.setText(initialsOf(fullName));

        if (profile.getProfilePhoto() != null && !profile.getProfilePhoto().isEmpty()) {
            ivAvatar.setVisibility(View.VISIBLE);
            Glide.with(this).load(profile.getProfilePhoto()).centerCrop().into(ivAvatar);
        }

        if (fullName != null) {
            ((HomeActivity) requireActivity()).updateDrawerHeader(
                    fullName, username, profile.getProfilePhoto());
        }
    }

    private String initialsOf(@Nullable String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length && initials.length() < 2; i++) {
            if (!parts[i].isEmpty()) initials.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return initials.toString();
    }

    // ── Feature grid ──────────────────────────────────────────────────────

    private void setUpBlock(View root, int iconId, int badgeId, int iconRes, int accentColorRes) {
        ImageView icon = root.findViewById(iconId);
        TextView badge = root.findViewById(badgeId);

        @ColorInt int accent = getResources().getColor(accentColorRes, null);

        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(accent));
        badge.setTextColor(accent);
    }

    private void loadClassroomBadge(TextView badge) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NotificationApiService.class)
                .getNotifications(token, true)
                .enqueue(new Callback<PageResponse<Notification>>() {
                    @Override
                    public void onResponse(Call<PageResponse<Notification>> call, Response<PageResponse<Notification>> response) {
                        if (!isAdded()) return;
                        int count = 0;
                        if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                            for (Notification n : response.body().getResults()) {
                                if (isClassroomType(n.getNotificationType())) count++;
                            }
                        }
                        badge.setText(count > 0
                                ? getString(R.string.home_badge_new, count)
                                : getString(R.string.home_badge_up_to_date));
                    }

                    @Override
                    public void onFailure(Call<PageResponse<Notification>> call, Throwable t) {}
                });
    }

    private void loadCrewBadge(TextView badge) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .getPosts(token, null, "OPEN", null)
                .enqueue(new Callback<PageResponse<Post>>() {
                    @Override
                    public void onResponse(Call<PageResponse<Post>> call, Response<PageResponse<Post>> response) {
                        if (!isAdded()) return;
                        int count = response.isSuccessful() && response.body() != null ? response.body().getCount() : 0;
                        badge.setText(count > 0
                                ? getString(R.string.home_badge_open, count)
                                : getString(R.string.home_badge_none_open));
                    }

                    @Override
                    public void onFailure(Call<PageResponse<Post>> call, Throwable t) {}
                });
    }

    private void loadLostFoundBadge(TextView badge) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(LostFoundApiService.class)
                .getItems(token, null, null, null, null, null, null)
                .enqueue(new Callback<List<LostFoundItem>>() {
                    @Override
                    public void onResponse(Call<List<LostFoundItem>> call, Response<List<LostFoundItem>> response) {
                        if (!isAdded()) return;
                        int count = response.isSuccessful() && response.body() != null ? response.body().size() : 0;
                        badge.setText(count > 0
                                ? getString(R.string.home_badge_open, count)
                                : getString(R.string.home_badge_all_clear));
                    }

                    @Override
                    public void onFailure(Call<List<LostFoundItem>> call, Throwable t) {}
                });
    }

    private void loadRouteMateBadge(TextView badge) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(RouteMateApiService.class)
                .getRoutes(token, null, null, null, null, null, null)
                .enqueue(new Callback<List<Route>>() {
                    @Override
                    public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                        if (!isAdded()) return;
                        int count = response.isSuccessful() && response.body() != null ? response.body().size() : 0;
                        badge.setText(count > 0
                                ? getString(R.string.home_badge_active, count)
                                : getString(R.string.home_badge_none_active));
                    }

                    @Override
                    public void onFailure(Call<List<Route>> call, Throwable t) {}
                });
    }

    // ── Recent activity ──────────────────────────────────────────────────

    private void loadRecentActivity() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NotificationApiService.class)
                .getNotifications(token, null)
                .enqueue(new Callback<PageResponse<Notification>>() {
                    @Override
                    public void onResponse(Call<PageResponse<Notification>> call, Response<PageResponse<Notification>> response) {
                        if (!isAdded()) return;
                        List<Notification> results = response.isSuccessful() && response.body() != null
                                && response.body().getResults() != null
                                ? response.body().getResults() : Collections.emptyList();
                        renderActivity(results.subList(0, Math.min(results.size(), MAX_ACTIVITY_ITEMS)));
                    }

                    @Override
                    public void onFailure(Call<PageResponse<Notification>> call, Throwable t) {
                        if (!isAdded()) return;
                        renderActivity(Collections.emptyList());
                    }
                });
    }

    private void renderActivity(List<Notification> items) {
        activityContainer.removeAllViews();

        if (items.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(getString(R.string.home_activity_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12f);
            activityContainer.addView(empty);
            return;
        }

        for (Notification n : items) {
            View row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_activity, activityContainer, false);

            @ColorInt int accent = getResources().getColor(colorForNotificationType(n.getNotificationType()), null);
            row.findViewById(R.id.dot).setBackgroundTintList(ColorStateList.valueOf(accent));

            ((TextView) row.findViewById(R.id.tvText)).setText(n.getMessage());
            TextView tvTime = row.findViewById(R.id.tvTime);
            tvTime.setText(TimeUtils.getRelativeTime(n.getCreatedAt()));
            tvTime.setTextColor(accent);

            row.setOnClickListener(v -> NotificationNavigator.open(requireContext(), n.getActionUrl()));

            activityContainer.addView(row);
        }
    }

    private boolean isClassroomType(@Nullable String notificationType) {
        return "NOTICE_POSTED".equals(notificationType)
                || "RESOURCE_POSTED".equals(notificationType)
                || "FEED_POST".equals(notificationType);
    }

    @ColorRes
    private int colorForNotificationType(@Nullable String notificationType) {
        if (notificationType == null) return R.color.color_muted;
        switch (notificationType) {
            case "JOIN_REQUEST":
            case "JOIN_REQUEST_RESPONSE":
            case "POST_FULL":
                return R.color.color_purple;
            case "NOTICE_POSTED":
            case "RESOURCE_POSTED":
            case "FEED_POST":
                return R.color.color_cyan;
            case "ROUTE_JOIN_REQUEST":
            case "ROUTE_JOIN_REQUEST_RESPONSE":
                return R.color.color_indigo;
            default:
                return R.color.color_muted;
        }
    }
}
