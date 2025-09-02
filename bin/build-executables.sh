#!/bin/bash
# build-executables.sh - Compila e impacchetta server e client in JAR eseguibili
# MODIFICATO per la nuova struttura con la cartella 'src'

set -e

echo "📦 Inizio la build completa del progetto con Maven..."

# MODIFICATO: Va nella cartella 'src' per poter eseguire Maven.
# Il comando trova la directory dello script (bin), va alla root (..) e poi entra in 'src'.
cd "$(dirname "$0")/../src"

echo "   Mi posiziono in: $(pwd)"

# Questo comando ora viene eseguito correttamente dalla cartella 'src'
# "clean" pulisce le build precedenti
# "package" compila il codice e crea i JAR nella cartella "target" di ogni modulo
mvn clean package

echo "✅ Build completata!"

# MODIFICATO: Prepara la cartella "dist" all'interno di "bin"
echo "🚚 Preparazione della distribuzione del client..."
mkdir -p ../bin/dist

# MODIFICATO: Copia il JAR dalla cartella 'client/target' (che si trova in 'src')
# alla cartella di destinazione '../bin/dist' (risalendo da 'src').
cp client/target/client-*.jar ../bin/dist/BooksClient.jar

echo ""
echo "🎉 Operazione terminata con successo!"
echo "   JAR del client pronto in: bin/dist/BooksClient.jar"
echo "   JAR del server pronto in: src/server/target/server-*.jar"