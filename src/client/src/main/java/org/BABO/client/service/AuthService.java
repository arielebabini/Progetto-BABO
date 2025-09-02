package org.BABO.client.service;

import org.BABO.shared.dto.Authentication.AuthRequest;
import org.BABO.shared.dto.Authentication.AuthResponse;
import org.BABO.shared.dto.Authentication.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio per gestire le operazioni di autenticazione e registrazione utenti.
 * <p>
 * Questa classe fornisce un'interfaccia client per tutte le operazioni relative
 * all'autenticazione degli utenti, inclusi login, registrazione, gestione profili
 * e operazioni di sicurezza come reset e cambio password.
 * </p>
 *
 * <h3>Caratteristiche principali:</h3>
 * <ul>
 *   <li>Tutte le operazioni sono eseguite in modo asincrono tramite {@link CompletableFuture}</li>
 *   <li>Utilizza Java HttpClient nativo per le richieste HTTP</li>
 *   <li>Serializzazione/deserializzazione JSON tramite Jackson</li>
 *   <li>Timeout configurati per evitare blocchi indefiniti</li>
 *   <li>Gestione robusta degli errori con logging dettagliato</li>
 * </ul>
 *
 * <h3>Endpoint supportati:</h3>
 * <ul>
 *   <li>{@code POST /api/auth/login} - Autenticazione utente</li>
 *   <li>{@code POST /api/auth/register} - Registrazione nuovo utente</li>
 *   <li>{@code GET /api/auth/profile/{userId}} - Recupero profilo utente</li>
 *   <li>{@code POST /api/auth/logout} - Disconnessione utente</li>
 *   <li>{@code POST /api/auth/reset-password} - Reset password</li>
 *   <li>{@code POST /api/auth/change-password/{userId}} - Cambio password</li>
 *   <li>{@code PUT /api/auth/update-email/{userId}} - Aggiornamento email</li>
 *   <li>{@code GET /api/auth/health} - Controllo stato servizio</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * AuthService authService = new AuthService();
 *
 * // Login asincrono
 * authService.loginAsync("user@example.com", "password")
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Login riuscito: " + response.getUser().getDisplayName());
 *         } else {
 *             System.out.println("Login fallito: " + response.getMessage());
 *         }
 *     });
 *
 * // Registrazione con gestione errori
 * authService.registerAsync("Mario", "Rossi", "RSSMRA80A01H501M",
 *                          "mario.rossi@email.com", "mariorossi", "password123")
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Registrazione completata");
 *         }
 *     })
 *     .exceptionally(throwable -> {
 *         System.err.println("Errore: " + throwable.getMessage());
 *         return null;
 *     });
 * }</pre>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see AuthRequest
 * @see AuthResponse
 * @see RegisterRequest
 */
public class AuthService {

    /** URL base per tutte le operazioni di autenticazione */
    private static final String BASE_URL = "http://localhost:8080/api/auth";

    /** Client HTTP per le richieste al server */
    private final HttpClient httpClient;

    /** Mapper JSON per serializzazione/deserializzazione */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore del servizio di autenticazione.
     * <p>
     * Inizializza il client HTTP con un timeout di connessione di 10 secondi
     * e configura l'ObjectMapper per la gestione JSON.
     * </p>
     */
    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Esegue il login di un utente in modo asincrono.
     * <p>
     * Invia le credenziali al server e restituisce una risposta contenente
     * i dettagli dell'utente autenticato o un messaggio di errore.
     * </p>
     *
     * @param email l'indirizzo email dell'utente (non può essere {@code null})
     * @param password la password dell'utente (non può essere {@code null})
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         contenente l'esito dell'operazione e i dati dell'utente se il login è riuscito
     * @throws IllegalArgumentException se email o password sono {@code null}
     *
     * @apiNote In caso di errore di rete o del server, viene restituita una
     *          {@link AuthResponse} con {@code success = false} e messaggio descrittivo
     */
    public CompletableFuture<AuthResponse> loginAsync(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email e password non possono essere null");
        }

        System.out.println("Tentativo login per: " + email);

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

                System.out.println("Risposta login - Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("Login riuscito per: " + authResponse.getUser().getDisplayName());
                    return authResponse;
                } else {
                    try {
                        AuthResponse errorResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                        System.out.println("Login fallito: " + errorResponse.getMessage());
                        return errorResponse;
                    } catch (Exception e) {
                        System.out.println("Login fallito con status: " + response.statusCode());
                        return new AuthResponse(false, "Errore di autenticazione (status: " + response.statusCode() + ")");
                    }
                }

            } catch (Exception e) {
                System.err.println("Errore durante il login: " + e.getMessage());
                e.printStackTrace();
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Registra un nuovo utente nel sistema in modo asincrono.
     * <p>
     * Crea un nuovo account utente con tutti i dati forniti. L'operazione
     * include la validazione lato server dei dati e la creazione dell'utente
     * nel database.
     * </p>
     *
     * @param name il nome dell'utente (non può essere {@code null} o vuoto)
     * @param surname il cognome dell'utente (non può essere {@code null} o vuoto)
     * @param cf il codice fiscale dell'utente (deve essere valido)
     * @param email l'indirizzo email dell'utente (deve essere unico nel sistema)
     * @param username lo username scelto dall'utente (deve essere unico)
     * @param password la password scelta dall'utente (deve rispettare i criteri di sicurezza)
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         contenente l'esito della registrazione
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @apiNote La registrazione riuscita restituisce status HTTP 201 (Created).
     *          In caso di email o username già esistenti, viene restituito un errore specifico.
     */
    public CompletableFuture<AuthResponse> registerAsync(String name, String surname, String cf,
                                                         String email, String username, String password) {
        if (name == null || surname == null || cf == null || email == null || username == null || password == null) {
            throw new IllegalArgumentException("Tutti i parametri di registrazione sono obbligatori");
        }

        System.out.println("Tentativo registrazione per: " + email);

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

                System.out.println("Risposta registrazione - Status: " + response.statusCode());

                if (response.statusCode() == 201) { // 201 Created
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("Registrazione completata per: " + authResponse.getUser().getDisplayName());
                    return authResponse;
                } else {
                    try {
                        AuthResponse errorResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                        System.out.println("Registrazione fallita: " + errorResponse.getMessage());
                        return errorResponse;
                    } catch (Exception e) {
                        System.out.println("Registrazione fallita con status: " + response.statusCode());
                        return new AuthResponse(false, "Errore di registrazione (status: " + response.statusCode() + ")");
                    }
                }

            } catch (Exception e) {
                System.err.println("Errore durante la registrazione: " + e.getMessage());
                e.printStackTrace();
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera il profilo di un utente specifico in modo asincrono.
     * <p>
     * Ottiene tutti i dati del profilo di un utente identificato dal suo ID.
     * Questo metodo è utile per visualizzare informazioni dettagliate dell'utente
     * o per verificare l'esistenza di un account.
     * </p>
     *
     * @param userId l'identificativo univoco dell'utente (non può essere {@code null})
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         contenente i dati del profilo utente
     * @throws IllegalArgumentException se userId è {@code null}
     *
     * @apiNote Se l'utente non esiste, viene restituito status HTTP 404
     *          e un messaggio di errore appropriato
     */
    public CompletableFuture<AuthResponse> getUserProfileAsync(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }

        System.out.println("Recupero profilo utente per ID: " + userId);

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

                System.out.println("Risposta profilo utente - Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("Profilo utente recuperato: " + authResponse.getUser().getDisplayName());
                    return authResponse;
                } else if (response.statusCode() == 404) {
                    System.out.println("Utente non trovato");
                    return new AuthResponse(false, "Utente non trovato");
                } else {
                    try {
                        AuthResponse errorResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                        System.out.println("Errore recupero profilo: " + errorResponse.getMessage());
                        return errorResponse;
                    } catch (Exception e) {
                        System.out.println("Errore recupero profilo con status: " + response.statusCode());
                        return new AuthResponse(false, "Errore recupero profilo (status: " + response.statusCode() + ")");
                    }
                }

            } catch (Exception e) {
                System.err.println("Errore durante il recupero profilo: " + e.getMessage());
                e.printStackTrace();
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Verifica lo stato di salute del servizio di autenticazione.
     * <p>
     * Esegue un controllo di connettività per verificare se il servizio
     * di autenticazione è attivo e risponde correttamente. Utile per
     * diagnostici e monitoraggio dell'applicazione.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         indicante lo stato del servizio
     *
     * @apiNote Questo metodo ha un timeout ridotto (10 secondi) per rilevare
     *          rapidamente problemi di connettività
     */
    public CompletableFuture<AuthResponse> healthCheckAsync() {
        System.out.println("Test connessione servizio autenticazione...");

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
                    System.out.println("Servizio autenticazione: " + authResponse.getMessage());
                    return authResponse;
                } else {
                    System.out.println("Servizio autenticazione non disponibile (status: " + response.statusCode() + ")");
                    return new AuthResponse(false, "Servizio non disponibile");
                }

            } catch (Exception e) {
                System.err.println("Errore connessione servizio auth: " + e.getMessage());
                return new AuthResponse(false, "Servizio non raggiungibile: " + e.getMessage());
            }
        });
    }

    /**
     * Esegue il logout dell'utente corrente in modo asincrono.
     * <p>
     * Termina la sessione corrente dell'utente, invalidando eventuali token
     * di autenticazione e pulendo lo stato lato server.
     * </p>
     *
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         contenente l'esito dell'operazione di logout
     *
     * @apiNote Il logout è sempre considerato riuscito lato client, anche se
     *          il server non è raggiungibile, per garantire la pulizia locale
     */
    public CompletableFuture<AuthResponse> logoutAsync() {
        System.out.println("Logout...");

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
                    System.out.println("Logout completato");
                    return authResponse;
                } else {
                    return new AuthResponse(false, "Errore durante il logout");
                }

            } catch (Exception e) {
                System.err.println("Errore logout: " + e.getMessage());
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Esegue il reset della password per un utente specifico in modo asincrono.
     * <p>
     * Permette di reimpostare la password di un utente fornendo l'email
     * e la nuova password desiderata. L'operazione non richiede la password
     * attuale ed è tipicamente utilizzata in scenari di recupero account.
     * </p>
     *
     * @param email l'indirizzo email dell'utente per cui resettare la password
     * @param newPassword la nuova password da impostare (deve rispettare i criteri di sicurezza)
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         contenente l'esito dell'operazione
     * @throws IllegalArgumentException se email o newPassword sono {@code null}
     *
     * @apiNote Questa operazione dovrebbe essere protetta da meccanismi di verifica
     *          aggiuntivi (es. token via email) in un ambiente di produzione
     */
    public CompletableFuture<AuthResponse> resetPasswordAsync(String email, String newPassword) {
        if (email == null || newPassword == null) {
            throw new IllegalArgumentException("Email e nuova password non possono essere null");
        }

        System.out.println("Tentativo reset password per: " + email);

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

                System.out.println("Risposta reset password - Status: " + response.statusCode());

                AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                return authResponse;

            } catch (Exception e) {
                System.err.println("Errore reset password: " + e.getMessage());
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Cambia la password per un utente specifico in modo asincrono.
     * <p>
     * Modifica la password corrente dell'utente richiedendo sia la password
     * attuale che quella nuova per motivi di sicurezza. L'operazione richiede
     * l'autenticazione dell'utente.
     * </p>
     *
     * @param userId l'identificativo dell'utente per cui cambiare la password
     * @param oldPassword la password attuale dell'utente per verifica
     * @param newPassword la nuova password da impostare
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         contenente l'esito dell'operazione
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @apiNote L'operazione fallisce se la password attuale non è corretta
     *          o se la nuova password non rispetta i criteri di sicurezza
     */
    public CompletableFuture<AuthResponse> changePasswordAsync(String userId, String oldPassword, String newPassword) {
        if (userId == null || oldPassword == null || newPassword == null) {
            throw new IllegalArgumentException("Tutti i parametri per il cambio password sono obbligatori");
        }

        System.out.println("Tentativo cambio password per utente ID: " + userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // JSON semplice senza userId (che va nell'URL)
                String requestBody = String.format(
                        "{\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}",
                        oldPassword.replace("\"", "\\\""),
                        newPassword.replace("\"", "\\\"")
                );

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/change-password/" + userId))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Risposta cambio password - Status: " + response.statusCode());
                System.out.println("Body: " + response.body());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    boolean success = responseBody.contains("\"success\":true");
                    String message = extractMessageFromJson(responseBody);

                    return new AuthResponse(success, message != null ? message : "Password cambiata con successo");
                } else {
                    String responseBody = response.body();
                    String errorMessage = extractMessageFromJson(responseBody);

                    return new AuthResponse(false, errorMessage != null ? errorMessage : "Errore del server");
                }

            } catch (Exception e) {
                System.err.println("Errore cambio password: " + e.getMessage());
                return new AuthResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Estrae il campo "message" da una stringa JSON.
     * <p>
     * Metodo di utilità per estrarre messaggi dalle risposte del server
     * quando il parsing JSON completo non è necessario o fallisce.
     * </p>
     *
     * @param json la stringa JSON da cui estrarre il messaggio
     * @return il messaggio estratto dal campo "message", o {@code null} se non trovato
     *
     * @implNote Utilizza parsing manuale per maggiore robustezza in caso di
     *           JSON malformato o strutture non standard
     */
    private String extractMessageFromJson(String json) {
        try {
            // Cerca il campo "message" nel JSON
            int messageIndex = json.indexOf("\"message\":");
            if (messageIndex != -1) {
                int startQuote = json.indexOf("\"", messageIndex + 10);
                if (startQuote != -1) {
                    int endQuote = json.indexOf("\"", startQuote + 1);
                    if (endQuote != -1) {
                        return json.substring(startQuote + 1, endQuote);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Aggiorna l'indirizzo email di un utente in modo asincrono.
     * <p>
     * Modifica l'email associata a un account utente. L'operazione richiede
     * che la nuova email non sia già utilizzata da un altro account nel sistema.
     * </p>
     *
     * @param userId l'identificativo dell'utente per cui aggiornare l'email
     * @param newEmail il nuovo indirizzo email da associare all'account
     * @return un {@link CompletableFuture} che si risolve con {@link AuthResponse}
     *         contenente l'esito dell'operazione e i dati aggiornati dell'utente
     * @throws IllegalArgumentException se userId o newEmail sono {@code null}
     *
     * @apiNote L'email deve essere valida e non già presente nel sistema.
     *          L'operazione potrebbe richiedere una verifica via email
     *          per confermare il nuovo indirizzo
     */
    public CompletableFuture<AuthResponse> updateEmailAsync(String userId, String newEmail) {
        if (userId == null || newEmail == null) {
            throw new IllegalArgumentException("ID utente e nuova email non possono essere null");
        }

        System.out.println("Tentativo aggiornamento email per utente ID: " + userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Crea il body della richiesta con la nuova email
                Map<String, String> requestBody = Map.of("email", newEmail);
                String jsonBody = objectMapper.writeValueAsString(requestBody);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/update-email/" + userId))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Risposta aggiornamento email - Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    System.out.println("Email aggiornata con successo");
                    return authResponse;
                } else {
                    try {
                        AuthResponse errorResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                        System.out.println("Errore aggiornamento email: " + errorResponse.getMessage());
                        return errorResponse;
                    } catch (Exception e) {
                        return new AuthResponse(false, "Errore durante l'aggiornamento dell'email");
                    }
                }

            } catch (Exception e) {
                System.err.println("Errore durante l'aggiornamento email: " + e.getMessage());
                return new AuthResponse(false, "Errore di connessione al server");
            }
        });
    }
}