package com.campusconnect.app.role;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.auth.login.LoginActivity;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.home.HomeActivity;

/**
 * "Who are you?" landing screen shown right after onboarding.
 *
 *  - Student  → LoginActivity (the real sign-in flow)
 *  - Faculty  → coming-soon message (no backend yet)
 *  - Log in   → LoginActivity
 *
 * A returning, already-authenticated user is bounced straight to Home so the
 * role picker never gets in the way of an existing session.
 */
public class RoleSelectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (tokenManager.hasToken()) {
            goToHome();
            return;
        }

        setContentView(R.layout.activity_role_selection);

        findViewById(R.id.cardStudent).setOnClickListener(v -> goToLogin());
        findViewById(R.id.tvLogin).setOnClickListener(v -> goToLogin());
        findViewById(R.id.cardFaculty).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.role_faculty_coming_soon),
                        Toast.LENGTH_SHORT).show());
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
