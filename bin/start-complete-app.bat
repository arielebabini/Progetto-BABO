@echo off
rem start-complete-app.bat - Avvio automatico completo di BookRecommender per Windows
rem Avvia database, server e client in sequenza

setlocal EnableDelayedExpansion

echo AVVIO AUTOMATICO BOOKRECOMMENDER
echo ======================================

rem --- CONFIGURAZIONE ---
set DB_NAME=DataProva
set DB_USER=postgres
set DB_PASS=postgress
set SERVER_PORT=8080
set JAVAFX_HOME="C:\Program Files\Java\javafx-sdk-24.0.2"

echo.
echo 1. VERIFICA PREREQUISITI
echo ========================
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Java non trovato! Installa Java 17+ da https://adoptium.net/
    pause
    exit /b 1
)
echo Java disponibile

call mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Maven non trovato o non funzionante!
    pause
    exit /b 1
)
echo Maven disponibile

rem MODIFICATO: Cerca pom.xml nella directory padre (la root del progetto)
if not exist "..\pom.xml" (
    rem MODIFICATO: Messaggio aggiornato per la nuova posizione
    echo Esegui questo script dalla directory 'bin' del progetto BookRecommender
    pause
    exit /b 1
)
echo Struttura progetto OK

echo.
echo 2. VERIFICA DATABASE POSTGRESQL
echo ===============================
psql --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo PostgreSQL trovato
    set PGPASSWORD=%DB_PASS%
    psql -h localhost -U %DB_USER% -d %DB_NAME% -c "SELECT 1;" >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        echo Database %DB_NAME% raggiungibile
    ) else (
        echo ATTENZIONE: Database non raggiungibile, l'app usera' dati di fallback
    )
) else (
    echo ATTENZIONE: PostgreSQL non trovato, l'app usera' dati di fallback
)

echo.
echo 3. AVVIO SERVER SPRING BOOT
echo ============================
netstat -an | findstr ":%SERVER_PORT% " >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Server gia' in esecuzione sulla porta %SERVER_PORT%
) else (
    echo Avvio server Spring Boot...
    echo    Porta: %SERVER_PORT%
    echo    Logs: server.log (nella root del progetto)
    rem MODIFICATO: Esegue il comando mvn dalla directory padre (..)
    rem             usando pushd per cambiare temporaneamente cartella.
    start "Spring Boot Server" /MIN cmd /c "pushd .. && (call mvn spring-boot:run -pl server > server.log 2>&1) && popd"
    echo    Attesa avvio server...
    timeout /t 10 /nobreak >nul
    set attempts=0
   
 :wait_server
    set /a attempts+=1
    if "%attempts%" gtr "30" (
        echo ERRORE: Server non si e' avviato entro 30 secondi
        echo    Controlla server.log per dettagli
        pause
        exit /b 1
    )
    powershell -Command "try { Invoke-RestMethod -Uri 'http://localhost:%SERVER_PORT%/api/books/health' -TimeoutSec 2 } catch { exit 1 }" >nul 2>&1
    if %ERRORLEVEL% neq 0 (
        echo    Tentativo %attempts%/30...
        timeout /t 1 /nobreak >nul
        goto wait_server
    )
    echo Server avviato con successo!
    echo    API: http://localhost:%SERVER_PORT%/api/books
)

echo.
echo 4. VERIFICA DISTRIBUZIONE CLIENT
echo =================================
rem NOTA: Questo percorso è corretto perché 'dist' si trova ora
rem       nella stessa cartella ('bin') di questo script.
if not exist "dist\BookReccommender.jar" (
    echo ERRORE: File del client non trovato!
    rem MODIFICATO: Istruzioni aggiornate per riflettere la nuova struttura
    echo Assicurati che esista il file: bin\dist\BookReccommender.jar
    echo Esegui 'mvn clean install' nella root e copia il JAR da 'client\target' a 'bin\dist'.
    pause
    exit /b 1
)
echo Client distribuito e pronto.

echo.
echo 5. AVVIO CLIENT
echo ================
echo Avvio Book Recommender Client...
echo.
rem NOTA: Questo percorso è corretto perché 'dist' si trova ora
rem       nella stessa cartella ('bin') di questo script.
java --module-path %JAVAFX_HOME%\lib --add-modules javafx.controls,javafx.fxml ^
     --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
     --add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED ^
     -Djava.awt.headless=false ^
     -Dprism.forceGPU=false ^
     -jar dist\BookReccommender.jar

echo.
echo Client terminato
echo.
set /p stop_server="Vuoi fermare anche il server? (s/N): "
if /i "%stop_server%"=="s" (
    echo Arresto server...
    taskkill /f /im java.exe /fi "WINDOWTITLE eq Spring Boot Server*" >nul 2>&1
    echo Server fermato
)

echo.
echo Sessione completata!
pause