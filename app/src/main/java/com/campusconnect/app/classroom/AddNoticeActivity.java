package com.campusconnect.app.classroom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Notice;
import com.campusconnect.app.classroom.model.NoticeRequest;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Post or edit a notice. */
public class AddNoticeActivity extends BaseActivity {

    private static final String EXTRA_SUBJECT_ID = "subject_id";
    private static final String EXTRA_NOTICE_ID = "notice_id";
    private static final String EXTRA_TEXT = "text";
    private static final String EXTRA_HIGHLIGHT = "highlight";

    public static Intent createIntent(Context ctx, int subjectId) {
        Intent i = new Intent(ctx, AddNoticeActivity.class);
        i.putExtra(EXTRA_SUBJECT_ID, subjectId);
        return i;
    }

    public static Intent editIntent(Context ctx, int subjectId, Notice n) {
        Intent i = createIntent(ctx, subjectId);
        i.putExtra(EXTRA_NOTICE_ID, n.getId());
        i.putExtra(EXTRA_TEXT, n.getText());
        i.putExtra(EXTRA_HIGHLIGHT, n.getHighlight());
        return i;
    }

    private int subjectId;
    private int noticeId = -1;

    private EditText etText, etHighlight;
    private CheckBox cbHighlight;
    private TextView btnPickFile, btnSave;

    private Uri pickedFileUri;
    private ActivityResultLauncher<String> filePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notice);

        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        noticeId = getIntent().getIntExtra(EXTRA_NOTICE_ID, -1);

        etText = findViewById(R.id.etText);
        etHighlight = findViewById(R.id.etHighlight);
        cbHighlight = findViewById(R.id.cbHighlight);
        btnPickFile = findViewById(R.id.btnPickFile);
        btnSave = findViewById(R.id.btnSaveNotice);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(
                noticeId == -1 ? R.string.post_notice_title : R.string.edit_notice_title));
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        cbHighlight.setOnCheckedChangeListener((b, checked) ->
                etHighlight.setVisibility(checked ? View.VISIBLE : View.GONE));

        filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pickedFileUri = uri;
                        btnPickFile.setText(FileUtils.displayName(this, uri));
                        btnPickFile.setTextColor(getResources().getColor(R.color.color_text_primary, null));
                    }
                });

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        btnPickFile.setOnClickListener(v -> filePicker.launch("*/*"));
        btnSave.setOnClickListener(v -> save());

        prefillIfEditing();
    }

    private void prefillIfEditing() {
        if (noticeId == -1) return;
        etText.setText(getIntent().getStringExtra(EXTRA_TEXT));
        String highlight = getIntent().getStringExtra(EXTRA_HIGHLIGHT);
        if (highlight != null && !highlight.isEmpty()) {
            cbHighlight.setChecked(true);
            etHighlight.setVisibility(View.VISIBLE);
            etHighlight.setText(highlight);
        }
    }

    private void save() {
        String text = etText.getText().toString().trim();
        String highlight = cbHighlight.isChecked() ? etHighlight.getText().toString().trim() : "";

        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText(getString(R.string.loading));

        if (pickedFileUri != null) {
            saveMultipart(text, highlight);
        } else {
            saveJson(new NoticeRequest(text, highlight));
        }
    }

    private void saveJson(NoticeRequest body) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        NoticeApiService api = RetrofitClient.createService(NoticeApiService.class);
        Call<Notice> call = noticeId == -1
                ? api.addNotice(token, subjectId, body)
                : api.updateNotice(token, subjectId, noticeId, body);
        call.enqueue(saveCallback());
    }

    private void saveMultipart(String text, String highlight) {
        File file;
        try {
            file = FileUtils.copyToCache(this, pickedFileUri);
        } catch (Exception e) {
            resetSaveButton();
            Toast.makeText(this, "Couldn't read that file. Try another.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("text", text(text));
        fields.put("highlight", text(highlight));

        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileBody);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        NoticeApiService api = RetrofitClient.createService(NoticeApiService.class);
        Call<Notice> call = noticeId == -1
                ? api.addNoticeMultipart(token, subjectId, fields, filePart)
                : api.updateNoticeMultipart(token, subjectId, noticeId, fields, filePart);
        call.enqueue(saveCallback());
    }

    private Callback<Notice> saveCallback() {
        return new Callback<Notice>() {
            @Override
            public void onResponse(Call<Notice> call, Response<Notice> response) {
                if (isFinishing()) return;
                if (response.isSuccessful()) {
                    finish();
                } else {
                    resetSaveButton();
                    Toast.makeText(AddNoticeActivity.this,
                            getString(R.string.resource_save_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Notice> call, Throwable t) {
                if (isFinishing()) return;
                resetSaveButton();
                Toast.makeText(AddNoticeActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Save");
    }

    private RequestBody text(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }
}
