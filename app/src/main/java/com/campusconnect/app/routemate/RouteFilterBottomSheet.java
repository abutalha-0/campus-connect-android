package com.campusconnect.app.routemate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.campusconnect.app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class RouteFilterBottomSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onApplyFilters(String homeArea, String destination, String genderPreference);
    }

    private FilterListener listener;
    private String homeArea, destination, genderPreference;

    public static RouteFilterBottomSheet newInstance(String homeArea, String destination, String genderPreference) {
        RouteFilterBottomSheet fragment = new RouteFilterBottomSheet();
        Bundle args = new Bundle();
        args.putString("homeArea", homeArea);
        args.putString("destination", destination);
        args.putString("genderPreference", genderPreference);
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
            homeArea = getArguments().getString("homeArea");
            destination = getArguments().getString("destination");
            genderPreference = getArguments().getString("genderPreference");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_route_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etHomeArea = view.findViewById(R.id.etFilterHomeArea);
        EditText etDestination = view.findViewById(R.id.etFilterDestination);
        MaterialButtonToggleGroup toggleGender = view.findViewById(R.id.toggleFilterGender);

        etHomeArea.setText(homeArea);
        etDestination.setText(destination);

        if ("ANY".equalsIgnoreCase(genderPreference)) toggleGender.check(R.id.btnFilterGenderAny);
        else if ("MALE_ONLY".equalsIgnoreCase(genderPreference)) toggleGender.check(R.id.btnFilterGenderMale);
        else if ("FEMALE_ONLY".equalsIgnoreCase(genderPreference)) toggleGender.check(R.id.btnFilterGenderFemale);

        view.findViewById(R.id.tvClearAll).setOnClickListener(v -> {
            etHomeArea.setText("");
            etDestination.setText("");
            toggleGender.clearChecked();
        });

        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> {
            String selectedHome = etHomeArea.getText().toString().trim();
            String selectedDest = etDestination.getText().toString().trim();
            int checkedId = toggleGender.getCheckedButtonId();
            String selectedGender = null;
            if (checkedId == R.id.btnFilterGenderAny) selectedGender = "ANY";
            else if (checkedId == R.id.btnFilterGenderMale) selectedGender = "MALE_ONLY";
            else if (checkedId == R.id.btnFilterGenderFemale) selectedGender = "FEMALE_ONLY";

            if (listener != null) {
                listener.onApplyFilters(
                        selectedHome.isEmpty() ? null : selectedHome,
                        selectedDest.isEmpty() ? null : selectedDest,
                        selectedGender
                );
            }
            dismiss();
        });
    }
}
