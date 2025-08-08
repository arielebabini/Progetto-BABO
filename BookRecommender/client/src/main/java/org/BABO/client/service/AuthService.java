package org.BABO.client.service;

import org.BABO.shared.dto.AuthRequest;
import org.BABO.shared.dto.AuthResponse;
import org.BABO.shared.dto.RegisterRequest;
import org.BABO.shared.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service per gestire le chiamate API di autenticazione
 * Gestisce login, registrazione e operazioni utente
 */
public class AuthService {

    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Login asincrono
     */
    public CompletableFuture<AuthResponse> loginAsync(String email, String password) {
        System.out.println("🔐 Tentativo login per: " + email);

        AuthRequest request = new AuthRequest(email, password);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestBody = objectMapper.writeValueAsString(request);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta login - Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("✅ Login riuscito per: " + authResponse.getUser().getDisplayName());
                    return authResponse;
                } else {
                    try {
                        AuthResponse errorResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                        System.out.println("❌ Login fallito: " + errorResponse.getMessage());
                        return errorResponse;
                    } catch (Exception e) {
                        System.out.println("❌ Login fallito con status: " + response.statusCode());
                        return new AuthResponse(false, "Errore di autenticazione (status: " + response.statusCode() + ")");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore durante il login: " + e.getMessage());
                e.printStackTrace();
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Registrazione asincrona
     */
    public CompletableFuture<AuthResponse> registerAsync(String name, String surname, String cf,
                                                         String email, String username, String password) {
        System.out.println("📝 Tentativo registrazione per: " + email);

        RegisterRequest request = new RegisterRequest(name, surname, cf, email, username, password);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestBody = objectMapper.writeValueAsString(request);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta registrazione - Status: " + response.statusCode());

                if (response.statusCode() == 201) { // 201 Created
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("✅ Registrazione completata per: " + authResponse.getUser().getDisplayName());
                    return authResponse;
                } else {
                    try {
                        AuthResponse errorResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                        System.out.println("❌ Registrazione fallita: " + errorResponse.getMessage());
                        return errorResponse;
                    } catch (Exception e) {
                        System.out.println("❌ Registrazione fallita con status: " + response.statusCode());
                        return new AuthResponse(false, "Errore di registrazione (status: " + response.statusCode() + ")");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore durante la registrazione: " + e.getMessage());
                e.printStackTrace();
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera il profilo utente dal server
     */
    public CompletableFuture<AuthResponse> getUserProfileAsync(String userId) {
        System.out.println("👤 Recupero profilo utente per ID: " + userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/profile/" + userId))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(15))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta profilo utente - Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("✅ Profilo utente recuperato: " + authResponse.getUser().getDisplayName());
                    return authResponse;
                } else if (response.statusCode() == 404) {
                    System.out.println("❌ Utente non trovato");
                    return new AuthResponse(false, "Utente non trovato");
                } else {
                    try {
                        AuthResponse errorResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                        System.out.println("❌ Errore recupero profilo: " + errorResponse.getMessage());
                        return errorResponse;
                    } catch (Exception e) {
                        System.out.println("❌ Errore recupero profilo con status: " + response.statusCode());
                        return new AuthResponse(false, "Errore recupero profilo (status: " + response.statusCode() + ")");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero profilo: " + e.getMessage());
                e.printStackTrace();
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Test connessione al servizio di autenticazione
     */
    public CompletableFuture<AuthResponse> healthCheckAsync() {
        System.out.println("🩺 Test connessione servizio autenticazione...");

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/health"))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("✅ Servizio autenticazione: " + authResponse.getMessage());
                    return authResponse;
                } else {
                    System.out.println("⚠️ Servizio autenticazione non disponibile (status: " + response.statusCode() + ")");
                    return new AuthResponse(false, "Servizio non disponibile");
                }

            } catch (Exception e) {
                System.err.println("❌ Errore connessione servizio auth: " + e.getMessage());
                return new AuthResponse(false, "Servizio non raggiungibile: " + e.getMessage());
            }
        });
    }

    /**
     * Logout (placeholder per future implementazioni con token)
     */
    public CompletableFuture<AuthResponse> logoutAsync() {
        System.out.println("🚪 Logout...");

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/logout"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(15))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("✅ Logout completato");
                    return authResponse;
                } else {
                    return new AuthResponse(false, "Errore durante il logout");
                }

            } catch (Exception e) {
                System.err.println("❌ Errore logout: " + e.getMessage());
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Reset password asincrono
     */
    public CompletableFuture<AuthResponse> resetPasswordAsync(String email, String newPassword) {
        System.out.println("🔄 Tentativo reset password per: " + email);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Crea DTO per reset password
                String requestBody = objectMapper.writeValueAsString(Map.of(
                        "email", email,
                        "newPassword", newPassword
                ));

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/reset-password"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📡 Risposta reset password - Status: " + response.statusCode());

                AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                return authResponse;

            } catch (Exception e) {
                System.err.println("❌ Errore reset password: " + e.getMessage());
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }
}