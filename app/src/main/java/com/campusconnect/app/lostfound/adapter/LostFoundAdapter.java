package com.campusconnect.app.lostfound.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.utils.TimeUtils;
import com.campusconnect.app.lostfound.model.LostFoundItem;
import java.util.List;

public class LostFoundAdapter extends RecyclerView.Adapter<LostFoundAdapter.ViewHolder> {

    private final List<LostFoundItem> items;
    private final OnItemClickListener listener;
    private final boolean showActions;
    private OnActionClickListener actionListener;

    public interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
    }

    public interface OnActionClickListener {
        void onEditClick(LostFoundItem item);
        void onMarkClosedClick(LostFoundItem item);
    }

    public LostFoundAdapter(List<LostFoundItem> items, OnItemClickListener listener) {
        this(items, listener, false);
    }

    public LostFoundAdapter(List<LostFoundItem> items, OnItemClickListener listener, boolean showActions) {
        this.items = items;
        this.listener = listener;
        this.showActions = showActions;
    }

    public void setOnActionClickListener(OnActionClickListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost_found, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundItem item = items.get(position);
        holder.bind(item, listener, actionListener, showActions);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvStatusBadge, tvMeta;
        LinearLayout actionRow;
        Button btnEdit, btnMarkClosed;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            actionRow = itemView.findViewById(R.id.actionRow);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnMarkClosed = itemView.findViewById(R.id.btnMarkClosed);
        }

        void bind(LostFoundItem item, OnItemClickListener listener,
                  OnActionClickListener actionListener, boolean showActions) {
            tvTitle.setText(item.getTitle());
            String relativeTime = TimeUtils.getRelativeTime(item.getCreatedAt());
            String loc = item.getLocation();
            if (loc != null && !loc.trim().isEmpty()) {
                tvMeta.setText(String.format("%s · %s", loc, relativeTime));
            } else if (item.getCategory() != null && !item.getCategory().trim().isEmpty()) {
                tvMeta.setText(String.format("%s · %s", item.getCategory(), relativeTime));
            } else {
                tvMeta.setText(relativeTime);
            }

            boolean isLost = LostFoundItem.TYPE_LOST.equalsIgnoreCase(item.getItemType());
            int accentColor = ContextCompat.getColor(itemView.getContext(), isLost ? R.color.amber : R.color.amber_gold);
            int dimColor = ContextCompat.getColor(itemView.getContext(), isLost ? R.color.amber_dim : R.color.amber_gold_dim);

            if (LostFoundItem.STATUS_CLAIMED.equalsIgnoreCase(item.getStatus())) {
                tvStatusBadge.setText("CLAIMED");
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.amber_gold));
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.amber_gold_dim)));
            } else if (LostFoundItem.STATUS_CLOSED.equalsIgnoreCase(item.getStatus())) {
                tvStatusBadge.setText("CLOSED");
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_faint));
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor("#1A2436")));
            } else {
                tvStatusBadge.setText(item.getItemType());
                tvStatusBadge.setTextColor(accentColor);
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(dimColor));
            }

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.bg_lf_input)
                        .centerCrop()
                        .into(ivThumbnail);
            } else {
                ivThumbnail.setImageResource(isLost ? R.drawable.ic_wallet : R.drawable.ic_tag);
                ivThumbnail.setImageTintList(ColorStateList.valueOf(accentColor));
                ivThumbnail.setBackgroundTintList(ColorStateList.valueOf(dimColor));
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));

            if (showActions) {
                actionRow.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onEditClick(item);
                });
                if (LostFoundItem.STATUS_CLOSED.equalsIgnoreCase(item.getStatus())) {
                    btnMarkClosed.setVisibility(View.GONE);
                } else {
                    btnMarkClosed.setVisibility(View.VISIBLE);
                    btnMarkClosed.setOnClickListener(v -> {
                        if (actionListener != null) actionListener.onMarkClosedClick(item);
                    });
                }
            } else {
                actionRow.setVisibility(View.GONE);
            }
        }
    }
}
