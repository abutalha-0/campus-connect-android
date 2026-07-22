package com.campusconnect.app.onboarding;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.role.RoleSelectionActivity;
import java.util.List;

/**
 * First-run swipeable intro (4 pages, one per Home feature block). Shown
 * once — after that, or on Skip/Get Started, we go straight to LoginActivity
 * (which itself already redirects to Home if a token exists).
 */
public class OnboardingActivity extends BaseActivity {

    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_SEEN = "seen";

    private ViewPager2 viewPager;
    private View btnBack;
    private View[] dots;
    private TextView btnNext;
    private List<OnboardingPage> pages;
    private int activeDot = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SEEN, false)) {
            goToRoleSelection();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        // Edge-to-edge: let each page's gradient paint behind the status/nav
        // bars for a true full-bleed look, then pad just the top/bottom
        // control rows so touch targets stay clear of the system bars.
        // The theme's statusBarColor/navigationBarColor are solid — override
        // them to transparent here only, so the gradient shows through
        // instead of a flat bar sitting on top of it.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        applyEdgeToEdgeInsets();

        pages = OnboardingPages.all();
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        dots = new View[]{
                findViewById(R.id.dot0), findViewById(R.id.dot1),
                findViewById(R.id.dot2), findViewById(R.id.dot3)
        };

        findViewById(R.id.btnSkip).setOnClickListener(v -> finishOnboarding());
        btnBack.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true));
        btnNext.setOnClickListener(v -> {
            int next = viewPager.getCurrentItem() + 1;
            if (next >= pages.size()) {
                finishOnboarding();
            } else {
                viewPager.setCurrentItem(next, true);
            }
        });

        viewPager.setAdapter(new OnboardingPagerAdapter(this));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                onPageChanged(position);
            }
        });

        // The first page's fragment isn't created yet on this exact frame —
        // give ViewPager2 one layout pass before triggering its animation.
        viewPager.post(() -> onPageChanged(0));
    }

    private void applyEdgeToEdgeInsets() {
        View topBar = findViewById(R.id.topBarRow);
        View bottomControls = findViewById(R.id.bottomControlsRow);
        int topBarBasePadding = topBar.getPaddingTop();
        int bottomControlsBasePadding = bottomControls.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            topBar.setPadding(topBar.getPaddingLeft(), topBarBasePadding + bars.top,
                    topBar.getPaddingRight(), topBar.getPaddingBottom());
            bottomControls.setPadding(bottomControls.getPaddingLeft(), bottomControls.getPaddingTop(),
                    bottomControls.getPaddingRight(), bottomControlsBasePadding + bars.bottom);
            return insets;
        });
    }

    private void onPageChanged(int position) {
        OnboardingPage page = pages.get(position);
        @ColorInt int accent = getResources().getColor(page.accentColorRes, null);

        updateDots(position, accent);

        btnBack.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

        boolean isLast = position == pages.size() - 1;
        findViewById(R.id.btnSkip).setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
        btnNext.setText(isLast ? "Get Started" : "Next");
        btnNext.setBackground(pillButton(accent));

        Fragment current = getSupportFragmentManager().findFragmentByTag("f" + position);
        if (current instanceof OnboardingPageFragment) {
            ((OnboardingPageFragment) current).playEnterAnimation();
        }
    }

    private void updateDots(int newActivePosition, @ColorInt int accent) {
        int mutedColor = 0x40FFFFFF;
        int previousActive = activeDot;
        activeDot = newActivePosition;

        for (int i = 0; i < dots.length; i++) {
            boolean active = i == newActivePosition;
            dots[i].setBackgroundTintList(ColorStateList.valueOf(active ? accent : mutedColor));
            if (i == previousActive || i == newActivePosition) {
                animateDotWidth(dots[i], active);
            }
        }
    }

    private void animateDotWidth(View dot, boolean growing) {
        float density = getResources().getDisplayMetrics().density;
        int from = dot.getLayoutParams().width;
        int to = (int) ((growing ? 22 : 8) * density);
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(220);
        animator.addUpdateListener(a -> {
            ViewGroup.LayoutParams params = dot.getLayoutParams();
            params.width = (int) a.getAnimatedValue();
            dot.setLayoutParams(params);
        });
        animator.start();
    }

    private GradientDrawable pillButton(@ColorInt int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(16 * getResources().getDisplayMetrics().density);
        drawable.setColor(color);
        return drawable;
    }

    private void finishOnboarding() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_SEEN, true).apply();
        goToRoleSelection();
    }

    private void goToRoleSelection() {
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
