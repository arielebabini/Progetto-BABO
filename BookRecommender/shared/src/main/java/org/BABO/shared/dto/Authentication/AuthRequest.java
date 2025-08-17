package org.BABO.shared.dto.Authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le richieste di autenticazione (login)
 */
public class AuthRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    public AuthRequest() {}

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}