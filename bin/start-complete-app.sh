#!/bin/bash
# start-complete-app.sh - Avvio automatico completo di Books Client
# Adattato per la nuova struttura con la cartella 'src'

set -e

echo "🚀 AVVIO AUTOMATICO BOOKS CLIENT"
echo "======================================"

# --- CONFIGURAZIONE ---
DB_NAME="DataProva"
DB_USER="postgres"
DB_PASS="postgress"
SERVER_PORT=8080

# --- FUNZIONI ---
check_port() {
    lsof -i :$1 >/dev/null 2>&1
}

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

trap cleanup INT TERM

# --- ESECUZIONE ---

echo ""
echo "1️⃣ VERIFICA PREREQUISITI"
echo "========================"

if ! command -v java >/dev/null 2>&1; then
    echo "❌ Java non trovato! Installa Java 17+ da https://adoptium.net/"
    exit 1
fi
echo "✅ Java: $(java -version 2>&1 | head -n1)"

if ! command -v mvn >/dev/null 2>&1; then
    echo "❌ Maven non trovato!"
    exit 1
fi
echo "✅ Maven disponibile"

# MODIFICATO: Verifica la presenza di pom.xml in ../src
if [ ! -f "../src/pom.xml" ]; then
    echo "❌ File pom.xml non trovato in ../src/"
    echo "   Assicurati di eseguire lo script dalla cartella 'bin'."
    exit 1
fi
echo "✅ Struttura progetto OK"

echo ""
echo "2️⃣ AVVIO DATABASE POSTGRESQL"
echo "============================="

if command -v psql >/dev/null 2>&1; then
    echo "✅ PostgreSQL trovato"
    if pg_isready -q 2>/dev/null; then
        echo "✅ PostgreSQL già in esecuzione"
    else
        echo "🔄 Avvio PostgreSQL..."
        if command -v brew >/dev/null 2>&1; then
            brew services start postgresql; sleep 3
        else
            echo "⚠️  Avvia manualmente PostgreSQL"
        fi
    fi
    if PGPASSWORD=$DB_PASS psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT 1;" >/dev/null 2>&1; then
        echo "✅ Database $DB_NAME raggiungibile"
    else
        echo "❌ Database non raggiungibile. Verifica configurazione."
        exit 1
    fi
else
    echo "⚠️  PostgreSQL non trovato, l'app userà dati di fallback"
fi

echo ""
echo "3️⃣ AVVIO SERVER SPRING BOOT"
echo "============================"

if check_port $SERVER_PORT; then
    echo "✅ Server già in esecuzione sulla porta $SERVER_PORT"
else
    echo "🔄 Avvio server Spring Boot..."
    echo "   Porta: $SERVER_PORT"
    # MODIFICATO: Specifica che i log vengono salvati in 'src'
    echo "   Logs salvati in: src/server.log"

    # MODIFICATO: Cerca il JAR del server nella cartella ../src
    SERVER_JAR=$(find ../src/server/target -name "server-*.jar" | head -n 1)
    if [ -z "$SERVER_JAR" ]; then
        echo "❌ JAR del server non trovato! Esegui 'mvn clean install' nella cartella 'src'."
        exit 1
    fi

    echo "   Avvio del file JAR: $SERVER_JAR"
    # MODIFICATO: Salva il log in ../src/server.log
    nohup java -jar "$SERVER_JAR" > ../src/server.log 2>&1 &
    SERVER_PID=$!
    echo "   Server PID: $SERVER_PID"

    if wait_for_service "http://localhost:$SERVER_PORT/api/books/health" "Server Spring Boot"; then
        echo "✅ Server avviato con successo!"
    else
        echo "❌ Errore avvio server. Controlla src/server.log per dettagli:"
        # MODIFICATO: Legge il log da ../src/server.log
        tail -20 ../src/server.log
        exit 1
    fi
fi

echo ""
echo "4️⃣ VERIFICA DISTRIBUZIONE CLIENT"
echo "================================="

if [ ! -f "dist/BooksClient.jar" ]; then
    echo "❌ File JAR del client non trovato in 'bin/dist/'!"
    echo "   Esegui 'mvn clean install' in 'src' e copia il JAR da 'src/client/target/' a 'bin/dist/'."
    exit 1
fi
echo "✅ Client distribuito: dist/BooksClient.jar"

echo ""
echo "5️⃣ AVVIO CLIENT"
echo "================"

echo "🚀 Avvio Books Client..."
echo ""

# MODIFICATO: Esegue il client dalla directory 'src' per fargli trovare le risorse (es. bookcovers)
# Un subshell ( ... ) viene usato per cambiare directory temporaneamente.
# Il percorso del JAR è stato aggiornato di conseguenza a ../bin/dist/BooksClient.jar

(
  cd ../src
  if [[ "$OSTYPE" == "darwin"* ]]; then
      # macOS
      echo "🍎 Sistema: macOS"
      java -Djava.awt.headless=false -Djavafx.platform=desktop -jar ../bin/dist/BooksClient.jar &
  elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
      # Linux
      echo "🐧 Sistema: Linux"
      java --add-modules javafx.controls,javafx.fxml -jar ../bin/dist/BooksClient.jar &
  else
      # Altri sistemi
      echo "🖥️  Sistema: Altri"
      java -jar ../bin/dist/BooksClient.jar &
  fi
) &
CLIENT_PID=$!


echo "   Client PID: $CLIENT_PID"
echo ""
echo "✅ CLIENT AVVIATO CON SUCCESSO!"
echo "   La directory di lavoro del client è 'src/'."
echo ""
echo "📊 SERVIZI ATTIVI:"
echo "   🖥️  Server: http://localhost:$SERVER_PORT"
echo "   📱 Client: Applicazione JavaFX"
echo ""
echo "🔧 COMANDI UTILI:"
# MODIFICATO: Aggiornato percorso log
echo "   Server logs: tail -f ../src/server.log"
echo "   Stop server: kill $SERVER_PID"
echo ""
echo "⏹️  Premi CTRL+C per fermare tutti i servizi"
echo ""

wait $CLIENT_PID 2>/dev/null || true

echo ""
echo "👋 Client terminato"
cleanup