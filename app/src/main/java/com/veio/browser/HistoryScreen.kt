package com.veio.browser

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class HistoryScreen(
    private val activity: Activity,
    private val historyStore: HistoryStore,
    private val onOpenUrl: (String) -> Unit,
    private val onClose: () -> Unit
) {

    private val isDark: Boolean
        get() = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private val backgroundColor: Int
        get() = if (isDark) Color.rgb(18, 20, 25)
        else Color.rgb(248, 249, 252)

    private val cardColor: Int
        get() = if (isDark) Color.rgb(31, 35, 43)
        else Color.WHITE

    private val primaryText: Int
        get() = if (isDark) Color.rgb(239, 242, 248)
        else Color.rgb(35, 40, 55)

    private val secondaryText: Int
        get() = if (isDark) Color.rgb(166, 174, 190)
        else Color.rgb(105, 112, 128)

    private val accentColor = Color.rgb(78, 128, 224)

    private val dangerColor = Color.rgb(210, 65, 75)

    private val listContainer = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    val view: View = buildScreen()

    private fun buildScreen(): View {
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(backgroundColor)
        }

        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(cardColor)
        }

        val backButton = TextView(activity).apply {
            text = "‹"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(primaryText)
            contentDescription = "رجوع"
            setPadding(dp(8), 0, dp(8), 0)
            setOnClickListener { onClose() }
        }

        val title = TextView(activity).apply {
            text = "سجل التصفح"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primaryText)
            gravity = Gravity.CENTER_VERTICAL
        }

        val spacer = View(activity)

        val clearButton = TextView(activity).apply {
            text = "مسح الكل"
            textSize = 14f
            setTextColor(dangerColor)
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setOnClickListener { confirmClearHistory() }
        }

        header.addView(backButton)
        header.addView(
            title,
            LinearLayout.LayoutParams(0, dp(44), 1f)
        )
        header.addView(
            spacer,
            LinearLayout.LayoutParams(dp(4), dp(1))
        )
        header.addView(clearButton)

        val scrollView = ScrollView(activity).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(dp(12), dp(12), dp(12), dp(16))
        }

        scrollView.addView(
            listContainer,
            ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        renderHistory()
        return root
    }

    private fun renderHistory() {
        listContainer.removeAllViews()

        val entries = historyStore.getAll()

        if (entries.isEmpty()) {
            val emptyMessage = TextView(activity).apply {
                text = "لا توجد صفحات محفوظة في السجل حتى الآن."
                textSize = 16f
                setTextColor(secondaryText)
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(48), dp(20), dp(48))
            }

            listContainer.addView(
                emptyMessage,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            return
        }

        entries.forEach { entry ->
            listContainer.addView(createHistoryCard(entry))
        }
    }

    private fun createHistoryCard(
        entry: HistoryStore.Entry
    ): View {
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedBackground(cardColor, dp(14))
        }

        val titleText = TextView(activity).apply {
            text = entry.title.ifBlank { entry.url }
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primaryText)
            maxLines = 2
            setOnClickListener { onOpenUrl(entry.url) }
        }

        val urlText = TextView(activity).apply {
            text = entry.url
            textSize = 12f
            setTextColor(secondaryText)
            maxLines = 2
            setPadding(0, dp(5), 0, dp(8))
            setOnClickListener { onOpenUrl(entry.url) }
        }

        val actions = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val openButton = makeActionButton(
            text = "فتح",
            color = accentColor
        ) {
            onOpenUrl(entry.url)
        }

        val deleteButton = makeActionButton(
            text = "حذف",
            color = dangerColor
        ) {
            historyStore.delete(entry.url)
            renderHistory()
        }

        actions.addView(
            openButton,
            LinearLayout.LayoutParams(
                0,
                dp(40),
                1f
            ).apply {
                marginEnd = dp(8)
            }
        )

        actions.addView(
            deleteButton,
            LinearLayout.LayoutParams(
                0,
                dp(40),
                1f
            )
        )

        card.addView(titleText)
        card.addView(urlText)
        card.addView(actions)

        return card.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
        }
    }

    private fun makeActionButton(
        text: String,
        color: Int,
        action: () -> Unit
    ): Button {
        return Button(activity).apply {
            this.text = text
            textSize = 13f
            isAllCaps = false
            setTextColor(color)
            background = roundedBackground(
                cardColor,
                dp(10),
                color
            )
            setOnClickListener { action() }
        }
    }

    private fun confirmClearHistory() {
        if (historyStore.getAll().isEmpty()) {
            renderHistory()
            return
        }

        AlertDialog.Builder(activity)
            .setTitle("مسح سجل التصفح")
            .setMessage("هل تريد حذف جميع الصفحات المحفوظة في السجل؟")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("مسح الكل") { _, _ ->
                historyStore.clear()
                renderHistory()
            }
            .show()
    }

    private fun roundedBackground(
        color: Int,
        radius: Int,
        strokeColor: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()

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
