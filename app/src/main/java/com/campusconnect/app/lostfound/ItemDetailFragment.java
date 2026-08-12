package com.campusconnect.app.lostfound;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TimeUtils;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.lostfound.api.LostFoundApiService;
import com.campusconnect.app.lostfound.model.LostFoundItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailFragment extends Fragment {

    private int itemId;
    private TokenManager tokenManager;
    private LostFoundItem currentItem;

    public static ItemDetailFragment newInstance(int itemId) {
        ItemDetailFragment fragment = new ItemDetailFragment();
        Bundle args = new Bundle();
        args.putInt("item_id", itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemId = getArguments().getInt("item_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnRevealContact).setOnClickListener(v -> {
            if (currentItem != null) showContactDialog(currentItem);
        });

        loadItemDetail();
    }

    private void loadItemDetail() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(LostFoundApiService.class)
                .getItemDetail(token, itemId)
                .enqueue(new Callback<LostFoundItem>() {
                    @Override
                    public void onResponse(Call<LostFoundItem> call, Response<LostFoundItem> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentItem = response.body();
                            populateUI(currentItem);
                        } else {
                            Toast.makeText(getContext(), "Failed to load item details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LostFoundItem> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateUI(LostFoundItem item) {
        View view = getView();
        if (view == null) return;

        ImageView ivPhoto = view.findViewById(R.id.ivPhoto);
        TextView tvTypeBadge = view.findViewById(R.id.tvTypeBadge);
        TextView tvStatusBadge = view.findViewById(R.id.tvStatusBadgeDetail);
        TextView tvTitle = view.findViewById(R.id.tvTitleDetail);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvCategory = view.findViewById(R.id.tvCategoryDetail);
        TextView tvLocation = view.findViewById(R.id.tvLocationDetail);
        TextView tvDateTime = view.findViewById(R.id.tvDateTimeDetail);
        TextView tvPostedBy = view.findViewById(R.id.tvPostedBy);

        tvTitle.setText(item.getTitle());
        tvDescription.setText(item.getDescription());
        tvCategory.setText(item.getCategory());
        tvLocation.setText(item.getLocation());
        tvDateTime.setText(TimeUtils.formatDate(item.getDateSeen()));
        tvPostedBy.setText(item.getReportedBy());

        boolean isLost = LostFoundItem.TYPE_LOST.equalsIgnoreCase(item.getItemType());
        int accentColor = ContextCompat.getColor(requireContext(), isLost ? R.color.amber : R.color.amber_gold);
        int dimColor = ContextCompat.getColor(requireContext(), isLost ? R.color.amber_dim : R.color.amber_gold_dim);

        tvTypeBadge.setText(item.getItemType());
        tvTypeBadge.setTextColor(accentColor);
        tvTypeBadge.setBackgroundTintList(ColorStateList.valueOf(dimColor));

        tvStatusBadge.setText(item.getStatus());
        if (LostFoundItem.STATUS_CLOSED.equalsIgnoreCase(item.getStatus())) {
            tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1A2436")));
        } else if (LostFoundItem.STATUS_OPEN.equalsIgnoreCase(item.getStatus())) {
            tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple));
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple_dim)));
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(this).load(item.getImageUrl()).into(ivPhoto);
        }
    }

    private void showContactDialog(LostFoundItem item) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_contact_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvName = dialog.findViewById(R.id.tvContactName);
        TextView tvInitials = dialog.findViewById(R.id.tvContactInitials);
        TextView tvValue = dialog.findViewById(R.id.tvContactValue);

        tvName.setText(item.getReportedBy());
        tvInitials.setText(initialsOf(item.getReportedBy()));
        tvValue.setText(item.getContactInfo());

        dialog.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length && initials.length() < 2; i++) {
            if (!parts[i].isEmpty()) initials.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return initials.toString();
    }
}
