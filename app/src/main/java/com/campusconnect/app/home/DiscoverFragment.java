package com.campusconnect.app.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.NotificationBellBinder;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.user.User;
import com.campusconnect.app.user.UserApiService;
import com.campusconnect.app.user.UserListResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverFragment extends Fragment {

    // Waits for a pause in typing before searching, so we're not firing a
    // network request on every keystroke.
    private static final long SEARCH_DEBOUNCE_MS = 350;

    private RecyclerView recyclerView;
    private TextView tvStatus;
    private EditText etSearch;
    private TokenManager tokenManager;
    private UserAdapter adapter;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    // Pagination: the backend returns 10 users per page. "next" is the full
    // URL for the next page (or null once we're on the last one), scoped to
    // whatever the current search query is.
    private String nextPageUrl;
    private boolean loadingMore;

    // Cancelled on every fresh search so a slow, stale response for an older
    // query can never land after (and overwrite) a newer one's results.
    private Call<UserListResponse> currentSearchCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvStatus = view.findViewById(R.id.tvStatus);
        etSearch = view.findViewById(R.id.etSearch);
        tokenManager = new TokenManager(getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0 || loadingMore || nextPageUrl == null) return;
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                // Fetch the next page a little before the user hits the
                // physical bottom, so scrolling doesn't stall waiting on it.
                if (firstVisible + visibleItemCount + 5 >= totalItemCount) {
                    loadMore();
                }
            }
        });

        view.findViewById(R.id.btnMenu).setOnClickListener(v ->
                ((HomeActivity) requireActivity()).openDrawer());
        NotificationBellBinder.bindClick(view, requireContext());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                String query = s.toString().trim();
                pendingSearch = () -> loadUsers(query);
                searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
            }
        });

        loadUsers("");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            NotificationBellBinder.refreshUnreadDot(getView(), tokenManager, this::isAdded);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
        if (currentSearchCall != null) currentSearchCall.cancel();
    }

    private void loadUsers(String query) {
        if (currentSearchCall != null) currentSearchCall.cancel();

        tvStatus.setText(getString(R.string.loading));
        tvStatus.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        adapter = null;
        nextPageUrl = null;

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        UserApiService service = RetrofitClient.createService(UserApiService.class);
        currentSearchCall = query.isEmpty()
                ? service.getUsers(token)
                : service.searchUsers(token, query);

        currentSearchCall.enqueue(new Callback<UserListResponse>() {
                    @Override
                    public void onResponse(Call<UserListResponse> call,
                                           Response<UserListResponse> response) {
                        if (!isAdded() || call.isCanceled()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            java.util.List<User> users = response.body().getResults();

                            if (users == null || users.isEmpty()) {
                                tvStatus.setText(query.isEmpty()
                                        ? getString(R.string.no_users)
                                        : getString(R.string.discover_search_empty, query));
                                tvStatus.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvStatus.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                nextPageUrl = response.body().getNext();
                                adapter = new UserAdapter(users, user ->
                                        com.campusconnect.app.core.utils.ProfileNavigator.open(
                                                getActivity(), user.getId(), user.getRole()));
                                recyclerView.setAdapter(adapter);
                            }
                        } else {
                            tvStatus.setText("Failed to load users.");
                            tvStatus.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserListResponse> call, Throwable t) {
                        if (!isAdded() || call.isCanceled()) return;
                        t.printStackTrace();
                        android.util.Log.e("Discover", "Error loading users: " + t.getMessage());
                        tvStatus.setText(getString(R.string.error_network));
                        tvStatus.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadMore() {
        if (nextPageUrl == null || loadingMore || adapter == null) return;
        loadingMore = true;

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(UserApiService.class)
                .getPage(token, nextPageUrl)
                .enqueue(new Callback<UserListResponse>() {
                    @Override
                    public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                        loadingMore = false;
                        if (!isAdded() || adapter == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            nextPageUrl = response.body().getNext();
                            java.util.List<User> more = response.body().getResults();
                            if (more != null && !more.isEmpty()) adapter.addUsers(more);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserListResponse> call, Throwable t) {
                        // Leave nextPageUrl as-is so the next scroll attempt retries.
                        loadingMore = false;
                    }
                });
    }
}
