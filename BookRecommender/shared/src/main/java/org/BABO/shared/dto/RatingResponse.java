package org.BABO.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.BookRating;

import java.util.List;

/**
 * DTO per le risposte delle operazioni sulle valutazioni
 * Utilizzato per la comunicazione server-client
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("rating")
    private BookRating rating;           // Singola valutazione

    @JsonProperty("ratings")
    private List<BookRating> ratings;    // Lista di valutazioni

    @JsonProperty("averageRating")
    private Double averageRating;        // Media delle valutazioni per un libro

    @JsonProperty("totalRatings")
    private Integer totalRatings;        // Numero totale di valutazioni

    @JsonProperty("ratingBreakdown")
    private RatingBreakdown breakdown;   // Dettaglio distribuzione voti

    // Costruttori
    public RatingResponse() {}

    public RatingResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public RatingResponse(boolean success, String message, BookRating rating) {
        this.success = success;
        this.message = message;
        this.rating = rating;
    }

    public RatingResponse(boolean success, String message, List<BookRating> ratings) {
        this.success = success;
        this.message = message;
        this.ratings = ratings;
        if (ratings != null) {
            this.totalRatings = ratings.size();
        }
    }

    public RatingResponse(boolean success, String message, List<BookRating> ratings, Double averageRating) {
        this(success, message, ratings);
        this.averageRating = averageRating;
    }

    // Costruttore per statistiche complete
    public RatingResponse(boolean success, String message, List<BookRating> ratings,
                          Double averageRating, RatingBreakdown breakdown) {
        this(success, message, ratings, averageRating);
        this.breakdown = breakdown;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public BookRating getRating() {
        return rating;
    }

    public List<BookRating> getRatings() {
        return ratings;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getTotalRatings() {
        return totalRatings;
    }

    public RatingBreakdown getBreakdown() {
        return breakdown;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRating(BookRating rating) {
        this.rating = rating;
    }

    public void setRatings(List<BookRating> ratings) {
        this.ratings = ratings;
        if (ratings != null) {
            this.totalRatings = ratings.size();
        }
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public void setTotalRatings(Integer totalRatings) {
        this.totalRatings = totalRatings;
    }

    public void setBreakdown(RatingBreakdown breakdown) {
        this.breakdown = breakdown;
    }

    // Metodi di utilità

    /**
     * Verifica se contiene una singola valutazione
     */
    public boolean hasSingleRating() {
        return rating != null;
    }

    /**
     * Verifica se contiene multiple valutazioni
     */
    public boolean hasMultipleRatings() {
        return ratings != null && !ratings.isEmpty();
    }

    /**
     * Verifica se contiene statistiche
     */
    public boolean hasStatistics() {
        return averageRating != null || breakdown != null;
    }

    /**
     * Restituisce il numero di stelle basato sulla media
     */
    public int getStarRating() {
        if (averageRating == null || averageRating <= 0) return 0;
        return (int) Math.round(averageRating);
    }

    /**
     * Restituisce una rappresentazione testuale della valutazione media
     */
    public String getDisplayRating() {
        if (averageRating == null || averageRating <= 0) {
            return "Non valutato";
        }

        String stars = "★".repeat(getStarRating()) + "☆".repeat(5 - getStarRating());
        return String.format("%s (%.1f/5)", stars, averageRating);
    }

    /**
     * Restituisce una descrizione della qualità basata sulla media
     */
    public String getQualityDescription() {
        if (averageRating == null || averageRating <= 0) return "Non valutato";

        if (averageRating >= 4.5) return "Eccellente";
        else if (averageRating >= 4.0) return "Molto buono";
        else if (averageRating >= 3.5) return "Buono";
        else if (averageRating >= 3.0) return "Discreto";
        else if (averageRating >= 2.5) return "Sufficiente";
        else if (averageRating >= 2.0) return "Mediocre";
        else return "Scarso";
    }

    @Override
    public String toString() {
        return "RatingResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", rating=" + rating +
                ", ratingsCount=" + (ratings != null ? ratings.size() : 0) +
                ", averageRating=" + averageRating +
                ", totalRatings=" + totalRatings +
                ", hasBreakdown=" + (breakdown != null) +
                '}';
    }

    // Classe interna per la distribuzione dei voti
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RatingBreakdown {

        @JsonProperty("fiveStars")
        private int fiveStars;

        @JsonProperty("fourStars")
        private int fourStars;

        @JsonProperty("threeStars")
        private int threeStars;

        @JsonProperty("twoStars")
        private int twoStars;

        @JsonProperty("oneStar")
        private int oneStar;

        @JsonProperty("averageStyle")
        private Double averageStyle;

        @JsonProperty("averageContent")
        private Double averageContent;

        @JsonProperty("averagePleasantness")
        private Double averagePleasantness;

        @JsonProperty("averageOriginality")
        private Double averageOriginality;

        @JsonProperty("averageEdition")
        private Double averageEdition;

        // Costruttori
        public RatingBreakdown() {}

        public RatingBreakdown(int fiveStars, int fourStars, int threeStars, int twoStars, int oneStar) {
            this.fiveStars = fiveStars;
            this.fourStars = fourStars;
            this.threeStars = threeStars;
            this.twoStars = twoStars;
            this.oneStar = oneStar;
        }

        // Getters e Setters
        public int getFiveStars() { return fiveStars; }
        public void setFiveStars(int fiveStars) { this.fiveStars = fiveStars; }

        public int getFourStars() { return fourStars; }
        public void setFourStars(int fourStars) { this.fourStars = fourStars; }

        public int getThreeStars() { return threeStars; }
        public void setThreeStars(int threeStars) { this.threeStars = threeStars; }

        public int getTwoStars() { return twoStars; }
        public void setTwoStars(int twoStars) { this.twoStars = twoStars; }

        public int getOneStar() { return oneStar; }
        public void setOneStar(int oneStar) { this.oneStar = oneStar; }

        public Double getAverageStyle() { return averageStyle; }
        public void setAverageStyle(Double averageStyle) { this.averageStyle = averageStyle; }

        public Double getAverageContent() { return averageContent; }
        public void setAverageContent(Double averageContent) { this.averageContent = averageContent; }

        public Double getAveragePleasantness() { return averagePleasantness; }
        public void setAveragePleasantness(Double averagePleasantness) { this.averagePleasantness = averagePleasantness; }

        public Double getAverageOriginality() { return averageOriginality; }
        public void setAverageOriginality(Double averageOriginality) { this.averageOriginality = averageOriginality; }

        public Double getAverageEdition() { return averageEdition; }
        public void setAverageEdition(Double averageEdition) { this.averageEdition = averageEdition; }

        public int getTotalRatings() {
            return fiveStars + fourStars + threeStars + twoStars + oneStar;
        }

        @Override
        public String toString() {
            return "RatingBreakdown{" +
                    "5★=" + fiveStars +
                    ", 4★=" + fourStars +
                    ", 3★=" + threeStars +
                    ", 2★=" + twoStars +
                    ", 1★=" + oneStar +
                    ", total=" + getTotalRatings() +
                    '}';
        }
    }
}