package com.campusconnect.app.faculty;

import android.os.Bundle;
import android.widget.Button;

import com.campusconnect.app.R;
import com.campusconnect.app.core.base.BaseActivity;

/**
 * Landing screen for faculty accounts. This is a scaffold for now — the full
 * Faculty Profile (Own) UI is built in the next phase.
 */
public class FacultyHomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_home);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }
}
