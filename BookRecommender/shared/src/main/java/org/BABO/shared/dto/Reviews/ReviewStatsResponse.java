package org.BABO.shared.dto.Reviews;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewStatsResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("stats")
    private ReviewStats stats;

    // Costruttori
    public ReviewStatsResponse() {}

    public ReviewStatsResponse(boolean success, String message, ReviewStats stats) {
        this.success = success;
        this.message = message;
        this.stats = stats;
    }

    // Getters e Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ReviewStats getStats() {
        return stats;
    }

    public void setStats(ReviewStats stats) {
        this.stats = stats;
    }
}
