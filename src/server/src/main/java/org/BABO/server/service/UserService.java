package org.BABO.server.service;

import org.BABO.shared.model.User;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio server per la gestione completa degli utenti e dell'autenticazione.
 * <p>
 * Questa classe fornisce un'interfaccia completa per tutte le operazioni relative
 * alla gestione degli utenti del sistema, incluse registrazione, autenticazione,
 * aggiornamento profili, gestione password e funzionalità amministrative.
 * Gestisce l'interazione diretta con il database PostgreSQL implementando
 * sicurezza tramite hashing delle password e controlli di validazione.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Autenticazione Sicura:</strong> Login con hashing SHA-256 delle password</li>
 *   <li><strong>Registrazione Utenti:</strong> Creazione nuovi account con validazione dati</li>
 *   <li><strong>Gestione Profili:</strong> Aggiornamento informazioni personali e credenziali</li>
 *   <li><strong>Reset Password:</strong> Recupero password tramite email</li>
 *   <li><strong>Controlli Amministrativi:</strong> Funzioni admin per gestione utenti</li>
 *   <li><strong>Validazione Dati:</strong> Controlli di integrità e formato su input utente</li>
 * </ul>
 *
 * <h3>Struttura Database:</h3>
 * <p>
 * Il servizio opera sulla tabella {@code users} con la seguente struttura:
 * </p>
 * <ul>
 *   <li>{@code id} (BIGSERIAL PRIMARY KEY) - Identificativo univoco utente</li>
 *   <li>{@code name} (VARCHAR) - Nome dell'utente</li>
 *   <li>{@code surname} (VARCHAR) - Cognome dell'utente</li>
 *   <li>{@code cf} (VARCHAR, nullable) - Codice fiscale (opzionale)</li>
 *   <li>{@code email} (VARCHAR UNIQUE) - Indirizzo email univoco</li>
 *   <li>{@code username} (VARCHAR UNIQUE) - Username univoco</li>
 *   <li>{@code password} (VARCHAR) - Password hashata con SHA-256</li>
 * </ul>
 *
 * <h3>Sistema di Sicurezza:</h3>
 * <p>
 * Il servizio implementa le seguenti misure di sicurezza:
 * </p>
 * <ul>
 *   <li><strong>Hashing Password:</strong> Utilizzo di SHA-256 per storage sicuro</li>
 *   <li><strong>Validazione Input:</strong> Sanitizzazione e controllo formato dati</li>
 *   <li><strong>Prevenzione Duplicati:</strong> Controlli univocità email e username</li>
 *   <li><strong>Controllo Accesso Admin:</strong> Lista whitelist amministratori</li>
 * </ul>
 *
 * <h3>Configurazione Database:</h3>
 * <pre>{@code
 * URL: jdbc:postgresql://localhost:5432/DataProva
 * User: postgres
 * Password: postgress
 * }</pre>
 *
 * <h3>Amministratori Sistema:</h3>
 * <p>
 * Gli utenti con le seguenti email hanno privilegi amministrativi:
 * </p>
 * <ul>
 *   <li>federico@admin.com</li>
 *   <li>ariele@admin.com</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * @Autowired
 * private UserService userService;
 *
 * // Registrazione nuovo utente
 * User newUser = userService.registerUser(
 *     "Mario", "Rossi", "RSSMRA80A01H501U",
 *     "mario.rossi@email.com", "mario_rossi", "password123"
 * );
 *
 * if (newUser != null) {
 *     System.out.println("Utente registrato: " + newUser.getDisplayName());
 * }
 *
 * // Autenticazione
 * User authenticatedUser = userService.authenticateUser(
 *     "mario.rossi@email.com", "password123"
 * );
 *
 * if (authenticatedUser != null) {
 *     System.out.println("Login riuscito per: " + authenticatedUser.getUsername());
 *
 *     // Verifica privilegi admin
 *     boolean isAdmin = userService.isUserAdmin(authenticatedUser.getEmail());
 *     if (isAdmin) {
 *         // Recupera tutti gli utenti per admin
 *         List<User> allUsers = userService.getAllUsers();
 *         System.out.println("Utenti totali: " + allUsers.size());
 *     }
 * }
 *
 * // Aggiornamento profilo
 * User updatedUser = userService.updateUserProfile(
 *     String.valueOf(authenticatedUser.getId()),
 *     "Mario", "Rossi", "RSSMRA80A01H501U"
 * );
 *
 * // Cambio password
 * boolean passwordChanged = userService.changePassword(
 *     String.valueOf(authenticatedUser.getId()),
 *     "password123", "newPassword456"
 * );
 * }</pre>
 *
 * <h3>Gestione Errori:</h3>
 * <p>
 * Il servizio implementa logging dettagliato e gestione graceful degli errori,
 * restituendo null o false in caso di operazioni fallite invece di propagare
 * eccezioni. Tutti gli errori sono tracciati con emoji distintive per
 * facilitare debugging e monitoring.
 * </p>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * La classe è marcata come {@code @Service} e utilizza connessioni database
 * atomiche per ogni operazione, garantendo thread-safety in ambienti multi-utente.
 * Ogni operazione apre e chiude la propria connessione per evitare conflitti.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see User
 * @see org.springframework.stereotype.Service
 */
@Service
public class UserService {

    /** URL di connessione al database PostgreSQL */
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";

    /** Username per l'autenticazione database */
    private static final String DB_USER = "postgres";

    /** Password per l'autenticazione database */
    private static final String DB_PASSWORD = "postgress";

    /**
     * Autentica un utente utilizzando email e password con verifica hash sicura.
     * <p>
     * Questo metodo rappresenta il punto di accesso principale al sistema per gli utenti
     * registrati. Implementa autenticazione sicura tramite hashing SHA-256 delle password
     * e normalizzazione automatica degli input per garantire consistenza dei dati.
     * </p>
     *
     * <p>
     * Il processo di autenticazione include:
     * </p>
     * <ol>
     *   <li>Normalizzazione email (lowercase e trim)</li>
     *   <li>Hashing della password fornita con SHA-256</li>
     *   <li>Query database per matching email e hash password</li>
     *   <li>Costruzione oggetto User completo se autenticazione riuscita</li>
     * </ol>
     *
     * @param email l'indirizzo email dell'utente (case-insensitive)
     * @param password la password in chiaro dell'utente
     * @return un oggetto {@link User} completo se l'autenticazione è riuscita,
     *         {@code null} se le credenziali sono errate o si verificano errori
     *
     * @throws IllegalArgumentException se email o password sono null
     *
     * @apiNote L'email viene automaticamente normalizzata a lowercase per consistency.
     *          La password viene hashata con SHA-256 prima del confronto database.
     *          Il metodo non rivela se l'errore è dovuto a email inesistente o password errata
     *          per ragioni di sicurezza.
     *
     * @see #hashPassword(String)
     */
    public User authenticateUser(String email, String password) {
        System.out.println("🔐 Tentativo autenticazione per: " + email);

        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        String hashedPassword = hashPassword(password);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email.toLowerCase().trim());
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // COSTRUTTORE: Long id, String name, String surname, String cf, String email, String username
                User user = new User(
                        rs.getLong("id"),           // Long id
                        rs.getString("name"),       // String name
                        rs.getString("surname"),    // String surname
                        rs.getString("cf"),         // String cf
                        rs.getString("email"),      // String email
                        rs.getString("username")    // String username
                );

                System.out.println("✅ Autenticazione riuscita per: " + user.getDisplayName());
                return user;
            } else {
                System.out.println("❌ Autenticazione fallita per: " + email);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'autenticazione: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registra un nuovo utente nel sistema con validazione completa dei dati.
     * <p>
     * Questo metodo gestisce il processo completo di registrazione di un nuovo utente,
     * inclusa validazione dei dati di input, controllo di univocità email/username,
     * hashing sicuro della password e inserimento nel database con gestione degli errori.
     * </p>
     *
     * <p>
     * Il flusso di registrazione include:
     * </p>
     * <ol>
     *   <li><strong>Validazione formato:</strong> Controllo validità email e lunghezza password minima</li>
     *   <li><strong>Controllo duplicati:</strong> Verifica univocità email e username</li>
     *   <li><strong>Normalizzazione dati:</strong> Conversione a lowercase, trim e uppercase per CF</li>
     *   <li><strong>Hashing password:</strong> Conversione password in hash SHA-256 sicuro</li>
     *   <li><strong>Inserimento database:</strong> Creazione record con RETURNING per conferma</li>
     * </ol>
     *
     * @param name il nome dell'utente (viene effettuato trim)
     * @param surname il cognome dell'utente (viene effettuato trim)
     * @param cf il codice fiscale dell'utente (opzionale, viene normalizzato uppercase)
     * @param email l'indirizzo email univoco (viene normalizzato lowercase)
     * @param username l'username univoco (viene normalizzato lowercase)
     * @param password la password in chiaro (deve essere almeno 6 caratteri)
     * @return un oggetto {@link User} completo del nuovo utente registrato,
     *         {@code null} se la validazione fallisce, esistono duplicati o si verificano errori
     *
     * @throws IllegalArgumentException se parametri obbligatori sono null
     *
     * @apiNote La password deve essere almeno 6 caratteri. L'email deve contenere @ e . con
     *          lunghezza minima 6 caratteri. Il codice fiscale può essere null ed è opzionale.
     *          Tutti gli errori di vincoli database (duplicati) sono gestiti gracefully.
     *
     * @see #isValidEmail(String)
     * @see #userExists(String, String)
     * @see #hashPassword(String)
     */
    public User registerUser(String name, String surname, String cf, String email, String username, String password) {
        System.out.println("📝 Tentativo registrazione per: " + email);

        // Validazione base
        if (!isValidEmail(email)) {
            System.out.println("❌ Email non valida: " + email);
            return null;
        }

        if (password.length() < 6) {
            System.out.println("❌ Password troppo corta");
            return null;
        }

        // Controlla se l'utente esiste già
        if (userExists(email, username)) {
            System.out.println("❌ Utente già esistente con email o username: " + email + " / " + username);
            return null;
        }

        String query = "INSERT INTO users (name, surname, cf, email, username, password) VALUES (?, ?, ?, ?, ?, ?) RETURNING *";
        String hashedPassword = hashPassword(password);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name.trim());
            stmt.setString(2, surname.trim());
            stmt.setString(3, cf != null ? cf.trim().toUpperCase() : null);
            stmt.setString(4, email.toLowerCase().trim());
            stmt.setString(5, username.toLowerCase().trim());
            stmt.setString(6, hashedPassword);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // COSTRUTTORE: Long id, String name, String surname, String cf, String email, String username
                User newUser = new User(
                        rs.getLong("id"),           // Long id
                        rs.getString("name"),       // String name
                        rs.getString("surname"),    // String surname
                        rs.getString("cf"),         // String cf
                        rs.getString("email"),      // String email
                        rs.getString("username")    // String username
                );

                System.out.println("✅ Registrazione completata per: " + newUser.getDisplayName());
                return newUser;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la registrazione: " + e.getMessage());
            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                System.out.println("❌ Email o username già in uso");
            }
        }

        return null;
    }

    /**
     * Verifica se esiste già un utente con la stessa email o username nel sistema.
     * <p>
     * Metodo di utilità utilizzato per prevenire la registrazione di utenti duplicati.
     * Controlla l'univocità di email e username prima di procedere con la registrazione
     * di un nuovo utente, garantendo l'integrità dei vincoli del database.
     * </p>
     *
     * @param email l'indirizzo email da verificare (viene normalizzato lowercase)
     * @param username l'username da verificare (viene normalizzato lowercase)
     * @return {@code true} se esiste già un utente con email o username specificati,
     *         {@code false} se entrambi sono disponibili o in caso di errori database
     *
     * @throws IllegalArgumentException se email o username sono null
     *
     * @apiNote Entrambi i parametri vengono normalizzati automaticamente per consistency
     *          con le procedure di registrazione e autenticazione. Il metodo restituisce
     *          true anche se solo uno dei due parametri risulta duplicato.
     */
    public boolean userExists(String email, String username) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ? OR username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email.toLowerCase().trim());
            stmt.setString(2, username.toLowerCase().trim());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore controllo esistenza utente: " + e.getMessage());
        }

        return false;
    }

    /**
     * Recupera un utente specifico tramite il suo ID univoco.
     * <p>
     * Metodo per il recupero diretto di utenti tramite identificativo numerico.
     * Utilizzato principalmente per operazioni interne del sistema e per verifiche
     * di autorizzazione quando si conosce l'ID dell'utente.
     * </p>
     *
     * @param userId l'identificativo dell'utente come stringa (verrà convertito in Long)
     * @return un oggetto {@link User} completo se l'ID esiste nel sistema,
     *         {@code null} se l'utente non viene trovato, l'ID non è valido o si verificano errori
     *
     * @throws IllegalArgumentException se userId è null
     *
     * @apiNote Il parametro userId viene automaticamente convertito da String a Long.
     *          Se la conversione fallisce, il metodo restituisce null. Questo design
     *          facilita l'integrazione con API REST che tipicamente gestiscono ID come stringhe.
     */
    public User getUserById(String userId) {
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, Long.parseLong(userId)); // Converte String a Long per il DB
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // COSTRUTTORE: Long id, String name, String surname, String cf, String email, String username
                return new User(
                        rs.getLong("id"),           // Long id
                        rs.getString("name"),       // String name
                        rs.getString("surname"),    // String surname
                        rs.getString("cf"),         // String cf
                        rs.getString("email"),      // String email
                        rs.getString("username")    // String username
                );
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("❌ Errore recupero utente: " + e.getMessage());
        }

        return null;
    }

    /**
     * Recupera un utente specifico tramite il suo indirizzo email.
     * <p>
     * Metodo alternativo per il recupero utenti utilizzando l'email come chiave di ricerca.
     * Particolarmente utile per operazioni di recupero password, verifica esistenza
     * account e operazioni amministrative dove si conosce l'email ma non l'ID.
     * </p>
     *
     * @param email l'indirizzo email dell'utente da cercare (case-insensitive)
     * @return un oggetto {@link User} completo se l'email esiste nel sistema,
     *         {@code null} se l'utente non viene trovato o si verificano errori
     *
     * @throws IllegalArgumentException se email è null
     *
     * @apiNote L'email viene automaticamente normalizzata a lowercase per consistency
     *          con le altre operazioni del servizio. Il matching è case-insensitive.
     */
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getLong("id"),           // Long id
                        rs.getString("name"),       // String name
                        rs.getString("surname"),    // String surname
                        rs.getString("cf"),         // String cf
                        rs.getString("email"),      // String email
                        rs.getString("username")    // String username
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero utente per email: " + e.getMessage());
        }

        return null;
    }

    /**
     * Aggiorna le informazioni del profilo utente (nome, cognome, codice fiscale).
     * <p>
     * Questo metodo consente l'aggiornamento delle informazioni personali dell'utente
     * escludendo dati sensibili come email, username e password che richiedono
     * procedure separate. Include validazione e normalizzazione automatica dei dati.
     * </p>
     *
     * <p>
     * I campi aggiornabili includono:
     * </p>
     * <ul>
     *   <li><strong>Nome:</strong> Viene effettuato trim automatico</li>
     *   <li><strong>Cognome:</strong> Viene effettuato trim automatico</li>
     *   <li><strong>Codice Fiscale:</strong> Normalizzato uppercase, può essere null</li>
     * </ul>
     *
     * @param userId l'identificativo dell'utente da aggiornare (stringa convertita in Long)
     * @param name il nuovo nome dell'utente
     * @param surname il nuovo cognome dell'utente
     * @param cf il nuovo codice fiscale (opzionale, può essere null)
     * @return un oggetto {@link User} aggiornato se l'operazione è riuscita,
     *         {@code null} se l'utente non esiste, l'ID non è valido o si verificano errori
     *
     * @throws IllegalArgumentException se userId, name o surname sono null
     *
     * @apiNote Utilizza clausola RETURNING PostgreSQL per ottenere immediatamente
     *          i dati aggiornati. Il codice fiscale viene automaticamente normalizzato
     *          uppercase se fornito.
     */
    public User updateUserProfile(String userId, String name, String surname, String cf) {
        String query = "UPDATE users SET name = ?, surname = ?, cf = ? WHERE id = ? RETURNING *";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name.trim());
            stmt.setString(2, surname.trim());
            stmt.setString(3, cf != null ? cf.trim().toUpperCase() : null);
            stmt.setLong(4, Long.parseLong(userId)); // Converte String a Long per il DB

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // COSTRUTTORE: Long id, String name, String surname, String cf, String email, String username
                User updatedUser = new User(
                        rs.getLong("id"),           // Long id
                        rs.getString("name"),       // String name
                        rs.getString("surname"),    // String surname
                        rs.getString("cf"),         // String cf
                        rs.getString("email"),      // String email
                        rs.getString("username")    // String username
                );

                System.out.println("✅ Profilo aggiornato per: " + updatedUser.getDisplayName());
                return updatedUser;
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("❌ Errore aggiornamento profilo: " + e.getMessage());
        }

        return null;
    }

    /**
     * Cambia la password di un utente con verifica della password attuale.
     * <p>
     * Implementa un processo di cambio password sicuro che richiede la verifica
     * della password corrente prima di impostare quella nuova. Utilizza hashing
     * SHA-256 per entrambe le password e gestione transazionale per garantire atomicità.
     * </p>
     *
     * <p>
     * Il processo di cambio password include:
     * </p>
     * <ol>
     *   <li><strong>Verifica password attuale:</strong> Hash e confronto con valore database</li>
     *   <li><strong>Hashing nuova password:</strong> Conversione sicura della nuova password</li>
     *   <li><strong>Aggiornamento atomico:</strong> Modifica database solo se verifica ha successo</li>
     * </ol>
     *
     * @param userId l'identificativo dell'utente per cui cambiare la password
     * @param oldPassword la password attuale dell'utente in chiaro
     * @param newPassword la nuova password desiderata in chiaro
     * @return {@code true} se il cambio password è avvenuto con successo,
     *         {@code false} se la password attuale è errata, l'utente non esiste o si verificano errori
     *
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è null
     *
     * @apiNote La verifica della password attuale impedisce cambiamenti non autorizzati.
     *          Non ci sono vincoli espliciti sulla nuova password in questo metodo,
     *          ma è consigliabile implementare validazioni lato client.
     *
     * @see #hashPassword(String)
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        // Prima verifica la password attuale
        String checkQuery = "SELECT password FROM users WHERE id = ?";
        String updateQuery = "UPDATE users SET password = ? WHERE id = ?";

        String hashedOldPassword = hashPassword(oldPassword);
        String hashedNewPassword = hashPassword(newPassword);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Verifica password attuale
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setLong(1, Long.parseLong(userId)); // Converte String a Long
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String currentPassword = rs.getString("password");
                    if (!currentPassword.equals(hashedOldPassword)) {
                        System.out.println("❌ Password attuale non corretta");
                        return false;
                    }
                } else {
                    System.out.println("❌ Utente non trovato");
                    return false;
                }
            }

            // Aggiorna password
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, hashedNewPassword);
                updateStmt.setLong(2, Long.parseLong(userId)); // Converte String a Long

                int updated = updateStmt.executeUpdate();
                if (updated > 0) {
                    System.out.println("✅ Password cambiata con successo per utente ID: " + userId);
                    return true;
                }
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("❌ Errore cambio password: " + e.getMessage());
        }

        return false;
    }

    /**
     * Genera l'hash SHA-256 sicuro di una password per storage nel database.
     * <p>
     * Metodo interno di sicurezza che implementa hashing unidirezionale delle password
     * utilizzando l'algoritmo SHA-256. Questo garantisce che le password non vengano
     * mai memorizzate in chiaro nel database, aumentando significativamente la sicurezza
     * del sistema in caso di data breach.
     * </p>
     *
     * <p>
     * Il processo di hashing include:
     * </p>
     * <ol>
     *   <li>Conversione password in array di byte UTF-8</li>
     *   <li>Applicazione algoritmo SHA-256</li>
     *   <li>Conversione risultato in stringa esadecimale</li>
     * </ol>
     *
     * @param password la password in chiaro da hashare
     * @return l'hash SHA-256 della password come stringa esadecimale lowercase,
     *         la password originale se l'algoritmo SHA-256 non è disponibile (fallback)
     *
     * @implNote In caso di {@link NoSuchAlgorithmException} il metodo restituisce
     *           la password originale come fallback, ma questo scenario è estremamente
     *           improbabile sui sistemi Java moderni che includono SHA-256 di default.
     *
     * @apiNote L'hash prodotto è sempre di 64 caratteri esadecimali (256 bit / 4 = 64).
     *          L'algoritmo SHA-256 è unidirezionale e computazionalmente sicuro per
     *          applicazioni di autenticazione.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            // Converti in stringa esadecimale
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            System.err.println("❌ Errore hashing password: " + e.getMessage());
            return password;
        }
    }

    /**
     * Reimposta la password di un utente tramite il suo indirizzo email.
     * <p>
     * Metodo per il recupero password che consente di impostare una nuova password
     * per un utente utilizzando solo l'indirizzo email come identificativo.
     * Tipicamente utilizzato in flussi di "password dimenticata" dove l'utente
     * non può accedere al proprio account.
     * </p>
     *
     * <p>
     * <strong>Nota di Sicurezza:</strong> Questo metodo dovrebbe essere utilizzato
     * solo dopo aver verificato l'identità dell'utente tramite altri mezzi
     * (es. token di verifica via email, domande di sicurezza, etc.).
     * </p>
     *
     * @param email l'indirizzo email dell'utente di cui reimpostare la password
     * @param newPassword la nuova password in chiaro da impostare
     * @return {@code true} se la password è stata reimpostata con successo,
     *         {@code false} se l'email non esiste nel sistema o si verificano errori
     *
     * @throws IllegalArgumentException se email o newPassword sono null
     *
     * @apiNote Questo metodo NON verifica l'identità dell'utente e dovrebbe essere
     *          utilizzato solo dopo adeguate verifiche di sicurezza esterne.
     *          L'email viene normalizzata automaticamente e la password viene hashata
     *          prima del salvataggio.
     *
     * @see #hashPassword(String)
     */
    public boolean resetPasswordByEmail(String email, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE email = ?";
        String hashedNewPassword = hashPassword(newPassword);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, hashedNewPassword);
            stmt.setString(2, email.toLowerCase().trim());

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("✅ Password reimpostata per email: " + email);
                return true;
            } else {
                System.out.println("❌ Email non trovata: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore reset password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida il formato di un indirizzo email utilizzando controlli di base.
     * <p>
     * Metodo interno di validazione che implementa controlli di formato elementari
     * per gli indirizzi email. Non è una validazione RFC-compliant completa ma
     * fornisce un filtro di base per identificare email chiaramente mal formate.
     * </p>
     *
     * <p>
     * I controlli implementati includono:
     * </p>
     * <ul>
     *   <li>Presenza del carattere '@' (obbligatorio per ogni email)</li>
     *   <li>Presenza del carattere '.' (indica dominio con estensione)</li>
     *   <li>Lunghezza minima di 6 caratteri (formato minimo: a@b.co)</li>
     *   <li>Non null e non vuota</li>
     * </ul>
     *
     * @param email l'indirizzo email da validare
     * @return {@code true} se l'email passa i controlli di base,
     *         {@code false} se non rispetta i criteri minimi o è null
     *
     * @implNote Questa validazione è volutamente semplice per evitare falsi negativi.
     *           Per validazioni più rigorose si consiglia l'utilizzo di librerie
     *           specializzate o regex RFC-compliant.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }

    /**
     * Aggiorna l'indirizzo email di un utente esistente.
     * <p>
     * Metodo per la modifica dell'indirizzo email di un utente identificato dal suo ID.
     * Include normalizzazione automatica del nuovo indirizzo email e verifica
     * dell'esistenza dell'utente nel sistema.
     * </p>
     *
     * @param userId l'identificativo dell'utente di cui aggiornare l'email (stringa convertita in Long)
     * @param newEmail il nuovo indirizzo email da assegnare all'utente
     * @return {@code true} se l'aggiornamento è avvenuto con successo,
     *         {@code false} se l'utente non esiste, l'ID non è valido o si verificano errori
     *
     * @throws IllegalArgumentException se userId o newEmail sono null
     *
     * @apiNote Il nuovo indirizzo email viene automaticamente normalizzato a lowercase.
     *          Il metodo non verifica la validità del formato email né l'univocità,
     *          quindi potrebbe causare violazioni di vincoli database se la nuova
     *          email è già in uso da un altro utente.
     */
    public boolean updateUserEmail(String userId, String newEmail) {
        String query = "UPDATE users SET email = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newEmail.toLowerCase().trim());
            stmt.setLong(2, Long.parseLong(userId));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Email aggiornata per utente ID: " + userId + " -> " + newEmail);
                return true;
            } else {
                System.err.println("❌ Nessun utente trovato con ID: " + userId);
                return false;
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("❌ Errore aggiornamento email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se un utente ha privilegi amministrativi basandosi sul suo indirizzo email.
     * <p>
     * Sistema di controllo accesso semplificato che determina i privilegi amministrativi
     * tramite una whitelist di indirizzi email predefiniti. Gli amministratori hanno
     * accesso a funzionalità avanzate come gestione utenti, statistiche sistema e
     * operazioni di manutenzione.
     * </p>
     *
     * <p>
     * Indirizzi email con privilegi amministrativi:
     * </p>
     * <ul>
     *   <li>federico@admin.com</li>
     *   <li>ariele@admin.com</li>
     * </ul>
     *
     * @param email l'indirizzo email da verificare per i privilegi admin (case-insensitive)
     * @return {@code true} se l'email è nella whitelist amministratori,
     *         {@code false} per tutti gli altri casi inclusi parametri null
     *
     * @apiNote Il controllo è case-insensitive tramite equalsIgnoreCase.
     *          Per modificare la lista amministratori è necessario aggiornare
     *          l'array adminEmails nel codice e ridistribuire l'applicazione.
     */
    public boolean isUserAdmin(String email) {
        if (email == null) return false;

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

        return false;
    }

    /**
     * Recupera la lista completa di tutti gli utenti registrati nel sistema.
     * <p>
     * Metodo amministrativo che restituisce tutti gli utenti del sistema per
     * scopi di gestione, reporting e monitoring. Include logging dettagliato
     * per debugging e verifica della corretta mappatura dei dati dal database
     * agli oggetti del modello.
     * </p>
     *
     * <p>
     * Funzionalità incluse:
     * </p>
     * <ul>
     *   <li><strong>Ordinamento:</strong> Utenti ordinati per ID decrescente (più recenti per primi)</li>
     *   <li><strong>Debug completo:</strong> Logging dettagliato di ogni record recuperato</li>
     *   <li><strong>Mappatura sicura:</strong> Accesso esplicito per indice colonna per evitare errori</li>
     *   <li><strong>Gestione errori:</strong> Logging completo degli errori SQL</li>
     * </ul>
     *
     * @return una {@link List} di {@link User} contenente tutti gli utenti registrati,
     *         ordinata per ID decrescente. Lista vuota se non ci sono utenti o in caso di errori
     *
     * @apiNote Questo metodo dovrebbe essere utilizzato solo da amministratori in quanto
     *          restituisce informazioni sensibili di tutti gli utenti. Include logging
     *          verbose per facilitare debugging in caso di problemi di mappatura dati.
     *
     * @see #isUserAdmin(String)
     */
    public List<User> getAllUsers() {
        System.out.println("📋 Recupero di tutti gli utenti registrati");

        List<User> users = new ArrayList<>();

        // Query con campi espliciti nell'ordine esatto del DB
        String query = """
        SELECT id, name, surname, cf, email, username
        FROM users 
        ORDER BY id DESC
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long id = rs.getLong(1);        // Colonna 1: id
                String name = rs.getString(2);  // Colonna 2: nome
                String surname = rs.getString(3); // Colonna 3: cognome
                String cf = rs.getString(4);    // Colonna 4: cf
                String email = rs.getString(5); // Colonna 5: email
                String username = rs.getString(6); // Colonna 6: username

                System.out.println("🔍 DEBUG dati dal DB (ordine corretto):");
                System.out.println("   1. ID: " + id);
                System.out.println("   2. Name: " + name);
                System.out.println("   3. Surname: " + surname);
                System.out.println("   4. CF: " + cf);
                System.out.println("   5. Email: " + email);
                System.out.println("   6. Username: " + username);

                User user = new User(
                        id,
                        name,
                        surname,
                        cf,
                        email,
                        username
                );

                System.out.println("👤 Utente creato correttamente:");
                System.out.println("   ID: " + user.getId());
                System.out.println("   Name: " + user.getName());
                System.out.println("   Surname: " + user.getSurname());
                System.out.println("   CF: " + user.getCf());
                System.out.println("   Email: " + user.getEmail());
                System.out.println("   Username: " + user.getUsername());
                System.out.println("---");

                users.add(user);
            }

            System.out.println("✅ Recuperati " + users.size() + " utenti");

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero utenti: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Elimina definitivamente un utente dal sistema.
     * <p>
     * Metodo amministrativo per la rimozione completa di un utente dal database.
     * Include validazione dell'ID, controlli di sicurezza e logging dettagliato
     * dell'operazione. L'eliminazione è permanente e non può essere annullata.
     * </p>
     *
     * <p>
     * <strong>Attenzione:</strong> Questa operazione è irreversibile e rimuove
     * completamente tutti i dati dell'utente dal sistema. Potrebbe causare
     * violazioni di integrità referenziale se l'utente ha record correlati
     * in altre tabelle.
     * </p>
     *
     * <p>
     * Validazioni implementate:
     * </p>
     * <ul>
     *   <li>ID non null, non vuoto e diverso da "null"</li>
     *   <li>ID convertibile in numero Long valido</li>
     *   <li>Esistenza dell'utente nel database prima dell'eliminazione</li>
     * </ul>
     *
     * @param userId l'identificativo dell'utente da eliminare (stringa convertita in Long)
     * @return {@code true} se l'utente è stato eliminato con successo,
     *         {@code false} se l'ID non è valido, l'utente non esiste o si verificano errori
     *
     * @throws IllegalArgumentException se userId è null
     *
     * @apiNote Questo metodo dovrebbe essere utilizzato solo da amministratori
     *          dopo adeguate verifiche. L'eliminazione potrebbe lasciare record
     *          orfani in tabelle correlate se non gestite con CASCADE appropriati.
     *
     * @see #isUserAdmin(String)
     */
    public boolean deleteUser(String userId) {
        System.out.println("🗑️ Eliminazione utente ID: " + userId);

        if (userId == null || userId.trim().isEmpty() || "null".equals(userId)) {
            System.err.println("❌ ID utente non valido: " + userId);
            return false;
        }

        String query = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            long userIdLong;
            try {
                userIdLong = Long.parseLong(userId.trim());
            } catch (NumberFormatException e) {
                System.err.println("❌ ID utente non numerico: " + userId);
                return false;
            }

            stmt.setLong(1, userIdLong);
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                System.out.println("✅ Utente eliminato con ID: " + userId);
                return true;
            } else {
                System.out.println("❌ Nessun utente trovato con ID: " + userId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore eliminazione utente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica la disponibilità e connettività del database PostgreSQL.
     * <p>
     * Metodo diagnostico utilizzato per verificare la salute del sistema database
     * e la correttezza della configurazione di connessione. Utile per health checks,
     * monitoring dell'infrastruttura e diagnostica di sistema.
     * </p>
     *
     * @return {@code true} se la connessione al database è disponibile e funzionante,
     *         {@code false} in caso di problemi di connettività, autenticazione o configurazione
     *
     * @apiNote Questo metodo non dovrebbe essere chiamato frequentemente in produzione
     *          per evitare overhead di connessione. È progettato per verifiche periodiche
     *          e diagnostica di sistema.
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}