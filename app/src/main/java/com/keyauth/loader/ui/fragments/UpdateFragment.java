package com.keyauth.loader.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.keyauth.loader.R;
import com.keyauth.loader.utils.LanguageManager;

public class UpdateFragment extends Fragment {

    private LinearProgressIndicator progressBar;
    private LinearLayout layoutUpdateInfo;
    private TextView tvUpdateStatus;
    private TextView tvCurrentVersion;
    private TextView tvLastUpdateCheck;
    private MaterialButton btnCheckUpdates;

    private LanguageManager languageManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        languageManager = new LanguageManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update, container, false);
        
        initViews(view);
        setupClickListeners();
        updateLanguage();
        initializeUpdateStatus();

        return view;
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        layoutUpdateInfo = view.findViewById(R.id.layoutUpdateInfo);
        tvUpdateStatus = view.findViewById(R.id.tvUpdateStatus);
        tvCurrentVersion = view.findViewById(R.id.tvCurrentVersion);
        tvLastUpdateCheck = view.findViewById(R.id.tvLastUpdateCheck);
        btnCheckUpdates = view.findViewById(R.id.btnCheckUpdates);
    }

    private void setupClickListeners() {
        btnCheckUpdates.setOnClickListener(v -> {
            checkForUpdates();
        });
    }

    private void initializeUpdateStatus() {
        // Set current version info
        if (tvCurrentVersion != null) {
            String versionText = languageManager.isChineseEnabled() ?
                "版本 1.0.0 (构建 1)" : "Version 1.0.0 (Build 1)";
            tvCurrentVersion.setText(versionText);
        }

        // Set last update check
        if (tvLastUpdateCheck != null) {
            String lastCheckText = languageManager.isChineseEnabled() ?
                "上次检查: 从未" : "Last checked: Never";
            tvLastUpdateCheck.setText(lastCheckText);
        }

        // Set initial status
        if (tvUpdateStatus != null) {
            tvUpdateStatus.setText(languageManager.isChineseEnabled() ?
                "点击检查更新" : "Click to check for updates");
        }
    }

    private void checkForUpdates() {
        showLoading();

        // Simulate update check
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            hideLoading();
            showUpToDate();
            updateLastCheckTime();
        }, 2000);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnCheckUpdates.setEnabled(false);
        if (tvUpdateStatus != null) {
            tvUpdateStatus.setText(languageManager.isChineseEnabled() ?
                "正在检查更新..." : "Checking for updates...");
        }
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnCheckUpdates.setEnabled(true);
    }

    private void showUpToDate() {
        layoutUpdateInfo.setVisibility(View.VISIBLE);
        if (tvUpdateStatus != null) {
            tvUpdateStatus.setText(languageManager.isChineseEnabled() ?
                "应用已是最新版本" : "App is up to date");
        }
    }

    private void updateLastCheckTime() {
        if (tvLastUpdateCheck != null) {
            String currentTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",
                java.util.Locale.getDefault()).format(new java.util.Date());
            String lastCheckText = languageManager.isChineseEnabled() ?
                "上次检查: " + currentTime : "Last checked: " + currentTime;
            tvLastUpdateCheck.setText(lastCheckText);
        }
    }

    public void updateLanguage() {
        if (btnCheckUpdates != null) {
            btnCheckUpdates.setText(languageManager.isChineseEnabled() ?
                "检查更新" : "Check for Updates");
        }

        // Update other text views
        initializeUpdateStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLanguage();
    }
}
