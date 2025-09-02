package org.BABO.shared.dto.Library;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) che incapsula una richiesta dal client per creare una nuova libreria.
 * <p>
 * Questa classe definisce la struttura dei dati necessaria per comunicare al server
 * il nome della nuova libreria da creare e a quale utente associarla.
 * </p>
 */
public class CreateLibraryRequest {

    /**
     * Lo username dell'utente che intende creare la libreria.
     */
    @JsonProperty("username")
    private String username;

    /**
     * Il nome che si desidera dare alla nuova libreria.
     */
    @JsonProperty("namelib")
    private String namelib;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public CreateLibraryRequest() {}

    /**
     * Costruttore completo per inizializzare tutti i campi della richiesta.
     *
     * @param username Lo username dell'utente.
     * @param namelib Il nome della libreria da creare.
     */
    public CreateLibraryRequest(String username, String namelib) {
        this.username = username;
        this.namelib = namelib;
    }

    // Getters

    /**
     * Restituisce lo username dell'utente.
     * @return Lo username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce il nome della libreria.
     * @return Il nome della libreria.
     */
    public String getNamelib() {
        return namelib;
    }

    // Setters

    /**
     * Imposta lo username dell'utente.
     * @param username Il nuovo username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Imposta il nome della libreria.
     * @param namelib Il nuovo nome.
     */
    public void setNamelib(String namelib) {
        this.namelib = namelib;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto {@code CreateLibraryRequest}.
     * <p>
     * Utile per scopi di debugging, mostra i valori dei campi.
     * </p>
     * @return La stringa descrittiva.
     */
    @Override
    public String toString() {
        return "CreateLibraryRequest{" +
                "username='" + username + '\'' +
                ", namelib='" + namelib + '\'' +
                '}';
    }
}