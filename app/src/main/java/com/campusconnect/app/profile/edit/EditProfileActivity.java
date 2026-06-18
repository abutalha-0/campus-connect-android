package com.campusconnect.app.profile.edit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Education;
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.Link;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.Project;
import com.campusconnect.app.profile.models.UserSkill;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends BaseActivity {

    private LinearLayout layoutProjects, layoutEducation,
            layoutExperience, layoutSkills, layoutLinks;
    private TextView tvBasicInfoPreview;
    private Profile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // bind views
        layoutProjects = findViewById(R.id.layoutProjects);
        layoutEducation = findViewById(R.id.layoutEducation);
        layoutExperience = findViewById(R.id.layoutExperience);
        layoutSkills = findViewById(R.id.layoutSkills);
        layoutLinks = findViewById(R.id.layoutLinks);
        tvBasicInfoPreview = findViewById(R.id.tvBasicInfoPreview);

        // back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // section navigation
        findViewById(R.id.btnEditBasicInfo).setOnClickListener(v ->
                startActivity(new Intent(this, EditBasicInfoActivity.class)));

        findViewById(R.id.btnEditPhoto).setOnClickListener(v ->
                startActivity(new Intent(this, EditPhotoActivity.class)));

        findViewById(R.id.btnAddProject).setOnClickListener(v ->
                startActivity(new Intent(this, AddProjectActivity.class)));

        findViewById(R.id.btnAddEducation).setOnClickListener(v ->
                startActivity(new Intent(this, AddEducationActivity.class)));

        findViewById(R.id.btnAddExperience).setOnClickListener(v ->
                startActivity(new Intent(this, AddExperienceActivity.class)));

        findViewById(R.id.btnAddSkill).setOnClickListener(v ->
                startActivity(new Intent(this, AddSkillActivity.class)));

        findViewById(R.id.btnAddLink).setOnClickListener(v ->
                startActivity(new Intent(this, AddLinkActivity.class)));

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload every time user returns from an edit screen
        loadProfile();
    }

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            populateSections(currentProfile);
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this,
                                getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateSections(Profile profile) {
        // basic info preview
        String bio = profile.getBio() != null && !profile.getBio().isEmpty()
                ? profile.getBio() : "No bio yet";
        tvBasicInfoPreview.setText(bio);

        // projects
        layoutProjects.removeAllViews();
        if (profile.getProjects() != null) {
            for (Project p : profile.getProjects()) {
                addEditableItem(layoutProjects, p.getName(),
                        "project", p.getId());
            }
        }

        // education
        layoutEducation.removeAllViews();
        if (profile.getEducation() != null) {
            for (Education e : profile.getEducation()) {
                addEditableItem(layoutEducation, e.getInstitutionName(),
                        "education", e.getId());
            }
        }

        // experience
        layoutExperience.removeAllViews();
        if (profile.getExperience() != null) {
            for (Experience e : profile.getExperience()) {
                addEditableItem(layoutExperience, e.getTitle(),
                        "experience", e.getId());
            }
        }

        // skills
        layoutSkills.removeAllViews();
        if (profile.getSkills() != null) {
            for (UserSkill s : profile.getSkills()) {
                addEditableItem(layoutSkills, s.getSkill().getName(),
                        "skill", s.getId());
            }
        }

        // links
        layoutLinks.removeAllViews();
        if (profile.getLinks() != null) {
            for (Link l : profile.getLinks()) {
                addEditableItem(layoutLinks, l.getLinkName(),
                        "link", l.getId());
            }
        }
    }

    private void addEditableItem(LinearLayout parent,
                                 String title, String type, int id) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        LinearLayout.LayoutParams tvParams =
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        TextView tvTitle = new TextView(this);
        tvTitle.setLayoutParams(tvParams);
        tvTitle.setText(title);
        tvTitle.setTextColor(getResources().getColor(R.color.color_text_body, null));
        tvTitle.setTextSize(13f);

        TextView btnDelete = new TextView(this);
        btnDelete.setText("✕");
        btnDelete.setTextColor(getResources().getColor(R.color.color_red, null));
        btnDelete.setTextSize(14f);
        btnDelete.setPadding(16, 0, 0, 0);
        btnDelete.setOnClickListener(v -> deleteItem(type, id));

        row.addView(tvTitle);
        row.addView(btnDelete);
        parent.addView(row);
    }


    private void deleteItem(String type, int id) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        ProfileApiService service = RetrofitClient.createService(ProfileApiService.class);

        Call<Void> call = null;

        switch (type) {
            case "project":
                call = service.deleteProject(token, id); break;
            case "education":
                call = service.deleteEducation(token, id); break;
            case "experience":
                call = service.deleteExperience(token, id); break;
            case "skill":
                call = service.deleteSkill(token, id); break;
            case "link":
                call = service.deleteLink(token, id); break;
        }

        if (call == null) return;

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this,
                            "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadProfile();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this,
                        getString(R.string.error_network),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}