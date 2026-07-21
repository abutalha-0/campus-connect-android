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
import com.campusconnect.app.profile.models.Project;
import com.campusconnect.app.profile.models.ProjectRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Full-page Add/Edit Project — pass an editing id via startEdit() to switch to update mode. */
public class AddProjectActivity extends BaseActivity {

    private static final String EXTRA_ID = "id";
    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_ASSOCIATED = "associated";
    private static final String EXTRA_DESCRIPTION = "description";

    public static void start(Context context) {
        context.startActivity(new Intent(context, AddProjectActivity.class));
    }

    public static void startEdit(Context context, Project project) {
        Intent intent = new Intent(context, AddProjectActivity.class);
        intent.putExtra(EXTRA_ID, project.getId());
        intent.putExtra(EXTRA_NAME, project.getName());
        intent.putExtra(EXTRA_ASSOCIATED, project.getAssociatedWith());
        intent.putExtra(EXTRA_DESCRIPTION, project.getDescription());
        context.startActivity(intent);
    }

    private EditText etName, etAssociatedWith, etDescription;
    private int editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        etName = findViewById(R.id.etProjectName);
        etAssociatedWith = findViewById(R.id.etAssociatedWith);
        etDescription = findViewById(R.id.etDescription);

        editingId = getIntent().getIntExtra(EXTRA_ID, -1);
        boolean isEdit = editingId != -1;

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(isEdit ? "Edit Project" : "Add Project");
        ((TextView) findViewById(R.id.btnSave)).setText(isEdit ? "Save" : "Add");

        if (isEdit) {
            etName.setText(getIntent().getStringExtra(EXTRA_NAME));
            etAssociatedWith.setText(getIntent().getStringExtra(EXTRA_ASSOCIATED));
            etDescription.setText(getIntent().getStringExtra(EXTRA_DESCRIPTION));
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String assoc = etAssociatedWith.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Project name is required");
            return;
        }

        boolean isEdit = editingId != -1;
        TextView btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText(isEdit ? "Saving…" : "Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        ProjectRequest body = new ProjectRequest(name, assoc, desc);
        ProfileApiService service = RetrofitClient.createService(ProfileApiService.class);
        Call<Project> call = isEdit ? service.updateProject(token, editingId, body)
                                     : service.addProject(token, body);

        call.enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddProjectActivity.this,
                            isEdit ? "Project updated!" : "Project added!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSave.setEnabled(true);
                    btnSave.setText(isEdit ? "Save" : "Add");
                    Toast.makeText(AddProjectActivity.this,
                            isEdit ? "Failed to update project. Try again."
                                   : "Failed to add project. Try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                Toast.makeText(AddProjectActivity.this, getString(R.string.error_network),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
