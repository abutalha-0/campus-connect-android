package com.campusconnect.app.profile.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.base.BaseBottomSheet;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Education;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Doubles as "Add Education" and "Edit Education" — call setEditing() with
 * an existing Education before show() to pre-fill and switch to update mode.
 *
 * The endpoint is multipart/form-data (not JSON), and start_year / end_year
 * are integers on the server, so every field gets wrapped as a text part.
 */
public class AddEducationBottomSheet extends BaseBottomSheet {

    private EditText etInstitution, etDegree, etStartYear, etEndYear;
    private TokenManager tokenManager;

    @Nullable private Education editingEducation;

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener l) { this.onSavedListener = l; }

    public void setEditing(@Nullable Education education) { this.editingEducation = education; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_education, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager  = new TokenManager(requireContext());
        etInstitution = view.findViewById(R.id.etInstitution);
        etDegree      = view.findViewById(R.id.etDegree);
        etStartYear   = view.findViewById(R.id.etStartYear);
        etEndYear     = view.findViewById(R.id.etEndYear);

        if (editingEducation != null) {
            ((TextView) view.findViewById(R.id.tvSheetTitle)).setText("Edit Education");
            ((TextView) view.findViewById(R.id.btnSave)).setText("Save");
            etInstitution.setText(editingEducation.getInstitutionName());
            etDegree.setText(editingEducation.getDegree());
            etStartYear.setText(String.valueOf(editingEducation.getStartYear()));
            if (editingEducation.getEndYear() != null) {
                etEndYear.setText(String.valueOf(editingEducation.getEndYear()));
            }
        }

        view.findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    /** Small helper: wraps a plain string as a multipart text part. */
    private RequestBody textPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private void save() {
        String institution = etInstitution.getText().toString().trim();
        String degree       = etDegree.getText().toString().trim();
        String startYearStr = etStartYear.getText().toString().trim();
        String endYearStr   = etEndYear.getText().toString().trim();

        if (institution.isEmpty()) { etInstitution.setError("Institution name is required"); return; }
        if (degree.isEmpty())      { etDegree.setError("Degree is required"); return; }
        if (startYearStr.isEmpty()) { etStartYear.setError("Start year is required"); return; }

        int startYear;
        try {
            startYear = Integer.parseInt(startYearStr);
        } catch (NumberFormatException e) {
            etStartYear.setError("Enter a valid year, e.g. 2022");
            return;
        }

        Integer endYear = null;
        if (!endYearStr.isEmpty()) {
            try {
                endYear = Integer.parseInt(endYearStr);
            } catch (NumberFormatException e) {
                etEndYear.setError("Enter a valid year, e.g. 2026");
                return;
            }
        }

        boolean isEdit = editingEducation != null;
        TextView btnSave = requireView().findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText(isEdit ? "Saving…" : "Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RequestBody institutionPart = textPart(institution);
        RequestBody degreePart      = textPart(degree);
        RequestBody startYearPart   = textPart(String.valueOf(startYear));
        RequestBody endYearPart     = endYear != null ? textPart(String.valueOf(endYear)) : null;

        Call<Education> call = isEdit
                ? RetrofitClient.createService(ProfileApiService.class)
                        .updateEducation(token, editingEducation.getId(),
                                institutionPart, degreePart, startYearPart, endYearPart)
                : RetrofitClient.createService(ProfileApiService.class)
                        .addEducation(token, institutionPart, degreePart, startYearPart, endYearPart);

        call.enqueue(new Callback<Education>() {
            @Override
            public void onResponse(Call<Education> call, Response<Education> response) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(),
                            isEdit ? "Education updated!" : "Education added!",
                            Toast.LENGTH_SHORT).show();
                    if (onSavedListener != null) onSavedListener.onSaved();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(),
                            isEdit ? "Failed to update education. Try again."
                                   : "Failed to add education. Try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Education> call, Throwable t) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
