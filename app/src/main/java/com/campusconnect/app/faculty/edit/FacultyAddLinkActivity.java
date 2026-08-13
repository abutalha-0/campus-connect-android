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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Manage faculty contact links: current links as removable chips + add form. */
public class FacultyAddLinkActivity extends BaseActivity {

    private ChipGroup currentLinksChipGroup;
    private EditText etLinkUrl;
    private EditText etLinkName;
    private TextView tvUrlHelper;
    private TextView platformGithub, platformLinkedin, platformFacebook, platformWebsite;
    private SocialPlatform selectedPlatform = SocialPlatform.LINKEDIN;

    private List<FacultyLink> links = new ArrayList<>();
    // Single-instance platforms (github/linkedin/facebook) already linked —
    // "website" is never in here since it's unlimited.
    private final Set<String> usedSingleInstanceKeys = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_add_link);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText("Contact & Links");
        ((TextView) findViewById(R.id.btnSave)).setText("Done");

        currentLinksChipGroup = findViewById(R.id.currentLinksChipGroup);
        etLinkUrl = findViewById(R.id.etLinkUrl);
        etLinkName = findViewById(R.id.etLinkName);
        tvUrlHelper = findViewById(R.id.tvUrlHelper);
        platformGithub = findViewById(R.id.platformGithub);
        platformLinkedin = findViewById(R.id.platformLinkedin);
        platformFacebook = findViewById(R.id.platformFacebook);
        platformWebsite = findViewById(R.id.platformWebsite);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> finish());

        platformGithub.setOnClickListener(v -> tapPlatform(SocialPlatform.GITHUB));
        platformLinkedin.setOnClickListener(v -> tapPlatform(SocialPlatform.LINKEDIN));
        platformFacebook.setOnClickListener(v -> tapPlatform(SocialPlatform.FACEBOOK));
        platformWebsite.setOnClickListener(v -> tapPlatform(SocialPlatform.WEBSITE));
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

    /** A tap on a platform tab — blocked with an explanation if it's a single-instance
     *  platform (GitHub/LinkedIn/Facebook) that's already linked. */
    private void tapPlatform(SocialPlatform platform) {
        if (platform.singleInstance && usedSingleInstanceKeys.contains(platform.key)) {
            Toast.makeText(this,
                    "You've already added a " + platform.label + " link. Remove it first to add another.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        selectPlatform(platform);
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

        tvUrlHelper.setText("Paste your " + platform.label + " profile link");
        etLinkUrl.setHint(platform.exampleUrl);
        etLinkUrl.setError(null);
        etLinkName.setHint(platform.label);

        applyUsedPlatformDimming();
    }

    /** Dim (but keep tappable, for the explanatory toast) any single-instance
     *  platform tab that's already linked. */
    private void applyUsedPlatformDimming() {
        for (SocialPlatform platform : new SocialPlatform[]{SocialPlatform.GITHUB, SocialPlatform.LINKEDIN, SocialPlatform.FACEBOOK}) {
            TextView tab = platform == SocialPlatform.GITHUB ? platformGithub :
                    platform == SocialPlatform.LINKEDIN ? platformLinkedin : platformFacebook;
            tab.setAlpha(usedSingleInstanceKeys.contains(platform.key) ? 0.35f : 1f);
        }
    }

    private void renderCurrentLinks() {
        currentLinksChipGroup.removeAllViews();
        usedSingleInstanceKeys.clear();
        for (FacultyLink link : links) {
            SocialPlatform platform = SocialPlatform.fromKey(link.getIcon());
            if (platform.singleInstance) usedSingleInstanceKeys.add(platform.key);

            String label = link.getLinkName() != null ? link.getLinkName() : platform.label;
            Chip chip = ProfileChipFactory.create(this, label, platform.iconRes, platform.accentColor);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> deleteLink(link));
            currentLinksChipGroup.addView(chip);
        }

        // If the currently-selected platform just became unavailable (e.g. it
        // was just linked), fall back to Website rather than leaving the form
        // pointed at a platform the user can no longer submit.
        if (selectedPlatform.singleInstance && usedSingleInstanceKeys.contains(selectedPlatform.key)) {
            selectPlatform(SocialPlatform.WEBSITE);
        } else {
            applyUsedPlatformDimming();
        }
    }

    private void deleteLink(FacultyLink link) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FacultyApiService.class)
                .deleteLink(token, link.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            links.remove(link);
                            renderCurrentLinks();
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
            etLinkUrl.setError("Paste a profile link");
            return;
        }
        if (selectedPlatform.singleInstance && usedSingleInstanceKeys.contains(selectedPlatform.key)) {
            Toast.makeText(this,
                    "You've already added a " + selectedPlatform.label + " link. Remove it first to add another.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!selectedPlatform.matchesDomain(url)) {
            etLinkUrl.setError("Enter a valid " + selectedPlatform.label + " link (e.g. " + selectedPlatform.exampleUrl + ")");
            return;
        }

        TextView btnAdd = findViewById(R.id.btnAddLink);
        btnAdd.setEnabled(false);
        btnAdd.setText("Adding…");

        String typedName = etLinkName.getText().toString().trim();
        String name = typedName.isEmpty() ? selectedPlatform.label : typedName;

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        FacultyLinkRequest body = new FacultyLinkRequest(name, selectedPlatform.key, url);

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
                            etLinkName.setText("");
                        } else {
                            Toast.makeText(FacultyAddLinkActivity.this,
                                    extractIconError(response), Toast.LENGTH_LONG).show();
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

    /** Surfaces the serializer's "icon" validation message (e.g. duplicate
     *  platform) if present, falling back to a generic failure message. */
    private String extractIconError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                org.json.JSONObject json = new org.json.JSONObject(response.errorBody().string());
                if (json.has("icon")) {
                    return json.getJSONArray("icon").getString(0);
                }
            }
        } catch (Exception ignored) {
            // fall through to generic message
        }
        return "Failed to add link. Try again.";
    }
}
