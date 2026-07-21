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
import com.campusconnect.app.profile.models.Education;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-page Add/Edit Education — pass an editing id via startEdit() to
 * switch to update mode. The endpoint is multipart/form-data (not JSON), so
 * every field gets wrapped as a text part.
 */
public class AddEducationActivity extends BaseActivity {

    private static final String EXTRA_ID = "id";
    private static final String EXTRA_INSTITUTION = "institution";
    private static final String EXTRA_DEGREE = "degree";
    private static final String EXTRA_START_YEAR = "start_year";
    private static final String EXTRA_END_YEAR = "end_year";

    public static void start(Context context) {
        context.startActivity(new Intent(context, AddEducationActivity.class));
    }

    public static void startEdit(Context context, Education education) {
        Intent intent = new Intent(context, AddEducationActivity.class);
        intent.putExtra(EXTRA_ID, education.getId());
        intent.putExtra(EXTRA_INSTITUTION, education.getInstitutionName());
        intent.putExtra(EXTRA_DEGREE, education.getDegree());
        intent.putExtra(EXTRA_START_YEAR, education.getStartYear());
        if (education.getEndYear() != null) {
            intent.putExtra(EXTRA_END_YEAR, education.getEndYear());
        }
        context.startActivity(intent);
    }

    private EditText etInstitution, etDegree, etStartYear, etEndYear;
    private int editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_education);

        etInstitution = findViewById(R.id.etInstitution);
        etDegree = findViewById(R.id.etDegree);
        etStartYear = findViewById(R.id.etStartYear);
        etEndYear = findViewById(R.id.etEndYear);

        editingId = getIntent().getIntExtra(EXTRA_ID, -1);
        boolean isEdit = editingId != -1;

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(isEdit ? "Edit Education" : "Add Education");
        ((TextView) findViewById(R.id.btnSave)).setText(isEdit ? "Save" : "Add");

        if (isEdit) {
            etInstitution.setText(getIntent().getStringExtra(EXTRA_INSTITUTION));
            etDegree.setText(getIntent().getStringExtra(EXTRA_DEGREE));
            etStartYear.setText(String.valueOf(getIntent().getIntExtra(EXTRA_START_YEAR, 0)));
            if (getIntent().hasExtra(EXTRA_END_YEAR)) {
                etEndYear.setText(String.valueOf(getIntent().getIntExtra(EXTRA_END_YEAR, 0)));
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private RequestBody textPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private void save() {
        String institution = etInstitution.getText().toString().trim();
        String degree       = etDegree.getText().toString().trim();
        String startYearStr = etStartYear.getText().toString().trim();
        String endYearStr   = etEndYear.getText().toString().trim();

        if (institution.isEmpty()) { etInstitution.setError("Institution name is required"); return; }
        if (degree.isEmpty())      { etDegree.setError("Degree is required"); return; }
        if (startYearStr.isEmpty()) { etStartYear.setError("Start year is required"); return; }

        int startYear;
        try {
            startYear = Integer.parseInt(startYearStr);
        } catch (NumberFormatException e) {
            etStartYear.setError("Enter a valid year, e.g. 2022");
            return;
        }

        Integer endYear = null;
        if (!endYearStr.isEmpty()) {
            try {
                endYear = Integer.parseInt(endYearStr);
            } catch (NumberFormatException e) {
                etEndYear.setError("Enter a valid year, e.g. 2026");
                return;
            }
        }

        boolean isEdit = editingId != -1;
        TextView btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText(isEdit ? "Saving…" : "Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RequestBody institutionPart = textPart(institution);
        RequestBody degreePart      = textPart(degree);
        RequestBody startYearPart   = textPart(String.valueOf(startYear));
        RequestBody endYearPart     = endYear != null ? textPart(String.valueOf(endYear)) : null;

        ProfileApiService service = RetrofitClient.createService(ProfileApiService.class);
        Call<Education> call = isEdit
                ? service.updateEducation(token, editingId, institutionPart, degreePart, startYearPart, endYearPart)
                : service.addEducation(token, institutionPart, degreePart, startYearPart, endYearPart);

        call.enqueue(new Callback<Education>() {
            @Override
            public void onResponse(Call<Education> call, Response<Education> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddEducationActivity.this,
                            isEdit ? "Education updated!" : "Education added!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSave.setEnabled(true);
                    btnSave.setText(isEdit ? "Save" : "Add");
                    Toast.makeText(AddEducationActivity.this,
                            isEdit ? "Failed to update education. Try again."
                                   : "Failed to add education. Try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Education> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                Toast.makeText(AddEducationActivity.this, getString(R.string.error_network),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
