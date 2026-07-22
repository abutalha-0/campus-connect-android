package com.campusconnect.app.classroom;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.classroom.model.SubjectRequest;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** View the subject share code and update or delete the subject. */
public class SubjectSettingsActivity extends BaseActivity {

    private static final String EXTRA_SUBJECT_ID = "subject_id";

    public static Intent createIntent(Context ctx, int subjectId) {
        Intent i = new Intent(ctx, SubjectSettingsActivity.class);
        i.putExtra(EXTRA_SUBJECT_ID, subjectId);
        return i;
    }

    private int subjectId;
    private String code = "";
    private EditText etName, etIntake, etSection, etRoom;
    private TextView tvCode, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_settings);

        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(R.string.subject_settings_title));
        findViewById(R.id.btnSave).setVisibility(android.view.View.GONE); // top-bar Save unused

        tvCode = findViewById(R.id.tvCode);
        etName = findViewById(R.id.etName);
        etIntake = findViewById(R.id.etIntake);
        etSection = findViewById(R.id.etSection);
        etRoom = findViewById(R.id.etRoom);
        btnSave = findViewById(R.id.btnSaveSubject);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        findViewById(R.id.btnCopyCode).setOnClickListener(v -> copyCode());
        btnSave.setOnClickListener(v -> save());
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());

        loadSubject();
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
                            Subject s = response.body();
                            code = s.getCode();
                            tvCode.setText(code);
                            etName.setText(s.getName());
                            etIntake.setText(s.getIntake());
                            etSection.setText(s.getSection());
                            etRoom.setText(s.getRoom());
                        }
                    }

                    @Override
                    public void onFailure(Call<Subject> call, Throwable t) {
                        // fields stay empty
                    }
                });
    }

    private void copyCode() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null && code != null && !code.isEmpty()) {
            cm.setPrimaryClip(ClipData.newPlainText("Subject code", code));
            ((TextView) findViewById(R.id.btnCopyCode)).setText(getString(R.string.subject_copied));
        }
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String intake = etIntake.getText().toString().trim();
        String section = etSection.getText().toString().trim();
        String room = etRoom.getText().toString().trim();

        if (name.isEmpty() || intake.isEmpty() || section.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(SubjectApiService.class)
                .updateSubject(token, subjectId, new SubjectRequest(name, intake, section, room))
                .enqueue(new Callback<Subject>() {
                    @Override
                    public void onResponse(Call<Subject> call, Response<Subject> response) {
                        if (isFinishing()) return;
                        btnSave.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            Intent data = new Intent();
                            data.putExtra("name", response.body().getName());
                            setResult(RESULT_OK, data);
                            Toast.makeText(SubjectSettingsActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SubjectSettingsActivity.this,
                                    getString(R.string.subject_update_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Subject> call, Throwable t) {
                        if (isFinishing()) return;
                        btnSave.setEnabled(true);
                        Toast.makeText(SubjectSettingsActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.subject_delete_title))
                .setMessage(getString(R.string.subject_delete_message))
                .setPositiveButton("Delete", (d, w) -> deleteSubject())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSubject() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(SubjectApiService.class)
                .deleteSubject(token, subjectId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Intent data = new Intent();
                            data.putExtra("deleted", true);
                            setResult(RESULT_OK, data);
                            Toast.makeText(SubjectSettingsActivity.this,
                                    getString(R.string.subject_deleted), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SubjectSettingsActivity.this,
                                    "Couldn't delete. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(SubjectSettingsActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
