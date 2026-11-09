package com.example.ime.view

import android.content.Context
import android.util.AttributeSet

/**
 * SuggestionStripView inherits from SuggestionHeaderAdapterView.
 * Ensures zero empty placeholder dots and seamless backwards compatibility.
 */
class SuggestionStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SuggestionHeaderAdapterView(context, attrs, defStyleAttr)
