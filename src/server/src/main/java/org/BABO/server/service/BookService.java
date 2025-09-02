package org.BABO.server.service;

import org.BABO.shared.model.Book;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio business per la gestione del catalogo libri, fungendo da strato di accesso dati (Repository)
 * per l'applicazione BABO (Book and Book Organization).
 * <p>
 * Questa classe è responsabile dell'interazione diretta con il database PostgreSQL per recuperare,
 * cercare e manipolare i dati dei libri. Incapsula la logica di accesso ai dati, separandola
 * dalla logica di business e di presentazione del {@link org.BABO.server.controller.BookController}.
 * Il servizio implementa query SQL ottimizzate per garantire performance e scalabilità.
 * </p>
 *
 * <h3>Funzionalità Principali:</h3>
 * <ul>
 * <li><strong>Recupero Completo:</strong> Ritorna l'intero catalogo di libri.</li>
 * <li><strong>Ricerca Avanzata:</strong> Supporta la ricerca per titolo, autore, genere e anno di pubblicazione.</li>
 * <li><strong>Accesso Dettagliato:</strong> Permette di recuperare un libro specifico tramite il suo ID.</li>
 * <li><strong>Gestione di Fallback:</strong> In caso di fallimento della connessione al database,
 * fornisce un set di libri "di riserva" per mantenere l'applicazione funzionante.</li>
 * </ul>
 *
 * <h3>Architettura e Design Pattern:</h3>
 * <p>
 * Il servizio segue il pattern del Repository, astraendo le operazioni di persistenza
 * e fornendo un'interfaccia chiara ai layer superiori.
 * </p>
 * <ul>
 * <li><strong>Repository Pattern:</strong> Centralizza le operazioni di accesso ai dati.</li>
 * <li><strong>JDBC Standard:</strong> Utilizza l'API Java Database Connectivity per la
 * connessione e l'esecuzione delle query.</li>
 * <li><strong>Query Ottimizzate:</strong> Le query sono parametrizzate per prevenire
 * SQL injection e ottimizzate per la ricerca.</li>
 * </ul>
 *
 * <h3>Resilienza e Gestione Errori:</h3>
 * <p>
 * La classe è progettata con un meccanismo di fallback per garantire la resilienza
 * del servizio in caso di problemi di connessione al database.
 * </p>
 * <ul>
 * <li><strong>Connessione Sicura:</strong> Gestisce la connessione al database con try-with-resources.</li>
 * <li><strong>Fallback Data:</strong> Se la connessione fallisce, viene fornito un set
 * predefinito di libri per evitare un'interruzione totale del servizio.</li>
 * <li><strong>Logging Dettagliato:</strong> Log di successo e di errore per monitorare
 * lo stato delle operazioni.</li>
 * </ul>
 *
 * @author BABO Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see org.BABO.shared.model.Book
 * @see org.BABO.server.controller.BookController
 */
@Service
public class BookService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Recupera l'intero catalogo di libri dal database PostgreSQL.
     * <p>
     * Questo metodo si connette al database, esegue una query per selezionare
     * tutti i libri e popola una lista di oggetti {@link Book}. In caso di
     * fallimento della connessione o della query, attiva il meccanismo di
     * fallback.
     * </p>
     *
     * <h4>Query SQL eseguita:</h4>
     * <pre>{@code
     * SELECT isbn, books_title, book_author, description, publi_year, category
     * FROM books
     * ORDER BY books_title
     * }</pre>
     *
     * <h4>Gestione degli errori:</h4>
     * <p>
     * Se si verifica una {@link SQLException}, il metodo stampa un messaggio di
     * errore e invoca {@link #addFallbackBooks(List)} per caricare un set
     * di dati predefinito.
     * </p>
     *
     * @return Una {@link List} di {@link Book} contenente tutti i libri trovati
     * nel database o i libri di fallback.
     * @see #addFallbackBooks(List)
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
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
                String category = rs.getString("category");

                // Crea ID sequenziale per compatibilità
                Long id = (long) (books.size() + 1);

                // Genera nome file immagine basato su ISBN
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

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
     * Recupera un libro specifico dal catalogo in base al suo ID.
     * <p>
     * Questo metodo recupera prima l'intero catalogo di libri e quindi cerca
     * il libro corrispondente all'ID specificato.
     * </p>
     *
     * @param id L'ID numerico del libro da recuperare.
     * @return L'oggetto {@link Book} se trovato, altrimenti {@code null}.
     * @see #getAllBooks()
     */
    public Book getBookById(Long id) {
        List<Book> allBooks = getAllBooks();
        return allBooks.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Esegue una ricerca generica nel catalogo per titolo o autore.
     * <p>
     * La ricerca è case-insensitive e utilizza il pattern {@code LIKE %...%}
     * per trovare corrispondenze parziali.
     * </p>
     *
     * <h4>Query SQL eseguita:</h4>
     * <pre>{@code
     * SELECT isbn, books_title, book_author, description, publi_year, category
     * FROM books
     * WHERE LOWER(books_title) LIKE LOWER(?) OR LOWER(book_author) LIKE LOWER(?)
     * ORDER BY books_title
     * }</pre>
     *
     * @param searchQuery La stringa di ricerca per titolo o autore.
     * @return Una {@link List} di {@link Book} che corrispondono alla ricerca.
     * @see #searchInFallbackBooks(String)
     */
    public List<Book> searchBooks(String searchQuery) {
        System.out.println("🔍 Ricerca generica per: '" + searchQuery + "'");

        List<Book> books = new ArrayList<>();
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

            System.out.println("🔍 Ricerca generica '" + searchQuery + "': trovati " + books.size() + " risultati nel database");

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la ricerca nel database: " + e.getMessage());
            books = searchInFallbackBooks(searchQuery);
        }

        return books;
    }

    /**
     * Esegue una ricerca specifica di libri basata solo sul titolo.
     * <p>
     * Utilizza un'espressione {@code LIKE} per trovare libri il cui titolo
     * contiene la stringa di ricerca, ignorando la distinzione tra maiuscole e minuscole.
     * </p>
     *
     * @param titleQuery La stringa di ricerca per il titolo.
     * @return Una {@link List} di {@link Book} che corrispondono al criterio di ricerca.
     */
    public List<Book> searchBooksByTitle(String titleQuery) {
        System.out.println("📖 Ricerca per TITOLO: '" + titleQuery + "'");

        List<Book> books = new ArrayList<>();
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
     * Esegue una ricerca specifica di libri basata solo sull'autore.
     * <p>
     * Utilizza un'espressione {@code LIKE} per trovare libri il cui autore
     * contiene la stringa di ricerca, ignorando la distinzione tra maiuscole e minuscole.
     * </p>
     *
     * @param authorQuery La stringa di ricerca per l'autore.
     * @return Una {@link List} di {@link Book} che corrispondono al criterio di ricerca.
     */
    public List<Book> searchBooksByAuthor(String authorQuery) {
        System.out.println("👤 Ricerca per AUTORE: '" + authorQuery + "'");

        List<Book> books = new ArrayList<>();
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
     * Esegue una ricerca di libri combinando i criteri di autore e anno di pubblicazione.
     * <p>
     * Questo metodo costruisce la query dinamicamente per supportare la ricerca
     * solo per autore o per autore e anno combinati.
     * </p>
     *
     * @param authorQuery La stringa di ricerca per l'autore.
     * @param year L'anno di pubblicazione.
     * @return Una {@link List} di {@link Book} che corrispondono ai criteri di ricerca.
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
                String category = rs.getString("category");

                Long id = (long) (books.size() + 1);
                String fileName = (isbn != null && !isbn.trim().isEmpty())
                        ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                        : (title != null ? title.replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");

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
     * Esegue una ricerca all'interno della lista di libri di fallback.
     * <p>
     * Questo metodo viene utilizzato come meccanismo di contingenza quando il database
     * non è disponibile. Popola una lista con dati predefiniti e quindi filtra
     * i libri in base a una stringa di ricerca. La ricerca è case-insensitive
     * e verifica la corrispondenza parziale sia nel titolo che nell'autore.
     * Un messaggio di debug viene stampato per registrare il numero di risultati
     * trovati.
     * </p>
     *
     * @param searchQuery La stringa di ricerca che l'utente ha inserito.
     * @return Una {@link List} di {@link Book} che soddisfano i criteri di ricerca
     * all'interno del set di dati di fallback.
     * @see #addFallbackBooks(List)
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
     * Esegue una ricerca all'interno della lista di libri di fallback basata solo sul titolo.
     * <p>
     * Questo metodo di supporto viene invocato quando il database non è disponibile e la
     * ricerca deve essere effettuata nel set di dati di fallback predefinito. Il metodo
     * filtra la lista di fallback per trovare i libri il cui titolo contiene la
     * stringa di ricerca specificata, ignorando la distinzione tra maiuscole e minuscole.
     * </p>
     *
     * @param titleQuery La stringa di ricerca per il titolo.
     * @return Una {@link List} di {@link Book} che corrispondono al criterio di ricerca.
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
     * Esegue una ricerca all'interno della lista di libri di fallback basata solo sull'autore.
     * <p>
     * Questo metodo di supporto viene invocato quando il database non è disponibile.
     * Simile a {@link #searchInFallbackBooksByTitle(String)}, filtra la lista di
     * libri di fallback per trovare le corrispondenze con il nome dell'autore.
     * La ricerca è case-insensitive per garantire che la corrispondenza
     * parziale non dipenda dalla capitalizzazione. Un messaggio di debug
     * viene stampato per monitorare il numero di risultati trovati.
     * </p>
     *
     * @param authorQuery La stringa di ricerca per il nome dell'autore.
     * @return Una {@link List} di {@link Book} che corrispondono al criterio di ricerca.
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
     * Recupera una lista di libri in evidenza dal catalogo.
     * <p>
     * Questo metodo simula la selezione di libri da mettere in evidenza (ad esempio,
     * in una sezione "In Primo Piano" della homepage) prendendo semplicemente i primi
     * tre libri dall'intero catalogo restituito da {@link #getAllBooks()}. La logica
     * è progettata per dimostrare un'interfaccia, e non rappresenta una selezione
     * basata su criteri complessi come popolarità o rating.
     * </p>
     *
     * @return Una {@link List} di {@link Book} che rappresenta i libri in evidenza.
     * @see #getAllBooks()
     */
    public List<Book> getFeaturedBooks() {
        List<Book> allBooks = getAllBooks();
        int endIndex = Math.min(3, allBooks.size());
        return allBooks.subList(0, endIndex);
    }

    /**
     * Recupera una selezione di libri "gratuiti" o a scopo dimostrativo.
     * <p>
     * Questo metodo estrae un sottoinsieme di libri dall'intero catalogo per
     * simulare una sezione di libri gratuiti. La selezione è arbitraria e prende
     * 24 libri a partire dalla metà della lista completa. L'implementazione
     * include un messaggio di debug per tracciare il range di libri selezionati.
     * </p>
     *
     * @return Una {@link List} di {@link Book} che rappresenta i libri gratuiti o
     * il sottoinsieme selezionato. Ritorna una lista vuota se la lista completa
     * non è abbastanza grande.
     * @see #getAllBooks()
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
     * Recupera una lista di libri che simulano le nuove uscite.
     * <p>
     * Questo metodo seleziona gli ultimi 24 libri dall'intero catalogo per
     * rappresentare le "nuove uscite" o i titoli più recenti. L'approccio
     * si basa sull'assunzione che i libri più recenti siano quelli posizionati
     * alla fine della lista restituita da {@link #getAllBooks()}. Se il numero
     * totale di libri è inferiore a 24, l'intero catalogo viene restituito.
     * </p>
     *
     * @return Una {@link List} di {@link Book} che simula le nuove uscite.
     * @see #getAllBooks()
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
     * Recupera un libro specifico dal database utilizzando il suo ISBN.
     * <p>
     * Questo metodo esegue una ricerca mirata nel database basata sull'ISBN (International Standard Book Number).
     * L'ISBN viene utilizzato come identificatore univoco per trovare il libro esatto. La query
     * è parametrizzata per prevenire attacchi di tipo SQL injection. Una volta trovato il libro,
     * i dati vengono mappati a un oggetto {@link Book}, con un ID generato e un nome di file immagine
     * standardizzato.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * SELECT isbn, books_title, book_author, description, publi_year, category
     * FROM books
     * WHERE isbn = ?
     * }</pre>
     *
     * <h3>Gestione degli errori:</h3>
     * <p>
     * In caso di eccezioni {@link SQLException} durante l'esecuzione della query, viene
     * stampato un messaggio di errore e restituito {@code null} per indicare il fallimento
     * dell'operazione.
     * </p>
     *
     * @param isbn La stringa ISBN del libro da cercare.
     * @return L'oggetto {@link Book} corrispondente all'ISBN, o {@code null} se non viene trovato
     * alcun libro o se si verifica un errore.
     */
    public Book getBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }

        System.out.println("🔍 Ricerca libro per ISBN: " + isbn);

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
                String title = rs.getString("books_title");
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
     * Recupera una lista di libri dal database in base a una categoria specifica.
     * <p>
     * Questo metodo esegue una ricerca esatta nel database per trovare i libri
     * che appartengono a una determinata categoria (genere). La query è
     * ottimizzata per ignorare gli spazi e la capitalizzazione, garantendo
     * una corrispondenza precisa. Se la ricerca esatta fallisce a causa di un
     * errore SQL (ad esempio, un problema di connessione), il metodo attiva un
     * meccanismo di fallback e tenta una ricerca meno restrittiva
     * (tramite {@link #getBooksByCategoryLike(String)}) per massimizzare le possibilità
     * di trovare un risultato.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * SELECT isbn, books_title, book_author, description, publi_year, category
     * FROM books
     * WHERE LOWER(TRIM(category)) = LOWER(TRIM(?))
     * ORDER BY books_title
     * }</pre>
     *
     * <h3>Gestione degli errori:</h3>
     * <p>
     * Il metodo gestisce le {@link SQLException} e, in caso di errore, esegue il
     * fallback a una ricerca "similare" per la stessa categoria.
     * </p>
     *
     * @param categoryName Il nome della categoria (genere) da cercare.
     * @return Una {@link List} di {@link Book} che corrispondono alla categoria specificata.
     * @see #getBooksByCategoryLike(String)
     */
    public List<Book> getBooksByCategory(String categoryName) {
        System.out.println("🎭 Ricerca nel DB per categoria: '" + categoryName + "'");

        List<Book> books = new ArrayList<>();

        // Query SQL che cerca ESATTAMENTE per categoria
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
     * Esegue una ricerca meno restrittiva nel database per trovare libri per categoria.
     * <p>
     * Questo metodo di utilità è un meccanismo di fallback per {@link #getBooksByCategory(String)}.
     * A differenza della ricerca esatta, utilizza un'operazione {@code LIKE} per trovare
     * libri la cui categoria contiene la stringa di ricerca specificata. La query
     * è case-insensitive per massimizzare le corrispondenze. Viene utilizzato quando
     * una ricerca precisa fallisce, ad esempio, a causa di un'incongruenza
     * nei dati o un errore di connessione.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * SELECT isbn, books_title, book_author, description, publi_year, category
     * FROM books
     * WHERE LOWER(category) LIKE LOWER(?)
     * ORDER BY books_title
     * }</pre>
     *
     * @param categoryName La stringa di ricerca per la categoria.
     * @return Una {@link List} di {@link Book} che corrispondono al criterio di ricerca "similare".
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
     * Aggiunge un set predefinito di libri a una lista per fungere da dati di fallback.
     * <p>
     * Questo metodo di supporto crea e popola una lista di oggetti {@link Book} con dati
     * statici. Viene invocato come meccanismo di contingenza quando la connessione al
     * database non riesce, garantendo che l'applicazione non si presenti vuota all'utente.
     * I libri di fallback includono classici e opere di diversi generi per simulare un
     * catalogo diversificato.
     * </p>
     *
     * @param books La {@link List} di {@link Book} a cui aggiungere i dati di fallback.
     */
    private void addFallbackBooks(List<Book> books) {
        Book book1 = new Book(1L, "978-88-452-9039-1", "Il Nome della Rosa", "Umberto Eco",
                "Un affascinante thriller medievale ambientato in un'abbazia benedettina nel XIV secolo. Il frate francescano Guglielmo da Baskerville e il suo discepolo Adso da Melk indagano su una serie di misteriose morti in un'abbazia italiana.",
                "1980", "placeholder.jpg");
        book1.setCategory("Giallo/Thriller");
        books.add(book1);

        Book book2 = new Book(2L, "978-88-04-68451-1", "1984", "George Orwell",
                "Un romanzo distopico sul totalitarismo e la sorveglianza di massa. Winston Smith vive in un mondo dove il Grande Fratello controlla ogni aspetto della vita e dove la verità è manipolata dal Partito.",
                "1949", "placeholder.jpg");
        book2.setCategory("Fantascienza");
        books.add(book2);

        Book book3 = new Book(3L, "978-88-04-71854-5", "Orgoglio e Pregiudizio", "Jane Austen",
                "Il romanzo più famoso di Jane Austen racconta la storia di Elizabeth Bennet e del suo complicato rapporto con l'orgoglioso signor Darcy. Un classico della letteratura che esplora temi di amore, classe sociale e crescita personale.",
                "1813", "placeholder.jpg");
        book3.setCategory("Romance");
        books.add(book3);

        Book book4 = new Book(4L, "978-88-04-66289-3", "Il Signore degli Anelli", "J.R.R. Tolkien",
                "La saga epica che ha definito il genere fantasy moderno. Segui Frodo Baggins nel suo pericoloso viaggio per distruggere l'Anello del Potere e salvare la Terra di Mezzo dalle forze oscure di Sauron.",
                "1954", "placeholder.jpg");
        book4.setCategory("Fantasy");
        books.add(book4);

        Book book5 = new Book(5L, "978-88-04-59847-2", "Cento anni di solitudine", "Gabriel García Márquez",
                "Un capolavoro del realismo magico che narra la storia multigenerazionale della famiglia Buendía nel villaggio immaginario di Macondo. Un'opera che mescla realtà e fantasia in modo magistrale.",
                "1967", "placeholder.jpg");
        book5.setCategory("Narrativa");
        books.add(book5);

        System.out.println("📚 Aggiunti " + books.size() + " libri di fallback con categorie");
    }

    /**
     * Recupera una lista di libri con i dettagli delle recensioni più alte.
     * <p>
     * Questo metodo esegue una query SQL complessa per selezionare gli 8 libri più recensiti
     * dal database, calcolando anche il numero totale di recensioni e la media del voto.
     * La query utilizza `INNER JOIN` e `GROUP BY` per aggregare i dati e `ORDER BY`
     * per ordinarli in base al numero di recensioni e alla valutazione media, restituendo
     * i libri più popolari e meglio votati.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * SELECT b.isbn, b.books_title, b.book_author, b.description, b.publi_year,
     * COUNT(a.isbn) as review_count,
     * AVG(a.average) as avg_rating
     * FROM books b
     * INNER JOIN assessment a ON b.isbn = a.isbn
     * GROUP BY b.isbn, b.books_title, b.book_author, b.description, b.publi_year
     * ORDER BY review_count DESC, avg_rating DESC
     * LIMIT 8
     * }</pre>
     *
     * <h3>Gestione degli errori e Fallback:</h3>
     * <p>
     * In caso di errore {@link SQLException} durante l'esecuzione della query, il metodo
     * stampa un messaggio di errore e attiva un meccanismo di fallback. Invece di restituire
     * una lista vuota, invoca {@link #getFeaturedBooks()} e aggiunge dati di recensione
     * simulati per garantire che l'interfaccia utente abbia sempre qualcosa da visualizzare.
     * </p>
     *
     * @return Una {@link List} di {@link Book} che contiene gli 8 libri più recensiti
     * e la loro valutazione, o un set di libri di fallback in caso di errore.
     * @see #getFeaturedBooks()
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

                // Salva i dati delle recensioni nel libro per uso nella UI
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
     * Recupera una lista di libri con la valutazione più alta dal database.
     * <p>
     * Questo metodo esegue una query SQL complessa per selezionare gli 8 libri con la media
     * di voto più alta, calcolando anche il numero di recensioni totali per ogni libro.
     * La query utilizza `INNER JOIN` per collegare la tabella dei libri e quella delle
     * valutazioni, e ordina i risultati in base al punteggio medio e al numero di
     * recensioni per garantire che vengano restituiti i libri più popolari e meglio votati.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * SELECT b.isbn, b.books_title, b.book_author, b.description, b.publi_year,
     * COUNT(a.isbn) as review_count,
     * AVG(a.average) as avg_rating
     * FROM books b
     * INNER JOIN assessment a ON b.isbn = a.isbn
     * GROUP BY b.isbn, b.books_title, b.book_author, b.description, b.publi_year
     * ORDER BY avg_rating DESC, review_count DESC
     * LIMIT 8
     * }</pre>
     *
     * <h3>Gestione degli errori e Fallback:</h3>
     * <p>
     * In caso di errore {@link SQLException}, il metodo stampa un messaggio di errore
     * e attiva un meccanismo di fallback. Questo meccanismo recupera i libri
     * dalle "nuove uscite" (tramite {@link #getNewReleases()}) e aggiunge
     * dati di recensione simulati per garantire che l'interfaccia utente abbia
     * sempre qualcosa da visualizzare, evitando un'interruzione totale del servizio.
     * </p>
     *
     * @return Una {@link List} di {@link Book} che contiene gli 8 libri con la valutazione
     * più alta, o un set di libri di fallback in caso di errore.
     * @see #getNewReleases()
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
     * Aggiunge un nuovo libro al database.
     * <p>
     * Questo metodo gestisce il processo di inserimento di un nuovo libro nel catalogo.
     * Prima di procedere, esegue una validazione di base sui dati forniti e verifica
     * che l'ISBN non sia già presente nel database per evitare duplicati. La query di
     * inserimento è preparata e parametrizzata per garantire la sicurezza e
     * prevenire attacchi di tipo SQL injection.
     * </p>
     *
     * <h3>Passaggi principali:</h3>
     * <ol>
     * <li>Validazione dei campi obbligatori (ISBN, titolo, autore).</li>
     * <li>Verifica dell'esistenza del libro tramite l'ISBN.</li>
     * <li>Esecuzione di una query SQL di tipo `INSERT`.</li>
     * <li>Restituzione di un valore booleano che indica il successo o il fallimento.</li>
     * </ol>
     *
     * @param isbn L'ISBN del libro da aggiungere.
     * @param title Il titolo del libro.
     * @param author L'autore del libro.
     * @param description La descrizione del libro.
     * @param year L'anno di pubblicazione.
     * @param category La categoria o il genere del libro.
     * @return {@code true} se il libro è stato aggiunto con successo, {@code false} altrimenti.
     * @see #bookExistsByIsbn(String)
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
     * Elimina un libro dal database in base al suo ISBN.
     * <p>
     * Questo metodo gestisce la rimozione di un libro dal catalogo. Esegue una
     * validazione iniziale dell'ISBN fornito e quindi esegue una query `DELETE`
     * sul database. La query è parametrizzata per garantire la sicurezza e
     * prevenire l'eliminazione accidentale di dati.
     * </p>
     *
     * <h3>Passaggi principali:</h3>
     * <ol>
     * <li>Validazione dell'ISBN per assicurare che non sia nullo o vuoto.</li>
     * <li>Esecuzione di una query SQL di tipo `DELETE` basata sull'ISBN.</li>
     * <li>Verifica se sono state eliminate righe e restituzione di un risultato booleano.</li>
     * </ol>
     *
     * @param isbn L'ISBN del libro da eliminare.
     * @return {@code true} se il libro è stato eliminato con successo, {@code false} se
     * l'ISBN non è valido o se il libro non è stato trovato nel database.
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
     * Aggiorna i dettagli di un libro esistente nel database.
     * <p>
     * Questo metodo gestisce la modifica di un record di libro. Richiede l'ISBN per
     * identificare in modo univoco il libro da aggiornare e i nuovi valori per gli
     * altri campi. La query di aggiornamento è preparata e parametrizzata per
     * garantire la sicurezza e l'integrità dei dati. Se l'operazione ha successo,
     * viene stampato un messaggio di conferma e il metodo restituisce {@code true}.
     * In caso contrario, ad esempio se l'ISBN non viene trovato o si verifica
     * un errore SQL, restituisce {@code false}.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * UPDATE books SET books_title = ?, book_author = ?, description = ?, publi_year = ?, category = ? WHERE isbn = ?
     * }</pre>
     *
     * @param isbn L'ISBN del libro da aggiornare.
     * @param title Il nuovo titolo del libro.
     * @param author Il nuovo autore del libro.
     * @param description La nuova descrizione del libro.
     * @param year Il nuovo anno di pubblicazione.
     * @param category La nuova categoria del libro.
     * @return {@code true} se il libro è stato aggiornato con successo, {@code false} altrimenti.
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
     * Verifica l'esistenza di un libro nel database utilizzando il suo ISBN.
     * <p>
     * Questo metodo di utilità esegue una query di conteggio per determinare
     * se un libro con un ISBN specifico è già presente nel catalogo. È una
     * funzione cruciale per prevenire l'inserimento di duplicati. La query
     * è ottimizzata per la performance e la sicurezza, restituendo un valore
     * booleano che indica l'esistenza del record.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * SELECT COUNT(*) FROM books WHERE isbn = ?
     * }</pre>
     *
     * @param isbn L'ISBN del libro da verificare.
     * @return {@code true} se il libro esiste, {@code false} altrimenti.
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
     * Recupera l'intero catalogo di libri dal database per l'uso da parte di un utente amministratore.
     * <p>
     * Questo metodo è progettato per recuperare tutti i libri presenti nel database. A differenza di
     * altri metodi di ricerca, non applica filtri o limiti di risultati, garantendo che l'utente
     * amministratore possa visualizzare e gestire l'intera collezione. La lista viene ordinata
     * per titolo. Il metodo include una robusta gestione degli errori per stampare eventuali
     * eccezioni {@link SQLException} che potrebbero verificarsi durante l'accesso al database.
     * </p>
     *
     * <h3>Query SQL eseguita:</h3>
     * <pre>{@code
     * SELECT isbn, books_title, book_author, description, publi_year, category FROM books ORDER BY books_title
     * }</pre>
     *
     * @return Una {@link List} di {@link Book} contenente tutti i libri presenti nel database.
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