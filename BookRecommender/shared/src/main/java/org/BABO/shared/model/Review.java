package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Classe che rappresenta una recensione per la gestione admin
 * Basata sulla tabella 'assessment' del database, focus su recensioni testuali
 * Condivisa tra client e server per la serializzazione JSON
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Review {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("bookTitle")
    private String bookTitle;  // Titolo del libro per visualizzazione

    @JsonProperty("bookAuthor")
    private String bookAuthor; // Autore del libro per visualizzazione

    @JsonProperty("rating")
    private Integer rating;    // Rating complessivo (media dei voti)

    @JsonProperty("reviewText")
    private String reviewText; // Testo della recensione

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("userEmail")
    private String userEmail;  // Email dell'utente per gestione admin

    // Campi dettagliati (opzionali)
    @JsonProperty("styleRating")
    private Integer styleRating;

    @JsonProperty("contentRating")
    private Integer contentRating;

    @JsonProperty("pleasantnessRating")
    private Integer pleasantnessRating;

    @JsonProperty("originalityRating")
    private Integer originalityRating;

    @JsonProperty("editionRating")
    private Integer editionRating;

    // Costruttori
    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    public Review(String username, String isbn, String reviewText, Integer rating) {
        this();
        this.username = username;
        this.isbn = isbn;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    // Costruttore completo
    public Review(Long id, String username, String isbn, String bookTitle, String bookAuthor,
                  Integer rating, String reviewText, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.isbn = isbn;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public Integer getRating() {
        return rating != null ? rating : 0;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getStyleRating() {
        return styleRating;
    }

    public void setStyleRating(Integer styleRating) {
        this.styleRating = styleRating;
    }

    public Integer getContentRating() {
        return contentRating;
    }

    public void setContentRating(Integer contentRating) {
        this.contentRating = contentRating;
    }

    public Integer getPleasantnessRating() {
        return pleasantnessRating;
    }

    public void setPleasantnessRating(Integer pleasantnessRating) {
        this.pleasantnessRating = pleasantnessRating;
    }

    public Integer getOriginalityRating() {
        return originalityRating;
    }

    public void setOriginalityRating(Integer originalityRating) {
        this.originalityRating = originalityRating;
    }

    public Integer getEditionRating() {
        return editionRating;
    }

    public void setEditionRating(Integer editionRating) {
        this.editionRating = editionRating;
    }

    // Metodi di utility

    /**
     * Verifica se la recensione ha del testo
     */
    public boolean hasReviewText() {
        return reviewText != null && !reviewText.trim().isEmpty();
    }

    /**
     * Ottiene una versione troncata del testo della recensione
     */
    public String getTruncatedReviewText(int maxLength) {
        if (reviewText == null || reviewText.isEmpty()) {
            return "Nessuna recensione testuale";
        }

        if (reviewText.length() <= maxLength) {
            return reviewText;
        }

        return reviewText.substring(0, maxLength) + "...";
    }

    /**
     * Ottiene una rappresentazione testuale del rating con stelle
     */
    public String getRatingStars() {
        int ratingValue = getRating();
        if (ratingValue < 1 || ratingValue > 5) {
            return "☆☆☆☆☆";
        }

        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= ratingValue) {
                stars.append("⭐");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    /**
     * Verifica se la recensione è completa (ha almeno rating e testo)
     */
    public boolean isComplete() {
        return rating != null && rating >= 1 && rating <= 5 && hasReviewText();
    }

    /**
     * Ottiene informazioni di debug sulla recensione
     */
    public String getDebugInfo() {
        return String.format("Review[id=%d, user=%s, book=%s, rating=%d, hasText=%b]",
                id, username, bookTitle, rating, hasReviewText());
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", isbn='" + isbn + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", rating=" + rating +
                ", hasReviewText=" + hasReviewText() +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;

        if (id != null ? !id.equals(review.id) : review.id != null) return false;
        if (username != null ? !username.equals(review.username) : review.username != null) return false;
        return isbn != null ? isbn.equals(review.isbn) : review.isbn == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (isbn != null ? isbn.hashCode() : 0);
        return result;
    }
}