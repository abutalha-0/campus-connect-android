package com.campusconnect.app.core.base;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.ui.ComingSoonActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base for every logged-in screen that should carry the app's navigation
 * chrome: the side drawer and the Home/Discover/Profile bottom bar.
 *
 * Subclasses call {@link #setContentView(int)} with their own layout exactly as
 * before — this class quietly inflates {@code activity_nav_shell} as the real
 * content view and drops the screen's layout into it. Screens that must stay
 * bare (login, register, onboarding, role selection) keep extending
 * {@link BaseActivity} instead.
 */
public abstract class NavShellActivity extends BaseActivity {

    /** Which bottom-nav tab HomeActivity should open on. */
    public static final String EXTRA_NAV_ITEM = "nav_shell_item";

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private FrameLayout contentContainer;

    private View drawerHome, drawerProfile;
    private ImageView ivDrawerHomeIcon, ivDrawerProfileIcon;
    private TextView tvDrawerHomeLabel, tvDrawerProfileLabel;

    private TextView tvDrawerName, tvDrawerSubtitle, tvDrawerAvatarInitials;
    private ImageView ivDrawerAvatar;

    // ── Content view plumbing ─────────────────────────────────────────────

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        inflateShell();
        LayoutInflater.from(this).inflate(layoutResID, contentContainer, true);
        onShellContentReady();
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        inflateShell();
        contentContainer.addView(view, params);
        onShellContentReady();
    }

    private void inflateShell() {
        if (contentContainer != null) {
            // A screen called setContentView twice — reuse the shell and
            // replace whatever it was hosting.
            contentContainer.removeAllViews();
            return;
        }
        super.setContentView(R.layout.activity_nav_shell);
        drawerLayout = findViewById(R.id.drawerLayout);
        bottomNav = findViewById(R.id.bottomNav);
        contentContainer = findViewById(R.id.navShellContent);
    }

    /** Runs once the hosted layout is in place, so drawer/nav can be wired. */
    private void onShellContentReady() {
        setUpDrawer();
        bindMenuButton();
        padDrawerForNavigationBar();
        hideBottomNavWithKeyboard();
        setUpBottomNav();
        setUpDrawerBackHandling();
    }

    // ── Bottom navigation ─────────────────────────────────────────────────

    private void setUpBottomNav() {
        int selected = getSelectedNavItem();
        if (selected != 0) {
            // setSelectedItemId fires the listener, so set it before attaching.
            bottomNav.setSelectedItemId(selected);
        } else {
            clearBottomNavSelection();
        }
        setActiveDrawerItem(selected);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == getSelectedNavItem()) return true;
            return onNavItemSelected(id);
        });
    }

    /**
     * A BottomNavigationView checks its first item by default. On drill-in
     * screens that would falsely light up "Home", so clear the whole group.
     */
    private void clearBottomNavSelection() {
        Menu menu = bottomNav.getMenu();
        menu.setGroupCheckable(0, true, false);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
        menu.setGroupCheckable(0, true, true);
    }

    /** Drives the bottom bar programmatically; routes through the listener. */
    protected void setSelectedNavItem(@IdRes int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    /**
     * The tab this screen represents, or {@code 0} (the default) for drill-in
     * screens that aren't a tab — nothing is highlighted for those.
     */
    @IdRes
    protected int getSelectedNavItem() {
        return 0;
    }

    /**
     * Handles a bottom-nav tap. Drill-in screens hand off to the home screen
     * with the stack cleared, so tabs never pile up activities. HomeActivity
     * overrides this to swap fragments in place instead.
     */
    protected boolean onNavItemSelected(@IdRes int itemId) {
        Intent intent = new Intent(this, com.campusconnect.app.home.HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_NAV_ITEM, itemId);
        startActivity(intent);
        return true;
    }

    /**
     * On form screens the bottom bar would otherwise ride up on top of the
     * keyboard and cover the field being typed into.
     */
    private void hideBottomNavWithKeyboard() {
        View divider = findViewById(R.id.navShellDivider);
        ViewCompat.setOnApplyWindowInsetsListener(contentContainer, (v, insets) -> {
            boolean keyboardUp = insets.isVisible(WindowInsetsCompat.Type.ime());
            int visibility = keyboardUp ? View.GONE : View.VISIBLE;
            bottomNav.setVisibility(visibility);
            divider.setVisibility(visibility);
            return insets;
        });
    }

    // ── Drawer ────────────────────────────────────────────────────────────

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
            closeDrawer();
            bottomNav.setSelectedItemId(R.id.nav_home);
        });

        drawerProfile.setOnClickListener(v -> {
            closeDrawer();
            bottomNav.setSelectedItemId(R.id.nav_profile);
        });

        findViewById(R.id.drawerSettings).setOnClickListener(v -> {
            closeDrawer();
            ComingSoonActivity.start(this, getString(R.string.label_settings),
                    R.drawable.ic_settings,
                    getResources().getColor(R.color.color_cyan, null));
        });

        findViewById(R.id.drawerLogout).setOnClickListener(v -> logout());

        renderCachedDrawerHeader();
    }

    /**
     * Reveals and hooks up the header hamburger for any hosted layout that has
     * one. Those buttons ship as {@code visibility="gone"} so that shared bars
     * like top_bar_edit — which drawer-less screens (the faculty edit screens)
     * also include — don't show a button that opens nothing.
     *
     * HomeActivity has no hamburger here: its header lives in the tab
     * fragments, which open the drawer themselves once their view exists.
     */
    private void bindMenuButton() {
        View menuButton = contentContainer.findViewById(R.id.btnMenu);
        if (menuButton != null) {
            menuButton.setVisibility(View.VISIBLE);
            menuButton.setOnClickListener(v -> openDrawer());
        }
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

    private void setUpDrawerBackHandling() {
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

    /**
     * Highlights whichever bottom-nav destination the drawer currently reflects.
     * Nothing lights up on Discover or on drill-in screens — the drawer only
     * has Home and Profile entries.
     */
    protected void setActiveDrawerItem(@IdRes int navId) {
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

    /**
     * Called by HomeFragment once it has fresh profile data. The values are
     * cached so every other screen's drawer can render immediately without
     * its own profile fetch.
     */
    public void updateDrawerHeader(String name, @Nullable String username,
                                   @Nullable String photoUrl) {
        tokenManager.saveProfileSummary(name, username, photoUrl);
        bindDrawerHeader(name, username, photoUrl);
    }

    private void renderCachedDrawerHeader() {
        bindDrawerHeader(tokenManager.getProfileName(),
                tokenManager.getProfileUsername(),
                tokenManager.getProfilePhoto());
    }

    private void bindDrawerHeader(@Nullable String name, @Nullable String username,
                                  @Nullable String photoUrl) {
        if (name == null) name = "";
        tvDrawerName.setText(name);
        tvDrawerSubtitle.setText(username != null && !username.isEmpty() ? "@" + username : "");
        tvDrawerAvatarInitials.setText(initialsOf(name));

        if (photoUrl != null && !photoUrl.isEmpty()) {
            ivDrawerAvatar.setVisibility(View.VISIBLE);
            Glide.with(this).load(photoUrl).centerCrop().into(ivDrawerAvatar);
        } else {
            ivDrawerAvatar.setVisibility(View.GONE);
        }
    }

    private String initialsOf(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length && initials.length() < 2; i++) {
            if (!parts[i].isEmpty()) initials.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return initials.toString();
    }

    public void openDrawer() {
        drawerLayout.openDrawer(Gravity.START);
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(Gravity.START);
    }
}
