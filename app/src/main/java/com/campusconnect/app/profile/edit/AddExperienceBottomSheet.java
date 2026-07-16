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
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.ExperienceRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Doubles as "Add Experience" and "Edit Experience" — call setEditing() with
 * an existing Experience before show() to pre-fill and switch to update mode.
 */
public class AddExperienceBottomSheet extends BaseBottomSheet {

    private EditText etTitle, etOrganization, etStartDate, etEndDate, etDescription;
    private TokenManager tokenManager;

    @Nullable private Experience editingExperience;

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener l) { this.onSavedListener = l; }

    public void setEditing(@Nullable Experience experience) { this.editingExperience = experience; }

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

        if (editingExperience != null) {
            ((TextView) view.findViewById(R.id.tvSheetTitle)).setText("Edit Experience");
            ((TextView) view.findViewById(R.id.btnSave)).setText("Save");
            etTitle.setText(editingExperience.getTitle());
            etOrganization.setText(editingExperience.getOrganization());
            etStartDate.setText(editingExperience.getStartDate());
            etEndDate.setText(editingExperience.getEndDate());
            etDescription.setText(editingExperience.getDescription());
        }

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

        boolean isEdit = editingExperience != null;
        TextView btnSave = requireView().findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText(isEdit ? "Saving…" : "Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        String endOrNull = end.isEmpty() ? null : end;
        ExperienceRequest body = new ExperienceRequest(title, org, desc, start, endOrNull);

        Call<Experience> call = isEdit
                ? RetrofitClient.createService(ProfileApiService.class)
                        .updateExperience(token, editingExperience.getId(), body)
                : RetrofitClient.createService(ProfileApiService.class)
                        .addExperience(token, body);

        call.enqueue(new Callback<Experience>() {
            @Override
            public void onResponse(Call<Experience> call, Response<Experience> response) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(),
                            isEdit ? "Experience updated!" : "Experience added!",
                            Toast.LENGTH_SHORT).show();
                    if (onSavedListener != null) onSavedListener.onSaved();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(),
                            isEdit ? "Failed to update experience. Try again."
                                   : "Failed to add experience. Try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Experience> call, Throwable t) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
