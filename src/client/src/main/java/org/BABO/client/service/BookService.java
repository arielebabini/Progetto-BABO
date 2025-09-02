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
 * Servizio per gestire le operazioni sui libri tramite API REST.
 * <p>
 * Questa classe fornisce un'interfaccia client per interagire con il server backend
 * attraverso chiamate HTTP REST. Utilizza OkHttp per le richieste HTTP e Jackson
 * per la serializzazione/deserializzazione JSON.
 * </p>
 *
 * <h3>Caratteristiche principali:</h3>
 * <ul>
 *   <li>Supporto per operazioni asincrone tramite CompletableFuture</li>
 *   <li>Gestione automatica del fallback in caso di server non disponibile</li>
 *   <li>Timeout configurati per connessione e lettura</li>
 *   <li>Logging dettagliato delle operazioni</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * BookService bookService = new BookService();
 *
 * // Caricamento asincrono di tutti i libri
 * bookService.getAllBooksAsync()
 *     .thenAccept(books -> {
 *         System.out.println("Caricati " + books.size() + " libri");
 *     });
 *
 * // Ricerca sincrona per titolo
 * try {
 *     List<Book> results = bookService.searchBooksByTitle("1984");
 *     System.out.println("Trovati " + results.size() + " libri");
 * } catch (IOException e) {
 *     System.err.println("Errore nella ricerca: " + e.getMessage());
 * }
 *
 * // Chiusura delle risorse
 * bookService.shutdown();
 * }</pre>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 */
public class BookService {

    /** URL base del server per le API REST */
    private static final String SERVER_BASE_URL = "http://localhost:8080/api";

    /** Client HTTP per le richieste al server */
    private final OkHttpClient httpClient;

    /** Mapper JSON per serializzazione/deserializzazione */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore del servizio che inizializza il client HTTP e l'ObjectMapper.
     * <p>
     * Configura il client HTTP con timeout di:
     * <ul>
     *   <li>Connessione: 10 secondi</li>
     *   <li>Lettura: 30 secondi</li>
     * </ul>
     * </p>
     */
    public BookService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verifica se il server è disponibile.
     * <p>
     * Effettua una richiesta HEAD per verificare la raggiungibilità del server
     * senza scaricare dati. Questo metodo è utile per controlli di connettività
     * prima di eseguire operazioni più complesse.
     * </p>
     *
     * @return {@code true} se il server è raggiungibile, {@code false} altrimenti
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
            System.err.println("Server non raggiungibile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera tutti i libri dal server in modo asincrono.
     * <p>
     * Se il server non è disponibile o si verifica un errore, restituisce
     * automaticamente una lista di libri di fallback per garantire che
     * l'applicazione rimanga funzionale.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con la lista di tutti i libri
     * @see #getAllBooks()
     * @see #getFallbackBooks()
     */
    public CompletableFuture<List<Book>> getAllBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAllBooks();
            } catch (Exception e) {
                System.err.println("Errore durante il recupero dei libri: " + e.getMessage());
                return getFallbackBooks();
            }
        });
    }

    /**
     * Recupera tutti i libri dal server in modo sincrono.
     * <p>
     * Esegue una richiesta GET all'endpoint {@code /api/books} e deserializza
     * la risposta JSON in una lista di oggetti {@link Book}.
     * </p>
     *
     * @return la lista di tutti i libri disponibili sul server
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     *                     o nella deserializzazione della risposta
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
                System.out.println("Caricati " + books.size() + " libri dal server");
                return books;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Recupera un libro specifico tramite il suo identificativo.
     *
     * @param id l'identificativo univoco del libro da recuperare
     * @return il {@link Book} corrispondente all'ID, o {@code null} se non trovato
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se l'ID è {@code null}
     */
    public Book getBookById(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("L'ID del libro non può essere null");
        }

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

    /**
     * Ricerca libri in modo asincrono utilizzando una query generica.
     * <p>
     * La ricerca viene effettuata su tutti i campi del libro (titolo, autore, descrizione).
     * In caso di errore, restituisce una lista vuota anziché propagare l'eccezione.
     * </p>
     *
     * @param query la stringa di ricerca da utilizzare
     * @return un {@link CompletableFuture} che si risolve con la lista dei libri trovati
     * @see #searchBooks(String)
     */
    public CompletableFuture<List<Book>> searchBooksAsync(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return searchBooks(query);
            } catch (Exception e) {
                System.err.println("Errore durante la ricerca: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    /**
     * Ricerca libri in modo sincrono utilizzando una query generica.
     * <p>
     * Esegue una ricerca full-text su tutti i campi del libro. La ricerca
     * è case-insensitive e supporta ricerche parziali.
     * </p>
     *
     * @param query la stringa di ricerca da utilizzare (non può essere {@code null})
     * @return la lista di libri che corrispondono alla query
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se la query è {@code null}
     */
    public List<Book> searchBooks(String query) throws IOException {
        if (query == null) {
            throw new IllegalArgumentException("La query di ricerca non può essere null");
        }

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
                System.out.println("Trovati " + results.size() + " risultati per: " + query);
                return results;
            } else {
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Ricerca libri esclusivamente per titolo.
     * <p>
     * Questa ricerca è più specifica rispetto alla ricerca generica e cerca
     * solo nel campo titolo del libro. È utile quando si conosce esattamente
     * il titolo da cercare.
     * </p>
     *
     * @param title il titolo del libro da cercare (non può essere {@code null} o vuoto)
     * @return la lista di libri con titolo corrispondente
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se il titolo è {@code null} o vuoto
     */
    public List<Book> searchBooksByTitle(String title) throws IOException {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo non può essere null o vuoto");
        }

        HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/books/search/title")
                .newBuilder()
                .addQueryParameter("q", title.trim())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                List<Book> results = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});
                System.out.println("Trovati " + results.size() + " risultati per titolo: " + title);
                return results;
            } else {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                System.err.println("Errore " + response.code() + ": " + errorBody);
                throw new IOException("Errore nella risposta del server: " + response.code());
            }
        }
    }

    /**
     * Ricerca libri per categoria in modo asincrono.
     * <p>
     * Permette di filtrare i libri per una specifica categoria. La ricerca
     * è case-insensitive e supporta match esatti sul nome della categoria.
     * </p>
     *
     * @param categoryName il nome della categoria da cercare
     * @return un {@link CompletableFuture} che si risolve con la lista dei libri della categoria
     */
    public CompletableFuture<List<Book>> searchBooksByCategoryAsync(String categoryName) {
        System.out.println("Client: Ricerca libri per categoria: " + categoryName);

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/books/category")
                        .newBuilder()
                        .addQueryParameter("name", categoryName)
                        .build();

                System.out.println("[DEBUG] URL categoria: " + url.toString());

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    System.out.println("[DEBUG] Response categoria: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        List<Book> books = objectMapper.readValue(jsonResponse, new TypeReference<List<Book>>() {});

                        System.out.println("Trovati " + books.size() + " libri per categoria: " + categoryName);

                        // Debug: mostra le categorie trovate
                        if (!books.isEmpty()) {
                            System.out.println("Prime categorie trovate:");
                            for (int i = 0; i < Math.min(3, books.size()); i++) {
                                Book book = books.get(i);
                                System.out.println("  - " + book.getTitle() + " (Cat: " + book.getCategory() + ")");
                            }
                        }

                        return books;
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "No body";
                        System.out.println("Errore server categoria " + response.code() + ": " + errorBody);
                        return new ArrayList<>();
                    }
                }
            } catch (Exception e) {
                System.err.println("Errore ricerca categoria: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * Recupera i libri in evidenza per la sezione home in modo asincrono.
     * <p>
     * I libri in evidenza sono selezionati dal server e rappresentano
     * contenuti promozionali o raccomandazioni editoriali.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con la lista dei libri in evidenza
     * @see #getFeaturedBooks()
     */
    public CompletableFuture<List<Book>> getFeaturedBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getFeaturedBooks();
            } catch (Exception e) {
                System.err.println("Errore durante il recupero dei libri in evidenza: " + e.getMessage());
                return getFallbackBooks().subList(0, Math.min(1, getFallbackBooks().size()));
            }
        });
    }

    /**
     * Recupera i libri in evidenza per la sezione home in modo sincrono.
     *
     * @return la lista di libri in evidenza selezionati dal server
     * @throws IOException se si verifica un errore durante la richiesta HTTP
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
     * Recupera i libri consigliati (gratuiti) per la sezione home in modo asincrono.
     * <p>
     * Restituisce una selezione di libri disponibili gratuitamente,
     * ideali per utenti che vogliono esplorare contenuti senza costi.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con la lista dei libri consigliati
     * @see #getSuggestedBooks()
     */
    public CompletableFuture<List<Book>> getSuggestedBooksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getSuggestedBooks();
            } catch (Exception e) {
                System.err.println("Errore durante il recupero dei libri gratuiti: " + e.getMessage());
                return getFallbackBooks();
            }
        });
    }

    /**
     * Recupera i libri consigliati (gratuiti) per la sezione home in modo sincrono.
     *
     * @return la lista di libri gratuiti consigliati dal server
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     */
    public List<Book> getSuggestedBooks() throws IOException {
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
     * Recupera le nuove uscite per la sezione home in modo asincrono.
     * <p>
     * Restituisce i libri pubblicati più di recente, ordinati per data
     * di pubblicazione decrescente.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con la lista delle nuove uscite
     * @see #getNewReleases()
     */
    public CompletableFuture<List<Book>> getNewReleasesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getNewReleases();
            } catch (Exception e) {
                System.err.println("Errore durante il recupero delle nuove uscite: " + e.getMessage());
                return getFallbackBooks();
            }
        });
    }

    /**
     * Recupera le nuove uscite per la sezione home in modo sincrono.
     *
     * @return la lista delle nuove uscite ordinate per data di pubblicazione
     * @throws IOException se si verifica un errore durante la richiesta HTTP
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

    /**
     * Restituisce una lista di libri di fallback da utilizzare quando il server non è disponibile.
     * <p>
     * Questi libri sono predefiniti e rappresentano una selezione di classici della letteratura.
     * Vengono utilizzati per garantire che l'applicazione rimanga funzionale anche in modalità offline.
     * </p>
     *
     * <h4>Libri inclusi nel fallback:</h4>
     * <ul>
     *   <li>Il Nome della Rosa - Umberto Eco</li>
     *   <li>1984 - George Orwell</li>
     *   <li>Il Piccolo Principe - Antoine de Saint-Exupéry</li>
     *   <li>Orgoglio e Pregiudizio - Jane Austen</li>
     *   <li>Il Signore degli Anelli - J.R.R. Tolkien</li>
     *   <li>To Kill a Mockingbird - Harper Lee</li>
     *   <li>Il Grande Gatsby - F. Scott Fitzgerald</li>
     *   <li>Cento Anni di Solitudine - Gabriel García Márquez</li>
     * </ul>
     *
     * @return una lista di 8 libri classici come fallback
     */
    private List<Book> getFallbackBooks() {
        System.out.println("Utilizzo libri di fallback (modalità offline)");
        List<Book> books = new ArrayList<>();
        books.add(new Book(1L, "Il Nome della Rosa", "Umberto Eco",
                "Un affascinante thriller medievale ambientato in un'abbazia benedettina.", "placeholder.jpg"));
        books.add(new Book(2L, "1984", "George Orwell",
                "Un romanzo distopico sul totalitarismo e la sorveglianza di massa.","placeholder.jpg"));
        books.add(new Book(3L, "Il Piccolo Principe", "Antoine de Saint-Exupéry",
                "Una fiaba poetica che ha conquistato il cuore di lettori di tutte le età.","placeholder.jpg"));
        books.add(new Book(4L, "Orgoglio e Pregiudizio", "Jane Austen",
                "Un classico romanzo romantico dell'epoca georgiana.","placeholder.jpg"));
        books.add(new Book(5L, "Il Signore degli Anelli", "J.R.R. Tolkien",
                "L'epica avventura fantasy per eccellenza.","placeholder.jpg"));
        books.add(new Book(6L, "To Kill a Mockingbird", "Harper Lee",
                "Un potente romanzo sulla giustizia e i diritti civili.","placeholder.jpg"));
        books.add(new Book(7L, "Il Grande Gatsby", "F. Scott Fitzgerald",
                "Un ritratto della società americana degli anni '20.","placeholder.jpg"));
        books.add(new Book(8L, "Cento Anni di Solitudine", "Gabriel García Márquez",
                "Un capolavoro del realismo magico latinoamericano.","placeholder.jpg"));
        return books;
    }

    /**
     * Chiude il client HTTP e libera tutte le risorse associate.
     * <p>
     * Questo metodo deve essere chiamato quando il servizio non è più necessario
     * per evitare memory leak e garantire una corretta pulizia delle risorse.
     * Termina l'executor service del dispatcher e svuota il connection pool.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ul>
     *   <li>Terminazione dell'executor service del dispatcher</li>
     *   <li>Svuotamento del connection pool</li>
     *   <li>Chiusura di tutte le connessioni HTTP attive</li>
     * </ul>
     *
     * @apiNote Dopo aver chiamato questo metodo, il servizio non deve più essere utilizzato
     */
    public void shutdown() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}