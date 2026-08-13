package com.campusconnect.app.routemate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.campusconnect.app.routemate.api.RouteMateApiService;
import com.campusconnect.app.routemate.model.Route;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRouteBottomSheet extends BottomSheetDialogFragment {

    public interface OnRouteCreatedListener {
        void onRouteCreated();
    }

    private OnRouteCreatedListener listener;
    private Route routeToEdit;
    private TokenManager tokenManager;
    private int selectedHour = 8;
    private int selectedMinute = 0;
    private String departureTimeApi = "";

    private EditText etHomeArea;
    private EditText etDestination;
    private EditText etDepartureTime;
    private EditText etTransportMode;
    private EditText etDaysActive;
    private MaterialButtonToggleGroup toggleGender;
    private EditText etContactInfo;
    private EditText etNote;
    private MaterialButton btnSubmit;

    private TextView tvHomeAreaError;
    private TextView tvDestinationError;
    private TextView tvDepartureTimeError;
    private TextView tvContactInfoError;

    public static PostRouteBottomSheet newInstance(@Nullable Route route) {
        PostRouteBottomSheet fragment = new PostRouteBottomSheet();
        if (route != null) {
            Bundle args = new Bundle();
            args.putSerializable("route", route);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public void setOnRouteCreatedListener(OnRouteCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeToEdit = (Route) getArguments().getSerializable("route");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_post_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        etHomeArea = view.findViewById(R.id.etHomeArea);
        etDestination = view.findViewById(R.id.etDestination);
        etDepartureTime = view.findViewById(R.id.etDepartureTime);
        etTransportMode = view.findViewById(R.id.etTransportMode);
        etDaysActive = view.findViewById(R.id.etDaysActive);
        toggleGender = view.findViewById(R.id.toggleGenderPref);
        etContactInfo = view.findViewById(R.id.etContactInfo);
        etNote = view.findViewById(R.id.etNote);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        tvHomeAreaError = view.findViewById(R.id.tvHomeAreaError);
        tvDestinationError = view.findViewById(R.id.tvDestinationError);
        tvDepartureTimeError = view.findViewById(R.id.tvDepartureTimeError);
        tvContactInfoError = view.findViewById(R.id.tvContactInfoError);

        etDepartureTime.setOnClickListener(v -> showTimePicker(etDepartureTime));

        if (routeToEdit != null) {
            tvTitle.setText(R.string.edit_route_title);
            btnSubmit.setText("Save Changes");
            etHomeArea.setText(routeToEdit.getHomeArea());
            etDestination.setText(routeToEdit.getDestination());
            
            if (routeToEdit.getDepartureTimeStart() != null && !routeToEdit.getDepartureTimeStart().isEmpty()) {
                departureTimeApi = routeToEdit.getDepartureTimeStart();
                etDepartureTime.setText(formatDisplayTime(departureTimeApi));
                parseApiTime(departureTimeApi);
            }

            etTransportMode.setText(routeToEdit.getTransportMode());
            etDaysActive.setText(routeToEdit.getDaysActive());
            etContactInfo.setText(routeToEdit.getContactInfo());
            etNote.setText(routeToEdit.getNote());

            if ("MALE_ONLY".equalsIgnoreCase(routeToEdit.getGenderPreference())) {
                toggleGender.check(R.id.btnGenderMale);
            } else if ("FEMALE_ONLY".equalsIgnoreCase(routeToEdit.getGenderPreference())) {
                toggleGender.check(R.id.btnGenderFemale);
            } else {
                toggleGender.check(R.id.btnGenderAny);
            }
        }

        setupValidationListeners();
        validateForm(false);

        btnSubmit.setOnClickListener(v -> {
            if (!validateForm(true)) {
                return;
            }

            String home = etHomeArea.getText().toString().trim();
            String dest = etDestination.getText().toString().trim();

            Route route = routeToEdit != null ? routeToEdit : new Route();
            route.setHomeArea(home);
            route.setDestination(dest);
            route.setDepartureTimeStart(departureTimeApi);
            route.setDepartureTimeEnd(departureTimeApi);
            route.setTransportMode(etTransportMode.getText().toString().trim());
            route.setDaysActive(etDaysActive.getText().toString().trim());
            route.setContactInfo(etContactInfo.getText().toString().trim());
            route.setNote(etNote.getText().toString().trim());

            int checkedGender = toggleGender.getCheckedButtonId();
            if (checkedGender == R.id.btnGenderMale) route.setGenderPreference("MALE_ONLY");
            else if (checkedGender == R.id.btnGenderFemale) route.setGenderPreference("FEMALE_ONLY");
            else route.setGenderPreference("ANY");

            btnSubmit.setEnabled(false);
            submitRoute(route);
        });
    }

    private void setupValidationListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm(false);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etHomeArea.addTextChangedListener(watcher);
        etDestination.addTextChangedListener(watcher);
        etDepartureTime.addTextChangedListener(watcher);
        etContactInfo.addTextChangedListener(watcher);
    }

    private boolean validateForm(boolean showFeedback) {
        boolean isValid = true;
        String firstErrorMessage = null;
        View firstInvalidView = null;

        // 1. Home Area
        String home = etHomeArea.getText().toString().trim();
        if (home.isEmpty()) {
            isValid = false;
            if (firstErrorMessage == null) {
                firstErrorMessage = "Home Area is required";
                firstInvalidView = etHomeArea;
            }
            if (showFeedback) {
                showFieldError(etHomeArea, tvHomeAreaError, "Home Area is required");
            }
        } else {
            clearFieldError(etHomeArea, tvHomeAreaError);
        }

        // 2. Destination
        String dest = etDestination.getText().toString().trim();
        if (dest.isEmpty()) {
            isValid = false;
            if (firstErrorMessage == null) {
                firstErrorMessage = "Destination is required";
                firstInvalidView = etDestination;
            }
            if (showFeedback) {
                showFieldError(etDestination, tvDestinationError, "Destination is required");
            }
        } else {
            clearFieldError(etDestination, tvDestinationError);
        }

        // 3. Departure Time
        String depTime = etDepartureTime.getText().toString().trim();
        if (depTime.isEmpty() || departureTimeApi.isEmpty()) {
            isValid = false;
            if (firstErrorMessage == null) {
                firstErrorMessage = "Departure time is required";
                firstInvalidView = etDepartureTime;
            }
            if (showFeedback) {
                showFieldError(etDepartureTime, tvDepartureTimeError, "Departure time is required");
            }
        } else {
            clearFieldError(etDepartureTime, tvDepartureTimeError);
        }

        // 4. Contact Info (Phone Number)
        String contact = etContactInfo.getText().toString().trim();
        if (!isPhoneValid(contact)) {
            isValid = false;
            String msg = "Enter a valid phone number";
            if (firstErrorMessage == null) {
                firstErrorMessage = msg;
                firstInvalidView = etContactInfo;
            }
            if (showFeedback) {
                showFieldError(etContactInfo, tvContactInfoError, msg);
            }
        } else {
            clearFieldError(etContactInfo, tvContactInfoError);
        }

        // Update button visual state
        btnSubmit.setAlpha(isValid ? 1.0f : 0.5f);

        if (showFeedback && !isValid && firstErrorMessage != null) {
            if (getView() != null) {
                Snackbar.make(getView(), firstErrorMessage, Snackbar.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), firstErrorMessage, Toast.LENGTH_SHORT).show();
            }
            if (firstInvalidView != null) {
                firstInvalidView.requestFocus();
            }
        }

        return isValid;
    }

    private boolean isPhoneValid(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String normalized = phone.replaceAll("[\\s-]", "");
        return normalized.matches("^(?:\\+8801|01)[3-9]\\d{8}$");
    }

    private void showFieldError(EditText editText, TextView errorView, String errorMessage) {
        editText.setBackgroundResource(R.drawable.bg_rm_input_error);
        errorView.setText(errorMessage);
        errorView.setVisibility(View.VISIBLE);
    }

    private void clearFieldError(EditText editText, TextView errorView) {
        editText.setBackgroundResource(R.drawable.bg_rm_input);
        errorView.setVisibility(View.GONE);
    }

    private void showTimePicker(EditText etDepartureTime) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(selectedHour)
                .setMinute(selectedMinute)
                .setTitleText("Select Departure Time")
                .build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();

            departureTimeApi = String.format(Locale.US, "%02d:%02d:00", selectedHour, selectedMinute);
            etDepartureTime.setText(formatDisplayTime(departureTimeApi));
            validateForm(false);
        });

        picker.show(getParentFragmentManager(), "DEPARTURE_TIME_PICKER");
    }

    private String formatDisplayTime(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "";
        try {
            String[] parts = rawTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int min = Integer.parseInt(parts[1]);

            int displayHour = hour % 12;
            if (displayHour == 0) displayHour = 12;
            String amPm = hour >= 12 ? "PM" : "AM";
            return String.format(Locale.US, "%02d:%02d %s", displayHour, min, amPm);
        } catch (Exception e) {
            return rawTime;
        }
    }

    private void parseApiTime(String rawTime) {
        try {
            String[] parts = rawTime.split(":");
            selectedHour = Integer.parseInt(parts[0]);
            selectedMinute = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}
    }

    private void submitRoute(Route route) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RouteMateApiService service = RetrofitClient.createService(RouteMateApiService.class);

        Call<Route> call;
        if (routeToEdit != null) {
            call = service.updateRoute(token, routeToEdit.getId(), route);
        } else {
            call = service.createRoute(token, route);
        }

        call.enqueue(new Callback<Route>() {
            @Override
            public void onResponse(Call<Route> call, Response<Route> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), routeToEdit != null ? "Route updated!" : "Route published!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onRouteCreated();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to save route. Check input fields.", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                }
            }
        });
    }
}
