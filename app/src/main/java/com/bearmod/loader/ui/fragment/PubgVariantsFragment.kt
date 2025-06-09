package com.bearmod.loader.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bearmod.loader.R
import com.bearmod.loader.data.model.PubgButtonState
import com.bearmod.loader.data.model.PubgVariant
import com.bearmod.loader.data.model.PubgManifestLoader
import com.bearmod.loader.ui.adapter.PubgVariantAdapter
import com.bearmod.loader.utils.PackageVersionChecker
import com.bearmod.loader.utils.PermissionManager

/**
 * Fragment displaying PUBG Mobile variants in ZEUS God Aim UI style
 * Shows list of available PUBG versions with download functionality
 * Enhanced with JSON manifest loading and Android 11+ permissions
 */
class PubgVariantsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PubgVariantAdapter
    // Buttons moved to Settings fragment
    private lateinit var packageChecker: PackageVersionChecker
    private lateinit var permissionManager: PermissionManager
    private var variants = mutableListOf<PubgVariant>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pubg_variants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize utilities
        packageChecker = PackageVersionChecker(requireContext())
        permissionManager = PermissionManager(requireContext())

        setupRecyclerView(view)
        // Footer buttons moved to Settings fragment
        checkPermissionsAndLoadVariants()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.rv_pubg_variants)

        adapter = PubgVariantAdapter(
            variants = variants,
            onDownloadClick = { variant -> onDownloadClicked(variant) },
            onUpdateClick = { variant -> onUpdateClicked(variant) },
            onOpenClick = { variant -> onOpenClicked(variant) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PubgVariantsFragment.adapter

            // Add item decoration for spacing
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.bottom = 8
                    outRect.top = 8
                }
            })
        }
    }

    private fun setupFooterButtons(view: View) {
        // Footer buttons moved to Settings fragment
        // This method is kept for compatibility but no longer sets up buttons
    }

    private fun setupButtonAnimations(button: Button) {
        button.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val pressAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.button_press_scale)
                    view.startAnimation(pressAnimation)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val releaseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.button_release_scale)
                    view.startAnimation(releaseAnimation)
                }
            }
            false // Return false to allow click events to continue
        }
    }

    // clearAppData method moved to Settings fragment

    /**
     * Check permissions and load variants
     */
    private fun checkPermissionsAndLoadVariants() {
        // For now, bypass permission check to test variant loading
        // TODO: Re-enable permission checking after debugging
        loadPubgVariants()

        /*
        if (permissionManager.hasAllRequiredPermissions()) {
            loadPubgVariants()
        } else {
            requestPermissions()
        }
        */
    }

    /**
     * Request required permissions
     */
    private fun requestPermissions() {
        permissionManager.requestStoragePermission(this)
    }

    /**
     * Load PUBG variants from JSON manifest with fallback
     */
    private fun loadPubgVariants() {
        // Temporarily force fallback to test hardcoded variants
        Toast.makeText(
            requireContext(),
            "Testing fallback variants...",
            Toast.LENGTH_SHORT
        ).show()
        loadFallbackVariants()

        /*
        try {
            // Load variants from JSON manifest
            variants.clear()
            val loadedVariants = PubgManifestLoader.loadFromAssets(requireContext())

            if (loadedVariants.isNotEmpty()) {
                variants.addAll(loadedVariants)

                // Update button states based on installed packages
                updateVariantStates()

                adapter.updateVariants(variants)

                Toast.makeText(
                    requireContext(),
                    "Loaded ${variants.size} PUBG variants from manifest",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // If JSON returns empty list, use fallback
                loadFallbackVariants()
            }
        } catch (e: Exception) {
            // Fallback to hardcoded variants
            Toast.makeText(
                requireContext(),
                "JSON loading failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            loadFallbackVariants()
        }
        */
    }

    /**
     * Load fallback hardcoded variants
     */
    private fun loadFallbackVariants() {
        android.util.Log.d("PubgVariantsFragment", "Loading fallback variants...")

        variants.clear()
        val hardcodedVariants = PubgVariant.getAllVariants()

        android.util.Log.d("PubgVariantsFragment", "Got ${hardcodedVariants.size} hardcoded variants")

        variants.addAll(hardcodedVariants)

        android.util.Log.d("PubgVariantsFragment", "Variants list now has ${variants.size} items")

        updateVariantStates()

        android.util.Log.d("PubgVariantsFragment", "Updated variant states")

        adapter.updateVariants(variants)

        android.util.Log.d("PubgVariantsFragment", "Updated adapter with ${variants.size} variants")

        Toast.makeText(
            requireContext(),
            "Loaded ${variants.size} PUBG variants (fallback)",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Update variant button states based on installed packages
     */
    private fun updateVariantStates() {
        variants.forEachIndexed { index, variant ->
            val packageInfo = packageChecker.getPubgVariantInfo(variant.id)

            variants[index] = when {
                packageInfo?.isInstalled != true -> {
                    variant.copy(
                        buttonState = PubgButtonState.DOWNLOAD,
                        installedVersion = null
                    )
                }
                packageChecker.isUpdateAvailable(variant.packageName, variant.version) -> {
                    variant.copy(
                        buttonState = PubgButtonState.UPDATE,
                        installedVersion = packageInfo.installedVersion
                    )
                }
                else -> {
                    variant.copy(
                        buttonState = PubgButtonState.OPEN,
                        installedVersion = packageInfo.installedVersion
                    )
                }
            }
        }
    }

    private fun onDownloadClicked(variant: PubgVariant) {
        // Handle download click
        Toast.makeText(
            requireContext(),
            "Starting download for ${variant.name}",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Implement actual download logic
        // This would integrate with your OTA download system
        startDownload(variant)
    }

    private fun onUpdateClicked(variant: PubgVariant) {
        // Handle update click
        Toast.makeText(
            requireContext(),
            "Starting update for ${variant.name}",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Implement actual update logic
        // This would integrate with your OTA download system
        startDownload(variant) // Same as download for now
    }

    private fun onOpenClicked(variant: PubgVariant) {
        // Handle open/launch click
        val success = packageChecker.launchPubgVariant(variant.id)

        if (success) {
            Toast.makeText(
                requireContext(),
                "Launching ${variant.name}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Failed to launch ${variant.name}. App may not be installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startDownload(variant: PubgVariant) {
        // Find and update the variant to show installing state
        val index = variants.indexOfFirst { it.id == variant.id }
        if (index != -1) {
            variants[index] = variant.copy(
                buttonState = PubgButtonState.INSTALLING,
                downloadProgress = 0
            )
            adapter.notifyItemChanged(index)

            // Simulate download progress (replace with actual download logic)
            simulateDownloadProgress(variant, index)
        }
    }

    private fun simulateDownloadProgress(variant: PubgVariant, index: Int) {
        // This is just for demonstration - replace with actual download implementation
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        var progress = 0
        
        val updateProgress = object : Runnable {
            override fun run() {
                progress += 10
                
                if (progress <= 100) {
                    variants[index] = variants[index].copy(downloadProgress = progress)
                    adapter.notifyItemChanged(index)
                    
                    if (progress < 100) {
                        handler.postDelayed(this, 500) // Update every 500ms
                    } else {
                        // Download completed - button state will be determined by version checking
                        variants[index] = variants[index].copy(
                            buttonState = PubgButtonState.OPEN, // Assume successful installation
                            downloadProgress = 100
                        )
                        adapter.notifyItemChanged(index)

                        Toast.makeText(
                            requireContext(),
                            "${variant.name} installed successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        
        handler.post(updateProgress)
    }

    /**
     * Handle permission request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionManager.handlePermissionResult(
            requestCode,
            permissions,
            grantResults,
            onAllGranted = {
                loadPubgVariants()
            },
            onDenied = { deniedPermissions ->
                Toast.makeText(
                    requireContext(),
                    "Permissions required: ${deniedPermissions.joinToString(", ")}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    /**
     * Handle activity results for Android 11+ permissions
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        permissionManager.handleActivityResult(
            requestCode,
            resultCode,
            onAllGranted = {
                loadPubgVariants()
            },
            onDenied = {
                Toast.makeText(
                    requireContext(),
                    "Storage permission is required to download PUBG variants",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    companion object {
        fun newInstance(): PubgVariantsFragment {
            return PubgVariantsFragment()
        }
    }
}
