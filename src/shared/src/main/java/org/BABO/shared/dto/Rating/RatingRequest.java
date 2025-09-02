package org.BABO.shared.dto.Rating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) che incapsula una richiesta dal client al server per
 * aggiungere o aggiornare una valutazione per un libro.
 * <p>
 * Questa classe definisce la struttura dei dati necessari per inviare una valutazione
 * completa, che include i voti per diverse categorie e un'opzionale recensione testuale.
 * L'annotazione {@code @JsonIgnoreProperties(ignoreUnknown = true)} garantisce che il modello
 * rimanga compatibile anche se la richiesta JSON dovesse contenere campi aggiuntivi.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingRequest {

    /**
     * Lo username dell'utente che sta inviando la valutazione.
     */
    @JsonProperty("username")
    private String username;

    /**
     * L'ISBN del libro a cui si riferisce la valutazione.
     */
    @JsonProperty("isbn")
    private String isbn;

    /**
     * Voto per lo stile di scrittura del libro (da 1 a 5).
     */
    @JsonProperty("style")
    private Integer style;

    /**
     * Voto per il contenuto e l'argomento del libro (da 1 a 5).
     */
    @JsonProperty("content")
    private Integer content;

    /**
     * Voto per la piacevolezza generale della lettura (da 1 a 5).
     */
    @JsonProperty("pleasantness")
    private Integer pleasantness;

    /**
     * Voto per l'originalità della trama o del concetto (da 1 a 5).
     */
    @JsonProperty("originality")
    private Integer originality;

    /**
     * Voto per la qualità dell'edizione fisica del libro (da 1 a 5).
     */
    @JsonProperty("edition")
    private Integer edition;

    /**
     * Recensione testuale opzionale.
     */
    @JsonProperty("review")
    private String review;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public RatingRequest() {}

    /**
     * Costruttore per una richiesta con i campi obbligatori di identificazione.
     *
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     */
    public RatingRequest(String username, String isbn) {
        this.username = username;
        this.isbn = isbn;
    }

    /**
     * Costruttore per una richiesta che include i voti per tutte le categorie.
     *
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     * @param style Voto per lo stile.
     * @param content Voto per il contenuto.
     * @param pleasantness Voto per la piacevolezza.
     * @param originality Voto per l'originalità.
     * @param edition Voto per l'edizione.
     */
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

    /**
     * Costruttore completo per una richiesta con voti e recensione testuale.
     *
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     * @param style Voto per lo stile.
     * @param content Voto per il contenuto.
     * @param pleasantness Voto per la piacevolezza.
     * @param originality Voto per l'originalità.
     * @param edition Voto per l'edizione.
     * @param review La recensione testuale.
     */
    public RatingRequest(String username, String isbn, Integer style, Integer content,
                         Integer pleasantness, Integer originality, Integer edition, String review) {
        this(username, isbn, style, content, pleasantness, originality, edition);
        this.review = review;
    }

    // Getters

    /**
     * Restituisce lo username dell'utente.
     * @return Lo username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce l'ISBN del libro.
     * @return L'ISBN.
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Restituisce il voto per lo stile.
     * @return Il voto per lo stile.
     */
    public Integer getStyle() {
        return style;
    }

    /**
     * Restituisce il voto per il contenuto.
     * @return Il voto per il contenuto.
     */
    public Integer getContent() {
        return content;
    }

    /**
     * Restituisce il voto per la piacevolezza.
     * @return Il voto per la piacevolezza.
     */
    public Integer getPleasantness() {
        return pleasantness;
    }

    /**
     * Restituisce il voto per l'originalità.
     * @return Il voto per l'originalità.
     */
    public Integer getOriginality() {
        return originality;
    }

    /**
     * Restituisce il voto per l'edizione.
     * @return Il voto per l'edizione.
     */
    public Integer getEdition() {
        return edition;
    }

    /**
     * Restituisce la recensione testuale.
     * @return La recensione.
     */
    public String getReview() {
        return review;
    }

    // Setters

    /**
     * Imposta lo username dell'utente.
     * @param username Il nuovo username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Imposta l'ISBN del libro.
     * @param isbn Il nuovo ISBN.
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Imposta il voto per lo stile.
     * @param style Il nuovo voto.
     */
    public void setStyle(Integer style) {
        this.style = style;
    }

    /**
     * Imposta il voto per il contenuto.
     * @param content Il nuovo voto.
     */
    public void setContent(Integer content) {
        this.content = content;
    }

    /**
     * Imposta il voto per la piacevolezza.
     * @param pleasantness Il nuovo voto.
     */
    public void setPleasantness(Integer pleasantness) {
        this.pleasantness = pleasantness;
    }

    /**
     * Imposta il voto per l'originalità.
     * @param originality Il nuovo voto.
     */
    public void setOriginality(Integer originality) {
        this.originality = originality;
    }

    /**
     * Imposta il voto per l'edizione.
     * @param edition Il nuovo voto.
     */
    public void setEdition(Integer edition) {
        this.edition = edition;
    }

    /**
     * Imposta la recensione testuale.
     * @param review La nuova recensione.
     */
    public void setReview(String review) {
        this.review = review;
    }

    // Metodi di utilità

    /**
     * Valida la richiesta verificando la presenza e la correttezza dei campi obbligatori.
     * <p>
     * I campi `username`, `isbn` e tutti i voti (`style`, `content`, ecc.)
     * devono essere presenti e i voti devono essere compresi tra 1 e 5.
     * </p>
     * @return {@code true} se la richiesta è valida, {@code false} altrimenti.
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
     * Restituisce una stringa che elenca tutti gli errori di validazione.
     * <p>
     * Questo metodo è utile per fornire un feedback dettagliato all'utente in caso di richiesta non valida.
     * </p>
     * @return Una stringa con gli errori di validazione, vuota se la richiesta è valida.
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
     * Calcola la media dei voti per le cinque categorie.
     *
     * @return La media dei voti arrotondata a due cifre decimali, o 0.0 se la richiesta non è valida.
     */
    public double calculateAverage() {
        if (!isValid()) return 0.0;
        double sum = style + content + pleasantness + originality + edition;
        return Math.round((sum / 5.0) * 100.0) / 100.0;
    }

    /**
     * Verifica se la richiesta contiene una recensione testuale.
     *
     * @return {@code true} se il campo `review` non è nullo e non è vuoto.
     */
    public boolean hasReview() {
        return review != null && !review.trim().isEmpty();
    }

    /**
     * Restituisce la recensione testuale pulita e troncata.
     * <p>
     * Rimuove gli spazi bianchi e, se la recensione supera i 1000 caratteri, la tronca
     * aggiungendo i puntini di sospensione.
     * </p>
     * @return La recensione pulita o {@code null} se il campo era nullo o vuoto.
     */
    public String getCleanReview() {
        if (review == null) return null;
        String cleaned = review.trim();
        if (cleaned.isEmpty()) return null;
        if (cleaned.length() > 1000) {
            cleaned = cleaned.substring(0, 1000) + "...";
        }
        return cleaned;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `RatingRequest`.
     * <p>
     * Utile per scopi di debugging. Mostra i principali campi e una porzione della recensione.
     * </p>
     * @return La stringa descrittiva.
     */
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

    /**
     * Confronta questo oggetto `RatingRequest` con un altro per verificarne l'uguaglianza.
     * <p>
     * Due richieste sono considerate uguali se i campi `username` e `isbn` sono identici.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingRequest that = (RatingRequest) o;
        return username != null && username.equals(that.username) &&
                isbn != null && isbn.equals(that.isbn);
    }

    /**
     * Restituisce il valore di hash per questo oggetto.
     * <p>
     * Il valore di hash è calcolato sulla base dei campi `username` e `isbn`.
     * </p>
     * @return Il valore di hash.
     */
    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (isbn != null ? isbn.hashCode() : 0);
        return result;
    }
}