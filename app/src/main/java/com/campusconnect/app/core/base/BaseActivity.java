package com.campusconnect.app.core.base;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.faculty.FacultyHomeActivity;
import com.campusconnect.app.home.HomeActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);
    }

    /**
     * Sends the user to the correct home screen based on their saved role and
     * clears the back stack. Faculty land on the faculty home, everyone else
     * on the student home.
     */
    protected void goToRoleHome() {
        Class<?> target = tokenManager.isFaculty()
                ? FacultyHomeActivity.class
                : HomeActivity.class;
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected void logout() {
        tokenManager.clearTokens();
        Intent intent = new Intent(this, getLoginActivityClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected Class<?> getLoginActivityClass() {
        try {
            return Class.forName("com.campusconnect.app.auth.login.LoginActivity");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("LoginActivity not found", e);
        }
    }
}