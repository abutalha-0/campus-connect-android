package com.campusconnect.app.classroom;

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
import com.campusconnect.app.classroom.model.CreateClassRequest;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** "Create Class" — draft courses by secret code, then create the class. */
public class CreateClassActivity extends BaseActivity {

    private static final int[] PALETTE = {
            0xFF22D3EE, 0xFFA855F7, 0xFFF59E0B, 0xFFF87171, 0xFF4ADE80
    };

    private LinearLayout draftContainer;
    private EditText etCode;
    private TextView btnCreate, btnAddCode;

    private final List<Subject> draft = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        draftContainer = findViewById(R.id.draftContainer);
        etCode = findViewById(R.id.etCode);
        btnCreate = findViewById(R.id.btnCreate);
        btnAddCode = findViewById(R.id.btnAddCode);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnAddCode.setOnClickListener(v -> lookupAndAdd());
        btnCreate.setOnClickListener(v -> createClass());

        renderDraft();
    }

    private void lookupAndAdd() {
        String code = etCode.getText().toString().trim();
        if (code.isEmpty()) return;

        for (Subject s : draft) {
            if (code.equals(s.getCode())) {
                Toast.makeText(this, getString(R.string.course_already_added), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnAddCode.setEnabled(false);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .lookup(token, code)
                .enqueue(new Callback<Subject>() {
                    @Override
                    public void onResponse(Call<Subject> call, Response<Subject> response) {
                        btnAddCode.setEnabled(true);
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            draft.add(response.body());
                            etCode.setText("");
                            renderDraft();
                        } else {
                            Toast.makeText(CreateClassActivity.this,
                                    getString(R.string.create_class_no_subject), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Subject> call, Throwable t) {
                        btnAddCode.setEnabled(true);
                        if (isFinishing()) return;
                        Toast.makeText(CreateClassActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderDraft() {
        draftContainer.removeAllViews();
        for (int i = 0; i < draft.size(); i++) {
            Subject s = draft.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.item_course, draftContainer, false);

            int color = PALETTE[i % PALETTE.length];
            TextView badge = row.findViewById(R.id.tvBadge);
            badge.setText(initialsOf(s.getName()));
            badge.setTextColor(color);
            badge.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x24000000));

            ((TextView) row.findViewById(R.id.tvCourseName)).setText(s.getName());
            ((TextView) row.findViewById(R.id.tvCourseCode)).setText("Code: " + s.getCode());

            row.findViewById(R.id.btnRemove).setOnClickListener(v -> {
                draft.remove(s);
                renderDraft();
            });
            draftContainer.addView(row);
        }
        btnCreate.setText(draft.isEmpty()
                ? "Create Class (add subjects later)"
                : "Create Class (" + draft.size() + ")");
    }

    private void createClass() {
        List<String> codes = new ArrayList<>();
        for (Subject s : draft) codes.add(s.getCode());

        btnCreate.setEnabled(false);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ClassApiService.class)
                .createClass(token, new CreateClassRequest(codes))
                .enqueue(new Callback<Classroom>() {
                    @Override
                    public void onResponse(Call<Classroom> call, Response<Classroom> response) {
                        if (isFinishing()) return;
                        btnCreate.setEnabled(true);
                        if (response.isSuccessful()) {
                            finish();  // ClassroomActivity will show the new class
                        } else if (response.code() == 403) {
                            new androidx.appcompat.app.AlertDialog.Builder(CreateClassActivity.this)
                                    .setTitle(getString(R.string.class_create_not_cr_title))
                                    .setMessage(getString(R.string.class_create_not_cr))
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            Toast.makeText(CreateClassActivity.this,
                                    getString(R.string.create_class_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Classroom> call, Throwable t) {
                        if (isFinishing()) return;
                        btnCreate.setEnabled(true);
                        Toast.makeText(CreateClassActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String t = name.trim();
        return t.length() >= 2 ? t.substring(0, 2).toUpperCase() : t.toUpperCase();
    }
}
