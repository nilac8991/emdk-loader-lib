package com.zebra.nilac.emdkloader.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.sign

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
        return if (Build.VERSION.SDK_INT < 28) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            ).signatures.toList()
        } else {
            val signingInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            ).signingInfo
            signingInfo.apkContentsSigners.toList()
        }
    }

    private fun Signature.toHex(): ByteArray {
        return this.toCharsString().chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}