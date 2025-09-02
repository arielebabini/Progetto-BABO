package org.BABO.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.BABO.shared.dto.AdminResponse;
import org.BABO.shared.model.BookRating;
import org.BABO.shared.model.User;
import okhttp3.*;

import org.BABO.shared.model.Book;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servizio per gestire le operazioni amministrative del sistema BookRecommender.
 * <p>
 * Questa classe fornisce un'interfaccia client per tutte le operazioni riservate
 * agli amministratori del sistema, inclusa la gestione di utenti, libri e recensioni.
 * Tutte le operazioni richiedono privilegi amministrativi e vengono validate lato server.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Gestione Utenti:</strong> Visualizzazione ed eliminazione account utente</li>
 *   <li><strong>Gestione Libri:</strong> Aggiunta, eliminazione e visualizzazione catalogo</li>
 *   <li><strong>Gestione Recensioni:</strong> Moderazione e rimozione valutazioni inappropriate</li>
 *   <li><strong>Operazioni Asincrone:</strong> Tutte le operazioni utilizzano {@link CompletableFuture}</li>
 * </ul>
 *
 * <h3>Endpoint amministrativi supportati:</h3>
 * <ul>
 *   <li>{@code GET /api/auth/admin/users} - Lista tutti gli utenti</li>
 *   <li>{@code DELETE /api/auth/admin/users/{userId}} - Elimina utente specifico</li>
 *   <li>{@code GET /api/auth/admin/books} - Lista tutti i libri</li>
 *   <li>{@code POST /api/auth/admin/books} - Aggiunge nuovo libro</li>
 *   <li>{@code DELETE /api/auth/admin/books/{isbn}} - Elimina libro</li>
 *   <li>{@code GET /api/auth/admin/ratings} - Lista tutte le recensioni</li>
 *   <li>{@code DELETE /api/ratings/admin/delete} - Elimina recensione specifica</li>
 *   <li>{@code DELETE /api/auth/admin/reviews/user/{username}} - Elimina tutte le recensioni di un utente</li>
 * </ul>
 *
 * <h3>Sicurezza e autorizzazione:</h3>
 * <p>
 * Tutti i metodi richiedono l'email di un amministratore valido come parametro.
 * Il server verifica i privilegi amministrativi prima di eseguire qualsiasi operazione.
 * Le operazioni non autorizzate vengono respinte con messaggi di errore appropriati.
 * </p>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * AdminService adminService = new AdminService();
 * String adminEmail = "admin@bookrecommender.com";
 *
 * // Recupero lista utenti
 * adminService.getAllUsersAsync(adminEmail)
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             List<User> users = response.getUsers();
 *             System.out.println("Trovati " + users.size() + " utenti");
 *         } else {
 *             System.out.println("Errore: " + response.getMessage());
 *         }
 *     });
 *
 * // Aggiunta nuovo libro
 * adminService.addBookAsync(adminEmail, "978-0123456789", "Nuovo Libro",
 *                          "Autore Esempio", "Descrizione libro", "2024", "Fiction")
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Libro aggiunto con successo");
 *         }
 *     });
 *
 * // Eliminazione recensione inappropriata
 * adminService.deleteRatingAsync(adminEmail, "utente123", "978-0123456789")
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Recensione rimossa");
 *         }
 *     });
 * }</pre>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see AdminResponse
 * @see User
 * @see Book
 * @see BookRating
 */
public class AdminService {

    /** URL base per tutte le operazioni amministrative */
    private static final String SERVER_BASE_URL = "http://localhost:8080/api/auth";

    /** Client HTTP per le richieste al server */
    private final OkHttpClient httpClient;

    /** Mapper JSON per serializzazione/deserializzazione con supporto per LocalDateTime */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore del servizio amministrativo.
     * <p>
     * Inizializza il client HTTP e configura l'ObjectMapper con i moduli necessari
     * per gestire correttamente i tipi di data come LocalDateTime.
     * </p>
     */
    public AdminService() {
        this.httpClient = new OkHttpClient.Builder()
                .build();
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Per LocalDateTime
    }

    /**
     * Recupera la lista completa di tutti gli utenti registrati nel sistema.
     * <p>
     * Questa operazione è riservata agli amministratori e restituisce informazioni
     * dettagliate su tutti gli account utente, inclusi dati personali e statistiche
     * di utilizzo. Le informazioni sensibili come le password sono escluse dalla risposta.
     * </p>
     *
     * @param adminEmail l'indirizzo email dell'amministratore che richiede l'operazione
     *                   (deve avere privilegi amministrativi validi)
     * @return un {@link CompletableFuture} che si risolve con {@link AdminResponse}
     *         contenente la lista di tutti gli utenti del sistema
     * @throws IllegalArgumentException se adminEmail è {@code null}
     *
     * @apiNote L'operazione può richiedere tempo significativo per sistemi con molti utenti.
     *          I dati restituiti includono informazioni personali, quindi devono essere
     *          trattati in conformità alle normative sulla privacy.
     */
    public CompletableFuture<AdminResponse> getAllUsersAsync(String adminEmail) {
        if (adminEmail == null) {
            throw new IllegalArgumentException("L'email dell'amministratore non può essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Richiesta lista utenti per admin: " + adminEmail);

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

                            System.out.println("Recuperati " + users.size() + " utenti");
                            return new AdminResponse(true, "Utenti recuperati con successo", users);

                        } else {
                            Map<String, Object> errorMap = objectMapper.readValue(
                                    jsonResponse, new TypeReference<Map<String, Object>>() {}
                            );
                            String message = (String) errorMap.get("message");

                            System.out.println("Errore server: " + message);
                            return new AdminResponse(false, message, null);
                        }
                    }
                }

                return new AdminResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("Errore recupero utenti: " + e.getMessage());
                return new AdminResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Elimina definitivamente un account utente dal sistema.
     * <p>
     * Questa operazione rimuove completamente l'utente specificato dal database,
     * inclusi tutti i dati associati come recensioni, valutazioni e preferenze.
     * L'operazione è irreversibile e deve essere utilizzata con cautela.
     * </p>
     *
     * @param userId l'identificativo univoco dell'utente da eliminare
     * @param adminEmail l'indirizzo email dell'amministratore che richiede l'eliminazione
     * @return un {@link CompletableFuture} che si risolve con {@link AdminResponse}
     *         indicante l'esito dell'operazione di eliminazione
     * @throws IllegalArgumentException se userId o adminEmail sono {@code null}
     *
     * @apiNote Questa operazione elimina anche tutti i dati correlati all'utente
     *          (recensioni, valutazioni, ecc.) per rispettare i vincoli di integrità
     *          referenziale del database.
     *
     * @implNote L'eliminazione viene eseguita in una transazione per garantire
     *           la consistenza dei dati in caso di errori durante il processo.
     */
    public CompletableFuture<AdminResponse> deleteUserAsync(String userId, String adminEmail) {
        if (userId == null || adminEmail == null) {
            throw new IllegalArgumentException("ID utente e email amministratore non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Eliminazione utente " + userId + " per admin: " + adminEmail);

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
                            System.out.println("Utente eliminato con successo");
                        } else {
                            System.out.println("Eliminazione fallita: " + message);
                        }

                        return new AdminResponse(success, message, null);
                    }
                }

                return new AdminResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("Errore eliminazione utente: " + e.getMessage());
                return new AdminResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Recupera la lista completa di tutti i libri presenti nel catalogo.
     * <p>
     * Questa operazione amministrativa restituisce informazioni dettagliate su tutti
     * i libri nel sistema, inclusi metadati, statistiche di visualizzazione e stato
     * di pubblicazione. È utile per la gestione del catalogo e l'analisi dei contenuti.
     * </p>
     *
     * @param adminEmail l'indirizzo email dell'amministratore che richiede l'operazione
     * @return un {@link CompletableFuture} che si risolve con {@link AdminBooksResponse}
     *         contenente la lista completa dei libri del catalogo
     * @throws IllegalArgumentException se adminEmail è {@code null}
     *
     * @see AdminBooksResponse
     */
    public CompletableFuture<AdminBooksResponse> getAllBooksAsync(String adminEmail) {
        if (adminEmail == null) {
            throw new IllegalArgumentException("L'email dell'amministratore non può essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Richiesta lista libri per admin: " + adminEmail);

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

                            System.out.println("Recuperati " + books.size() + " libri");
                            return new AdminBooksResponse(true, "Libri recuperati con successo", books);

                        } else {
                            Map<String, Object> errorMap = objectMapper.readValue(
                                    jsonResponse, new TypeReference<Map<String, Object>>() {}
                            );
                            String message = (String) errorMap.get("message");

                            System.out.println("Errore server: " + message);
                            return new AdminBooksResponse(false, message, null);
                        }
                    }
                }

                return new AdminBooksResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("Errore recupero libri: " + e.getMessage());
                return new AdminBooksResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Aggiunge un nuovo libro al catalogo del sistema.
     * <p>
     * Questa operazione crea una nuova voce nel catalogo con tutti i metadati
     * specificati. Il sistema verifica che l'ISBN non sia già presente per
     * evitare duplicati. Dopo l'aggiunta, il libro diventa immediatamente
     * disponibile per la ricerca e la consultazione da parte degli utenti.
     * </p>
     *
     * @param adminEmail l'indirizzo email dell'amministratore che aggiunge il libro
     * @param isbn il codice ISBN univoco del libro (deve essere valido e non duplicato)
     * @param title il titolo completo del libro
     * @param author il nome dell'autore o degli autori
     * @param description una descrizione dettagliata del contenuto del libro
     * @param year l'anno di pubblicazione (deve essere un anno valido)
     * @param category la categoria o genere letterario del libro
     * @return un {@link CompletableFuture} che si risolve con {@link AdminBooksResponse}
     *         indicante l'esito dell'operazione di aggiunta
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @apiNote L'ISBN deve essere unico nel sistema. Se esiste già un libro con
     *          lo stesso ISBN, l'operazione fallirà con un messaggio di errore specifico.
     */
    public CompletableFuture<AdminBooksResponse> addBookAsync(String adminEmail, String isbn, String title,
                                                              String author, String description, String year, String category) {
        if (adminEmail == null || isbn == null || title == null || author == null ||
                description == null || year == null || category == null) {
            throw new IllegalArgumentException("Tutti i parametri del libro sono obbligatori");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Aggiunta libro: " + title + " (" + isbn + ")");

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
                            System.out.println("Libro aggiunto con successo");
                        } else {
                            System.out.println("Aggiunta fallita: " + message);
                        }

                        return new AdminBooksResponse(success, message, null);
                    }
                }

                return new AdminBooksResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("Errore aggiunta libro: " + e.getMessage());
                return new AdminBooksResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Elimina definitivamente un libro dal catalogo del sistema.
     * <p>
     * Questa operazione rimuove completamente il libro identificato dall'ISBN
     * dal sistema, incluse tutte le recensioni, valutazioni e dati associati.
     * L'operazione è irreversibile e deve essere utilizzata con cautela.
     * </p>
     *
     * @param adminEmail l'indirizzo email dell'amministratore che richiede l'eliminazione
     * @param isbn il codice ISBN del libro da eliminare
     * @return un {@link CompletableFuture} che si risolve con {@link AdminBooksResponse}
     *         indicante l'esito dell'operazione di eliminazione
     * @throws IllegalArgumentException se adminEmail o isbn sono {@code null}
     *
     * @apiNote L'eliminazione di un libro comporta anche la rimozione di tutte
     *          le recensioni e valutazioni associate per mantenere l'integrità
     *          referenziale del database.
     *
     * @implNote L'operazione viene eseguita in una transazione per garantire
     *           che tutti i dati correlati vengano rimossi in modo consistente.
     */
    public CompletableFuture<AdminBooksResponse> deleteBookAsync(String adminEmail, String isbn) {
        if (adminEmail == null || isbn == null) {
            throw new IllegalArgumentException("Email amministratore e ISBN non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Eliminazione libro ISBN: " + isbn);

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
                            System.out.println("Libro eliminato con successo");
                        } else {
                            System.out.println("Eliminazione fallita: " + message);
                        }

                        return new AdminBooksResponse(success, message, null);
                    }
                }

                return new AdminBooksResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("Errore eliminazione libro: " + e.getMessage());
                return new AdminBooksResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Elimina una recensione specifica di un utente per un libro particolare.
     * <p>
     * Questa operazione di moderazione permette agli amministratori di rimuovere
     * recensioni inappropriate, spam o che violano le linee guida della community.
     * La recensione viene identificata univocamente dalla combinazione di username
     * e ISBN del libro.
     * </p>
     *
     * @param adminEmail l'indirizzo email dell'amministratore che richiede l'eliminazione
     * @param username il nome utente dell'autore della recensione da eliminare
     * @param isbn il codice ISBN del libro per cui eliminare la recensione
     * @return un {@link CompletableFuture} che si risolve con {@link AdminRatingsResponse}
     *         indicante l'esito dell'operazione di eliminazione
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @apiNote Questa operazione utilizza l'endpoint del RatingController e non
     *          quello dell'AuthController per una gestione più specifica delle recensioni.
     *
     * @see AdminRatingsResponse
     */
    public CompletableFuture<AdminRatingsResponse> deleteRatingAsync(String adminEmail, String username, String isbn) {
        if (adminEmail == null || username == null || isbn == null) {
            throw new IllegalArgumentException("Tutti i parametri per l'eliminazione della recensione sono obbligatori");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Eliminazione recensione per " + username + " - ISBN: " + isbn + " da admin: " + adminEmail);

                // Costruisce l'URL per l'endpoint del RatingController
                HttpUrl url = HttpUrl.parse("http://localhost:8080/api/ratings/admin/delete")
                        .newBuilder()
                        .addQueryParameter("adminEmail", adminEmail)
                        .addQueryParameter("username", username)
                        .addQueryParameter("isbn", isbn)
                        .build();

                // Crea la richiesta DELETE
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();

                // Esegue la richiesta
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();

                        // Parsing della risposta JSON
                        Map<String, Object> responseMap = objectMapper.readValue(
                                jsonResponse, new TypeReference<Map<String, Object>>() {}
                        );

                        boolean success = (boolean) responseMap.get("success");
                        String message = (String) responseMap.get("message");

                        if (success) {
                            System.out.println("Recensione eliminata con successo");
                        } else {
                            System.out.println("Eliminazione fallita: " + message);
                        }

                        return new AdminRatingsResponse(success, message, null);
                    } else {
                        System.err.println("Risposta vuota dal server");
                        return new AdminRatingsResponse(false, "Risposta vuota dal server", null);
                    }
                }

            } catch (Exception e) {
                System.err.println("Errore eliminazione recensione: " + e.getMessage());
                e.printStackTrace();
                return new AdminRatingsResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Elimina tutte le recensioni associate a un utente specifico.
     * <p>
     * Questa operazione di moderazione massiva permette agli amministratori di
     * rimuovere completamente la presenza di un utente dal sistema di recensioni,
     * tipicamente utilizzata in casi di comportamenti abusivi o violazioni gravi
     * delle linee guida.
     * </p>
     *
     * @param adminEmail l'indirizzo email dell'amministratore che richiede l'eliminazione
     * @param targetUsername il nome utente di cui eliminare tutte le recensioni
     * @return un {@link CompletableFuture} che si risolve con {@link AdminResponse}
     *         indicante l'esito dell'operazione e il numero di recensioni eliminate
     * @throws IllegalArgumentException se adminEmail o targetUsername sono {@code null}
     *
     * @apiNote Questa operazione può richiedere tempo significativo per utenti
     *          con molte recensioni. Il numero di recensioni eliminate viene
     *          incluso nel messaggio di risposta.
     */
    public CompletableFuture<AdminResponse> deleteAllUserReviewsAsync(String adminEmail, String targetUsername) {
        if (adminEmail == null || targetUsername == null) {
            throw new IllegalArgumentException("Email amministratore e username target non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Eliminazione tutte recensioni utente: " + targetUsername + " da admin: " + adminEmail);

                Request request = new Request.Builder()
                        .url(SERVER_BASE_URL + "/admin/reviews/user/" + targetUsername + "?adminEmail=" + adminEmail)
                        .delete()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        System.out.println("Risposta eliminazione recensioni utente: " + jsonResponse);

                        AdminResponse adminResponse = objectMapper.readValue(jsonResponse, AdminResponse.class);
                        return adminResponse;
                    } else {
                        return new AdminResponse(false, "Risposta vuota dal server");
                    }
                }

            } catch (Exception e) {
                System.err.println("Errore eliminazione recensioni utente: " + e.getMessage());
                e.printStackTrace();
                return new AdminResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera la lista completa di tutte le recensioni presenti nel sistema.
     * <p>
     * Questa operazione amministrativa fornisce una panoramica completa di tutte
     * le valutazioni e recensioni degli utenti, utile per attività di moderazione,
     * analisi della qualità dei contenuti e statistiche del sistema.
     * </p>
     *
     * @param adminEmail l'indirizzo email dell'amministratore che richiede l'operazione
     * @return un {@link CompletableFuture} che si risolve con {@link AdminRatingsResponse}
     *         contenente la lista completa delle recensioni del sistema
     * @throws IllegalArgumentException se adminEmail è {@code null}
     *
     * @apiNote Questa operazione può restituire grandi quantità di dati per sistemi
     *          con molte recensioni. Considerare l'implementazione di paginazione
     *          per migliorare le prestazioni in ambienti di produzione.
     *
     * @see AdminRatingsResponse
     * @see BookRating
     */
    public CompletableFuture<AdminRatingsResponse> getAllReviewsAsync(String adminEmail) {
        if (adminEmail == null) {
            throw new IllegalArgumentException("L'email dell'amministratore non può essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Richiesta lista recensioni per admin: " + adminEmail);

                HttpUrl url = HttpUrl.parse(SERVER_BASE_URL + "/admin/ratings")
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
                            List<Map<String, Object>> ratingsData = (List<Map<String, Object>>) responseMap.get("ratings");

                            List<BookRating> ratings = objectMapper.convertValue(
                                    ratingsData, new TypeReference<List<BookRating>>() {}
                            );

                            System.out.println("Recuperate " + ratings.size() + " recensioni");
                            return new AdminRatingsResponse(true, "Recensioni recuperate con successo", ratings);

                        } else {
                            Map<String, Object> errorMap = objectMapper.readValue(
                                    jsonResponse, new TypeReference<Map<String, Object>>() {}
                            );
                            String message = (String) errorMap.get("message");

                            System.out.println("Errore server: " + message);
                            return new AdminRatingsResponse(false, message, null);
                        }
                    }
                }

                return new AdminRatingsResponse(false, "Risposta vuota dal server", null);

            } catch (Exception e) {
                System.err.println("Errore recupero recensioni: " + e.getMessage());
                return new AdminRatingsResponse(false, "Errore di connessione: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Classe di risposta per le operazioni amministrative sui libri.
     * <p>
     * Incapsula i risultati delle operazioni CRUD sui libri del catalogo,
     * fornendo informazioni sull'esito dell'operazione e i dati dei libri
     * quando applicabile.
     * </p>
     *
     * @since 1.0
     */
    public static class AdminBooksResponse {

        /** Indica se l'operazione è stata completata con successo */
        private final boolean success;

        /** Messaggio descrittivo dell'esito dell'operazione */
        private final String message;

        /** Lista dei libri restituiti dall'operazione (può essere null) */
        private final List<Book> books;

        /**
         * Costruttore per creare una risposta delle operazioni sui libri.
         *
         * @param success true se l'operazione è riuscita, false altrimenti
         * @param message messaggio descrittivo dell'operazione
         * @param books lista dei libri (può essere null per operazioni che non restituiscono dati)
         */
        public AdminBooksResponse(boolean success, String message, List<Book> books) {
            this.success = success;
            this.message = message;
            this.books = books;
        }

        /**
         * Verifica se l'operazione è stata completata con successo.
         *
         * @return true se l'operazione è riuscita, false altrimenti
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Ottiene il messaggio descrittivo dell'operazione.
         *
         * @return il messaggio che descrive l'esito dell'operazione
         */
        public String getMessage() {
            return message;
        }

        /**
         * Ottiene la lista dei libri restituiti dall'operazione.
         *
         * @return la lista dei libri, o null se l'operazione non restituisce dati sui libri
         */
        public List<Book> getBooks() {
            return books;
        }
    }

    /**
     * Classe di risposta per le operazioni amministrative sulle recensioni.
     * <p>
     * Incapsula i risultati delle operazioni di moderazione delle recensioni,
     * fornendo informazioni sull'esito dell'operazione e i dati delle valutazioni
     * quando applicabile.
     * </p>
     *
     * @since 1.0
     */
    public static class AdminRatingsResponse {

        /** Indica se l'operazione è stata completata con successo */
        private final boolean success;

        /** Messaggio descrittivo dell'esito dell'operazione */
        private final String message;

        /** Lista delle recensioni restituite dall'operazione (può essere null) */
        private final List<BookRating> ratings;

        /**
         * Costruttore per creare una risposta delle operazioni sulle recensioni.
         *
         * @param success true se l'operazione è riuscita, false altrimenti
         * @param message messaggio descrittivo dell'operazione
         * @param ratings lista delle recensioni (può essere null per operazioni che non restituiscono dati)
         */
        public AdminRatingsResponse(boolean success, String message, List<BookRating> ratings) {
            this.success = success;
            this.message = message;
            this.ratings = ratings;
        }

        /**
         * Verifica se l'operazione è stata completata con successo.
         *
         * @return true se l'operazione è riuscita, false altrimenti
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Ottiene il messaggio descrittivo dell'operazione.
         *
         * @return il messaggio che descrive l'esito dell'operazione
         */
        public String getMessage() {
            return message;
        }

        /**
         * Ottiene la lista delle recensioni restituite dall'operazione.
         *
         * @return la lista delle recensioni, o null se l'operazione non restituisce dati sulle recensioni
         */
        public List<BookRating> getRatings() {
            return ratings;
        }

        /**
         * Rappresentazione testuale dell'oggetto per debugging.
         *
         * @return una stringa che descrive lo stato della risposta
         */
        @Override
        public String toString() {
            return "AdminRatingsResponse{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", ratings=" + (ratings != null ? ratings.size() + " items" : "null") +
                    '}';
        }
    }
}