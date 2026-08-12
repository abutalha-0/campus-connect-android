package com.campusconnect.app.lostfound;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.lostfound.adapter.LostFoundAdapter;
import com.campusconnect.app.lostfound.api.LostFoundApiService;
import com.campusconnect.app.lostfound.model.LostFoundItem;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LostFoundAdapter adapter;
    private List<LostFoundItem> allMyItems = new ArrayList<>();
    private List<LostFoundItem> filteredItems = new ArrayList<>();

    private boolean showingClosed = false;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        view.findViewById(R.id.btnBackMyPosts).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        recyclerView = view.findViewById(R.id.recyclerViewMyPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new LostFoundAdapter(filteredItems, item -> {
            Fragment detailFragment = ItemDetailFragment.newInstance(item.getId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }, true);

        adapter.setOnActionClickListener(new LostFoundAdapter.OnActionClickListener() {
            @Override
            public void onEditClick(LostFoundItem item) {
                Fragment postFragment = PostItemFragment.newInstance(item.getId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, postFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onMarkClosedClick(LostFoundItem item) {
                markAsClosed(item);
            }
        });

        recyclerView.setAdapter(adapter);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupMyPosts);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showingClosed = (checkedId == R.id.btnClosed);
                filterMyItems();
            }
        });

        loadMyPosts();
    }

    private void loadMyPosts() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(LostFoundApiService.class)
                .getMyPosts(token)
                .enqueue(new Callback<List<LostFoundItem>>() {
                    @Override
                    public void onResponse(Call<List<LostFoundItem>> call, Response<List<LostFoundItem>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            allMyItems.clear();
                            allMyItems.addAll(response.body());
                            filterMyItems();
                        } else {
                            Toast.makeText(getContext(), "Failed to load your posts", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LostFoundItem>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterMyItems() {
        filteredItems.clear();
        for (LostFoundItem item : allMyItems) {
            boolean isClosed = LostFoundItem.STATUS_CLOSED.equalsIgnoreCase(item.getStatus());
            if (showingClosed == isClosed) {
                filteredItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();

        View emptyState = getView().findViewById(R.id.tvEmptyMyPosts);
        TextView tvSectionLabel = getView().findViewById(R.id.tvSectionLabel);

        if (filteredItems.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvSectionLabel.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            tvSectionLabel.setVisibility(View.VISIBLE);
            tvSectionLabel.setText(showingClosed ? R.string.label_closed_caps : R.string.label_active_caps);
        }
    }

    private void markAsClosed(LostFoundItem item) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        LostFoundItem patchItem = new LostFoundItem();
        patchItem.setStatus(LostFoundItem.STATUS_CLOSED);

        RetrofitClient.createService(LostFoundApiService.class)
                .updateItem(token, item.getId(), patchItem)
                .enqueue(new Callback<LostFoundItem>() {
                    @Override
                    public void onResponse(Call<LostFoundItem> call, Response<LostFoundItem> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Post marked as closed", Toast.LENGTH_SHORT).show();
                            loadMyPosts();
                        } else {
                            Toast.makeText(getContext(), "Failed to update post", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LostFoundItem> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
