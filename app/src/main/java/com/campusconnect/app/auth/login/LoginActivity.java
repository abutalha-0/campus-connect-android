package com.campusconnect.app.auth.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.auth.AuthApiService;
import com.campusconnect.app.auth.AuthResponse;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.ApiError;
import com.campusconnect.app.core.utils.PasswordToggle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if already logged in skip to home
        if (tokenManager.hasToken()) {
            goToRoleHome();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        PasswordToggle.attach(etPassword, findViewById(R.id.btnTogglePassword));

        btnLogin.setOnClickListener(v -> handleLogin());
        // Login is always reached by navigating forward from RoleSelection,
        // so finishing here naturally reveals it again underneath — same
        // destination as the back button, letting the user pick Student or
        // Faculty instead of assuming Student.
        tvGoToRegister.setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);

        RetrofitClient.createService(AuthApiService.class)
                .login(request)
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
                            tokenManager.saveRole(auth.getUser().getRole());
                            goToRoleHome();
                        } else {
                            String message = ApiError.extract(response, getString(R.string.error_credentials));
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? getString(R.string.loading) : getString(R.string.btn_login));
    }
}