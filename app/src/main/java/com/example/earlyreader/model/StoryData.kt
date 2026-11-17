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
                    WordData(text = "on", segments = emptyList(), sight = true),
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
                    WordData(text = "on", segments = emptyList(), sight = true),
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
                    WordData(text = "on", segments = emptyList(), sight = true),
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
                    WordData(text = "on", segments = emptyList(), sight = true),
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
                    WordData(text = "on", segments = emptyList(), sight = true),
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
                    WordData(text = "on", segments = emptyList(), sight = true),
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
                    WordData(text = "on", segments = emptyList(), sight = true),
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
                    WordData(text = "thick.", segments = emptyList(), sight = true),
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
                    WordData(text = "bell!", segments = listOf("b", "e", "l", "l"), img = "🔔"),
                    WordData(text = "A", segments = emptyList(), sight = true),
                    WordData(text = "bell", segments = listOf("b", "e", "l", "l"), img = "🔔"),
                    WordData(text = "rang", segments = listOf("r", "a", "n", "g")),
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
                    WordData(text = "bell.", segments = listOf("b", "e", "l", "l"), img = "🔔"),
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
                    WordData(text = "duck", segments = listOf("d", "u", "c", "k"), img = "🦆"),
                    WordData(text = "sat", segments = listOf("s", "a", "t")),
                    WordData(text = "by", segments = emptyList(), sight = true),
                    WordData(text = "a", segments = emptyList(), sight = true),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "c", "k"), img = "🦆"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "quick.", segments = listOf("q", "u", "i", "c", "k")),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "c", "k"), img = "🦆"),
                    WordData(text = "can", segments = listOf("c", "a", "n")),
                    WordData(text = "swim.", segments = listOf("s", "w", "i", "m"), img = "🏊"),
                    WordData(text = "Swim,", segments = listOf("S", "w", "i", "m"), img = "🏊"),
                    WordData(text = "duck,", segments = listOf("d", "u", "c", "k"), img = "🦆"),
                    WordData(text = "swim!", segments = listOf("s", "w", "i", "m"), img = "🏊"),
                )
            ),
            PageData(
                words = listOf(
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "duck", segments = listOf("d", "u", "c", "k"), img = "🦆"),
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
                    WordData(text = "duck", segments = listOf("d", "u", "c", "k"), img = "🦆"),
                    WordData(text = "had", segments = listOf("h", "a", "d")),
                    WordData(text = "fun", segments = listOf("f", "u", "n")),
                    WordData(text = "in", segments = listOf("i", "n")),
                    WordData(text = "the", segments = emptyList(), sight = true),
                    WordData(text = "pond.", segments = listOf("p", "o", "n", "d"), img = "💧"),
                    WordData(text = "The", segments = emptyList(), sight = true),
                    WordData(text = "pet", segments = listOf("p", "e", "t")),
                    WordData(text = "duck", segments = listOf("d", "u", "c", "k"), img = "🦆"),
                    WordData(text = "is", segments = emptyList(), sight = true),
                    WordData(text = "glad.", segments = listOf("g", "l", "a", "d"), img = "😊"),
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
    )
}
