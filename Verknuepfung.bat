@echo off
rem Lighthouse: Verknuepfung auf dem Desktop anlegen (ruft Verknuepfung.ps1)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Verknuepfung.ps1" %*
pause
