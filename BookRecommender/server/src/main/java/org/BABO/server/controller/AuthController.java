package org.BABO.server.controller;

import org.BABO.shared.model.User;
import org.BABO.shared.dto.AuthRequest;
import org.BABO.shared.dto.AuthResponse;
import org.BABO.shared.dto.RegisterRequest;
import org.BABO.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Endpoint per cambiare password
     * POST /api/auth/change-password/{userId}
     * AGGIORNATO per compatibilità String/Long
     */
    @PostMapping("/change-password/{userId}")
    public ResponseEntity<AuthResponse> changePassword(
            @PathVariable String userId,
            @RequestBody ChangePasswordRequest request) {
        try {
            if (request.getOldPassword() == null || request.getNewPassword() == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Password vecchia e nuova sono obbligatorie"));
            }

            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "La nuova password deve essere almeno 6 caratteri"));
            }

            boolean success = userService.changePassword(
                    userId,  // Ora String, convertito internamente nel service
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
     * Endpoint per recuperare tutti gli utenti (solo per admin)
     * GET /api/auth/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            System.out.println("📊 Restituiti " + users.size() + " utenti");
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            System.err.println("❌ Errore recupero utenti: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint per eliminare un utente (solo per admin)
     * DELETE /api/auth/users/{userId}
     * AGGIORNATO per compatibilità String/Long
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<AuthResponse> deleteUser(@PathVariable String userId) {
        try {
            boolean success = userService.deleteUser(userId); // Ora String

            if (success) {
                return ResponseEntity.ok(
                        new AuthResponse(true, "Utente eliminato con successo")
                );
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione utente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Errore interno del server"));
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
     * DTO per la richiesta di cambio password
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
}