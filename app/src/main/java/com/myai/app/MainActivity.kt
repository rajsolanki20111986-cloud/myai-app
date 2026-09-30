package com.myai.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

data class SavedUI(
    val id: Long,
    var name: String,
    var url: String
)

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var webView: WebView

    private val prefsName = "myai_prefs"
    private val uiKey = "saved_uis"

    private val savedUIs = mutableListOf<SavedUI>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadSavedUIs()
        showHome()
    }

    private fun showHome() {
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.rgb(10, 10, 10))

        val topBar = LinearLayout(this)
        topBar.gravity = Gravity.CENTER_VERTICAL
        topBar.setPadding(18, 14, 18, 14)
        topBar.setBackgroundColor(Color.rgb(20, 20, 20))

        val menu = TextView(this)
        menu.text = "☰"
        menu.textSize = 28f
        menu.setTextColor(Color.WHITE)
        menu.setPadding(0, 0, 25, 0)
        menu.setOnClickListener {
            showDrawer()
        }

        val title = TextView(this)
        title.text = "MyAI"
        title.textSize = 21f
        title.setTextColor(Color.WHITE)

        topBar.addView(menu)
        topBar.addView(title)

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(18, 20, 18, 20)

        val scroll = android.widget.ScrollView(this)
        scroll.addView(content)

        root.addView(
            topBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        val heading = TextView(this)
        heading.text = "Your UIs"
        heading.textSize = 24f
        heading.setTextColor(Color.WHITE)
        heading.setPadding(0, 0, 0, 18)
        content.addView(heading)

        val addButton = Button(this)
        addButton.text = "+ New UI"
        addButton.setOnClickListener {
            showNewUIDialog()
        }
        content.addView(addButton)

        if (savedUIs.isEmpty()) {
            val empty = TextView(this)
            empty.text = "\nNo UI saved yet.\n\nTap + New UI to add your first AI or website."
            empty.textSize = 16f
            empty.setTextColor(Color.LTGRAY)
            empty.gravity = Gravity.CENTER
            content.addView(empty)
        } else {
            savedUIs.forEach { ui ->
                addUICard(ui)
            }
        }
    }

    private fun addUICard(ui: SavedUI) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(18, 18, 18, 18)
        card.setBackgroundColor(Color.rgb(28, 28, 28))

        val name = TextView(this)
        name.text = ui.name
        name.textSize = 19f
        name.setTextColor(Color.WHITE)

        val preview = TextView(this)
        preview.text = "\n${ui.url}\n"
        preview.textSize = 14f
        preview.setTextColor(Color.LTGRAY)

        val buttons = LinearLayout(this)
        buttons.gravity = Gravity.END

        val open = Button(this)
        open.text = "Open"
        open.setOnClickListener {
            openUI(ui)
        }

        val rename = Button(this)
        rename.text = "Rename"
        rename.setOnClickListener {
            showRenameDialog(ui)
        }

        val delete = Button(this)
        delete.text = "Delete"
        delete.setOnClickListener {
            confirmDelete(ui)
        }

        buttons.addView(open)
        buttons.addView(rename)
        buttons.addView(delete)

        card.addView(name)
        card.addView(preview)
        card.addView(buttons)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 18, 0, 0)

        content.addView(card, params)
    }

    private fun showNewUIDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 10, 30, 0)

        val nameInput = EditText(this)
        nameInput.hint = "UI name (optional)"

        val urlInput = EditText(this)
        urlInput.hint = "https://example.com"
        urlInput.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
            android.text.InputType.TYPE_TEXT_VARIATION_URI

        layout.addView(nameInput)
        layout.addView(urlInput)

        AlertDialog.Builder(this)
            .setTitle("New UI")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val url = urlInput.text.toString().trim()

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    var name = nameInput.text.toString().trim()

                    if (name.isBlank()) {
                        name = "UI ${savedUIs.size + 1}"
                    }

                    savedUIs.add(
                        SavedUI(
                            id = System.currentTimeMillis(),
                            name = name,
                            url = url
                        )
                    )

                    saveSavedUIs()
                    showHome()
                }
            }
            .show()
    }

    private fun showRenameDialog(ui: SavedUI) {
        val input = EditText(this)
        input.setText(ui.name)
        input.selectAll()

        AlertDialog.Builder(this)
            .setTitle("Rename UI")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()

                if (newName.isNotBlank()) {
                    ui.name = newName
                    saveSavedUIs()
                    showHome()
                }
            }
            .show()
    }

    private fun confirmDelete(ui: SavedUI) {
        AlertDialog.Builder(this)
            .setTitle("Delete UI?")
            .setMessage("Remove \"${ui.name}\" from MyAI?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                savedUIs.remove(ui)
                saveSavedUIs()
                showHome()
            }
            .show()
    }

    private fun openUI(ui: SavedUI) {
        webView = WebView(this)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient = WebViewClient()

        webView.loadUrl(ui.url)

        val back = Button(this)
        back.text = "← Home"
        back.setOnClickListener {
            showHome()
        }

        val screen = LinearLayout(this)
        screen.orientation = LinearLayout.VERTICAL
        screen.setBackgroundColor(Color.BLACK)

        screen.addView(
            back,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        screen.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(screen)
    }

    private fun showDrawer() {
        val drawer = LinearLayout(this)
        drawer.orientation = LinearLayout.VERTICAL
        drawer.setPadding(25, 45, 25, 25)
        drawer.setBackgroundColor(Color.rgb(18, 18, 18))

        val title = TextView(this)
        title.text = "MyAI"
        title.textSize = 24f
        title.setTextColor(Color.WHITE)
        title.setPadding(0, 0, 0, 30)

        drawer.addView(title)

        val home = Button(this)
        home.text = "🏠 Home"
        home.setOnClickListener {
            showHome()
        }
        drawer.addView(home)

        savedUIs.forEach { ui ->
            val item = Button(this)
            item.text = ui.name
            item.setOnClickListener {
                openUI(ui)
            }
            drawer.addView(item)
        }

        val newUI = Button(this)
        newUI.text = "+ New UI"
        newUI.setOnClickListener {
            showNewUIDialog()
        }
        drawer.addView(newUI)

        val settings = Button(this)
        settings.text = "⚙ Settings"
        settings.setOnClickListener {
            showSettingsMessage()
        }
        drawer.addView(settings)

        setContentView(drawer)
    }

    private fun showSettingsMessage() {
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setMessage(
                "MyAI settings will be added in the next stages.\n\n" +
                "Security, permissions and website verification will be handled separately."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveSavedUIs() {
        val array = JSONArray()

        savedUIs.forEach { ui ->
            val obj = JSONObject()
            obj.put("id", ui.id)
            obj.put("name", ui.name)
            obj.put("url", ui.url)
            array.put(obj)
        }

        getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putString(uiKey, array.toString())
            .apply()
    }

    private fun loadSavedUIs() {
        val data = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getString(uiKey, null) ?: return

        try {
            val array = JSONArray(data)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                savedUIs.add(
                    SavedUI(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        url = obj.getString("url")
                    )
                )
            }
        } catch (_: Exception) {
            savedUIs.clear()
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            showHome()
        }
    }
}
