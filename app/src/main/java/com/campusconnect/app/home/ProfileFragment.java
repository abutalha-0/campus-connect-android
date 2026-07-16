package com.campusconnect.app.home;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.ImageUtils;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.edit.AddEducationBottomSheet;
import com.campusconnect.app.profile.edit.AddExperienceBottomSheet;
import com.campusconnect.app.profile.edit.AddLinkBottomSheet;
import com.campusconnect.app.profile.edit.AddProjectBottomSheet;
import com.campusconnect.app.profile.edit.AddSkillBottomSheet;
import com.campusconnect.app.profile.edit.EditBasicInfoBottomSheet;
import com.campusconnect.app.profile.edit.ProfileChipFactory;
import com.campusconnect.app.profile.edit.SocialPlatform;
import com.campusconnect.app.profile.models.Education;
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.Link;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.Project;
import com.campusconnect.app.profile.models.UserSkill;
import com.google.android.material.chip.ChipGroup;
import java.io.File;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";

    /** No-arg constructor (used by the bottom nav) means "view my own profile". */
    public static ProfileFragment newInstance(int userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    private int viewingUserId = -1;
    private boolean isOwnProfile = true;

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
    private TextView btnEditHero;
    private TextView btnEditAbout;
    private TextView btnAddTabItem;
    private TextView btnChangePhoto;
    private TextView btnEditSkills;
    private ChipGroup skillsChipGroup;
    private TextView btnAddSocial;
    private ChipGroup socialsChipGroup;

    private Profile currentProfile;
    private String  activeTab = "projects";

    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    // ── Fragment lifecycle ────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        viewingUserId = args != null ? args.getInt(ARG_USER_ID, -1) : -1;
        isOwnProfile = viewingUserId == -1;

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) onPhotoPicked(uri);
                }
        );
    }

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

        tvFullName   = view.findViewById(R.id.tvFullName);
        tvUsername   = view.findViewById(R.id.tvUsername);
        tvUserType   = view.findViewById(R.id.tvUserType);
        tvAbout      = view.findViewById(R.id.tvAbout);
        ivAvatar     = view.findViewById(R.id.ivAvatar);
        coverBanner  = view.findViewById(R.id.coverBanner);

        tabProjects      = view.findViewById(R.id.tabProjects);
        tabExperience    = view.findViewById(R.id.tabExperience);
        tabEducation     = view.findViewById(R.id.tabEducation);
        tvActiveTabTitle = view.findViewById(R.id.tvActiveTabTitle);
        tabContent       = view.findViewById(R.id.tabContent);

        btnEditHero     = view.findViewById(R.id.btnEditHero);
        btnEditAbout    = view.findViewById(R.id.btnEditAbout);
        btnAddTabItem   = view.findViewById(R.id.btnAddTabItem);
        btnChangePhoto  = view.findViewById(R.id.btnChangePhoto);
        btnEditSkills   = view.findViewById(R.id.btnEditSkills);
        skillsChipGroup = view.findViewById(R.id.skillsChipGroup);
        btnAddSocial     = view.findViewById(R.id.btnAddSocial);
        socialsChipGroup = view.findViewById(R.id.socialsChipGroup);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF06B6D4, 0xFF3B82F6, 0xFF8B5CF6}
        );
        coverBanner.setBackground(gradient);

        if (isOwnProfile) {
            // Only ever hosted by HomeActivity's bottom nav in this case,
            // so the hamburger can safely open its drawer.
            view.findViewById(R.id.btnMenu).setOnClickListener(v ->
                    ((HomeActivity) requireActivity()).openDrawer());

            btnEditHero.setOnClickListener(v -> openEditUserType());
            btnEditAbout.setOnClickListener(v -> openEditAbout());
            btnAddTabItem.setOnClickListener(v -> openAddSheet());
            btnChangePhoto.setOnClickListener(v -> openPhotoPicker());
            btnEditSkills.setOnClickListener(v -> openManageSkills());
            btnAddSocial.setOnClickListener(v -> openManageLinks());
        } else {
            // Hosted by PublicProfileActivity, which already has its own
            // back-button header — hide this one entirely.
            view.findViewById(R.id.topBar).setVisibility(View.GONE);

            // Read-only: viewing someone else's profile, so none of the
            // edit/add entry points apply.
            btnEditHero.setVisibility(View.GONE);
            btnEditAbout.setVisibility(View.GONE);
            btnAddTabItem.setVisibility(View.GONE);
            btnChangePhoto.setVisibility(View.GONE);
            btnEditSkills.setVisibility(View.GONE);
            btnAddSocial.setVisibility(View.GONE);
        }

        tabProjects.setOnClickListener(v   -> switchTab("projects"));
        tabExperience.setOnClickListener(v -> switchTab("experience"));
        tabEducation.setOnClickListener(v  -> switchTab("education"));

        loadProfile();
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        ProfileApiService service = RetrofitClient.createService(ProfileApiService.class);
        Call<Profile> call = isOwnProfile
                ? service.getMyProfile(token)
                : service.getPublicProfile(token, viewingUserId);

        call.enqueue(new Callback<Profile>() {
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
                        // no-op
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

        renderSkills(profile.getSkills());
        renderSocials(profile.getLinks());
        switchTab(activeTab);
    }

    // ── Skills ────────────────────────────────────────────────────────────────

    private void renderSkills(@Nullable List<UserSkill> skills) {
        skillsChipGroup.removeAllViews();
        if (skills == null || skills.isEmpty()) return;
        for (UserSkill skill : skills) {
            if (skill.getSkill() == null) continue;
            skillsChipGroup.addView(
                    ProfileChipFactory.create(requireContext(), skill.getSkill().getName()));
        }
    }

    private void openManageSkills() {
        AddSkillBottomSheet sheet = new AddSkillBottomSheet();
        sheet.setSkills(currentProfile != null ? currentProfile.getSkills() : null);
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "manage_skills");
    }

    // ── Connect & Network ────────────────────────────────────────────────────

    private void renderSocials(@Nullable List<Link> links) {
        socialsChipGroup.removeAllViews();
        if (links == null || links.isEmpty()) return;
        for (Link link : links) {
            SocialPlatform platform = SocialPlatform.fromKey(link.getIcon());
            String label = link.getLinkName() != null ? link.getLinkName() : platform.label;
            com.google.android.material.chip.Chip chip = ProfileChipFactory.create(
                    requireContext(), label, platform.iconRes, platform.accentColor);
            chip.setOnClickListener(v -> openUrl(link.getUrl()));
            socialsChipGroup.addView(chip);
        }
    }

    private void openUrl(@Nullable String url) {
        if (url == null || url.isEmpty() || getContext() == null) return;
        try {
            startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Couldn't open that link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openManageLinks() {
        AddLinkBottomSheet sheet = new AddLinkBottomSheet();
        sheet.setLinks(currentProfile != null ? currentProfile.getLinks() : null);
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "manage_links");
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    private void switchTab(String tab) {
        activeTab = tab;

        for (TextView t : new TextView[]{tabProjects, tabExperience, tabEducation}) {
            t.setBackgroundResource(R.drawable.bg_tab_pill_inactive);
            t.setTextColor(getResources().getColor(R.color.color_muted, null));
        }

        TextView active = tab.equals("projects")   ? tabProjects  :
                tab.equals("experience")  ? tabExperience : tabEducation;
        active.setBackgroundResource(R.drawable.bg_tab_pill_active);
        active.setTextColor(getResources().getColor(R.color.color_cyan, null));

        String label = tab.substring(0, 1).toUpperCase() + tab.substring(1);
        if (tvActiveTabTitle != null) tvActiveTabTitle.setText(label);

        tabContent.removeAllViews();
        if (currentProfile == null) return;

        switch (tab) {
            case "projects":   renderProjects(currentProfile.getProjects());     break;
            case "experience": renderExperience(currentProfile.getExperience()); break;
            case "education":  renderEducation(currentProfile.getEducation());   break;
        }
    }

    // ── Bottom sheet launchers ────────────────────────────────────────────────

    private void openEditUserType() {
        EditBasicInfoBottomSheet sheet = new EditBasicInfoBottomSheet();
        sheet.setMode(EditBasicInfoBottomSheet.Mode.HERO);
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "edit_user_type");
    }

    private void openEditAbout() {
        EditBasicInfoBottomSheet sheet = new EditBasicInfoBottomSheet();
        sheet.setMode(EditBasicInfoBottomSheet.Mode.ABOUT);
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "edit_about");
    }

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

    // ── Profile photo flow ───────────────────────────────────────────────────

    private void openPhotoPicker() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void onPhotoPicked(Uri uri) {
        Glide.with(this).load(uri).centerCrop().into(ivAvatar);
        Toast.makeText(getContext(), "Uploading photo…", Toast.LENGTH_SHORT).show();
        uploadProfilePhoto(uri);
    }

    private void uploadProfilePhoto(Uri uri) {
        if (getContext() == null) return;

        File compressedFile;
        try {
            compressedFile = ImageUtils.compressImageFromUri(getContext(), uri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Couldn't read that image. Try another.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody fileBody = RequestBody.create(
                MediaType.parse("image/jpeg"), compressedFile);

        MultipartBody.Part photoPart = MultipartBody.Part.createFormData(
                "profile_photo", compressedFile.getName(), fileBody);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RetrofitClient.createService(ProfileApiService.class)
                .updateProfilePhoto(token, photoPart)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            if (currentProfile.getProfilePhoto() != null) {
                                Glide.with(ProfileFragment.this)
                                        .load(currentProfile.getProfilePhoto())
                                        .centerCrop()
                                        .into(ivAvatar);
                            }
                            Toast.makeText(getContext(), "Profile photo updated!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Upload failed. Try again.", Toast.LENGTH_SHORT).show();
                            loadProfile();
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                        loadProfile();
                    }
                });
    }

    // ── Delete flow ───────────────────────────────────────────────────────────

    /**
     * Shows a simple Yes/No confirmation dialog before deleting any
     * project / experience / education entry. `itemLabel` is shown in
     * the dialog message so the user knows exactly what they're removing
     * (e.g. "Delete \"Campus Connect\"?").
     */
    private void confirmDelete(String itemTypeLabel, String itemName, Runnable onConfirmed) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete " + itemTypeLabel + "?")
                .setMessage("Remove \"" + itemName + "\" from your profile? "
                        + "This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProject(Project project) {
        confirmDelete("Project", project.getName(), () -> {
            String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
            RetrofitClient.createService(ProfileApiService.class)
                    .deleteProject(token, project.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Project deleted",
                                        Toast.LENGTH_SHORT).show();
                                loadProfile();
                            } else {
                                Toast.makeText(getContext(), "Couldn't delete. Try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), getString(R.string.error_network),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void deleteExperience(Experience experience) {
        confirmDelete("Experience", experience.getTitle(), () -> {
            String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
            RetrofitClient.createService(ProfileApiService.class)
                    .deleteExperience(token, experience.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Experience deleted",
                                        Toast.LENGTH_SHORT).show();
                                loadProfile();
                            } else {
                                Toast.makeText(getContext(), "Couldn't delete. Try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), getString(R.string.error_network),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void deleteEducation(Education education) {
        confirmDelete("Education", education.getInstitutionName(), () -> {
            String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
            RetrofitClient.createService(ProfileApiService.class)
                    .deleteEducation(token, education.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Education deleted",
                                        Toast.LENGTH_SHORT).show();
                                loadProfile();
                            } else {
                                Toast.makeText(getContext(), "Couldn't delete. Try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), getString(R.string.error_network),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // ── Tab content renderers ────────────────────────────────────────────────

    private void renderProjects(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            showEmptyState("No projects added yet.");
            return;
        }
        for (Project p : projects) {
            bindEntry(p.getName(), p.getAssociatedWith(), yearOf(p.getCreatedAt()),
                    p.getDescription(),
                    () -> openEditProjectSheet(p),
                    () -> deleteProject(p));
        }
    }

    private void renderExperience(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            showEmptyState("No experience added yet.");
            return;
        }
        for (Experience e : experiences) {
            String dates = e.getStartDate() + " — "
                    + (e.getEndDate() != null ? e.getEndDate() : "Present");
            bindEntry(e.getTitle(), e.getOrganization(), dates, e.getDescription(),
                    () -> openEditExperienceSheet(e),
                    () -> deleteExperience(e));
        }
    }

    private void renderEducation(List<Education> educationList) {
        if (educationList == null || educationList.isEmpty()) {
            showEmptyState("No education added yet.");
            return;
        }
        for (Education e : educationList) {
            String years = e.getStartYear() + " — "
                    + (e.getEndYear() != null ? e.getEndYear() : "Present");
            bindEntry(e.getDegree(), e.getInstitutionName(), years, null,
                    () -> openEditEducationSheet(e),
                    () -> deleteEducation(e));
        }
    }

    /** Inflates the shared title/subtitle/meta/description card used by all three tabs. */
    private void bindEntry(String title, @Nullable String subtitle, @Nullable String meta,
                            @Nullable String description, Runnable onEdit, Runnable onDelete) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_profile_entry, tabContent, false);

        ((TextView) card.findViewById(R.id.tvTitle)).setText(title);
        setOrHide(card.findViewById(R.id.tvSubtitle), subtitle);
        setOrHide(card.findViewById(R.id.tvMeta), meta);
        setOrHide(card.findViewById(R.id.tvDescription), description);

        View btnEdit = card.findViewById(R.id.btnEdit);
        View btnDelete = card.findViewById(R.id.btnDelete);
        if (isOwnProfile) {
            btnEdit.setOnClickListener(v -> onEdit.run());
            btnDelete.setOnClickListener(v -> onDelete.run());
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        tabContent.addView(card);
    }

    private void setOrHide(TextView view, @Nullable String text) {
        if (text == null || text.isEmpty()) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        }
    }

    /** Pulls the leading "YYYY" out of an ISO date string, or "" if it doesn't look like one. */
    private String yearOf(@Nullable String isoDate) {
        if (isoDate != null && isoDate.length() >= 4) {
            return isoDate.substring(0, 4);
        }
        return "";
    }

    // ── Edit-in-place launchers ───────────────────────────────────────────────

    private void openEditProjectSheet(Project project) {
        AddProjectBottomSheet sheet = new AddProjectBottomSheet();
        sheet.setEditing(project);
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "edit_project");
    }

    private void openEditExperienceSheet(Experience experience) {
        AddExperienceBottomSheet sheet = new AddExperienceBottomSheet();
        sheet.setEditing(experience);
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "edit_experience");
    }

    private void openEditEducationSheet(Education education) {
        AddEducationBottomSheet sheet = new AddEducationBottomSheet();
        sheet.setEditing(education);
        sheet.setOnSavedListener(this::loadProfile);
        sheet.show(getChildFragmentManager(), "edit_education");
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