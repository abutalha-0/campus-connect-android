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
import com.campusconnect.app.profile.models.Project;
import com.campusconnect.app.profile.models.ProjectRequest;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AddProjectBottomSheet
 * ──────────────────────
 * CHANGED: the "url" field was removed — the real API
 * (POST /api/profiles/me/projects/) only accepts name, description,
 * and associated_with. Calls addProject(token, ProjectRequest) now.
 */
public class AddProjectBottomSheet extends BottomSheetDialogFragment {

    private EditText etName, etAssociatedWith, etDescription;
    private TokenManager tokenManager;

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener l) { this.onSavedListener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_project, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager     = new TokenManager(requireContext());
        etName           = view.findViewById(R.id.etProjectName);
        etAssociatedWith = view.findViewById(R.id.etAssociatedWith);
        etDescription    = view.findViewById(R.id.etDescription);
        // NOTE: etUrl is gone — the field no longer exists in the layout

        view.findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void save() {
        String name  = etName.getText().toString().trim();
        String assoc = etAssociatedWith.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Project name is required");
            return;
        }

        TextView btnSave = requireView().findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText("Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        // CHANGED: build a ProjectRequest instead of passing raw strings + url
        ProjectRequest body = new ProjectRequest(name, assoc, desc);

        RetrofitClient.createService(ProfileApiService.class)
                .addProject(token, body)   // CHANGED signature
                .enqueue(new Callback<Project>() {
                    @Override
                    public void onResponse(Call<Project> call, Response<Project> response) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Add");
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Project added!", Toast.LENGTH_SHORT).show();
                            if (onSavedListener != null) onSavedListener.onSaved();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to add project. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Project> call, Throwable t) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Add");
                        Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}