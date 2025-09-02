package org.BABO.shared.dto.Authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) che incapsula una richiesta dal client per l'autenticazione (login).
 * <p>
 * Questa classe è utilizzata per inviare al server le credenziali di accesso dell'utente,
 * ovvero l'indirizzo email e la password. È un modello standard per le richieste di login.
 * </p>
 */
public class AuthRequest {

    /**
     * L'indirizzo email dell'utente, che funge da identificativo per il login.
     */
    @JsonProperty("email")
    private String email;

    /**
     * La password dell'utente.
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
    public AuthRequest() {}

    /**
     * Costruttore completo per inizializzare i campi di email e password.
     *
     * @param email L'indirizzo email dell'utente.
     * @param password La password dell'utente.
     */
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters

    /**
     * Restituisce l'indirizzo email.
     * @return L'indirizzo email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Restituisce la password.
     * @return La password.
     */
    public String getPassword() {
        return password;
    }

    // Setters

    /**
     * Imposta l'indirizzo email.
     * @param email Il nuovo indirizzo email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Imposta la password.
     * @param password La nuova password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}