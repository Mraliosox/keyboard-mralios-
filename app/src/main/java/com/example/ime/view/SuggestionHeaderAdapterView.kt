package com.example.ime.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.ime.theme.KeyboardTheme

/**
 * Custom Suggestion Header Adapter View for Mralios Keyboard.
 * Combines Samsung's top function toolbar with iPhone's polished rounded suggestion cells.
 * 
 * Solves Problem #2: Displays clean, genuine candidate words and automatically
 * hides itself (View.GONE) when empty, completely eliminating empty placeholder dots.
 */
open class SuggestionHeaderAdapterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onSuggestionClicked: ((String) -> Unit)? = null
    var onClipboardClicked: (() -> Unit)? = null
    var onVoiceClicked: (() -> Unit)? = null
    var onShortcutsClicked: (() -> Unit)? = null
    var onSettingsClicked: (() -> Unit)? = null
    var onThemeToggleClicked: (() -> Unit)? = null

    private var theme: KeyboardTheme = KeyboardTheme.IOS_LIGHT

    private val toolbarRow = LinearLayout(context)
    private val suggestionRow = LinearLayout(context)
    private val suggestionCells = ArrayList<TextView>(3)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        setupViews()
    }

    private fun setupViews() {
        removeAllViews()
        val density = resources.displayMetrics.density

        // 1. Top Samsung-style function toolbar
        toolbarRow.orientation = HORIZONTAL
        toolbarRow.gravity = Gravity.CENTER_VERTICAL
        toolbarRow.layoutDirection = View.LAYOUT_DIRECTION_RTL
        val toolPadH = (8 * density).toInt()
        val toolPadV = (2 * density).toInt()
        toolbarRow.setPadding(toolPadH, toolPadV, toolPadH, toolPadV)
        val toolbarParams = LayoutParams(LayoutParams.MATCH_PARENT, (34 * density).toInt())
        toolbarRow.layoutParams = toolbarParams

        val btnClipboard = createToolButton("📋", "الحافظة") { onClipboardClicked?.invoke() }
        val btnShortcuts = createToolButton("⚡", "الاختصارات") { onShortcutsClicked?.invoke() }
        val btnVoice = createToolButton("🎙️", "إدخال صوتي") { onVoiceClicked?.invoke() }
        val btnTheme = createToolButton("🌓", "تبديل المظهر") { onThemeToggleClicked?.invoke() }
        val btnSettings = createToolButton("⚙️", "الإعدادات") { onSettingsClicked?.invoke() }

        toolbarRow.addView(btnClipboard)
        toolbarRow.addView(btnShortcuts)
        toolbarRow.addView(btnVoice)
        toolbarRow.addView(btnTheme)
        toolbarRow.addView(btnSettings)

        // 2. Suggestion bar row (starts GONE; expands dynamically when words are available)
        suggestionRow.orientation = HORIZONTAL
        suggestionRow.gravity = Gravity.CENTER_VERTICAL
        suggestionRow.layoutDirection = View.LAYOUT_DIRECTION_RTL
        val sugPadH = (6 * density).toInt()
        val sugPadV = (2 * density).toInt()
        suggestionRow.setPadding(sugPadH, sugPadV, sugPadH, (4 * density).toInt())
        val sugParams = LayoutParams(LayoutParams.MATCH_PARENT, (38 * density).toInt())
        suggestionRow.layoutParams = sugParams
        suggestionRow.visibility = View.GONE // Hidden by default when empty

        suggestionCells.clear()
        for (i in 0 until 3) {
            val tv = TextView(context).apply {
                gravity = Gravity.CENTER
                textSize = 15f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                val padH = (8 * density).toInt()
                val padV = (4 * density).toInt()
                setPadding(padH, padV, padH, padV)

                val cellParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f).apply {
                    setMargins((3 * density).toInt(), 0, (3 * density).toInt(), 0)
                }
                layoutParams = cellParams
                visibility = View.GONE // Initial state

                setOnClickListener {
                    val word = text.toString().trim()
                    if (word.isNotEmpty()) {
                        onSuggestionClicked?.invoke(word)
                    }
                }
            }
            suggestionCells.add(tv)
            suggestionRow.addView(tv)
        }

        addView(toolbarRow)
        addView(suggestionRow)
    }

    private fun createToolButton(icon: String, description: String, onClick: () -> Unit): TextView {
        val density = resources.displayMetrics.density
        return TextView(context).apply {
            text = icon
            contentDescription = description
            textSize = 15f
            gravity = Gravity.CENTER
            val pad = (6 * density).toInt()
            setPadding(pad, 0, pad, 0)
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                setMargins((4 * density).toInt(), 0, (4 * density).toInt(), 0)
            }
            layoutParams = params
            setOnClickListener { onClick() }
        }
    }

    /**
     * Updates candidate words in the suggestion strip.
     * Guaranteed: If list is empty, hides the row completely (View.GONE) to prevent
     * any empty circles, dots, or blank placeholders from rendering.
     */
    fun setSuggestions(suggestions: List<String>) {
        val validWords = suggestions
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "." && it != "،" && it != "؟" }

        if (validWords.isEmpty()) {
            suggestionRow.visibility = View.GONE
            for (tv in suggestionCells) {
                tv.text = ""
                tv.visibility = View.GONE
            }
            return
        }

        suggestionRow.visibility = View.VISIBLE

        for (i in 0 until 3) {
            val tv = suggestionCells[i]
            val word = validWords.getOrNull(i)
            if (!word.isNullOrEmpty()) {
                tv.text = word
                tv.visibility = View.VISIBLE
            } else {
                tv.text = ""
                tv.visibility = View.GONE
            }
        }
    }

    /**
     * Applies styling matching active KeyboardTheme.
     */
    fun setTheme(newTheme: KeyboardTheme) {
        this.theme = newTheme
        setBackgroundColor(newTheme.suggestionStripBackground)

        val density = resources.displayMetrics.density
        for (i in suggestionCells.indices) {
            val tv = suggestionCells[i]
            val bg = GradientDrawable().apply {
                cornerRadius = 8f * density
            }

            if (i == 1) {
                // Primary auto-correct candidate (center cell)
                bg.setColor(newTheme.keyBackground)
                bg.setStroke((1.5f * density).toInt(), newTheme.suggestionHighlightColor)
                tv.background = bg
                tv.setTextColor(newTheme.suggestionHighlightColor)
                tv.typeface = Typeface.DEFAULT_BOLD
            } else {
                // Secondary candidate
                bg.setColor(newTheme.keyBackground.let {
                    Color.argb(80, Color.red(it), Color.green(it), Color.blue(it))
                })
                tv.background = bg
                tv.setTextColor(newTheme.suggestionTextColor)
                tv.typeface = Typeface.DEFAULT
            }
        }
    }
}
