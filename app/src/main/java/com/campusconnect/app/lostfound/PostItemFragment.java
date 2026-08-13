package com.campusconnect.app.lostfound;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.campusconnect.app.R;
import com.campusconnect.app.core.api.RetrofitClient;
import com.campusconnect.app.core.utils.Constants;
import com.campusconnect.app.core.utils.FileUtils;
import com.campusconnect.app.core.utils.TokenManager;
import com.campusconnect.app.lostfound.api.LostFoundApiService;
import com.campusconnect.app.lostfound.model.LostFoundItem;
import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostItemFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String ARG_ITEM_ID = "item_id";

    private int editingItemId = -1;
    private String currentType = LostFoundItem.TYPE_LOST;
    private Uri selectedImageUri;

    public static PostItemFragment newInstance(int itemId) {
        PostItemFragment fragment = new PostItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ID, itemId);
        fragment.setArguments(args);
        return fragment;
    }

    private View btnTypeLost, btnTypeFound;
    private TextView tvLostLabel, tvFoundLabel;
    private EditText etTitle, etCategory, etDescription, etLocation, etContactInfo;
    private TextView tvDateTime;
    private ImageView ivPreview;
    private View btnUploadPhoto;

    private TokenManager tokenManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            editingItemId = getArguments().getInt(ARG_ITEM_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        btnTypeLost = view.findViewById(R.id.btnTypeLost);
        btnTypeFound = view.findViewById(R.id.btnTypeFound);
        tvLostLabel = view.findViewById(R.id.tvLostLabel);
        tvFoundLabel = view.findViewById(R.id.tvFoundLabel);
        etTitle = view.findViewById(R.id.etTitle);
        etCategory = view.findViewById(R.id.etCategoryPost);
        etDescription = view.findViewById(R.id.etDescription);
        etLocation = view.findViewById(R.id.etLocationPost);
        etContactInfo = view.findViewById(R.id.etContactInfo);
        tvDateTime = view.findViewById(R.id.tvDateTimePost);
        ivPreview = view.findViewById(R.id.ivPreview);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnTypeLost.setOnClickListener(v -> selectType(LostFoundItem.TYPE_LOST));
        btnTypeFound.setOnClickListener(v -> selectType(LostFoundItem.TYPE_FOUND));

        tvDateTime.setOnClickListener(v -> showDateTimePicker());
        btnUploadPhoto.setOnClickListener(v -> openImagePicker());

        view.findViewById(R.id.btnSubmit).setOnClickListener(v -> submitItem());

        if (editingItemId != -1) {
            loadItemForEdit();
            ((TextView) view.findViewById(R.id.btnSubmit)).setText("Update item");
            ((TextView) view.findViewById(R.id.tvPostTitle)).setText("Edit item");
        }
    }

    private void loadItemForEdit() {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(LostFoundApiService.class)
                .getItemDetail(token, editingItemId)
                .enqueue(new Callback<LostFoundItem>() {
                    @Override
                    public void onResponse(Call<LostFoundItem> call, Response<LostFoundItem> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            populateForEdit(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<LostFoundItem> call, Throwable t) {}
                });
    }

    private void populateForEdit(LostFoundItem item) {
        etTitle.setText(item.getTitle());
        etCategory.setText(item.getCategory());
        etDescription.setText(item.getDescription());
        etLocation.setText(item.getLocation());
        etContactInfo.setText(item.getContactInfo());
        tvDateTime.setText(item.getDateSeen());
        selectType(item.getItemType());
        
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(item.getImageUrl()).into(ivPreview);
            ivPreview.setVisibility(View.VISIBLE);
        }
    }

    private void selectType(String type) {
        currentType = type;
        boolean isLost = LostFoundItem.TYPE_LOST.equals(type);
        
        btnTypeLost.setBackgroundResource(isLost ? R.drawable.bg_lf_type_lost_selected : R.drawable.bg_lf_type_unselected);
        tvLostLabel.setTextColor(ContextCompat.getColor(requireContext(), isLost ? R.color.amber : R.color.text_dim));
        
        btnTypeFound.setBackgroundResource(!isLost ? R.drawable.bg_lf_type_found_selected : R.drawable.bg_lf_type_unselected);
        tvFoundLabel.setTextColor(ContextCompat.getColor(requireContext(), !isLost ? R.color.amber_gold : R.color.text_dim));
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            String dateOnly = String.format(Locale.US, "%04d-%02d-%02d", 
                    year, month + 1, dayOfMonth);
            tvDateTime.setText(dateOnly);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivPreview.setImageURI(selectedImageUri);
            ivPreview.setVisibility(View.VISIBLE);
        }
    }

    private void submitItem() {
        String title = etTitle.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String dateTime = tvDateTime.getText().toString().trim();

        if (title.isEmpty() || category.isEmpty() || description.isEmpty() || location.isEmpty() || dateTime.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        String contactInfo = etContactInfo.getText().toString().trim();

        String dateOnly = dateTime.split(" ")[0];

        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        LostFoundApiService api = RetrofitClient.createService(LostFoundApiService.class);

        RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descPart = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody typePart = RequestBody.create(MediaType.parse("text/plain"), currentType);
        RequestBody catPart = RequestBody.create(MediaType.parse("text/plain"), category);
        RequestBody locPart = RequestBody.create(MediaType.parse("text/plain"), location);
        RequestBody datePart = RequestBody.create(MediaType.parse("text/plain"), dateOnly);
        RequestBody contactPart = RequestBody.create(MediaType.parse("text/plain"), contactInfo);

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                File file = FileUtils.copyToCache(requireContext(), selectedImageUri);
                RequestBody requestFile = RequestBody.create(
                        MediaType.parse(requireContext().getContentResolver().getType(selectedImageUri)),
                        file
                );
                imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Call<LostFoundItem> call;
        if (editingItemId == -1) {
            call = api.createItemWithImage(token, titlePart, descPart, typePart, catPart, locPart, datePart, contactPart, imagePart);
        } else {
            RequestBody statusPart = RequestBody.create(MediaType.parse("text/plain"), LostFoundItem.STATUS_OPEN); // Default for edit if not changed
            call = api.updateItemWithImage(token, editingItemId, titlePart, descPart, typePart, catPart, locPart, datePart, contactPart, statusPart, imagePart);
        }

        call.enqueue(new Callback<LostFoundItem>() {
            @Override
            public void onResponse(Call<LostFoundItem> call, Response<LostFoundItem> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), editingItemId == -1 ? "Item posted successfully" : "Item updated successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Failed to save item", Toast.LENGTH_SHORT).show();
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
