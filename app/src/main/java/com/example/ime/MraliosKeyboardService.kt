package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.ClipboardItemEntity
import com.example.data.PreferencesManager
import com.example.data.ShortcutEntity
import com.example.engine.DictionaryManager
import com.example.engine.ShortcutsManager
import com.example.engine.SoundManager
import com.example.ime.layout.KeyboardLayouts
import com.example.ime.model.KeyModel
import com.example.ime.theme.KeyboardTheme
import com.example.ime.view.ClipboardPanelView
import com.example.ime.view.EmojiPickerView
import com.example.ime.view.KeyboardView
import com.example.ime.view.ShortcutsPanelView
import com.example.ime.view.SuggestionHeaderAdapterView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Mralios Keyboard Service (v1.2) - Core IME service.
 * 
 * Features:
 * - Hybrid iPhone (smooth round keys, popups, shadows) + Samsung (number row, toolbar, clipboard).
 * - Instant text shortcut auto-expansion on Space (Problem #1 solved).
 * - Intelligent suggestion feeding with zero placeholder dots (Problem #2 solved).
 * - Full Arabic & English support with diacritics and Levenshtein typo correction.
 */
open class MraliosKeyboardService : InputMethodService(), SharedPreferences.OnSharedPreferenceChangeListener {

    protected lateinit var prefs: PreferencesManager
    protected lateinit var dictionaryManager: DictionaryManager
    protected lateinit var shortcutsManager: ShortcutsManager
    protected lateinit var database: AppDatabase
    protected lateinit var soundManager: SoundManager
    protected val scope = CoroutineScope(Dispatchers.Main)

    private var vibrator: Vibrator? = null
    private var clipboardManager: ClipboardManager? = null
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener { captureClipboard() }

    // UI Components
    protected lateinit var rootContainer: LinearLayout
    protected lateinit var suggestionHeaderView: SuggestionHeaderAdapterView
    protected lateinit var keyboardView: KeyboardView
    protected lateinit var clipboardPanelView: ClipboardPanelView
    protected lateinit var shortcutsPanelView: ShortcutsPanelView
    protected lateinit var emojiPickerView: EmojiPickerView

    // Keyboard State
    protected var isArabic: Boolean = true
    protected var isShifted: Boolean = false
    protected var isCapsLock: Boolean = false
    private var lastShiftTime: Long = 0
    protected var isSymbols: Boolean = false
    protected var currentTheme: KeyboardTheme = KeyboardTheme.IOS_LIGHT

    protected val currentComposingWord = StringBuilder()
    protected var previousWord: String? = null
    private var lastSpaceTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        shortcutsManager = ShortcutsManager.getInstance(this)
        dictionaryManager = DictionaryManager(this)
        database = AppDatabase.getInstance(this)
        soundManager = SoundManager(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        isArabic = prefs.primaryLanguage == "ar"

        try {
            clipboardManager?.addPrimaryClipChangedListener(clipListener)
            prefs.prefs.registerOnSharedPreferenceChangeListener(this)
        } catch (_: Exception) {}

        // Listen for shortcuts updates from Settings
        shortcutsManager.addListener {
            reloadShortcuts()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferencesManager.KEY_SHORTCUTS_VERSION,
            ShortcutsManager.KEY_SHORTCUTS_VERSION -> {
                reloadShortcuts()
            }
            PreferencesManager.KEY_THEME,
            PreferencesManager.KEY_HEIGHT -> {
                applySettings()
                updateLayout()
            }
        }
    }

    private fun reloadShortcuts() {
        dictionaryManager.loadDataFromDb()
        updateShortcutsPanelData()
    }

    private fun captureClipboard() {
        try {
            val clip = clipboardManager?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.text?.toString()?.trim()
                if (!text.isNullOrEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            database.clipboardDao().insert(ClipboardItemEntity(text = text))
                            database.clipboardDao().trimToTen()
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {}
    }

    override fun onCreateInputView(): View {
        rootContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // 1. Suggestion Header & Toolbar View
        suggestionHeaderView = SuggestionHeaderAdapterView(this).apply {
            onSuggestionClicked = { word ->
                commitSuggestion(word)
            }
            onClipboardClicked = {
                toggleClipboardView()
            }
            onShortcutsClicked = {
                toggleShortcutsView()
            }
            onVoiceClicked = {
                launchVoiceInput()
            }
            onThemeToggleClicked = {
                cycleTheme()
            }
            onSettingsClicked = {
                openSettings()
            }
        }
        rootContainer.addView(suggestionHeaderView)

        // 2. Shortcuts Drawer Panel
        shortcutsPanelView = ShortcutsPanelView(this).apply {
            visibility = View.GONE
            onShortcutSelected = { fullText ->
                handleDirectTextCommit(fullText)
                visibility = View.GONE
                keyboardView.visibility = View.VISIBLE
            }
            onCloseClicked = {
                visibility = View.GONE
                keyboardView.visibility = View.VISIBLE
            }
            onManageClicked = {
                openSettings()
            }
        }
        rootContainer.addView(shortcutsPanelView)

        // 3. Clipboard Drawer Panel
        clipboardPanelView = ClipboardPanelView(this).apply {
            visibility = View.GONE
            onClipSelected = { clipText ->
                handleDirectTextCommit(clipText)
                visibility = View.GONE
                keyboardView.visibility = View.VISIBLE
            }
            onCloseClicked = {
                visibility = View.GONE
                keyboardView.visibility = View.VISIBLE
            }
            onClearClicked = {
                scope.launch(Dispatchers.IO) {
                    try {
                        database.clipboardDao().clearAll()
                    } catch (_: Exception) {}
                }
            }
        }
        rootContainer.addView(clipboardPanelView)

        // 4. Emoji Picker View
        emojiPickerView = EmojiPickerView(this).apply {
            visibility = View.GONE
            onEmojiSelected = { emoji ->
                handleDirectTextCommit(emoji)
            }
            onBackToKeyboardClicked = {
                visibility = View.GONE
                keyboardView.visibility = View.VISIBLE
            }
        }
        rootContainer.addView(emojiPickerView)

        // 5. Main Keyboard Canvas View
        keyboardView = KeyboardView(this).apply {
            onKeyEventListener = { key ->
                handleKeyPress(key)
            }
            onTextCommitListener = { text ->
                handleDirectTextCommit(text)
            }
            onSwipeWordListener = { word ->
                handleSwipeWordCommit(word)
            }
        }
        rootContainer.addView(keyboardView)

        applySettings()
        updateLayout()
        updateShortcutsPanelData()

        return rootContainer
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        resetComposingState()
        applySettings()
        updateLayout()
        updateShortcutsPanelData()

        keyboardView.visibility = View.VISIBLE
        shortcutsPanelView.visibility = View.GONE
        clipboardPanelView.visibility = View.GONE
        emojiPickerView.visibility = View.GONE

        updateSuggestions()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySettings()
        updateLayout()
    }

    protected fun applySettings() {
        soundManager.configure(
            enabled = prefs.soundEnabled,
            profile = prefs.soundProfile,
            volumePercent = prefs.soundVolume,
            customUri = prefs.customSoundUri
        )

        if (!::keyboardView.isInitialized) return

        currentTheme = KeyboardTheme.getTheme(prefs.theme, this)

        keyboardView.theme = currentTheme
        keyboardView.showKeyPopup = prefs.showKeyPopup
        keyboardView.enableSwipeTyping = prefs.enableSwipeTyping
        keyboardView.showNumberRow = prefs.showNumberRow
        keyboardView.heightPreference = prefs.keyboardHeight

        if (::suggestionHeaderView.isInitialized) {
            suggestionHeaderView.setTheme(currentTheme)
            suggestionHeaderView.visibility = if (prefs.showSuggestions) View.VISIBLE else View.GONE
        }

        if (::shortcutsPanelView.isInitialized) shortcutsPanelView.setTheme(currentTheme)
        if (::clipboardPanelView.isInitialized) clipboardPanelView.setTheme(currentTheme)
        if (::emojiPickerView.isInitialized) emojiPickerView.setTheme(currentTheme)
    }

    protected fun updateLayout() {
        if (!::keyboardView.isInitialized) return
        val showNum = prefs.showNumberRow
        val rows = when {
            isSymbols -> KeyboardLayouts.getSymbolsLayout(showNum)
            isArabic -> KeyboardLayouts.getArabicLayout(showNum)
            else -> KeyboardLayouts.getEnglishLayout(showNum, isShifted || isCapsLock)
        }
        keyboardView.setLayout(rows)
    }

    private fun updateShortcutsPanelData() {
        if (!::shortcutsPanelView.isInitialized) return
        val all = shortcutsManager.getAllShortcuts()
        val entities = all.map {
            ShortcutEntity(triggerText = it.trigger, expandedText = it.expansion)
        }
        shortcutsPanelView.setShortcuts(entities)
    }

    /**
     * Primary Key Press Handler.
     */
    protected open fun handleKeyPress(key: KeyModel) {
        val ic = currentInputConnection ?: return

        when (key.code) {
            KeyModel.CODE_DELETE -> {
                playFeedback(SoundManager.SoundEvent.DELETE)
                handleDelete()
            }

            KeyModel.CODE_SPACE -> {
                playFeedback(SoundManager.SoundEvent.SPACE)
                handleSpace()
            }

            KeyModel.CODE_ENTER -> {
                playFeedback(SoundManager.SoundEvent.ENTER)
                handleEnter()
            }

            KeyModel.CODE_SHIFT -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                val now = System.currentTimeMillis()
                if (now - lastShiftTime < 400) {
                    isCapsLock = !isCapsLock
                    isShifted = isCapsLock
                } else {
                    if (isCapsLock) {
                        isCapsLock = false
                        isShifted = false
                    } else {
                        isShifted = !isShifted
                    }
                }
                lastShiftTime = now
                updateLayout()
            }

            KeyModel.CODE_GLOBE -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                isArabic = !isArabic
                isSymbols = false
                isShifted = false
                isCapsLock = false
                prefs.primaryLanguage = if (isArabic) "ar" else "en"
                updateLayout()
                updateSuggestions()
            }

            KeyModel.CODE_MODE_CHANGE -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                isSymbols = !isSymbols
                isShifted = false
                isCapsLock = false
                updateLayout()
            }

            KeyModel.CODE_EMOJI -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                keyboardView.visibility = View.GONE
                shortcutsPanelView.visibility = View.GONE
                clipboardPanelView.visibility = View.GONE
                emojiPickerView.visibility = View.VISIBLE
            }

            KeyModel.CODE_CLIPBOARD -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                toggleClipboardView()
            }

            KeyModel.CODE_VOICE -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                launchVoiceInput()
            }

            KeyModel.CODE_SETTINGS -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                openSettings()
            }

            KeyModel.CODE_DIACRITICS -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                ic.commitText("َ", 1)
            }

            else -> {
                playFeedback(SoundManager.SoundEvent.CHARACTER)
                var charStr = key.label

                // Auto-Capitalization for English
                if (!isArabic && !isSymbols && prefs.autoCapitalization && currentComposingWord.isEmpty() && !isShifted && !isCapsLock) {
                    val textBefore = ic.getTextBeforeCursor(2, 0)
                    if (textBefore.isNullOrEmpty() || textBefore.endsWith(". ") || textBefore.endsWith("! ") || textBefore.endsWith("? ")) {
                        charStr = charStr.uppercase()
                    }
                }

                ic.commitText(charStr, 1)
                currentComposingWord.append(charStr)

                if (isShifted && !isCapsLock) {
                    isShifted = false
                    updateLayout()
                }

                updateSuggestions()
            }
        }
    }

    protected open fun handleDirectTextCommit(text: String) {
        playFeedback(SoundManager.SoundEvent.CHARACTER)
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
        currentComposingWord.append(text)
        updateSuggestions()
    }

    protected open fun handleSwipeWordCommit(word: String) {
        playFeedback(SoundManager.SoundEvent.SPACE)
        val ic = currentInputConnection ?: return
        ic.commitText(word, 1)
        ic.commitText(" ", 1)

        previousWord = word
        currentComposingWord.clear()

        dictionaryManager.learnWord(word, isArabic)
        updateSuggestions()
    }

    protected open fun handleDelete() {
        val ic = currentInputConnection ?: return

        if (currentComposingWord.isNotEmpty()) {
            currentComposingWord.deleteCharAt(currentComposingWord.length - 1)
        }

        ic.deleteSurroundingText(1, 0)
        updateSuggestions()
    }

    /**
     * Problem #1 Solution: Robust Shortcut Replacement on Space.
     */
    protected open fun handleSpace() {
        val ic = currentInputConnection ?: return
        val currentTime = System.currentTimeMillis()

        // 1. Check double-space period rule
        if (prefs.doubleSpacePeriod && (currentTime - lastSpaceTime) < 400 && currentComposingWord.isEmpty()) {
            val textBefore = ic.getTextBeforeCursor(1, 0)
            if (textBefore != null && textBefore == " ") {
                ic.deleteSurroundingText(1, 0)
                ic.commitText(". ", 2)
                lastSpaceTime = 0
                updateSuggestions()
                return
            }
        }
        lastSpaceTime = currentTime

        // 2. Identify the typed word to check for text shortcuts
        var typedWord = currentComposingWord.toString().trim()

        // Fallback: If composing word was empty, inspect word immediately preceding cursor
        if (typedWord.isEmpty()) {
            val before = ic.getTextBeforeCursor(40, 0)?.toString() ?: ""
            val lastSpaceIdx = before.lastIndexOf(' ')
            typedWord = if (lastSpaceIdx >= 0) {
                before.substring(lastSpaceIdx + 1).trim()
            } else {
                before.trim()
            }
        }

        if (typedWord.isNotEmpty()) {
            // Check ShortcutsManager for registered replacement
            val expansion = shortcutsManager.getExpansion(typedWord)
                ?: dictionaryManager.findShortcut(typedWord)

            if (expansion != null) {
                // Delete abbreviation and commit full expanded text + space
                ic.deleteSurroundingText(typedWord.length, 0)
                ic.commitText(expansion, 1)
                ic.commitText(" ", 1)

                previousWord = expansion
                currentComposingWord.clear()
                updateSuggestions()
                return
            } else {
                // Not a shortcut; learn word in dictionary
                dictionaryManager.learnWord(typedWord, isArabic)
                previousWord = typedWord
            }

            currentComposingWord.clear()
        }

        ic.commitText(" ", 1)
        updateSuggestions()
    }

    protected open fun handleEnter() {
        val ic = currentInputConnection ?: return
        val options = currentInputEditorInfo?.imeOptions ?: 0
        val action = options and EditorInfo.IME_MASK_ACTION

        when (action) {
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE -> {
                ic.performEditorAction(action)
            }
            else -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }
        resetComposingState()
        updateSuggestions()
    }

    protected open fun commitSuggestion(suggestion: String) {
        playFeedback(SoundManager.SoundEvent.SPACE)
        val ic = currentInputConnection ?: return

        if (currentComposingWord.isNotEmpty()) {
            ic.deleteSurroundingText(currentComposingWord.length, 0)
        }

        ic.commitText(suggestion, 1)
        ic.commitText(" ", 1)

        previousWord = suggestion
        dictionaryManager.learnWord(suggestion, isArabic)
        currentComposingWord.clear()

        updateSuggestions()
    }

    /**
     * Problem #2 Solution: Feeds genuine words and cleans out all dots.
     */
    protected open fun updateSuggestions() {
        if (!::suggestionHeaderView.isInitialized) return
        if (!prefs.showSuggestions) {
            suggestionHeaderView.setSuggestions(emptyList())
            return
        }

        val rawWord = currentComposingWord.toString().trim()
        val candidateList = mutableListOf<String>()

        if (rawWord.isNotEmpty()) {
            // 1. High-priority matching shortcuts
            val matchingShortcuts = shortcutsManager.findMatchingShortcuts(rawWord, limit = 2)
            for (sc in matchingShortcuts) {
                candidateList.add(sc.expansion)
            }

            // 2. Dictionary vocabulary & learned words
            val dictSuggestions = dictionaryManager.getSuggestions(
                currentWord = rawWord,
                previousWord = if (prefs.enableNextWordPrediction) previousWord else null,
                isArabic = isArabic
            )

            for (w in dictSuggestions) {
                if (!candidateList.contains(w) && w.isNotBlank()) {
                    candidateList.add(w)
                }
            }
        } else if (prefs.enableNextWordPrediction && !previousWord.isNullOrBlank()) {
            val nextWords = dictionaryManager.getSuggestions(
                currentWord = "",
                previousWord = previousWord,
                isArabic = isArabic
            )
            candidateList.addAll(nextWords)
        }

        // Clean out any punctuation or whitespace dots
        val cleanList = candidateList
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "." && it != "،" && it != "؟" }
            .take(3)

        suggestionHeaderView.setSuggestions(cleanList)
    }

    protected open fun resetComposingState() {
        currentComposingWord.clear()
        previousWord = null
        lastSpaceTime = 0
    }

    protected open fun playFeedback(event: SoundManager.SoundEvent) {
        if (prefs.soundEnabled) {
            soundManager.playSound(event)
        }

        if (prefs.vibrationEnabled && vibrator != null) {
            val duration = prefs.vibrationStrength.toLong().coerceIn(5L, 60L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        }
    }

    private fun toggleClipboardView() {
        if (!::clipboardPanelView.isInitialized || !::keyboardView.isInitialized) return
        if (clipboardPanelView.visibility == View.VISIBLE) {
            clipboardPanelView.visibility = View.GONE
            keyboardView.visibility = View.VISIBLE
        } else {
            scope.launch(Dispatchers.IO) {
                val items = database.clipboardDao().getRecentItems()
                withContext(Dispatchers.Main) {
                    clipboardPanelView.setClips(items)
                    keyboardView.visibility = View.GONE
                    shortcutsPanelView.visibility = View.GONE
                    emojiPickerView.visibility = View.GONE
                    clipboardPanelView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun toggleShortcutsView() {
        if (!::shortcutsPanelView.isInitialized || !::keyboardView.isInitialized) return
        if (shortcutsPanelView.visibility == View.VISIBLE) {
            shortcutsPanelView.visibility = View.GONE
            keyboardView.visibility = View.VISIBLE
        } else {
            updateShortcutsPanelData()
            keyboardView.visibility = View.GONE
            clipboardPanelView.visibility = View.GONE
            emojiPickerView.visibility = View.GONE
            shortcutsPanelView.visibility = View.VISIBLE
        }
    }

    private fun launchVoiceInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isArabic) "ar-SA" else "en-US")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun cycleTheme() {
        val nextTheme = when (prefs.theme) {
            PreferencesManager.THEME_IOS_LIGHT -> PreferencesManager.THEME_IOS_DARK
            PreferencesManager.THEME_IOS_DARK -> PreferencesManager.THEME_SAMSUNG_LIGHT
            PreferencesManager.THEME_SAMSUNG_LIGHT -> PreferencesManager.THEME_SAMSUNG_DARK
            PreferencesManager.THEME_SAMSUNG_DARK -> PreferencesManager.THEME_AUTO
            else -> PreferencesManager.THEME_IOS_LIGHT
        }
        prefs.theme = nextTheme
        applySettings()
        updateLayout()
    }

    private fun openSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipListener)
            prefs.prefs.unregisterOnSharedPreferenceChangeListener(this)
        } catch (_: Exception) {}
        soundManager.release()
        scope.cancel()
        super.onDestroy()
    }
}
