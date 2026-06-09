package com.nilac.emdkloader.interfaces

interface EMDKManagerInitCallBack {
    fun onSuccess()

    fun onFailed(message: String)
}