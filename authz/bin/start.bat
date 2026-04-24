@echo off
setlocal enabledelayedexpansion

rem Resolve directories
set "BIN_DIR=%~dp0"
for %%I in ("%BIN_DIR%..") do set "BASE_DIR=%%~fI"
set "APP_DIR=%BASE_DIR%\app"
set "CFG_DIR=%BASE_DIR%\config"
set "LOG_DIR=%BASE_DIR%\logs"

set "JAR_NAME=authz-0.0.1-SNAPSHOT.jar"
set "JAR_PATH=%APP_DIR%\%JAR_NAME%"

set "PID_FILE=%LOG_DIR%\app.pid"
set "OUT_LOG=%LOG_DIR%\app.out.log"
set "ERR_LOG=%LOG_DIR%\app.err.log"

if not exist "%APP_DIR%" mkdir "%APP_DIR%"
if not exist "%CFG_DIR%" mkdir "%CFG_DIR%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

if not exist "%JAR_PATH%" (
  echo [ERROR] Jar not found: "%JAR_PATH%"
  echo         Please copy the jar into authz\app\%JAR_NAME%
  exit /b 1
)

if not exist "%CFG_DIR%\application-prod.yml" (
  echo [ERROR] Config not found: "%CFG_DIR%\application-prod.yml"
  echo         Please create it under authz\config\
  exit /b 1
)

rem If pid exists and process is alive, refuse to start twice
if exist "%PID_FILE%" (
  set /p PID=<"%PID_FILE%"
  if not "!PID!"=="" (
    powershell -NoProfile -Command ^
      "$p=Get-Process -Id !PID! -ErrorAction SilentlyContinue; if($p){ exit 0 } else { exit 1 }"
    if "!errorlevel!"=="0" (
      echo [INFO] Already running. pid=!PID!
      exit /b 0
    )
  )
)

rem Optional overrides
if "%JAVA_HOME%"=="" (
  set "JAVA_EXE=java"
) else (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

rem You can override these by setting environment variables before running start.bat
if "%JAVA_OPTS%"=="" set "JAVA_OPTS=-Xms256m -Xmx512m"

rem Start in background via PowerShell, capture PID, redirect logs
pushd "%BASE_DIR%"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$base='%BASE_DIR%';" ^
  "$jar='%JAR_PATH%';" ^
  "$cfgDir='%CFG_DIR%';" ^
  "$pidFile='%PID_FILE%';" ^
  "$outLog='%OUT_LOG%';" ^
  "$errLog='%ERR_LOG%';" ^
  "$javaExe='%JAVA_EXE%';" ^
  "$javaOpts='%JAVA_OPTS%';" ^
  "$args=@();" ^
  "if ($javaOpts) { $args += ($javaOpts -split ' ' | ? { $_ -ne '' }) }" ^
  "$args += @('-jar', $jar, '--spring.profiles.active=prod', ('--spring.config.additional-location=' + $cfgDir + '\')); " ^
  "New-Item -ItemType Directory -Force -Path (Split-Path -Parent $pidFile) | Out-Null;" ^
  "$p = Start-Process -FilePath $javaExe -ArgumentList $args -WorkingDirectory $base -WindowStyle Hidden -RedirectStandardOutput $outLog -RedirectStandardError $errLog -PassThru;" ^
  "Set-Content -Path $pidFile -Value $p.Id -Encoding ASCII;" ^
  "Write-Host ('[OK] Started. pid=' + $p.Id);"

popd

endlocal
exit /b 0

