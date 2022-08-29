package com.zebra.nilac.emdkloader.interfaces

import com.symbol.emdk.EMDKResults

interface ProfileLoaderResultCallback {
    fun onProfileLoaded()

    fun onProfileLoadFailed(message: String)

    fun onProfileLoadFailed(errorObject: EMDKResults)
}