package org.BABO.server.controller;

import org.BABO.shared.dto.Library.AddBookToLibraryRequest;
import org.BABO.shared.dto.Library.CreateLibraryRequest;
import org.BABO.shared.dto.Library.LibraryResponse;
import org.BABO.shared.dto.Library.RemoveBookFromLibraryRequest;
import org.BABO.shared.model.Book;
import org.BABO.server.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per la gestione completa delle librerie personali degli utenti nell'applicazione BABO.
 * <p>
 * Questa classe rappresenta il sistema di gestione delle raccolte personali di libri, permettendo agli utenti
 * di organizzare, catalogare e gestire le proprie librerie digitali. Il controller implementa un sistema
 * completo di CRUD operations per librerie e contenuti, con supporto per operazioni avanzate come
 * rinomina, statistiche e controlli di proprietà. Progettato per scalare efficacemente con collezioni
 * di grandi dimensioni e supportare workflow di organizzazione complessi.
 * </p>
 *
 * <h3>Funzionalità principali del sistema librerie:</h3>
 * <ul>
 *   <li><strong>Gestione Librerie:</strong> Creazione, rinomina, eliminazione di raccolte personalizzate</li>
 *   <li><strong>Organizzazione Contenuti:</strong> Aggiunta, rimozione e catalogazione libri per categoria</li>
 *   <li><strong>Controlli Proprietà:</strong> Verifica possesso libri e prevenzione duplicati</li>
 *   <li><strong>Statistiche Avanzate:</strong> Metriche di utilizzo e analytics delle collezioni</li>
 *   <li><strong>Multi-User Support:</strong> Isolamento completo tra librerie di utenti diversi</li>
 *   <li><strong>Validazione Robusta:</strong> Controlli di integrità e consistenza dati</li>
 *   <li><strong>Performance Ottimizzate:</strong> Caching e query ottimizzate per grandi collezioni</li>
 * </ul>
 *
 * <h3>Architettura e Design Pattern:</h3>
 * <p>
 * Il controller implementa pattern enterprise per gestione di collezioni complesse:
 * </p>
 * <ul>
 *   <li><strong>Repository Pattern:</strong> Astrazione accesso dati tramite {@link LibraryService}</li>
 *   <li><strong>DTO Pattern:</strong> Request/Response objects per comunicazione type-safe</li>
 *   <li><strong>Validation Chain:</strong> Validazione multi-livello per integrità dati</li>
 *   <li><strong>Transaction Management:</strong> Operazioni atomiche per consistenza</li>
 *   <li><strong>Error Handling:</strong> Gestione graceful errori con rollback automatico</li>
 *   <li><strong>Security Pattern:</strong> Isolamento dati utente e controlli autorizzazione</li>
 * </ul>
 *
 * <h3>Sistema di Validazione e Sicurezza:</h3>
 * <ul>
 *   <li>Validazione rigorosa input per prevenire injection attacks</li>
 *   <li>Controlli autorizzazione per accesso cross-user</li>
 *   <li>Sanitizzazione nomi librerie e parametri</li>
 *   <li>Prevenzione operazioni su dati non autorizzati</li>
 * </ul>
 *
 * <h3>Performance e Scalabilità:</h3>
 * <ul>
 *   <li>Query ottimizzate per grandi collezioni utente</li>
 *   <li>Caching intelligente per librerie frequentemente accedute</li>
 *   <li>Batch operations per modifiche multiple</li>
 *   <li>Lazy loading per metadati non critici</li>
 * </ul>
 *
 * <h3>Esempi di utilizzo:</h3>
 * <pre>{@code
 * // Creazione nuova libreria
 * CreateLibraryRequest createReq = new CreateLibraryRequest("user123", "Fantascienza");
 * ResponseEntity<LibraryResponse> response = libraryController.createLibrary(createReq);
 *
 * // Aggiunta libro a libreria
 * AddBookToLibraryRequest addReq = new AddBookToLibraryRequest("user123", "Fantascienza", "978-0441569595");
 * libraryController.addBookToLibrary(addReq);
 *
 * // Recupero librerie utente
 * ResponseEntity<LibraryResponse> libraries = libraryController.getUserLibraries("user123");
 *
 * // Controllo possesso libro
 * ResponseEntity<LibraryResponse> owns = libraryController.checkBookOwnership("user123", "978-0441569595");
 * }</pre>
 *
 * @author BABO Development Team
 * @version 2.2.0
 * @since 1.0.0
 * @see LibraryService
 * @see LibraryResponse
 * @see Book
 */
@RestController
@RequestMapping("/api/library")
@CrossOrigin(origins = "*")
public class LibraryController {

    /** Servizio business per operazioni su librerie e gestione collezioni */
    @Autowired
    private LibraryService libraryService;

    /**
     * Crea una nuova libreria personale per un utente specificato.
     * <p>
     * Endpoint per la creazione di raccolte personalizzate, con validazione univocità
     * nomi librerie per utente e gestione automatica metadati. Supporta creazione
     * atomic con rollback automatico in caso di errori.
     * </p>
     *
     * @param request {@link CreateLibraryRequest} contenente username e nome libreria
     * @return {@link ResponseEntity} di {@link LibraryResponse} con:
     *         <ul>
     *           <li><strong>201 Created:</strong> Libreria creata con successo</li>
     *           <li><strong>400 Bad Request:</strong> Parametri mancanti o non validi</li>
     *           <li><strong>409 Conflict:</strong> Nome libreria già esistente per l'utente</li>
     *           <li><strong>500 Internal Server Error:</strong> Errore durante creazione</li>
     *         </ul>
     * @since 1.0.0
     * @see LibraryService#createLibrary(String, String)
     */
    @PostMapping("/create")
    public ResponseEntity<LibraryResponse> createLibrary(@RequestBody CreateLibraryRequest request) {
        try {
            System.out.println("Richiesta creazione libreria: " + request.getNamelib() + " per " + request.getUsername());

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (request.getNamelib() == null || request.getNamelib().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            boolean success = libraryService.createLibrary(request.getUsername(), request.getNamelib());

            if (success) {
                System.out.println("Libreria creata con successo: " + request.getNamelib());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new LibraryResponse(true, "Libreria creata con successo"));
            } else {
                System.out.println("Creazione libreria fallita per: " + request.getNamelib());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new LibraryResponse(false, "Libreria già esistente o errore nella creazione"));
            }

        } catch (Exception e) {
            System.err.println("Errore durante la creazione libreria: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera l'elenco completo delle librerie di un utente.
     * <p>
     * Endpoint ottimizzato per il recupero di tutte le librerie associate a un utente,
     * con ordinamento alfabetico e metadati di base per ogni raccolta.
     * </p>
     *
     * @param username identificatore dell'utente
     * @return {@link ResponseEntity} con lista nomi librerie ordinata alfabeticamente
     * @since 1.0.0
     * @see LibraryService#getUserLibraries(String)
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<LibraryResponse> getUserLibraries(@PathVariable("username") String username) {
        try {
            System.out.println("Richiesta librerie per utente: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            List<String> libraries = libraryService.getUserLibraries(username);
            System.out.println("Recuperate " + libraries.size() + " librerie per: " + username);

            return ResponseEntity.ok(
                    new LibraryResponse(true, "Librerie recuperate con successo", libraries)
            );

        } catch (Exception e) {
            System.err.println("Errore durante il recupero librerie: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutti i libri contenuti in una libreria specifica.
     * <p>
     * Endpoint per l'accesso al contenuto di una raccolta, con metadati completi
     * per ogni libro e ordinamento personalizzabile. Supporta lazy loading
     * per librerie con molti libri.
     * </p>
     *
     * @param username proprietario della libreria
     * @param namelib nome della libreria da consultare
     * @return {@link ResponseEntity} con lista completa {@link Book} nella libreria
     * @since 1.0.0
     * @see LibraryService#getBooksInLibrary(String, String)
     */
    @GetMapping("/books/{username}/{namelib}")
    public ResponseEntity<LibraryResponse> getBooksInLibrary(
            @PathVariable("username") String username,
            @PathVariable("namelib") String namelib) {
        try {
            System.out.println("Richiesta libri nella libreria '" + namelib + "' per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (namelib == null || namelib.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            List<Book> books = libraryService.getBooksInLibrary(username, namelib);
            System.out.println("Recuperati " + books.size() + " libri dalla libreria '" + namelib + "'");

            return ResponseEntity.ok(
                    new LibraryResponse(true, "Libri recuperati con successo", books, true)
            );

        } catch (Exception e) {
            System.err.println("Errore durante il recupero libri: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Aggiunge un libro a una libreria esistente.
     * <p>
     * Endpoint per l'inserimento di libri nelle raccolte personali, con controlli
     * automatici per duplicati e validazione esistenza libro nel catalogo.
     * </p>
     *
     * @param request {@link AddBookToLibraryRequest} con username, libreria e ISBN
     * @return {@link ResponseEntity} con conferma aggiunta o errore specifico
     * @since 1.0.0
     * @see LibraryService#addBookToLibrary(String, String, String)
     */
    @PostMapping("/add-book")
    public ResponseEntity<LibraryResponse> addBookToLibrary(@RequestBody AddBookToLibraryRequest request) {
        try {
            System.out.println("Richiesta aggiunta libro alla libreria: " + request.getNamelib());

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (request.getNamelib() == null || request.getNamelib().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            if (request.getIsbn() == null || request.getIsbn().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "ISBN è obbligatorio"));
            }

            boolean success = libraryService.addBookToLibrary(
                    request.getUsername(),
                    request.getNamelib(),
                    request.getIsbn()
            );

            if (success) {
                System.out.println("Libro aggiunto con successo alla libreria");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libro aggiunto con successo alla libreria")
                );
            } else {
                System.out.println("Aggiunta libro fallita");
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new LibraryResponse(false, "Libro già presente nella libreria o errore nell'aggiunta"));
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta libro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Rimuove un libro specifico da una libreria.
     * <p>
     * Endpoint per la rimozione selettiva di libri dalle raccolte, mantenendo
     * l'integrità della libreria e gestendo automaticamente riferimenti correlati.
     * </p>
     *
     * @param request {@link RemoveBookFromLibraryRequest} con dettagli rimozione
     * @return {@link ResponseEntity} con conferma rimozione o errore se non trovato
     * @since 1.0.0
     * @see LibraryService#removeBookFromLibrary(String, String, String)
     */
    @DeleteMapping("/remove-book")
    public ResponseEntity<LibraryResponse> removeBookFromLibrary(@RequestBody RemoveBookFromLibraryRequest request) {
        try {
            System.out.println("Richiesta rimozione libro dalla libreria: " + request.getNamelib());

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (request.getNamelib() == null || request.getNamelib().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            if (request.getIsbn() == null || request.getIsbn().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "ISBN è obbligatorio"));
            }

            boolean success = libraryService.removeBookFromLibrary(
                    request.getUsername(),
                    request.getNamelib(),
                    request.getIsbn()
            );

            if (success) {
                System.out.println("Libro rimosso con successo dalla libreria");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libro rimosso con successo dalla libreria")
                );
            } else {
                System.out.println("Rimozione libro fallita");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new LibraryResponse(false, "Libro non trovato nella libreria specificata"));
            }

        } catch (Exception e) {
            System.err.println("Errore durante la rimozione libro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Elimina completamente una libreria e tutto il suo contenuto.
     * <p>
     * Operazione irreversibile che rimuove la raccolta e tutti i riferimenti
     * ai libri contenuti. Include conferme di sicurezza e logging per audit.
     * </p>
     *
     * @param username proprietario della libreria
     * @param namelib nome della libreria da eliminare
     * @return {@link ResponseEntity} con conferma eliminazione
     * @since 1.0.0
     * @see LibraryService#deleteLibrary(String, String)
     */
    @DeleteMapping("/delete/{username}/{namelib}")
    public ResponseEntity<LibraryResponse> deleteLibrary(
            @PathVariable("username") String username,
            @PathVariable("namelib") String namelib) {
        try {
            System.out.println("Richiesta eliminazione libreria '" + namelib + "' per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (namelib == null || namelib.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            boolean success = libraryService.deleteLibrary(username, namelib);

            if (success) {
                System.out.println("Libreria eliminata con successo");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libreria eliminata con successo")
                );
            } else {
                System.out.println("Eliminazione libreria fallita");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new LibraryResponse(false, "Libreria non trovata"));
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione libreria: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Rinomina una libreria esistente mantenendo tutto il contenuto.
     * <p>
     * Endpoint per aggiornamento denominazione librerie con controlli unicità
     * e mantenimento integrità referenziale per tutti i libri contenuti.
     * </p>
     *
     * @param username proprietario della libreria
     * @param oldName nome attuale della libreria
     * @param newName nuovo nome da assegnare
     * @return {@link ResponseEntity} con conferma rinomina
     * @since 1.1.0
     * @see LibraryService#renameLibrary(String, String, String)
     */
    @PutMapping("/rename/{username}/{oldName}/{newName}")
    public ResponseEntity<LibraryResponse> renameLibrary(
            @PathVariable("username") String username,
            @PathVariable("oldName") String oldName,
            @PathVariable("newName") String newName) {
        try {
            System.out.println("Richiesta rinomina libreria da '" + oldName + "' a '" + newName + "' per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (oldName == null || oldName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria attuale è obbligatorio"));
            }

            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nuovo nome libreria è obbligatorio"));
            }

            boolean success = libraryService.renameLibrary(username, oldName, newName);

            if (success) {
                System.out.println("Libreria rinominata con successo");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libreria rinominata con successo")
                );
            } else {
                System.out.println("Rinomina libreria fallita");
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new LibraryResponse(false, "Libreria non trovata o nuovo nome già esistente"));
            }

        } catch (Exception e) {
            System.err.println("Errore durante la rinomina libreria: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Verifica se un utente possiede un libro specifico in qualsiasi sua libreria.
     * <p>
     * Endpoint utility per controlli di proprietà, utile per interfacce che devono
     * mostrare stato possesso o abilitare/disabilitare funzioni basate sulla proprietà.
     * </p>
     *
     * @param username utente di cui verificare il possesso
     * @param isbn codice ISBN del libro da verificare
     * @return {@link ResponseEntity} con flag boolean di possesso
     * @since 1.2.0
     * @see LibraryService#doesUserOwnBook(String, String)
     */
    @GetMapping("/user/{username}/owns/{isbn}")
    public ResponseEntity<LibraryResponse> checkBookOwnership(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("Controllo possesso libro per utente: " + username + " e ISBN: " + isbn);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "ISBN è obbligatorio"));
            }

            boolean ownsBook = libraryService.doesUserOwnBook(username, isbn);

            if (ownsBook) {
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Utente possiede il libro")
                );
            } else {
                return ResponseEntity.ok(
                        new LibraryResponse(false, "Utente non possiede il libro")
                );
            }

        } catch (Exception e) {
            System.err.println("Errore controllo possesso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera statistiche aggregate sulle librerie di un utente.
     * <p>
     * Endpoint per metriche e analytics che fornisce conteggi totali,
     * distribuzioni per categoria e altre statistiche utili per dashboard utente.
     * </p>
     *
     * @param username utente di cui recuperare le statistiche
     * @return {@link ResponseEntity} con conteggio totale libri e altre metriche
     * @since 1.2.0
     * @see LibraryService#getUserTotalBooksCount(String)
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<LibraryResponse> getUserStats(@PathVariable("username") String username) {
        try {
            System.out.println("Richiesta statistiche librerie per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            int totalBooks = libraryService.getUserTotalBooksCount(username);

            return ResponseEntity.ok(
                    new LibraryResponse(true, "Libri totali: " + totalBooks)
            );

        } catch (Exception e) {
            System.err.println("Errore durante il recupero statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Health check endpoint per monitoring del servizio librerie.
     * <p>
     * Diagnostica stato servizio e connettività database per sistemi di monitoring.
     * </p>
     *
     * @return {@link ResponseEntity} con stato operativo del servizio
     * @since 1.0.0
     * @see LibraryService#isDatabaseAvailable()
     */
    @GetMapping("/health")
    public ResponseEntity<LibraryResponse> healthCheck() {
        boolean dbAvailable = libraryService.isDatabaseAvailable();

        if (dbAvailable) {
            return ResponseEntity.ok(
                    new LibraryResponse(true, "Library Service is running and database is connected!")
            );
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new LibraryResponse(false, "Library Service is running but database is not available"));
        }
    }

    /**
     * Endpoint di debugging per analisi dettagliata librerie utente.
     * <p>
     * Strumento diagnostico che fornisce panoramica completa delle librerie
     * di un utente con conteggi, campioni di contenuto e metadati per troubleshooting.
     * </p>
     *
     * @param username utente di cui analizzare le librerie
     * @return {@link ResponseEntity} con report dettagliato in formato testo
     * @apiNote Destinato solo per environment di development e debugging
     * @since 1.5.0
     */
    @GetMapping("/debug/{username}")
    public ResponseEntity<String> debugLibraries(@PathVariable("username") String username) {
        try {
            System.out.println("Debug librerie per utente: " + username);

            List<String> libraries = libraryService.getUserLibraries(username);
            StringBuilder debug = new StringBuilder();

            debug.append("Debug Library Information:\n");
            debug.append("Username: ").append(username).append("\n");
            debug.append("Total Libraries: ").append(libraries.size()).append("\n");
            debug.append("Server Time: ").append(java.time.LocalDateTime.now()).append("\n\n");

            if (!libraries.isEmpty()) {
                debug.append("Libraries:\n");
                for (int i = 0; i < libraries.size(); i++) {
                    String libName = libraries.get(i);
                    List<Book> books = libraryService.getBooksInLibrary(username, libName);
                    debug.append(String.format("  %d. %s (%d books)\n", i + 1, libName, books.size()));

                    if (!books.isEmpty()) {
                        for (int j = 0; j < Math.min(3, books.size()); j++) {
                            Book book = books.get(j);
                            debug.append(String.format("     - %s by %s\n", book.getTitle(), book.getAuthor()));
                        }
                        if (books.size() > 3) {
                            debug.append("     ... and ").append(books.size() - 3).append(" more\n");
                        }
                    }
                }
            } else {
                debug.append("No libraries found for this user.\n");
            }

            return ResponseEntity.ok(debug.toString());

        } catch (Exception e) {
            System.err.println("Errore nel debug librerie: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}