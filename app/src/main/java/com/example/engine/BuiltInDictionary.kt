package com.example.engine

object BuiltInDictionary {

    val COMMON_ARABIC_WORDS: List<String> = listOf(
        "السلام", "عليكم", "ورحمة", "الله", "وبركاته", "شكرا", "جزيلا", "عفوا", "أهلا", "سهلا",
        "مرحبا", "صباح", "الخير", "مساء", "النور", "الورد", "تمام", "الحمد", "لله", "كيف",
        "حالك", "أخبارك", "أخي", "حبيبي", "صديقي", "نعم", "لا", "إن", "شاء", "ما",
        "جميل", "رائع", "ممتاز", "بالتوفيق", "مبروك", "ألف", "حياك", "تسلم", "يعطيك", "العافية",
        "جزاك", "خيرا", "أنا", "أنت", "هو", "هي", "نحن", "هم", "اليوم", "غدا",
        "أمس", "الآن", "بعد", "قبل", "مع", "في", "من", "إلى", "على", "عن",
        "هذا", "هذه", "ذلك", "تلك", "التي", "الذي", "الذين", "كل", "عام", "وأنتم",
        "بخير", "رمضان", "كريم", "عيد", "سعيد", "جمعة", "مباركة", "أحبك", "شكراً", "أهلاً",
        "ممكن", "أكيد", "طبعا", "ليس", "كان", "يكون", "سوف", "قد", "حسنا", "طيب",
        "فكرة", "عمل", "بيت", "مدرسة", "جامعة", "سيارة", "طريق", "وقت", "ساعة", "دقيقة",
        "رسالة", "هاتف", "رقم", "اسم", "مكان", "شيء", "أحد", "كبير", "صغير", "جديد",
        "قديم", "سريع", "قريب", "بعيد", "كثير", "قليل", "أول", "آخر", "أفضل", "أهم",
        "صحيح", "واضح", "سهل", "صعب", "معلومات", "سؤال", "جواب", "أرسل", "اتصل", "انتظر",
        "تفضل", "أرجو", "أتمنى", "أعتقد", "أظن", "أعرف", "أفهم", "أريد", "أستطيع", "أحتاج"
    )

    val COMMON_ENGLISH_WORDS: List<String> = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "great", "hello", "thanks", "please", "welcome", "sorry", "today", "tomorrow", "tonight", "love",
        "happy", "amazing", "cool", "phone", "keyboard", "email", "message", "friend", "family", "home"
    )

    val ARABIC_BIGRAMS: Map<String, List<String>> = mapOf(
        "السلام" to listOf("عليكم", "ورحمة"),
        "عليكم" to listOf("ورحمة", "السلام"),
        "ورحمة" to listOf("الله"),
        "الله" to listOf("وبركاته", "يعافيك", "يحفظك", "يسعدك", "خيرا"),
        "صباح" to listOf("الخير", "الورد", "النور"),
        "مساء" to listOf("الخير", "الورد", "النور"),
        "إن" to listOf("شاء", "كنت", "كان"),
        "ان" to listOf("شاء", "كنت", "كان"),
        "شاء" to listOf("الله"),
        "الحمد" to listOf("لله"),
        "جزاك" to listOf("الله"),
        "شكرا" to listOf("جزيلا", "لك"),
        "شكراً" to listOf("جزيلاً", "لك"),
        "أهلا" to listOf("وسهلا", "بك"),
        "أهلاً" to listOf("وسهلاً", "بك"),
        "كل" to listOf("عام", "يوم", "شيء", "واحد"),
        "عام" to listOf("وأنتم"),
        "وأنتم" to listOf("بخير"),
        "عيد" to listOf("سعيد", "مبارك"),
        "رمضان" to listOf("كريم", "مبارك"),
        "جمعة" to listOf("مباركة"),
        "يعطيك" to listOf("العافية"),
        "تسلم" to listOf("حبيبي", "يا", "إيدك"),
        "كيف" to listOf("حالك", "الأمور", "الصحة"),
        "ما" to listOf("شاء", "رأيك", "الأخبار")
    )

    val ENGLISH_BIGRAMS: Map<String, List<String>> = mapOf(
        "how" to listOf("are", "is", "do", "can"),
        "are" to listOf("you", "there", "we"),
        "thank" to listOf("you", "so"),
        "thanks" to listOf("a", "for", "lot"),
        "good" to listOf("morning", "night", "afternoon", "job", "luck"),
        "see" to listOf("you", "it", "later"),
        "i" to listOf("am", "will", "have", "would", "think", "love", "can"),
        "you" to listOf("are", "can", "have", "will", "know"),
        "we" to listOf("are", "can", "have", "will"),
        "let" to listOf("me", "us", "go"),
        "have" to listOf("a", "been", "to"),
        "what" to listOf("is", "are", "do", "about"),
        "nice" to listOf("to", "one")
    )
}
