package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe che rappresenta una valutazione di un libro da parte di un utente
 * Basata sulla tabella 'assessment' del database
 * Condivisa tra client e server per la serializzazione JSON
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookRating {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("data")
    private String data;            // Cambiato da LocalDateTime a String per semplicità

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

    @JsonProperty("average")
    private Double average;          // Media automatica dei voti

    @JsonProperty("review")
    private String review;           // Recensione testuale (opzionale)

    // Costruttori
    public BookRating() {
        this.data = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public BookRating(String username, String isbn) {
        this();
        this.username = username;
        this.isbn = isbn;
    }

    public BookRating(String username, String isbn, Integer style, Integer content,
                      Integer pleasantness, Integer originality, Integer edition) {
        this(username, isbn);
        this.style = style;
        this.content = content;
        this.pleasantness = pleasantness;
        this.originality = originality;
        this.edition = edition;
        calculateAverage();
    }

    public BookRating(String username, String isbn, Integer style, Integer content,
                      Integer pleasantness, Integer originality, Integer edition, String review) {
        this(username, isbn, style, content, pleasantness, originality, edition);
        this.review = review;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getData() {
        return data;
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

    public Double getAverage() {
        return average;
    }

    public String getReview() {
        return review;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setStyle(Integer style) {
        this.style = style;
        calculateAverage();
    }

    public void setContent(Integer content) {
        this.content = content;
        calculateAverage();
    }

    public void setPleasantness(Integer pleasantness) {
        this.pleasantness = pleasantness;
        calculateAverage();
    }

    public void setOriginality(Integer originality) {
        this.originality = originality;
        calculateAverage();
    }

    public void setEdition(Integer edition) {
        this.edition = edition;
        calculateAverage();
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public void setReview(String review) {
        this.review = review;
    }

    // Metodi di utilità

    /**
     * Calcola automaticamente la media dei voti
     */
    public void calculateAverage() {
        int count = 0;
        double sum = 0.0;

        if (style != null && style > 0) {
            sum += style;
            count++;
        }
        if (content != null && content > 0) {
            sum += content;
            count++;
        }
        if (pleasantness != null && pleasantness > 0) {
            sum += pleasantness;
            count++;
        }
        if (originality != null && originality > 0) {
            sum += originality;
            count++;
        }
        if (edition != null && edition > 0) {
            sum += edition;
            count++;
        }

        if (count > 0) {
            this.average = Math.round((sum / count) * 100.0) / 100.0; // Arrotonda a 2 decimali
        } else {
            this.average = 0.0;
        }
    }

    /**
     * Verifica se la valutazione è completa (tutti i campi obbligatori)
     */
    public boolean isComplete() {
        return username != null && !username.trim().isEmpty() &&
                isbn != null && !isbn.trim().isEmpty() &&
                style != null && style > 0 &&
                content != null && content > 0 &&
                pleasantness != null && pleasantness > 0 &&
                originality != null && originality > 0 &&
                edition != null && edition > 0;
    }

    /**
     * Restituisce il numero di stelle piene basato sulla media
     */
    public int getStarRating() {
        if (average == null || average <= 0) return 0;
        return (int) Math.round(average);
    }

    /**
     * Restituisce una rappresentazione testuale della valutazione
     */
    public String getDisplayRating() {
        if (average == null || average <= 0) {
            return "Non valutato";
        }

        String stars = "★".repeat(getStarRating()) + "☆".repeat(5 - getStarRating());
        return String.format("%s (%.1f/5)", stars, average);
    }

    /**
     * Restituisce una descrizione testuale della qualità
     */
    public String getQualityDescription() {
        if (average == null || average <= 0) return "Non valutato";

        if (average >= 4.5) return "Eccellente";
        else if (average >= 4.0) return "Molto buono";
        else if (average >= 3.5) return "Buono";
        else if (average >= 3.0) return "Discreto";
        else if (average >= 2.5) return "Sufficiente";
        else if (average >= 2.0) return "Mediocre";
        else return "Scarso";
    }

    /**
     * Verifica se la valutazione appartiene a un utente specifico
     */
    public boolean belongsToUser(String username) {
        return this.username != null && this.username.equalsIgnoreCase(username);
    }

    /**
     * Verifica se la valutazione è per un libro specifico
     */
    public boolean isForBook(String isbn) {
        return this.isbn != null && this.isbn.equals(isbn);
    }

    @Override
    public String toString() {
        return "BookRating{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", isbn='" + isbn + '\'' +
                ", data=" + data +
                ", style=" + style +
                ", content=" + content +
                ", pleasantness=" + pleasantness +
                ", originality=" + originality +
                ", edition=" + edition +
                ", average=" + average +
                ", review='" + review + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookRating that = (BookRating) o;

        // Due valutazioni sono uguali se sono dello stesso utente per lo stesso libro
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