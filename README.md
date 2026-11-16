# Early Reader

A calm, offline phonics app for pre-readers (built for a 4-year-old, used together with a parent). Short illustrated sentences built from simple words; drag a finger along a segmented bar under the active word to sound it out, then hear it read aloud.

- No ads, no accounts, no analytics, fully offline.
- Kotlin + Jetpack Compose, Material 3.
- See [reading-app-plan.md](reading-app-plan.md) for the full design/planning doc.

**Status:** Two prototypes. Neither bundles audio; the browser demo speaks with the browser's built-in voice.

- **Browser demo** (`docs/index.html`): the full "The Red Hen" story flow. Sentence in context, drag-to-scrub segmented bar (digraphs like sh-i-p supported), a picture revealed after the first drag, tricky (sight) words, a spoken help button, and a read-aloud page finish. Keyboard and screen-reader friendly.
- **Android app** (Kotlin + Compose): Milestone 1 only. A single hard-coded word ("cat") with the segmented scrub bar, a completion reward, and accessibility semantics. It has no audio, story player, or help button yet (see the milestones in the plan).

Per-letter sounds use short dictionary words ("hat" for h) because TTS reads a lone letter by its name. A real phoneme clip pack is planned.

## Try it in the browser

The demo is live at the GitHub Pages link in the repo "About" section. Drag across the bar to sound out each word (a mouse works too, as do the arrow keys on the bar), tap the speaker for help, and tap tricky words to hear them.

## Download the Android app

Grab the latest debug APK from the [Releases page](../../releases/latest). The repo and the APK file (`dragon-readers-debug.apk`) are named "dragon-readers"; the app itself is called Early Reader. The release asset name must stay as is, because the download link depends on it. You'll need to allow "install from unknown sources" since this isn't on the Play Store yet.

## Build from source

```bash
./gradlew assembleDebug
```

Requires an Android SDK (`local.properties` with `sdk.dir=...`, not committed) and JDK 17+.
