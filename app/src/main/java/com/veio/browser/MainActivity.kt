package com.veio.browser

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var titleView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(248, 249, 252)
        window.navigationBarColor = Color.rgb(248, 249, 252)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(Color.rgb(248, 249, 252))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(8))
        }

        val logo = TextView(this).apply {
            text = "ڤيو"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(35, 70, 150))
        }

        titleView = TextView(this).apply {
            text = "متصفحك"
            textSize = 13f
            setTextColor(Color.rgb(105, 112, 128))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        header.addView(
            logo,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        header.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val addressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(4), dp(8), dp(4))
            background = roundedBackground(
                Color.WHITE,
                Color.rgb(224, 228, 238),
                24
            )
        }

        addressBar = EditText(this).apply {
            hint = "ابحث أو اكتب عنوان موقع"
            textSize = 14f
            isSingleLine = true
            setTextColor(Color.rgb(35, 40, 55))
            setHintTextColor(Color.rgb(135, 141, 155))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = null
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            textDirection = View.TEXT_DIRECTION_FIRST_STRONG
            imeOptions = EditorInfo.IME_ACTION_GO
        }

        val goButton = makeButton("انتقال") {
            loadAddress(addressBar.text.toString())
        }

        addressBar.setOnEditorActionListener { _, actionId, event ->
            val enterPressed =
                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN

            if (actionId == EditorInfo.IME_ACTION_GO || enterPressed) {
                loadAddress(addressBar.text.toString())
                true
            } else {
                false
            }
        }

        addressContainer.addView(
            addressBar,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
        addressContainer.addView(goButton)

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
            visibility = View.GONE
        }

        webView = WebView(this).apply {
            layoutDirection = View.LAYOUT_DIRECTION_LTR
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
                    progressBar.visibility = View.VISIBLE
                    titleView.text = "جارٍ التحميل..."
                    addressBar.setText(url ?: "")
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                    titleView.text = view?.title ?: "متصفح ڤيو"
                    addressBar.setText(url ?: "")
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(
                    view: WebView?,
                    newProgress: Int
                ) {
                    progressBar.progress = newProgress
                    progressBar.visibility =
                        if (newProgress >= 100) View.GONE else View.VISIBLE
                }
            }
        }

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = roundedBackground(
                Color.WHITE,
                Color.rgb(230, 233, 241),
                20
            )
        }

        bottomBar.addView(makeButton("رجوع") {
            if (webView.canGoBack()) webView.goBack()
        })
        bottomBar.addView(makeButton("للأمام") {
            if (webView.canGoForward()) webView.goForward()
        })
        bottomBar.addView(makeButton("الرئيسية") {
            loadAddress("https://www.google.com")
        })
        bottomBar.addView(makeButton("إعادة") {
            webView.reload()
        })

        root.addView(header)

        root.addView(
            addressContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(12), 0, dp(12), dp(8))
            }
        )

        root.addView(
            progressBar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(3)
            )
        )

        root.addView(
            webView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            bottomBar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(12), dp(6), dp(12), dp(8))
            }
        )

        setContentView(root)
        loadAddress("https://www.google.com")
    }

    private fun loadAddress(input: String) {
        val value = input.trim()
        if (value.isEmpty()) return

        val url = when {
            value.startsWith("https://", true) ||
                value.startsWith("http://", true) -> value

            value.contains(" ") ||
                !value.contains(".") -> {
                "https://www.google.com/search?q=" +
                    java.net.URLEncoder.encode(value, "UTF-8")
            }

            else -> "https://$value"
        }

        webView.loadUrl(url)
    }

    private fun makeButton(
        label: String,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(40, 65, 125))
            setPadding(dp(12), dp(10), dp(12), dp(10))
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
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
