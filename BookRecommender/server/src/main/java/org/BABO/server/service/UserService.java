package org.BABO.server.service;

import org.BABO.shared.model.User;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per gestire le operazioni sugli utenti
 * Include autenticazione, registrazione e gestione profili
 * AGGIORNATO per compatibilità String/Long ID
 */
@Service
public class UserService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DataProva";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgress";

    /**
     * Autentica un utente con email e password
     * AGGIORNATO per compatibilità con il nuovo modello User
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
                // COSTRUTTORE CORRETTO: Long id, String name, String surname, String cf, String email, String username
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
     * Registra un nuovo utente
     * AGGIORNATO per compatibilità con il nuovo modello User
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
                // COSTRUTTORE CORRETTO: Long id, String name, String surname, String cf, String email, String username
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
     * Controlla se un utente esiste già con la stessa email o username
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
     * Recupera un utente per ID
     */
    public User getUserById(String userId) {
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, Long.parseLong(userId)); // Converte String a Long per il DB
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // COSTRUTTORE CORRETTO: Long id, String name, String surname, String cf, String email, String username
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
     * Recupera un utente per email
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
     * Aggiorna il profilo dell'utente
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
                // COSTRUTTORE CORRETTO: Long id, String name, String surname, String cf, String email, String username
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
     * Cambia la password dell'utente
     * AGGIORNATO per compatibilità String userId
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
     * Recupera tutti gli utenti (solo per admin)
     * AGGIORNATO per compatibilità con il nuovo modello User
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY name, surname";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                // COSTRUTTORE CORRETTO: Long id, String name, String surname, String cf, String email, String username
                User user = new User(
                        rs.getLong("id"),           // Long id
                        rs.getString("name"),       // String name
                        rs.getString("surname"),    // String surname
                        rs.getString("cf"),         // String cf
                        rs.getString("email"),      // String email
                        rs.getString("username")    // String username
                );
                users.add(user);
            }

            System.out.println("📊 Recuperati " + users.size() + " utenti");

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero utenti: " + e.getMessage());
        }

        return users;
    }

    /**
     * Elimina un utente (aggiornato per String userId)
     */
    public boolean deleteUser(String userId) {
        String query = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, Long.parseLong(userId)); // Converte String a Long
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                System.out.println("🗑️ Utente eliminato con ID: " + userId);
                return true;
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("❌ Errore eliminazione utente: " + e.getMessage());
        }

        return false;
    }

    /**
     * Hash della password usando SHA-256
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
            return password; // Fallback non sicuro, solo per test
        }
    }

    /**
     * Reset password tramite email
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
     * Validazione email semplice
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }

    /**
     * Controlla se il database è disponibile
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}