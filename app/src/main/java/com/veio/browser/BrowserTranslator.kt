package com.veio.browser

import android.app.Activity
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.widget.Toast

class BrowserTranslator(
    private val activity: Activity,
    private val webView: WebView
) {

    /**
     * Opens Google Translate's website-translation route
     * inside the existing WebView.
     *
     * No paid translation API is used.
     */
    fun translateCurrentPage(
        targetLanguage: String = "ar"
    ): Boolean {
        val originalUrl = webView.url?.trim().orEmpty()

        if (webView.visibility != View.VISIBLE ||
            originalUrl.isBlank()
        ) {
            showMessage("افتح موقعًا أولًا لترجمته.")
            return false
        }

        val uri = runCatching {
            Uri.parse(originalUrl)
        }.getOrNull()

        val scheme = uri?.scheme?.lowercase()
        val host = uri?.host?.lowercase().orEmpty()

        if (scheme != "http" && scheme != "https") {
            showMessage("لا يمكن ترجمة هذا النوع من الصفحات.")
            return false
        }

        if (host == "translate.google.com" ||
            host.endsWith(".translate.google.com")
        ) {
            showMessage("أنت بالفعل داخل خدمة ترجمة جوجل.")
            return false
        }

        val language = targetLanguage
            .trim()
            .lowercase()

        if (!language.matches(Regex("[a-z]{2,3}(-[a-z0-9]{2,8})*"))) {
            showMessage("رمز لغة الترجمة غير صالح.")
            return false
        }

        val translateUrl =
            "https://translate.google.com/translate" +
            "?sl=auto" +
            "&tl=${Uri.encode(language)}" +
            "&u=${Uri.encode(originalUrl)}"

        webView.loadUrl(translateUrl)
        return true
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            activity,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
