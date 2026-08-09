package com.campusconnect.app.lostfound;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.campusconnect.app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.Calendar;
import java.util.Locale;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onApplyFilters(String status, String category, String location, String date);
    }

    private FilterListener listener;
    private String selectedStatus, selectedCategory, selectedLocation, selectedDate;

    public static FilterBottomSheet newInstance(String status, String category, String location, String date) {
        FilterBottomSheet fragment = new FilterBottomSheet();
        Bundle args = new Bundle();
        args.putString("status", status);
        args.putString("category", category);
        args.putString("location", location);
        args.putString("date", date);
        fragment.setArguments(args);
        return fragment;
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedStatus = getArguments().getString("status");
            selectedCategory = getArguments().getString("category");
            selectedLocation = getArguments().getString("location");
            selectedDate = getArguments().getString("date");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupStatus);
        EditText etCategory = view.findViewById(R.id.etCategory);
        EditText etLocation = view.findViewById(R.id.etLocation);
        TextView tvDate = view.findViewById(R.id.tvDate);

        if (selectedStatus != null) {
            if ("OPEN".equalsIgnoreCase(selectedStatus)) toggleGroup.check(R.id.btnStatusOpen);
            else if ("CLAIMED".equalsIgnoreCase(selectedStatus)) toggleGroup.check(R.id.btnStatusClaimed);
            else if ("CLOSED".equalsIgnoreCase(selectedStatus)) toggleGroup.check(R.id.btnStatusClosed);
        }

        etCategory.setText(selectedCategory);
        etLocation.setText(selectedLocation);
        tvDate.setText(selectedDate);

        tvDate.setOnClickListener(v -> showDatePicker(tvDate));

        view.findViewById(R.id.btnClearAll).setOnClickListener(v -> {
            toggleGroup.clearChecked();
            etCategory.setText("");
            etLocation.setText("");
            tvDate.setText("");
        });

        view.findViewById(R.id.btnApply).setOnClickListener(v -> {
            int checkedId = toggleGroup.getCheckedRadioButtonId();
            String status = null;
            if (checkedId == R.id.btnStatusOpen) status = "OPEN";
            else if (checkedId == R.id.btnStatusClaimed) status = "CLAIMED";
            else if (checkedId == R.id.btnStatusClosed) status = "CLOSED";

            if (listener != null) {
                listener.onApplyFilters(status,
                        etCategory.getText().toString(),
                        etLocation.getText().toString(),
                        tvDate.getText().toString());
            }
            dismiss();
        });
    }

    private void showDatePicker(TextView tvDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
