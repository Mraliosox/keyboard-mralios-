package com.example.ime.theme

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.example.data.PreferencesManager
import org.json.JSONObject

data class KeyboardTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val keyboardBackground: Int,
    val keyBackground: Int,
    val keyBackgroundPressed: Int,
    val actionKeyBackground: Int,
    val actionKeyBackgroundPressed: Int,
    val accentColor: Int,
    val primaryTextColor: Int,
    val secondaryTextColor: Int,
    val keyShadowColor: Int,
    val suggestionStripBackground: Int,
    val suggestionTextColor: Int,
    val suggestionHighlightColor: Int,
    val popupBackgroundColor: Int,
    val popupTextColor: Int,
    val gestureTrailColor: Int
) {
    fun toJson(): String {
        val obj = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("isDark", isDark)
            put("keyboardBackground", keyboardBackground)
            put("keyBackground", keyBackground)
            put("keyBackgroundPressed", keyBackgroundPressed)
            put("actionKeyBackground", actionKeyBackground)
            put("actionKeyBackgroundPressed", actionKeyBackgroundPressed)
            put("accentColor", accentColor)
            put("primaryTextColor", primaryTextColor)
            put("secondaryTextColor", secondaryTextColor)
            put("keyShadowColor", keyShadowColor)
            put("suggestionStripBackground", suggestionStripBackground)
            put("suggestionTextColor", suggestionTextColor)
            put("suggestionHighlightColor", suggestionHighlightColor)
            put("popupBackgroundColor", popupBackgroundColor)
            put("popupTextColor", popupTextColor)
            put("gestureTrailColor", gestureTrailColor)
        }
        return obj.toString()
    }

    companion object {
        val IOS_LIGHT = KeyboardTheme(
            id = "ios_light",
            name = "iPhone Light",
            isDark = false,
            keyboardBackground = Color.parseColor("#D1D5DB"),
            keyBackground = Color.parseColor("#FFFFFF"),
            keyBackgroundPressed = Color.parseColor("#E5E7EB"),
            actionKeyBackground = Color.parseColor("#B0B5BD"),
            actionKeyBackgroundPressed = Color.parseColor("#9CA3AF"),
            accentColor = Color.parseColor("#007AFF"),
            primaryTextColor = Color.parseColor("#111827"),
            secondaryTextColor = Color.parseColor("#4B5563"),
            keyShadowColor = Color.parseColor("#8E8E93"),
            suggestionStripBackground = Color.parseColor("#E5E7EB"),
            suggestionTextColor = Color.parseColor("#1F2937"),
            suggestionHighlightColor = Color.parseColor("#007AFF"),
            popupBackgroundColor = Color.parseColor("#FFFFFF"),
            popupTextColor = Color.parseColor("#000000"),
            gestureTrailColor = Color.parseColor("#3B82F6")
        )

        val IOS_DARK = KeyboardTheme(
            id = "ios_dark",
            name = "iPhone Dark",
            isDark = true,
            keyboardBackground = Color.parseColor("#1C1C1E"),
            keyBackground = Color.parseColor("#3A3A3C"),
            keyBackgroundPressed = Color.parseColor("#505054"),
            actionKeyBackground = Color.parseColor("#2C2C2E"),
            actionKeyBackgroundPressed = Color.parseColor("#444448"),
            accentColor = Color.parseColor("#0A84FF"),
            primaryTextColor = Color.parseColor("#F9FAFB"),
            secondaryTextColor = Color.parseColor("#9CA3AF"),
            keyShadowColor = Color.parseColor("#0A0A0C"),
            suggestionStripBackground = Color.parseColor("#2C2C2E"),
            suggestionTextColor = Color.parseColor("#F3F4F6"),
            suggestionHighlightColor = Color.parseColor("#0A84FF"),
            popupBackgroundColor = Color.parseColor("#3A3A3C"),
            popupTextColor = Color.parseColor("#FFFFFF"),
            gestureTrailColor = Color.parseColor("#60A5FA")
        )

        val SAMSUNG_LIGHT = KeyboardTheme(
            id = "samsung_light",
            name = "Samsung Light",
            isDark = false,
            keyboardBackground = Color.parseColor("#ECEFF1"),
            keyBackground = Color.parseColor("#FFFFFF"),
            keyBackgroundPressed = Color.parseColor("#CFD8DC"),
            actionKeyBackground = Color.parseColor("#E0E0E0"),
            actionKeyBackgroundPressed = Color.parseColor("#BDBDBD"),
            accentColor = Color.parseColor("#1976D2"),
            primaryTextColor = Color.parseColor("#212121"),
            secondaryTextColor = Color.parseColor("#757575"),
            keyShadowColor = Color.parseColor("#B0BEC5"),
            suggestionStripBackground = Color.parseColor("#F5F5F5"),
            suggestionTextColor = Color.parseColor("#212121"),
            suggestionHighlightColor = Color.parseColor("#1976D2"),
            popupBackgroundColor = Color.parseColor("#FFFFFF"),
            popupTextColor = Color.parseColor("#212121"),
            gestureTrailColor = Color.parseColor("#2196F3")
        )

        val SAMSUNG_DARK = KeyboardTheme(
            id = "samsung_dark",
            name = "Samsung Dark",
            isDark = true,
            keyboardBackground = Color.parseColor("#121212"),
            keyBackground = Color.parseColor("#242424"),
            keyBackgroundPressed = Color.parseColor("#383838"),
            actionKeyBackground = Color.parseColor("#1E1E1E"),
            actionKeyBackgroundPressed = Color.parseColor("#2E2E2E"),
            accentColor = Color.parseColor("#2979FF"),
            primaryTextColor = Color.parseColor("#EEEEEE"),
            secondaryTextColor = Color.parseColor("#9E9E9E"),
            keyShadowColor = Color.parseColor("#000000"),
            suggestionStripBackground = Color.parseColor("#1A1A1A"),
            suggestionTextColor = Color.parseColor("#EEEEEE"),
            suggestionHighlightColor = Color.parseColor("#2979FF"),
            popupBackgroundColor = Color.parseColor("#2A2A2A"),
            popupTextColor = Color.parseColor("#FFFFFF"),
            gestureTrailColor = Color.parseColor("#64B5F6")
        )

        val CYBERPUNK = KeyboardTheme(
            id = "cyberpunk",
            name = "Cyberpunk نيون",
            isDark = true,
            keyboardBackground = Color.parseColor("#0D0B18"),
            keyBackground = Color.parseColor("#1C1635"),
            keyBackgroundPressed = Color.parseColor("#2E2355"),
            actionKeyBackground = Color.parseColor("#271C4A"),
            actionKeyBackgroundPressed = Color.parseColor("#3B2A6F"),
            accentColor = Color.parseColor("#00F0FF"),
            primaryTextColor = Color.parseColor("#00F0FF"),
            secondaryTextColor = Color.parseColor("#FF007F"),
            keyShadowColor = Color.parseColor("#05030A"),
            suggestionStripBackground = Color.parseColor("#140F27"),
            suggestionTextColor = Color.parseColor("#00F0FF"),
            suggestionHighlightColor = Color.parseColor("#FF007F"),
            popupBackgroundColor = Color.parseColor("#241A45"),
            popupTextColor = Color.parseColor("#00F0FF"),
            gestureTrailColor = Color.parseColor("#00F0FF")
        )

        val EMERALD_GOLD = KeyboardTheme(
            id = "emerald",
            name = "Emerald فخم",
            isDark = true,
            keyboardBackground = Color.parseColor("#061A14"),
            keyBackground = Color.parseColor("#0F3327"),
            keyBackgroundPressed = Color.parseColor("#1B4D3C"),
            actionKeyBackground = Color.parseColor("#143D30"),
            actionKeyBackgroundPressed = Color.parseColor("#235B47"),
            accentColor = Color.parseColor("#D4AF37"),
            primaryTextColor = Color.parseColor("#E8F5E9"),
            secondaryTextColor = Color.parseColor("#A5D6A7"),
            keyShadowColor = Color.parseColor("#020B08"),
            suggestionStripBackground = Color.parseColor("#0A261D"),
            suggestionTextColor = Color.parseColor("#E8F5E9"),
            suggestionHighlightColor = Color.parseColor("#D4AF37"),
            popupBackgroundColor = Color.parseColor("#143D30"),
            popupTextColor = Color.parseColor("#D4AF37"),
            gestureTrailColor = Color.parseColor("#D4AF37")
        )

        val SUNSET_PURPLE = KeyboardTheme(
            id = "sunset",
            name = "Sunset غروب بنفسجي",
            isDark = true,
            keyboardBackground = Color.parseColor("#1A0E2E"),
            keyBackground = Color.parseColor("#2D1B4E"),
            keyBackgroundPressed = Color.parseColor("#442875"),
            actionKeyBackground = Color.parseColor("#3A2364"),
            actionKeyBackgroundPressed = Color.parseColor("#52318D"),
            accentColor = Color.parseColor("#FF6B6B"),
            primaryTextColor = Color.parseColor("#FFFFFF"),
            secondaryTextColor = Color.parseColor("#FFA07A"),
            keyShadowColor = Color.parseColor("#0E071A"),
            suggestionStripBackground = Color.parseColor("#22123D"),
            suggestionTextColor = Color.parseColor("#FFFFFF"),
            suggestionHighlightColor = Color.parseColor("#FF6B6B"),
            popupBackgroundColor = Color.parseColor("#3A2364"),
            popupTextColor = Color.parseColor("#FF6B6B"),
            gestureTrailColor = Color.parseColor("#FF6B6B")
        )

        val OLED_BLACK = KeyboardTheme(
            id = "oled",
            name = "OLED أسود نقي",
            isDark = true,
            keyboardBackground = Color.parseColor("#000000"),
            keyBackground = Color.parseColor("#121212"),
            keyBackgroundPressed = Color.parseColor("#262626"),
            actionKeyBackground = Color.parseColor("#181818"),
            actionKeyBackgroundPressed = Color.parseColor("#2E2E2E"),
            accentColor = Color.parseColor("#FFFFFF"),
            primaryTextColor = Color.parseColor("#FFFFFF"),
            secondaryTextColor = Color.parseColor("#888888"),
            keyShadowColor = Color.parseColor("#000000"),
            suggestionStripBackground = Color.parseColor("#080808"),
            suggestionTextColor = Color.parseColor("#FFFFFF"),
            suggestionHighlightColor = Color.parseColor("#FFFFFF"),
            popupBackgroundColor = Color.parseColor("#1A1A1A"),
            popupTextColor = Color.parseColor("#FFFFFF"),
            gestureTrailColor = Color.parseColor("#FFFFFF")
        )

        fun isSystemInDarkMode(context: Context): Boolean {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return uiMode == Configuration.UI_MODE_NIGHT_YES
        }

        fun getCustomTheme(context: Context): KeyboardTheme {
            val prefs = PreferencesManager(context)
            val json = prefs.customThemeJson
            if (!json.isNullOrBlank()) {
                try {
                    val obj = JSONObject(json)
                    return KeyboardTheme(
                        id = PreferencesManager.THEME_CUSTOM,
                        name = obj.optString("name", "ثيمي المخصص"),
                        isDark = obj.optBoolean("isDark", true),
                        keyboardBackground = obj.optInt("keyboardBackground", Color.parseColor("#1C1C1E")),
                        keyBackground = obj.optInt("keyBackground", Color.parseColor("#2C2C2E")),
                        keyBackgroundPressed = obj.optInt("keyBackgroundPressed", Color.parseColor("#3A3A3C")),
                        actionKeyBackground = obj.optInt("actionKeyBackground", Color.parseColor("#3A3A3C")),
                        actionKeyBackgroundPressed = obj.optInt("actionKeyBackgroundPressed", Color.parseColor("#48484A")),
                        accentColor = obj.optInt("accentColor", Color.parseColor("#0A84FF")),
                        primaryTextColor = obj.optInt("primaryTextColor", Color.parseColor("#FFFFFF")),
                        secondaryTextColor = obj.optInt("secondaryTextColor", Color.parseColor("#8E8E93")),
                        keyShadowColor = obj.optInt("keyShadowColor", Color.parseColor("#000000")),
                        suggestionStripBackground = obj.optInt("suggestionStripBackground", Color.parseColor("#2C2C2E")),
                        suggestionTextColor = obj.optInt("suggestionTextColor", Color.parseColor("#FFFFFF")),
                        suggestionHighlightColor = obj.optInt("suggestionHighlightColor", Color.parseColor("#0A84FF")),
                        popupBackgroundColor = obj.optInt("popupBackgroundColor", Color.parseColor("#2C2C2E")),
                        popupTextColor = obj.optInt("popupTextColor", Color.parseColor("#FFFFFF")),
                        gestureTrailColor = obj.optInt("gestureTrailColor", Color.parseColor("#0A84FF"))
                    )
                } catch (_: Exception) {}
            }
            return CYBERPUNK
        }

        fun getTheme(themePref: String, context: Context): KeyboardTheme {
            val systemDark = isSystemInDarkMode(context)
            return when (themePref) {
                "dark", "ios_dark" -> IOS_DARK
                "light", "ios_light" -> IOS_LIGHT
                "samsung_light" -> SAMSUNG_LIGHT
                "samsung_dark" -> SAMSUNG_DARK
                "cyberpunk" -> CYBERPUNK
                "emerald" -> EMERALD_GOLD
                "sunset" -> SUNSET_PURPLE
                "oled" -> OLED_BLACK
                "custom" -> getCustomTheme(context)
                else -> if (systemDark) IOS_DARK else IOS_LIGHT
            }
        }
    }
}
