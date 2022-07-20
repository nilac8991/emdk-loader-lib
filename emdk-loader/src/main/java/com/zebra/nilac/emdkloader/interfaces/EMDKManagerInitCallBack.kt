package com.zebra.nilac.emdkloader.interfaces

interface EMDKManagerInitCallBack {
    fun onSuccess()

    fun onFailed(message: String)
}