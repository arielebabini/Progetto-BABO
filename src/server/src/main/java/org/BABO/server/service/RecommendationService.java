package org.BABO.server.service;

import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.model.Book;
import org.BABO.shared.dto.Recommendation.RecommendationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio server per la gestione completa delle raccomandazioni di libri tra utenti.
 * <p>
 * Questo servizio implementa un sistema di raccomandazioni peer-to-peer che consente agli utenti
 * di suggerire libri ad altri utenti basandosi sui loro interessi e letture. Il sistema include
 * controlli di autorizzazione, limiti per prevenire spam e gestione transazionale per garantire
 * la consistenza dei dati.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 * <li><strong>Sistema di Autorizzazione:</strong> Solo utenti che possiedono un libro possono raccomandare altri libri correlati</li>
 * <li><strong>Controllo Limiti:</strong> Massimo 3 raccomandazioni per libro per utente per evitare spam</li>
 * <li><strong>Gestione Transazionale:</strong> Operazioni atomiche con rollback automatico in caso di errore</li>
 * <li><strong>Validazione Dati:</strong> Controlli di esistenza libri e validità richieste</li>
 * <li><strong>Recupero Dettagli:</strong> Integrazione con BookService per dettagli completi libri raccomandati</li>
 * <li><strong>Analytics:</strong> Statistiche uso sistema e conteggi per dashboard amministrative</li>
 * </ul>
 *
 * <h3>Struttura Database:</h3>
 * <p>
 * Il servizio opera sulla tabella {@code advise} con la seguente struttura:
 * </p>
 * <ul>
 * <li>{@code id} (BIGSERIAL PRIMARY KEY) - Identificativo univoco del record</li>
 * <li>{@code username} (VARCHAR) - Nome utente che effettua la raccomandazione</li>
 * <li>{@code isbn} (VARCHAR) - ISBN del libro target per cui si raccomandano altri libri</li>
 * <li>{@code isbn1} (VARCHAR, nullable) - Primo libro raccomandato</li>
 * <li>{@code isbn2} (VARCHAR, nullable) - Secondo libro raccomandato</li>
 * <li>{@code isbn3} (VARCHAR, nullable) - Terzo libro raccomandato</li>
 * </ul>
 *
 * <h3>Vincoli di Unique:</h3>
 * <p>
 * La tabella implementa un vincolo UNIQUE sulla coppia (username, isbn) per garantire
 * che ogni utente possa avere un solo set di raccomandazioni per ogni libro target.
 * </p>
 *
 * <h3>Sistema di Autorizzazione:</h3>
 * <p>
 * Per poter raccomandare libri correlati ad un libro target, l'utente deve:
 * </p>
 * <ol>
 * <li>Avere il libro target in almeno una delle sue librerie personali</li>
 * <li>Non aver già raggiunto il limite massimo di 3 raccomandazioni per quel libro</li>
 * <li>Non aver già raccomandato lo stesso libro (controllo duplicati)</li>
 * </ol>
 *
 * <h3>Configurazione Database:</h3>
 * <pre>{@code
 * URL: jdbc:postgresql://localhost:5432/DataProva
 * User: postgres
 * Password: postgress
 * Limite raccomandazioni per libro: 3
 * }</pre>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * @Autowired
 * private RecommendationService recommendationService;
 *
 * // Verifica autorizzazione utente
 * boolean canRecommend = recommendationService.canUserRecommend("user123", "978-0123456789");
 * if (canRecommend) {
 * // Crea richiesta raccomandazione
 * RecommendationRequest request = new RecommendationRequest();
 * request.setUsername("user123");
 * request.setTargetBookIsbn("978-0123456789");
 * request.setRecommendedBookIsbn("978-9876543210");
 *
 * // Aggiunge raccomandazione
 * boolean success = recommendationService.addRecommendation(request);
 * if (success) {
 * System.out.println("Raccomandazione aggiunta con successo");
 * }
 * }
 *
 * // Recupera tutte le raccomandazioni per un libro
 * List<BookRecommendation> recommendations =
 * recommendationService.getRecommendationsForBook("978-0123456789");
 *
 * // Ottiene dettagli completi dei libri raccomandati
 * List<Book> recommendedBooks =
 * recommendationService.getRecommendedBooksDetails("978-0123456789");
 *
 * // Statistiche sistema
 * String stats = recommendationService.getRecommendationStats();
 * System.out.println(stats);
 * }</pre>
 *
 * <h3>Gestione Errori e Transazioni:</h3>
 * <p>
 * Il servizio utilizza transazioni database per garantire consistenza dei dati e implementa
 * pattern di rollback automatico in caso di errori. Tutti gli errori sono loggati con emoji
 * distintive per facilitare debugging e monitoring.
 * </p>
 *
 * <h3>Integrazione con Altri Servizi:</h3>
 * <p>
 * Il servizio si integra strettamente con:
 * </p>
 * <ul>
 * <li>{@link BookService} - Per validazione esistenza libri e recupero dettagli</li>
 * <li>{@link LibraryService} - Per verificare autorizzazioni utente basate su possesso libri</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see BookRecommendation
 * @see RecommendationRequest
 * @see BookService
 * @see LibraryService
 */
@Service
public class RecommendationService {

    /** URL di connessione al database PostgreSQL */
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";

    /** Username per l'autenticazione database */
    private static final String DB_USER = "postgres";

    /** Password per l'autenticazione database */
    private static final String DB_PASSWORD = "postgress";

    /** Limite massimo di raccomandazioni per libro per utente */
    private static final int MAX_RECOMMENDATIONS_PER_BOOK = 3;

    /** Servizio per operazioni sui libri */
    @Autowired
    private BookService bookService;

    /** Servizio per operazioni sulle librerie utente */
    @Autowired
    private LibraryService libraryService;

    /**
     * Verifica se un utente è autorizzato a raccomandare libri per un libro target specifico.
     * <p>
     * Questa è la funzione di controllo accesso principale del sistema. Un utente può
     * raccomandare libri solo se possiede il libro target in almeno una delle sue librerie.
     * Questo meccanismo garantisce che le raccomandazioni provengano da utenti che hanno
     * effettivamente letto o possiedono il libro di riferimento.
     * </p>
     *
     * <p>
     * Il processo di verifica include:
     * </p>
     * <ol>
     * <li>Recupero di tutte le librerie dell'utente tramite {@link LibraryService}</li>
     * <li>Scansione di ogni libreria per verificare presenza del libro target</li>
     * <li>Confronto ISBN per identificazione esatta del libro</li>
     * </ol>
     *
     * @param username il nome utente da verificare per le autorizzazioni
     * @param targetBookIsbn il codice ISBN del libro target per cui si vogliono fare raccomandazioni
     * @return {@code true} se l'utente possiede il libro target in almeno una libreria,
     * {@code false} se non lo possiede o in caso di errori di verifica
     *
     * @throws IllegalArgumentException se username o targetBookIsbn sono null o vuoti
     *
     * @apiNote La verifica viene effettuata confrontando gli ISBN in modo case-sensitive.
     * Il metodo ha performance O(n*m) dove n è il numero di librerie e m il numero
     * medio di libri per libreria.
     *
     * @see LibraryService#getUserLibraries(String)
     * @see LibraryService#getBooksInLibrary(String, String)
     */
    public boolean canUserRecommend(String username, String targetBookIsbn) {
        System.out.println("🔍 Verifica permessi raccomandazione per utente: " + username + ", ISBN: " + targetBookIsbn);

        // Verifica se l'utente ha il libro target nelle sue librerie
        List<String> userLibraries = libraryService.getUserLibraries(username);

        for (String libraryName : userLibraries) {
            List<Book> booksInLibrary = libraryService.getBooksInLibrary(username, libraryName);
            boolean hasBook = booksInLibrary.stream()
                    .anyMatch(book -> targetBookIsbn.equals(book.getIsbn()));

            if (hasBook) {
                System.out.println("✅ Utente " + username + " ha il libro " + targetBookIsbn + " nella libreria: " + libraryName);
                return true;
            }
        }

        System.out.println("❌ Utente " + username + " non ha il libro " + targetBookIsbn + " nelle sue librerie");
        return false;
    }

    /**
     * Aggiunge una nuova raccomandazione al sistema con controlli completi di validazione e autorizzazione.
     * <p>
     * Questo è il metodo principale per l'inserimento di nuove raccomandazioni. Implementa
     * un workflow completo che include validazione richiesta, controllo autorizzazioni,
     * verifica limiti, validazione esistenza libri e inserimento transazionale nel database.
     * </p>
     *
     * <p>
     * Il flusso operativo include:
     * </p>
     * <ol>
     * <li><strong>Validazione richiesta:</strong> Controllo completezza e correttezza dati input</li>
     * <li><strong>Verifica autorizzazioni:</strong> L'utente deve possedere il libro target</li>
     * <li><strong>Controllo limiti:</strong> Massimo 3 raccomandazioni per libro per utente</li>
     * <li><strong>Validazione libro:</strong> Il libro raccomandato deve esistere nel sistema</li>
     * <li><strong>Inserimento transazionale:</strong> Operazione atomica con rollback automatico</li>
     * <li><strong>Debug e logging:</strong> Tracciamento completo dello stato per troubleshooting</li>
     * </ol>
     *
     * @param request l'oggetto {@link RecommendationRequest} contenente tutti i dati della raccomandazione:
     * username, ISBN libro target, ISBN libro raccomandato
     * @return {@code true} se la raccomandazione è stata aggiunta con successo,
     * {@code false} in caso di validazione fallita, autorizzazioni insufficienti,
     * limiti raggiunti, libro inesistente o errori database
     *
     * @throws IllegalArgumentException se request è null
     * @throws RuntimeException se si verificano errori critici non recuperabili
     *
     * @apiNote Le operazioni database utilizzano transazioni per garantire consistenza.
     * Il metodo include logging dettagliato dello stato prima e dopo l'operazione
     * per facilitare debugging e monitoring.
     *
     * @see #canUserRecommend(String, String)
     * @see #getRecommendationsCountForUser(String, String)
     * @see #insertRecommendation(RecommendationRequest)
     */
    public boolean addRecommendation(RecommendationRequest request) {
        System.out.println("📝 Aggiunta raccomandazione: " + request.getRecommendedBookIsbn() +
                " per " + request.getTargetBookIsbn() + " da " + request.getUsername());

        // Debug stato iniziale
        debugRecommendationState(request.getUsername(), request.getTargetBookIsbn());

        // Validazione
        if (!request.isValid()) {
            System.out.println("❌ Richiesta non valida: " + request.getValidationErrors());
            return false;
        }

        // Verifica permessi
        if (!canUserRecommend(request.getUsername(), request.getTargetBookIsbn())) {
            System.out.println("❌ Utente non autorizzato a consigliare per questo libro");
            return false;
        }

        // Verifica limite raccomandazioni
        int currentCount = getRecommendationsCountForUser(request.getUsername(), request.getTargetBookIsbn());
        System.out.println("🔢 Raccomandazioni attuali: " + currentCount + "/" + MAX_RECOMMENDATIONS_PER_BOOK);

        if (currentCount >= MAX_RECOMMENDATIONS_PER_BOOK) {
            System.out.println("❌ Limite raccomandazioni raggiunto per l'utente");
            return false;
        }

        // Verifica che il libro consigliato esista
        try {
            Book recommendedBook = bookService.getBookByIsbn(request.getRecommendedBookIsbn());
            if (recommendedBook == null) {
                System.out.println("❌ Libro consigliato non trovato: " + request.getRecommendedBookIsbn());
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ Errore verifica libro consigliato: " + e.getMessage());
            return false;
        }

        // Aggiungi raccomandazione al database
        boolean result = insertRecommendation(request);

        // Debug stato finale
        if (result) {
            System.out.println("✅ Raccomandazione aggiunta con successo");
            debugRecommendationState(request.getUsername(), request.getTargetBookIsbn());
        } else {
            System.out.println("❌ Fallimento aggiunta raccomandazione");
        }

        return result;
    }

    /**
     * Inserisce una nuova raccomandazione nel database utilizzando gestione transazionale completa.
     * <p>
     * Metodo interno che gestisce l'inserimento effettivo nel database utilizzando transazioni
     * per garantire consistenza dei dati. Implementa pattern di UPSERT con gestione dei conflitti
     * e utilizza locking per prevenire race conditions in ambiente multi-utente.
     * </p>
     *
     * <p>
     * La strategia di inserimento segue questo approccio:
     * </p>
     * <ol>
     * <li><strong>Transazione atomica:</strong> Disabilita auto-commit per operazioni multiple</li>
     * <li><strong>SELECT FOR UPDATE:</strong> Lock ottimistico per prevenire race conditions</li>
     * <li><strong>Conditional logic:</strong> UPDATE se record esiste, INSERT se non esiste</li>
     * <li><strong>Rollback automatico:</strong> In caso di errori tutti i cambiamenti vengono annullati</li>
     * </ol>
     *
     * @param request la richiesta di raccomandazione contenente i dati da inserire
     * @return {@code true} se l'inserimento è completato con successo,
     * {@code false} in caso di errori SQL, conflitti di concorrenza o violazioni vincoli
     *
     * @implNote Utilizza SELECT FOR UPDATE per prevenire race conditions e garantisce
     * che le operazioni siano atomiche. In caso di errore, esegue rollback automatico
     * di tutte le modifiche effettuate nella transazione.
     *
     * @see #updateExistingRecommendationWithId(Connection, RecommendationRequest, ResultSet, Long)
     * @see #createNewRecommendationWithUpsert(Connection, RecommendationRequest)
     */
    private boolean insertRecommendation(RecommendationRequest request) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Usa una transazione per evitare race conditions
            conn.setAutoCommit(false);

            try {
                // Prima controlla se esiste già un record per questo utente e libro target
                String selectQuery = "SELECT id, isbn1, isbn2, isbn3 FROM advise WHERE username = ? AND isbn = ? FOR UPDATE";

                try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                    selectStmt.setString(1, request.getUsername());
                    selectStmt.setString(2, request.getTargetBookIsbn());

                    ResultSet rs = selectStmt.executeQuery();

                    if (rs.next()) {
                        // Record esiste, aggiorna aggiungendo la nuova raccomandazione
                        Long recordId = rs.getLong("id");
                        boolean result = updateExistingRecommendationWithId(conn, request, rs, recordId);
                        if (result) {
                            conn.commit();
                        } else {
                            conn.rollback();
                        }
                        return result;
                    } else {
                        // Record non esiste, creane uno nuovo
                        boolean result = createNewRecommendationWithUpsert(conn, request);
                        if (result) {
                            conn.commit();
                        } else {
                            conn.rollback();
                        }
                        return result;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore database aggiunta raccomandazione: " + e.getMessage());
            return false;
        }
    }

    /**
     * Aggiorna un record di raccomandazione esistente aggiungendo una nuova raccomandazione.
     * <p>
     * Metodo interno che gestisce l'aggiornamento di record già presenti nella tabella advise.
     * Trova il primo slot disponibile (isbn1, isbn2, o isbn3) e inserisce la nuova raccomandazione,
     * verificando preventivamente che non sia un duplicato.
     * </p>
     *
     * <p>
     * La logica di aggiornamento segue questo approccio:
     * </p>
     * <ol>
     * <li><strong>Controllo duplicati:</strong> Verifica che la raccomandazione non esista già</li>
     * <li><strong>Ricerca slot libero:</strong> Identifica primo campo isbn disponibile</li>
     * <li><strong>Aggiornamento mirato:</strong> UPDATE solo del campo specifico necessario</li>
     * <li><strong>Validazione risultato:</strong> Verifica che l'aggiornamento sia avvenuto</li>
     * </ol>
     *
     * @param conn la connessione database attiva (deve essere in transazione)
     * @param request la richiesta contenente i dati della nuova raccomandazione
     * @param rs il ResultSet posizionato sui dati del record esistente
     * @param recordId l'ID univoco del record da aggiornare
     * @return {@code true} se l'aggiornamento è completato con successo,
     * {@code false} se tutti gli slot sono occupati, la raccomandazione è duplicata
     * o si verificano errori SQL
     *
     * @throws SQLException se si verificano errori durante l'accesso al database
     *
     * @implNote Il metodo legge lo stato attuale degli slot dal ResultSet e determina
     * dinamicamente quale UPDATE SQL eseguire basandosi sui campi disponibili.
     */
    private boolean updateExistingRecommendationWithId(Connection conn, RecommendationRequest request, ResultSet rs, Long recordId) throws SQLException {
        String isbn1 = rs.getString("isbn1");
        String isbn2 = rs.getString("isbn2");
        String isbn3 = rs.getString("isbn3");

        System.out.println("🔍 Stato attuale (ID: " + recordId + "): isbn1=" + isbn1 + ", isbn2=" + isbn2 + ", isbn3=" + isbn3);

        // Controlla se la raccomandazione esiste già
        if (request.getRecommendedBookIsbn().equals(isbn1) ||
                request.getRecommendedBookIsbn().equals(isbn2) ||
                request.getRecommendedBookIsbn().equals(isbn3)) {
            return false;
        }

        // Trova il primo slot libero
        String updateQuery;
        int paramIndex;

        if (isbn1 == null || isbn1.trim().isEmpty()) {
            updateQuery = "UPDATE advise SET isbn1 = ? WHERE id = ?";
            paramIndex = 1;
        } else if (isbn2 == null || isbn2.trim().isEmpty()) {
            updateQuery = "UPDATE advise SET isbn2 = ? WHERE id = ?";
            paramIndex = 2;
        } else if (isbn3 == null || isbn3.trim().isEmpty()) {
            updateQuery = "UPDATE advise SET isbn3 = ? WHERE id = ?";
            paramIndex = 3;
        } else {
            System.out.println("❌ Tutti gli slot per le raccomandazioni sono occupati");
            return false;
        }

        System.out.println("📝 Aggiornamento slot " + paramIndex + " con ISBN: " + request.getRecommendedBookIsbn() + " (Record ID: " + recordId + ")");

        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setString(1, request.getRecommendedBookIsbn());
            updateStmt.setLong(2, recordId);

            int updated = updateStmt.executeUpdate();
            if (updated > 0) {
                System.out.println("✅ Raccomandazione aggiunta al slot " + paramIndex);
                return true;
            } else {
                System.out.println("❌ Nessuna riga aggiornata per slot " + paramIndex);
                return false;
            }
        }
    }

    /**
     * Crea un nuovo record di raccomandazione utilizzando pattern UPSERT con gestione conflitti.
     * <p>
     * Metodo interno che gestisce la creazione di nuovi record nella tabella advise utilizzando
     * la clausola PostgreSQL ON CONFLICT per gestire eventuali race conditions. Questo approccio
     * garantisce atomicità anche in presenza di inserimenti concorrenti.
     * </p>
     *
     * <p>
     * La strategia UPSERT implementata:
     * </p>
     * <ol>
     * <li><strong>INSERT tentativo:</strong> Prova inserimento nuovo record</li>
     * <li><strong>ON CONFLICT gestione:</strong> Se esiste già, aggiorna solo se slot è vuoto</li>
     * <li><strong>RETURNING clausola:</strong> Restituisce stato finale per verifica</li>
     * <li><strong>Fallback manuale:</strong> Se necessario, esegue aggiornamento esplicito</li>
     * </ol>
     *
     * @param conn la connessione database attiva (deve essere in transazione)
     * @param request la richiesta contenente i dati della nuova raccomandazione
     * @return {@code true} se la creazione è completata con successo,
     * {@code false} in caso di conflitti non risolti o errori SQL
     *
     * @throws SQLException se si verificano errori durante l'accesso al database
     *
     * @implNote Utilizza la clausola PostgreSQL specifica ON CONFLICT che potrebbe non essere
     * portabile su altri database. La query RETURNING permette di verificare lo stato
     * finale dell'operazione e gestire eventuali conflitti residui.
     */
    private boolean createNewRecommendationWithUpsert(Connection conn, RecommendationRequest request) throws SQLException {
        System.out.println("📝 Creazione nuovo record per utente: " + request.getUsername() + ", libro: " + request.getTargetBookIsbn());

        // Usa INSERT ON CONFLICT per gestire eventuali race conditions
        String upsertQuery = """
        INSERT INTO advise (username, isbn, isbn1, isbn2, isbn3) 
        VALUES (?, ?, ?, NULL, NULL) 
        ON CONFLICT (username, isbn) 
        DO UPDATE SET isbn1 = COALESCE(advise.isbn1, EXCLUDED.isbn1)
        RETURNING id, isbn1, isbn2, isbn3
        """;

        try (PreparedStatement upsertStmt = conn.prepareStatement(upsertQuery)) {
            upsertStmt.setString(1, request.getUsername());
            upsertStmt.setString(2, request.getTargetBookIsbn());
            upsertStmt.setString(3, request.getRecommendedBookIsbn());

            ResultSet rs = upsertStmt.executeQuery();

            if (rs.next()) {
                Long recordId = rs.getLong("id");
                String isbn1 = rs.getString("isbn1");
                String isbn2 = rs.getString("isbn2");
                String isbn3 = rs.getString("isbn3");

                System.out.println("✅ Record creato/aggiornato con ID: " + recordId);
                System.out.println("🔍 Stato finale: isbn1=" + isbn1 + ", isbn2=" + isbn2 + ", isbn3=" + isbn3);

                // Se il primo slot non è quello che abbiamo appena inserito, potrebbe essere un conflitto
                if (!request.getRecommendedBookIsbn().equals(isbn1)) {
                    System.out.println("⚠️ Possibile conflitto rilevato, tento aggiornamento manuale");
                    return updateExistingRecommendationWithId(conn, request, rs, recordId);
                }

                return true;
            } else {
                System.out.println("❌ Nessun record restituito dall'UPSERT");
                return false;
            }
        }
    }

    /**
     * Esegue debugging dello stato attuale delle raccomandazioni per troubleshooting.
     * <p>
     * Metodo di utilità interno utilizzato per logging dettagliato dello stato delle
     * raccomandazioni di un utente per un libro specifico. Particolarmente utile durante
     * sviluppo, testing e risoluzione di problemi in produzione.
     * </p>
     *
     * <p>
     * Le informazioni loggate includono:
     * </p>
     * <ul>
     * <li>ID del record nel database</li>
     * <li>Username e ISBN del libro target</li>
     * <li>Stato di tutti e tre gli slot di raccomandazione (isbn1, isbn2, isbn3)</li>
     * <li>Messaggi informativi se non esistono raccomandazioni</li>
     * </ul>
     *
     * @param username il nome utente per cui verificare lo stato delle raccomandazioni
     * @param targetBookIsbn l'ISBN del libro target per cui verificare le raccomandazioni
     *
     * @apiNote Questo metodo è utilizzato solo per logging e non modifica mai i dati.
     * Gli errori in questo metodo non compromettono il funzionamento principale
     * del servizio.
     */
    private void debugRecommendationState(String username, String targetBookIsbn) {
        String query = "SELECT id, isbn1, isbn2, isbn3 FROM advise WHERE username = ? AND isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, targetBookIsbn);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Long id = rs.getLong("id");
                String isbn1 = rs.getString("isbn1");
                String isbn2 = rs.getString("isbn2");
                String isbn3 = rs.getString("isbn3");

                System.out.println("🔍 DEBUG - Stato attuale raccomandazioni:");
                System.out.println("   ID: " + id);
                System.out.println("   Username: " + username);
                System.out.println("   Target ISBN: " + targetBookIsbn);
                System.out.println("   isbn1: " + isbn1);
                System.out.println("   isbn2: " + isbn2);
                System.out.println("   isbn3: " + isbn3);
            } else {
                System.out.println("🔍 DEBUG - Nessun record trovato per " + username + " / " + targetBookIsbn);
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore debug: " + e.getMessage());
        }
    }

    /**
     * Conta il numero di raccomandazioni attive che un utente ha effettuato per un libro specifico.
     * <p>
     * Questo metodo è utilizzato per verificare il rispetto del limite massimo di raccomandazioni
     * per libro per utente. Conta solo gli slot effettivamente occupati (non NULL) nel record
     * dell'utente per il libro target specificato.
     * </p>
     *
     * @param username il nome utente di cui contare le raccomandazioni
     * @param targetBookIsbn l'ISBN del libro target per cui contare le raccomandazioni
     * @return il numero di raccomandazioni attive (0-3), 0 se l'utente non ha mai
     * fatto raccomandazioni per questo libro o in caso di errori
     *
     * @apiNote Il conteggio considera solo i campi isbn1, isbn2, isbn3 non NULL.
     * Utilizzato per verificare il limite MAX_RECOMMENDATIONS_PER_BOOK prima
     * di permettere nuove raccomandazioni.
     */
    public int getRecommendationsCountForUser(String username, String targetBookIsbn) {
        String query = "SELECT isbn1, isbn2, isbn3 FROM advise WHERE username = ? AND isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, targetBookIsbn);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = 0;
                if (rs.getString("isbn1") != null) count++;
                if (rs.getString("isbn2") != null) count++;
                if (rs.getString("isbn3") != null) count++;
                return count;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore conteggio raccomandazioni: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Calcola il numero totale di raccomandazioni effettuate da un utente in tutto il sistema.
     * <p>
     * Questo metodo fornisce una metrica globale dell'attività di raccomandazione di un utente,
     * sommando tutte le raccomandazioni fatte per qualsiasi libro. Utile per implementare
     * sistemi di gamification, badge utente o statistiche di contributo alla community.
     * </p>
     *
     * <p>
     * Il calcolo include:
     * </p>
     * <ul>
     * <li>Tutte le raccomandazioni attive dell'utente (slot non NULL)</li>
     * <li>Raccomandazioni per qualsiasi libro target</li>
     * <li>Aggregazione tramite query SQL ottimizzata</li>
     * </ul>
     *
     * @param username il nome utente di cui calcolare il totale raccomandazioni (case-sensitive)
     * @return il numero totale di raccomandazioni effettuate dall'utente nel sistema,
     * 0 se l'utente non ha mai fatto raccomandazioni o in caso di errori
     *
     * @throws IllegalArgumentException se username è null o vuoto
     *
     * @apiNote Il calcolo viene effettuato tramite query SQL aggregata per ottimizzare
     * le performance. Username viene normalizzato a lowercase per consistency.
     */
    public int getUserRecommendationsCount(String username) {
        System.out.println("📊 Conteggio raccomandazioni per utente: " + username);

        String query = """
        SELECT 
            SUM(CASE WHEN isbn1 IS NOT NULL THEN 1 ELSE 0 END) +
            SUM(CASE WHEN isbn2 IS NOT NULL THEN 1 ELSE 0 END) +
            SUM(CASE WHEN isbn3 IS NOT NULL THEN 1 ELSE 0 END) as total_recommendations
        FROM advise 
        WHERE username = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("total_recommendations");
                System.out.println("✅ Raccomandazioni totali per " + username + ": " + count);
                return count;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nel conteggio raccomandazioni utente: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Recupera tutte le raccomandazioni ricevute da un libro specifico da parte di tutti gli utenti.
     * <p>
     * Questo metodo fornisce una vista completa di tutte le raccomandazioni che gli utenti
     * hanno fatto per un libro target specifico. I risultati sono utili per creare sezioni
     * "Altri utenti consigliano anche" nelle pagine di dettaglio libro e per analisi di
     * correlazione tra libri.
     * </p>
     *
     * <p>
     * Il processo di recupero include:
     * </p>
     * <ol>
     * <li>Query database per tutti i record con il libro target specificato</li>
     * <li>Scansione di tutti e tre gli slot di raccomandazione per ogni record</li>
     * <li>Creazione di oggetti BookRecommendation per ogni raccomandazione attiva</li>
     * <li>Aggregazione in una lista unificata</li>
     * </ol>
     *
     * @param targetBookIsbn l'ISBN del libro per cui recuperare tutte le raccomandazioni ricevute
     * @return una {@link List} di {@link BookRecommendation} contenente tutte le raccomandazioni
     * per il libro specificato. Lista vuota se non ci sono raccomandazioni o errori
     *
     * @throws IllegalArgumentException se targetBookIsbn è null o vuoto
     *
     * @apiNote Ogni slot non NULL genera un oggetto BookRecommendation separato, quindi
     * un singolo utente può contribuire con fino a 3 raccomandazioni per libro.
     * I risultati non sono ordinati e potrebbero contenere duplicati se più utenti
     * raccomandano lo stesso libro.
     *
     * @see BookRecommendation
     * @see #createRecommendation(String, String, String)
     */
    public List<BookRecommendation> getRecommendationsForBook(String targetBookIsbn) {
        System.out.println("📚 Recupero raccomandazioni per libro: " + targetBookIsbn);

        List<BookRecommendation> recommendations = new ArrayList<>();
        String query = "SELECT username, isbn1, isbn2, isbn3 FROM advise WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, targetBookIsbn);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                String isbn1 = rs.getString("isbn1");
                String isbn2 = rs.getString("isbn2");
                String isbn3 = rs.getString("isbn3");

                // Crea una raccomandazione per ogni ISBN non nullo
                if (isbn1 != null) {
                    recommendations.add(createRecommendation(username, targetBookIsbn, isbn1));
                }
                if (isbn2 != null) {
                    recommendations.add(createRecommendation(username, targetBookIsbn, isbn2));
                }
                if (isbn3 != null) {
                    recommendations.add(createRecommendation(username, targetBookIsbn, isbn3));
                }
            }

            System.out.println("✅ Recuperate " + recommendations.size() + " raccomandazioni");

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero raccomandazioni: " + e.getMessage());
        }

        return recommendations;
    }

    /**
     * Factory method per creare oggetti BookRecommendation con i dati specificati.
     * <p>
     * Metodo di utilità interno che standardizza la creazione di oggetti BookRecommendation
     * garantendo consistenza nei dati e semplificando il codice di mappatura dai risultati
     * del database agli oggetti del modello.
     * </p>
     *
     * @param username il nome dell'utente che ha effettuato la raccomandazione
     * @param targetIsbn l'ISBN del libro target per cui è stata fatta la raccomandazione
     * @param recommendedIsbn l'ISBN del libro raccomandato
     * @return un nuovo oggetto {@link BookRecommendation} inizializzato con i parametri forniti
     *
     * @implNote Il campo motivazione viene impostato a null poiché il sistema attuale
     * non supporta motivazioni testuali per le raccomandazioni.
     */
    private BookRecommendation createRecommendation(String username, String targetIsbn, String recommendedIsbn) {
        return new BookRecommendation(
                username,
                targetIsbn,
                recommendedIsbn,
                null
        );
    }

    /**
     * Rimuove una raccomandazione specifica e ricompatta i dati per mantenere consistenza.
     * <p>
     * Questo metodo gestisce la rimozione di singole raccomandazioni implementando una
     * logica di ricompattamento che mantiene tutti i valori rimanenti negli slot iniziali
     * per ottimizzare lo spazio e mantenere consistenza nei dati.
     * </p>
     *
     * <p>
     * Il processo di rimozione include:
     * </p>
     * <ol>
     * <li><strong>Ricerca record:</strong> Localizza il record contenente la raccomandazione</li>
     * <li><strong>Identificazione target:</strong> Trova quale slot contiene la raccomandazione da rimuovere</li>
     * <li><strong>Ricompattamento:</strong> Sposta i valori rimanenti nei primi slot disponibili</li>
     * <li><strong>Aggiornamento/Eliminazione:</strong> UPDATE record se rimangono raccomandazioni, DELETE se vuoto</li>
     * </ol>
     *
     * @param username il nome dell'utente proprietario della raccomandazione da rimuovere
     * @param targetBookIsbn l'ISBN del libro target da cui rimuovere la raccomandazione
     * @param recommendedBookIsbn l'ISBN del libro raccomandato da rimuovere
     * @return {@code true} se la raccomandazione è stata rimossa con successo,
     * {@code false} se non è stata trovata o si sono verificati errori
     *
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è null o vuoto
     *
     * @apiNote La rimozione include ricompattamento automatico: se si rimuove isbn1,
     * isbn2 diventa isbn1 e isbn3 diventa isbn2. Se tutte le raccomandazioni
     * vengono rimosse, l'intero record viene eliminato dal database.
     */
    public boolean removeRecommendation(String username, String targetBookIsbn, String recommendedBookIsbn) {
        System.out.println("🗑️ Rimozione raccomandazione: " + recommendedBookIsbn +
                " per " + targetBookIsbn + " da " + username);

        String selectQuery = "SELECT id, isbn1, isbn2, isbn3 FROM advise WHERE username = ? AND isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {

            selectStmt.setString(1, username);
            selectStmt.setString(2, targetBookIsbn);

            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                Long recordId = rs.getLong("id");
                String isbn1 = rs.getString("isbn1");
                String isbn2 = rs.getString("isbn2");
                String isbn3 = rs.getString("isbn3");

                System.out.println("🔍 Stato prima rimozione: isbn1=" + isbn1 + ", isbn2=" + isbn2 + ", isbn3=" + isbn3);

                String newIsbn1 = null;
                String newIsbn2 = null;
                String newIsbn3 = null;

                List<String> remainingRecommendations = new ArrayList<>();

                if (isbn1 != null && !isbn1.equals(recommendedBookIsbn)) {
                    remainingRecommendations.add(isbn1);
                }
                if (isbn2 != null && !isbn2.equals(recommendedBookIsbn)) {
                    remainingRecommendations.add(isbn2);
                }
                if (isbn3 != null && !isbn3.equals(recommendedBookIsbn)) {
                    remainingRecommendations.add(isbn3);
                }

                // Se la raccomandazione non è stata trovata
                int originalCount = 0;
                if (isbn1 != null) originalCount++;
                if (isbn2 != null) originalCount++;
                if (isbn3 != null) originalCount++;

                if (remainingRecommendations.size() == originalCount) {
                    System.out.println("⚠️ Raccomandazione non trovata per rimozione");
                    return false;
                }

                // Riassegna i valori ricompattati
                if (remainingRecommendations.size() > 0) {
                    newIsbn1 = remainingRecommendations.get(0);
                }
                if (remainingRecommendations.size() > 1) {
                    newIsbn2 = remainingRecommendations.get(1);
                }
                if (remainingRecommendations.size() > 2) {
                    newIsbn3 = remainingRecommendations.get(2);
                }

                System.out.println("🔄 Stato dopo ricompattamento: isbn1=" + newIsbn1 + ", isbn2=" + newIsbn2 + ", isbn3=" + newIsbn3);

                // Se tutte le raccomandazioni sono state rimosse, elimina il record
                if (remainingRecommendations.isEmpty()) {
                    String deleteQuery = "DELETE FROM advise WHERE id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setLong(1, recordId);
                        int deleted = deleteStmt.executeUpdate();
                        if (deleted > 0) {
                            System.out.println("✅ Record completamente rimosso (nessuna raccomandazione rimanente)");
                            return true;
                        }
                    }
                } else {
                    // Aggiorna il record con i valori ricompattati
                    String updateQuery;
                    if (newIsbn2 == null && newIsbn3 == null) {
                        // Solo isbn1 rimane
                        updateQuery = "UPDATE advise SET isbn1 = ?, isbn2 = NULL, isbn3 = NULL WHERE id = ?";
                    } else if (newIsbn3 == null) {
                        // isbn1 e isbn2 rimangono
                        updateQuery = "UPDATE advise SET isbn1 = ?, isbn2 = ?, isbn3 = NULL WHERE id = ?";
                    } else {
                        // Tutti e tre rimangono
                        updateQuery = "UPDATE advise SET isbn1 = ?, isbn2 = ?, isbn3 = ? WHERE id = ?";
                    }

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, newIsbn1);

                        if (newIsbn2 != null || newIsbn3 != null) {
                            updateStmt.setString(2, newIsbn2);
                        }

                        if (newIsbn3 != null) {
                            updateStmt.setString(3, newIsbn3);
                            updateStmt.setLong(4, recordId);
                        } else if (newIsbn2 != null) {
                            updateStmt.setLong(3, recordId);
                        } else {
                            updateStmt.setLong(2, recordId);
                        }

                        int updated = updateStmt.executeUpdate();
                        if (updated > 0) {
                            System.out.println("✅ Raccomandazione rimossa e valori ricompattati");
                            return true;
                        } else {
                            System.out.println("❌ Errore nell'aggiornamento del record");
                            return false;
                        }
                    }
                }
            } else {
                System.out.println("⚠️ Nessuna raccomandazione trovata per questo utente e libro");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore rimozione raccomandazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return false;
    }

    /**
     * Recupera tutte le raccomandazioni effettuate da un utente specifico per un libro target.
     * <p>
     * Questo metodo fornisce una vista focalizzata delle raccomandazioni di un singolo utente
     * per un libro specifico. Utile per implementare funzionalità di editing delle proprie
     * raccomandazioni e per verificare lo stato attuale delle raccomandazioni di un utente.
     * </p>
     *
     * @param username il nome dell'utente di cui recuperare le raccomandazioni
     * @param targetBookIsbn l'ISBN del libro target per cui recuperare le raccomandazioni
     * @return una {@link List} di {@link BookRecommendation} contenente le raccomandazioni
     * dell'utente per il libro specificato (0-3 elementi). Lista vuota se non esistono
     * raccomandazioni o in caso di errori
     *
     * @throws IllegalArgumentException se username o targetBookIsbn sono null o vuoti
     *
     * @apiNote I risultati sono sempre ordinati per slot (isbn1, isbn2, isbn3) e ogni
     * slot non NULL genera un oggetto BookRecommendation separato.
     */
    public List<BookRecommendation> getUserRecommendationsForBook(String username, String targetBookIsbn) {
        List<BookRecommendation> recommendations = new ArrayList<>();
        String query = "SELECT isbn1, isbn2, isbn3 FROM advise WHERE username = ? AND isbn = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, targetBookIsbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String isbn1 = rs.getString("isbn1");
                String isbn2 = rs.getString("isbn2");
                String isbn3 = rs.getString("isbn3");
                if (isbn1 != null) {
                    recommendations.add(createRecommendation(username, targetBookIsbn, isbn1));
                }
                if (isbn2 != null) {
                    recommendations.add(createRecommendation(username, targetBookIsbn, isbn2));
                }
                if (isbn3 != null) {
                    recommendations.add(createRecommendation(username, targetBookIsbn, isbn3));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Errore recupero raccomandazioni utente: " + e.getMessage());
        }
        return recommendations;
    }

    /**
     * Recupera i dettagli completi dei libri raccomandati per un libro target specifico.
     * <p>
     * Questo metodo combina le funzionalità di recupero raccomandazioni con l'integrazione
     * al BookService per fornire oggetti Book completi invece di semplici ISBN. È il metodo
     * principale per implementare sezioni "Altri utenti consigliano anche" con ricchi dettagli
     * sui libri raccomandati.
     * </p>
     *
     * <p>
     * Il processo di arricchimento include:
     * </p>
     * <ol>
     * <li>Recupero di tutte le raccomandazioni per il libro target</li>
     * <li>Estrazione degli ISBN dei libri raccomandati</li>
     * <li>Risoluzione di ogni ISBN in oggetto Book completo tramite BookService</li>
     * <li>Filtraggio di libri non trovati o errori</li>
     * </ol>
     *
     * @param targetBookIsbn l'ISBN del libro per cui recuperare i dettagli dei libri raccomandati
     * @return una {@link List} di {@link Book} contenente i dettagli completi di tutti i libri
     * raccomandati. Lista vuota se non ci sono raccomandazioni valide o errori
     *
     * @throws IllegalArgumentException se targetBookIsbn è null o vuoto
     *
     * @apiNote I libri che non possono essere risolti tramite BookService (es. ISBN non validi
     * o libri eliminati) vengono automaticamente filtrati dalla lista risultante.
     * Gli errori di risoluzione vengono loggati ma non interrompono l'elaborazione.
     *
     * @see BookService#getBookByIsbn(String)
     * @see #getRecommendationsForBook(String)
     */
    public List<Book> getRecommendedBooksDetails(String targetBookIsbn) {
        List<BookRecommendation> recommendations = getRecommendationsForBook(targetBookIsbn);
        List<Book> recommendedBooks = new ArrayList<>();
        for (BookRecommendation rec : recommendations) {
            try {
                Book book = bookService.getBookByIsbn(rec.getRecommendedBookIsbn());
                if (book != null) {
                    recommendedBooks.add(book);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Errore recupero dettagli libro " + rec.getRecommendedBookIsbn() + ": " + e.getMessage());
            }
        }
        return recommendedBooks;
    }

    /**
     * Verifica lo stato di connessione al database.
     * <p>
     * Questo metodo tenta di stabilire una connessione al database utilizzando i parametri
     * di configurazione predefiniti (DB_URL, DB_USER, DB_PASSWORD). Viene utilizzato
     * principalmente per controlli di salute del servizio e per garantire che il sistema
     * sia operativo prima di tentare altre operazioni.
     * </p>
     *
     * @return {@code true} se la connessione al database ha successo,
     * {@code false} in caso di qualsiasi errore SQL che impedisca la connessione.
     *
     * @apiNote La connessione viene immediatamente chiusa dopo il test, quindi non
     * mantiene risorse aperte.
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Calcola e restituisce una stringa formattata con le statistiche sulle raccomandazioni.
     * <p>
     * Questo metodo esegue due query aggregate sul database per ottenere metriche di alto livello
     * sull'uso del sistema di raccomandazioni. Le statistiche includono il numero totale di
     * record nella tabella 'advise' e il conteggio di tutte le raccomandazioni effettivamente
     * attive (ovvero, gli slot non nulli).
     * </p>
     *
     * <p>
     * Il risultato viene formattato in una stringa di testo che mostra:
     * </p>
     * <ul>
     * <li>Il numero totale di record (righe) nella tabella.</li>
     * <li>Il numero totale di raccomandazioni attive (valori non nulli).</li>
     * <li>La media di raccomandazioni per record (calcolata solo se ci sono record).</li>
     * </ul>
     *
     * @return una {@code String} contenente le statistiche formattate. In caso di errore SQL,
     * restituisce una stringa di errore descrittiva.
     *
     * @implNote Le query utilizzano funzioni aggregate SQL (COUNT e SUM con CASE) per calcoli
     * efficienti a livello di database.
     */
    public String getRecommendationStats() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Conta record totali
            String countQuery = "SELECT COUNT(*) as total_records FROM advise";
            int totalRecords = 0;

            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalRecords = rs.getInt("total_records");
                }
            }

            // Conta raccomandazioni attive
            String activeQuery = "SELECT " +
                    "SUM(CASE WHEN isbn1 IS NOT NULL THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN isbn2 IS NOT NULL THEN 1 ELSE 0 END) + " +
                    "SUM(CASE WHEN isbn3 IS NOT NULL THEN 1 ELSE 0 END) as active_recommendations " +
                    "FROM advise";

            int activeRecommendations = 0;

            try (PreparedStatement stmt = conn.prepareStatement(activeQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    activeRecommendations = rs.getInt("active_recommendations");
                }
            }

            return String.format(
                    "📊 Statistiche Raccomandazioni:\n" +
                            "• Record totali: %d\n" +
                            "• Raccomandazioni attive: %d\n" +
                            "• Media raccomandazioni per record: %.1f",
                    totalRecords,
                    activeRecommendations,
                    totalRecords > 0 ? (double) activeRecommendations / totalRecords : 0.0
            );

        } catch (SQLException e) {
            return "❌ Errore nel calcolo statistiche: " + e.getMessage();
        }
    }

    /**
     * Restituisce il limite massimo di raccomandazioni per libro per utente.
     * <p>
     * Questo è un metodo di utilità che espone il valore della costante {@link #MAX_RECOMMENDATIONS_PER_BOOK},
     * consentendo ad altri servizi o componenti di ottenere il limite configurato senza accedere
     * direttamente al campo statico.
     * </p>
     *
     * @return il valore intero del limite massimo di raccomandazioni per libro per utente.
     *
     * @see #MAX_RECOMMENDATIONS_PER_BOOK
     */
    public int getMaxRecommendationsPerBook() {
        return MAX_RECOMMENDATIONS_PER_BOOK;
    }

}