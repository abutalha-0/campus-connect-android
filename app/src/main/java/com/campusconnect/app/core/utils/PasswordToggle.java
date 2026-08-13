package com.campusconnect.app.core.utils;

import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;

import com.campusconnect.app.R;

/** Wires a show/hide eye icon to a password EditText. */
public final class PasswordToggle {

    private PasswordToggle() {}

    public static void attach(EditText field, ImageView icon) {
        icon.setOnClickListener(v -> {
            boolean currentlyVisible = field.getInputType()
                    == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            if (currentlyVisible) {
                field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                icon.setImageResource(R.drawable.ic_eye);
            } else {
                field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                icon.setImageResource(R.drawable.ic_eye_off);
            }
            field.setSelection(field.getText().length());
        });
    }
}
