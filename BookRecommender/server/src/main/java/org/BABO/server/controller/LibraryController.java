package org.BABO.server.controller;

import org.BABO.shared.model.Book;
import org.BABO.shared.dto.*;
import org.BABO.server.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per gestire le operazioni sulle librerie personali
 * CORRETTO: Aggiunta specifica dei nomi nei @PathVariable
 */
@RestController
@RequestMapping("/api/library")
@CrossOrigin(origins = "*")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    /**
     * Crea una nuova libreria per un utente
     * POST /api/library/create
     */
    @PostMapping("/create")
    public ResponseEntity<LibraryResponse> createLibrary(@RequestBody CreateLibraryRequest request) {
        try {
            System.out.println("📚 Richiesta creazione libreria: " + request.getNamelib() + " per " + request.getUsername());

            // Validazione input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (request.getNamelib() == null || request.getNamelib().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            // Tentativo creazione
            boolean success = libraryService.createLibrary(request.getUsername(), request.getNamelib());

            if (success) {
                System.out.println("✅ Libreria creata con successo: " + request.getNamelib());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new LibraryResponse(true, "Libreria creata con successo"));
            } else {
                System.out.println("❌ Creazione libreria fallita per: " + request.getNamelib());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new LibraryResponse(false, "Libreria già esistente o errore nella creazione"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante la creazione libreria: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutte le librerie di un utente
     * GET /api/library/user/{username}
     * CORRETTO: Aggiunto nome esplicito al @PathVariable
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<LibraryResponse> getUserLibraries(@PathVariable("username") String username) {
        try {
            System.out.println("📖 Richiesta librerie per utente: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            List<String> libraries = libraryService.getUserLibraries(username);
            System.out.println("✅ Recuperate " + libraries.size() + " librerie per: " + username);

            return ResponseEntity.ok(
                    new LibraryResponse(true, "Librerie recuperate con successo", libraries)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero librerie: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutti i libri in una specifica libreria
     * GET /api/library/books/{username}/{namelib}
     * CORRETTO: Aggiunto nome esplicito ai @PathVariable
     */
    @GetMapping("/books/{username}/{namelib}")
    public ResponseEntity<LibraryResponse> getBooksInLibrary(
            @PathVariable("username") String username,
            @PathVariable("namelib") String namelib) {
        try {
            System.out.println("📖 Richiesta libri nella libreria '" + namelib + "' per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (namelib == null || namelib.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            List<Book> books = libraryService.getBooksInLibrary(username, namelib);
            System.out.println("✅ Recuperati " + books.size() + " libri dalla libreria '" + namelib + "'");

            return ResponseEntity.ok(
                    new LibraryResponse(true, "Libri recuperati con successo", books, true)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero libri: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Aggiunge un libro a una libreria
     * POST /api/library/add-book
     */
    @PostMapping("/add-book")
    public ResponseEntity<LibraryResponse> addBookToLibrary(@RequestBody AddBookToLibraryRequest request) {
        try {
            System.out.println("➕ Richiesta aggiunta libro alla libreria: " + request.getNamelib());

            // Validazione input
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

            // Tentativo aggiunta
            boolean success = libraryService.addBookToLibrary(
                    request.getUsername(),
                    request.getNamelib(),
                    request.getIsbn()
            );

            if (success) {
                System.out.println("✅ Libro aggiunto con successo alla libreria");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libro aggiunto con successo alla libreria")
                );
            } else {
                System.out.println("❌ Aggiunta libro fallita");
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new LibraryResponse(false, "Libro già presente nella libreria o errore nell'aggiunta"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante l'aggiunta libro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Rimuove un libro da una libreria
     * DELETE /api/library/remove-book
     */
    @DeleteMapping("/remove-book")
    public ResponseEntity<LibraryResponse> removeBookFromLibrary(@RequestBody RemoveBookFromLibraryRequest request) {
        try {
            System.out.println("➖ Richiesta rimozione libro dalla libreria: " + request.getNamelib());

            // Validazione input
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

            // Tentativo rimozione
            boolean success = libraryService.removeBookFromLibrary(
                    request.getUsername(),
                    request.getNamelib(),
                    request.getIsbn()
            );

            if (success) {
                System.out.println("✅ Libro rimosso con successo dalla libreria");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libro rimosso con successo dalla libreria")
                );
            } else {
                System.out.println("❌ Rimozione libro fallita");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new LibraryResponse(false, "Libro non trovato nella libreria specificata"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante la rimozione libro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Elimina una libreria intera
     * DELETE /api/library/delete/{username}/{namelib}
     * CORRETTO: Aggiunto nome esplicito ai @PathVariable
     */
    @DeleteMapping("/delete/{username}/{namelib}")
    public ResponseEntity<LibraryResponse> deleteLibrary(
            @PathVariable("username") String username,
            @PathVariable("namelib") String namelib) {
        try {
            System.out.println("🗑️ Richiesta eliminazione libreria '" + namelib + "' per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            if (namelib == null || namelib.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Nome libreria è obbligatorio"));
            }

            // Tentativo eliminazione
            boolean success = libraryService.deleteLibrary(username, namelib);

            if (success) {
                System.out.println("✅ Libreria eliminata con successo");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libreria eliminata con successo")
                );
            } else {
                System.out.println("❌ Eliminazione libreria fallita");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new LibraryResponse(false, "Libreria non trovata"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante l'eliminazione libreria: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Rinomina una libreria
     * PUT /api/library/rename/{username}/{oldName}/{newName}
     * CORRETTO: Aggiunto nome esplicito ai @PathVariable
     */
    @PutMapping("/rename/{username}/{oldName}/{newName}")
    public ResponseEntity<LibraryResponse> renameLibrary(
            @PathVariable("username") String username,
            @PathVariable("oldName") String oldName,
            @PathVariable("newName") String newName) {
        try {
            System.out.println("✏️ Richiesta rinomina libreria da '" + oldName + "' a '" + newName + "' per: " + username);

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

            // Tentativo rinomina
            boolean success = libraryService.renameLibrary(username, oldName, newName);

            if (success) {
                System.out.println("✅ Libreria rinominata con successo");
                return ResponseEntity.ok(
                        new LibraryResponse(true, "Libreria rinominata con successo")
                );
            } else {
                System.out.println("❌ Rinomina libreria fallita");
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new LibraryResponse(false, "Libreria non trovata o nuovo nome già esistente"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante la rinomina libreria: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint di test per verificare che il servizio librerie funzioni
     * GET /api/library/health
     */
    @GetMapping("/health")
    public ResponseEntity<LibraryResponse> healthCheck() {
        boolean dbAvailable = libraryService.isDatabaseAvailable();

        if (dbAvailable) {
            return ResponseEntity.ok(
                    new LibraryResponse(true, "✅ Library Service is running and database is connected!")
            );
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new LibraryResponse(false, "❌ Library Service is running but database is not available"));
        }
    }

    /**
     * Endpoint di debug per testare le operazioni sulle librerie
     * GET /api/library/debug/{username}
     * CORRETTO: Aggiunto nome esplicito al @PathVariable
     */
    @GetMapping("/debug/{username}")
    public ResponseEntity<String> debugLibraries(@PathVariable("username") String username) {
        try {
            System.out.println("🧪 Debug librerie per utente: " + username);

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
            System.err.println("❌ Errore nel debug librerie: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Verifica se un utente possiede un libro
     * GET /api/library/user/{username}/owns/{isbn}
     */
    @GetMapping("/user/{username}/owns/{isbn}")
    public ResponseEntity<LibraryResponse> checkBookOwnership(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("🔍 Controllo possesso libro per utente: " + username + " e ISBN: " + isbn);

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
            System.err.println("❌ Errore controllo possesso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Ottieni statistiche dell'utente per le librerie
     * GET /api/library/stats/{username}
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<LibraryResponse> getUserStats(@PathVariable("username") String username) {
        try {
            System.out.println("📊 Richiesta statistiche librerie per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LibraryResponse(false, "Username è obbligatorio"));
            }

            int totalBooks = libraryService.getUserTotalBooksCount(username);

            return ResponseEntity.ok(
                    new LibraryResponse(true, "Libri totali: " + totalBooks)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LibraryResponse(false, "Errore interno del server"));
        }
    }
}