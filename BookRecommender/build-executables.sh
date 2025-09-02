#!/bin/bash
# build-executables.sh - Compila e impacchetta server e client in JAR eseguibili

set -e

echo "📦 Inizio la build completa del progetto con Maven..."

# Esegue il "packaging" per entrambi i moduli (server e client)
# "clean" pulisce le build precedenti
# "package" compila il codice e crea i JAR nella cartella "target" di ogni modulo
mvn clean package

echo "✅ Build completata!"

# Prepara la cartella "dist" per il client, come previsto da start-complete-app.sh
echo "🚚 Preparazione della distribuzione del client..."
mkdir -p dist
cp client/target/client-*.jar dist/BooksClient.jar

echo "🎉 JAR del client pronto in: dist/BooksClient.jar"
echo "   JAR del server pronto in: server/target/server-*.jar"