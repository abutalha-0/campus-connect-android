package com.campusconnect.app.profile;

import android.os.Bundle;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.home.ProfileFragment;

/**
 * Thin host for ProfileFragment in read-only mode — see
 * ProfileFragment.newInstance(userId). Keeping the actual profile UI in one
 * place means this screen always matches the own-profile screen's design.
 */
public class PublicProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        int userId = getIntent().getIntExtra("user_id", -1);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, ProfileFragment.newInstance(userId))
                    .commit();
        }
    }
}
