package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * La classe `User` rappresenta un utente all'interno del sistema.
 * <p>
 * È una classe unificata, condivisa tra client e server, per garantire una corretta
 * serializzazione e deserializzazione JSON. La classe è annotata con
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} per ignorare
 * eventuali proprietà JSON non mappate, garantendo la compatibilità in caso di
 * evoluzione del modello dati.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    /**
     * L'ID dell'utente. Viene unificato come {@code String} per garantire
     * la compatibilità tra client e server.
     */
    @JsonProperty("id")
    private String id;

    /**
     * Il nome dell'utente.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Il cognome dell'utente.
     */
    @JsonProperty("surname")
    private String surname;

    /**
     * Il codice fiscale dell'utente.
     */
    @JsonProperty("cf")
    private String cf;

    /**
     * L'indirizzo email dell'utente.
     */
    @JsonProperty("email")
    private String email;

    /**
     * Lo username dell'utente, utilizzato per l'accesso.
     */
    @JsonProperty("username")
    private String username;

    /**
     * La password dell'utente. Questa proprietà non viene serializzata
     * per motivi di sicurezza.
     */
    @JsonProperty("password")
    private String password;

    /**
     * La data e ora di creazione dell'utente.
     */
    private LocalDateTime createdAt;

    /**
     * Costruttore di default.
     * <p>
     * È richiesto dalla libreria di serializzazione Jackson per la deserializzazione JSON.
     * </p>
     */
    public User() {}

    /**
     * Costruttore utilizzato per la compatibilità con il server.
     * <p>
     * L'ID, che può essere di tipo {@code Long} lato server, viene
     * convertito in {@code String} per coerenza con il modello unificato.
     * </p>
     * @param id L'ID dell'utente come {@code Long}, convertito in {@code String}.
     * @param name Il nome dell'utente.
     * @param surname Il cognome dell'utente.
     * @param cf Il codice fiscale dell'utente.
     * @param email L'indirizzo email dell'utente.
     * @param username Lo username dell'utente.
     */
    public User(Long id, String name, String surname, String cf, String email, String username) {
        this.id = id != null ? id.toString() : null;
        this.name = name;
        this.surname = surname;
        this.cf = cf;
        this.email = email;
        this.username = username;
    }

    /**
     * Costruttore completo, utilizzato tipicamente per la registrazione di un nuovo utente.
     *
     * @param name Il nome dell'utente.
     * @param surname Il cognome dell'utente.
     * @param cf Il codice fiscale dell'utente.
     * @param email L'indirizzo email dell'utente.
     * @param username Lo username dell'utente.
     * @param password La password dell'utente.
     */
    public User(String name, String surname, String cf, String email, String username, String password) {
        this.name = name;
        this.surname = surname;
        this.cf = cf;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    /**
     * Restituisce l'ID dell'utente.
     * @return L'ID dell'utente come {@code String}.
     */
    public String getId() { return id; }

    /**
     * Restituisce il nome dell'utente.
     * @return Il nome dell'utente.
     */
    public String getName() { return name; }

    /**
     * Restituisce il cognome dell'utente.
     * @return Il cognome dell'utente.
     */
    public String getSurname() { return surname; }

    /**
     * Restituisce il codice fiscale dell'utente.
     * @return Il codice fiscale dell'utente.
     */
    public String getCf() { return cf; }

    /**
     * Restituisce l'indirizzo email dell'utente.
     * @return L'indirizzo email dell'utente.
     */
    public String getEmail() { return email; }

    /**
     * Restituisce lo username dell'utente.
     * @return Lo username dell'utente.
     */
    public String getUsername() { return username; }

    /**
     * Restituisce la password dell'utente.
     * @return La password dell'utente.
     */
    public String getPassword() { return password; }

    /**
     * Imposta l'ID dell'utente.
     * @param id Il nuovo ID come {@code String}.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Imposta il nome dell'utente.
     * @param name Il nuovo nome.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Imposta il cognome dell'utente.
     * @param surname Il nuovo cognome.
     */
    public void setSurname(String surname) { this.surname = surname; }

    /**
     * Imposta il codice fiscale dell'utente.
     * @param cf Il nuovo codice fiscale.
     */
    public void setCf(String cf) { this.cf = cf; }

    /**
     * Imposta l'indirizzo email dell'utente.
     * @param email Il nuovo indirizzo email.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Imposta lo username dell'utente.
     * @param username Il nuovo username.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Imposta la password dell'utente.
     * @param password La nuova password.
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Restituisce il nome completo dell'utente, combinando nome e cognome.
     * Se uno dei due campi è nullo, viene restituita una stringa con l'altro campo o vuota.
     * @return Il nome completo, formattato come "nome cognome".
     */
    public String getFullName() {
        return (name != null ? name : "") + " " + (surname != null ? surname : "");
    }

    /**
     * Restituisce un nome visualizzabile per l'utente, scegliendo tra
     * nome completo, solo nome, username, email, o un valore di default.
     * <p>
     * La priorità è la seguente:
     * <ol>
     * <li>Nome e cognome (se entrambi non vuoti)</li>
     * <li>Solo nome (se non vuoto)</li>
     * <li>Username (se non vuoto)</li>
     * <li>Email (se non vuota)</li>
     * <li>"Utente" (come fallback)</li>
     * </ol>
     * </p>
     * @return La stringa visualizzabile che identifica l'utente.
     */
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

    /**
     * Restituisce l'ID dell'utente come un oggetto {@code Long}.
     * <p>
     * Questo metodo è utile per garantire la compatibilità con il
     * sistema di persistenza del server che potrebbe utilizzare {@code Long}
     * come tipo di dato per l'ID.
     * </p>
     * @return L'ID convertito in {@code Long}, o {@code null} se non è un numero valido.
     */
    public Long getIdAsLong() {
        try {
            return id != null ? Long.parseLong(id) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Imposta l'ID dell'utente a partire da un oggetto {@code Long}.
     * <p>
     * Questo metodo supporta la conversione da {@code Long} a {@code String}
     * per mantenere l'ID come {@code String} nel modello unificato.
     * </p>
     * @param id L'ID come {@code Long}.
     */
    public void setIdFromLong(Long id) {
        this.id = id != null ? id.toString() : null;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `User`.
     * <p>
     * La password viene esclusa per motivi di sicurezza.
     * </p>
     * @return La rappresentazione in stringa dell'utente.
     */
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

    /**
     * Confronta questo oggetto `User` con un altro per verificarne l'uguaglianza.
     * <p>
     * L'uguaglianza è basata sull'ID dell'utente. Due utenti sono considerati
     * uguali se hanno lo stesso ID.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null ? id.equals(user.id) : user.id == null;
    }

    /**
     * Restituisce il valore di hash per questo oggetto.
     * <p>
     * Il valore di hash è basato sull'ID dell'utente.
     * </p>
     * @return Il valore di hash.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * Restituisce la data e ora di creazione dell'utente.
     * @return La data e ora di creazione.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Imposta la data e ora di creazione dell'utente.
     * @param createdAt La nuova data e ora di creazione.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}