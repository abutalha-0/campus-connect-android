package com.campusconnect.app.classroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.FeedComment;
import com.campusconnect.app.classroom.model.FeedCommentRequest;
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

/** Full post view: body, voting, and comments. */
public class FeedPostDetailActivity extends BaseActivity {

    private static final String EXTRA_POST_ID = "post_id";

    public static void start(Context ctx, int postId) {
        Intent i = new Intent(ctx, FeedPostDetailActivity.class);
        i.putExtra(EXTRA_POST_ID, postId);
        ctx.startActivity(i);
    }

    private int postId;
    private FeedPost currentPost;

    private TextView tvAvatar, tvAuthor, tvRole, tvTime, tvTag, tvTitle, tvBody;
    private TextView tvScore, btnUpvote, btnDownvote;
    private View btnEdit, btnDelete;
    private LinearLayout commentsContainer;
    private EditText etComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_post_detail);

        postId = getIntent().getIntExtra(EXTRA_POST_ID, -1);

        tvAvatar = findViewById(R.id.tvAvatar);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvRole = findViewById(R.id.tvRole);
        tvTime = findViewById(R.id.tvTime);
        tvTag = findViewById(R.id.tvTag);
        tvTitle = findViewById(R.id.tvTitle);
        tvBody = findViewById(R.id.tvBody);
        tvScore = findViewById(R.id.tvScore);
        btnUpvote = findViewById(R.id.btnUpvote);
        btnDownvote = findViewById(R.id.btnDownvote);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        commentsContainer = findViewById(R.id.commentsContainer);
        etComment = findViewById(R.id.etComment);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnUpvote.setOnClickListener(v -> vote(1));
        btnDownvote.setOnClickListener(v -> vote(-1));
        btnEdit.setOnClickListener(v -> {
            if (currentPost != null) startActivity(AddFeedPostActivity.editIntent(this, currentPost));
        });
        btnDelete.setOnClickListener(v -> confirmDeletePost());
        findViewById(R.id.btnSendComment).setOnClickListener(v -> sendComment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPost();
        loadComments();
    }

    // ── Post ──────────────────────────────────────────────────────────────

    /** No single-post endpoint exists, so we reuse the list call and pick this post out of it. */
    private void loadPost() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .getPosts(token)
                .enqueue(new Callback<List<FeedPost>>() {
                    @Override
                    public void onResponse(Call<List<FeedPost>> call, Response<List<FeedPost>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            for (FeedPost p : response.body()) {
                                if (p.getId() == postId) {
                                    populate(p);
                                    return;
                                }
                            }
                            Toast.makeText(FeedPostDetailActivity.this,
                                    "This post is no longer available.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FeedPost>> call, Throwable t) {
                        // leave as-is
                    }
                });
    }

    private void populate(FeedPost post) {
        currentPost = post;

        FeedPost.Author author = post.getAuthor();
        String name = author != null ? author.getFullName() : "";
        String role = author != null ? author.getRole() : "";

        tvAvatar.setText(initialsOf(name));
        tvAuthor.setText(name);

        tvRole.setText(role);
        int roleColor = "FACULTY".equals(role) ? getResources().getColor(R.color.color_cyan, null)
                : "CR".equals(role) ? getResources().getColor(R.color.color_purple, null)
                : getResources().getColor(R.color.color_muted, null);
        tvRole.setTextColor(roleColor);
        tvRole.setBackgroundTintList(ColorStateList.valueOf((roleColor & 0x00FFFFFF) | 0x24000000));

        tvTime.setText(RelativeTime.format(post.getCreatedAt()));

        if (post.getTag() != null && !post.getTag().isEmpty()) {
            tvTag.setVisibility(View.VISIBLE);
            tvTag.setText(post.getTag());
        } else {
            tvTag.setVisibility(View.GONE);
        }

        tvTitle.setText(post.getTitle());
        tvBody.setText(post.getBody());

        tvScore.setText(String.valueOf(post.getScore()));
        btnUpvote.setTextColor(getResources().getColor(
                post.getMyVote() == 1 ? R.color.color_cyan : R.color.color_muted, null));
        btnDownvote.setTextColor(getResources().getColor(
                post.getMyVote() == -1 ? R.color.color_red : R.color.color_muted, null));

        btnEdit.setVisibility(post.canEdit() ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(post.canEdit() ? View.VISIBLE : View.GONE);
    }

    private void vote(int value) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .vote(token, postId, new FeedVoteRequest(value))
                .enqueue(new Callback<FeedPost>() {
                    @Override
                    public void onResponse(Call<FeedPost> call, Response<FeedPost> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            populate(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<FeedPost> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FeedPostDetailActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeletePost() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.feed_delete_title))
                .setMessage(getString(R.string.feed_delete_message))
                .setPositiveButton("Delete", (d, w) -> deletePost())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .deletePost(token, postId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(FeedPostDetailActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(FeedPostDetailActivity.this, "Couldn't delete. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FeedPostDetailActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Comments ──────────────────────────────────────────────────────────

    private void loadComments() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .getComments(token, postId)
                .enqueue(new Callback<List<FeedComment>>() {
                    @Override
                    public void onResponse(Call<List<FeedComment>> call, Response<List<FeedComment>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            renderComments(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FeedComment>> call, Throwable t) {
                        // leave as-is
                    }
                });
    }

    private void renderComments(List<FeedComment> comments) {
        commentsContainer.removeAllViews();
        if (comments.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.feed_comments_empty));
            empty.setTextColor(getResources().getColor(R.color.color_muted, null));
            empty.setTextSize(12.5f);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, dp(16), 0, dp(16));
            commentsContainer.addView(empty);
            return;
        }
        for (FeedComment c : comments) {
            commentsContainer.addView(buildCommentRow(c));
        }
    }

    private View buildCommentRow(FeedComment comment) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_feed_comment, commentsContainer, false);

        FeedPost.Author author = comment.getAuthor();
        String name = author != null ? author.getFullName() : "";
        String role = author != null ? author.getRole() : "";

        ((TextView) row.findViewById(R.id.tvAvatar)).setText(initialsOf(name));
        ((TextView) row.findViewById(R.id.tvAuthor)).setText(name);

        TextView roleBadge = row.findViewById(R.id.tvRole);
        roleBadge.setText(role);
        int roleColor = "FACULTY".equals(role) ? getResources().getColor(R.color.color_cyan, null)
                : "CR".equals(role) ? getResources().getColor(R.color.color_purple, null)
                : getResources().getColor(R.color.color_muted, null);
        roleBadge.setTextColor(roleColor);
        roleBadge.setBackgroundTintList(ColorStateList.valueOf((roleColor & 0x00FFFFFF) | 0x24000000));

        ((TextView) row.findViewById(R.id.tvTime)).setText(RelativeTime.format(comment.getCreatedAt()));
        ((TextView) row.findViewById(R.id.tvText)).setText(comment.getText());

        View btnEdit = row.findViewById(R.id.btnEdit);
        View btnDelete = row.findViewById(R.id.btnDelete);
        if (comment.canEdit()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v -> promptEditComment(comment));
            btnDelete.setOnClickListener(v -> confirmDeleteComment(comment));
        }
        return row;
    }

    private void promptEditComment(FeedComment comment) {
        EditText input = new EditText(this);
        input.setText(comment.getText());
        input.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        int pad = dp(20);
        input.setPadding(pad, dp(12), pad, 0);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.feed_edit_post))
                .setView(input)
                .setPositiveButton("Save", (d, w) -> editComment(comment, input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editComment(FeedComment comment, String newText) {
        if (newText.isEmpty()) return;
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .updateComment(token, postId, comment.getId(), new FeedCommentRequest(newText))
                .enqueue(new Callback<FeedComment>() {
                    @Override
                    public void onResponse(Call<FeedComment> call, Response<FeedComment> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            loadComments();
                        } else {
                            Toast.makeText(FeedPostDetailActivity.this, "Couldn't save. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FeedComment> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FeedPostDetailActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteComment(FeedComment comment) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.feed_comment_delete_title))
                .setPositiveButton("Delete", (d, w) -> deleteComment(comment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteComment(FeedComment comment) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .deleteComment(token, postId, comment.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            loadComments();
                        } else {
                            Toast.makeText(FeedPostDetailActivity.this, "Couldn't delete. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FeedPostDetailActivity.this, getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendComment() {
        String text = etComment.getText().toString().trim();
        if (text.isEmpty()) return;

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .addComment(token, postId, new FeedCommentRequest(text))
                .enqueue(new Callback<FeedComment>() {
                    @Override
                    public void onResponse(Call<FeedComment> call, Response<FeedComment> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful()) {
                            etComment.setText("");
                            loadComments();
                        } else {
                            Toast.makeText(FeedPostDetailActivity.this, "Couldn't post comment. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FeedComment> call, Throwable t) {
                        if (isFinishing()) return;
                        Toast.makeText(FeedPostDetailActivity.this, getString(R.string.error_network),
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
