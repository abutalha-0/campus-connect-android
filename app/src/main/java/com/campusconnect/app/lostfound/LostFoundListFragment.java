package com.campusconnect.app.lostfound;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.home.HomeActivity;
import com.campusconnect.app.lostfound.adapter.LostFoundAdapter;
import com.campusconnect.app.lostfound.api.LostFoundApiService;
import com.campusconnect.app.lostfound.model.LostFoundItem;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LostFoundListFragment extends Fragment {

    private RecyclerView recyclerView;
    private LostFoundAdapter adapter;
    private List<LostFoundItem> items = new ArrayList<>();
    
    private String currentType = null;
    private String currentStatus = null;
    private String currentCategory = null;
    private String currentLocation = null;
    private String currentDate = null;
    private String currentSearch = null;

    private View filterBadge;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lost_found_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        filterBadge = view.findViewById(R.id.filterBadge);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LostFoundAdapter(items, item -> {
            Fragment detailFragment = ItemDetailFragment.newInstance(item.getId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnMenu).setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).openDrawer();
            }
        });

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupType);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnLost) currentType = "LOST";
                else if (checkedId == R.id.btnFound) currentType = "FOUND";
                else currentType = null;
                loadItems();
            }
        });

        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentSearch = s.toString();
                loadItems();
            }
        });

        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            FilterBottomSheet filterSheet = FilterBottomSheet.newInstance(currentStatus, currentCategory, currentLocation, currentDate);
            filterSheet.setFilterListener((status, category, location, date) -> {
                currentStatus = status;
                currentCategory = category;
                currentLocation = location;
                currentDate = date;
                updateFilterUI();
                loadItems();
            });
            filterSheet.show(getChildFragmentManager(), "FilterBottomSheet");
        });

        view.findViewById(R.id.fabAdd).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new PostItemFragment())
                    .addToBackStack(null)
                    .commit();
        });

        loadItems();
    }

    private void updateFilterUI() {
        boolean hasFilters = currentStatus != null || (currentCategory != null && !currentCategory.isEmpty())
                || (currentLocation != null && !currentLocation.isEmpty())
                || (currentDate != null && !currentDate.isEmpty());
        filterBadge.setVisibility(hasFilters ? View.VISIBLE : View.GONE);
    }

    private void loadItems() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(LostFoundApiService.class)
                .getItems(token, currentType, currentStatus, currentCategory, currentLocation, currentDate, currentSearch)
                .enqueue(new Callback<List<LostFoundItem>>() {
                    @Override
                    public void onResponse(Call<List<LostFoundItem>> call, Response<List<LostFoundItem>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            items.clear();
                            items.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            
                            View emptyState = getView().findViewById(R.id.tvEmptyState);
                            if (items.isEmpty()) {
                                emptyState.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                emptyState.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LostFoundItem>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
