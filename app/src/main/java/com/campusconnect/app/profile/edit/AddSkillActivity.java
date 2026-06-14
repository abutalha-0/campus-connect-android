package com.campusconnect.app.profile.edit;

import android.os.Bundle;
import android.widget.TextView;
import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;

public class AddSkillActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Edit Basic Info — coming next");
        tv.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setBackgroundColor(getResources().getColor(R.color.color_background, null));
        setContentView(tv);
    }
}