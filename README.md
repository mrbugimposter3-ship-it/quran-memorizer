# MemoriBlitz — Qur'an Memorisation Trainer

A fast, loud Qur'an memorisation trainer with the full Qur'an (6236 ayahs, 114 surahs, 30 juz, 604 pages) bundled in three synced codebases:

| Folder      | Platform                       | Build                                   |
|-------------|--------------------------------|-----------------------------------------|
| `desktop/`  | Java Swing (Windows/Linux/mac) | `cd desktop && build.bat` → `MemoriBlitz.jar` |
| `android/`  | Android (Java, Gradle)         | `cd android && gradlew assembleDebug`   |
| `web/`      | Single-file HTML app           | open `web/index.html` (no build)        |

## Features
- **Arabic, transliteration, English and Urdu** for every ayah (RTL rendered).
- **36 game modes** — TEST, MATCH, FLASH, FILL, RECITE, ORDER, NEXT, ARABIC, REVERSE, PAIR, INITIALS, SPEED, GRID, FILL-AR, JUMBLE, VS AI, VERSE BUILD, URDU, REVEAL, WORD WALK, MIX, ODD ONE OUT, WORD COUNT, FIRST/LAST, NEXT WORD, PARTIAL, EN→URDU, URDU→EN, URDU→AR, AR→TRANSLIT, SURAH TRIVIA, WHICH SURAH, WHICH JUZ, WHICH PAGE, AYAH NO., **NUMBER STORY**.
- **NUMBER STORY** turns each ayah into a mnemonic sentence: surah/ayah/juz/hizb/page/position-on-page are encoded as famous-book names (Lord of the Rings, Sherlock, Gatsby, Mockingbird, Lord of the Flies, Geronimo Stilton…) for photographic recall.
- Qaseeda Burdah Shareef and Dalail al-Khayrat (separate from the Qur'an).
- 1/2/3-column READ view, autoscroll, word-by-word WORD WALK, reminder popup, passcode lock, adjustable fonts.
- Adaptive scoring toward the ayahs you keep missing.

## Data
`quran.data` is pipe-delimited (`S|…`, `J|…`, `P|…`, `A|…`). One copy lives in each platform's resource folder (`desktop/res/`, `android/app/src/main/assets/`). `burdah.data` and `dalail.data` hold the supplementary texts.
