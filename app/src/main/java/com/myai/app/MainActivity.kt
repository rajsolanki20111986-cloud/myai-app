package com.myai.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var mainContent: LinearLayout
    private lateinit var drawer: LinearLayout
    private lateinit var overlay: View

    private var drawerOpen = false
    private var downX = 0f

    private val savedUIs = mutableListOf<SavedUI>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedUIs.addAll(UIStorage.load(this))
        showHome()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun background(
        color: Int,
        radius: Float = 0f
    ): GradientDrawable {
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

        titleArea.addView(text("MyAI", 21f))
        titleArea.addView(
            text(
                "Your personal UI workspace",
                12f,
                Color.LTGRAY
            )
        )

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
        content.setPadding(
            dp(18),
            dp(22),
            dp(18),
            dp(30)
        )

        content.addView(text("Your UIs", 25f))

        content.addView(
            text(
                "Open your saved AI tools and websites from one place.",
                14f,
                Color.GRAY
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(5), 0, dp(18))
            }
        )

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

            val title = text("No UI saved", 18f)
            title.gravity = Gravity.CENTER

            val message = text(
                "Tap + New UI to add your first AI or website.",
                14f,
                Color.GRAY
            )
            message.gravity = Gravity.CENTER

            emptyBox.addView(title)
            emptyBox.addView(message)

            content.addView(
                emptyBox,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dp(20), 0, 0)
                }
            )

        } else {
            savedUIs.forEach {
                addUICard(content, it)
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
            dp(14),
            dp(14)
        )
        card.background = background(
            Color.rgb(25, 25, 25),
            14f
        )

        card.addView(text(ui.name, 19f))

        card.addView(
            text(
                "\n${ui.url}",
                13f,
                Color.GRAY
            )
        )

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

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(16), 0, 0)
            }
        )
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

        val params = FrameLayout.LayoutParams(
            dp(300),
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        params.gravity = Gravity.START

        drawer.translationX = -dp(300).toFloat()

        root.addView(drawer, params)

        drawer.addView(text("MyAI", 25f))

        drawer.addView(
            text(
                "Workspace",
                13f,
                Color.GRAY
            ),
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

        drawer.addView(
            item,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply {
                setMargins(0, 0, 0, dp(8))
            }
        )
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

                        if (
                            difference > 0 &&
                            downX < dp(70)
                        ) {
                            openDrawer()
                        } else if (
                            difference < 0 &&
                            drawerOpen
                        ) {
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

        val urlInput = EditText(this)
        urlInput.hint = "https://example.com"
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

                    var name =
                        nameInput.text.toString().trim()

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

                    UIStorage.save(this, savedUIs)
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

                val newName =
                    input.text.toString().trim()

                if (newName.isNotBlank()) {

                    ui.name = newName

                    UIStorage.save(
                        this,
                        savedUIs
                    )

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

                UIStorage.save(
                    this,
                    savedUIs
                )

                showHome()
            }
            .show()
    }

    private fun openUI(ui: SavedUI) {

        val result = UrlSecurity.check(ui.url)

        if (result.status == UrlSecurity.Status.INVALID) {

            AlertDialog.Builder(this)
                .setTitle("Invalid URL")
                .setMessage(
                    "This UI has an invalid web address."
                )
                .setPositiveButton("OK", null)
                .show()

            return
        }

        val webView = WebView(this)

        val settings: WebSettings =
            webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient =
            WebViewClient()

        webView.loadUrl(ui.url)

        val screen = LinearLayout(this)
        screen.orientation =
            LinearLayout.VERTICAL

        screen.setBackgroundColor(
            Color.BLACK
        )

        val topBar = LinearLayout(this)
        topBar.gravity =
            Gravity.CENTER_VERTICAL

        topBar.setPadding(
            dp(10),
            dp(5),
            dp(10),
            dp(5)
        )

        topBar.setBackgroundColor(
            Color.rgb(18, 18, 18)
        )

        val homeButton = Button(this)
        homeButton.text = "← Home"

        homeButton.setOnClickListener {
            showHome()
        }

        topBar.addView(homeButton)

        topBar.addView(
            text(ui.name, 17f),
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
                "Website verification, permissions, voice access " +
                "and safety controls will be handled separately."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onBackPressed() {

        if (drawerOpen) {
            closeDrawer()
        } else {
            showHome()
        }
    }
}
