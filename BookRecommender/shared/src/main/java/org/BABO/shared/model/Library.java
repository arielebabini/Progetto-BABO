package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * La classe `Library` rappresenta una libreria personale di un utente.
 * <p>
 * È un modello unificato, condiviso tra client e server, per garantire una corretta
 * serializzazione e deserializzazione JSON dei dati relativi alle librerie.
 * </p>
 */
public class Library {

    /**
     * L'ID univoco che identifica l'elemento della libreria.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Lo username dell'utente che possiede la libreria.
     */
    @JsonProperty("username")
    private String username;

    /**
     * Il nome specifico dato alla libreria dall'utente.
     */
    @JsonProperty("namelib")
    private String namelib;

    /**
     * L'ISBN del libro contenuto in questa libreria.
     */
    @JsonProperty("isbn")
    private String isbn;

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public Library() {}

    /**
     * Costruttore per creare un nuovo elemento di libreria senza un ID predefinito.
     * <p>
     * Tipicamente usato quando un nuovo elemento viene aggiunto al sistema.
     * </p>
     * @param username Lo username dell'utente proprietario.
     * @param namelib Il nome della libreria.
     * @param isbn L'ISBN del libro da aggiungere.
     */
    public Library(String username, String namelib, String isbn) {
        this.username = username;
        this.namelib = namelib;
        this.isbn = isbn;
    }

    /**
     * Costruttore completo per inizializzare tutti i campi dell'oggetto.
     *
     * @param id L'ID univoco dell'elemento della libreria.
     * @param username Lo username dell'utente proprietario.
     * @param namelib Il nome della libreria.
     * @param isbn L'ISBN del libro.
     */
    public Library(Long id, String username, String namelib, String isbn) {
        this.id = id;
        this.username = username;
        this.namelib = namelib;
        this.isbn = isbn;
    }

    // Getters

    /**
     * Restituisce l'ID dell'elemento della libreria.
     * @return L'ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Restituisce lo username dell'utente proprietario.
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
     * Restituisce l'ISBN del libro.
     * @return L'ISBN.
     */
    public String getIsbn() {
        return isbn;
    }

    // Setters

    /**
     * Imposta l'ID dell'elemento della libreria.
     * @param id Il nuovo ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Imposta lo username dell'utente proprietario.
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
     * Imposta l'ISBN del libro.
     * @param isbn Il nuovo ISBN.
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `Library`.
     * @return La rappresentazione in stringa.
     */
    @Override
    public String toString() {
        return "Library{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", namelib='" + namelib + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }

    /**
     * Confronta questo oggetto `Library` con un altro per verificarne l'uguaglianza.
     * <p>
     * L'uguaglianza è basata unicamente sull'ID dell'elemento.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return id != null ? id.equals(library.id) : library.id == null;
    }

    /**
     * Restituisce il valore di hash per questo oggetto.
     * <p>
     * Il valore di hash è calcolato sull'ID.
     * </p>
     * @return Il valore di hash.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}