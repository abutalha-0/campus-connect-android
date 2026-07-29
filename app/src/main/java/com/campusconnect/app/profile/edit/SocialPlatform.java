package com.campusconnect.app.profile.edit;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import com.campusconnect.app.R;

/**
 * Supported Connect & Network platforms. `key` is what we store in
 * Link.icon (a guess — addLink isn't live on the backend yet).
 */
public enum SocialPlatform {
    // A user may only ever have one GitHub/LinkedIn/Facebook link; "website"
    // is the only platform they can add multiple times (personal site, blog,
    // portfolio, etc). The user pastes their full profile link for every
    // platform — `domains` is just what that pasted link must belong to
    // (empty for "website", which accepts any URL).
    GITHUB("github", "GitHub", R.drawable.ic_github, 0xFFE5E7EB, true,
            new String[]{"github.com"}, "https://github.com/yourname"),
    LINKEDIN("linkedin", "LinkedIn", R.drawable.ic_linkedin, 0xFF38BDF8, true,
            new String[]{"linkedin.com"}, "https://linkedin.com/in/yourname"),
    FACEBOOK("facebook", "Facebook", R.drawable.ic_facebook, 0xFF818CF8, true,
            new String[]{"facebook.com", "fb.com"}, "https://facebook.com/yourname"),
    WEBSITE("website", "Website", R.drawable.ic_link_generic, 0xFF9CA3AF, false,
            new String[]{}, "https://yoursite.com");

    public final String key;
    public final String label;
    @DrawableRes public final int iconRes;
    @ColorInt public final int accentColor;
    public final boolean singleInstance;
    public final String[] domains;
    public final String exampleUrl;

    SocialPlatform(String key, String label, @DrawableRes int iconRes, @ColorInt int accentColor,
                   boolean singleInstance, String[] domains, String exampleUrl) {
        this.key = key;
        this.label = label;
        this.iconRes = iconRes;
        this.accentColor = accentColor;
        this.singleInstance = singleInstance;
        this.domains = domains;
        this.exampleUrl = exampleUrl;
    }

    /** Whether a pasted link belongs to this platform (always true for
     *  "website", which has no domain restriction). */
    public boolean matchesDomain(String url) {
        if (domains.length == 0) return true;
        String host = android.net.Uri.parse(url == null ? "" : url.trim()).getHost();
        if (host == null) return false;
        host = host.toLowerCase();
        for (String domain : domains) {
            if (host.equals(domain) || host.endsWith("." + domain)) return true;
        }
        return false;
    }

    public static SocialPlatform fromKey(String key) {
        for (SocialPlatform p : values()) {
            if (p.key.equalsIgnoreCase(key)) return p;
        }
        return WEBSITE;
    }
}
