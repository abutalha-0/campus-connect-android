package com.campusconnect.app.classroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Notice;
import com.campusconnect.app.classroom.model.Resource;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.classroom.util.NoticeDates;
import com.campusconnect.app.classroom.util.ResourceTypes;
import com.campusconnect.app.classroom.util.Weeks;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Subject detail: header + Resources / Notice / Work tabs. Resources are live. */
public class SubjectDetailActivity extends BaseActivity {

    public static final String EXTRA_SUBJECT_ID = "subject_id";
    public static final String EXTRA_SUBJECT_NAME = "subject_name";
    public static final String EXTRA_FACULTY_NAME = "faculty_name";

    public static void start(Context ctx, int id, String name, String facultyName) {
        Intent i = new Intent(ctx, SubjectDetailActivity.class);
        i.putExtra(EXTRA_SUBJECT_ID, id);
        i.putExtra(EXTRA_SUBJECT_NAME, name);
        i.putExtra(EXTRA_FACULTY_NAME, facultyName);
        ctx.startActivity(i);
    }

    private int subjectId;
    private TextView tabResources, tabNotice, tabWork;
    private View contentResources, contentNotice, contentWork;
    private LinearLayout resourcesContainer, noticesContainer;
    private ActivityResultLauncher<Intent> settingsLauncher;
    private View btnSettings, btnPostResource, btnPostNotice;

    // Whether the current viewer owns this subject (faculty) / may post
    // content here (faculty or CR). Resolved from the subject detail call;
    // conservative (false) until then so controls don't flash on for a
    // read-only viewer.
    private boolean isOwner = false;
    private boolean canPost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_detail);

        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        ((TextView) findViewById(R.id.tvSubjectName))
                .setText(getIntent().getStringExtra(EXTRA_SUBJECT_NAME));
        ((TextView) findViewById(R.id.tvFacultyName))
                .setText(getIntent().getStringExtra(EXTRA_FACULTY_NAME));

        tabResources = findViewById(R.id.tabResources);
        tabNotice = findViewById(R.id.tabNotice);
        tabWork = findViewById(R.id.tabWork);
        contentResources = findViewById(R.id.tabContentResources);
        contentNotice = findViewById(R.id.tabContentNotice);
        contentWork = findViewById(R.id.tabContentWork);
        resourcesContainer = findViewById(R.id.resourcesContainer);
        noticesContainer = findViewById(R.id.noticesContainer);
        btnSettings = findViewById(R.id.btnSettings);
        btnPostResource = findViewById(R.id.btnPostResource);
        btnPostNotice = findViewById(R.id.btnPostNotice);

        // Hidden until the subject detail call resolves who's viewing.
        btnSettings.setVisibility(View.GONE);
        btnPostResource.setVisibility(View.GONE);
        btnPostNotice.setVisibility(View.GONE);

        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                    if (result.getData().getBooleanExtra("deleted", false)) {
                        finish();
                        return;
                    }
                    String newName = result.getData().getStringExtra("name");
                    if (newName != null) {
                        ((TextView) findViewById(R.id.tvSubjectName)).setText(newName);
                    }
                });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tabResources.setOnClickListener(v -> selectTab(0));
        tabNotice.setOnClickListener(v -> selectTab(1));
        tabWork.setOnClickListener(v -> selectTab(2));

        btnPostResource.setOnClickListener(v ->
                startActivity(AddResourceActivity.createIntent(this, subjectId)));
        btnPostNotice.setOnClickListener(v ->
                startActivity(AddNoticeActivity.createIntent(this, subjectId)));
        btnSettings.setOnClickListener(v ->
                settingsLauncher.launch(SubjectSettingsActivity.createIntent(this, subjectId)));

        selectTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSubject();
        loadResources();
        loadNotices();
    }

    private void loadSubject() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(SubjectApiService.class)
                .getSubject(token, subjectId)
                .enqueue(new Callback<Subject>() {
                    @Override
                    public void onResponse(Call<Subject> call, Response<Subject> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Subject subject = response.body();
                            ((TextView) findViewById(R.id.tvSubjectName)).setText(subject.getName());
                            ((TextView) findViewById(R.id.tvFacultyName))
                                    .setText(subject.getFacultyName());

                            isOwner = subject.isOwner();
                            canPost = subject.canPost();
                            btnSettings.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                            btnPostResource.setVisibility(canPost ? View.VISIBLE : View.GONE);
                            btnPostNotice.setVisibility(canPost ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<Subject> call, Throwable t) {
                        // header/permissions stay as last known
                    }
                });
    }

    private void selectTab(int index) {
        tabResources.setTextColor(getResources().getColor(
                index == 0 ? R.color.color_text_primary : R.color.color_muted, null));
        tabResources.setTypeface(null, index == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabNotice.setTextColor(getResources().getColor(
                index == 1 ? R.color.color_text_primary : R.color.color_muted, null));
        tabNotice.setTypeface(null, index == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabWork.setTextColor(getResources().getColor(
                index == 2 ? R.color.color_text_primary : R.color.color_muted, null));
        tabWork.setTypeface(null, index == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        contentResources.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        contentNotice.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        contentWork.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }

    // ── Resources ─────────────────────────────────────────────────────────

    private void loadResources() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ResourceApiService.class)
                .getResources(token, subjectId)
                .enqueue(new Callback<List<Resource>>() {
                    @Override
                    public void onResponse(Call<List<Resource>> call, Response<List<Resource>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            renderResources(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Resource>> call, Throwable t) {
                        // leave as-is
                    }
                });
    }

    private void renderResources(List<Resource> resources) {
        resourcesContainer.removeAllViews();

        if (resources.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.resources_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setPadding(0, dp(20), 0, dp(20));
            empty.setGravity(android.view.Gravity.CENTER);
            resourcesContainer.addView(empty);
            return;
        }

        // Group by the Saturday–Friday week of each resource's upload time.
        Map<String, java.util.List<Resource>> groups = new LinkedHashMap<>();
        for (Resource r : resources) {
            String key = Weeks.weekKey(r.getCreatedAt());
            groups.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(r);
        }

        for (Map.Entry<String, java.util.List<Resource>> entry : groups.entrySet()) {
            TextView header = new TextView(this);
            header.setText(Weeks.weekLabel(entry.getValue().get(0).getCreatedAt()));
            header.setTextColor(getResources().getColor(R.color.color_muted, null));
            header.setTextSize(11f);
            header.setLetterSpacing(0.06f);
            header.setTypeface(header.getTypeface(), android.graphics.Typeface.BOLD);
            header.setPadding(0, dp(8), 0, dp(10));
            resourcesContainer.addView(header);

            for (Resource r : entry.getValue()) {
                resourcesContainer.addView(buildResourceRow(r));
            }
        }
    }

    private View buildResourceRow(Resource r) {
        View row = LayoutInflater.from(this)
                .inflate(R.layout.item_resource, resourcesContainer, false);

        String key = r.getResourceType();
        int color = ResourceTypes.colorFor(key);

        TextView badge = row.findViewById(R.id.tvBadge);
        badge.setText(key);
        badge.setTextColor(color);
        badge.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x24000000));

        ((TextView) row.findViewById(R.id.tvTitle)).setText(r.getTitle());
        ((TextView) row.findViewById(R.id.tvMeta)).setText(ResourceTypes.labelFor(this, key));

        row.findViewById(R.id.resourceBody).setOnClickListener(v -> showDetailDialog(r));

        View btnEdit = row.findViewById(R.id.btnEdit);
        View btnDelete = row.findViewById(R.id.btnDelete);
        if (r.canEdit()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v ->
                    startActivity(AddResourceActivity.editIntent(this, subjectId, r)));
            btnDelete.setOnClickListener(v -> confirmDelete(r));
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        return row;
    }

    private void showDetailDialog(Resource r) {
        StringBuilder message = new StringBuilder(ResourceTypes.labelFor(this, r.getResourceType()));
        if (r.getDescription() != null && !r.getDescription().isEmpty()) {
            message.append("\n\n").append(r.getDescription());
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(r.getTitle())
                .setMessage(message.toString())
                .setNegativeButton("Close", null);

        if (r.getFileUrl() != null && !r.getFileUrl().isEmpty()) {
            b.setPositiveButton(getString(R.string.resource_open), (d, w) -> openUrl(r.getFileUrl()));
        }
        b.show();
    }

    private void confirmDelete(Resource r) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.resource_delete_title))
                .setMessage("Remove \"" + r.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteResource(r))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteResource(Resource r) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ResourceApiService.class)
                .deleteResource(token, subjectId, r.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(SubjectDetailActivity.this, "Resource deleted",
                                    Toast.LENGTH_SHORT).show();
                            loadResources();
                        } else {
                            Toast.makeText(SubjectDetailActivity.this, "Couldn't delete. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(SubjectDetailActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Notices ───────────────────────────────────────────────────────────

    private void loadNotices() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NoticeApiService.class)
                .getNotices(token, subjectId)
                .enqueue(new retrofit2.Callback<List<Notice>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<Notice>> call, retrofit2.Response<List<Notice>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            renderNotices(response.body());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<Notice>> call, Throwable t) {
                        // leave as-is
                    }
                });
    }

    private void renderNotices(List<Notice> notices) {
        noticesContainer.removeAllViews();
        if (notices.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.notices_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, dp(20), 0, dp(20));
            noticesContainer.addView(empty);
            return;
        }
        for (Notice n : notices) {
            noticesContainer.addView(buildNoticeRow(n));
        }
    }

    private View buildNoticeRow(Notice n) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_notice, noticesContainer, false);

        Notice.Author author = n.getAuthor();
        String name = author != null ? author.getFullName() : "";
        String role = author != null ? author.getRole() : "";

        ((TextView) card.findViewById(R.id.tvAvatar)).setText(initialsOf(name));
        ((TextView) card.findViewById(R.id.tvAuthor)).setText(name);

        TextView roleBadge = card.findViewById(R.id.tvRole);
        roleBadge.setText(role);
        int roleColor = "FACULTY".equals(role) ? getResources().getColor(R.color.color_cyan, null)
                : "CR".equals(role) ? getResources().getColor(R.color.color_purple, null)
                : getResources().getColor(R.color.color_muted, null);
        roleBadge.setTextColor(roleColor);
        roleBadge.setBackgroundTintList(ColorStateList.valueOf((roleColor & 0x00FFFFFF) | 0x24000000));

        ((TextView) card.findViewById(R.id.tvDate)).setText(formatDate(n.getCreatedAt()));
        ((TextView) card.findViewById(R.id.tvText)).setText(n.getText());

        View highlightBox = card.findViewById(R.id.highlightBox);
        if (n.hasHighlight()) {
            highlightBox.setVisibility(View.VISIBLE);
            ((TextView) card.findViewById(R.id.tvHighlight)).setText(highlightText(n));
        } else {
            highlightBox.setVisibility(View.GONE);
        }

        View btnEdit = card.findViewById(R.id.btnEdit);
        View btnDelete = card.findViewById(R.id.btnDelete);
        if (n.canEdit()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v -> startActivity(AddNoticeActivity.editIntent(this, subjectId, n)));
            btnDelete.setOnClickListener(v -> confirmDeleteNotice(n));
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }
        return card;
    }

    /**
     * Combines the free-text highlight label with the structured event
     * date/time (either or both may be present) into one display line, e.g.
     * "Exam date · Jul 20, 2026 · 10:00 AM".
     */
    private String highlightText(Notice n) {
        String label = n.getHighlight();
        String dateLabel = NoticeDates.format(n.getEventDate(), n.getEventTime());

        boolean hasLabel = label != null && !label.isEmpty();
        boolean hasDate = dateLabel != null;

        if (hasLabel && hasDate) return label + " · " + dateLabel;
        if (hasDate) return dateLabel;
        return label;
    }

    private void confirmDeleteNotice(Notice n) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.notice_delete_title))
                .setMessage("Remove this notice? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteNotice(n))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNotice(Notice n) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NoticeApiService.class)
                .deleteNotice(token, subjectId, n.getId())
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(SubjectDetailActivity.this, "Notice deleted", Toast.LENGTH_SHORT).show();
                            loadNotices();
                        } else {
                            Toast.makeText(SubjectDetailActivity.this, "Couldn't delete. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(SubjectDetailActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String initialsOf(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    private String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso.substring(0, 10));
            return new SimpleDateFormat("MMM d", Locale.US).format(d);
        } catch (Exception e) {
            return "";
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't open that link.", Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
