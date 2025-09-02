package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * La classe `BookRating` rappresenta una valutazione dettagliata di un libro da parte di un utente.
 * <p>
 * Questo modello unificato, basato sulla tabella 'assessment' del database, è condiviso tra
 * client e server per la serializzazione e deserializzazione JSON. La classe permette di
 * registrare e gestire voti specifici per diverse categorie e una recensione testuale opzionale.
 * L'annotazione {@code @JsonIgnoreProperties(ignoreUnknown = true)} garantisce che il modello
 * rimanga compatibile anche in caso di evoluzioni future del lato server.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookRating {

    /**
     * L'ID univoco che identifica la valutazione.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Lo username dell'utente che ha effettuato la valutazione.
     */
    @JsonProperty("username")
    private String username;

    /**
     * L'ISBN del libro a cui si riferisce la valutazione.
     */
    @JsonProperty("isbn")
    private String isbn;

    /**
     * La data e l'ora in cui è stata creata la valutazione, formattata come stringa.
     */
    @JsonProperty("data")
    private String data;

    /**
     * Il voto assegnato allo stile di scrittura, su una scala da 1 a 5.
     */
    @JsonProperty("style")
    private Integer style;

    /**
     * Il voto assegnato al contenuto del libro, su una scala da 1 a 5.
     */
    @JsonProperty("content")
    private Integer content;

    /**
     * Il voto assegnato alla piacevolezza del libro, su una scala da 1 a 5.
     */
    @JsonProperty("pleasantness")
    private Integer pleasantness;

    /**
     * Il voto assegnato all'originalità del libro, su una scala da 1 a 5.
     */
    @JsonProperty("originality")
    private Integer originality;

    /**
     * Il voto assegnato all'edizione (es. copertina, impaginazione), su una scala da 1 a 5.
     */
    @JsonProperty("edition")
    private Integer edition;

    /**
     * La media dei voti parziali, calcolata automaticamente.
     */
    @JsonProperty("average")
    private Double average;

    /**
     * La recensione testuale opzionale associata alla valutazione.
     */
    @JsonProperty("review")
    private String review;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Inizializza automaticamente la data della valutazione al momento della sua istanziazione.
     * È richiesto dalla libreria di serializzazione Jackson.
     * </p>
     */
    public BookRating() {
        this.data = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Costruttore per creare un oggetto di valutazione con i campi minimi.
     *
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     */
    public BookRating(String username, String isbn) {
        this();
        this.username = username;
        this.isbn = isbn;
    }

    /**
     * Costruttore per creare una valutazione con tutti i voti parziali.
     * <p>
     * Questo costruttore calcola automaticamente il voto medio (`average`) dopo aver
     * impostato i voti parziali.
     * </p>
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     * @param style Il voto per lo stile.
     * @param content Il voto per il contenuto.
     * @param pleasantness Il voto per la piacevolezza.
     * @param originality Il voto per l'originalità.
     * @param edition Il voto per l'edizione.
     */
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

    /**
     * Costruttore completo che include tutti i voti parziali e una recensione testuale.
     *
     * @param username Lo username dell'utente.
     * @param isbn L'ISBN del libro.
     * @param style Il voto per lo stile.
     * @param content Il voto per il contenuto.
     * @param pleasantness Il voto per la piacevolezza.
     * @param originality Il voto per l'originalità.
     * @param edition Il voto per l'edizione.
     * @param review La recensione testuale.
     */
    public BookRating(String username, String isbn, Integer style, Integer content,
                      Integer pleasantness, Integer originality, Integer edition, String review) {
        this(username, isbn, style, content, pleasantness, originality, edition);
        this.review = review;
    }

    // Getters

    /**
     * Restituisce l'ID della valutazione.
     * @return L'ID.
     */
    public Long getId() {
        return id;
    }

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
     * Restituisce la data della valutazione.
     * @return La data come stringa.
     */
    public String getData() {
        return data;
    }

    /**
     * Restituisce il voto per lo stile.
     * @return Il voto.
     */
    public Integer getStyle() {
        return style;
    }

    /**
     * Restituisce il voto per il contenuto.
     * @return Il voto.
     */
    public Integer getContent() {
        return content;
    }

    /**
     * Restituisce il voto per la piacevolezza.
     * @return Il voto.
     */
    public Integer getPleasantness() {
        return pleasantness;
    }

    /**
     * Restituisce il voto per l'originalità.
     * @return Il voto.
     */
    public Integer getOriginality() {
        return originality;
    }

    /**
     * Restituisce il voto per l'edizione.
     * @return Il voto.
     */
    public Integer getEdition() {
        return edition;
    }

    /**
     * Restituisce la media dei voti.
     * @return La media.
     */
    public Double getAverage() {
        return average;
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
     * Imposta l'ID della valutazione.
     * @param id Il nuovo ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * Imposta la data della valutazione.
     * @param data La nuova data.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Imposta il voto per lo stile e ricalcola la media.
     * @param style Il nuovo voto.
     */
    public void setStyle(Integer style) {
        this.style = style;
        calculateAverage();
    }

    /**
     * Imposta il voto per il contenuto e ricalcola la media.
     * @param content Il nuovo voto.
     */
    public void setContent(Integer content) {
        this.content = content;
        calculateAverage();
    }

    /**
     * Imposta il voto per la piacevolezza e ricalcola la media.
     * @param pleasantness Il nuovo voto.
     */
    public void setPleasantness(Integer pleasantness) {
        this.pleasantness = pleasantness;
        calculateAverage();
    }

    /**
     * Imposta il voto per l'originalità e ricalcola la media.
     * @param originality Il nuovo voto.
     */
    public void setOriginality(Integer originality) {
        this.originality = originality;
        calculateAverage();
    }

    /**
     * Imposta il voto per l'edizione e ricalcola la media.
     * @param edition Il nuovo voto.
     */
    public void setEdition(Integer edition) {
        this.edition = edition;
        calculateAverage();
    }

    /**
     * Imposta la media dei voti.
     * @param average La nuova media.
     */
    public void setAverage(Double average) {
        this.average = average;
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
     * Calcola la media dei voti parziali.
     * <p>
     * Il metodo itera su tutti i voti (`style`, `content`, `pleasantness`,
     * `originality`, `edition`) e calcola la media solo per i voti
     * che non sono nulli e sono maggiori di 0. Il risultato viene
     * arrotondato a due cifre decimali.
     * </p>
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
            this.average = Math.round((sum / count) * 100.0) / 100.0;
        } else {
            this.average = 0.0;
        }
    }

    /**
     * Verifica se la valutazione è completa.
     * <p>
     * Una valutazione è considerata completa se sono presenti lo username, l'ISBN
     * e tutti i voti parziali sono validi (non nulli e maggiori di 0).
     * </p>
     * @return {@code true} se la valutazione è completa, {@code false} altrimenti.
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
     * Restituisce un numero intero di stelle basato sulla media arrotondata.
     *
     * @return Il numero di stelle intere, da 0 a 5.
     */
    public int getStarRating() {
        if (average == null || average <= 0) return 0;
        return (int) Math.round(average);
    }

    /**
     * Restituisce una rappresentazione testuale del voto medio con stelle.
     *
     * @return Una stringa formattata con le stelle (`★`) e il valore numerico medio.
     */
    public String getDisplayRating() {
        if (average == null || average <= 0) {
            return "Non valutato";
        }

        String stars = "★".repeat(getStarRating()) + "☆".repeat(5 - getStarRating());
        return String.format("%s (%.1f/5)", stars, average);
    }

    /**
     * Restituisce una descrizione testuale della qualità del voto medio.
     * <p>
     * Il metodo assegna una descrizione (es. "Eccellente", "Buono", "Scarso")
     * in base al valore della media.
     * </p>
     * @return Una stringa descrittiva della qualità.
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
     * Verifica se questa valutazione è stata creata da un utente specifico.
     *
     * @param username Lo username da confrontare.
     * @return {@code true} se la valutazione appartiene all'utente specificato, {@code false} altrimenti.
     */
    public boolean belongsToUser(String username) {
        return this.username != null && this.username.equalsIgnoreCase(username);
    }

    /**
     * Verifica se questa valutazione si riferisce a un libro specifico.
     *
     * @param isbn L'ISBN del libro da confrontare.
     * @return {@code true} se la valutazione è per il libro specificato, {@code false} altrimenti.
     */
    public boolean isForBook(String isbn) {
        return this.isbn != null && this.isbn.equals(isbn);
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `BookRating`.
     * <p>
     * Inclusi tutti i campi per una visualizzazione completa a scopo di debug o log.
     * </p>
     * @return La rappresentazione in stringa.
     */
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

    /**
     * Confronta questo oggetto `BookRating` con un altro per verificarne l'uguaglianza.
     * <p>
     * Due valutazioni sono considerate uguali se sono state effettuate dallo stesso utente
     * per lo stesso libro. L'uguaglianza si basa quindi sulla combinazione di `username` e `isbn`.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookRating that = (BookRating) o;

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