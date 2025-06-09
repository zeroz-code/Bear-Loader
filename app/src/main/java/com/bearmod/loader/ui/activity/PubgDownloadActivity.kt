package com.bearmod.loader.ui.activity

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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bearmod.loader.R
import com.google.gson.Gson
import java.io.File
import java.io.InputStreamReader

/**
 * PUBG Mobile Variants Download Activity
 * Zeus-style implementation with Material Design 3 and Bear Logo branding
 * Integrated into KeyAuth Loader ecosystem
 */
class PubgDownloadActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS_CODE = 1001
    private lateinit var recyclerView: RecyclerView
    private lateinit var bearLogoImageView: ImageView
    private lateinit var titleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pubg_download)

        initializeViews()
        setupBearBranding()
        
        if (!hasStoragePermissions()) {
            requestStoragePermissions()
        } else {
            loadPubgVariants()
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rv_pubg_variants)
        bearLogoImageView = findViewById(R.id.iv_bear_logo)
        titleTextView = findViewById(R.id.tv_title)
    }

    private fun setupBearBranding() {
        // Set Bear Logo
        bearLogoImageView.setImageResource(R.drawable.bear_logo)

        // Set title without Bear branding
        titleTextView.text = "PUBG Mobile Variants"
    }

    private fun loadPubgVariants() {
        try {
            val inputStream = assets.open("pubg_manifest.json")
            val jsonText = InputStreamReader(inputStream).readText()
            val manifest = Gson().fromJson(jsonText, PubgManifest::class.java)

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
                
                PubgVariant(
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
                this,
                "Loaded ${pubgList.size} PUBG variants",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to load variants: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupRecyclerView(variants: List<PubgVariant>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PubgAdapter(variants)
    }

    private fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivityForResult(intent, REQUEST_PERMISSIONS_CODE)
            } catch (e: Exception) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, REQUEST_PERMISSIONS_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSIONS_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (hasStoragePermissions()) {
                loadPubgVariants()
            } else {
                Toast.makeText(
                    this,
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
                    this,
                    "Storage permission is required to download PUBG variants",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

/**
 * Simplified data models for Zeus-style implementation
 */
data class PubgManifest(
    val version: String,
    val build: String,
    val variants: Map<String, VariantInfo>
)

data class VariantInfo(
    val apk: DownloadInfo,
    val obb: DownloadInfo,
    val size: String
)

data class DownloadInfo(
    val name: String,
    val url: String,
    val sha256: String
)

data class PubgVariant(
    val name: String,
    val version: String,
    val type: String,
    val size: String,
    val iconRes: Int,
    val downloadUrl: String,
    val obbUrl: String
)

/**
 * RecyclerView Adapter for PUBG variants with Material Design 3 styling
 */
class PubgAdapter(private val items: List<PubgVariant>) : RecyclerView.Adapter<PubgViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PubgViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pubg_variant_zeus, parent, false)
        return PubgViewHolder(view)
    }

    override fun onBindViewHolder(holder: PubgViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

/**
 * ViewHolder for PUBG variants with enhanced download functionality
 */
class PubgViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {

    fun bind(variant: PubgVariant) {
        // Set PUBG icon
        itemView.findViewById<ImageView>(R.id.iv_pubg_icon).setImageResource(variant.iconRes)

        // Set variant information
        itemView.findViewById<TextView>(R.id.tv_pubg_name).text = variant.name
        itemView.findViewById<TextView>(R.id.tv_pubg_version).text = "Version: ${variant.version}"
        itemView.findViewById<TextView>(R.id.tv_pubg_size).text = "Download Size: ${variant.size}"

        // Set download button click listener
        itemView.findViewById<android.widget.ImageButton>(R.id.btn_download).setOnClickListener {
            showDownloadConfirmation(variant)
        }
    }

    private fun showDownloadConfirmation(variant: PubgVariant) {
        AlertDialog.Builder(itemView.context)
            .setTitle("Download ${variant.name}")
            .setMessage("Do you want to download ${variant.name}?\n\nThis will download both APK and OBB files.")
            .setPositiveButton("Download") { _, _ ->
                startDownload(variant)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startDownload(variant: PubgVariant) {
        val context = itemView.context
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        try {
            // Download OBB file using DownloadManager
            val obbFileName = variant.obbUrl.substringAfterLast('/')
            val obbFile = File(Environment.getExternalStorageDirectory(), "Android/obb/$obbFileName")

            // Create directory if it doesn't exist
            if (!obbFile.parentFile?.exists()!!) {
                obbFile.parentFile?.mkdirs()
            }

            val obbRequest = DownloadManager.Request(Uri.parse(variant.obbUrl)).apply {
                setTitle("Downloading ${variant.name} OBB")
                setDescription("Downloading game data files...")
                setDestinationUri(Uri.fromFile(obbFile))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadId = downloadManager.enqueue(obbRequest)

            // Launch APK download via browser
            val apkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(variant.downloadUrl))
            context.startActivity(apkIntent)

            Toast.makeText(
                context,
                "Started downloading ${variant.name}\nOBB: Background download\nAPK: Browser download",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Download failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
