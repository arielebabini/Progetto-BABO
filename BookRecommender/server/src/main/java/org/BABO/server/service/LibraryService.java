package org.BABO.server.service;

import org.BABO.shared.model.Book;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per gestire le operazioni sulle librerie personali degli utenti
 * VERSIONE CORRETTA - USA SOLO IMMAGINI LOCALI, MAI ONLINE
 */
@Service
public class LibraryService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Crea una nuova libreria per un utente
     */
    public boolean createLibrary(String username, String libraryName) {
        System.out.println("📚 Creazione libreria '" + libraryName + "' per utente: " + username);

        String query = "INSERT INTO user_libraries (username, name) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, libraryName.trim());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Libreria creata con successo: " + libraryName);
                return true;
            } else {
                System.out.println("❌ Nessuna riga inserita per la libreria: " + libraryName);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la creazione della libreria: " + e.getMessage());

            // Controlla se è un errore di duplicato
            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                System.out.println("❌ Libreria '" + libraryName + "' già esistente per l'utente " + username);
            }
            return false;
        }
    }

    /**
     * Recupera tutte le librerie di un utente
     */
    public List<String> getUserLibraries(String username) {
        System.out.println("📖 Recupero librerie per utente: " + username);

        List<String> libraries = new ArrayList<>();
        String query = "SELECT name FROM user_libraries WHERE username = ? ORDER BY created_at DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                libraries.add(rs.getString("name"));
            }

            System.out.println("✅ Recuperate " + libraries.size() + " librerie per: " + username);

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero librerie: " + e.getMessage());
            e.printStackTrace();
        }

        return libraries;
    }

    /**
     * Recupera tutti i libri in una specifica libreria
     * CORRETTO - USA SOLO NOMI FILE LOCALI, MAI URL REMOTI
     */
    public List<Book> getBooksInLibrary(String username, String libraryName) {
        System.out.println("📖 Recupero libri nella libreria '" + libraryName + "' per: " + username);

        List<Book> books = new ArrayList<>();

        String query = """
        SELECT b.isbn, b.books_title, b.book_author, b.description, b.publi_year, 
               b.category, b.publisher
        FROM library_books lb
        JOIN books b ON lb.isbn = b.isbn
        WHERE lb.username = ? AND lb.library_name = ?
        ORDER BY lb.added_at DESC
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, libraryName.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Crea oggetto Book con costruttore vuoto
                Book book = new Book();

                // Imposta campi obbligatori con nomi colonne CORRETTI
                String isbn = rs.getString("isbn");
                book.setIsbn(isbn);
                book.setTitle(rs.getString("books_title"));        // ✅ Nome colonna corretto
                book.setAuthor(rs.getString("book_author"));

                // Imposta campi opzionali (con controllo null)
                String description = rs.getString("description");
                if (description != null) {
                    book.setDescription(description);
                }

                String publishYear = rs.getString("publi_year");
                if (publishYear != null) {
                    book.setPublishYear(publishYear);
                }

                String category = rs.getString("category");
                if (category != null) {
                    book.setCategory(category);
                }

                String publisher = rs.getString("publisher");
                if (publisher != null) {
                    book.setPublisher(publisher);
                }

                String localImageFileName = generateLocalImageFileName(isbn, book.getTitle());
                book.setImageUrl(localImageFileName);
                System.out.println("📷 Impostato file immagine locale: " + localImageFileName + " per ISBN: " + isbn);

                // Genera ID per compatibilità
                book.setId((long) Math.abs(isbn.hashCode()));

                // Imposta valori di default
                book.setIsFree(true);
                book.setIsNew(false);

                books.add(book);
            }

            System.out.println("✅ Recuperati " + books.size() + " libri dalla libreria '" + libraryName + "'");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero libri: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    /**
     * Aggiunge un libro a una libreria
     */
    public boolean addBookToLibrary(String username, String libraryName, String isbn) {
        System.out.println("➕ Aggiunta libro (ISBN: " + isbn + ") alla libreria '" + libraryName + "'");

        // Prima verifica che la libreria esista
        if (!libraryExists(username, libraryName)) {
            System.out.println("❌ Libreria '" + libraryName + "' non trovata per l'utente " + username);
            return false;
        }

        // Verifica che il libro esista nel catalogo
        if (!bookExists(isbn)) {
            System.out.println("❌ Libro con ISBN '" + isbn + "' non trovato nel catalogo");
            return false;
        }

        String query = "INSERT INTO library_books (username, library_name, isbn) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, libraryName.trim());
            stmt.setString(3, isbn.trim());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Libro aggiunto con successo alla libreria");
                return true;
            } else {
                System.out.println("❌ Nessuna riga inserita per l'aggiunta del libro");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'aggiunta libro: " + e.getMessage());

            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                System.out.println("❌ Libro già presente nella libreria");
            }
            return false;
        }
    }

    /**
     * Rimuove un libro da una libreria
     */
    public boolean removeBookFromLibrary(String username, String libraryName, String isbn) {
        System.out.println("➖ Rimozione libro (ISBN: " + isbn + ") dalla libreria '" + libraryName + "'");

        String query = "DELETE FROM library_books WHERE username = ? AND library_name = ? AND isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, libraryName.trim());
            stmt.setString(3, isbn.trim());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Libro rimosso con successo dalla libreria");
                return true;
            } else {
                System.out.println("❌ Libro non trovato nella libreria specificata");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la rimozione libro: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una libreria intera
     */
    public boolean deleteLibrary(String username, String libraryName) {
        System.out.println("🗑️ Eliminazione libreria '" + libraryName + "' per: " + username);

        // Prima elimina tutti i libri dalla libreria (se non c'è cascade)
        String deleteBooksQuery = "DELETE FROM library_books WHERE username = ? AND library_name = ?";
        String deleteLibraryQuery = "DELETE FROM user_libraries WHERE username = ? AND name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Inizia una transazione per garantire atomicità
            conn.setAutoCommit(false);

            try {
                // Prima elimina i libri dalla libreria
                try (PreparedStatement stmt1 = conn.prepareStatement(deleteBooksQuery)) {
                    stmt1.setString(1, username.toLowerCase().trim());
                    stmt1.setString(2, libraryName.trim());
                    int deletedBooks = stmt1.executeUpdate();
                    System.out.println("📚 Eliminati " + deletedBooks + " libri dalla libreria");
                }

                // Poi elimina la libreria
                try (PreparedStatement stmt2 = conn.prepareStatement(deleteLibraryQuery)) {
                    stmt2.setString(1, username.toLowerCase().trim());
                    stmt2.setString(2, libraryName.trim());
                    int result = stmt2.executeUpdate();

                    if (result > 0) {
                        conn.commit(); // Conferma la transazione
                        System.out.println("✅ Libreria eliminata con successo");
                        return true;
                    } else {
                        conn.rollback(); // Annulla la transazione
                        System.out.println("❌ Libreria non trovata");
                        return false;
                    }
                }

            } catch (SQLException e) {
                conn.rollback(); // Annulla la transazione in caso di errore
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'eliminazione libreria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Rinomina una libreria
     */
    public boolean renameLibrary(String username, String oldName, String newName) {
        System.out.println("✏️ Rinomina libreria da '" + oldName + "' a '" + newName + "'");

        // Aggiorna sia la tabella user_libraries che library_books
        String updateLibraryQuery = "UPDATE user_libraries SET name = ? WHERE username = ? AND name = ?";
        String updateBooksQuery = "UPDATE library_books SET library_name = ? WHERE username = ? AND library_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Inizia una transazione
            conn.setAutoCommit(false);

            try {
                // Aggiorna il nome nella tabella user_libraries
                try (PreparedStatement stmt1 = conn.prepareStatement(updateLibraryQuery)) {
                    stmt1.setString(1, newName.trim());
                    stmt1.setString(2, username.toLowerCase().trim());
                    stmt1.setString(3, oldName.trim());
                    int result = stmt1.executeUpdate();

                    if (result > 0) {
                        // Aggiorna anche i riferimenti in library_books
                        try (PreparedStatement stmt2 = conn.prepareStatement(updateBooksQuery)) {
                            stmt2.setString(1, newName.trim());
                            stmt2.setString(2, username.toLowerCase().trim());
                            stmt2.setString(3, oldName.trim());
                            stmt2.executeUpdate();
                        }

                        conn.commit(); // Conferma la transazione
                        System.out.println("✅ Libreria rinominata con successo");
                        return true;
                    } else {
                        conn.rollback();
                        System.out.println("❌ Libreria non trovata");
                        return false;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la rinomina libreria: " + e.getMessage());

            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                System.out.println("❌ Nuovo nome libreria già esistente");
            }
            return false;
        }
    }

    /**
     * Ottieni statistiche delle librerie di un utente
     */
    public String getUserLibraryStats(String username) {
        System.out.println("📊 Calcolo statistiche per utente: " + username);

        StringBuilder stats = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Numero totale di librerie
            String librariesQuery = "SELECT COUNT(*) as total_libraries FROM user_libraries WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(librariesQuery)) {
                stmt.setString(1, username.toLowerCase().trim());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.append("Numero librerie: ").append(rs.getInt("total_libraries")).append("\n");
                }
            }

            // Numero totale di libri
            String booksQuery = "SELECT COUNT(*) as total_books FROM library_books WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(booksQuery)) {
                stmt.setString(1, username.toLowerCase().trim());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.append("Numero totale libri: ").append(rs.getInt("total_books")).append("\n");
                }
            }

            // Libreria con più libri
            String topLibraryQuery = """
                SELECT library_name, COUNT(*) as book_count 
                FROM library_books 
                WHERE username = ? 
                GROUP BY library_name 
                ORDER BY book_count DESC 
                LIMIT 1
            """;
            try (PreparedStatement stmt = conn.prepareStatement(topLibraryQuery)) {
                stmt.setString(1, username.toLowerCase().trim());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.append("Libreria più grande: ").append(rs.getString("library_name"))
                            .append(" (").append(rs.getInt("book_count")).append(" libri)");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il calcolo statistiche: " + e.getMessage());
            return "Errore nel calcolo delle statistiche";
        }

        return stats.toString();
    }

    /**
     * Ottieni il numero totale di libri nelle librerie di un utente
     */
    public int getUserTotalBooksCount(String username) {
        System.out.println("📊 Conteggio libri totali per utente: " + username);

        String query = "SELECT COUNT(*) as total_books FROM library_books WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("total_books");
                System.out.println("✅ Libri totali per " + username + ": " + count);
                return count;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nel conteggio libri utente: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Controlla se il database è disponibile
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database non disponibile: " + e.getMessage());
            return false;
        }
    }

    // ==================== METODI DI UTILITÀ ====================

    /**
     * Genera il nome del file immagine locale basato su ISBN o titolo
     * Restituisce solo il nome del file, non l'URL completo
     * L'ImageUtils si occuperà di caricarlo dalle risorse
     */
    private String generateLocalImageFileName(String isbn, String title) {
        if (isbn != null && !isbn.trim().isEmpty()) {
            // Rimuovi caratteri speciali dall'ISBN
            String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
            return cleanIsbn + ".jpg";
        } else if (title != null && !title.trim().isEmpty()) {
            // Se non c'è ISBN, usa il titolo (pulito)
            String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "");
            return cleanTitle + ".jpg";
        } else {
            // Fallback al placeholder
            return "placeholder.jpg";
        }
    }

    /**
     * Verifica se una libreria esiste per un utente
     */
    private boolean libraryExists(String username, String libraryName) {
        String query = "SELECT 1 FROM user_libraries WHERE username = ? AND name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, libraryName.trim());

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("❌ Errore verifica esistenza libreria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se un libro esiste nel catalogo
     */
    private boolean bookExists(String isbn) {
        // ✅ Query corretta: usa books_title invece di book_title se il DB è stato aggiornato
        String query = "SELECT 1 FROM books WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();

            if (!exists) {
                System.out.println("📚 Libro con ISBN '" + isbn + "' non trovato. Libri disponibili:");
                // Debug query: mostra alcuni libri disponibili
                String debugQuery = "SELECT isbn, books_title, book_author FROM books LIMIT 5";
                try (PreparedStatement debugStmt = conn.prepareStatement(debugQuery);
                     ResultSet debugRs = debugStmt.executeQuery()) {
                    while (debugRs.next()) {
                        System.out.println("  - " + debugRs.getString("isbn") + ": " +
                                debugRs.getString("books_title") + " by " +    // ✅ books_title
                                debugRs.getString("book_author"));
                    }
                }
            }

            return exists;

        } catch (SQLException e) {
            System.err.println("❌ Errore verifica esistenza libro: " + e.getMessage());
            return false;
        }
    }

    /**
     * Metodo per testare la connessione e la struttura del database
     */
    public String testDatabaseStructure() {
        StringBuilder result = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            result.append("🔍 Test struttura database:\n\n");

            // Test tabelle esistenti
            String[] tables = {"users", "user_libraries", "books", "library_books"};
            for (String table : tables) {
                String query = """
                    SELECT COUNT(*) as count
                    FROM information_schema.tables 
                    WHERE table_schema = 'public' AND table_name = ?
                """;

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, table);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        boolean exists = rs.getInt("count") > 0;
                        result.append("📋 Tabella ").append(table).append(": ")
                                .append(exists ? "✅ Esiste" : "❌ Non esiste").append("\n");
                    }
                }
            }

            // Test record di esempio
            result.append("\n📊 Conteggio record:\n");
            for (String table : tables) {
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table);
                    if (rs.next()) {
                        result.append("📈 ").append(table).append(": ")
                                .append(rs.getInt("count")).append(" record\n");
                    }
                } catch (SQLException e) {
                    result.append("❌ ").append(table).append(": Errore accesso\n");
                }
            }

        } catch (SQLException e) {
            result.append("❌ Errore connessione database: ").append(e.getMessage());
        }

        return result.toString();
    }
}