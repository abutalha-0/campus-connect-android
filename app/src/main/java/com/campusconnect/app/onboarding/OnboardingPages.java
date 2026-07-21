package com.campusconnect.app.onboarding;

import com.campusconnect.app.R;
import java.util.Arrays;
import java.util.List;

/** The 4 onboarding slides — same four feature blocks shown on the Home grid. */
public class OnboardingPages {

    public static List<OnboardingPage> all() {
        return Arrays.asList(
                new OnboardingPage(
                        "Classroom, sorted",
                        "Subjects, schedules, resources and class notices — all in one feed so you never miss an update.",
                        R.color.color_cyan, R.drawable.avd_classroom),
                new OnboardingPage(
                        "Find your Crew",
                        "Team up with classmates, form study groups, and keep your closest campus circle a tap away.",
                        R.color.color_purple, R.drawable.avd_crew),
                new OnboardingPage(
                        "Lost something?",
                        "Report lost items or flag what you found — Campus Connect matches them with the right people, fast.",
                        R.color.color_amber, R.drawable.avd_lost),
                new OnboardingPage(
                        "Never walk alone",
                        "Match with a Route Mate heading your way — share a ride or a walk, and get home safer together.",
                        R.color.color_indigo, R.drawable.avd_route)
        );
    }

    private OnboardingPages() {}
}
