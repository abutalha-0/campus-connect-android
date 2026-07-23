package com.campusconnect.app.classroom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.campusconnect.app.R;
import com.campusconnect.app.classroom.model.Notice;
import com.campusconnect.app.classroom.util.NoticeDates;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.FileUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Post or edit a notice. "Highlight" is one unified section — an optional
 * free-text label and/or an optional date/time — since both feed the same
 * highlighted callout on the notice card. A live preview shows exactly what
 * that callout will look like as the faculty/CR builds it.
 */
public class AddNoticeActivity extends BaseActivity {

    private static final String EXTRA_SUBJECT_ID = "subject_id";
    private static final String EXTRA_NOTICE_ID = "notice_id";
    private static final String EXTRA_TEXT = "text";
    private static final String EXTRA_HIGHLIGHT = "highlight";
    private static final String EXTRA_EVENT_DATE = "event_date";
    private static final String EXTRA_EVENT_TIME = "event_time";

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
        i.putExtra(EXTRA_EVENT_DATE, n.getEventDate());
        i.putExtra(EXTRA_EVENT_TIME, n.getEventTime());
        return i;
    }

    private int subjectId;
    private int noticeId = -1;

    private EditText etText, etHighlight;
    private SwitchMaterial swHighlight;
    private View highlightDetails, previewBox;
    private TextView chipToday, chipTomorrow, chipNextWeek;
    private TextView btnPickDate, btnPickTime, tvPreview;
    private TextView btnPickFile, btnClearFile, btnSave;

    // -1 means "not set". A date must be set before a time is meaningful.
    private int year = -1, month = -1, day = -1;
    private int hour = -1, minute = -1;

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
        swHighlight = findViewById(R.id.swHighlight);
        highlightDetails = findViewById(R.id.highlightDetails);
        previewBox = findViewById(R.id.previewBox);
        chipToday = findViewById(R.id.chipToday);
        chipTomorrow = findViewById(R.id.chipTomorrow);
        chipNextWeek = findViewById(R.id.chipNextWeek);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        tvPreview = findViewById(R.id.tvPreview);
        btnPickFile = findViewById(R.id.btnPickFile);
        btnClearFile = findViewById(R.id.btnClearFile);
        btnSave = findViewById(R.id.btnSaveNotice);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(
                noticeId == -1 ? R.string.post_notice_title : R.string.edit_notice_title));
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        swHighlight.setOnCheckedChangeListener((b, checked) -> {
            highlightDetails.setVisibility(checked ? View.VISIBLE : View.GONE);
            updatePreview();
        });
        etHighlight.addTextChangedListener(simpleWatcher(this::updatePreview));

        chipToday.setOnClickListener(v -> pickPresetDate(0));
        chipTomorrow.setOnClickListener(v -> pickPresetDate(1));
        chipNextWeek.setOnClickListener(v -> pickPresetDate(7));
        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnPickTime.setOnClickListener(v -> openTimePicker());

        etText.addTextChangedListener(simpleWatcher(this::updateSaveEnabled));

        filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pickedFileUri = uri;
                        btnPickFile.setText(FileUtils.displayName(this, uri));
                        btnPickFile.setTextColor(getResources().getColor(R.color.color_text_primary, null));
                        btnClearFile.setVisibility(View.VISIBLE);
                    }
                });

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        btnPickFile.setOnClickListener(v -> filePicker.launch("*/*"));
        btnClearFile.setOnClickListener(v -> clearFile());
        btnSave.setOnClickListener(v -> save());

        prefillIfEditing();
        updateSaveEnabled();
    }

    private void prefillIfEditing() {
        if (noticeId == -1) return;
        etText.setText(getIntent().getStringExtra(EXTRA_TEXT));

        String highlight = getIntent().getStringExtra(EXTRA_HIGHLIGHT);
        String eventDate = getIntent().getStringExtra(EXTRA_EVENT_DATE);
        String eventTime = getIntent().getStringExtra(EXTRA_EVENT_TIME);

        boolean hasHighlight = (highlight != null && !highlight.isEmpty())
                || (eventDate != null && eventDate.length() >= 10);
        if (!hasHighlight) return;

        swHighlight.setChecked(true);
        highlightDetails.setVisibility(View.VISIBLE);
        if (highlight != null) etHighlight.setText(highlight);

        if (eventDate != null && eventDate.length() >= 10) {
            String[] parts = eventDate.substring(0, 10).split("-");
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]) - 1;
            day = Integer.parseInt(parts[2]);
            if (eventTime != null && eventTime.length() >= 5) {
                String[] t = eventTime.split(":");
                hour = Integer.parseInt(t[0]);
                minute = Integer.parseInt(t[1]);
            }
            updateDateTimeLabels();
        }
        updatePreview();
    }

    // ── Date / time pickers ──────────────────────────────────────────────

    private void pickPresetDate(int daysFromToday) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromToday);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        setActiveChip(daysFromToday);
        updateDateTimeLabels();
        updatePreview();
    }

    private void setActiveChip(int daysFromToday) {
        styleChip(chipToday, daysFromToday == 0);
        styleChip(chipTomorrow, daysFromToday == 1);
        styleChip(chipNextWeek, daysFromToday == 7);
    }

    private void styleChip(TextView chip, boolean active) {
        chip.setBackgroundResource(active ? R.drawable.bg_tab_pill_active : R.drawable.bg_tab_pill_inactive);
        chip.setTextColor(getResources().getColor(active ? R.color.color_cyan : R.color.color_muted, null));
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        int y = year != -1 ? year : cal.get(Calendar.YEAR);
        int m = month != -1 ? month : cal.get(Calendar.MONTH);
        int d = day != -1 ? day : cal.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, pickedYear, pickedMonth, pickedDay) -> {
            year = pickedYear;
            month = pickedMonth;
            day = pickedDay;
            setActiveChip(-1);  // a manually picked date matches no quick chip
            updateDateTimeLabels();
            updatePreview();
        }, y, m, d).show();
    }

    private void openTimePicker() {
        Calendar cal = Calendar.getInstance();
        int h = hour != -1 ? hour : cal.get(Calendar.HOUR_OF_DAY);
        int min = minute != -1 ? minute : cal.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, pickedHour, pickedMinute) -> {
            hour = pickedHour;
            minute = pickedMinute;
            updateDateTimeLabels();
            updatePreview();
        }, h, min, false).show();
    }

    private void updateDateTimeLabels() {
        boolean dateSet = year != -1;
        btnPickTime.setEnabled(dateSet);
        btnPickTime.setAlpha(dateSet ? 1f : 0.5f);

        if (dateSet) {
            String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            btnPickDate.setText(NoticeDates.format(dateStr, null));
            btnPickDate.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        }
        if (hour != -1) {
            String timeStr = String.format(Locale.US, "%02d:%02d:00", hour, minute);
            btnPickTime.setText(NoticeDates.formatTimeOnly(timeStr));
            btnPickTime.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        } else {
            btnPickTime.setText(getString(R.string.notice_pick_time));
        }
    }

    // ── Live preview ──────────────────────────────────────────────────────

    private void updatePreview() {
        if (!swHighlight.isChecked()) {
            previewBox.setVisibility(View.GONE);
            return;
        }

        String label = etHighlight.getText().toString().trim();
        String dateLabel = year != -1
                ? NoticeDates.format(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day),
                        hour != -1 ? String.format(Locale.US, "%02d:%02d:00", hour, minute) : null)
                : null;

        String preview = combine(label, dateLabel);
        if (preview == null) {
            previewBox.setVisibility(View.GONE);
        } else {
            previewBox.setVisibility(View.VISIBLE);
            tvPreview.setText(preview);
        }
    }

    private String combine(String label, String dateLabel) {
        boolean hasLabel = label != null && !label.isEmpty();
        boolean hasDate = dateLabel != null;
        if (hasLabel && hasDate) return label + " · " + dateLabel;
        if (hasDate) return dateLabel;
        if (hasLabel) return label;
        return null;
    }

    // ── Attachment ────────────────────────────────────────────────────────

    private void clearFile() {
        pickedFileUri = null;
        btnPickFile.setText(getString(R.string.resource_attach_file));
        btnPickFile.setTextColor(getResources().getColor(R.color.color_muted, null));
        btnClearFile.setVisibility(View.GONE);
    }

    // ── Save ──────────────────────────────────────────────────────────────

    private void updateSaveEnabled() {
        boolean valid = !etText.getText().toString().trim().isEmpty();
        btnSave.setEnabled(valid);
        btnSave.setAlpha(valid ? 1f : 0.5f);
    }

    private void save() {
        String text = etText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean highlightOn = swHighlight.isChecked();
        String highlight = highlightOn ? etHighlight.getText().toString().trim() : "";
        String eventDate = highlightOn && year != -1
                ? String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day) : null;
        String eventTime = eventDate != null && hour != -1
                ? String.format(Locale.US, "%02d:%02d:00", hour, minute) : null;

        btnSave.setEnabled(false);
        btnSave.setText(getString(R.string.loading));

        if (pickedFileUri != null) {
            saveMultipart(text, highlight, eventDate, eventTime);
        } else {
            saveJson(text, highlight, eventDate, eventTime);
        }
    }

    // Built as a raw JSON body (not a typed Gson model) so event_date/
    // event_time can be sent as explicit null to clear them — the default
    // Gson used elsewhere in the app omits null fields entirely.
    private void saveJson(String text, String highlight, String eventDate, String eventTime) {
        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("highlight", highlight);
            json.put("event_date", eventDate != null ? eventDate : JSONObject.NULL);
            json.put("event_time", eventTime != null ? eventTime : JSONObject.NULL);
        } catch (JSONException e) {
            resetSaveButton();
            Toast.makeText(this, getString(R.string.resource_save_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), json.toString());

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        NoticeApiService api = RetrofitClient.createService(NoticeApiService.class);
        Call<Notice> call = noticeId == -1
                ? api.addNotice(token, subjectId, body)
                : api.updateNotice(token, subjectId, noticeId, body);
        call.enqueue(saveCallback());
    }

    private void saveMultipart(String text, String highlight, String eventDate, String eventTime) {
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
        // Multipart form fields can't carry a literal JSON null, so clearing
        // the date while simultaneously attaching a new file isn't supported
        // here — only included when a date is actually set.
        if (eventDate != null) {
            fields.put("event_date", text(eventDate));
            if (eventTime != null) fields.put("event_time", text(eventTime));
        }

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
        updateSaveEnabled();
        btnSave.setText(getString(R.string.notice_save));
    }

    private RequestBody text(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }

    private TextWatcher simpleWatcher(Runnable onChange) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { onChange.run(); }
        };
    }
}
