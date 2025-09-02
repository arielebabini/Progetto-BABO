package org.BABO.server.controller;

import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.model.Book;
import org.BABO.shared.dto.Recommendation.RecommendationRequest;
import org.BABO.shared.dto.Recommendation.RecommendationResponse;
import org.BABO.server.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per la gestione del sistema di raccomandazioni peer-to-peer nell'applicazione BABO.
 * <p>
 * Questo controller gestisce il sistema di raccomandazioni sociale che permette agli utenti di suggerire
 * libri ad altri membri della community basandosi sulla loro esperienza di lettura. Il sistema implementa
 * logiche di validazione per garantire raccomandazioni di qualità, controlli di proprietà per verificare
 * che solo chi ha letto effettivamente un libro possa consigliarne altri, e meccanismi anti-spam
 * per mantenere l'integrità delle raccomandazioni.
 * </p>
 *
 * <h3>Funzionalità principali del sistema:</h3>
 * <ul>
 *   <li><strong>Raccomandazioni Peer-to-Peer:</strong> Sistema sociale di consigli tra lettori</li>
 *   <li><strong>Validazione Proprietà:</strong> Solo chi possiede un libro può consigliarne altri</li>
 *   <li><strong>Controlli Anti-Spam:</strong> Limiti quantitativi per prevenire abusi</li>
 *   <li><strong>Discovery Avanzato:</strong> Algoritmi per suggerimenti personalizzati e pertinenti</li>
 *   <li><strong>Community Engagement:</strong> Promozione interazione e condivisione esperienze</li>
 *   <li><strong>Analytics Dettagliate:</strong> Metriche su efficacia e popolarità raccomandazioni</li>
 * </ul>
 *
 * <h3>Logica di Business e Validazione:</h3>
 * <ul>
 *   <li><strong>Ownership Requirement:</strong> L'utente deve possedere il libro target nelle sue librerie</li>
 *   <li><strong>Quantitative Limits:</strong> Limite massimo raccomandazioni per libro/utente</li>
 *   <li><strong>Quality Assurance:</strong> Validazione motivazioni e pertinenza suggerimenti</li>
 *   <li><strong>Duplicate Prevention:</strong> Controlli per evitare raccomandazioni duplicate</li>
 * </ul>
 *
 * <h3>Architettura e Pattern:</h3>
 * <ul>
 *   <li><strong>Service Layer:</strong> Delegazione logica business a {@link RecommendationService}</li>
 *   <li><strong>DTO Pattern:</strong> Request/Response objects per comunicazione type-safe</li>
 *   <li><strong>Validation Chain:</strong> Controlli multi-livello per qualità raccomandazioni</li>
 *   <li><strong>Community Pattern:</strong> Design focalizzato su interazione sociale</li>
 * </ul>
 *
 * @author BABO Development Team
 * @version 1.8.0
 * @since 1.0.0
 * @see RecommendationService
 * @see BookRecommendation
 * @see RecommendationRequest
 * @see RecommendationResponse
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Aggiunge una nuova raccomandazione al sistema.
     * <p>
     * Endpoint per creazione raccomandazioni con validazione completa della proprietà
     * del libro e controlli anti-spam per mantenere qualità del sistema.
     * </p>
     *
     * @param request {@link RecommendationRequest} con dati raccomandazione
     * @return {@link ResponseEntity} di {@link RecommendationResponse} con conferma
     * @since 1.0.0
     */
    @PostMapping("/add")
    public ResponseEntity<RecommendationResponse> addRecommendation(@RequestBody RecommendationRequest request) {
        try {
            System.out.println("Richiesta aggiunta raccomandazione: " + request.toString());

            if (!request.isValid()) {
                String errors = request.getValidationErrors();
                System.out.println("Validazione fallita: " + errors);
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Dati non validi: " + errors));
            }

            boolean success = recommendationService.addRecommendation(request);

            if (success) {
                System.out.println("Raccomandazione aggiunta con successo");

                BookRecommendation recommendation = new BookRecommendation(
                        request.getUsername(),
                        request.getTargetBookIsbn(),
                        request.getRecommendedBookIsbn(),
                        request.getReason()
                );

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new RecommendationResponse(true, "Raccomandazione aggiunta con successo", recommendation));
            } else {
                System.out.println("Aggiunta raccomandazione fallita");
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new RecommendationResponse(false,
                                "Impossibile aggiungere la raccomandazione. Verifica di avere il libro nelle tue librerie e di non aver raggiunto il limite massimo."));
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta raccomandazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera tutte le raccomandazioni per un libro specifico.
     * <p>
     * Endpoint per discovery di libri correlati con dettagli completi
     * delle raccomandazioni e metadati dei libri consigliati.
     * </p>
     *
     * @param isbn identificatore del libro target
     * @return {@link ResponseEntity} con raccomandazioni e dettagli libri
     * @since 1.0.0
     */
    @GetMapping("/book/{isbn}")
    public ResponseEntity<RecommendationResponse> getBookRecommendations(@PathVariable("isbn") String isbn) {
        try {
            System.out.println("Richiesta raccomandazioni per libro ISBN: " + isbn);

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "ISBN è obbligatorio"));
            }

            List<BookRecommendation> recommendations = recommendationService.getRecommendationsForBook(isbn);
            List<Book> recommendedBooks = recommendationService.getRecommendedBooksDetails(isbn);

            System.out.println("Recuperate " + recommendations.size() + " raccomandazioni con " +
                    recommendedBooks.size() + " dettagli libri");

            return ResponseEntity.ok(
                    new RecommendationResponse(true, "Raccomandazioni recuperate con successo",
                            recommendations, recommendedBooks)
            );

        } catch (Exception e) {
            System.err.println("Errore durante il recupero raccomandazioni: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Verifica permessi utente per raccomandare libri.
     * <p>
     * Endpoint utility per controlli client-side sui permessi di raccomandazione
     * e informazioni sui limiti quantitativi.
     * </p>
     *
     * @param username utente di cui verificare i permessi
     * @param isbn libro target per raccomandazioni
     * @return {@link ResponseEntity} con flag permessi e informazioni limiti
     * @since 1.2.0
     */
    @GetMapping("/can-recommend/{username}/{isbn}")
    public ResponseEntity<RecommendationResponse> canUserRecommend(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("Verifica permessi raccomandazione per utente: " + username + ", ISBN: " + isbn);

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

            System.out.println("Verifica completata: canRecommend=" + canRecommend + ", count=" + currentCount);

            return ResponseEntity.ok(
                    new RecommendationResponse(true, message, canRecommend, currentCount, maxRecommendations)
            );

        } catch (Exception e) {
            System.err.println("Errore durante la verifica permessi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera raccomandazioni specifiche di un utente per un libro.
     *
     * @param username utente di cui recuperare le raccomandazioni
     * @param isbn libro target
     * @return {@link ResponseEntity} con raccomandazioni dell'utente
     * @since 1.1.0
     */
    @GetMapping("/user/{username}/book/{isbn}")
    public ResponseEntity<RecommendationResponse> getUserRecommendationsForBook(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("Richiesta raccomandazioni utente: " + username + " per libro: " + isbn);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Username è obbligatorio"));
            }

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "ISBN è obbligatorio"));
            }

            List<BookRecommendation> recommendations = recommendationService.getUserRecommendationsForBook(username, isbn);
            System.out.println("Recuperate " + recommendations.size() + " raccomandazioni per l'utente");

            return ResponseEntity.ok(
                    new RecommendationResponse(true, "Raccomandazioni utente recuperate con successo", recommendations)
            );

        } catch (Exception e) {
            System.err.println("Errore durante il recupero raccomandazioni utente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Rimuove una raccomandazione specifica dal sistema.
     * <p>
     * Endpoint per eliminazione raccomandazioni con controlli di autorizzazione
     * per garantire che solo l'autore possa rimuovere le proprie raccomandazioni.
     * </p>
     *
     * @param request {@link RecommendationRequest} con identificatori raccomandazione
     * @return {@link ResponseEntity} con conferma rimozione
     * @since 1.3.0
     */
    @DeleteMapping("/remove")
    public ResponseEntity<RecommendationResponse> removeRecommendation(@RequestBody RecommendationRequest request) {
        try {
            System.out.println("Richiesta rimozione raccomandazione: " + request.toString());

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

            boolean success = recommendationService.removeRecommendation(
                    request.getUsername(),
                    request.getTargetBookIsbn(),
                    request.getRecommendedBookIsbn()
            );

            if (success) {
                System.out.println("Raccomandazione rimossa con successo");
                return ResponseEntity.ok(
                        new RecommendationResponse(true, "Raccomandazione rimossa con successo")
                );
            } else {
                System.out.println("Rimozione raccomandazione fallita");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RecommendationResponse(false, "Raccomandazione non trovata"));
            }

        } catch (Exception e) {
            System.err.println("Errore durante la rimozione raccomandazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera statistiche aggregate del sistema raccomandazioni.
     *
     * @return {@link ResponseEntity} con metriche sistema
     * @since 1.4.0
     */
    @GetMapping("/stats")
    public ResponseEntity<RecommendationResponse> getRecommendationStats() {
        try {
            System.out.println("Richiesta statistiche raccomandazioni");

            String stats = recommendationService.getRecommendationStats();
            System.out.println("Statistiche generate");

            return ResponseEntity.ok(
                    new RecommendationResponse(true, stats)
            );

        } catch (Exception e) {
            System.err.println("Errore durante il calcolo statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Recupera statistiche raccomandazioni per utente specifico.
     *
     * @param username utente di cui calcolare le statistiche
     * @return {@link ResponseEntity} con metriche utente
     * @since 1.5.0
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<RecommendationResponse> getUserRecommendationStats(@PathVariable("username") String username) {
        try {
            System.out.println("Richiesta statistiche raccomandazioni per: " + username);

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(false, "Username è obbligatorio"));
            }

            int totalRecommendations = recommendationService.getUserRecommendationsCount(username);

            return ResponseEntity.ok(
                    new RecommendationResponse(true, "Raccomandazioni totali: " + totalRecommendations)
            );

        } catch (Exception e) {
            System.err.println("Errore durante il recupero statistiche: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Health check per monitoring servizio raccomandazioni.
     *
     * @return {@link ResponseEntity} con stato servizio
     * @since 1.0.0
     */
    @GetMapping("/health")
    public ResponseEntity<RecommendationResponse> healthCheck() {
        boolean dbAvailable = recommendationService.isDatabaseAvailable();

        if (dbAvailable) {
            return ResponseEntity.ok(
                    new RecommendationResponse(true, "Recommendation Service is running and database is connected!")
            );
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new RecommendationResponse(false, "Recommendation Service is running but database is not available"));
        }
    }

    /**
     * Endpoint di debugging per analisi dettagliata raccomandazioni.
     * <p>
     * Strumento diagnostico per troubleshooting con panoramica completa
     * permessi utente e stato raccomandazioni per libro specifico.
     * </p>
     *
     * @param username utente da analizzare
     * @param isbn libro target da analizzare
     * @return {@link ResponseEntity} con report dettagliato
     * @apiNote Destinato solo per development e debugging
     * @since 1.6.0
     */
    @GetMapping("/debug/{username}/{isbn}")
    public ResponseEntity<String> debugRecommendations(
            @PathVariable("username") String username,
            @PathVariable("isbn") String isbn) {
        try {
            System.out.println("Debug raccomandazioni per utente: " + username + ", ISBN: " + isbn);

            StringBuilder debug = new StringBuilder();
            debug.append("Debug Recommendation Information:\n");
            debug.append("Username: ").append(username).append("\n");
            debug.append("ISBN: ").append(isbn).append("\n");
            debug.append("Server Time: ").append(java.time.LocalDateTime.now()).append("\n\n");

            boolean canRecommend = recommendationService.canUserRecommend(username, isbn);
            debug.append("Can Recommend: ").append(canRecommend).append("\n");

            int currentCount = recommendationService.getRecommendationsCountForUser(username, isbn);
            int maxCount = recommendationService.getMaxRecommendationsPerBook();
            debug.append("Current Recommendations: ").append(currentCount).append("/").append(maxCount).append("\n");

            List<BookRecommendation> userRecs = recommendationService.getUserRecommendationsForBook(username, isbn);
            debug.append("User Recommendations for this book: ").append(userRecs.size()).append("\n");

            for (int i = 0; i < userRecs.size(); i++) {
                BookRecommendation rec = userRecs.get(i);
                debug.append("  ").append(i + 1).append(". ").append(rec.getRecommendedBookIsbn()).append("\n");
            }

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
            System.err.println("Errore nel debug raccomandazioni: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}