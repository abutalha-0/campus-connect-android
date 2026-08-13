package com.campusconnect.app.routemate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.campusconnect.app.R;
import com.campusconnect.app.routemate.model.Route;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(Route route);
    }

    private final List<Route> routeList;
    private final OnRouteClickListener listener;

    public RouteAdapter(List<Route> routeList, OnRouteClickListener listener) {
        this.routeList = routeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routeList.get(position);
        holder.bind(route, listener);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOwnerInitials;
        private final TextView tvOwnerName;
        private final TextView tvOwnerUsername;
        private final TextView tvBadgeStatus;
        private final TextView tvHomeArea;
        private final TextView tvDestination;
        private final TextView tvTransportMode;
        private final TextView tvDaysActive;
        private final TextView tvTimeWindow;
        private final TextView tvGenderPref;
        private final TextView tvNotePreview;
        private final MaterialButton btnAction;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOwnerInitials = itemView.findViewById(R.id.tvOwnerInitials);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
            tvOwnerUsername = itemView.findViewById(R.id.tvOwnerUsername);
            tvBadgeStatus = itemView.findViewById(R.id.tvBadgeStatus);
            tvHomeArea = itemView.findViewById(R.id.tvHomeArea);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvTransportMode = itemView.findViewById(R.id.tvTransportMode);
            tvDaysActive = itemView.findViewById(R.id.tvDaysActive);
            tvTimeWindow = itemView.findViewById(R.id.tvTimeWindow);
            tvGenderPref = itemView.findViewById(R.id.tvGenderPref);
            tvNotePreview = itemView.findViewById(R.id.tvNotePreview);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        public void bind(Route route, OnRouteClickListener listener) {
            String fullName = route.getOwnerFullName();
            if (fullName == null || fullName.isEmpty()) {
                fullName = route.getOwnerUsername() != null ? route.getOwnerUsername() : "Route Owner";
            }
            tvOwnerName.setText(fullName);

            String initials = getInitials(fullName);
            tvOwnerInitials.setText(initials);

            String username = route.getOwnerUsername() != null ? "@" + route.getOwnerUsername() : "";
            tvOwnerUsername.setText(username);

            tvHomeArea.setText(route.getHomeArea());
            tvDestination.setText(route.getDestination());

            if (route.getTransportMode() != null && !route.getTransportMode().isEmpty()) {
                tvTransportMode.setText(route.getTransportMode());
                tvTransportMode.setVisibility(View.VISIBLE);
            } else {
                tvTransportMode.setVisibility(View.GONE);
            }

            if (route.getDaysActive() != null && !route.getDaysActive().isEmpty()) {
                tvDaysActive.setText(route.getDaysActive());
                tvDaysActive.setVisibility(View.VISIBLE);
            } else {
                tvDaysActive.setVisibility(View.GONE);
            }

            String start = route.getDepartureTimeStart() != null ? route.getDepartureTimeStart() : "";
            String end = route.getDepartureTimeEnd() != null ? route.getDepartureTimeEnd() : "";
            if (!start.isEmpty() || !end.isEmpty()) {
                tvTimeWindow.setText(start + " - " + end);
                tvTimeWindow.setVisibility(View.VISIBLE);
            } else {
                tvTimeWindow.setVisibility(View.GONE);
            }

            if (route.getGenderPreference() != null) {
                switch (route.getGenderPreference()) {
                    case "MALE_ONLY":
                        tvGenderPref.setText("Male Only");
                        break;
                    case "FEMALE_ONLY":
                        tvGenderPref.setText("Female Only");
                        break;
                    default:
                        tvGenderPref.setText("Any Gender");
                        break;
                }
            }

            if (route.getNote() != null && !route.getNote().isEmpty()) {
                tvNotePreview.setText(route.getNote());
                tvNotePreview.setVisibility(View.VISIBLE);
            } else {
                tvNotePreview.setVisibility(View.GONE);
            }

            // Status & Match badge
            if (route.getUserRequestStatus() != null) {
                tvBadgeStatus.setText(route.getUserRequestStatus());
            } else if (route.getAcceptedMembersCount() > 0) {
                tvBadgeStatus.setText(route.getAcceptedMembersCount() + " matched");
            } else {
                tvBadgeStatus.setText(route.getStatus() != null ? route.getStatus() : "ACTIVE");
            }

            View.OnClickListener clickListener = v -> listener.onRouteClick(route);
            itemView.setOnClickListener(clickListener);
            btnAction.setOnClickListener(clickListener);
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
}
