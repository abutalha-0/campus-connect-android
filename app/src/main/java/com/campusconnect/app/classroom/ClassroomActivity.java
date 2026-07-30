package com.campusconnect.app.classroom;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Classroom;
import com.campusconnect.app.classroom.model.FeedPost;
import com.campusconnect.app.classroom.model.FeedVoteRequest;
import com.campusconnect.app.classroom.model.JoinClassRequest;
import com.campusconnect.app.classroom.model.ScheduleEvent;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.classroom.util.NoticeDates;
import com.campusconnect.app.classroom.util.RelativeTime;
import com.campusconnect.app.classroom.util.Weeks;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.ProfileNavigator;
import com.campusconnect.app.core.utils.SkeletonAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Classroom entry for students. Empty "Join / Create" state when not in a class;
 * otherwise a real in-place tab layout — Subjects / Schedule / Feed — all
 * swapped within this one screen rather than navigating to separate Activities.
 */
public class ClassroomActivity extends BaseActivity {

    private static final int[] PALETTE = {
            0xFF22D3EE, 0xFFA855F7, 0xFFF59E0B, 0xFFF87171, 0xFF4ADE80
    };
    private static final SimpleDateFormat DAY_FMT = new SimpleDateFormat("EEE", Locale.US);

    private View emptyState, classState, skeletonState;
    private TextView tvHeaderCode, btnCreateClass;
    private View btnSettings;
    private EditText etJoinCode;

    // ── Tabs ──────────────────────────────────────────────────────────────
    private TextView tabSubjects, tabSchedule, tabFeed;
    private String activeTab = "subjects";

    // Subjects tab
    private LinearLayout subjectsContainer;

    // Schedule tab
    private LinearLayout weeksContainer;

    // Feed tab
    private LinearLayout feedTabContent, feedSkeletonContainer, postsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom);

        emptyState = findViewById(R.id.emptyState);
        classState = findViewById(R.id.classState);
        skeletonState = findViewById(R.id.skeletonState);
        tvHeaderCode = findViewById(R.id.tvHeaderCode);
        btnCreateClass = findViewById(R.id.btnCreateClass);
        btnSettings = findViewById(R.id.btnSettings);
        etJoinCode = findViewById(R.id.etJoinCode);

        tabSubjects = findViewById(R.id.tabSubjects);
        tabSchedule = findViewById(R.id.tabSchedule);
        tabFeed = findViewById(R.id.tabFeed);

        subjectsContainer = findViewById(R.id.subjectsContainer);
        weeksContainer = findViewById(R.id.weeksContainer);
        feedTabContent = findViewById(R.id.feedTabContent);
        feedSkeletonContainer = findViewById(R.id.feedSkeletonContainer);
        postsContainer = findViewById(R.id.postsContainer);

        LinearLayout skeletonList = (LinearLayout) skeletonState;
        for (int i = 0; i < 3; i++) {
            LayoutInflater.from(this).inflate(R.layout.skeleton_class_subject, skeletonList, true);
        }
        for (int i = 0; i < 3; i++) {
            LayoutInflater.from(this).inflate(R.layout.skeleton_feed_post, feedSkeletonContainer, true);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCreateClass.setOnClickListener(v ->
                startActivity(new Intent(this, CreateClassActivity.class)));
        findViewById(R.id.btnJoinSubmit).setOnClickListener(v -> joinClass());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ClassSettingsActivity.class)));

        tabSubjects.setOnClickListener(v -> switchTab("subjects"));
        tabSchedule.setOnClickListener(v -> switchTab("schedule"));
        tabFeed.setOnClickListener(v -> switchTab("feed"));

        findViewById(R.id.btnNewPost).setOnClickListener(v ->
                startActivity(AddFeedPostActivity.createIntent(this)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        skeletonState.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        classState.setVisibility(View.GONE);
        SkeletonAnimator.start(skeletonState);
        loadClass();

        // Refresh whichever tab is currently active — covers returning from
        // AddNoticeActivity/AddFeedPostActivity, same as each screen's own
        // onResume used to before they were merged in here.
        if ("schedule".equals(activeTab)) {
            loadSchedule();
        } else if ("feed".equals(activeTab)) {
            loadFeedPosts();
        }
    }

    // ── Tab switching ─────────────────────────────────────────────────────

    private void switchTab(String tab) {
        if (tab.equals(activeTab)) return;
        activeTab = tab;

        for (TextView t : new TextView[]{tabSubjects, tabSchedule, tabFeed}) {
            t.setBackgroundResource(R.drawable.bg_tab_pill_inactive);
            t.setTextColor(getResources().getColor(R.color.color_muted, null));
        }
        TextView active = tab.equals("subjects") ? tabSubjects
                : tab.equals("schedule") ? tabSchedule : tabFeed;
        active.setBackgroundResource(R.drawable.bg_tab_pill_active);
        active.setTextColor(getResources().getColor(R.color.color_cyan, null));

        subjectsContainer.setVisibility(tab.equals("subjects") ? View.VISIBLE : View.GONE);
        weeksContainer.setVisibility(tab.equals("schedule") ? View.VISIBLE : View.GONE);
        feedTabContent.setVisibility(tab.equals("feed") ? View.VISIBLE : View.GONE);

        if (tab.equals("schedule")) {
            loadSchedule();
        } else if (tab.equals("feed")) {
            loadFeedPosts();
        }
    }

    // ── Class + Subjects ──────────────────────────────────────────────────

    private void loadClass() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .getMyClass(token)
                .enqueue(new Callback<Classroom>() {
                    @Override
                    public void onResponse(Call<Classroom> call, Response<Classroom> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            showClass(response.body());
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(Call<Classroom> call, Throwable t) {
                        if (isFinishing()) return;
                        showEmpty();
                    }
                });
    }

    private void showEmpty() {
        SkeletonAnimator.stop(skeletonState);
        skeletonState.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        classState.setVisibility(View.GONE);
        tvHeaderCode.setVisibility(View.GONE);
        btnCreateClass.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.GONE);
    }

    private void showClass(Classroom classroom) {
        SkeletonAnimator.stop(skeletonState);
        skeletonState.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        classState.setVisibility(View.VISIBLE);
        btnCreateClass.setVisibility(View.GONE);
        btnSettings.setVisibility(View.VISIBLE);

        tvHeaderCode.setVisibility(View.VISIBLE);
        tvHeaderCode.setText("Class code: " + classroom.getCode());

        renderSubjects(classroom.getSubjects());
    }

    private void renderSubjects(List<Subject> subjects) {
        subjectsContainer.removeAllViews();
        if (subjects == null || subjects.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.class_no_courses));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, dp(24));
            subjectsContainer.addView(empty);
            return;
        }
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_class_subject, subjectsContainer, false);

            int color = PALETTE[i % PALETTE.length];
            TextView badge = row.findViewById(R.id.tvBadge);
            badge.setText(initialsOf(s.getName()));
            badge.setTextColor(color);
            badge.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x24000000));

            ((TextView) row.findViewById(R.id.tvSubjectName)).setText(s.getName());
            TextView instructor = row.findViewById(R.id.tvInstructor);
            instructor.setText(s.getFacultyName() != null ? s.getFacultyName() : "");
            // Tapping the instructor name opens their public profile instead
            // of the subject — this child click target takes the touch
            // before it reaches the row's own listener below.
            instructor.setOnClickListener(v ->
                    com.campusconnect.app.faculty.FacultyPublicProfileActivity
                            .start(this, s.getFacultyUserId()));
            row.setOnClickListener(v -> SubjectDetailActivity.start(
                    this, s.getId(), s.getName(), s.getFacultyName()));
            subjectsContainer.addView(row);
        }
    }

    private void joinClass() {
        String code = etJoinCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, getString(R.string.join_enter_code), Toast.LENGTH_SHORT).show();
            return;
        }
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .joinClass(token, new JoinClassRequest(code))
                .enqueue(new Callback<Classroom>() {
                    @Override
                    public void onResponse(Call<Classroom> call, Response<Classroom> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            etJoinCode.setText("");
                            showClass(response.body());
                        } else if (response.code() == 404) {
                            Toast.makeText(ClassroomActivity.this,
                                    getString(R.string.join_no_class), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 400) {
                            Toast.makeText(ClassroomActivity.this,
                                    getString(R.string.join_already), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClassroomActivity.this,
                                    getString(R.string.join_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Classroom> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassroomActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String t = name.trim();
        return t.length() >= 2 ? t.substring(0, 2).toUpperCase() : t.toUpperCase();
    }

    // ── Schedule tab ──────────────────────────────────────────────────────

    private void loadSchedule() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ScheduleApiService.class)
                .getSchedule(token)
                .enqueue(new Callback<List<ScheduleEvent>>() {
                    @Override
                    public void onResponse(Call<List<ScheduleEvent>> call,
                                            Response<List<ScheduleEvent>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            renderSchedule(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                        // leave as-is
                    }
                });
    }

    private void renderSchedule(List<ScheduleEvent> events) {
        weeksContainer.removeAllViews();

        if (events.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.schedule_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, dp(40));
            weeksContainer.addView(empty);
            return;
        }

        // Assign each event a color by its overall chronological position, so
        // colors vary without needing any color data from the backend.
        Map<Integer, Integer> colorByNoticeId = new HashMap<>();
        for (int i = 0; i < events.size(); i++) {
            colorByNoticeId.put(events.get(i).getNoticeId(), PALETTE[i % PALETTE.length]);
        }

        Map<String, List<ScheduleEvent>> weekGroups = new LinkedHashMap<>();
        for (ScheduleEvent e : events) {
            weekGroups.computeIfAbsent(Weeks.weekKey(e.getEventDate()), k -> new ArrayList<>()).add(e);
        }

        for (List<ScheduleEvent> weekEvents : weekGroups.values()) {
            weeksContainer.addView(buildWeekBlock(weekEvents, colorByNoticeId));
        }
    }

    private View buildWeekBlock(List<ScheduleEvent> weekEvents, Map<Integer, Integer> colors) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams blockLp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blockLp.bottomMargin = dp(26);
        block.setLayoutParams(blockLp);

        String anyDate = weekEvents.get(0).getEventDate();
        String relativeLabel = Weeks.relativeWeekLabel(anyDate);
        String range = Weeks.weekLabel(anyDate);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(relativeLabel != null ? relativeLabel : range);
        tvLabel.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        tvLabel.setTextSize(14f);
        tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.BOLD);
        block.addView(tvLabel);

        if (relativeLabel != null) {
            TextView tvRange = new TextView(this);
            tvRange.setText(range);
            tvRange.setTextColor(getResources().getColor(R.color.color_muted, null));
            tvRange.setTextSize(11f);
            LinearLayout.LayoutParams rangeLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rangeLp.topMargin = dp(1);
            tvRange.setLayoutParams(rangeLp);
            block.addView(tvRange);
        }

        LinearLayout.LayoutParams stripLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stripLp.topMargin = dp(12);
        stripLp.bottomMargin = dp(14);
        View strip = buildDayGlanceStrip(weekEvents, colors, anyDate);
        strip.setLayoutParams(stripLp);
        block.addView(strip);

        String previousDay = null;
        for (ScheduleEvent e : weekEvents) {
            String day = Weeks.dayLabel(e.getEventDate());
            boolean showDay = !day.equals(previousDay);
            block.addView(buildEventRow(e, colors.get(e.getNoticeId()), showDay ? day : ""));
            previousDay = day;
        }

        return block;
    }

    private View buildDayGlanceStrip(List<ScheduleEvent> weekEvents, Map<Integer, Integer> colors, String anyDateInWeek) {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setBackgroundResource(R.drawable.bg_card);
        strip.setPadding(dp(10), dp(12), dp(10), dp(12));

        // First event's color per day, in this week's chronological order.
        Map<String, Integer> colorByDay = new LinkedHashMap<>();
        for (ScheduleEvent e : weekEvents) {
            colorByDay.putIfAbsent(Weeks.dayLabel(e.getEventDate()), colors.get(e.getNoticeId()));
        }

        Calendar cal = Weeks.weekStart(anyDateInWeek);
        if (cal == null) cal = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            String dayLabel = DAY_FMT.format(cal.getTime());

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView label = new TextView(this);
            label.setText(dayLabel);
            label.setTextSize(10f);
            label.setTextColor(getResources().getColor(R.color.color_muted, null));
            col.addView(label);

            View dot = new View(this);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(8), dp(8));
            dotLp.topMargin = dp(6);
            dot.setLayoutParams(dotLp);
            dot.setBackgroundResource(R.drawable.bg_dot);
            Integer color = colorByDay.get(dayLabel);
            dot.setBackgroundTintList(ColorStateList.valueOf(color != null ? color : 0x26FFFFFF));
            col.addView(dot);

            strip.addView(col);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return strip;
    }

    private View buildEventRow(ScheduleEvent e, int color, String dayLabel) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(12);
        row.setLayoutParams(rowLp);

        TextView tvDay = new TextView(this);
        tvDay.setText(dayLabel);
        tvDay.setTextColor(getResources().getColor(R.color.color_muted, null));
        tvDay.setTextSize(11f);
        tvDay.setTypeface(tvDay.getTypeface(), Typeface.BOLD);
        tvDay.setGravity(Gravity.END);
        LinearLayout.LayoutParams dayLp = new LinearLayout.LayoutParams(dp(34), LinearLayout.LayoutParams.WRAP_CONTENT);
        dayLp.topMargin = dp(13);
        dayLp.setMarginEnd(dp(10));
        tvDay.setLayoutParams(dayLp);
        row.addView(tvDay);

        View dot = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(9), dp(9));
        dotLp.topMargin = dp(16);
        dotLp.setMarginEnd(dp(10));
        dot.setLayoutParams(dotLp);
        dot.setBackgroundResource(R.drawable.bg_dot);
        dot.setBackgroundTintList(ColorStateList.valueOf(color));
        row.addView(dot);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(e.getTitle());
        tvTitle.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        tvTitle.setTextSize(13.5f);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        titleRow.addView(tvTitle);

        TextView tvTime = new TextView(this);
        tvTime.setText(e.getEventTime() != null
                ? NoticeDates.formatTimeOnly(e.getEventTime())
                : getString(R.string.schedule_all_day));
        tvTime.setTextColor(getResources().getColor(R.color.color_muted, null));
        tvTime.setTextSize(11f);
        titleRow.addView(tvTime);
        card.addView(titleRow);

        LinearLayout tagsRow = new LinearLayout(this);
        tagsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tagsLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tagsLp.topMargin = dp(6);
        tagsRow.setLayoutParams(tagsLp);
        tagsRow.addView(buildTag(e.getSubjectName(), color));
        tagsRow.addView(buildTag(e.getAuthorRole(), color));
        card.addView(tagsRow);

        row.addView(card);

        row.setOnClickListener(v -> SubjectDetailActivity.startAtNotice(
                this, e.getSubjectId(), e.getSubjectName(), e.getNoticeId()));

        return row;
    }

    private View buildTag(String text, int color) {
        TextView tag = new TextView(this);
        tag.setText(text);
        tag.setTextColor(color);
        tag.setTextSize(10f);
        tag.setTypeface(tag.getTypeface(), Typeface.BOLD);
        tag.setBackgroundResource(R.drawable.bg_resource_badge);
        tag.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x1F000000));
        tag.setPadding(dp(9), dp(3), dp(9), dp(3));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(6));
        tag.setLayoutParams(lp);
        return tag;
    }

    // ── Feed tab ──────────────────────────────────────────────────────────

    private void loadFeedPosts() {
        SkeletonAnimator.showLoading(feedSkeletonContainer, postsContainer);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .getPosts(token)
                .enqueue(new Callback<List<FeedPost>>() {
                    @Override
                    public void onResponse(Call<List<FeedPost>> call, Response<List<FeedPost>> response) {
                        if (isFinishing()) return;
                        SkeletonAnimator.showContent(feedSkeletonContainer, postsContainer);
                        if (response.isSuccessful() && response.body() != null) {
                            renderPosts(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FeedPost>> call, Throwable t) {
                        // leave as-is
                        if (isFinishing()) return;
                        SkeletonAnimator.showContent(feedSkeletonContainer, postsContainer);
                    }
                });
    }

    private void renderPosts(List<FeedPost> posts) {
        postsContainer.removeAllViews();
        if (posts.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.feed_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, dp(40));
            postsContainer.addView(empty);
            return;
        }
        for (FeedPost post : posts) {
            postsContainer.addView(buildPostCard(post));
        }
    }

    private View buildPostCard(FeedPost post) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_feed_post, postsContainer, false);

        FeedPost.Author author = post.getAuthor();
        String name = author != null ? author.getFullName() : "";
        String role = author != null ? author.getRole() : "";

        TextView tvAvatar = card.findViewById(R.id.tvAvatar);
        TextView tvAuthor = card.findViewById(R.id.tvAuthor);
        tvAvatar.setText(authorInitials(name));
        tvAuthor.setText(name);
        if (author != null) {
            View.OnClickListener openAuthor = v ->
                    ProfileNavigator.open(this, author.getId(), author.getRole());
            tvAvatar.setOnClickListener(openAuthor);
            tvAuthor.setOnClickListener(openAuthor);
        }

        TextView roleBadge = card.findViewById(R.id.tvRole);
        roleBadge.setText(role);
        int roleColor = "FACULTY".equals(role) ? getResources().getColor(R.color.color_cyan, null)
                : "CR".equals(role) ? getResources().getColor(R.color.color_purple, null)
                : getResources().getColor(R.color.color_muted, null);
        roleBadge.setTextColor(roleColor);
        roleBadge.setBackgroundTintList(ColorStateList.valueOf((roleColor & 0x00FFFFFF) | 0x24000000));

        ((TextView) card.findViewById(R.id.tvTime)).setText(RelativeTime.format(post.getCreatedAt()));

        TextView tvTag = card.findViewById(R.id.tvTag);
        if (post.getTag() != null && !post.getTag().isEmpty()) {
            tvTag.setVisibility(View.VISIBLE);
            tvTag.setText(post.getTag());
        } else {
            tvTag.setVisibility(View.GONE);
        }

        ((TextView) card.findViewById(R.id.tvTitle)).setText(post.getTitle());
        ((TextView) card.findViewById(R.id.tvBody)).setText(post.getBody());
        ((TextView) card.findViewById(R.id.tvCommentsCount))
                .setText("💬  " + post.getCommentsCount());

        bindVoteControls(card, post);

        View btnEdit = card.findViewById(R.id.btnEdit);
        View btnDelete = card.findViewById(R.id.btnDelete);
        if (post.canEdit()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v ->
                    startActivity(AddFeedPostActivity.editIntent(this, post)));
            btnDelete.setOnClickListener(v -> confirmDeletePost(post));
        }

        card.setOnClickListener(v -> FeedPostDetailActivity.start(this, post.getId()));

        return card;
    }

    private void bindVoteControls(View card, FeedPost post) {
        TextView tvScore = card.findViewById(R.id.tvScore);
        TextView btnUp = card.findViewById(R.id.btnUpvote);
        TextView btnDown = card.findViewById(R.id.btnDownvote);

        tvScore.setText(String.valueOf(post.getScore()));
        btnUp.setTextColor(getResources().getColor(
                post.getMyVote() == 1 ? R.color.color_cyan : R.color.color_muted, null));
        btnDown.setTextColor(getResources().getColor(
                post.getMyVote() == -1 ? R.color.color_red : R.color.color_muted, null));

        btnUp.setOnClickListener(v -> vote(post.getId(), 1));
        btnDown.setOnClickListener(v -> vote(post.getId(), -1));
    }

    private void vote(int postId, int value) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .vote(token, postId, new FeedVoteRequest(value))
                .enqueue(new Callback<FeedPost>() {
                    @Override
                    public void onResponse(Call<FeedPost> call, Response<FeedPost> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            loadFeedPosts();
                        } else {
                            Toast.makeText(ClassroomActivity.this, "Couldn't vote. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FeedPost> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassroomActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeletePost(FeedPost post) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.feed_delete_title))
                .setMessage(getString(R.string.feed_delete_message))
                .setPositiveButton("Delete", (d, w) -> deletePost(post))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost(FeedPost post) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .deletePost(token, post.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(ClassroomActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                            loadFeedPosts();
                        } else {
                            Toast.makeText(ClassroomActivity.this, "Couldn't delete. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassroomActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String authorInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    // ── Shared ────────────────────────────────────────────────────────────

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
