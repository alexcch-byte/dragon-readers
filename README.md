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
│       │   │   ├── SpeechManager.kt    # Android TextToSpeech: queued sounds, voice choice, ready / no engine / no language status
│       │   │   ├── SpokenForm.kt       # What is said for a word on its own (the article "a" is "uh")
│       │   │   └── VoiceSelection.kt   # Pure voice-choice, speech-status and offline-guidance logic (unit tested)
│       │   ├── model/
│       │   │   ├── WordData.kt         # The word data class (letter segments, tricky flag, picture, sentence)
│       │   │   ├── WordBank.kt         # The six starter CVC words for Word Practice
│       │   │   └── StoryData.kt        # 14-story suite with multi-sentence pages
│       │   ├── storage/
│       │   │   └── ProgressStorage.kt  # Offline progress (SharedPreferences behind a small interface)
│       │   ├── ui/
│       │   │   ├── CenteredScrollColumn.kt  # Centred column that scrolls on small phones and returns to the top per page
│       │   │   ├── scrub/              # Segmented drag-to-sound scrub bar
│       │   │   ├── sentences/          # 6-Level Sentence Lab with Master Celebration
│       │   │   ├── theme/              # Color palette, Material 3 typography & theme
│       │   │   └── welcome/            # Welcome page (tiles, mascot) and its pure logic (Read tile, progress line, saved mode)
│       │   └── MainActivity.kt         # Welcome page + 4-mode switcher, parent gate, Compose previews
│       └── test/java/com/example/earlyreader/
│           ├── ScrubBarModelTest.kt    # Unit tests: data, sounds, voice choice, progress, tracker limit
│           └── WelcomeModelTest.kt     # Unit tests: Welcome tiles, Read-tile caption, saved mode, progress line
├── docs/                               # Browser prototypes (GitHub Pages live demo)
│   ├── index.html                      # Welcome page + 14-story web reader with segmented scrub bar
│   └── sentences.html                  # 6-level web Sentence Lab with local storage
└── .github/workflows/ci.yml            # Automated CI workflow on JDK 17
```

---

## Feature Showcase

### 1. 14 Complete Early Reader Stories (5 Difficulty Tiers)
Rich multi-sentence decodable stories for pre-readers that get gradually harder. The difficulty tiers below are approximate (they follow the sounds each story introduces), and page length is not graded. The 14 stories are the same on the web reader and the Android app:

#### Level 1: Basic CVC Words
1. **The Red Hen** (*"A hen sat. The hen is red."*)
2. **The Big Pig** (*"A big pig sat. The pig is in mud."*)
3. **The Sun and the Bug** (*"The sun is hot. A bug sat on a cup."*)
4. **The Cat on the Mat** (*"A fat cat sat. The cat is on a mat."*)
5. **A Pet in the Net** (*"A boy has a pet. The pet is a wet dog."*)

#### Level 2: Consonant Blends & Double Digraphs
6. **The Frog on the Log** (*"A frog sat on a big log. The log is wet."*)
7. **The Pup in the Fog** (*"A pup ran out in the fog. The fog is thick."*)
8. **The Pet Duck** (*"A pet duck sat by a pond. The duck is quick."*)

#### Level 3: Core Consonant Digraphs & Glued Sounds
9. **The Fish in the Dish** (*"A big fish is in a dish."*) — Focus: Digraphs `sh`, `th` and blend `sw`.
10. **The Chick and the Chip** (*"A chick sat on a branch."*) — Focus: Digraphs `ch`, `ck` and blend `br`.
11. **The King and His Ring** (*"The king has a pink ring."*) — Focus: Nasal glued sounds `ng`, `nk`.

#### Level 4: Double Consonant Endings & Complex Blends
12. **The Buzz in the Grass** (*"A bug can buzz in the grass."*) — Focus: Double letters `zz`, `ss`, `ll`, `ff`.
13. **The Camp in the Wind** (*"We set a tent at the camp."*) — Focus: Consonant clusters and blends `mp`, `st`, `nd`, `nt`, `ft`.

#### Level 5: Master Mixed Decodable
14. **Mom on the Sand** (*"The hot sun is on the sand."* / *"Mom ran on the sand."*) — Focus: Synthesizing blends and digraphs; includes the reference mockup sentence *\"Mom ran on the sand.\"*

Digraphs, double letters, and glued sounds are ONE segment (*d-u-ck*, *b-e-ll*, *r-a-ng*, *qu-i-ck*, *th-i-ck*, *f-i-sh*, *ch-i-ck*, *k-i-ng*, *p-i-nk*, *b-u-zz*, *c-a-m-p*), so each makes one sound. Only words that cannot be sounded out are tricky words (*a, the, is, to, saw, out, we, for ...*).

- **Interactive Story Switcher:** Pick any story from an accessible modal dialog with completion star indicators.
- **Circular Progression:** Finishing a story offers immediate circular navigation to the next tale.
- **Emoji / Picture Reveal:** Emojis remain hidden until the child's first decode attempt (in Track to Read, until the tracker has passed the word), rewarding reading effort.
- **Whole-Sentence Read Aloud:** Hear the completed page read smoothly with system text-to-speech, or tap the sentence container or "🔊 Read full sentence" button anytime to hear the entire sentence read aloud in context. Whole sentences are read slowly, about twice as long as at normal speed (on Android half the word speed; on the web the speed is chosen per voice, because some Windows voices ignore very low settings); a Sentence Lab instruction line is read at a normal pace.

### 2. Welcome Page and the Android 4-Mode Experience
**Welcome page.** The app opens on a friendly Welcome page (and so does the web demo): the dragon mascot, "Early Reader", "Hi! What shall we do today?" and four big picture tiles, each at least 96 dp (px on the web) tall, with a picture, a one- or two-word label and a small caption:
- **👆 Read** opens Track to Read on the story the child was on. Its caption says "Continue: The Cat on the Mat" once there is something to carry on with (a story other than the first, or a reading screen opened since the app started) and "Start: The Red Hen" otherwise.
- **🔤 Sounds** opens Word Practice (on the web, Phonics Scrub).
- **📖 Stories** opens Story Mode (on the web, the story picker).
- **✏️ Sentence Lab** (captioned "for ages 6+") opens the Sentence Lab (on the web, `sentences.html`).

A small, gentle progress line ("⭐ 3 of 14 stories finished", and on Android "🔤 12 words sounded out") shows only once there is some progress. The dragon / "🔊 Hear hello" button says "Hi! Let's read!" at the normal word speed, only when it is tapped: nothing is ever spoken when the page opens. The grown-ups' gear stays in the top bar on Android (still behind the maths gate), and "For grown-ups" stays in the web footer. The tiles are a 2x2 grid on phones and a single row of four on wide screens (a tablet in landscape; the web page switches at 760 px); the page scrolls when it is taller than the screen. Every tile has a screen-reader description (for example "Read, continue The Cat on the Mat", announced as a button), the title is a heading, and colour is never the only cue (each tile has its own picture and words).

Switch seamlessly between learning modes from the top bar (on the Welcome page no chip is selected):
- **Word Practice (Ages 4–5):** Starter word bank cycling with decodable letter segments, short synthetic phonics sounds ("ah", "aw", "buh", "kuh"), sentence-in-context highlight, and tap-to-read full sentence audio.
- **Story Mode (Ages 4–6):** Full multi-page decodable reader with segmented scrub bar, tricky word flashcard assistance, dedicated "🔊 Read full sentence" audio button, and celebratory rewards. The page and word position in each story is kept when switching tabs, stories or going Home; it is forgotten when a story is finished or progress is reset.
- **Track to Read (Ages 4–6):** Stepped sentence tracker with a draggable slider bar that jumps discretely from word to word and zero-shift word chips. The tracker moves at most one word past the furthest word reached, so one tap cannot finish a sentence, and a tap on the bar can only step on to a new word about every 300 ms, while even a real drag reaches at most one new word every 80 ms, so neither mashing nor a flick or wobble across the bar can finish a page in a blink (an ordinary slide along the words is not noticeably slowed); a screen-reader step (TalkBack's step action on Android) or an arrow key (web only; the Android tracker has no keyboard handling) moves one word at a time; tapping a word chip only highlights it (and speaks it in Speak Words mode). Defaults to **🔇 Highlight Only** (silent tracking so the child can read each word aloud without the app speaking over them), with an optional **🔊 Speak Words** toggle and on-demand full sentence playback.
- **Sentence Lab (Ages 6–8):** Comprehensive 6-level interactive grammar suite, meant for older readers (the "Lab" chip is described as such to screen readers). It is not for the 4-year-old; the reading modes are. Tapping a level's instruction line reads it aloud on Android. Its level pills wrap onto a second line on a narrow phone and the screen scrolls when a level is taller than the screen.

**Home button (🏠, top left):** Home opens the Welcome page. It no longer asks anything and it never erases anything: each screen keeps its page, word and level, stars and progress are untouched, speech stops as the page changes, and a tile (or the matching mode chip) takes the child back to where they were. On Android, while the Welcome page is showing, Home has a violet ring and is announced as selected ("you are on the welcome page"), and the mode chips are all unselected; the system Back button on any screen also goes to the Welcome page, and Back on the Welcome page leaves the app. A fresh launch opens the Welcome page; the saved "last mode" is still the last real screen (Home and the Welcome page are never saved as a mode), and unknown or old saved values still mean Track to Read. The only way to clear stars and places is "Reset all progress" in the grown-ups' settings, which then returns to the Welcome page. The web reader's Home button works the same way: it stops speech, shows the Welcome page, and a tile continues the current story at the page that was showing (kept for this browser tab session, so a reload also opens the Welcome page and "Read" carries on from the saved page).

On narrow phones or with large text the top bar puts Home and the grown-ups' settings gear together on the first line and moves the mode chips (all at least 48dp wide and tall) to a second line, so the gear is always reachable and never alone on a line. The story picker and settings dialogs scroll, and so do the reading screens and the Sentence Lab, which return to the top for each new page, word or sentence and scroll the Next buttons into view when a page is finished. A bare "a" is spoken as "uh" (not the letter name "ay"), and the app's launcher icon is a dragon reading a book.

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
- **Offline TTS Detection:** Detects local vs network voices in `SpeechManager`, preferring an installed US English voice, then any installed English one. If only network voices are available, provides guidance and a safe shortcut to the Android Text-to-speech system settings. The grown-ups' settings say plainly when there is no speech engine ("No speech engine found") or no English language data ("Language data missing: install an English voice"), instead of reporting a network problem.
- **Adult Parent Gate:** Mathematical challenge gate (e.g. `4 + 5 = ?`) protecting settings, voice status, and progress reset. A wrong answer closes the gate and the next try is a new question; "Reset all progress" asks for a second confirmation.

---

## Jetpack Compose Previews

Inspect UI composables directly inside Android Studio without deploying:
- `PreviewWordPracticePhone`: Single-word practice on phone portrait.
- `PreviewStoryModeTablet`: Multi-word story reader on tablet landscape (1280x800dp).
- `PreviewEarlyReaderApp`: Full app with the 4-mode switcher and theme.
- `PreviewWelcomePhone` / `PreviewWelcomeTablet`: The Welcome page on a 320x568dp phone (with progress) and a 1280x800dp tablet.
- `PreviewSegmentedScrubBarCat`: Scrub bar with single-letter word ("cat").
- `PreviewSegmentedScrubBarShip`: Scrub bar with digraph word ("ship").
- `PreviewSentenceLabLevel1` through `PreviewSentenceLabLevel6`: Dedicated previews for each Sentence Lab level.

---

## Continuous Integration & Automated Testing

Every push and pull request to `master` triggers a GitHub Actions workflow (`.github/workflows/ci.yml`):
- Configures Eclipse Temurin JDK 17.
- Executes the JUnit unit test suite (`ScrubBarModelTest.kt` and `WelcomeModelTest.kt`): the Welcome page logic (which screen each tile opens, the Read tile's Start / Continue caption and description, saved-mode mapping including unknown and old values, the progress line), story and sound-map data (every segment has a sound, no `ck`/`ll`/`ng`/`qu` split in two, the short vowels sound different), scrub-bar boundary maths and thresholds, the voice-choice, speech-status and offline-guidance logic, progress bookkeeping, the Track to Read word limit and its tap-interval rule, and the Sentence Lab datasets (including the alternative and silly Build sentences). The web/Android story parity test fails (it is not skipped) if `docs/index.html` cannot be found. The scrub gesture, text-to-speech playback and the Compose screens themselves are not unit tested; they were checked by hand and in the browser.
- Runs `./gradlew test assembleDebug`.
- Archives and uploads the debug APK artifact (`dragon-readers-debug.apk`); no release build is made.

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

- **Orientation.** Phones are locked to portrait. Tablets (smallest screen width 600dp or more, such as a Fire HD) may rotate to landscape; the screens scroll, but the landscape layout has not been checked on a real tablet.
- **The Welcome page's "Continue" is remembered per story, not per page, across app restarts.** While the app is open each screen keeps its exact page and word when the child goes Home and back; after the app is closed Android remembers which story the child was on (and every star) but starts that story again at page 1. On the web the page is kept for the browser tab session only, and the web progress line counts stories finished in the same session.
- **Phoneme sounds are respelled words.** Speech engines cannot say an isolated sound, so each sound is a short respelling spoken by the system voice. The short vowels are *a* "ah", *e* "eh", *i* "ih", *o* "aw", *u* "uh" (an engine cannot say the short *a* of "apple" on its own, so "ah" stands in for it and short *o* is "aw"; they are different from each other, but neither is a perfect phoneme). How good it sounds depends on the device voice.
- **The APK on the GitHub releases page is the older one-word ("cat") prototype build.** It has not been rebuilt, so it does not include the stories, Track to Read or the Sentence Lab. Build from source to get the current app.
- Sentence Lab progress on Android is remembered per level id, but the last-opened level is remembered by position, so after the level reorder in this version, an older saved level position may open a different level once.
