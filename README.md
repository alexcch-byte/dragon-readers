# Early Reader

A calm phonics app for pre-readers (built for a 4-year-old, used together with a parent). Short illustrated sentences built from simple words; drag a finger along a segmented bar under the active word to sound it out, then hear it read aloud. A second page, the Sentence Lab, is for readers about 6 to 8.

- No accounts, no ads, no tracking.
- The Android app has no network code at all. The browser pages work without internet once loaded, if your device has a built-in voice (a few browsers only offer online voices, which may send the spoken text to a speech service).
- Kotlin + Jetpack Compose, Material 3.
- See [reading-app-plan.md](reading-app-plan.md) for the full design/planning doc.

**Status:** Three prototypes, none of which bundles audio; the browser pages speak with the browser's built-in voice.

- **Browser demo** (`docs/index.html`): the full "The Red Hen" story flow. Sentence in context, drag-to-scrub segmented bar (digraphs like sh-i-p supported), a picture revealed after the first drag, tricky (sight) words, a spoken help button, and a read-aloud page finish. Keyboard and screen-reader friendly.
- **Sentence Lab** (`docs/sentences.html`, linked from the demo): practice for ages 6 to 8 with six short levels. Each has a worked example, a kind nudge on a wrong answer, a "Show me" button and read-aloud:
  1. **Naming words** (nouns): tap every noun in a sentence.
  2. **Action word** (verb): tap the verb.
  3. **Describing words** (adjectives): tap the adjectives.
  4. **Who and what**: put the parts of a sentence (subject, verb, object) into labelled boxes.
  5. **Is it a sentence?**: decide whether a group of words is a complete thought or only a piece of one.
  6. **Build a sentence**: put scrambled words in order and choose the capital letter and end mark (. ? !).

  Finished levels are remembered on the device (in the browser's local storage), and completing all 6 levels awards a special Sentence Master celebration badge.
- **Android app** (Kotlin + Compose): Milestones 1, 2, 3, 4, and 5+. Features a 3-mode switcher between Word Practice, Story Mode (5-story suite: "The Red Hen", "The Big Pig", "The Sun and the Bug", "The Cat on the Mat", "A Pet in the Net"), and Sentence Lab (Levels 1–5: naming words / nouns, action words / verbs, describing words / adjectives, complete thoughts vs fragments / "Is it a sentence?", and scrambled sentence builder with sentence tray, word & punctuation banks, pedagogical validations, interactive 5-level selector pills, badge tags, kind nudges, TalkBack live regions, and progress persistence), interactive story picker dialog, offline session progress persistence via SharedPreferences, tricky/sight words, segmented scrub bar, auto-advancing, page sentence read-aloud, starter word bank cycling with sentence-in-context display, TextToSpeech audio support with offline voice detection and system TTS settings shortcut, Help button, picture/emoji reveal on decode attempt, parent gate settings with math challenge and progress reset, Compose UI previews for phone and tablet form factors, and accessibility semantics.

Per-letter sounds use short dictionary words ("ant" for a, "hat" for h) because TTS reads a lone letter by its name. Key words start with the letter sound and avoid story words. A real phoneme clip pack is planned.

## Try it in the browser

The demo is live at the GitHub Pages link in the repo "About" section. Features the complete 5-story suite ("The Red Hen", "The Big Pig", "The Sun and the Bug", "The Cat on the Mat", and "A Pet in the Net") with an interactive story switcher, circular next-story navigation, segmented sounding out, TTS audio, picture reveals, and tricky words. The Sentence Lab is at `sentences.html` next to it with an all-levels master celebration.

## Download the Android prototype

Grab the latest debug APK from GitHub Actions CI build artifacts or the Releases page. The release asset name (`dragon-readers-debug.apk`) is preserved for direct download compatibility. You'll need to allow "install from unknown sources" since this isn't on the Play Store yet.

## Continuous Integration

A GitHub Actions workflow (`.github/workflows/ci.yml`) automatically runs on all pushes and pull requests to `master`, verifying the full test suite and generating the `dragon-readers-debug.apk` artifact on JDK 17.

## Build from source

```bash
./gradlew assembleDebug
```

Requires an Android SDK (`local.properties` with `sdk.dir=...`, not committed) and JDK 17+.
