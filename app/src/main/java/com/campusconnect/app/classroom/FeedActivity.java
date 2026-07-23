package com.campusconnect.app.classroom;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.FeedPost;
import com.campusconnect.app.classroom.model.FeedVoteRequest;
import com.campusconnect.app.classroom.util.RelativeTime;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Class-wide discussion feed: list of posts, voting, and entry to create/view. */
public class FeedActivity extends BaseActivity {

    private LinearLayout postsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        postsContainer = findViewById(R.id.postsContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNewPost).setOnClickListener(v ->
                startActivity(AddFeedPostActivity.createIntent(this)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }

    private void loadPosts() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .getPosts(token)
                .enqueue(new Callback<List<FeedPost>>() {
                    @Override
                    public void onResponse(Call<List<FeedPost>> call, Response<List<FeedPost>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            renderPosts(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FeedPost>> call, Throwable t) {
                        // leave as-is
                    }
                });
    }

    private void renderPosts(List<FeedPost> posts) {
        postsContainer.removeAllViews();
        if (posts.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.feed_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, dp(40));
            postsContainer.addView(empty);
            return;
        }
        for (FeedPost post : posts) {
            postsContainer.addView(buildPostCard(post));
        }
    }

    private View buildPostCard(FeedPost post) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_feed_post, postsContainer, false);

        FeedPost.Author author = post.getAuthor();
        String name = author != null ? author.getFullName() : "";
        String role = author != null ? author.getRole() : "";

        ((TextView) card.findViewById(R.id.tvAvatar)).setText(initialsOf(name));
        ((TextView) card.findViewById(R.id.tvAuthor)).setText(name);

        TextView roleBadge = card.findViewById(R.id.tvRole);
        roleBadge.setText(role);
        int roleColor = "FACULTY".equals(role) ? getResources().getColor(R.color.color_cyan, null)
                : "CR".equals(role) ? getResources().getColor(R.color.color_purple, null)
                : getResources().getColor(R.color.color_muted, null);
        roleBadge.setTextColor(roleColor);
        roleBadge.setBackgroundTintList(ColorStateList.valueOf((roleColor & 0x00FFFFFF) | 0x24000000));

        ((TextView) card.findViewById(R.id.tvTime)).setText(RelativeTime.format(post.getCreatedAt()));

        TextView tvTag = card.findViewById(R.id.tvTag);
        if (post.getTag() != null && !post.getTag().isEmpty()) {
            tvTag.setVisibility(View.VISIBLE);
            tvTag.setText(post.getTag());
        } else {
            tvTag.setVisibility(View.GONE);
        }

        ((TextView) card.findViewById(R.id.tvTitle)).setText(post.getTitle());
        ((TextView) card.findViewById(R.id.tvBody)).setText(post.getBody());
        ((TextView) card.findViewById(R.id.tvCommentsCount))
                .setText("💬  " + post.getCommentsCount());

        bindVoteControls(card, post);

        View btnEdit = card.findViewById(R.id.btnEdit);
        View btnDelete = card.findViewById(R.id.btnDelete);
        if (post.canEdit()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v ->
                    startActivity(AddFeedPostActivity.editIntent(this, post)));
            btnDelete.setOnClickListener(v -> confirmDelete(post));
        }

        card.setOnClickListener(v -> FeedPostDetailActivity.start(this, post.getId()));

        return card;
    }

    private void bindVoteControls(View card, FeedPost post) {
        TextView tvScore = card.findViewById(R.id.tvScore);
        TextView btnUp = card.findViewById(R.id.btnUpvote);
        TextView btnDown = card.findViewById(R.id.btnDownvote);

        tvScore.setText(String.valueOf(post.getScore()));
        btnUp.setTextColor(getResources().getColor(
                post.getMyVote() == 1 ? R.color.color_cyan : R.color.color_muted, null));
        btnDown.setTextColor(getResources().getColor(
                post.getMyVote() == -1 ? R.color.color_red : R.color.color_muted, null));

        btnUp.setOnClickListener(v -> vote(post.getId(), 1));
        btnDown.setOnClickListener(v -> vote(post.getId(), -1));
    }

    private void vote(int postId, int value) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .vote(token, postId, new FeedVoteRequest(value))
                .enqueue(new Callback<FeedPost>() {
                    @Override
                    public void onResponse(Call<FeedPost> call, Response<FeedPost> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            loadPosts();
                        } else {
                            Toast.makeText(FeedActivity.this, "Couldn't vote. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FeedPost> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FeedActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(FeedPost post) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.feed_delete_title))
                .setMessage(getString(R.string.feed_delete_message))
                .setPositiveButton("Delete", (d, w) -> deletePost(post))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost(FeedPost post) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .deletePost(token, post.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(FeedActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                            loadPosts();
                        } else {
                            Toast.makeText(FeedActivity.this, "Couldn't delete. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FeedActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
