package com.campusconnect.app.profile;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.profile.models.Education;
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.Project;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class PublicProfileActivity extends BaseActivity {

    private TextView tvFullName, tvUsername, tvUserType, tvAbout;
    private TextView tabProjects, tabExperience, tabEducation;
    private LinearLayout tabContent;
    private ImageView ivAvatar;
    private View coverBanner;

    private Profile currentProfile;
    private String activeTab = "projects";
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        // get user id passed from DiscoverFragment
        userId = getIntent().getIntExtra("user_id", -1);

        // bind views
        tvFullName = findViewById(R.id.tvFullName);
        tvUsername = findViewById(R.id.tvUsername);
        tvUserType = findViewById(R.id.tvUserType);
        tvAbout = findViewById(R.id.tvAbout);
        tabProjects = findViewById(R.id.tabProjects);
        tabExperience = findViewById(R.id.tabExperience);
        tabEducation = findViewById(R.id.tabEducation);
        tabContent = findViewById(R.id.tabContent);
        ivAvatar = findViewById(R.id.ivAvatar);
        coverBanner = findViewById(R.id.coverBanner);

        // avatar rounded corners
        ivAvatar.setOutlineProvider(android.view.ViewOutlineProvider.BACKGROUND);
        ivAvatar.setClipToOutline(true);

        // gradient cover
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF06B6D4, 0xFF3B82F6, 0xFF8B5CF6}
        );
        coverBanner.setBackground(gradient);

        // back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // tab listeners
        tabProjects.setOnClickListener(v -> switchTab("projects"));
        tabExperience.setOnClickListener(v -> switchTab("experience"));
        tabEducation.setOnClickListener(v -> switchTab("education"));

        if (userId != -1) {
            loadProfile();
        }
    }

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RetrofitClient.createService(ProfileApiService.class)
                .getPublicProfile(token, userId)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            populateProfile(currentProfile);
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                    }
                });
    }

    private void populateProfile(Profile profile) {
        if (profile.getUser() != null) {
            tvFullName.setText(profile.getUser().getFullName());
            tvUsername.setText("@" + profile.getUser().getUsername());
        }

        tvUserType.setText(profile.getUserType() != null
                ? profile.getUserType() : "STUDENT");

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
            View card = LayoutInflater.from(this)
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
            View card = LayoutInflater.from(this)
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
            View card = LayoutInflater.from(this)
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
        TextView empty = new TextView(this);
        empty.setText(message);
        empty.setTextColor(getResources().getColor(R.color.color_muted, null));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 48, 0, 48);
        tabContent.addView(empty);
    }
}