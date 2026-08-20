package com.campusconnect.app.lostfound;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.campusconnect.app.lostfound.model.ClaimAnswer;
import com.campusconnect.app.lostfound.model.ClaimAttempt;
import com.campusconnect.app.lostfound.model.ClaimQuestion;
import com.campusconnect.app.lostfound.model.LostFoundItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailFragment extends Fragment {

    private int itemId;
    private TokenManager tokenManager;
    private LostFoundItem currentItem;
    private Button btnClaimItem;
    private TextView tvClaimStatus;
    private LinearLayout layoutClaimAttempts, containerClaimAttempts;
    private boolean isMyClaimApproved = false;

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

        btnClaimItem = view.findViewById(R.id.btnClaimItem);
        tvClaimStatus = view.findViewById(R.id.tvClaimStatus);
        layoutClaimAttempts = view.findViewById(R.id.layoutClaimAttempts);
        containerClaimAttempts = view.findViewById(R.id.containerClaimAttempts);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnRevealContact).setOnClickListener(v -> {
            if (currentItem != null) showContactDialog(currentItem);
        });

        view.findViewById(R.id.btnReport).setOnClickListener(v -> showReportDialog());

        btnClaimItem.setOnClickListener(v -> {
            if (currentItem != null) showClaimDialog(currentItem);
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
                            loadClaims(currentItem);
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

    private boolean isItemRevealed(LostFoundItem item) {
        if (item == null) return false;
        if (!LostFoundItem.TYPE_FOUND.equalsIgnoreCase(item.getItemType())) return true;
        if (item.getClaimAttempts() != null) return true;
        if (LostFoundItem.STATUS_CLAIMED.equalsIgnoreCase(item.getStatus())) return true;
        if (isMyClaimApproved) return true;
        return false;
    }

    private void updateLocationAndDescriptionDisplay(boolean isRevealed) {
        View view = getView();
        if (view == null || currentItem == null) return;

        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvLocation = view.findViewById(R.id.tvLocationDetail);

        String desc = currentItem.getDescription();
        String loc = currentItem.getLocation();
        boolean isFound = LostFoundItem.TYPE_FOUND.equalsIgnoreCase(currentItem.getItemType());

        if (isFound && !isRevealed) {
            tvDescription.setText("[Description redacted for security verification]");
            tvDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));

            tvLocation.setText("[Location hidden until claim approved]");
            tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));
        } else {
            if (desc != null && !desc.trim().isEmpty()) {
                tvDescription.setText(desc);
                tvDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dim));
            } else {
                tvDescription.setText("No description provided");
                tvDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));
            }

            if (loc != null && !loc.trim().isEmpty()) {
                tvLocation.setText(loc);
                tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            } else {
                tvLocation.setText("Not specified");
                tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        }
    }

    private void populateUI(LostFoundItem item) {
        View view = getView();
        if (view == null) return;

        ImageView ivPhoto = view.findViewById(R.id.ivPhoto);
        TextView tvTypeBadge = view.findViewById(R.id.tvTypeBadge);
        TextView tvStatusBadge = view.findViewById(R.id.tvStatusBadgeDetail);
        TextView tvTitle = view.findViewById(R.id.tvTitleDetail);
        TextView tvCategory = view.findViewById(R.id.tvCategoryDetail);
        TextView tvDateTime = view.findViewById(R.id.tvDateTimeDetail);
        TextView tvPostedBy = view.findViewById(R.id.tvPostedBy);

        tvTitle.setText(item.getTitle());
        tvCategory.setText(item.getCategory());

        updateLocationAndDescriptionDisplay(isItemRevealed(item));

        String dt = item.getDateSeen();
        if (dt == null || dt.trim().isEmpty()) {
            tvDateTime.setText("Not specified");
        } else {
            tvDateTime.setText(TimeUtils.formatDate(dt));
        }
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
        } else if (LostFoundItem.STATUS_CLAIMED.equalsIgnoreCase(item.getStatus())) {
            tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.amber_gold));
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.amber_gold_dim)));
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(this).load(item.getImageUrl()).into(ivPhoto);
        }
    }

    private void loadClaims(LostFoundItem item) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        RetrofitClient.createService(LostFoundApiService.class)
                .getClaimAttempts(token, item.getId())
                .enqueue(new Callback<List<ClaimAttempt>>() {
                    @Override
                    public void onResponse(Call<List<ClaimAttempt>> call, Response<List<ClaimAttempt>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<ClaimAttempt> attempts = response.body();

                            // Check if current user has claim attempts in owner serializer context or claimant GET
                            if (item.getClaimAttempts() != null) {
                                // Owner view
                                renderOwnerClaimAttempts(item.getClaimAttempts());
                            } else if (!attempts.isEmpty()) {
                                // Claimant view
                                ClaimAttempt myAttempt = attempts.get(0);
                                tvClaimStatus.setText("Your Claim Attempt Status: " + myAttempt.getStatus());
                                tvClaimStatus.setVisibility(View.VISIBLE);
                                btnClaimItem.setVisibility(View.GONE);
                                if (ClaimAttempt.STATUS_APPROVED.equalsIgnoreCase(myAttempt.getStatus())) {
                                    isMyClaimApproved = true;
                                    updateLocationAndDescriptionDisplay(true);
                                }
                            } else {
                                // Non-owner who has not claimed yet
                                tvClaimStatus.setVisibility(View.GONE);
                                if (LostFoundItem.TYPE_FOUND.equalsIgnoreCase(item.getItemType())
                                        && LostFoundItem.STATUS_OPEN.equalsIgnoreCase(item.getStatus())) {
                                    btnClaimItem.setVisibility(View.VISIBLE);
                                } else {
                                    btnClaimItem.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            if (LostFoundItem.TYPE_FOUND.equalsIgnoreCase(item.getItemType())
                                    && LostFoundItem.STATUS_OPEN.equalsIgnoreCase(item.getStatus())
                                    && item.getClaimAttempts() == null) {
                                btnClaimItem.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ClaimAttempt>> call, Throwable t) {
                        if (!isAdded()) return;
                    }
                });
    }

    private void renderOwnerClaimAttempts(List<ClaimAttempt> attempts) {
        if (attempts == null || attempts.isEmpty()) {
            layoutClaimAttempts.setVisibility(View.GONE);
            return;
        }

        layoutClaimAttempts.setVisibility(View.VISIBLE);
        containerClaimAttempts.removeAllViews();

        for (ClaimAttempt attempt : attempts) {
            View cardView = getLayoutInflater().inflate(R.layout.dialog_contact_info, containerClaimAttempts, false);
            // Replace with card styling inline
            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(32, 32, 32, 32);
            itemLayout.setBackgroundResource(R.drawable.bg_lf_card);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, 24);
            itemLayout.setLayoutParams(lp);

            TextView tvClaimant = new TextView(requireContext());
            tvClaimant.setText("Claimant: @" + (attempt.getClaimantUsername() != null ? attempt.getClaimantUsername() : "User " + attempt.getClaimantId()));
            tvClaimant.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvClaimant.setTextSize(14f);
            tvClaimant.setTypeface(null, android.graphics.Typeface.BOLD);
            itemLayout.addView(tvClaimant);

            TextView tvStatus = new TextView(requireContext());
            tvStatus.setText("Status: " + attempt.getStatus());
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(),
                    ClaimAttempt.STATUS_APPROVED.equals(attempt.getStatus()) ? R.color.amber_gold :
                    ClaimAttempt.STATUS_REJECTED.equals(attempt.getStatus()) ? R.color.text_faint : R.color.purple));
            tvStatus.setTextSize(12f);
            tvStatus.setPadding(0, 4, 0, 8);
            itemLayout.addView(tvStatus);

            if (attempt.getAnswers() != null) {
                for (ClaimAnswer ans : attempt.getAnswers()) {
                    TextView tvAns = new TextView(requireContext());
                    String qText = ans.getQuestionText() != null ? ans.getQuestionText() : "Question #" + ans.getQuestionId();
                    tvAns.setText("Q: " + qText + "\nA: " + ans.getAnswerText());
                    tvAns.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dim));
                    tvAns.setTextSize(13f);
                    tvAns.setPadding(0, 4, 0, 4);
                    itemLayout.addView(tvAns);
                }
            }

            if (ClaimAttempt.STATUS_PENDING.equals(attempt.getStatus())) {
                LinearLayout btnRow = new LinearLayout(requireContext());
                btnRow.setOrientation(LinearLayout.HORIZONTAL);
                btnRow.setPadding(0, 16, 0, 0);

                Button btnApprove = new Button(requireContext());
                btnApprove.setText("Approve");
                btnApprove.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.amber_gold)));
                btnApprove.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_on_amber_text));

                Button btnReject = new Button(requireContext());
                btnReject.setText("Reject");
                btnReject.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#334155")));
                btnReject.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                bLp.setMarginEnd(12);
                btnApprove.setLayoutParams(bLp);

                LinearLayout.LayoutParams bLp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                btnReject.setLayoutParams(bLp2);

                btnApprove.setOnClickListener(v -> reviewAttempt(attempt.getId(), ClaimAttempt.STATUS_APPROVED));
                btnReject.setOnClickListener(v -> reviewAttempt(attempt.getId(), ClaimAttempt.STATUS_REJECTED));

                btnRow.addView(btnApprove);
                btnRow.addView(btnReject);
                itemLayout.addView(btnRow);
            }

            containerClaimAttempts.addView(itemLayout);
        }
    }

    private void reviewAttempt(int attemptId, String newStatus) {
        String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);

        RetrofitClient.createService(LostFoundApiService.class)
                .reviewClaimAttempt(token, attemptId, body)
                .enqueue(new Callback<ClaimAttempt>() {
                    @Override
                    public void onResponse(Call<ClaimAttempt> call, Response<ClaimAttempt> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Claim request updated to " + newStatus, Toast.LENGTH_SHORT).show();
                            loadItemDetail();
                        } else {
                            Toast.makeText(getContext(), "Failed to update claim request", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ClaimAttempt> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showClaimDialog(LostFoundItem item) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundResource(R.drawable.bg_lf_card);

        TextView title = new TextView(requireContext());
        title.setText("Submit Verification Claim");
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(title);

        TextView desc = new TextView(requireContext());
        desc.setText("Answer the questions set by the finder to verify your ownership.");
        desc.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));
        desc.setTextSize(13f);
        desc.setPadding(0, 8, 0, 24);
        layout.addView(desc);

        List<ClaimQuestion> questions = item.getClaimQuestions();
        List<EditText> answerInputs = new ArrayList<>();
        List<Integer> questionIds = new ArrayList<>();

        if (questions != null && !questions.isEmpty()) {
            for (ClaimQuestion q : questions) {
                TextView tvQ = new TextView(requireContext());
                tvQ.setText(q.getQuestionText());
                tvQ.setTextColor(ContextCompat.getColor(requireContext(), R.color.amber_gold));
                tvQ.setTextSize(14f);
                tvQ.setTypeface(null, android.graphics.Typeface.BOLD);
                tvQ.setPadding(0, 12, 0, 6);
                layout.addView(tvQ);

                EditText etAns = new EditText(requireContext());
                etAns.setHint("Enter your answer...");
                etAns.setBackgroundResource(R.drawable.bg_lf_input);
                etAns.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                etAns.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));
                etAns.setPadding(32, 24, 32, 24);
                layout.addView(etAns);

                answerInputs.add(etAns);
                questionIds.add(q.getId());
            }
        } else {
            TextView tvDefault = new TextView(requireContext());
            tvDefault.setText("Describe unique features of the item to the owner:");
            tvDefault.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dim));
            tvDefault.setTextSize(13f);
            layout.addView(tvDefault);

            EditText etAns = new EditText(requireContext());
            etAns.setHint("Enter details...");
            etAns.setBackgroundResource(R.drawable.bg_lf_input);
            etAns.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            etAns.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));
            etAns.setPadding(32, 24, 32, 24);
            layout.addView(etAns);

            answerInputs.add(etAns);
            questionIds.add(0);
        }

        Button btnSubmit = new Button(requireContext());
        btnSubmit.setText("Submit Claim Attempt");
        btnSubmit.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.amber_gold)));
        btnSubmit.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_on_amber_text));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 32, 0, 0);
        btnSubmit.setLayoutParams(lp);

        btnSubmit.setOnClickListener(v -> {
            List<Map<String, Object>> answersList = new ArrayList<>();
            for (int i = 0; i < answerInputs.size(); i++) {
                String ansText = answerInputs.get(i).getText().toString().trim();
                if (ansText.isEmpty()) {
                    Toast.makeText(getContext(), "Please answer all questions", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> aMap = new HashMap<>();
                if (questionIds.get(i) > 0) {
                    aMap.put("question", questionIds.get(i));
                }
                aMap.put("answer_text", ansText);
                answersList.add(aMap);
            }

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("answers", answersList);

            String token = Constants.TOKEN_PREFIX + tokenManager.getAccessToken();
            RetrofitClient.createService(LostFoundApiService.class)
                    .submitClaimAttempt(token, item.getId(), reqBody)
                    .enqueue(new Callback<ClaimAttempt>() {
                        @Override
                        public void onResponse(Call<ClaimAttempt> call, Response<ClaimAttempt> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Claim attempt submitted successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadItemDetail();
                            } else {
                                Toast.makeText(getContext(), "Failed to submit claim attempt", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ClaimAttempt> call, Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        layout.addView(btnSubmit);

        dialog.setContentView(layout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }

    private void showContactDialog(LostFoundItem item) {
        if (!isItemRevealed(item)) {
            Toast.makeText(getContext(), "Location & contact details are protected until your claim request is approved by the finder.", Toast.LENGTH_LONG).show();
            return;
        }
        String contactInfo = item.getContactInfo();

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_contact_info);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvName = dialog.findViewById(R.id.tvContactName);
        TextView tvInitials = dialog.findViewById(R.id.tvContactInitials);
        TextView tvValue = dialog.findViewById(R.id.tvContactValue);
        Button btnCall = dialog.findViewById(R.id.btnCallContact);
        Button btnEmail = dialog.findViewById(R.id.btnEmailContact);
        Button btnCopy = dialog.findViewById(R.id.btnCopyContact);

        tvName.setText(item.getReportedBy());
        tvInitials.setText(initialsOf(item.getReportedBy()));

        if (contactInfo == null || contactInfo.trim().isEmpty()) {
            contactInfo = "No contact details specified";
        }
        tvValue.setText(contactInfo);

        final String finalContact = contactInfo;

        String digitsOnly = finalContact.replaceAll("[^0-9+]", "");
        if (digitsOnly.length() >= 7) {
            btnCall.setVisibility(View.VISIBLE);
            btnCall.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + digitsOnly));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to launch dialer", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (finalContact.contains("@")) {
            btnEmail.setVisibility(View.VISIBLE);
            btnEmail.setOnClickListener(v -> {
                try {
                    String emailAddress = finalContact;
                    for (String word : finalContact.split("\\s+")) {
                        if (word.contains("@")) {
                            emailAddress = word;
                            break;
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAddress));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Campus Connect Lost & Found: " + item.getTitle());
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to launch email client", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Contact Info", finalContact);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), R.string.msg_copied, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showReportDialog() {
        String[] reasons = new String[]{
                getString(R.string.report_reason_inaccurate),
                getString(R.string.report_reason_claimed),
                getString(R.string.report_reason_inappropriate),
                getString(R.string.report_reason_other)
        };

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.report_dialog_title);
        builder.setSingleChoiceItems(reasons, 0, null);
        builder.setPositiveButton(R.string.btn_submit_report, (dialog, which) -> {
            Toast.makeText(getContext(), R.string.report_success_toast, Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(R.string.btn_close, (dialog, which) -> dialog.dismiss());
        builder.show();
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

