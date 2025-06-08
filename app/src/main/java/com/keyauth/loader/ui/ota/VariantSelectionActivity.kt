package com.keyauth.loader.ui.ota

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.keyauth.loader.R
import com.keyauth.loader.data.model.OTAUpdateState
import com.keyauth.loader.databinding.ActivityVariantSelectionBinding
import com.keyauth.loader.network.NetworkFactory
import com.keyauth.loader.ui.ota.adapter.VariantAdapter
import com.keyauth.loader.utils.APKInstaller
import kotlinx.coroutines.launch

/**
 * Activity for selecting variant to download
 */
class VariantSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVariantSelectionBinding
    private lateinit var variantAdapter: VariantAdapter
    
    private val viewModel: OTAViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val otaRepository = NetworkFactory.createOTARepository(this@VariantSelectionActivity)
                val apkInstaller = APKInstaller(this@VariantSelectionActivity)
                return OTAViewModel(otaRepository, apkInstaller) as T
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVariantSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupRecyclerView()
        setupObservers()
    }
    
    private fun setupUI() {
        // Back button
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Continue button
        binding.btnContinue.setOnClickListener {
            val selectedVariant = variantAdapter.getSelectedVariant()
            if (selectedVariant != null) {
                viewModel.selectVariant(selectedVariant.id)
            }
        }
    }
    
    private fun setupRecyclerView() {
        variantAdapter = VariantAdapter { variant ->
            // Update continue button state
            binding.btnContinue.isEnabled = true
            binding.btnContinue.text = getString(R.string.download_variant, variant.displayName)
        }
        
        binding.recyclerViewVariants.apply {
            layoutManager = LinearLayoutManager(this@VariantSelectionActivity)
            adapter = variantAdapter
        }
        
        // Load available variants
        val variants = viewModel.getAvailableVariants()
        variantAdapter.submitList(variants)
        
        // Update UI based on available variants
        if (variants.isEmpty()) {
            showNoVariantsState()
        } else {
            showVariantsState()
        }
    }
    
    private fun setupObservers() {
        // Observe update state
        lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                when (state) {
                    is OTAUpdateState.Downloading -> {
                        navigateToDownload()
                    }
                    is OTAUpdateState.Error -> {
                        showError(state.message)
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
        
        // Observe loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnContinue.isEnabled = !isLoading && variantAdapter.getSelectedVariant() != null
            }
        }
    }
    
    private fun showVariantsState() {
        binding.apply {
            recyclerViewVariants.visibility = View.VISIBLE
            tvNoVariants.visibility = View.GONE
            btnContinue.visibility = View.VISIBLE
        }
    }
    
    private fun showNoVariantsState() {
        binding.apply {
            recyclerViewVariants.visibility = View.GONE
            tvNoVariants.visibility = View.VISIBLE
            btnContinue.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun navigateToDownload() {
        val intent = Intent(this, DownloadActivity::class.java)
        startActivity(intent)
        finish()
    }
}
