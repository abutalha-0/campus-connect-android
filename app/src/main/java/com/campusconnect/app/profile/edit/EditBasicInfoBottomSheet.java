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
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.ProfileUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Edits bio + about + user type, all via one combined PATCH /api/profiles/me/.
 * The hero card and the About card each only own part of this data, so
 * setMode() hides the other section's fields — but both modes still fetch
 * and resubmit all three values together so the hidden ones aren't wiped out.
 */
public class EditBasicInfoBottomSheet extends BaseBottomSheet {

    public enum Mode { HERO, ABOUT }

    private EditText etBio, etAbout;
    private TextView checkStudent, checkCR;
    private View groupAboutFields, groupUserType;
    private String selectedUserType = "STUDENT";
    private TokenManager tokenManager;
    private Mode mode = Mode.ABOUT;

    public interface OnSavedListener { void onSaved(); }
    private OnSavedListener onSavedListener;
    public void setOnSavedListener(OnSavedListener listener) {
        this.onSavedListener = listener;
    }

    public void setMode(Mode mode) { this.mode = mode; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.bottom_sheet_edit_basic_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());

        etBio        = view.findViewById(R.id.etBio);
        etAbout      = view.findViewById(R.id.etAbout);
        checkStudent = view.findViewById(R.id.checkStudent);
        checkCR      = view.findViewById(R.id.checkCR);
        groupAboutFields = view.findViewById(R.id.groupAboutFields);
        groupUserType    = view.findViewById(R.id.groupUserType);

        TextView tvSheetTitle = view.findViewById(R.id.tvSheetTitle);
        if (mode == Mode.HERO) {
            tvSheetTitle.setText("Edit User Type");
            groupAboutFields.setVisibility(View.GONE);
            groupUserType.setVisibility(View.VISIBLE);
        } else {
            tvSheetTitle.setText("Edit About");
            groupAboutFields.setVisibility(View.VISIBLE);
            groupUserType.setVisibility(View.GONE);
        }

        view.findViewById(R.id.btnSave).setOnClickListener(v -> saveBasicInfo());
        view.findViewById(R.id.optionStudent).setOnClickListener(v -> selectUserType("STUDENT"));
        view.findViewById(R.id.optionCR).setOnClickListener(v -> selectUserType("CR"));

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(ProfileApiService.class)
                .getMyProfile(token)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Profile p = response.body();
                            if (p.getBio()   != null) etBio.setText(p.getBio());
                            if (p.getAbout() != null) etAbout.setText(p.getAbout());
                            if (p.getUserType() != null) selectUserType(p.getUserType());
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        // sheet stays editable even if pre-fill fails
                    }
                });
    }

    private void selectUserType(String type) {
        selectedUserType = type;
        checkStudent.setText(type.equals("STUDENT") ? "●" : "○");
        checkCR.setText(type.equals("CR") ? "●" : "○");
        int cyan  = requireContext().getResources().getColor(R.color.color_cyan,  null);
        int muted = requireContext().getResources().getColor(R.color.color_muted, null);
        checkStudent.setTextColor(type.equals("STUDENT") ? cyan : muted);
        checkCR.setTextColor(type.equals("CR") ? cyan : muted);
    }

    private void saveBasicInfo() {
        String bio   = etBio.getText().toString().trim();
        String about = etAbout.getText().toString().trim();

        TextView btnSave = requireView().findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText("Saving…");

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        // Always sends all three fields (even the ones hidden in this mode)
        // so the hidden section's current value isn't overwritten with blanks.
        ProfileUpdateRequest body = new ProfileUpdateRequest(bio, about, selectedUserType);

        RetrofitClient.createService(ProfileApiService.class)
                .updateProfile(token, body)
                .enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                            if (onSavedListener != null) onSavedListener.onSaved();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Update failed. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        if (!isAdded()) return;
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
