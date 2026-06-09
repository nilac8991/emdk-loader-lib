package com.nilac.emdkloader.utils

import android.util.Log
import com.nilac.emdkloader.models.ProfileError
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Parses the XML status response embedded in the [com.symbol.emdk.EMDKResults] returned by
 * `ProfileManager.processProfile()`.
 *
 * See: https://techdocs.zebra.com/emdk-for-android/latest/guide/xmlresponseguide/
 */
object ProfileResponseParser {

    private const val TAG = "ProfileResponseParser"

    private const val TAG_PARM_ERROR = "parm-error"
    private const val TAG_CHARACTERISTIC_ERROR = "characteristic-error"

    /**
     * Returns the list of errors found in [statusXml]. An empty list means the profile was
     * applied successfully (or there was nothing to parse).
     */
    fun parseErrors(statusXml: String?): List<ProfileError> {
        if (statusXml.isNullOrBlank()) return emptyList()

        val errors = mutableListOf<ProfileError>()
        try {
            val parser = XmlPullParserFactory.newInstance()
                .apply { isNamespaceAware = true }
                .newPullParser()
            parser.setInput(StringReader(statusXml))

            var event = parser.eventType

            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        TAG_PARM_ERROR -> errors.add(
                            ProfileError(
                                parmName = parser.getAttributeValue(null, "name"),
                                description = parser.getAttributeValue(null, "desc"),
                            )
                        )

                        TAG_CHARACTERISTIC_ERROR -> errors.add(
                            ProfileError(
                                characteristicType = parser.getAttributeValue(null, "type"),
                                description = parser.getAttributeValue(null, "desc"),
                            )
                        )
                    }
                }
                event = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse MX response XML", e)
            return listOf(ProfileError(description = "Failed to parse MX response: ${e.message}"))
        }

        return errors
    }

    /** Builds a single human-readable message from a list of [errors]. */
    fun buildFailureMessage(errors: List<ProfileError>): String {
        if (errors.isEmpty()) return "Profile processing failed with an unknown error"
        return errors.joinToString(separator = "\n") { it.toString() }
    }
}
