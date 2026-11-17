# Early Reader

A calm, gentle phonics and early literacy application designed for pre-readers (built for a 4-year-old learning with a parent) and early readers aged 6 to 8. Readers sound out words by dragging a finger across a segmented scrub bar under the active word, hear phonemes blended aloud, discover words in sentence context, and explore grammar concepts in the interactive Sentence Lab.

[![Android CI](https://github.com/alexcch-byte/dragon-readers/actions/workflows/ci.yml/badge.svg)](https://github.com/alexcch-byte/dragon-readers/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Zero Network](https://img.shields.io/badge/Network-100%25%20Offline-success.svg)](README.md)

---

## Guiding Principles

- **Zero Ads, Zero Tracking, Zero Accounts:** No logins, analytics, or third-party SDKs.
- **100% Offline-First:** The Android app requires zero network permissions (`android.permission.INTERNET` is omitted). All progress is persisted locally.
- **Calm, Child-Centric UI:** Warm off-white canvas, high contrast (WCAG AA compliant), large >= 48dp touch targets, clear phonics cues, and delightful star animations without overstimulation.
- **Phonics & Sight Words:** Decodable CVC words (with digraph support like *sh-i-p*) paired with special "tricky words" (sight words like *the*, *to*, *no*) that read as whole words.

---

## Architecture & Codebase

```
dragon-readers/
├── app/                                # Android Jetpack Compose application
│   └── src/
│       ├── main/java/com/example/earlyreader/
│       │   ├── audio/SpeechManager.kt  # Android TextToSpeech with offline voice detection
│       │   ├── model/
│       │   │   ├── WordData.kt         # Starter words & CVC phoneme definitions
│       │   │   └── StoryData.kt        # 5-story suite with multi-sentence pages
│       │   ├── storage/
│       │   │   └── ProgressStorage.kt  # SharedPreferences offline progress persistence
│       │   ├── ui/
│       │   │   ├── scrub/              # Segmented drag-to-sound scrub bar
│       │   │   ├── sentences/          # 6-Level Sentence Lab with Master Celebration
│       │   │   └── theme/              # Color palette, Material 3 typography & theme
│       │   └── MainActivity.kt         # 3-mode switcher, parent gate, Compose previews
│       └── test/java/com/example/earlyreader/
│           └── ScrubBarModelTest.kt    # Full unit test suite (boundaries, TTS, datasets)
├── docs/                               # Browser prototypes (GitHub Pages live demo)
│   ├── index.html                      # 5-story web reader with segmented scrub bar
│   └── sentences.html                  # 6-level web Sentence Lab with local storage
└── .github/workflows/ci.yml            # Automated CI workflow on JDK 17
```

---

## Feature Showcase

### 1. 5 Complete Early Reader Stories
Rich multi-sentence decodable stories for pre-readers:
1. **The Red Hen** (*"A red hen sat on a mat. She had a big nest..."*)
2. **The Big Pig** (*"A big pig sat in the mud. He saw a red bug..."*)
3. **The Sun and the Bug** (*"The hot sun was up in the sky. A bug sat on a warm rock..."*)
4. **The Cat on the Mat** (*"A fat cat sat on a soft mat. He saw a quick rat..."*)
5. **A Pet in the Net** (*"A wet pup ran in the mud. He got stuck in a big net..."*)

- **Interactive Story Switcher:** Pick any story from an accessible modal dialog with completion star indicators.
- **Circular Progression:** Finishing a story offers immediate circular navigation to the next tale.
- **Emoji / Picture Reveal:** Emojis remain calm and hidden until the child's first decode attempt, rewarding reading effort.
- **Whole-Sentence Read Aloud:** Hear the completed page read smoothly with system text-to-speech.

### 2. Android 3-Mode Experience
Switch seamlessly between learning modes from the top bar:
- **Word Practice (Ages 4–5):** Starter word bank cycling with decodable letter segments, dictionary key-word sound hints, and sentence-in-context highlight.
- **Story Mode (Ages 4–6):** Full multi-page decodable reader with tricky word flashcard assistance and celebratory rewards.
- **Sentence Lab (Ages 6–8):** Comprehensive 6-level interactive grammar suite.

### 3. Complete 6-Level Sentence Lab
Ported from the web prototype to native Jetpack Compose with accessibility semantics:
1. **Naming Words (Nouns):** Identify and tap all naming words with `🏷️ noun` badges (`#DBEAFE`).
2. **Action Words (Verbs):** Identify what someone or something is doing with `⚡ verb` badges (`#FFEDD5`).
3. **Describing Words (Adjectives):** Find words that tell what things are like with `🎨 adj` badges (`#DCFCE7`).
4. **Is it a Sentence? (Complete Thoughts vs Fragments):** Read phrases on a quotation card and classify them as `"✅ Complete sentence"` or `"🧩 Only a piece"` with pedagogical explanations.
5. **Build a Sentence:** Tap scrambled words into a sentence tray, select appropriate ending punctuation (`.`, `?`, `!`), and validate capitalisation and grammar with rules-based educational nudges.
6. **Who and What (Roles):** Sort sentence parts into distinct boxes:
   - 👤 **Who or what** (Subject: `#F3E8FF` / `#7E22CE`)
   - ⚡ **Action** (Verb: `#FFEDD5` / `#C2410C`)
   - 🎯 **What?** (Object: `#CCFBF1` / `#0F766E`)
- **🏆 Master Sentence Celebration:** Awarded upon completing all 6 levels, displaying the golden trophy celebration card and persistent stars on level selector pills.

### 4. Accessibility & Offline Voice Guidance
- **Screen Reader Support:** Full TalkBack compatibility with descriptive semantics and polite live regions (`LiveRegionMode.Polite`).
- **Offline TTS Detection:** Detects local vs network voices in `SpeechManager`. If only network voices are available, provides guidance and a safe shortcut to the Android Text-to-speech system settings.
- **Adult Parent Gate:** Mathematical challenge gate (e.g. `4 + 5 = 9`) protecting settings, voice status, and progress reset.

---

## Jetpack Compose Previews

Inspect UI composables directly inside Android Studio without deploying:
- `PreviewWordPracticePhone`: Single-word practice on phone portrait.
- `PreviewStoryModeTablet`: Multi-word story reader on tablet landscape (1280x800dp).
- `PreviewEarlyReaderApp`: Full app with 3-mode switcher and theme.
- `PreviewSegmentedScrubBarCat`: Scrub bar with single-letter word ("cat").
- `PreviewSegmentedScrubBarShip`: Scrub bar with digraph word ("ship").
- `PreviewSentenceLabLevel1` through `PreviewSentenceLabLevel6`: Dedicated previews for each Sentence Lab level.

---

## Continuous Integration & Automated Testing

Every push and pull request to `master` triggers a GitHub Actions workflow (`.github/workflows/ci.yml`):
- Configures Eclipse Temurin JDK 17.
- Executes full JUnit unit test suite (`ScrubBarModelTest.kt`) covering boundary maths, phoneme segmentation, speech voice detection, and all 6 Sentence Lab datasets.
- Runs `./gradlew test assembleDebug`.
- Archives and uploads the release APK artifact (`dragon-readers-debug.apk`).

---

## Building from Source

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17+ (e.g. JetBrains Runtime `jbr` bundled with Android Studio)
- Android SDK API 34+

### Build Debug APK
```bash
# Set JAVA_HOME (Windows PowerShell example)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Run tests and compile debug APK
.\gradlew.bat test assembleDebug
```

Output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Try it in the Browser

The web prototype is live on GitHub Pages in `docs/`:
- **Early Reader Story Demo:** `docs/index.html`
- **Sentence Lab:** `docs/sentences.html`

Both work completely offline after initial load using the browser's built-in Web Speech API.
