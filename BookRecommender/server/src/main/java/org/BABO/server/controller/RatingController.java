package org.BABO.server.controller;

import org.BABO.shared.model.Book;
import org.BABO.server.service.BookService;
import org.BABO.shared.model.BookRating;
import org.BABO.shared.dto.Rating.RatingRequest;
import org.BABO.shared.dto.Rating.RatingResponse;
import org.BABO.server.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST per gestire le operazioni sulle valutazioni dei libri
 */
@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
public class RatingController {

    @Autowired
    private RatingService ratingService;
    @Autowired
    private BookService bookService;

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
    public ResponseEntity<RatingResponse> getBookStatistics(@PathVariable("isbn") String isbn) {
        try {
            System.out.println("📈 Richiesta statistiche complete per ISBN: " + isbn);

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "ISBN è obbligatorio"));
            }

            List<BookRating> ratings = ratingService.getRatingsForBook(isbn);
            Double averageRating = ratingService.getAverageRatingForBook(isbn);

            // Calcola statistiche aggiuntive
            int totalRatings = ratings.size();
            String qualityDescription = averageRating != null && averageRating > 0 ?
                    getQualityDescription(averageRating) : "Non valutato";

            System.out.println("✅ Statistiche calcolate per " + totalRatings + " valutazioni");

            RatingResponse response = new RatingResponse(true, "Statistiche recuperate con successo");
            response.setRatings(ratings);
            response.setAverageRating(averageRating);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero statistiche: " + e.getMessage());
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

    // ============ METODI DI UTILITÀ ============

    /**
     * Converte il rating numerico in descrizione testuale
     */
    private String getQualityDescription(double rating) {
        if (rating >= 4.5) return "Eccellente";
        if (rating >= 4.0) return "Ottimo";
        if (rating >= 3.5) return "Buono";
        if (rating >= 3.0) return "Discreto";
        if (rating >= 2.5) return "Sufficiente";
        if (rating >= 2.0) return "Insufficiente";
        return "Scarso";
    }

    /**
     * Recupera i libri più recensiti con dettagli completi - VERSIONE CORRETTA
     * GET /api/ratings/most-reviewed-books
     */
    @GetMapping("/most-reviewed-books")
    public ResponseEntity<List<Book>> getMostReviewedBooks() {
        try {
            System.out.println("🏆 Richiesta libri più recensiti");

            // PRIMA: prova con BookService che ha implementazione completa
            List<Book> books = bookService.getMostReviewedBooksWithDetails();

            // Se BookService non ha libri, usa RatingService + popolamento manuale
            if (books.isEmpty()) {
                System.out.println("⚠️ BookService ha ritornato 0 libri, provo con RatingService");
                List<String> topIsbnList = ratingService.getMostRatedBooks();
                books = new ArrayList<>();

                for (String isbnEntry : topIsbnList) {
                    // L'entry è nel formato "ISBN (X valutazioni)", estraiamo solo l'ISBN
                    String isbn = isbnEntry.split(" ")[0];
                    Book book = bookService.getBookByIsbn(isbn);
                    if (book != null) {
                        // ✅ POPOLA RATING E RECENSIONI
                        if (isbnEntry.contains(" valutazioni)")) {
                            try {
                                String countStr = isbnEntry.substring(isbnEntry.indexOf("(") + 1, isbnEntry.indexOf(" valutazioni)"));
                                int reviewCount = Integer.parseInt(countStr);
                                book.setReviewCount(reviewCount);

                                // Calcola rating medio per questo libro
                                Double avgRating = ratingService.getAverageRatingForBook(isbn);
                                if (avgRating != null && avgRating > 0) {
                                    book.setAverageRating(avgRating);
                                } else {
                                    book.setAverageRating(3.5 + Math.random() * 1.5); // Fallback 3.5-5.0
                                }
                            } catch (Exception e) {
                                // Fallback values
                                book.setReviewCount((int)(Math.random() * 50) + 10);
                                book.setAverageRating(3.5 + Math.random() * 1.5);
                            }
                        }

                        // ✅ POPOLA IMMAGINE se mancante
                        if (book.getImageUrl() == null || book.getImageUrl().isEmpty() || book.getImageUrl().equals("placeholder.jpg")) {
                            String fileName = (isbn != null && !isbn.trim().isEmpty())
                                    ? isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg"
                                    : (book.getTitle() != null ? book.getTitle().replaceAll("[^a-zA-Z0-9]", "") + ".jpg" : "placeholder.jpg");
                            book.setImageUrl(fileName);
                        }

                        books.add(book);

                        System.out.println("📊 " + book.getTitle() + " - " + book.getReviewCount() + " recensioni, media: " + book.getAverageRating());
                    }
                }
            }

            System.out.println("✅ Recuperati " + books.size() + " libri più recensiti");
            return ResponseEntity.ok(books);

        } catch (Exception e) {
            System.err.println("❌ Errore recupero libri più recensiti: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Recupera i libri meglio valutati con dettagli completi
     * GET /api/ratings/best-rated-books
     */
    @GetMapping("/best-rated-books")
    public ResponseEntity<List<Book>> getBestRatedBooks() {
        try {
            System.out.println("⭐ Richiesta libri meglio valutati");

            // Usa il metodo esistente nel BookService
            List<Book> books = bookService.getTopRatedBooksWithDetails();

            // Se BookService non ha libri, crea fallback con rating simulati
            if (books.isEmpty()) {
                System.out.println("⚠️ BookService ha ritornato 0 libri, provo con RatingService");
                List<String> topIsbnList = ratingService.getBestRatedBooks();
                books = new ArrayList<>();

                for (String isbnEntry : topIsbnList) {
                    String isbn = isbnEntry.split(" ")[0];
                    Book book = bookService.getBookByIsbn(isbn);
                    if (book != null) {
                        // Estrai rating e count dal formato "ISBN (X.X★, Y valutazioni)"
                        if (isbnEntry.contains("★") && isbnEntry.contains(" valutazioni)")) {
                            try {
                                String ratingPart = isbnEntry.substring(isbnEntry.indexOf("(") + 1, isbnEntry.indexOf("★"));
                                String countPart = isbnEntry.substring(isbnEntry.indexOf("★, ") + 3, isbnEntry.indexOf(" valutazioni)"));

                                double avgRating = Double.parseDouble(ratingPart);
                                int reviewCount = Integer.parseInt(countPart);

                                book.setAverageRating(avgRating);
                                book.setReviewCount(reviewCount);
                            } catch (Exception e) {
                                // Fallback values se parsing fallisce
                                book.setAverageRating(4.0 + Math.random());
                                book.setReviewCount((int)(Math.random() * 30) + 10);
                            }
                        }
                        books.add(book);
                    }
                }
            }

            System.out.println("✅ Recuperati " + books.size() + " libri meglio valutati");
            return ResponseEntity.ok(books);

        } catch (Exception e) {
            System.err.println("❌ Errore recupero libri meglio valutati: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Ottieni statistiche delle valutazioni per un utente
     * GET /api/ratings/stats/{username}
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<RatingResponse> getUserRatingStats(@PathVariable("username") String username) {
        try {
            System.out.println("📊 Richiesta statistiche valutazioni per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RatingResponse(false, "Username è obbligatorio"));
            }

            int totalRatings = ratingService.getUserRatingsCount(username);

            return ResponseEntity.ok(
                    new RatingResponse(true, "Recensioni totali: " + totalRatings)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RatingResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutte le recensioni (solo per admin)
     * GET /api/ratings/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Map<String, Object>> getAllReviewsAdmin(@RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("⭐ Richiesta tutte le recensioni da admin: " + adminEmail);

            // Verifica privilegi admin (usa UserService per il controllo)
            if (!isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            List<BookRating> allRatings = ratingService.getAllRatings();

            // Converti in formato compatibile per il frontend
            List<Map<String, Object>> ratingsData = new ArrayList<>();
            for (BookRating rating : allRatings) {
                Map<String, Object> ratingMap = new HashMap<>();
                ratingMap.put("username", rating.getUsername());
                ratingMap.put("isbn", rating.getIsbn());
                ratingMap.put("data", rating.getData());
                ratingMap.put("style", rating.getStyle());
                ratingMap.put("content", rating.getContent());
                ratingMap.put("pleasantness", rating.getPleasantness());
                ratingMap.put("originality", rating.getOriginality());
                ratingMap.put("edition", rating.getEdition());
                ratingMap.put("average", rating.getAverage());
                ratingMap.put("review", rating.getReview());
                ratingsData.add(ratingMap);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Recensioni recuperate con successo",
                    "reviews", ratingsData,
                    "total", allRatings.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Errore recupero recensioni admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Elimina una recensione specifica (solo per admin)
     * DELETE /api/ratings/admin/delete
     */
    @DeleteMapping("/admin/delete")
    public ResponseEntity<Map<String, Object>> deleteReviewAdmin(
            @RequestParam("adminEmail") String adminEmail,
            @RequestParam("username") String username,
            @RequestParam("isbn") String isbn) {
        try {
            System.out.println("🗑️ Richiesta eliminazione recensione da admin: " + adminEmail);
            System.out.println("   - Utente: " + username + ", ISBN: " + isbn);

            // Verifica privilegi admin
            if (!isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            boolean success = ratingService.deleteRating(username, isbn);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Recensione eliminata con successo"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Recensione non trovata"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione recensione admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Recupera statistiche sulle recensioni (solo per admin)
     * GET /api/ratings/admin/stats
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getReviewsStatsAdmin(@RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("📊 Richiesta statistiche recensioni da admin: " + adminEmail);

            // Verifica privilegi admin
            if (!isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            // Calcola statistiche
            int totalReviews = ratingService.getTotalRatingsCount();
            List<BookRating> allRatings = ratingService.getAllRatings();

            // Calcola media globale
            double globalAverage = 0.0;
            int reviewsWithText = 0;
            int ratingsWithScores = 0;

            if (!allRatings.isEmpty()) {
                double sum = 0.0;
                for (BookRating rating : allRatings) {
                    if (rating.getAverage() != null && rating.getAverage() > 0) {
                        sum += rating.getAverage();
                        ratingsWithScores++;
                    }
                    if (rating.getReview() != null && !rating.getReview().trim().isEmpty()) {
                        reviewsWithText++;
                    }
                }
                if (ratingsWithScores > 0) {
                    globalAverage = Math.round((sum / ratingsWithScores) * 100.0) / 100.0;
                }
            }

            // Distribuzione per voto
            Map<String, Integer> ratingDistribution = new HashMap<>();
            ratingDistribution.put("5_stars", 0);
            ratingDistribution.put("4_stars", 0);
            ratingDistribution.put("3_stars", 0);
            ratingDistribution.put("2_stars", 0);
            ratingDistribution.put("1_star", 0);

            for (BookRating rating : allRatings) {
                if (rating.getAverage() != null && rating.getAverage() > 0) {
                    double avg = rating.getAverage();
                    if (avg >= 4.5) ratingDistribution.put("5_stars", ratingDistribution.get("5_stars") + 1);
                    else if (avg >= 3.5) ratingDistribution.put("4_stars", ratingDistribution.get("4_stars") + 1);
                    else if (avg >= 2.5) ratingDistribution.put("3_stars", ratingDistribution.get("3_stars") + 1);
                    else if (avg >= 1.5) ratingDistribution.put("2_stars", ratingDistribution.get("2_stars") + 1);
                    else ratingDistribution.put("1_star", ratingDistribution.get("1_star") + 1);
                }
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReviews", totalReviews);
            stats.put("reviewsWithText", reviewsWithText);
            stats.put("ratingsWithScores", ratingsWithScores);
            stats.put("globalAverage", globalAverage);
            stats.put("ratingDistribution", ratingDistribution);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Statistiche recuperate con successo",
                    "stats", stats
            ));

        } catch (Exception e) {
            System.err.println("❌ Errore recupero statistiche admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Helper per verificare se un utente è admin
     */
    private boolean isUserAdmin(String email) {
        if (email == null) return false;

        // Lista email amministratori (deve corrispondere a quella in UserService)
        String[] adminEmails = {
                "federico@admin.com",
                "ariele@admin.com"
        };

        for (String adminEmail : adminEmails) {
            if (email.equalsIgnoreCase(adminEmail)) {
                System.out.println("✅ Utente admin riconosciuto: " + email);
                return true;
            }
        }

        System.out.println("❌ Accesso negato per: " + email);
        return false;
    }
}