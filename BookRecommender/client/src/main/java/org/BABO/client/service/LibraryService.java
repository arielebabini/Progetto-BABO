package org.BABO.client.service;

import org.BABO.shared.dto.Library.AddBookToLibraryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.BABO.shared.dto.Library.CreateLibraryRequest;
import org.BABO.shared.dto.Library.LibraryResponse;
import org.BABO.shared.dto.Library.RemoveBookFromLibraryRequest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Servizio client per comunicare con l'API delle librerie del server
 * Gestisce tutte le chiamate HTTP per le operazioni CRUD sulle librerie personali
 */
public class LibraryService {

    private static final String SERVER_BASE_URL = "http://localhost:8080/api/library";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LibraryService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Crea una nuova libreria per un utente
     */
    public CompletableFuture<LibraryResponse> createLibraryAsync(String username, String namelib) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createLibrary(username, namelib);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la creazione libreria: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Crea una nuova libreria per un utente
     */
    public LibraryResponse createLibrary(String username, String namelib) throws IOException {
        System.out.println("📚 Creazione libreria '" + namelib + "' per utente: " + username);

        CreateLibraryRequest request = new CreateLibraryRequest(username, namelib);
        String requestBody = objectMapper.writeValueAsString(request);

        Request httpRequest = new Request.Builder()
                .url(SERVER_BASE_URL + "/create")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("✅ Libreria creata con successo: " + namelib);
                } else {
                    System.out.println("❌ Creazione libreria fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Recupera tutte le librerie di un utente
     */
    public CompletableFuture<LibraryResponse> getUserLibrariesAsync(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getUserLibraries(username);
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero librerie: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le librerie di un utente
     */
    public LibraryResponse getUserLibraries(String username) throws IOException {
        System.out.println("📖 Recupero librerie per utente: " + username);

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/user/" + username)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    int count = libraryResponse.getLibraries() != null ? libraryResponse.getLibraries().size() : 0;
                    System.out.println("✅ Recuperate " + count + " librerie per: " + username);
                } else {
                    System.out.println("❌ Recupero librerie fallito: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Recupera tutti i libri in una specifica libreria
     */
    public CompletableFuture<LibraryResponse> getBooksInLibraryAsync(String username, String namelib) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBooksInLibrary(username, namelib);
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero libri: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutti i libri in una specifica libreria
     */
    public LibraryResponse getBooksInLibrary(String username, String namelib) throws IOException {
        System.out.println("📖 Recupero libri nella libreria '" + namelib + "' per: " + username);

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/books/" + username + "/" + namelib)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    int count = libraryResponse.getBooks() != null ? libraryResponse.getBooks().size() : 0;
                    System.out.println("✅ Recuperati " + count + " libri dalla libreria '" + namelib + "'");
                } else {
                    System.out.println("❌ Recupero libri fallito: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Aggiunge un libro a una libreria
     */
    public CompletableFuture<LibraryResponse> addBookToLibraryAsync(String username, String namelib, String isbn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return addBookToLibrary(username, namelib, isbn);
            } catch (Exception e) {
                System.err.println("❌ Errore durante l'aggiunta libro: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiunge un libro a una libreria
     */
    public LibraryResponse addBookToLibrary(String username, String namelib, String isbn) throws IOException {
        System.out.println("➕ Aggiunta libro (ISBN: " + isbn + ") alla libreria '" + namelib + "'");

        AddBookToLibraryRequest request = new AddBookToLibraryRequest(username, namelib, isbn);
        String requestBody = objectMapper.writeValueAsString(request);

        Request httpRequest = new Request.Builder()
                .url(SERVER_BASE_URL + "/add-book")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("✅ Libro aggiunto con successo alla libreria");
                } else {
                    System.out.println("❌ Aggiunta libro fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Rimuove un libro da una libreria
     */
    public CompletableFuture<LibraryResponse> removeBookFromLibraryAsync(String username, String namelib, String isbn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return removeBookFromLibrary(username, namelib, isbn);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la rimozione libro: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Rimuove un libro da una libreria
     */
    public LibraryResponse removeBookFromLibrary(String username, String namelib, String isbn) throws IOException {
        System.out.println("➖ Rimozione libro (ISBN: " + isbn + ") dalla libreria '" + namelib + "'");

        RemoveBookFromLibraryRequest request = new RemoveBookFromLibraryRequest(username, namelib, isbn);
        String requestBody = objectMapper.writeValueAsString(request);

        Request httpRequest = new Request.Builder()
                .url(SERVER_BASE_URL + "/remove-book")
                .delete(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("✅ Libro rimosso con successo dalla libreria");
                } else {
                    System.out.println("❌ Rimozione libro fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Elimina una libreria intera
     */
    public CompletableFuture<LibraryResponse> deleteLibraryAsync(String username, String namelib) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return deleteLibrary(username, namelib);
            } catch (Exception e) {
                System.err.println("❌ Errore durante l'eliminazione libreria: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina una libreria intera
     */
    public LibraryResponse deleteLibrary(String username, String namelib) throws IOException {
        System.out.println("🗑️ Eliminazione libreria '" + namelib + "' per: " + username);

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/delete/" + username + "/" + namelib)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("✅ Libreria eliminata con successo");
                } else {
                    System.out.println("❌ Eliminazione libreria fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }


    /**
     * Verifica se l'utente ha il libro in una libreria
     */
    public CompletableFuture<Boolean> doesUserOwnBookAsync(String username, String isbn) {
        System.out.println("🔍 Verifica possesso libro ISBN: " + isbn + " per utente: " + username);

        return getUserLibrariesAsync(username)
                .thenCompose(librariesResponse -> {
                    if (!librariesResponse.isSuccess() || librariesResponse.getLibraries() == null) {
                        System.out.println("❌ Impossibile recuperare librerie utente");
                        return CompletableFuture.completedFuture(false);
                    }

                    List<String> libraries = librariesResponse.getLibraries();
                    System.out.println("📚 Controllo possesso in " + libraries.size() + " librerie");

                    // Crea una lista di controlli per tutte le librerie
                    List<CompletableFuture<Boolean>> checks = libraries.stream()
                            .map(libraryName -> isBookInLibraryAsync(username, libraryName, isbn))
                            .collect(Collectors.toList());

                    // Combina tutti i controlli - ritorna true se almeno uno è true
                    return CompletableFuture.allOf(checks.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                boolean owns = checks.stream()
                                        .map(CompletableFuture::join)
                                        .anyMatch(Boolean::booleanValue);

                                System.out.println(owns ? "✅ Utente possiede il libro" : "❌ Utente NON possiede il libro");
                                return owns;
                            });
                });
    }

    /**
     * Rinomina una libreria
     */
    public CompletableFuture<LibraryResponse> renameLibraryAsync(String username, String oldName, String newName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return renameLibrary(username, oldName, newName);
            } catch (Exception e) {
                System.err.println("❌ Errore durante la rinomina libreria: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Rinomina una libreria
     */
    public LibraryResponse renameLibrary(String username, String oldName, String newName) throws IOException {
        System.out.println("✏️ Rinomina libreria da '" + oldName + "' a '" + newName + "'");

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/rename/" + username + "/" + oldName + "/" + newName)
                .put(RequestBody.create("", MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("✅ Libreria rinominata con successo");
                } else {
                    System.out.println("❌ Rinomina libreria fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Ottieni statistiche delle librerie di un utente
     */
    public CompletableFuture<LibraryResponse> getUserStatsAsync(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getUserStats(username);
            } catch (Exception e) {
                System.err.println("❌ Errore durante il recupero statistiche: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Ottieni statistiche delle librerie di un utente
     */
    public LibraryResponse getUserStats(String username) throws IOException {
        System.out.println("📊 Recupero statistiche per utente: " + username);

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/stats/" + username)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("✅ Statistiche recuperate per: " + username);
                } else {
                    System.out.println("❌ Recupero statistiche fallito: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Metodo di utilità per verificare se un libro è già in una libreria
     */
    public CompletableFuture<Boolean> isBookInLibraryAsync(String username, String namelib, String isbn) {
        return getBooksInLibraryAsync(username, namelib)
                .thenApply(response -> {
                    if (response.isSuccess() && response.getBooks() != null) {
                        return response.getBooks().stream()
                                .anyMatch(book -> isbn.equals(book.getIsbn()));
                    }
                    return false;
                });
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