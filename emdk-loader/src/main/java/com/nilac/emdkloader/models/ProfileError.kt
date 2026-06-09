package com.nilac.emdkloader.models

/**
 * A single error extracted from the XML status response returned by
 * [com.symbol.emdk.ProfileManager.processProfile].
 *
 * MX reports two kinds of errors in the response document:
 *  - a `<parm-error>` element, identified by its [parmName] (a parameter-level failure), and
 *  - a `<characteristic-error>` element, identified by its [characteristicType] (a feature-level failure).
 *
 * Exactly one of [parmName] / [characteristicType] is populated for a given error.
 */
data class ProfileError(
    val characteristicType: String? = null,
    val parmName: String? = null,
    val description: String? = null,
) {
    override fun toString(): String {
        val detail = description ?: "unknown error"
        return when {
            parmName != null -> "Parameter '$parmName' failed: $detail"
            characteristicType != null -> "Characteristic '$characteristicType' failed: $detail"
            else -> detail
        }
    }
}
