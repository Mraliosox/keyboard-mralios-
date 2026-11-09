package com.example.ime.model

import android.graphics.RectF

enum class KeyType {
    CHARACTER,
    SHIFT,
    DELETE,
    SPACE,
    ENTER,
    GLOBE,
    MODE_CHANGE,
    EMOJI,
    SETTINGS,
    CLIPBOARD,
    VOICE,
    DIACRITICS
}

data class KeyModel(
    val label: String,
    val code: Int,
    val secondaryLabel: String? = null,
    val popupCharacters: List<String> = emptyList(),
    val widthWeight: Float = 1.0f,
    val keyType: KeyType = KeyType.CHARACTER,
    var bounds: RectF = RectF(),
    var isPressed: Boolean = false
) {
    companion object {
        const val CODE_SHIFT = -1
        const val CODE_MODE_CHANGE = -2
        const val CODE_DELETE = -5
        const val CODE_SPACE = 32
        const val CODE_ENTER = 10
        const val CODE_GLOBE = -10
        const val CODE_EMOJI = -11
        const val CODE_SETTINGS = -12
        const val CODE_CLIPBOARD = -13
        const val CODE_VOICE = -14
        const val CODE_DIACRITICS = -15
    }
}
