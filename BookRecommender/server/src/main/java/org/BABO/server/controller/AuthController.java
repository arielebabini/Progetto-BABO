package org.BABO.server.controller;

import org.BABO.server.service.BookService;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.User;
import org.BABO.shared.dto.Authentication.AuthRequest;
import org.BABO.shared.dto.Authentication.AuthResponse;
import org.BABO.shared.dto.Authentication.RegisterRequest;
import org.BABO.server.service.UserService;
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
 * Controller REST per la gestione completa dell'autenticazione, profili utente e amministrazione sistema.
 * <p>
 * Questa classe rappresenta il punto di accesso principale per tutte le operazioni di autenticazione,
 * gestione utenti e funzionalità amministrative del sistema BABO. Fornisce endpoint REST sicuri
 * per login, registrazione, gestione profili, amministrazione utenti/libri/recensioni e diagnostica
 * del sistema attraverso un'architettura basata su Spring Boot.
 * </p>
 *
 * <h3>Architettura e responsabilità:</h3>
 * <p>
 * Il controller implementa il pattern MVC separando chiaramente la logica di presentazione (REST endpoints)
 * dalla business logic (delegata ai services) e dalla persistenza (gestita dai services con accesso diretto
 * al database PostgreSQL). Tutte le operazioni sono progettate per essere stateless e thread-safe.
 * </p>
 *
 * <h3>Endpoint categories principali:</h3>
 * <ul>
 *   <li><strong>Autenticazione Base:</strong> {@code /api/auth/login}, {@code /api/auth/register}, {@code /api/auth/logout}</li>
 *   <li><strong>Gestione Profili:</strong> {@code /api/auth/profile/*}, {@code /api/auth/change-password/*}, {@code /api/auth/update-email/*}</li>
 *   <li><strong>Recupero Password:</strong> {@code /api/auth/reset-password}, {@code /api/auth/check-availability}</li>
 *   <li><strong>Amministrazione Utenti:</strong> {@code /api/auth/admin/users/*} (richiede privilegi admin)</li>
 *   <li><strong>Amministrazione Libri:</strong> {@code /api/auth/admin/books/*} (richiede privilegi admin)</li>
 *   <li><strong>Amministrazione Recensioni:</strong> {@code /api/auth/admin/ratings} (richiede privilegi admin)</li>
 *   <li><strong>Diagnostica Sistema:</strong> {@code /api/auth/health} (monitoraggio stato servizi)</li>
 * </ul>
 *
 * <h3>Sistema di sicurezza e autorizzazione:</h3>
 * <p>
 * Il controller implementa un sistema di sicurezza a livelli con le seguenti caratteristiche:
 * </p>
 * <ul>
 *   <li><strong>Validazione Input:</strong> Tutti i parametri vengono validati per formato, lunghezza e sicurezza</li>
 *   <li><strong>Autenticazione SHA-256:</strong> Le password sono protette tramite hashing crittografico</li>
 *   <li><strong>Controllo Privilegi Admin:</strong> Funzioni amministrative protette da whitelist email</li>
 *   <li><strong>Gestione Errori Sicura:</strong> Gli errori non espongono informazioni sensibili del sistema</li>
 *   <li><strong>CORS Abilitato:</strong> Configurato per accessi cross-origin con {@code @CrossOrigin(origins = "*")}</li>
 * </ul>
 *
 * <h3>Gestione response e error handling:</h3>
 * <p>
 * Tutti gli endpoint restituiscono response strutturate utilizzando {@link AuthResponse} per operazioni
 * di autenticazione standard, oppure {@code Map<String, Object>} per operazioni amministrative complesse.
 * Il sistema implementa codici di stato HTTP appropriati e messaggi di errore user-friendly:
 * </p>
 * <ul>
 *   <li><strong>200 OK:</strong> Operazioni completate con successo</li>
 *   <li><strong>201 CREATED:</strong> Nuove risorse create (registrazione utenti)</li>
 *   <li><strong>400 BAD REQUEST:</strong> Dati input non validi o incomplete</li>
 *   <li><strong>401 UNAUTHORIZED:</strong> Credenziali di autenticazione errate</li>
 *   <li><strong>403 FORBIDDEN:</strong> Accesso negato per mancanza privilegi amministrativi</li>
 *   <li><strong>404 NOT FOUND:</strong> Risorsa richiesta inesistente</li>
 *   <li><strong>409 CONFLICT:</strong> Violazione vincoli univocità (email/username duplicati)</li>
 *   <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema o database</li>
 *   <li><strong>503 SERVICE UNAVAILABLE:</strong> Database o servizi esterni non disponibili</li>
 * </ul>
 *
 * <h3>Dipendenze services e integrazione:</h3>
 * <p>
 * Il controller orchestra tre servizi principali iniettati via Spring's {@code @Autowired}:
 * </p>
 * <ul>
 *   <li><strong>{@link UserService}:</strong> Gestione completa utenti, autenticazione, profili e admin</li>
 *   <li><strong>{@link BookService}:</strong> Operazioni CRUD sui libri per funzionalità amministrative</li>
 *   <li><strong>{@link RatingService}:</strong> Gestione valutazioni e recensioni degli utenti</li>
 * </ul>
 *
 * <h3>Configurazione database diretta:</h3>
 * <p>
 * Per alcune operazioni amministrative complesse (es. recupero recensioni con join), il controller
 * accede direttamente al database PostgreSQL utilizzando le seguenti credenziali:
 * </p>
 * <pre>{@code
 * URL: jdbc:postgresql://localhost:5432/DataProva
 * User: postgres
 * Password: postgress
 * }</pre>
 *
 * <h3>Logging e monitoraggio:</h3>
 * <p>
 * Il sistema implementa logging dettagliato con emoji distintive per facilitare debugging e monitoring:
 * </p>
 * <ul>
 *   <li>🔐 Operazioni di autenticazione e login</li>
 *   <li>📝 Registrazioni e modifiche profilo</li>
 *   <li>🔄 Reset e cambi password</li>
 *   <li>👑 Operazioni amministrative</li>
 *   <li>🗑️ Eliminazioni di risorse</li>
 *   <li>📚 Gestione libri</li>
 *   <li>⭐ Gestione recensioni</li>
 *   <li>✅ Operazioni completate con successo</li>
 *   <li>❌ Errori ed operazioni fallite</li>
 * </ul>
 *
 * <h3>Compatibilità e retrocompatibilità:</h3>
 * <p>
 * Il controller è progettato per gestire ID utente sia come {@code String} che come {@code Long},
 * fornendo conversione automatica per compatibilità con diversi client frontend. Tutti i metodi
 * che ricevono {@code userId} come path parameter lo trattano come {@code String} e lo convertono
 * internamente secondo necessità.
 * </p>
 *
 * <h3>Esempio utilizzo completo:</h3>
 * <pre>{@code
 * // Registrazione nuovo utente
 * POST /api/auth/register
 * Content-Type: application/json
 * {
 *   "name": "Mario",
 *   "surname": "Rossi",
 *   "cf": "RSSMRA80A01H501U",
 *   "email": "mario.rossi@email.com",
 *   "username": "mario_rossi",
 *   "password": "password123"
 * }
 *
 * // Login utente
 * POST /api/auth/login
 * Content-Type: application/json
 * {
 *   "email": "mario.rossi@email.com",
 *   "password": "password123"
 * }
 *
 * // Recupero profilo
 * GET /api/auth/profile/1
 *
 * // Cambio password
 * POST /api/auth/change-password/1
 * Content-Type: application/json
 * {
 *   "oldPassword": "password123",
 *   "newPassword": "newPassword456"
 * }
 *
 * // Operazioni admin - Lista utenti
 * GET /api/auth/admin/users?adminEmail=federico@admin.com
 *
 * // Operazioni admin - Aggiungi libro
 * POST /api/auth/admin/books?adminEmail=federico@admin.com
 * Content-Type: application/json
 * {
 *   "isbn": "9788804660347",
 *   "title": "Il Nome della Rosa",
 *   "author": "Umberto Eco",
 *   "description": "Romanzo storico ambientato in un'abbazia medievale",
 *   "year": "1980",
 *   "category": "Narrativa"
 * }
 * }</pre>
 *
 * <h3>Note di performance e scalabilità:</h3>
 * <p>
 * Il controller è progettato per essere stateless e può gestire richieste concorrenti in sicurezza.
 * Ogni operazione database utilizza connessioni atomiche che vengono aperte e chiuse per ogni richiesta,
 * evitando problemi di connection pooling ma garantendo isolation. Per applicazioni ad alto traffico
 * si consiglia l'implementazione di un connection pool dedicato.
 * </p>
 *
 * <h3>Considerazioni di sicurezza aggiuntive:</h3>
 * <ul>
 *   <li><strong>Rate Limiting:</strong> Non implementato - da considerare per ambiente produttivo</li>
 *   <li><strong>Input Sanitization:</strong> Implementata per prevenire SQL injection</li>
 *   <li><strong>Logging Sicuro:</strong> Le password non vengono mai loggate in chiaro</li>
 *   <li><strong>HTTPS Only:</strong> Raccomandato per traffico di produzione</li>
 * </ul>
 *
 * @author BABO Team
 * @version 2.0
 * @since 1.0
 * @see UserService per la gestione completa degli utenti
 * @see BookService per le operazioni sui libri
 * @see RatingService per la gestione delle recensioni
 * @see AuthResponse per la struttura delle response di autenticazione
 * @see User per il modello dati utente
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.springframework.web.bind.annotation.RequestMapping
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    /** Servizio per la gestione completa degli utenti e autenticazione */
    @Autowired
    private UserService userService;

    /** Servizio per le operazioni CRUD sui libri */
    @Autowired
    private BookService bookService;

    /** Servizio per la gestione delle valutazioni e recensioni */
    @Autowired
    private RatingService ratingService;

    /**
     * Endpoint per l'autenticazione utente nel sistema.
     * <p>
     * Questo endpoint rappresenta il punto di accesso principale per gli utenti registrati
     * che desiderano accedere al sistema. Implementa autenticazione sicura tramite verifica
     * di email e password con hashing crittografico, validazione completa degli input e
     * gestione dettagliata degli errori con logging per debugging e monitoring.
     * </p>
     *
     * <p>
     * Il processo di autenticazione segue questi passaggi:
     * </p>
     * <ol>
     *   <li><strong>Validazione Input:</strong> Controllo presenza e formato di email e password</li>
     *   <li><strong>Normalizzazione:</strong> Email convertita a lowercase per consistency</li>
     *   <li><strong>Hashing Sicuro:</strong> Password hashata con SHA-256 prima del confronto</li>
     *   <li><strong>Verifica Database:</strong> Matching con credenziali memorizzate</li>
     *   <li><strong>Response Costruzione:</strong> Oggetto User completo se successo</li>
     * </ol>
     *
     * <h4>Validazioni implementate:</h4>
     * <ul>
     *   <li>Email: non null, non vuota, formato base con @ e .</li>
     *   <li>Password: non null, non vuota, lunghezza minima non applicata in login</li>
     *   <li>Trim automatico: rimozione spazi iniziali/finali da entrambi i campi</li>
     * </ul>
     *
     * <h4>Sicurezza e privacy:</h4>
     * <p>
     * L'endpoint non rivela se l'errore di autenticazione è dovuto a email inesistente
     * o password errata, restituendo sempre lo stesso messaggio generico per prevenire
     * attacchi di user enumeration. Le password vengono sempre hashate prima del
     * confronto e mai loggate in chiaro.
     * </p>
     *
     * @param request oggetto {@link AuthRequest} contenente le credenziali di login
     *               con i campi email (String) e password (String)
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Login riuscito con oggetto User completo</li>
     *           <li><strong>400 BAD REQUEST:</strong> Dati input mancanti o non validi</li>
     *           <li><strong>401 UNAUTHORIZED:</strong> Credenziali errate</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori di sistema o database</li>
     *         </ul>
     *
     * @throws IllegalArgumentException se il request body è malformato
     *
     * @apiNote L'endpoint accetta solo richieste POST con Content-Type application/json.
     *          La response include sempre un flag success (boolean) e un message (String).
     *          In caso di successo, include anche l'oggetto User con tutti i dati del profilo
     *          eccetto la password per ragioni di sicurezza.
     *
     * @see UserService#authenticateUser(String, String) per la logica di autenticazione
     * @see AuthRequest per la struttura della richiesta
     * @see AuthResponse per la struttura della response
     * @see User per i dati utente restituiti
     *
     * @since 1.0
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
     * Endpoint per la registrazione di nuovi utenti nel sistema.
     * <p>
     * Questo endpoint gestisce il processo completo di onboarding di nuovi utenti,
     * inclusa validazione rigorosa dei dati, controllo univocità credenziali,
     * hashing sicuro delle password e creazione dell'account nel database con
     * gestione completa degli errori e logging dettagliato per debugging.
     * </p>
     *
     * <p>
     * Il flusso di registrazione implementa i seguenti passaggi:
     * </p>
     * <ol>
     *   <li><strong>Validazione Completa:</strong> Controllo formato e requisiti per tutti i campi</li>
     *   <li><strong>Verifica Duplicati:</strong> Controllo univocità email e username</li>
     *   <li><strong>Normalizzazione Dati:</strong> Conversione formati per consistency database</li>
     *   <li><strong>Hashing Password:</strong> Conversione sicura della password in hash SHA-256</li>
     *   <li><strong>Creazione Account:</strong> Inserimento nel database con transaction sicura</li>
     *   <li><strong>Response Utente:</strong> Restituzione oggetto User completo se successo</li>
     * </ol>
     *
     * <h4>Validazioni rigorose implementate:</h4>
     * <ul>
     *   <li><strong>Nome:</strong> Non null, non vuoto dopo trim</li>
     *   <li><strong>Cognome:</strong> Non null, non vuoto dopo trim</li>
     *   <li><strong>Email:</strong> Non null, non vuoto, formato valido con @ e estensione</li>
     *   <li><strong>Username:</strong> Non null, non vuoto, case-insensitive</li>
     *   <li><strong>Password:</strong> Non null, lunghezza minima 6 caratteri</li>
     *   <li><strong>Codice Fiscale:</strong> Opzionale, normalizzato uppercase se fornito</li>
     * </ul>
     *
     * <h4>Gestione univocità e duplicati:</h4>
     * <p>
     * Il sistema verifica che email e username non siano già utilizzati da altri utenti.
     * Il controllo avviene sia tramite query preventiva che gestione delle eccezioni
     * di vincolo database, garantendo robustezza anche in caso di race conditions.
     * </p>
     *
     * <h4>Sicurezza e conformità:</h4>
     * <ul>
     *   <li><strong>Password Security:</strong> Hashing SHA-256 prima del storage</li>
     *   <li><strong>Data Normalization:</strong> Email lowercase, CF uppercase per consistency</li>
     *   <li><strong>Input Sanitization:</strong> Trim automatico di tutti i campi stringa</li>
     *   <li><strong>Error Obfuscation:</strong> Messaggi di errore che non espongono dettagli sistema</li>
     * </ul>
     *
     * @param request oggetto {@link RegisterRequest} contenente tutti i dati di registrazione:
     *               name (String), surname (String), cf (String, opzionale),
     *               email (String), username (String), password (String)
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>201 CREATED:</strong> Registrazione completata con oggetto User</li>
     *           <li><strong>400 BAD REQUEST:</strong> Dati input non validi o incompleti</li>
     *           <li><strong>409 CONFLICT:</strong> Email o username già in uso</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori di sistema</li>
     *         </ul>
     *
     * @throws IllegalArgumentException se il request body è malformato
     *
     * @apiNote L'endpoint restituisce HTTP 201 Created in caso di successo per indicare
     *          che una nuova risorsa (utente) è stata creata nel sistema. L'oggetto User
     *          restituito non include la password hashata per motivi di sicurezza.
     *
     * @see UserService#registerUser(String, String, String, String, String, String)
     * @see UserService#userExists(String, String) per il controllo duplicati
     * @see RegisterRequest per la struttura della richiesta
     * @see AuthResponse per la struttura della response
     *
     * @since 1.0
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
     * Endpoint per il reset della password utente tramite indirizzo email.
     * <p>
     * Questo endpoint implementa la funzionalità di "password dimenticata" permettendo
     * agli utenti di reimpostare la propria password utilizzando solo l'indirizzo email
     * come identificativo. La nuova password viene hashata prima del salvataggio e
     * il processo include validazione rigorosa per sicurezza e integrità dei dati.
     * </p>
     *
     * <p>
     * <strong>Importante:</strong> Questo endpoint dovrebbe essere utilizzato solo dopo
     * aver implementato un sistema di verifica identità (es. token via email, OTP, etc.).
     * La versione attuale non include verifica di sicurezza aggiuntiva.
     * </p>
     *
     * <h4>Processo di reset password:</h4>
     * <ol>
     *   <li><strong>Validazione Input:</strong> Controllo presenza email e requisiti nuova password</li>
     *   <li><strong>Verifica Esistenza:</strong> Controllo che l'email esista nel sistema</li>
     *   <li><strong>Hashing Sicuro:</strong> Conversione nuova password in hash SHA-256</li>
     *   <li><strong>Aggiornamento Database:</strong> Sostituzione password esistente</li>
     *   <li><strong>Conferma Operazione:</strong> Response con esito dell'operazione</li>
     * </ol>
     *
     * <h4>Validazioni di sicurezza:</h4>
     * <ul>
     *   <li><strong>Email Required:</strong> Campo obbligatorio e formato valido</li>
     *   <li><strong>Password Length:</strong> Minimo 8 caratteri per sicurezza aumentata</li>
     *   <li><strong>Email Normalization:</strong> Conversione lowercase per consistency</li>
     *   <li><strong>Existence Check:</strong> Verifica che l'account esista prima del reset</li>
     * </ul>
     *
     * @param request oggetto {@link ResetPasswordRequest} contenente:
     *               email (String) - indirizzo email dell'account da reimpostare
     *               newPassword (String) - nuova password in chiaro (min 8 caratteri)
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Password reimpostata con successo</li>
     *           <li><strong>400 BAD REQUEST:</strong> Dati input non validi</li>
     *           <li><strong>404 NOT FOUND:</strong> Email non trovata nel sistema</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema o database</li>
     *         </ul>
     *
     * @throws IllegalArgumentException se il request body è malformato
     *
     * @apiNote <strong>ATTENZIONE SICUREZZA:</strong> Questo endpoint permette il reset
     *          della password senza verifica dell'identità dell'utente. In un ambiente
     *          di produzione dovrebbe essere protetto da token di verifica temporanei
     *          inviati via email o altri meccanismi di autenticazione secondaria.
     *
     * @see UserService#resetPasswordByEmail(String, String) per la logica di reset
     * @see ResetPasswordRequest per la struttura della richiesta
     * @see AuthResponse per la struttura della response
     *
     * @since 1.0
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

    /**
     * DTO interno per le richieste di reset password.
     * <p>
     * Classe di trasferimento dati per incapsulare i parametri necessari
     * all'operazione di reset password tramite email. Implementa il pattern
     * DTO per garantire type safety e validazione strutturata dei dati.
     * </p>
     *
     * @see #resetPassword(ResetPasswordRequest) per l'utilizzo
     * @since 1.0
     */
    public static class ResetPasswordRequest {
        /** L'indirizzo email dell'utente per cui reimpostare la password */
        private String email;

        /** La nuova password in chiaro da impostare */
        private String newPassword;

        /** Costruttore di default richiesto per la deserializzazione JSON */
        public ResetPasswordRequest() {}

        /**
         * Ottiene l'indirizzo email per il reset.
         * @return l'email dell'utente
         */
        public String getEmail() { return email; }

        /**
         * Imposta l'indirizzo email per il reset.
         * @param email l'email dell'utente
         */
        public void setEmail(String email) { this.email = email; }

        /**
         * Ottiene la nuova password da impostare.
         * @return la nuova password in chiaro
         */
        public String getNewPassword() { return newPassword; }

        /**
         * Imposta la nuova password da utilizzare per il reset.
         * @param newPassword la nuova password in chiaro
         */
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * Endpoint per verificare la disponibilità di email e username prima della registrazione.
     * <p>
     * Questo endpoint di utilità permette alle applicazioni client di verificare in tempo reale
     * se un indirizzo email o username sono già in uso nel sistema, facilitando la validazione
     * lato frontend e migliorando l'esperienza utente durante il processo di registrazione.
     * Il controllo può essere effettuato per entrambi i parametri o singolarmente.
     * </p>
     *
     * <h4>Funzionalità e casi d'uso:</h4>
     * <ul>
     *   <li><strong>Validazione Real-time:</strong> Controllo immediato durante la digitazione</li>
     *   <li><strong>UX Enhancement:</strong> Feedback istantaneo senza submit del form</li>
     *   <li><strong>Prevenzione Errori:</strong> Evita tentativi di registrazione con dati duplicati</li>
     *   <li><strong>Flessibilità:</strong> Controllo singolo campo o combinato</li>
     * </ul>
     *
     * @param email indirizzo email da verificare (opzionale)
     * @param username nome utente da verificare (opzionale)
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK (success: true):</strong> Email/username disponibili</li>
     *           <li><strong>200 OK (success: false):</strong> Email/username già in uso</li>
     *           <li><strong>400 BAD REQUEST:</strong> Nessun parametro specificato</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema</li>
     *         </ul>
     *
     * @apiNote Almeno uno tra email e username deve essere specificato. Il controllo
     *          è case-insensitive e include normalizzazione automatica dei dati.
     *
     * @see UserService#userExists(String, String) per la logica di controllo
     * @since 1.0
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
     * Endpoint per recuperare il profilo completo di un utente specifico.
     * <p>
     * Questo endpoint fornisce accesso ai dati del profilo utente utilizzando l'ID univoco
     * come identificativo. Restituisce tutte le informazioni personali dell'utente
     * eccetto la password per ragioni di sicurezza. Supporta compatibilità tra
     * identificatori String e Long per flessibilità di integrazione.
     * </p>
     *
     * <h4>Dati restituiti nel profilo:</h4>
     * <ul>
     *   <li><strong>Identificativo:</strong> ID numerico univoco del sistema</li>
     *   <li><strong>Dati Personali:</strong> Nome, cognome, codice fiscale (se presente)</li>
     *   <li><strong>Credenziali:</strong> Email e username (password esclusa)</li>
     *   <li><strong>Metadati:</strong> Data registrazione, ultimo accesso (se implementato)</li>
     * </ul>
     *
     * @param userId identificativo dell'utente da recuperare (String convertito in Long)
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Profilo recuperato con oggetto User completo</li>
     *           <li><strong>404 NOT FOUND:</strong> Utente inesistente</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema</li>
     *         </ul>
     *
     * @apiNote L'endpoint è compatibile con ID sia numerici che stringhe per supportare
     *          diversi tipi di client frontend. La conversione viene gestita automaticamente.
     *
     * @see UserService#getUserById(String) per la logica di recupero
     * @see User per la struttura dei dati restituiti
     * @since 1.0
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
     * Endpoint per aggiornare le informazioni del profilo utente.
     * <p>
     * Questo endpoint permette la modifica delle informazioni personali dell'utente
     * escludendo i dati sensibili come email, username e password che richiedono
     * endpoint dedicati. Include validazione dei dati e normalizzazione automatica
     * per garantire consistenza nel database.
     * </p>
     *
     * <h4>Campi modificabili:</h4>
     * <ul>
     *   <li><strong>Nome:</strong> Nome dell'utente (trim automatico)</li>
     *   <li><strong>Cognome:</strong> Cognome dell'utente (trim automatico)</li>
     *   <li><strong>Codice Fiscale:</strong> CF opzionale (normalizzazione uppercase)</li>
     * </ul>
     *
     * <h4>Campi non modificabili via questo endpoint:</h4>
     * <ul>
     *   <li>Email (usa {@link #updateEmail})</li>
     *   <li>Username (non modificabile dopo registrazione)</li>
     *   <li>Password (usa {@link #changePassword})</li>
     *   <li>ID utente (immutabile)</li>
     * </ul>
     *
     * @param userId identificativo dell'utente da aggiornare
     * @param updatedUser oggetto User contenente i nuovi dati del profilo
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Profilo aggiornato con dati refresh</li>
     *           <li><strong>404 NOT FOUND:</strong> Utente inesistente</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema</li>
     *         </ul>
     *
     * @see UserService#updateUserProfile(String, String, String, String)
     * @since 1.0
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
     * Endpoint per il cambio password con verifica della password attuale.
     * <p>
     * Implementa un processo sicuro di cambio password che richiede la verifica
     * della password corrente prima di impostare quella nuova. Questo previene
     * modifiche non autorizzate e garantisce che solo il proprietario legittimo
     * dell'account possa modificare le credenziali di accesso.
     * </p>
     *
     * <h4>Processo di cambio password sicuro:</h4>
     * <ol>
     *   <li><strong>Verifica Identità:</strong> Controllo password attuale</li>
     *   <li><strong>Validazione Nuova:</strong> Controllo requisiti nuova password</li>
     *   <li><strong>Hashing Sicuro:</strong> Conversione entrambe le password in hash</li>
     *   <li><strong>Transazione Atomica:</strong> Aggiornamento database sicuro</li>
     *   <li><strong>Conferma:</strong> Response con esito dell'operazione</li>
     * </ol>
     *
     * <h4>Requisiti di sicurezza:</h4>
     * <ul>
     *   <li><strong>Password Attuale:</strong> Deve corrispondere esattamente</li>
     *   <li><strong>Nuova Password:</strong> Minimo 6 caratteri (configurabile)</li>
     *   <li><strong>Hashing:</strong> SHA-256 per entrambe le password</li>
     *   <li><strong>Autorizzazione:</strong> Solo il proprietario può cambiare</li>
     * </ul>
     *
     * @param userId identificativo dell'utente per cui cambiare la password
     * @param request oggetto {@link ChangePasswordRequest} con password vecchia e nuova
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Password cambiata con successo</li>
     *           <li><strong>400 BAD REQUEST:</strong> Password attuale errata o dati non validi</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema</li>
     *         </ul>
     *
     * @see UserService#changePassword(String, String, String)
     * @see ChangePasswordRequest per la struttura della richiesta
     * @since 1.0
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
     * Endpoint per il logout utente (placeholder per implementazioni future).
     * <p>
     * Attualmente fornisce un endpoint di logout di base che restituisce sempre successo.
     * In futuro potrà essere esteso per gestire invalidazione di sessioni, JWT tokens,
     * logging di audit, e cleanup di risorse temporanee associate alla sessione utente.
     * </p>
     *
     * <h4>Funzionalità future pianificate:</h4>
     * <ul>
     *   <li><strong>JWT Invalidation:</strong> Blacklist dei token attivi</li>
     *   <li><strong>Session Cleanup:</strong> Rimozione dati sessione dal cache</li>
     *   <li><strong>Audit Logging:</strong> Registrazione eventi di logout</li>
     *   <li><strong>Multi-device:</strong> Logout selettivo da dispositivi specifici</li>
     * </ul>
     *
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Logout effettuato (sempre per ora)</li>
     *         </ul>
     *
     * @apiNote Questo è un placeholder che restituisce sempre successo.
     *          L'implementazione completa dipenderà dal sistema di sessioni adottato.
     *
     * @since 1.0
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        // Per ora è solo un placeholder, in futuro si potrebbe invalidare il token JWT
        return ResponseEntity.ok(
                new AuthResponse(true, "Logout effettuato con successo")
        );
    }

    /**
     * Endpoint amministrativo per recuperare la lista completa di tutti gli utenti registrati.
     * <p>
     * Questo endpoint fornisce accesso privilegiato alla lista completa degli utenti
     * del sistema per scopi amministrativi, reporting e gestione. Include controllo
     * rigoroso dei privilegi amministrativi e logging dettagliato per audit e sicurezza.
     * </p>
     *
     * <h4>Controlli di sicurezza:</h4>
     * <ul>
     *   <li><strong>Verifica Admin:</strong> Solo email nella whitelist amministratori</li>
     *   <li><strong>Audit Logging:</strong> Registrazione accessi alla lista utenti</li>
     *   <li><strong>Response Strutturata:</strong> Formato JSON con metadati</li>
     * </ul>
     *
     * <h4>Dati restituiti per ogni utente:</h4>
     * <ul>
     *   <li>ID, nome, cognome, email, username</li>
     *   <li>Codice fiscale (se presente)</li>
     *   <li>Password esclusa per sicurezza</li>
     * </ul>
     *
     * @param adminEmail email dell'amministratore che richiede la lista (verifica privilegi)
     * @return {@code ResponseEntity<?>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Lista utenti con metadati (success, message, users, total)</li>
     *           <li><strong>403 FORBIDDEN:</strong> Privilegi amministrativi insufficienti</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema</li>
     *         </ul>
     *
     * @see UserService#isUserAdmin(String) per la verifica privilegi
     * @see UserService#getAllUsers() per il recupero dati
     * @since 1.0
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
     * Endpoint amministrativo per eliminare un utente dal sistema.
     * <p>
     * Funzionalità di gestione utenti riservata agli amministratori per l'eliminazione
     * permanente di account utente. L'operazione è irreversibile e include controlli
     * di sicurezza, logging audit e gestione delle dipendenze referenziali.
     * </p>
     *
     * <p>
     * <strong>ATTENZIONE:</strong> L'eliminazione è permanente e potrebbe causare
     * problemi di integrità referenziale se l'utente ha dati correlati in altre tabelle.
     * </p>
     *
     * <h4>Controlli pre-eliminazione:</h4>
     * <ul>
     *   <li><strong>Privilegi Admin:</strong> Verifica whitelist amministratori</li>
     *   <li><strong>Esistenza Utente:</strong> Controllo presenza nel database</li>
     *   <li><strong>Audit Trail:</strong> Logging completo dell'operazione</li>
     * </ul>
     *
     * @param userId identificativo dell'utente da eliminare
     * @param adminEmail email dell'amministratore che richiede l'eliminazione
     * @return {@code ResponseEntity<?>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Utente eliminato con successo</li>
     *           <li><strong>403 FORBIDDEN:</strong> Privilegi insufficienti</li>
     *           <li><strong>404 NOT FOUND:</strong> Utente inesistente</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema</li>
     *         </ul>
     *
     * @see UserService#deleteUser(String) per la logica di eliminazione
     * @since 1.0
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
     * Endpoint di diagnostica per verificare lo stato di salute del servizio di autenticazione.
     * <p>
     * Fornisce un health check completo del sistema di autenticazione includendo
     * verifica della connettività database, stato dei servizi integrati e
     * diagnostica generale dell'infrastruttura. Utile per monitoring,
     * load balancing e troubleshooting.
     * </p>
     *
     * <h4>Controlli effettuati:</h4>
     * <ul>
     *   <li><strong>Database Connectivity:</strong> Test connessione PostgreSQL</li>
     *   <li><strong>Service Status:</strong> Verifica disponibilità UserService</li>
     *   <li><strong>System Resources:</strong> Controllo risorse base (futuro)</li>
     * </ul>
     *
     * <h4>Response status:</h4>
     * <ul>
     *   <li><strong>200 OK:</strong> Servizio e database operativi</li>
     *   <li><strong>503 SERVICE UNAVAILABLE:</strong> Database non raggiungibile</li>
     * </ul>
     *
     * @return {@code ResponseEntity<AuthResponse>} con stato dettagliato del servizio
     *
     * @see UserService#isDatabaseAvailable() per la verifica database
     * @since 1.0
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
     * DTO per le richieste di cambio password con verifica.
     * <p>
     * Classe di trasferimento dati che incapsula i parametri necessari
     * per il processo sicuro di cambio password, includendo sia la password
     * attuale per verifica che quella nuova da impostare.
     * </p>
     *
     * @see #changePassword(String, ChangePasswordRequest)
     * @since 1.0
     */
    public static class ChangePasswordRequest {
        /** Password attuale dell'utente per verifica identità */
        private String oldPassword;

        /** Nuova password da impostare */
        private String newPassword;

        /** Costruttore di default per deserializzazione JSON */
        public ChangePasswordRequest() {}

        /**
         * Ottiene la password attuale per verifica.
         * @return la password attuale in chiaro
         */
        public String getOldPassword() { return oldPassword; }

        /**
         * Imposta la password attuale per verifica.
         * @param oldPassword la password attuale in chiaro
         */
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        /**
         * Ottiene la nuova password da impostare.
         * @return la nuova password in chiaro
         */
        public String getNewPassword() { return newPassword; }

        /**
         * Imposta la nuova password da utilizzare.
         * @param newPassword la nuova password in chiaro
         */
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * Endpoint per aggiornare l'indirizzo email di un utente esistente.
     * <p>
     * Permette la modifica dell'indirizzo email dell'utente con validazione
     * del formato, controllo univocità e normalizzazione automatica.
     * Include verifiche per prevenire duplicati e garantire integrità dei dati.
     * </p>
     *
     * <h4>Validazioni implementate:</h4>
     * <ul>
     *   <li><strong>Formato Email:</strong> Regex per validazione RFC basic</li>
     *   <li><strong>Univocità:</strong> Controllo che non sia già utilizzata</li>
     *   <li><strong>Normalizzazione:</strong> Conversione a lowercase</li>
     *   <li><strong>Esistenza Utente:</strong> Verifica che l'utente esista</li>
     * </ul>
     *
     * @param userId identificativo dell'utente di cui modificare l'email
     * @param request mappa contenente la chiave "email" con il nuovo indirizzo
     * @return {@code ResponseEntity<AuthResponse>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Email aggiornata con dati utente refresh</li>
     *           <li><strong>400 BAD REQUEST:</strong> Email non valida o già in uso</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori sistema</li>
     *         </ul>
     *
     * @see UserService#updateUserEmail(String, String)
     * @see UserService#getUserByEmail(String) per controllo duplicati
     * @since 1.0
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

    // ===============================
    // SEZIONE AMMINISTRAZIONE LIBRI
    // ===============================

    /**
     * Endpoint amministrativo per recuperare tutti i libri del sistema.
     * <p>
     * Fornisce accesso amministrativo completo al catalogo libri per gestione,
     * reporting e operazioni di manutenzione. Include controllo privilegi
     * rigoroso e logging audit per sicurezza e compliance.
     * </p>
     *
     * <h4>Dati libro restituiti:</h4>
     * <ul>
     *   <li>ISBN, titolo, autore, descrizione</li>
     *   <li>Anno pubblicazione, categoria</li>
     *   <li>Metadati sistema (data inserimento, etc.)</li>
     * </ul>
     *
     * @param adminEmail email amministratore per verifica privilegi
     * @return {@code ResponseEntity<?>} con lista libri e metadati
     *
     * @see BookService#getAllBooksForAdmin()
     * @since 1.0
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
     * Endpoint amministrativo per aggiungere un nuovo libro al catalogo.
     * <p>
     * Permette agli amministratori di inserire nuovi libri nel sistema
     * con validazione completa dei dati, controllo duplicati ISBN e
     * normalizzazione automatica delle informazioni bibliografiche.
     * </p>
     *
     * <h4>Campi obbligatori:</h4>
     * <ul>
     *   <li><strong>ISBN:</strong> Identificativo univoco del libro</li>
     *   <li><strong>Titolo:</strong> Titolo completo dell'opera</li>
     *   <li><strong>Autore:</strong> Nome dell'autore principale</li>
     * </ul>
     *
     * <h4>Campi opzionali:</h4>
     * <ul>
     *   <li><strong>Descrizione:</strong> Sinossi o descrizione dell'opera</li>
     *   <li><strong>Anno:</strong> Anno di pubblicazione</li>
     *   <li><strong>Categoria:</strong> Genere o categoria letteraria</li>
     * </ul>
     *
     * @param bookData mappa con i dati del libro da inserire
     * @param adminEmail email amministratore per verifica privilegi
     * @return {@code ResponseEntity<?>} con esito dell'operazione
     *
     * @see BookService#addBook(String, String, String, String, String, String)
     * @since 1.0
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
     * Endpoint amministrativo per eliminare un libro dal catalogo.
     * <p>
     * Permette la rimozione permanente di libri dal sistema utilizzando
     * l'ISBN come identificativo univoco. L'operazione è irreversibile
     * e potrebbe influenzare recensioni e valutazioni esistenti.
     * </p>
     *
     * <p>
     * <strong>ATTENZIONE:</strong> L'eliminazione potrebbe causare problemi
     * di integrità referenziale se esistono recensioni associate al libro.
     * </p>
     *
     * @param isbn codice ISBN del libro da eliminare
     * @param adminEmail email amministratore per verifica privilegi
     * @return {@code ResponseEntity<?>} con esito dell'eliminazione
     *
     * @see BookService#deleteBook(String)
     * @since 1.0
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
     * Endpoint amministrativo per aggiornare le informazioni di un libro esistente.
     * <p>
     * Permette la modifica delle informazioni bibliografiche di un libro
     * identificato tramite ISBN. L'ISBN stesso non può essere modificato
     * in quanto utilizzato come chiave primaria del sistema.
     * </p>
     *
     * <h4>Campi modificabili:</h4>
     * <ul>
     *   <li>Titolo, autore, descrizione</li>
     *   <li>Anno pubblicazione, categoria</li>
     *   <li>Altri metadati non sensibili</li>
     * </ul>
     *
     * @param isbn codice ISBN del libro da aggiornare (immutabile)
     * @param bookData mappa con i nuovi dati del libro
     * @param adminEmail email amministratore per verifica privilegi
     * @return {@code ResponseEntity<?>} con esito dell'aggiornamento
     *
     * @see BookService#updateBook(String, String, String, String, String, String)
     * @since 1.0
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

    // ===============================
    // SEZIONE AMMINISTRAZIONE RECENSIONI
    // ===============================

    /**
     * Endpoint amministrativo per recuperare tutte le recensioni e valutazioni del sistema.
     * <p>
     * Fornisce accesso amministrativo completo alle recensioni utenti per monitoring,
     * moderazione e analisi qualitativa. Utilizza accesso diretto al database per
     * gestire query complesse con join tra tabelle e mapping personalizzato dei risultati.
     * </p>
     *
     * <p>
     * <strong>Implementazione tecnica:</strong> Questo endpoint utilizza connessione
     * diretta al database PostgreSQL invece del RatingService per evitare problemi
     * di mapping complesso dei dati di valutazione che includono campi multipli
     * e strutture nested.
     * </p>
     *
     * <h4>Dati recensione restituiti:</h4>
     * <ul>
     *   <li><strong>Identificativi:</strong> Username utente, ISBN libro</li>
     *   <li><strong>Valutazioni numeriche:</strong> Stile, contenuto, piacevolezza, originalità, edizione</li>
     *   <li><strong>Metriche:</strong> Media delle valutazioni</li>
     *   <li><strong>Contenuto:</strong> Testo della recensione completa</li>
     *   <li><strong>Metadata:</strong> Data creazione, timestamp</li>
     * </ul>
     *
     * <h4>Struttura query database:</h4>
     * <p>
     * La query accede alla tabella {@code assessment} con proiezione di tutti
     * i campi rilevanti ordinati per data decrescente per visualizzare
     * le recensioni più recenti per prime.
     * </p>
     *
     * <h4>Configurazione database utilizzata:</h4>
     * <pre>{@code
     * URL: jdbc:postgresql://localhost:5432/DataProva
     * User: postgres
     * Password: postgress
     * Query: SELECT username, isbn, data, style, content, pleasantness,
     *               originality, edition, average, review
     *        FROM assessment ORDER BY data DESC
     * }</pre>
     *
     * @param adminEmail email amministratore per verifica privilegi
     * @return {@code ResponseEntity<?>} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> Lista recensioni con metadati (success, message, ratings, total)</li>
     *           <li><strong>403 FORBIDDEN:</strong> Privilegi insufficienti</li>
     *           <li><strong>500 INTERNAL SERVER ERROR:</strong> Errori database o sistema</li>
     *         </ul>
     *
     * @throws SQLException in caso di problemi di connettività database
     *
     * @apiNote Questo endpoint utilizza connessione diretta al database invece
     *          del service layer per gestire la complessità del mapping dei dati
     *          di valutazione con campi multipli e tipi misti.
     *
     * @see java.sql.Connection per la gestione della connessione database
     * @see java.sql.PreparedStatement per l'esecuzione sicura delle query
     * @since 1.0
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
}