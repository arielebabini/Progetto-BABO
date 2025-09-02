package org.BABO.shared.dto.Reviews;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.BABO.shared.model.Review;
import java.util.List;

/**
 * Data Transfer Object (DTO) che incapsula la risposta per le operazioni di gestione delle recensioni.
 * <p>
 * Questa classe è utilizzata principalmente per le operazioni di amministrazione e query
 * sulle recensioni. Fornisce un feedback sullo stato dell'operazione e una lista
 * di recensioni richieste, insieme a dati statistici di riepilogo.
 * </p>
 */
public class ReviewsResponse {

    /**
     * Indica se l'operazione di gestione delle recensioni ha avuto successo.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Un messaggio di testo che fornisce dettagli sul risultato dell'operazione.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Una lista di oggetti {@link Review} correlata alla richiesta.
     * Può essere {@code null} se l'operazione non restituisce recensioni.
     */
    @JsonProperty("reviews")
    private List<Review> reviews;

    /**
     * Il numero totale di recensioni presenti nella lista.
     */
    @JsonProperty("totalReviews")
    private int totalReviews;

    /**
     * Il numero totale di utenti che hanno recensito.
     */
    @JsonProperty("totalUsers")
    private int totalUsers;

    /**
     * La valutazione media complessiva.
     */
    @JsonProperty("averageRating")
    private double averageRating;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public ReviewsResponse() {}

    /**
     * Costruttore per creare una risposta con stato, messaggio e una lista di recensioni.
     * <p>
     * Il numero totale di recensioni viene calcolato automaticamente dalla dimensione
     * della lista fornita.
     * </p>
     * @param success Lo stato di successo dell'operazione.
     * @param message Il messaggio di feedback.
     * @param reviews La lista di recensioni.
     */
    public ReviewsResponse(boolean success, String message, List<Review> reviews) {
        this.success = success;
        this.message = message;
        this.reviews = reviews;
        if (reviews != null) {
            this.totalReviews = reviews.size();
        }
    }

    // Getters e Setters

    /**
     * Restituisce lo stato di successo della risposta.
     * @return {@code true} se l'operazione è andata a buon fine, {@code false} altrimenti.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Imposta lo stato di successo della risposta.
     * @param success Il nuovo stato.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Restituisce il messaggio di feedback della risposta.
     * @return Il messaggio.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Imposta il messaggio di feedback della risposta.
     * @param message Il nuovo messaggio.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Restituisce la lista di recensioni.
     * @return La lista di oggetti {@link Review}.
     */
    public List<Review> getReviews() {
        return reviews;
    }

    /**
     * Imposta la lista di recensioni e aggiorna il conteggio totale.
     * @param reviews La nuova lista di recensioni.
     */
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        if (reviews != null) {
            this.totalReviews = reviews.size();
        }
    }

    /**
     * Restituisce il numero totale di recensioni nella lista.
     * @return Il numero totale.
     */
    public int getTotalReviews() {
        return totalReviews;
    }

    /**
     * Imposta il numero totale di recensioni.
     * @param totalReviews Il nuovo numero.
     */
    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    /**
     * Restituisce il numero totale di utenti che hanno recensito.
     * @return Il numero totale di utenti.
     */
    public int getTotalUsers() {
        return totalUsers;
    }

    /**
     * Imposta il numero totale di utenti che hanno recensito.
     * @param totalUsers Il nuovo numero.
     */
    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    /**
     * Restituisce la valutazione media complessiva.
     * @return La media.
     */
    public double getAverageRating() {
        return averageRating;
    }

    /**
     * Imposta la valutazione media complessiva.
     * @param averageRating La nuova media.
     */
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}