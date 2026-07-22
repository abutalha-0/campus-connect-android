package com.campusconnect.app.faculty;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.classroom.AddSubjectActivity;
import com.campusconnect.app.classroom.SubjectApiService;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.ui.ComingSoonActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.ImageUtils;
import com.campusconnect.app.faculty.edit.FacultyAddLinkActivity;
import com.campusconnect.app.faculty.edit.FacultyEditIdentityActivity;
import com.campusconnect.app.faculty.model.FacultyLink;
import com.campusconnect.app.faculty.model.FacultyProfile;
import com.campusconnect.app.faculty.util.Designations;
import com.campusconnect.app.profile.edit.SocialPlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Faculty Profile (Own): identity card (name, designation, photo) and a
 * contact & links card. The "Subjects Taught" section is added in a later
 * phase.
 */
public class FacultyHomeActivity extends BaseActivity {

    private TextView tvName, tvDesignation, tvEmail, tvAvatarInitials;
    private ImageView ivAvatar;
    private LinearLayout linksContainer, subjectsContainer;

    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_home);

        tvName = findViewById(R.id.tvName);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvEmail = findViewById(R.id.tvEmail);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        ivAvatar = findViewById(R.id.ivAvatar);
        linksContainer = findViewById(R.id.linksContainer);
        subjectsContainer = findViewById(R.id.subjectsContainer);

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> { if (uri != null) uploadProfilePhoto(uri); });

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        findViewById(R.id.btnEditIdentity).setOnClickListener(v ->
                startActivity(new Intent(this, FacultyEditIdentityActivity.class)));
        findViewById(R.id.btnChangePhoto).setOnClickListener(v -> openPhotoPicker());
        findViewById(R.id.btnEditContact).setOnClickListener(v ->
                startActivity(new Intent(this, FacultyAddLinkActivity.class)));

        findViewById(R.id.navHome).setOnClickListener(v ->
                ComingSoonActivity.start(this, getString(R.string.label_home),
                        R.drawable.ic_nav_home,
                        getResources().getColor(R.color.color_cyan, null)));

        findViewById(R.id.btnAddSubject).setOnClickListener(v ->
                startActivity(new Intent(this, AddSubjectActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadSubjects();
    }

    private void loadProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FacultyApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<FacultyProfile>() {
                    @Override
                    public void onResponse(Call<FacultyProfile> call, Response<FacultyProfile> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            populate(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<FacultyProfile> call, Throwable t) {
                        // leave the screen as-is; onResume will retry next time
                    }
                });
    }

    private void populate(FacultyProfile profile) {
        findViewById(R.id.tvNotVerifiedBanner)
                .setVisibility(profile.isVerified() ? View.GONE : View.VISIBLE);

        String name = profile.getFullName() != null ? profile.getFullName()
                : (profile.getUser() != null ? profile.getUser().getFullName() : "");
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
        } else {
            ivAvatar.setVisibility(View.GONE);
        }

        renderLinks(profile.getLinks());
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

    // ── Subjects Taught ───────────────────────────────────────────────────

    private void loadSubjects() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(SubjectApiService.class)
                .getMySubjects(token)
                .enqueue(new Callback<List<Subject>>() {
                    @Override
                    public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            renderSubjects(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Subject>> call, Throwable t) {
                        // leave section as-is
                    }
                });
    }

    private void renderSubjects(List<Subject> subjects) {
        subjectsContainer.removeAllViews();

        if (subjects.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.subjects_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setPadding(0, dp(8), 0, dp(8));
            subjectsContainer.addView(empty);
            return;
        }

        // Group by intake, intakes ordered highest-first (numeric when possible).
        List<String> intakes = new ArrayList<>();
        for (Subject s : subjects) {
            if (!intakes.contains(s.getIntake())) intakes.add(s.getIntake());
        }
        Collections.sort(intakes, this::compareIntakeDesc);

        Map<String, List<Subject>> groups = new LinkedHashMap<>();
        for (String intake : intakes) {
            List<Subject> items = new ArrayList<>();
            for (Subject s : subjects) {
                if (intake.equals(s.getIntake())) items.add(s);
            }
            groups.put(intake, items);
        }

        boolean first = true;
        for (Map.Entry<String, List<Subject>> entry : groups.entrySet()) {
            addIntakeHeader(entry.getKey(), first);
            addSubjectGrid(entry.getValue());
            first = false;
        }
    }

    private void addIntakeHeader(String intake, boolean first) {
        TextView header = new TextView(this);
        header.setText(getString(R.string.subject_intake_prefix, intake));
        header.setTextColor(getResources().getColor(R.color.color_muted, null));
        header.setTextSize(11f);
        header.setLetterSpacing(0.06f);
        header.setTypeface(header.getTypeface(), android.graphics.Typeface.BOLD);
        header.setPadding(0, first ? 0 : dp(12), 0, dp(8));
        subjectsContainer.addView(header);
    }

    /** Lays out subject cards two per row. */
    private void addSubjectGrid(List<Subject> items) {
        LinearLayout row = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowLp.bottomMargin = dp(10);
                row.setLayoutParams(rowLp);
                subjectsContainer.addView(row);
            }

            View card = LayoutInflater.from(this)
                    .inflate(R.layout.item_subject_card, row, false);
            Subject subject = items.get(i);
            ((TextView) card.findViewById(R.id.tvSubjectName)).setText(subject.getName());
            ((TextView) card.findViewById(R.id.tvSubjectSection))
                    .setText(getString(R.string.subject_section_prefix) + subject.getSection());
            card.setOnClickListener(v -> com.campusconnect.app.classroom.SubjectDetailActivity.start(
                    this, subject.getId(), subject.getName(), subject.getFacultyName()));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginStart(i % 2 == 0 ? 0 : dp(5));
            lp.setMarginEnd(i % 2 == 0 ? dp(5) : 0);
            card.setLayoutParams(lp);
            row.addView(card);
        }

        // Balance the last row if it has a single card.
        if (row != null && items.size() % 2 == 1) {
            View spacer = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginStart(dp(5));
            spacer.setLayoutParams(lp);
            row.addView(spacer);
        }
    }

    private int compareIntakeDesc(String a, String b) {
        try {
            return Integer.compare(Integer.parseInt(b.trim()), Integer.parseInt(a.trim()));
        } catch (NumberFormatException e) {
            return b.compareTo(a);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // ── Photo ─────────────────────────────────────────────────────────────

    private void openPhotoPicker() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void uploadProfilePhoto(Uri uri) {
        File compressed;
        try {
            compressed = ImageUtils.compressImageFromUri(this, uri);
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't read that image. Try another.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading photo…", Toast.LENGTH_SHORT).show();

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), compressed);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData(
                "profile_photo", compressed.getName(), fileBody);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FacultyApiService.class)
                .updateProfilePhoto(token, photoPart)
                .enqueue(new Callback<FacultyProfile>() {
                    @Override
                    public void onResponse(Call<FacultyProfile> call, Response<FacultyProfile> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            populate(response.body());
                            Toast.makeText(FacultyHomeActivity.this, "Profile photo updated!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FacultyHomeActivity.this, "Upload failed. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FacultyProfile> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FacultyHomeActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
