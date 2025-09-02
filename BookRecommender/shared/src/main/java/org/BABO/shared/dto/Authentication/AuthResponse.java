package org.BABO.shared.dto.Authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) che incapsula la risposta del server per le operazioni di autenticazione.
 * <p>
 * Questa classe è un modello standard per la comunicazione client-server,
 * fornendo un feedback sullo stato dell'operazione di login o registrazione,
 * un messaggio descrittivo e, in caso di successo, un token di autenticazione
 * e i dati dell'utente autenticato.
 * </p>
 */
public class AuthResponse {

    /**
     * Indica se l'operazione di autenticazione ha avuto successo.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Un messaggio di testo che fornisce dettagli sul risultato dell'operazione (es. "Login riuscito" o "Credenziali non valide").
     */
    @JsonProperty("message")
    private String message;

    /**
     * Un token di autenticazione, come un JWT, che l'utente può utilizzare per richieste future.
     * Questo campo è opzionale e dipende dall'implementazione.
     */
    @JsonProperty("token")
    private String token; // Per future implementazioni JWT

    /**
     * L'oggetto utente {@link org.BABO.shared.model.User} contenente i dati dell'utente autenticato.
     */
    @JsonProperty("user")
    private org.BABO.shared.model.User user;

    // Costruttori

    /**
     * Costruttore di default. Necessario per la deserializzazione JSON.
     */
    public AuthResponse() {}

    /**
     * Costruttore per una risposta base con solo successo e messaggio.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     */
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Costruttore per una risposta che include lo stato di successo, un messaggio e i dati dell'utente.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param user L'oggetto utente.
     */
    public AuthResponse(boolean success, String message, org.BABO.shared.model.User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    /**
     * Costruttore completo che include tutti i campi della risposta.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param token Il token di autenticazione.
     * @param user L'oggetto utente.
     */
    public AuthResponse(boolean success, String message, String token, org.BABO.shared.model.User user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // Getters

    /**
     * Restituisce lo stato di successo della risposta.
     * @return {@code true} se l'autenticazione è andata a buon fine.
     */
    public boolean isSuccess() { return success; }

    /**
     * Restituisce il messaggio di feedback.
     * @return Il messaggio.
     */
    public String getMessage() { return message; }

    /**
     * Restituisce il token di autenticazione.
     * @return Il token, o {@code null}.
     */
    public String getToken() { return token; }

    /**
     * Restituisce l'oggetto utente.
     * @return L'oggetto {@link org.BABO.shared.model.User}, o {@code null}.
     */
    public org.BABO.shared.model.User getUser() { return user; }

    // Setters

    /**
     * Imposta lo stato di successo.
     * @param success Il nuovo stato.
     */
    public void setSuccess(boolean success) { this.success = success; }

    /**
     * Imposta il messaggio di feedback.
     * @param message Il nuovo messaggio.
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Imposta il token di autenticazione.
     * @param token Il nuovo token.
     */
    public void setToken(String token) { this.token = token; }

    /**
     * Imposta l'oggetto utente.
     * @param user Il nuovo oggetto utente.
     */
    public void setUser(org.BABO.shared.model.User user) { this.user = user; }
}