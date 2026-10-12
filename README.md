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

## Build & Deploy

### Desktop (JAR)
```bash
cd desktop
build.bat        # Windows → MemoriBlitz.jar
run.bat          # launch it
```

### Android (APK) — locally
Requires JDK 17. The Android SDK path is auto-read from `local.properties` (git-ignored):
```bash
cd android
./gradlew assembleDebug                      # installable debug APK
./gradlew assembleRelease                    # unsigned release APK
# outputs:
#   app/build/outputs/apk/debug/app-debug.apk
#   app/build/outputs/apk/release/app-release-unsigned.apk
```

Sign the unsigned release APK for distribution:
```bash
keytool -genkey -v -keystore release.keystore -alias quran -keyalg RSA -keysize 2048 -validity 10000
zipalign -v 4 app-release-unsigned.apk app-release-aligned.apk
apksigner sign --ks release.keystore --ks-key-alias quran --out QuranMemorizer.apk app-release-aligned.apk
```

### Android (APK) — via GitHub Actions
Push a version tag, or run the workflow manually (Actions → **Release (build APK)** → Run workflow):
```bash
git tag v1.0.0 && git push origin v1.0.0
```
The workflow builds **debug + release** APKs, uploads them as artifacts, and (on tags) attaches both to a GitHub Release.

### Web → GitHub Pages
1. Repo → **Settings → Pages → Source → "GitHub Actions"** (one-time).
2. Every push to `main` builds and deploys `web/index.html` to:
   `https://mrbugimposter3-ship-it.github.io/quran-memorizer/`

## Reading view (1 / 2 / 3 columns)
The Qur'an reader in all three apps lets you switch between **1, 2, or 3 columns** of Arabic / English / Urdu side-by-side (instead of stacked top-to-bottom), with adjustable font size and autoscroll speed (from ±15 min per juz).
