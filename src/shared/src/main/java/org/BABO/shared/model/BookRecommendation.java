package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * La classe `BookRecommendation` rappresenta una raccomandazione di libro da parte di un utente.
 * <p>
 * È un modello unificato, condiviso tra client e server, per garantire una corretta
 * serializzazione e deserializzazione JSON dei dati relativi alle raccomandazioni.
 * L'annotazione {@code @JsonIgnoreProperties(ignoreUnknown = true)} ignora
 * eventuali proprietà JSON non mappate, garantendo la compatibilità con future evoluzioni.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookRecommendation {

    /**
     * L'ID univoco che identifica la raccomandazione.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Lo username dell'utente che ha effettuato la raccomandazione.
     */
    @JsonProperty("recommenderUsername")
    private String recommenderUsername;

    /**
     * L'ISBN del libro per cui è stata fatta la raccomandazione.
     */
    @JsonProperty("targetBookIsbn")
    private String targetBookIsbn;

    /**
     * L'ISBN del libro che viene raccomandato.
     */
    @JsonProperty("recommendedBookIsbn")
    private String recommendedBookIsbn;

    /**
     * Il motivo per cui il libro è stato raccomandato.
     */
    @JsonProperty("reason")
    private String reason;

    /**
     * La data di creazione della raccomandazione.
     */
    @JsonProperty("createdDate")
    private String createdDate;

    /**
     * Indica se la raccomandazione è attiva.
     */
    @JsonProperty("isActive")
    private Boolean isActive;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public BookRecommendation() {}

    /**
     * Costruttore per creare una raccomandazione di base.
     *
     * @param recommenderUsername Lo username dell'utente che raccomanda.
     * @param targetBookIsbn L'ISBN del libro a cui è collegata la raccomandazione.
     * @param recommendedBookIsbn L'ISBN del libro raccomandato.
     */
    public BookRecommendation(String recommenderUsername, String targetBookIsbn, String recommendedBookIsbn) {
        this.recommenderUsername = recommenderUsername;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.isActive = true;
    }

    /**
     * Costruttore per creare una raccomandazione con un motivo.
     *
     * @param recommenderUsername Lo username dell'utente che raccomanda.
     * @param targetBookIsbn L'ISBN del libro a cui è collegata la raccomandazione.
     * @param recommendedBookIsbn L'ISBN del libro raccomandato.
     * @param reason Il motivo della raccomandazione.
     */
    public BookRecommendation(String recommenderUsername, String targetBookIsbn, String recommendedBookIsbn, String reason) {
        this.recommenderUsername = recommenderUsername;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.reason = reason;
        this.isActive = true;
    }

    /**
     * Costruttore completo per inizializzare tutti i campi.
     *
     * @param id L'ID della raccomandazione.
     * @param recommenderUsername Lo username dell'utente che raccomanda.
     * @param targetBookIsbn L'ISBN del libro a cui è collegata la raccomandazione.
     * @param recommendedBookIsbn L'ISBN del libro raccomandato.
     * @param reason Il motivo della raccomandazione.
     * @param createdDate La data di creazione.
     * @param isActive Indica se la raccomandazione è attiva.
     */
    public BookRecommendation(Long id, String recommenderUsername, String targetBookIsbn,
                              String recommendedBookIsbn, String reason, String createdDate, Boolean isActive) {
        this.id = id;
        this.recommenderUsername = recommenderUsername;
        this.targetBookIsbn = targetBookIsbn;
        this.recommendedBookIsbn = recommendedBookIsbn;
        this.reason = reason;
        this.createdDate = createdDate;
        this.isActive = isActive;
    }

    // Getters

    /**
     * Restituisce l'ID della raccomandazione.
     * @return L'ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Restituisce lo username dell'utente che ha raccomandato il libro.
     * @return Lo username.
     */
    public String getRecommenderUsername() {
        return recommenderUsername;
    }

    /**
     * Restituisce l'ISBN del libro di riferimento.
     * @return L'ISBN del libro di riferimento.
     */
    public String getTargetBookIsbn() {
        return targetBookIsbn;
    }

    /**
     * Restituisce l'ISBN del libro raccomandato.
     * @return L'ISBN del libro raccomandato.
     */
    public String getRecommendedBookIsbn() {
        return recommendedBookIsbn;
    }

    /**
     * Restituisce il motivo della raccomandazione.
     * @return Il motivo.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Restituisce la data di creazione della raccomandazione.
     * @return La data di creazione.
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Restituisce lo stato di attività della raccomandazione.
     * @return {@code true} se attiva, {@code false} altrimenti.
     */
    public Boolean getIsActive() {
        return isActive;
    }

    // Setters

    /**
     * Imposta l'ID della raccomandazione.
     * @param id Il nuovo ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Imposta lo username dell'utente che raccomanda.
     * @param recommenderUsername Il nuovo username.
     */
    public void setRecommenderUsername(String recommenderUsername) {
        this.recommenderUsername = recommenderUsername;
    }

    /**
     * Imposta l'ISBN del libro di riferimento.
     * @param targetBookIsbn Il nuovo ISBN.
     */
    public void setTargetBookIsbn(String targetBookIsbn) {
        this.targetBookIsbn = targetBookIsbn;
    }

    /**
     * Imposta l'ISBN del libro raccomandato.
     * @param recommendedBookIsbn Il nuovo ISBN.
     */
    public void setRecommendedBookIsbn(String recommendedBookIsbn) {
        this.recommendedBookIsbn = recommendedBookIsbn;
    }

    /**
     * Imposta il motivo della raccomandazione.
     * @param reason Il nuovo motivo.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Imposta la data di creazione.
     * @param createdDate La nuova data.
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Imposta lo stato di attività della raccomandazione.
     * @param isActive Il nuovo stato.
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Metodi di utilità

    /**
     * Verifica se la raccomandazione ha un motivo specificato.
     * @return {@code true} se il motivo non è nullo e non è vuoto, {@code false} altrimenti.
     */
    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    /**
     * Restituisce una versione troncata del motivo per la visualizzazione.
     * Se il motivo supera i 100 caratteri, viene troncato e vengono aggiunti i puntini di sospensione.
     *
     * @return Il motivo troncato o un messaggio di default.
     */
    public String getDisplayReason() {
        if (hasReason()) {
            return reason.length() > 100 ? reason.substring(0, 97) + "..." : reason;
        }
        return "Nessun motivo specificato";
    }

    /**
     * Restituisce una versione abbreviata e oscurata dello username dell'utente che ha raccomandato il libro.
     * <p>
     * Vengono mostrati solo i primi tre caratteri, seguiti da `***`. Se lo username è troppo corto,
     * restituisce solo `***`.
     * </p>
     * @return Lo username abbreviato.
     */
    public String getShortUsername() {
        if (recommenderUsername == null || recommenderUsername.length() <= 3) {
            return "***";
        }
        return recommenderUsername.substring(0, 3) + "***";
    }

    /**
     * Verifica se la raccomandazione è valida.
     * <p>
     * Una raccomandazione è valida se ha uno username del raccomandante, un ISBN del libro di riferimento,
     * un ISBN del libro raccomandato, e se i due ISBN non sono uguali.
     * </p>
     * @return {@code true} se la raccomandazione è valida, {@code false} altrimenti.
     */
    public boolean isValid() {
        return recommenderUsername != null && !recommenderUsername.trim().isEmpty() &&
                targetBookIsbn != null && !targetBookIsbn.trim().isEmpty() &&
                recommendedBookIsbn != null && !recommendedBookIsbn.trim().isEmpty() &&
                !targetBookIsbn.equals(recommendedBookIsbn);
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `BookRecommendation`.
     * <p>
     * Il motivo viene troncato per evitare stringhe troppo lunghe nei log.
     * </p>
     * @return La rappresentazione in stringa.
     */
    @Override
    public String toString() {
        return "BookRecommendation{" +
                "id=" + id +
                ", recommenderUsername='" + recommenderUsername + '\'' +
                ", targetBookIsbn='" + targetBookIsbn + '\'' +
                ", recommendedBookIsbn='" + recommendedBookIsbn + '\'' +
                ", reason='" + (reason != null && reason.length() > 50 ? reason.substring(0, 50) + "..." : reason) + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    /**
     * Confronta questo oggetto `BookRecommendation` con un altro per verificarne l'uguaglianza.
     * <p>
     * L'uguaglianza è basata sulla combinazione dello username del raccomandante,
     * dell'ISBN del libro di riferimento e dell'ISBN del libro raccomandato.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookRecommendation that = (BookRecommendation) o;
        return recommenderUsername != null && recommenderUsername.equals(that.recommenderUsername) &&
                targetBookIsbn != null && targetBookIsbn.equals(that.targetBookIsbn) &&
                recommendedBookIsbn != null && recommendedBookIsbn.equals(that.recommendedBookIsbn);
    }

    /**
     * Restituisce il valore di hash per questo oggetto.
     * <p>
     * Il valore di hash è calcolato sulla base dello username del raccomandante,
     * dell'ISBN del libro di riferimento e dell'ISBN del libro raccomandato.
     * </p>
     * @return Il valore di hash.
     */
    @Override
    public int hashCode() {
        int result = recommenderUsername != null ? recommenderUsername.hashCode() : 0;
        result = 31 * result + (targetBookIsbn != null ? targetBookIsbn.hashCode() : 0);
        result = 31 * result + (recommendedBookIsbn != null ? recommendedBookIsbn.hashCode() : 0);
        return result;
    }
}