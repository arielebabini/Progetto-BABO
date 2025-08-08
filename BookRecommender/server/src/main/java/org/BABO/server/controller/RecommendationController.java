package org.BABO.server.controller;

import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.model.Book;
import org.BABO.shared.dto.RecommendationRequest;
import org.BABO.shared.dto.RecommendationResponse;
import org.BABO.server.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per gestire le operazioni sulle raccomandazioni
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Aggiunge una nuova raccomandazione
     * POST /api/recommendations/add
     */
    @PostMapping("/add")
    public ResponseEntity<RecommendationResponse> addRecommendation(@RequestBody RecommendationRequest request) {
        try {
            System.out.println("📝 Richiesta aggiunta raccomandazione: " + request.toString());

            // Validazione input
            if (!request.isValid()) {
                String errors = request.getValidationErrors();
                System.out.println("❌ Validazione fallita: " + errors);
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Dati non validi: " + errors));
            }

            // Tentativo aggiunta
            boolean success = recommendationService.addRecommendation(request);

            if (success) {
                System.out.println("✅ Raccomandazione aggiunta con successo");

                // Crea oggetto raccomandazione per la risposta
                BookRecommendation recommendation = new BookRecommendation(
                        request.getUsername(),
                        request.getTargetBookIsbn(),
                        request.getRecommendedBookIsbn(),
                        request.getReason()
                );

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new RecommendationResponse(true, "Raccomandazione aggiunta con successo", recommendation));
            } else {
                System.out.println("❌ Aggiunta raccomandazione fallita");
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new RecommendationResponse(false,
                                "Impossibile aggiungere la raccomandazione. Verifica di avere il libro nelle tue librerie e di non aver raggiunto il limite massimo."));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante l'aggiunta raccomandazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutte le raccomandazioni per un libro
     * GET /api/recommendations/book/{isbn}
     */
    @GetMapping("/book/{isbn}")
    public ResponseEntity<RecommendationResponse> getBookRecommendations(@PathVariable("isbn") String isbn) {
        try {
            System.out.println("📚 Richiesta raccomandazioni per libro ISBN: " + isbn);

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "ISBN è obbligatorio"));
            }

            List<BookRecommendation> recommendations = recommendationService.getRecommendationsForBook(isbn);
            List<Book> recommendedBooks = recommendationService.getRecommendedBooksDetails(isbn);

            System.out.println("✅ Recuperate " + recommendations.size() + " raccomandazioni con " +
                    recommendedBooks.size() + " dettagli libri");

            return ResponseEntity.ok(
                    new RecommendationResponse(true, "Raccomandazioni recuperate con successo",
                            recommendations, recommendedBooks)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero raccomandazioni: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Verifica se un utente può consigliare libri per un ISBN specifico
     * GET /api/recommendations/can-recommend/{username}/{isbn}
     */
    @GetMapping("/can-recommend/{username}/{isbn}")
    public ResponseEntity<RecommendationResponse> canUserRecommend(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("🔍 Verifica permessi raccomandazione per utente: " + username + ", ISBN: " + isbn);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Username è obbligatorio"));
            }

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "ISBN è obbligatorio"));
            }

            boolean canRecommend = recommendationService.canUserRecommend(username, isbn);
            int currentCount = recommendationService.getRecommendationsCountForUser(username, isbn);
            int maxRecommendations = recommendationService.getMaxRecommendationsPerBook();

            String message;
            if (canRecommend) {
                int remaining = maxRecommendations - currentCount;
                if (remaining > 0) {
                    message = "Puoi aggiungere ancora " + remaining + " raccomandazioni";
                } else {
                    message = "Hai raggiunto il limite massimo di " + maxRecommendations + " raccomandazioni";
                    canRecommend = false;
                }
            } else {
                message = "Non puoi consigliare libri per questo titolo. Assicurati di averlo nelle tue librerie.";
            }

            System.out.println("✅ Verifica completata: canRecommend=" + canRecommend + ", count=" + currentCount);

            return ResponseEntity.ok(
                    new RecommendationResponse(true, message, canRecommend, currentCount, maxRecommendations)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante la verifica permessi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera le raccomandazioni fatte da un utente per un libro specifico
     * GET /api/recommendations/user/{username}/book/{isbn}
     */
    @GetMapping("/user/{username}/book/{isbn}")
    public ResponseEntity<RecommendationResponse> getUserRecommendationsForBook(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("👤 Richiesta raccomandazioni utente: " + username + " per libro: " + isbn);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Username è obbligatorio"));
            }

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "ISBN è obbligatorio"));
            }

            List<BookRecommendation> recommendations = recommendationService.getUserRecommendationsForBook(username, isbn);
            System.out.println("✅ Recuperate " + recommendations.size() + " raccomandazioni per l'utente");

            return ResponseEntity.ok(
                    new RecommendationResponse(true, "Raccomandazioni utente recuperate con successo", recommendations)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero raccomandazioni utente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Rimuove una raccomandazione specifica
     * DELETE /api/recommendations/remove
     */
    @DeleteMapping("/remove")
    public ResponseEntity<RecommendationResponse> removeRecommendation(@RequestBody RecommendationRequest request) {
        try {
            System.out.println("🗑️ Richiesta rimozione raccomandazione: " + request.toString());

            // Validazione input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Username è obbligatorio"));
            }

            if (request.getTargetBookIsbn() == null || request.getTargetBookIsbn().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "ISBN del libro target è obbligatorio"));
            }

            if (request.getRecommendedBookIsbn() == null || request.getRecommendedBookIsbn().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "ISBN del libro consigliato è obbligatorio"));
            }

            // Tentativo rimozione
            boolean success = recommendationService.removeRecommendation(
                    request.getUsername(),
                    request.getTargetBookIsbn(),
                    request.getRecommendedBookIsbn()
            );

            if (success) {
                System.out.println("✅ Raccomandazione rimossa con successo");
                return ResponseEntity.ok(
                        new RecommendationResponse(true, "Raccomandazione rimossa con successo")
                );
            } else {
                System.out.println("❌ Rimozione raccomandazione fallita");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RecommendationResponse(false, "Raccomandazione non trovata"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante la rimozione raccomandazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Ottieni statistiche delle raccomandazioni (per admin)
     * GET /api/recommendations/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<RecommendationResponse> getRecommendationStats() {
        try {
            System.out.println("📊 Richiesta statistiche raccomandazioni");

            String stats = recommendationService.getRecommendationStats();
            System.out.println("✅ Statistiche generate");

            return ResponseEntity.ok(
                    new RecommendationResponse(true, stats)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il calcolo statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint di test per verificare che il servizio raccomandazioni funzioni
     * GET /api/recommendations/health
     */
    @GetMapping("/health")
    public ResponseEntity<RecommendationResponse> healthCheck() {
        boolean dbAvailable = recommendationService.isDatabaseAvailable();

        if (dbAvailable) {
            return ResponseEntity.ok(
                    new RecommendationResponse(true, "✅ Recommendation Service is running and database is connected!")
            );
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new RecommendationResponse(false, "❌ Recommendation Service is running but database is not available"));
        }
    }

    /**
     * Endpoint di debug per testare le operazioni sulle raccomandazioni
     * GET /api/recommendations/debug/{username}/{isbn}
     */
    @GetMapping("/debug/{username}/{isbn}")
    public ResponseEntity<String> debugRecommendations(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("🧪 Debug raccomandazioni per utente: " + username + ", ISBN: " + isbn);

            StringBuilder debug = new StringBuilder();
            debug.append("Debug Recommendation Information:\n");
            debug.append("Username: ").append(username).append("\n");
            debug.append("ISBN: ").append(isbn).append("\n");
            debug.append("Server Time: ").append(java.time.LocalDateTime.now()).append("\n\n");

            // Verifica permessi
            boolean canRecommend = recommendationService.canUserRecommend(username, isbn);
            debug.append("Can Recommend: ").append(canRecommend).append("\n");

            // Conta raccomandazioni esistenti
            int currentCount = recommendationService.getRecommendationsCountForUser(username, isbn);
            int maxCount = recommendationService.getMaxRecommendationsPerBook();
            debug.append("Current Recommendations: ").append(currentCount).append("/").append(maxCount).append("\n");

            // Lista raccomandazioni esistenti
            List<BookRecommendation> userRecs = recommendationService.getUserRecommendationsForBook(username, isbn);
            debug.append("User Recommendations for this book: ").append(userRecs.size()).append("\n");

            for (int i = 0; i < userRecs.size(); i++) {
                BookRecommendation rec = userRecs.get(i);
                debug.append("  ").append(i + 1).append(". ").append(rec.getRecommendedBookIsbn()).append("\n");
            }

            // Raccomandazioni di tutti per questo libro
            List<BookRecommendation> allRecs = recommendationService.getRecommendationsForBook(isbn);
            debug.append("\nAll Recommendations for this book: ").append(allRecs.size()).append("\n");

            for (int i = 0; i < Math.min(5, allRecs.size()); i++) {
                BookRecommendation rec = allRecs.get(i);
                debug.append("  ").append(i + 1).append(". ").append(rec.getRecommendedBookIsbn())
                        .append(" by ").append(rec.getShortUsername()).append("\n");
            }

            if (allRecs.size() > 5) {
                debug.append("  ... and ").append(allRecs.size() - 5).append(" more\n");
            }

            return ResponseEntity.ok(debug.toString());

        } catch (Exception e) {
            System.err.println("❌ Errore nel debug raccomandazioni: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Ottieni statistiche delle raccomandazioni per un utente
     * GET /api/recommendations/stats/{username}
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<RecommendationResponse> getUserRecommendationStats(@PathVariable("username") String username) {
        try {
            System.out.println("📊 Richiesta statistiche raccomandazioni per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Username è obbligatorio"));
            }

            int totalRecommendations = recommendationService.getUserRecommendationsCount(username);

            return ResponseEntity.ok(
                    new RecommendationResponse(true, "Raccomandazioni totali: " + totalRecommendations)
            );

        } catch (Exception e) {
            System.err.println("❌ Errore durante il recupero statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }
}