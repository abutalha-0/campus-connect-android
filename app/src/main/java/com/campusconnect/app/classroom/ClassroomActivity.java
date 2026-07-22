package com.campusconnect.app.classroom;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.AddCourseRequest;
import com.campusconnect.app.classroom.model.Classroom;
import com.campusconnect.app.classroom.model.DeleteClassRequest;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Classroom entry for students. Shows the "Join / Create" empty state when the
 * student has no class, or their class (code + courses + management) when they do.
 */
public class ClassroomActivity extends BaseActivity {

    private static final int[] PALETTE = {
            0xFF22D3EE, 0xFFA855F7, 0xFFF59E0B, 0xFFF87171, 0xFF4ADE80
    };

    private View emptyState, classState;
    private TextView tvClassCode;
    private LinearLayout coursesContainer;
    private EditText etAddCode;

    private String classCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom);

        emptyState = findViewById(R.id.emptyState);
        classState = findViewById(R.id.classState);
        tvClassCode = findViewById(R.id.tvClassCode);
        coursesContainer = findViewById(R.id.coursesContainer);
        etAddCode = findViewById(R.id.etAddCode);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCreateClass).setOnClickListener(v ->
                startActivity(new Intent(this, CreateClassActivity.class)));
        findViewById(R.id.btnJoinSubmit).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.join_coming_soon), Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnCopyCode).setOnClickListener(v -> copyCode());
        findViewById(R.id.btnAddCourse).setOnClickListener(v -> addCourse());
        findViewById(R.id.btnDeleteClass).setOnClickListener(v -> promptDelete());
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
                            showEmpty();  // 404 → no class yet
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
        findViewById(R.id.btnCreateClass).setVisibility(View.VISIBLE);
    }

    private void showClass(Classroom classroom) {
        emptyState.setVisibility(View.GONE);
        classState.setVisibility(View.VISIBLE);
        findViewById(R.id.btnCreateClass).setVisibility(View.GONE);

        classCode = classroom.getCode();
        tvClassCode.setText(classCode);
        renderCourses(classroom.getSubjects());
    }

    private void renderCourses(List<Subject> subjects) {
        coursesContainer.removeAllViews();
        if (subjects == null || subjects.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.class_no_courses));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, dp(14), 0, dp(14));
            coursesContainer.addView(empty);
            return;
        }
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.item_course, coursesContainer, false);

            int color = PALETTE[i % PALETTE.length];
            TextView badge = row.findViewById(R.id.tvBadge);
            badge.setText(initialsOf(s.getName()));
            badge.setTextColor(color);
            badge.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x24000000));

            ((TextView) row.findViewById(R.id.tvCourseName)).setText(s.getName());
            ((TextView) row.findViewById(R.id.tvCourseCode)).setText("Code: " + s.getCode());
            row.findViewById(R.id.btnRemove).setOnClickListener(v -> removeCourse(s.getId()));
            coursesContainer.addView(row);
        }
    }

    private void copyCode() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null && classCode != null && !classCode.isEmpty()) {
            cm.setPrimaryClip(ClipData.newPlainText("Class code", classCode));
            ((TextView) findViewById(R.id.btnCopyCode)).setText(getString(R.string.subject_copied));
        }
    }

    private void addCourse() {
        String code = etAddCode.getText().toString().trim();
        if (code.isEmpty()) return;

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .addCourse(token, new AddCourseRequest(code))
                .enqueue(new Callback<Subject>() {
                    @Override
                    public void onResponse(Call<Subject> call, Response<Subject> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            etAddCode.setText("");
                            loadClass();
                        } else if (response.code() == 400) {
                            Toast.makeText(ClassroomActivity.this,
                                    getString(R.string.course_already_added), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClassroomActivity.this,
                                    getString(R.string.create_class_no_subject), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Subject> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassroomActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeCourse(int subjectId) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .removeCourse(token, subjectId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            loadClass();
                        } else {
                            Toast.makeText(ClassroomActivity.this, "Couldn't remove. Try again.",
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

    private void promptDelete() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint(getString(R.string.hint_password));
        int pad = dp(12);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(20), pad, dp(20), 0);
        wrap.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.class_delete_title))
                .setMessage(getString(R.string.class_delete_message) + "\n\n"
                        + getString(R.string.class_delete_password_label))
                .setView(wrap)
                .setPositiveButton(getString(R.string.class_delete),
                        (d, w) -> deleteClass(input.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteClass(String password) {
        if (password == null || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.class_delete_wrong_password), Toast.LENGTH_SHORT).show();
            return;
        }
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .deleteClass(token, new DeleteClassRequest(password))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(ClassroomActivity.this, "Class deleted", Toast.LENGTH_SHORT).show();
                            loadClass();
                        } else if (response.code() == 400) {
                            Toast.makeText(ClassroomActivity.this,
                                    getString(R.string.class_delete_wrong_password), Toast.LENGTH_SHORT).show();
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

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String t = name.trim();
        return t.length() >= 2 ? t.substring(0, 2).toUpperCase() : t.toUpperCase();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
