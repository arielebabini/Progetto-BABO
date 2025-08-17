package org.BABO.shared.dto.Authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le richieste di registrazione
 */
public class RegisterRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("cf")
    private String cf;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String name, String surname, String cf, String email, String username, String password) {
        this.name = name;
        this.surname = surname;
        this.cf = cf;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getCf() { return cf; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setCf(String cf) { this.cf = cf; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}