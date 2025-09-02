#####################################################################
#                                                                   #
#        BOOKRECOMMENDER - Guida all'Avvio Tramite Eseguibili       #
#                                                                   #
#####################################################################
Questo file fornisce le istruzioni per avviare l'applicazione BookRecommender (server e client) utilizzando gli script di avvio rapido presenti nella cartella bin.

======================================
PREREQUISITI
======================================

Prima di iniziare, assicurati di avere installato e configurato correttamente sul tuo sistema:
-Java 17 o superiore
-Maven
-PostgreSQL (opzionale, ma raccomandato per un'esperienza completa)

======================================
STRUTTURA DELLE CARTELLE
======================================

Per un corretto funzionamento, gli script devono essere eseguiti dalla cartella bin. La struttura delle cartelle del progetto deve essere la seguente:

BookRecommender/
├── bin/
│   ├── start-complete-app.bat  (per Windows)
│   ├── start-complete-app.sh   (per macOS/Linux)
│   ├── build-executables.sh    (script di utilità per macOS/Linux)
│   └── dist/
│       ├── BooksClient.jar         (il client compilato per macOS/Linux)
│       └── BookReccommender.jar    (il client compilato per Windows)
├── client/
├── server/
└── pom.xml

Importante: Assicurati che i file .jar siano presenti all'interno della cartella bin/dist. Se così non fosse, esegui il comando mvn clean install dalla cartella principale del progetto e copia il JAR generato da client/target/ a bin/dist/ (Windows) per macOS invece esegui ./build-ececutables.sh

======================================
ISTRUZIONI PER L'AVVIO
======================================

Apri un terminale (su macOS/Linux) o un Prompt dei comandi (su Windows) e posizionati all'interno della cartella bin del progetto.

===================
WINDOWS
===================

Il metodo più semplice per avviare l'applicazione attraverso l'eseguibile è navigare nella cartella bin con Esplora File e fare doppio clic sul file:

    start-complete-app.bat

In alternativa, puoi avviarlo dal Prompt dei comandi utilizzando questi comandi:

    start-complete-app.bat

se il Prompt dei comandi è stato aperto nella cartella bin; altrimenti bisogna utilizzare questi comandi:

    cd percorso\del\progetto\BookRecommender\bin

per posizionarsi nella cartella corretta (bin), e poi:

    start-complete-app.bat

===================
macOS/Linux
===================

Per motivi di sicurezza del sistema operativo, prima del primo avvio è necessario rendere gli script eseguibili.

Dai i permessi di esecuzione (da fare solo la prima volta):
    Apri il terminale nella cartella bin ed esegui questo comando per entrambi gli script.

    Bash

        chmod +x start-complete-app.sh
        chmod +x build-executables.sh
        Avvia l'applicazione:

    Bash

        ./start-complete-app.sh

==================================
RISOLUZIONE PROBLEMI macOS/Linux
==================================

Se macOS blocca l'esecuzione dello script mostrando un avviso di sicurezza ("Gatekeeper"), esegui questo comando per rimuovere l'attributo di quarantena, sempre dalla cartella bin:

    Bash

        xattr -d com.apple.quarantine start-complete-app.sh
        xattr -d com.apple.quarantine build-executables.sh

Dopo aver eseguito questo comando, prova a riavviare l'applicazione con:
    ./start-complete-app.sh.

Uso dello script build-executables.sh
Questo script serve per preparare o compilare componenti necessari all'applicazione. Se richiesto dalle istruzioni o se l'avvio principale fallisce, eseguilo prima di start-complete-app.sh:

    Bash

        ./build-executables.sh

===============================================
FUNZIONAMENTO DELLO SCRIPT
===============================================

-Una volta avviato, lo script eseguirà automaticamente i seguenti passaggi:
-Verifica dei prerequisiti (Java, Maven).
-Controllo della connessione al database PostgreSQL.
-Avvio del server Spring Boot in background.
-Avvio del client JavaFX con l'interfaccia grafica.

Al termine dell'utilizzo, chiudi la finestra del client. Ti verrà chiesto nel terminale se desideri arrestare anche il server.