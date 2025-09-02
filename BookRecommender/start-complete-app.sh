#!/bin/bash
# start-complete-app.sh - Avvio automatico completo di Books Client
# Avvia database, server e client in sequenza

set -e

echo "🚀 AVVIO AUTOMATICO BOOKS CLIENT"
echo "======================================"

# Variabili configurabili
DB_NAME="DataProva"
DB_USER="postgres"
DB_PASS="postgress"
SERVER_PORT=8080
MAX_WAIT_TIME=30

# Funzione per controllare se una porta è in uso
check_port() {
    lsof -i :$1 >/dev/null 2>&1
}

# Funzione per aspettare che un servizio sia pronto
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1

    echo "⏳ Attesa $service_name..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            echo "✅ $service_name pronto!"
            return 0
        fi
        echo "   Tentativo $attempt/$max_attempts..."
        sleep 1
        attempt=$((attempt + 1))
    done

    echo "❌ $service_name non si è avviato entro $max_attempts secondi"
    return 1
}

# Funzione per terminare tutti i processi al CTRL+C
cleanup() {
    echo ""
    echo "🛑 Arresto servizi..."

    if [ ! -z "$SERVER_PID" ]; then
        echo "   Terminando server (PID: $SERVER_PID)..."
        kill $SERVER_PID 2>/dev/null || true
    fi

    echo "   Servizi terminati"
    exit 0
}

# Imposta la gestione del segnale di interruzione
trap cleanup INT TERM

echo ""
echo "1️⃣ VERIFICA PREREQUISITI"
echo "========================"

# Verifica Java
if ! command -v java >/dev/null 2>&1; then
    echo "❌ Java non trovato! Installa Java 17+ da https://adoptium.net/"
    exit 1
fi

echo "✅ Java: $(java -version 2>&1 | head -n1)"

# Verifica Maven
if ! command -v mvn >/dev/null 2>&1; then
    echo "❌ Maven non trovato!"
    exit 1
fi

echo "✅ Maven disponibile"

# Verifica struttura progetto
if [ ! -f "pom.xml" ] || [ ! -d "server" ] || [ ! -d "client" ]; then
    echo "❌ Esegui questo script dalla directory root del progetto BookRecommender"
    exit 1
fi

echo "✅ Struttura progetto OK"

echo ""
echo "2️⃣ AVVIO DATABASE POSTGRESQL"
echo "============================="

# Verifica se PostgreSQL è installato
if command -v psql >/dev/null 2>&1; then
    echo "✅ PostgreSQL trovato"

    # Controlla se è già in esecuzione
    if pg_isready -q 2>/dev/null; then
        echo "✅ PostgreSQL già in esecuzione"
    else
        echo "🔄 Avvio PostgreSQL..."
        if command -v brew >/dev/null 2>&1; then
            brew services start postgresql
            sleep 3
        else
            echo "⚠️  Avvia manualmente PostgreSQL"
        fi
    fi

    # Verifica connessione database
    if PGPASSWORD=$DB_PASS psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT 1;" >/dev/null 2>&1; then
        echo "✅ Database $DB_NAME raggiungibile"
    else
        echo "❌ Database non raggiungibile. Verifica configurazione:"
        echo "   Database: $DB_NAME"
        echo "   User: $DB_USER"
        echo "   Password: $DB_PASS"
        exit 1
    fi
else
    echo "⚠️  PostgreSQL non trovato, l'app userà dati di fallback"
fi

echo ""
echo "3️⃣ AVVIO SERVER SPRING BOOT"
echo "============================"

# Controlla se il server è già in esecuzione
if check_port $SERVER_PORT; then
    echo "✅ Server già in esecuzione sulla porta $SERVER_PORT"
else
    echo "🔄 Avvio server Spring Boot..."
    echo "   Porta: $SERVER_PORT"
    echo "   Logs salvati in: server.log"

    SERVER_JAR=$(find server/target -name "server-*.jar" | head -n 1)
    if [ -z "$SERVER_JAR" ]; then
        echo "❌ JAR del server non trovato! Esegui prima ./build-executables.sh"
        exit 1
    fi

    echo "   Avvio del file JAR: $SERVER_JAR"
    # Avvia il server JAR in background
    nohup java -jar $SERVER_JAR > server.log 2>&1 &
    SERVER_PID=$!

    echo "   Server PID: $SERVER_PID"

    # Aspetta che il server sia pronto
    if wait_for_service "http://localhost:$SERVER_PORT/api/books/health" "Server Spring Boot"; then
        echo "✅ Server avviato con successo!"
        echo "   API: http://localhost:$SERVER_PORT/api/books"
    else
        echo "❌ Errore avvio server. Controlla server.log per dettagli:"
        tail -20 server.log
        exit 1
    fi
fi

echo ""
echo "4️⃣ VERIFICA DISTRIBUZIONE CLIENT"
echo "================================="

# Verifica che la distribuzione sia pronta
if [ ! -f "dist/BooksClient.jar" ]; then
    echo "🔄 Distribuzione client non trovata, generazione..."
    ./build-executables.sh
fi

# Verifica dimensioni JAR (deve contenere dipendenze)
JAR_SIZE=$(du -m "dist/BooksClient.jar" | cut -f1)
if [ "$JAR_SIZE" -lt 20 ]; then
    echo "⚠️  JAR sembra troppo piccolo (${JAR_SIZE}MB), rigenerazione..."
    ./build-executables.sh
fi

echo "✅ Client distribuito: dist/BooksClient.jar (${JAR_SIZE}MB)"

echo ""
echo "5️⃣ AVVIO CLIENT"
echo "================"

echo "🚀 Avvio Books Client..."
echo ""
echo "📚 ======================================"
echo "   BOOKS CLIENT - Starting..."
echo "======================================"
echo ""

# Determina il comando di avvio appropriato
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    echo "🍎 Sistema: macOS"
    java -Djava.awt.headless=false \
         -Djavafx.platform=desktop \
         -jar dist/BooksClient.jar &
    CLIENT_PID=$!
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    echo "🐧 Sistema: Linux"
    java --add-modules javafx.controls,javafx.fxml \
         --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
         --add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED \
         -jar dist/BooksClient.jar &
    CLIENT_PID=$!
else
    # Altri sistemi
    echo "🖥️  Sistema: Altri"
    java -jar dist/AppleBooksClient.jar &
    CLIENT_PID=$!
fi

echo "   Client PID: $CLIENT_PID"
echo ""
echo "✅ ==============================================="
echo "   BOOKS CLIENT AVVIATO CON SUCCESSO!"
echo "==============================================="
echo ""
echo "📊 SERVIZI ATTIVI:"
echo "   🗄️  Database: PostgreSQL"
echo "   🖥️  Server: http://localhost:$SERVER_PORT"
echo "   📱 Client: Applicazione JavaFX"
echo ""
echo "🔧 COMANDI UTILI:"
echo "   Server logs: tail -f server.log"
echo "   Stop server: kill $SERVER_PID"
echo "   API test: curl http://localhost:$SERVER_PORT/api/books/health"
echo ""
echo "⏹️  Premi CTRL+C per fermare tutti i servizi"
echo ""

# Aspetta che il client termini o che l'utente prema CTRL+C
wait $CLIENT_PID 2>/dev/null || true

echo ""
echo "👋 Client terminato"
cleanup