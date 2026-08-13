package com.campusconnect.app.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.faculty.util.Designations;
import com.campusconnect.app.profile.edit.ProfileChipFactory;
import com.campusconnect.app.user.User;
import com.google.android.material.chip.ChipGroup;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    // Cards show up to this many skill chips before collapsing the rest
    // into a "+N more" chip.
    private static final int MAX_SKILL_CHIPS = 3;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> users;
    private OnUserClickListener listener;

    public UserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        android.content.Context ctx = holder.itemView.getContext();

        String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                ? user.getFullName()
                : user.getUsername();

        holder.tvFullName.setText(displayName);
        holder.tvUsername.setText("@" + user.getUsername());

        boolean isFaculty = Constants.ROLE_FACULTY.equals(user.getRole());
        boolean isCr = "CR".equals(user.getUserType());
        holder.tvCrBadge.setVisibility(isCr ? View.VISIBLE : View.GONE);

        String subtitle;
        if (isFaculty && user.getDepartment() != null && !user.getDepartment().isEmpty()) {
            String designationLabel = Designations.labelFor(ctx, user.getDesignation());
            subtitle = ctx.getString(R.string.discover_faculty_line, designationLabel, user.getDepartment());
        } else {
            subtitle = user.getBio();
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            holder.tvBio.setText(subtitle);
            holder.tvBio.setVisibility(View.VISIBLE);
        } else {
            holder.tvBio.setVisibility(View.GONE);
        }

        String initial = displayName.substring(0, 1).toUpperCase();
        holder.tvInitial.setText(initial);

        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
            holder.ivAvatar.setVisibility(View.VISIBLE);
            Glide.with(ctx).load(user.getProfilePhoto()).centerCrop().into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setVisibility(View.GONE);
        }

        User.Education education = user.getCurrentEducation();
        if (education != null && education.getDegree() != null && education.getInstitutionName() != null) {
            holder.tvEducation.setText(ctx.getString(R.string.discover_education_line,
                    education.getDegree(), education.getInstitutionName()));
            holder.tvEducation.setVisibility(View.VISIBLE);
        } else {
            holder.tvEducation.setVisibility(View.GONE);
        }

        holder.skillsChipGroup.removeAllViews();
        List<String> skills = user.getSkills();
        if (skills != null && !skills.isEmpty()) {
            int shown = Math.min(skills.size(), MAX_SKILL_CHIPS);
            for (int i = 0; i < shown; i++) {
                holder.skillsChipGroup.addView(ProfileChipFactory.create(ctx, skills.get(i)));
            }
            if (skills.size() > shown) {
                holder.skillsChipGroup.addView(ProfileChipFactory.create(
                        ctx, ctx.getString(R.string.discover_skills_more, skills.size() - shown)));
            }
        }

        Integer projectCount = user.getProjectCount();
        if (projectCount != null && projectCount > 0) {
            holder.tvProjectCount.setText(ctx.getString(R.string.discover_project_count, projectCount));
            holder.tvProjectCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvProjectCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /** Appends the next page's users (e.g. from infinite scroll) without
     *  rebuilding the whole list. */
    public void addUsers(List<User> more) {
        int start = users.size();
        users.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvUsername, tvBio, tvInitial, tvCrBadge, tvEducation, tvProjectCount;
        CircleImageView ivAvatar;
        ChipGroup skillsChipGroup;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvBio = itemView.findViewById(R.id.tvBio);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvCrBadge = itemView.findViewById(R.id.tvCrBadge);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvEducation = itemView.findViewById(R.id.tvEducation);
            tvProjectCount = itemView.findViewById(R.id.tvProjectCount);
            skillsChipGroup = itemView.findViewById(R.id.skillsChipGroup);
        }
    }
}
