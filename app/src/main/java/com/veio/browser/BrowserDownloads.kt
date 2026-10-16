package com.veio.browser

import android.app.Activity
import android.app.DownloadManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONTokener

class BrowserDownloads(
    private val activity: Activity,
    private val webView: WebView,
    private val container: FrameLayout
) {

    private data class DownloadLink(
        val title: String,
        val url: String
    )

    private val downloadManager =
        activity.getSystemService(Context.DOWNLOAD_SERVICE)
            as DownloadManager

    private val accentColor = Color.rgb(78, 128, 224)
    private val dangerColor = Color.rgb(210, 65, 75)

    private val isDark: Boolean
        get() = (activity.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

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

    private val borderColor: Int
        get() = if (isDark) Color.rgb(62, 68, 80)
        else Color.rgb(224, 228, 238)

    private val downloadButton = TextView(activity).apply {
        text = "↓"
        textSize = 28f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        contentDescription = "التنزيلات"
        background = roundedBackground(accentColor, 28)
        elevation = dp(6).toFloat()
        visibility = View.GONE

        setOnClickListener {
            showAvailableLinks()
        }

        setOnLongClickListener {
            showDownloads()
            true
        }
    }

    private val downloadsPage = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        setBackgroundColor(pageBackground)
        visibility = View.GONE
    }

    private var savedVisibility = mutableMapOf<View, Int>()
    private var availableLinks = emptyList<DownloadLink>()

    init {
        container.addView(
            downloadButton,
            FrameLayout.LayoutParams(
                dp(54),
                dp(54),
                Gravity.BOTTOM or Gravity.END
            ).apply {
                setMargins(dp(12), dp(12), dp(16), dp(16))
            }
        )

        container.addView(
            downloadsPage,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            if (!url.isNullOrBlank()) {
                startDownload(
                    url = url,
                    suggestedName = URLUtil.guessFileName(
                        url,
                        contentDisposition,
                        mimeType
                    ),
                    userAgent = userAgent
                )
            }
        }

        buildDownloadsPage()
    }

    /**
     * Call this from WebViewClient.onPageFinished().
     * The floating button appears only when suitable file links exist.
     */
    fun onPageFinished() {
        downloadButton.visibility = View.GONE
        availableLinks = emptyList()

        val script = """
            (function() {
                const result = [];
                const seen = new Set();
                const filePattern =
                    /\.(pdf|zip|rar|7z|apk|xapk|jpg|jpeg|png|gif|webp|mp3|m4a|wav|ogg|mp4|mkv|webm|avi|doc|docx|xls|xlsx|ppt|pptx|txt|csv|epub|iso|tar|gz)(?:[?#]|$)/i;

                document.querySelectorAll('a[href]').forEach(function(a) {
                    try {
                        const url = new URL(a.href, location.href);

                        if (url.protocol !== 'http:' &&
                            url.protocol !== 'https:') return;

                        const marked =
                            a.hasAttribute('download') ||
                            filePattern.test(url.href);

                        if (!marked || seen.has(url.href)) return;

                        seen.add(url.href);

                        let title = (a.innerText || a.title ||
                            a.getAttribute('download') || '').trim();

                        if (!title) {
                            title = url.pathname.split('/').pop() ||
                                'ملف للتنزيل';
                        }

                        result.push({
                            title: title.substring(0, 120),
                            url: url.href
                        });
                    } catch (_) {}
                });

                return JSON.stringify(result.slice(0, 40));
            })();
        """.trimIndent()

        webView.evaluateJavascript(script) { raw ->
            if (raw.isNullOrBlank() || raw == "null") return@evaluateJavascript

            val decoded = runCatching {
                JSONTokener(raw).nextValue() as? String
            }.getOrNull() ?: return@evaluateJavascript

            val links = mutableListOf<DownloadLink>()

            runCatching {
                val array = JSONArray(decoded)

                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val url = item.optString("url").trim()
                    val title = item.optString("title").trim()

                    if (url.startsWith("https://") ||
                        url.startsWith("http://")) {
                        links.add(
                            DownloadLink(
                                title = title.ifBlank { "ملف للتنزيل" },
                                url = url
                            )
                        )
                    }
                }
            }

            availableLinks = links
            downloadButton.visibility =
                if (links.isNotEmpty() &&
                    webView.visibility == View.VISIBLE
                ) View.VISIBLE else View.GONE
        }
    }

    /**
     * Opens the downloads page inside the existing browser container.
     */
    fun showDownloads() {
        savedVisibility.clear()

        for (index in 0 until container.childCount) {
            val child = container.getChildAt(index)
            savedVisibility[child] = child.visibility
            child.visibility = View.GONE
        }

        downloadsPage.visibility = View.VISIBLE
        downloadsPage.bringToFront()
        renderDownloads()
    }

    /**
     * Closes the downloads page and restores the previous browser screen.
     */
    fun closeDownloads() {
        downloadsPage.visibility = View.GONE

        savedVisibility.forEach { (view, visibility) ->
            if (view.parent === container) {
                view.visibility = visibility
            }
        }

        savedVisibility.clear()
    }

    private fun showAvailableLinks() {
        if (availableLinks.isEmpty()) {
            downloadButton.visibility = View.GONE
            return
        }

        val labels = availableLinks.map {
            "${it.title}\n${it.url}"
        }.toTypedArray()

        AlertDialog.Builder(activity)
            .setTitle("اختر الملف للتنزيل")
            .setItems(labels) { _, which ->
                if (which in availableLinks.indices) {
                    val link = availableLinks[which]
                    startDownload(
                        url = link.url,
                        suggestedName = URLUtil.guessFileName(
                            link.url,
                            null,
                            null
                        ),
                        userAgent = webView.settings.userAgentString
                    )
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun startDownload(
        url: String,
        suggestedName: String,
        userAgent: String?
    ) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))

            request.setTitle(suggestedName.ifBlank { "تنزيل ملف" })
            request.setDescription("جارٍ تنزيل الملف باستخدام Veio")
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)

            if (!userAgent.isNullOrBlank()) {
                request.addRequestHeader("User-Agent", userAgent)
            }

            val cookie = CookieManager.getInstance().getCookie(url)
            if (!cookie.isNullOrBlank()) {
                request.addRequestHeader("Cookie", cookie)
            }

            val fileName = suggestedName
                .substringAfterLast('/')
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .take(180)
                .ifBlank { "veio_download" }

            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

            downloadManager.enqueue(request)

            Toast.makeText(
                activity,
                "بدأ تنزيل الملف. يمكنك متابعة حالته من صفحة التنزيلات.",
                Toast.LENGTH_LONG
            ).show()
        } catch (exception: Exception) {
            Toast.makeText(
                activity,
                "تعذر بدء التنزيل: ${exception.localizedMessage ?: "خطأ غير معروف"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun buildDownloadsPage() {
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(surfaceColor)
        }

        val backButton = makeTextButton("‹", 28f) {
            closeDownloads()
        }

        val title = TextView(activity).apply {
            text = "التنزيلات"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primaryText)
            gravity = Gravity.CENTER_VERTICAL
        }

        val refreshButton = makeTextButton("تحديث", 14f) {
            renderDownloads()
        }

        header.addView(backButton)
        header.addView(
            title,
            LinearLayout.LayoutParams(0, dp(44), 1f)
        )
        header.addView(refreshButton)

        downloadsPage.addView(
            header,
            LinearLayout.LayoutParams(-1, -2)
        )
    }

    private fun renderDownloads() {
        downloadsPage.removeViews(
            1,
            (downloadsPage.childCount - 1).coerceAtLeast(0)
        )

        val scrollView = ScrollView(activity).apply {
            isFillViewport = true
            setPadding(dp(12), dp(12), dp(12), dp(16))
        }

        val list = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val cursor = downloadManager.query(DownloadManager.Query())

        var count = 0

        cursor?.use {
            val idColumn = it.getColumnIndex(DownloadManager.COLUMN_ID)
            val titleColumn = it.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val statusColumn = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val downloadedColumn =
                it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalColumn =
                it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "ملف تم تنزيله"
                val status = it.getInt(statusColumn)
                val downloaded = it.getLong(downloadedColumn)
                val total = it.getLong(totalColumn)

                list.addView(
                    createDownloadCard(
                        id = id,
                        title = title,
                        status = status,
                        downloaded = downloaded,
                        total = total
                    )
                )

                count++
            }
        }

        if (count == 0) {
            val emptyMessage = TextView(activity).apply {
                text = "لا توجد تنزيلات حتى الآن."
                textSize = 16f
                setTextColor(secondaryText)
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(48), dp(20), dp(48))
            }

            list.addView(
                emptyMessage,
                LinearLayout.LayoutParams(-1, -2)
            )
        }

        scrollView.addView(
            list,
            FrameLayout.LayoutParams(-1, -2)
        )

        downloadsPage.addView(
            scrollView,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )
    }

    private fun createDownloadCard(
        id: Long,
        title: String,
        status: Int,
        downloaded: Long,
        total: Long
    ): View {
        val statusText = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "اكتمل التنزيل"
            DownloadManager.STATUS_FAILED -> "فشل التنزيل"
            DownloadManager.STATUS_PAUSED -> "التنزيل متوقف مؤقتًا"
            DownloadManager.STATUS_RUNNING -> {
                if (total > 0) {
                    "جارٍ التنزيل: ${downloaded * 100 / total}%"
                } else {
                    "جارٍ التنزيل"
                }
            }
            DownloadManager.STATUS_PENDING -> "في انتظار التنزيل"
            else -> "حالة غير معروفة"
        }

        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedBackground(surfaceColor, 14, borderColor)
        }

        val name = TextView(activity).apply {
            text = title
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primaryText)
            maxLines = 3
        }

        val statusView = TextView(activity).apply {
            text = statusText
            textSize = 13f
            setTextColor(secondaryText)
            setPadding(0, dp(6), 0, dp(10))
        }

        val actions = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            actions.addView(
                makeTextButton("فتح الملف", 13f) {
                    openDownloadedFile(id)
                },
                LinearLayout.LayoutParams(0, dp(42), 1f)
            )
        }

        actions.addView(
            makeTextButton("حذف", 13f, dangerColor) {
                AlertDialog.Builder(activity)
                    .setTitle("حذف التنزيل")
                    .setMessage("هل تريد إزالة هذا التنزيل؟")
                    .setNegativeButton("إلغاء", null)
                    .setPositiveButton("حذف") { _, _ ->
                        downloadManager.remove(id)
                        renderDownloads()
                    }
                    .show()
            },
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        card.addView(name)
        card.addView(statusView)
        card.addView(actions)

        return card.apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(10)
            }
        }
    }

    private fun openDownloadedFile(id: Long) {
        try {
            val uri = downloadManager.getUriForDownloadedFile(id)

            if (uri == null) {
                Toast.makeText(
                    activity,
                    "لم يتم العثور على الملف.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val mimeType =
                downloadManager.getMimeTypeForDownloadedFile(id) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            activity.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                activity,
                "لا يوجد تطبيق مناسب لفتح هذا الملف.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun makeTextButton(
        text: String,
        size: Float,
        textColor: Int = accentColor,
        action: () -> Unit
    ): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = size
            gravity = Gravity.CENTER
            setTextColor(textColor)
            setPadding(dp(10), dp(6), dp(10), dp(6))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()

            if (strokeColor != null) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * activity.resources.displayMetrics.density)
            .toInt()
    }
}
