package com.campusconnect.app.classroom;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.ScheduleEvent;
import com.campusconnect.app.classroom.util.NoticeDates;
import com.campusconnect.app.classroom.util.Weeks;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

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
 * Auto-collected schedule: every dated notice across the class's subjects,
 * grouped by week with a day-glance strip and a vertical timeline. Not
 * editable — dates come from notices (see AddNoticeActivity).
 */
public class ScheduleActivity extends BaseActivity {

    private static final int[] PALETTE = {
            0xFF22D3EE, 0xFFA855F7, 0xFFF59E0B, 0xFFF87171, 0xFF4ADE80
    };
    private static final SimpleDateFormat DAY_FMT = new SimpleDateFormat("EEE", Locale.US);

    private LinearLayout weeksContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        weeksContainer = findViewById(R.id.weeksContainer);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedule();
    }

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
                            render(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ScheduleEvent>> call, Throwable t) {
                        // leave as-is
                    }
                });
    }

    private void render(List<ScheduleEvent> events) {
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

    // ── Week block ────────────────────────────────────────────────────────

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

    // ── Timeline event row ───────────────────────────────────────────────

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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
