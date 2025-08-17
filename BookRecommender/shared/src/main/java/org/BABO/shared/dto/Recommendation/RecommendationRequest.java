package org.BABO.shared.dto.Recommendation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le richieste di raccomandazioni
 * Utilizzato per la comunicazione client-server
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationRequest {

    @JsonProperty("username")
    private String username;

    @JsonProperty("targetBookIsbn")
    private String targetBookIsbn;

    @JsonProperty("recommendedBookIsbn")
    private String recommendedBookIsbn;

    @JsonProperty("reason")
    private String reason;

    // Costruttori
    public RecommendationRequest() {}

    public RecommendationRequest(String username, String targetBookIsbn, String recommendedBookIsbn) {
        this.username = username;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
    }

    public RecommendationRequest(String username, String targetBookIsbn, String recommendedBookIsbn, String reason) {
        this.username = username;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.reason = reason;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getTargetBookIsbn() {
        return targetBookIsbn;
    }

    public String getRecommendedBookIsbn() {
        return recommendedBookIsbn;
    }

    public String getReason() {
        return reason;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setTargetBookIsbn(String targetBookIsbn) {
        this.targetBookIsbn = targetBookIsbn;
    }

    public void setRecommendedBookIsbn(String recommendedBookIsbn) {
        this.recommendedBookIsbn = recommendedBookIsbn;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Metodi di validazione
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                targetBookIsbn != null && !targetBookIsbn.trim().isEmpty() &&
                recommendedBookIsbn != null && !recommendedBookIsbn.trim().isEmpty() &&
                !targetBookIsbn.equals(recommendedBookIsbn);
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();

        if (username == null || username.trim().isEmpty()) {
            errors.append("Username è obbligatorio. ");
        }

        if (targetBookIsbn == null || targetBookIsbn.trim().isEmpty()) {
            errors.append("ISBN del libro target è obbligatorio. ");
        }

        if (recommendedBookIsbn == null || recommendedBookIsbn.trim().isEmpty()) {
            errors.append("ISBN del libro consigliato è obbligatorio. ");
        }

        if (targetBookIsbn != null && recommendedBookIsbn != null &&
                targetBookIsbn.equals(recommendedBookIsbn)) {
            errors.append("Non puoi consigliare lo stesso libro. ");
        }

        if (reason != null && reason.length() > 500) {
            errors.append("Il motivo non può superare i 500 caratteri. ");
        }

        return errors.toString().trim();
    }

    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    public String getCleanReason() {
        if (reason == null) return null;

        String cleaned = reason.trim();
        if (cleaned.isEmpty()) return null;

        // Limita la lunghezza del motivo (500 caratteri)
        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 500) + "...";
        }

        return cleaned;
    }

    @Override
    public String toString() {
        return "RecommendationRequest{" +
                "username='" + username + '\'' +
                ", targetBookIsbn='" + targetBookIsbn + '\'' +
                ", recommendedBookIsbn='" + recommendedBookIsbn + '\'' +
                ", reason='" + (reason != null ? reason.substring(0, Math.min(50, reason.length())) + "..." : "null") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationRequest that = (RecommendationRequest) o;
        return username != null && username.equals(that.username) &&
                targetBookIsbn != null && targetBookIsbn.equals(that.targetBookIsbn) &&
                recommendedBookIsbn != null && recommendedBookIsbn.equals(that.recommendedBookIsbn);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (targetBookIsbn != null ? targetBookIsbn.hashCode() : 0);
        result = 31 * result + (recommendedBookIsbn != null ? recommendedBookIsbn.hashCode() : 0);
        return result;
    }
}