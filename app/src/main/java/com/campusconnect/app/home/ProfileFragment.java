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
import com.campusconnect.app.profile.edit.AddEducationBottomSheet;
import com.campusconnect.app.profile.edit.AddExperienceBottomSheet;
import com.campusconnect.app.profile.edit.AddProjectBottomSheet;
import com.campusconnect.app.profile.edit.EditBasicInfoBottomSheet;
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

    // ── Hero views ────────────────────────────────────────────────────────────
    private TextView tvFullName, tvUsername, tvUserType, tvAbout;
    private ImageView ivAvatar;
    private View coverBanner;

    // ── Tab views ─────────────────────────────────────────────────────────────
    private TextView tabProjects, tabExperience, tabEducation;
    private TextView tvActiveTabTitle;
    private LinearLayout tabContent;

    // ── Edit / add entry points ───────────────────────────────────────────────
    /** Pencil on the hero card → EditBasicInfoBottomSheet */
    private TextView btnEditHero;
    /** Pencil in the About section header → same sheet */
    private TextView btnEditAbout;
    /** "+" button in the tab sub-header → Add sheet for active tab */
    private TextView btnAddTabItem;

    private Profile currentProfile;
    private String  activeTab = "projects";

    // ── Fragment lifecycle ────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(getContext());

        // bind hero views
        tvFullName   = view.findViewById(R.id.tvFullName);
        tvUsername   = view.findViewById(R.id.tvUsername);
        tvUserType   = view.findViewById(R.id.tvUserType);
        tvAbout      = view.findViewById(R.id.tvAbout);
        ivAvatar     = view.findViewById(R.id.ivAvatar);
        coverBanner  = view.findViewById(R.id.coverBanner);

        // bind tab views
        tabProjects      = view.findViewById(R.id.tabProjects);
        tabExperience    = view.findViewById(R.id.tabExperience);
        tabEducation     = view.findViewById(R.id.tabEducation);
        tvActiveTabTitle = view.findViewById(R.id.tvActiveTabTitle);
        tabContent       = view.findViewById(R.id.tabContent);

        // bind edit / add buttons
        btnEditHero    = view.findViewById(R.id.btnEditHero);
        btnEditAbout   = view.findViewById(R.id.btnEditAbout);
        btnAddTabItem  = view.findViewById(R.id.btnAddTabItem);

        // gradient cover banner (same as before)
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF06B6D4, 0xFF3B82F6, 0xFF8B5CF6}
        );
        coverBanner.setBackground(gradient);

        // ── Logout ────────────────────────────────────────────────────────────
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            tokenManager.clearTokens();
            android.content.Intent intent = new android.content.Intent(
                    getActivity(),
                    com.campusconnect.app.auth.login.LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // ── Pencil buttons → EditBasicInfoBottomSheet ─────────────────────────
        View.OnClickListener openEditInfo = v -> openEditBasicInfo();
        btnEditHero.setOnClickListener(openEditInfo);
        btnEditAbout.setOnClickListener(openEditInfo);

        // ── "+" button → add sheet for the active tab ─────────────────────────
        btnAddTabItem.setOnClickListener(v -> openAddSheet());

        // ── Tabs ──────────────────────────────────────────────────────────────
        tabProjects.setOnClickListener(v   -> switchTab("projects"));
        tabExperience.setOnClickListener(v -> switchTab("experience"));
        tabEducation.setOnClickListener(v  -> switchTab("education"));

        loadProfile();
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call,
                                           Response<Profile> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            populateProfile(currentProfile);
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        // no-op — existing behaviour
                    }
                });
    }

    private void populateProfile(Profile profile) {
        if (profile.getUser() != null) {
            tvFullName.setText(profile.getUser().getFullName());
            tvUsername.setText("@" + profile.getUser().getUsername());
        }

        tvUserType.setText(
                profile.getUserType() != null ? profile.getUserType() : "STUDENT");

        if (profile.getAbout() != null && !profile.getAbout().isEmpty()) {
            tvAbout.setText(profile.getAbout());
        } else if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            tvAbout.setText(profile.getBio());
        } else {
            tvAbout.setText(getString(R.string.no_bio));
        }

        if (profile.getProfilePhoto() != null
                && !profile.getProfilePhoto().isEmpty()) {
            Glide.with(this)
                    .load(profile.getProfilePhoto())
                    .centerCrop()
                    .into(ivAvatar);
        }

        switchTab(activeTab);
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    private void switchTab(String tab) {
        activeTab = tab;

        // reset all
        for (TextView t : new TextView[]{tabProjects, tabExperience, tabEducation}) {
            t.setBackgroundResource(0);
            t.setTextColor(getResources().getColor(R.color.color_muted, null));
        }

        // activate selected
        TextView active = tab.equals("projects")   ? tabProjects  :
                tab.equals("experience")  ? tabExperience : tabEducation;
        active.setBackgroundResource(R.drawable.bg_tab_active);
        active.setTextColor(getResources().getColor(R.color.color_cyan, null));

        // update the sub-header title
        String label = tab.substring(0, 1).toUpperCase() + tab.substring(1);
        if (tvActiveTabTitle != null) tvActiveTabTitle.setText(label);

        // render content
        tabContent.removeAllViews();
        if (currentProfile == null) return;

        switch (tab) {
            case "projects":   renderProjects(currentProfile.getProjects());     break;
            case "experience": renderExperience(currentProfile.getExperience()); break;
            case "education":  renderEducation(currentProfile.getEducation());   break;
        }
    }

    // ── Bottom sheet launchers ────────────────────────────────────────────────

    /**
     * Opens EditBasicInfoBottomSheet. After a successful save the sheet calls
     * back and we reload the profile so the hero card reflects the new data.
     */
    private void openEditBasicInfo() {
        EditBasicInfoBottomSheet sheet = new EditBasicInfoBottomSheet();
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "edit_basic_info");
    }

    /**
     * Opens the correct Add sheet based on which tab is currently active.
     */
    private void openAddSheet() {
        switch (activeTab) {
            case "projects":
                AddProjectBottomSheet projectSheet = new AddProjectBottomSheet();
                projectSheet.setOnSavedListener(this::loadProfile);
                projectSheet.show(getChildFragmentManager(), "add_project");
                break;

            case "experience":
                AddExperienceBottomSheet expSheet = new AddExperienceBottomSheet();
                expSheet.setOnSavedListener(this::loadProfile);
                expSheet.show(getChildFragmentManager(), "add_experience");
                break;

            case "education":
                AddEducationBottomSheet eduSheet = new AddEducationBottomSheet();
                eduSheet.setOnSavedListener(this::loadProfile);
                eduSheet.show(getChildFragmentManager(), "add_education");
                break;
        }
    }

    // ── Tab content renderers (unchanged from original) ───────────────────────

    private void renderProjects(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            showEmptyState("No projects added yet.");
            return;
        }
        for (Project p : projects) {
            View card = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_project, tabContent, false);
            ((TextView) card.findViewById(R.id.tvName))
                    .setText(p.getName());
            ((TextView) card.findViewById(R.id.tvAssociated))
                    .setText(p.getAssociatedWith() != null ? p.getAssociatedWith() : "");
            ((TextView) card.findViewById(R.id.tvDescription))
                    .setText(p.getDescription() != null ? p.getDescription() : "");
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
            ((TextView) card.findViewById(R.id.tvTitle))
                    .setText(e.getTitle());
            ((TextView) card.findViewById(R.id.tvOrganization))
                    .setText(e.getOrganization());
            ((TextView) card.findViewById(R.id.tvDescription))
                    .setText(e.getDescription() != null ? e.getDescription() : "");
            String dates = e.getStartDate() + " — "
                    + (e.getEndDate() != null ? e.getEndDate() : "Present");
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
            ((TextView) card.findViewById(R.id.tvInstitution))
                    .setText(e.getInstitutionName());
            ((TextView) card.findViewById(R.id.tvDegree))
                    .setText(e.getDegree());
            String years = e.getStartYear() + " — "
                    + (e.getEndYear() != null ? e.getEndYear() : "Present");
            ((TextView) card.findViewById(R.id.tvYears)).setText(years);
            tabContent.addView(card);
        }
    }

    private void showEmptyState(String message) {
        TextView empty = new TextView(getContext());
        empty.setText(message);
        empty.setTextColor(
                getResources().getColor(R.color.color_muted, null));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 48, 0, 48);
        tabContent.addView(empty);
    }
}