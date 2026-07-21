package com.campusconnect.app.onboarding;

import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.campusconnect.app.R;

/** One swipeable onboarding slide — background, icon panel, title, description, all tinted per page. */
public class OnboardingPageFragment extends Fragment {

    private static final String ARG_INDEX = "index";

    public static OnboardingPageFragment newInstance(int index) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    private OnboardingPage page;
    private View glowHalo;
    private FrameLayout iconPanel;
    private ImageView ivIcon;
    private TextView tvTitle, tvDesc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int index = requireArguments().getInt(ARG_INDEX);
        page = OnboardingPages.all().get(index);
        int accent = getResources().getColor(page.accentColorRes, null);

        glowHalo = view.findViewById(R.id.glowHalo);
        iconPanel = view.findViewById(R.id.iconPanel);
        ivIcon = view.findViewById(R.id.ivIcon);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDesc = view.findViewById(R.id.tvDesc);

        view.setBackground(buildPageBackground(accent));
        glowHalo.setBackground(buildGlowHalo(accent));
        iconPanel.setBackground(buildPanelBackground(accent));
        ivIcon.setImageResource(page.animatedIconRes);
        tvTitle.setText(page.title);
        tvDesc.setText(page.description);
    }

    /** Called by OnboardingActivity when this page becomes the current one — replays every visit. */
    public void playEnterAnimation() {
        if (iconPanel == null) return;

        for (View v : new View[]{glowHalo, iconPanel}) {
            v.setScaleX(0.7f);
            v.setScaleY(0.7f);
            v.setAlpha(0f);
            v.animate()
                    .scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(550)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }

        fadeUp(tvTitle, 100);
        fadeUp(tvDesc, 200);

        if (ivIcon.getDrawable() instanceof Animatable) {
            ((Animatable) ivIcon.getDrawable()).start();
        }
    }

    private void fadeUp(View view, long startDelay) {
        view.setAlpha(0f);
        view.setTranslationY(14f * getResources().getDisplayMetrics().density);
        view.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(startDelay)
                .setDuration(550)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /** A big, soft, fully-transparent-edged radial glow that sits behind the icon panel. */
    private GradientDrawable buildGlowHalo(@androidx.annotation.ColorInt int accent) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        drawable.setGradientCenter(0.5f, 0.5f);
        drawable.setGradientRadius(110 * getResources().getDisplayMetrics().density);
        drawable.setColors(new int[]{
                withAlpha(accent, 130), withAlpha(accent, 40), withAlpha(accent, 0)});
        return drawable;
    }

    private GradientDrawable buildPanelBackground(@androidx.annotation.ColorInt int accent) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(32 * getResources().getDisplayMetrics().density);
        drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        drawable.setGradientCenter(0.3f, 0.2f);
        drawable.setGradientRadius(120 * getResources().getDisplayMetrics().density);
        drawable.setColors(new int[]{withAlpha(accent, 130), 0xE6121826});
        return drawable;
    }

    private GradientDrawable buildPageBackground(@androidx.annotation.ColorInt int accent) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        drawable.setGradientCenter(0.2f, 0f);
        drawable.setGradientRadius(500 * getResources().getDisplayMetrics().density);
        drawable.setColors(new int[]{withAlpha(accent, 80),
                getResources().getColor(R.color.color_background, null)});
        return drawable;
    }

    private int withAlpha(@androidx.annotation.ColorInt int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
