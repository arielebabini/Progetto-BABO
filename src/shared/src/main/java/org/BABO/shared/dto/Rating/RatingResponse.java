package org.BABO.shared.dto.Rating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.BookRating;

import java.util.List;

/**
 * Data Transfer Object (DTO) che incapsula la risposta del server per le operazioni relative alle valutazioni.
 * <p>
 * Questa classe è un modello standardizzato per la comunicazione client-server
 * e può contenere una singola valutazione, una lista di valutazioni, dati statistici
 * come la media e il totale, e la distribuzione dettagliata dei voti.
 * L'annotazione {@code @JsonIgnoreProperties(ignoreUnknown = true)} ignora eventuali
 * proprietà JSON non riconosciute, garantendo la compatibilità del modello.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingResponse {

    /**
     * Indica se l'operazione ha avuto successo.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Un messaggio di testo che fornisce dettagli sul risultato dell'operazione.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Un singolo oggetto {@link BookRating}. Utile per operazioni come la visualizzazione
     * della valutazione di un utente specifico.
     */
    @JsonProperty("rating")
    private BookRating rating;

    /**
     * Una lista di oggetti {@link BookRating}. Utilizzata per operazioni che restituiscono
     * più valutazioni, come la lista di tutte le valutazioni per un dato libro.
     */
    @JsonProperty("ratings")
    private List<BookRating> ratings;

    /**
     * La valutazione media aggregata per il libro.
     */
    @JsonProperty("averageRating")
    private Double averageRating;

    /**
     * Il numero totale di valutazioni ricevute per il libro.
     */
    @JsonProperty("totalRatings")
    private Integer totalRatings;

    /**
     * Un oggetto che contiene la distribuzione dettagliata dei voti (es. conteggio di 1, 2, 3, 4, 5 stelle).
     */
    @JsonProperty("ratingBreakdown")
    private RatingBreakdown breakdown;

    // Costruttori

    /**
     * Costruttore di default. Necessario per la deserializzazione JSON.
     */
    public RatingResponse() {}

    /**
     * Costruttore per una risposta base con solo successo e messaggio.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     */
    public RatingResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Costruttore per una risposta che include una singola valutazione.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param rating La singola valutazione.
     */
    public RatingResponse(boolean success, String message, BookRating rating) {
        this.success = success;
        this.message = message;
        this.rating = rating;
    }

    /**
     * Costruttore per una risposta che include una lista di valutazioni.
     * <p>
     * Il campo {@code totalRatings} viene calcolato automaticamente dalla dimensione della lista.
     * </p>
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param ratings La lista di valutazioni.
     */
    public RatingResponse(boolean success, String message, List<BookRating> ratings) {
        this.success = success;
        this.message = message;
        this.ratings = ratings;
        if (ratings != null) {
            this.totalRatings = ratings.size();
        }
    }

    /**
     * Costruttore per una risposta che include una lista di valutazioni e la media.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param ratings La lista di valutazioni.
     * @param averageRating La valutazione media.
     */
    public RatingResponse(boolean success, String message, List<BookRating> ratings, Double averageRating) {
        this(success, message, ratings);
        this.averageRating = averageRating;
    }

    /**
     * Costruttore completo per una risposta che include tutti i dati statistici.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param ratings La lista di valutazioni.
     * @param averageRating La valutazione media.
     * @param breakdown La distribuzione dettagliata dei voti.
     */
    public RatingResponse(boolean success, String message, List<BookRating> ratings,
                          Double averageRating, RatingBreakdown breakdown) {
        this(success, message, ratings, averageRating);
        this.breakdown = breakdown;
    }

    // Getters

    /**
     * Restituisce lo stato di successo.
     * @return {@code true} se l'operazione è andata a buon fine.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Restituisce il messaggio di feedback.
     * @return Il messaggio.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Restituisce la singola valutazione.
     * @return L'oggetto {@link BookRating}, o {@code null}.
     */
    public BookRating getRating() {
        return rating;
    }

    /**
     * Restituisce la lista di valutazioni.
     * @return La lista di oggetti {@link BookRating}, o {@code null}.
     */
    public List<BookRating> getRatings() {
        return ratings;
    }

    /**
     * Restituisce la valutazione media.
     * @return La media, o {@code null}.
     */
    public Double getAverageRating() {
        return averageRating;
    }

    /**
     * Restituisce il numero totale di valutazioni.
     * @return Il totale, o {@code null}.
     */
    public Integer getTotalRatings() {
        return totalRatings;
    }

    /**
     * Restituisce il dettaglio della distribuzione dei voti.
     * @return L'oggetto {@link RatingBreakdown}, o {@code null}.
     */
    public RatingBreakdown getBreakdown() {
        return breakdown;
    }

    // Setters

    /**
     * Imposta lo stato di successo.
     * @param success Il nuovo stato.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Imposta il messaggio di feedback.
     * @param message Il nuovo messaggio.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Imposta la singola valutazione.
     * @param rating La nuova valutazione.
     */
    public void setRating(BookRating rating) {
        this.rating = rating;
    }

    /**
     * Imposta la lista di valutazioni e aggiorna il conteggio totale.
     * @param ratings La nuova lista.
     */
    public void setRatings(List<BookRating> ratings) {
        this.ratings = ratings;
        if (ratings != null) {
            this.totalRatings = ratings.size();
        }
    }

    /**
     * Imposta la valutazione media.
     * @param averageRating La nuova media.
     */
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * Imposta il numero totale di valutazioni.
     * @param totalRatings Il nuovo totale.
     */
    public void setTotalRatings(Integer totalRatings) {
        this.totalRatings = totalRatings;
    }

    /**
     * Imposta la distribuzione dei voti.
     * @param breakdown Il nuovo oggetto {@link RatingBreakdown}.
     */
    public void setBreakdown(RatingBreakdown breakdown) {
        this.breakdown = breakdown;
    }

    // Metodi di utilità

    /**
     * Verifica se la risposta contiene una singola valutazione.
     * @return {@code true} se il campo `rating` non è nullo.
     */
    public boolean hasSingleRating() {
        return rating != null;
    }

    /**
     * Verifica se la risposta contiene una lista di valutazioni.
     * @return {@code true} se il campo `ratings` non è nullo e non è vuoto.
     */
    public boolean hasMultipleRatings() {
        return ratings != null && !ratings.isEmpty();
    }

    /**
     * Verifica se la risposta contiene dati statistici.
     * @return {@code true} se `averageRating` o `breakdown` non sono nulli.
     */
    public boolean hasStatistics() {
        return averageRating != null || breakdown != null;
    }

    /**
     * Restituisce un numero intero di stelle basato sulla valutazione media arrotondata.
     * @return Il numero di stelle (da 0 a 5).
     */
    public int getStarRating() {
        if (averageRating == null || averageRating <= 0) return 0;
        return (int) Math.round(averageRating);
    }

    /**
     * Restituisce una rappresentazione testuale della valutazione media.
     * <p>
     * Esempio: "★★★★☆ (4.2/5)".
     * </p>
     * @return La stringa formattata.
     */
    public String getDisplayRating() {
        if (averageRating == null || averageRating <= 0) {
            return "Non valutato";
        }
        int roundedStars = getStarRating();
        String stars = "★".repeat(roundedStars) + "☆".repeat(5 - roundedStars);
        return String.format("%s (%.1f/5)", stars, averageRating);
    }

    /**
     * Restituisce una breve descrizione della qualità del libro basata sulla valutazione media.
     *
     * @return Una stringa descrittiva (es. "Eccellente", "Buono", "Scarso").
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

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `RatingResponse`.
     * <p>
     * Utile per scopi di debugging, mostra i principali campi e lo stato dell'oggetto.
     * </p>
     * @return La stringa descrittiva.
     */
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

    /**
     * Classe interna statica che rappresenta il dettaglio della distribuzione dei voti.
     * <p>
     * Incapsula i conteggi per ciascun numero di stelle e le medie per sottocategorie
     * di valutazione (stile, contenuto, ecc.).
     * </p>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RatingBreakdown {

        /**
         * Il conteggio delle valutazioni a 5 stelle.
         */
        @JsonProperty("fiveStars")
        private int fiveStars;

        /**
         * Il conteggio delle valutazioni a 4 stelle.
         */
        @JsonProperty("fourStars")
        private int fourStars;

        /**
         * Il conteggio delle valutazioni a 3 stelle.
         */
        @JsonProperty("threeStars")
        private int threeStars;

        /**
         * Il conteggio delle valutazioni a 2 stelle.
         */
        @JsonProperty("twoStars")
        private int twoStars;

        /**
         * Il conteggio delle valutazioni a 1 stella.
         */
        @JsonProperty("oneStar")
        private int oneStar;

        /**
         * La valutazione media per lo stile del libro.
         */
        @JsonProperty("averageStyle")
        private Double averageStyle;

        /**
         * La valutazione media per il contenuto.
         */
        @JsonProperty("averageContent")
        private Double averageContent;

        /**
         * La valutazione media per la piacevolezza.
         */
        @JsonProperty("averagePleasantness")
        private Double averagePleasantness;

        /**
         * La valutazione media per l'originalità.
         */
        @JsonProperty("averageOriginality")
        private Double averageOriginality;

        /**
         * La valutazione media per l'edizione.
         */
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

        /**
         * Calcola e restituisce il numero totale di valutazioni in base alla distribuzione.
         * @return Il conteggio totale.
         */
        public int getTotalRatings() {
            return fiveStars + fourStars + threeStars + twoStars + oneStar;
        }

        /**
         * Restituisce una rappresentazione in stringa dell'oggetto {@code RatingBreakdown}.
         * @return La stringa descrittiva.
         */
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