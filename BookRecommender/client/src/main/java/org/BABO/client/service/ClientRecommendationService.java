package org.BABO.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.BABO.shared.dto.RecommendationRequest;
import org.BABO.shared.dto.RecommendationResponse;
import org.BABO.shared.model.BookRecommendation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio client per gestire le comunicazioni con il server per le raccomandazioni
 * Utilizza HTTP Client asincrono per le operazioni
 */
public class ClientRecommendationService {

    private static final String BASE_URL = "http://localhost:8080/api/recommendations";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClientRecommendationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    /**
     * Aggiunge una nuova raccomandazione
     */
    public CompletableFuture<RecommendationResponse> addRecommendationAsync(RecommendationRequest request) {
        System.out.println("💡 Invio richiesta raccomandazione: " + request.getRecommendedBookIsbn() +
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

                System.out.println("📡 Risposta server raccomandazione: " + response.statusCode());

                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("✅ Raccomandazione salvata: " + recResponse.getMessage());
                    return recResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    RecommendationResponse errorResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    return errorResponse;
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nella richiesta raccomandazione: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le raccomandazioni per un libro
     */
    public CompletableFuture<RecommendationResponse> getBookRecommendationsAsync(String isbn) {
        System.out.println("📚 Recupero raccomandazioni per libro ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta recupero raccomandazioni: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("✅ Raccomandazioni recuperate: " + recResponse.getRecommendationsCount());
                    return recResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RecommendationResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero raccomandazioni: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Verifica se un utente può consigliare libri per un ISBN specifico
     */
    public CompletableFuture<RecommendationResponse> canUserRecommendAsync(String username, String isbn) {
        System.out.println("🔍 Verifica permessi raccomandazione per: " + username + ", ISBN: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/can-recommend/" + encodeUrl(username) + "/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta verifica permessi: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("✅ Verifica completata: " + recResponse.getCanRecommend());
                    return recResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RecommendationResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nella verifica permessi: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera le raccomandazioni fatte da un utente per un libro specifico
     */
    public CompletableFuture<RecommendationResponse> getUserRecommendationsForBookAsync(String username, String isbn) {
        System.out.println("👤 Recupero raccomandazioni utente: " + username + " per libro: " + isbn);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/user/" + encodeUrl(username) + "/book/" + encodeUrl(isbn);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta raccomandazioni utente: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("✅ Raccomandazioni utente recuperate: " + recResponse.getRecommendationsCount());
                    return recResponse;
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    return new RecommendationResponse(false, "Errore server: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero raccomandazioni utente: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Rimuove una raccomandazione specifica
     */
    public CompletableFuture<RecommendationResponse> removeRecommendationAsync(RecommendationRequest request) {
        System.out.println("🗑️ Rimozione raccomandazione: " + request.getRecommendedBookIsbn() +
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

                System.out.println("📡 Risposta rimozione raccomandazione: " + response.statusCode());

                if (response.statusCode() == 200) {
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("✅ Raccomandazione rimossa");
                    return recResponse;
                } else if (response.statusCode() == 404) {
                    System.out.println("📝 Raccomandazione non trovata");
                    return new RecommendationResponse(false, "Raccomandazione non trovata");
                } else {
                    System.out.println("❌ Errore server: " + response.body());
                    RecommendationResponse errorResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    return errorResponse;
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nella rimozione raccomandazione: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Test di connessione al servizio raccomandazioni
     */
    public CompletableFuture<RecommendationResponse> healthCheckAsync() {
        System.out.println("🏥 Test connessione servizio raccomandazioni");

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
                    RecommendationResponse recResponse = objectMapper.readValue(response.body(), RecommendationResponse.class);
                    System.out.println("✅ Servizio raccomandazioni disponibile");
                    return recResponse;
                } else {
                    System.out.println("❌ Servizio raccomandazioni non disponibile");
                    return new RecommendationResponse(false, "Servizio non disponibile: " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel health check: " + e.getMessage());
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    // =============== METODI SINCRONI (per compatibilità) ===============

    /**
     * Aggiunge una raccomandazione (sincrono)
     */
    public RecommendationResponse addRecommendation(RecommendationRequest request) {
        try {
            return addRecommendationAsync(request).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RecommendationResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Recupera raccomandazioni per un libro (sincrono)
     */
    public RecommendationResponse getBookRecommendations(String isbn) {
        try {
            return getBookRecommendationsAsync(isbn).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RecommendationResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Verifica permessi (sincrono)
     */
    public RecommendationResponse canUserRecommend(String username, String isbn) {
        try {
            return canUserRecommendAsync(username, isbn).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RecommendationResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Recupera raccomandazioni utente (sincrono)
     */
    public RecommendationResponse getUserRecommendationsForBook(String username, String isbn) {
        try {
            return getUserRecommendationsForBookAsync(username, isbn).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RecommendationResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Rimuove raccomandazione (sincrono)
     */
    public RecommendationResponse removeRecommendation(RecommendationRequest request) {
        try {
            return removeRecommendationAsync(request).get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'operazione sincrona: " + e.getMessage());
            return new RecommendationResponse(false, "Errore: " + e.getMessage());
        }
    }

    /**
     * Test di connessione (sincrono)
     */
    public RecommendationResponse healthCheck() {
        try {
            return healthCheckAsync().get();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'health check sincrono: " + e.getMessage());
            return new RecommendationResponse(false, "Errore: " + e.getMessage());
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
     * Verifica se l'utente può aggiungere più raccomandazioni per un libro
     */
    public CompletableFuture<Boolean> canAddMoreRecommendationsAsync(String username, String isbn) {
        return canUserRecommendAsync(username, isbn)
                .thenApply(response -> {
                    if (response.isSuccess() && response.getCanRecommend() != null) {
                        return response.canAddMoreRecommendations();
                    }
                    return false;
                });
    }

    /**
     * Ottiene il numero di slot rimanenti per le raccomandazioni
     */
    public CompletableFuture<Integer> getRemainingRecommendationSlotsAsync(String username, String isbn) {
        return canUserRecommendAsync(username, isbn)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        return response.getRemainingRecommendationsSlots();
                    }
                    return 0;
                });
    }

    /**
     * Verifica se una specifica raccomandazione esiste già
     */
    public CompletableFuture<Boolean> recommendationExistsAsync(String username, String targetIsbn, String recommendedIsbn) {
        return getUserRecommendationsForBookAsync(username, targetIsbn)
                .thenApply(response -> {
                    if (response.isSuccess() && response.getRecommendations() != null) {
                        return response.getRecommendations().stream()
                                .anyMatch(rec -> recommendedIsbn.equals(rec.getRecommendedBookIsbn()));
                    }
                    return false;
                });
    }

    /**
     * Ottiene statistiche delle raccomandazioni
     */
    public CompletableFuture<RecommendationResponse> getStatsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/stats"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), RecommendationResponse.class);
                } else {
                    return new RecommendationResponse(false, "Errore nel recupero statistiche");
                }

            } catch (Exception e) {
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Chiude il client HTTP
     */
    public void close() {
        System.out.println("🔒 ClientRecommendationService chiuso");
    }
}