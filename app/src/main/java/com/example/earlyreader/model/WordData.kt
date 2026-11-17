package com.example.earlyreader.model

// Mirrors the word shape from section 5 of reading-app-plan.md's content model.
data class WordData(
    val text: String,
    val segments: List<String>,
    val sight: Boolean = false,
    val img: String? = null,
    val sentenceContext: List<String> = emptyList(),
)
