package com.example.ime.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.data.PreferencesManager
import com.example.engine.SwipeTypingEngine
import com.example.ime.layout.KeyboardLayouts
import com.example.ime.model.KeyModel
import com.example.ime.model.KeyType
import com.example.ime.theme.KeyboardTheme
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onKeyEventListener: ((KeyModel) -> Unit)? = null
    var onTextCommitListener: ((String) -> Unit)? = null
    var onSwipeWordListener: ((String) -> Unit)? = null

    var theme: KeyboardTheme = KeyboardTheme.IOS_LIGHT
        set(value) {
            field = value
            updatePaints()
            invalidate()
        }

    var showKeyPopup: Boolean = true
    var enableSwipeTyping: Boolean = true
    var showNumberRow: Boolean = true
    var heightPreference: String = PreferencesManager.HEIGHT_NORMAL

    private var rows: List<List<KeyModel>> = emptyList()
    private val keyPositions = mutableMapOf<Char, SwipeTypingEngine.KeyPosition>()
    private val swipeEngine = SwipeTypingEngine()

    // Paints
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val actionKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val actionKeyPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val swipeTrailPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Touch & State
    private var activeKey: KeyModel? = null
    private var isLongPressActive: Boolean = false
    private var selectedPopupIndex: Int = -1
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val swipePathPoints = mutableListOf<PointF>()
    private val gesturePath = Path()
    private var isSwiping = false

    private val popupStemPath = Path()
    private val popupRect = RectF()

    init {
        updatePaints()
    }

    private fun updatePaints() {
        bgPaint.color = theme.keyboardBackground
        keyPaint.color = theme.keyBackground
        keyPressedPaint.color = theme.keyBackgroundPressed
        actionKeyPaint.color = theme.actionKeyBackground
        actionKeyPressedPaint.color = theme.actionKeyBackgroundPressed
        keyShadowPaint.color = theme.keyShadowColor
        textPaint.color = theme.primaryTextColor
        textPaint.textAlign = Paint.Align.CENTER
        secondaryTextPaint.color = theme.secondaryTextColor
        secondaryTextPaint.textAlign = Paint.Align.CENTER

        popupBgPaint.color = theme.popupBackgroundColor
        popupTextPaint.color = theme.popupTextColor
        popupTextPaint.textAlign = Paint.Align.CENTER
        popupHighlightPaint.color = theme.accentColor

        swipeTrailPaint.color = theme.gestureTrailColor
        swipeTrailPaint.style = Paint.Style.STROKE
        swipeTrailPaint.strokeWidth = 14f
        swipeTrailPaint.strokeCap = Paint.Cap.ROUND
        swipeTrailPaint.strokeJoin = Paint.Join.ROUND
    }

    fun setLayout(newRows: List<List<KeyModel>>) {
        this.rows = newRows
        if (width > 0 && height > 0) {
            calculateKeyBounds(width, height)
        }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenHeight = displayMetrics.heightPixels

        val calculatedHeight: Int = if (isLandscape) {
            when (heightPreference) {
                PreferencesManager.HEIGHT_COMPACT -> (150 * density).toInt()
                PreferencesManager.HEIGHT_TALL -> (210 * density).toInt()
                else -> (180 * density).toInt()
            }
        } else {
            val ratio = when (heightPreference) {
                PreferencesManager.HEIGHT_COMPACT -> 0.28f
                PreferencesManager.HEIGHT_TALL -> 0.38f
                else -> 0.33f
            }
            val baseDp = when (heightPreference) {
                PreferencesManager.HEIGHT_COMPACT -> 235
                PreferencesManager.HEIGHT_TALL -> 305
                else -> 270
            }
            val dynamicH = (screenHeight * ratio).toInt()
            max(dynamicH, (baseDp * density).toInt())
        }

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> min(calculatedHeight, MeasureSpec.getSize(heightMeasureSpec))
            else -> calculatedHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            calculateKeyBounds(w, h)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val w = right - left
        val h = bottom - top
        if (w > 0 && h > 0) {
            calculateKeyBounds(w, h)
        }
    }

    private fun calculateKeyBounds(width: Int, height: Int) {
        if (rows.isEmpty() || width <= 0 || height <= 0) return

        keyPositions.clear()
        val density = resources.displayMetrics.density
        val horizontalPadding = 4f * density
        val verticalPadding = 4f * density
        val keyMarginX = 2.5f * density
        val keyMarginY = 4f * density

        val availableHeight = height - (verticalPadding * 2)
        val rowHeight = availableHeight / rows.size

        textPaint.textSize = min(rowHeight * 0.44f, 22f * density)
        secondaryTextPaint.textSize = min(rowHeight * 0.24f, 11f * density)
        popupTextPaint.textSize = min(rowHeight * 0.55f, 28f * density)

        for (rowIndex in rows.indices) {
            val row = rows[rowIndex]
            val totalWeight = row.sumOf { it.widthWeight.toDouble() }.toFloat()
            val availableRowWidth = width - (horizontalPadding * 2) - (keyMarginX * 2 * row.size)

            var currentX = horizontalPadding
            val top = verticalPadding + (rowIndex * rowHeight) + keyMarginY
            val bottom = top + rowHeight - (keyMarginY * 2)

            for (key in row) {
                val keyWidth = (key.widthWeight / totalWeight) * availableRowWidth + (keyMarginX * 2)
                val left = currentX + keyMarginX
                val right = left + keyWidth - (keyMarginX * 2)

                key.bounds.set(left, top, right, bottom)
                currentX += keyWidth

                if (key.label.length == 1) {
                    val ch = key.label[0]
                    keyPositions[ch] = SwipeTypingEngine.KeyPosition(ch, key.bounds.centerX(), key.bounds.centerY())
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Fail-safe: ensure key bounds are non-empty
        if (rows.isNotEmpty() && (rows[0].isNotEmpty() && rows[0][0].bounds.width() <= 0f) && width > 0 && height > 0) {
            calculateKeyBounds(width, height)
        }

        // Draw Keyboard background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val density = resources.displayMetrics.density
        val cornerRadius = 8f * density
        val shadowHeight = 2.5f * density

        // Draw Keys
        for (row in rows) {
            for (key in row) {
                val bounds = key.bounds
                if (bounds.width() <= 0f) continue

                // 1. Draw subtle drop shadow beneath key
                if (!key.isPressed) {
                    canvas.drawRoundRect(
                        bounds.left,
                        bounds.top + shadowHeight,
                        bounds.right,
                        bounds.bottom + shadowHeight,
                        cornerRadius,
                        cornerRadius,
                        keyShadowPaint
                    )
                }

                // 2. Key background
                val isAction = key.keyType != KeyType.CHARACTER && key.keyType != KeyType.SPACE
                val paint = when {
                    isAction && key.isPressed -> actionKeyPressedPaint
                    isAction -> actionKeyPaint
                    key.isPressed -> keyPressedPaint
                    else -> keyPaint
                }

                canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)

                // 3. Primary label
                val textY = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(key.label, bounds.centerX(), textY, textPaint)

                // 4. Secondary label (Samsung style)
                if (!key.secondaryLabel.isNullOrEmpty() && key.keyType == KeyType.CHARACTER) {
                    val secX = bounds.right - 7f * density
                    val secY = bounds.top + 9f * density
                    canvas.drawText(key.secondaryLabel!!, secX, secY, secondaryTextPaint)
                }
            }
        }

        // Draw Gesture Trail for Swipe typing
        if (isSwiping && swipePathPoints.size > 1) {
            gesturePath.reset()
            gesturePath.moveTo(swipePathPoints[0].x, swipePathPoints[0].y)
            for (i in 1 until swipePathPoints.size) {
                gesturePath.lineTo(swipePathPoints[i].x, swipePathPoints[i].y)
            }
            canvas.drawPath(gesturePath, swipeTrailPaint)
        }

        // Draw Long-Press Diacritics / Alternates Selector Bubble
        if (isLongPressActive && activeKey != null && activeKey!!.popupCharacters.isNotEmpty()) {
            drawDiacriticsSelector(canvas, activeKey!!)
        }
        // Draw iOS Pop-up Preview Bubble when a character key is pressed
        else if (showKeyPopup && activeKey != null && activeKey!!.isPressed && activeKey!!.keyType == KeyType.CHARACTER) {
            drawIosPopupPreview(canvas, activeKey!!)
        }
    }

    /**
     * Draws an authentic iPhone-style floating key preview bubble above the pressed key.
     */
    private fun drawIosPopupPreview(canvas: Canvas, key: KeyModel) {
        val density = resources.displayMetrics.density
        val bounds = key.bounds

        val popupWidth = bounds.width() * 1.35f
        val popupHeight = bounds.height() * 1.55f
        val popupLeft = bounds.centerX() - popupWidth / 2f
        val popupRight = bounds.centerX() + popupWidth / 2f
        val popupTop = bounds.top - popupHeight * 0.75f

        popupStemPath.reset()
        val corner = 12f * density

        popupRect.set(popupLeft, popupTop, popupRight, popupTop + popupHeight * 0.7f)
        canvas.drawRoundRect(popupRect, corner, corner, popupBgPaint)

        // Draw stem connecting to key
        popupStemPath.moveTo(popupLeft + corner, popupTop + popupHeight * 0.7f)
        popupStemPath.lineTo(bounds.left, bounds.top)
        popupStemPath.lineTo(bounds.right, bounds.top)
        popupStemPath.lineTo(popupRight - corner, popupTop + popupHeight * 0.7f)
        popupStemPath.close()
        canvas.drawPath(popupStemPath, popupBgPaint)

        // Draw letter inside bubble
        val letterY = popupRect.centerY() - (popupTextPaint.descent() + popupTextPaint.ascent()) / 2
        canvas.drawText(key.label, popupRect.centerX(), letterY, popupTextPaint)
    }

    /**
     * Draws horizontal selector bar containing alternate characters / diacritics on long press.
     */
    private fun drawDiacriticsSelector(canvas: Canvas, key: KeyModel) {
        val density = resources.displayMetrics.density
        val chars = key.popupCharacters
        if (chars.isEmpty()) return

        val itemWidth = 38f * density
        val barHeight = 48f * density
        val barWidth = itemWidth * chars.size
        val corner = 10f * density

        var barLeft = key.bounds.centerX() - barWidth / 2f
        barLeft = max(8f * density, min(barLeft, width - barWidth - 8f * density))
        val barRight = barLeft + barWidth
        val barTop = max(8f * density, key.bounds.top - barHeight - 12f * density)
        val barBottom = barTop + barHeight

        val barRect = RectF(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(barRect, corner, corner, popupBgPaint)

        for (i in chars.indices) {
            val itemLeft = barLeft + (i * itemWidth)
            val itemRight = itemLeft + itemWidth
            val itemRect = RectF(itemLeft, barTop + 4f * density, itemRight, barBottom - 4f * density)

            if (i == selectedPopupIndex) {
                canvas.drawRoundRect(itemRect, 6f * density, 6f * density, popupHighlightPaint)
            }

            val textY = itemRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(chars[i], itemRect.centerX(), textY, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val key = findKeyAt(x, y)
                activeKey = key
                isLongPressActive = false
                selectedPopupIndex = -1
                isSwiping = false
                swipePathPoints.clear()

                if (key != null) {
                    key.isPressed = true
                    swipePathPoints.add(PointF(x, y))

                    // Long press timer (for diacritics / alternates)
                    longPressHandler.removeCallbacksAndMessages(null)
                    longPressHandler.postDelayed({
                        if (activeKey == key && key.isPressed) {
                            if (key.popupCharacters.isNotEmpty()) {
                                isLongPressActive = true
                                selectedPopupIndex = 0
                                invalidate()
                            }
                        }
                    }, 300)
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isLongPressActive && activeKey != null && activeKey!!.popupCharacters.isNotEmpty()) {
                    val chars = activeKey!!.popupCharacters
                    val density = resources.displayMetrics.density
                    val itemWidth = 38f * density
                    val barWidth = itemWidth * chars.size
                    var barLeft = activeKey!!.bounds.centerX() - barWidth / 2f
                    barLeft = max(8f * density, min(barLeft, width - barWidth - 8f * density))

                    val relativeX = x - barLeft
                    val idx = (relativeX / itemWidth).toInt()
                    selectedPopupIndex = max(0, min(idx, chars.size - 1))
                    invalidate()
                    return true
                }

                // Check for swipe gesture
                if (enableSwipeTyping && activeKey?.keyType == KeyType.CHARACTER && !isLongPressActive) {
                    val lastPt = swipePathPoints.lastOrNull()
                    if (lastPt != null && hypot((x - lastPt.x).toDouble(), (y - lastPt.y).toDouble()) > 10) {
                        swipePathPoints.add(PointF(x, y))
                        if (swipePathPoints.size > 3) {
                            isSwiping = true
                            longPressHandler.removeCallbacksAndMessages(null)
                        }
                        invalidate()
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                longPressHandler.removeCallbacksAndMessages(null)

                if (isLongPressActive && activeKey != null && selectedPopupIndex >= 0) {
                    val selectedChar = activeKey!!.popupCharacters.getOrNull(selectedPopupIndex)
                    if (selectedChar != null) {
                        onTextCommitListener?.invoke(selectedChar)
                    }
                } else if (isSwiping && swipePathPoints.size > 4) {
                    val dictionary = if (activeKey?.label?.firstOrNull()?.isArabicLetter() == true) {
                        com.example.engine.BuiltInDictionary.COMMON_ARABIC_WORDS
                    } else {
                        com.example.engine.BuiltInDictionary.COMMON_ENGLISH_WORDS
                    }
                    val matchedWord = swipeEngine.findWordForPath(swipePathPoints, keyPositions, dictionary)
                    if (!matchedWord.isNullOrEmpty()) {
                        onSwipeWordListener?.invoke(matchedWord)
                    } else if (activeKey != null) {
                        onKeyEventListener?.invoke(activeKey!!)
                    }
                } else if (activeKey != null) {
                    onKeyEventListener?.invoke(activeKey!!)
                }

                activeKey?.isPressed = false
                activeKey = null
                isLongPressActive = false
                isSwiping = false
                selectedPopupIndex = -1
                swipePathPoints.clear()
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                longPressHandler.removeCallbacksAndMessages(null)
                activeKey?.isPressed = false
                activeKey = null
                isLongPressActive = false
                isSwiping = false
                swipePathPoints.clear()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findKeyAt(x: Float, y: Float): KeyModel? {
        for (row in rows) {
            for (key in row) {
                if (key.bounds.contains(x, y)) {
                    return key
                }
            }
        }
        val maxTolerance = 24f * resources.displayMetrics.density
        var closestKey: KeyModel? = null
        var minDist = maxTolerance
        for (row in rows) {
            for (key in row) {
                val d = hypot((x - key.bounds.centerX()).toDouble(), (y - key.bounds.centerY()).toDouble()).toFloat()
                if (d < minDist) {
                    minDist = d
                    closestKey = key
                }
            }
        }
        return closestKey
    }

    private fun Char.isArabicLetter(): Boolean {
        return this in '\u0600'..'\u06FF' || this in '\u0750'..'\u077F'
    }
}
