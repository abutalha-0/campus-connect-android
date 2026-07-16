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
import com.campusconnect.app.profile.models.Project;
import com.campusconnect.app.profile.models.ProjectRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Doubles as "Add Project" and "Edit Project" — call setEditing() with an
 * existing Project before show() to pre-fill and switch to update mode.
 */
public class AddProjectBottomSheet extends BaseBottomSheet {

    private EditText etName, etAssociatedWith, etDescription;
    private TokenManager tokenManager;

    @Nullable private Project editingProject;

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener l) { this.onSavedListener = l; }

    public void setEditing(@Nullable Project project) { this.editingProject = project; }

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

        if (editingProject != null) {
            ((TextView) view.findViewById(R.id.tvSheetTitle)).setText("Edit Project");
            ((TextView) view.findViewById(R.id.btnSave)).setText("Save");
            etName.setText(editingProject.getName());
            etAssociatedWith.setText(editingProject.getAssociatedWith());
            etDescription.setText(editingProject.getDescription());
        }

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

        boolean isEdit = editingProject != null;
        TextView btnSave = requireView().findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText(isEdit ? "Saving…" : "Adding…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        ProjectRequest body = new ProjectRequest(name, assoc, desc);

        Call<Project> call = isEdit
                ? RetrofitClient.createService(ProfileApiService.class)
                        .updateProject(token, editingProject.getId(), body)
                : RetrofitClient.createService(ProfileApiService.class)
                        .addProject(token, body);

        call.enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(),
                            isEdit ? "Project updated!" : "Project added!",
                            Toast.LENGTH_SHORT).show();
                    if (onSavedListener != null) onSavedListener.onSaved();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(),
                            isEdit ? "Failed to update project. Try again."
                                   : "Failed to add project. Try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                btnSave.setText(isEdit ? "Save" : "Add");
                Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
