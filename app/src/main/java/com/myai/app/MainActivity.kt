package com.myai.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

data class SavedUI(
    val id: Long,
    var name: String,
    var url: String
)

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var mainContent: LinearLayout
    private lateinit var drawer: LinearLayout
    private lateinit var overlay: View

    private var drawerOpen = false
    private var downX = 0f

    private val prefsName = "myai_prefs"
    private val uiKey = "saved_uis"

    private val savedUIs = mutableListOf<SavedUI>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadSavedUIs()
        showHome()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun background(color: Int, radius: Float = 0f): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            if (radius > 0) {
                cornerRadius = dp(radius.toInt()).toFloat()
            }
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int = Color.WHITE
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
        }
    }

    private fun showHome() {
        drawerOpen = false

        root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(8, 8, 8))

        mainContent = LinearLayout(this)
        mainContent.orientation = LinearLayout.VERTICAL
        mainContent.setBackgroundColor(Color.rgb(8, 8, 8))

        createHomeContent()

        root.addView(
            mainContent,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        createDrawer()

        setContentView(root)
        setupSwipe()
    }

    private fun createHomeContent() {

        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.gravity = Gravity.CENTER_VERTICAL
        topBar.setPadding(dp(18), dp(12), dp(18), dp(12))
        topBar.setBackgroundColor(Color.rgb(18, 18, 18))

        val menuButton = TextView(this)
        menuButton.text = "☰"
        menuButton.textSize = 28f
        menuButton.setTextColor(Color.WHITE)
        menuButton.gravity = Gravity.CENTER
        menuButton.setOnClickListener {
            openDrawer()
        }

        topBar.addView(
            menuButton,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        val titleArea = LinearLayout(this)
        titleArea.orientation = LinearLayout.VERTICAL

        val title = text("MyAI", 21f)

        val subtitle = text(
            "Your personal UI workspace",
            12f,
            Color.LTGRAY
        )

        titleArea.addView(title)
        titleArea.addView(subtitle)

        topBar.addView(
            titleArea,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        mainContent.addView(topBar)

        val scroll = android.widget.ScrollView(this)

        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(18), dp(22), dp(18), dp(30))

        val heading = text("Your UIs", 25f)
        content.addView(heading)

        val description = text(
            "Open your saved AI tools and websites from one place.",
            14f,
            Color.GRAY
        )

        val descriptionParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        descriptionParams.setMargins(0, dp(5), 0, dp(18))
        content.addView(description, descriptionParams)

        val addButton = Button(this)
        addButton.text = "+  New UI"
        addButton.setTextColor(Color.WHITE)
        addButton.background = background(
            Color.rgb(35, 35, 35),
            10f
        )

        addButton.setOnClickListener {
            showNewUIDialog()
        }

        content.addView(
            addButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        if (savedUIs.isEmpty()) {
            val emptyBox = LinearLayout(this)
            emptyBox.orientation = LinearLayout.VERTICAL
            emptyBox.gravity = Gravity.CENTER
            emptyBox.setPadding(
                dp(20),
                dp(40),
                dp(20),
                dp(40)
            )

            val emptyTitle = text(
                "No UI saved",
                18f
            )
            emptyTitle.gravity = Gravity.CENTER

            val emptyText = text(
                "Tap + New UI to add your first AI or website.",
                14f,
                Color.GRAY
            )
            emptyText.gravity = Gravity.CENTER

            emptyBox.addView(emptyTitle)
            emptyBox.addView(emptyText)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, dp(20), 0, 0)

            content.addView(emptyBox, params)

        } else {
            savedUIs.forEach { ui ->
                addUICard(content, ui)
            }
        }

        scroll.addView(content)

        mainContent.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }

    private fun addUICard(
        parent: LinearLayout,
        ui: SavedUI
    ) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(
            dp(18),
            dp(18),
            dp(18),
            dp(14)
        )
        card.background = background(
            Color.rgb(25, 25, 25),
            14f
        )

        val name = text(ui.name, 19f)
        card.addView(name)

        val preview = text(
            "\n${ui.url}",
            13f,
            Color.GRAY
        )
        card.addView(preview)

        val buttons = LinearLayout(this)
        buttons.gravity = Gravity.END
        buttons.setPadding(0, dp(12), 0, 0)

        val open = smallButton("Open")
        open.setOnClickListener {
            openUI(ui)
        }

        val rename = smallButton("Rename")
        rename.setOnClickListener {
            showRenameDialog(ui)
        }

        val delete = smallButton("Delete")
        delete.setOnClickListener {
            confirmDelete(ui)
        }

        buttons.addView(open)
        buttons.addView(rename)
        buttons.addView(delete)

        card.addView(buttons)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dp(16), 0, 0)

        parent.addView(card, params)
    }

    private fun smallButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            background = background(
                Color.rgb(38, 38, 38),
                8f
            )

            layoutParams = LinearLayout.LayoutParams(
                dp(90),
                dp(44)
            ).apply {
                setMargins(dp(4), 0, 0, 0)
            }
        }
    }

    private fun createDrawer() {

        overlay = View(this)
        overlay.setBackgroundColor(Color.argb(150, 0, 0, 0))
        overlay.visibility = View.GONE

        overlay.setOnClickListener {
            closeDrawer()
        }

        root.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        drawer = LinearLayout(this)
        drawer.orientation = LinearLayout.VERTICAL
        drawer.setPadding(
            dp(20),
            dp(28),
            dp(20),
            dp(20)
        )
        drawer.setBackgroundColor(Color.rgb(17, 17, 17))

        val drawerParams = FrameLayout.LayoutParams(
            dp(300),
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        drawerParams.gravity = Gravity.START
        drawer.translationX = -dp(300).toFloat()

        root.addView(drawer, drawerParams)

        val drawerTitle = text("MyAI", 25f)
        drawer.addView(drawerTitle)

        val drawerSubtitle = text(
            "Workspace",
            13f,
            Color.GRAY
        )

        drawer.addView(
            drawerSubtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(4), 0, dp(25))
            }
        )

        addDrawerItem("🏠  Home") {
            closeDrawer()
            showHome()
        }

        savedUIs.forEach { ui ->
            addDrawerItem("◉  ${ui.name}") {
                closeDrawer()
                openUI(ui)
            }
        }

        addDrawerItem("+  New UI") {
            closeDrawer()
            showNewUIDialog()
        }

        addDrawerItem("⚙  Settings") {
            closeDrawer()
            showSettingsMessage()
        }
    }

    private fun addDrawerItem(
        label: String,
        action: () -> Unit
    ) {
        val item = TextView(this)
        item.text = label
        item.textSize = 16f
        item.setTextColor(Color.WHITE)
        item.gravity = Gravity.CENTER_VERTICAL
        item.setPadding(
            dp(12),
            0,
            dp(12),
            0
        )
        item.background = background(
            Color.rgb(25, 25, 25),
            8f
        )

        item.setOnClickListener {
            action()
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(50)
        )
        params.setMargins(0, 0, 0, dp(8))

        drawer.addView(item, params)
    }

    private fun openDrawer() {
        if (drawerOpen) return

        drawerOpen = true
        overlay.visibility = View.VISIBLE

        drawer.animate()
            .translationX(0f)
            .setDuration(220)
            .start()
    }

    private fun closeDrawer() {
        if (!drawerOpen) return

        drawerOpen = false

        drawer.animate()
            .translationX(-drawer.width.toFloat())
            .setDuration(220)
            .withEndAction {
                overlay.visibility = View.GONE
            }
            .start()
    }

    private fun setupSwipe() {
        root.setOnTouchListener { _, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val difference = event.x - downX

                    if (abs(difference) > dp(80)) {

                        if (difference > 0 && downX < dp(70)) {
                            openDrawer()
                        } else if (difference < 0 && drawerOpen) {
                            closeDrawer()
                        }
                    }

                    true
                }

                else -> true
            }
        }
    }

    private fun showNewUIDialog() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(
            dp(25),
            dp(5),
            dp(25),
            0
        )

        val nameInput = EditText(this)
        nameInput.hint = "UI name (optional)"
        nameInput.setTextColor(Color.WHITE)
        nameInput.setHintTextColor(Color.GRAY)

        val urlInput = EditText(this)
        urlInput.hint = "https://example.com"
        urlInput.setTextColor(Color.WHITE)
        urlInput.setHintTextColor(Color.GRAY)
        urlInput.inputType =
            InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_VARIATION_URI

        layout.addView(nameInput)
        layout.addView(urlInput)

        AlertDialog.Builder(this)
            .setTitle("New UI")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->

                val url = urlInput.text.toString().trim()

                if (
                    url.startsWith("http://") ||
                    url.startsWith("https://")
                ) {

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
            .setMessage(
                "Remove \"${ui.name}\" from MyAI?"
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                savedUIs.remove(ui)
                saveSavedUIs()
                showHome()
            }
            .show()
    }

    private fun openUI(ui: SavedUI) {

        val webView = WebView(this)

        val settings: WebSettings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient = WebViewClient()

        webView.loadUrl(ui.url)

        val screen = LinearLayout(this)
        screen.orientation = LinearLayout.VERTICAL
        screen.setBackgroundColor(Color.BLACK)

        val topBar = LinearLayout(this)
        topBar.gravity = Gravity.CENTER_VERTICAL
        topBar.setPadding(
            dp(10),
            dp(5),
            dp(10),
            dp(5)
        )
        topBar.setBackgroundColor(Color.rgb(18, 18, 18))

        val homeButton = Button(this)
        homeButton.text = "← Home"
        homeButton.setOnClickListener {
            showHome()
        }

        topBar.addView(homeButton)

        val title = text(
            ui.name,
            17f
        )
        title.gravity = Gravity.CENTER_VERTICAL

        topBar.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        screen.addView(
            topBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
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

        webView.requestFocus()
    }

    private fun showSettingsMessage() {

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setMessage(
                "MyAI settings will be added step by step.\n\n" +
                "Website verification, permissions, " +
                "voice access and safety controls will be " +
                "handled separately."
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

        getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                uiKey,
                array.toString()
            )
            .apply()
    }

    private fun loadSavedUIs() {

        val data = getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        )
            .getString(uiKey, null)
            ?: return

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

        if (drawerOpen) {
            closeDrawer()
            return
        }

        showHome()
    }
}
