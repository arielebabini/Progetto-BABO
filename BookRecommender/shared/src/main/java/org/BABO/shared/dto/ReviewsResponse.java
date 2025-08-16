package org.BABO.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.Review;
import java.util.List;

/**
 * Classe response per le operazioni sulle recensioni (admin)
 */
public class ReviewsResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("reviews")
    private List<Review> reviews;

    @JsonProperty("totalReviews")
    private int totalReviews;

    @JsonProperty("totalUsers")
    private int totalUsers;

    @JsonProperty("averageRating")
    private double averageRating;

    // Costruttori
    public ReviewsResponse() {}

    public ReviewsResponse(boolean success, String message, List<Review> reviews) {
        this.success = success;
        this.message = message;
        this.reviews = reviews;
        if (reviews != null) {
            this.totalReviews = reviews.size();
        }
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

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        if (reviews != null) {
            this.totalReviews = reviews.size();
        }
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}

