package com.example.ime.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.data.ClipboardItemEntity
import com.example.ime.theme.KeyboardTheme

class ClipboardPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onClipSelected: ((String) -> Unit)? = null
    var onCloseClicked: (() -> Unit)? = null
    var onClearClicked: (() -> Unit)? = null

    private val headerLayout = LinearLayout(context)
    private val scrollView = HorizontalScrollView(context)
    private val clipsContainer = LinearLayout(context)
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

        // Header: Title + Clear + Close
        headerLayout.orientation = HORIZONTAL
        headerLayout.gravity = Gravity.CENTER_VERTICAL

        titleText.apply {
            text = "📋 الحافظة (آخر 10 نصوص)"
            textSize = 14f
            val params = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            layoutParams = params
        }

        val btnClear = TextView(context).apply {
            text = "مسح الكل"
            textSize = 12f
            setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
            setOnClickListener { onClearClicked?.invoke() }
        }

        val btnClose = TextView(context).apply {
            text = "✕ إغلاق"
            textSize = 13f
            setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
            setOnClickListener { onCloseClicked?.invoke() }
        }

        headerLayout.addView(titleText)
        headerLayout.addView(btnClear)
        headerLayout.addView(btnClose)

        // Scrollable horizontal clips
        scrollView.apply {
            isHorizontalScrollBarEnabled = false
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            layoutParams = params
        }

        clipsContainer.apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            layoutParams = params
        }
        scrollView.addView(clipsContainer)

        addView(headerLayout)
        addView(scrollView)
    }

    fun setTheme(theme: KeyboardTheme) {
        this.theme = theme
        setBackgroundColor(theme.keyboardBackground)
        titleText.setTextColor(theme.primaryTextColor)
    }

    fun setClips(clips: List<ClipboardItemEntity>) {
        clipsContainer.removeAllViews()
        val density = resources.displayMetrics.density

        if (clips.isEmpty()) {
            val emptyTv = TextView(context).apply {
                text = "لا توجد عناصر منسوخة حالياً"
                textSize = 13f
                setTextColor(theme.secondaryTextColor)
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
            }
            clipsContainer.addView(emptyTv)
            return
        }

        for (item in clips) {
            val card = TextView(context).apply {
                text = item.text
                textSize = 14f
                maxLines = 2
                maxEms = 12
                setTextColor(theme.primaryTextColor)
                setPadding((12 * density).toInt(), (8 * density).toInt(), (12 * density).toInt(), (8 * density).toInt())

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

                setOnClickListener {
                    onClipSelected?.invoke(item.text)
                }
            }
            clipsContainer.addView(card)
        }
    }
}
