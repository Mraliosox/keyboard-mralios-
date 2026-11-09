package com.example.engine

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.LearnedWordEntity
import com.example.data.ShortcutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.min

class DictionaryManager(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val learnedWordDao = database.learnedWordDao()
    private val shortcutDao = database.shortcutDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory caches for instant retrieval (pre-populated with defaults)
    private val learnedArabicWords = mutableMapOf<String, Int>()
    private val learnedEnglishWords = mutableMapOf<String, Int>()
    private val shortcutsMap = mutableMapOf<String, String>(
        "سلام" to "السلام عليكم ورحمة الله وبركاته",
        "ايميلي" to "example@gmail.com",
        "شكرا" to "شكراً جزيلاً لك وجزاك الله خيراً!"
    )

    init {
        loadDataFromDb()
    }

    fun loadDataFromDb() {
        scope.launch {
            try {
                val arabic = learnedWordDao.getTopWords("ar")
                synchronized(learnedArabicWords) {
                    learnedArabicWords.clear()
                    arabic.forEach { learnedArabicWords[it.word] = it.frequency }
                }

                val english = learnedWordDao.getTopWords("en")
                synchronized(learnedEnglishWords) {
                    learnedEnglishWords.clear()
                    english.forEach { learnedEnglishWords[it.word] = it.frequency }
                }

                val shortcuts = shortcutDao.getAllList()
                synchronized(shortcutsMap) {
                    shortcutsMap.clear()
                    // Default fallback
                    shortcutsMap["سلام"] = "السلام عليكم ورحمة الله وبركاته"
                    shortcutsMap["ايميلي"] = "example@gmail.com"
                    shortcutsMap["شكرا"] = "شكراً جزيلاً لك وجزاك الله خيراً!"
                    shortcuts.forEach {
                        shortcutsMap[normalizeText(it.triggerText)] = it.expandedText
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun normalizeText(text: String): String {
        return text.trim()
            .lowercase(Locale.ROOT)
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace("َ", "")
            .replace("ً", "")
            .replace("ُ", "")
            .replace("ٌ", "")
            .replace("ِ", "")
            .replace("ٍ", "")
            .replace("ْ", "")
            .replace("ّ", "")
    }

    /**
     * Checks if typed word matches a registered text shortcut.
     */
    fun findShortcut(trigger: String): String? {
        val norm = normalizeText(trigger)
        synchronized(shortcutsMap) {
            return shortcutsMap[norm] ?: shortcutsMap[trigger.trim().lowercase(Locale.ROOT)]
        }
    }

    /**
     * Learns a newly committed word into the user's custom dictionary.
     */
    fun learnWord(word: String, isArabic: Boolean) {
        val trimmed = word.trim()
        if (trimmed.length < 2 || trimmed.contains(" ")) return

        val locale = if (isArabic) "ar" else "en"
        val map = if (isArabic) learnedArabicWords else learnedEnglishWords

        synchronized(map) {
            val currentFreq = map[trimmed] ?: 0
            map[trimmed] = currentFreq + 1
        }

        scope.launch {
            try {
                val existing = learnedWordDao.findWord(trimmed)
                val freq = (existing?.frequency ?: 0) + 1
                learnedWordDao.insertOrUpdate(
                    LearnedWordEntity(
                        word = trimmed,
                        frequency = freq,
                        locale = locale,
                        lastUsed = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Returns smart suggestions based on current word, previous word, and language.
     * When there are no suggestions, returns an empty list (no blank or empty dots).
     */
    fun getSuggestions(
        currentWord: String,
        previousWord: String?,
        isArabic: Boolean
    ): List<String> {
        val trimmed = currentWord.trim()

        // 1. If currently no word is being typed, return Next-Word Predictions (if any)
        if (trimmed.isEmpty()) {
            if (!previousWord.isNullOrBlank()) {
                val cleanPrev = previousWord.trim().lowercase(Locale.ROOT)
                val bigrams = if (isArabic) BuiltInDictionary.ARABIC_BIGRAMS else BuiltInDictionary.ENGLISH_BIGRAMS
                val predictions = bigrams[cleanPrev]
                if (!predictions.isNullOrEmpty()) {
                    return predictions.take(3)
                }
            }
            return emptyList()
        }

        val candidates = mutableListOf<Candidate>()

        // 2. Check shortcut expansion first (highest priority)
        val shortcutExpansion = findShortcut(trimmed)
        if (shortcutExpansion != null) {
            candidates.add(Candidate(shortcutExpansion, 2000))
        }

        // Also check if any shortcut trigger starts with the typed prefix
        val normTyped = normalizeText(trimmed)
        synchronized(shortcutsMap) {
            for ((triggerKey, exp) in shortcutsMap) {
                if (triggerKey.startsWith(normTyped) && exp != shortcutExpansion) {
                    candidates.add(Candidate(exp, 1500))
                }
            }
        }

        val learnedMap = if (isArabic) learnedArabicWords else learnedEnglishWords
        val builtInList = if (isArabic) BuiltInDictionary.COMMON_ARABIC_WORDS else BuiltInDictionary.COMMON_ENGLISH_WORDS

        val lowerTyped = trimmed.lowercase(Locale.ROOT)

        // 3. Search in learned words
        synchronized(learnedMap) {
            for ((w, freq) in learnedMap) {
                val lowerW = w.lowercase(Locale.ROOT)
                if (lowerW == lowerTyped) {
                    candidates.add(Candidate(w, 500 + freq))
                } else if (lowerW.startsWith(lowerTyped)) {
                    candidates.add(Candidate(w, 300 + freq))
                } else if (lowerTyped.length >= 3 && levenshteinDistance(lowerTyped, lowerW) <= 1) {
                    candidates.add(Candidate(w, 150 + freq))
                }
            }
        }

        // 4. Search in built-in vocabulary
        for (w in builtInList) {
            val lowerW = w.lowercase(Locale.ROOT)
            if (candidates.none { it.word.equals(w, ignoreCase = true) }) {
                if (lowerW == lowerTyped) {
                    candidates.add(Candidate(w, 400))
                } else if (lowerW.startsWith(lowerTyped)) {
                    val bonus = (builtInList.size - builtInList.indexOf(w)) / 10
                    candidates.add(Candidate(w, 250 + bonus))
                } else if (lowerTyped.length >= 3 && Math.abs(lowerW.length - lowerTyped.length) <= 2) {
                    val dist = levenshteinDistance(lowerTyped, lowerW)
                    if (dist <= 2) {
                        candidates.add(Candidate(w, 100 - dist * 30))
                    }
                }
            }
        }

        // Sort candidates by score descending
        val sorted = candidates.sortedByDescending { it.score }.map { it.word }.distinct()

        val result = mutableListOf<String>()

        if (sorted.isNotEmpty()) {
            val best = sorted.first()
            if (sorted.size >= 3) {
                val secondary = if (sorted[1] != trimmed) sorted[1] else sorted.getOrElse(2) { trimmed }
                val tertiary = sorted.getOrElse(2) { if (best != trimmed) trimmed else "" }
                result.add(secondary)
                result.add(best)
                if (tertiary.isNotEmpty() && tertiary != best && tertiary != secondary) {
                    result.add(tertiary)
                }
            } else if (sorted.size == 2) {
                result.add(if (sorted[0] == trimmed) sorted[1] else trimmed)
                result.add(sorted[0])
                if (sorted.size > 1 && sorted[1] != result[0]) {
                    result.add(sorted[1])
                }
            } else {
                result.add(trimmed)
                result.add(best)
            }
        } else {
            result.add(trimmed)
        }

        return result.take(3)
    }

    /**
     * Levenshtein Distance for typo tolerance and edit distance calculation.
     */
    fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = Array(lhsLength + 1) { it }
        var newCost = Array(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i

            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength]
    }

    private data class Candidate(val word: String, val score: Int)
}
