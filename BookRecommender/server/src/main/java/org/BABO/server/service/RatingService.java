package org.BABO.server.service;
import java.time.format.DateTimeFormatter;

import org.BABO.shared.model.BookRating;
import org.BABO.shared.dto.RatingResponse;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        rating.setStyle(rs.getInt("style"));
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
}