package com.campusconnect.app.classroom;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.classroom.model.SubjectRequest;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Add a subject; on success shows the generated share code with a copy action. */
public class AddSubjectActivity extends BaseActivity {

    private EditText etName, etIntake, etSection, etRoom;
    private TextView btnAddSubject, codeCardCode;
    private View codeCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        etName = findViewById(R.id.etName);
        etIntake = findViewById(R.id.etIntake);
        etSection = findViewById(R.id.etSection);
        etRoom = findViewById(R.id.etRoom);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        codeCard = findViewById(R.id.codeCard);
        codeCardCode = findViewById(R.id.tvCode);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnAddSubject.setOnClickListener(v -> addSubject());
    }

    private void addSubject() {
        String name = etName.getText().toString().trim();
        String intake = etIntake.getText().toString().trim();
        String section = etSection.getText().toString().trim();
        String room = etRoom.getText().toString().trim();

        if (name.isEmpty() || intake.isEmpty() || section.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddSubject.setEnabled(false);
        btnAddSubject.setText(getString(R.string.loading));

        SubjectRequest body = new SubjectRequest(name, intake, section, room);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RetrofitClient.createService(SubjectApiService.class)
                .addSubject(token, body)
                .enqueue(new Callback<Subject>() {
                    @Override
                    public void onResponse(Call<Subject> call, Response<Subject> response) {
                        btnAddSubject.setEnabled(true);
                        btnAddSubject.setText(getString(R.string.subject_add_btn));
                        if (response.isSuccessful() && response.body() != null) {
                            showCode(response.body().getCode());
                            clearForm();
                        } else if (response.code() == 403) {
                            // Account not verified (or not a faculty account).
                            Toast.makeText(AddSubjectActivity.this,
                                    getString(R.string.subject_not_verified), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AddSubjectActivity.this,
                                    getString(R.string.subject_add_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Subject> call, Throwable t) {
                        btnAddSubject.setEnabled(true);
                        btnAddSubject.setText(getString(R.string.subject_add_btn));
                        Toast.makeText(AddSubjectActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCode(String code) {
        codeCardCode.setText(code);
        codeCard.setVisibility(View.VISIBLE);
        TextView btnCopy = findViewById(R.id.btnCopyCode);
        btnCopy.setText(getString(R.string.subject_copy));
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("Subject code", code));
                btnCopy.setText(getString(R.string.subject_copied));
            }
        });
    }

    private void clearForm() {
        etName.setText("");
        etIntake.setText("");
        etSection.setText("");
        etRoom.setText("");
    }
}
