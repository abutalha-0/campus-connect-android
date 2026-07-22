package com.campusconnect.app.faculty.edit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.faculty.FacultyApiService;
import com.campusconnect.app.faculty.model.FacultyProfile;
import com.campusconnect.app.faculty.model.FacultyProfileUpdateRequest;
import com.campusconnect.app.faculty.util.Designations;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Edit the faculty identity: name, designation, department. */
public class FacultyEditIdentityActivity extends BaseActivity {

    private EditText etName, etDepartment;
    private Spinner spinnerDesignation;
    private TextView btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_edit_identity);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText("Edit Identity");

        etName = findViewById(R.id.etName);
        etDepartment = findViewById(R.id.etDepartment);
        spinnerDesignation = findViewById(R.id.spinnerDesignation);
        btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.faculty_designations, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerDesignation.setAdapter(adapter);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());

        loadCurrent();
    }

    private void loadCurrent() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FacultyApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<FacultyProfile>() {
                    @Override
                    public void onResponse(Call<FacultyProfile> call, Response<FacultyProfile> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            FacultyProfile p = response.body();
                            etName.setText(p.getFullName());
                            etDepartment.setText(p.getDepartment());
                            spinnerDesignation.setSelection(
                                    Designations.indexOf(FacultyEditIdentityActivity.this, p.getDesignation()));
                        }
                    }

                    @Override
                    public void onFailure(Call<FacultyProfile> call, Throwable t) {
                        // fields stay empty; user can still type
                    }
                });
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String designation = Designations.keyAt(this, spinnerDesignation.getSelectedItemPosition());

        if (name.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        FacultyProfileUpdateRequest body =
                new FacultyProfileUpdateRequest(name, department, designation);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RetrofitClient.createService(FacultyApiService.class)
                .updateProfile(token, body)
                .enqueue(new Callback<FacultyProfile>() {
                    @Override
                    public void onResponse(Call<FacultyProfile> call, Response<FacultyProfile> response) {
                        btnSave.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(FacultyEditIdentityActivity.this, "Profile updated",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(FacultyEditIdentityActivity.this,
                                    "Couldn't save. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FacultyProfile> call, Throwable t) {
                        btnSave.setEnabled(true);
                        Toast.makeText(FacultyEditIdentityActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
