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
                System.out.println("✅ Caricati " + books.size() + " libri dal server");
                return books;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Recupera un libro specifico per ID
     */
    public CompletableFuture<Book> getBookByIdAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBookById(id);
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero del libro: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Recupera un libro specifico per ID
     */
    public Book getBookById(Long id) throws IOException {
        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books/" + id)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                return objectMapper.readValue(jsonResponse, Book.class);
            } else if (response.code() == 404) {
                return null;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    // ===================================================================
    // METODI DI RICERCA GENERICA E AVANZATA
    // ===================================================================

    /**
     * Ricerca libri per titolo o autore (ricerca generica)
     */
    public CompletableFuture<List<Book>> searchBooksAsync(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return searchBooks(query);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la ricerca: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    /**
     * Ricerca libri per titolo o autore (ricerca generica)
     */
    public List<Book> searchBooks(String query) throws IOException {
        HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/books/search")
                .newBuilder()
                .addQueryParameter("q", query)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> results = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("🔍 Trovati " + results.size() + " risultati per: " + query);
                return results;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Ricerca libri SOLO per titolo
     */
    public CompletableFuture<List<Book>> searchBooksByTitleAsync(String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return searchBooksByTitle(title);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la ricerca per titolo: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public List<Book> searchBooksByTitle(String title) throws IOException {
        HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/books/search/title")
                .newBuilder()
                .addQueryParameter("q", title)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> results = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("📖 Trovati " + results.size() + " risultati per titolo: " + title);
                return results;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Ricerca libri SOLO per autore
     */
    public CompletableFuture<List<Book>> searchBooksByAuthorAsync(String author) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return searchBooksByAuthor(author);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la ricerca per autore: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public List<Book> searchBooksByAuthor(String author) throws IOException {
        HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/books/search/author")
                .newBuilder()
                .addQueryParameter("q", author)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> results = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("👤 Trovati " + results.size() + " risultati per autore: " + author);
                return results;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Ricerca libri per autore e anno
     */
    public CompletableFuture<List<Book>> searchBooksByAuthorAndYearAsync(String author, String year) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return searchBooksByAuthorAndYear(author, year);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la ricerca per autore e anno: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public List<Book> searchBooksByAuthorAndYear(String author, String year) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SERVER_BASE_URL + "/books/search/author-year")
                .newBuilder()
                .addQueryParameter("author", author);

        if (year != null && !year.trim().isEmpty()) {
            urlBuilder.addQueryParameter("year", year);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> results = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("👤📅 Trovati " + results.size() + " risultati per autore-anno: " + author + " (" + year + ")");
                return results;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    // ===================================================================
    // METODI ESISTENTI (FEATURED, FREE, NEW RELEASES)
    // ===================================================================

    /**
     * Recupera libri in evidenza
     */
    public CompletableFuture<List<Book>> getFeaturedBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getFeaturedBooks();
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero dei libri in evidenza: " + e.getMessage());
                return getFallbackBooks().subList(0, Math.min(1, getFallbackBooks().size()));
            }
        });
    }

    /**
     * Recupera libri in evidenza
     */
    public List<Book> getFeaturedBooks() throws IOException {
        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books/featured")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                return objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Recupera libri gratuiti
     */
    public CompletableFuture<List<Book>> getFreeBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getFreeBooks();
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero dei libri gratuiti: " + e.getMessage());
                return getFallbackBooks();
            }
        });
    }

    /**
     * Recupera libri gratuiti
     */
    public List<Book> getFreeBooks() throws IOException {
        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books/free")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                return objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Recupera nuove uscite
     */
    public CompletableFuture<List<Book>> getNewReleasesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getNewReleases();
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero delle nuove uscite: " + e.getMessage());
                return getFallbackBooks();
            }
        });
    }

    /**
     * Recupera nuove uscite
     */
    public List<Book> getNewReleases() throws IOException {
        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books/new-releases")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                return objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    // ===================================================================
    // UTILITY E FALLBACK
    // ===================================================================

    /**
     * Libri di fallback quando il server non è disponibile
     */
    private List<Book> getFallbackBooks() {
        System.out.println("📚 Utilizzo libri di fallback (modalità offline)");
        List<Book> books = new ArrayList<>();
        books.add(new Book(1L, "Il Nome della Rosa", "Umberto Eco",
                "Un affascinante thriller medievale ambientato in un'abbazia benedettina.", "placeholder.jpg"));
        books.add(new Book(2L, "1984", "George Orwell",
                "Un romanzo distopico sul totalitarismo e la sorveglianza di massa.", "placeholder.jpg"));
        books.add(new Book(3L, "Il Piccolo Principe", "Antoine de Saint-Exupéry",
                "Una fiaba poetica che ha conquistato il cuore di lettori di tutte le età.", "placeholder.jpg"));
        books.add(new Book(4L, "Orgoglio e Pregiudizio", "Jane Austen",
                "Un classico romanzo romantico dell'epoca georgiana.", "placeholder.jpg"));
        books.add(new Book(5L, "Il Signore degli Anelli", "J.R.R. Tolkien",
                "L'epica avventura fantasy per eccellenza.", "placeholder.jpg"));
        books.add(new Book(6L, "To Kill a Mockingbird", "Harper Lee",
                "Un potente romanzo sulla giustizia e i diritti civili.", "placeholder.jpg"));
        books.add(new Book(7L, "Il Grande Gatsby", "F. Scott Fitzgerald",
                "Un ritratto della società americana degli anni '20.", "placeholder.jpg"));
        books.add(new Book(8L, "Cento Anni di Solitudine", "Gabriel García Márquez",
                "Un capolavoro del realismo magico latinoamericano.", "placeholder.jpg"));
        return books;
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