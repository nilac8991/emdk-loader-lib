package com.zebra.nilac.emdkloader

import android.content.Context
import android.util.Log
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.EMDKResults.STATUS_CODE
import com.symbol.emdk.ProfileManager
import com.symbol.emdk.ProfileManager.PROFILE_FLAG
import com.zebra.nilac.emdkloader.interfaces.EMDKManagerInitCallBack
import kotlinx.coroutines.*

class EMDKLoader {

    private var mEmdkManager: EMDKManager? = null
    private var mEMDKManagerInitCallback: EMDKManagerInitCallBack? = null

    private var initScope = MainScope()

    fun initEMDKManager(context: Context) {
        initEMDKManager(context, null)
    }

    fun initEMDKManager(context: Context, emdkManagerInitCallBacks: EMDKManagerInitCallBack?) {
        Log.i(TAG, "Initialising EMDK Manager asynchronously")
        mEMDKManagerInitCallback = emdkManagerInitCallBacks

        initScope.launch(Dispatchers.IO) {
            EMDKManager.getEMDKManager(context, object : EMDKManager.EMDKListener {
                override fun onOpened(manager: EMDKManager?) {
                    Log.i(TAG, "EMDK opened with manager: $manager")
                    mEmdkManager = manager

                    mEMDKManagerInitCallback?.onSuccess()
                }

                override fun onClosed() {
                    Log.w(TAG, "EMDK Manager was closed")
                }
            }).also {
                if (it.statusCode !== EMDKResults.STATUS_CODE.SUCCESS) {
                    Log.e(TAG, "Failed to init: " + it.statusCode)

                    mEMDKManagerInitCallback?.onFailed(it.statusString)
                }
            }
        }
    }

    fun getManager(): EMDKManager? {
        return mEmdkManager
    }

    fun isManagerInit(): Boolean {
        return mEmdkManager != null
    }

    fun release() {
        Log.w(TAG, "About to release the EMDK Manager")
        mEmdkManager?.release()
    }

    companion object {
        private const val TAG = "EMDKLoader"

        @Volatile
        private var INSTANCE: EMDKLoader? = null

        fun getInstance(): EMDKLoader {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = EMDKLoader()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}