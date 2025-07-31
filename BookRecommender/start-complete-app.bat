@echo off
rem start-complete-app.bat - Avvio automatico completo di Apple Books Client per Windows
rem Avvia database, server e client in sequenza

setlocal EnableDelayedExpansion

echo 🚀 AVVIO AUTOMATICO APPLE BOOKS CLIENT
echo ======================================

rem Variabili configurabili
set DB_NAME=DataProva
set DB_USER=postgres
set DB_PASS=postgress
set SERVER_PORT=8080

echo.
echo 1️⃣ VERIFICA PREREQUISITI
echo ========================

rem Verifica Java
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Java non trovato! Installa Java 17+ da https://adoptium.net/
    pause
    exit /b 1
)

echo ✅ Java disponibile

rem Verifica Maven
mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Maven non trovato!
    pause
    exit /b 1
)

echo ✅ Maven disponibile

rem Verifica struttura progetto
if not exist "pom.xml" (
    echo ❌ Esegui questo script dalla directory root del progetto BookRecommender
    pause
    exit /b 1
)

if not exist "server" (
    echo ❌ Cartella server non trovata
    pause
    exit /b 1
)

if not exist "client" (
    echo ❌ Cartella client non trovata
    pause
    exit /b 1
)

echo ✅ Struttura progetto OK

echo.
echo 2️⃣ VERIFICA DATABASE POSTGRESQL
echo ===============================

rem Verifica PostgreSQL (opzionale su Windows)
psql --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo ✅ PostgreSQL trovato

    rem Test connessione database
    set PGPASSWORD=%DB_PASS%
    psql -h localhost -U %DB_USER% -d %DB_NAME% -c "SELECT 1;" >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        echo ✅ Database %DB_NAME% raggiungibile
    ) else (
        echo ⚠️ Database non raggiungibile, l'app userà dati di fallback
    )
) else (
    echo ⚠️ PostgreSQL non trovato, l'app userà dati di fallback
)

echo.
echo 3️⃣ AVVIO SERVER SPRING BOOT
echo ============================

rem Controlla se la porta è già in uso
netstat -an | findstr ":%SERVER_PORT% " >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo ✅ Server già in esecuzione sulla porta %SERVER_PORT%
) else (
    echo 🔄 Avvio server Spring Boot...
    echo    Porta: %SERVER_PORT%
    echo    Logs: server.log

    rem Avvia il server in una nuova finestra
    start "Spring Boot Server" /MIN cmd /c "mvn spring-boot:run -pl server > server.log 2>&1"

    echo    Attesa avvio server...
    timeout /t 10 /nobreak >nul

    rem Verifica che il server sia attivo
    set attempts=0
    :wait_server
    set /a attempts+=1
    if %attempts% gtr 30 (
        echo ❌ Server non si è avviato entro 30 secondi
        echo    Controlla server.log per dettagli
        pause
        exit /b 1
    )

    rem Test server con PowerShell (più affidabile di curl su Windows)
    powershell -Command "try { Invoke-RestMethod -Uri 'http://localhost:%SERVER_PORT%/api/books/health' -TimeoutSec 2 } catch { exit 1 }" >nul 2>&1
    if %ERRORLEVEL% neq 0 (
        echo    Tentativo %attempts%/30...
        timeout /t 1 /nobreak >nul
        goto wait_server
    )

    echo ✅ Server avviato con successo!
    echo    API: http://localhost:%SERVER_PORT%/api/books
)

echo.
echo 4️⃣ VERIFICA DISTRIBUZIONE CLIENT
echo =================================

rem Verifica che la distribuzione sia pronta
if not exist "dist\AppleBooksClient.jar" (
    echo 🔄 Distribuzione client non trovata, generazione...
    call build-executables.bat
)

rem Verifica dimensioni JAR (approssimativa su Windows)
for %%I in (dist\AppleBooksClient.jar) do set JAR_SIZE=%%~zI
set /a JAR_SIZE_MB=%JAR_SIZE%/1024/1024

if %JAR_SIZE_MB% lss 20 (
    echo ⚠️ JAR sembra troppo piccolo ^(%JAR_SIZE_MB%MB^), rigenerazione...
    call build-executables.bat
)

echo ✅ Client distribuito: dist\AppleBooksClient.jar ^(~%JAR_SIZE_MB%MB^)

echo.
echo 5️⃣ AVVIO CLIENT
echo ================

echo 🚀 Avvio Apple Books Client...
echo.
echo 📚 ======================================
echo    APPLE BOOKS CLIENT - Starting...
echo ======================================
echo.

rem Avvia il client
echo 🪟 Sistema: Windows
java --add-modules javafx.controls,javafx.fxml ^
     --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
     --add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED ^
     -Djava.awt.headless=false ^
     -Dprism.forceGPU=false ^
     -jar dist\AppleBooksClient.jar

echo.
echo 👋 Client terminato

rem Chiedi se fermare anche il server
echo.
set /p stop_server="Vuoi fermare anche il server? (s/N): "
if /i "%stop_server%"=="s" (
    echo 🛑 Arresto server...
    taskkill /f /im java.exe /fi "WINDOWTITLE eq Spring Boot Server*" >nul 2>&1
    echo ✅ Server fermato
)

echo.
echo 🎉 Sessione completata!
pause