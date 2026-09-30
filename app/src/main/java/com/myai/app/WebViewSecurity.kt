package com.myai.app

import android.webkit.WebSettings
import android.webkit.WebView

object WebViewSecurity {

    fun configure(webView: WebView) {

        val settings: WebSettings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false

        settings.allowFileAccess = false
        settings.allowContentAccess = false

        webView.settings.mixedContentMode =
            WebSettings.MIXED_CONTENT_NEVER_ALLOW
    }
}
