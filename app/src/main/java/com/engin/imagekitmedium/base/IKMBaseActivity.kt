package com.engin.imagekitmedium.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.engin.imagekitmedium.utils.Constants
import org.json.JSONException
import org.json.JSONObject

abstract class IKMBaseActivity : AppCompatActivity() {
    protected var projectDetailString =
        "{\"projectId\":\"736430079244660731\",\"appId\":\"103016511\",\"authApiKey\":\"CgB6e3x9ZHxBDSUl7OehcDajdvJ537nP5bWuOPsW50rgjeIa5JHf1j1cVmfDcxOtrV1OSs3C7ZvDQBE+7emkedAy\",\"clientSecret\":\"8F7A6631E56C5A5A82F45D818D637CDFF93AF891877274AA659C3BF92CED95F2\",\"clientId\":\"469387083497080000\",\"token\":\"tokenTest\"}"
    protected var authJson: JSONObject? = null

    protected fun initAuthJson() {
        try {
            authJson = JSONObject(projectDetailString)
        } catch (e: JSONException) {
            Log.e(Constants.logTag, e.toString())
        }
    }
}