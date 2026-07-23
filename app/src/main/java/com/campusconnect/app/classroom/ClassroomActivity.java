package com.campusconnect.app.classroom;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Classroom;
import com.campusconnect.app.classroom.model.JoinClassRequest;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Classroom entry for students. Empty "Join / Create" state when not in a class;
 * otherwise the class subjects list with a settings gear (view code / manage).
 */
public class ClassroomActivity extends BaseActivity {

    private static final int[] PALETTE = {
            0xFF22D3EE, 0xFFA855F7, 0xFFF59E0B, 0xFFF87171, 0xFF4ADE80
    };

    private View emptyState, classState;
    private TextView tvHeaderCode, btnCreateClass;
    private View btnSettings;
    private LinearLayout subjectsContainer;
    private EditText etJoinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom);

        emptyState = findViewById(R.id.emptyState);
        classState = findViewById(R.id.classState);
        tvHeaderCode = findViewById(R.id.tvHeaderCode);
        btnCreateClass = findViewById(R.id.btnCreateClass);
        btnSettings = findViewById(R.id.btnSettings);
        subjectsContainer = findViewById(R.id.subjectsContainer);
        etJoinCode = findViewById(R.id.etJoinCode);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCreateClass.setOnClickListener(v ->
                startActivity(new Intent(this, CreateClassActivity.class)));
        findViewById(R.id.btnJoinSubmit).setOnClickListener(v -> joinClass());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ClassSettingsActivity.class)));
        findViewById(R.id.tabFeed).setOnClickListener(v ->
                startActivity(new Intent(this, FeedActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClass();
    }

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
        emptyState.setVisibility(View.VISIBLE);
        classState.setVisibility(View.GONE);
        tvHeaderCode.setVisibility(View.GONE);
        btnCreateClass.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.GONE);
    }

    private void showClass(Classroom classroom) {
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
            empty.setGravity(android.view.Gravity.CENTER);
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
