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
 * Servizio per la gestione completa delle librerie personali degli utenti.
 * <p>
 * Questa classe fornisce un'interfaccia per tutte le operazioni relative alle
 * librerie personalizzate degli utenti, permettendo di organizzare, gestire
 * e categorizzare le collezioni di libri. Ogni utente può creare multiple
 * librerie tematiche per organizzare i propri libri secondo preferenze personali.
 * </p>
 *
 * <h3>Concetto di libreria:</h3>
 * <p>
 * Una libreria rappresenta una collezione personalizzata di libri creata dall'utente
 * per organizzare la propria esperienza di lettura. Gli utenti possono creare
 * librerie tematiche come "Da Leggere", "Preferiti", "Libri di Studio", etc.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Gestione Librerie:</strong> Creazione, rinomina ed eliminazione di librerie</li>
 *   <li><strong>Gestione Contenuti:</strong> Aggiunta e rimozione di libri dalle librerie</li>
 *   <li><strong>Consultazione:</strong> Visualizzazione dei contenuti e statistiche</li>
 *   <li><strong>Verifica Possesso:</strong> Controllo della presenza di libri nelle collezioni</li>
 *   <li><strong>Operazioni Asincrone:</strong> Tutte le operazioni utilizzano {@link CompletableFuture}</li>
 * </ul>
 *
 * <h3>Endpoint supportati:</h3>
 * <ul>
 *   <li>{@code POST /api/library/create} - Crea una nuova libreria</li>
 *   <li>{@code GET /api/library/user/{username}} - Recupera tutte le librerie di un utente</li>
 *   <li>{@code GET /api/library/books/{username}/{namelib}} - Recupera i libri di una libreria</li>
 *   <li>{@code POST /api/library/add-book} - Aggiunge un libro a una libreria</li>
 *   <li>{@code DELETE /api/library/remove-book} - Rimuove un libro da una libreria</li>
 *   <li>{@code DELETE /api/library/delete/{username}/{namelib}} - Elimina una libreria</li>
 *   <li>{@code PUT /api/library/rename/{username}/{oldName}/{newName}} - Rinomina una libreria</li>
 *   <li>{@code GET /api/library/stats/{username}} - Recupera statistiche delle librerie</li>
 * </ul>
 *
 * <h3>Regole di business:</h3>
 * <ul>
 *   <li>Ogni utente può creare multiple librerie con nomi univoci</li>
 *   <li>I nomi delle librerie devono essere univoci per utente</li>
 *   <li>Un libro può essere presente in multiple librerie dello stesso utente</li>
 *   <li>L'eliminazione di una libreria rimuove tutti i libri contenuti</li>
 *   <li>Le operazioni sono limitate al proprietario della libreria</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * LibraryService libraryService = new LibraryService();
 * String username = "lettore123";
 *
 * // Creazione di una nuova libreria
 * libraryService.createLibraryAsync(username, "Romanzi Classici")
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Libreria creata: " + response.getMessage());
 *
 *             // Aggiunta di un libro alla libreria
 *             return libraryService.addBookToLibraryAsync(username, "Romanzi Classici", "978-0123456789");
 *         } else {
 *             System.out.println("Errore: " + response.getMessage());
 *             return CompletableFuture.completedFuture(response);
 *         }
 *     })
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Libro aggiunto alla libreria");
 *         }
 *     });
 *
 * // Verifica se un utente possiede un libro specifico
 * libraryService.doesUserOwnBookAsync(username, "978-0123456789")
 *     .thenAccept(owns -> {
 *         if (owns) {
 *             System.out.println("L'utente possiede questo libro");
 *         } else {
 *             System.out.println("L'utente non possiede questo libro");
 *         }
 *     });
 *
 * // Recupero statistiche dell'utente
 * libraryService.getUserStatsAsync(username)
 *     .thenAccept(response -> {
 *         if (response.isSuccess()) {
 *             System.out.println("Statistiche utente recuperate");
 *             // Elaborazione delle statistiche...
 *         }
 *     });
 *
 * // Chiusura risorse
 * libraryService.shutdown();
 * }</pre>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see LibraryResponse
 * @see CreateLibraryRequest
 * @see AddBookToLibraryRequest
 * @see RemoveBookFromLibraryRequest
 */
public class LibraryService {

    /** URL base per tutte le operazioni sulle librerie */
    private static final String SERVER_BASE_URL = "http://localhost:8080/api/library";

    /** Client HTTP per le richieste al server con timeout configurati */
    private final OkHttpClient httpClient;

    /** Mapper JSON per serializzazione/deserializzazione */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore del servizio per la gestione delle librerie.
     * <p>
     * Inizializza il client HTTP con timeout ottimizzati per le operazioni
     * sulle librerie: 10 secondi per la connessione e 30 secondi per la lettura.
     * Configura l'ObjectMapper per la gestione JSON standard.
     * </p>
     */
    public LibraryService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Crea una nuova libreria per un utente specificato in modo asincrono.
     * <p>
     * Questa operazione crea una nuova collezione personalizzata di libri
     * per l'utente con il nome specificato. Il nome deve essere univoco
     * tra tutte le librerie dell'utente.
     * </p>
     *
     * @param username il nome utente proprietario della nuova libreria
     * @param namelib il nome da assegnare alla nuova libreria (deve essere univoco per l'utente)
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         contenente l'esito dell'operazione e i dettagli della libreria creata
     * @throws IllegalArgumentException se username o namelib sono {@code null}
     *
     * @see #createLibrary(String, String)
     */
    public CompletableFuture<LibraryResponse> createLibraryAsync(String username, String namelib) {
        if (username == null || namelib == null) {
            throw new IllegalArgumentException("Username e nome libreria non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return createLibrary(username, namelib);
            } catch (Exception e) {
                System.err.println("Errore durante la creazione libreria: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Crea una nuova libreria per un utente specificato in modo sincrono.
     * <p>
     * Versione sincrona del metodo di creazione libreria. Esegue direttamente
     * la richiesta HTTP e restituisce immediatamente il risultato.
     * </p>
     *
     * @param username il nome utente proprietario della nuova libreria
     * @param namelib il nome da assegnare alla nuova libreria
     * @return {@link LibraryResponse} con l'esito dell'operazione
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se username o namelib sono {@code null}
     */
    public LibraryResponse createLibrary(String username, String namelib) throws IOException {
        if (username == null || namelib == null) {
            throw new IllegalArgumentException("Username e nome libreria non possono essere null");
        }

        System.out.println("Creazione libreria '" + namelib + "' per utente: " + username);

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
                    System.out.println("Libreria creata con successo: " + namelib);
                } else {
                    System.out.println("Creazione libreria fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Recupera tutte le librerie di un utente in modo asincrono.
     * <p>
     * Restituisce l'elenco completo di tutte le librerie create dall'utente,
     * inclusi i nomi e i metadati di base. È utile per mostrare una panoramica
     * delle collezioni dell'utente e per la navigazione tra librerie.
     * </p>
     *
     * @param username il nome utente di cui recuperare le librerie
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         contenente la lista di tutte le librerie dell'utente
     * @throws IllegalArgumentException se username è {@code null}
     *
     * @see #getUserLibraries(String)
     */
    public CompletableFuture<LibraryResponse> getUserLibrariesAsync(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Lo username non può essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return getUserLibraries(username);
            } catch (Exception e) {
                System.err.println("Errore durante il recupero librerie: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutte le librerie di un utente in modo sincrono.
     *
     * @param username il nome utente di cui recuperare le librerie
     * @return {@link LibraryResponse} contenente la lista delle librerie
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se username è {@code null}
     */
    public LibraryResponse getUserLibraries(String username) throws IOException {
        if (username == null) {
            throw new IllegalArgumentException("Lo username non può essere null");
        }

        System.out.println("Recupero librerie per utente: " + username);

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
                    System.out.println("Recuperate " + count + " librerie per: " + username);
                } else {
                    System.out.println("Recupero librerie fallito: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Recupera tutti i libri contenuti in una libreria specifica in modo asincrono.
     * <p>
     * Restituisce l'elenco completo dei libri presenti nella libreria identificata
     * dal nome, inclusi tutti i metadati dei libri. È essenziale per visualizzare
     * il contenuto delle collezioni e per operazioni di gestione dei libri.
     * </p>
     *
     * @param username il nome utente proprietario della libreria
     * @param namelib il nome della libreria di cui recuperare i contenuti
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         contenente la lista di tutti i libri nella libreria
     * @throws IllegalArgumentException se username o namelib sono {@code null}
     *
     * @see #getBooksInLibrary(String, String)
     */
    public CompletableFuture<LibraryResponse> getBooksInLibraryAsync(String username, String namelib) {
        if (username == null || namelib == null) {
            throw new IllegalArgumentException("Username e nome libreria non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBooksInLibrary(username, namelib);
            } catch (Exception e) {
                System.err.println("Errore durante il recupero libri: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera tutti i libri contenuti in una libreria specifica in modo sincrono.
     *
     * @param username il nome utente proprietario della libreria
     * @param namelib il nome della libreria di cui recuperare i contenuti
     * @return {@link LibraryResponse} contenente la lista dei libri
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se username o namelib sono {@code null}
     */
    public LibraryResponse getBooksInLibrary(String username, String namelib) throws IOException {
        if (username == null || namelib == null) {
            throw new IllegalArgumentException("Username e nome libreria non possono essere null");
        }

        System.out.println("Recupero libri nella libreria '" + namelib + "' per: " + username);

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
                    System.out.println("Recuperati " + count + " libri dalla libreria '" + namelib + "'");
                } else {
                    System.out.println("Recupero libri fallito: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Aggiunge un libro a una libreria specifica in modo asincrono.
     * <p>
     * Inserisce il libro identificato dall'ISBN nella libreria specificata.
     * Se il libro è già presente nella libreria, l'operazione può essere
     * gestita come no-op o come errore a seconda della configurazione del server.
     * </p>
     *
     * @param username il nome utente proprietario della libreria
     * @param namelib il nome della libreria a cui aggiungere il libro
     * @param isbn il codice ISBN del libro da aggiungere
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         indicante l'esito dell'operazione di aggiunta
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @see #addBookToLibrary(String, String, String)
     */
    public CompletableFuture<LibraryResponse> addBookToLibraryAsync(String username, String namelib, String isbn) {
        if (username == null || namelib == null || isbn == null) {
            throw new IllegalArgumentException("Username, nome libreria e ISBN non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return addBookToLibrary(username, namelib, isbn);
            } catch (Exception e) {
                System.err.println("Errore durante l'aggiunta libro: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiunge un libro a una libreria specifica in modo sincrono.
     *
     * @param username il nome utente proprietario della libreria
     * @param namelib il nome della libreria a cui aggiungere il libro
     * @param isbn il codice ISBN del libro da aggiungere
     * @return {@link LibraryResponse} con l'esito dell'operazione
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     */
    public LibraryResponse addBookToLibrary(String username, String namelib, String isbn) throws IOException {
        if (username == null || namelib == null || isbn == null) {
            throw new IllegalArgumentException("Username, nome libreria e ISBN non possono essere null");
        }

        System.out.println("Aggiunta libro (ISBN: " + isbn + ") alla libreria '" + namelib + "'");

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
                    System.out.println("Libro aggiunto con successo alla libreria");
                } else {
                    System.out.println("Aggiunta libro fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Rimuove un libro da una libreria specifica in modo asincrono.
     * <p>
     * Elimina il libro identificato dall'ISBN dalla libreria specificata.
     * Se il libro non è presente nella libreria, l'operazione restituisce
     * un messaggio di errore appropriato.
     * </p>
     *
     * @param username il nome utente proprietario della libreria
     * @param namelib il nome della libreria da cui rimuovere il libro
     * @param isbn il codice ISBN del libro da rimuovere
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         indicante l'esito dell'operazione di rimozione
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @see #removeBookFromLibrary(String, String, String)
     */
    public CompletableFuture<LibraryResponse> removeBookFromLibraryAsync(String username, String namelib, String isbn) {
        if (username == null || namelib == null || isbn == null) {
            throw new IllegalArgumentException("Username, nome libreria e ISBN non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return removeBookFromLibrary(username, namelib, isbn);
            } catch (Exception e) {
                System.err.println("Errore durante la rimozione libro: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Rimuove un libro da una libreria specifica in modo sincrono.
     *
     * @param username il nome utente proprietario della libreria
     * @param namelib il nome della libreria da cui rimuovere il libro
     * @param isbn il codice ISBN del libro da rimuovere
     * @return {@link LibraryResponse} con l'esito dell'operazione
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     */
    public LibraryResponse removeBookFromLibrary(String username, String namelib, String isbn) throws IOException {
        if (username == null || namelib == null || isbn == null) {
            throw new IllegalArgumentException("Username, nome libreria e ISBN non possono essere null");
        }

        System.out.println("Rimozione libro (ISBN: " + isbn + ") dalla libreria '" + namelib + "'");

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
                    System.out.println("Libro rimosso con successo dalla libreria");
                } else {
                    System.out.println("Rimozione libro fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Elimina completamente una libreria e tutti i suoi contenuti in modo asincrono.
     * <p>
     * Rimuove definitivamente la libreria specificata dal sistema, inclusi
     * tutti i libri contenuti. L'operazione è irreversibile e deve essere
     * utilizzata con cautela. Tutti i riferimenti ai libri nella libreria
     * vengono eliminati, ma i libri stessi rimangono nel catalogo generale.
     * </p>
     *
     * @param username il nome utente proprietario della libreria da eliminare
     * @param namelib il nome della libreria da eliminare
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         indicante l'esito dell'operazione di eliminazione
     * @throws IllegalArgumentException se username o namelib sono {@code null}
     *
     * @apiNote Questa operazione elimina solo la libreria e i riferimenti ai libri,
     *          non i libri stessi dal catalogo generale del sistema.
     *
     * @see #deleteLibrary(String, String)
     */
    public CompletableFuture<LibraryResponse> deleteLibraryAsync(String username, String namelib) {
        if (username == null || namelib == null) {
            throw new IllegalArgumentException("Username e nome libreria non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return deleteLibrary(username, namelib);
            } catch (Exception e) {
                System.err.println("Errore durante l'eliminazione libreria: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina completamente una libreria e tutti i suoi contenuti in modo sincrono.
     *
     * @param username il nome utente proprietario della libreria da eliminare
     * @param namelib il nome della libreria da eliminare
     * @return {@link LibraryResponse} con l'esito dell'operazione
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se username o namelib sono {@code null}
     */
    public LibraryResponse deleteLibrary(String username, String namelib) throws IOException {
        if (username == null || namelib == null) {
            throw new IllegalArgumentException("Username e nome libreria non possono essere null");
        }

        System.out.println("Eliminazione libreria '" + namelib + "' per: " + username);

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/delete/" + username + "/" + namelib)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("Libreria eliminata con successo");
                } else {
                    System.out.println("Eliminazione libreria fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Verifica se un utente possiede un libro specifico in qualsiasi delle sue librerie.
     * <p>
     * Esegue una ricerca completa tra tutte le librerie dell'utente per determinare
     * se il libro identificato dall'ISBN è presente in almeno una di esse.
     * È utile per controlli di possesso prima di permettere recensioni o raccomandazioni.
     * </p>
     *
     * @param username il nome utente di cui verificare il possesso del libro
     * @param isbn il codice ISBN del libro da cercare
     * @return un {@link CompletableFuture} che si risolve con {@code true} se l'utente
     *         possiede il libro in almeno una libreria, {@code false} altrimenti
     * @throws IllegalArgumentException se username o isbn sono {@code null}
     *
     * @apiNote Questa operazione può richiedere multiple chiamate HTTP per controllare
     *          tutte le librerie dell'utente, quindi potrebbe avere latenza variabile
     *          a seconda del numero di librerie.
     */
    public CompletableFuture<Boolean> doesUserOwnBookAsync(String username, String isbn) {
        if (username == null || isbn == null) {
            throw new IllegalArgumentException("Username e ISBN non possono essere null");
        }

        System.out.println("Verifica possesso libro ISBN: " + isbn + " per utente: " + username);

        return getUserLibrariesAsync(username)
                .thenCompose(librariesResponse -> {
                    if (!librariesResponse.isSuccess() || librariesResponse.getLibraries() == null) {
                        System.out.println("Impossibile recuperare librerie utente");
                        return CompletableFuture.completedFuture(false);
                    }

                    List<String> libraries = librariesResponse.getLibraries();
                    System.out.println("Controllo possesso in " + libraries.size() + " librerie");

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

                                System.out.println(owns ? "Utente possiede il libro" : "Utente NON possiede il libro");
                                return owns;
                            });
                });
    }

    /**
     * Rinomina una libreria esistente in modo asincrono.
     * <p>
     * Modifica il nome di una libreria esistente mantenendo intatti tutti
     * i contenuti e i metadati. Il nuovo nome deve essere univoco tra
     * tutte le librerie dell'utente. L'operazione aggiorna tutti i
     * riferimenti interni alla libreria.
     * </p>
     *
     * @param username il nome utente proprietario della libreria da rinominare
     * @param oldName il nome attuale della libreria da modificare
     * @param newName il nuovo nome da assegnare alla libreria (deve essere univoco)
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         indicante l'esito dell'operazione di rinomina
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @see #renameLibrary(String, String, String)
     */
    public CompletableFuture<LibraryResponse> renameLibraryAsync(String username, String oldName, String newName) {
        if (username == null || oldName == null || newName == null) {
            throw new IllegalArgumentException("Username, nome vecchio e nome nuovo non possono essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return renameLibrary(username, oldName, newName);
            } catch (Exception e) {
                System.err.println("Errore durante la rinomina libreria: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Rinomina una libreria esistente in modo sincrono.
     *
     * @param username il nome utente proprietario della libreria da rinominare
     * @param oldName il nome attuale della libreria da modificare
     * @param newName il nuovo nome da assegnare alla libreria
     * @return {@link LibraryResponse} con l'esito dell'operazione
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     */
    public LibraryResponse renameLibrary(String username, String oldName, String newName) throws IOException {
        if (username == null || oldName == null || newName == null) {
            throw new IllegalArgumentException("Username, nome vecchio e nome nuovo non possono essere null");
        }

        System.out.println("Rinomina libreria da '" + oldName + "' a '" + newName + "'");

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/rename/" + username + "/" + oldName + "/" + newName)
                .put(RequestBody.create("", MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("Libreria rinominata con successo");
                } else {
                    System.out.println("Rinomina libreria fallita: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Recupera statistiche aggregate delle librerie di un utente in modo asincrono.
     * <p>
     * Fornisce un'analisi completa dell'utilizzo delle librerie dell'utente,
     * inclusi numero totale di librerie, numero totale di libri, statistiche
     * per libreria e metriche di engagement. È utile per dashboard personali
     * e analisi delle abitudini di lettura.
     * </p>
     *
     * @param username il nome utente di cui recuperare le statistiche
     * @return un {@link CompletableFuture} che si risolve con {@link LibraryResponse}
     *         contenente le statistiche complete nel campo stats
     * @throws IllegalArgumentException se username è {@code null}
     *
     * @apiNote Le statistiche includono tipicamente:
     *          <ul>
     *            <li>Numero totale di librerie create</li>
     *            <li>Numero totale di libri nelle librerie</li>
     *            <li>Dimensione media delle librerie</li>
     *            <li>Libreria più grande e più piccola</li>
     *            <li>Data di creazione della prima libreria</li>
     *          </ul>
     *
     * @see #getUserStats(String)
     */
    public CompletableFuture<LibraryResponse> getUserStatsAsync(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Lo username non può essere null");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return getUserStats(username);
            } catch (Exception e) {
                System.err.println("Errore durante il recupero statistiche: " + e.getMessage());
                return new LibraryResponse(false, "Errore di connessione: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera statistiche aggregate delle librerie di un utente in modo sincrono.
     *
     * @param username il nome utente di cui recuperare le statistiche
     * @return {@link LibraryResponse} contenente le statistiche complete
     * @throws IOException se si verifica un errore durante la richiesta HTTP
     * @throws IllegalArgumentException se username è {@code null}
     */
    public LibraryResponse getUserStats(String username) throws IOException {
        if (username == null) {
            throw new IllegalArgumentException("Lo username non può essere null");
        }

        System.out.println("Recupero statistiche per utente: " + username);

        Request request = new Request.Builder()
                .url(SERVER_BASE_URL + "/stats/" + username)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonResponse = response.body().string();
                LibraryResponse libraryResponse = objectMapper.readValue(jsonResponse, LibraryResponse.class);

                if (response.isSuccessful()) {
                    System.out.println("Statistiche recuperate per: " + username);
                } else {
                    System.out.println("Recupero statistiche fallito: " + libraryResponse.getMessage());
                }

                return libraryResponse;
            } else {
                throw new IOException("Risposta vuota dal server");
            }
        }
    }

    /**
     * Verifica se un libro specifico è presente in una libreria particolare.
     * <p>
     * Metodo di utilità che controlla la presenza di un libro identificato
     * dall'ISBN all'interno di una libreria specifica. È utilizzato
     * internamente da altri metodi per operazioni di verifica e validazione.
     * </p>
     *
     * @param username il nome utente proprietario della libreria
     * @param namelib il nome della libreria in cui cercare
     * @param isbn il codice ISBN del libro da cercare
     * @return un {@link CompletableFuture} che si risolve con {@code true} se il libro
     *         è presente nella libreria, {@code false} altrimenti
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     *
     * @implNote Questo metodo recupera tutti i libri della libreria e confronta
     *           gli ISBN per determinare la presenza. Per librerie molto grandi,
     *           potrebbe essere più efficiente un endpoint dedicato lato server.
     */
    public CompletableFuture<Boolean> isBookInLibraryAsync(String username, String namelib, String isbn) {
        if (username == null || namelib == null || isbn == null) {
            throw new IllegalArgumentException("Username, nome libreria e ISBN non possono essere null");
        }

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
     * Chiude il servizio e libera tutte le risorse HTTP associate.
     * <p>
     * Questo metodo deve essere chiamato quando il servizio non è più necessario
     * per garantire una corretta pulizia delle risorse e evitare memory leak.
     * Termina l'executor service del dispatcher e svuota il connection pool.
     * Dopo la chiamata a questo metodo, il servizio non deve più essere utilizzato.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ul>
     *   <li>Terminazione dell'executor service del dispatcher HTTP</li>
     *   <li>Svuotamento e chiusura del connection pool</li>
     *   <li>Rilascio di tutte le connessioni HTTP attive</li>
     * </ul>
     *
     * @apiNote È buona pratica chiamare questo metodo in un blocco finally
     *          o utilizzare try-with-resources se il servizio implementasse
     *          l'interfaccia AutoCloseable.
     */
    public void shutdown() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}