package com.campusconnect.app.routemate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.campusconnect.app.routemate.adapter.RouteAdapter;
import com.campusconnect.app.routemate.api.RouteMateApiService;
import com.campusconnect.app.routemate.model.MyRoutesResponse;
import com.campusconnect.app.routemate.model.Route;
import com.campusconnect.app.routemate.model.RouteHistoryResponse;
import com.campusconnect.app.routemate.model.RouteJoinRequest;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteMateListFragment extends Fragment {

    private enum Tab { EXPLORE, MY_ROUTES, HISTORY }
    private Tab currentTab = Tab.EXPLORE;

    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    private List<Route> routesList = new ArrayList<>();

    private String filterHomeArea = null;
    private String filterDestination = null;
    private String filterGenderPreference = null;
    private String currentSearch = null;

    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private View filterBadge;
    private ExtendedFloatingActionButton fabPostRoute;

    private TokenManager tokenManager;
    private RouteMateApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_mate_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        apiService = RetrofitClient.createService(RouteMateApiService.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle);
        filterBadge = view.findViewById(R.id.filterBadge);
        fabPostRoute = view.findViewById(R.id.fabPostRoute);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RouteAdapter(routesList, route -> {
            RouteDetailBottomSheet detailSheet = RouteDetailBottomSheet.newInstance(route);
            detailSheet.setOnRouteUpdatedListener(this::loadCurrentTabData);
            detailSheet.show(getParentFragmentManager(), "RouteDetail");
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnMenu).setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).openDrawer();
            }
        });

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupTab);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnMyRoutes) {
                    currentTab = Tab.MY_ROUTES;
                    view.findViewById(R.id.layoutSearchRow).setVisibility(View.GONE);
                } else if (checkedId == R.id.btnHistory) {
                    currentTab = Tab.HISTORY;
                    view.findViewById(R.id.layoutSearchRow).setVisibility(View.GONE);
                } else {
                    currentTab = Tab.EXPLORE;
                    view.findViewById(R.id.layoutSearchRow).setVisibility(View.VISIBLE);
                }
                loadCurrentTabData();
            }
        });

        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentSearch = s.toString().trim();
                if (currentSearch.isEmpty()) currentSearch = null;
                if (currentTab == Tab.EXPLORE) loadCurrentTabData();
            }
        });

        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            RouteFilterBottomSheet filterSheet = RouteFilterBottomSheet.newInstance(filterHomeArea, filterDestination, filterGenderPreference);
            filterSheet.setFilterListener((homeArea, destination, genderPreference) -> {
                filterHomeArea = homeArea;
                filterDestination = destination;
                filterGenderPreference = genderPreference;

                boolean hasFilter = (filterHomeArea != null || filterDestination != null || filterGenderPreference != null);
                filterBadge.setVisibility(hasFilter ? View.VISIBLE : View.GONE);

                loadCurrentTabData();
            });
            filterSheet.show(getParentFragmentManager(), "RouteFilter");
        });

        fabPostRoute.setOnClickListener(v -> {
            PostRouteBottomSheet postSheet = PostRouteBottomSheet.newInstance(null);
            postSheet.setOnRouteCreatedListener(this::loadCurrentTabData);
            postSheet.show(getParentFragmentManager(), "PostRoute");
        });

        loadCurrentTabData();
    }

    private void loadCurrentTabData() {
        if (currentTab == Tab.EXPLORE) {
            loadExploreRoutes();
        } else if (currentTab == Tab.MY_ROUTES) {
            loadMyRoutes();
        } else if (currentTab == Tab.HISTORY) {
            loadRouteHistory();
        }
    }

    private void loadExploreRoutes() {
        showLoading(true);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        apiService.getRoutes(token, filterHomeArea, filterDestination, filterGenderPreference, null, "ACTIVE", currentSearch)
                .enqueue(new Callback<List<Route>>() {
                    @Override
                    public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                        if (!isAdded()) return;
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            routesList.clear();
                            routesList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            if (routesList.isEmpty()) {
                                tvEmptyTitle.setText(R.string.empty_routes);
                                tvEmptySubtitle.setText("No active routes matching your criteria. Be the first to post!");
                                layoutEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load routes", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Route>> call, Throwable t) {
                        if (!isAdded()) return;
                        showLoading(false);
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyRoutes() {
        showLoading(true);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        apiService.getMyRoutes(token).enqueue(new Callback<MyRoutesResponse>() {
            @Override
            public void onResponse(Call<MyRoutesResponse> call, Response<MyRoutesResponse> response) {
                if (!isAdded()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    routesList.clear();
                    MyRoutesResponse res = response.body();

                    if (res.getPostedRoutes() != null) {
                        routesList.addAll(res.getPostedRoutes());
                    }

                    // Convert joined route requests into displayable Route items
                    if (res.getJoinedRoutes() != null) {
                        for (RouteJoinRequest req : res.getJoinedRoutes()) {
                            Route r = new Route();
                            r.setId(req.getRoute());
                            r.setHomeArea(req.getRouteHomeArea());
                            r.setDestination(req.getRouteDestination());
                            r.setOwnerUsername(req.getRouteOwnerUsername());
                            r.setUserRequestStatus(req.getStatus());
                            r.setNote("Requested to join. Note: " + (req.getNote() != null ? req.getNote() : "None"));
                            routesList.add(r);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (routesList.isEmpty()) {
                        tvEmptyTitle.setText(R.string.empty_my_routes);
                        tvEmptySubtitle.setText("Routes you post or request to join will appear here.");
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load my routes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyRoutesResponse> call, Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRouteHistory() {
        showLoading(true);
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        apiService.getRouteHistory(token).enqueue(new Callback<RouteHistoryResponse>() {
            @Override
            public void onResponse(Call<RouteHistoryResponse> call, Response<RouteHistoryResponse> response) {
                if (!isAdded()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    routesList.clear();
                    RouteHistoryResponse res = response.body();

                    if (res.getClosedPostedRoutes() != null) {
                        routesList.addAll(res.getClosedPostedRoutes());
                    }

                    if (res.getAcceptedMatches() != null) {
                        for (RouteJoinRequest req : res.getAcceptedMatches()) {
                            Route r = new Route();
                            r.setId(req.getRoute());
                            r.setHomeArea(req.getRouteHomeArea());
                            r.setDestination(req.getRouteDestination());
                            r.setOwnerUsername(req.getRouteOwnerUsername());
                            r.setUserRequestStatus("MATCHED");
                            r.setNote("Matched route with " + req.getRequesterUsername());
                            routesList.add(r);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (routesList.isEmpty()) {
                        tvEmptyTitle.setText(R.string.empty_history_routes);
                        tvEmptySubtitle.setText("Past completed routes and accepted matches will be archived here.");
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RouteHistoryResponse> call, Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (loading && layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
