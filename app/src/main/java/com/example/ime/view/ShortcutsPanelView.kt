package com.example.ime.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.data.ShortcutEntity
import com.example.ime.theme.KeyboardTheme

class ShortcutsPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onShortcutSelected: ((String) -> Unit)? = null
    var onCloseClicked: (() -> Unit)? = null
    var onManageClicked: (() -> Unit)? = null

    private val headerLayout = LinearLayout(context)
    private val scrollView = HorizontalScrollView(context)
    private val shortcutsContainer = LinearLayout(context)
    private val titleText = TextView(context)
    private var theme: KeyboardTheme = KeyboardTheme.IOS_LIGHT

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density
        setPadding(
            (8 * density).toInt(),
            (6 * density).toInt(),
            (8 * density).toInt(),
            (6 * density).toInt()
        )

        // Header: Title + Manage + Close
        headerLayout.orientation = HORIZONTAL
        headerLayout.gravity = Gravity.CENTER_VERTICAL

        titleText.apply {
            text = "⚡ الاختصارات النصية السريعة"
            textSize = 14f
            val params = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            layoutParams = params
        }

        val btnManage = TextView(context).apply {
            text = "⚙️ إدارة"
            textSize = 12f
            setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
            setOnClickListener { onManageClicked?.invoke() }
        }

        val btnClose = TextView(context).apply {
            text = "✕ إغلاق"
            textSize = 13f
            setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
            setOnClickListener { onCloseClicked?.invoke() }
        }

        headerLayout.addView(titleText)
        headerLayout.addView(btnManage)
        headerLayout.addView(btnClose)

        // Scrollable horizontal shortcuts chips
        scrollView.apply {
            isHorizontalScrollBarEnabled = false
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            layoutParams = params
        }

        shortcutsContainer.apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            layoutParams = params
        }
        scrollView.addView(shortcutsContainer)

        addView(headerLayout)
        addView(scrollView)
    }

    fun setTheme(theme: KeyboardTheme) {
        this.theme = theme
        setBackgroundColor(theme.keyboardBackground)
        titleText.setTextColor(theme.primaryTextColor)
    }

    fun setShortcuts(shortcuts: List<ShortcutEntity>) {
        shortcutsContainer.removeAllViews()
        val density = resources.displayMetrics.density

        if (shortcuts.isEmpty()) {
            val emptyTv = TextView(context).apply {
                text = "لا توجد اختصارات محفوظة حالياً"
                textSize = 13f
                setTextColor(theme.secondaryTextColor)
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
            }
            shortcutsContainer.addView(emptyTv)
            return
        }

        for (item in shortcuts) {
            val chip = LinearLayout(context).apply {
                orientation = VERTICAL
                val padH = (12 * density).toInt()
                val padV = (6 * density).toInt()
                setPadding(padH, padV, padH, padV)

                val bgDrawable = GradientDrawable().apply {
                    setColor(theme.keyBackground)
                    cornerRadius = 8f * density
                    setStroke((1 * density).toInt(), theme.keyShadowColor)
                }
                background = bgDrawable

                val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins((4 * density).toInt(), (6 * density).toInt(), (4 * density).toInt(), (6 * density).toInt())
                }
                layoutParams = params

                val triggerView = TextView(context).apply {
                    text = "⚡ " + item.triggerText
                    textSize = 13f
                    setTextColor(theme.accentColor)
                }

                val expandedView = TextView(context).apply {
                    text = item.expandedText
                    textSize = 12f
                    maxLines = 1
                    maxEms = 14
                    setTextColor(theme.primaryTextColor)
                }

                addView(triggerView)
                addView(expandedView)

                setOnClickListener {
                    onShortcutSelected?.invoke(item.expandedText)
                }
            }
            shortcutsContainer.addView(chip)
        }
    }
}
