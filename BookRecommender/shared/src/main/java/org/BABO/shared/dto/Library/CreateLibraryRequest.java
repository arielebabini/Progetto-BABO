package org.BABO.shared.dto.Library;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le richieste di creazione di una nuova libreria
 */
public class CreateLibraryRequest {

    @JsonProperty("username")
    private String username;

    @JsonProperty("namelib")
    private String namelib;

    public CreateLibraryRequest() {}

    public CreateLibraryRequest(String username, String namelib) {
        this.username = username;
        this.namelib = namelib;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getNamelib() {
        return namelib;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setNamelib(String namelib) {
        this.namelib = namelib;
    }

    @Override
    public String toString() {
        return "CreateLibraryRequest{" +
                "username='" + username + '\'' +
                ", namelib='" + namelib + '\'' +
                '}';
    }
}