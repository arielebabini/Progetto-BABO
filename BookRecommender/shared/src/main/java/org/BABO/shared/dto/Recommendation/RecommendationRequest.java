package org.BABO.shared.dto.Recommendation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) che incapsula una richiesta per creare, aggiornare o eliminare una raccomandazione.
 * <p>
 * Questa classe è utilizzata per standardizzare il formato dei dati inviati dal client al server
 * per le operazioni sulle raccomandazioni. Contiene i dati necessari per identificare l'utente,
 * il libro di riferimento (`targetBookIsbn`), il libro consigliato (`recommendedBookIsbn`)
 * e un'opzionale motivazione.
 * L'annotazione {@code @JsonIgnoreProperties(ignoreUnknown = true)} garantisce la compatibilità
 * del modello anche se il server inviasse campi aggiuntivi.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationRequest {

    /**
     * Lo username dell'utente che sta creando la raccomandazione.
     */
    @JsonProperty("username")
    private String username;

    /**
     * L'ISBN del libro a cui è collegata la raccomandazione (es. "raccomando questo libro a chi ha letto...").
     */
    @JsonProperty("targetBookIsbn")
    private String targetBookIsbn;

    /**
     * L'ISBN del libro che viene raccomandato.
     */
    @JsonProperty("recommendedBookIsbn")
    private String recommendedBookIsbn;

    /**
     * La motivazione opzionale per cui il libro viene raccomandato.
     */
    @JsonProperty("reason")
    private String reason;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public RecommendationRequest() {}

    /**
     * Costruttore per creare una richiesta di raccomandazione con i campi essenziali.
     *
     * @param username Lo username dell'utente.
     * @param targetBookIsbn L'ISBN del libro di riferimento.
     * @param recommendedBookIsbn L'ISBN del libro consigliato.
     */
    public RecommendationRequest(String username, String targetBookIsbn, String recommendedBookIsbn) {
        this.username = username;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
    }

    /**
     * Costruttore completo per creare una richiesta con tutti i campi, inclusa la motivazione.
     *
     * @param username Lo username dell'utente.
     * @param targetBookIsbn L'ISBN del libro di riferimento.
     * @param recommendedBookIsbn L'ISBN del libro consigliato.
     * @param reason La motivazione della raccomandazione.
     */
    public RecommendationRequest(String username, String targetBookIsbn, String recommendedBookIsbn, String reason) {
        this.username = username;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.reason = reason;
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
     * Restituisce l'ISBN del libro di riferimento.
     * @return L'ISBN del libro target.
     */
    public String getTargetBookIsbn() {
        return targetBookIsbn;
    }

    /**
     * Restituisce l'ISBN del libro consigliato.
     * @return L'ISBN del libro consigliato.
     */
    public String getRecommendedBookIsbn() {
        return recommendedBookIsbn;
    }

    /**
     * Restituisce la motivazione della raccomandazione.
     * @return La motivazione.
     */
    public String getReason() {
        return reason;
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
     * Imposta l'ISBN del libro di riferimento.
     * @param targetBookIsbn Il nuovo ISBN.
     */
    public void setTargetBookIsbn(String targetBookIsbn) {
        this.targetBookIsbn = targetBookIsbn;
    }

    /**
     * Imposta l'ISBN del libro consigliato.
     * @param recommendedBookIsbn Il nuovo ISBN.
     */
    public void setRecommendedBookIsbn(String recommendedBookIsbn) {
        this.recommendedBookIsbn = recommendedBookIsbn;
    }

    /**
     * Imposta la motivazione della raccomandazione.
     * @param reason La nuova motivazione.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    // Metodi di validazione e utilità

    /**
     * Verifica se la richiesta è valida.
     * <p>
     * Una richiesta è considerata valida se `username`, `targetBookIsbn` e
     * `recommendedBookIsbn` non sono nulli, non sono vuoti e sono diversi tra loro.
     * </p>
     * @return {@code true} se la richiesta è valida, {@code false} altrimenti.
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                targetBookIsbn != null && !targetBookIsbn.trim().isEmpty() &&
                recommendedBookIsbn != null && !recommendedBookIsbn.trim().isEmpty() &&
                !targetBookIsbn.equals(recommendedBookIsbn);
    }

    /**
     * Restituisce una stringa con tutti gli errori di validazione della richiesta.
     * <p>
     * Questo metodo può essere utilizzato per generare messaggi di feedback
     * specifici per l'utente in caso di richiesta non valida.
     * </p>
     * @return Una stringa contenente gli errori, vuota se la richiesta è valida.
     */
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();

        if (username == null || username.trim().isEmpty()) {
            errors.append("Username è obbligatorio. ");
        }
        if (targetBookIsbn == null || targetBookIsbn.trim().isEmpty()) {
            errors.append("ISBN del libro target è obbligatorio. ");
        }
        if (recommendedBookIsbn == null || recommendedBookIsbn.trim().isEmpty()) {
            errors.append("ISBN del libro consigliato è obbligatorio. ");
        }
        if (targetBookIsbn != null && recommendedBookIsbn != null &&
                targetBookIsbn.equals(recommendedBookIsbn)) {
            errors.append("Non puoi consigliare lo stesso libro. ");
        }
        if (reason != null && reason.length() > 500) {
            errors.append("Il motivo non può superare i 500 caratteri. ");
        }

        return errors.toString().trim();
    }

    /**
     * Verifica se la richiesta include una motivazione.
     *
     * @return {@code true} se il campo `reason` non è nullo e non è vuoto, {@code false} altrimenti.
     */
    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    /**
     * Restituisce la motivazione pulita e formattata.
     * <p>
     * Il metodo rimuove gli spazi bianchi iniziali e finali e, se necessario,
     * tronca il testo a un massimo di 500 caratteri, aggiungendo i puntini di sospensione.
     * </p>
     * @return La motivazione pulita o {@code null} se il campo è nullo o vuoto.
     */
    public String getCleanReason() {
        if (reason == null) return null;

        String cleaned = reason.trim();
        if (cleaned.isEmpty()) return null;

        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 500) + "...";
        }
        return cleaned;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto {@code RecommendationRequest}.
     * <p>
     * Utile per scopi di debugging, mostra i principali campi e tronca il campo `reason`
     * per evitare stampe eccessivamente lunghe.
     * </p>
     * @return La rappresentazione in stringa.
     */
    @Override
    public String toString() {
        return "RecommendationRequest{" +
                "username='" + username + '\'' +
                ", targetBookIsbn='" + targetBookIsbn + '\'' +
                ", recommendedBookIsbn='" + recommendedBookIsbn + '\'' +
                ", reason='" + (reason != null ? reason.substring(0, Math.min(50, reason.length())) + "..." : "null") + '\'' +
                '}';
    }

    /**
     * Confronta questo oggetto `RecommendationRequest` con un altro per verificarne l'uguaglianza.
     * <p>
     * Due richieste sono considerate uguali se i campi `username`, `targetBookIsbn`
     * e `recommendedBookIsbn` sono identici.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationRequest that = (RecommendationRequest) o;
        return username != null && username.equals(that.username) &&
                targetBookIsbn != null && targetBookIsbn.equals(that.targetBookIsbn) &&
                recommendedBookIsbn != null && recommendedBookIsbn.equals(that.recommendedBookIsbn);
    }

    /**
     * Restituisce il valore di hash per questo oggetto.
     * <p>
     * Il valore di hash è calcolato sulla base dei campi `username`, `targetBookIsbn`
     * e `recommendedBookIsbn`.
     * </p>
     * @return Il valore di hash.
     */
    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (targetBookIsbn != null ? targetBookIsbn.hashCode() : 0);
        result = 31 * result + (recommendedBookIsbn != null ? recommendedBookIsbn.hashCode() : 0);
        return result;
    }
}