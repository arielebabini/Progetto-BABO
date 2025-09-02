package org.BABO.shared.dto.Reviews;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) per incapsulare la risposta che contiene le statistiche delle recensioni.
 * <p>
 * Questa classe è utilizzata per standardizzare la comunicazione dal server al client,
 * fornendo lo stato dell'operazione, un messaggio di feedback e un oggetto {@link ReviewStats}
 * che racchiude i dati statistici richiesti.
 * </p>
 */
public class ReviewStatsResponse {

    /**
     * Indica se la richiesta per le statistiche delle recensioni ha avuto successo.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Un messaggio di testo che fornisce dettagli sul risultato dell'operazione.
     */
    @JsonProperty("message")
    private String message;

    /**
     * L'oggetto che contiene le statistiche aggregate delle recensioni.
     * Può essere {@code null} se l'operazione non ha avuto successo.
     */
    @JsonProperty("stats")
    private ReviewStats stats;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public ReviewStatsResponse() {}

    /**
     * Costruttore completo per inizializzare tutti i campi della risposta.
     *
     * @param success Lo stato di successo dell'operazione.
     * @param message Il messaggio di feedback.
     * @param stats L'oggetto con le statistiche delle recensioni.
     */
    public ReviewStatsResponse(boolean success, String message, ReviewStats stats) {
        this.success = success;
        this.message = message;
        this.stats = stats;
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
     * Restituisce l'oggetto che contiene le statistiche.
     * @return L'oggetto {@link ReviewStats}.
     */
    public ReviewStats getStats() {
        return stats;
    }

    /**
     * Imposta l'oggetto con le statistiche delle recensioni.
     * @param stats Il nuovo oggetto {@link ReviewStats}.
     */
    public void setStats(ReviewStats stats) {
        this.stats = stats;
    }
}