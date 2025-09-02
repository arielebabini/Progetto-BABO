package org.BABO.shared.dto.Recommendation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.model.Book;

import java.util.List;

/**
 * Data Transfer Object (DTO) che incapsula la risposta per le operazioni relative alle raccomandazioni.
 * <p>
 * Questa classe è utilizzata per standardizzare la comunicazione tra il server e il client,
 * fornendo lo stato dell'operazione, un messaggio di feedback e vari dati correlati
 * alle raccomandazioni, come una singola raccomandazione, una lista di raccomandazioni
 * o una lista di libri raccomandati.
 * L'annotazione {@code @JsonIgnoreProperties(ignoreUnknown = true)} ignora eventuali
 * proprietà JSON sconosciute, garantendo la compatibilità con future versioni dell'API.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponse {

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
     * L'oggetto di una singola raccomandazione di libro.
     * Utilizzato per operazioni che restituiscono un singolo elemento.
     */
    @JsonProperty("recommendation")
    private BookRecommendation recommendation;

    /**
     * Una lista di oggetti {@link BookRecommendation}.
     * Utilizzato per operazioni che restituiscono più raccomandazioni.
     */
    @JsonProperty("recommendations")
    private List<BookRecommendation> recommendations;

    /**
     * Una lista di oggetti {@link Book} che corrispondono alle raccomandazioni.
     * Utile per restituire i dettagli dei libri raccomandati.
     */
    @JsonProperty("recommendedBooks")
    private List<Book> recommendedBooks;

    /**
     * Flag che indica se l'utente ha il permesso di raccomandare per il libro specificato.
     */
    @JsonProperty("canRecommend")
    private Boolean canRecommend;

    /**
     * Il numero attuale di raccomandazioni effettuate dall'utente.
     */
    @JsonProperty("currentRecommendationsCount")
    private Integer currentRecommendationsCount;

    /**
     * Il numero massimo di raccomandazioni consentite per l'utente.
     */
    @JsonProperty("maxRecommendations")
    private Integer maxRecommendations;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public RecommendationResponse() {}

    /**
     * Costruttore per una risposta base con successo e messaggio.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     */
    public RecommendationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Costruttore per una risposta che include una singola raccomandazione.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param recommendation La raccomandazione restituita.
     */
    public RecommendationResponse(boolean success, String message, BookRecommendation recommendation) {
        this.success = success;
        this.message = message;
        this.recommendation = recommendation;
    }

    /**
     * Costruttore per una risposta che include una lista di raccomandazioni.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param recommendations La lista di raccomandazioni restituite.
     */
    public RecommendationResponse(boolean success, String message, List<BookRecommendation> recommendations) {
        this.success = success;
        this.message = message;
        this.recommendations = recommendations;
    }

    /**
     * Costruttore per una risposta che include una lista di raccomandazioni e i libri correlati.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param recommendations La lista di raccomandazioni.
     * @param recommendedBooks La lista dei libri raccomandati.
     */
    public RecommendationResponse(boolean success, String message, List<BookRecommendation> recommendations,
                                  List<Book> recommendedBooks) {
        this.success = success;
        this.message = message;
        this.recommendations = recommendations;
        this.recommendedBooks = recommendedBooks;
    }

    /**
     * Costruttore per una risposta che fornisce informazioni sui permessi di raccomandazione.
     *
     * @param success Lo stato di successo.
     * @param message Il messaggio di feedback.
     * @param canRecommend Indica se l'utente può raccomandare.
     * @param currentCount Il numero attuale di raccomandazioni dell'utente.
     * @param maxRecommendations Il limite massimo di raccomandazioni.
     */
    public RecommendationResponse(boolean success, String message, Boolean canRecommend,
                                  Integer currentCount, Integer maxRecommendations) {
        this.success = success;
        this.message = message;
        this.canRecommend = canRecommend;
        this.currentRecommendationsCount = currentCount;
        this.maxRecommendations = maxRecommendations;
    }

    // Getters

    /**
     * Restituisce lo stato di successo della risposta.
     * @return {@code true} se l'operazione è andata a buon fine, {@code false} altrimenti.
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
     * Restituisce la singola raccomandazione.
     * @return L'oggetto {@link BookRecommendation}.
     */
    public BookRecommendation getRecommendation() {
        return recommendation;
    }

    /**
     * Restituisce la lista di raccomandazioni.
     * @return La lista di oggetti {@link BookRecommendation}.
     */
    public List<BookRecommendation> getRecommendations() {
        return recommendations;
    }

    /**
     * Restituisce la lista dei libri raccomandati.
     * @return La lista di oggetti {@link Book}.
     */
    public List<Book> getRecommendedBooks() {
        return recommendedBooks;
    }

    /**
     * Restituisce il flag che indica i permessi di raccomandazione.
     * @return {@code true} se l'utente può raccomandare, {@code false} altrimenti.
     */
    public Boolean getCanRecommend() {
        return canRecommend;
    }

    /**
     * Restituisce il numero attuale di raccomandazioni dell'utente.
     * @return Il conteggio attuale.
     */
    public Integer getCurrentRecommendationsCount() {
        return currentRecommendationsCount;
    }

    /**
     * Restituisce il numero massimo di raccomandazioni consentite.
     * @return Il limite massimo.
     */
    public Integer getMaxRecommendations() {
        return maxRecommendations;
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
     * Imposta la singola raccomandazione.
     * @param recommendation La nuova raccomandazione.
     */
    public void setRecommendation(BookRecommendation recommendation) {
        this.recommendation = recommendation;
    }

    /**
     * Imposta la lista di raccomandazioni.
     * @param recommendations La nuova lista.
     */
    public void setRecommendations(List<BookRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    /**
     * Imposta la lista dei libri raccomandati.
     * @param recommendedBooks La nuova lista.
     */
    public void setRecommendedBooks(List<Book> recommendedBooks) {
        this.recommendedBooks = recommendedBooks;
    }

    /**
     * Imposta il flag dei permessi di raccomandazione.
     * @param canRecommend Il nuovo stato.
     */
    public void setCanRecommend(Boolean canRecommend) {
        this.canRecommend = canRecommend;
    }

    /**
     * Imposta il numero attuale di raccomandazioni dell'utente.
     * @param currentRecommendationsCount Il nuovo conteggio.
     */
    public void setCurrentRecommendationsCount(Integer currentRecommendationsCount) {
        this.currentRecommendationsCount = currentRecommendationsCount;
    }

    /**
     * Imposta il numero massimo di raccomandazioni.
     * @param maxRecommendations Il nuovo limite massimo.
     */
    public void setMaxRecommendations(Integer maxRecommendations) {
        this.maxRecommendations = maxRecommendations;
    }

    // Metodi di utilità

    /**
     * Verifica se la risposta contiene una singola raccomandazione.
     *
     * @return {@code true} se {@code recommendation} non è nullo, {@code false} altrimenti.
     */
    public boolean hasSingleRecommendation() {
        return recommendation != null;
    }

    /**
     * Verifica se la risposta contiene una lista di raccomandazioni.
     *
     * @return {@code true} se {@code recommendations} non è nullo e non è vuota, {@code false} altrimenti.
     */
    public boolean hasMultipleRecommendations() {
        return recommendations != null && !recommendations.isEmpty();
    }

    /**
     * Verifica se la risposta contiene una lista di libri raccomandati.
     *
     * @return {@code true} se {@code recommendedBooks} non è nullo e non è vuota, {@code false} altrimenti.
     */
    public boolean hasRecommendedBooks() {
        return recommendedBooks != null && !recommendedBooks.isEmpty();
    }

    /**
     * Restituisce il numero di raccomandazioni nella lista.
     *
     * @return Il numero di elementi nella lista {@code recommendations}, o 0 se la lista è nulla.
     */
    public int getRecommendationsCount() {
        return recommendations != null ? recommendations.size() : 0;
    }

    /**
     * Restituisce il numero di libri raccomandati nella lista.
     *
     * @return Il numero di elementi nella lista {@code recommendedBooks}, o 0 se la lista è nulla.
     */
    public int getRecommendedBooksCount() {
        return recommendedBooks != null ? recommendedBooks.size() : 0;
    }

    /**
     * Verifica se l'utente può aggiungere altre raccomandazioni.
     *
     * @return {@code true} se l'utente può raccomandare e non ha superato il limite massimo, {@code false} altrimenti.
     */
    public boolean canAddMoreRecommendations() {
        if (canRecommend == null || !canRecommend) return false;
        if (maxRecommendations == null || currentRecommendationsCount == null) return true;
        return currentRecommendationsCount < maxRecommendations;
    }

    /**
     * Restituisce il numero di "slot" di raccomandazioni rimanenti.
     *
     * @return Il numero di raccomandazioni che l'utente può ancora fare, o 0 se il limite è stato raggiunto o non è definito.
     */
    public int getRemainingRecommendationsSlots() {
        if (maxRecommendations == null || currentRecommendationsCount == null) return 0;
        return Math.max(0, maxRecommendations - currentRecommendationsCount);
    }

    /**
     * Genera un messaggio di testo sullo stato dei permessi di raccomandazione.
     *
     * @return Una stringa descrittiva dei permessi dell'utente.
     */
    public String getPermissionMessage() {
        if (canRecommend == null || !canRecommend) {
            return "Non puoi consigliare libri per questo titolo";
        }
        if (maxRecommendations != null && currentRecommendationsCount != null) {
            int remaining = getRemainingRecommendationsSlots();
            if (remaining <= 0) {
                return "Hai raggiunto il limite massimo di " + maxRecommendations + " raccomandazioni";
            } else {
                return "Puoi aggiungere ancora " + remaining + " raccomandazioni";
            }
        }
        return "Puoi consigliare libri per questo titolo";
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto {@code RecommendationResponse}.
     * <p>
     * Utile per scopi di debugging e logging.
     * </p>
     * @return La rappresentazione in stringa.
     */
    @Override
    public String toString() {
        return "RecommendationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", recommendation=" + recommendation +
                ", recommendationsCount=" + getRecommendationsCount() +
                ", recommendedBooksCount=" + getRecommendedBooksCount() +
                ", canRecommend=" + canRecommend +
                ", currentRecommendationsCount=" + currentRecommendationsCount +
                ", maxRecommendations=" + maxRecommendations +
                '}';
    }
}