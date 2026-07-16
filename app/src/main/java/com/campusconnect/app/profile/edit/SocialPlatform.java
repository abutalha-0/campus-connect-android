package com.campusconnect.app.profile.edit;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import com.campusconnect.app.R;

/**
 * Supported Connect & Network platforms. `key` is what we store in
 * Link.icon (a guess — addLink isn't live on the backend yet).
 */
public enum SocialPlatform {
    GITHUB("github", "GitHub", R.drawable.ic_github, 0xFFE5E7EB),
    LINKEDIN("linkedin", "LinkedIn", R.drawable.ic_linkedin, 0xFF38BDF8),
    FACEBOOK("facebook", "Facebook", R.drawable.ic_facebook, 0xFF818CF8),
    WEBSITE("website", "Website", R.drawable.ic_link_generic, 0xFF9CA3AF);

    public final String key;
    public final String label;
    @DrawableRes public final int iconRes;
    @ColorInt public final int accentColor;

    SocialPlatform(String key, String label, @DrawableRes int iconRes, @ColorInt int accentColor) {
        this.key = key;
        this.label = label;
        this.iconRes = iconRes;
        this.accentColor = accentColor;
    }

    public static SocialPlatform fromKey(String key) {
        for (SocialPlatform p : values()) {
            if (p.key.equalsIgnoreCase(key)) return p;
        }
        return WEBSITE;
    }
}
