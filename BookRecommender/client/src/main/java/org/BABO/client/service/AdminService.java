package org.BABO.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.BABO.shared.model.User;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    public static class AdminResponse {
        private final boolean success;
        private final String message;
        private final List<User> users;

        public AdminResponse(boolean success, String message, List<User> users) {
            this.success = success;
            this.message = message;
            this.users = users;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<User> getUsers() { return users; }
    }
}