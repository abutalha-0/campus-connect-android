package com.campusconnect.app.classroom;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.campusconnect.app.core.utils.ProfileNavigator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Class settings: creator + class code (all); course management/delete (creator); leave (member). */
public class ClassSettingsActivity extends BaseActivity {

    private static final int[] PALETTE = {
            0xFF22D3EE, 0xFFA855F7, 0xFFF59E0B, 0xFFF87171, 0xFF4ADE80
    };

    private TextView tvClassCode, tvCreatorName, tvCreatorInitial;
    private LinearLayout coursesContainer;
    private EditText etAddCode;
    private View managementSection, btnLeaveClass;

    private String classCode = "";
    private boolean isCreator = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_settings);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(R.string.class_settings_title));
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        tvClassCode = findViewById(R.id.tvClassCode);
        tvCreatorName = findViewById(R.id.tvCreatorName);
        tvCreatorInitial = findViewById(R.id.tvCreatorInitial);
        coursesContainer = findViewById(R.id.coursesContainer);
        etAddCode = findViewById(R.id.etAddCode);
        managementSection = findViewById(R.id.managementSection);
        btnLeaveClass = findViewById(R.id.btnLeaveClass);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        findViewById(R.id.btnCopyCode).setOnClickListener(v -> copyCode());
        findViewById(R.id.btnAddCourse).setOnClickListener(v -> addCourse());
        findViewById(R.id.btnDeleteClass).setOnClickListener(v -> promptDelete());
        btnLeaveClass.setOnClickListener(v -> promptLeave());
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
                            populate(response.body());
                        } else {
                            finish();  // class no longer exists
                        }
                    }

                    @Override
                    public void onFailure(Call<Classroom> call, Throwable t) {
                        // keep showing whatever we have
                    }
                });
    }

    private void populate(Classroom classroom) {
        isCreator = classroom.isCreator();
        classCode = classroom.getCode();
        tvClassCode.setText(classCode);

        String creator = classroom.getCreatorName() != null ? classroom.getCreatorName() : "";
        tvCreatorName.setText(creator);
        tvCreatorInitial.setText(initialsOf(creator));
        View.OnClickListener openCreator = v ->
                ProfileNavigator.open(this, classroom.getCreatorId(), "STUDENT");
        tvCreatorName.setOnClickListener(openCreator);
        tvCreatorInitial.setOnClickListener(openCreator);

        managementSection.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        btnLeaveClass.setVisibility(isCreator ? View.GONE : View.VISIBLE);

        if (isCreator) {
            renderCourses(classroom.getSubjects());
        }
    }

    private void renderCourses(List<Subject> subjects) {
        coursesContainer.removeAllViews();
        if (subjects == null || subjects.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.class_no_courses));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, dp(12), 0, dp(14));
            coursesContainer.addView(empty);
            return;
        }
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.item_course, coursesContainer, false);

            int color = PALETTE[i % PALETTE.length];
            TextView badge = row.findViewById(R.id.tvBadge);
            badge.setText(twoInitials(s.getName()));
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
                            Toast.makeText(ClassSettingsActivity.this,
                                    getString(R.string.course_already_added), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClassSettingsActivity.this,
                                    getString(R.string.create_class_no_subject), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Subject> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassSettingsActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ClassSettingsActivity.this, "Couldn't remove. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassSettingsActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void promptDelete() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint(getString(R.string.hint_password));
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(20), dp(12), dp(20), 0);
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
                            Toast.makeText(ClassSettingsActivity.this, "Class deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (response.code() == 400) {
                            Toast.makeText(ClassSettingsActivity.this,
                                    getString(R.string.class_delete_wrong_password), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClassSettingsActivity.this, "Couldn't delete. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassSettingsActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void promptLeave() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.class_leave_title))
                .setMessage(getString(R.string.class_leave_message))
                .setPositiveButton(getString(R.string.class_leave), (d, w) -> leaveClass())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveClass() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .leaveClass(token)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(ClassSettingsActivity.this,
                                    getString(R.string.class_left), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ClassSettingsActivity.this, "Couldn't leave. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(ClassSettingsActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    private String twoInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String t = name.trim();
        return t.length() >= 2 ? t.substring(0, 2).toUpperCase() : t.toUpperCase();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
