package org.BABO.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per le richieste di aggiunta/aggiornamento valutazioni
 * Utilizzato per la comunicazione client-server
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingRequest {

    @JsonProperty("username")
    private String username;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("style")
    private Integer style;           // Voto per lo stile (1-5)

    @JsonProperty("content")
    private Integer content;         // Voto per il contenuto (1-5)

    @JsonProperty("pleasantness")
    private Integer pleasantness;    // Voto per la piacevolezza (1-5)

    @JsonProperty("originality")
    private Integer originality;     // Voto per l'originalità (1-5)

    @JsonProperty("edition")
    private Integer edition;         // Voto per l'edizione (1-5)

    @JsonProperty("review")
    private String review;           // Recensione testuale (opzionale)

    // Costruttori
    public RatingRequest() {}

    public RatingRequest(String username, String isbn) {
        this.username = username;
        this.isbn = isbn;
    }

    public RatingRequest(String username, String isbn, Integer style, Integer content,
                         Integer pleasantness, Integer originality, Integer edition) {
        this.username = username;
        this.isbn = isbn;
        this.style = style;
        this.content = content;
        this.pleasantness = pleasantness;
        this.originality = originality;
        this.edition = edition;
    }

    public RatingRequest(String username, String isbn, Integer style, Integer content,
                         Integer pleasantness, Integer originality, Integer edition, String review) {
        this(username, isbn, style, content, pleasantness, originality, edition);
        this.review = review;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getStyle() {
        return style;
    }

    public Integer getContent() {
        return content;
    }

    public Integer getPleasantness() {
        return pleasantness;
    }

    public Integer getOriginality() {
        return originality;
    }

    public Integer getEdition() {
        return edition;
    }

    public String getReview() {
        return review;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setStyle(Integer style) {
        this.style = style;
    }

    public void setContent(Integer content) {
        this.content = content;
    }

    public void setPleasantness(Integer pleasantness) {
        this.pleasantness = pleasantness;
    }

    public void setOriginality(Integer originality) {
        this.originality = originality;
    }

    public void setEdition(Integer edition) {
        this.edition = edition;
    }

    public void setReview(String review) {
        this.review = review;
    }

    // Metodi di utilità

    /**
     * Valida che tutti i campi obbligatori siano presenti
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                isbn != null && !isbn.trim().isEmpty() &&
                style != null && style >= 1 && style <= 5 &&
                content != null && content >= 1 && content <= 5 &&
                pleasantness != null && pleasantness >= 1 && pleasantness <= 5 &&
                originality != null && originality >= 1 && originality <= 5 &&
                edition != null && edition >= 1 && edition <= 5;
    }

    /**
     * Restituisce una lista degli errori di validazione
     */
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();

        if (username == null || username.trim().isEmpty()) {
            errors.append("Username è obbligatorio. ");
        }

        if (isbn == null || isbn.trim().isEmpty()) {
            errors.append("ISBN è obbligatorio. ");
        }

        if (style == null || style < 1 || style > 5) {
            errors.append("Voto stile deve essere tra 1 e 5. ");
        }

        if (content == null || content < 1 || content > 5) {
            errors.append("Voto contenuto deve essere tra 1 e 5. ");
        }

        if (pleasantness == null || pleasantness < 1 || pleasantness > 5) {
            errors.append("Voto piacevolezza deve essere tra 1 e 5. ");
        }

        if (originality == null || originality < 1 || originality > 5) {
            errors.append("Voto originalità deve essere tra 1 e 5. ");
        }

        if (edition == null || edition < 1 || edition > 5) {
            errors.append("Voto edizione deve essere tra 1 e 5. ");
        }

        return errors.toString().trim();
    }

    /**
     * Calcola la media dei voti
     */
    public double calculateAverage() {
        if (!isValid()) return 0.0;

        double sum = style + content + pleasantness + originality + edition;
        return Math.round((sum / 5.0) * 100.0) / 100.0;
    }

    /**
     * Verifica se ha una recensione testuale
     */
    public boolean hasReview() {
        return review != null && !review.trim().isEmpty();
    }

    /**
     * Pulisce e valida la recensione
     */
    public String getCleanReview() {
        if (review == null) return null;

        String cleaned = review.trim();
        if (cleaned.isEmpty()) return null;

        // Limita la lunghezza della recensione (es. 1000 caratteri)
        if (cleaned.length() > 1000) {
            cleaned = cleaned.substring(0, 1000) + "...";
        }

        return cleaned;
    }

    @Override
    public String toString() {
        return "RatingRequest{" +
                "username='" + username + '\'' +
                ", isbn='" + isbn + '\'' +
                ", style=" + style +
                ", content=" + content +
                ", pleasantness=" + pleasantness +
                ", originality=" + originality +
                ", edition=" + edition +
                ", review='" + (review != null ? review.substring(0, Math.min(50, review.length())) + "..." : "null") + '\'' +
                ", average=" + calculateAverage() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingRequest that = (RatingRequest) o;
        return username != null && username.equals(that.username) &&
                isbn != null && isbn.equals(that.isbn);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (isbn != null ? isbn.hashCode() : 0);
        return result;
    }
}