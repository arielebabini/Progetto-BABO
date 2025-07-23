package org.BABO.server.service;

import org.BABO.shared.model.Book;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio per gestire le operazioni sui libri
 * Connessione al database PostgreSQL con gestione corretta di ISBN e titolo
 */
@Service
public class BookService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Recupera tutti i libri dal database
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT isbn, books_title, book_author, description, publi_year FROM books ORDER BY books_title";

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

                // Crea ID sequenziale per compatibilità
                Long id = (long) (books.size() + 1);

                // Genera nome file immagine basato su ISBN
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                // Crea Book con tutti i campi
                Book book = new Book(id, isbn, title, author, description, year, fileName);
                books.add(book);

                System.out.println("📖 Caricato: " + title + " (ISBN: " + isbn + ") di " + author + " (" + year + ")");
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
     */
    public List<Book> searchBooks(String searchQuery) {
        System.out.println("🔍 Ricerca generica per: '" + searchQuery + "'");

        List<Book> books = new ArrayList<>();
        String query = "SELECT isbn, books_title, book_author, description, publi_year FROM books " +
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

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                books.add(new Book(id, isbn, title, author, description, year, fileName));
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
     */
    public List<Book> searchBooksByTitle(String titleQuery) {
        System.out.println("📖 Ricerca per TITOLO: '" + titleQuery + "'");

        List<Book> books = new ArrayList<>();
        String query = "SELECT isbn, books_title, book_author, description, publi_year FROM books " +
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

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                books.add(new Book(id, isbn, title, author, description, year, fileName));

                System.out.println("📖 Trovato titolo: " + title + " (ISBN: " + isbn + ", " + year + ")");
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
     */
    public List<Book> searchBooksByAuthor(String authorQuery) {
        System.out.println("👤 Ricerca per AUTORE: '" + authorQuery + "'");

        List<Book> books = new ArrayList<>();
        String query = "SELECT isbn, books_title, book_author, description, publi_year FROM books " +
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

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                books.add(new Book(id, isbn, title, author, description, year, fileName));

                System.out.println("👤 Trovato autore: " + author + " - " + title + " (ISBN: " + isbn + ", " + year + ")");
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
            query = "SELECT isbn, books_title, book_author, description, publi_year FROM books " +
                    "WHERE LOWER(book_author) LIKE LOWER(?) AND CAST(publi_year AS TEXT) = ? " +
                    "ORDER BY books_title";
            System.out.println("📊 Query con FILTRO ANNO: " + query);
        } else {
            query = "SELECT isbn, books_title, book_author, description, publi_year FROM books " +
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

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

                books.add(new Book(id, isbn, title, author, description, dbYear, fileName));

                count++;
                if (count <= 3) {
                    System.out.println("👤📅 Risultato " + count + ": " + author + " - " + title + " (ISBN: " + isbn + ", " + dbYear + ")");
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
     * Recupera libri gratuiti (primi 8)
     */
    public List<Book> getFreeBooks() {
        List<Book> allBooks = getAllBooks();
        int endIndex = Math.min(8, allBooks.size());
        return allBooks.subList(0, endIndex);
    }

    /**
     * Recupera nuove uscite (ultimi libri disponibili)
     */
    public List<Book> getNewReleases() {
        List<Book> allBooks = getAllBooks();
        if (allBooks.size() <= 8) {
            return allBooks;
        }

        int startIndex = Math.max(0, allBooks.size() - 8);
        return allBooks.subList(startIndex, allBooks.size());
    }

    /**
     * Recupera un libro per ISBN
     */
    public Book getBookByIsbn(String isbn) {
        System.out.println("🔍 Ricerca libro per ISBN: " + isbn);

        if (isbn == null || isbn.trim().isEmpty()) {
            System.out.println("⚠️ ISBN vuoto o null");
            return null;
        }

        String query = "SELECT * FROM books WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Crea il libro con i campi della tua tabella
                Book book = new Book();

                // Campi obbligatori
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("books_title"));
                book.setAuthor(rs.getString("book_author"));
                book.setDescription(rs.getString("description"));
                book.setPublishYear(rs.getString("publi_year"));
                book.setImageUrl(rs.getString("image_url_m"));

                // Campi opzionali
                if (rs.getString("category") != null) {
                    book.setCategory(rs.getString("category"));
                }
                if (rs.getString("publisher") != null) {
                    book.setPublisher(rs.getString("publisher"));
                }

                // Imposta valori di default per campi mancanti
                book.setIsFree(true);
                book.setIsNew(false);

                System.out.println("✅ Libro trovato: " + book.getTitle());
                return book;
            } else {
                System.out.println("⚠️ Nessun libro trovato con ISBN: " + isbn);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore nella ricerca per ISBN: " + e.getMessage());
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
     * Aggiunge libri di fallback quando il database non è disponibile
     */
    private void addFallbackBooks(List<Book> books) {
        books.add(new Book(1L, "978-88-452-9039-1", "Il Nome della Rosa", "Umberto Eco",
                "Un affascinante thriller medievale ambientato in un'abbazia benedettina nel XIV secolo. Il frate francescano Guglielmo da Baskerville e il suo discepolo Adso da Melk indagano su una serie di misteriose morti in un'abbazia italiana.",
                "1980", "placeholder.jpg"));

        books.add(new Book(2L, "978-88-04-68451-1", "1984", "George Orwell",
                "Un romanzo distopico sul totalitarismo e la sorveglianza di massa. Winston Smith vive in un mondo dove il Grande Fratello controlla ogni aspetto della vita e dove la verità è manipolata dal Partito.",
                "1949", "placeholder.jpg"));

        books.add(new Book(3L, "978-88-452-7891-8", "Il Piccolo Principe", "Antoine de Saint-Exupéry",
                "Una fiaba poetica che ha conquistato il cuore di lettori di tutte le età. La storia di un giovane principe che viaggia di pianeta in pianeta, incontrando strani adulti e imparando lezioni sulla vita e sull'amore.",
                "1943", "placeholder.jpg"));

        books.add(new Book(4L, "978-88-17-12345-6", "Orgoglio e Pregiudizio", "Jane Austen",
                "Un classico romanzo romantico dell'epoca georgiana che esplora temi di amore, classe sociale e crescita personale attraverso la storia di Elizabeth Bennet e Mr. Darcy.",
                "1813", "placeholder.jpg"));

        books.add(new Book(5L, "978-88-452-6789-0", "Il Signore degli Anelli", "J.R.R. Tolkien",
                "L'epica avventura fantasy per eccellenza. Frodo Baggins e la Compagnia dell'Anello intraprendono un pericoloso viaggio per distruggere l'Unico Anello e sconfiggere il Signore Oscuro Sauron.",
                "1954", "placeholder.jpg"));

        books.add(new Book(6L, "978-88-04-51234-7", "To Kill a Mockingbird", "Harper Lee",
                "Un potente romanzo sulla giustizia e i diritti civili ambientato nel Sud degli Stati Uniti negli anni '30. La storia è narrata attraverso gli occhi della giovane Scout Finch.",
                "1960", "placeholder.jpg"));

        books.add(new Book(7L, "978-88-17-98765-4", "Il Grande Gatsby", "F. Scott Fitzgerald",
                "Un ritratto della società americana degli anni '20 attraverso la storia del misterioso milionario Jay Gatsby e del suo amore impossibile per Daisy Buchanan.",
                "1925", "placeholder.jpg"));

        books.add(new Book(8L, "978-88-452-1357-9", "Cento Anni di Solitudine", "Gabriel García Márquez",
                "Un capolavoro del realismo magico latinoamericano che narra la storia multigenerazionale della famiglia Buendía nel villaggio immaginario di Macondo.",
                "1967", "placeholder.jpg"));

        books.add(new Book(9L, "978-88-04-86420-1", "Harry Potter e la Pietra Filosofale", "J.K. Rowling",
                "Il primo libro della celebre saga che introduce Harry Potter, un giovane mago che scopre il suo destino nel mondo magico di Hogwarts.",
                "1997", "placeholder.jpg"));

        books.add(new Book(10L, "978-88-452-2468-0", "Il Codice da Vinci", "Dan Brown",
                "Un thriller che mescola arte, storia e mistero quando il professor Robert Langdon viene coinvolto in un'indagine su un omicidio al Louvre.",
                "2003", "placeholder.jpg"));
    }

    /**
     * Metodo di utilità per verificare la connessione al database
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}