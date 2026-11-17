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

  Finished levels are remembered on the device (in the browser's local storage) and nothing else is stored.
- **Android app** (Kotlin + Compose): Milestone 1 only. A single hard-coded word ("cat") with the segmented scrub bar, a completion reward and an "Again" button, and accessibility semantics. It has no audio, story player, help button, or Sentence Lab yet (see the milestones in the plan).

Per-letter sounds use short dictionary words ("ant" for a, "hat" for h) because TTS reads a lone letter by its name. Key words start with the letter sound and avoid story words. A real phoneme clip pack is planned.

## Try it in the browser

The demo is live at the GitHub Pages link in the repo "About" section. Drag across the bar to sound out each word (a mouse works too, as do the arrow keys on the bar), tap the speaker for help, and tap tricky words to hear them. The Sentence Lab is at `sentences.html` next to it.

## Download the Android prototype (one word)

Grab the latest debug APK from the [Releases page](../../releases/latest). This is an early prototype with a single word ("cat") to drag, not the full story app; the story and the Sentence Lab only exist in the browser for now. The repo and the APK file (`dragon-readers-debug.apk`) are named "dragon-readers"; the app itself is called Early Reader. The release asset name must stay as is, because the download link depends on it. You'll need to allow "install from unknown sources" since this isn't on the Play Store yet.

## Build from source

```bash
./gradlew assembleDebug
```

Requires an Android SDK (`local.properties` with `sdk.dir=...`, not committed) and JDK 17+.
