package com.campusconnect.app.profile.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Link;
import com.campusconnect.app.profile.models.LinkRequest;
import com.campusconnect.app.profile.models.Profile;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-page "Manage Connect & Network": current links as removable chips,
 * plus a platform picker + URL field to add a new one.
 */
public class AddLinkActivity extends BaseActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, AddLinkActivity.class));
    }

    private ChipGroup currentLinksChipGroup;
    private EditText etLinkUrl;
    private TextView platformGithub, platformLinkedin, platformFacebook, platformWebsite;
    private SocialPlatform selectedPlatform = SocialPlatform.GITHUB;

    private List<Link> links = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_link);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText("Connect & Network");
        ((TextView) findViewById(R.id.btnSave)).setText("Done");

        currentLinksChipGroup = findViewById(R.id.currentLinksChipGroup);
        etLinkUrl = findViewById(R.id.etLinkUrl);
        platformGithub = findViewById(R.id.platformGithub);
        platformLinkedin = findViewById(R.id.platformLinkedin);
        platformFacebook = findViewById(R.id.platformFacebook);
        platformWebsite = findViewById(R.id.platformWebsite);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> finish());

        platformGithub.setOnClickListener(v -> selectPlatform(SocialPlatform.GITHUB));
        platformLinkedin.setOnClickListener(v -> selectPlatform(SocialPlatform.LINKEDIN));
        platformFacebook.setOnClickListener(v -> selectPlatform(SocialPlatform.FACEBOOK));
        platformWebsite.setOnClickListener(v -> selectPlatform(SocialPlatform.WEBSITE));

        findViewById(R.id.btnAddLink).setOnClickListener(v -> addLink());

        loadCurrentLinks();
    }

    private void loadCurrentLinks() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getLinks() != null) {
                            links = new ArrayList<>(response.body().getLinks());
                            renderCurrentLinks();
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        // sheet stays usable even if this fails
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
        for (Link link : links) {
            SocialPlatform platform = SocialPlatform.fromKey(link.getIcon());
            String label = link.getLinkName() != null ? link.getLinkName() : platform.label;
            Chip chip = ProfileChipFactory.create(this, label, platform.iconRes, platform.accentColor);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> deleteLink(link, chip));
            currentLinksChipGroup.addView(chip);
        }
    }

    private void deleteLink(Link link, Chip chip) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .deleteLink(token, link.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            links.remove(link);
                            currentLinksChipGroup.removeView(chip);
                        } else {
                            Toast.makeText(AddLinkActivity.this, "Couldn't remove link. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(AddLinkActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
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
        LinkRequest body = new LinkRequest(selectedPlatform.label, selectedPlatform.key, url);

        RetrofitClient.createService(ProfileApiService.class)
                .addLink(token, body)
                .enqueue(new Callback<Link>() {
                    @Override
                    public void onResponse(Call<Link> call, Response<Link> response) {
                        btnAdd.setEnabled(true);
                        btnAdd.setText("+ Add Link");
                        if (response.isSuccessful() && response.body() != null) {
                            links.add(response.body());
                            renderCurrentLinks();
                            etLinkUrl.setText("");
                        } else {
                            Toast.makeText(AddLinkActivity.this, "Failed to add link. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Link> call, Throwable t) {
                        btnAdd.setEnabled(true);
                        btnAdd.setText("+ Add Link");
                        Toast.makeText(AddLinkActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
