package com.campusconnect.app.faculty.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.auth.AuthResponse;
import com.campusconnect.app.auth.login.LoginActivity;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.faculty.FacultyApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacultyRegisterActivity extends BaseActivity {

    private EditText etName, etEmail, etEmployeeId, etDepartment, etPassword, etConfirmPassword;
    private Spinner spinnerDesignation;
    private TextView tvMismatch;
    private Button btnCreateAccount;

    private String[] designationKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (tokenManager.hasToken()) {
            goToRoleHome();
            return;
        }

        setContentView(R.layout.activity_faculty_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etEmployeeId = findViewById(R.id.etEmployeeId);
        etDepartment = findViewById(R.id.etDepartment);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerDesignation = findViewById(R.id.spinnerDesignation);
        tvMismatch = findViewById(R.id.tvMismatch);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        designationKeys = getResources().getStringArray(R.array.faculty_designation_keys);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.faculty_designations, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerDesignation.setAdapter(adapter);
        // Default to "Assistant Professor" as in the design.
        spinnerDesignation.setSelection(1);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCreateAccount.setOnClickListener(v -> handleRegister());
        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String employeeId = etEmployeeId.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();
        String designation = designationKeys[spinnerDesignation.getSelectedItemPosition()];

        if (name.isEmpty() || email.isEmpty() || employeeId.isEmpty()
                || department.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            tvMismatch.setVisibility(TextView.VISIBLE);
            return;
        }
        tvMismatch.setVisibility(TextView.GONE);

        setLoading(true);

        FacultyRegisterRequest request = new FacultyRegisterRequest(
                name, email, employeeId, department, designation, password);

        RetrofitClient.createService(FacultyApiService.class)
                .register(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            tokenManager.saveTokens(
                                    auth.getTokens().getAccess(),
                                    auth.getTokens().getRefresh());
                            tokenManager.saveRole(auth.getUser().getRole());
                            Toast.makeText(FacultyRegisterActivity.this,
                                    "Account created! Welcome to Campus Connect.",
                                    Toast.LENGTH_SHORT).show();
                            goToRoleHome();
                        } else {
                            Toast.makeText(FacultyRegisterActivity.this,
                                    getString(R.string.faculty_signup_failed),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(FacultyRegisterActivity.this,
                                getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
        btnCreateAccount.setText(loading
                ? getString(R.string.loading)
                : getString(R.string.faculty_create_account));
    }
}
