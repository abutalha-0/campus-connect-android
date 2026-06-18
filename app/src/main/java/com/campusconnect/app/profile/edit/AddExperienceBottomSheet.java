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
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.ExperienceRequest;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AddExperienceBottomSheet
 * ─────────────────────────
 * CHANGED: calls addExperience(token, ExperienceRequest) instead of 5
 * raw string params — matches POST /api/profiles/me/experience/.
 * Dates must be full YYYY-MM-DD (enforced via hint text in the layout).
 */
public class AddExperienceBottomSheet extends BottomSheetDialogFragment {

    private EditText etTitle, etOrganization, etStartDate, etEndDate, etDescription;
    private TokenManager tokenManager;

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener l) { this.onSavedListener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_experience, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager   = new TokenManager(requireContext());
        etTitle        = view.findViewById(R.id.etTitle);
        etOrganization = view.findViewById(R.id.etOrganization);
        etStartDate    = view.findViewById(R.id.etStartDate);
        etEndDate      = view.findViewById(R.id.etEndDate);
        etDescription  = view.findViewById(R.id.etDescription);

        view.findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String org   = etOrganization.getText().toString().trim();
        String start = etStartDate.getText().toString().trim();
        String end   = etEndDate.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();

        if (title.isEmpty()) { etTitle.setError("Title is required"); return; }
        if (org.isEmpty())   { etOrganization.setError("Organization is required"); return; }
        if (start.isEmpty()) { etStartDate.setError("Start date is required (YYYY-MM-DD)"); return; }
        if (!start.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etStartDate.setError("Use format YYYY-MM-DD");
            return;
        }
        if (!end.isEmpty() && !end.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etEndDate.setError("Use format YYYY-MM-DD");
            return;
        }

        TextView btnSave = requireView().findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText("Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        String endOrNull = end.isEmpty() ? null : end;

        // CHANGED: build an ExperienceRequest instead of passing 5 raw params
        ExperienceRequest body = new ExperienceRequest(title, org, desc, start, endOrNull);

        RetrofitClient.createService(ProfileApiService.class)
                .addExperience(token, body)   // CHANGED signature
                .enqueue(new Callback<Experience>() {
                    @Override
                    public void onResponse(Call<Experience> call, Response<Experience> response) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Add");
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Experience added!", Toast.LENGTH_SHORT).show();
                            if (onSavedListener != null) onSavedListener.onSaved();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to add experience. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Experience> call, Throwable t) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Add");
                        Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}