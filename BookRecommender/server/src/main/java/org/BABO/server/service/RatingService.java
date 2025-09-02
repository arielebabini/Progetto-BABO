package org.BABO.server.service;

import org.BABO.shared.dto.Rating.RatingResponse;
import org.BABO.shared.model.BookRating;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione completa delle valutazioni e recensioni dei libri.
 * <p>
 * Questo servizio orchestra tutte le operazioni relative ai feedback degli utenti sui libri,
 * includendo l'inserimento, l'aggiornamento, il recupero e l'analisi aggregata delle valutazioni.
 * Interagisce direttamente con il database PostgreSQL per garantire la persistenza e l'integrità dei dati.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 * <li><strong>Gestione Valutazioni:</strong> CRUD completo per le valutazioni degli utenti (stile, contenuto, piacevolezza, etc.).</li>
 * <li><strong>Sistema di Recensioni:</strong> Permette agli utenti di associare un testo di recensione alla valutazione numerica.</li>
 * <li><strong>Calcoli Statistici:</strong> Fornisce medie, distribuzioni e statistiche dettagliate per ogni libro.</li>
 * <li><strong>Query Complesse:</strong> Recupera dati aggregati come i libri più votati o con la media migliore.</li>
 * <li><strong>Integrità dei Dati:</strong> Assicura che ogni utente possa inserire una sola valutazione per libro, gestendo l'aggiornamento.</li>
 * <li><strong>Gestione Amministrativa:</strong> Include metodi per la moderazione delle recensioni e il monitoraggio del sistema.</li>
 * </ul>
 *
 * <h3>Architettura Database:</h3>
 * <ul>
 * <li><strong>assessment:</strong> Tabella principale che memorizza ogni singola valutazione e recensione, collegando utente e libro.</li>
 * <li><strong>Primary Key Composita:</strong> Utilizza la coppia (username, isbn) per garantire l'unicità delle valutazioni.</li>
 * <li><strong>Timestamping:</strong> Registra automaticamente la data e l'ora di ogni inserimento o modifica.</li>
 * </ul>
 *
 * <h3>Logica di Business:</h3>
 * <ul>
 * <li>Normalizzazione degli input (es. username in minuscolo) per consistenza.</li>
 * <li>Gestione dei valori nulli per campi opzionali come le recensioni.</li>
 * <li>Arrotondamento dei valori medi per una presentazione pulita.</li>
 * </ul>
 *
 * @author BABO Development Team
 * @version 1.1.0
 * @since 1.0.0
 * @see BookRating
 * @see RatingResponse
 */
@Service
public class RatingService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Aggiunge una nuova valutazione o aggiorna una esistente per un libro da parte di un utente.
     * <p>
     * Questo metodo agisce come un "upsert": verifica prima se l'utente ha già valutato il libro.
     * In caso affermativo, aggiorna la valutazione esistente; altrimenti, ne inserisce una nuova.
     * </p>
     *
     * @param rating L'oggetto {@link BookRating} contenente tutti i dati della valutazione.
     * @return {@code true} se l'operazione di inserimento o aggiornamento ha avuto successo, {@code false} altrimenti.
     * @since 1.0.0
     */
    public boolean addOrUpdateRating(BookRating rating) {
        System.out.println("⭐ Aggiunta/aggiornamento valutazione per ISBN: " + rating.getIsbn() + " da: " + rating.getUsername());

        if (!rating.isComplete()) {
            System.out.println("❌ Valutazione incompleta: " + rating);
            return false;
        }

        if (ratingExists(rating.getUsername(), rating.getIsbn())) {
            return updateExistingRating(rating);
        } else {
            return insertNewRating(rating);
        }
    }

    /**
     * Recupera la valutazione specifica fornita da un utente per un determinato libro.
     *
     * @param username L'identificativo dell'utente che ha effettuato la valutazione.
     * @param isbn L'ISBN del libro di cui si desidera recuperare la valutazione.
     * @return Un oggetto {@link BookRating} se una valutazione esiste, altrimenti {@code null}.
     * @since 1.0.0
     */
    public BookRating getRatingByUserAndBook(String username, String isbn) {
        System.out.println("🔍 Ricerca valutazione per utente: " + username + " e ISBN: " + isbn);

        String query = """
            SELECT username, isbn, data, style, content, pleasantness, originality, edition, average, review
            FROM assessment 
            WHERE username = ? AND isbn = ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, isbn.trim());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BookRating rating = mapResultSetToRating(rs);
                System.out.println("✅ Valutazione trovata: " + rating.getDisplayRating());
                return rating;
            } else {
                System.out.println("📝 Nessuna valutazione trovata per questo utente e libro");
                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero valutazione: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera tutte le valutazioni e recensioni associate a un libro specifico.
     * <p>
     * I risultati sono ordinati per data, mostrando per prime le valutazioni più recenti.
     * </p>
     *
     * @param isbn L'ISBN del libro per cui recuperare le valutazioni.
     * @return Una lista di oggetti {@link BookRating}; la lista sarà vuota se non ci sono valutazioni.
     * @since 1.0.0
     */
    public List<BookRating> getRatingsForBook(String isbn) {
        System.out.println("📊 Recupero tutte le valutazioni per ISBN: " + isbn);

        List<BookRating> ratings = new ArrayList<>();
        String query = """
            SELECT username, isbn, data, style, content, pleasantness, originality, edition, average, review
            FROM assessment 
            WHERE isbn = ?
            ORDER BY data DESC
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BookRating rating = mapResultSetToRating(rs);
                ratings.add(rating);
            }

            System.out.println("✅ Recuperate " + ratings.size() + " valutazioni per il libro");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero valutazioni libro: " + e.getMessage());
            e.printStackTrace();
        }

        return ratings;
    }

    /**
     * Recupera l'intera cronologia delle valutazioni effettuate da un singolo utente.
     * <p>
     * Utile per visualizzare il profilo di un utente e le sue attività di recensione.
     * I risultati sono ordinati per data decrescente.
     * </p>
     *
     * @param username L'identificativo dell'utente.
     * @return Una lista di {@link BookRating} che rappresenta tutte le valutazioni dell'utente.
     * @since 1.0.0
     */
    public List<BookRating> getUserRatings(String username) {
        System.out.println("👤 Recupero tutte le valutazioni dell'utente: " + username);

        List<BookRating> ratings = new ArrayList<>();
        String query = """
            SELECT username, isbn, data, style, content, pleasantness, originality, edition, average, review
            FROM assessment 
            WHERE username = ?
            ORDER BY data DESC
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BookRating rating = mapResultSetToRating(rs);
                ratings.add(rating);
            }

            System.out.println("✅ Recuperate " + ratings.size() + " valutazioni dell'utente");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero valutazioni utente: " + e.getMessage());
            e.printStackTrace();
        }

        return ratings;
    }

    /**
     * Elimina permanentemente la valutazione di un utente per un libro.
     *
     * @param username L'utente che ha creato la valutazione.
     * @param isbn L'ISBN del libro associato alla valutazione.
     * @return {@code true} se la valutazione è stata eliminata con successo, {@code false} altrimenti.
     * @since 1.0.0
     */
    public boolean deleteRating(String username, String isbn) {
        System.out.println("🗑️ Eliminazione valutazione per utente: " + username + " e ISBN: " + isbn);

        String query = "DELETE FROM assessment WHERE username = ? AND isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, isbn.trim());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Valutazione eliminata con successo");
                return true;
            } else {
                System.out.println("❌ Nessuna valutazione trovata da eliminare");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'eliminazione valutazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calcola il punteggio medio complessivo per un libro basato su tutte le valutazioni ricevute.
     * <p>
     * Il risultato è arrotondato a due cifre decimali per una migliore leggibilità.
     * </p>
     *
     * @param isbn L'ISBN del libro di cui calcolare la media.
     * @return Un {@link Double} rappresentante la media, o {@code null} se non ci sono valutazioni.
     * @since 1.0.0
     */
    public Double getAverageRatingForBook(String isbn) {
        System.out.println("📊 Calcolo media valutazioni per ISBN: " + isbn);

        String query = "SELECT AVG(average) as avg_rating FROM assessment WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating");
                if (!rs.wasNull()) {
                    double roundedAvg = Math.round(avgRating * 100.0) / 100.0;
                    System.out.println("✅ Media calcolata: " + roundedAvg);
                    return roundedAvg;
                }
            }

            System.out.println("📝 Nessuna valutazione trovata per calcolare la media");
            return null;

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il calcolo media: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fornisce un report statistico completo per un libro.
     * <p>
     * Include la media totale, la distribuzione delle valutazioni (da 1 a 5 stelle),
     * le medie per ogni categoria (stile, contenuto, etc.) e la lista di tutte le recensioni.
     * </p>
     *
     * @param isbn L'ISBN del libro per cui generare le statistiche.
     * @return Un oggetto {@link RatingResponse} contenente tutte le statistiche aggregate.
     * @since 1.0.0
     */
    public RatingResponse getBookRatingStatistics(String isbn) {
        System.out.println("📈 Calcolo statistiche complete per ISBN: " + isbn);

        List<BookRating> ratings = getRatingsForBook(isbn);

        if (ratings.isEmpty()) {
            return new RatingResponse(true, "Nessuna valutazione trovata", ratings, 0.0);
        }

        double totalAverage = 0.0;
        int[] starDistribution = new int[5];
        double totalStyle = 0, totalContent = 0, totalPleasantness = 0, totalOriginality = 0, totalEdition = 0;

        for (BookRating rating : ratings) {
            totalAverage += rating.getAverage();
            int stars = rating.getStarRating();
            if (stars >= 1 && stars <= 5) {
                starDistribution[stars - 1]++;
            }
            totalStyle += rating.getStyle();
            totalContent += rating.getContent();
            totalPleasantness += rating.getPleasantness();
            totalOriginality += rating.getOriginality();
            totalEdition += rating.getEdition();
        }

        double averageRating = Math.round((totalAverage / ratings.size()) * 100.0) / 100.0;
        RatingResponse.RatingBreakdown breakdown = new RatingResponse.RatingBreakdown(
                starDistribution[4], starDistribution[3], starDistribution[2], starDistribution[1], starDistribution[0]);
        breakdown.setAverageStyle(Math.round((totalStyle / ratings.size()) * 100.0) / 100.0);
        breakdown.setAverageContent(Math.round((totalContent / ratings.size()) * 100.0) / 100.0);
        breakdown.setAveragePleasantness(Math.round((totalPleasantness / ratings.size()) * 100.0) / 100.0);
        breakdown.setAverageOriginality(Math.round((totalOriginality / ratings.size()) * 100.0) / 100.0);
        breakdown.setAverageEdition(Math.round((totalEdition / ratings.size()) * 100.0) / 100.0);

        System.out.println("✅ Statistiche calcolate: media " + averageRating + " su " + ratings.size() + " valutazioni");

        RatingResponse response = new RatingResponse(true, "Statistiche recuperate", ratings, averageRating, breakdown);
        response.setTotalRatings(ratings.size());

        return response;
    }

    /**
     * Verifica la disponibilità della connessione al database.
     *
     * @return {@code true} se il database è accessibile, {@code false} altrimenti.
     * @since 1.0.0
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database non disponibile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ottiene il conteggio totale di tutte le valutazioni presenti nel sistema.
     * <p>
     * Utile per scopi di monitoraggio e statistiche generali dell'applicazione.
     * </p>
     *
     * @return Il numero totale di valutazioni nel database.
     * @since 1.0.0
     */
    public int getTotalRatingsCount() {
        String query = "SELECT COUNT(*) as total FROM assessment";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nel conteggio valutazioni: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Recupera una lista dei libri che hanno ricevuto il maggior numero di valutazioni.
     * <p>
     * Questo metodo è utile per creare classifiche di popolarità ("libri più discussi").
     * Restituisce una lista formattata dei 10 libri con più valutazioni.
     * </p>
     *
     * @return Una lista di stringhe, dove ogni stringa contiene l'ISBN e il numero di valutazioni.
     * @since 1.0.0
     */
    public List<String> getMostRatedBooks() {
        List<String> topBooks = new ArrayList<>();
        String query = """
            SELECT isbn, COUNT(*) as rating_count
            FROM assessment 
            GROUP BY isbn 
            ORDER BY rating_count DESC 
            LIMIT 10
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                int count = rs.getInt("rating_count");
                topBooks.add(isbn + " (" + count + " valutazioni)");
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nel recupero libri più valutati: " + e.getMessage());
        }

        return topBooks;
    }

    /**
     * Esegue un test diagnostico sulla tabella `assessment` del database.
     * <p>
     * Verifica l'esistenza della tabella, conta i record totali e ne elenca la struttura delle colonne.
     * È uno strumento utile per il debug e la manutenzione.
     * </p>
     *
     * @return Una stringa formattata con il report del test.
     * @since 1.0.0
     */
    public String testAssessmentTable() {
        StringBuilder result = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            result.append("🔍 Test tabella assessment:\n\n");
            String checkTable = "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'assessment'";
            try (PreparedStatement stmt = conn.prepareStatement(checkTable)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    result.append("📋 Tabella assessment: ").append(rs.getInt("count") > 0 ? "✅ Esiste" : "❌ Non esiste").append("\n");
                }
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM assessment")) {
                if (rs.next()) {
                    result.append("📊 Record totali: ").append(rs.getInt("count")).append("\n");
                }
            } catch (SQLException e) {
                result.append("❌ Errore accesso tabella assessment\n");
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'assessment' ORDER BY ordinal_position")) {
                result.append("\n📝 Struttura colonne:\n");
                while (rs.next()) {
                    result.append("  - ").append(rs.getString("column_name")).append(" (").append(rs.getString("data_type")).append(")\n");
                }
            }
        } catch (SQLException e) {
            result.append("❌ Errore connessione database: ").append(e.getMessage());
        }
        return result.toString();
    }

    /**
     * Recupera una lista dei libri con la valutazione media più alta.
     * <p>
     * Filtra solo i libri con almeno 2 valutazioni per assicurare rilevanza statistica.
     * Utile per classifiche di qualità ("libri migliori").
     * </p>
     *
     * @return Una lista di stringhe formattate con ISBN, media e numero di valutazioni.
     * @since 1.0.0
     */
    public List<String> getBestRatedBooks() {
        List<String> topBooks = new ArrayList<>();
        String query = """
            SELECT isbn, AVG(average) as avg_rating, COUNT(*) as rating_count
            FROM assessment 
            GROUP BY isbn 
            HAVING COUNT(*) >= 2
            ORDER BY avg_rating DESC
            LIMIT 10
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                double avgRating = rs.getDouble("avg_rating");
                int count = rs.getInt("rating_count");
                topBooks.add(isbn + " (" + String.format("%.1f", avgRating) + "★, " + count + " valutazioni)");
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nel recupero libri meglio valutati: " + e.getMessage());
        }

        return topBooks;
    }

    /**
     * Recupera gli ISBN dei libri con la valutazione media più alta.
     * <p>
     * Filtra i libri che hanno ricevuto un numero minimo di valutazioni (almeno 2) per garantire
     * la rilevanza statistica. I risultati sono ordinati per media decrescente.
     * </p>
     *
     * @param limit Il numero massimo di ISBN da restituire.
     * @return Una lista di stringhe contenente gli ISBN dei libri meglio valutati.
     * @since 1.0.0
     */
    public List<String> getBestRatedBooksIsbn(int limit) {
        System.out.println("⭐ Recupero " + limit + " ISBN libri meglio valutati");

        List<String> topIsbnList = new ArrayList<>();
        String query = """
        SELECT isbn, AVG(average) as avg_rating, COUNT(*) as rating_count
        FROM assessment 
        WHERE average IS NOT NULL AND average > 0
        GROUP BY isbn
        HAVING COUNT(*) >= 2
        ORDER BY avg_rating DESC, rating_count DESC
        LIMIT ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                topIsbnList.add(rs.getString("isbn"));
            }
            System.out.println("✅ Recuperati " + topIsbnList.size() + " ISBN meglio valutati");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero ISBN meglio valutati: " + e.getMessage());
            e.printStackTrace();
        }

        return topIsbnList;
    }

    /**
     * Conta il numero totale di valutazioni (con o senza recensione) fatte da un utente.
     *
     * @param username L'utente di cui contare le valutazioni.
     * @return Il numero totale di valutazioni.
     * @since 1.0.0
     */
    public int getUserRatingsCount(String username) {
        System.out.println("📊 Conteggio recensioni per utente: " + username);

        String query = "SELECT COUNT(*) as total_ratings FROM assessment WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_ratings");
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nel conteggio recensioni utente: " + e.getMessage());
        }

        return 0;
    }

    /**
     * ===============================
     * METODI ADMIN PER GESTIONE RECENSIONI
     * ===============================
     */

    /**
     * Elimina il testo di una recensione specifica, mantenendo la valutazione numerica.
     * <p>
     * Metodo di moderazione. <strong>Attenzione:</strong> l'implementazione attuale ignora il parametro {@code reviewId}
     * e cancella la recensione più recente del database. Da usare con cautela.
     * </p>
     *
     * @param reviewId L'ID della recensione da eliminare (attualmente non utilizzato).
     * @return {@code true} se una recensione è stata eliminata con successo, {@code false} altrimenti.
     * @since 1.1.0
     */
    public boolean deleteReview(Long reviewId) {
        System.out.println("🗑️ Eliminazione recensione ID: " + reviewId);

        String query = "UPDATE assessment SET review = NULL WHERE id = (SELECT id FROM assessment WHERE review IS NOT NULL ORDER BY data DESC LIMIT 1)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            int result = stmt.executeUpdate(query);
            if (result > 0) {
                System.out.println("✅ Recensione eliminata con successo");
                return true;
            } else {
                System.out.println("❌ Nessuna recensione trovata da eliminare");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'eliminazione recensione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina il testo di tutte le recensioni di un utente specifico.
     * <p>
     * Utile per la moderazione di massa in caso di abusi. Le valutazioni numeriche dell'utente
     * vengono preservate.
     * </p>
     *
     * @param username L'utente le cui recensioni testuali devono essere rimosse.
     * @return Il numero di recensioni eliminate.
     * @since 1.1.0
     */
    public int deleteAllUserReviews(String username) {
        System.out.println("🚫 Eliminazione tutte recensioni utente: " + username);

        String query = "UPDATE assessment SET review = NULL WHERE username = ? AND review IS NOT NULL";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            int result = stmt.executeUpdate();
            System.out.println("✅ Eliminate " + result + " recensioni dell'utente " + username);
            return result;

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'eliminazione recensioni utente: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Recupera tutte le valutazioni presenti nel database.
     * <p>
     * Metodo per uso amministrativo, utile per backup, migrazioni o analisi complete.
     * Attenzione all'uso su database di grandi dimensioni per evitare un consumo eccessivo di memoria.
     * </p>
     *
     * @return Una lista contenente tutti gli oggetti {@link BookRating} del sistema.
     * @since 1.1.0
     */
    public List<BookRating> getAllRatings() {
        System.out.println("📋 Recupero di tutte le valutazioni per admin");

        List<BookRating> allRatings = new ArrayList<>();
        String query = """
            SELECT username, isbn, data, style, content, pleasantness, originality, edition, average, review
            FROM assessment 
            ORDER BY data DESC
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                allRatings.add(mapResultSetToRating(rs));
            }
            System.out.println("✅ Recuperate " + allRatings.size() + " valutazioni totali");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero di tutte le valutazioni: " + e.getMessage());
            e.printStackTrace();
        }

        return allRatings;
    }

    /**
     * ===============================
     * METODI PRIVATI DI SUPPORTO
     * ===============================
     */

    /**
     * Inserisce una nuova valutazione nel database.
     *
     * @param rating L'oggetto {@link BookRating} da persistere.
     * @return {@code true} se l'inserimento ha successo, {@code false} altrimenti.
     */
    private boolean insertNewRating(BookRating rating) {
        String query = """
        INSERT INTO assessment (username, isbn, data, style, content, pleasantness, originality, edition, average, review)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, rating.getUsername().toLowerCase().trim());
            stmt.setString(2, rating.getIsbn().trim());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, rating.getStyle());
            stmt.setInt(5, rating.getContent());
            stmt.setInt(6, rating.getPleasantness());
            stmt.setInt(7, rating.getOriginality());
            stmt.setInt(8, rating.getEdition());
            stmt.setDouble(9, rating.getAverage());
            if (rating.getReview() != null && !rating.getReview().trim().isEmpty()) {
                stmt.setString(10, rating.getReview().trim());
            } else {
                stmt.setNull(10, java.sql.Types.VARCHAR);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'inserimento valutazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Aggiorna una valutazione esistente nel database.
     *
     * @param rating L'oggetto {@link BookRating} con i dati aggiornati.
     * @return {@code true} se l'aggiornamento ha successo, {@code false} altrimenti.
     */
    private boolean updateExistingRating(BookRating rating) {
        String query = """
        UPDATE assessment 
        SET data = ?, style = ?, content = ?, pleasantness = ?, originality = ?, edition = ?, average = ?, review = ?
        WHERE username = ? AND isbn = ?
    """;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, rating.getStyle());
            stmt.setInt(3, rating.getContent());
            stmt.setInt(4, rating.getPleasantness());
            stmt.setInt(5, rating.getOriginality());
            stmt.setInt(6, rating.getEdition());
            stmt.setDouble(7, rating.getAverage());
            if (rating.getReview() != null && !rating.getReview().trim().isEmpty()) {
                stmt.setString(8, rating.getReview().trim());
            } else {
                stmt.setNull(8, java.sql.Types.VARCHAR);
            }
            stmt.setString(9, rating.getUsername().toLowerCase().trim());
            stmt.setString(10, rating.getIsbn().trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'aggiornamento valutazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Controlla se un utente ha già lasciato una valutazione per un libro.
     *
     * @param username L'identificativo dell'utente.
     * @param isbn L'ISBN del libro.
     * @return {@code true} se esiste già una valutazione, {@code false} altrimenti.
     */
    private boolean ratingExists(String username, String isbn) {
        String query = "SELECT 1 FROM assessment WHERE username = ? AND isbn = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, isbn.trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("❌ Errore verifica esistenza valutazione: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mappa una riga di un {@link ResultSet} a un oggetto {@link BookRating}.
     *
     * @param rs Il ResultSet proveniente da una query sulla tabella 'assessment'.
     * @return Un oggetto {@link BookRating} popolato con i dati.
     * @throws SQLException Se si verifica un errore durante l'accesso ai dati del ResultSet.
     */
    private BookRating mapResultSetToRating(ResultSet rs) throws SQLException {
        BookRating rating = new BookRating();
        rating.setUsername(rs.getString("username"));
        rating.setIsbn(rs.getString("isbn"));
        Timestamp timestamp = rs.getTimestamp("data");
        if (timestamp != null) {
            rating.setData(timestamp.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        rating.setStyle(rs.getInt("style"));
        rating.setContent(rs.getInt("content"));
        rating.setPleasantness(rs.getInt("pleasantness"));
        rating.setOriginality(rs.getInt("originality"));
        rating.setEdition(rs.getInt("edition"));
        double average = rs.getDouble("average");
        if (!rs.wasNull()) {
            rating.setAverage(average);
        }
        String review = rs.getString("review");
        if (review != null && !review.trim().isEmpty()) {
            rating.setReview(review.trim());
        }
        return rating;
    }
}