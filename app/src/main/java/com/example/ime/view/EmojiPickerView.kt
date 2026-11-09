package com.example.ime.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.ime.theme.KeyboardTheme

class EmojiPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onEmojiSelected: ((String) -> Unit)? = null
    var onBackToKeyboardClicked: (() -> Unit)? = null
    var onDeleteClicked: (() -> Unit)? = null

    private var theme: KeyboardTheme = KeyboardTheme.IOS_LIGHT

    private val headerLayout = LinearLayout(context)
    private val categoryScroll = HorizontalScrollView(context)
    private val categoryContainer = LinearLayout(context)
    private val emojiScrollView = ScrollView(context)
    private val emojiGrid = GridLayout(context)

    private val categories = listOf(
        "😀 الوجوه" to listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹",
            "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗",
            "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓",
            "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
            "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😮‍💨"
        ),
        "👋 الإيماءات" to listOf(
            "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
            "🫰", "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️",
            "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲",
            "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶"
        ),
        "❤️ القلوب" to listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
            "💟", "☮️", "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️"
        ),
        "🎉 الاحتفالات" to listOf(
            "✨", "🎉", "🎊", "🎈", "🎁", "🏆", "🥇", "🥈", "🥉", "🏅",
            "🎖️", "🎗️", "🎫", "🎟️", "🎪", "🎭", "🎨", "🎬", "🎤", "🎧",
            "🎼", "🎹", "🥁", "🎷", "🎺", "🎸", "🪕", "🎻", "🎲", "🎯"
        ),
        "🍔 الطعام" to listOf(
            "☕", "🧋", "🍵", "🥤", "🧃", "🍎", "🍏", "🍊", "🍋", "🍌",
            "🍉", "🍇", "🍓", "🫐", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝",
            "🍕", "🍔", "🍟", "🌭", "🍿", "🧈", "🥞", "🧇", "🧀", "🍗"
        )
    )

    private var selectedCategoryIndex = 0

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density
        setPadding(
            (6 * density).toInt(),
            (4 * density).toInt(),
            (6 * density).toInt(),
            (4 * density).toInt()
        )

        setupHeader()
        setupCategoryTabs()
        setupEmojiGrid()
        displayCategory(0)
    }

    private fun setupHeader() {
        val density = resources.displayMetrics.density
        headerLayout.orientation = HORIZONTAL
        headerLayout.gravity = Gravity.CENTER_VERTICAL

        val btnBack = TextView(context).apply {
            text = "ABC / ع"
            textSize = 14f
            setPadding((12 * density).toInt(), (6 * density).toInt(), (12 * density).toInt(), (6 * density).toInt())
            setOnClickListener { onBackToKeyboardClicked?.invoke() }
        }

        val spacer = TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnDelete = TextView(context).apply {
            text = "⌫"
            textSize = 18f
            setPadding((12 * density).toInt(), (6 * density).toInt(), (12 * density).toInt(), (6 * density).toInt())
            setOnClickListener { onDeleteClicked?.invoke() }
        }

        headerLayout.addView(btnBack)
        headerLayout.addView(spacer)
        headerLayout.addView(btnDelete)
        addView(headerLayout)
    }

    private fun setupCategoryTabs() {
        val density = resources.displayMetrics.density
        categoryScroll.isHorizontalScrollBarEnabled = false
        categoryContainer.orientation = HORIZONTAL

        for (i in categories.indices) {
            val tab = TextView(context).apply {
                text = categories[i].first
                textSize = 13f
                setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
                setOnClickListener {
                    displayCategory(i)
                }
            }
            categoryContainer.addView(tab)
        }

        categoryScroll.addView(categoryContainer)
        addView(categoryScroll)
    }

    private fun setupEmojiGrid() {
        emojiGrid.columnCount = 7
        emojiGrid.alignmentMode = GridLayout.ALIGN_BOUNDS
        emojiScrollView.addView(emojiGrid)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
        addView(emojiScrollView, params)
    }

    private fun displayCategory(index: Int) {
        selectedCategoryIndex = index
        emojiGrid.removeAllViews()
        val density = resources.displayMetrics.density
        val emojis = categories[index].second

        for (em in emojis) {
            val tv = TextView(context).apply {
                text = em
                textSize = 24f
                gravity = Gravity.CENTER
                val pad = (6 * density).toInt()
                setPadding(pad, pad, pad, pad)
                setOnClickListener {
                    onEmojiSelected?.invoke(em)
                }
            }
            emojiGrid.addView(tv)
        }
    }

    fun setTheme(theme: KeyboardTheme) {
        this.theme = theme
        setBackgroundColor(theme.keyboardBackground)
    }
}
