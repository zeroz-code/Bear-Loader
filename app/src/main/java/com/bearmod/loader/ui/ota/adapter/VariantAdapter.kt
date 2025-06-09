package com.bearmod.loader.ui.ota.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bearmod.loader.R
import com.bearmod.loader.data.model.VariantItem
import com.bearmod.loader.databinding.ItemVariantBinding
import java.util.Locale

/**
 * Adapter for variant selection RecyclerView
 */
class VariantAdapter(
    private val onVariantSelected: (VariantItem) -> Unit
) : ListAdapter<VariantItem, VariantAdapter.VariantViewHolder>(VariantDiffCallback()) {
    
    private var selectedPosition = -1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val binding = ItemVariantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VariantViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        val variant = getItem(position)
        val isSelected = position == selectedPosition
        holder.bind(variant, isSelected) { selectedVariant ->
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val previousPosition = selectedPosition
                selectedPosition = currentPosition

                // Notify changes for selection state
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(selectedPosition)

                onVariantSelected(selectedVariant)
            }
        }
    }
    
    /**
     * Get the currently selected variant
     */
    fun getSelectedVariant(): VariantItem? {
        return if (selectedPosition != -1 && selectedPosition < itemCount) {
            getItem(selectedPosition)
        } else {
            null
        }
    }
    
    /**
     * ViewHolder for variant items
     */
    class VariantViewHolder(
        private val binding: ItemVariantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            variant: VariantItem,
            isSelected: Boolean,
            onItemClick: (VariantItem) -> Unit
        ) {
            binding.apply {
                // Set variant info
                tvVariantName.text = variant.displayName
                tvVariantDescription.text = variant.description
                
                // Set variant icon - Use official PUBG Mobile icon from Google Play Store for all variants
                val iconRes = when (variant.id) {
                    "GL" -> R.drawable.ic_pubg_mobile_official_new
                    "KR" -> R.drawable.ic_pubg_mobile_official_new
                    "TW" -> R.drawable.ic_pubg_mobile_official_new
                    "VNG" -> R.drawable.ic_pubg_mobile_official_new
                    "BGMI" -> R.drawable.ic_pubg_mobile_official_new
                    else -> R.drawable.ic_pubg_mobile_official_new
                }
                ivVariantIcon.setImageResource(iconRes)
                
                // Set selection state
                cardVariant.isChecked = isSelected
                radioButton.isChecked = isSelected
                
                // Set availability state
                cardVariant.isEnabled = variant.isAvailable
                tvVariantName.alpha = if (variant.isAvailable) 1.0f else 0.5f
                tvVariantDescription.alpha = if (variant.isAvailable) 1.0f else 0.5f
                ivVariantIcon.alpha = if (variant.isAvailable) 1.0f else 0.5f
                
                // Add file size info if available
                variant.variantInfo?.let { variantInfo ->
                    val apkSize = formatFileSize(variantInfo.apk.size ?: 0)
                    val obbSize = formatFileSize(variantInfo.obb.size ?: 0)
                    tvVariantSize.text = root.context.getString(R.string.variant_file_sizes, apkSize, obbSize)
                }
                
                // Set click listener
                root.setOnClickListener {
                    if (variant.isAvailable) {
                        onItemClick(variant)
                    }
                }
                
                cardVariant.setOnClickListener {
                    if (variant.isAvailable) {
                        onItemClick(variant)
                    }
                }
                
                radioButton.setOnClickListener {
                    if (variant.isAvailable) {
                        onItemClick(variant)
                    }
                }
            }
        }
        
        private fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return binding.root.context.getString(R.string.unknown_size)

            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0

            return when {
                gb >= 1 -> String.format(Locale.getDefault(), "%.2f GB", gb)
                mb >= 1 -> String.format(Locale.getDefault(), "%.2f MB", mb)
                kb >= 1 -> String.format(Locale.getDefault(), "%.2f KB", kb)
                else -> "$bytes B"
            }
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates
     */
    private class VariantDiffCallback : DiffUtil.ItemCallback<VariantItem>() {
        override fun areItemsTheSame(oldItem: VariantItem, newItem: VariantItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: VariantItem, newItem: VariantItem): Boolean {
            return oldItem == newItem
        }
    }
}
