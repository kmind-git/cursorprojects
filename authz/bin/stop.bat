@echo off
setlocal enabledelayedexpansion

set "BIN_DIR=%~dp0"
for %%I in ("%BIN_DIR%..") do set "BASE_DIR=%%~fI"
set "LOG_DIR=%BASE_DIR%\logs"
set "PID_FILE=%LOG_DIR%\app.pid"

if not exist "%PID_FILE%" (
  echo [INFO] Not running (pid file not found): "%PID_FILE%"
  exit /b 0
)

set /p PID=<"%PID_FILE%"
if "%PID%"=="" (
  echo [WARN] pid file is empty. Removing it.
  del /f /q "%PID_FILE%" >nul 2>nul
  exit /b 0
)

echo [INFO] Stopping pid=%PID% ...
taskkill /PID %PID% /T /F >nul 2>nul

rem Verify via PowerShell (tasklist output is locale-dependent)
powershell -NoProfile -Command ^
  "$p=Get-Process -Id %PID% -ErrorAction SilentlyContinue; if($p){ exit 1 } else { exit 0 }"
if not "%errorlevel%"=="0" (
  echo [WARN] Process still exists. pid=%PID%
  exit /b 1
)

del /f /q "%PID_FILE%" >nul 2>nul
echo [OK] Stopped.
exit /b 0

