package com.example.earlyreader.model

// Mirrors story and page structure from reading-app-plan.md and docs/index.html.
data class PageData(
    val words: List<WordData>,
) {
    val fullSentence: String
        get() = words.joinToString(" ") { it.text }
}

data class StoryData(
    val id: String,
    val title: String,
    val pages: List<PageData>,
)

object StoryBank {
    val redHenStory = StoryData(
        id = "hen",
        title = "The Red Hen",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "red.", segments = listOf("r", "e", "d"), img = "🎈"),
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "fox", segments = listOf("f", "o", "x"), img = "🦊"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g"), img = "🐘"),
                    WordData(text = "box.", segments = listOf("b", "o", "x"), img = "📦"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "fox", segments = listOf("f", "o", "x"), img = "🦊"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "see", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "hen.", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "ran!", segments = listOf("r", "a", "n"), img = "🏃"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "hid", segments = listOf("h", "i", "d")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "red", segments = listOf("r", "e", "d"), img = "🎈"),
                    WordData(text = "box.", segments = listOf("b", "o", "x"), img = "📦"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "fox", segments = listOf("f", "o", "x"), img = "🦊"),
                    WordData(text = "did", segments = listOf("d", "i", "d")),
                    WordData(text = "not", segments = listOf("n", "o", "t")),
                    WordData(text = "get", segments = listOf("g", "e", "t")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "hen.", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "Yes!", segments = listOf("Y", "e", "s")),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "won.", segments = emptyList(), sight = true),
                )
            )
        )
    )
}
