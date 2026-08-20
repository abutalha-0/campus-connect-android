package com.campusconnect.app.crew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.core.api.PageResponse;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.NavShellActivity;
import com.campusconnect.app.core.utils.ApiError;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.crew.model.Category;
import com.campusconnect.app.crew.model.Post;
import com.campusconnect.app.crew.model.PostRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * NEW (Crew feature): create a Post. Modeled on AddFeedPostActivity.java —
 * top_bar_edit include, bottom Save TextView, Retrofit Call/enqueue.
 * A spinner selection shows/hides one of three pre-built field groups
 * (study/contest/travel), which get packed into Post.details (JSON on the backend).
 */
public class AddPostActivity extends NavShellActivity {

    public static Intent createIntent(Context ctx) {
        return new Intent(ctx, AddPostActivity.class);
    }

    private Spinner spinnerCategory;
    private EditText etTitle, etDescription, etLocation, etContactInfo, etMaxMembers;
    private LinearLayout studyFieldsContainer, contestFieldsContainer, travelFieldsContainer;
    private EditText etCourseCode, etCourseTitle, etTopic;
    private EditText etContestName, etTechStack;
    private EditText etDestination, etTravelDate, etBudget;
    private TextView btnSave;

    private final List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(R.string.crew_new_post));
        findViewById(R.id.btnSave).setVisibility(View.GONE);
        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        spinnerCategory = findViewById(R.id.spinnerCategory);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etContactInfo = findViewById(R.id.etContactInfo);
        etMaxMembers = findViewById(R.id.etMaxMembers);

        studyFieldsContainer = findViewById(R.id.studyFieldsContainer);
        contestFieldsContainer = findViewById(R.id.contestFieldsContainer);
        travelFieldsContainer = findViewById(R.id.travelFieldsContainer);
        etCourseCode = findViewById(R.id.etCourseCode);
        etCourseTitle = findViewById(R.id.etCourseTitle);
        etTopic = findViewById(R.id.etTopic);
        etContestName = findViewById(R.id.etContestName);
        etTechStack = findViewById(R.id.etTechStack);
        etDestination = findViewById(R.id.etDestination);
        etTravelDate = findViewById(R.id.etTravelDate);
        etBudget = findViewById(R.id.etBudget);

        btnSave = findViewById(R.id.btnSavePost);
        btnSave.setOnClickListener(v -> save());

        loadCategories();
    }

    private void loadCategories() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .getCategories(token)
                .enqueue(new Callback<PageResponse<Category>>() {
                    @Override
                    public void onResponse(Call<PageResponse<Category>> call, Response<PageResponse<Category>> response) {
                        if (isFinishing()) return;
                        categories.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                            categories.addAll(response.body().getResults());
                        }
                        populateCategorySpinner();
                    }

                    @Override
                    public void onFailure(Call<PageResponse<Category>> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(AddPostActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateCategorySpinner() {
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, categories);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onCategorySelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (!categories.isEmpty()) onCategorySelected(0);
    }

    private void onCategorySelected(int position) {
        if (position < 0 || position >= categories.size()) return;
        Category category = categories.get(position);
        String slug = normalizedSlug(category.getSlug());

        studyFieldsContainer.setVisibility(View.GONE);
        contestFieldsContainer.setVisibility(View.GONE);
        travelFieldsContainer.setVisibility(View.GONE);

        if (category.getDetailSchema() == null || category.getDetailSchema().getRequired() == null) {
            switch (slug) {
                case "study-partner": studyFieldsContainer.setVisibility(View.VISIBLE); break;
                case "contest-teammate": contestFieldsContainer.setVisibility(View.VISIBLE); break;
                case "travel-mate": travelFieldsContainer.setVisibility(View.VISIBLE); break;
                default: break;
            }
            return;
        }

        if (category.getDetailSchema().getRequired().contains("course_code")) {
            studyFieldsContainer.setVisibility(View.VISIBLE);
        } else if (category.getDetailSchema().getRequired().contains("contest_name")) {
            contestFieldsContainer.setVisibility(View.VISIBLE);
        } else if (category.getDetailSchema().getRequired().contains("destination")) {
            travelFieldsContainer.setVisibility(View.VISIBLE);
        }
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || categories.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        Category category = categories.get(categoryPosition);

        Map<String, String> details = new HashMap<>();
        String categorySlug = normalizedSlug(category.getSlug());
        switch (categorySlug) {
            case "study-partner":
                details.put("course_code", etCourseCode.getText().toString().trim());
                details.put("course_title", etCourseTitle.getText().toString().trim());
                details.put("topic", etTopic.getText().toString().trim());
                break;
            case "contest-teammate":
                details.put("contest_name", etContestName.getText().toString().trim());
                details.put("tech_stack", etTechStack.getText().toString().trim());
                break;
            case "travel-mate":
                details.put("destination", etDestination.getText().toString().trim());
                details.put("travel_date", etTravelDate.getText().toString().trim());
                details.put("budget", etBudget.getText().toString().trim());
                break;
            default: break;
        }

        Integer maxMembers = null;
        String maxMembersText = etMaxMembers.getText().toString().trim();
        if (!maxMembersText.isEmpty()) {
            try { maxMembers = Integer.parseInt(maxMembersText); } catch (NumberFormatException ignored) {}
        }

        // No slug field shown to the user — derived from the title plus a
        // short suffix so two posts with the same title don't collide.
        String slug = slugify(title) + "-" + Long.toString(System.currentTimeMillis() % 100000);

        PostRequest request = new PostRequest(
                category.getId(), title, slug, description,
                etLocation.getText().toString().trim(),
                etContactInfo.getText().toString().trim(),
                details, maxMembers
        );

        btnSave.setEnabled(false);
        btnSave.setText(getString(R.string.loading));

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .createPost(token, request)
                .enqueue(new Callback<Post>() {
                    @Override
                    public void onResponse(Call<Post> call, Response<Post> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            finish();
                        } else {
                            resetSaveButton();
                            String message = ApiError.extract(response, getString(R.string.crew_post_failed), "details", "title", "category");
                            Toast.makeText(AddPostActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Post> call, Throwable t) {
                        if (isFinishing()) return;
                        resetSaveButton();
                        Toast.makeText(AddPostActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText(getString(R.string.crew_post_btn));
    }

    private String slugify(String input) {
        return input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s-]", "").trim().replaceAll("\\s+", "-");
    }

    private String normalizedSlug(String slug) {
        return "contest-team".equals(slug) ? "contest-teammate" : slug;
    }
}
