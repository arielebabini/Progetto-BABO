package org.BABO.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le risposte di autenticazione
 */
public class AuthResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("token")
    private String token; // Per future implementazioni JWT

    @JsonProperty("user")
    private org.BABO.shared.model.User user;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, org.BABO.shared.model.User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public AuthResponse(boolean success, String message, String token, org.BABO.shared.model.User user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public org.BABO.shared.model.User getUser() { return user; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setToken(String token) { this.token = token; }
    public void setUser(org.BABO.shared.model.User user) { this.user = user; }
}