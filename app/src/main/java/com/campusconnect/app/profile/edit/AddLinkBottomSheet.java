package com.campusconnect.app.profile.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Link;
import com.campusconnect.app.profile.models.LinkRequest;
import com.campusconnect.app.core.base.BaseBottomSheet;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * "Manage Connect & Network" sheet: shows current links as removable chips
 * and lets the user add a new one. addLink isn't live on the backend yet
 * (see ProfileApiService) — the UI is wired ahead of time.
 */
public class AddLinkBottomSheet extends BaseBottomSheet {

    private TokenManager tokenManager;
    private ChipGroup currentLinksChipGroup;
    private EditText etLinkUrl;
    private TextView platformGithub, platformLinkedin, platformFacebook, platformWebsite;
    private SocialPlatform selectedPlatform = SocialPlatform.GITHUB;

    private List<Link> links = new ArrayList<>();

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener l) { this.onSavedListener = l; }

    public void setLinks(@Nullable List<Link> links) {
        this.links = links != null ? links : new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_manage_links, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        currentLinksChipGroup = view.findViewById(R.id.currentLinksChipGroup);
        etLinkUrl = view.findViewById(R.id.etLinkUrl);
        platformGithub = view.findViewById(R.id.platformGithub);
        platformLinkedin = view.findViewById(R.id.platformLinkedin);
        platformFacebook = view.findViewById(R.id.platformFacebook);
        platformWebsite = view.findViewById(R.id.platformWebsite);

        platformGithub.setOnClickListener(v -> selectPlatform(SocialPlatform.GITHUB));
        platformLinkedin.setOnClickListener(v -> selectPlatform(SocialPlatform.LINKEDIN));
        platformFacebook.setOnClickListener(v -> selectPlatform(SocialPlatform.FACEBOOK));
        platformWebsite.setOnClickListener(v -> selectPlatform(SocialPlatform.WEBSITE));

        view.findViewById(R.id.btnAddLink).setOnClickListener(v -> addLink());
        view.findViewById(R.id.btnDone).setOnClickListener(v -> dismiss());

        renderCurrentLinks();
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
            Chip chip = ProfileChipFactory.create(requireContext(), label,
                    platform.iconRes, platform.accentColor);
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
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            links.remove(link);
                            currentLinksChipGroup.removeView(chip);
                            if (onSavedListener != null) onSavedListener.onSaved();
                        } else {
                            Toast.makeText(requireContext(), "Couldn't remove link. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), getString(R.string.error_network),
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

        TextView btnAdd = requireView().findViewById(R.id.btnAddLink);
        btnAdd.setEnabled(false);
        btnAdd.setText("Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        LinkRequest body = new LinkRequest(selectedPlatform.label, selectedPlatform.key, url);

        RetrofitClient.createService(ProfileApiService.class)
                .addLink(token, body)
                .enqueue(new Callback<Link>() {
                    @Override
                    public void onResponse(Call<Link> call, Response<Link> response) {
                        if (!isAdded()) return;
                        btnAdd.setEnabled(true);
                        btnAdd.setText("+ Add Link");
                        if (response.isSuccessful() && response.body() != null) {
                            links.add(response.body());
                            renderCurrentLinks();
                            etLinkUrl.setText("");
                            if (onSavedListener != null) onSavedListener.onSaved();
                        } else {
                            Toast.makeText(requireContext(), "Failed to add link. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Link> call, Throwable t) {
                        if (!isAdded()) return;
                        btnAdd.setEnabled(true);
                        btnAdd.setText("+ Add Link");
                        Toast.makeText(requireContext(), getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
