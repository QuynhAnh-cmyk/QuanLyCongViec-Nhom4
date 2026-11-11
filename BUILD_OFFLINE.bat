@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0BUILD_OFFLINE_KHONG_GRADLE.ps1"
pause
