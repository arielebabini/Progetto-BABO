package org.BABO.server.controller;

import org.BABO.shared.model.BookRating;
import org.BABO.shared.dto.RatingRequest;
import org.BABO.shared.dto.RatingResponse;
import org.BABO.server.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per gestire le operazioni sulle valutazioni dei libri
 */
@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    /**
     * Aggiunge o aggiorna una valutazione per un libro
     * POST /api/ratings/add
     */
    @PostMapping("/add")
    public ResponseEntity<RatingResponse> addOrUpdateRating(@RequestBody RatingRequest request) {
        try {
            System.out.println("⭐ Richiesta aggiunta/aggiornamento valutazione: " + request);

            // Validazione input
            if (!request.isValid()) {
                String errors = request.getValidationErrors();
                System.out.println("❌ Validazione fallita: " + errors);
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "Errori di validazione: " + errors));
            }

            // Crea oggetto BookRating dalla richiesta
            BookRating rating = new BookRating(
                    request.getUsername(),
                    request.getIsbn(),
                    request.getStyle(),
                    request.getContent(),
                    request.getPleasantness(),
                    request.getOriginality(),
                    request.getEdition(),
                    request.getCleanReview()
            );

            // Salva la valutazione
            boolean success = ratingService.addOrUpdateRating(rating);

            if (success) {
                // Recupera la valutazione salvata per restituirla
                BookRating savedRating = ratingService.getRatingByUserAndBook(
                        request.getUsername(), request.getIsbn()
                );

                System.out.println("✅ Valutazione salvata con successo");
                return ResponseEntity.ok(
                        new RatingResponse(true, "Valutazione salvata con successo", savedRating)
                );
            } else {
                System.out.println("❌ Salvataggio valutazione fallito");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RatingResponse(false, "Errore durante il salvataggio della valutazione"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante l'aggiunta valutazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera la valutazione di un utente per un libro specifico
     * GET /api/ratings/user/{username}/book/{isbn}
     */
    @GetMapping("/user/{username}/book/{isbn}")
    public ResponseEntity<RatingResponse> getUserRatingForBook(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("🔍 Richiesta valutazione per utente: " + username + " e ISBN: " + isbn);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "Username è obbligatorio"));
            }

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "ISBN è obbligatorio"));
            }

            BookRating rating = ratingService.getRatingByUserAndBook(username, isbn);

            if (rating != null) {
                System.out.println("✅ Valutazione trovata: " + rating.getDisplayRating());
                return ResponseEntity.ok(
                        new RatingResponse(true, "Valutazione trovata", rating)
                );
            } else {
                System.out.println("📝 Nessuna valutazione trovata");
                return ResponseEntity.ok(
                        new RatingResponse(true, "Nessuna valutazione trovata per questo utente e libro")
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero valutazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutte le valutazioni di un utente
     * GET /api/ratings/user/{username}
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<RatingResponse> getUserRatings(@PathVariable("username") String username) {
        try {
            System.out.println("👤 Richiesta tutte le valutazioni dell'utente: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "Username è obbligatorio"));
            }

            List<BookRating> ratings = ratingService.getUserRatings(username);

            System.out.println("✅ Recuperate " + ratings.size() + " valutazioni dell'utente");
            return ResponseEntity.ok(
                    new RatingResponse(true, "Valutazioni recuperate con successo", ratings)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero valutazioni utente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutte le valutazioni per un libro specifico
     * GET /api/ratings/book/{isbn}
     */
    @GetMapping("/book/{isbn}")
    public ResponseEntity<RatingResponse> getBookRatings(@PathVariable("isbn") String isbn) {
        try {
            System.out.println("📊 Richiesta tutte le valutazioni per ISBN: " + isbn);

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "ISBN è obbligatorio"));
            }

            List<BookRating> ratings = ratingService.getRatingsForBook(isbn);
            Double averageRating = ratingService.getAverageRatingForBook(isbn);

            System.out.println("✅ Recuperate " + ratings.size() + " valutazioni per il libro");
            return ResponseEntity.ok(
                    new RatingResponse(true, "Valutazioni recuperate con successo", ratings, averageRating)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero valutazioni libro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera statistiche complete per un libro
     * GET /api/ratings/book/{isbn}/statistics
     */
    @GetMapping("/book/{isbn}/statistics")
    public ResponseEntity<RatingResponse> getBookRatingStatistics(@PathVariable("isbn") String isbn) {
        try {
            System.out.println("📈 Richiesta statistiche complete per ISBN: " + isbn);

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "ISBN è obbligatorio"));
            }

            RatingResponse statistics = ratingService.getBookRatingStatistics(isbn);

            System.out.println("✅ Statistiche calcolate per il libro");
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            System.err.println("❌ Errore durante il calcolo statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Ottiene solo la media delle valutazioni per un libro
     * GET /api/ratings/book/{isbn}/average
     */
    @GetMapping("/book/{isbn}/average")
    public ResponseEntity<RatingResponse> getBookAverageRating(@PathVariable("isbn") String isbn) {
        try {
            System.out.println("📊 Richiesta media valutazioni per ISBN: " + isbn);

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "ISBN è obbligatorio"));
            }

            Double averageRating = ratingService.getAverageRatingForBook(isbn);

            if (averageRating != null) {
                RatingResponse response = new RatingResponse(true, "Media calcolata con successo");
                response.setAverageRating(averageRating);

                System.out.println("✅ Media calcolata: " + averageRating);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(
                        new RatingResponse(true, "Nessuna valutazione trovata per questo libro")
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante il calcolo media: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Elimina una valutazione
     * DELETE /api/ratings/user/{username}/book/{isbn}
     */
    @DeleteMapping("/user/{username}/book/{isbn}")
    public ResponseEntity<RatingResponse> deleteRating(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("🗑️ Richiesta eliminazione valutazione per utente: " + username + " e ISBN: " + isbn);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "Username è obbligatorio"));
            }

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "ISBN è obbligatorio"));
            }

            boolean success = ratingService.deleteRating(username, isbn);

            if (success) {
                System.out.println("✅ Valutazione eliminata con successo");
                return ResponseEntity.ok(
                        new RatingResponse(true, "Valutazione eliminata con successo")
                );
            } else {
                System.out.println("❌ Nessuna valutazione trovata da eliminare");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RatingResponse(false, "Nessuna valutazione trovata per questo utente e libro"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante l'eliminazione valutazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint di test per verificare che il servizio valutazioni funzioni
     * GET /api/ratings/health
     */
    @GetMapping("/health")
    public ResponseEntity<RatingResponse> healthCheck() {
        try {
            boolean dbAvailable = ratingService.isDatabaseAvailable();
            int totalRatings = ratingService.getTotalRatingsCount();

            if (dbAvailable) {
                return ResponseEntity.ok(
                        new RatingResponse(true, "✅ Rating Service is running! Totale valutazioni: " + totalRatings)
                );
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new RatingResponse(false, "❌ Rating Service is running but database is not available"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "❌ Errore nel servizio valutazioni: " + e.getMessage()));
        }
    }

    /**
     * Endpoint di debug per testare le operazioni sulle valutazioni
     * GET /api/ratings/debug/{username}
     */
    @GetMapping("/debug/{username}")
    public ResponseEntity<String> debugRatings(@PathVariable("username") String username) {
        try {
            System.out.println("🧪 Debug valutazioni per utente: " + username);

            List<BookRating> userRatings = ratingService.getUserRatings(username);
            StringBuilder debug = new StringBuilder();

            debug.append("Debug Rating Information:\n");
            debug.append("Username: ").append(username).append("\n");
            debug.append("Total User Ratings: ").append(userRatings.size()).append("\n");
            debug.append("Total System Ratings: ").append(ratingService.getTotalRatingsCount()).append("\n");
            debug.append("Server Time: ").append(java.time.LocalDateTime.now()).append("\n\n");

            if (!userRatings.isEmpty()) {
                debug.append("User Ratings:\n");
                for (int i = 0; i < userRatings.size(); i++) {
                    BookRating rating = userRatings.get(i);
                    debug.append(String.format("  %d. ISBN: %s - %s (%.1f/5)\n",
                            i + 1, rating.getIsbn(), rating.getQualityDescription(), rating.getAverage()));
                    debug.append(String.format("     Stile: %d, Contenuto: %d, Piacevolezza: %d, Originalità: %d, Edizione: %d\n",
                            rating.getStyle(), rating.getContent(), rating.getPleasantness(),
                            rating.getOriginality(), rating.getEdition()));
                    if (rating.getReview() != null && !rating.getReview().isEmpty()) {
                        debug.append("     Recensione: ").append(rating.getReview().substring(0, Math.min(100, rating.getReview().length()))).append("...\n");
                    }
                    debug.append("\n");
                }
            } else {
                debug.append("No ratings found for this user.\n");
            }

            // Aggiungi info sui libri più valutati
            debug.append("\nMost Rated Books:\n");
            List<String> topBooks = ratingService.getMostRatedBooks();
            for (int i = 0; i < Math.min(5, topBooks.size()); i++) {
                debug.append(String.format("  %d. %s\n", i + 1, topBooks.get(i)));
            }

            return ResponseEntity.ok(debug.toString());

        } catch (Exception e) {
            System.err.println("❌ Errore nel debug valutazioni: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint per testare la struttura della tabella assessment
     * GET /api/ratings/test-database
     */
    @GetMapping("/test-database")
    public ResponseEntity<String> testDatabase() {
        try {
            String testResult = ratingService.testAssessmentTable();
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error testing database: " + e.getMessage());
        }
    }

    /**
     * Recupera i libri più valutati del sistema
     * GET /api/ratings/top-rated
     */
    @GetMapping("/top-rated")
    public ResponseEntity<RatingResponse> getTopRatedBooks() {
        try {
            System.out.println("🏆 Richiesta libri più valutati");

            List<String> topBooks = ratingService.getMostRatedBooks();

            RatingResponse response = new RatingResponse(true, "Libri più valutati recuperati");
            // Nota: Qui potresti voler espandere per restituire oggetti Book completi invece di stringhe

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero libri più valutati: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint per validare una richiesta di valutazione senza salvarla
     * POST /api/ratings/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<RatingResponse> validateRating(@RequestBody RatingRequest request) {
        try {
            System.out.println("✅ Validazione richiesta valutazione: " + request);

            if (request.isValid()) {
                double average = request.calculateAverage();
                return ResponseEntity.ok(
                        new RatingResponse(true, "Valutazione valida. Media calcolata: " + average)
                );
            } else {
                String errors = request.getValidationErrors();
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "Errori di validazione: " + errors));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante la validazione: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }
}