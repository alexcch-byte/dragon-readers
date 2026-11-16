# Early Reader App (Android) — Planning Document

Working title: **TBD** (placeholder package: `com.example.earlyreader`)
Audience: a 4-year-old pre-reader, used together with a parent (co-play) or briefly alone.
Style inspiration: Reading.com-type phonics apps (guided, short sessions, decodable books, calm colorful UI). Do NOT copy their branding, characters, or assets.

## 1. Core concept
Short illustrated stories of simple phrases built from 3–4 letter words. The active word is shown large with a segmented progress bar underneath. The child drags a finger along the bar to isolate/sound out the word. A help button speaks the correct pronunciation.

## 2. Interaction spec
- Page shows one short sentence (3–5 words). One word is **active** (enlarged); others dimmed.
- Under the active word: a **segmented bar**, one segment per grapheme (c-a-t, sh-i-p). Segment widths proportional to letter count. Touch targets >= 72dp.
- Dragging forward highlights/fills segments under the finger. Reaching the end speaks the whole word and shows a small reward.
- Drag is forgiving: vertical drift up to ~80dp still counts; respond to the first pointer only (palm rejection).
- Backward drag: silent or soft replay; never penalize.
- **Help button** (large speaker icon): speaks the word at slow rate, then replays segment by segment with highlight following.
- **Picture** for common nouns/verbs appears beside the active word after his first drag attempt (so he tries to decode first). Tapping the picture speaks the word. Sight words (a, the, is, on, to, I) have no picture and use whole-word highlight ("tricky word").
- After all words are done, the full sentence is read aloud once, then next-page button.
- No text input, no ads, no external links. Parent gate (hold-to-open or simple math) for settings.
- Sessions: 5–10 pages per story, clean end screen. No streaks or timers.

## 3. Audio approach (generic voice via Android TextToSpeech)
- Use `android.speech.tts.TextToSpeech`. Explicitly choose a voice: filter `tts.voices` for en-CA/en-US, `!isNetworkConnectionRequired`, high quality; call `setVoice()`. Rate ~0.8, pitch 1.0 (maybe 1.1; test).
- Prompt parent to install offline voice data if only a poor default exists.
- Pre-synthesize each word with `synthesizeToFile` at first launch and cache (instant playback, consistent).
- Problem: TTS says letter *names*, not phoneme sounds, for isolated letters. Scrub-sound options, in order of build:
  1. **Visual-only scrub** (highlight + soft tick; speak whole word at end). Start here.
  2. Respelled TTS per segment ("rrr", "eh", "duh"): unreliable, tune per voice.
  3. **Tape-style scrub**: synthesize word to PCM, map finger x to playback position via `AudioTrack` (granular/slow playback). Segment boundaries approximated by letter width, hand-tuned for tricky words. Try after the prototype; fall back to option 1 if it sounds poor.
- Test on the actual tablet/phone; voice quality varies by device.

## 4. Tech stack
- Kotlin, Jetpack Compose, Material 3 (custom theme).
- Custom `Canvas` composable for the scrub bar; `pointerInput` + `detectDragGestures` mapping x -> segment index.
- Audio: `TextToSpeech` + cached WAV via `SoundPool`/`AudioTrack`/Media3 as needed.
- Persistence: DataStore (progress, voice, speed). No Room initially.
- Content: JSON in `assets/stories/`, loaded with kotlinx.serialization.
- Fully offline. No analytics or ad SDKs. Target Google Play "Designed for Families"; PIPEDA-friendly (no data collection).
- minSdk ~26, targetSdk current.

## 5. Content model
```json
{
  "story": "The Red Hen",
  "pages": [
    {
      "prompt": "Ask: what sound does h make?",
      "words": [
        {"text": "A", "sight": true},
        {"text": "hen", "img": "hen", "segments": ["h","e","n"]},
        {"text": "sat", "segments": ["s","a","t"]}
      ]
    }
  ]
}
```
- `img`: resource key (emoji or vector drawable); omitted for sight words.
- `segments`: hand-authored graphemes (no automatic G2P; unreliable for early English).
- `prompt`: optional co-play line shown only in a parent overlay.

## 6. Content plan
- Phonics sequence: CVC words -> digraphs (sh, ch, th) -> some 4-letter words (ship, frog, jump).
- Starter word bank: cat, hat, mat, sat, hen, red, bed, pen, pig, dig, big, sit, dog, log, fox, box, sun, bus, cup, bug.
- Sight words introduced gradually: a, the, is, on, to, I, yes, not.
- Author ~5 stories (~35 pages, ~40 unique words).

Sample story — The Red Hen:
1. A hen sat.
2. The hen is red.
3. A fox ran.
4. The fox is big.
5. The hen got in a box.
6. The fox did not get the hen.
7. Yes! The hen won.

## 7. Pictures
- Easiest: Noto Color Emoji rendered by the system (no assets; look varies by device; some words lack an emoji).
- More control: OpenMoji (license believed CC BY-SA 4.0; verify and add attribution) as vector drawables.
- Custom: own flat illustrations for consistent style (more time).
- Decide before building the content pipeline.

## 8. Visual style
- Violet primary (around #5031F5), soft off-white background, one warm accent for the active word and filled segments.
- Font: rounded sans with single-story a/g: **Andika** (SIL OFL) or Lexend. Lowercase, 72–96sp for active word.
- Scrub bar: thick rounded pill, segment dividers, large thumb, filled-in accent color for traversed parts.
- Layout: sentence across top; picture beside/above active word; help button as big speaker icon in a corner.
- Rewards: brief sparkle and page-complete star only.
- Original characters/illustrations only.

## 9. Parent area (gated)
- Voice picker and speech-rate slider.
- Progress overview (stories completed, words attempted).
- Reset progress.
- Single child profile at first.

## 10. Risks / pitfalls
- Palm resting on screen; multi-touch.
- Fast drags making per-segment audio choppy: cap retrigger rate, fade.
- TTS first-call latency (cache audio).
- TTS quality/pronunciation variance across devices and voices.
- Segment-boundary accuracy for tape-style scrub.
- Emoji rendering inconsistency across manufacturers.

## 11. Milestones
1. Compose prototype: one hard-coded word, drag highlights segments, no audio.
2. TTS word audio (help button, completion) with caching.
3. Story/page player and JSON loader; pictures.
4. Author ~5 stories.
5. Theme/polish, parent gate, DataStore progress.
6. Optional: tape-style scrub (option 3), more stories, additional profiles.
7. Test with the child; tune drag sensitivity and pacing.

## 12. Open questions
- Picture source (system emoji vs OpenMoji vs custom)?
- Keep scrub visual-only for v1, or attempt tape-style audio scrub?
- App name and package ID?
- Target device (phone vs tablet) and minimum Android version?

## 13. First prompt for Claude Code
"Read reading-app-plan.md. Scaffold a Kotlin + Jetpack Compose Android project implementing Milestone 1: a single screen showing the word 'cat' with a segmented scrub bar (c-a-t). Dragging a finger across the bar fills segments and highlights them; at the end show a brief reward. Use the theme in section 8 and keep the content model in section 5. No audio yet."
