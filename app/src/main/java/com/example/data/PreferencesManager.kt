package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)

    companion object {
        const val KEY_THEME = "pref_theme"
        const val KEY_HEIGHT = "pref_height"
        const val KEY_NUMBER_ROW = "pref_number_row"
        const val KEY_KEY_POPUP = "pref_key_popup"
        const val KEY_SUGGESTIONS = "pref_suggestions"
        const val KEY_NEXT_WORD = "pref_next_word"
        const val KEY_SWIPE_TYPING = "pref_swipe_typing"
        const val KEY_DOUBLE_SPACE_PERIOD = "pref_double_space_period"
        const val KEY_AUTO_CAP = "pref_auto_cap"
        const val KEY_SOUND = "pref_sound"
        const val KEY_SOUND_PROFILE = "pref_sound_profile"
        const val KEY_SOUND_VOLUME = "pref_sound_volume"
        const val KEY_CUSTOM_SOUND_URI = "pref_custom_sound_uri"
        const val KEY_VIBRATION = "pref_vibration"
        const val KEY_VIBRATION_STRENGTH = "pref_vibration_strength"
        const val KEY_PRIMARY_LANGUAGE = "pref_primary_language"
        const val KEY_SHORTCUTS_VERSION = "pref_shortcuts_version"

        const val THEME_AUTO = "auto"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_IOS_LIGHT = "ios_light"
        const val THEME_IOS_DARK = "ios_dark"
        const val THEME_SAMSUNG_LIGHT = "samsung_light"
        const val THEME_SAMSUNG_DARK = "samsung_dark"
        const val THEME_CYBERPUNK = "cyberpunk"
        const val THEME_EMERALD = "emerald"
        const val THEME_SUNSET = "sunset"
        const val THEME_OLED = "oled"
        const val THEME_CUSTOM = "custom"

        const val KEY_CUSTOM_THEME_JSON = "pref_custom_theme_json"

        const val HEIGHT_COMPACT = "compact"
        const val HEIGHT_NORMAL = "normal"
        const val HEIGHT_TALL = "tall"
    }

    var theme: String
        get() = prefs.getString(KEY_THEME, THEME_AUTO) ?: THEME_AUTO
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var customThemeJson: String?
        get() = prefs.getString(KEY_CUSTOM_THEME_JSON, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_THEME_JSON, value).apply()

    var keyboardHeight: String
        get() = prefs.getString(KEY_HEIGHT, HEIGHT_NORMAL) ?: HEIGHT_NORMAL
        set(value) = prefs.edit().putString(KEY_HEIGHT, value).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_NUMBER_ROW, true)
        set(value) = prefs.edit().putBoolean(KEY_NUMBER_ROW, value).apply()

    var showKeyPopup: Boolean
        get() = prefs.getBoolean(KEY_KEY_POPUP, true)
        set(value) = prefs.edit().putBoolean(KEY_KEY_POPUP, value).apply()

    var showSuggestions: Boolean
        get() = prefs.getBoolean(KEY_SUGGESTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_SUGGESTIONS, value).apply()

    var enableNextWordPrediction: Boolean
        get() = prefs.getBoolean(KEY_NEXT_WORD, true)
        set(value) = prefs.edit().putBoolean(KEY_NEXT_WORD, value).apply()

    var enableSwipeTyping: Boolean
        get() = prefs.getBoolean(KEY_SWIPE_TYPING, true)
        set(value) = prefs.edit().putBoolean(KEY_SWIPE_TYPING, value).apply()

    var doubleSpacePeriod: Boolean
        get() = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true)
        set(value) = prefs.edit().putBoolean(KEY_DOUBLE_SPACE_PERIOD, value).apply()

    var autoCapitalization: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CAP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CAP, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var soundProfile: String
        get() = prefs.getString(KEY_SOUND_PROFILE, "pop") ?: "pop"
        set(value) = prefs.edit().putString(KEY_SOUND_PROFILE, value).apply()

    var soundVolume: Int
        get() = prefs.getInt(KEY_SOUND_VOLUME, 80)
        set(value) = prefs.edit().putInt(KEY_SOUND_VOLUME, value).apply()

    var customSoundUri: String?
        get() = prefs.getString(KEY_CUSTOM_SOUND_URI, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_SOUND_URI, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var vibrationStrength: Int
        get() = prefs.getInt(KEY_VIBRATION_STRENGTH, 25)
        set(value) = prefs.edit().putInt(KEY_VIBRATION_STRENGTH, value).apply()

    var primaryLanguage: String
        get() = prefs.getString(KEY_PRIMARY_LANGUAGE, "ar") ?: "ar"
        set(value) = prefs.edit().putString(KEY_PRIMARY_LANGUAGE, value).apply()

    var shortcutsVersion: Long
        get() = prefs.getLong(KEY_SHORTCUTS_VERSION, 0L)
        set(value) = prefs.edit().putLong(KEY_SHORTCUTS_VERSION, value).apply()

    fun notifyShortcutsChanged() {
        shortcutsVersion = System.currentTimeMillis()
    }
}
