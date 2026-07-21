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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.ui.ComingSoonActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Profile;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard tab: greeting + feature grid + recent activity. The four grid
 * blocks and the activity feed have no backend yet, so they're driven by
 * static config here — swap in real data sources once those APIs exist.
 */
public class HomeFragment extends Fragment {

    private TokenManager tokenManager;

    private TextView tvGreeting, tvUserName, tvAvatarInitials;
    private ImageView ivAvatar;
    private LinearLayout activityContainer;

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

        tvGreeting.setText(greetingForCurrentTime());

        view.findViewById(R.id.btnMenu).setOnClickListener(v ->
                ((HomeActivity) requireActivity()).openDrawer());

        setUpBlock(view, R.id.blockClassroom, R.id.ivClassroomIcon, R.id.tvClassroomBadge,
                R.drawable.ic_classroom, R.color.color_cyan, "3 new",
                getString(R.string.label_classroom));

        setUpBlock(view, R.id.blockCrew, R.id.ivCrewIcon, R.id.tvCrewBadge,
                R.drawable.ic_crew, R.color.color_purple, "2 online",
                getString(R.string.label_crew));

        setUpBlock(view, R.id.blockLost, R.id.ivLostIcon, R.id.tvLostBadge,
                R.drawable.ic_lost, R.color.color_amber, "All clear",
                getString(R.string.label_lost));

        setUpBlock(view, R.id.blockRoute, R.id.ivRouteIcon, R.id.tvRouteBadge,
                R.drawable.ic_route, R.color.color_indigo, "1 match",
                getString(R.string.label_route));

        renderActivity(mockActivity());
        loadProfile();
    }

    // ── Greeting ──────────────────────────────────────────────────────────

    private String greetingForCurrentTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    // ── Profile (greeting name + avatar) ─────────────────────────────────

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            populateProfile(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        // no-op — greeting/avatar just stay blank until next load
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

    private void setUpBlock(View root, int blockId, int iconId, int badgeId,
                             int iconRes, int accentColorRes, String badgeText, String title) {
        View block = root.findViewById(blockId);
        ImageView icon = root.findViewById(iconId);
        TextView badge = root.findViewById(badgeId);

        @ColorInt int accent = getResources().getColor(accentColorRes, null);

        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(accent));

        badge.setText(badgeText);
        badge.setTextColor(accent);

        block.setOnClickListener(v -> ComingSoonActivity.start(
                requireContext(), title, iconRes, accent));
    }

    // ── Recent activity ──────────────────────────────────────────────────

    private void renderActivity(List<ActivityItem> items) {
        activityContainer.removeAllViews();
        for (ActivityItem item : items) {
            View row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_activity, activityContainer, false);

            @ColorInt int accent = getResources().getColor(item.accentColorRes, null);
            row.findViewById(R.id.dot).setBackgroundTintList(ColorStateList.valueOf(accent));

            ((TextView) row.findViewById(R.id.tvText)).setText(item.text);
            TextView tvTime = row.findViewById(R.id.tvTime);
            tvTime.setText(item.time);
            tvTime.setTextColor(accent);

            activityContainer.addView(row);
        }
    }

    private List<ActivityItem> mockActivity() {
        return java.util.Arrays.asList(
                new ActivityItem("Assignment 2 deadline extended", "2h", R.color.color_cyan),
                new ActivityItem("Sara Khan joined your Crew", "4h", R.color.color_purple),
                new ActivityItem("Wallet found near Library Gate", "6h", R.color.color_amber),
                new ActivityItem("New Route Mate match: Dhanmondi", "8h", R.color.color_indigo)
        );
    }

    private static class ActivityItem {
        final String text;
        final String time;
        final int accentColorRes;

        ActivityItem(String text, String time, int accentColorRes) {
            this.text = text;
            this.time = time;
            this.accentColorRes = accentColorRes;
        }
    }
}
