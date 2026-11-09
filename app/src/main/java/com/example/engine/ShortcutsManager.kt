package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages text shortcuts (abbreviation -> full expansion) with local persistence
 * in SharedPreferences and instant in-memory concurrent lookup.
 */
class ShortcutsManager(context: Context) {

    data class ShortcutItem(
        val trigger: String,
        val expansion: String
    )

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // In-memory cache for ultra-fast, zero-latency keypress lookup
    private val shortcutsMap = ConcurrentHashMap<String, ShortcutItem>()

    // Listeners for shortcuts updates
    private val listeners = mutableListOf<() -> Unit>()

    init {
        loadShortcuts()
    }

    /**
     * Loads shortcuts from SharedPreferences into memory.
     * Seeds initial default shortcuts on first launch.
     */
    fun loadShortcuts() {
        shortcutsMap.clear()

        val jsonString = prefs.getString(KEY_SHORTCUTS_JSON, null)
        if (jsonString.isNullOrBlank()) {
            // Seed rich default shortcuts for instant out-of-the-box testing
            seedDefaults()
            saveToPreferences()
        } else {
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val trigger = obj.optString("trigger", "").trim()
                    val expansion = obj.optString("expansion", "").trim()
                    if (trigger.isNotEmpty() && expansion.isNotEmpty()) {
                        val norm = normalizeTrigger(trigger)
                        shortcutsMap[norm] = ShortcutItem(trigger, expansion)
                    }
                }
            } catch (_: Exception) {
                seedDefaults()
                saveToPreferences()
            }
        }

        notifyListeners()
    }

    /**
     * Seeds standard Arabic and English shortcuts.
     */
    private fun seedDefaults() {
        addDefault("سلام", "السلام عليكم ورحمة الله وبركاته")
        addDefault("ايميلي", "example@gmail.com")
        addDefault("شكرا", "شكراً جزيلاً لك وجزاك الله خيراً!")
        addDefault("مبروك", "ألف مبروك وبالتوفيق والنجاح الدائم إن شاء الله!")
        addDefault("صباحو", "صباح الخير والبركة والسرور!")
        addDefault("مساء", "مساء النور والسرور!")
        addDefault("omw", "On my way!")
        addDefault("brb", "Be right back!")
        addDefault("thx", "Thanks a lot!")
    }

    private fun addDefault(trigger: String, expansion: String) {
        val norm = normalizeTrigger(trigger)
        shortcutsMap[norm] = ShortcutItem(trigger, expansion)
    }

    /**
     * Normalizes an Arabic or Latin trigger string for robust matching:
     * - Trims leading and trailing spaces
     * - Removes Arabic diacritics/tashkeel (فتحة، ضمة، كسرة، سكون، تنوين، شدة)
     * - Unifies Arabic alef variants (أ، إ، آ، ٱ -> ا)
     * - Unifies teh marbuta (ة -> ه)
     * - Unifies alef maqsura (ى -> ي)
     * - Removes tatweel (ـ)
     * - Lowercases Latin characters
     */
    fun normalizeTrigger(text: String): String {
        return text.trim()
            .lowercase(Locale.ROOT)
            .replace("[ًٌٍَُِّْـ]".toRegex(), "")
            .replace("[أإآٱ]".toRegex(), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
    }

    /**
     * Checks if the given word matches a registered shortcut.
     * Returns the full expansion if found, or null otherwise.
     */
    fun getExpansion(trigger: String): String? {
        if (trigger.isBlank()) return null

        val norm = normalizeTrigger(trigger)
        val item = shortcutsMap[norm]
        if (item != null) {
            return item.expansion
        }

        // Direct fallback check without normalization
        val direct = shortcutsMap[trigger.trim().lowercase(Locale.ROOT)]
        return direct?.expansion
    }

    /**
     * Finds shortcuts whose trigger starts with the given prefix
     * for feeding into the suggestion bar while the user is typing.
     */
    fun findMatchingShortcuts(prefix: String, limit: Int = 3): List<ShortcutItem> {
        val norm = normalizeTrigger(prefix)
        if (norm.isEmpty()) return emptyList()

        val results = mutableListOf<ShortcutItem>()
        for ((key, item) in shortcutsMap) {
            if (key.startsWith(norm) || normalizeTrigger(item.trigger).startsWith(norm)) {
                results.add(item)
                if (results.size >= limit) break
            }
        }
        return results
    }

    /**
     * Returns all saved shortcuts as a list.
     */
    fun getAllShortcuts(): List<ShortcutItem> {
        return shortcutsMap.values.toList().sortedBy { it.trigger }
    }

    /**
     * Adds or updates a shortcut.
     */
    fun saveShortcut(trigger: String, expansion: String) {
        val cleanTrigger = trigger.trim()
        val cleanExpansion = expansion.trim()
        if (cleanTrigger.isEmpty() || cleanExpansion.isEmpty()) return

        val norm = normalizeTrigger(cleanTrigger)
        shortcutsMap[norm] = ShortcutItem(cleanTrigger, cleanExpansion)
        saveToPreferences()
        notifyListeners()
    }

    /**
     * Deletes a shortcut by its trigger.
     */
    fun deleteShortcut(trigger: String) {
        val norm = normalizeTrigger(trigger)
        shortcutsMap.remove(norm)
        shortcutsMap.remove(trigger.trim().lowercase(Locale.ROOT))
        saveToPreferences()
        notifyListeners()
    }

    /**
     * Clears all shortcuts and restores default ones.
     */
    fun resetToDefaults() {
        shortcutsMap.clear()
        seedDefaults()
        saveToPreferences()
        notifyListeners()
    }

    private fun saveToPreferences() {
        try {
            val array = JSONArray()
            for (item in shortcutsMap.values) {
                val obj = JSONObject().apply {
                    put("trigger", item.trigger)
                    put("expansion", item.expansion)
                }
                array.put(obj)
            }
            prefs.edit()
                .putString(KEY_SHORTCUTS_JSON, array.toString())
                .putLong(KEY_SHORTCUTS_VERSION, System.currentTimeMillis())
                .apply()
        } catch (_: Exception) {}
    }

    fun addListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            try {
                listener.invoke()
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val PREF_NAME = "mralios_shortcuts_prefs"
        private const val KEY_SHORTCUTS_JSON = "saved_shortcuts_json"
        const val KEY_SHORTCUTS_VERSION = "pref_shortcuts_version"

        @Volatile
        private var instance: ShortcutsManager? = null

        fun getInstance(context: Context): ShortcutsManager {
            return instance ?: synchronized(this) {
                instance ?: ShortcutsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
