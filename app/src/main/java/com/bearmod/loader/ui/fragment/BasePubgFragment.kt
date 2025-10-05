package com.bearmod.loader.ui.fragment

import android.content.Intent
import androidx.fragment.app.Fragment
import com.bearmod.loader.utils.PermissionManager

/**
 * Minimal base fragment for PUBG fragments. Provides permission helpers and small utilities.
 * Keep this lightweight to avoid touching core auth or app flows.
 */
open class BasePubgFragment : Fragment() {

    private var permissionManager: PermissionManager? = null

    fun getPermissionManager(): PermissionManager {
        if (permissionManager == null) permissionManager = PermissionManager(requireContext())
        return permissionManager!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Subclasses can override; keep default behavior
    }
}
