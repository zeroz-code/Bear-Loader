package com.bearmod.loader.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.bearmod.loader.R;
import com.bearmod.loader.ui.adapter.MainVariantAdapter;
import com.bearmod.loader.utils.LanguageManager;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewVariants;
    private LinearProgressIndicator progressBar;
    private LinearLayout layoutNoVariants;
    private LinearLayout layoutError;
    private TextView tvErrorTitle;
    private TextView tvErrorMessage;
    private MaterialButton btnRetry;
    private TextView tvSelectGame;

    private MainVariantAdapter variantAdapter;
    private LanguageManager languageManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        languageManager = new LanguageManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        updateLanguage();

        return view;
    }

    private void initViews(View view) {
        recyclerViewVariants = view.findViewById(R.id.recyclerViewVariants);
        progressBar = view.findViewById(R.id.progressBar);
        layoutNoVariants = view.findViewById(R.id.layoutNoVariants);
        layoutError = view.findViewById(R.id.layoutError);
        tvErrorTitle = view.findViewById(R.id.tvErrorTitle);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        btnRetry = view.findViewById(R.id.btnRetry);
        tvSelectGame = view.findViewById(R.id.tvSelectGame);
    }

    private void setupRecyclerView() {
        variantAdapter = new MainVariantAdapter(variant -> {
            handleVariantDownload(variant);
            return kotlin.Unit.INSTANCE;
        });
        recyclerViewVariants.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewVariants.setAdapter(variantAdapter);
    }

    private void handleVariantDownload(com.bearmod.loader.data.model.VariantItem variant) {
        // Handle variant download
        // For now, we'll just show a simple message
        // In a real implementation, this would trigger the OTA download
        if (getContext() != null) {
            String message = languageManager.isChineseEnabled() ?
                "正在下载 " + variant.getDisplayName() :
                "Downloading " + variant.getDisplayName();
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private com.bearmod.loader.data.model.VariantInfo createSampleVariantInfo(long apkSize, long obbSize) {
        // Create sample file info for demonstration
        String timestamp = String.valueOf(System.currentTimeMillis());

        com.bearmod.loader.data.model.FileInfo apkInfo = new com.bearmod.loader.data.model.FileInfo(
            "pubg_mobile_" + timestamp + ".apk",
            "https://example.com/pubg_" + timestamp + ".apk",
            "sample_apk_hash_" + timestamp,
            apkSize
        );

        com.bearmod.loader.data.model.FileInfo obbInfo = new com.bearmod.loader.data.model.FileInfo(
            "pubg_mobile_" + timestamp + ".obb",
            "https://example.com/pubg_" + timestamp + ".obb",
            "sample_obb_hash_" + timestamp,
            obbSize
        );

        return new com.bearmod.loader.data.model.VariantInfo(apkInfo, obbInfo);
    }

    private void setupObservers() {
        // For now, let's load variants directly
        // In a real implementation, you would observe the ViewModel's StateFlow
        loadVariants();
    }

    private void loadVariants() {
        showLoading();

        // Simulate loading variants
        // In a real implementation, this would come from the OTAViewModel
        java.util.List<com.bearmod.loader.data.model.VariantItem> variants = new java.util.ArrayList<>();

        // Add PUBG Mobile variants - Main supported versions only
        // Using official PUBG Mobile branding and authentic information

        variants.add(new com.bearmod.loader.data.model.VariantItem(
            "GL",
            getString(R.string.pubg_mobile_global),
            getString(R.string.pubg_global_description),
            null,
            true,
            createSampleVariantInfo(1500000000L, 3200000000L) // 1.5GB APK, 3.2GB OBB
        ));

        variants.add(new com.bearmod.loader.data.model.VariantItem(
            "KR",
            getString(R.string.pubg_mobile_korea),
            getString(R.string.pubg_korea_description),
            null,
            true,
            createSampleVariantInfo(1450000000L, 3100000000L) // 1.45GB APK, 3.1GB OBB
        ));

        variants.add(new com.bearmod.loader.data.model.VariantItem(
            "TW",
            getString(R.string.pubg_mobile_taiwan),
            getString(R.string.pubg_taiwan_description),
            null,
            true,
            createSampleVariantInfo(1480000000L, 3150000000L) // 1.48GB APK, 3.15GB OBB
        ));

        variants.add(new com.bearmod.loader.data.model.VariantItem(
            "VNG",
            getString(R.string.pubg_mobile_vietnam),
            getString(R.string.pubg_vietnam_description),
            null,
            true,
            createSampleVariantInfo(1420000000L, 3050000000L) // 1.42GB APK, 3.05GB OBB
        ));

        variants.add(new com.bearmod.loader.data.model.VariantItem(
            "BGMI",
            getString(R.string.pubg_mobile_india),
            getString(R.string.pubg_india_description),
            null,
            true,
            createSampleVariantInfo(1400000000L, 3100000000L) // 1.4GB APK, 3.1GB OBB
        ));

        // Update UI
        hideLoading();
        if (!variants.isEmpty()) {
            variantAdapter.submitList(variants);
            showVariants();
        } else {
            showNoVariants();
        }
    }

    private void setupClickListeners() {
        btnRetry.setOnClickListener(v -> {
            loadVariants();
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewVariants.setVisibility(View.GONE);
        layoutNoVariants.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showVariants() {
        recyclerViewVariants.setVisibility(View.VISIBLE);
        layoutNoVariants.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void showNoVariants() {
        recyclerViewVariants.setVisibility(View.GONE);
        layoutNoVariants.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
    }

    private void showError(String errorMessage) {
        recyclerViewVariants.setVisibility(View.GONE);
        layoutNoVariants.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);

        if (tvErrorMessage != null) {
            tvErrorMessage.setText(errorMessage);
        }
    }

    public void updateLanguage() {
        if (tvSelectGame != null) {
            tvSelectGame.setText(languageManager.isChineseEnabled() ?
                "选择游戏" : "Select Game");
        }

        if (tvErrorTitle != null) {
            tvErrorTitle.setText(languageManager.isChineseEnabled() ?
                "加载失败" : "Loading Failed");
        }

        if (btnRetry != null) {
            btnRetry.setText(languageManager.isChineseEnabled() ?
                "重试" : "Retry");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLanguage();
    }
}
