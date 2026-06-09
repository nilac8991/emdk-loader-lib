package com.nilac.emdkloader

import android.util.Log
import com.nilac.emdkloader.interfaces.ProfileLoaderResultCallback
import com.nilac.emdkloader.models.ProfileError
import com.nilac.emdkloader.utils.ProfileResponseParser
import com.symbol.emdk.EMDKBase
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.ProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileLoader {

    private val mEmdkLoaderInstance: EMDKLoader = EMDKLoader.getInstance()
    private val processScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun processProfileNow(
        profileName: String,
        profile: String?,
        callBacks: ProfileLoaderResultCallback
    ) = processProfile(profileName, profile, 0L, callBacks)

    fun processProfile(
        profileName: String,
        profile: String?,
        callBacks: ProfileLoaderResultCallback
    ) = processProfile(profileName, profile, 0L, callBacks)

    fun processProfileWithDelay(
        profileName: String,
        profile: String?,
        delay: Long?,
        callBacks: ProfileLoaderResultCallback
    ) = processProfile(profileName, profile, delay ?: 0L, callBacks)

    private fun processProfile(
        profileName: String,
        profile: String?,
        delayMillis: Long,
        callBacks: ProfileLoaderResultCallback
    ) {
        val manager = mEmdkLoaderInstance.getManager()
        if (manager == null) {
            Log.e(TAG, "Unable to process profile, EMDK Manager was not initialised!")
            callBacks.onProfileLoadFailed("EMDK Manager is not initialised")
            return
        }

        Log.i(TAG, "Requesting ProfileManager asynchronously...")
        manager.getInstanceAsync(
            EMDKManager.FEATURE_TYPE.PROFILE,
            object : EMDKManager.StatusListener {
                override fun onStatus(statusData: EMDKManager.StatusData, emdkBase: EMDKBase) {
                    if (statusData.featureType != EMDKManager.FEATURE_TYPE.PROFILE) return

                    Log.d(TAG, "ProfileManager status: ${statusData.result}")

                    if (statusData.result != EMDKResults.STATUS_CODE.SUCCESS) {
                        Log.e(TAG, "Profile Manager is not available: ${statusData.result}")
                        callBacks.onProfileLoadFailed("Profile Manager is not available (${statusData.result})")
                        return
                    }

                    applyProfile(emdkBase as ProfileManager, profileName, profile, delayMillis, callBacks)
                }
            }
        )
    }

    private fun applyProfile(
        profileManager: ProfileManager,
        profileName: String,
        profile: String?,
        delayMillis: Long,
        callBacks: ProfileLoaderResultCallback
    ) {
        val params = arrayOfNulls<String>(1)
        if (!profile.isNullOrEmpty()) {
            params[0] = profile
        }

        processScope.launch {
            if (delayMillis > 0) {
                Log.d(TAG, "Waiting ${delayMillis}ms before processing profile")
                delay(delayMillis)
            }

            Log.d(TAG, "Processing MX profile '$profileName'")
            val results =
                profileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, params)
            handleResults(results, callBacks)
        }
    }

    private fun handleResults(results: EMDKResults, callBacks: ProfileLoaderResultCallback) {
        when (results.statusCode) {
            EMDKResults.STATUS_CODE.SUCCESS -> {
                Log.i(TAG, "Profile applied successfully")
                callBacks.onProfileLoaded()
            }

            EMDKResults.STATUS_CODE.CHECK_XML -> {
                val errors = ProfileResponseParser.parseErrors(results.statusString)
                if (errors.isEmpty()) {
                    Log.i(TAG, "Profile applied successfully")
                    callBacks.onProfileLoaded()
                } else {
                    val message = ProfileResponseParser.buildFailureMessage(errors)
                    Log.e(TAG, "Profile applied with ${errors.size} error(s):\n$message")
                    callBacks.onProfileLoadFailed(message, errors)
                }
            }

            else -> {
                Log.e(TAG, "Profile processing failed: ${results.statusString}")
                callBacks.onProfileLoadFailed(
                    results.statusString ?: "Profile processing failed (${results.statusCode})"
                )
            }
        }
    }

    companion object {
        const val TAG = "ProfileLoader"
    }
}
