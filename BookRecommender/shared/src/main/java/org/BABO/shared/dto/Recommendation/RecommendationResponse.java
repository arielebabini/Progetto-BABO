package org.BABO.shared.dto.Recommendation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.model.Book;

import java.util.List;

/**
 * DTO per le risposte delle operazioni sulle raccomandazioni
 * Utilizzato per la comunicazione server-client
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("recommendation")
    private BookRecommendation recommendation;

    @JsonProperty("recommendations")
    private List<BookRecommendation> recommendations;

    @JsonProperty("recommendedBooks")
    private List<Book> recommendedBooks;

    @JsonProperty("canRecommend")
    private Boolean canRecommend;

    @JsonProperty("currentRecommendationsCount")
    private Integer currentRecommendationsCount;

    @JsonProperty("maxRecommendations")
    private Integer maxRecommendations;

    // Costruttori
    public RecommendationResponse() {}

    public RecommendationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public RecommendationResponse(boolean success, String message, BookRecommendation recommendation) {
        this.success = success;
        this.message = message;
        this.recommendation = recommendation;
    }

    public RecommendationResponse(boolean success, String message, List<BookRecommendation> recommendations) {
        this.success = success;
        this.message = message;
        this.recommendations = recommendations;
    }

    public RecommendationResponse(boolean success, String message, List<BookRecommendation> recommendations,
                                  List<Book> recommendedBooks) {
        this.success = success;
        this.message = message;
        this.recommendations = recommendations;
        this.recommendedBooks = recommendedBooks;
    }

    // Costruttore per verifiche di permessi
    public RecommendationResponse(boolean success, String message, Boolean canRecommend,
                                  Integer currentCount, Integer maxRecommendations) {
        this.success = success;
        this.message = message;
        this.canRecommend = canRecommend;
        this.currentRecommendationsCount = currentCount;
        this.maxRecommendations = maxRecommendations;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public BookRecommendation getRecommendation() {
        return recommendation;
    }

    public List<BookRecommendation> getRecommendations() {
        return recommendations;
    }

    public List<Book> getRecommendedBooks() {
        return recommendedBooks;
    }

    public Boolean getCanRecommend() {
        return canRecommend;
    }

    public Integer getCurrentRecommendationsCount() {
        return currentRecommendationsCount;
    }

    public Integer getMaxRecommendations() {
        return maxRecommendations;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecommendation(BookRecommendation recommendation) {
        this.recommendation = recommendation;
    }

    public void setRecommendations(List<BookRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public void setRecommendedBooks(List<Book> recommendedBooks) {
        this.recommendedBooks = recommendedBooks;
    }

    public void setCanRecommend(Boolean canRecommend) {
        this.canRecommend = canRecommend;
    }

    public void setCurrentRecommendationsCount(Integer currentRecommendationsCount) {
        this.currentRecommendationsCount = currentRecommendationsCount;
    }

    public void setMaxRecommendations(Integer maxRecommendations) {
        this.maxRecommendations = maxRecommendations;
    }

    // Metodi di utilità
    public boolean hasSingleRecommendation() {
        return recommendation != null;
    }

    public boolean hasMultipleRecommendations() {
        return recommendations != null && !recommendations.isEmpty();
    }

    public boolean hasRecommendedBooks() {
        return recommendedBooks != null && !recommendedBooks.isEmpty();
    }

    public int getRecommendationsCount() {
        return recommendations != null ? recommendations.size() : 0;
    }

    public int getRecommendedBooksCount() {
        return recommendedBooks != null ? recommendedBooks.size() : 0;
    }

    public boolean canAddMoreRecommendations() {
        if (canRecommend == null || !canRecommend) return false;
        if (maxRecommendations == null || currentRecommendationsCount == null) return true;
        return currentRecommendationsCount < maxRecommendations;
    }

    public int getRemainingRecommendationsSlots() {
        if (maxRecommendations == null || currentRecommendationsCount == null) return 0;
        return Math.max(0, maxRecommendations - currentRecommendationsCount);
    }

    public String getPermissionMessage() {
        if (canRecommend == null || !canRecommend) {
            return "Non puoi consigliare libri per questo titolo";
        }

        if (maxRecommendations != null && currentRecommendationsCount != null) {
            int remaining = getRemainingRecommendationsSlots();
            if (remaining <= 0) {
                return "Hai raggiunto il limite massimo di " + maxRecommendations + " raccomandazioni";
            } else {
                return "Puoi aggiungere ancora " + remaining + " raccomandazioni";
            }
        }

        return "Puoi consigliare libri per questo titolo";
    }

    @Override
    public String toString() {
        return "RecommendationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", recommendation=" + recommendation +
                ", recommendationsCount=" + getRecommendationsCount() +
                ", recommendedBooksCount=" + getRecommendedBooksCount() +
                ", canRecommend=" + canRecommend +
                ", currentRecommendationsCount=" + currentRecommendationsCount +
                ", maxRecommendations=" + maxRecommendations +
                '}';
    }
}