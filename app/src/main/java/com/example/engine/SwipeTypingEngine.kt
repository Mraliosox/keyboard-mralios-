package com.example.engine

import android.graphics.PointF
import com.example.ime.model.KeyModel
import kotlin.math.hypot

class SwipeTypingEngine {

    data class KeyPosition(val char: Char, val centerX: Float, val centerY: Float)

    /**
     * Resolves swipe points trajectory to the best matching word from candidate dictionary.
     */
    fun findWordForPath(
        pathPoints: List<PointF>,
        keyPositions: Map<Char, KeyPosition>,
        dictionary: List<String>
    ): String? {
        if (pathPoints.size < 4) return null

        val startPoint = pathPoints.first()
        val endPoint = pathPoints.last()

        val startChar = findNearestChar(startPoint.x, startPoint.y, keyPositions) ?: return null
        val endChar = findNearestChar(endPoint.x, endPoint.y, keyPositions) ?: return null

        // Filter candidate words that start and end with the approximate keys
        val candidates = dictionary.filter { word ->
            word.length >= 3 &&
            word.first().equals(startChar, ignoreCase = true) &&
            word.last().equals(endChar, ignoreCase = true)
        }

        if (candidates.isEmpty()) return null

        var bestWord: String? = null
        var lowestScore = Float.MAX_VALUE

        for (word in candidates) {
            val score = scoreWordForPath(word, pathPoints, keyPositions)
            if (score < lowestScore) {
                lowestScore = score
                bestWord = word
            }
        }

        return bestWord
    }

    private fun scoreWordForPath(
        word: String,
        pathPoints: List<PointF>,
        keyPositions: Map<Char, KeyPosition>
    ): Float {
        var lastPathIdx = 0
        var totalDistance = 0f

        for (ch in word) {
            val keyPos = keyPositions[ch.lowercaseChar()] ?: keyPositions[ch] ?: return Float.MAX_VALUE
            var minDistanceForChar = Float.MAX_VALUE
            var bestIdxForChar = lastPathIdx

            for (i in lastPathIdx until pathPoints.size) {
                val pt = pathPoints[i]
                val d = hypot((pt.x - keyPos.centerX).toDouble(), (pt.y - keyPos.centerY).toDouble()).toFloat()
                if (d < minDistanceForChar) {
                    minDistanceForChar = d
                    bestIdxForChar = i
                }
            }

            totalDistance += minDistanceForChar
            lastPathIdx = bestIdxForChar
        }

        // Normalize by word length
        return totalDistance / word.length
    }

    private fun findNearestChar(x: Float, y: Float, keyPositions: Map<Char, KeyPosition>): Char? {
        var closestChar: Char? = null
        var minDist = Float.MAX_VALUE
        for ((char, pos) in keyPositions) {
            val d = hypot((x - pos.centerX).toDouble(), (y - pos.centerY).toDouble()).toFloat()
            if (d < minDist) {
                minDist = d
                closestChar = char
            }
        }
        return closestChar
    }
}
