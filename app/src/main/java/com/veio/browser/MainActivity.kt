package com.veio.browser

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
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
import android.widget.ProgressBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var homeSearch: EditText
    private lateinit var addressContainer: LinearLayout
    private lateinit var homeScreen: LinearLayout
    private lateinit var contentFrame: FrameLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var titleView: TextView

    private val isDark: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
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
            setPadding(dp(18), dp(12), dp(18), dp(10))
        }

        val logo = TextView(this).apply {
            text = "ڤيو"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accentColor)
        }

        titleView = TextView(this).apply {
            text = "صفحتك الرئيسية"
            textSize = 13f
            setTextColor(secondaryText)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        header.addView(logo)
        header.addView(
            titleView,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        addressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(3), dp(8), dp(3))
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

        addressContainer.addView(
            makeButton("انتقال") {
                submitInput(addressBar.text.toString())
            }
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
            text = "ڤيو"
            textSize = 38f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accentColor)
        }

        val welcome = TextView(this).apply {
            text = "اكتشف طريقك إلى الويب"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(secondaryText)
            setPadding(0, dp(2), 0, dp(18))
        }

        val homeSearchContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(5), dp(8), dp(5))
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
        homeScreen.addView(welcome)

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

            webViewClient = object : WebViewClient() {

                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: android.graphics.Bitmap?
                ) {
                    super.onPageStarted(view, url, favicon)

                    showBrowser()
                    progressBar.visibility = View.VISIBLE
                    titleView.text = "جارٍ التحميل..."
                    addressBar.setText(url.orEmpty())
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    super.onPageFinished(view, url)

                    progressBar.visibility = View.GONE

                    titleView.text = view?.title
                        ?.takeIf { it.isNotBlank() }
                        ?: "متصفح ڤيو"

                    addressBar.setText(url.orEmpty())
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

            // مساحة علوية بسيطة، ليكون البحث في النصف العلوي من الشاشة.
            params.topMargin = (contentFrame.height * 0.055f).toInt()
            homeScreen.layoutParams = params
        }

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(5), dp(4), dp(5))
            background = roundedBackground(
                surfaceColor,
                borderColor,
                20
            )
        }

        bottomBar.addView(
            makeButton("رجوع") {
                if (
                    webView.visibility == View.VISIBLE &&
                    webView.canGoBack()
                ) {
                    webView.goBack()
                } else {
                    showHome()
                }
            }
        )

        bottomBar.addView(
            makeButton("للأمام") {
                if (
                    webView.visibility == View.VISIBLE &&
                    webView.canGoForward()
                ) {
                    webView.goForward()
                }
            }
        )

        bottomBar.addView(
            makeButton("الرئيسية") {
                showHome()
            }
        )

        bottomBar.addView(
            makeButton("إعادة") {
                if (webView.visibility == View.VISIBLE) {
                    webView.reload()
                } else {
                    homeSearch.requestFocus()
                }
            }
        )

        root.addView(header)

        root.addView(
            addressContainer,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(dp(12), 0, dp(12), dp(8))
            }
        )

        root.addView(
            progressBar,
            LinearLayout.LayoutParams(-1, dp(3))
        )

        root.addView(
            contentFrame,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        root.addView(
            bottomBar,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(dp(12), dp(6), dp(12), dp(8))
            }
        )

        setContentView(root)

        // لا نفتح Google تلقائيًا؛ البداية دائمًا من واجهة ڤيو.
        showHome()
    }

    private fun createSearchField(fieldHint: String): EditText {
        return EditText(this).apply {
            hint = fieldHint
            textSize = 14f
            isSingleLine = true

            setTextColor(primaryText)
            setHintTextColor(secondaryText)
            setPadding(dp(8), dp(9), dp(8), dp(9))

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

        titleView.text = "صفحتك الرئيسية"
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

            setTextColor(
                if (label == "بحث" || label == "انتقال") {
                    accentColor
                } else {
                    primaryText
                }
            )

            setPadding(dp(10), dp(10), dp(10), dp(10))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
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
        return (value * resources.displayMetrics.density).toInt()
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
