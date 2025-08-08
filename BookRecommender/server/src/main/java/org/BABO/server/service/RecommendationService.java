package org.BABO.server.service;

import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.model.Book;
import org.BABO.shared.dto.RecommendationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per gestire le operazioni sulle raccomandazioni di libri
 * Utilizza la tabella 'advise' esistente nel database
 */
@Service
public class RecommendationService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";
    private static final int MAX_RECOMMENDATIONS_PER_BOOK = 3;

    @Autowired
    private BookService bookService;

    @Autowired
    private LibraryService libraryService;

    /**
     * Verifica se un utente può consigliare libri per un determinato ISBN
     * L'utente deve avere il libro target in una delle sue librerie
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
     * Aggiunge una raccomandazione
     */
    /**
     * Aggiunge una raccomandazione - VERSIONE CON DEBUG
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
     * Inserisce la raccomandazione nella tabella advise
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
     * Aggiorna un record esistente aggiungendo una raccomandazione
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
            System.out.println("⚠️ Raccomandazione già esistente per ISBN: " + request.getRecommendedBookIsbn());
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
     * Crea un nuovo record nella tabella advise
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
     * Metodo helper per debug - verifica stato attuale delle raccomandazioni
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
     * Recupera il numero di raccomandazioni che un utente ha fatto per un libro
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
     * Ottieni il numero totale di raccomandazioni fatte da un utente
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
     * Recupera tutte le raccomandazioni per un libro
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
     * Crea un oggetto BookRecommendation
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
     * Rimuove una raccomandazione specifica
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

                // CORREZIONE: Ricompatta i valori invece di impostare NULL
                String newIsbn1 = null;
                String newIsbn2 = null;
                String newIsbn3 = null;

                // Rimuovi la raccomandazione target e ricompatta
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
     * Rimuove il record se tutte le raccomandazioni sono vuote
     */
    private void cleanupEmptyRecommendationById(Connection conn, Long recordId) {
        try {
            String selectQuery = "SELECT isbn1, isbn2, isbn3 FROM advise WHERE id = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setLong(1, recordId);

                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    String isbn1 = rs.getString("isbn1");
                    String isbn2 = rs.getString("isbn2");
                    String isbn3 = rs.getString("isbn3");

                    // Se tutti gli ISBN sono null, elimina il record
                    if (isbn1 == null && isbn2 == null && isbn3 == null) {
                        String deleteQuery = "DELETE FROM advise WHERE id = ?";

                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                            deleteStmt.setLong(1, recordId);

                            int deleted = deleteStmt.executeUpdate();
                            if (deleted > 0) {
                                System.out.println("🧹 Record vuoto eliminato (ID: " + recordId + ")");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Errore cleanup record vuoto: " + e.getMessage());
        }
    }

    /**
     * Recupera le raccomandazioni fatte da un utente per un libro specifico
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
     * Recupera i dettagli dei libri consigliati per un libro target
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
     * Verifica se il database è disponibile
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Statistiche raccomandazioni (per admin o analytics)
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
     * Ottiene il limite massimo di raccomandazioni per libro
     */
    public int getMaxRecommendationsPerBook() {
        return MAX_RECOMMENDATIONS_PER_BOOK;
    }
}