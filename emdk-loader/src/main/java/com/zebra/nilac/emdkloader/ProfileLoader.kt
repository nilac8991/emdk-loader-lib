package com.zebra.nilac.emdkloader

import android.util.Log
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.ProfileManager
import com.zebra.nilac.emdkloader.interfaces.ProfileLoaderResultCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ProfileLoader {

    private var mProfileLoaderResultCallback: ProfileLoaderResultCallback? = null
    private var mEmdkLoaderInstance: EMDKLoader = EMDKLoader.getInstance()

    private var profileProcessScope = MainScope()

    private var mEmdkManager: EMDKManager? = null

    fun processProfile(
        profileName: String,
        profile: String?,
        callBacks: ProfileLoaderResultCallback
    ) {
        this.mProfileLoaderResultCallback = callBacks
        mEmdkManager = mEmdkLoaderInstance.getManager()

        if (mEmdkManager == null) {
            Log.e(TAG, "Unable to process profile, EMDK Manager was not initialised!")
            mProfileLoaderResultCallback?.onProfileLoadFailed("EMDK not initialised!")
            return
        }

        Log.i(TAG, "Applying profile...")
        val profileManager =
            mEmdkManager?.getInstance(EMDKManager.FEATURE_TYPE.PROFILE) as ProfileManager?

        if (profileManager == null) {
            Log.e(TAG, "Profile Manager is not available!")
            return
        }

        Log.d(TAG, "Processing EMDK profile")
        val params = arrayOfNulls<String>(1)

        if (!profile.isNullOrEmpty()) {
            params[0] = profile
        }

        profileProcessScope.launch(Dispatchers.IO) {
            profileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, params)
                .also {
                    Log.d(TAG, "XML: " + it.statusString)

                    if (it.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {
                        mProfileLoaderResultCallback?.onProfileLoaded()
                    } else if (it.statusCode == EMDKResults.STATUS_CODE.FAILURE || it.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
                        mProfileLoaderResultCallback?.onProfileLoadFailed(it.statusString)
                    }
                }
        }
    }

    companion object {
        const val TAG = "ProfileLoader"
    }
}