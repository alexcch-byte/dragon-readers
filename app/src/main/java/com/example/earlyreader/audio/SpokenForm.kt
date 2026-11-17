package com.example.earlyreader.audio

/**
 * What the voice should be given for a text said ON ITS OWN (a tapped word, a tricky-word card, the help button).
 *
 * A bare "a" or "A" is read as the letter name ("ay"), so the article is spoken as "uh" (the same as the web reader's
 * `say`). Anything longer than one word, such as a whole sentence, is returned unchanged: in context the voice
 * already says "a" correctly.
 */
fun spokenForm(text: String): String {
    val clean = text.trim().trimEnd('.', '!', '?', ',')
    return if (clean.equals("a", ignoreCase = true)) "uh" else text
}
