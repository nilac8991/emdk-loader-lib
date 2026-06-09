package com.nilac.emdkloader.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object SignatureUtils {

    private const val TAG = "SignatureUtils"

    @OptIn(ExperimentalEncodingApi::class)
    fun getAppSigningCertificate(context: Context): String {
        val signatures = getSignatures(context)
        if (signatures.isEmpty()) {
            Log.e(TAG, "Unable to retrieve the signature")
            return ""
        }

        return Base64.encode(signatures.first().toHex()).also {
            Log.d(TAG, "APP Signature: $it")
        }
    }

    private fun getSignatures(context: Context): List<Signature> {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures?.toList().orEmpty()
            } else {
                val signingInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo ?: return emptyList()

                // With a rotated signing key apkContentsSigners holds the current signer(s);
                // otherwise the full history is the authoritative source.
                val signatures = if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
                signatures?.toList().orEmpty()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Unable to retrieve package signatures", e)
            emptyList()
        }
    }

    private fun Signature.toHex(): ByteArray {
        return this.toCharsString().chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}