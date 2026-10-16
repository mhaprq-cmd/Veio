package com.veio.browser

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

/**
 * واجهة الشريط الجانبي لمتصفح Veio.
 *
 * هذا الملف مسؤول عن بناء الواجهة فقط.
 * يتم تمرير أحداث السجل ووضع سطح المكتب وحظر الإعلانات
 * إلى MainActivity من خلال الدوال الممررة إلى المنشئ.
 */
class BrowserDrawer(
    private val activity: Activity,
    desktopModeEnabled: Boolean = false,
    adBlockEnabled: Boolean = false,
    private val onHistoryClick: () -> Unit,
    private val onDesktopModeChanged: (Boolean) -> Unit,
    private val onAdBlockChanged: (Boolean) -> Unit,
    private val onCloseClick: () -> Unit
) {

    private val density =
        activity.resources.displayMetrics.density

    private val isDark: Boolean
        get() = (
            activity.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
            ) == android.content.res.Configuration.UI_MODE_NIGHT_YES

    private val backgroundColor: Int
        get() = if (isDark) {
            Color.rgb(25, 28, 35)
        } else {
            Color.WHITE
        }

    private val textColor: Int
        get() = if (isDark) {
            Color.rgb(239, 242, 248)
        } else {
            Color.rgb(35, 40, 55)
        }

    private val secondaryColor: Int
        get() = if (isDark) {
            Color.rgb(166, 174, 190)
        } else {
            Color.rgb(105, 112, 128)
        }

    private val accentColor = Color.rgb(78, 128, 224)

    private val dividerColor: Int
        get() = if (isDark) {
            Color.rgb(62, 68, 80)
        } else {
            Color.rgb(224, 228, 238)
        }

    private lateinit var desktopSwitch: Switch
    private lateinit var adBlockSwitch: Switch

    /**
     * اللوحة التي ستوضع لاحقًا داخل طبقة جانبية
     * فوق واجهة المتصفح.
     */
    val view: View = createDrawer(
        desktopModeEnabled,
        adBlockEnabled
    )

    /**
     * تحديث حالة مفتاح سطح المكتب من MainActivity.
     */
    fun setDesktopModeEnabled(enabled: Boolean) {
        if (!::desktopSwitch.isInitialized) return

        desktopSwitch.setOnCheckedChangeListener(null)
        desktopSwitch.isChecked = enabled

        desktopSwitch.setOnCheckedChangeListener { _, checked ->
            onDesktopModeChanged(checked)
        }
    }

    /**
     * تحديث حالة مفتاح حظر الإعلانات من MainActivity.
     */
    fun setAdBlockEnabled(enabled: Boolean) {
        if (!::adBlockSwitch.isInitialized) return

        adBlockSwitch.setOnCheckedChangeListener(null)
        adBlockSwitch.isChecked = enabled

        adBlockSwitch.setOnCheckedChangeListener { _, checked ->
            onAdBlockChanged(checked)
        }
    }

    private fun createDrawer(
        desktopEnabled: Boolean,
        adBlockEnabled: Boolean
    ): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(backgroundColor)
            elevation = dp(16).toFloat()

            addView(
                createHeader(),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(64)
                )
            )

            addView(createDivider())

            addView(
                createMenuItem(
                    symbol = "◷",
                    title = "السجل",
                    description = "الصفحات التي زرتها"
                ) {
                    onCloseClick()
                    onHistoryClick()
                }
            )

            addView(createDivider())

            addView(
                createSwitchItem(
                    symbol = "▣",
                    title = "سطح المكتب",
                    description = "عرض المواقع بإصدار سطح المكتب",
                    initialValue = desktopEnabled,
                    onSwitchCreated = { desktopSwitch = it },
                    onChanged = onDesktopModeChanged
                )
            )

            addView(createDivider())

            addView(
                createSwitchItem(
                    symbol = "⛨",
                    title = "حظر الإعلانات",
                    description = "حظر الإعلانات وأدوات التتبع",
                    initialValue = adBlockEnabled,
                    onSwitchCreated = { adBlockSwitch = it },
                    onChanged = onAdBlockChanged
                )
            )

            addView(createDivider())

            val futureSection = TextView(activity).apply {
                text = "المزيد من الخيارات لاحقًا"
                textSize = 12f
                setTextColor(secondaryColor)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(
                    dp(20),
                    dp(16),
                    dp(20),
                    dp(16)
                )
            }

            addView(futureSection)
        }
    }

    private fun createHeader(): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(16), 0, dp(12), 0)

            val title = TextView(activity).apply {
                text = "Veio"
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(accentColor)
                gravity = Gravity.CENTER_VERTICAL
            }

            addView(
                title,
                LinearLayout.LayoutParams(0, -1, 1f)
            )

            val closeButton = TextView(activity).apply {
                text = "×"
                textSize = 30f
                gravity = Gravity.CENTER
                setTextColor(textColor)
                contentDescription = "إغلاق القائمة"
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onCloseClick()
                }
            }

            addView(
                closeButton,
                LinearLayout.LayoutParams(dp(44), dp(48))
            )
        }
    }

    private fun createMenuItem(
        symbol: String,
        title: String,
        description: String,
        onClick: () -> Unit
    ): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(18), dp(10), dp(18), dp(10))
            minimumHeight = dp(68)
            isClickable = true
            isFocusable = true
            setBackgroundResource(
                android.R.drawable.list_selector_background
            )

            addView(
                createSymbol(symbol),
                LinearLayout.LayoutParams(dp(42), dp(42))
            )

            val labels = createLabels(title, description)

            addView(
                labels,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            setOnClickListener {
                onClick()
            }
        }
    }

    private fun createSwitchItem(
        symbol: String,
        title: String,
        description: String,
        initialValue: Boolean,
        onSwitchCreated: (Switch) -> Unit,
        onChanged: (Boolean) -> Unit
    ): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(18), dp(10), dp(12), dp(10))
            minimumHeight = dp(76)

            addView(
                createSymbol(symbol),
                LinearLayout.LayoutParams(dp(42), dp(42))
            )

            addView(
                createLabels(title, description),
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            val toggle = Switch(activity).apply {
                isChecked = initialValue
                contentDescription = title
                setOnCheckedChangeListener { _, checked ->
                    onChanged(checked)
                }
            }

            onSwitchCreated(toggle)

            addView(
                toggle,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun createLabels(
        title: String,
        description: String
    ): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, dp(6), 0)

            addView(
                TextView(activity).apply {
                    text = title
                    textSize = 15f
                    setTextColor(textColor)
                }
            )

            addView(
                TextView(activity).apply {
                    text = description
                    textSize = 11f
                    setTextColor(secondaryColor)
                    setPadding(0, dp(4), 0, 0)
                }
            )
        }
    }

    private fun createSymbol(symbol: String): TextView {
        return TextView(activity).apply {
            text = symbol
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(accentColor)
        }
    }

    private fun createDivider(): View {
        return View(activity).apply {
            setBackgroundColor(dividerColor)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * density).toInt()
    }
}
