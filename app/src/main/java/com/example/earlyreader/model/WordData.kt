package com.example.earlyreader.model

// One word for Word Practice or a story page: its letter segments (one per sound), whether it is a tricky sight
// word, an optional picture (an emoji) and the sentence it appears in.
data class WordData(
    val text: String,
    val segments: List<String>,
    val sight: Boolean = false,
    val img: String? = null,
    val sentenceContext: List<String> = emptyList(),
)
