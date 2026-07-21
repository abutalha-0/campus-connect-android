package com.campusconnect.app.onboarding;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

/** One onboarding slide's content — colors/gradients are derived from accentColorRes at render time. */
public class OnboardingPage {

    public final String title;
    public final String description;
    @ColorRes public final int accentColorRes;
    @DrawableRes public final int animatedIconRes;

    public OnboardingPage(String title, String description,
                           @ColorRes int accentColorRes, @DrawableRes int animatedIconRes) {
        this.title = title;
        this.description = description;
        this.accentColorRes = accentColorRes;
        this.animatedIconRes = animatedIconRes;
    }
}
