package org.BABO.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.BABO.shared.dto.AdminResponse;
import org.BABO.shared.dto.ReviewStatsResponse;
import org.BABO.shared.dto.ReviewsResponse;
import org.BABO.shared.model.User;
import okhttp3.*;

import java.io.IOException;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.Review;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio per operazioni amministrative
 */
public class AdminService {

    private static final String SERVER_BASE_URL = "http://localhost:8080/api/auth";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AdminService() {
        this.httpClient = new OkHttpClient.Builder()
                .build();
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Per LocalDateTime
    }

    /**
     * Recupera tutti gli utenti registrati (solo admin)
     */
    public CompletableFuture<AdminResponse> getAllUsersAsync(String adminEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("👑 Richiesta lista utenti per admin: " + adminEmail);

                HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/admin/users")
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();

                        if (response.isSuccessful()) {
                            Map<String, Object> responseMap = objectMapper.readValue(
                                    jsonResponse, new TypeReference<Map<String, Object>>() {}
                            );

                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> usersData = (List<Map<String, Object>>) responseMap.get("users");

                            List<User> users = objectMapper.convertValue(
                                    usersData, new TypeReference<List<User>>() {}
                            );

                            System.out.println("✅ Recuperati " + users.size() + " utenti");
                            return new AdminResponse(true, "Utenti recuperati con successo", users);

                        } else {
                            Map<String, Object> errorMap = objectMapper.readValue(
                                    jsonResponse, new TypeReference<Map<String, Object>>() {}
                            );
                            String message = (String) errorMap.get("message");

                            System.out.println("❌ Errore server: " + message);
                            return new AdminResponse(false, message, null);
                        }
                    }
                }

                return new AdminResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("❌ Errore recupero utenti: " + e.getMessage());
                return new AdminResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Elimina un utente (solo admin)
     */
    public CompletableFuture<AdminResponse> deleteUserAsync(String userId, String adminEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🗑️ Eliminazione utente " + userId + " per admin: " + adminEmail);

                HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/admin/users/" + userId)
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        Map<String, Object> responseMap = objectMapper.readValue(
                                jsonResponse, new TypeReference<Map<String, Object>>() {}
                        );

                        boolean success = (boolean) responseMap.get("success");
                        String message = (String) responseMap.get("message");

                        if (success) {
                            System.out.println("✅ Utente eliminato con successo");
                        } else {
                            System.out.println("❌ Eliminazione fallita: " + message);
                        }

                        return new AdminResponse(success, message, null);
                    }
                }

                return new AdminResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("❌ Errore eliminazione utente: " + e.getMessage());
                return new AdminResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Classe per le risposte del servizio admin
     */


    /**
     * ===============================
     * METODI GESTIONE LIBRI
     * ===============================
     */

    /**
     * Recupera tutti i libri per admin
     */
    public CompletableFuture<AdminBooksResponse> getAllBooksAsync(String adminEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📚 Richiesta lista libri per admin: " + adminEmail);

                HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/admin/books")
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();

                        if (response.isSuccessful()) {
                            Map<String, Object> responseMap = objectMapper.readValue(
                                    jsonResponse, new TypeReference<Map<String, Object>>() {}
                            );

                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> booksData = (List<Map<String, Object>>) responseMap.get("books");

                            List<Book> books = objectMapper.convertValue(
                                    booksData, new TypeReference<List<Book>>() {}
                            );

                            System.out.println("✅ Recuperati " + books.size() + " libri");
                            return new AdminBooksResponse(true, "Libri recuperati con successo", books);

                        } else {
                            Map<String, Object> errorMap = objectMapper.readValue(
                                    jsonResponse, new TypeReference<Map<String, Object>>() {}
                            );
                            String message = (String) errorMap.get("message");

                            System.out.println("❌ Errore server: " + message);
                            return new AdminBooksResponse(false, message, null);
                        }
                    }
                }

                return new AdminBooksResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("❌ Errore recupero libri: " + e.getMessage());
                return new AdminBooksResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Aggiunge un nuovo libro
     */
    public CompletableFuture<AdminBooksResponse> addBookAsync(String adminEmail, String isbn, String title,
                                                              String author, String description, String year, String category) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📚 Aggiunta libro: " + title + " (" + isbn + ")");

                HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/admin/books")
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .build();

                // Crea JSON body
                Map<String, String> bookData = new HashMap<>();
                bookData.put("isbn", isbn);
                bookData.put("title", title);
                bookData.put("author", author);
                bookData.put("description", description);
                bookData.put("year", year);
                bookData.put("category", category);

                String jsonBody = objectMapper.writeValueAsString(bookData);

                RequestBody body = RequestBody.create(
                        jsonBody, MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        Map<String, Object> responseMap = objectMapper.readValue(
                                jsonResponse, new TypeReference<Map<String, Object>>() {}
                        );

                        boolean success = (boolean) responseMap.get("success");
                        String message = (String) responseMap.get("message");

                        if (success) {
                            System.out.println("✅ Libro aggiunto con successo");
                        } else {
                            System.out.println("❌ Aggiunta fallita: " + message);
                        }

                        return new AdminBooksResponse(success, message, null);
                    }
                }

                return new AdminBooksResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("❌ Errore aggiunta libro: " + e.getMessage());
                return new AdminBooksResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Elimina un libro
     */
    public CompletableFuture<AdminBooksResponse> deleteBookAsync(String adminEmail, String isbn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🗑️ Eliminazione libro ISBN: " + isbn);

                HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/admin/books/" + isbn)
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        Map<String, Object> responseMap = objectMapper.readValue(
                                jsonResponse, new TypeReference<Map<String, Object>>() {}
                        );

                        boolean success = (boolean) responseMap.get("success");
                        String message = (String) responseMap.get("message");

                        if (success) {
                            System.out.println("✅ Libro eliminato con successo");
                        } else {
                            System.out.println("❌ Eliminazione fallita: " + message);
                        }

                        return new AdminBooksResponse(success, message, null);
                    }
                }

                return new AdminBooksResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("❌ Errore eliminazione libro: " + e.getMessage());
                return new AdminBooksResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * ===============================
     * METODI PER GESTIONE RECENSIONI
     * ===============================
     */

    /**
     * Elimina una recensione specifica (solo per admin)
     */
    public CompletableFuture<Map<String, Object>> deleteReviewAsync(String username, String isbn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String adminEmail = getCurrentAdminEmail();
                if (adminEmail == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Accesso admin richiesto");
                    return error;
                }

                HttpUrl url = HttpUrl.parse("http://localhost:8080/api/ratings/admin/delete")
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .addQueryParameter("username", username)
                        .addQueryParameter("isbn", isbn)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                    } else {
                        Map<String, Object> error = new HashMap<>();
                        error.put("success", false);
                        error.put("message", "Errore server: " + response.code());
                        return error;
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nell'eliminazione recensione: " + e.getMessage());
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Errore di connessione: " + e.getMessage());
                return error;
            }
        });
    }

    /**
     * Elimina tutte le recensioni di un utente specifico (solo per admin)
     */
    public CompletableFuture<AdminResponse> deleteAllUserReviewsAsync(String adminEmail, String targetUsername) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🚫 Eliminazione tutte recensioni utente: " + targetUsername + " da admin: " + adminEmail);

                Request request = new Request.Builder()
                        .url(SERVER_BASE_URL + "/admin/reviews/user/" + targetUsername + "?adminEmail=" + adminEmail)
                        .delete()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        System.out.println("📨 Risposta eliminazione recensioni utente: " + jsonResponse);

                        AdminResponse adminResponse = objectMapper.readValue(jsonResponse, AdminResponse.class);
                        return adminResponse;
                    } else {
                        return new AdminResponse(false, "Risposta vuota dal server");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore eliminazione recensioni utente: " + e.getMessage());
                e.printStackTrace();
                return new AdminResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Modera una recensione (nasconde/approva) (solo per admin)
     */
    public CompletableFuture<AdminResponse> moderateReviewAsync(String adminEmail, Long reviewId, boolean approve) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String action = approve ? "approve" : "hide";
                System.out.println("🔍 Moderazione recensione ID: " + reviewId + " - azione: " + action + " da admin: " + adminEmail);

                // Crea JSON per la richiesta
                String jsonBody = objectMapper.writeValueAsString(Map.of(
                        "adminEmail", adminEmail,
                        "action", action
                ));

                Request request = new Request.Builder()
                        .url(SERVER_BASE_URL + "/admin/reviews/" + reviewId + "/moderate")
                        .put(RequestBody.create(jsonBody, MediaType.get("application/json")))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        System.out.println("📨 Risposta moderazione recensione: " + jsonResponse);

                        AdminResponse adminResponse = objectMapper.readValue(jsonResponse, AdminResponse.class);
                        return adminResponse;
                    } else {
                        return new AdminResponse(false, "Risposta vuota dal server");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore moderazione recensione: " + e.getMessage());
                e.printStackTrace();
                return new AdminResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le recensioni dal server (per admin)
     */
    public CompletableFuture<Map<String, Object>> getAllReviewsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String adminEmail = getCurrentAdminEmail();
                if (adminEmail == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Accesso admin richiesto");
                    return error;
                }

                HttpUrl url = HttpUrl.parse("http://localhost:8080/api/ratings/admin/all")
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                    } else {
                        Map<String, Object> error = new HashMap<>();
                        error.put("success", false);
                        error.put("message", "Errore server: " + response.code());
                        return error;
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero recensioni: " + e.getMessage());
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Errore di connessione: " + e.getMessage());
                return error;
            }
        });
    }

    /**
     * Recupera statistiche sulle recensioni (per admin)
     */
    public CompletableFuture<Map<String, Object>> getReviewsStatsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String adminEmail = getCurrentAdminEmail();
                if (adminEmail == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Accesso admin richiesto");
                    return error;
                }

                HttpUrl url = HttpUrl.parse("http://localhost:8080/api/ratings/admin/stats")
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                    } else {
                        Map<String, Object> error = new HashMap<>();
                        error.put("success", false);
                        error.put("message", "Errore server: " + response.code());
                        return error;
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Errore nel recupero statistiche: " + e.getMessage());
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Errore di connessione: " + e.getMessage());
                return error;
            }
        });
    }

    /**
     * Helper per ottenere l'email dell'admin corrente
     */
    private String getCurrentAdminEmail() {
        // Questo metodo dovrebbe ottenere l'email dell'utente corrente autenticato
        // Assumiamo che sia disponibile tramite AuthenticationManager
        // Modifica secondo la tua implementazione

        try {
            // Esempio: return AuthenticationManager.getInstance().getCurrentUser().getEmail();
            return "federico@admin.com"; // PLACEHOLDER - sostituisci con implementazione reale
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero email admin: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper per encoding URL
     */
    private String encodeUrl(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Classe per le risposte del servizio admin libri
     */
    public static class AdminBooksResponse {
        private final boolean success;
        private final String message;
        private final List<Book> books;

        public AdminBooksResponse(boolean success, String message, List<Book> books) {
            this.success = success;
            this.message = message;
            this.books = books;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<Book> getBooks() { return books; }
    }
}