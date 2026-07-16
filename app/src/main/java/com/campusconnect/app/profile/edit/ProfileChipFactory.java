package com.campusconnect.app.profile.edit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import com.campusconnect.app.R;
import com.google.android.material.chip.Chip;

/** Builds the small pill chips used for Skills and Connect & Network. */
public class ProfileChipFactory {

    public static Chip create(Context context, String text) {
        return create(context, text, 0, ContextCompat.getColor(context, R.color.color_cyan));
    }

    public static Chip create(Context context, String text,
                               @DrawableRes int iconRes, @ColorInt int accentColor) {
        float density = context.getResources().getDisplayMetrics().density;

        Chip chip = new Chip(context);
        chip.setText(text);
        chip.setTextColor(accentColor);
        chip.setTextSize(11.5f);
        chip.setChipBackgroundColor(ColorStateList.valueOf(withAlpha(accentColor, 26)));
        chip.setChipStrokeColor(ColorStateList.valueOf(withAlpha(accentColor, 51)));
        chip.setChipStrokeWidth(density);
        chip.setCheckable(false);
        chip.setClickable(false);
        chip.setRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));

        // Chip reserves extra invisible padding by default to guarantee a 48dp
        // touch target — that's what reads as "empty space" around small chips.
        chip.setEnsureMinTouchTargetSize(false);
        chip.setChipMinHeight(26 * density);
        chip.setChipStartPadding(2 * density);
        chip.setChipEndPadding(2 * density);
        chip.setTextStartPadding(6 * density);
        chip.setTextEndPadding(6 * density);
        chip.setIconStartPadding(4 * density);
        chip.setIconEndPadding(2 * density);

        if (iconRes != 0) {
            chip.setChipIconResource(iconRes);
            chip.setChipIconTint(ColorStateList.valueOf(accentColor));
            chip.setChipIconSize(14 * density);
            chip.setChipIconVisible(true);
        } else {
            chip.setChipIconVisible(false);
        }

        return chip;
    }

    private static int withAlpha(@ColorInt int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
