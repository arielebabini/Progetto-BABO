package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe che rappresenta una raccomandazione di libro
 * Condivisa tra client e server per la serializzazione JSON
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookRecommendation {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("recommenderUsername")
    private String recommenderUsername;

    @JsonProperty("targetBookIsbn")
    private String targetBookIsbn;

    @JsonProperty("recommendedBookIsbn")
    private String recommendedBookIsbn;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("createdDate")
    private String createdDate;

    @JsonProperty("isActive")
    private Boolean isActive;

    // Costruttori
    public BookRecommendation() {}

    public BookRecommendation(String recommenderUsername, String targetBookIsbn, String recommendedBookIsbn) {
        this.recommenderUsername = recommenderUsername;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.isActive = true;
    }

    public BookRecommendation(String recommenderUsername, String targetBookIsbn, String recommendedBookIsbn, String reason) {
        this.recommenderUsername = recommenderUsername;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.reason = reason;
        this.isActive = true;
    }

    public BookRecommendation(Long id, String recommenderUsername, String targetBookIsbn,
                              String recommendedBookIsbn, String reason, String createdDate, Boolean isActive) {
        this.id = id;
        this.recommenderUsername = recommenderUsername;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.reason = reason;
        this.createdDate = createdDate;
        this.isActive = isActive;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getRecommenderUsername() {
        return recommenderUsername;
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

    public String getCreatedDate() {
        return createdDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setRecommenderUsername(String recommenderUsername) {
        this.recommenderUsername = recommenderUsername;
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

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Metodi di utilità
    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    public String getDisplayReason() {
        if (hasReason()) {
            return reason.length() > 100 ? reason.substring(0, 97) + "..." : reason;
        }
        return "Nessun motivo specificato";
    }

    public String getShortUsername() {
        if (recommenderUsername == null || recommenderUsername.length() <= 3) {
            return "***";
        }
        return recommenderUsername.substring(0, 3) + "***";
    }

    public boolean isValid() {
        return recommenderUsername != null && !recommenderUsername.trim().isEmpty() &&
                targetBookIsbn != null && !targetBookIsbn.trim().isEmpty() &&
                recommendedBookIsbn != null && !recommendedBookIsbn.trim().isEmpty() &&
                !targetBookIsbn.equals(recommendedBookIsbn);
    }

    @Override
    public String toString() {
        return "BookRecommendation{" +
                "id=" + id +
                ", recommenderUsername='" + recommenderUsername + '\'' +
                ", targetBookIsbn='" + targetBookIsbn + '\'' +
                ", recommendedBookIsbn='" + recommendedBookIsbn + '\'' +
                ", reason='" + (reason != null && reason.length() > 50 ? reason.substring(0, 50) + "..." : reason) + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookRecommendation that = (BookRecommendation) o;
        return recommenderUsername != null && recommenderUsername.equals(that.recommenderUsername) &&
                targetBookIsbn != null && targetBookIsbn.equals(that.targetBookIsbn) &&
                recommendedBookIsbn != null && recommendedBookIsbn.equals(that.recommendedBookIsbn);
    }

    @Override
    public int hashCode() {
        int result = recommenderUsername != null ? recommenderUsername.hashCode() : 0;
        result = 31 * result + (targetBookIsbn != null ? targetBookIsbn.hashCode() : 0);
        result = 31 * result + (recommendedBookIsbn != null ? recommendedBookIsbn.hashCode() : 0);
        return result;
    }
}