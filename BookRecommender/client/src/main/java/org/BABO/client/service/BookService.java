package org.BABO.client.service;

import org.BABO.shared.model.Book;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio client per comunicare con l'API del server
 * Gestisce tutte le chiamate HTTP al backend con ricerca avanzata
 * ✅ AGGIORNATO: Con supporto per categorie
 */
public class BookService {

    private static final String SERVER_BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BookService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verifica se il server è raggiungibile
     */
    public boolean isServerAvailable() {
        try {
            Request request = new Request.Builder()
                    .url(SERVER_BASE_URL + "/books")
                    .head() // Solo un HEAD request per verificare
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            System.err.println("❌ Server non raggiungibile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera tutti i libri dal server in modo asincrono
     */
    public CompletableFuture<List<Book>> getAllBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAllBooks();
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero dei libri: " + e.getMessage());
                return getFallbackBooks();
            }
        });
    }

    /**
     * Recupera tutti i libri dal server
     */
    public List<Book> getAllBooks() throws IOException {
        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> books = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("✅ Recuperati " + books.size() + " libri dal server");
                return books;
            } else {
                throw new IOException("Errore nella richiesta: " + response.code());
            }
        }
    }

    /**
     * ✅ NUOVO: Recupera libri per categoria tramite server (CORRETTO)
     */
    public List<Book> getBooksByCategory(String categoryName) {
        System.out.println("🎯 Ricerca libri per categoria dal server: " + categoryName);

        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.out.println("⚠️ Nome categoria vuoto");
            return new ArrayList<>();
        }

        try {
            // Prima prova a cercare i libri generali e filtra per categoria
            // dato che il server non ha endpoint specifico per categoria
            List<Book> allBooks = getAllBooks();
            List<Book> categoryBooks = new ArrayList<>();

            String categoryLower = categoryName.toLowerCase();

            for (Book book : allBooks) {
                // Filtra per categoria se presente nel campo category
                String bookCategory = book.getCategory();
                if (bookCategory != null && bookCategory.toLowerCase().contains(categoryLower)) {
                    categoryBooks.add(book);
                }
                // Se non ha categoria o per mapping intelligente titolo->categoria
                else if (shouldIncludeBookInCategory(book, categoryName)) {
                    // Assegna categoria al libro
                    book.setCategory(categoryName);
                    categoryBooks.add(book);
                }
            }

            System.out.println("✅ Trovati " + categoryBooks.size() + " libri per categoria '" + categoryName + "' dal database");
            return categoryBooks;

        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca categoria dal server: " + e.getMessage());
            // Solo ora usa il fallback
            return searchInFallbackBooksByCategory(categoryName);
        }
    }

    /**
     * Determina se un libro dovrebbe essere incluso in una categoria basandosi sul contenuto
     */
    private boolean shouldIncludeBookInCategory(Book book, String categoryName) {
        String categoryLower = categoryName.toLowerCase();
        String titleLower = book.getTitle().toLowerCase();
        String authorLower = book.getAuthor().toLowerCase();
        String descLower = book.getDescription() != null ? book.getDescription().toLowerCase() : "";

        switch (categoryLower) {
            case "thriller":
                return titleLower.contains("rosa") || titleLower.contains("mistero") ||
                        descLower.contains("thriller") || descLower.contains("mistero") ||
                        descLower.contains("suspense");

            case "fantasy":
                return titleLower.contains("signore") || titleLower.contains("anelli") ||
                        titleLower.contains("hobbit") || titleLower.contains("principe") ||
                        authorLower.contains("tolkien") || descLower.contains("fantasy") ||
                        descLower.contains("epica") || descLower.contains("avventura");

            case "narrativa":
                return titleLower.contains("1984") || titleLower.contains("gatsby") ||
                        titleLower.contains("mockingbird") || titleLower.contains("solitudine") ||
                        authorLower.contains("orwell") || authorLower.contains("fitzgerald") ||
                        authorLower.contains("harper lee") || authorLower.contains("márquez");

            case "romance":
                return titleLower.contains("orgoglio") || titleLower.contains("pregiudizio") ||
                        authorLower.contains("austen") || descLower.contains("romantico") ||
                        descLower.contains("amore");

            case "saggistica":
                return titleLower.contains("anni") || titleLower.contains("storia") ||
                        descLower.contains("saggio") || descLower.contains("realistico") ||
                        descLower.contains("società");

            default:
                // Per altre categorie, cerca nel titolo, autore o descrizione
                return titleLower.contains(categoryLower) ||
                        authorLower.contains(categoryLower) ||
                        descLower.contains(categoryLower);
        }
    }

    /**
     * ✅ NUOVO: Versione asincrona per getBooksByCategory
     */
    public CompletableFuture<List<Book>> getBooksByCategoryAsync(String categoryName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBooksByCategory(categoryName);
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero asincrono dei libri per categoria: " + e.getMessage());
                return searchInFallbackBooksByCategory(categoryName);
            }
        });
    }

    /**
     * ✅ NUOVO: Ricerca per categoria nei libri di fallback
     */
    private List<Book> searchInFallbackBooksByCategory(String categoryName) {
        System.out.println("🔄 Fallback: ricerca categoria locale per '" + categoryName + "'");

        List<Book> fallbackBooks = getFallbackBooks();
        List<Book> results = new ArrayList<>();
        String categoryLower = categoryName.toLowerCase();

        for (Book book : fallbackBooks) {
            // Controlla se il libro ha una categoria che contiene il termine cercato
            String bookCategory = book.getCategory();
            if (bookCategory != null && bookCategory.toLowerCase().contains(categoryLower)) {
                results.add(book);
            }
            // Fallback: cerca anche nel titolo se non ha categoria o per alcune categorie speciali
            else if (bookCategory == null || bookCategory.isEmpty() || shouldCheckTitleForCategory(categoryName, book)) {
                if (book.getTitle().toLowerCase().contains(categoryLower) ||
                        (book.getDescription() != null && book.getDescription().toLowerCase().contains(categoryLower))) {
                    results.add(book);
                }
            }
        }

        System.out.println("📚 Ricerca categoria fallback '" + categoryName + "': trovati " + results.size() + " risultati");
        return results;
    }

    /**
     * Determina se cercare nel titolo per una specifica categoria
     */
    private boolean shouldCheckTitleForCategory(String categoryName, Book book) {
        String categoryLower = categoryName.toLowerCase();
        String titleLower = book.getTitle().toLowerCase();

        // Mapping intelligente titolo -> categoria
        switch (categoryLower) {
            case "fantasy":
                return titleLower.contains("signore") || titleLower.contains("anelli") ||
                        titleLower.contains("hobbit") || titleLower.contains("principe");
            case "narrativa":
                return titleLower.contains("orgoglio") || titleLower.contains("gatsby") ||
                        titleLower.contains("mockingbird") || titleLower.contains("solitudine");
            case "thriller":
                return titleLower.contains("rosa") || titleLower.contains("mystery");
            case "saggistica":
                return titleLower.contains("anni") || titleLower.contains("storia");
            case "romance":
                return titleLower.contains("orgoglio") || titleLower.contains("pregiudizio");
            default:
                return false;
        }
    }

    /**
     * Ricerca libri asincrona
     */
    public CompletableFuture<List<Book>> searchBooksAsync(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return searchBooks(query);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la ricerca: " + e.getMessage());
                return searchInFallbackBooks(query);
            }
        });
    }

    /**
     * Ricerca libri per titolo
     */
    public List<Book> searchBooks(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8");
        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books/search?title=" + encodedQuery)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> books = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("✅ Trovati " + books.size() + " libri per query: " + query);
                return books;
            } else {
                throw new IOException("Errore nella ricerca: " + response.code());
            }
        }
    }

    /**
     * Ricerca libri per autore asincrona
     */
    public CompletableFuture<List<Book>> searchBooksByAuthorAsync(String author) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return searchBooksByAuthor(author);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la ricerca per autore: " + e.getMessage());
                return searchInFallbackBooksByAuthor(author);
            }
        });
    }

    /**
     * Ricerca libri per autore
     */
    public List<Book> searchBooksByAuthor(String author) throws IOException {
        if (author == null || author.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String encodedAuthor = java.net.URLEncoder.encode(author.trim(), "UTF-8");
        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books/search?author=" + encodedAuthor)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> books = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("✅ Trovati " + books.size() + " libri per autore: " + author);
                return books;
            } else {
                throw new IOException("Errore nella ricerca per autore: " + response.code());
            }
        }
    }

    /**
     * Recupera libri in evidenza (primi 3 dal database)
     */
    public CompletableFuture<List<Book>> getFeaturedBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Book> allBooks = getAllBooks();
                List<Book> featured = new ArrayList<>();

                // Primi 3 come featured
                int limit = Math.min(3, allBooks.size());
                for (int i = 0; i < limit; i++) {
                    featured.add(allBooks.get(i));
                }

                return !featured.isEmpty() ? featured : getFallbackFeaturedBooks();
            } catch (Exception e) {
                System.err.println("❌ Errore recupero libri in evidenza: " + e.getMessage());
                return getFallbackFeaturedBooks();
            }
        });
    }

    /**
     * Recupera libri gratuiti (primi 8 dal database)
     */
    public CompletableFuture<List<Book>> getFreeBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Book> allBooks = getAllBooks();
                List<Book> freeBooks = new ArrayList<>();

                // Primi 8 come gratuiti
                int limit = Math.min(8, allBooks.size());
                for (int i = 0; i < limit; i++) {
                    Book book = allBooks.get(i);
                    book.setIsFree(true);
                    freeBooks.add(book);
                }

                return !freeBooks.isEmpty() ? freeBooks : getFallbackFreeBooks();
            } catch (Exception e) {
                System.err.println("❌ Errore recupero libri gratuiti: " + e.getMessage());
                return getFallbackFreeBooks();
            }
        });
    }

    /**
     * Recupera nuove uscite (ultimi 8 dal database)
     */
    public CompletableFuture<List<Book>> getNewReleasesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Book> allBooks = getAllBooks();
                List<Book> newReleases = new ArrayList<>();

                // Ultimi 8 come nuove uscite (o tutti se meno di 8)
                int totalBooks = allBooks.size();
                int startIndex = Math.max(0, totalBooks - 8);

                for (int i = startIndex; i < totalBooks; i++) {
                    Book book = allBooks.get(i);
                    book.setIsNew(true);
                    newReleases.add(book);
                }

                return !newReleases.isEmpty() ? newReleases : getFallbackNewBooks();
            } catch (Exception e) {
                System.err.println("❌ Errore recupero nuove uscite: " + e.getMessage());
                return getFallbackNewBooks();
            }
        });
    }

    /**
     * Ricerca nei libri di fallback
     */
    private List<Book> searchInFallbackBooks(String query) {
        List<Book> fallbackBooks = getFallbackBooks();
        List<Book> results = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (Book book : fallbackBooks) {
            if (book.getTitle().toLowerCase().contains(queryLower) ||
                    book.getAuthor().toLowerCase().contains(queryLower) ||
                    (book.getDescription() != null && book.getDescription().toLowerCase().contains(queryLower))) {
                results.add(book);
            }
        }

        System.out.println("🔍 Ricerca fallback '" + query + "': trovati " + results.size() + " risultati");
        return results;
    }

    /**
     * Ricerca per autore nei libri di fallback
     */
    private List<Book> searchInFallbackBooksByAuthor(String author) {
        List<Book> fallbackBooks = getFallbackBooks();
        List<Book> results = new ArrayList<>();
        String authorLower = author.toLowerCase();

        for (Book book : fallbackBooks) {
            if (book.getAuthor().toLowerCase().contains(authorLower)) {
                results.add(book);
            }
        }

        System.out.println("👤 Ricerca autore fallback '" + author + "': trovati " + results.size() + " risultati");
        return results;
    }

    /**
     * Libri di fallback quando il server non è disponibile
     */
    private List<Book> getFallbackBooks() {
        List<Book> books = new ArrayList<>();

        // Libri con categorie assegnate per testare la funzionalità
        books.add(new Book(1L, "978-88-452-9039-1", "Il Nome della Rosa", "Umberto Eco",
                "Un affascinante thriller medievale ambientato in un'abbazia benedettina nel XIV secolo.",
                "1980", "placeholder.jpg"));

        books.add(new Book(2L, "978-88-04-68451-1", "1984", "George Orwell",
                "Un romanzo distopico sul totalitarismo e la sorveglianza di massa.",
                "1949", "placeholder.jpg"));

        books.add(new Book(3L, "978-88-452-1234-5", "Il Piccolo Principe", "Antoine de Saint-Exupéry",
                "Una fiaba poetica che ha conquistato il cuore di lettori di tutte le età.",
                "1943", "placeholder.jpg"));

        books.add(new Book(4L, "978-88-04-98765-4", "Orgoglio e Pregiudizio", "Jane Austen",
                "Un classico romanzo romantico dell'epoca georgiana.",
                "1813", "placeholder.jpg"));

        books.add(new Book(5L, "978-88-452-5678-9", "Il Signore degli Anelli", "J.R.R. Tolkien",
                "L'epica avventura fantasy per eccellenza.",
                "1954", "placeholder.jpg"));

        books.add(new Book(6L, "978-88-04-11111-1", "To Kill a Mockingbird", "Harper Lee",
                "Un potente romanzo sulla giustizia e i diritti civili.",
                "1960", "placeholder.jpg"));

        books.add(new Book(7L, "978-88-452-2222-2", "Il Grande Gatsby", "F. Scott Fitzgerald",
                "Un ritratto della società americana degli anni '20.",
                "1925", "placeholder.jpg"));

        books.add(new Book(8L, "978-88-04-3333-3", "Cento Anni di Solitudine", "Gabriel García Márquez",
                "Un capolavoro del realismo magico latinoamericano.",
                "1967", "placeholder.jpg"));

        // Assegna categorie ai libri di fallback
        assignCategoriesToFallbackBooks(books);

        return books;
    }

    /**
     * Assegna categorie ai libri di fallback
     */
    private void assignCategoriesToFallbackBooks(List<Book> books) {
        for (Book book : books) {
            if (book.getTitle().contains("Rosa")) {
                book.setCategory("Thriller");
            } else if (book.getTitle().contains("1984")) {
                book.setCategory("Narrativa");
            } else if (book.getTitle().contains("Principe")) {
                book.setCategory("Fantasy");
            } else if (book.getTitle().contains("Orgoglio")) {
                book.setCategory("Romance");
            } else if (book.getTitle().contains("Anelli")) {
                book.setCategory("Fantasy");
            } else if (book.getTitle().contains("Mockingbird")) {
                book.setCategory("Narrativa");
            } else if (book.getTitle().contains("Gatsby")) {
                book.setCategory("Narrativa");
            } else if (book.getTitle().contains("Solitudine")) {
                book.setCategory("Saggistica");
            }
        }
    }

    /**
     * Libri in evidenza fallback
     */
    private List<Book> getFallbackFeaturedBooks() {
        List<Book> featured = new ArrayList<>();
        List<Book> allBooks = getFallbackBooks();
        if (!allBooks.isEmpty()) {
            featured.add(allBooks.get(0)); // Prima 3 come featured
            if (allBooks.size() > 1) featured.add(allBooks.get(1));
            if (allBooks.size() > 2) featured.add(allBooks.get(2));
        }
        return featured;
    }

    /**
     * Libri gratuiti fallback
     */
    private List<Book> getFallbackFreeBooks() {
        List<Book> free = new ArrayList<>();
        List<Book> allBooks = getFallbackBooks();
        // Primi 6 come gratuiti
        int limit = Math.min(6, allBooks.size());
        for (int i = 0; i < limit; i++) {
            Book book = allBooks.get(i);
            book.setIsFree(true);
            free.add(book);
        }
        return free;
    }

    /**
     * Nuove uscite fallback
     */
    private List<Book> getFallbackNewBooks() {
        List<Book> newBooks = new ArrayList<>();
        List<Book> allBooks = getFallbackBooks();
        // Ultimi 4 come nuove uscite
        int start = Math.max(0, allBooks.size() - 4);
        for (int i = start; i < allBooks.size(); i++) {
            Book book = allBooks.get(i);
            book.setIsNew(true);
            newBooks.add(book);
        }
        return newBooks;
    }

    /**
     * ✅ METODO MANCANTE: Ricerca libri per titolo (per AdvancedSearchPanel)
     */
    public List<Book> searchBooksByTitle(String title) {
        System.out.println("📖 Ricerca per titolo: " + title);

        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Usa lo stesso endpoint della ricerca generale
            String encodedTitle = java.net.URLEncoder.encode(title.trim(), "UTF-8");
            Request request = new Request.Builder()
                    .url(SERVER_BASE_URL + "/books/search?title=" + encodedTitle)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    List<Book> books = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                    System.out.println("✅ Trovati " + books.size() + " libri per titolo: " + title);
                    return books;
                } else {
                    System.err.println("❌ Errore API ricerca titolo: " + response.code());
                    return searchInFallbackBooksByTitle(title);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Errore ricerca per titolo: " + e.getMessage());
            return searchInFallbackBooksByTitle(title);
        }
    }

    /**
     * Ricerca per titolo nei libri di fallback
     */
    private List<Book> searchInFallbackBooksByTitle(String title) {
        List<Book> fallbackBooks = getFallbackBooks();
        List<Book> results = new ArrayList<>();
        String titleLower = title.toLowerCase();

        for (Book book : fallbackBooks) {
            if (book.getTitle().toLowerCase().contains(titleLower)) {
                results.add(book);
            }
        }

        System.out.println("📖 Ricerca titolo fallback '" + title + "': trovati " + results.size() + " risultati");
        return results;
    }

    /**
     * Chiude le risorse HTTP
     */
    public void shutdown() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}