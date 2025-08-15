package org.BABO.server.service;

import org.BABO.shared.model.Book;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio per gestire le operazioni sui libri
 * Connessione al database PostgreSQL con gestione corretta di ISBN, titolo e CATEGORY
 * ✅ CORRETTO: Ora legge correttamente il campo category dal database
 */
@Service
public class BookService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Recupera tutti i libri dal database
     * ✅ CORRETTO: Ora include il campo category
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        // ✅ AGGIUNTO 'category' alla SELECT
        String query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books ORDER BY books_title";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("📊 Connessione al database riuscita");

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category"); // ✅ LEGGI LA CATEGORY

                // Crea ID sequenziale per compatibilità
                Long id = (long) (books.size() + 1);

                // Genera nome file immagine basato su ISBN
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // ✅ CREA IL BOOK E IMPOSTA LA CATEGORIA
                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);

                System.out.println("📖 Caricato: " + title + " (ISBN: " + isbn + ", Categoria: " + category + ") di " + author + " (" + year + ")");
            }

            System.out.println("✅ Caricati " + books.size() + " libri dal database");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero dei libri dal database: " + e.getMessage());
            e.printStackTrace();

            // Aggiungi libri di fallback se il database non è disponibile
            System.out.println("📚 Uso libri di fallback...");
            addFallbackBooks(books);
        }

        return books;
    }

    /**
     * Recupera un libro specifico per ID
     */
    public Book getBookById(Long id) {
        List<Book> allBooks = getAllBooks();
        return allBooks.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Ricerca libri generica (per titolo o autore insieme)
     * ✅ CORRETTO: Ora include il campo category
     */
    public List<Book> searchBooks(String searchQuery) {
        System.out.println("🔍 Ricerca generica per: '" + searchQuery + "'");

        List<Book> books = new ArrayList<>();
        // ✅ AGGIUNTO 'category' alla SELECT
        String query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                "WHERE LOWER(books_title) LIKE LOWER(?) OR LOWER(book_author) LIKE LOWER(?) " +
                "ORDER BY books_title";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchQuery + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category"); // ✅ LEGGI LA CATEGORY

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // ✅ CREA IL BOOK E IMPOSTA LA CATEGORIA
                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);
            }

            System.out.println("🔍 Ricerca generica '" + searchQuery + "': trovati " + books.size() + " risultati nel database");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la ricerca nel database: " + e.getMessage());
            books = searchInFallbackBooks(searchQuery);
        }

        return books;
    }

    /**
     * Ricerca libri SOLO per titolo
     * ✅ CORRETTO: Ora include il campo category
     */
    public List<Book> searchBooksByTitle(String titleQuery) {
        System.out.println("📖 Ricerca per TITOLO: '" + titleQuery + "'");

        List<Book> books = new ArrayList<>();
        // ✅ AGGIUNTO 'category' alla SELECT
        String query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                "WHERE LOWER(books_title) LIKE LOWER(?) " +
                "ORDER BY books_title";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + titleQuery + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category"); // ✅ LEGGI LA CATEGORY

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // ✅ CREA IL BOOK E IMPOSTA LA CATEGORIA
                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);

                System.out.println("📖 Trovato titolo: " + title + " (ISBN: " + isbn + ", Categoria: " + category + ", " + year + ")");
            }

            System.out.println("📖 Ricerca titolo '" + titleQuery + "': trovati " + books.size() + " risultati");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la ricerca per titolo: " + e.getMessage());
            books = searchInFallbackBooksByTitle(titleQuery);
        }

        return books;
    }

    /**
     * Ricerca libri SOLO per autore
     * ✅ CORRETTO: Ora include il campo category
     */
    public List<Book> searchBooksByAuthor(String authorQuery) {
        System.out.println("👤 Ricerca per AUTORE: '" + authorQuery + "'");

        List<Book> books = new ArrayList<>();
        // ✅ AGGIUNTO 'category' alla SELECT
        String query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                "WHERE LOWER(book_author) LIKE LOWER(?) " +
                "ORDER BY books_title";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + authorQuery + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category"); // ✅ LEGGI LA CATEGORY

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // ✅ CREA IL BOOK E IMPOSTA LA CATEGORIA
                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);

                System.out.println("👤 Trovato autore: " + author + " - " + title + " (ISBN: " + isbn + ", Categoria: " + category + ", " + year + ")");
            }

            System.out.println("👤 Ricerca autore '" + authorQuery + "': trovati " + books.size() + " risultati");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la ricerca per autore: " + e.getMessage());
            books = searchInFallbackBooksByAuthor(authorQuery);
        }

        return books;
    }

    /**
     * Ricerca libri per autore e anno
     */
    public List<Book> searchBooksByAuthorAndYear(String authorQuery, String year) {
        System.out.println("👤📅 Ricerca per AUTORE e ANNO: '" + authorQuery + "' (" + year + ")");

        List<Book> books = new ArrayList<>();
        String query;

        // Costruisci query in base ai parametri
        if (year != null && !year.trim().isEmpty()) {
            query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                    "WHERE LOWER(book_author) LIKE LOWER(?) AND CAST(publi_year AS TEXT) = ? " +
                    "ORDER BY books_title";
            System.out.println("📊 Query con FILTRO ANNO: " + query);
        } else {
            query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                    "WHERE LOWER(book_author) LIKE LOWER(?) " +
                    "ORDER BY books_title";
            System.out.println("📊 Query SOLO AUTORE: " + query);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + authorQuery + "%";
            stmt.setString(1, searchPattern);
            System.out.println("📝 Parametro 1 (autore): " + searchPattern);

            if (year != null && !year.trim().isEmpty()) {
                stmt.setString(2, year.trim());
                System.out.println("📝 Parametro 2 (anno): " + year.trim());
            }

            ResultSet rs = stmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String dbYear = rs.getString("publi_year");
                String category = rs.getString("category"); // ✅ LEGGI LA CATEGORY

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // ✅ CREA IL BOOK E IMPOSTA LA CATEGORIA
                Book book = new Book(id, isbn, title, author, description, dbYear, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);

                count++;
                if (count <= 3) {
                    System.out.println("👤📅 Risultato " + count + ": " + author + " - " + title + " (ISBN: " + isbn + ", Categoria: " + category + ", " + dbYear + ")");
                }
            }

            System.out.println("👤📅 Ricerca autore-anno COMPLETATA: trovati " + books.size() + " risultati totali");

        } catch (SQLException e) {
            System.err.println("❌ Errore SQL durante la ricerca per autore e anno: " + e.getMessage());
            e.printStackTrace();

            System.out.println("🔄 Fallback: ricerca solo per autore");
            books = searchBooksByAuthor(authorQuery);
        }

        return books;
    }

    /**
     * Ricerca libri per categoria specifica
     */
    public List<Book> searchBooksByCategory(String categoryName) {
        System.out.println("🎭 Ricerca per CATEGORIA: '" + categoryName + "'");

        List<Book> books = new ArrayList<>();
        String query = " SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                " WHERE LOWER(category) LIKE LOWER(?) " +
                " ORDER BY books_title LIMIT 50";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + categoryName + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category");

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);

                System.out.println("🎭 Trovato: " + title + " - Categoria DB: " + category);
            }

            System.out.println("✅ Ricerca categoria '" + categoryName + "': trovati " + books.size() + " risultati");

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca per categoria: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    public CompletableFuture<List<Book>> searchBooksByCategoryAsync(String categoryName) {
        return CompletableFuture.supplyAsync(() -> searchBooksByCategory(categoryName));
    }

    /**
     * Ricerca nei libri di fallback (quando database non disponibile)
     */
    private List<Book> searchInFallbackBooks(String searchQuery) {
        List<Book> fallbackBooks = new ArrayList<>();
        addFallbackBooks(fallbackBooks);

        List<Book> results = new ArrayList<>();
        for (Book book : fallbackBooks) {
            if (book.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(searchQuery.toLowerCase())) {
                results.add(book);
            }
        }

        System.out.println("🔍 Ricerca fallback '" + searchQuery + "': trovati " + results.size() + " risultati");
        return results;
    }

    /**
     * Ricerca per titolo nei libri di fallback
     */
    private List<Book> searchInFallbackBooksByTitle(String titleQuery) {
        List<Book> fallbackBooks = new ArrayList<>();
        addFallbackBooks(fallbackBooks);

        List<Book> results = new ArrayList<>();
        for (Book book : fallbackBooks) {
            if (book.getTitle().toLowerCase().contains(titleQuery.toLowerCase())) {
                results.add(book);
            }
        }

        System.out.println("📖 Ricerca titolo fallback '" + titleQuery + "': trovati " + results.size() + " risultati");
        return results;
    }

    /**
     * Ricerca per autore nei libri di fallback
     */
    private List<Book> searchInFallbackBooksByAuthor(String authorQuery) {
        List<Book> fallbackBooks = new ArrayList<>();
        addFallbackBooks(fallbackBooks);

        List<Book> results = new ArrayList<>();
        for (Book book : fallbackBooks) {
            if (book.getAuthor().toLowerCase().contains(authorQuery.toLowerCase())) {
                results.add(book);
            }
        }

        System.out.println("👤 Ricerca autore fallback '" + authorQuery + "': trovati " + results.size() + " risultati");
        return results;
    }

    /**
     * Recupera libri in evidenza (primi 3)
     */
    public List<Book> getFeaturedBooks() {
        List<Book> allBooks = getAllBooks();
        int endIndex = Math.min(3, allBooks.size());
        return allBooks.subList(0, endIndex);
    }

    /**
     * Recupera libri gratuiti
     */
    public List<Book> getFreeBooks() {
        List<Book> allBooks = getAllBooks();

        int startIndex = allBooks.size() / 2;
        int endIndex = Math.min(startIndex + 24, allBooks.size());

        if (startIndex >= allBooks.size()) {
            return new ArrayList<>();
        }

        System.out.println("🆓 LIBRI GRATUITI: dalla posizione " + startIndex + " a " + endIndex);
        return allBooks.subList(startIndex, endIndex);
    }

    /**
     * Recupera nuove uscite (ultimi libri disponibili)
     */
    public List<Book> getNewReleases() {
        List<Book> allBooks = getAllBooks();
        if (allBooks.size() <= 24) {
            return allBooks;
        }

        int startIndex = Math.max(0, allBooks.size() - 24);
        return allBooks.subList(startIndex, allBooks.size());
    }

    /**
     * Recupera un libro per ISBN
     */
    public Book getBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }

        System.out.println("🔍 Ricerca libro per ISBN: " + isbn);

        // ✅ Query corretta per database aggiornato
        String query = """
        SELECT isbn, books_title, book_author, description, publi_year, category 
        FROM books 
        WHERE isbn = ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Leggi i campi dal database aggiornato
                String dbIsbn = rs.getString("isbn");
                String title = rs.getString("books_title");        // ✅ Nome colonna corretto nel DB
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String publishYear = rs.getString("publi_year");
                String category = rs.getString("category");

                // Genera ID basato sull'ISBN per compatibilità
                Long id = (long) Math.abs(dbIsbn.hashCode());

                // Genera nome file immagine locale (come negli altri metodi)
                String fileName = (dbIsbn != null && !dbIsbn.trim().isEmpty())
                        ? dbIsbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : "placeholder.jpg";

                // Crea oggetto Book con costruttore standard
                Book book = new Book(id, dbIsbn, title, author, description, publishYear, fileName);

                // Imposta categoria se presente
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }

                // Valori di default
                book.setIsFree(true);
                book.setIsNew(false);

                System.out.println("✅ Libro trovato: " + title + " di " + author +
                        " (ISBN: " + dbIsbn + ", Categoria: " + category + ")");
                return book;

            } else {
                System.out.println("⚠️ Nessun libro trovato con ISBN: " + isbn);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nella ricerca per ISBN: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Versione asincrona per compatibilità
     */
    public CompletableFuture<Book> getBookByIsbnAsync(String isbn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBookByIsbn(isbn);
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero asincrono del libro: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Ricerca libri per categoria esatta dal database
     */
    public List<Book> getBooksByCategory(String categoryName) {
        System.out.println("🎭 Ricerca nel DB per categoria: '" + categoryName + "'");

        List<Book> books = new ArrayList<>();

        // Query SQL che cerca ESATTAMENTE per categoria (case-insensitive)
        String query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                "WHERE LOWER(TRIM(category)) = LOWER(TRIM(?)) " +
                "ORDER BY books_title";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, categoryName);

            ResultSet rs = stmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category");

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);

                count++;
                if (count <= 3) {
                    System.out.println("🎭 Risultato " + count + ": " + title + " (Categoria DB: '" + category + "')");
                }
            }

            System.out.println("🎭 Ricerca categoria COMPLETATA: trovati " + books.size() + " libri per '" + categoryName + "'");

        } catch (SQLException e) {
            System.err.println("❌ Errore SQL durante la ricerca per categoria: " + e.getMessage());
            e.printStackTrace();

            // Fallback: prova con ricerca LIKE se la ricerca esatta non funziona
            System.out.println("🔄 Fallback: ricerca con LIKE per categoria");
            books = getBooksByCategoryLike(categoryName);
        }

        return books;
    }

    /**
     * Fallback: ricerca categoria con LIKE
     */
    private List<Book> getBooksByCategoryLike(String categoryName) {
        List<Book> books = new ArrayList<>();

        String query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books " +
                "WHERE LOWER(category) LIKE LOWER(?) " +
                "ORDER BY books_title";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + categoryName + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category");

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);
            }

            System.out.println("🔄 Fallback LIKE trovato " + books.size() + " libri");

        } catch (SQLException e) {
            System.err.println("❌ Errore anche nel fallback categoria: " + e.getMessage());
        }

        return books;
    }

    /**
     * Libri di fallback quando il database non è disponibile
     */
    private void addFallbackBooks(List<Book> books) {
        Book book1 = new Book(1L, "978-88-452-9039-1", "Il Nome della Rosa", "Umberto Eco",
                "Un affascinante thriller medievale ambientato in un'abbazia benedettina nel XIV secolo. Il frate francescano Guglielmo da Baskerville e il suo discepolo Adso da Melk indagano su una serie di misteriose morti in un'abbazia italiana.",
                "1980", "placeholder.jpg");
        book1.setCategory("Giallo/Thriller"); // ✅ IMPOSTA CATEGORIA
        books.add(book1);

        Book book2 = new Book(2L, "978-88-04-68451-1", "1984", "George Orwell",
                "Un romanzo distopico sul totalitarismo e la sorveglianza di massa. Winston Smith vive in un mondo dove il Grande Fratello controlla ogni aspetto della vita e dove la verità è manipolata dal Partito.",
                "1949", "placeholder.jpg");
        book2.setCategory("Fantascienza"); // ✅ IMPOSTA CATEGORIA
        books.add(book2);

        Book book3 = new Book(3L, "978-88-04-71854-5", "Orgoglio e Pregiudizio", "Jane Austen",
                "Il romanzo più famoso di Jane Austen racconta la storia di Elizabeth Bennet e del suo complicato rapporto con l'orgoglioso signor Darcy. Un classico della letteratura che esplora temi di amore, classe sociale e crescita personale.",
                "1813", "placeholder.jpg");
        book3.setCategory("Romance"); // ✅ IMPOSTA CATEGORIA
        books.add(book3);

        Book book4 = new Book(4L, "978-88-04-66289-3", "Il Signore degli Anelli", "J.R.R. Tolkien",
                "La saga epica che ha definito il genere fantasy moderno. Segui Frodo Baggins nel suo pericoloso viaggio per distruggere l'Anello del Potere e salvare la Terra di Mezzo dalle forze oscure di Sauron.",
                "1954", "placeholder.jpg");
        book4.setCategory("Fantasy"); // ✅ IMPOSTA CATEGORIA
        books.add(book4);

        Book book5 = new Book(5L, "978-88-04-59847-2", "Cento anni di solitudine", "Gabriel García Márquez",
                "Un capolavoro del realismo magico che narra la storia multigenerazionale della famiglia Buendía nel villaggio immaginario di Macondo. Un'opera che mescla realtà e fantasia in modo magistrale.",
                "1967", "placeholder.jpg");
        book5.setCategory("Narrativa"); // ✅ IMPOSTA CATEGORIA
        books.add(book5);

        System.out.println("📚 Aggiunti " + books.size() + " libri di fallback con categorie");
    }

    /**
     * Recupera i 3 libri con il maggior numero di recensioni con dati completi
     */
    public List<Book> getMostReviewedBooksWithDetails() {
        System.out.println("🏆 Recupero libri più recensiti con dettagli");

        List<Book> mostReviewed = new ArrayList<>();

        String query = """
        SELECT b.isbn, b.books_title, b.book_author, b.description, b.publi_year, 
               COUNT(a.isbn) as review_count,
               AVG(a.average) as avg_rating
        FROM books b
        INNER JOIN assessment a ON b.isbn = a.isbn
        GROUP BY b.isbn, b.books_title, b.book_author, b.description, b.publi_year
        ORDER BY review_count DESC, avg_rating DESC
        LIMIT 8
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Long id = 1L;
            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                int reviewCount = rs.getInt("review_count");
                double avgRating = rs.getDouble("avg_rating");

                // Genera nome file immagine
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // Crea libro con dati completi
                Book book = new Book(id++, isbn, title, author, description, year, fileName);
                book.setIsFree(true);
                book.setIsNew(false);

                // IMPORTANTE: Salva i dati delle recensioni nel libro per uso nella UI
                book.setReviewCount(reviewCount);
                book.setAverageRating(Math.round(avgRating * 10.0) / 10.0); // Arrotonda a 1 decimale

                mostReviewed.add(book);

                System.out.println("📊 " + title + " - " + reviewCount + " recensioni, media: " + avgRating);
            }

            System.out.println("✅ Recuperati " + mostReviewed.size() + " libri più recensiti");

        } catch (SQLException e) {
            System.err.println("❌ Errore nel recupero libri più recensiti: " + e.getMessage());
            e.printStackTrace();

            // Fallback: usa i featured books
            System.out.println("⚠️ Fallback ai libri featured");
            mostReviewed = getFeaturedBooks();

            // Aggiungi valutazioni simulate per il fallback
            for (Book book : mostReviewed) {
                book.setReviewCount((int)(Math.random() * 50) + 10); // 10-60 recensioni simulate
                book.setAverageRating(Math.round((3.5 + Math.random() * 1.5) * 10.0) / 10.0); // 3.5-5.0 rating
            }
        }

        return mostReviewed;
    }

    /**
     * Recupera i 6 libri con la valutazione media più alta
     */
    public List<Book> getTopRatedBooksWithDetails() {
        System.out.println("🏆 Recupero 6 libri con valutazione assoluta più alta");

        List<Book> topRated = new ArrayList<>();

        String query = """
        SELECT b.isbn, b.books_title, b.book_author, b.description, b.publi_year, 
               COUNT(a.isbn) as review_count,
               AVG(a.average) as avg_rating
        FROM books b
        INNER JOIN assessment a ON b.isbn = a.isbn
        GROUP BY b.isbn, b.books_title, b.book_author, b.description, b.publi_year
        ORDER BY avg_rating DESC, review_count DESC
        LIMIT 8
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Long id = 1L;
            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                int reviewCount = rs.getInt("review_count");
                double avgRating = rs.getDouble("avg_rating");

                // Genera nome file immagine
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // Crea libro con dati completi
                Book book = new Book(id++, isbn, title, author, description, year, fileName);
                book.setIsFree(true);
                book.setIsNew(false);

                // Salva i dati delle valutazioni
                book.setReviewCount(reviewCount);
                book.setAverageRating(Math.round(avgRating * 10.0) / 10.0); // Arrotonda a 1 decimale

                topRated.add(book);

                System.out.println("⭐ " + title + " - media: " + avgRating + " (" + reviewCount + " recensioni)");
            }

            System.out.println("✅ Recuperati " + topRated.size() + " libri meglio valutati");

        } catch (SQLException e) {
            System.err.println("❌ Errore nel recupero libri meglio valutati: " + e.getMessage());
            e.printStackTrace();

            // Fallback: usa i new releases con valutazioni simulate
            System.out.println("⚠️ Fallback ai new releases");
            topRated = getNewReleases();

            // Aggiungi valutazioni simulate per il fallback (4.0-5.0 per "meglio valutati")
            for (Book book : topRated) {
                book.setReviewCount((int)(Math.random() * 30) + 15); // 15-45 recensioni simulate
                book.setAverageRating(Math.round((4.0 + Math.random() * 1.0) * 10.0) / 10.0); // 4.0-5.0 rating
            }

            // Assicurati di avere esattamente 6 libri
            if (topRated.size() > 6) {
                topRated = topRated.subList(0, 6);
            }
        }

        return topRated;
    }

    /**
     * ===============================
     * METODI ADMIN PER GESTIONE LIBRI
     * ===============================
     */

    /**
     * Aggiunge un nuovo libro al database (solo per admin)
     */
    public boolean addBook(String isbn, String title, String author, String description, String year, String category) {
        System.out.println("📚 Aggiunta nuovo libro: " + title);

        // Validazione base
        if (isbn == null || isbn.trim().isEmpty() ||
                title == null || title.trim().isEmpty() ||
                author == null || author.trim().isEmpty()) {
            System.err.println("❌ Dati libro incompleti");
            return false;
        }

        // Verifica che l'ISBN non esista già
        if (bookExistsByIsbn(isbn.trim())) {
            System.err.println("❌ Libro con ISBN " + isbn + " già esistente");
            return false;
        }

        String query = "INSERT INTO books (isbn, books_title, book_author, description, publi_year, category) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            stmt.setString(2, title.trim());
            stmt.setString(3, author.trim());
            stmt.setString(4, description != null ? description.trim() : "");
            stmt.setString(5, year != null ? year.trim() : "");
            stmt.setString(6, category != null ? category.trim() : "");

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Libro aggiunto con successo: " + title + " (ISBN: " + isbn + ")");
                return true;
            } else {
                System.err.println("❌ Nessuna riga inserita");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore inserimento libro: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un libro dal database (solo per admin)
     */
    public boolean deleteBook(String isbn) {
        System.out.println("🗑️ Eliminazione libro con ISBN: " + isbn);

        // Validazione
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("❌ ISBN non valido");
            return false;
        }

        String query = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Libro eliminato con successo: ISBN " + isbn);
                return true;
            } else {
                System.err.println("❌ Nessun libro trovato con ISBN: " + isbn);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore eliminazione libro: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Aggiorna un libro esistente nel database (solo per admin)
     */
    public boolean updateBook(String isbn, String title, String author, String description, String year, String category) {
        System.out.println("📝 Aggiornamento libro con ISBN: " + isbn);

        // Validazione
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("❌ ISBN non valido");
            return false;
        }

        String query = "UPDATE books SET books_title = ?, book_author = ?, description = ?, publi_year = ?, category = ? WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title != null ? title.trim() : "");
            stmt.setString(2, author != null ? author.trim() : "");
            stmt.setString(3, description != null ? description.trim() : "");
            stmt.setString(4, year != null ? year.trim() : "");
            stmt.setString(5, category != null ? category.trim() : "");
            stmt.setString(6, isbn.trim());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Libro aggiornato con successo: " + title + " (ISBN: " + isbn + ")");
                return true;
            } else {
                System.err.println("❌ Nessun libro trovato con ISBN: " + isbn);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore aggiornamento libro: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica se un libro esiste già nel database tramite ISBN
     */
    private boolean bookExistsByIsbn(String isbn) {
        String query = "SELECT COUNT(*) FROM books WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore verifica esistenza libro: " + e.getMessage());
        }

        return false;
    }

    /**
     * Recupera tutti i libri con informazioni estese (per admin)
     */
    public List<Book> getAllBooksForAdmin() {
        System.out.println("👑 Recupero tutti i libri per admin");

        List<Book> books = new ArrayList<>();
        String query = "SELECT isbn, books_title, book_author, description, publi_year, category FROM books ORDER BY books_title";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("books_title");
                String author = rs.getString("book_author");
                String description = rs.getString("description");
                String year = rs.getString("publi_year");
                String category = rs.getString("category");

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : "placeholder.jpg";

                Book book = new Book(id, isbn, title, author, description, year, fileName);
                if (category != null && !category.trim().isEmpty()) {
                    book.setCategory(category);
                }
                books.add(book);
            }

            System.out.println("✅ Recuperati " + books.size() + " libri per admin");

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero libri admin: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }
}