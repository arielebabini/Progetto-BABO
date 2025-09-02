package org.BABO.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.BABO.shared.dto.Recommendation.RecommendationRequest;
import org.BABO.shared.dto.Recommendation.RecommendationResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio client per la gestione del sistema di raccomandazioni libri.
 * <p>
 * Questa classe fornisce un'interfaccia completa per tutte le operazioni relative
 * al sistema di raccomandazioni, permettendo agli utenti di suggerire libri correlati
 * e di consultare i suggerimenti della community. Il sistema è basato su relazioni
 * tra libri create dagli utenti attraverso le loro esperienze di lettura.
 * </p>
 *
 * <h3>Concetto di raccomandazione:</h3>
 * <p>
 * Una raccomandazione rappresenta un collegamento tra due libri dove un utente
 * suggerisce che chi ha apprezzato il "libro target" potrebbe essere interessato
 * al "libro raccomandato". Questo crea una rete di connessioni basata sulle
 * preferenze reali degli utenti.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Creazione Raccomandazioni:</strong> Aggiunta di nuovi suggerimenti tra libri</li>
 *   <li><strong>Consultazione:</strong> Recupero delle raccomandazioni per libro specifico</li>
 *   <li><strong>Verifica Permessi:</strong> Controllo delle autorizzazioni per raccomandare</li>
 *   <li><strong>Gestione Personale:</strong> Visualizzazione e rimozione delle proprie raccomandazioni</li>
 *   <li><strong>Operazioni Asincrone:</strong> Tutte le operazioni utilizzano {@link CompletableFuture}</li>
 * </ul>
 *
 * <h3>Endpoint supportati:</h3>
 * <ul>
 *   <li>{@code POST /api/recommendations/add} - Aggiunge una nuova raccomandazione</li>
 *   <li>{@code GET /api/recommendations/book/{isbn}} - Recupera raccomandazioni per un libro</li>
 *   <li>{@code GET /api/recommendations/can-recommend/{username}/{isbn}} - Verifica permessi</li>
 *   <li>{@code GET /api/recommendations/user/{username}/book/{isbn}} - Raccomandazioni utente per libro</li>
 *   <li>{@code DELETE /api/recommendations/remove} - Rimuove una raccomandazione specifica</li>
 * </ul>
 *
 * <h3>Logica di business:</h3>
 * <p>
 * Il sistema implementa regole specifiche per garantire la qualità delle raccomandazioni:
 * </p>
 * <ul>
 *   <li>Un utente può raccomandare un libro solo se ha letto e valutato il libro target</li>
 *   <li>Non sono permesse auto-raccomandazioni (stesso libro come target e raccomandato)</li>
 *   <li>Ogni utente può fare una sola raccomandazione per coppia di libri</li>
 *   <li>Le raccomandazioni duplicate vengono automaticamente gestite dal sistema</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * ClientRecommendationService recService = new ClientRecommendationService();
 *
 * // Verifica se un utente può fare raccomandazioni per un libro
 * recService.canUserRecommendAsync("user123", "978-0123456789")
 *     .thenAccept(response -> {
 *         if (response.isSuccess() && response.getCanRecommend()) {
 *             System.out.println("L'utente può fare raccomandazioni per questo libro");
 *
 *             // Crea una nuova raccomandazione
 *             RecommendationRequest request = new RecommendationRequest(
 *                 "user123", "978-0123456789", "978-9876543210",
 *                 "Chi ha amato questo libro apprezzerà anche..."
 *             );
 *
 *             return recService.addRecommendationAsync(request);
 *         } else {
 *             System.out.println("Utente non autorizzato: " + response.getMessage());
 *             return CompletableFuture.completedFuture(response);
 *         }
 *     })
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Raccomandazione aggiunta con successo!");
 *         }
 *     });
 *
 * // Recupero raccomandazioni per un libro
 * recService.getBookRecommendationsAsync("978-0123456789")
 *     .thenAccept(response -> {
 *         if (response.isSuccess() && response.getRecommendations() != null) {
 *             System.out.println("Trovate " + response.getRecommendationsCount() + " raccomandazioni:");
 *             response.getRecommendations().forEach(rec ->
 *                 System.out.println("- " + rec.getRecommendedBook().getTitle())
 *             );
 *         }
 *     });
 *
 * // Chiusura risorse
 * recService.close();
 * }</pre>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see RecommendationRequest
 * @see RecommendationResponse
 */
public class ClientRecommendationService {

    /** URL base per tutte le operazioni sulle raccomandazioni */
    private static final String BASE_URL = "http://localhost:8080/api/recommendations";

    /** Client HTTP per le richieste al server */
    private final HttpClient httpClient;

    /** Mapper JSON per serializzazione/deserializzazione */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore del servizio per le raccomandazioni.
     * <p>
     * Inizializza il client HTTP con un timeout di connessione di 10 secondi
     * e configura l'ObjectMapper per la gestione JSON standard.
     * </p>
     */
    public ClientRecommendationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    /**
     * Aggiunge una nuova raccomandazione al sistema.
     * <p>
     * Crea un collegamento tra due libri dove l'utente suggerisce che chi ha
     * apprezzato il libro target potrebbe essere interessato al libro raccomandato.
     * Il sistema verifica automaticamente che l'utente abbia i permessi necessari
     * e che la raccomandazione non sia duplicata.
     * </p>
     *
     * @param request l'oggetto {@link RecommendationRequest} contenente tutti i dati:
     *                username dell'utente, ISBN del libro target, ISBN del libro raccomandato
     *                e motivazione opzionale della raccomandazione
     * @return un {@link CompletableFuture} che si risolve con {@link RecommendationResponse}
     *         contenente l'esito dell'operazione e i dettagli della raccomandazione creata
     * @throws IllegalArgumentException se request è {@code null}
     *
     * @apiNote L'operazione restituisce status HTTP 201 per nuove raccomandazioni
     *          e 200 per aggiornamenti. Il server verifica automaticamente che:
     *          <ul>
     *            <li>L'utente abbia valutato il libro target</li>
     *            <li>I due ISBN siano diversi (no auto-raccomandazioni)</li>
     *            <li>Non esista già la stessa raccomandazione dall'utente</li>
     *          </ul>
     */
    public CompletableFuture<RecommendationResponse> addRecommendationAsync(RecommendationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta di raccomandazione non può essere null");
        }

        System.out.println("Invio richiesta raccomandazione: " + request.getRecommendedBookIsbn() +
                " per " + request.getTargetBookIsbn());

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

                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("Raccomandazione salvata: " + recResponse.getMessage());
                    return recResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    RecommendationResponse errorResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    return errorResponse;
                }

            } catch (Exception e) {
                System.err.println("Errore nella richiesta raccomandazione: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le raccomandazioni associate a un libro specifico.
     * <p>
     * Restituisce l'elenco completo di tutti i libri raccomandati dagli utenti
     * per il libro identificato dall'ISBN. Include informazioni sui libri
     * raccomandati, gli autori delle raccomandazioni e le motivazioni fornite.
     * È fondamentale per mostrare suggerimenti di lettura nelle pagine dei dettagli libro.
     * </p>
     *
     * @param isbn il codice ISBN del libro di cui recuperare le raccomandazioni
     * @return un {@link CompletableFuture} che si risolve con {@link RecommendationResponse}
     *         contenente la lista di tutte le raccomandazioni per il libro
     * @throws IllegalArgumentException se isbn è {@code null}
     *
     * @apiNote Le raccomandazioni sono ordinate per popolarità (numero di utenti
     *          che hanno fatto la stessa raccomandazione) e includono metadati
     *          completi sui libri raccomandati per facilitare la visualizzazione.
     */
    public CompletableFuture<RecommendationResponse> getBookRecommendationsAsync(String isbn) {
        if (isbn == null) {
            throw new IllegalArgumentException("L'ISBN non può essere null");
        }

        System.out.println("Recupero raccomandazioni per libro ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("Raccomandazioni recuperate: " + recResponse.getRecommendationsCount());
                    return recResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RecommendationResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero raccomandazioni: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Verifica se un utente è autorizzato a creare raccomandazioni per un libro specifico.
     * <p>
     * Controlla che l'utente soddisfi i requisiti necessari per poter raccomandare
     * altri libri partendo dal libro target. Tipicamente richiede che l'utente
     * abbia letto e valutato il libro target, dimostrando una conoscenza diretta
     * dell'opera.
     * </p>
     *
     * @param username il nome utente di cui verificare i permessi
     * @param isbn il codice ISBN del libro per cui verificare i permessi di raccomandazione
     * @return un {@link CompletableFuture} che si risolve con {@link RecommendationResponse}
     *         contenente nel campo {@code canRecommend} l'esito della verifica
     * @throws IllegalArgumentException se username o isbn sono {@code null}
     *
     * @apiNote Questa verifica dovrebbe essere eseguita prima di presentare
     *          all'utente l'interfaccia per creare raccomandazioni, per evitare
     *          tentativi fallimentari e migliorare l'esperienza utente.
     */
    public CompletableFuture<RecommendationResponse> canUserRecommendAsync(String username, String isbn) {
        if (username == null || isbn == null) {
            throw new IllegalArgumentException("Username e ISBN non possono essere null");
        }

        System.out.println("Verifica permessi raccomandazione per: " + username + ", ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/can-recommend/" + encodeUrl(username) + "/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("Verifica completata: " + recResponse.getCanRecommend());
                    return recResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RecommendationResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nella verifica permessi: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera le raccomandazioni create da un utente specifico per un libro target.
     * <p>
     * Restituisce l'elenco di tutti i libri che l'utente ha raccomandato partendo
     * dal libro identificato dall'ISBN. È utile per visualizzare il contributo
     * personale dell'utente al sistema di raccomandazioni e per permettere
     * la gestione delle proprie raccomandazioni.
     * </p>
     *
     * @param username il nome utente di cui recuperare le raccomandazioni
     * @param isbn il codice ISBN del libro target per cui recuperare le raccomandazioni
     * @return un {@link CompletableFuture} che si risolve con {@link RecommendationResponse}
     *         contenente la lista delle raccomandazioni create dall'utente per il libro
     * @throws IllegalArgumentException se username o isbn sono {@code null}
     *
     * @apiNote Le raccomandazioni restituite includono le motivazioni fornite
     *          dall'utente e possono essere utilizzate per pre-compilare form
     *          di modifica o per mostrare un riepilogo delle attività dell'utente.
     */
    public CompletableFuture<RecommendationResponse> getUserRecommendationsForBookAsync(String username, String isbn) {
        if (username == null || isbn == null) {
            throw new IllegalArgumentException("Username e ISBN non possono essere null");
        }

        System.out.println("Recupero raccomandazioni utente: " + username + " per libro: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username) + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("Raccomandazioni utente recuperate: " + recResponse.getRecommendationsCount());
                    return recResponse;
                } else {
                    System.out.println("Errore server: " + response.body());
                    return new RecommendationResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("Errore nel recupero raccomandazioni utente: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Rimuove definitivamente una raccomandazione specifica dal sistema.
     * <p>
     * Elimina la raccomandazione identificata dai dati nella richiesta
     * (utente, libro target e libro raccomandato). L'operazione è irreversibile
     * e può essere eseguita solo dall'utente che ha creato la raccomandazione
     * originariamente. Utilizzata quando un utente cambia opinione o corregge
     * una raccomandazione errata.
     * </p>
     *
     * @param request l'oggetto {@link RecommendationRequest} che identifica univocamente
     *                la raccomandazione da rimuovere (deve contenere username,
     *                ISBN del libro target e ISBN del libro raccomandato)
     * @return un {@link CompletableFuture} che si risolve con {@link RecommendationResponse}
     *         indicante l'esito dell'operazione di rimozione
     * @throws IllegalArgumentException se request è {@code null}
     *
     * @apiNote Se la raccomandazione non esiste, l'operazione restituisce status
     *          HTTP 404 con messaggio appropriato. L'eliminazione non influenza
     *          altre raccomandazioni dello stesso libro da altri utenti.
     *
     * @implNote Utilizza il metodo HTTP DELETE con body JSON, che richiede
     *           una configurazione specifica dell'HttpRequest per essere supportato
     *           correttamente da tutti i server.
     */
    public CompletableFuture<RecommendationResponse> removeRecommendationAsync(RecommendationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta di rimozione non può essere null");
        }

        System.out.println("Rimozione raccomandazione: " + request.getRecommendedBookIsbn() +
                " per " + request.getTargetBookIsbn());

        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = objectMapper.writeValueAsString(request);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/remove"))
                        .header("Content-Type", "application/json")
                        .DELETE()
                        .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("Raccomandazione rimossa");
                    return recResponse;
                } else if (response.statusCode() == 404) {
                    System.out.println("Raccomandazione non trovata");
                    return new RecommendationResponse(false, "Raccomandazione non trovata");
                } else {
                    System.out.println("Errore server: " + response.body());
                    RecommendationResponse errorResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    return errorResponse;
                }

            } catch (Exception e) {
                System.err.println("Errore nella rimozione raccomandazione: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
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
        System.out.println("ClientRecommendationService chiuso");
    }
}