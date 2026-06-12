package com.campusconnect.app.home;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends BaseActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNav = findViewById(R.id.bottomNav);

        // load ProfileFragment by default
        if (savedInstanceState == null) {
            loadFragment(new ProfileFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            } else if (id == R.id.nav_discover) {
                loadFragment(new DiscoverFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}