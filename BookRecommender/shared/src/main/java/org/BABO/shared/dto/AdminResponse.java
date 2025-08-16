package org.BABO.shared.dto;

import org.BABO.shared.model.User;

import java.util.List;

public class  AdminResponse {
    private final boolean success;
    private final String message;
    private final List<User> users;

    public AdminResponse(boolean success, String message, List<User> users) {
        this.success = success;
        this.message = message;
        this.users = users;
    }

    public AdminResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.users = null; // Non ci sono utenti per operazioni come delete
    }

    // Getters rimangono uguali
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<User> getUsers() { return users; }
}
