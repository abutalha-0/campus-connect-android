package com.campusconnect.app.auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.auth.AuthApiService;
import com.campusconnect.app.auth.AuthResponse;
import com.campusconnect.app.auth.login.LoginActivity;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.home.HomeActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private EditText etFullName, etUsername, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegister());
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        RegisterRequest request = new RegisterRequest(email, username, fullName, password);

        RetrofitClient.createService(AuthApiService.class)
                .register(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            tokenManager.saveTokens(
                                    auth.getTokens().getAccess(),
                                    auth.getTokens().getRefresh()
                            );
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed. Email or username may already exist.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? getString(R.string.loading) : getString(R.string.btn_register));
    }
}