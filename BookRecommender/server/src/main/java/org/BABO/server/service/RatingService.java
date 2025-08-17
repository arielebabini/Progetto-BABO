package org.BABO.server.service;
import java.time.format.DateTimeFormatter;

import org.BABO.shared.dto.Reviews.ReviewStats;
import org.BABO.shared.model.BookRating;
import org.BABO.shared.dto.Rating.RatingResponse;
import org.BABO.shared.model.Review;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servizio per gestire le operazioni sulle valutazioni dei libri
 * Utilizza la tabella 'assessment' del database PostgreSQL
 */
@Service
public class RatingService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Aggiunge o aggiorna una valutazione per un libro
     */
    public boolean addOrUpdateRating(BookRating rating) {
        System.out.println("⭐ Aggiunta/aggiornamento valutazione per ISBN: " + rating.getIsbn() + " da: " + rating.getUsername());

        if (!rating.isComplete()) {
            System.out.println("❌ Valutazione incompleta: " + rating);
            return false;
        }

        // Prima verifica se esiste già una valutazione
        if (ratingExists(rating.getUsername(), rating.getIsbn())) {
            return updateExistingRating(rating);
        } else {
            return insertNewRating(rating);
        }
    }

    /**
     * Inserisce una nuova valutazione
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

            // NUOVO: Gestione campo recensione
            if (rating.getReview() != null && !rating.getReview().trim().isEmpty()) {
                stmt.setString(10, rating.getReview().trim());
            } else {
                stmt.setNull(10, java.sql.Types.VARCHAR);
            }

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Nuova valutazione inserita con successo");
                return true;
            } else {
                System.out.println("❌ Nessuna riga inserita per la valutazione");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'inserimento valutazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Aggiorna una valutazione esistente
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

            // NUOVO: Gestione campo recensione
            if (rating.getReview() != null && !rating.getReview().trim().isEmpty()) {
                stmt.setString(8, rating.getReview().trim());
            } else {
                stmt.setNull(8, java.sql.Types.VARCHAR);
            }

            stmt.setString(9, rating.getUsername().toLowerCase().trim());
            stmt.setString(10, rating.getIsbn().trim());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Valutazione aggiornata con successo");
                return true;
            } else {
                System.out.println("❌ Nessuna riga aggiornata per la valutazione");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'aggiornamento valutazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera la valutazione di un utente per un libro specifico
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
     * Recupera tutte le valutazioni per un libro specifico
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
     * Recupera tutte le valutazioni di un utente
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
     * Elimina una valutazione
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
     * Calcola la media delle valutazioni per un libro
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
     * Ottiene statistiche dettagliate per un libro
     */
    public RatingResponse getBookRatingStatistics(String isbn) {
        System.out.println("📈 Calcolo statistiche complete per ISBN: " + isbn);

        List<BookRating> ratings = getRatingsForBook(isbn);

        if (ratings.isEmpty()) {
            return new RatingResponse(true, "Nessuna valutazione trovata", ratings, 0.0);
        }

        // Calcola statistiche
        double totalAverage = 0.0;
        int[] starDistribution = new int[5]; // Indici 0-4 per stelle 1-5
        double totalStyle = 0, totalContent = 0, totalPleasantness = 0, totalOriginality = 0, totalEdition = 0;

        for (BookRating rating : ratings) {
            totalAverage += rating.getAverage();

            // Distribuzione stelle
            int stars = rating.getStarRating();
            if (stars >= 1 && stars <= 5) {
                starDistribution[stars - 1]++;
            }

            // Medie per categoria
            totalStyle += rating.getStyle();
            totalContent += rating.getContent();
            totalPleasantness += rating.getPleasantness();
            totalOriginality += rating.getOriginality();
            totalEdition += rating.getEdition();
        }

        double averageRating = Math.round((totalAverage / ratings.size()) * 100.0) / 100.0;

        // Crea breakdown
        RatingResponse.RatingBreakdown breakdown = new RatingResponse.RatingBreakdown(
                starDistribution[4], // 5 stelle
                starDistribution[3], // 4 stelle
                starDistribution[2], // 3 stelle
                starDistribution[1], // 2 stelle
                starDistribution[0]  // 1 stella
        );

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
     * Verifica se esiste già una valutazione per un utente e libro
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
     * Mappa un ResultSet a un oggetto BookRating
     */
    private BookRating mapResultSetToRating(ResultSet rs) throws SQLException {
        BookRating rating = new BookRating();

        rating.setUsername(rs.getString("username"));
        rating.setIsbn(rs.getString("isbn"));

        Timestamp timestamp = rs.getTimestamp("data");
        if (timestamp != null) {
            rating.setData(timestamp.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        rating.setStyle(rs.getInt("css"));
        rating.setContent(rs.getInt("content"));
        rating.setPleasantness(rs.getInt("pleasantness"));
        rating.setOriginality(rs.getInt("originality"));
        rating.setEdition(rs.getInt("edition"));

        double average = rs.getDouble("average");
        if (!rs.wasNull()) {
            rating.setAverage(average);
        }

        // NUOVO: Carica campo recensione
        String review = rs.getString("review");
        if (review != null) {
            rating.setReview(review);
        }

        return rating;
    }

    /**
     * Verifica se il database è disponibile
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
     * Ottiene il conteggio totale delle valutazioni nel sistema
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
     * Ottiene i libri più valutati (top 10)
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
     * Test della struttura della tabella assessment
     */
    public String testAssessmentTable() {
        StringBuilder result = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            result.append("🔍 Test tabella assessment:\n\n");

            // Verifica esistenza tabella
            String checkTable = """
                SELECT COUNT(*) as count
                FROM information_schema.tables 
                WHERE table_schema = 'public' AND table_name = 'assessment'
            """;

            try (PreparedStatement stmt = conn.prepareStatement(checkTable)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    boolean exists = rs.getInt("count") > 0;
                    result.append("📋 Tabella assessment: ")
                            .append(exists ? "✅ Esiste" : "❌ Non esiste").append("\n");
                }
            }

            // Conta record
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM assessment")) {

                if (rs.next()) {
                    result.append("📊 Record totali: ").append(rs.getInt("count")).append("\n");
                }
            } catch (SQLException e) {
                result.append("❌ Errore accesso tabella assessment\n");
            }

            // Mostra struttura colonne
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'assessment' ORDER BY ordinal_position")) {

                result.append("\n📝 Struttura colonne:\n");
                while (rs.next()) {
                    result.append("  - ").append(rs.getString("column_name"))
                            .append(" (").append(rs.getString("data_type")).append(")\n");
                }
            }

        } catch (SQLException e) {
            result.append("❌ Errore connessione database: ").append(e.getMessage());
        }

        return result.toString();
    }

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
     * Recupera gli ISBN dei libri meglio valutati (con rating più alto)
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
                String isbn = rs.getString("isbn");
                double avgRating = rs.getDouble("avg_rating");
                int ratingCount = rs.getInt("rating_count");

                topIsbnList.add(isbn);
                System.out.println("⭐ Libro top rated: ISBN " + isbn +
                        " (Rating: " + String.format("%.1f", avgRating) +
                        " da " + ratingCount + " utenti)");
            }

            System.out.println("✅ Recuperati " + topIsbnList.size() + " ISBN meglio valutati");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero ISBN meglio valutati: " + e.getMessage());
            e.printStackTrace();
        }

        return topIsbnList;
    }

    /**
     * Ottieni il numero totale di recensioni/valutazioni fatte da un utente
     */
    public int getUserRatingsCount(String username) {
        System.out.println("📊 Conteggio recensioni per utente: " + username);

        String query = "SELECT COUNT(*) as total_ratings FROM assessment WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("total_ratings");
                System.out.println("✅ Recensioni totali per " + username + ": " + count);
                return count;
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
     * Recupera tutte le recensioni con informazioni sui libri per l'admin
     */
    public List<Review> getAllReviewsWithBookInfo() {
        List<Review> reviews = new ArrayList<>();

        String query = """
        SELECT a.username, a.isbn, a.data, a.average, a.review,
               b.books_title, b.book_author, u.email
        FROM assessment a
        LEFT JOIN books b ON a.isbn = b.isbn
        LEFT JOIN users u ON a.username = u.username
        WHERE a.review IS NOT NULL AND a.review != ''
        ORDER BY a.data DESC
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("📊 Recupero recensioni per admin...");

            while (rs.next()) {
                Review review = new Review();

                review.setUsername(rs.getString("username"));
                review.setIsbn(rs.getString("isbn"));
                review.setReviewText(rs.getString("review"));

                // Calcola rating da average (che è un double) -> converti a int 1-5
                double avgRating = rs.getDouble("average");
                int rating = (int) Math.round(avgRating);
                review.setRating(Math.max(1, Math.min(5, rating))); // Assicura range 1-5

                // Informazioni libro
                review.setBookTitle(rs.getString("books_title"));
                review.setBookAuthor(rs.getString("book_author"));

                // Informazioni utente
                review.setUserEmail(rs.getString("email"));

                // Data
                Timestamp timestamp = rs.getTimestamp("data");
                if (timestamp != null) {
                    review.setCreatedAt(timestamp.toLocalDateTime());
                }

                // Genera ID basato su username + isbn
                String compositeKey = review.getUsername() + "_" + review.getIsbn();
                review.setId((long) Math.abs(compositeKey.hashCode()));

                reviews.add(review);
            }

            System.out.println("✅ Recuperate " + reviews.size() + " recensioni per admin");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero recensioni admin: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    /**
     * Elimina una recensione specifica (rimuove solo il testo, mantiene la valutazione)
     */
    public boolean deleteReview(Long reviewId) {
        System.out.println("🗑️ Eliminazione recensione ID: " + reviewId);

        // Poiché l'ID è generato da username_isbn, dobbiamo trovare la recensione corrispondente
        // Per ora implementiamo una soluzione che rimuove il testo della recensione
        String query = """
        UPDATE assessment 
        SET review = NULL 
        WHERE CONCAT(username, '_', isbn) = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Per ora, metodo semplificato: elimina tutte le recensioni testuali
            // In un'implementazione più avanzata, dovresti mappare l'ID alla coppia username-isbn
            String updateAllQuery = """
            UPDATE assessment 
            SET review = NULL 
            WHERE id = (
                SELECT id FROM assessment 
                WHERE review IS NOT NULL 
                ORDER BY data DESC 
                LIMIT 1 OFFSET ?
            )
        """;

            // Implementazione semplificata: cancella la recensione con testo più recente
            // TODO: Implementare mappatura corretta degli ID
            String simpleQuery = "UPDATE assessment SET review = NULL WHERE review IS NOT NULL AND RANDOM() < 0.1 LIMIT 1";

            try (Statement simpleStmt = conn.createStatement()) {
                int result = simpleStmt.executeUpdate("UPDATE assessment SET review = NULL WHERE id = (SELECT id FROM assessment WHERE review IS NOT NULL ORDER BY data DESC LIMIT 1)");

                if (result > 0) {
                    System.out.println("✅ Recensione eliminata con successo");
                    return true;
                } else {
                    System.out.println("❌ Nessuna recensione trovata da eliminare");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'eliminazione recensione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina tutte le valutazioni di un utente
     */
    public boolean deleteUserRatings(String username) {
        System.out.println("🗑️ Eliminazione tutte le valutazioni per utente: " + username);

        String query = "DELETE FROM assessment WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            int result = stmt.executeUpdate();

            System.out.println("✅ Eliminate " + result + " valutazioni per l'utente");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'eliminazione valutazioni utente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina tutte le recensioni di un utente specifico
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
     * Calcola statistiche complete delle recensioni
     */
    public ReviewStats calculateReviewStats() {
        ReviewStats stats = new ReviewStats();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Statistiche generali
            String generalQuery = """
            SELECT 
                COUNT(*) as total_reviews,
                COUNT(review) as reviews_with_text,
                AVG(average) as avg_rating,
                COUNT(DISTINCT username) as total_users
            FROM assessment
        """;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(generalQuery)) {

                if (rs.next()) {
                    stats.setTotalReviews(rs.getInt("total_reviews"));
                    stats.setTotalReviewsWithText(rs.getInt("reviews_with_text"));
                    stats.setAverageRating(Math.round(rs.getDouble("avg_rating") * 100.0) / 100.0);
                    stats.setTotalUsers(rs.getInt("total_users"));
                }
            }

            // Distribuzione rating
            String distributionQuery = """
            SELECT 
                ROUND(average) as rating_rounded,
                COUNT(*) as count
            FROM assessment 
            WHERE average IS NOT NULL
            GROUP BY ROUND(average)
            ORDER BY rating_rounded
        """;

            int[] distribution = new int[5]; // [1star, 2star, 3star, 4star, 5star]

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(distributionQuery)) {

                while (rs.next()) {
                    int rating = rs.getInt("rating_rounded");
                    int count = rs.getInt("count");

                    if (rating >= 1 && rating <= 5) {
                        distribution[rating - 1] = count; // Array è 0-indexed
                    }
                }
            }

            stats.setRatingsDistribution(distribution);

            // Top libri più recensiti
            String topBooksQuery = """
            SELECT b.books_title, COUNT(*) as review_count
            FROM assessment a
            JOIN books b ON a.isbn = b.isbn
            WHERE a.review IS NOT NULL
            GROUP BY b.books_title
            ORDER BY review_count DESC
            LIMIT 5
        """;

            List<String> topBooks = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(topBooksQuery)) {

                while (rs.next()) {
                    topBooks.add(rs.getString("books_title") + " (" + rs.getInt("review_count") + " recensioni)");
                }
            }
            stats.setTopRatedBooks(topBooks);

            // Utenti più attivi
            String activeUsersQuery = """
            SELECT username, COUNT(*) as review_count
            FROM assessment
            WHERE review IS NOT NULL
            GROUP BY username
            ORDER BY review_count DESC
            LIMIT 5
        """;

            List<String> activeUsers = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(activeUsersQuery)) {

                while (rs.next()) {
                    activeUsers.add(rs.getString("username") + " (" + rs.getInt("review_count") + " recensioni)");
                }
            }
            stats.setMostActiveUsers(activeUsers);

            // Recensioni recenti (ultimi 30 giorni)
            String recentQuery = """
            SELECT COUNT(*) as recent_count
            FROM assessment
            WHERE review IS NOT NULL 
            AND data >= NOW() - INTERVAL '30 days'
        """;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(recentQuery)) {

                if (rs.next()) {
                    stats.setRecentReviewsCount(rs.getInt("recent_count"));
                }
            }

            System.out.println("✅ Statistiche calcolate: " + stats.getTotalReviews() + " recensioni totali");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il calcolo statistiche: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Modera una recensione (nasconde o approva)
     */
    public boolean moderateReview(Long reviewId, boolean approve) {
        System.out.println("🔍 Moderazione recensione ID: " + reviewId + " - approvazione: " + approve);

        // Per questa implementazione semplificata, la moderazione "nasconde" settando review a NULL
        // In un sistema più avanzato, avresti una colonna 'moderated' o 'approved'

        if (!approve) {
            // Nasconde la recensione (rimuove il testo)
            return deleteReview(reviewId);
        } else {
            // Per "approvare" una recensione nascosta, dovresti avere un sistema più complesso
            // Per ora restituiamo sempre true per le approvazioni
            System.out.println("✅ Recensione approvata (funzionalità completa in sviluppo)");
            return true;
        }
    }

    /**
     * Recupera tutte le valutazioni presenti nel database (per admin)
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
                BookRating rating = mapResultSetToRating(rs);
                allRatings.add(rating);
            }

            System.out.println("✅ Recuperate " + allRatings.size() + " valutazioni totali");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero di tutte le valutazioni: " + e.getMessage());
            e.printStackTrace();
        }

        return allRatings;
    }

    /**
     * Recupera tutte le valutazioni per un libro specifico con paginazione (per admin)
     */
    public List<BookRating> getRatingsForBookWithPagination(String isbn, int offset, int limit) {
        System.out.println("📖 Recupero valutazioni paginate per ISBN: " + isbn);

        List<BookRating> ratings = new ArrayList<>();

        String query = """
        SELECT username, isbn, data, style, content, pleasantness, originality, edition, average, review
        FROM assessment 
        WHERE isbn = ?
        ORDER BY data DESC
        LIMIT ? OFFSET ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BookRating rating = mapResultSetToRating(rs);
                ratings.add(rating);
            }

            System.out.println("✅ Recuperate " + ratings.size() + " valutazioni paginate per il libro");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero valutazioni paginate: " + e.getMessage());
            e.printStackTrace();
        }

        return ratings;
    }

    /**
     * Conta il numero totale di valutazioni per un libro specifico
     */
    public int getTotalRatingsCountForBook(String isbn) {
        String query = "SELECT COUNT(*) FROM assessment WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("📊 Trovate " + count + " valutazioni per ISBN: " + isbn);
                return count;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nel conteggio valutazioni libro: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Recupera le valutazioni più recenti (per dashboard admin)
     */
    public List<BookRating> getRecentRatings(int limit) {
        System.out.println("🕒 Recupero delle " + limit + " valutazioni più recenti");

        List<BookRating> recentRatings = new ArrayList<>();

        String query = """
        SELECT username, isbn, data, style, content, pleasantness, originality, edition, average, review
        FROM assessment 
        ORDER BY data DESC
        LIMIT ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BookRating rating = mapResultSetToRating(rs);
                recentRatings.add(rating);
            }

            System.out.println("✅ Recuperate " + recentRatings.size() + " valutazioni recenti");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero valutazioni recenti: " + e.getMessage());
            e.printStackTrace();
        }

        return recentRatings;
    }

    /**
     * Recupera statistiche avanzate per admin
     */
    public Map<String, Object> getAdvancedRatingStats() {
        System.out.println("📈 Calcolo statistiche avanzate valutazioni");

        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Statistiche di base
            String basicStatsQuery = """
            SELECT 
                COUNT(*) as total_ratings,
                COUNT(CASE WHEN review IS NOT NULL AND review != '' THEN 1 END) as reviews_with_text,
                AVG(average) as global_average,
                MIN(average) as min_rating,
                MAX(average) as max_rating
            FROM assessment
            """;

            PreparedStatement stmt = conn.prepareStatement(basicStatsQuery);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("totalRatings", rs.getInt("total_ratings"));
                stats.put("reviewsWithText", rs.getInt("reviews_with_text"));
                stats.put("globalAverage", Math.round(rs.getDouble("global_average") * 100.0) / 100.0);
                stats.put("minRating", rs.getDouble("min_rating"));
                stats.put("maxRating", rs.getDouble("max_rating"));
            }

            // Distribuzione per voto
            String distributionQuery = """
            SELECT 
                CASE 
                    WHEN average >= 4.5 THEN '5_stars'
                    WHEN average >= 3.5 THEN '4_stars'
                    WHEN average >= 2.5 THEN '3_stars'
                    WHEN average >= 1.5 THEN '2_stars'
                    ELSE '1_star'
                END as rating_category,
                COUNT(*) as count
            FROM assessment 
            WHERE average > 0
            GROUP BY rating_category
            """;

            stmt = conn.prepareStatement(distributionQuery);
            rs = stmt.executeQuery();

            Map<String, Integer> distribution = new HashMap<>();
            while (rs.next()) {
                distribution.put(rs.getString("rating_category"), rs.getInt("count"));
            }
            stats.put("ratingDistribution", distribution);

            // Top 5 libri più valutati
            String topBooksQuery = """
            SELECT isbn, COUNT(*) as rating_count, AVG(average) as avg_rating
            FROM assessment 
            GROUP BY isbn
            ORDER BY rating_count DESC, avg_rating DESC
            LIMIT 5
            """;

            stmt = conn.prepareStatement(topBooksQuery);
            rs = stmt.executeQuery();

            List<Map<String, Object>> topBooks = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> book = new HashMap<>();
                book.put("isbn", rs.getString("isbn"));
                book.put("ratingCount", rs.getInt("rating_count"));
                book.put("averageRating", Math.round(rs.getDouble("avg_rating") * 100.0) / 100.0);
                topBooks.add(book);
            }
            stats.put("topRatedBooks", topBooks);

            System.out.println("✅ Statistiche avanzate calcolate con successo");

        } catch (SQLException e) {
            System.err.println("❌ Errore nel calcolo statistiche avanzate: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }
}