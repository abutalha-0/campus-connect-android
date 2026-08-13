package com.campusconnect.app.crew;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusconnect.app.R;
import com.campusconnect.app.crew.model.Post;

import java.util.List;

/** NEW (Crew feature): RecyclerView adapter for the post list, structured like UserAdapter.java. */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    private List<Post> posts;
    private final OnPostClickListener listener;

    public PostAdapter(List<Post> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        android.content.Context ctx = holder.itemView.getContext();

        holder.tvCategory.setText(post.getCategoryDetail() != null ? post.getCategoryDetail().getName() : "");
        holder.tvTitle.setText(post.getTitle());
        holder.tvDescription.setText(post.getDescription());
        holder.tvLocation.setText(post.getLocation() == null || post.getLocation().isEmpty() ? "" : "\uD83D\uDCCD " + post.getLocation());

        holder.tvStatusBadge.setText(post.getStatus());
        holder.tvStatusBadge.setTextColor(statusColor(ctx, post.getStatus()));

        holder.tvCapacity.setText(post.getMaxMembers() == null ? ""
                : post.getAcceptedMembersCount() + "/" + post.getMaxMembers() + " joined");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPostClick(post);
        });
    }

    @Override
    public int getItemCount() { return posts.size(); }

    public void setPosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    private int statusColor(android.content.Context ctx, String status) {
        if (status == null) return ctx.getResources().getColor(R.color.color_muted, null);
        switch (status) {
            case "OPEN": return ctx.getResources().getColor(R.color.color_green, null);
            case "FULL": return ctx.getResources().getColor(R.color.color_amber, null);
            case "CLOSED": case "CANCELLED": case "EXPIRED": return ctx.getResources().getColor(R.color.color_red, null);
            default: return ctx.getResources().getColor(R.color.color_muted, null);
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvTitle, tvDescription, tvLocation, tvCapacity, tvStatusBadge;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
