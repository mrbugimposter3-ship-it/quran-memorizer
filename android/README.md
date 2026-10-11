# QuranMemorizer (Android)

The Android companion to the desktop MemoriBlitz app. The **full Qur'an** —
all 6236 ayahs across 114 surahs, 30 juz and 604 pages — is bundled in
`assets/quran.data`, so you pick what to memorise and drill it on the go.
Every ayah carries its **Arabic (Uthmani)**, transliteration and English.

Same **green + coffee** theme as the desktop app, same four game modes.

---

## Opening it in Android Studio

1. Open **Android Studio**.
2. `File > Open`, select the `QuranMemorizerAndroid` folder.
3. Let Gradle sync (Android Studio will use its bundled Gradle / JDK).
4. Run on an emulator or a connected device.

Requirements: **Android Studio** (recent), compile SDK 34, min SDK 21
(Android 5.0+).

---

## What it does

* **SELECT SURAH / JUZ / PAGE** — pick any surah, any of the 30 juz, or any of
  the 604 mushaf pages, or the whole Qur'an. Your choice is remembered.
* **READ** — browse the current set ayah by ayah (reference, juz, page,
  transliteration, English meaning).
* **TEST** — match the ayah to its meaning (and back), against a combo
  multiplier and three lives.
* **MATCH** — pair transliterations with their meanings, board by board.
* **FLASH** — self-graded cards drawn from an adaptive deck weighted toward
  your weak ayahs.
* **FILL** — supply the missing word of the ayah.

Progress (per-ayah hit/miss tallies and best scores) persists locally via
`SharedPreferences`.

---

## Project layout

```
app/src/main/
  assets/quran.data                     the full Qur'an (transliteration +
                                        Sahih International English + metadata)
  java/com/memoriblitz/quran/
    QuranData.java      data model + asset loader
    ProgressStore.java  selection + per-ayah progress (SharedPreferences)
    Ui.java             green + coffee styling helpers
    MainActivity.java   menu
    SelectActivity.java surah / juz / page selection
    ReaderActivity.java browse the study set
    TestActivity.java   multiple choice
    MatchActivity.java  matching pairs
    FlashActivity.java  adaptive flashcards
    FillActivity.java   fill-in-the-blank
  res/
    values/colors.xml   green + coffee palette
    values/themes.xml   app theme
    drawable/ic_launcher.xml
```

The data file is the exact same `quran.data` used by the desktop app, so the
two stay in sync.
