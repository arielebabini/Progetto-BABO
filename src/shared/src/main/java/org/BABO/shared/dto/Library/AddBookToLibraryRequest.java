package org.BABO.shared.dto.Library;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) che incapsula una richiesta dal client per aggiungere un libro a una libreria.
 * <p>
 * Questa classe definisce la struttura dei dati necessaria per comunicare al server
 * il libro (identificato dal suo ISBN) e la libreria di destinazione (identificata
 * da username e nome della libreria) a cui deve essere aggiunto.
 * </p>
 */
public class AddBookToLibraryRequest {

    /**
     * Lo username dell'utente proprietario della libreria.
     */
    @JsonProperty("username")
    private String username;

    /**
     * Il nome della libreria a cui aggiungere il libro.
     */
    @JsonProperty("namelib")
    private String namelib;

    /**
     * L'ISBN del libro da aggiungere.
     */
    @JsonProperty("isbn")
    private String isbn;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public AddBookToLibraryRequest() {}

    /**
     * Costruttore completo per inizializzare tutti i campi della richiesta.
     *
     * @param username Lo username dell'utente.
     * @param namelib Il nome della libreria.
     * @param isbn L'ISBN del libro da aggiungere.
     */
    public AddBookToLibraryRequest(String username, String namelib, String isbn) {
        this.username = username;
        this.namelib = namelib;
        this.isbn = isbn;
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

    /**
     * Restituisce l'ISBN del libro da aggiungere.
     * @return L'ISBN del libro.
     */
    public String getIsbn() {
        return isbn;
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
     * Imposta l'ISBN del libro da aggiungere.
     * @param isbn Il nuovo ISBN.
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto {@code AddBookToLibraryRequest}.
     * <p>
     * Utile per scopi di debugging, mostra i valori dei campi.
     * </p>
     * @return La stringa descrittiva.
     */
    @Override
    public String toString() {
        return "AddBookToLibraryRequest{" +
                "username='" + username + '\'' +
                ", namelib='" + namelib + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }
}