package com.bearmod.loader.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bearmod.loader.network.NetworkFactory

object OTAInterop {

    interface Callback {
        fun onResult(result: com.bearmod.loader.utils.NetworkResult<com.bearmod.loader.data.model.OTAResponse>)
    }

    @JvmStatic
    fun runCheckForUpdates(context: Context, callback: Callback) {
        val otaRepo = NetworkFactory.createOTARepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                otaRepo.checkForUpdates()
            } catch (t: Throwable) {
                com.bearmod.loader.utils.NetworkResult.Error("${t.message}")
            }

            withContext(Dispatchers.Main) {
                callback.onResult(result)
            }
        }
    }
}
