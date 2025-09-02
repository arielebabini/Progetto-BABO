#!/bin/bash
# start-server.sh - Avvia solo il server Spring Boot in background

set -e

echo "🖥️  AVVIO SERVER SPRING BOOT"
echo "============================"

# Verifica se è già in esecuzione
if lsof -i :5432 >/dev/null 2>&1; then
    echo "✅ Server già in esecuzione sulla porta 5432"
    echo "   Per fermarlo: pkill -f 'spring-boot:run'"
    echo "   Test: curl http://localhost:5432/api/books/health"
    exit 0
fi

# Verifica prerequisiti
if [ ! -f "pom.xml" ] || [ ! -d "server" ]; then
    echo "❌ Esegui questo script dalla directory root del progetto BookRecommender"
    exit 1
fi

echo "🚀 Avvio server in background..."

# Avvia server con Maven
nohup mvn spring-boot:run -pl server -Dspring-boot.run.fork=true > server.log 2>&1 &
SERVER_PID=$!

echo "✅ Server avviato!"
echo "   PID: $SERVER_PID"
echo "   Logs: tail -f server.log"
echo "   Per fermarlo: kill $SERVER_PID"

# Salva PID per riferimento futuro
echo $SERVER_PID > server.pid

# Aspetta che sia pronto
echo "⏳ Attesa avvio server..."
attempts=0
max_attempts=30

while [ $attempts -lt $max_attempts ]; do
    if curl -s http://localhost:5432/api/books/health >/dev/null 2>&1; then
        echo "✅ Server pronto!"
        echo "   Health check: $(curl -s http://localhost:5432/api/books/health)"
        echo "   API: http://localhost:5432/api/books"
        break
    fi

    attempts=$((attempts + 1))
    echo "   Tentativo $attempts/$max_attempts..."
    sleep 1
done

if [ $attempts -eq $max_attempts ]; then
    echo "❌ Server non si è avviato entro $max_attempts secondi"
    echo "   Controlla i logs: tail server.log"
    exit 1
fi

echo ""
echo "🎉 Server avviato con successo in background!"
echo "   Per fermarlo: ./stop-server.sh oppure kill $SERVER_PID"