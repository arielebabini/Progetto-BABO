package org.BABO.shared.dto.Reviews;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ReviewStats {

    @JsonProperty("totalReviews")
    private int totalReviews;

    @JsonProperty("totalReviewsWithText")
    private int totalReviewsWithText;

    @JsonProperty("totalUsers")
    private int totalUsers;

    @JsonProperty("averageRating")
    private double averageRating;

    @JsonProperty("ratingsDistribution")
    private int[] ratingsDistribution; // Array [1star, 2star, 3star, 4star, 5star]

    @JsonProperty("topRatedBooks")
    private List<String> topRatedBooks;

    @JsonProperty("mostActiveUsers")
    private List<String> mostActiveUsers;

    @JsonProperty("recentReviewsCount")
    private int recentReviewsCount; // Recensioni ultime 30 giorni

    // Costruttori
    public ReviewStats() {
        this.ratingsDistribution = new int[5]; // [1star, 2star, 3star, 4star, 5star]
    }

    // Getters e Setters
    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public int getTotalReviewsWithText() {
        return totalReviewsWithText;
    }

    public void setTotalReviewsWithText(int totalReviewsWithText) {
        this.totalReviewsWithText = totalReviewsWithText;
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

    public int[] getRatingsDistribution() {
        return ratingsDistribution;
    }

    public void setRatingsDistribution(int[] ratingsDistribution) {
        this.ratingsDistribution = ratingsDistribution;
    }

    public List<String> getTopRatedBooks() {
        return topRatedBooks;
    }

    public void setTopRatedBooks(List<String> topRatedBooks) {
        this.topRatedBooks = topRatedBooks;
    }

    public List<String> getMostActiveUsers() {
        return mostActiveUsers;
    }

    public void setMostActiveUsers(List<String> mostActiveUsers) {
        this.mostActiveUsers = mostActiveUsers;
    }

    public int getRecentReviewsCount() {
        return recentReviewsCount;
    }

    public void setRecentReviewsCount(int recentReviewsCount) {
        this.recentReviewsCount = recentReviewsCount;
    }

    /**
     * Ottiene la percentuale per ciascun rating
     */
    public double[] getRatingsPercentages() {
        double[] percentages = new double[5];
        int total = getTotalReviews();

        if (total > 0) {
            for (int i = 0; i < 5; i++) {
                percentages[i] = (ratingsDistribution[i] * 100.0) / total;
            }
        }

        return percentages;
    }

    /**
     * Ottiene il rating più comune
     */
    public int getMostCommonRating() {
        int maxCount = 0;
        int mostCommon = 5; // Default 5 stelle

        for (int i = 0; i < 5; i++) {
            if (ratingsDistribution[i] > maxCount) {
                maxCount = ratingsDistribution[i];
                mostCommon = i + 1; // +1 perché l'array è 0-indexed ma i rating sono 1-5
            }
        }

        return mostCommon;
    }

    /**
     * Verifica se ci sono abbastanza dati per le statistiche
     */
    public boolean hasEnoughData() {
        return totalReviews > 0;
    }

    @Override
    public String toString() {
        return "ReviewStats{" +
                "totalReviews=" + totalReviews +
                ", totalReviewsWithText=" + totalReviewsWithText +
                ", totalUsers=" + totalUsers +
                ", averageRating=" + averageRating +
                ", recentReviewsCount=" + recentReviewsCount +
                '}';
    }
}
