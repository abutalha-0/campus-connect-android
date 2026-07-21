package com.campusconnect.app.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.ui.ComingSoonActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;

    private View drawerHome, drawerProfile;
    private ImageView ivDrawerHomeIcon, ivDrawerProfileIcon;
    private TextView tvDrawerHomeLabel, tvDrawerProfileLabel;

    private TextView tvDrawerName, tvDrawerSubtitle, tvDrawerAvatarInitials;
    private ImageView ivDrawerAvatar;

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
        padDrawerForNavigationBar();
        setActiveDrawerItem(R.id.nav_home);

        // load HomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                setActiveDrawerItem(R.id.nav_home);
                return true;
            } else if (id == R.id.nav_discover) {
                loadFragment(new DiscoverFragment());
                setActiveDrawerItem(R.id.nav_discover);
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                setActiveDrawerItem(R.id.nav_profile);
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
        drawerHome = findViewById(R.id.drawerHome);
        drawerProfile = findViewById(R.id.drawerProfile);
        ivDrawerHomeIcon = findViewById(R.id.ivDrawerHomeIcon);
        ivDrawerProfileIcon = findViewById(R.id.ivDrawerProfileIcon);
        tvDrawerHomeLabel = findViewById(R.id.tvDrawerHomeLabel);
        tvDrawerProfileLabel = findViewById(R.id.tvDrawerProfileLabel);

        tvDrawerName = findViewById(R.id.tvDrawerName);
        tvDrawerSubtitle = findViewById(R.id.tvDrawerSubtitle);
        tvDrawerAvatarInitials = findViewById(R.id.tvDrawerAvatarInitials);
        ivDrawerAvatar = findViewById(R.id.ivDrawerAvatar);

        drawerHome.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_home);
            closeDrawer();
        });

        drawerProfile.setOnClickListener(v -> {
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

    /**
     * The drawer panel is match_parent height inside the DrawerLayout, so on
     * newer Android versions (edge-to-edge enforced from targetSdk 35+) its
     * bottom content renders under the system navigation bar unless we
     * reserve real space for it — the legacy fitsSystemWindows theme trick
     * no longer does this reliably on its own.
     */
    private void padDrawerForNavigationBar() {
        View drawerPanel = findViewById(R.id.drawerPanel);
        int basePaddingBottom = drawerPanel.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(drawerPanel, (v, insets) -> {
            int navBarBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    basePaddingBottom + navBarBottom);
            return insets;
        });
    }

    /**
     * Highlights whichever bottom-nav destination the drawer currently reflects.
     * Neither Home nor Profile lights up when Discover is selected — the
     * drawer doesn't have its own Discover entry.
     */
    private void setActiveDrawerItem(int navId) {
        styleDrawerItem(drawerHome, ivDrawerHomeIcon, tvDrawerHomeLabel, navId == R.id.nav_home);
        styleDrawerItem(drawerProfile, ivDrawerProfileIcon, tvDrawerProfileLabel, navId == R.id.nav_profile);
    }

    private void styleDrawerItem(View row, ImageView icon, TextView label, boolean active) {
        @ColorInt int color = getResources().getColor(
                active ? R.color.color_cyan : R.color.color_text_primary, null);
        row.setBackgroundResource(active
                ? R.drawable.bg_drawer_item_active
                : R.drawable.bg_drawer_item_ripple);
        icon.setImageTintList(ColorStateList.valueOf(color));
        label.setTextColor(color);
    }

    /** Called by HomeFragment once it has real profile data to show. */
    public void updateDrawerHeader(String name, @Nullable String username,
                                    @Nullable String photoUrl) {
        tvDrawerName.setText(name);
        tvDrawerSubtitle.setText(username != null ? "@" + username : "");

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length && initials.length() < 2; i++) {
            if (!parts[i].isEmpty()) initials.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        tvDrawerAvatarInitials.setText(initials.toString());

        if (photoUrl != null && !photoUrl.isEmpty()) {
            ivDrawerAvatar.setVisibility(View.VISIBLE);
            Glide.with(this).load(photoUrl).centerCrop().into(ivDrawerAvatar);
        }
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
