package com.campusconnect.app.classroom;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Resource;
import com.campusconnect.app.classroom.model.ResourceRequest;
import com.campusconnect.app.classroom.util.ResourceTypes;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Add or edit a resource within a subject. */
public class AddResourceActivity extends BaseActivity {

    private static final String EXTRA_SUBJECT_ID = "subject_id";
    private static final String EXTRA_RESOURCE_ID = "resource_id";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_TYPE = "type";
    private static final String EXTRA_DESC = "desc";
    private static final String EXTRA_FILE_URL = "file_url";

    public static Intent createIntent(Context ctx, int subjectId) {
        Intent i = new Intent(ctx, AddResourceActivity.class);
        i.putExtra(EXTRA_SUBJECT_ID, subjectId);
        return i;
    }

    public static Intent editIntent(Context ctx, int subjectId, Resource r) {
        Intent i = createIntent(ctx, subjectId);
        i.putExtra(EXTRA_RESOURCE_ID, r.getId());
        i.putExtra(EXTRA_TITLE, r.getTitle());
        i.putExtra(EXTRA_TYPE, r.getResourceType());
        i.putExtra(EXTRA_DESC, r.getDescription());
        i.putExtra(EXTRA_FILE_URL, r.getFileUrl());
        return i;
    }

    private int subjectId;
    private int resourceId = -1;
    private String existingFileUrl = "";

    private EditText etTitle, etDescription, etVideoLink;
    private Spinner spinnerType;
    private TextView btnPickFile, btnSave;
    private View filePickerRow, videoLinkRow;

    private Uri pickedFileUri;
    private ActivityResultLauncher<String> filePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_resource);

        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        resourceId = getIntent().getIntExtra(EXTRA_RESOURCE_ID, -1);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etVideoLink = findViewById(R.id.etVideoLink);
        spinnerType = findViewById(R.id.spinnerType);
        btnPickFile = findViewById(R.id.btnPickFile);
        btnSave = findViewById(R.id.btnSaveResource);
        filePickerRow = findViewById(R.id.filePickerRow);
        videoLinkRow = findViewById(R.id.videoLinkRow);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(
                resourceId == -1 ? R.string.add_resource_title : R.string.edit_resource_title));
        // top_bar_edit's Save button is unused here; hide it in favour of the big button.
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.resource_types, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerType.setAdapter(adapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateTypeUi(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pickedFileUri = uri;
                        btnPickFile.setText(displayNameOf(uri));
                        btnPickFile.setTextColor(getResources().getColor(R.color.color_text_primary, null));
                    }
                });

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        btnPickFile.setOnClickListener(v -> filePicker.launch("*/*"));
        btnSave.setOnClickListener(v -> save());

        prefillIfEditing();
        updateTypeUi();
    }

    private void prefillIfEditing() {
        if (resourceId == -1) return;
        etTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
        etDescription.setText(getIntent().getStringExtra(EXTRA_DESC));
        existingFileUrl = getIntent().getStringExtra(EXTRA_FILE_URL);
        if (existingFileUrl == null) existingFileUrl = "";
        String type = getIntent().getStringExtra(EXTRA_TYPE);
        spinnerType.setSelection(ResourceTypes.indexOf(this, type));
        if ("VID".equals(type)) etVideoLink.setText(existingFileUrl);
    }

    private String currentTypeKey() {
        return ResourceTypes.keyAt(this, spinnerType.getSelectedItemPosition());
    }

    /** Videos take a URL; other types take a file upload. */
    private void updateTypeUi() {
        boolean isVideo = "VID".equals(currentTypeKey());
        videoLinkRow.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        filePickerRow.setVisibility(isVideo ? View.GONE : View.VISIBLE);
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String typeKey = currentTypeKey();

        if (title.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText(getString(R.string.loading));

        boolean isVideo = "VID".equals(typeKey);
        if (!isVideo && pickedFileUri != null) {
            saveMultipart(title, typeKey, description);
        } else {
            String fileUrl = isVideo ? etVideoLink.getText().toString().trim() : existingFileUrl;
            saveJson(new ResourceRequest(title, typeKey, description, fileUrl));
        }
    }

    // ── JSON path (no new file) ─────────────────────────────────────────────
    private void saveJson(ResourceRequest body) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        ResourceApiService api = RetrofitClient.createService(ResourceApiService.class);
        Call<Resource> call = resourceId == -1
                ? api.addResource(token, subjectId, body)
                : api.updateResource(token, subjectId, resourceId, body);
        call.enqueue(saveCallback());
    }

    // ── Multipart path (with file) ──────────────────────────────────────────
    private void saveMultipart(String title, String typeKey, String description) {
        File file;
        try {
            file = copyToCache(pickedFileUri);
        } catch (Exception e) {
            resetSaveButton();
            Toast.makeText(this, "Couldn't read that file. Try another.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("title", text(title));
        fields.put("resource_type", text(typeKey));
        fields.put("description", text(description));

        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileBody);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        ResourceApiService api = RetrofitClient.createService(ResourceApiService.class);
        Call<Resource> call = resourceId == -1
                ? api.addResourceMultipart(token, subjectId, fields, filePart)
                : api.updateResourceMultipart(token, subjectId, resourceId, fields, filePart);
        call.enqueue(saveCallback());
    }

    private Callback<Resource> saveCallback() {
        return new Callback<Resource>() {
            @Override
            public void onResponse(Call<Resource> call, Response<Resource> response) {
                if (isFinishing()) return;
                if (response.isSuccessful()) {
                    finish();
                } else {
                    resetSaveButton();
                    Toast.makeText(AddResourceActivity.this,
                            getString(R.string.resource_save_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Resource> call, Throwable t) {
                if (isFinishing()) return;
                resetSaveButton();
                Toast.makeText(AddResourceActivity.this,
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

    private File copyToCache(Uri uri) throws Exception {
        String name = displayNameOf(uri);
        File out = new File(getCacheDir(), name);
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(out)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) > 0) os.write(buffer, 0, read);
        }
        return out;
    }

    private String displayNameOf(Uri uri) {
        String name = null;
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = c.getString(idx);
            }
        } catch (Exception ignored) {}
        return name != null ? name : ("upload_" + System.currentTimeMillis());
    }
}
