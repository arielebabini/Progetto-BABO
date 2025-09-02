#!/bin/bash
# check-services.sh - Controlla lo stato di tutti i servizi

echo "📊 STATO SERVIZI BOOKS CLIENT"
echo "===================================="

# Funzione per test HTTP
test_http() {
    curl -s "$1" >/dev/null 2>&1
}

# Funzione per controllo porta
check_port() {
    lsof -i :$1 >/dev/null 2>&1
}

echo ""
echo "🗄️  DATABASE POSTGRESQL"
echo "======================="

if command -v psql >/dev/null 2>&1; then
    if pg_isready -q 2>/dev/null; then
        echo "✅ PostgreSQL in esecuzione"

        # Test connessione al database specifico
        if PGPASSWORD=postgress psql -h localhost -U postgres -d DataProva -c "SELECT COUNT(*) FROM books;" 2>/dev/null | grep -q "[0-9]"; then
            BOOK_COUNT=$(PGPASSWORD=postgress psql -h localhost -U postgres -d DataProva -c "SELECT COUNT(*) FROM books;" 2>/dev/null | grep -E "^ *[0-9]+" | tr -d ' ')
            echo "✅ Database DataProva raggiungibile ($BOOK_COUNT libri)"
        else
            echo "⚠️  Database DataProva non raggiungibile"
        fi
    else
        echo "❌ PostgreSQL non in esecuzione"
        echo "   👉 Avvia con: brew services start postgresql (macOS)"
    fi
else
    echo "⚠️  psql non installato"
fi

echo ""
echo "🖥️  SERVER SPRING BOOT"
echo "====================="

if check_port 8080; then
    echo "✅ Porta 8080 in uso"

    # Test health endpoint
    if test_http "http://localhost:8080/api/books/health"; then
        HEALTH_RESPONSE=$(curl -s "http://localhost:8080/api/books/health" 2>/dev/null)
        echo "✅ Health check: $HEALTH_RESPONSE"

        # Test books API
        if test_http "http://localhost:8080/api/books"; then
            BOOKS_RESPONSE=$(curl -s "http://localhost:8080/api/books" 2>/dev/null)
            BOOK_COUNT=$(echo "$BOOKS_RESPONSE" | grep -o '"id"' | wc -l | tr -d ' ')
            echo "✅ Books API: $BOOK_COUNT libri disponibili"
        else
            echo "⚠️  Books API non risponde"
        fi
    else
        echo "⚠️  Server non risponde agli endpoint"
    fi

    # Mostra processo Java in esecuzione
    echo "📋 Processi Java attivi:"
    ps aux | grep java | grep -v grep | grep -E "(spring-boot|BookRecommender)" | head -3
else
    echo "❌ Server non in esecuzione (porta 8080 libera)"
    echo "   👉 Avvia con: mvn spring-boot:run -pl server"
fi

echo ""
echo "📱 CLIENT JAVAFX"
echo "================"

if [ -f "dist/BooksClient.jar" ]; then
    JAR_SIZE=$(du -h "dist/BooksClient.jar" | cut -f1)
    echo "✅ JAR client presente: $JAR_SIZE"

    # Test contenuto JAR
    if jar tf "dist/BooksClient.jar" | grep -q "javafx/application/Application.class"; then
        echo "✅ JAR contiene JavaFX"
    else
        echo "⚠️  JAR non contiene JavaFX"
    fi

    if jar tf "dist/BooksClient.jar" | grep -q "org/BABO/client/ClientApplication.class"; then
        echo "✅ Main class presente"
    else
        echo "❌ Main class non trovata"
    fi
else
    echo "❌ JAR client non trovato"
    echo "   👉 Genera con: ./build-executables.sh"
fi

# Controlla se il client è in esecuzione
if pgrep -f "BooksClient.jar" >/dev/null; then
    echo "✅ Client in esecuzione"
    echo "📋 Processi client:"
    ps aux | grep BooksClient | grep -v grep
else
    echo "⭕ Client non in esecuzione"
fi

echo ""
echo "🔧 SCRIPT DISPONIBILI"
echo "===================="

scripts=("start-complete-app.sh" "build-executables.sh" "test-executable.sh")

for script in "${scripts[@]}"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            echo "✅ $script (eseguibile)"
        else
            echo "⚠️  $script (non eseguibile - chmod +x $script)"
        fi
    else
        echo "❌ $script (mancante)"
    fi
done

echo ""
echo "📝 RIASSUNTO"
echo "============"

# Determina stato generale
if check_port 8080 && test_http "http://localhost:8080/api/books/health" && [ -f "dist/BooksClient.jar" ]; then
    echo "🟢 SISTEMA PRONTO"
    echo "   👉 Avvia con: ./start-complete-app.sh"
elif check_port 8080; then
    echo "🟡 SERVER ATTIVO, CLIENT DA PREPARARE"
    echo "   👉 Rigenera client: ./build-executables.sh"
    echo "   👉 Avvia client: ./dist/macos/BooksClient.command"
else
    echo "🔴 SERVIZI NON ATTIVI"
    echo "   👉 Avvio completo: ./start-complete-app.sh"
fi