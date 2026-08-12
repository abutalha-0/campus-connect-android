package com.campusconnect.app.crew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.crew.model.Category;
import com.campusconnect.app.crew.model.Post;
import com.campusconnect.app.notifications.NotificationApiService;
import com.campusconnect.app.notifications.NotificationsActivity;
import com.campusconnect.app.notifications.model.UnreadCountResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * NEW (Crew feature): Crew list screen. Launched from the Home dashboard's
 * "Crew" tile (see HomeFragment.java) — structured like ClassroomActivity
 * (back-button header, drill-in screen), not a bottom-nav fragment.
 */
public class CrewActivity extends BaseActivity {

    public static Intent createIntent(Context ctx) {
        return new Intent(ctx, CrewActivity.class);
    }

    private RecyclerView recyclerView;
    private TextView tvStatus;
    private Spinner spinnerCategoryFilter;
    private PostAdapter adapter;
    private View notificationDot;

    private final List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew);

        ((ImageView) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        notificationDot = findViewById(R.id.notificationDot);
        findViewById(R.id.btnNotifications).setOnClickListener(v ->
                startActivity(NotificationsActivity.createIntent(this)));
        findViewById(R.id.fabAddPost).setOnClickListener(v ->
                startActivity(AddPostActivity.createIntent(this)));

        recyclerView = findViewById(R.id.recyclerView);
        tvStatus = findViewById(R.id.tvStatus);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUnreadDot();
        if (adapter != null) loadPosts(selectedCategorySlug());
    }

    private void loadCategories() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .getCategories(token)
                .enqueue(new Callback<List<Category>>() {
                    @Override
                    public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                        if (isFinishing()) return;
                        categories.clear();
                        if (response.isSuccessful() && response.body() != null) categories.addAll(response.body());
                        populateCategorySpinner();
                    }

                    @Override
                    public void onFailure(Call<List<Category>> call, Throwable t) {
                        if (isFinishing()) return;
                        populateCategorySpinner();
                    }
                });
    }

    private void populateCategorySpinner() {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.crew_filter_all));
        for (Category c : categories) labels.add(c.getName());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, labels);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategoryFilter.setAdapter(spinnerAdapter);

        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPosts(selectedCategorySlug());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadPosts(null);
    }

    @Nullable
    private String selectedCategorySlug() {
        int position = spinnerCategoryFilter.getSelectedItemPosition();
        if (position <= 0 || position - 1 >= categories.size()) return null;
        return categories.get(position - 1).getSlug();
    }

    private void loadPosts(@Nullable String categorySlug) {
        tvStatus.setText(getString(R.string.loading));
        tvStatus.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .getPosts(token, categorySlug, null, null)
                .enqueue(new Callback<List<Post>>() {
                    @Override
                    public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                        if (isFinishing()) return;

                        if (!response.isSuccessful() || response.body() == null) {
                            tvStatus.setText(getString(R.string.error_network));
                            return;
                        }

                        List<Post> posts = response.body();
                        if (posts.isEmpty()) {
                            tvStatus.setText(getString(R.string.crew_empty));
                            return;
                        }

                        tvStatus.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        if (adapter == null) {
                            adapter = new PostAdapter(posts, post ->
                                    startActivity(PostDetailActivity.createIntent(CrewActivity.this, post.getSlug())));
                            recyclerView.setAdapter(adapter);
                        } else {
                            adapter.setPosts(posts);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Post>> call, Throwable t) {
                        if (isFinishing()) return;
                        tvStatus.setText(getString(R.string.error_network));
                    }
                });
    }

    private void refreshUnreadDot() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(NotificationApiService.class)
                .getUnreadCount(token)
                .enqueue(new Callback<UnreadCountResponse>() {
                    @Override
                    public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                        if (isFinishing()) return;
                        boolean hasUnread = response.isSuccessful() && response.body() != null && response.body().getUnreadCount() > 0;
                        notificationDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<UnreadCountResponse> call, Throwable t) {}
                });
    }
}
