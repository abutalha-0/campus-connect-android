package com.campusconnect.app.profile.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.ExperienceRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Full-page Add/Edit Experience — pass an editing id via startEdit() to switch to update mode. */
public class AddExperienceActivity extends BaseActivity {

    private static final String EXTRA_ID = "id";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_ORG = "org";
    private static final String EXTRA_START = "start";
    private static final String EXTRA_END = "end";
    private static final String EXTRA_DESCRIPTION = "description";

    public static void start(Context context) {
        context.startActivity(new Intent(context, AddExperienceActivity.class));
    }

    public static void startEdit(Context context, Experience experience) {
        Intent intent = new Intent(context, AddExperienceActivity.class);
        intent.putExtra(EXTRA_ID, experience.getId());
        intent.putExtra(EXTRA_TITLE, experience.getTitle());
        intent.putExtra(EXTRA_ORG, experience.getOrganization());
        intent.putExtra(EXTRA_START, experience.getStartDate());
        intent.putExtra(EXTRA_END, experience.getEndDate());
        intent.putExtra(EXTRA_DESCRIPTION, experience.getDescription());
        context.startActivity(intent);
    }

    private EditText etTitle, etOrganization, etStartDate, etEndDate, etDescription;
    private int editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_experience);

        etTitle = findViewById(R.id.etTitle);
        etOrganization = findViewById(R.id.etOrganization);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etDescription = findViewById(R.id.etDescription);

        editingId = getIntent().getIntExtra(EXTRA_ID, -1);
        boolean isEdit = editingId != -1;

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(isEdit ? "Edit Experience" : "Add Experience");
        ((TextView) findViewById(R.id.btnSave)).setText(isEdit ? "Save" : "Add");

        if (isEdit) {
            etTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
            etOrganization.setText(getIntent().getStringExtra(EXTRA_ORG));
            etStartDate.setText(getIntent().getStringExtra(EXTRA_START));
            etEndDate.setText(getIntent().getStringExtra(EXTRA_END));
            etDescription.setText(getIntent().getStringExtra(EXTRA_DESCRIPTION));
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String org   = etOrganization.getText().toString().trim();
        String start = etStartDate.getText().toString().trim();
        String end   = etEndDate.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();

        if (title.isEmpty()) { etTitle.setError("Title is required"); return; }
        if (org.isEmpty())   { etOrganization.setError("Organization is required"); return; }
        if (start.isEmpty()) { etStartDate.setError("Start date is required (YYYY-MM-DD)"); return; }
        if (!start.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etStartDate.setError("Use format YYYY-MM-DD");
            return;
        }
        if (!end.isEmpty() && !end.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etEndDate.setError("Use format YYYY-MM-DD");
            return;
        }

        boolean isEdit = editingId != -1;
        TextView btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText(isEdit ? "Saving…" : "Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        String endOrNull = end.isEmpty() ? null : end;
        ExperienceRequest body = new ExperienceRequest(title, org, desc, start, endOrNull);
        ProfileApiService service = RetrofitClient.createService(ProfileApiService.class);
        Call<Experience> call = isEdit ? service.updateExperience(token, editingId, body)
                                        : service.addExperience(token, body);

        call.enqueue(new Callback<Experience>() {
            @Override
            public void onResponse(Call<Experience> call, Response<Experience> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddExperienceActivity.this,
                            isEdit ? "Experience updated!" : "Experience added!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSave.setEnabled(true);
                    btnSave.setText(isEdit ? "Save" : "Add");
                    Toast.makeText(AddExperienceActivity.this,
                            isEdit ? "Failed to update experience. Try again."
                                   : "Failed to add experience. Try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Experience> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                Toast.makeText(AddExperienceActivity.this, getString(R.string.error_network),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
