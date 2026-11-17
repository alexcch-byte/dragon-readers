# Early Reader

A calm, gentle phonics and early literacy application designed for pre-readers (built for a 4-year-old learning with a parent) and early readers aged 6 to 8. Readers sound out words by dragging a finger across a segmented scrub bar under the active word, hear phonemes blended aloud, discover words in sentence context, and explore grammar concepts in the interactive Sentence Lab.

[![Android CI](https://github.com/alexcch-byte/dragon-readers/actions/workflows/ci.yml/badge.svg)](https://github.com/alexcch-byte/dragon-readers/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Zero Network](https://img.shields.io/badge/Network-100%25%20Offline-success.svg)](README.md)

---

## Guiding Principles

- **Zero Ads, Zero Tracking, Zero Accounts:** No logins, analytics, or third-party SDKs.
- **100% Offline-First:** The Android app requires zero network permissions (`android.permission.INTERNET` is omitted). All progress is persisted locally.
- **Calm, Child-Centric UI:** Warm off-white canvas, high-contrast colours (chosen for WCAG AA contrast, not formally audited), large touch targets (the main controls, such as the mode switcher, buttons and level pills, are at least 48dp; a few secondary taps, such as the words inside a sentence, are smaller), clear phonics cues, and star animations without overstimulation.
- **Phonics & Sight Words:** Decodable CVC words (with digraph support like *sh-i-p*) paired with special "tricky words" (sight words like *the*, *to*, *no*) that read as whole words.

---

## Architecture & Codebase

```
dragon-readers/
├── app/                                # Android Jetpack Compose application
│   └── src/
│       ├── main/java/com/example/earlyreader/
│       │   ├── audio/
│       │   │   ├── SpeechManager.kt    # Android TextToSpeech: queued sounds, offline voice detection
│       │   │   └── VoiceSelection.kt   # Pure voice-choice / offline-guidance logic (unit tested)
│       │   ├── model/
│       │   │   ├── WordData.kt         # Starter words & CVC phoneme definitions
│       │   │   └── StoryData.kt        # 8-story suite with multi-sentence pages
│       │   ├── storage/
│       │   │   └── ProgressStorage.kt  # Offline progress (SharedPreferences behind a small interface)
│       │   ├── ui/
│       │   │   ├── scrub/              # Segmented drag-to-sound scrub bar
│       │   │   ├── sentences/          # 6-Level Sentence Lab with Master Celebration
│       │   │   └── theme/              # Color palette, Material 3 typography & theme
│       │   └── MainActivity.kt         # 4-mode switcher, parent gate, Compose previews
│       └── test/java/com/example/earlyreader/
│           └── ScrubBarModelTest.kt    # Unit tests: data, sounds, voice choice, progress, tracker limit
├── docs/                               # Browser prototypes (GitHub Pages live demo)
│   ├── index.html                      # 8-story web reader with segmented scrub bar
│   └── sentences.html                  # 6-level web Sentence Lab with local storage
└── .github/workflows/ci.yml            # Automated CI workflow on JDK 17
```

---

## Feature Showcase

### 1. 8 Complete Early Reader Stories
Rich multi-sentence decodable stories for pre-readers. The eight stories are the same on the web reader and the Android app (the first page of each is quoted here):
1. **The Red Hen** (*"A hen sat. The hen is red."*)
2. **The Big Pig** (*"A big pig sat. The pig is in mud."*)
3. **The Sun and the Bug** (*"The sun is hot. A bug sat on a cup."*)
4. **The Cat on the Mat** (*"A fat cat sat. The cat is on a mat."*)
5. **A Pet in the Net** (*"A boy has a pet. The pet is a wet dog."*)
6. **The Frog on the Log** (*"A frog sat on a big log. The log is wet."*)
7. **The Pup in the Fog** (*"A pup ran out in the fog. The fog is thick."*)
8. **The Pet Duck** (*"A pet duck sat by a pond. The duck is quick."*)

Digraphs and double letters are ONE segment (*d-u-ck*, *b-e-ll*, *r-a-ng*, *qu-i-ck*, *th-i-ck*, *f-i-sh*), so each makes one sound. Only words that cannot be sounded out are tricky words (*a, the, is, to, saw, out ...*).

- **Interactive Story Switcher:** Pick any story from an accessible modal dialog with completion star indicators.
- **Circular Progression:** Finishing a story offers immediate circular navigation to the next tale.
- **Emoji / Picture Reveal:** Emojis remain hidden until the child's first decode attempt (in Track to Read, until the tracker has passed the word), rewarding reading effort.
- **Whole-Sentence Read Aloud:** Hear the completed page read smoothly with system text-to-speech, or tap the sentence container or "🔊 Read full sentence" button anytime to hear the entire sentence read aloud in context.

### 2. Android 4-Mode Experience
Switch seamlessly between learning modes from the top bar:
- **Word Practice (Ages 4–5):** Starter word bank cycling with decodable letter segments, short synthetic phonics sounds ("ah", "aw", "buh", "kuh"), sentence-in-context highlight, and tap-to-read full sentence audio.
- **Story Mode (Ages 4–6):** Full multi-page decodable reader with segmented scrub bar, tricky word flashcard assistance, dedicated "🔊 Read full sentence" audio button, and celebratory rewards. The page, word and story position is kept when switching tabs.
- **Track to Read (Ages 4–6):** Stepped sentence tracker with a draggable slider bar that jumps discretely from word to word and zero-shift word chips. The tracker moves at most one word past the furthest word reached, so one tap cannot finish a sentence; tapping a word chip only highlights it (and speaks it in Speak Words mode). Defaults to **🔇 Highlight Only** (silent tracking so the child can read each word aloud without the app speaking over them), with an optional **🔊 Speak Words** toggle and on-demand full sentence playback.
- **Sentence Lab (Ages 6–8):** Comprehensive 6-level interactive grammar suite.

### 3. Complete 6-Level Sentence Lab
Ported from the web prototype to native Jetpack Compose with accessibility semantics. The levels are in the same order on the web and on Android:
1. **Naming Words (Nouns):** Identify and tap all naming words with `🏷️ noun` badges (`#DBEAFE`).
2. **Action Words (Verbs):** Identify what someone or something is doing with `⚡ verb` badges (`#FFEDD5`).
3. **Describing Words (Adjectives):** Find words that tell what things are like with `🎨 adj` badges (`#DCFCE7`).
4. **Who and What (Roles):** Sort sentence parts into distinct boxes:
   - 👤 **Who or what** (Subject: `#F3E8FF` / `#7E22CE`)
   - ⚡ **Action** (Verb: `#FFEDD5` / `#C2410C`)
   - 🎯 **What?** (Object: `#CCFBF1` / `#0F766E`)
5. **Is it a Sentence? (Complete Thoughts vs Fragments):** Read phrases on a quotation card and classify them as `"✅ Complete sentence"` or `"🧩 Only a piece"` with pedagogical explanations. A wrong guess followed by the only other button counts as a peek, not a solve.
6. **Build a Sentence:** Tap scrambled words into a sentence tray, select appropriate ending punctuation (`.`, `?`, `!`), and validate capitalisation and grammar with rules-based educational nudges.
- **🏆 Master Sentence Celebration:** Awarded when all 6 levels are complete, with the golden trophy card and persistent stars on the level pills. "Show me" is always available, but a level finished only by peeking earns no star and no trophy credit (the web page marks it "✨ peeked").

### 4. Accessibility & Offline Voice Guidance
- **Screen Reader Support:** Full TalkBack compatibility with descriptive semantics and polite live regions (`LiveRegionMode.Polite`).
- **Offline TTS Detection:** Detects local vs network voices in `SpeechManager`. If only network voices are available, provides guidance and a safe shortcut to the Android Text-to-speech system settings.
- **Adult Parent Gate:** Mathematical challenge gate (e.g. `4 + 5 = ?`) protecting settings, voice status, and progress reset. A wrong answer closes the gate and the next try is a new question; "Reset all progress" asks for a second confirmation.

---

## Jetpack Compose Previews

Inspect UI composables directly inside Android Studio without deploying:
- `PreviewWordPracticePhone`: Single-word practice on phone portrait.
- `PreviewStoryModeTablet`: Multi-word story reader on tablet landscape (1280x800dp).
- `PreviewEarlyReaderApp`: Full app with the 4-mode switcher and theme.
- `PreviewSegmentedScrubBarCat`: Scrub bar with single-letter word ("cat").
- `PreviewSegmentedScrubBarShip`: Scrub bar with digraph word ("ship").
- `PreviewSentenceLabLevel1` through `PreviewSentenceLabLevel6`: Dedicated previews for each Sentence Lab level.

---

## Continuous Integration & Automated Testing

Every push and pull request to `master` triggers a GitHub Actions workflow (`.github/workflows/ci.yml`):
- Configures Eclipse Temurin JDK 17.
- Executes the JUnit unit test suite (`ScrubBarModelTest.kt`): story and sound-map data (every segment has a sound, no `ck`/`ll`/`ng`/`qu` split in two, the short vowels sound different), scrub-bar boundary maths and thresholds, the voice-choice and offline-guidance logic, progress bookkeeping, the Track to Read word limit, and the Sentence Lab datasets. The scrub gesture, text-to-speech playback and the Compose screens themselves are not unit tested; they were checked by hand and in the browser.
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

Once a page has loaded, it keeps working without internet using the browser's built-in Web Speech API (there is no service worker or install, so reloading the page needs a connection). The Android app needs no network at all.

---

## Known Limitations

- **Phoneme sounds are respelled words.** Speech engines cannot say an isolated sound, so each sound is a short respelling spoken by the system voice. The short vowels are *a* "ah", *e* "eh", *i* "ih", *o* "aw", *u* "uh" (an engine cannot say the short *a* of "apple" on its own, so "ah" stands in for it and short *o* is "aw"; they are different from each other, but neither is a perfect phoneme). How good it sounds depends on the device voice.
- **The APK on the GitHub releases page is the older one-word ("cat") prototype build.** It has not been rebuilt, so it does not include the stories, Track to Read or the Sentence Lab. Build from source to get the current app.
- Sentence Lab progress on Android is remembered per level id, but the last-opened level is remembered by position, so after the level reorder in this version, an older saved level position may open a different level once.
