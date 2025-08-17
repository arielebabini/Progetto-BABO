package org.BABO.server.controller;

import org.BABO.server.service.BookService;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.User;
import org.BABO.shared.dto.Authentication.AuthRequest;
import org.BABO.shared.dto.Authentication.AuthResponse;
import org.BABO.shared.dto.Authentication.RegisterRequest;
import org.BABO.server.service.UserService;
import org.BABO.shared.dto.AdminResponse;
import org.BABO.server.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST per gestire l'autenticazione e la gestione utenti
 * AGGIORNATO per compatibilità String/Long
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private RatingService ratingService;

    /**
     * Endpoint per il login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            System.out.println("🔐 Richiesta login per: " + request.getEmail());

            // Validazione input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Email è obbligatoria"));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Password è obbligatoria"));
            }

            // Tentativo autenticazione
            User user = userService.authenticateUser(request.getEmail(), request.getPassword());

            if (user != null) {
                System.out.println("✅ Login riuscito per: " + user.getDisplayName());
                return ResponseEntity.ok(
                        new AuthResponse(true, "Login effettuato con successo", user)
                );
            } else {
                System.out.println("❌ Login fallito per: " + request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(false, "Email o password non corretti"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante il login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint per la registrazione
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            System.out.println("📝 Richiesta registrazione per: " + request.getEmail());

            // Validazione input
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Nome è obbligatorio"));
            }

            if (request.getSurname() == null || request.getSurname().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Cognome è obbligatorio"));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Email è obbligatoria"));
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Username è obbligatorio"));
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Password deve essere almeno 6 caratteri"));
            }

            // Controlla se l'utente esiste già
            if (userService.userExists(request.getEmail(), request.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new AuthResponse(false, "Email o username già in uso"));
            }

            // Tentativo registrazione
            User newUser = userService.registerUser(
                    request.getName(),
                    request.getSurname(),
                    request.getCf(),
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword()
            );

            if (newUser != null) {
                System.out.println("✅ Registrazione completata per: " + newUser.getDisplayName());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new AuthResponse(true, "Registrazione completata con successo", newUser));
            } else {
                System.out.println("❌ Registrazione fallita per: " + request.getEmail());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AuthResponse(false, "Errore durante la registrazione"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante la registrazione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint per reset password (senza vecchia password)
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            System.out.println("🔄 Richiesta reset password per email: " + request.getEmail());

            // Validazione
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Email è obbligatoria"));
            }

            if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "La nuova password deve essere almeno 8 caratteri"));
            }

            boolean success = userService.resetPasswordByEmail(
                    request.getEmail(),
                    request.getNewPassword()
            );

            if (success) {
                return ResponseEntity.ok(
                        new AuthResponse(true, "Password reimpostata con successo")
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new AuthResponse(false, "Email non trovata nel sistema"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    public static class ResetPasswordRequest {
        private String email;
        private String newPassword;

        public ResetPasswordRequest() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * Endpoint per verificare se un email/username è disponibile
     * GET /api/auth/check-availability?email={email}&username={username}
     */
    @GetMapping("/check-availability")
    public ResponseEntity<AuthResponse> checkAvailability(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username) {
        try {
            if (email == null && username == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Specificare almeno email o username"));
            }

            boolean exists = userService.userExists(
                    email != null ? email : "",
                    username != null ? username : ""
            );

            if (exists) {
                return ResponseEntity.ok(
                        new AuthResponse(false, "Email o username già in uso")
                );
            } else {
                return ResponseEntity.ok(
                        new AuthResponse(true, "Email e username disponibili")
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Errore controllo disponibilità: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint per recuperare il profilo dell'utente
     * GET /api/auth/profile/{userId}
     * AGGIORNATO per compatibilità String/Long
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<AuthResponse> getUserProfile(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId); // Ora accetta String

            if (user != null) {
                return ResponseEntity.ok(
                        new AuthResponse(true, "Profilo recuperato", user)
                );
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Errore recupero profilo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint per aggiornare il profilo
     * PUT /api/auth/profile/{userId}
     * AGGIORNATO per compatibilità String/Long
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<AuthResponse> updateProfile(
            @PathVariable String userId,
            @RequestBody User updatedUser) {
        try {
            User user = userService.updateUserProfile(
                    userId,  // Ora String
                    updatedUser.getName(),
                    updatedUser.getSurname(),
                    updatedUser.getCf()
            );

            if (user != null) {
                return ResponseEntity.ok(
                        new AuthResponse(true, "Profilo aggiornato con successo", user)
                );
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento profilo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint per cambiare password - VERSIONE ALTERNATIVA
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password/{userId}")
    public ResponseEntity<AuthResponse> changePassword(
            @PathVariable("userId") String userId,
            @RequestBody ChangePasswordRequest request) {
        try {
            System.out.println("🔐 Richiesta cambio password per utente ID: " + userId);

            if (request.getOldPassword() == null || request.getNewPassword() == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Password vecchia e nuova sono obbligatorie"));
            }

            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "La nuova password deve essere almeno 6 caratteri"));
            }

            boolean success = userService.changePassword(
                    userId,
                    request.getOldPassword(),
                    request.getNewPassword()
            );

            if (success) {
                return ResponseEntity.ok(
                        new AuthResponse(true, "Password cambiata con successo")
                );
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(false, "Password attuale non corretta"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore cambio password: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Endpoint per logout (placeholder per future implementazioni con sessioni/JWT)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        // Per ora è solo un placeholder, in futuro si potrebbe invalidare il token JWT
        return ResponseEntity.ok(
                new AuthResponse(true, "Logout effettuato con successo")
        );
    }

    /**
     * Recupera tutti gli utenti (solo per admin)
     * GET /api/auth/admin/users
     */
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers(@RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("👑 Richiesta lista utenti da: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            List<User> users = userService.getAllUsers();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Utenti recuperati con successo",
                    "users", users,
                    "total", users.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Errore recupero utenti admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Elimina un utente (solo per admin)
     * DELETE /api/auth/admin/users/{userId}
     */
    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable("userId") String userId,
            @RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("🗑️ Richiesta eliminazione utente " + userId + " da: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            boolean success = userService.deleteUser(userId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Utente eliminato con successo"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Utente non trovato"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione utente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Endpoint di test per verificare che il servizio di autenticazione funzioni
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<AuthResponse> healthCheck() {
        boolean dbAvailable = userService.isDatabaseAvailable();

        if (dbAvailable) {
            return ResponseEntity.ok(
                    new AuthResponse(true, "✅ Auth Service is running and database is connected!")
            );
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new AuthResponse(false, "❌ Auth Service is running but database is not available"));
        }
    }

    /**
     * DTO per la richiesta di cambio password - VERSIONE AGGIORNATA
     */
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public ChangePasswordRequest() {}

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * Endpoint per aggiornare l'email dell'utente
     * PUT /api/auth/update-email/{userId}
     */
    @PutMapping("/update-email/{userId}")
    public ResponseEntity<AuthResponse> updateEmail(
            @PathVariable("userId") String userId,
            @RequestBody Map<String, String> request) {
        try {
            String newEmail = request.get("email");

            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "La nuova email è obbligatoria"));
            }

            newEmail = newEmail.trim().toLowerCase();

            // Valida formato email
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Formato email non valido"));
            }

            // Verifica che l'email non sia già in uso da un altro utente
            User existingUser = userService.getUserByEmail(newEmail);
            if (existingUser != null && !existingUser.getId().equals(Long.parseLong(userId))) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Email già in uso da un altro utente"));
            }

            // Aggiorna l'email
            boolean success = userService.updateUserEmail(userId, newEmail);

            if (success) {
                // Recupera l'utente aggiornato
                User updatedUser = userService.getUserById(userId);
                return ResponseEntity.ok(
                        new AuthResponse(true, "Email aggiornata con successo", updatedUser)
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AuthResponse(false, "Errore durante l'aggiornamento dell'email"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
        }
    }

    /**
     * ===============================
     * ENDPOINT ADMIN GESTIONE LIBRI
     * ===============================
     */

    /**
     * Recupera tutti i libri (solo per admin)
     * GET /api/auth/admin/books
     */
    @GetMapping("/admin/books")
    public ResponseEntity<?> getAllBooksAdmin(@RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("📚 Richiesta lista libri da admin: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            List<Book> books = bookService.getAllBooksForAdmin();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Libri recuperati con successo",
                    "books", books,
                    "total", books.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Errore recupero libri admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Aggiunge un nuovo libro (solo per admin)
     * POST /api/auth/admin/books
     */
    @PostMapping("/admin/books")
    public ResponseEntity<?> addBook(@RequestBody Map<String, String> bookData,
                                     @RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("📚 Richiesta aggiunta libro da: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            // Estrai dati libro
            String isbn = bookData.get("isbn");
            String title = bookData.get("title");
            String author = bookData.get("author");
            String description = bookData.get("description");
            String year = bookData.get("year");
            String category = bookData.get("category");

            // Validazione base
            if (isbn == null || isbn.trim().isEmpty() ||
                    title == null || title.trim().isEmpty() ||
                    author == null || author.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "ISBN, titolo e autore sono obbligatori"));
            }

            boolean success = bookService.addBook(isbn, title, author, description, year, category);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Libro aggiunto con successo"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Impossibile aggiungere il libro (ISBN già esistente o errore database)"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore aggiunta libro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Elimina un libro (solo per admin)
     * DELETE /api/auth/admin/books/{isbn}
     */
    @DeleteMapping("/admin/books/{isbn}")
    public ResponseEntity<?> deleteBook(@PathVariable("isbn") String isbn,
                                        @RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("🗑️ Richiesta eliminazione libro ISBN " + isbn + " da: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            boolean success = bookService.deleteBook(isbn);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Libro eliminato con successo"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Libro non trovato"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione libro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Aggiorna un libro esistente (solo per admin)
     * PUT /api/auth/admin/books/{isbn}
     */
    @PutMapping("/admin/books/{isbn}")
    public ResponseEntity<?> updateBook(@PathVariable("isbn") String isbn,
                                        @RequestBody Map<String, String> bookData,
                                        @RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("📝 Richiesta aggiornamento libro ISBN " + isbn + " da: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            // Estrai dati libro
            String title = bookData.get("title");
            String author = bookData.get("author");
            String description = bookData.get("description");
            String year = bookData.get("year");
            String category = bookData.get("category");

            boolean success = bookService.updateBook(isbn, title, author, description, year, category);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Libro aggiornato con successo"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Libro non trovato"));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento libro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * ===============================
     * ENDPOINT ADMIN GESTIONE RECENSIONI
     * ===============================
     */

    /**
     * Recupera tutte le recensioni/valutazioni (solo per admin)
     * GET /api/auth/admin/ratings
     */
    @GetMapping("/admin/ratings")
    public ResponseEntity<?> getAllRatingsAdmin(@RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("⭐ Richiesta tutte le valutazioni da admin: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accesso negato: privilegi admin richiesti"));
            }

            // Usa query diretta invece del metodo getAllRatings() per evitare problemi di mapping
            List<Map<String, Object>> ratingsData = new ArrayList<>();

            String query = """
        SELECT username, isbn, data, style, content, pleasantness, originality, edition, average, review
        FROM assessment 
        ORDER BY data DESC
        """;

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/DataProva",
                    "postgres",
                    "postgress");
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, Object> ratingMap = new HashMap<>();
                    ratingMap.put("username", rs.getString("username"));
                    ratingMap.put("isbn", rs.getString("isbn"));
                    ratingMap.put("data", rs.getString("data"));
                    ratingMap.put("style", rs.getObject("style"));
                    ratingMap.put("content", rs.getObject("content"));
                    ratingMap.put("pleasantness", rs.getObject("pleasantness"));
                    ratingMap.put("originality", rs.getObject("originality"));
                    ratingMap.put("edition", rs.getObject("edition"));
                    ratingMap.put("average", rs.getObject("average"));
                    ratingMap.put("review", rs.getString("review"));
                    ratingsData.add(ratingMap);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Valutazioni recuperate con successo",
                    "ratings", ratingsData,
                    "total", ratingsData.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Errore recupero valutazioni admin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore interno del server"));
        }
    }

    /**
     * Elimina una recensione specifica (solo per admin)
     * DELETE /api/auth/admin/reviews/{reviewId}
     */
    @DeleteMapping("/admin/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable("reviewId") Long reviewId,
                                          @RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("🗑️ Eliminazione recensione ID: " + reviewId + " da admin: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminResponse(false, "Accesso negato: privilegi admin richiesti"));
            }

            // Elimina la recensione
            boolean success = ratingService.deleteReview(reviewId);

            if (success) {
                System.out.println("✅ Recensione eliminata con successo");
                return ResponseEntity.ok(new AdminResponse(true, "Recensione eliminata con successo"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new AdminResponse(false, "Impossibile eliminare la recensione. Verificare che esista."));
            }

        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione recensione: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AdminResponse(false, "Errore interno del server"));
        }
    }

    /**
     * Elimina tutte le recensioni di un utente (solo per admin)
     * DELETE /api/auth/admin/reviews/user/{username}
     */
    @DeleteMapping("/admin/reviews/user/{username}")
    public ResponseEntity<?> deleteAllUserReviews(@PathVariable("username") String username,
                                                  @RequestParam("adminEmail") String adminEmail) {
        try {
            System.out.println("🚫 Eliminazione recensioni utente: " + username + " da admin: " + adminEmail);

            // Verifica privilegi admin
            if (!userService.isUserAdmin(adminEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminResponse(false, "Accesso negato: privilegi admin richiesti"));
            }

            // Verifica che l'utente esista
            if (!userService.userExists("", username)) {
                return ResponseEntity.badRequest()
                        .body(new AdminResponse(false, "Utente '" + username + "' non trovato nel sistema"));
            }

            // Elimina tutte le recensioni dell'utente
            int deletedCount = ratingService.deleteAllUserReviews(username);

            if (deletedCount > 0) {
                System.out.println("✅ Eliminate " + deletedCount + " recensioni dell'utente " + username);
                return ResponseEntity.ok(
                        new AdminResponse(true, "Eliminate " + deletedCount + " recensioni dell'utente " + username)
                );
            } else {
                return ResponseEntity.ok(
                        new AdminResponse(true, "Nessuna recensione trovata per l'utente " + username)
                );
            }

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Parametro non valido: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AdminResponse(false, "Parametro non valido: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione recensioni utente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AdminResponse(false, "Errore interno del server: " + e.getMessage()));
        }
    }
}