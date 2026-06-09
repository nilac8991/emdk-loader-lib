package com.nilac.emdkloader.interfaces

import com.nilac.emdkloader.models.ProfileError

interface ProfileLoaderResultCallback {

    /** Called once when the profile has been applied successfully. */
    fun onProfileLoaded()

    /**
     * Called once when the profile failed to apply.
     *
     * @param message summary of the failure.
     * @param errors  the structured errors parsed from the MX response, when available.
     *                Empty for failures that occur before processing.
     */
    fun onProfileLoadFailed(message: String, errors: List<ProfileError> = emptyList())
}
