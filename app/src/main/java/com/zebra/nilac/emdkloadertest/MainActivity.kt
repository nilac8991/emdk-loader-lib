package com.zebra.nilac.emdkloadertest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.symbol.emdk.EMDKResults
import com.zebra.nilac.emdkloader.EMDKLoader
import com.zebra.nilac.emdkloader.ProfileLoader
import com.zebra.nilac.emdkloader.interfaces.EMDKManagerInitCallBack
import com.zebra.nilac.emdkloader.interfaces.ProfileLoaderResultCallback

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initEMDKManager()
    }

    private fun initEMDKManager() {
        //Initialising EMDK First...
        Log.i(TAG, "Initialising EMDK Manager")

        EMDKLoader.getInstance().initEMDKManager(this, object : EMDKManagerInitCallBack {
            override fun onFailed(message: String) {
                Log.e(TAG, "Failed to initialise EMDK Manager")
            }

            override fun onSuccess() {
                Log.i(TAG, "EMDK Manager was successfully initialised")
                setBrightnessLevel()
            }
        })
    }

    private fun setBrightnessLevel() {
        Toast.makeText(
            this@MainActivity,
            "Processing",
            Toast.LENGTH_SHORT
        ).show()

        ProfileLoader().processProfileNow(
            "BrightnessCheck",
            null,
            object : ProfileLoaderResultCallback {
                override fun onProfileLoadFailed(errorObject: EMDKResults) {
                    //Nothing to see here..
                }

                override fun onProfileLoadFailed(message: String) {
                    Log.e(TAG, "Failed to process brightness profile")
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to process brightness profile",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onProfileLoaded() {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Brightness level has been set",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}