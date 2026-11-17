package com.example.earlyreader.model

object WordBank {
    val starterWords: List<WordData> = listOf(
        WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱", sentenceContext = listOf("A", "cat", "sat.")),
        WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔", sentenceContext = listOf("The", "hen", "is", "red.")),
        WordData(text = "fox", segments = listOf("f", "o", "x"), img = "🦊", sentenceContext = listOf("A", "fox", "ran.")),
        WordData(text = "box", segments = listOf("b", "o", "x"), img = "📦", sentenceContext = listOf("Get", "in", "a", "box.")),
        WordData(text = "big", segments = listOf("b", "i", "g"), img = "🐘", sentenceContext = listOf("The", "fox", "is", "big.")),
        WordData(text = "red", segments = listOf("r", "e", "d"), img = "🎈", sentenceContext = listOf("The", "hen", "is", "red.")),
    )
}
