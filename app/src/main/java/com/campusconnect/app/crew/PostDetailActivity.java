package com.campusconnect.app.crew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.ApiError;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.crew.model.JoinRequestModel;
import com.campusconnect.app.crew.model.JoinRequestRequest;
import com.campusconnect.app.crew.model.Post;
import com.campusconnect.app.crew.model.PostMember;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Profile;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * NEW (Crew feature): full post view. Ownership is determined by fetching
 * the logged-in user's own profile (existing endpoint) and comparing its id
 * against post.author.id, since crew.Post doesn't compute that server-side.
 */
public class PostDetailActivity extends BaseActivity {

    private static final String EXTRA_SLUG = "post_slug";

    public static Intent createIntent(Context ctx, String slug) {
        Intent i = new Intent(ctx, PostDetailActivity.class);
        i.putExtra(EXTRA_SLUG, slug);
        return i;
    }

    private String slug;
    private Post currentPost;
    private int myUserId = -1;

    private TextView tvStatus;
    private LinearLayout contentContainer;
    private TextView tvCategory, tvStatusBadge, tvTitle, tvAuthor, tvDescription;
    private TextView tvLocation, tvContact, tvCapacity, tvSkills;
    private LinearLayout detailsContainer;

    private LinearLayout joinActionsContainer;
    private EditText etJoinMessage;
    private TextView btnRequestJoin;

    private LinearLayout ownerActionsContainer;
    private TextView btnClosePost;
    private LinearLayout joinRequestsContainer, membersContainer;
    private TextView tvNoRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        slug = getIntent().getStringExtra(EXTRA_SLUG);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(R.string.crew_post_detail_title));
        findViewById(R.id.btnSave).setVisibility(View.GONE);
        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        tvStatus = findViewById(R.id.tvStatus);
        contentContainer = findViewById(R.id.contentContainer);
        tvCategory = findViewById(R.id.tvCategory);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvContact = findViewById(R.id.tvContact);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvSkills = findViewById(R.id.tvSkills);
        detailsContainer = findViewById(R.id.detailsContainer);

        joinActionsContainer = findViewById(R.id.joinActionsContainer);
        etJoinMessage = findViewById(R.id.etJoinMessage);
        btnRequestJoin = findViewById(R.id.btnRequestJoin);
        btnRequestJoin.setOnClickListener(v -> requestToJoin());

        ownerActionsContainer = findViewById(R.id.ownerActionsContainer);
        btnClosePost = findViewById(R.id.btnClosePost);
        btnClosePost.setOnClickListener(v -> closePost());
        joinRequestsContainer = findViewById(R.id.joinRequestsContainer);
        membersContainer = findViewById(R.id.membersContainer);
        tvNoRequests = findViewById(R.id.tvNoRequests);

        loadMyProfileThenPost();
    }

    private void loadMyProfileThenPost() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                            myUserId = response.body().getUser().getId();
                        }
                        loadPost();
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        if (isFinishing()) return;
                        loadPost();
                    }
                });
    }

    private void loadPost() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .getPost(token, slug)
                .enqueue(new Callback<Post>() {
                    @Override
                    public void onResponse(Call<Post> call, Response<Post> response) {
                        if (isFinishing()) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            tvStatus.setText(getString(R.string.error_network));
                            return;
                        }
                        currentPost = response.body();
                        bindPost(currentPost);
                    }

                    @Override
                    public void onFailure(Call<Post> call, Throwable t) {
                        if (isFinishing()) return;
                        tvStatus.setText(getString(R.string.error_network));
                    }
                });
    }

    private void bindPost(Post post) {
        tvStatus.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);

        tvCategory.setText(post.getCategoryDetail() != null ? post.getCategoryDetail().getName() : "");
        tvStatusBadge.setText(post.getStatus());
        tvTitle.setText(post.getTitle());
        tvAuthor.setText(post.getAuthor() != null ? getString(R.string.crew_posted_by, post.getAuthor().getFullName()) : "");
        tvDescription.setText(post.getDescription());
        tvLocation.setText(post.getLocation() == null || post.getLocation().isEmpty() ? "" : getString(R.string.crew_location_prefix, post.getLocation()));
        tvContact.setText(post.getContactInfo() == null || post.getContactInfo().isEmpty() ? "" : getString(R.string.crew_contact_prefix, post.getContactInfo()));
        tvCapacity.setText(post.getMaxMembers() == null
                ? getString(R.string.crew_no_limit)
                : getString(R.string.crew_capacity_format, post.getAcceptedMembersCount(), post.getMaxMembers()));

        detailsContainer.removeAllViews();
        Map<String, String> details = post.getDetails();
        if (details != null) {
            for (Map.Entry<String, String> entry : details.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
                TextView row = new TextView(this);
                row.setTextColor(getResources().getColor(R.color.color_text_body, null));
                row.setTextSize(13f);
                row.setPadding(0, 4, 0, 4);
                row.setText(prettifyKey(entry.getKey()) + ": " + entry.getValue());
                detailsContainer.addView(row);
            }
        }

        if (post.getSkills() != null && !post.getSkills().isEmpty()) {
            tvSkills.setText(getString(R.string.crew_skills_prefix, String.join(", ", post.getSkills())));
        } else {
            tvSkills.setText("");
        }

        boolean isOwner = post.getAuthor() != null && post.getAuthor().getId() == myUserId;
        joinActionsContainer.setVisibility(isOwner ? View.GONE : View.VISIBLE);
        ownerActionsContainer.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        boolean canJoin = "OPEN".equals(post.getStatus());
        btnRequestJoin.setEnabled(canJoin);
        btnRequestJoin.setText(canJoin ? getString(R.string.crew_request_join) : getString(R.string.crew_post_not_open));

        if (isOwner) {
            loadJoinRequests();
            loadMembers();
        }
    }

    private String prettifyKey(String key) {
        String spaced = key.replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private void requestToJoin() {
        String message = etJoinMessage.getText().toString().trim();
        btnRequestJoin.setEnabled(false);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        JoinRequestRequest body = new JoinRequestRequest(currentPost.getId(), message);
        RetrofitClient.createService(CrewApiService.class)
                .createJoinRequest(token, body)
                .enqueue(new Callback<JoinRequestModel>() {
                    @Override
                    public void onResponse(Call<JoinRequestModel> call, Response<JoinRequestModel> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(PostDetailActivity.this, getString(R.string.crew_join_request_sent), Toast.LENGTH_SHORT).show();
                            etJoinMessage.setText("");
                        } else {
                            btnRequestJoin.setEnabled(true);
                            String msg = ApiError.extract(response, getString(R.string.crew_join_request_failed), "post", "non_field_errors");
                            Toast.makeText(PostDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JoinRequestModel> call, Throwable t) {
                        if (isFinishing()) return;
                        btnRequestJoin.setEnabled(true);
                        Toast.makeText(PostDetailActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void closePost() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .closePost(token, slug)
                .enqueue(new Callback<Post>() {
                    @Override
                    public void onResponse(Call<Post> call, Response<Post> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentPost = response.body();
                            bindPost(currentPost);
                        }
                    }

                    @Override
                    public void onFailure(Call<Post> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(PostDetailActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadJoinRequests() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .getJoinRequests(token, slug)
                .enqueue(new Callback<List<JoinRequestModel>>() {
                    @Override
                    public void onResponse(Call<List<JoinRequestModel>> call, Response<List<JoinRequestModel>> response) {
                        if (isFinishing()) return;
                        joinRequestsContainer.removeAllViews();

                        List<JoinRequestModel> all = response.isSuccessful() && response.body() != null
                                ? response.body() : java.util.Collections.emptyList();

                        boolean anyPending = false;
                        for (JoinRequestModel jr : all) {
                            if (!"PENDING".equals(jr.getStatus())) continue;
                            anyPending = true;
                            addJoinRequestRow(jr);
                        }
                        tvNoRequests.setVisibility(anyPending ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<List<JoinRequestModel>> call, Throwable t) {}
                });
    }

    private void addJoinRequestRow(JoinRequestModel jr) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_join_request, joinRequestsContainer, false);
        ((TextView) row.findViewById(R.id.tvRequesterName)).setText(jr.getRequester() != null ? jr.getRequester().getFullName() : "");
        TextView tvMessage = row.findViewById(R.id.tvRequestMessage);
        if (jr.getMessage() == null || jr.getMessage().isEmpty()) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setText(jr.getMessage());
        }

        row.findViewById(R.id.btnAccept).setOnClickListener(v -> respondToRequest(jr.getId(), true));
        row.findViewById(R.id.btnReject).setOnClickListener(v -> respondToRequest(jr.getId(), false));

        joinRequestsContainer.addView(row);
    }

    private void respondToRequest(int joinRequestId, boolean accept) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        CrewApiService api = RetrofitClient.createService(CrewApiService.class);
        Call<JoinRequestModel> call = accept ? api.acceptJoinRequest(token, joinRequestId) : api.rejectJoinRequest(token, joinRequestId);

        call.enqueue(new Callback<JoinRequestModel>() {
            @Override
            public void onResponse(Call<JoinRequestModel> call, Response<JoinRequestModel> response) {
                if (isFinishing()) return;
                loadPost();
            }

            @Override
            public void onFailure(Call<JoinRequestModel> call, Throwable t) {
                if (isFinishing()) return;
                Toast.makeText(PostDetailActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMembers() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(CrewApiService.class)
                .getPostMembers(token, slug)
                .enqueue(new Callback<List<PostMember>>() {
                    @Override
                    public void onResponse(Call<List<PostMember>> call, Response<List<PostMember>> response) {
                        if (isFinishing()) return;
                        membersContainer.removeAllViews();
                        if (!response.isSuccessful() || response.body() == null) return;

                        for (PostMember member : response.body()) {
                            TextView row = new TextView(PostDetailActivity.this);
                            row.setTextColor(getResources().getColor(R.color.color_text_body, null));
                            row.setTextSize(13f);
                            row.setPadding(0, 6, 0, 6);
                            row.setText("\u2022 " + (member.getUser() != null ? member.getUser().getFullName() : ""));
                            membersContainer.addView(row);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PostMember>> call, Throwable t) {}
                });
    }
}
