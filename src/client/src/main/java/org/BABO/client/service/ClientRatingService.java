package org.BABO.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.BABO.shared.dto.Rating.RatingRequest;
import org.BABO.shared.dto.Rating.RatingResponse;
import org.BABO.shared.model.Book;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio client per la gestione delle valutazioni e recensioni dei libri.
 * <p>
 * Questa classe fornisce un'interfaccia completa per tutte le operazioni relative
 * alle valutazioni degli utenti sui libri, incluse creazione, lettura, aggiornamento
 * ed eliminazione di recensioni. Supporta anche il recupero di statistiche e
 * classifiche basate sulle valutazioni.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Gestione Valutazioni:</strong> Aggiunta, modifica ed eliminazione recensioni utente</li>
 *   <li><strong>Recupero Dati:</strong> Consultazione valutazioni per utente o libro specifico</li>
 *   <li><strong>Statistiche:</strong> Analisi aggregata delle valutazioni per libro</li>
 *   <li><strong>Classifiche:</strong> Libri più recensiti e meglio valutati</li>
 *   <li><strong>Operazioni Asincrone:</strong> Tutte le operazioni utilizzano {@link CompletableFuture}</li>
 * </ul>
 *
 * <h3>Endpoint supportati:</h3>
 * <ul>
 *   <li>{@code POST /api/ratings/add} - Aggiunge o aggiorna una valutazione</li>
 *   <li>{@code GET /api/ratings/user/{username}} - Recupera tutte le valutazioni di un utente</li>
 *   <li>{@code GET /api/ratings/user/{username}/book/{isbn}} - Recupera valutazione specifica</li>
 *   <li>{@code GET /api/ratings/book/{isbn}} - Recupera tutte le valutazioni di un libro</li>
 *   <li>{@code GET /api/ratings/book/{isbn}/statistics} - Statistiche complete libro</li>
 *   <li>{@code DELETE /api/ratings/user/{username}/book/{isbn}} - Elimina valutazione</li>
 *   <li>{@code GET /api/ratings/most-reviewed-books} - Libri più recensiti</li>
 *   <li>{@code GET /api/ratings/best-rated-books} - Libri meglio valutati</li>
 * </ul>
 *
 * <h3>Struttura valutazioni:</h3>
 * <p>
 * Le valutazioni includono punteggi separati per diversi aspetti del libro:
 * stile, contenuto, piacevolezza, originalità ed edizione. Il sistema calcola
 * automaticamente la media ponderata per fornire un punteggio complessivo.
 * </p>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * ClientRatingService ratingService = new ClientRatingService();
 *
 * // Creazione di una nuova valutazione
 * RatingRequest request = new RatingRequest("user123", "978-0123456789",
 *                                           4, 5, 4, 3, 4, "Ottimo libro!");
 *
 * ratingService.addOrUpdateRatingAsync(request)
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Valutazione salvata: " + response.getMessage());
 *         } else {
 *             System.out.println("Errore: " + response.getMessage());
 *         }
 *     });
 *
 * // Recupero statistiche libro
 * ratingService.getBookRatingStatisticsAsync("978-0123456789")
 *     .thenAccept(response -> {
 *         if (response.isSuccess() && response.getStats() != null) {
 *             System.out.println("Valutazione media: " + response.getStats().getAverageRating());
 *             System.out.println("Numero recensioni: " + response.getStats().getTotalReviews());
 *         }
 *     });
 *
 * // Recupero libri meglio valutati
 * ratingService.getBestRatedBooksAsync()
 *     .thenAccept(books -> {
 *         System.out.println("Top " + books.size() + " libri meglio valutati:");
 *         books.forEach(book -> System.out.println("- " + book.getTitle()));
 *     });
 *
 * // Chiusura risorse
 * ratingService.close();
 * }</pre>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see RatingRequest
 * @see RatingResponse
 * @see Book
 */
public class ClientRatingService {

    /** URL base per tutte le operazioni sulle valutazioni */
    private static final String BASE_URL = "http://localhost:8080/api/ratings";

    /** Client HTTP per le richieste al server */
    private final HttpClient httpClient;

    /** Mapper JSON per serializzazione/deserializzazione */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore del servizio per le valutazioni.
     * <p>
     * Inizializza il client HTTP con un timeout di connessione di 10 secondi
     * e configura l'ObjectMapper per la gestione JSON senza moduli aggiuntivi.
     * </p>
     */
    public ClientRatingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // ObjectMapper semplice senza moduli aggiuntivi
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Aggiunge una nuova valutazione o aggiorna una esistente.
     * <p>
     * Se esiste già una valutazione dell'utente per il libro specificato,
     * questa viene aggiornata con i nuovi valori. Altrimenti viene creata
     * una nuova valutazione. Il sistema calcola automaticamente la media
     * dei punteggi individuali.
     * </p>
     *
     * @param request l'oggetto {@link RatingRequest} contenente tutti i dati della valutazione
     *                (username, ISBN, punteggi per ogni aspetto e recensione testuale opzionale)
     * @return un {@link CompletableFuture} che si risolve con {@link RatingResponse}
     *         contenente l'esito dell'operazione e i dettagli della valutazione salvata
     * @throws IllegalArgumentException se request è {@code null}
     *
     * @apiNote I punteggi devono essere compresi tra 1 e 5. La recensione testuale
     *          è opzionale ma consigliata per fornire feedback dettagliato.
     */
    public CompletableFuture<RatingResponse> addOrUpdateRatingAsync(RatingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta di valutazione non può essere null");
        }

        System.out.println("Invio richiesta valutazione per: " + request.getIsbn());

        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = objectMapper.writeValueAsString(request);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/add"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Risposta server valutazione: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("Valutazione salvata: " + ratingResponse.getMessage());
                    return ratingResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nella richiesta valutazione: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera la valutazione specifica di un utente per un libro.
     * <p>
     * Restituisce i dettagli completi della valutazione (punteggi, recensione,
     * data di creazione) se l'utente ha recensito il libro specificato.
     * È utile per verificare se un utente ha già valutato un libro e per
     * pre-compilare form di modifica.
     * </p>
     *
     * @param username il nome utente di cui recuperare la valutazione
     * @param isbn il codice ISBN del libro per cui recuperare la valutazione
     * @return un {@link CompletableFuture} che si risolve con {@link RatingResponse}
     *         contenente la valutazione dell'utente o un messaggio se non trovata
     * @throws IllegalArgumentException se username o isbn sono {@code null}
     *
     * @apiNote Se l'utente non ha mai valutato il libro, la risposta avrà
     *          {@code success = false} con un messaggio appropriato.
     */
    public CompletableFuture<RatingResponse> getUserRatingForBookAsync(String username, String isbn) {
        if (username == null || isbn == null) {
            throw new IllegalArgumentException("Username e ISBN non possono essere null");
        }

        System.out.println("Recupero valutazione utente: " + username + " per ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username) + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Risposta recupero valutazione utente: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("Valutazione utente recuperata");
                    return ratingResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero valutazione utente: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le valutazioni effettuate da un utente specifico.
     * <p>
     * Restituisce la cronologia completa delle recensioni dell'utente,
     * inclusi tutti i libri valutati con i rispettivi punteggi e commenti.
     * È utile per creare profili utente e analizzare le preferenze di lettura.
     * </p>
     *
     * @param username il nome utente di cui recuperare tutte le valutazioni
     * @return un {@link CompletableFuture} che si risolve con {@link RatingResponse}
     *         contenente la lista di tutte le valutazioni dell'utente
     * @throws IllegalArgumentException se username è {@code null}
     *
     * @apiNote Le valutazioni sono ordinate per data di creazione decrescente.
     *          Se l'utente non ha mai effettuato valutazioni, viene restituita
     *          una lista vuota.
     */
    public CompletableFuture<RatingResponse> getUserRatingsAsync(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Lo username non può essere null");
        }

        System.out.println("Recupero tutte le valutazioni dell'utente: " + username);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Risposta valutazioni utente: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("Valutazioni utente recuperate: " +
                            (ratingResponse.getRatings() != null ? ratingResponse.getRatings().size() : 0));
                    return ratingResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero valutazioni utente: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le valutazioni ricevute da un libro specifico.
     * <p>
     * Restituisce l'elenco completo di tutte le recensioni degli utenti per
     * il libro identificato dall'ISBN. Include punteggi, commenti e informazioni
     * sugli autori delle recensioni. È essenziale per mostrare il feedback
     * degli utenti nelle pagine dei dettagli libro.
     * </p>
     *
     * @param isbn il codice ISBN del libro di cui recuperare le valutazioni
     * @return un {@link CompletableFuture} che si risolve con {@link RatingResponse}
     *         contenente la lista di tutte le valutazioni del libro
     * @throws IllegalArgumentException se isbn è {@code null}
     *
     * @apiNote Le valutazioni sono ordinate per data di creazione decrescente.
     *          Include informazioni aggregate come il punteggio medio calcolato
     *          in tempo reale.
     */
    public CompletableFuture<RatingResponse> getBookRatingsAsync(String isbn) {
        if (isbn == null) {
            throw new IllegalArgumentException("L'ISBN non può essere null");
        }

        System.out.println("Recupero valutazioni libro ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Risposta valutazioni libro: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("Valutazioni libro recuperate: " +
                            (ratingResponse.getRatings() != null ? ratingResponse.getRatings().size() : 0));
                    return ratingResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero valutazioni libro: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera statistiche aggregate complete per un libro specifico.
     * <p>
     * Fornisce un'analisi dettagliata delle valutazioni ricevute dal libro,
     * inclusi punteggio medio generale, medie per singolo aspetto (stile,
     * contenuto, etc.), numero totale di recensioni e distribuzione dei voti.
     * È fondamentale per dashboard amministrative e analisi dei dati.
     * </p>
     *
     * @param isbn il codice ISBN del libro di cui recuperare le statistiche
     * @return un {@link CompletableFuture} che si risolve con {@link RatingResponse}
     *         contenente le statistiche complete del libro nel campo stats
     * @throws IllegalArgumentException se isbn è {@code null}
     *
     * @apiNote Le statistiche sono calcolate in tempo reale dal server e includono:
     *          <ul>
     *            <li>Punteggio medio complessivo</li>
     *            <li>Punteggi medi per ogni aspetto (stile, contenuto, piacevolezza, originalità, edizione)</li>
     *            <li>Numero totale di recensioni</li>
     *            <li>Distribuzione delle valutazioni (1-5 stelle)</li>
     *          </ul>
     */
    public CompletableFuture<RatingResponse> getBookRatingStatisticsAsync(String isbn) {
        if (isbn == null) {
            throw new IllegalArgumentException("L'ISBN non può essere null");
        }

        System.out.println("Recupero statistiche libro ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/book/" + encodeUrl(isbn) + "/statistics";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Risposta statistiche libro: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("Statistiche libro recuperate");
                    return ratingResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero statistiche libro: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina definitivamente una valutazione specifica.
     * <p>
     * Rimuove completamente la valutazione dell'utente per il libro specificato
     * dal sistema. L'operazione è irreversibile e comporta il ricalcolo automatico
     * delle statistiche aggregate del libro. Tipicamente utilizzata quando un
     * utente decide di rimuovere la propria recensione.
     * </p>
     *
     * @param username il nome utente proprietario della valutazione da eliminare
     * @param isbn il codice ISBN del libro per cui eliminare la valutazione
     * @return un {@link CompletableFuture} che si risolve con {@link RatingResponse}
     *         indicante l'esito dell'operazione di eliminazione
     * @throws IllegalArgumentException se username o isbn sono {@code null}
     *
     * @apiNote Se la valutazione non esiste, l'operazione restituisce un errore
     *          404 con messaggio appropriato. L'eliminazione comporta il ricalcolo
     *          immediato delle statistiche aggregate del libro.
     */
    public CompletableFuture<RatingResponse> deleteRatingAsync(String username, String isbn) {
        if (username == null || isbn == null) {
            throw new IllegalArgumentException("Username e ISBN non possono essere null");
        }

        System.out.println("Eliminazione valutazione utente: " + username + " per ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username) + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .DELETE()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("Valutazione eliminata");
                    return ratingResponse;
                } else if (response.statusCode() == 404) {
                    System.out.println("Valutazione non trovata");
                    return new RatingResponse(false, "Valutazione non trovata");
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nell'eliminazione valutazione: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera la classifica dei libri più recensiti del sistema.
     * <p>
     * Restituisce una lista ordinata dei libri che hanno ricevuto il maggior
     * numero di recensioni dagli utenti. È utile per identificare i libri più
     * popolari e coinvolgenti della community, indipendentemente dal punteggio medio.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con una {@link List} di {@link Book}
     *         ordinata per numero di recensioni decrescente
     *
     * @apiNote La lista è limitata ai primi N libri (tipicamente 20-50) per
     *          ottimizzare le prestazioni. I libri restituiti includono metadati
     *          completi e il numero di recensioni ricevute.
     */
    public CompletableFuture<List<Book>> getTopRatedBooksAsync() {
        System.out.println("Recupero libri più recensiti dal server");

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/most-reviewed-books";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Book[] booksArray = objectMapper.readValue(response.body(), Book[].class);
                    List<Book> books = Arrays.asList(booksArray);

                    System.out.println("Libri più recensiti recuperati: " + books.size());
                    return books;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new ArrayList<>();
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero libri più recensiti: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    /**
     * Recupera la classifica dei libri meglio valutati del sistema.
     * <p>
     * Restituisce una lista ordinata dei libri con i punteggi medi più alti,
     * calcolati sulla base di tutte le valutazioni ricevute. È ideale per
     * raccomandazioni di qualità e per identificare i libri più apprezzati
     * dagli utenti.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con una {@link List} di {@link Book}
     *         ordinata per punteggio medio decrescente
     *
     * @apiNote Per essere inclusi nella classifica, i libri devono avere un numero
     *          minimo di recensioni (tipicamente 5-10) per garantire la significatività
     *          statistica del punteggio medio. La lista è limitata ai primi N libri.
     */
    public CompletableFuture<List<Book>> getBestRatedBooksAsync() {
        System.out.println("Recupero libri meglio valutati dal server");

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/best-rated-books";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Book[] booksArray = objectMapper.readValue(response.body(), Book[].class);
                    List<Book> books = Arrays.asList(booksArray);

                    System.out.println("Libri meglio valutati recuperati: " + books.size());
                    return books;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new ArrayList<>();
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero libri meglio valutati: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    /**
     * Codifica un valore per l'utilizzo sicuro negli URL.
     * <p>
     * Gestisce caratteri speciali, spazi e caratteri non ASCII per evitare
     * problemi nell'invio delle richieste HTTP. È particolarmente importante
     * per username e ISBN che potrebbero contenere caratteri speciali.
     * </p>
     *
     * @param value la stringa da codificare per l'URL
     * @return la stringa codificata in formato URL-safe, o il valore originale
     *         se la codifica fallisce
     *
     * @implNote Utilizza UTF-8 come charset per la codifica. In caso di errore
     *           durante la codifica, restituisce il valore originale per evitare
     *           interruzioni del servizio.
     */
    private String encodeUrl(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Chiude il servizio e libera tutte le risorse associate.
     * <p>
     * Questo metodo deve essere chiamato quando il servizio non è più necessario
     * per garantire una corretta pulizia delle risorse e evitare memory leak.
     * Dopo la chiamata a questo metodo, il servizio non deve più essere utilizzato.
     * </p>
     *
     * @apiNote Attualmente logga solo la chiusura, ma in futuro potrebbe includere
     *          operazioni di cleanup più elaborate come la chiusura di connection pool
     *          o la terminazione di thread in background.
     */
    public void close() {
        System.out.println("ClientRatingService chiuso");
    }
}