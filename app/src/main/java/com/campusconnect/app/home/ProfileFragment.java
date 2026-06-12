package com.campusconnect.app.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        tv.setText("Profile — coming soon");
        tv.setTextColor(getResources().getColor(
                com.campusconnect.app.R.color.color_text_primary, null));
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setBackgroundColor(getResources().getColor(
                com.campusconnect.app.R.color.color_background, null));
        return tv;
    }
}