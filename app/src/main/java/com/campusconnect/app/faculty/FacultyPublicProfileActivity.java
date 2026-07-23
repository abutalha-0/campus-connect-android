package com.campusconnect.app.faculty;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.faculty.model.FacultyLink;
import com.campusconnect.app.faculty.model.FacultyPublicProfile;
import com.campusconnect.app.faculty.util.Designations;
import com.campusconnect.app.profile.edit.ProfileChipFactory;
import com.campusconnect.app.profile.edit.SocialPlatform;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Read-only public profile for a faculty member — identity, contact, and subjects taught. */
public class FacultyPublicProfileActivity extends BaseActivity {

    private static final String EXTRA_USER_ID = "user_id";

    public static void start(Context ctx, int userId) {
        Intent i = new Intent(ctx, FacultyPublicProfileActivity.class);
        i.putExtra(EXTRA_USER_ID, userId);
        ctx.startActivity(i);
    }

    private int userId;
    private TextView tvName, tvDesignation, tvEmail, tvAvatarInitials;
    private ImageView ivAvatar;
    private ChipGroup subjectsChipGroup;
    private LinearLayout linksContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_public_profile);

        userId = getIntent().getIntExtra(EXTRA_USER_ID, -1);

        tvName = findViewById(R.id.tvName);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvEmail = findViewById(R.id.tvEmail);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        ivAvatar = findViewById(R.id.ivAvatar);
        subjectsChipGroup = findViewById(R.id.subjectsChipGroup);
        linksContainer = findViewById(R.id.linksContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnMessage).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.faculty_message_coming_soon),
                        Toast.LENGTH_SHORT).show());

        loadProfile();
    }

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FacultyApiService.class)
                .getPublicProfile(token, userId)
                .enqueue(new Callback<FacultyPublicProfile>() {
                    @Override
                    public void onResponse(Call<FacultyPublicProfile> call,
                                           Response<FacultyPublicProfile> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            populate(response.body());
                        } else {
                            Toast.makeText(FacultyPublicProfileActivity.this,
                                    "Couldn't load this profile.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FacultyPublicProfile> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FacultyPublicProfileActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populate(FacultyPublicProfile profile) {
        String name = profile.getUser() != null ? profile.getUser().getFullName() : "";
        tvName.setText(name);
        tvAvatarInitials.setText(initialsOf(name));

        String designationLabel = Designations.labelFor(this, profile.getDesignation());
        String dept = profile.getDepartment();
        tvDesignation.setText(dept != null && !dept.isEmpty()
                ? designationLabel + ", " + dept
                : designationLabel);

        if (profile.getUser() != null) {
            tvEmail.setText(profile.getUser().getEmail());
        }

        if (profile.getProfilePhoto() != null && !profile.getProfilePhoto().isEmpty()) {
            ivAvatar.setVisibility(View.VISIBLE);
            Glide.with(this).load(profile.getProfilePhoto()).centerCrop().into(ivAvatar);
        }

        renderSubjects(profile.getSubjects());
        renderLinks(profile.getLinks());
    }

    private void renderSubjects(List<FacultyPublicProfile.SubjectSummary> subjects) {
        subjectsChipGroup.removeAllViews();
        if (subjects == null || subjects.isEmpty()) return;
        for (FacultyPublicProfile.SubjectSummary s : subjects) {
            subjectsChipGroup.addView(ProfileChipFactory.create(this, s.getName()));
        }
    }

    private void renderLinks(List<FacultyLink> links) {
        linksContainer.removeAllViews();
        if (links == null) return;
        for (FacultyLink link : links) {
            SocialPlatform platform = SocialPlatform.fromKey(link.getIcon());
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_faculty_link, linksContainer, false);
            ImageView icon = row.findViewById(R.id.ivLinkIcon);
            icon.setImageResource(platform.iconRes);
            icon.setColorFilter(platform.accentColor);
            ((TextView) row.findViewById(R.id.tvLinkUrl)).setText(link.getUrl());
            row.setOnClickListener(v -> openUrl(link.getUrl()));
            linksContainer.addView(row);
        }
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't open that link.", Toast.LENGTH_SHORT).show();
        }
    }

    private String initialsOf(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length && initials.length() < 2; i++) {
            if (!parts[i].isEmpty()) initials.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return initials.toString();
    }
}
