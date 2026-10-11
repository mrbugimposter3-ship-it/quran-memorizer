# QuranMemorizer (MemoriBlitz)

A fast, loud Qur'an memorisation trainer for Windows. The **full Qur'an** —
all 6236 ayahs across 114 surahs, 30 juz and 604 pages — is bundled in the
app, so you pick exactly what to memorise and drill it at speed. Every ayah
carries its **Arabic (Uthmani)**, transliteration and English meaning.

Written in plain Java + Swing with **no external libraries**. Everything is
hand-drawn on `Graphics2D`, every sound effect is synthesised at runtime, and
the whole thing is one folder of `.java` files plus one data file.

The theme is a calm **green + coffee** palette: espresso-to-forest backgrounds,
leaf-green success states, and warm cream / caramel / terracotta accents.

---

## Running it

You need a JDK/JRE 8 or newer ([Adoptium](https://adoptium.net) is a good
source). Then, from this folder:

```
build.bat     compiles src\*.java into out\ and packs MemoriBlitz.jar
run.bat       rebuilds, then launches the game
```

So the normal workflow is just: **double-click `run.bat`**.

To launch without a console window, or to hand the game to someone else, use
the jar that `build.bat` produces:

```
java -jar MemoriBlitz.jar
```

Opening the folder in **Eclipse** works too: `File > Import > Existing Projects
into Workspace`, point it at this folder, and run `MemoriBlitz.java` as the
entry point. There is no package declaration and no dependencies, so it imports
cleanly anywhere.

---

## Choosing what to memorise

Press **SELECT SURAH / JUZ / PAGE** (or `S`) from the menu. Three tabs:

| Tab | What you pick | Range |
|-----|---------------|-------|
| **SURAH** | any of the 114 surahs | whole surah |
| **JUZ** | any of the 30 parts | whole juz |
| **PAGE** | any of the 604 mushaf pages | whole page |

There is also a **FULL QUR'AN** button for the truly ambitious. Scroll with the
mouse wheel or the arrow keys, click to select. Your choice becomes the active
study set and is remembered.

**READ** (`R`) browses the current set ayah by ayah — reference, juz, page,
transliteration and English meaning — so you can study before you drill.

---

## The four game modes

| Key | Mode | What it drills |
|-----|------|----------------|
| 1 | **TEST** | Ayah to meaning (and back), against a draining bar. Speed feeds the score, correct answers build a combo multiplier, three mistakes ends the run. |
| 2 | **MATCH** | A board of transliterations on the left and meanings on the right, shuffled. Tap a left tile then its matching right tile. Clear the board before the round timer runs out; each board adds a pair. |
| 3 | **FLASH** | Self-graded flashcards drawn from an adaptive deck, so ayahs you keep missing come back far more often. |
| 4 | **FILL** | An ayah's transliteration appears with one word blanked out — supply the missing word. |

---

## Controls

| Where | Action |
|-------|--------|
| Menu | `1`-`4` start a mode, `S` select material, `R` read, `M` toggles sound |
| TEST / FILL | `1`-`4` (or numpad) pick an option, or click it. Tap again during the reveal to skip on. |
| MATCH | Click a left tile, then its matching right tile |
| FLASH | `SPACE` reveals, `->` / `2` marks GOT IT, `<-` / `1` marks MISS |
| Anywhere | `ESC` returns to the menu |

Everything is also clickable, so it works fine on a touchscreen.

---

## How it keeps you honest

* **Combo multiplier** — consecutive correct answers climb to x8; one miss
  drops you straight back to x1.
* **Speed scoring** — TEST and FILL scale the award by how much of the timer
  was left.
* **Adaptive weighting** — every ayah carries a hit/miss tally. FLASH draws
  from a deck weighted toward your weak ayahs, so they resurface until they
  stop being weak.
* **A revealed answer instead of a loss** — wrong answers show you the right
  one, so the game stays fast without ever becoming a guessing game.

---

## Where progress is stored

All of it persists to one plain text file:

```
%USERPROFILE%\.quranmemorizer\progress.txt
```

It records your per-ayah hit/miss tallies and best scores per mode, so your
weak spots follow you between sessions.

---

## Project layout

```
src/
  MemoriBlitz.java    main frame, screen routing, entry point
  QuranData.java      the full Qur'an model + quran.data loader
  WordBank.java       study selection, per-ayah progress, best scores, file I/O
  Theme.java          green + coffee palette, fonts, painting helpers
  Btn.java            hit-tested animated button
  UI.java             button rendering, chips, panels
  Screen.java         base page: 60 Hz clock, particles, input plumbing
  GameScreen.java     shared game chrome: score, combo, level, count-down,
                      round-over card
  MenuScreen.java     the animated menu
  QuranSelect.java    surah / juz / page selection
  Reader.java         browse the current study set
  TestGame.java       mode 1
  MatchGame.java      mode 2
  FlashGame.java      mode 3
  FillGame.java       mode 4
  Sound.java          runtime PCM synthesiser (no audio files)
  Fx.java             particle system: sparks, confetti, rings, score pops
res/
  quran.data          the full Qur'an (transliteration + Sahih International
                      English, with surah / juz / page metadata)
```

### The data file

`res/quran.data` is a plain pipe-delimited text file, one record per line:

```
S|number|arabic|english|transliteration|ayah_count|type
J|juz|sura|ayah
P|page|sura|ayah
A|sura|ayah_in_surah|juz|page|arabic|transliteration|english
```

It is generated from Tanzil.net data (transliteration + Sahih International
English translation) with surah/juz/page metadata from the standard Madani
mushaf. 6236 `A` records, one per ayah.
