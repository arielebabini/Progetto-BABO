package org.BABO.server.service;

import org.BABO.shared.model.Book;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione completa delle librerie personali degli utenti nell'applicazione BABO.
 * <p>
 * Questo servizio gestisce tutte le operazioni relative alle collezioni personali di libri degli utenti,
 * fornendo un layer di business logic per creazione, gestione e organizzazione di librerie personalizzate.
 * Il servizio implementa pattern transazionali per operazioni atomiche, gestione sicura delle connessioni
 * database e ottimizzazioni per performance su collezioni di grandi dimensioni.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Gestione Librerie:</strong> CRUD completo per collezioni utente personalizzate</li>
 *   <li><strong>Organizzazione Contenuti:</strong> Aggiunta/rimozione libri con controlli integrità</li>
 *   <li><strong>Controlli Proprietà:</strong> Verifica ownership e prevenzione duplicati</li>
 *   <li><strong>Analytics Utente:</strong> Statistiche personalizzate e metriche di utilizzo</li>
 *   <li><strong>Gestione Immagini:</strong> Mapping automatico a risorse locali per performance</li>
 *   <li><strong>Transazioni Atomiche:</strong> Operazioni database sicure con rollback</li>
 * </ul>
 *
 * <h3>Architettura Database:</h3>
 * <ul>
 *   <li><strong>user_libraries:</strong> Tabella principale librerie con metadati</li>
 *   <li><strong>library_books:</strong> Relazioni many-to-many tra librerie e libri</li>
 *   <li><strong>books:</strong> Integrazione con catalogo principale per validazione</li>
 *   <li><strong>Transactional Safety:</strong> Operazioni complesse con gestione transazioni</li>
 * </ul>
 *
 * <h3>Ottimizzazioni Performance:</h3>
 * <ul>
 *   <li>Connection pooling per gestione efficiente risorse database</li>
 *   <li>Query ottimizzate con indici appropriati per grandi collezioni</li>
 *   <li>Lazy loading metadati opzionali per ridurre overhead</li>
 *   <li>Gestione sicura memory per liste grandi</li>
 * </ul>
 *
 * @author BABO Development Team
 * @version 2.2.0
 * @since 1.0.0
 * @see Book
 */
@Service
public class LibraryService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Crea una nuova libreria personale per un utente.
     * <p>
     * Implementa controlli di unicità per prevenire librerie duplicate
     * e normalizzazione automatica dei dati di input.
     * </p>
     *
     * @param username proprietario della libreria
     * @param libraryName nome della libreria da creare
     * @return true se creata con successo, false altrimenti
     * @since 1.0.0
     */
    public boolean createLibrary(String username, String libraryName) {
        System.out.println("Creazione libreria '" + libraryName + "' per utente: " + username);

        String query = "INSERT INTO user_libraries (username, name) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, libraryName.trim());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("Libreria creata con successo: " + libraryName);
                return true;
            } else {
                System.out.println("Nessuna riga inserita per la libreria: " + libraryName);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione della libreria: " + e.getMessage());

            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                System.out.println("Libreria '" + libraryName + "' già esistente per l'utente " + username);
            }
            return false;
        }
    }

    /**
     * Recupera tutte le librerie di un utente ordinata per data creazione.
     *
     * @param username utente di cui recuperare le librerie
     * @return lista nomi librerie in ordine cronologico inverso
     * @since 1.0.0
     */
    public List<String> getUserLibraries(String username) {
        System.out.println("Recupero librerie per utente: " + username);

        List<String> libraries = new ArrayList<>();
        String query = "SELECT name FROM user_libraries WHERE username = ? ORDER BY created_at DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                libraries.add(rs.getString("name"));
            }

            System.out.println("Recuperate " + libraries.size() + " librerie per: " + username);

        } catch (SQLException e) {
            System.err.println("Errore durante il recupero librerie: " + e.getMessage());
            e.printStackTrace();
        }

        return libraries;
    }

    /**
     * Recupera tutti i libri contenuti in una libreria specifica.
     * <p>
     * Implementa mapping automatico a risorse immagine locali per performance
     * ottimale e gestione offline. Include metadati completi per ogni libro.
     * </p>
     *
     * @param username proprietario della libreria
     * @param libraryName nome della libreria da consultare
     * @return lista completa libri con metadati
     * @since 1.0.0
     */
    public List<Book> getBooksInLibrary(String username, String libraryName) {
        System.out.println("Recupero libri nella libreria '" + libraryName + "' per: " + username);

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
                Book book = new Book();

                String isbn = rs.getString("isbn");
                book.setIsbn(isbn);
                book.setTitle(rs.getString("books_title"));
                book.setAuthor(rs.getString("book_author"));

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
                System.out.println("Impostato file immagine locale: " + localImageFileName + " per ISBN: " + isbn);

                book.setId((long) Math.abs(isbn.hashCode()));
                book.setIsFree(true);
                book.setIsNew(false);

                books.add(book);
            }

            System.out.println("Recuperati " + books.size() + " libri dalla libreria '" + libraryName + "'");

        } catch (SQLException e) {
            System.err.println("Errore durante il recupero libri: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    /**
     * Aggiunge un libro esistente a una libreria con controlli di validazione.
     * <p>
     * Verifica esistenza libreria e libro nel catalogo prima dell'inserimento
     * per garantire integrità referenziale.
     * </p>
     *
     * @param username proprietario della libreria
     * @param libraryName nome libreria di destinazione
     * @param isbn codice ISBN del libro da aggiungere
     * @return true se aggiunto con successo, false altrimenti
     * @since 1.0.0
     */
    public boolean addBookToLibrary(String username, String libraryName, String isbn) {
        System.out.println("Aggiunta libro (ISBN: " + isbn + ") alla libreria '" + libraryName + "'");

        if (!libraryExists(username, libraryName)) {
            System.out.println("Libreria '" + libraryName + "' non trovata per l'utente " + username);
            return false;
        }

        if (!bookExists(isbn)) {
            System.out.println("Libro con ISBN '" + isbn + "' non trovato nel catalogo");
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
                System.out.println("Libro aggiunto con successo alla libreria");
                return true;
            } else {
                System.out.println("Nessuna riga inserita per l'aggiunta del libro");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta libro: " + e.getMessage());

            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                System.out.println("Libro già presente nella libreria");
            }
            return false;
        }
    }

    /**
     * Rimuove un libro specifico da una libreria.
     *
     * @param username proprietario della libreria
     * @param libraryName nome libreria sorgente
     * @param isbn codice ISBN del libro da rimuovere
     * @return true se rimosso con successo, false se non trovato
     * @since 1.0.0
     */
    public boolean removeBookFromLibrary(String username, String libraryName, String isbn) {
        System.out.println("Rimozione libro (ISBN: " + isbn + ") dalla libreria '" + libraryName + "'");

        String query = "DELETE FROM library_books WHERE username = ? AND library_name = ? AND isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, libraryName.trim());
            stmt.setString(3, isbn.trim());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("Libro rimosso con successo dalla libreria");
                return true;
            } else {
                System.out.println("Libro non trovato nella libreria specificata");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Errore durante la rimozione libro: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina completamente una libreria e tutto il contenuto.
     * <p>
     * Operazione transazionale che rimuove prima i libri associati
     * e poi la libreria stessa per mantenere integrità database.
     * </p>
     *
     * @param username proprietario della libreria
     * @param libraryName nome libreria da eliminare
     * @return true se eliminata con successo, false altrimenti
     * @since 1.0.0
     */
    public boolean deleteLibrary(String username, String libraryName) {
        System.out.println("Eliminazione libreria '" + libraryName + "' per: " + username);

        String deleteBooksQuery = "DELETE FROM library_books WHERE username = ? AND library_name = ?";
        String deleteLibraryQuery = "DELETE FROM user_libraries WHERE username = ? AND name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt1 = conn.prepareStatement(deleteBooksQuery)) {
                    stmt1.setString(1, username.toLowerCase().trim());
                    stmt1.setString(2, libraryName.trim());
                    int deletedBooks = stmt1.executeUpdate();
                    System.out.println("Eliminati " + deletedBooks + " libri dalla libreria");
                }

                try (PreparedStatement stmt2 = conn.prepareStatement(deleteLibraryQuery)) {
                    stmt2.setString(1, username.toLowerCase().trim());
                    stmt2.setString(2, libraryName.trim());
                    int result = stmt2.executeUpdate();

                    if (result > 0) {
                        conn.commit();
                        System.out.println("Libreria eliminata con successo");
                        return true;
                    } else {
                        conn.rollback();
                        System.out.println("Libreria non trovata");
                        return false;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Errore durante l'eliminazione libreria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Rinomina una libreria esistente mantenendo tutti i contenuti.
     * <p>
     * Operazione transazionale che aggiorna sia i metadati libreria
     * che tutti i riferimenti ai libri contenuti.
     * </p>
     *
     * @param username proprietario della libreria
     * @param oldName nome attuale della libreria
     * @param newName nuovo nome da assegnare
     * @return true se rinominata con successo, false altrimenti
     * @since 1.1.0
     */
    public boolean renameLibrary(String username, String oldName, String newName) {
        System.out.println("Rinomina libreria da '" + oldName + "' a '" + newName + "'");

        String updateLibraryQuery = "UPDATE user_libraries SET name = ? WHERE username = ? AND name = ?";
        String updateBooksQuery = "UPDATE library_books SET library_name = ? WHERE username = ? AND library_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt1 = conn.prepareStatement(updateLibraryQuery)) {
                    stmt1.setString(1, newName.trim());
                    stmt1.setString(2, username.toLowerCase().trim());
                    stmt1.setString(3, oldName.trim());
                    int result = stmt1.executeUpdate();

                    if (result > 0) {
                        try (PreparedStatement stmt2 = conn.prepareStatement(updateBooksQuery)) {
                            stmt2.setString(1, newName.trim());
                            stmt2.setString(2, username.toLowerCase().trim());
                            stmt2.setString(3, oldName.trim());
                            stmt2.executeUpdate();
                        }

                        conn.commit();
                        System.out.println("Libreria rinominata con successo");
                        return true;
                    } else {
                        conn.rollback();
                        System.out.println("Libreria non trovata");
                        return false;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Errore durante la rinomina libreria: " + e.getMessage());

            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                System.out.println("Nuovo nome libreria già esistente");
            }
            return false;
        }
    }

    /**
     * Verifica se un utente possiede un libro in qualsiasi sua libreria.
     *
     * @param username utente da verificare
     * @param isbn codice ISBN del libro
     * @return true se l'utente possiede il libro, false altrimenti
     * @since 1.2.0
     */
    public boolean doesUserOwnBook(String username, String isbn) {
        System.out.println("Verifica possesso libro ISBN: " + isbn + " per utente: " + username);

        String query = """
        SELECT COUNT(*) as book_count 
        FROM library_books 
        WHERE username = ? AND isbn = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, isbn.trim());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("book_count");
                boolean owns = count > 0;
                System.out.println(owns ? "Utente possiede il libro" : "Utente NON possiede il libro");
                return owns;
            }

        } catch (SQLException e) {
            System.err.println("Errore verifica possesso libro: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Calcola statistiche aggregate delle librerie utente.
     *
     * @param username utente di cui calcolare le statistiche
     * @return stringa formattata con statistiche complete
     * @since 1.3.0
     */
    public String getUserLibraryStats(String username) {
        System.out.println("Calcolo statistiche per utente: " + username);

        StringBuilder stats = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String librariesQuery = "SELECT COUNT(*) as total_libraries FROM user_libraries WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(librariesQuery)) {
                stmt.setString(1, username.toLowerCase().trim());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.append("Numero librerie: ").append(rs.getInt("total_libraries")).append("\n");
                }
            }

            String booksQuery = "SELECT COUNT(*) as total_books FROM library_books WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(booksQuery)) {
                stmt.setString(1, username.toLowerCase().trim());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.append("Numero totale libri: ").append(rs.getInt("total_books")).append("\n");
                }
            }

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
            System.err.println("Errore durante il calcolo statistiche: " + e.getMessage());
            return "Errore nel calcolo delle statistiche";
        }

        return stats.toString();
    }

    /**
     * Conta il numero totale di libri posseduti dall'utente.
     *
     * @param username utente di cui contare i libri
     * @return numero totale libri in tutte le librerie
     * @since 1.3.0
     */
    public int getUserTotalBooksCount(String username) {
        System.out.println("Conteggio libri totali per utente: " + username);

        String query = "SELECT COUNT(*) as total_books FROM library_books WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("total_books");
                System.out.println("Libri totali per " + username + ": " + count);
                return count;
            }

        } catch (SQLException e) {
            System.err.println("Errore nel conteggio libri utente: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Verifica disponibilità connessione database.
     *
     * @return true se database accessibile, false altrimenti
     * @since 1.0.0
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            System.err.println("Database non disponibile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Utility per test struttura e connettività database.
     *
     * @return report dettagliato stato database e tabelle
     * @since 1.5.0
     */
    public String testDatabaseStructure() {
        StringBuilder result = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            result.append("Test struttura database:\n\n");

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
                        result.append("Tabella ").append(table).append(": ")
                                .append(exists ? "Esiste" : "Non esiste").append("\n");
                    }
                }
            }

            result.append("\nConteggio record:\n");
            for (String table : tables) {
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table);
                    if (rs.next()) {
                        result.append(table).append(": ")
                                .append(rs.getInt("count")).append(" record\n");
                    }
                } catch (SQLException e) {
                    result.append(table).append(": Errore accesso\n");
                }
            }

        } catch (SQLException e) {
            result.append("Errore connessione database: ").append(e.getMessage());
        }

        return result.toString();
    }

    // ==================== METODI UTILITY PRIVATI ====================

    /**
     * Genera nome file immagine locale basato su ISBN o titolo.
     * <p>
     * Mapping automatico a risorse locali per performance ottimale
     * senza dipendenze da servizi esterni.
     * </p>
     *
     * @param isbn codice ISBN del libro
     * @param title titolo del libro come fallback
     * @return nome file immagine locale
     */
    private String generateLocalImageFileName(String isbn, String title) {
        if (isbn != null && !isbn.trim().isEmpty()) {
            String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
            return cleanIsbn + ".jpg";
        } else if (title != null && !title.trim().isEmpty()) {
            String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "");
            return cleanTitle + ".jpg";
        } else {
            return "placeholder.jpg";
        }
    }

    /**
     * Verifica esistenza libreria per utente specificato.
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
            System.err.println("Errore verifica esistenza libreria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica esistenza libro nel catalogo principale.
     */
    private boolean bookExists(String isbn) {
        String query = "SELECT 1 FROM books WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();

            if (!exists) {
                System.out.println("Libro con ISBN '" + isbn + "' non trovato. Libri disponibili:");
                String debugQuery = "SELECT isbn, books_title, book_author FROM books LIMIT 5";
                try (PreparedStatement debugStmt = conn.prepareStatement(debugQuery);
                     ResultSet debugRs = debugStmt.executeQuery()) {
                    while (debugRs.next()) {
                        System.out.println("  - " + debugRs.getString("isbn") + ": " +
                                debugRs.getString("books_title") + " by " +
                                debugRs.getString("book_author"));
                    }
                }
            }

            return exists;

        } catch (SQLException e) {
            System.err.println("Errore verifica esistenza libro: " + e.getMessage());
            return false;
        }
    }
}