@echo off
setlocal EnableDelayedExpansion
echo Building QuranMemorizer...

rem Locate a JDK bin directory (javac + jar live together).
set "JAVAC="
for %%D in (
    "C:\Program Files\Java\jdk-27\bin\javac.exe"
    "C:\Program Files\Java\jdk-21\bin\javac.exe"
    "C:\Program Files\Java\jdk-17\bin\javac.exe"
    "C:\Program Files\Java\jdk-11\bin\javac.exe"
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot\bin\javac.exe"
    "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
) do (
    if exist %%D if not defined JAVAC set "JAVAC=%%~D"
)
if not defined JAVAC (
    where javac >nul 2>nul
    if not errorlevel 1 set "JAVAC=javac"
)
if not defined JAVAC (
    echo ERROR: javac not found. Install a JDK or add it to PATH.
    exit /b 1
)

for %%F in ("%JAVAC%") do set "BIN=%%~dpF"

rem Locate jar: same bin dir first, then PATH, then known JDKs.
set "JAR="
if not "%BIN%"=="" if exist "%BIN%jar.exe" set "JAR=%BIN%jar.exe"
if not defined JAR (
    where jar >nul 2>nul
    if not errorlevel 1 set "JAR=jar"
)
if not defined JAR (
    for %%D in (
        "C:\Program Files\Java\jdk-27\bin\jar.exe"
        "C:\Program Files\Java\jdk-21\bin\jar.exe"
        "C:\Program Files\Java\jdk-17\bin\jar.exe"
        "C:\Program Files\Java\jdk-11\bin\jar.exe"
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot\bin\jar.exe"
        "C:\Program Files\Android\Android Studio\jbr\bin\jar.exe"
    ) do (
        if exist %%D if not defined JAR set "JAR=%%~D"
    )
)
if not defined JAR (
    echo ERROR: jar not found.
    exit /b 1
)

if not exist out mkdir out
"%JAVAC%" -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED - see errors above.
    exit /b 1
)
copy /Y res\quran.data out\quran.data >nul
"%JAR%" cfe MemoriBlitz.jar MemoriBlitz -C out .
if errorlevel 1 (
    echo JAR FAILED.
    exit /b 1
)
echo Done - MemoriBlitz.jar
endlocal
