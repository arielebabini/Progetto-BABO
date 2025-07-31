package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe che rappresenta una libreria personale dell'utente
 * Condivisa tra client e server per la serializzazione JSON
 */
public class Library {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username; // Username dell'utente proprietario della libreria

    @JsonProperty("namelib")
    private String namelib; // Nome della libreria

    @JsonProperty("isbn")
    private String isbn; // ISBN del libro contenuto nella libreria

    // Costruttori
    public Library() {}

    public Library(String username, String namelib, String isbn) {
        this.username = username;
        this.namelib = namelib;
        this.isbn = isbn;
    }

    public Library(Long id, String username, String namelib, String isbn) {
        this.id = id;
        this.username = username;
        this.namelib = namelib;
        this.isbn = isbn;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNamelib() {
        return namelib;
    }

    public String getIsbn() {
        return isbn;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNamelib(String namelib) {
        this.namelib = namelib;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return "Library{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", namelib='" + namelib + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return id != null ? id.equals(library.id) : library.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}