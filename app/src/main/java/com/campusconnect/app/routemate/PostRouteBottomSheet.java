package com.campusconnect.app.routemate;

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
import com.campusconnect.app.routemate.api.RouteMateApiService;
import com.campusconnect.app.routemate.model.Route;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
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
        EditText etHomeArea = view.findViewById(R.id.etHomeArea);
        EditText etDestination = view.findViewById(R.id.etDestination);
        EditText etDepartureStart = view.findViewById(R.id.etDepartureStart);
        EditText etDepartureEnd = view.findViewById(R.id.etDepartureEnd);
        EditText etTransportMode = view.findViewById(R.id.etTransportMode);
        EditText etDaysActive = view.findViewById(R.id.etDaysActive);
        MaterialButtonToggleGroup toggleGender = view.findViewById(R.id.toggleGenderPref);
        EditText etContactInfo = view.findViewById(R.id.etContactInfo);
        EditText etNote = view.findViewById(R.id.etNote);
        MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);

        if (routeToEdit != null) {
            tvTitle.setText(R.string.edit_route_title);
            btnSubmit.setText("Save Changes");
            etHomeArea.setText(routeToEdit.getHomeArea());
            etDestination.setText(routeToEdit.getDestination());
            etDepartureStart.setText(routeToEdit.getDepartureTimeStart());
            etDepartureEnd.setText(routeToEdit.getDepartureTimeEnd());
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

        btnSubmit.setOnClickListener(v -> {
            String home = etHomeArea.getText().toString().trim();
            String dest = etDestination.getText().toString().trim();
            if (home.isEmpty() || dest.isEmpty()) {
                Toast.makeText(getContext(), "Home Area and Destination are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Route route = routeToEdit != null ? routeToEdit : new Route();
            route.setHomeArea(home);
            route.setDestination(dest);
            route.setDepartureTimeStart(etDepartureStart.getText().toString().trim());
            route.setDepartureTimeEnd(etDepartureEnd.getText().toString().trim());
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
                    }
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
