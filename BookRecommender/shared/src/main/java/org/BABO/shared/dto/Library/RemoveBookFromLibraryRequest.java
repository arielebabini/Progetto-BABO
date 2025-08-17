package org.BABO.shared.dto.Library;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le richieste di rimozione di un libro da una libreria
 */
public class RemoveBookFromLibraryRequest {

    @JsonProperty("username")
    private String username;

    @JsonProperty("namelib")
    private String namelib;

    @JsonProperty("isbn")
    private String isbn;

    public RemoveBookFromLibraryRequest() {}

    public RemoveBookFromLibraryRequest(String username, String namelib, String isbn) {
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
        return "RemoveBookFromLibraryRequest{" +
                "username='" + username + '\'' +
                ", namelib='" + namelib + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }
}