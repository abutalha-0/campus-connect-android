package com.campusconnect.app.home;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.campusconnect.app.R;
import com.campusconnect.app.core.utils.TokenManager;
import android.content.Intent;
import com.campusconnect.app.auth.login.LoginActivity;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(getResources().getColor(R.color.color_background, null));

        TextView tv = new TextView(getContext());
        tv.setText("Profile — coming soon");
        tv.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 0, 0, 32);

        Button btnLogout = new Button(getContext());
        btnLogout.setText("Logout (temp)");
        btnLogout.setOnClickListener(v -> {
            TokenManager tokenManager = new TokenManager(getContext());
            tokenManager.clearTokens();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        layout.addView(tv);
        layout.addView(btnLogout);

        return layout;
    }
}