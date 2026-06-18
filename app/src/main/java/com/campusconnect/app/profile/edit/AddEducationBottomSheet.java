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
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.profile.ProfileApiService;
import com.campusconnect.app.profile.models.Education;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AddEducationBottomSheet
 * ────────────────────────
 * CHANGED: the education endpoint is multipart/form-data (not JSON), and
 * start_year / end_year are integers on the server. We still type them
 * into plain EditTexts, but now we:
 *   1. Validate they parse as integers.
 *   2. Wrap every field as an okhttp3.RequestBody "text/plain" part.
 *   3. Call the @Multipart addEducation(...) method.
 */
public class AddEducationBottomSheet extends BottomSheetDialogFragment {

    private EditText etInstitution, etDegree, etStartYear, etEndYear;
    private TokenManager tokenManager;

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener l) { this.onSavedListener = l; }

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

        // CHANGED: validate that years are actually integers, since the
        // server field type is integer, not string.
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

        TextView btnSave = requireView().findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText("Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        // CHANGED: wrap each field as a multipart RequestBody part
        RequestBody institutionPart = textPart(institution);
        RequestBody degreePart      = textPart(degree);
        RequestBody startYearPart   = textPart(String.valueOf(startYear));
        RequestBody endYearPart     = endYear != null ? textPart(String.valueOf(endYear)) : null;

        RetrofitClient.createService(ProfileApiService.class)
                .addEducation(token, institutionPart, degreePart, startYearPart, endYearPart)
                .enqueue(new Callback<Education>() {
                    @Override
                    public void onResponse(Call<Education> call, Response<Education> response) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Add");
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Education added!", Toast.LENGTH_SHORT).show();
                            if (onSavedListener != null) onSavedListener.onSaved();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to add education. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Education> call, Throwable t) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Add");
                        Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}