package com.campusconnect.app.home;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.ui.ComingSoonActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // push content below status bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        bottomNav = findViewById(R.id.bottomNav);

        setUpDrawer();

        // load HomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(Gravity.START)) {
                    closeDrawer();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setUpDrawer() {
        findViewById(R.id.drawerHome).setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_home);
            closeDrawer();
        });

        findViewById(R.id.drawerProfile).setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_profile);
            closeDrawer();
        });

        findViewById(R.id.drawerSettings).setOnClickListener(v -> {
            closeDrawer();
            ComingSoonActivity.start(this, getString(R.string.label_settings),
                    R.drawable.ic_settings,
                    getResources().getColor(R.color.color_cyan, null));
        });

        findViewById(R.id.drawerLogout).setOnClickListener(v -> logout());
    }

    public void openDrawer() {
        drawerLayout.openDrawer(Gravity.START);
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(Gravity.START);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
