package com.veio.browser

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var homeSearch: EditText
    private lateinit var addressContainer: LinearLayout
    private lateinit var homeScreen: LinearLayout
    private lateinit var contentFrame: FrameLayout
    private lateinit var progressBar: ProgressBar

    private var mobileUserAgent: String? = null
    private var desktopMode = false

    private val isDark: Boolean
        get() = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private val pageBackground: Int
        get() = if (isDark) Color.rgb(18, 20, 25)
        else Color.rgb(248, 249, 252)

    private val surfaceColor: Int
        get() = if (isDark) Color.rgb(31, 35, 43)
        else Color.WHITE

    private val primaryText: Int
        get() = if (isDark) Color.rgb(239, 242, 248)
        else Color.rgb(35, 40, 55)

    private val secondaryText: Int
        get() = if (isDark) Color.rgb(166, 174, 190)
        else Color.rgb(105, 112, 128)

    private val accentColor = Color.rgb(78, 128, 224)

    private val borderColor: Int
        get() = if (isDark) Color.rgb(62, 68, 80)
        else Color.rgb(224, 228, 238)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySystemBarColors()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(pageBackground)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(2), dp(8), dp(2))
        }

        val menuButton = TextView(this).apply {
            text = "☰"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(primaryText)
            contentDescription = "القائمة"
            setPadding(dp(8), dp(3), dp(8), dp(3))
            isClickable = true
            isFocusable = true
        }

        val logo = TextView(this).apply {
            text = "Veio"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accentColor)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), 0, dp(5), 0)
        }

        header.addView(menuButton)
        header.addView(logo)
        menuButton.setOnClickListener {
            showBrowserMenu(menuButton)
        }

        addressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(1), dp(6), dp(1))
            background = roundedBackground(
                surfaceColor,
                borderColor,
                24
            )
            visibility = View.GONE
        }

        addressBar = createSearchField("اكتب عنوان الموقع أو ابحث")

        addressBar.setOnEditorActionListener { _, id, event ->
            val enter = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN

            if (id == EditorInfo.IME_ACTION_GO || enter) {
                submitInput(addressBar.text.toString())
                true
            } else {
                false
            }
        }

        addressContainer.addView(
            addressBar,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
            visibility = View.GONE
        }

        contentFrame = FrameLayout(this)

        homeScreen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(8), dp(20), dp(12))
            setBackgroundColor(pageBackground)
        }

        val homeLogo = TextView(this).apply {
            text = "Veio"
            textSize = 34f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accentColor)
        }

        val homeSearchContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(3), dp(8), dp(3))
            background = roundedBackground(
                surfaceColor,
                borderColor,
                28
            )
            elevation = dp(2).toFloat()
        }

        homeSearch = createSearchField("ابحث أو اكتب عنوان موقع")

        homeSearch.setOnEditorActionListener { _, id, event ->
            val enter = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN

            if (id == EditorInfo.IME_ACTION_GO || enter) {
                submitInput(homeSearch.text.toString())
                true
            } else {
                false
            }
        }

        homeSearchContainer.addView(
            homeSearch,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        homeSearchContainer.addView(
            makeButton("بحث") {
                submitInput(homeSearch.text.toString())
            }
        )

        val shortcutsTitle = TextView(this).apply {
            text = "اختصارات سريعة"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primaryText)
            gravity = Gravity.START
            setPadding(dp(4), dp(22), dp(4), dp(10))
        }

        val shortcuts = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        shortcuts.addView(
            makeShortcut("YouTube") {
                openWebsite("https://www.youtube.com")
            },
            shortcutParams()
        )

        shortcuts.addView(
            makeShortcut("Wikipedia") {
                openWebsite("https://www.wikipedia.org")
            },
            shortcutParams()
        )

        shortcuts.addView(
            makeShortcut("GitHub") {
                openWebsite("https://github.com")
            },
            shortcutParams()
        )

        homeScreen.addView(homeLogo)

        homeScreen.addView(
            homeSearchContainer,
            LinearLayout.LayoutParams(-1, -2)
        )

        homeScreen.addView(shortcutsTitle)

        homeScreen.addView(
            shortcuts,
            LinearLayout.LayoutParams(-1, dp(76))
        )
                webView = WebView(this).apply {
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            visibility = View.GONE

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            mobileUserAgent = settings.userAgentString

            webViewClient = object : WebViewClient() {

                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: android.graphics.Bitmap?
                ) {
                    super.onPageStarted(view, url, favicon)

                    showBrowser()
                    progressBar.visibility = View.VISIBLE
                    addressBar.setText(url.orEmpty())
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    super.onPageFinished(view, url)

                    progressBar.visibility = View.GONE
                    addressBar.setText(url.orEmpty())

                    if (!url.isNullOrBlank()) {
                        saveHistory(url, view?.title.orEmpty())
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {

                override fun onProgressChanged(
                    view: WebView?,
                    newProgress: Int
                ) {
                    progressBar.progress = newProgress
                    progressBar.visibility =
                        if (newProgress >= 100) View.GONE
                        else View.VISIBLE
                }
            }
        }

        contentFrame.addView(
            webView,
            FrameLayout.LayoutParams(-1, -1)
        )

        contentFrame.addView(
            homeScreen,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            )
        )

        contentFrame.post {
            val params =
                homeScreen.layoutParams as FrameLayout.LayoutParams

            params.topMargin = (contentFrame.height * 0.055f).toInt()
            homeScreen.layoutParams = params
        }

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(2), dp(1), dp(2), dp(1))
            background = roundedBackground(
                surfaceColor,
                borderColor,
                20
            )
        }

        bottomBar.addView(
            makeIconButton("‹", "رجوع") {
                if (
                    webView.visibility == View.VISIBLE &&
                    webView.canGoBack()
                ) {
                    webView.goBack()
                } else {
                    showHome()
                }
            },
            bottomButtonParams()
        )

        bottomBar.addView(
            makeIconButton("›", "للأمام") {
                if (
                    webView.visibility == View.VISIBLE &&
                    webView.canGoForward()
                ) {
                    webView.goForward()
                }
            },
            bottomButtonParams()
        )

        bottomBar.addView(
            makeIconButton("⌂", "الرئيسية") {
                showHome()
            },
            bottomButtonParams()
        )

        bottomBar.addView(
            makeIconButton("↻", "إعادة التحميل") {
                if (webView.visibility == View.VISIBLE) {
                    webView.reload()
                } else {
                    homeSearch.requestFocus()
                }
            },
            bottomButtonParams()
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, dp(34))
        )

        root.addView(
            addressContainer,
            LinearLayout.LayoutParams(-1, dp(44)).apply {
                setMargins(dp(8), dp(2), dp(8), dp(3))
            }
        )

        root.addView(
            progressBar,
            LinearLayout.LayoutParams(-1, dp(2))
        )

        root.addView(
            contentFrame,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        root.addView(
            bottomBar,
            LinearLayout.LayoutParams(-1, dp(38)).apply {
                setMargins(dp(8), dp(2), dp(8), dp(3))
            }
        )

        setContentView(root)

        showHome()
    }

    private fun createSearchField(fieldHint: String): EditText {
        return EditText(this).apply {
            hint = fieldHint
            textSize = 14f
            isSingleLine = true

            setTextColor(primaryText)
            setHintTextColor(secondaryText)
            setPadding(dp(8), dp(7), dp(8), dp(7))

            background = null
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            textDirection = View.TEXT_DIRECTION_FIRST_STRONG
            imeOptions = EditorInfo.IME_ACTION_GO
        }
    }

    private fun submitInput(input: String) {
        val value = input.trim()
        if (value.isEmpty()) return

        val activeField =
            if (webView.visibility == View.VISIBLE) addressBar
            else homeSearch

        (
            getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        ).hideSoftInputFromWindow(activeField.windowToken, 0)

        openInput(value)
    }

    private fun openInput(value: String) {
        val url = when {
            value.startsWith("https://", true) ||
                value.startsWith("http://", true) -> value

            value.any { it.isWhitespace() } ||
                !value.contains(".") ->
                "https://www.google.com/search?q=" +
                    java.net.URLEncoder.encode(value, "UTF-8")

            else -> "https://$value"
        }

        showBrowser()
        webView.loadUrl(url)
    }

    private fun openWebsite(url: String) {
        homeSearch.setText("")
        showBrowser()
        webView.loadUrl(url)
    }

    private fun showHome() {
        if (!::homeScreen.isInitialized) return

        webView.stopLoading()
        webView.visibility = View.GONE
        homeScreen.visibility = View.VISIBLE
        addressContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        homeSearch.setText("")
    }

    private fun showBrowser() {
        if (!::webView.isInitialized) return

        homeScreen.visibility = View.GONE
        webView.visibility = View.VISIBLE
        addressContainer.visibility = View.VISIBLE
    }

    private fun makeShortcut(
        label: String,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 12f
            gravity = Gravity.CENTER

            setTextColor(primaryText)
            background = roundedBackground(
                surfaceColor,
                borderColor,
                16
            )

            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun shortcutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            dp(76),
            1f
        ).apply {
            setMargins(dp(3), 0, dp(3), 0)
        }
    }

    private fun makeButton(
        label: String,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(accentColor)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun makeIconButton(
        symbol: String,
        description: String,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = symbol
            textSize = 25f
            gravity = Gravity.CENTER
            contentDescription = description
            setTextColor(primaryText)
            setPadding(dp(5), 0, dp(5), 0)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun bottomButtonParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            -1,
            1f
        )
    }
        private fun showBrowserMenu(anchor: View) {
        val menu = PopupMenu(this, anchor)

        menu.menu.add(Menu.NONE, 1, Menu.NONE, "السجل")
        menu.menu.add(
            Menu.NONE,
            2,
            Menu.NONE,
            if (desktopMode) {
                "إيقاف وضع سطح المكتب"
            } else {
                "سطح المكتب"
            }
        )

        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    showHistoryDialog()
                    true
                }

                2 -> {
                    toggleDesktopMode()
                    true
                }

                else -> false
            }
        }

        menu.show()
    }

    private fun saveHistory(url: String, title: String) {
        val preferences = getSharedPreferences(
            "veio_browser",
            Context.MODE_PRIVATE
        )

        val oldHistory = try {
            JSONArray(
                preferences.getString("history", "[]")
            )
        } catch (_: Exception) {
            JSONArray()
        }

        val updatedHistory = JSONArray()
        val normalizedUrl = url.trim()

        if (normalizedUrl.isEmpty()) return

        updatedHistory.put(
            JSONObject().apply {
                put("url", normalizedUrl)
                put("title", title)
            }
        )

        for (index in 0 until oldHistory.length()) {
            val entry = oldHistory.optJSONObject(index) ?: continue
            val oldUrl = entry.optString("url")

            if (oldUrl == normalizedUrl) continue

            updatedHistory.put(entry)

            if (updatedHistory.length() >= 100) break
        }

        preferences.edit()
            .putString("history", updatedHistory.toString())
            .apply()
    }

    private fun showHistoryDialog() {
        val preferences = getSharedPreferences(
            "veio_browser",
            Context.MODE_PRIVATE
        )

        val history = try {
            JSONArray(
                preferences.getString("history", "[]")
            )
        } catch (_: Exception) {
            JSONArray()
        }

        if (history.length() == 0) {
            AlertDialog.Builder(this)
                .setTitle("السجل")
                .setMessage("لا توجد صفحات محفوظة في السجل حتى الآن.")
                .setPositiveButton("حسنًا", null)
                .show()
            return
        }

        val entries = mutableListOf<Pair<String, String>>()
        val labels = mutableListOf<String>()

        for (index in 0 until history.length()) {
            val entry = history.optJSONObject(index) ?: continue
            val url = entry.optString("url")

            if (url.isBlank()) continue

            val title = entry.optString("title")
                .takeIf { it.isNotBlank() }
                ?: url

            entries.add(title to url)
            labels.add("$title\n$url")
        }

        if (entries.isEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("السجل")
            .setItems(labels.toTypedArray()) { _, which ->
                if (which in entries.indices) {
                    homeSearch.setText("")
                    showBrowser()
                    webView.loadUrl(entries[which].second)
                }
            }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    private fun toggleDesktopMode() {
        if (!::webView.isInitialized) return

        desktopMode = !desktopMode
        val settings = webView.settings

        if (desktopMode) {
            settings.userAgentString =
                "Mozilla/5.0 (X11; Linux x86_64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/131.0.0.0 Safari/537.36"

            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
        } else {
            mobileUserAgent?.let {
                settings.userAgentString = it
            }

            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
        }

        webView.url?.let {
            webView.reload()
        }
    }

    private fun roundedBackground(
        color: Int,
        strokeColor: Int,
        radiusDp: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
            setStroke(dp(1), strokeColor)
        }
    }

    private fun applySystemBarColors() {
        window.statusBarColor = pageBackground
        window.navigationBarColor = pageBackground

        window.decorView.systemUiVisibility =
            if (isDark) {
                0
            } else {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
    }

    private fun dp(value: Int): Int {
        return (
            value * resources.displayMetrics.density
        ).toInt()
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (
            ::webView.isInitialized &&
            webView.visibility == View.VISIBLE
        ) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                showHome()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }

        super.onDestroy()
    }
}
