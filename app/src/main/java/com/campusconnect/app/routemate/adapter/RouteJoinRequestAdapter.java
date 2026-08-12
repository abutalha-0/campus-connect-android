package com.campusconnect.app.routemate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.campusconnect.app.R;
import com.campusconnect.app.routemate.model.RouteJoinRequest;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class RouteJoinRequestAdapter extends RecyclerView.Adapter<RouteJoinRequestAdapter.RequestViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(RouteJoinRequest request);
        void onReject(RouteJoinRequest request);
    }

    private final List<RouteJoinRequest> requestList;
    private final boolean isOwnerView;
    private final OnRequestActionListener listener;

    public RouteJoinRequestAdapter(List<RouteJoinRequest> requestList, boolean isOwnerView, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.isOwnerView = isOwnerView;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_join_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RouteJoinRequest req = requestList.get(position);
        holder.bind(req, isOwnerView, listener);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRequesterInitials;
        private final TextView tvRequesterName;
        private final TextView tvRouteSummary;
        private final TextView tvRequestStatus;
        private final TextView tvRequestNote;
        private final TextView tvContactInfo;
        private final View layoutOwnerActions;
        private final MaterialButton btnAccept;
        private final MaterialButton btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequesterInitials = itemView.findViewById(R.id.tvRequesterInitials);
            tvRequesterName = itemView.findViewById(R.id.tvRequesterName);
            tvRouteSummary = itemView.findViewById(R.id.tvRouteSummary);
            tvRequestStatus = itemView.findViewById(R.id.tvRequestStatus);
            tvRequestNote = itemView.findViewById(R.id.tvRequestNote);
            tvContactInfo = itemView.findViewById(R.id.tvContactInfo);
            layoutOwnerActions = itemView.findViewById(R.id.layoutOwnerActions);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(RouteJoinRequest req, boolean isOwnerView, OnRequestActionListener listener) {
            String name = req.getRequesterFullName();
            if (name == null || name.isEmpty()) {
                name = req.getRequesterUsername() != null ? req.getRequesterUsername() : "Requester";
            }
            tvRequesterName.setText(name);
            tvRequesterInitials.setText(getInitials(name));

            String routeSummary = "Route: " + (req.getRouteHomeArea() != null ? req.getRouteHomeArea() : "")
                    + " ➔ " + (req.getRouteDestination() != null ? req.getRouteDestination() : "");
            tvRouteSummary.setText(routeSummary);

            tvRequestStatus.setText(req.getStatus() != null ? req.getStatus() : "PENDING");

            if (req.getNote() != null && !req.getNote().isEmpty()) {
                tvRequestNote.setText("Note: " + req.getNote());
                tvRequestNote.setVisibility(View.VISIBLE);
            } else {
                tvRequestNote.setVisibility(View.GONE);
            }

            if (req.getRequesterContactInfo() != null && !req.getRequesterContactInfo().isEmpty()) {
                tvContactInfo.setText("Contact: " + req.getRequesterContactInfo());
                tvContactInfo.setVisibility(View.VISIBLE);
            } else {
                tvContactInfo.setVisibility(View.GONE);
            }

            if (isOwnerView && "PENDING".equalsIgnoreCase(req.getStatus())) {
                layoutOwnerActions.setVisibility(View.VISIBLE);
                btnAccept.setOnClickListener(v -> {
                    if (listener != null) listener.onAccept(req);
                });
                btnReject.setOnClickListener(v -> {
                    if (listener != null) listener.onReject(req);
                });
            } else {
                layoutOwnerActions.setVisibility(View.GONE);
            }
        }

        private String getInitials(String name) {
            if (name == null || name.trim().isEmpty()) return "R";
            String[] parts = name.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length && sb.length() < 2; i++) {
                if (!parts[i].isEmpty()) {
                    sb.append(Character.toUpperCase(parts[i].charAt(0)));
                }
            }
            return sb.length() > 0 ? sb.toString() : "R";
        }
    }
}
