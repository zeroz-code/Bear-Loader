package com.bearmod.loader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bearmod.loader.R
import com.bearmod.loader.data.model.PubgButtonState
import com.bearmod.loader.data.model.PubgVariant
import com.bearmod.loader.utils.PackageVersionChecker

/**
 * RecyclerView adapter for PUBG Mobile variants
 * Implements ZEUS God Aim UI style with smart download/update/open buttons
 * Features version checking and dynamic button states
 */
class PubgVariantAdapter(
    private var variants: List<PubgVariant>,
    private val onDownloadClick: (PubgVariant) -> Unit,
    private val onUpdateClick: (PubgVariant) -> Unit,
    private val onOpenClick: (PubgVariant) -> Unit
) : RecyclerView.Adapter<PubgVariantAdapter.PubgVariantViewHolder>() {

    private lateinit var packageChecker: PackageVersionChecker

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PubgVariantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pubg_variant, parent, false)

        // Initialize package checker if not already done
        if (!::packageChecker.isInitialized) {
            packageChecker = PackageVersionChecker(parent.context)
        }

        return PubgVariantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PubgVariantViewHolder, position: Int) {
        val variant = variants[position]
        val updatedVariant = updateVariantState(variant)
        holder.bind(updatedVariant)
    }

    override fun getItemCount(): Int {
        android.util.Log.d("PubgVariantAdapter", "getItemCount() returning ${variants.size}")
        return variants.size
    }

    fun updateVariants(newVariants: List<PubgVariant>) {
        android.util.Log.d("PubgVariantAdapter", "updateVariants called with ${newVariants.size} variants")
        variants = newVariants
        notifyDataSetChanged()
        android.util.Log.d("PubgVariantAdapter", "notifyDataSetChanged() called")
    }

    /**
     * Update variant state based on installed package information
     */
    private fun updateVariantState(variant: PubgVariant): PubgVariant {
        if (!::packageChecker.isInitialized) return variant

        val packageInfo = packageChecker.getPubgVariantInfo(variant.id)

        return when {
            packageInfo?.isInstalled != true -> {
                // Not installed - show DOWNLOAD
                variant.copy(
                    buttonState = PubgButtonState.DOWNLOAD,
                    installedVersion = null
                )
            }
            packageChecker.isUpdateAvailable(variant.packageName, variant.version) -> {
                // Older version installed - show UPDATE
                variant.copy(
                    buttonState = PubgButtonState.UPDATE,
                    installedVersion = packageInfo.installedVersion
                )
            }
            else -> {
                // Current or newer version installed - show OPEN
                variant.copy(
                    buttonState = PubgButtonState.OPEN,
                    installedVersion = packageInfo.installedVersion
                )
            }
        }
    }

    inner class PubgVariantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_pubg_icon)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_pubg_name)
        private val versionTextView: TextView = itemView.findViewById(R.id.tv_pubg_version)
        private val sizeTextView: TextView = itemView.findViewById(R.id.tv_pubg_size)
        private val downloadButton: Button = itemView.findViewById(R.id.btn_download)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)

        fun bind(variant: PubgVariant) {
            // Set PUBG icon
            iconImageView.setImageResource(variant.iconRes)

            // Set variant information
            nameTextView.text = variant.name
            versionTextView.text = buildVersionText(variant)
            sizeTextView.text = "Download Size: ${variant.size}"

            // Configure button based on state
            configureButton(variant)
        }

        private fun buildVersionText(variant: PubgVariant): String {
            return when (variant.buttonState) {
                PubgButtonState.UPDATE -> {
                    "Version: ${variant.version} (Installed: ${variant.installedVersion})"
                }
                PubgButtonState.OPEN -> {
                    "Version: ${variant.installedVersion ?: variant.version} (Installed)"
                }
                else -> {
                    "Version: ${variant.version}"
                }
            }
        }

        private fun configureButton(variant: PubgVariant) {
            when (variant.buttonState) {
                PubgButtonState.DOWNLOAD -> {
                    downloadButton.text = "" // Icon-only button
                    downloadButton.background = ContextCompat.getDrawable(
                        itemView.context, R.drawable.button_transparent
                    )
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_download_simple, 0, 0, 0
                    )
                    downloadButton.isEnabled = true
                    downloadButton.setOnClickListener { onDownloadClick(variant) }
                    progressBar.visibility = View.GONE
                }

                PubgButtonState.UPDATE -> {
                    downloadButton.text = "" // Icon-only button
                    downloadButton.background = ContextCompat.getDrawable(
                        itemView.context, R.drawable.button_transparent
                    )
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_download_simple, 0, 0, 0
                    )
                    downloadButton.isEnabled = true
                    downloadButton.setOnClickListener { onUpdateClick(variant) }
                    progressBar.visibility = View.GONE
                }

                PubgButtonState.OPEN -> {
                    downloadButton.text = "" // Icon-only button
                    downloadButton.background = ContextCompat.getDrawable(
                        itemView.context, R.drawable.button_transparent
                    )
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_download_simple, 0, 0, 0
                    )
                    downloadButton.isEnabled = true
                    downloadButton.setOnClickListener { onOpenClick(variant) }
                    progressBar.visibility = View.GONE
                }

                PubgButtonState.INSTALLING -> {
                    downloadButton.text = "" // Icon-only button
                    downloadButton.background = ContextCompat.getDrawable(
                        itemView.context, R.drawable.button_transparent
                    )
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_download_simple, 0, 0, 0
                    )
                    downloadButton.isEnabled = false
                    downloadButton.setOnClickListener(null)
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = variant.downloadProgress
                }
            }
        }
    }
}
