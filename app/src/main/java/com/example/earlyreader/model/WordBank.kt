package com.example.earlyreader.model

object WordBank {
    val starterWords: List<WordData> = listOf(
        WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱"),
        WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
        WordData(text = "fox", segments = listOf("f", "o", "x"), img = "🦊"),
        WordData(text = "box", segments = listOf("b", "o", "x"), img = "📦"),
        WordData(text = "big", segments = listOf("b", "i", "g")),
        WordData(text = "red", segments = listOf("r", "e", "d")),
    )
}
