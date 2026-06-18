package com.campusconnect.app.profile.edit;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Profile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.campusconnect.app.profile.models.ProfileUpdateRequest;

public class EditBasicInfoActivity extends BaseActivity {

    private EditText etBio, etAbout;
    private TextView checkStudent, checkCR, btnSave;
    private String selectedUserType = "STUDENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_basic_info);

        etBio = findViewById(R.id.etBio);
        etAbout = findViewById(R.id.etAbout);
        checkStudent = findViewById(R.id.checkStudent);
        checkCR = findViewById(R.id.checkCR);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveBasicInfo());

        // user type selection
        findViewById(R.id.optionStudent).setOnClickListener(v ->
                selectUserType("STUDENT"));
        findViewById(R.id.optionCR).setOnClickListener(v ->
                selectUserType("CR"));

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call,
                                           Response<Profile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Profile profile = response.body();
                            if (profile.getBio() != null)
                                etBio.setText(profile.getBio());
                            if (profile.getAbout() != null)
                                etAbout.setText(profile.getAbout());
                            if (profile.getUserType() != null)
                                selectUserType(profile.getUserType());
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {}
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
        btnSave.setText("Saving...");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        ProfileUpdateRequest body = new ProfileUpdateRequest(bio, about, selectedUserType);

        RetrofitClient.createService(ProfileApiService.class)
                .updateProfile(token, body)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call,
                                           Response<Profile> response) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        if (response.isSuccessful()) {
                            Toast.makeText(EditBasicInfoActivity.this,
                                    "Profile updated!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditBasicInfoActivity.this,
                                    "Update failed. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        Toast.makeText(EditBasicInfoActivity.this,
                                getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}