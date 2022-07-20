package com.zebra.nilac.emdkloader.interfaces

interface ProfileLoaderResultCallback {
    fun onProfileLoaded()

    fun onProfileLoadFailed(message: String)
}