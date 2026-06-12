package com.campusconnect.app.profile;

import android.os.Bundle;
import android.widget.TextView;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;

public class PublicProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String fullName = getIntent().getStringExtra("full_name");
        String username = getIntent().getStringExtra("username");

        TextView tv = new TextView(this);
        tv.setText(fullName + "\n@" + username + "\n\nFull profile coming soon.");
        tv.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setBackgroundColor(getResources().getColor(R.color.color_background, null));
        tv.setPadding(32, 32, 32, 32);
        setContentView(tv);
    }
}