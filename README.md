# Early Reader

A calm, offline phonics app for pre-readers (built for a 4-year-old, used together with a parent). Short illustrated sentences built from simple words; drag a finger along a segmented bar under the active word to sound it out, then hear it read aloud.

- No ads, no accounts, no analytics, fully offline.
- Kotlin + Jetpack Compose, Material 3.
- See [reading-app-plan.md](reading-app-plan.md) for the full design/planning doc.

**Status:** Milestone 1 — single-screen prototype (drag-to-scrub segmented bar for the word "cat", no audio yet).

## Try it in the browser

A browser recreation of the drag-to-scrub interaction is live at the GitHub Pages link above (repo "About" section) — drag across the bar to sound out "cat".

## Download the Android app

Grab the latest debug APK from the [Releases page](../../releases/latest). You'll need to allow "install from unknown sources" since this isn't on the Play Store yet.

## Build from source

```bash
./gradlew assembleDebug
```

Requires an Android SDK (`local.properties` with `sdk.dir=...`, not committed) and JDK 17+.
