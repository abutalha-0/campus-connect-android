package com.campusconnect.app.profile.edit;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.Skill;
import com.campusconnect.app.profile.models.SkillRequest;
import com.campusconnect.app.profile.models.UserSkill;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-page "Manage Skills": current skills as removable chips, plus either
 * attaching an existing catalog skill (GET /api/profiles/skills/, submitted
 * as skill_id) or creating a brand-new one by typing a name that isn't in
 * the catalog (submitted as skill_name — server auto-creates it).
 */
public class AddSkillActivity extends BaseActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, AddSkillActivity.class));
    }

    private ChipGroup currentSkillsChipGroup;
    private ChipGroup skillCatalogChipGroup;
    private EditText etSkillSearch;
    private TextView tvProfBeginner, tvProfIntermediate, tvProfAdvanced;
    private TextView btnAddSkill;
    private String selectedProficiency = "BEGINNER";

    // Exactly one of these is non-null once something is selected.
    private Integer selectedSkillId;
    private String selectedNewSkillName;

    private List<UserSkill> skills = new ArrayList<>();
    private List<Skill> catalog = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_skill);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText("Manage Skills");
        ((TextView) findViewById(R.id.btnSave)).setText("Done");

        currentSkillsChipGroup = findViewById(R.id.currentSkillsChipGroup);
        skillCatalogChipGroup = findViewById(R.id.skillCatalogChipGroup);
        etSkillSearch = findViewById(R.id.etSkillSearch);
        tvProfBeginner = findViewById(R.id.tvProfBeginner);
        tvProfIntermediate = findViewById(R.id.tvProfIntermediate);
        tvProfAdvanced = findViewById(R.id.tvProfAdvanced);
        btnAddSkill = findViewById(R.id.btnAddSkill);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> finish());

        tvProfBeginner.setOnClickListener(v -> selectProficiency("BEGINNER"));
        tvProfIntermediate.setOnClickListener(v -> selectProficiency("INTERMEDIATE"));
        tvProfAdvanced.setOnClickListener(v -> selectProficiency("ADVANCED"));

        etSkillSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { renderCatalogChips(s.toString()); }
        });

        btnAddSkill.setOnClickListener(v -> addSkill());

        updateAddButtonEnabled();
        loadCurrentSkills();
        loadCatalog();
    }

    private void loadCurrentSkills() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getSkills() != null) {
                            skills = new ArrayList<>(response.body().getSkills());
                            renderCurrentSkills();
                            renderCatalogChips(etSkillSearch.getText().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        // sheet stays usable even if this fails
                    }
                });
    }

    private void selectProficiency(String proficiency) {
        selectedProficiency = proficiency;
        for (TextView t : new TextView[]{tvProfBeginner, tvProfIntermediate, tvProfAdvanced}) {
            t.setBackgroundResource(R.drawable.bg_tab_pill_inactive);
            t.setTextColor(getResources().getColor(R.color.color_muted, null));
        }
        TextView active = proficiency.equals("BEGINNER") ? tvProfBeginner :
                proficiency.equals("INTERMEDIATE") ? tvProfIntermediate : tvProfAdvanced;
        active.setBackgroundResource(R.drawable.bg_tab_pill_active);
        active.setTextColor(getResources().getColor(R.color.color_cyan, null));
    }

    // ── "Your Skills" (already attached) ─────────────────────────────────

    private void renderCurrentSkills() {
        currentSkillsChipGroup.removeAllViews();
        for (UserSkill skill : skills) {
            Chip chip = ProfileChipFactory.create(this,
                    skill.getSkill() != null ? skill.getSkill().getName() : "");
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> deleteSkill(skill, chip));
            currentSkillsChipGroup.addView(chip);
        }
    }

    private void deleteSkill(UserSkill skill, Chip chip) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .deleteSkill(token, skill.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            skills.remove(skill);
                            currentSkillsChipGroup.removeView(chip);
                            renderCatalogChips(etSkillSearch.getText().toString());
                        } else {
                            logAndShowError("removeSkill", response);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(AddSkillActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Skill catalog (pick existing, or create a new one by name) ───────

    private void loadCatalog() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getSkillCatalog(token)
                .enqueue(new Callback<List<Skill>>() {
                    @Override
                    public void onResponse(Call<List<Skill>> call, Response<List<Skill>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            catalog = response.body();
                            renderCatalogChips(etSkillSearch.getText().toString());
                        } else {
                            logAndShowError("getSkillCatalog", response);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Skill>> call, Throwable t) {
                        Toast.makeText(AddSkillActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderCatalogChips(String query) {
        skillCatalogChipGroup.removeAllViews();
        selectedSkillId = null;
        selectedNewSkillName = null;
        updateAddButtonEnabled();

        String q = query.trim();
        String qLower = q.toLowerCase();
        boolean exactMatchExists = false;

        for (Skill skill : catalog) {
            if (skill.getName().equalsIgnoreCase(q)) exactMatchExists = true;
            if (isAlreadyAdded(skill.getId())) continue;
            if (!qLower.isEmpty() && !skill.getName().toLowerCase().contains(qLower)) continue;
            skillCatalogChipGroup.addView(buildExistingSkillChip(skill));
        }

        // Offer to create a brand-new skill only when the typed text doesn't
        // already exist in the catalog (predefined or custom).
        if (!q.isEmpty() && !exactMatchExists) {
            skillCatalogChipGroup.addView(buildCreateNewChip(q));
        }
    }

    private boolean isAlreadyAdded(int skillId) {
        for (UserSkill us : skills) {
            if (us.getSkill() != null && us.getSkill().getId() == skillId) return true;
        }
        return false;
    }

    private Chip buildExistingSkillChip(Skill skill) {
        Chip chip = baseSelectableChip(skill.getName());
        chip.setOnCheckedChangeListener((button, isChecked) -> {
            styleCatalogChip(chip, isChecked);
            if (isChecked) {
                selectedSkillId = skill.getId();
                selectedNewSkillName = null;
            } else if (selectedSkillId != null && selectedSkillId == skill.getId()) {
                selectedSkillId = null;
            }
            updateAddButtonEnabled();
        });
        return chip;
    }

    private Chip buildCreateNewChip(String name) {
        Chip chip = baseSelectableChip("+ Add \"" + name + "\"");
        chip.setOnCheckedChangeListener((button, isChecked) -> {
            styleCatalogChip(chip, isChecked);
            if (isChecked) {
                selectedNewSkillName = name;
                selectedSkillId = null;
            } else if (name.equals(selectedNewSkillName)) {
                selectedNewSkillName = null;
            }
            updateAddButtonEnabled();
        });
        return chip;
    }

    private Chip baseSelectableChip(String text) {
        float density = getResources().getDisplayMetrics().density;
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setTextSize(11.5f);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setChipMinHeight(26 * density);
        chip.setChipStartPadding(2 * density);
        chip.setChipEndPadding(2 * density);
        chip.setTextStartPadding(6 * density);
        chip.setTextEndPadding(6 * density);
        chip.setRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));
        styleCatalogChip(chip, false);
        return chip;
    }

    private void styleCatalogChip(Chip chip, boolean selected) {
        int accent = getResources().getColor(
                selected ? R.color.color_cyan : R.color.color_muted, null);
        chip.setTextColor(accent);
        chip.setChipBackgroundColor(ColorStateList.valueOf(withAlpha(accent, selected ? 40 : 18)));
        chip.setChipStrokeColor(ColorStateList.valueOf(withAlpha(accent, selected ? 90 : 40)));
        chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private void updateAddButtonEnabled() {
        boolean enabled = selectedSkillId != null || selectedNewSkillName != null;
        btnAddSkill.setEnabled(enabled);
        btnAddSkill.setAlpha(enabled ? 1f : 0.4f);
    }

    private void addSkill() {
        if (selectedSkillId == null && selectedNewSkillName == null) return;

        btnAddSkill.setEnabled(false);
        btnAddSkill.setText("Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        SkillRequest body = selectedSkillId != null
                ? SkillRequest.byId(selectedSkillId, selectedProficiency)
                : SkillRequest.byName(selectedNewSkillName, selectedProficiency);

        RetrofitClient.createService(ProfileApiService.class)
                .addSkill(token, body)
                .enqueue(new Callback<UserSkill>() {
                    @Override
                    public void onResponse(Call<UserSkill> call, Response<UserSkill> response) {
                        btnAddSkill.setText("+ Add Skill");
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                skills.add(response.body());
                                renderCurrentSkills();
                            }
                            etSkillSearch.setText("");
                            renderCatalogChips("");
                        } else {
                            updateAddButtonEnabled();
                            logAndShowError("addSkill", response);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserSkill> call, Throwable t) {
                        btnAddSkill.setText("+ Add Skill");
                        updateAddButtonEnabled();
                        Toast.makeText(AddSkillActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Surfaces the server's actual rejection reason instead of a generic "failed" message. */
    private void logAndShowError(String action, Response<?> response) {
        String detail = "";
        try {
            if (response.errorBody() != null) detail = response.errorBody().string();
        } catch (Exception ignored) {
            // best-effort only
        }
        android.util.Log.e("AddSkillActivity", action + " failed: HTTP " + response.code() + " — " + detail);
        Toast.makeText(this,
                "Something went wrong (HTTP " + response.code() + "). See logcat for details.",
                Toast.LENGTH_LONG).show();
    }
}
