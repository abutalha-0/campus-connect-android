package com.campusconnect.app.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;
import com.campusconnect.app.core.base.NavShellActivity;

public class HomeActivity extends NavShellActivity {

    // Route Mate lives inside this Activity's fragmentContainer (like Lost &
    // Found), not as its own Activity — so a notification pointing at a
    // route has to come through here rather than being launched directly.
    private static final String EXTRA_OPEN_ROUTE_MATE = "open_route_mate";
    private static final String EXTRA_OPEN_LOST_FOUND_ITEM_ID = "open_lost_found_item_id";
    private static final String EXTRA_OPEN_LOST_FOUND_LIST = "open_lost_found_list";

    public static Intent createRouteMateIntent(Context ctx) {
        Intent i = new Intent(ctx, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(EXTRA_OPEN_ROUTE_MATE, true);
        return i;
    }

    public static Intent createLostFoundDetailIntent(Context ctx, int itemId) {
        Intent i = new Intent(ctx, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(EXTRA_OPEN_LOST_FOUND_ITEM_ID, itemId);
        return i;
    }

    public static Intent createLostFoundListIntent(Context ctx) {
        Intent i = new Intent(ctx, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(EXTRA_OPEN_LOST_FOUND_LIST, true);
        return i;
    }

    /** Home hosts the tabs as fragments, so it starts on Home rather than nothing. */
    private int currentNavItem = R.id.nav_home;

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

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
        applyIntentExtras(getIntent());
    }

    @Override
    protected int getSelectedNavItem() {
        return currentNavItem;
    }

    /** Home swaps fragments in place rather than launching another activity. */
    @Override
    protected boolean onNavItemSelected(@IdRes int itemId) {
        Fragment fragment = fragmentFor(itemId);
        if (fragment == null) return false;
        currentNavItem = itemId;
        loadFragment(fragment);
        setActiveDrawerItem(itemId);
        return true;
    }

    private Fragment fragmentFor(@IdRes int itemId) {
        if (itemId == R.id.nav_home) return new HomeFragment();
        if (itemId == R.id.nav_discover) return new DiscoverFragment();
        if (itemId == R.id.nav_profile) return new ProfileFragment();
        return null;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyIntentExtras(intent);
    }

    /**
     * Handles deep links and the tab request that other screens attach when their bottom nav sends the user back here.
     */
    private void applyIntentExtras(Intent intent) {
        if (intent == null) return;

        if (intent.getBooleanExtra(EXTRA_OPEN_ROUTE_MATE, false)) {
            intent.removeExtra(EXTRA_OPEN_ROUTE_MATE);
            loadFragment(new com.campusconnect.app.routemate.RouteMateListFragment());
            return;
        }

        if (intent.hasExtra(EXTRA_OPEN_LOST_FOUND_ITEM_ID)) {
            int itemId = intent.getIntExtra(EXTRA_OPEN_LOST_FOUND_ITEM_ID, -1);
            intent.removeExtra(EXTRA_OPEN_LOST_FOUND_ITEM_ID);
            if (itemId != -1) {
                loadFragment(com.campusconnect.app.lostfound.ItemDetailFragment.newInstance(itemId));
            }
            return;
        }

        if (intent.getBooleanExtra(EXTRA_OPEN_LOST_FOUND_LIST, false)) {
            intent.removeExtra(EXTRA_OPEN_LOST_FOUND_LIST);
            loadFragment(new com.campusconnect.app.lostfound.LostFoundListFragment());
            return;
        }

        int requestedTab = intent.getIntExtra(EXTRA_NAV_ITEM, 0);
        if (requestedTab != 0) {
            intent.removeExtra(EXTRA_NAV_ITEM);
            // Drives the bottom bar, which routes back through onNavItemSelected.
            setSelectedNavItem(requestedTab);
        }
    }
}
