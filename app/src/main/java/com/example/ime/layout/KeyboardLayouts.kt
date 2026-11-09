package com.example.ime.layout

import com.example.ime.model.KeyModel
import com.example.ime.model.KeyType

object KeyboardLayouts {

    val DIACRITICS_LIST = listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ْ", "ّ", "ـ", "ٰ")
    val ARABIC_NUMERALS = listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠")

    fun getNumberRow(): List<KeyModel> {
        val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        return numbers.mapIndexed { index, num ->
            KeyModel(
                label = num,
                code = num[0].code,
                secondaryLabel = ARABIC_NUMERALS.getOrNull(index),
                popupCharacters = listOf(ARABIC_NUMERALS.getOrElse(index) { num }, num),
                widthWeight = 1.0f,
                keyType = KeyType.CHARACTER
            )
        }
    }

    fun getArabicLayout(showNumberRow: Boolean): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()

        if (showNumberRow) {
            rows.add(getNumberRow())
        }

        // Row 1: ض ص ث ق ف غ ع ه خ ح ج د
        val r1Chars = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د")
        val r1 = r1Chars.map { ch ->
            val popups = when (ch) {
                "ق" -> listOf("ڨ", "ق")
                "ف" -> listOf("ڤ", "ف")
                "ه" -> listOf("ة", "ه")
                else -> emptyList()
            }
            KeyModel(
                label = ch,
                code = ch[0].code,
                popupCharacters = popups,
                widthWeight = 1.0f,
                keyType = KeyType.CHARACTER
            )
        }
        rows.add(r1)

        // Row 2: ش س ي ب ل ا ت ن م ك ط
        val r2Chars = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط")
        val r2 = r2Chars.map { ch ->
            val popups = when (ch) {
                "ي" -> listOf("ى", "ئ", "ي")
                "ب" -> listOf("پ", "ب")
                "ا" -> listOf("أ", "إ", "آ", "ء", "ٱ", "ا")
                "ت" -> listOf("ة", "ـ", "ت")
                "ك" -> listOf("گ", "ك")
                else -> emptyList()
            }
            KeyModel(
                label = ch,
                code = ch[0].code,
                popupCharacters = popups,
                widthWeight = 1.0f,
                keyType = KeyType.CHARACTER
            )
        }
        rows.add(r2)

        // Row 3: ذ ء ؤ ر ى ة و ز ظ [Delete]
        val r3Chars = listOf("ذ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ")
        val r3 = mutableListOf<KeyModel>()
        r3.addAll(r3Chars.map { ch ->
            val popups = when (ch) {
                "و" -> listOf("ؤ", "و")
                "ر" -> listOf("ڕ", "ر")
                "ز" -> listOf("ژ", "ز")
                else -> emptyList()
            }
            KeyModel(
                label = ch,
                code = ch[0].code,
                popupCharacters = popups,
                widthWeight = 1.0f,
                keyType = KeyType.CHARACTER
            )
        })
        // Delete key
        r3.add(
            KeyModel(
                label = "⌫",
                code = KeyModel.CODE_DELETE,
                widthWeight = 1.4f,
                keyType = KeyType.DELETE
            )
        )
        rows.add(r3)

        // Row 4: [?123] [🌐] [😊] [Spacebar العربية] [تشكيل] [. ،] [↵]
        val r4 = listOf(
            KeyModel(label = "?123", code = KeyModel.CODE_MODE_CHANGE, widthWeight = 1.4f, keyType = KeyType.MODE_CHANGE),
            KeyModel(label = "🌐", code = KeyModel.CODE_GLOBE, widthWeight = 1.1f, keyType = KeyType.GLOBE),
            KeyModel(label = "😊", code = KeyModel.CODE_EMOJI, widthWeight = 1.1f, keyType = KeyType.EMOJI),
            KeyModel(label = "العربية", code = KeyModel.CODE_SPACE, widthWeight = 3.8f, keyType = KeyType.SPACE),
            KeyModel(
                label = "َ ً ُ",
                code = KeyModel.CODE_DIACRITICS,
                popupCharacters = DIACRITICS_LIST,
                widthWeight = 1.2f,
                keyType = KeyType.DIACRITICS
            ),
            KeyModel(
                label = ".",
                code = '.'.code,
                secondaryLabel = "،",
                popupCharacters = listOf("،", "؟", "!", ":", ";", "."),
                widthWeight = 1.1f,
                keyType = KeyType.CHARACTER
            ),
            KeyModel(label = "↵", code = KeyModel.CODE_ENTER, widthWeight = 1.5f, keyType = KeyType.ENTER)
        )
        rows.add(r4)

        return rows
    }

    fun getEnglishLayout(showNumberRow: Boolean, isShifted: Boolean): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()

        if (showNumberRow) {
            rows.add(getNumberRow())
        }

        // Row 1: q w e r t y u i o p
        val r1Chars = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val r1 = r1Chars.map { ch ->
            val label = if (isShifted) ch.uppercase() else ch
            val popups = when (ch) {
                "e" -> if (isShifted) listOf("É", "È", "Ê", "Ë") else listOf("é", "è", "ê", "ë")
                "u" -> if (isShifted) listOf("Ú", "Ù", "Û", "Ü") else listOf("ú", "ù", "û", "ü")
                "i" -> if (isShifted) listOf("Í", "Ì", "Î", "Ï") else listOf("í", "ì", "î", "ï")
                "o" -> if (isShifted) listOf("Ó", "Ò", "Ô", "Ö") else listOf("ó", "ò", "ô", "ö")
                else -> emptyList()
            }
            KeyModel(
                label = label,
                code = label[0].code,
                popupCharacters = popups,
                widthWeight = 1.0f,
                keyType = KeyType.CHARACTER
            )
        }
        rows.add(r1)

        // Row 2: a s d f g h j k l
        val r2Chars = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val r2 = r2Chars.map { ch ->
            val label = if (isShifted) ch.uppercase() else ch
            val popups = when (ch) {
                "a" -> if (isShifted) listOf("Á", "À", "Â", "Ä", "Å") else listOf("á", "à", "â", "ä", "å")
                "c" -> if (isShifted) listOf("Ç") else listOf("ç")
                "n" -> if (isShifted) listOf("Ñ") else listOf("ñ")
                else -> emptyList()
            }
            KeyModel(
                label = label,
                code = label[0].code,
                popupCharacters = popups,
                widthWeight = 1.0f,
                keyType = KeyType.CHARACTER
            )
        }
        rows.add(r2)

        // Row 3: [Shift] z x c v b n m [Delete]
        val r3 = mutableListOf<KeyModel>()
        r3.add(
            KeyModel(
                label = if (isShifted) "⬆" else "⇧",
                code = KeyModel.CODE_SHIFT,
                widthWeight = 1.5f,
                keyType = KeyType.SHIFT
            )
        )
        val r3Chars = listOf("z", "x", "c", "v", "b", "n", "m")
        r3.addAll(r3Chars.map { ch ->
            val label = if (isShifted) ch.uppercase() else ch
            KeyModel(
                label = label,
                code = label[0].code,
                widthWeight = 1.0f,
                keyType = KeyType.CHARACTER
            )
        })
        r3.add(
            KeyModel(
                label = "⌫",
                code = KeyModel.CODE_DELETE,
                widthWeight = 1.5f,
                keyType = KeyType.DELETE
            )
        )
        rows.add(r3)

        // Row 4: [?123] [🌐] [😊] [Spacebar English] [, .] [↵]
        val r4 = listOf(
            KeyModel(label = "?123", code = KeyModel.CODE_MODE_CHANGE, widthWeight = 1.5f, keyType = KeyType.MODE_CHANGE),
            KeyModel(label = "🌐", code = KeyModel.CODE_GLOBE, widthWeight = 1.1f, keyType = KeyType.GLOBE),
            KeyModel(label = "😊", code = KeyModel.CODE_EMOJI, widthWeight = 1.1f, keyType = KeyType.EMOJI),
            KeyModel(label = "English", code = KeyModel.CODE_SPACE, widthWeight = 4.2f, keyType = KeyType.SPACE),
            KeyModel(
                label = ".",
                code = '.'.code,
                secondaryLabel = ",",
                popupCharacters = listOf(",", "?", "!", ":", ";", "."),
                widthWeight = 1.1f,
                keyType = KeyType.CHARACTER
            ),
            KeyModel(label = "↵", code = KeyModel.CODE_ENTER, widthWeight = 1.5f, keyType = KeyType.ENTER)
        )
        rows.add(r4)

        return rows
    }

    fun getSymbolsLayout(showNumberRow: Boolean): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()

        if (showNumberRow) {
            rows.add(getNumberRow())
        }

        // Row 1
        val r1Chars = listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")")
        rows.add(r1Chars.map { ch ->
            KeyModel(label = ch, code = ch[0].code, widthWeight = 1.0f, keyType = KeyType.CHARACTER)
        })

        // Row 2
        val r2Chars = listOf("!", "\"", "'", ":", ";", "/", "?", "~", "`", "|")
        rows.add(r2Chars.map { ch ->
            KeyModel(label = ch, code = ch[0].code, widthWeight = 1.0f, keyType = KeyType.CHARACTER)
        })

        // Row 3
        val r3 = mutableListOf<KeyModel>()
        val r3Chars = listOf("=", "\\", "<", ">", "{", "}", "[", "]")
        r3.addAll(r3Chars.map { ch ->
            KeyModel(label = ch, code = ch[0].code, widthWeight = 1.0f, keyType = KeyType.CHARACTER)
        })
        r3.add(
            KeyModel(label = "⌫", code = KeyModel.CODE_DELETE, widthWeight = 1.5f, keyType = KeyType.DELETE)
        )
        rows.add(r3)

        // Row 4
        val r4 = listOf(
            KeyModel(label = "ABC", code = KeyModel.CODE_MODE_CHANGE, widthWeight = 1.5f, keyType = KeyType.MODE_CHANGE),
            KeyModel(label = "🌐", code = KeyModel.CODE_GLOBE, widthWeight = 1.1f, keyType = KeyType.GLOBE),
            KeyModel(label = "😊", code = KeyModel.CODE_EMOJI, widthWeight = 1.1f, keyType = KeyType.EMOJI),
            KeyModel(label = "Space", code = KeyModel.CODE_SPACE, widthWeight = 4.0f, keyType = KeyType.SPACE),
            KeyModel(label = ".", code = '.'.code, widthWeight = 1.1f, keyType = KeyType.CHARACTER),
            KeyModel(label = "↵", code = KeyModel.CODE_ENTER, widthWeight = 1.5f, keyType = KeyType.ENTER)
        )
        rows.add(r4)

        return rows
    }
}
