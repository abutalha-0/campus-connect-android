package com.campusconnect.app.core.base;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Pads the sheet's root by the keyboard's height when it's visible, so a
 * focused field can scroll clear of it. Uses WindowInsets directly instead
 * of the legacy windowSoftInputMode/adjustResize flag — that flag fights
 * BottomSheetDialogFragment's own expand/collapse animation (causes
 * jumping/blank gaps) and is unreliable under edge-to-edge on newer Android.
 */
public abstract class BaseBottomSheet extends BottomSheetDialogFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Window window = requireDialog().getWindow();
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeBottom);
            return insets;
        });
    }
}
