package org.BABO.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.BABO.shared.dto.RatingRequest;
import org.BABO.shared.dto.RatingResponse;
import org.BABO.shared.model.BookRating;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio client per gestire le comunicazioni con il server per le valutazioni
 * Utilizza HTTP Client asincrono per le operazioni
 * RINOMINATO per evitare conflitti con il RatingService del server
 */
public class ClientRatingService {

    private static final String BASE_URL = "http://localhost:8080/api/ratings";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClientRatingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // ObjectMapper semplice senza moduli aggiuntivi
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Aggiunge o aggiorna una valutazione
     */
    public CompletableFuture<RatingResponse> addOrUpdateRatingAsync(RatingRequest request) {
        System.out.println("⭐ Invio richiesta valutazione per: " + request.getIsbn());

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

                System.out.println("📡 Risposta server valutazione: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Valutazione salvata: " + ratingResponse.getMessage());
                    return ratingResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nella richiesta valutazione: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera la valutazione di un utente per un libro
     */
    public CompletableFuture<RatingResponse> getUserRatingForBookAsync(String username, String isbn) {
        System.out.println("🔍 Recupero valutazione utente: " + username + " per ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username) + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta recupero valutazione utente: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Valutazione utente recuperata");
                    return ratingResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero valutazione utente: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le valutazioni di un utente
     */
    public CompletableFuture<RatingResponse> getUserRatingsAsync(String username) {
        System.out.println("👤 Recupero tutte le valutazioni dell'utente: " + username);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta valutazioni utente: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Valutazioni utente recuperate: " +
                            (ratingResponse.getRatings() != null ? ratingResponse.getRatings().size() : 0));
                    return ratingResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero valutazioni utente: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le valutazioni per un libro
     */
    public CompletableFuture<RatingResponse> getBookRatingsAsync(String isbn) {
        System.out.println("📊 Recupero valutazioni libro ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta valutazioni libro: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Valutazioni libro recuperate: " +
                            (ratingResponse.getRatings() != null ? ratingResponse.getRatings().size() : 0));
                    return ratingResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero valutazioni libro: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera statistiche complete per un libro
     */
    public CompletableFuture<RatingResponse> getBookRatingStatisticsAsync(String isbn) {
        System.out.println("📈 Recupero statistiche libro ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/book/" + encodeUrl(isbn) + "/statistics";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta statistiche libro: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Statistiche libro recuperate");
                    return ratingResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero statistiche libro: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera solo la media delle valutazioni per un libro
     */
    public CompletableFuture<RatingResponse> getBookAverageRatingAsync(String isbn) {
        System.out.println("📊 Recupero media valutazioni libro ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/book/" + encodeUrl(isbn) + "/average";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta media libro: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Media libro recuperata: " + ratingResponse.getAverageRating());
                    return ratingResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero media libro: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina una valutazione
     */
    public CompletableFuture<RatingResponse> deleteRatingAsync(String username, String isbn) {
        System.out.println("🗑️ Eliminazione valutazione utente: " + username + " per ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username) + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .DELETE()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta eliminazione valutazione: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Valutazione eliminata");
                    return ratingResponse;
                } else if (response.statusCode() == 404) {
                    System.out.println("📝 Valutazione non trovata");
                    return new RatingResponse(false, "Valutazione non trovata");
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nell'eliminazione valutazione: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Valida una richiesta di valutazione senza salvarla
     */
    public CompletableFuture<RatingResponse> validateRatingAsync(RatingRequest request) {
        System.out.println("✅ Validazione valutazione per: " + request.getIsbn());

        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = objectMapper.writeValueAsString(request);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/validate"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta validazione: " + response.statusCode());

                if (response.statusCode() == 200 || response.statusCode() == 400) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Validazione completata: " + ratingResponse.getMessage());
                    return ratingResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RatingResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nella validazione: " + e.getMessage());
                e.printStackTrace();
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Test di connessione al servizio valutazioni
     */
    public CompletableFuture<RatingResponse> healthCheckAsync() {
        System.out.println("🏥 Test connessione servizio valutazioni");

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/health"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta health check: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RatingResponse ratingResponse = objectMapper.readValue(response.body(), RatingResponse.class);
                    System.out.println("✅ Servizio valutazioni disponibile");
                    return ratingResponse;
                } else {
                    System.out.println("❌ Servizio valutazioni non disponibile");
                    return new RatingResponse(false, "Servizio non disponibile: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel health check: " + e.getMessage());
                return new RatingResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    // =============== METODI SINCRONI (per compatibilità) ===============

    /**
     * Aggiunge o aggiorna una valutazione (sincrono)
     */
    public RatingResponse addOrUpdateRating(RatingRequest request) {
        try {
            return addOrUpdateRatingAsync(request).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RatingResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Recupera la valutazione di un utente per un libro (sincrono)
     */
    public RatingResponse getUserRatingForBook(String username, String isbn) {
        try {
            return getUserRatingForBookAsync(username, isbn).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RatingResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Recupera tutte le valutazioni di un utente (sincrono)
     */
    public RatingResponse getUserRatings(String username) {
        try {
            return getUserRatingsAsync(username).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RatingResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Recupera tutte le valutazioni per un libro (sincrono)
     */
    public RatingResponse getBookRatings(String isbn) {
        try {
            return getBookRatingsAsync(isbn).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RatingResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Test di connessione (sincrono)
     */
    public RatingResponse healthCheck() {
        try {
            return healthCheckAsync().get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'health check sincrono: " + e.getMessage());
            return new RatingResponse(false, "Errore: " + e.getMessage());
        }
    }

    // =============== METODI DI UTILITÀ ===============

    /**
     * Codifica URL per gestire caratteri speciali
     */
    private String encodeUrl(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Chiude il client HTTP
     */
    public void close() {
        // HttpClient non ha un metodo close esplicito in Java 11+
        // Il cleanup avviene automaticamente
        System.out.println("🔒 ClientRatingService chiuso");
    }
}