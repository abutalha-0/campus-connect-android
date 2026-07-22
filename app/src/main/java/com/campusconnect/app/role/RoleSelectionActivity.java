package com.campusconnect.app.role;

import android.content.Intent;
import android.os.Bundle;
import com.campusconnect.app.R;
import com.campusconnect.app.auth.login.LoginActivity;
import com.campusconnect.app.auth.register.RegisterActivity;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.faculty.auth.FacultyRegisterActivity;

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
            goToRoleHome();
            return;
        }

        setContentView(R.layout.activity_role_selection);

        // Both role cards lead to their sign-up screen. Existing users of
        // either role log in through the single shared "Log in" footer.
        findViewById(R.id.cardStudent).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.cardFaculty).setOnClickListener(v ->
                startActivity(new Intent(this, FacultyRegisterActivity.class)));
        findViewById(R.id.tvLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}
