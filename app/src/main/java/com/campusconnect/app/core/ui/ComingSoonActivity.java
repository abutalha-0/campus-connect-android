package com.campusconnect.app.core.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.NavShellActivity;

/**
 * Generic placeholder screen for home-grid features that don't have a
 * backend yet (Classroom, Lost & Found, Route Mate, Settings, ...). Once a
 * feature ships, point its HomeFragment click handler at the real Activity
 * instead of here.
 */
public class ComingSoonActivity extends NavShellActivity {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_ICON = "icon";
    private static final String EXTRA_ACCENT = "accent";

    public static void start(Context context, String title,
                              @DrawableRes int iconRes, @ColorInt int accentColor) {
        Intent intent = new Intent(context, ComingSoonActivity.class);
        // Reachable from its own drawer (Settings), so reuse the instance
        // instead of stacking placeholder on top of placeholder.
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_ICON, iconRes);
        intent.putExtra(EXTRA_ACCENT, accentColor);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_soon);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        render(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        render(intent);
    }

    private void render(Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        int iconRes = intent.getIntExtra(EXTRA_ICON, R.drawable.ic_settings);
        int accentColor = intent.getIntExtra(EXTRA_ACCENT,
                getResources().getColor(R.color.color_cyan, null));

        ((TextView) findViewById(R.id.tvTopTitle)).setText(title);

        ImageView ivIcon = findViewById(R.id.ivIcon);
        ivIcon.setImageResource(iconRes);
        ivIcon.setImageTintList(ColorStateList.valueOf(accentColor));
    }
}
