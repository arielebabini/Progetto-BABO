package org.BABO.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le richieste di aggiunta di un libro a una libreria
 */
public class AddBookToLibraryRequest {

    @JsonProperty("username")
    private String username;

    @JsonProperty("namelib")
    private String namelib;

    @JsonProperty("isbn")
    private String isbn;

    public AddBookToLibraryRequest() {}

    public AddBookToLibraryRequest(String username, String namelib, String isbn) {
        this.username = username;
        this.namelib = namelib;
        this.isbn = isbn;
    }

    // Getters
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
        return "AddBookToLibraryRequest{" +
                "username='" + username + '\'' +
                ", namelib='" + namelib + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }
}