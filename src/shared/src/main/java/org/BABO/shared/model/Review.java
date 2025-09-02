package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * La classe `Review` rappresenta una recensione per la gestione amministrativa del sistema.
 * <p>
 * Questa classe è un modello unificato, condiviso tra client e server, per la serializzazione
 * e deserializzazione di oggetti JSON. È basata sulla tabella 'assessment' del database,
 * con un focus particolare sulle recensioni testuali.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Review {

    /**
     * L'ID univoco della recensione.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Lo username dell'utente che ha scritto la recensione.
     */
    @JsonProperty("username")
    private String username;

    /**
     * L'ISBN del libro recensito.
     */
    @JsonProperty("isbn")
    private String isbn;

    /**
     * Il titolo del libro, utilizzato per una visualizzazione più comoda.
     */
    @JsonProperty("bookTitle")
    private String bookTitle;

    /**
     * L'autore del libro, utilizzato per una visualizzazione più comoda.
     */
    @JsonProperty("bookAuthor")
    private String bookAuthor;

    /**
     * Il rating complessivo della recensione (media dei voti).
     */
    @JsonProperty("rating")
    private Integer rating;

    /**
     * Il testo della recensione.
     */
    @JsonProperty("reviewText")
    private String reviewText;

    /**
     * La data e ora di creazione della recensione.
     */
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    /**
     * L'indirizzo email dell'utente, utilizzato per la gestione da parte degli amministratori.
     */
    @JsonProperty("userEmail")
    private String userEmail;

    // Campi opzionali per i rating dettagliati

    /**
     * Voto per lo stile di scrittura.
     */
    @JsonProperty("styleRating")
    private Integer styleRating;

    /**
     * Voto per il contenuto.
     */
    @JsonProperty("contentRating")
    private Integer contentRating;

    /**
     * Voto per la piacevolezza.
     */
    @JsonProperty("pleasantnessRating")
    private Integer pleasantnessRating;

    /**
     * Voto per l'originalità.
     */
    @JsonProperty("originalityRating")
    private Integer originalityRating;

    /**
     * Voto per l'edizione.
     */
    @JsonProperty("editionRating")
    private Integer editionRating;

    /**
     * Costruttore di default.
     * <p>
     * Inizializza automaticamente la data di creazione al momento della sua istanziazione.
     * È richiesto dalla libreria di serializzazione Jackson.
     * </p>
     */
    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Costruttore per creare una recensione con i campi principali.
     *
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     * @param reviewText Il testo della recensione.
     * @param rating Il rating complessivo.
     */
    public Review(String username, String isbn, String reviewText, Integer rating) {
        this();
        this.username = username;
        this.isbn = isbn;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    /**
     * Costruttore completo per inizializzare tutti i campi principali.
     *
     * @param id L'ID della recensione.
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     * @param bookTitle Il titolo del libro.
     * @param bookAuthor L'autore del libro.
     * @param rating Il rating complessivo.
     * @param reviewText Il testo della recensione.
     * @param createdAt La data e ora di creazione.
     */
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

    /**
     * Restituisce l'ID della recensione.
     * @return L'ID della recensione.
     */
    public Long getId() {
        return id;
    }

    /**
     * Imposta l'ID della recensione.
     * @param id Il nuovo ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Restituisce lo username dell'utente.
     * @return Lo username dell'utente.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta lo username dell'utente.
     * @param username Il nuovo username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce l'ISBN del libro.
     * @return L'ISBN.
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Imposta l'ISBN del libro.
     * @param isbn Il nuovo ISBN.
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Restituisce il titolo del libro.
     * @return Il titolo del libro.
     */
    public String getBookTitle() {
        return bookTitle;
    }

    /**
     * Imposta il titolo del libro.
     * @param bookTitle Il nuovo titolo.
     */
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    /**
     * Restituisce l'autore del libro.
     * @return L'autore del libro.
     */
    public String getBookAuthor() {
        return bookAuthor;
    }

    /**
     * Imposta l'autore del libro.
     * @param bookAuthor Il nuovo autore.
     */
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    /**
     * Restituisce il rating complessivo.
     * @return Il rating, o 0 se è nullo.
     */
    public Integer getRating() {
        return rating != null ? rating : 0;
    }

    /**
     * Imposta il rating complessivo.
     * @param rating Il nuovo rating.
     */
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    /**
     * Restituisce il testo della recensione.
     * @return Il testo della recensione.
     */
    public String getReviewText() {
        return reviewText;
    }

    /**
     * Imposta il testo della recensione.
     * @param reviewText Il nuovo testo.
     */
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    /**
     * Restituisce la data e ora di creazione della recensione.
     * @return La data e ora di creazione.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Imposta la data e ora di creazione della recensione.
     * @param createdAt La nuova data e ora.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Restituisce l'email dell'utente.
     * @return L'email dell'utente.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Imposta l'email dell'utente.
     * @param userEmail La nuova email.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Restituisce il rating per lo stile di scrittura.
     * @return Il rating dello stile.
     */
    public Integer getStyleRating() {
        return styleRating;
    }

    /**
     * Imposta il rating per lo stile di scrittura.
     * @param styleRating Il nuovo rating.
     */
    public void setStyleRating(Integer styleRating) {
        this.styleRating = styleRating;
    }

    /**
     * Restituisce il rating per il contenuto.
     * @return Il rating del contenuto.
     */
    public Integer getContentRating() {
        return contentRating;
    }

    /**
     * Imposta il rating per il contenuto.
     * @param contentRating Il nuovo rating.
     */
    public void setContentRating(Integer contentRating) {
        this.contentRating = contentRating;
    }

    /**
     * Restituisce il rating per la piacevolezza.
     * @return Il rating della piacevolezza.
     */
    public Integer getPleasantnessRating() {
        return pleasantnessRating;
    }

    /**
     * Imposta il rating per la piacevolezza.
     * @param pleasantnessRating Il nuovo rating.
     */
    public void setPleasantnessRating(Integer pleasantnessRating) {
        this.pleasantnessRating = pleasantnessRating;
    }

    /**
     * Restituisce il rating per l'originalità.
     * @return Il rating dell'originalità.
     */
    public Integer getOriginalityRating() {
        return originalityRating;
    }

    /**
     * Imposta il rating per l'originalità.
     * @param originalityRating Il nuovo rating.
     */
    public void setOriginalityRating(Integer originalityRating) {
        this.originalityRating = originalityRating;
    }

    /**
     * Restituisce il rating per l'edizione.
     * @return Il rating dell'edizione.
     */
    public Integer getEditionRating() {
        return editionRating;
    }

    /**
     * Imposta il rating per l'edizione.
     * @param editionRating Il nuovo rating.
     */
    public void setEditionRating(Integer editionRating) {
        this.editionRating = editionRating;
    }

    // Metodi di utilità

    /**
     * Verifica se la recensione contiene del testo.
     * @return {@code true} se il testo della recensione non è nullo e non è vuoto, {@code false} altrimenti.
     */
    public boolean hasReviewText() {
        return reviewText != null && !reviewText.trim().isEmpty();
    }

    /**
     * Restituisce una versione troncata del testo della recensione.
     * Se la lunghezza del testo è superiore a {@code maxLength}, lo tronca e aggiunge i puntini di sospensione.
     * Se il testo è nullo o vuoto, restituisce un messaggio di default.
     *
     * @param maxLength La lunghezza massima desiderata per la stringa troncata.
     * @return Il testo troncato o il messaggio di default.
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
     * Restituisce una rappresentazione in stringa del rating utilizzando stelle.
     * Restituisce sempre 5 stelle piene come stringa "★★★★★".
     *
     * @return La stringa con la rappresentazione a stelle del rating.
     */
    public String getRatingStars() {
        int ratingValue = getRating();
        if (ratingValue < 1 || ratingValue > 5) {
            return "★★★★★";
        }

        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append("★");
        }
        return stars.toString();
    }

    /**
     * Verifica se la recensione è considerata completa.
     * Una recensione è completa se ha un rating valido (tra 1 e 5) e del testo.
     *
     * @return {@code true} se la recensione è completa, {@code false} altrimenti.
     */
    public boolean isComplete() {
        return rating != null && rating >= 1 && rating <= 5 && hasReviewText();
    }

    /**
     * Restituisce una stringa di informazioni di debug per la recensione.
     *
     * @return Una stringa formattata con i dati principali della recensione.
     */
    public String getDebugInfo() {
        return String.format("Review[id=%d, user=%s, book=%s, rating=%d, hasText=%b]",
                id, username, bookTitle, rating, hasReviewText());
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `Review`.
     * <p>
     * Vengono inclusi i campi principali per una rapida visualizzazione, escludendo
     * i rating dettagliati e l'email dell'utente.
     * </p>
     * @return La rappresentazione in stringa della recensione.
     */
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

    /**
     * Confronta questo oggetto `Review` con un altro per verificarne l'uguaglianza.
     * <p>
     * L'uguaglianza è basata sulla combinazione di ID, username e ISBN.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;

        if (id != null ? !id.equals(review.id) : review.id != null) return false;
        if (username != null ? !username.equals(review.username) : review.username != null) return false;
        return isbn != null ? isbn.equals(review.isbn) : review.isbn == null;
    }

    /**
     * Restituisce il valore di hash per questo oggetto.
     * <p>
     * Il valore di hash è calcolato sulla base dell'ID, dello username e dell'ISBN.
     * </p>
     * @return Il valore di hash.
     */
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (isbn != null ? isbn.hashCode() : 0);
        return result;
    }
}