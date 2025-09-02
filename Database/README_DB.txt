################################################################
#                                                              #
#        ISTRUZIONI PER L'INSTALLAZIONE DEL DATABASE           #
#                                                              #
################################################################

================================================================================
INFORMAZIONI SUL PROGETTO
================================================================================

Nome Progetto: Book Recommender BABO
Versione: 1.0 - SNAPSHOT

Autori: 
- Babini Ariele —> 757608
- Bottaro Federico —>758017

================================================================================
INDICE
================================================================================

1. Descrizione
2. Prerequisiti
3. Istruzioni per l'Installazione
    3.1. Individuazione del file
    3.2. Configurazione dell'Ambiente
    3.3. Esecuzione dello Script di Installazione
    3.4. Passaggi Post-Installazione
4. Verifica dell'Installazione
5. Problemi comuni
6. Informazioni Aggiuntive

================================================================================
1. DESCRIZIONE
================================================================================

Questo file fornisce le istruzioni dettagliate per installare e configurare correttamente il database per il progetto Book Recommender BABO.
Il database è basato su PostgreSQL e contiene tutte le tabelle, le viste e le procedure necessarie per il corretto funzionamento dell'applicazione.

================================================================================
2. PREREQUISITI
================================================================================

Prima di procedere, assicurati che i seguenti software siano installati e funzionanti sul tuo sistema:  

* PostgreSQL: È necessario avere un'installazione attiva di PostgreSQL. Durante l'installazione, assicurati che gli strumenti a riga di comando (come `createdb` e `pg_restore`) 
	              vengano aggiunti al PATH di sistema per poterli eseguire da qualsiasi terminale.     
* Download: [https://www.postgresql.org/download/](https://www.postgresql.org/download/)  

* pgAdmin 4 (Opzionale): Se si preferisce avere  un'interfaccia grafica per visualizzare e gestire il database, ti consigliamo di installare pgAdmin.     
* Download: [https://www.pgadmin.org/download/](https://www.pgadmin.org/download/)



================================================================================
3. ISTRUZIONI PER L'INSTALLAZIONE
================================================================================

Segui attentamente questi passaggi per installare il database.

--------------------------------------------------------------------------------
3.1. Individuazione del file
--------------------------------------------------------------------------------

Per prima cosa, dobbiamo conoscere il posizionamento del file che ci interessa, chiamato **`DataProva.sql`**, che si trova all'interno della cartella **`Database`**.  

Assicurati di avere a portata di mano il **percorso completo** di questo file (ad esempio: `/Users/TuoNome/Progetto_BABO/Database/DataProva.sql`), poiché ti servirà nel passaggio 3.3. 


--------------------------------------------------------------------------------
3.2. Configurazione dell'Ambiente
--------------------------------------------------------------------------------

Per procedere abbiamo bisogno di un database vuoto. Segui uno dei metodi seguenti. 

**Metodo 1: Riga di Comando (Consigliato)**

1. Apri il terminale (su Mac/Linux) o il **Prompt dei comandi** (su Windows) e inserisci il seguente comando per creare il database.

	```bash 
	      createdb -U postgres DataProva 
	```


 2. **Se il comando non funziona**, molto probabilmente significa che le variabili d'ambiente di Windows non sono impostate correttamente o che
        PostgreSQL non è stato installato correttamente. 

    * **Risoluzione (Windows):** Devi aggiungere la cartella `bin` e `lib` della tua installazione di PostgreSQL alla variabile d'ambiente `PATH` del sistema. 
      * Cerca "Modifica le variabili d'ambiente relative al sistema" nel menu Start. 
      * Nella finestra "Proprietà del sistema", clicca su "Variabili d'ambiente". 
      * Nella sezione "Variabili di sistema", seleziona `Variabili di Sistema`, cerca la variabile `Path` e clicca su "Modifica". 
      * Aggiungi due nuove voci con i percorsi alle cartelle `bin` e `lib` di PostgreSQL (es. `C:\Program Files\PostgreSQL\16\bin` e `C:\Program Files\PostgreSQL\16\lib`). 
      * Conferma con "OK" e riavvia il terminale. 

**Metodo 2: Interfaccia Grafica (pgAdmin)** 

3. Qualora non riuscissi con la riga di comando, puoi sempre usufruire dell'interfaccia grafica offerta da **pgAdmin**. 
  * Apri pgAdmin e connettiti al tuo server. 
  * Fai clic con il tasto destro su **"Databases"** nel menu ad albero a sinistra. 
  * Seleziona **Create** -> **Database...** 
  * Inserisci il nome del database (DataProva) nel campo "Database" e salva.

--------------------------------------------------------------------------------
3.3. Esecuzione dello Script di Installazione
--------------------------------------------------------------------------------

Il metodo più comune per installare la struttura del database è eseguire lo script SQL principale.

**Metodo 1: Tramite Linea di Comando (Consigliato)**

1.  Apri il terminale o il prompt dei comandi.
2.  Assicurati di esserti posizionato all’interno della cartella Database contenente il .sql
2.  Esegui il seguente comando, sostituendo il segnaposto con il nome DataProva:

    `pg_restore -U postgres -d DatProva DataProva.sql’


**Metodo 2: Tramite Interfaccia Grafica (GUI)**

1. Apri pgAdmin e individua il database creato al punto precedente nel pannello a sinistra.
2. Fai clic con il tasto destro del mouse sul nome del database.
3. Nel menu che appare, cerca e seleziona la voce "Restore...".
4. Si aprirà una finestra: nel campo "Filename", clicca sull'icona della cartella per navigare fino al file DataProva.sql contenuto nella cartella Database.
5. Seleziona il file e clicca sul pulsante "Restore" per avviare il processo.

================================================================================
4. VERIFICA DELL'INSTALLAZIONE
================================================================================

Per verificare che l'installazione sia andata a buon fine, puoi eseguire una semplice query di controllo. Connettiti al database:

	’psql -U postgres -d DataProva’

ed esegui:
	’\dt’

Se il comando restituisce un elenco di tabelle, il database è stato installato correttamente.


================================================================================
5. PROBLEMI COMUNI
================================================================================

In caso di errori specifici riguardo al mancato collegamento al database verificare:
- La porta PostgreSQL sia effettivamente la 5432
- Che la password del db sia postgress

Eventualmente apportare modifiche hai file start-complete-app.sh o start-complete-app.bat

================================================================================
6. INFORMAZIONI AGGIUNTIVE
================================================================================

Per qualsiasi domanda o problema non trattato in questo documento, si prega di contattare i membri del team alle seguenti email:
- ababini@studenti.uninsubria.it
- fbottaro@studenti.uninsubria.it
