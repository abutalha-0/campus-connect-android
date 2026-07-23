package com.campusconnect.app.classroom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.campusconnect.app.classroom.util.NoticeDates;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseActivity;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.FileUtils;

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

/** Post or edit a notice. */
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
    private CheckBox cbHighlight, cbDate;
    private View dateTimeRow;
    private TextView btnPickDate, btnPickTime, btnPickFile, btnSave;

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
        cbHighlight = findViewById(R.id.cbHighlight);
        cbDate = findViewById(R.id.cbDate);
        dateTimeRow = findViewById(R.id.dateTimeRow);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnPickFile = findViewById(R.id.btnPickFile);
        btnSave = findViewById(R.id.btnSaveNotice);

        ((TextView) findViewById(R.id.tvSheetTitle)).setText(getString(
                noticeId == -1 ? R.string.post_notice_title : R.string.edit_notice_title));
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        cbHighlight.setOnCheckedChangeListener((b, checked) ->
                etHighlight.setVisibility(checked ? View.VISIBLE : View.GONE));

        cbDate.setOnCheckedChangeListener((b, checked) -> {
            dateTimeRow.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (checked) {
                if (year == -1) openDatePicker();
            } else {
                clearDateTime();
            }
        });

        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnPickTime.setOnClickListener(v -> openTimePicker());

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

        String eventDate = getIntent().getStringExtra(EXTRA_EVENT_DATE);
        String eventTime = getIntent().getStringExtra(EXTRA_EVENT_TIME);
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
            cbDate.setChecked(true);
            dateTimeRow.setVisibility(View.VISIBLE);
            updateDateTimeLabels();
        }
    }

    // ── Date / time pickers ──────────────────────────────────────────────

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        int y = year != -1 ? year : cal.get(Calendar.YEAR);
        int m = month != -1 ? month : cal.get(Calendar.MONTH);
        int d = day != -1 ? day : cal.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, pickedYear, pickedMonth, pickedDay) -> {
            year = pickedYear;
            month = pickedMonth;
            day = pickedDay;
            updateDateTimeLabels();
        }, y, m, d).show();
        // Note: if the user cancels without ever having picked a date, the
        // checkbox stays checked but the row shows "Select date" — save()
        // requires a date once the checkbox is on.
    }

    private void openTimePicker() {
        Calendar cal = Calendar.getInstance();
        int h = hour != -1 ? hour : cal.get(Calendar.HOUR_OF_DAY);
        int min = minute != -1 ? minute : cal.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, pickedHour, pickedMinute) -> {
            hour = pickedHour;
            minute = pickedMinute;
            updateDateTimeLabels();
        }, h, min, false).show();
    }

    private void updateDateTimeLabels() {
        if (year != -1) {
            String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            btnPickDate.setText(NoticeDates.format(dateStr, null));
            btnPickDate.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        }
        if (hour != -1) {
            String timeStr = String.format(Locale.US, "%02d:%02d:00", hour, minute);
            btnPickTime.setText(NoticeDates.formatTimeOnly(timeStr));
            btnPickTime.setTextColor(getResources().getColor(R.color.color_text_primary, null));
        }
    }

    private void clearDateTime() {
        year = month = day = hour = minute = -1;
        btnPickDate.setText(getString(R.string.notice_pick_date));
        btnPickDate.setTextColor(getResources().getColor(R.color.color_muted, null));
        btnPickTime.setText(getString(R.string.notice_pick_time));
        btnPickTime.setTextColor(getResources().getColor(R.color.color_muted, null));
    }

    // ── Save ──────────────────────────────────────────────────────────────

    private void save() {
        String text = etText.getText().toString().trim();
        String highlight = cbHighlight.isChecked() ? etHighlight.getText().toString().trim() : "";

        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if (cbDate.isChecked() && year == -1) {
            Toast.makeText(this, getString(R.string.notice_pick_date), Toast.LENGTH_SHORT).show();
            return;
        }

        String eventDate = cbDate.isChecked() && year != -1
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
        // here — only include the fields when a date is actually set.
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
        btnSave.setEnabled(true);
        btnSave.setText("Save");
    }

    private RequestBody text(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }
}
