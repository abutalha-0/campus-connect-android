package com.campusconnect.app.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.profile.PublicProfileActivity;
import com.campusconnect.app.user.User;
import com.campusconnect.app.user.UserApiService;
import com.campusconnect.app.user.UserListResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvStatus;
    private TokenManager tokenManager;

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
        tokenManager = new TokenManager(getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUsers();
    }

    private void loadUsers() {
        tvStatus.setText(getString(R.string.loading));
        tvStatus.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();

        RetrofitClient.createService(UserApiService.class)
                .getUsers(token)
                .enqueue(new Callback<UserListResponse>() {
                    @Override
                    public void onResponse(Call<UserListResponse> call,
                                           Response<UserListResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            java.util.List<User> users = response.body().getResults();

                            if (users == null || users.isEmpty()) {
                                tvStatus.setText(getString(R.string.no_users));
                                tvStatus.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvStatus.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                UserAdapter adapter = new UserAdapter(users, user -> {
                                    Intent intent = new Intent(getActivity(),
                                            PublicProfileActivity.class);
                                    intent.putExtra("user_id", user.getId());
                                    intent.putExtra("full_name", user.getFullName());
                                    intent.putExtra("username", user.getUsername());
                                    startActivity(intent);
                                });
                                recyclerView.setAdapter(adapter);
                            }
                        } else {
                            tvStatus.setText("Failed to load users.");
                            tvStatus.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserListResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        tvStatus.setText(getString(R.string.error_network));
                        tvStatus.setVisibility(View.VISIBLE);
                    }
                });
    }
}