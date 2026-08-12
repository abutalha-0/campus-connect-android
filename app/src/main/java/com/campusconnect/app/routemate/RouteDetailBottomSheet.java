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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.routemate.adapter.RouteJoinRequestAdapter;
import com.campusconnect.app.routemate.api.RouteMateApiService;
import com.campusconnect.app.routemate.model.Route;
import com.campusconnect.app.routemate.model.RouteJoinRequest;
import com.campusconnect.app.routemate.model.RouteJoinRequestCreatePayload;
import com.campusconnect.app.routemate.model.RouteRespondPayload;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteDetailBottomSheet extends BottomSheetDialogFragment {

    public interface OnRouteUpdatedListener {
        void onRouteUpdated();
    }

    private OnRouteUpdatedListener listener;
    private Route route;
    private TokenManager tokenManager;
    private RouteMateApiService apiService;

    private List<RouteJoinRequest> joinRequests = new ArrayList<>();
    private RouteJoinRequestAdapter joinRequestAdapter;

    public static RouteDetailBottomSheet newInstance(Route route) {
        RouteDetailBottomSheet fragment = new RouteDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("route", route);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnRouteUpdatedListener(OnRouteUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            route = (Route) getArguments().getSerializable("route");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_route_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());
        apiService = RetrofitClient.createService(RouteMateApiService.class);

        TextView tvStatusBadge = view.findViewById(R.id.tvStatusBadge);
        TextView tvHomeArea = view.findViewById(R.id.tvHomeArea);
        TextView tvDestination = view.findViewById(R.id.tvDestination);
        TextView tvOwnerInitials = view.findViewById(R.id.tvOwnerInitials);
        TextView tvOwnerName = view.findViewById(R.id.tvOwnerName);
        TextView tvOwnerUsername = view.findViewById(R.id.tvOwnerUsername);
        TextView tvTransport = view.findViewById(R.id.tvTransport);
        TextView tvDays = view.findViewById(R.id.tvDays);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvGender = view.findViewById(R.id.tvGender);
        TextView tvNote = view.findViewById(R.id.tvNote);
        TextView tvContactInfo = view.findViewById(R.id.tvContactInfo);

        View layoutJoinRequestSection = view.findViewById(R.id.layoutJoinRequestSection);
        EditText etRequestContactInfo = view.findViewById(R.id.etRequestContactInfo);
        EditText etRequestNote = view.findViewById(R.id.etRequestNote);
        MaterialButton btnSubmitRequest = view.findViewById(R.id.btnSubmitRequest);

        View layoutOwnerRequestsSection = view.findViewById(R.id.layoutOwnerRequestsSection);
        RecyclerView rvJoinRequests = view.findViewById(R.id.rvJoinRequests);
        TextView tvNoRequests = view.findViewById(R.id.tvNoRequests);
        MaterialButton btnCloseRoute = view.findViewById(R.id.btnCloseRoute);
        MaterialButton btnDeleteRoute = view.findViewById(R.id.btnDeleteRoute);

        if (route != null) {
            tvStatusBadge.setText(route.getStatus());
            tvHomeArea.setText(route.getHomeArea());
            tvDestination.setText(route.getDestination());

            String fullName = route.getOwnerFullName() != null ? route.getOwnerFullName() : route.getOwnerUsername();
            tvOwnerName.setText(fullName != null ? fullName : "Route Owner");
            tvOwnerInitials.setText(getInitials(fullName));
            tvOwnerUsername.setText(route.getOwnerUsername() != null ? "@" + route.getOwnerUsername() : "");

            tvTransport.setText("Transport Mode: " + (route.getTransportMode() != null ? route.getTransportMode() : "Any"));
            tvDays.setText("Days Active: " + (route.getDaysActive() != null ? route.getDaysActive() : "Flexible"));

            String start = route.getDepartureTimeStart() != null ? route.getDepartureTimeStart() : "";
            String end = route.getDepartureTimeEnd() != null ? route.getDepartureTimeEnd() : "";
            tvTime.setText("Departure Window: " + start + (end.isEmpty() ? "" : " - " + end));

            tvGender.setText("Gender Preference: " + (route.getGenderPreference() != null ? route.getGenderPreference() : "ANY"));
            tvNote.setText("Notes: " + (route.getNote() != null && !route.getNote().isEmpty() ? route.getNote() : "None"));
            tvContactInfo.setText(route.getContactInfo() != null ? route.getContactInfo() : "[Locked until request accepted]");

            // Check if current user is owner or requester
            loadRouteDetail(view);
        }

        btnSubmitRequest.setOnClickListener(v -> {
            String contact = etRequestContactInfo.getText().toString().trim();
            String note = etRequestNote.getText().toString().trim();
            if (contact.isEmpty()) {
                Toast.makeText(getContext(), "Please provide your contact info for the route owner", Toast.LENGTH_SHORT).show();
                return;
            }
            sendJoinRequest(contact, note, btnSubmitRequest);
        });
    }

    private void loadRouteDetail(View view) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        apiService.getRouteDetail(token, route.getId()).enqueue(new Callback<Route>() {
            @Override
            public void onResponse(Call<Route> call, Response<Route> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    route = response.body();
                    updateUiState(view);
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {}
        });
    }

    private void updateUiState(View view) {
        TextView tvContactInfo = view.findViewById(R.id.tvContactInfo);
        if (route.getContactInfo() != null) {
            tvContactInfo.setText(route.getContactInfo());
        }

        View layoutJoinRequestSection = view.findViewById(R.id.layoutJoinRequestSection);
        View layoutOwnerRequestsSection = view.findViewById(R.id.layoutOwnerRequestsSection);
        MaterialButton btnSubmitRequest = view.findViewById(R.id.btnSubmitRequest);

        // Fetch join requests if owner, or show request status if non-owner
        fetchOwnerRequests(view);
    }

    private void fetchOwnerRequests(View view) {
        View layoutJoinRequestSection = view.findViewById(R.id.layoutJoinRequestSection);
        View layoutOwnerRequestsSection = view.findViewById(R.id.layoutOwnerRequestsSection);
        RecyclerView rvJoinRequests = view.findViewById(R.id.rvJoinRequests);
        TextView tvNoRequests = view.findViewById(R.id.tvNoRequests);
        MaterialButton btnCloseRoute = view.findViewById(R.id.btnCloseRoute);
        MaterialButton btnDeleteRoute = view.findViewById(R.id.btnDeleteRoute);
        MaterialButton btnSubmitRequest = view.findViewById(R.id.btnSubmitRequest);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        apiService.getRouteJoinRequests(token, route.getId()).enqueue(new Callback<List<RouteJoinRequest>>() {
            @Override
            public void onResponse(Call<List<RouteJoinRequest>> call, Response<List<RouteJoinRequest>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    // Current user is the OWNER
                    layoutOwnerRequestsSection.setVisibility(View.VISIBLE);
                    layoutJoinRequestSection.setVisibility(View.GONE);

                    joinRequests = response.body();
                    if (joinRequests.isEmpty()) {
                        tvNoRequests.setVisibility(View.VISIBLE);
                        rvJoinRequests.setVisibility(View.GONE);
                    } else {
                        tvNoRequests.setVisibility(View.GONE);
                        rvJoinRequests.setVisibility(View.VISIBLE);

                        rvJoinRequests.setLayoutManager(new LinearLayoutManager(getContext()));
                        joinRequestAdapter = new RouteJoinRequestAdapter(joinRequests, true, new RouteJoinRequestAdapter.OnRequestActionListener() {
                            @Override
                            public void onAccept(RouteJoinRequest req) {
                                respondToRequest(req, "ACCEPT");
                            }

                            @Override
                            public void onReject(RouteJoinRequest req) {
                                respondToRequest(req, "REJECT");
                            }
                        });
                        rvJoinRequests.setAdapter(joinRequestAdapter);
                    }

                    btnCloseRoute.setOnClickListener(v -> closeRoute());
                    btnDeleteRoute.setOnClickListener(v -> deleteRoute());
                } else {
                    // Current user is NOT the owner
                    layoutOwnerRequestsSection.setVisibility(View.GONE);
                    layoutJoinRequestSection.setVisibility(View.VISIBLE);

                    if (route.getUserRequestStatus() != null) {
                        btnSubmitRequest.setText("Status: " + route.getUserRequestStatus());
                        if ("PENDING".equalsIgnoreCase(route.getUserRequestStatus()) || "ACCEPTED".equalsIgnoreCase(route.getUserRequestStatus())) {
                            btnSubmitRequest.setEnabled(false);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RouteJoinRequest>> call, Throwable t) {
                if (!isAdded()) return;
                layoutOwnerRequestsSection.setVisibility(View.GONE);
                layoutJoinRequestSection.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sendJoinRequest(String contact, String note, MaterialButton btnSubmit) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        btnSubmit.setEnabled(false);

        RouteJoinRequestCreatePayload payload = new RouteJoinRequestCreatePayload(note, contact);
        apiService.createJoinRequest(token, route.getId(), payload).enqueue(new Callback<RouteJoinRequest>() {
            @Override
            public void onResponse(Call<RouteJoinRequest> call, Response<RouteJoinRequest> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Join request sent to route owner!", Toast.LENGTH_SHORT).show();
                        btnSubmit.setText("Status: PENDING");
                        if (listener != null) listener.onRouteUpdated();
                        dismiss();
                    } else {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(getContext(), "Could not send request. You may already have a request.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RouteJoinRequest> call, Throwable t) {
                if (isAdded()) {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void respondToRequest(RouteJoinRequest req, String action) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RouteRespondPayload payload = new RouteRespondPayload(action);
        apiService.respondJoinRequest(token, req.getId(), payload).enqueue(new Callback<RouteJoinRequest>() {
            @Override
            public void onResponse(Call<RouteJoinRequest> call, Response<RouteJoinRequest> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Request " + action.toLowerCase() + "ed!", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onRouteUpdated();
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<RouteJoinRequest> call, Throwable t) {}
        });
    }

    private void closeRoute() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        Route updated = new Route();
        updated.setStatus("CLOSED");
        apiService.updateRoute(token, route.getId(), updated).enqueue(new Callback<Route>() {
            @Override
            public void onResponse(Call<Route> call, Response<Route> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Route marked as closed", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onRouteUpdated();
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {}
        });
    }

    private void deleteRoute() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        apiService.deleteRoute(token, route.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Route deleted", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onRouteUpdated();
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "RM";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return sb.length() > 0 ? sb.toString() : "RM";
    }
}
