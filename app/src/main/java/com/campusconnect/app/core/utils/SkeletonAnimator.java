package com.campusconnect.app.core.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Starts/stops a looping alpha "pulse" on skeleton placeholder blocks
 * (views tagged with {@link #TAG}), so a loading screen reads as "loading"
 * rather than just blank while a network call is in flight.
 */
public final class SkeletonAnimator {

    /** Tag applied to every view that should pulse; set this in the skeleton layout. */
    public static final String TAG = "skeleton_block";

    private SkeletonAnimator() {}

    /** Starts pulsing every TAG-marked view found anywhere under {@code root}. */
    public static void start(View root) {
        if (TAG.equals(root.getTag())) {
            pulse(root);
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                start(group.getChildAt(i));
            }
        }
    }

    /** Pulses {@code view} directly, regardless of whether it's TAG-marked —
     *  useful for an existing view (e.g. an avatar with a static placeholder
     *  image) that should shimmer in place rather than being swapped out. */
    public static void pulse(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.35f);
        animator.setDuration(700);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
        view.setTag(com.campusconnect.app.R.id.skeleton_animator_tag, animator);
    }

    /** Cancels any pulsing animators started under {@code root}, resetting alpha to 1. */
    public static void stop(View root) {
        Object animatorTag = root.getTag(com.campusconnect.app.R.id.skeleton_animator_tag);
        if (animatorTag instanceof ObjectAnimator) {
            ((ObjectAnimator) animatorTag).cancel();
            root.setAlpha(1f);
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                stop(group.getChildAt(i));
            }
        }
    }

    /** Shows the skeleton (starting its pulse) and hides the real content view. */
    public static void showLoading(View skeleton, View content) {
        skeleton.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
        start(skeleton);
    }

    /** Hides the skeleton (stopping its pulse) and reveals the real content view. */
    public static void showContent(View skeleton, View content) {
        stop(skeleton);
        skeleton.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }
}
