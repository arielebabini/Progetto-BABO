package org.BABO.shared.dto.Authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) che incapsula una richiesta dal client per la registrazione di un nuovo utente.
 * <p>
 * Questa classe definisce la struttura dei dati necessaria per inviare al server
 * le informazioni di registrazione, inclusi i dati personali e le credenziali di accesso.
 * </p>
 */
public class RegisterRequest {

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
     * Lo username scelto dall'utente per il login.
     */
    @JsonProperty("username")
    private String username;

    /**
     * La password scelta dall'utente.
     */
    @JsonProperty("password")
    private String password;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public RegisterRequest() {}

    /**
     * Costruttore completo per inizializzare tutti i campi della richiesta.
     *
     * @param name Il nome dell'utente.
     * @param surname Il cognome dell'utente.
     * @param cf Il codice fiscale.
     * @param email L'indirizzo email.
     * @param username Lo username scelto.
     * @param password La password.
     */
    public RegisterRequest(String name, String surname, String cf, String email, String username, String password) {
        this.name = name;
        this.surname = surname;
        this.cf = cf;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getters

    /**
     * Restituisce il nome dell'utente.
     * @return Il nome.
     */
    public String getName() { return name; }

    /**
     * Restituisce il cognome dell'utente.
     * @return Il cognome.
     */
    public String getSurname() { return surname; }

    /**
     * Restituisce il codice fiscale dell'utente.
     * @return Il codice fiscale.
     */
    public String getCf() { return cf; }

    /**
     * Restituisce l'indirizzo email dell'utente.
     * @return L'email.
     */
    public String getEmail() { return email; }

    /**
     * Restituisce lo username scelto.
     * @return Lo username.
     */
    public String getUsername() { return username; }

    /**
     * Restituisce la password.
     * @return La password.
     */
    public String getPassword() { return password; }

    // Setters

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
     * @param email La nuova email.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Imposta lo username.
     * @param username Il nuovo username.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Imposta la password.
     * @param password La nuova password.
     */
    public void setPassword(String password) { this.password = password; }
}