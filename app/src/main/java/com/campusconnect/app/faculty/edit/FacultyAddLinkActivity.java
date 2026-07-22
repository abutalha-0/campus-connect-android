package com.campusconnect.app.faculty.edit;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.faculty.FacultyApiService;
import com.campusconnect.app.faculty.model.FacultyLink;
import com.campusconnect.app.faculty.model.FacultyLinkRequest;
import com.campusconnect.app.faculty.model.FacultyProfile;
import com.campusconnect.app.profile.edit.ProfileChipFactory;
import com.campusconnect.app.profile.edit.SocialPlatform;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Manage faculty contact links: current links as removable chips + add form. */
public class FacultyAddLinkActivity extends BaseActivity {

    private ChipGroup currentLinksChipGroup;
    private EditText etLinkUrl;
    private TextView platformGithub, platformLinkedin, platformFacebook, platformWebsite;
    private SocialPlatform selectedPlatform = SocialPlatform.LINKEDIN;

    private List<FacultyLink> links = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_add_link);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText("Contact & Links");
        ((TextView) findViewById(R.id.btnSave)).setText("Done");

        currentLinksChipGroup = findViewById(R.id.currentLinksChipGroup);
        etLinkUrl = findViewById(R.id.etLinkUrl);
        platformGithub = findViewById(R.id.platformGithub);
        platformLinkedin = findViewById(R.id.platformLinkedin);
        platformFacebook = findViewById(R.id.platformFacebook);
        platformWebsite = findViewById(R.id.platformWebsite);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> finish());

        platformGithub.setOnClickListener(v -> selectPlatform(SocialPlatform.GITHUB));
        platformLinkedin.setOnClickListener(v -> selectPlatform(SocialPlatform.LINKEDIN));
        platformFacebook.setOnClickListener(v -> selectPlatform(SocialPlatform.FACEBOOK));
        platformWebsite.setOnClickListener(v -> selectPlatform(SocialPlatform.WEBSITE));
        findViewById(R.id.btnAddLink).setOnClickListener(v -> addLink());

        // Default the picker to LinkedIn.
        selectPlatform(SocialPlatform.LINKEDIN);
        loadCurrentLinks();
    }

    private void loadCurrentLinks() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FacultyApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<FacultyProfile>() {
                    @Override
                    public void onResponse(Call<FacultyProfile> call, Response<FacultyProfile> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getLinks() != null) {
                            links = new ArrayList<>(response.body().getLinks());
                            renderCurrentLinks();
                        }
                    }

                    @Override
                    public void onFailure(Call<FacultyProfile> call, Throwable t) {
                        // screen stays usable
                    }
                });
    }

    private void selectPlatform(SocialPlatform platform) {
        selectedPlatform = platform;
        for (TextView t : new TextView[]{platformGithub, platformLinkedin, platformFacebook, platformWebsite}) {
            t.setBackgroundResource(R.drawable.bg_tab_pill_inactive);
            t.setTextColor(getResources().getColor(R.color.color_muted, null));
        }
        TextView active = platform == SocialPlatform.GITHUB ? platformGithub :
                platform == SocialPlatform.LINKEDIN ? platformLinkedin :
                platform == SocialPlatform.FACEBOOK ? platformFacebook : platformWebsite;
        active.setBackgroundResource(R.drawable.bg_tab_pill_active);
        active.setTextColor(getResources().getColor(R.color.color_cyan, null));
    }

    private void renderCurrentLinks() {
        currentLinksChipGroup.removeAllViews();
        for (FacultyLink link : links) {
            SocialPlatform platform = SocialPlatform.fromKey(link.getIcon());
            String label = link.getLinkName() != null ? link.getLinkName() : platform.label;
            Chip chip = ProfileChipFactory.create(this, label, platform.iconRes, platform.accentColor);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> deleteLink(link, chip));
            currentLinksChipGroup.addView(chip);
        }
    }

    private void deleteLink(FacultyLink link, Chip chip) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FacultyApiService.class)
                .deleteLink(token, link.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            links.remove(link);
                            currentLinksChipGroup.removeView(chip);
                        } else {
                            Toast.makeText(FacultyAddLinkActivity.this,
                                    "Couldn't remove link. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(FacultyAddLinkActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addLink() {
        String url = etLinkUrl.getText().toString().trim();
        if (url.isEmpty()) {
            etLinkUrl.setError("Enter a profile URL");
            return;
        }

        TextView btnAdd = findViewById(R.id.btnAddLink);
        btnAdd.setEnabled(false);
        btnAdd.setText("Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        FacultyLinkRequest body = new FacultyLinkRequest(
                selectedPlatform.label, selectedPlatform.key, url);

        RetrofitClient.createService(FacultyApiService.class)
                .addLink(token, body)
                .enqueue(new Callback<FacultyLink>() {
                    @Override
                    public void onResponse(Call<FacultyLink> call, Response<FacultyLink> response) {
                        btnAdd.setEnabled(true);
                        btnAdd.setText("+ Add Link");
                        if (response.isSuccessful() && response.body() != null) {
                            links.add(response.body());
                            renderCurrentLinks();
                            etLinkUrl.setText("");
                        } else {
                            Toast.makeText(FacultyAddLinkActivity.this,
                                    "Failed to add link. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FacultyLink> call, Throwable t) {
                        btnAdd.setEnabled(true);
                        btnAdd.setText("+ Add Link");
                        Toast.makeText(FacultyAddLinkActivity.this,
                                getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
