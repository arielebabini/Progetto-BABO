package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe che rappresenta un utente del sistema
 * Condivisa tra client e server per la serializzazione JSON
 * VERSIONE UNIFICATA per compatibilità client-server
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty("id")
    private String id; // Unificato come String per compatibilità

    @JsonProperty("name")
    private String name;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("cf")
    private String cf;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password; // Non viene serializzato per sicurezza

    // Costruttori
    public User() {}

    // Costruttore per compatibilità con il server (Long id convertito)
    public User(Long id, String name, String surname, String cf, String email, String username) {
        this.id = id != null ? id.toString() : null;
        this.name = name;
        this.surname = surname;
        this.cf = cf;
        this.email = email;
        this.username = username;
    }

    // Costruttore completo con password (per registrazione)
    public User(String name, String surname, String cf, String email, String username, String password) {
        this.name = name;
        this.surname = surname;
        this.cf = cf;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getCf() { return cf; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setCf(String cf) { this.cf = cf; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    // Metodi di utilità
    public String getFullName() {
        return (name != null ? name : "") + " " + (surname != null ? surname : "");
    }

    public String getDisplayName() {
        if (name != null && surname != null && !name.trim().isEmpty() && !surname.trim().isEmpty()) {
            return name.trim() + " " + surname.trim();
        } else if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        } else if (username != null && !username.trim().isEmpty()) {
            return username;
        } else if (email != null && !email.trim().isEmpty()) {
            return email;
        } else {
            return "Utente";
        }
    }

    // Metodi helper per compatibilità server
    public Long getIdAsLong() {
        try {
            return id != null ? Long.parseLong(id) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setIdFromLong(Long id) {
        this.id = id != null ? id.toString() : null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", cf='" + cf + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null ? id.equals(user.id) : user.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}