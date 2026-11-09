package com.example

import com.example.engine.BuiltInDictionary
import com.example.engine.SwipeTypingEngine
import com.example.ime.layout.KeyboardLayouts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.min

class ExampleUnitTest {

    @Test
    fun testKeyboardLayoutsStructure() {
        val arabicLayout = KeyboardLayouts.getArabicLayout(showNumberRow = true)
        assertTrue(arabicLayout.size >= 4)

        // Permanent top number row
        val numRow = arabicLayout[0]
        assertEquals("1", numRow[0].label)
        assertEquals("0", numRow[9].label)

        val englishLayout = KeyboardLayouts.getEnglishLayout(showNumberRow = true, isShifted = false)
        assertTrue(englishLayout.size >= 4)
    }

    @Test
    fun testBuiltInDictionaryAndBigrams() {
        assertTrue(BuiltInDictionary.COMMON_ARABIC_WORDS.contains("السلام"))
        assertTrue(BuiltInDictionary.COMMON_ENGLISH_WORDS.contains("hello"))

        val arabicNext = BuiltInDictionary.ARABIC_BIGRAMS["السلام"]
        assertNotNull(arabicNext)
        assertTrue(arabicNext!!.contains("عليكم"))

        val englishNext = BuiltInDictionary.ENGLISH_BIGRAMS["how"]
        assertNotNull(englishNext)
        assertTrue(englishNext!!.contains("are"))
    }

    @Test
    fun testLevenshteinDistanceCalculation() {
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

        assertEquals(0, levenshteinDistance("كتاب", "كتاب"))
        assertEquals(1, levenshteinDistance("كتاب", "كتب"))
        assertEquals(1, levenshteinDistance("hello", "hallo"))
    }
}
