package com.campusconnect.app.home;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Education;
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.Project;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TokenManager tokenManager;
    private TextView tvFullName, tvUsername, tvUserType, tvAbout;
    private TextView tabProjects, tabExperience, tabEducation;
    private LinearLayout tabContent;
    private ImageView ivAvatar;
    private View coverBanner;

    private Profile currentProfile;
    private String activeTab = "projects";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(getContext());

        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUserType = view.findViewById(R.id.tvUserType);
        tvAbout = view.findViewById(R.id.tvAbout);
        tabProjects = view.findViewById(R.id.tabProjects);
        tabExperience = view.findViewById(R.id.tabExperience);
        tabEducation = view.findViewById(R.id.tabEducation);
        tabContent = view.findViewById(R.id.tabContent);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        coverBanner = view.findViewById(R.id.coverBanner);

        // gradient cover banner
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF06B6D4, 0xFF3B82F6, 0xFF8B5CF6}
        );
        coverBanner.setBackground(gradient);

        // logout button
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            tokenManager.clearTokens();
            android.content.Intent intent = new android.content.Intent(
                    getActivity(), com.campusconnect.app.auth.login.LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK |
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // tab click listeners
        tabProjects.setOnClickListener(v -> switchTab("projects"));
        tabExperience.setOnClickListener(v -> switchTab("experience"));
        tabEducation.setOnClickListener(v -> switchTab("education"));

        loadProfile();
    }

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            populateProfile(currentProfile);
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        if (!isAdded()) return;
                    }
                });
    }

    private void populateProfile(Profile profile) {
        if (profile.getUser() != null) {
            tvFullName.setText(profile.getUser().getFullName());
            tvUsername.setText("@" + profile.getUser().getUsername());
        }

        tvUserType.setText(profile.getUserType() != null ? profile.getUserType() : "STUDENT");

        if (profile.getAbout() != null && !profile.getAbout().isEmpty()) {
            tvAbout.setText(profile.getAbout());
        } else if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            tvAbout.setText(profile.getBio());
        } else {
            tvAbout.setText(getString(R.string.no_bio));
        }

        if (profile.getProfilePhoto() != null && !profile.getProfilePhoto().isEmpty()) {
            Glide.with(this)
                    .load(profile.getProfilePhoto())
                    .centerCrop()
                    .into(ivAvatar);
        }

        switchTab(activeTab);
    }

    private void switchTab(String tab) {
        activeTab = tab;

        // reset all tabs
        tabProjects.setBackgroundResource(0);
        tabExperience.setBackgroundResource(0);
        tabEducation.setBackgroundResource(0);
        tabProjects.setTextColor(getResources().getColor(R.color.color_muted, null));
        tabExperience.setTextColor(getResources().getColor(R.color.color_muted, null));
        tabEducation.setTextColor(getResources().getColor(R.color.color_muted, null));

        // activate selected tab
        TextView activeTabView = tab.equals("projects") ? tabProjects :
                tab.equals("experience") ? tabExperience : tabEducation;
        activeTabView.setBackgroundResource(R.drawable.bg_tab_active);
        activeTabView.setTextColor(getResources().getColor(R.color.color_cyan, null));

        // render content
        tabContent.removeAllViews();

        if (currentProfile == null) return;

        switch (tab) {
            case "projects":
                renderProjects(currentProfile.getProjects());
                break;
            case "experience":
                renderExperience(currentProfile.getExperience());
                break;
            case "education":
                renderEducation(currentProfile.getEducation());
                break;
        }
    }

    private void renderProjects(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            showEmptyState("No projects added yet.");
            return;
        }
        for (Project p : projects) {
            View card = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_project, tabContent, false);
            ((TextView) card.findViewById(R.id.tvName)).setText(p.getName());
            ((TextView) card.findViewById(R.id.tvAssociated)).setText(
                    p.getAssociatedWith() != null ? p.getAssociatedWith() : "");
            ((TextView) card.findViewById(R.id.tvDescription)).setText(
                    p.getDescription() != null ? p.getDescription() : "");
            tabContent.addView(card);
        }
    }

    private void renderExperience(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            showEmptyState("No experience added yet.");
            return;
        }
        for (Experience e : experiences) {
            View card = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_experience, tabContent, false);
            ((TextView) card.findViewById(R.id.tvTitle)).setText(e.getTitle());
            ((TextView) card.findViewById(R.id.tvOrganization)).setText(e.getOrganization());
            ((TextView) card.findViewById(R.id.tvDescription)).setText(
                    e.getDescription() != null ? e.getDescription() : "");
            String dates = e.getStartDate() + " — " +
                    (e.getEndDate() != null ? e.getEndDate() : "Present");
            ((TextView) card.findViewById(R.id.tvDates)).setText(dates);
            tabContent.addView(card);
        }
    }

    private void renderEducation(List<Education> educationList) {
        if (educationList == null || educationList.isEmpty()) {
            showEmptyState("No education added yet.");
            return;
        }
        for (Education e : educationList) {
            View card = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_education, tabContent, false);
            ((TextView) card.findViewById(R.id.tvInstitution)).setText(e.getInstitutionName());
            ((TextView) card.findViewById(R.id.tvDegree)).setText(e.getDegree());
            String years = e.getStartYear() + " — " +
                    (e.getEndYear() != null ? e.getEndYear() : "Present");
            ((TextView) card.findViewById(R.id.tvYears)).setText(years);
            tabContent.addView(card);
        }
    }

    private void showEmptyState(String message) {
        TextView empty = new TextView(getContext());
        empty.setText(message);
        empty.setTextColor(getResources().getColor(R.color.color_muted, null));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 48, 0, 48);
        tabContent.addView(empty);
    }
}