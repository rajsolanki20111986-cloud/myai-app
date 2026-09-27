package com.myai.app

import android.app.AlertDialog
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var prefs: android.content.SharedPreferences
    private val PREFS_NAME = "myai_prefs"
    private val LINK_KEY = "server_link"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        webView = WebView(this)
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = WebViewClient()
        setContentView(webView)

        val savedLink = prefs.getString(LINK_KEY, null)
        if (savedLink.isNullOrBlank()) {
            showLinkDialog(firstTime = true)
        } else {
            webView.loadUrl(savedLink)
        }
    }

    private fun showLinkDialog(firstTime: Boolean) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_TEXT_VARIATION_URI
        input.hint = "https://xxxxxxxx.gradio.live"
        val current = prefs.getString(LINK_KEY, "")
        if (!current.isNullOrBlank()) {
            input.setText(current)
        }

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val padding = (16 * resources.displayMetrics.density).toInt()
        container.setPadding(padding, padding, padding, padding)
        container.addView(input)

        val title = if (firstTime) "Server link daalein" else "Server link badlein"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(container)
            .setCancelable(!firstTime)
            .setPositiveButton("Save") { _, _ ->
                val link = input.text.toString().trim()
                if (link.startsWith("http")) {
                    prefs.edit().putString(LINK_KEY, link).apply()
                    webView.loadUrl(link)
                } else {
                    showLinkDialog(firstTime)
                }
            }
            .show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
