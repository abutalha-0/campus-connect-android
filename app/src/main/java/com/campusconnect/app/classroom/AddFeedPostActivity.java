package com.campusconnect.app.classroom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.FeedPost;
import com.campusconnect.app.classroom.model.FeedPostRequest;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Create or edit a feed post. The tag list is drawn live from the student's own class. */
public class AddFeedPostActivity extends BaseActivity {

    private static final String EXTRA_POST_ID = "post_id";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_BODY = "body";
    private static final String EXTRA_TAG = "tag";

    public static Intent createIntent(Context ctx) {
        return new Intent(ctx, AddFeedPostActivity.class);
    }

    public static Intent editIntent(Context ctx, FeedPost post) {
        Intent i = createIntent(ctx);
        i.putExtra(EXTRA_POST_ID, post.getId());
        i.putExtra(EXTRA_TITLE, post.getTitle());
        i.putExtra(EXTRA_BODY, post.getBody());
        i.putExtra(EXTRA_TAG, post.getTag());
        return i;
    }

    private int postId = -1;
    private EditText etTitle, etBody;
    private Spinner spinnerTag;
    private TextView btnSave;

    private final List<String> tagOptions = new ArrayList<>();
    private String pendingTagSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed_post);

        postId = getIntent().getIntExtra(EXTRA_POST_ID, -1);

        etTitle = findViewById(R.id.etTitle);
        etBody = findViewById(R.id.etBody);
        spinnerTag = findViewById(R.id.spinnerTag);
        btnSave = findViewById(R.id.btnSavePost);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(
                postId == -1 ? R.string.feed_new_post : R.string.feed_edit_post));
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());

        if (postId != -1) {
            etTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
            etBody.setText(getIntent().getStringExtra(EXTRA_BODY));
            pendingTagSelection = getIntent().getStringExtra(EXTRA_TAG);
        }

        loadTagOptions();
    }

    private void loadTagOptions() {
        tagOptions.add(getString(R.string.feed_no_tag));

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(FeedApiService.class)
                .getTagOptions(token)
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            tagOptions.addAll(response.body());
                        }
                        populateSpinner();
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        if (isFinishing()) return;
                        populateSpinner();
                    }
                });
    }

    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, tagOptions);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerTag.setAdapter(adapter);

        if (pendingTagSelection != null && !pendingTagSelection.isEmpty()) {
            int index = tagOptions.indexOf(pendingTagSelection);
            if (index >= 0) spinnerTag.setSelection(index);
        }
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        if (title.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int selected = spinnerTag.getSelectedItemPosition();
        String tag = selected > 0 ? tagOptions.get(selected) : "";

        btnSave.setEnabled(false);
        btnSave.setText(getString(R.string.loading));

        FeedPostRequest requestBody = new FeedPostRequest(title, body, tag);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        FeedApiService api = RetrofitClient.createService(FeedApiService.class);
        Call<FeedPost> call = postId == -1
                ? api.addPost(token, requestBody)
                : api.updatePost(token, postId, requestBody);

        call.enqueue(new Callback<FeedPost>() {
            @Override
            public void onResponse(Call<FeedPost> call, Response<FeedPost> response) {
                if (isFinishing()) return;
                if (response.isSuccessful()) {
                    finish();
                } else {
                    resetSaveButton();
                    Toast.makeText(AddFeedPostActivity.this,
                            getString(R.string.feed_post_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FeedPost> call, Throwable t) {
                if (isFinishing()) return;
                resetSaveButton();
                Toast.makeText(AddFeedPostActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText(getString(R.string.feed_post_btn));
    }
}
