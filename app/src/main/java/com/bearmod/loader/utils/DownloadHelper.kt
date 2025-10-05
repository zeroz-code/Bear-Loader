package com.bearmod.loader.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
/**
 * Small helper to enqueue APK and OBB downloads using platform DownloadManager when available.
 * No browser-based download logic is used here — downloads are enqueued via the system DownloadManager.
 */
object DownloadHelper {

    private const val TAG = "DownloadHelper"

    data class EnqueueResult(val apkId: Long?, val obbId: Long?)

    fun enqueueDownloads(
        context: Context,
        apkUrl: String?,
        obbUrl: String?,
        apkFileName: String,
        obbFileName: String
    ): EnqueueResult {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (dm != null) {
                var apkId: Long? = null
                var obbId: Long? = null

                apkUrl?.let { url ->
                    try {
                        val req = DownloadManager.Request(Uri.parse(url))
                            .setTitle(apkFileName)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ota_updates/$apkFileName")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        apkId = dm.enqueue(req)
                    } catch (e: Exception) {
                        Log.w(TAG, "APK enqueue failed: ${e.message}")
                        android.widget.Toast.makeText(context, "Failed to enqueue APK download", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                obbUrl?.let { url ->
                    try {
                        val req = DownloadManager.Request(Uri.parse(url))
                            .setTitle(obbFileName)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Android/obb/$obbFileName")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        obbId = dm.enqueue(req)
                    } catch (e: Exception) {
                        Log.w(TAG, "OBB enqueue failed: ${e.message}")
                        android.widget.Toast.makeText(context, "Failed to enqueue OBB download", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                return EnqueueResult(apkId, obbId)
            }
        } catch (e: Exception) {
            Log.w(TAG, "DownloadManager not available: ${e.message}")
            android.widget.Toast.makeText(context, "Download service unavailable on device", android.widget.Toast.LENGTH_LONG).show()
            return EnqueueResult(null, null)
        }
        // If we reach here, we returned from inside the dm != null block
        return EnqueueResult(null, null)
    }
    
}
