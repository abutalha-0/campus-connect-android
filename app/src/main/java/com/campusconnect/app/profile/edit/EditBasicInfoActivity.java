package com.campusconnect.app.profile.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.ProfileUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Edits bio + about + user type, all via one combined PATCH /api/profiles/me/.
 * The hero card and the About card each only own part of this data, so
 * EXTRA_MODE hides the other section's fields — but both modes still fetch
 * and resubmit all three values together so the hidden ones aren't wiped out.
 */
public class EditBasicInfoActivity extends BaseActivity {

    public enum Mode { HERO, ABOUT }

    private static final String EXTRA_MODE = "mode";

    public static void start(Context context, Mode mode) {
        Intent intent = new Intent(context, EditBasicInfoActivity.class);
        intent.putExtra(EXTRA_MODE, mode.name());
        context.startActivity(intent);
    }

    private EditText etBio, etAbout;
    private TextView checkStudent, checkCR, btnSave;
    private String selectedUserType = "STUDENT";
    private Mode mode = Mode.ABOUT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_basic_info);

        mode = Mode.valueOf(getIntent().getStringExtra(EXTRA_MODE) != null
                ? getIntent().getStringExtra(EXTRA_MODE) : Mode.ABOUT.name());

        etBio = findViewById(R.id.etBio);
        etAbout = findViewById(R.id.etAbout);
        checkStudent = findViewById(R.id.checkStudent);
        checkCR = findViewById(R.id.checkCR);
        btnSave = findViewById(R.id.btnSave);

        View groupAboutFields = findViewById(R.id.groupAboutFields);
        View groupUserType = findViewById(R.id.groupUserType);
        TextView tvSheetTitle = findViewById(R.id.tvSheetTitle);

        if (mode == Mode.HERO) {
            tvSheetTitle.setText("Edit User Type");
            groupAboutFields.setVisibility(View.GONE);
            groupUserType.setVisibility(View.VISIBLE);
        } else {
            tvSheetTitle.setText("Edit About");
            groupAboutFields.setVisibility(View.VISIBLE);
            groupUserType.setVisibility(View.GONE);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveBasicInfo());

        findViewById(R.id.optionStudent).setOnClickListener(v -> selectUserType("STUDENT"));
        findViewById(R.id.optionCR).setOnClickListener(v -> selectUserType("CR"));

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Profile profile = response.body();
                            if (profile.getBio() != null) etBio.setText(profile.getBio());
                            if (profile.getAbout() != null) etAbout.setText(profile.getAbout());
                            if (profile.getUserType() != null) selectUserType(profile.getUserType());
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        // screen stays editable even if pre-fill fails
                    }
                });
    }

    private void selectUserType(String type) {
        selectedUserType = type;
        checkStudent.setText(type.equals("STUDENT") ? "●" : "○");
        checkCR.setText(type.equals("CR") ? "●" : "○");
        checkStudent.setTextColor(getResources().getColor(
                type.equals("STUDENT") ? R.color.color_cyan : R.color.color_muted, null));
        checkCR.setTextColor(getResources().getColor(
                type.equals("CR") ? R.color.color_cyan : R.color.color_muted, null));
    }

    private void saveBasicInfo() {
        String bio = etBio.getText().toString().trim();
        String about = etAbout.getText().toString().trim();

        btnSave.setEnabled(false);
        btnSave.setText("Saving…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        // Always sends all three fields (even the ones hidden in this mode)
        // so the hidden section's current value isn't overwritten with blanks.
        ProfileUpdateRequest body = new ProfileUpdateRequest(bio, about, selectedUserType);

        RetrofitClient.createService(ProfileApiService.class)
                .updateProfile(token, body)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        if (response.isSuccessful()) {
                            Toast.makeText(EditBasicInfoActivity.this,
                                    "Profile updated!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditBasicInfoActivity.this,
                                    "Update failed. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        Toast.makeText(EditBasicInfoActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
