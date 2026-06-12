package com.campusconnect.app.core.base;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.campusconnect.app.core.utils.TokenManager;

public abstract class BaseActivity extends AppCompatActivity {

    protected TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);
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