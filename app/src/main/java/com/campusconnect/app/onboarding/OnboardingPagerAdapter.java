package com.campusconnect.app.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    private final int pageCount;

    public OnboardingPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
        this.pageCount = OnboardingPages.all().size();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OnboardingPageFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }
}
