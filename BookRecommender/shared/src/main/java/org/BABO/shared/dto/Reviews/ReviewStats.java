package org.BABO.shared.dto.Reviews;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Data Transfer Object (DTO) che aggrega le statistiche relative alle recensioni dei libri.
 * <p>
 * Questa classe è utilizzata per incapsulare i dati statistici calcolati lato server
 * e trasferirli in modo strutturato al client. Le statistiche includono il numero totale
 * di recensioni, la loro distribuzione per voti, la media e altri dati di interesse.
 * </p>
 */
public class ReviewStats {

    /**
     * Il numero totale di recensioni presenti nel sistema.
     */
    @JsonProperty("totalReviews")
    private int totalReviews;

    /**
     * Il numero di recensioni che includono anche una recensione testuale.
     */
    @JsonProperty("totalReviewsWithText")
    private int totalReviewsWithText;

    /**
     * Il numero totale di utenti che hanno inviato almeno una recensione.
     */
    @JsonProperty("totalUsers")
    private int totalUsers;

    /**
     * La valutazione media complessiva di tutti i libri recensiti.
     */
    @JsonProperty("averageRating")
    private double averageRating;

    /**
     * La distribuzione dei voti in un array, dove ogni indice corrisponde a un numero di stelle
     * (es. index 0 = 1 stella, index 4 = 5 stelle).
     */
    @JsonProperty("ratingsDistribution")
    private int[] ratingsDistribution;

    /**
     * Una lista di ISBN dei libri con la valutazione media più alta.
     */
    @JsonProperty("topRatedBooks")
    private List<String> topRatedBooks;

    /**
     * Una lista di username degli utenti con il maggior numero di recensioni.
     */
    @JsonProperty("mostActiveUsers")
    private List<String> mostActiveUsers;

    /**
     * Il numero di recensioni create negli ultimi 30 giorni.
     */
    @JsonProperty("recentReviewsCount")
    private int recentReviewsCount;

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Inizializza l'array di distribuzione dei voti con una dimensione di 5.
     * È richiesto dalla libreria di serializzazione Jackson.
     * </p>
     */
    public ReviewStats() {
        this.ratingsDistribution = new int[5];
    }

    // Getters e Setters

    /**
     * Restituisce il numero totale di recensioni.
     * @return Il numero totale.
     */
    public int getTotalReviews() {
        return totalReviews;
    }

    /**
     * Imposta il numero totale di recensioni.
     * @param totalReviews Il nuovo totale.
     */
    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    /**
     * Restituisce il numero di recensioni con testo.
     * @return Il numero di recensioni testuali.
     */
    public int getTotalReviewsWithText() {
        return totalReviewsWithText;
    }

    /**
     * Imposta il numero di recensioni con testo.
     * @param totalReviewsWithText Il nuovo totale.
     */
    public void setTotalReviewsWithText(int totalReviewsWithText) {
        this.totalReviewsWithText = totalReviewsWithText;
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
     * @param totalUsers Il nuovo totale.
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

    /**
     * Restituisce l'array con la distribuzione dei voti.
     * @return L'array dei voti.
     */
    public int[] getRatingsDistribution() {
        return ratingsDistribution;
    }

    /**
     * Imposta l'array con la distribuzione dei voti.
     * @param ratingsDistribution Il nuovo array.
     */
    public void setRatingsDistribution(int[] ratingsDistribution) {
        this.ratingsDistribution = ratingsDistribution;
    }

    /**
     * Restituisce la lista degli ISBN dei libri più votati.
     * @return La lista dei libri.
     */
    public List<String> getTopRatedBooks() {
        return topRatedBooks;
    }

    /**
     * Imposta la lista degli ISBN dei libri più votati.
     * @param topRatedBooks La nuova lista.
     */
    public void setTopRatedBooks(List<String> topRatedBooks) {
        this.topRatedBooks = topRatedBooks;
    }

    /**
     * Restituisce la lista degli username degli utenti più attivi.
     * @return La lista degli utenti.
     */
    public List<String> getMostActiveUsers() {
        return mostActiveUsers;
    }

    /**
     * Imposta la lista degli username degli utenti più attivi.
     * @param mostActiveUsers La nuova lista.
     */
    public void setMostActiveUsers(List<String> mostActiveUsers) {
        this.mostActiveUsers = mostActiveUsers;
    }

    /**
     * Restituisce il numero di recensioni recenti (ultimi 30 giorni).
     * @return Il numero di recensioni recenti.
     */
    public int getRecentReviewsCount() {
        return recentReviewsCount;
    }

    /**
     * Imposta il numero di recensioni recenti.
     * @param recentReviewsCount Il nuovo numero.
     */
    public void setRecentReviewsCount(int recentReviewsCount) {
        this.recentReviewsCount = recentReviewsCount;
    }

    // Metodi di utilità

    /**
     * Calcola la percentuale di recensioni per ciascun voto.
     * <p>
     * Se il numero totale di recensioni è maggiore di zero, calcola la percentuale
     * di ogni voto (da 1 a 5 stelle) in base alla distribuzione.
     * </p>
     * @return Un array di double con le percentuali.
     */
    public double[] getRatingsPercentages() {
        double[] percentages = new double[5];
        int total = getTotalReviews();

        if (total > 0) {
            for (int i = 0; i < 5; i++) {
                percentages[i] = (ratingsDistribution[i] * 100.0) / total;
            }
        }
        return percentages;
    }

    /**
     * Identifica e restituisce il voto (numero di stelle) più comune.
     *
     * @return Il voto più frequente (1-5), o 5 come valore di default se non ci sono dati.
     */
    public int getMostCommonRating() {
        int maxCount = 0;
        int mostCommon = 5;

        for (int i = 0; i < 5; i++) {
            if (ratingsDistribution[i] > maxCount) {
                maxCount = ratingsDistribution[i];
                mostCommon = i + 1;
            }
        }
        return mostCommon;
    }

    /**
     * Verifica se sono presenti dati sufficienti per generare statistiche significative.
     *
     * @return {@code true} se il numero totale di recensioni è maggiore di 0, {@code false} altrimenti.
     */
    public boolean hasEnoughData() {
        return totalReviews > 0;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto {@code ReviewStats}.
     * <p>
     * Utile per scopi di debugging e logging, mostra i principali campi statistici.
     * </p>
     * @return La rappresentazione in stringa.
     */
    @Override
    public String toString() {
        return "ReviewStats{" +
                "totalReviews=" + totalReviews +
                ", totalReviewsWithText=" + totalReviewsWithText +
                ", totalUsers=" + totalUsers +
                ", averageRating=" + averageRating +
                ", recentReviewsCount=" + recentReviewsCount +
                '}';
    }
}