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
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "sat.", segments = listOf("s", "a", "t")),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "red.", segments = listOf("r", "e", "d")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "fox", segments = listOf("f", "o", "x"), img = "🦊"),
                    WordData(text = "ran.", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "fox", segments = listOf("f", "o", "x"), img = "🦊"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "big.", segments = listOf("b", "i", "g")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hen", segments = listOf("h", "e", "n"), img = "🐔"),
                    WordData(text = "got", segments = listOf("g", "o", "t")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
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

    val bigPigStory = StoryData(
        id = "pig",
        title = "The Big Pig",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "pig", segments = listOf("p", "i", "g"), img = "🐷"),
                    WordData(text = "sat.", segments = listOf("s", "a", "t")),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pig", segments = listOf("p", "i", "g"), img = "🐷"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "mud.", segments = listOf("m", "u", "d"), img = "🟫"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pig", segments = listOf("p", "i", "g"), img = "🐷"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "dig.", segments = listOf("d", "i", "g")),
                    WordData(text = "Dig,", segments = listOf("D", "i", "g")),
                    WordData(text = "pig,", segments = listOf("p", "i", "g"), img = "🐷"),
                    WordData(text = "dig!", segments = listOf("d", "i", "g")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "dog", segments = listOf("d", "o", "g"), img = "🐶"),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "to", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "pig.", segments = listOf("p", "i", "g"), img = "🐷"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pig", segments = listOf("p", "i", "g"), img = "🐷"),
                    WordData(text = "and", segments = listOf("a", "n", "d")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "dog", segments = listOf("d", "o", "g"), img = "🐶"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "sun.", segments = listOf("s", "u", "n"), img = "☀️"),
                )
            )
        )
    )

    val sunAndBugStory = StoryData(
        id = "bug",
        title = "The Sun and the Bug",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "sun", segments = listOf("s", "u", "n"), img = "☀️"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "hot.", segments = listOf("h", "o", "t")),
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐞"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "cup.", segments = listOf("c", "u", "p"), img = "🥤"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐞"),
                    WordData(text = "got", segments = listOf("g", "o", "t")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "mud.", segments = listOf("m", "u", "d"), img = "🟫"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐞"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "wet.", segments = listOf("w", "e", "t")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "dog", segments = listOf("d", "o", "g"), img = "🐶"),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "up.", segments = listOf("u", "p")),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "dog", segments = listOf("d", "o", "g"), img = "🐶"),
                    WordData(text = "did", segments = listOf("d", "i", "d")),
                    WordData(text = "not", segments = listOf("n", "o", "t")),
                    WordData(text = "see", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "bug.", segments = listOf("b", "u", "g"), img = "🐞"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐞"),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "bus.", segments = listOf("b", "u", "s"), img = "🚌"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐞"),
                    WordData(text = "won!", segments = emptyList(), sight = true),
                )
            )
        )
    )

    val catOnMatStory = StoryData(
        id = "cat",
        title = "The Cat on the Mat",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "fat", segments = listOf("f", "a", "t")),
                    WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱"),
                    WordData(text = "sat.", segments = listOf("s", "a", "t")),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "mat.", segments = listOf("m", "a", "t"), img = "🧶"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "rat", segments = listOf("r", "a", "t"), img = "🐀"),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "by.", segments = emptyList(), sight = true),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱"),
                    WordData(text = "saw", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "rat.", segments = listOf("r", "a", "t"), img = "🐀"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱"),
                    WordData(text = "got", segments = listOf("g", "o", "t")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "hat.", segments = listOf("h", "a", "t"), img = "🎩"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hat", segments = listOf("h", "a", "t"), img = "🎩"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "cat.", segments = listOf("c", "a", "t"), img = "🐱"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Can", segments = listOf("C", "a", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱"),
                    WordData(text = "nap?", segments = listOf("n", "a", "p"), img = "💤"),
                    WordData(text = "Yes!", segments = listOf("Y", "e", "s")),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "cat", segments = listOf("c", "a", "t"), img = "🐱"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "nap", segments = listOf("n", "a", "p"), img = "💤"),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "mat.", segments = listOf("m", "a", "t"), img = "🧶"),
                )
            )
        )
    )

    val petInNetStory = StoryData(
        id = "net",
        title = "A Pet in the Net",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "boy", segments = emptyList(), sight = true),
                    WordData(text = "has", segments = listOf("h", "a", "s")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "pet.", segments = listOf("p", "e", "t"), img = "🐶"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pet", segments = listOf("p", "e", "t"), img = "🐶"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "wet", segments = listOf("w", "e", "t"), img = "💧"),
                    WordData(text = "dog.", segments = listOf("d", "o", "g"), img = "🐶"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "dog", segments = listOf("d", "o", "g"), img = "🐶"),
                    WordData(text = "got", segments = listOf("g", "o", "t")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "net.", segments = listOf("n", "e", "t"), img = "🕸️"),
                    WordData(text = "Can", segments = listOf("C", "a", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "dog", segments = listOf("d", "o", "g"), img = "🐶"),
                    WordData(text = "get", segments = listOf("g", "e", "t")),
                    WordData(text = "out?", segments = emptyList(), sight = true),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "boy", segments = emptyList(), sight = true, img = "👦"),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "to", segments = emptyList(), sight = true),
                    WordData(text = "help.", segments = listOf("h", "e", "l", "p")),
                    WordData(text = "He", segments = emptyList(), sight = true),
                    WordData(text = "cut", segments = listOf("c", "u", "t"), img = "✂️"),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "net.", segments = listOf("n", "e", "t"), img = "🕸️"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pet", segments = listOf("p", "e", "t"), img = "🐶"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "glad.", segments = listOf("g", "l", "a", "d"), img = "😊"),
                    WordData(text = "Good", segments = emptyList(), sight = true),
                    WordData(text = "dog!", segments = listOf("d", "o", "g"), img = "🐶"),
                )
            )
        )
    )

    val frogOnLogStory = StoryData(
        id = "frog",
        title = "The Frog on the Log",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "frog", segments = listOf("f", "r", "o", "g"), img = "🐸"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "log.", segments = listOf("l", "o", "g"), img = "🪵"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "log", segments = listOf("l", "o", "g"), img = "🪵"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "wet.", segments = listOf("w", "e", "t"), img = "💧"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "frog", segments = listOf("f", "r", "o", "g"), img = "🐸"),
                    WordData(text = "saw", segments = emptyList(), sight = true),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐛"),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "sun.", segments = listOf("s", "u", "n"), img = "☀️"),
                    WordData(text = "Can", segments = listOf("C", "a", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "frog", segments = listOf("f", "r", "o", "g"), img = "🐸"),
                    WordData(text = "get", segments = listOf("g", "e", "t")),
                    WordData(text = "it?", segments = listOf("i", "t")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Jump,", segments = listOf("J", "u", "m", "p"), img = "🦘"),
                    WordData(text = "frog,", segments = listOf("f", "r", "o", "g"), img = "🐸"),
                    WordData(text = "jump!", segments = listOf("j", "u", "m", "p"), img = "🦘"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "frog", segments = listOf("f", "r", "o", "g"), img = "🐸"),
                    WordData(text = "got", segments = listOf("g", "o", "t")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "frog", segments = listOf("f", "r", "o", "g"), img = "🐸"),
                    WordData(text = "had", segments = listOf("h", "a", "d")),
                    WordData(text = "fun", segments = listOf("f", "u", "n")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                    WordData(text = "He", segments = emptyList(), sight = true),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "log.", segments = listOf("l", "o", "g"), img = "🪵"),
                )
            )
        )
    )

    val pupInFogStory = StoryData(
        id = "fog",
        title = "The Pup in the Fog",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "pup", segments = listOf("p", "u", "p"), img = "🐶"),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "out", segments = emptyList(), sight = true),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "fog.", segments = listOf("f", "o", "g"), img = "🌫️"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "fog", segments = listOf("f", "o", "g"), img = "🌫️"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "thick.", segments = listOf("th", "i", "ck")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pup", segments = listOf("p", "u", "p"), img = "🐶"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "fog.", segments = listOf("f", "o", "g"), img = "🌫️"),
                    WordData(text = "Where", segments = emptyList(), sight = true),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "pup?", segments = listOf("p", "u", "p"), img = "🐶"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Hear", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "bell!", segments = listOf("b", "e", "ll"), img = "🔔"),
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "bell", segments = listOf("b", "e", "ll"), img = "🔔"),
                    WordData(text = "rang", segments = listOf("r", "a", "ng")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "fog.", segments = listOf("f", "o", "g"), img = "🌫️"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pup", segments = listOf("p", "u", "p"), img = "🐶"),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "to", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "bell.", segments = listOf("b", "e", "ll"), img = "🔔"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pup", segments = listOf("p", "u", "p"), img = "🐶"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "glad!", segments = listOf("g", "l", "a", "d"), img = "😊"),
                )
            )
        )
    )

    val petDuckStory = StoryData(
        id = "duck",
        title = "The Pet Duck",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "pet", segments = listOf("p", "e", "t")),
                    WordData(text = "duck", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "by", segments = emptyList(), sight = true),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "quick.", segments = listOf("qu", "i", "ck")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "swim.", segments = listOf("s", "w", "i", "m"), img = "🏊"),
                    WordData(text = "Swim,", segments = listOf("S", "w", "i", "m"), img = "🏊"),
                    WordData(text = "duck,", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "swim!", segments = listOf("s", "w", "i", "m"), img = "🏊"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "saw", segments = emptyList(), sight = true),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "fish.", segments = listOf("f", "i", "sh"), img = "🐟"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "fish", segments = listOf("f", "i", "sh"), img = "🐟"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "fast.", segments = listOf("f", "a", "s", "t")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "had", segments = listOf("h", "a", "d")),
                    WordData(text = "fun", segments = listOf("f", "u", "n")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pet", segments = listOf("p", "e", "t")),
                    WordData(text = "duck", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "glad.", segments = listOf("g", "l", "a", "d"), img = "😊"),
                )
            )
        )
    )

    val fishStory = StoryData(
        id = "fish",
        title = "The Fish in the Dish",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "fish", segments = listOf("f", "i", "sh"), img = "🐟"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "dish.", segments = listOf("d", "i", "sh"), img = "🥣"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "fish", segments = listOf("f", "i", "sh"), img = "🐟"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "swim", segments = listOf("s", "w", "i", "m"), img = "🏊"),
                    WordData(text = "and", segments = listOf("a", "n", "d")),
                    WordData(text = "swish.", segments = listOf("s", "w", "i", "sh")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "I", segments = emptyList(), sight = true),
                    WordData(text = "wish", segments = listOf("w", "i", "sh"), img = "⭐"),
                    WordData(text = "for", segments = emptyList(), sight = true),
                    WordData(text = "that", segments = listOf("th", "a", "t")),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "fish", segments = listOf("f", "i", "sh"), img = "🐟"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                    WordData(text = "Swish,", segments = listOf("S", "w", "i", "sh"), img = "🏊"),
                    WordData(text = "swish!", segments = listOf("s", "w", "i", "sh")),
                )
            )
        )
    )

    val chickStory = StoryData(
        id = "chick",
        title = "The Chick and the Chip",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "chick", segments = listOf("ch", "i", "ck"), img = "🐥"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "branch.", segments = listOf("b", "r", "a", "n", "ch"), img = "🌿"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "chick", segments = listOf("ch", "i", "ck"), img = "🐥"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "peck", segments = listOf("p", "e", "ck")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "chip.", segments = listOf("ch", "i", "p"), img = "🥔"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Peck,", segments = listOf("P", "e", "ck")),
                    WordData(text = "chick,", segments = listOf("ch", "i", "ck"), img = "🐥"),
                    WordData(text = "peck", segments = listOf("p", "e", "ck")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "chip!", segments = listOf("ch", "i", "p"), img = "🥔"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "chick", segments = listOf("ch", "i", "ck"), img = "🐥"),
                    WordData(text = "had", segments = listOf("h", "a", "d")),
                    WordData(text = "fun", segments = listOf("f", "u", "n")),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "branch.", segments = listOf("b", "r", "a", "n", "ch"), img = "🌿"),
                    WordData(text = "Chip,", segments = listOf("Ch", "i", "p")),
                    WordData(text = "chop!", segments = listOf("ch", "o", "p")),
                )
            )
        )
    )

    val kingStory = StoryData(
        id = "king",
        title = "The King and His Ring",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "king", segments = listOf("k", "i", "ng"), img = "👑"),
                    WordData(text = "has", segments = listOf("h", "a", "s")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "pink", segments = listOf("p", "i", "nk"), img = "🌸"),
                    WordData(text = "ring.", segments = listOf("r", "i", "ng"), img = "💍"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "king", segments = listOf("k", "i", "ng"), img = "👑"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "bank.", segments = listOf("b", "a", "nk")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "ck"), img = "🦆"),
                    WordData(text = "had", segments = listOf("h", "a", "d")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "wing", segments = listOf("w", "i", "ng"), img = "🪽"),
                    WordData(text = "and", segments = listOf("a", "n", "d")),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "sing.", segments = listOf("s", "i", "ng"), img = "🎵"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Sing", segments = listOf("S", "i", "ng"), img = "🎵"),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "song", segments = listOf("s", "o", "ng"), img = "🎶"),
                    WordData(text = "for", segments = emptyList(), sight = true),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "king!", segments = listOf("k", "i", "ng"), img = "👑"),
                    WordData(text = "Clang,", segments = listOf("c", "l", "a", "ng")),
                    WordData(text = "ring!", segments = listOf("r", "i", "ng"), img = "💍"),
                )
            )
        )
    )

    val buzzStory = StoryData(
        id = "buzz",
        title = "The Buzz in the Grass",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐛"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "buzz", segments = listOf("b", "u", "zz"), img = "🐝"),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "grass.", segments = listOf("g", "r", "a", "ss"), img = "🌿"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐛"),
                    WordData(text = "got", segments = listOf("g", "o", "t")),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "hill.", segments = listOf("h", "i", "ll"), img = "⛰️"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Can", segments = listOf("C", "a", "n")),
                    WordData(text = "it", segments = listOf("i", "t")),
                    WordData(text = "pass", segments = listOf("p", "a", "ss")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "big", segments = listOf("b", "i", "g")),
                    WordData(text = "cliff?", segments = listOf("c", "l", "i", "ff"), img = "🧗"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "bug", segments = listOf("b", "u", "g"), img = "🐛"),
                    WordData(text = "has", segments = listOf("h", "a", "s")),
                    WordData(text = "red", segments = listOf("r", "e", "d")),
                    WordData(text = "fuzz.", segments = listOf("f", "u", "zz")),
                    WordData(text = "Buzz,", segments = listOf("B", "u", "zz"), img = "🐝"),
                    WordData(text = "buzz!", segments = listOf("b", "u", "zz")),
                )
            )
        )
    )

    val campStory = StoryData(
        id = "camp",
        title = "The Camp in the Wind",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "We", segments = emptyList(), sight = true),
                    WordData(text = "set", segments = listOf("s", "e", "t")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "tent", segments = listOf("t", "e", "n", "t"), img = "⛺"),
                    WordData(text = "at", segments = listOf("a", "t")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "camp.", segments = listOf("c", "a", "m", "p"), img = "🏕️"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "fast", segments = listOf("f", "a", "s", "t"), img = "⚡"),
                    WordData(text = "wind", segments = listOf("w", "i", "n", "d"), img = "💨"),
                    WordData(text = "hit", segments = listOf("h", "i", "t")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "tent.", segments = listOf("t", "e", "n", "t"), img = "⛺"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Get", segments = listOf("G", "e", "t")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "lamp", segments = listOf("l", "a", "m", "p"), img = "🏮"),
                    WordData(text = "at", segments = listOf("a", "t")),
                    WordData(text = "dusk.", segments = listOf("d", "u", "s", "k"), img = "🌆"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "We", segments = emptyList(), sight = true),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "rest", segments = listOf("r", "e", "s", "t"), img = "😴"),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "tent.", segments = listOf("t", "e", "n", "t"), img = "⛺"),
                    WordData(text = "Rest,", segments = listOf("R", "e", "s", "t"), img = "😴"),
                    WordData(text = "camp!", segments = listOf("c", "a", "m", "p"), img = "🏕️"),
                )
            )
        )
    )

    val sandStory = StoryData(
        id = "sand",
        title = "Mom on the Sand",
        pages = listOf(
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "hot", segments = listOf("h", "o", "t")),
                    WordData(text = "sun", segments = listOf("s", "u", "n"), img = "☀️"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "sand.", segments = listOf("s", "a", "n", "d"), img = "🏖️"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Mom", segments = listOf("M", "o", "m")),
                    WordData(text = "ran", segments = listOf("r", "a", "n"), img = "🏃"),
                    WordData(text = "on", segments = listOf("o", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "sand.", segments = listOf("s", "a", "n", "d"), img = "🏖️"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Mom", segments = listOf("M", "o", "m")),
                    WordData(text = "saw", segments = emptyList(), sight = true),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "crab", segments = listOf("c", "r", "a", "b"), img = "🦀"),
                    WordData(text = "and", segments = listOf("a", "n", "d")),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "shell.", segments = listOf("sh", "e", "ll"), img = "🐚"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "Mom", segments = listOf("M", "o", "m")),
                    WordData(text = "and", segments = listOf("a", "n", "d")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "crab", segments = listOf("c", "r", "a", "b"), img = "🦀"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "swim.", segments = listOf("s", "w", "i", "m"), img = "🏊"),
                    WordData(text = "Run,", segments = listOf("R", "u", "n"), img = "🏃"),
                    WordData(text = "Mom,", segments = listOf("M", "o", "m")),
                    WordData(text = "swim!", segments = listOf("s", "w", "i", "m"), img = "🏊"),
                )
            )
        )
    )

    val allStories: List<StoryData> = listOf(
        redHenStory,
        bigPigStory,
        sunAndBugStory,
        catOnMatStory,
        petInNetStory,
        frogOnLogStory,
        pupInFogStory,
        petDuckStory,
        fishStory,
        chickStory,
        kingStory,
        buzzStory,
        campStory,
        sandStory,
    )
}
