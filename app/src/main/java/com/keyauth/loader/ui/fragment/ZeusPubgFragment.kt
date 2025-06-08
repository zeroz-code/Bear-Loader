package com.keyauth.loader.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keyauth.loader.R
import com.google.gson.Gson
import java.io.File
import java.io.InputStreamReader

/**
 * Zeus-style PUBG Variants Fragment with Bear Logo branding
 * Integrated into KeyAuth Loader navigation system
 */
class ZeusPubgFragment : Fragment() {

    private val REQUEST_PERMISSIONS_CODE = 1001
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_zeus_pubg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)

        // For testing: bypass permission check temporarily
        // TODO: Re-enable permission checking after testing
        loadPubgVariants()

        /*
        if (!hasStoragePermissions()) {
            requestStoragePermissions()
        } else {
            loadPubgVariants()
        }
        */
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_pubg_variants)
    }

    private fun loadPubgVariants() {
        try {
            val inputStream = requireContext().assets.open("pubg_manifest.json")
            val jsonText = InputStreamReader(inputStream).readText()
            val manifest = Gson().fromJson(jsonText, ZeusPubgManifest::class.java)

            val pubgList = manifest.variants.map { (key, info) ->
                val name = when (key) {
                    "GL" -> "PUBG MOBILE"
                    "KR" -> "PUBG MOBILE KR"
                    "TW" -> "PUBG MOBILE TW"
                    "VNG" -> "PUBG MOBILE VNG"
                    "BGMI" -> "BGMI"
                    else -> key
                }
                
                val iconRes = when (key) {
                    "GL" -> R.drawable.ic_pubg_gl
                    "KR" -> R.drawable.ic_pubg_kr
                    "TW" -> R.drawable.ic_pubg_tw
                    "VNG" -> R.drawable.ic_pubg_vng
                    "BGMI" -> R.drawable.battleground_mobile_india
                    else -> R.mipmap.ic_launcher
                }
                
                ZeusPubgVariant(
                    name = name,
                    version = manifest.version,
                    type = "Brutal",
                    size = info.size,
                    iconRes = iconRes,
                    downloadUrl = info.apk.url,
                    obbUrl = info.obb.url
                )
            }

            setupRecyclerView(pubgList)
            
            Toast.makeText(
                requireContext(),
                "Loaded ${pubgList.size} PUBG variants",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to load variants: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupRecyclerView(variants: List<ZeusPubgVariant>) {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ZeusPubgAdapter(variants)
    }

    private fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(), 
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Show instruction dialog first
            AlertDialog.Builder(requireContext())
                .setTitle("Storage Permission Required")
                .setMessage("To download PUBG variants, please:\n\n1. Enable 'Allow access to manage all files'\n2. Toggle the switch to ON\n3. Return to the app\n\nIf the toggle is disabled, try:\n• Restart your device\n• Clear app cache\n• Use alternative method")
                .setPositiveButton("Open Settings") { _, _ ->
                    openStorageSettings()
                }
                .setNegativeButton("Skip for Now") { _, _ ->
                    // Try to load variants anyway (might work on some devices)
                    loadPubgVariants()
                }
                .setNeutralButton("Alternative") { _, _ ->
                    // Use legacy permission as fallback
                    requestLegacyPermission()
                }
                .show()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSIONS_CODE
            )
        }
    }

    private fun openStorageSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivityForResult(intent, REQUEST_PERMISSIONS_CODE)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, REQUEST_PERMISSIONS_CODE)
            } catch (e2: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Cannot open settings. Please manually enable storage permission in Settings > Apps > BEAR-MOD > Permissions",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun requestLegacyPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSIONS_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (hasStoragePermissions()) {
                loadPubgVariants()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Storage permission is required to download PUBG variants",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPubgVariants()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Storage permission is required to download PUBG variants",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        fun newInstance(): ZeusPubgFragment {
            return ZeusPubgFragment()
        }
    }
}

/**
 * Simplified data models for Zeus-style implementation
 */
data class ZeusPubgManifest(
    val version: String,
    val build: String,
    val variants: Map<String, ZeusVariantInfo>
)

data class ZeusVariantInfo(
    val apk: ZeusDownloadInfo,
    val obb: ZeusDownloadInfo,
    val size: String
)

data class ZeusDownloadInfo(
    val name: String,
    val url: String,
    val sha256: String
)

data class ZeusPubgVariant(
    val name: String,
    val version: String,
    val type: String,
    val size: String,
    val iconRes: Int,
    val downloadUrl: String,
    val obbUrl: String
)

/**
 * RecyclerView Adapter for Zeus-style PUBG variants
 */
class ZeusPubgAdapter(private val items: List<ZeusPubgVariant>) : RecyclerView.Adapter<ZeusPubgViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZeusPubgViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pubg_variant_zeus, parent, false)
        return ZeusPubgViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZeusPubgViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

/**
 * ViewHolder for Zeus-style PUBG variants with enhanced download functionality
 */
class ZeusPubgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(variant: ZeusPubgVariant) {
        // Set PUBG icon
        itemView.findViewById<ImageView>(R.id.iv_pubg_icon).setImageResource(variant.iconRes)

        // Set variant information
        itemView.findViewById<TextView>(R.id.tv_pubg_name).text = variant.name
        itemView.findViewById<TextView>(R.id.tv_pubg_version).text = "Version: ${variant.version}"
        itemView.findViewById<TextView>(R.id.tv_pubg_size).text = "Download Size: ${variant.size}"

        // Set download button click listener
        itemView.findViewById<ImageButton>(R.id.btn_download).setOnClickListener {
            showDownloadConfirmation(variant)
        }
    }

    private fun showDownloadConfirmation(variant: ZeusPubgVariant) {
        AlertDialog.Builder(itemView.context)
            .setTitle("Download ${variant.name}")
            .setMessage("Do you want to download ${variant.name}?\n\nThis will download both APK and OBB files.")
            .setPositiveButton("Download") { _, _ ->
                startDownload(variant)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startDownload(variant: ZeusPubgVariant) {
        val context = itemView.context

        try {
            // Simple approach: Launch both downloads via browser
            // This avoids permission issues and works on all devices

            // Show download instructions
            AlertDialog.Builder(context)
                .setTitle("Download ${variant.name}")
                .setMessage("Two downloads will start:\n\n1. APK file (install this)\n2. OBB file (place in Android/obb/)\n\nBoth will open in your browser.")
                .setPositiveButton("Start Downloads") { _, _ ->

                    // Launch APK download
                    val apkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(variant.downloadUrl))
                    context.startActivity(apkIntent)

                    // Small delay then launch OBB download
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val obbIntent = Intent(Intent.ACTION_VIEW, Uri.parse(variant.obbUrl))
                        context.startActivity(obbIntent)
                    }, 2000) // 2 second delay

                    Toast.makeText(
                        context,
                        "Downloads started!\nAPK: Install after download\nOBB: Place in Android/obb/",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .setNegativeButton("Cancel", null)
                .show()

        } catch (e: Exception) {
            // Fallback: Just open APK download
            try {
                val apkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(variant.downloadUrl))
                context.startActivity(apkIntent)

                Toast.makeText(
                    context,
                    "APK download started. OBB download: ${variant.obbUrl}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e2: Exception) {
                Toast.makeText(
                    context,
                    "Download failed. APK: ${variant.downloadUrl}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
