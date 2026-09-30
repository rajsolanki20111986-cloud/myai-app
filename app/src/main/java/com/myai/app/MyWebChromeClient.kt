package com.myai.app

import android.app.Activity
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient

class MyWebChromeClient(
    private val activity: Activity
) : WebChromeClient() {

    override fun onPermissionRequest(
        request: PermissionRequest
    ) {
        activity.runOnUiThread {

            val allowedResources = request.resources.filter {
                it == PermissionRequest.RESOURCE_AUDIO_CAPTURE
            }.toTypedArray()

            if (allowedResources.isNotEmpty()) {
                request.grant(allowedResources)
            } else {
                request.deny()
            }
        }
    }
}
