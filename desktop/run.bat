@echo off
call build.bat
if errorlevel 1 exit /b 1
java -jar MemoriBlitz.jar
