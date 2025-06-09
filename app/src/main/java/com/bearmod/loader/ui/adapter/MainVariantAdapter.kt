package com.bearmod.loader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bearmod.loader.R
import com.bearmod.loader.data.model.VariantItem
import com.bearmod.loader.databinding.ItemVariantMainBinding

/**
 * Adapter for displaying variants on the main screen
 */
class MainVariantAdapter(
    private val onDownloadClick: (VariantItem) -> Unit
) : ListAdapter<VariantItem, MainVariantAdapter.VariantViewHolder>(VariantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val binding = ItemVariantMainBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VariantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VariantViewHolder(
        private val binding: ItemVariantMainBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(variant: VariantItem) {
            binding.apply {
                val context = binding.root.context

                // Set PUBG Mobile variant info
                tvVariantName.text = getPubgVariantName(context, variant.id)
                tvVariantDescription.text = getPubgVariantDescription(context, variant.id)

                // Set PUBG variant icon
                ivVariantFlag.setImageResource(getPubgVariantIcon(variant.id))

                // Show NEW badge for newly added variants
                if (isNewVariant(variant.id)) {
                    tvNewBadge.visibility = android.view.View.VISIBLE
                    tvNewBadge.text = context.getString(R.string.new_variant)
                } else {
                    tvNewBadge.visibility = android.view.View.GONE
                }

                // Set file sizes with proper localization
                if (variant.variantInfo != null) {
                    val apkSize = formatFileSize(variant.variantInfo.apk.size ?: 0)
                    val obbSize = formatFileSize(variant.variantInfo.obb.size ?: 0)
                    tvVariantSize.text = context.getString(R.string.variant_file_sizes, apkSize, obbSize)
                } else {
                    tvVariantSize.text = context.getString(R.string.file_size_unknown)
                }

                // Determine installation status and show appropriate buttons
                val isInstalled = checkIfVariantInstalled(variant.id)
                val hasUpdate = checkIfUpdateAvailable(variant.id)

                when {
                    !isInstalled -> {
                        // Show download button only
                        btnDownload.visibility = android.view.View.VISIBLE
                        btnUpdate.visibility = android.view.View.GONE
                        btnDownload.text = context.getString(R.string.download)
                        btnDownload.setIconResource(R.drawable.ic_download)
                        tvVersionStatus.visibility = android.view.View.VISIBLE
                        tvVersionStatus.text = context.getString(R.string.version_not_installed)
                        tvVersionStatus.setBackgroundColor(context.getColor(R.color.warning_yellow))
                    }
                    hasUpdate -> {
                        // Show both download and update buttons
                        btnDownload.visibility = android.view.View.VISIBLE
                        btnUpdate.visibility = android.view.View.VISIBLE
                        btnDownload.text = context.getString(R.string.download)
                        btnUpdate.text = context.getString(R.string.update)
                        btnUpdate.setIconResource(R.drawable.ic_system_update)
                        tvVersionStatus.visibility = android.view.View.VISIBLE
                        tvVersionStatus.text = context.getString(R.string.version_outdated)
                        tvVersionStatus.setBackgroundColor(context.getColor(R.color.accent_orange))
                    }
                    else -> {
                        // Show download button only (latest version installed)
                        btnDownload.visibility = android.view.View.VISIBLE
                        btnUpdate.visibility = android.view.View.GONE
                        btnDownload.text = context.getString(R.string.install)
                        btnDownload.setIconResource(R.drawable.ic_install)
                        tvVersionStatus.visibility = android.view.View.VISIBLE
                        tvVersionStatus.text = context.getString(R.string.version_latest)
                        tvVersionStatus.setBackgroundColor(context.getColor(R.color.success_green))
                    }
                }

                // Set button click listeners
                btnDownload.setOnClickListener {
                    onDownloadClick(variant)
                }

                btnUpdate.setOnClickListener {
                    onDownloadClick(variant)
                }

                // Set card click listener (same as download button)
                cardVariant.setOnClickListener {
                    onDownloadClick(variant)
                }
            }
        }

        private fun getPubgVariantIcon(variantId: String): Int {
            // Use official PUBG Mobile icon from Google Play Store for all variants
            // This provides consistent branding across all versions with authentic design
            return when (variantId) {
                "GL" -> R.drawable.ic_pubg_mobile_official_new
                "KR" -> R.drawable.ic_pubg_mobile_official_new
                "TW" -> R.drawable.ic_pubg_mobile_official_new
                "VNG" -> R.drawable.ic_pubg_mobile_official_new
                "BGMI" -> R.drawable.ic_pubg_mobile_official_new
                else -> R.drawable.ic_pubg_mobile_official_new
            }
        }

        private fun getPubgVariantName(context: android.content.Context, variantId: String): String {
            return when (variantId) {
                "GL" -> context.getString(R.string.pubg_mobile_global)
                "KR" -> context.getString(R.string.pubg_mobile_korea)
                "TW" -> context.getString(R.string.pubg_mobile_taiwan)
                "VNG" -> context.getString(R.string.pubg_mobile_vietnam)
                "BGMI" -> context.getString(R.string.pubg_mobile_india)
                else -> context.getString(R.string.pubg_mobile_global)
            }
        }

        private fun getPubgVariantDescription(context: android.content.Context, variantId: String): String {
            return when (variantId) {
                "GL" -> context.getString(R.string.pubg_global_description)
                "KR" -> context.getString(R.string.pubg_korea_description)
                "TW" -> context.getString(R.string.pubg_taiwan_description)
                "VNG" -> context.getString(R.string.pubg_vietnam_description)
                "BGMI" -> context.getString(R.string.pubg_india_description)
                else -> context.getString(R.string.pubg_global_description)
            }
        }

        private fun checkIfVariantInstalled(variantId: String): Boolean {
            // TODO: Implement actual installation check logic
            // For now, simulate some variants as installed
            return when (variantId) {
                "GL" -> true   // Global is installed
                "IN" -> true   // BGMI is installed
                "KR" -> false  // Korea not installed
                "TW" -> false  // Taiwan not installed
                "VNG" -> false // Vietnam not installed
                "JP" -> false  // Japan not installed
                "ME" -> false  // Middle East not installed
                else -> false
            }
        }

        private fun checkIfUpdateAvailable(variantId: String): Boolean {
            // TODO: Implement actual update check logic
            // For now, simulate update availability
            return when (variantId) {
                "GL" -> false  // Global is up to date
                "IN" -> true   // BGMI has update available
                "TW" -> true   // Taiwan has update
                "KR" -> false  // Korea is up to date
                "VNG" -> false // Vietnam is up to date
                "JP" -> false  // Japan is up to date
                "ME" -> false  // Middle East is up to date
                else -> false
            }
        }

        private fun isNewVariant(variantId: String): Boolean {
            // Mark newly added variants as "NEW"
            return when (variantId) {
                "IN", "JP", "ME" -> true  // New variants
                else -> false
            }
        }

        private fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "Unknown"

            val units = arrayOf("B", "KB", "MB", "GB")
            var size = bytes.toDouble()
            var unitIndex = 0

            while (size >= 1024 && unitIndex < units.size - 1) {
                size /= 1024
                unitIndex++
            }

            return if (size >= 100) {
                "${size.toInt()} ${units[unitIndex]}"
            } else {
                "%.1f %s".format(size, units[unitIndex])
            }
        }
    }

    private class VariantDiffCallback : DiffUtil.ItemCallback<VariantItem>() {
        override fun areItemsTheSame(oldItem: VariantItem, newItem: VariantItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VariantItem, newItem: VariantItem): Boolean {
            return oldItem == newItem
        }
    }
}
