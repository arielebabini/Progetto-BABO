package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * La classe `Category` rappresenta una categoria di libri.
 * <p>
 * È un modello unificato, condiviso tra client e server, per garantire una corretta
 * serializzazione e deserializzazione JSON dei dati relativi alle categorie.
 * </p>
 */
public class Category {

    /**
     * L'ID univoco della categoria.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Il nome della categoria. È un campo `final` in quanto immutabile.
     */
    @JsonProperty("name")
    private final String name;

    /**
     * L'URL dell'immagine principale associata alla categoria. È un campo `final` in quanto immutabile.
     */
    @JsonProperty("imageUrl")
    private final String imageUrl;

    /**
     * Il percorso assoluto o URL dell'icona associata alla categoria. È un campo `final` in quanto immutabile.
     */
    @JsonProperty("iconPath")
    private final String iconPath;

    /**
     * La descrizione della categoria.
     */
    @JsonProperty("description")
    private String description;

    /**
     * Costruttore di default.
     * <p>
     * È richiesto dalla libreria di serializzazione Jackson per la deserializzazione JSON.
     * I campi `final` vengono inizializzati a `null`.
     * </p>
     */
    public Category() {
        this.name = null;
        this.imageUrl = null;
        this.iconPath = null;
    }

    /**
     * Costruttore principale per la creazione di una categoria, compatibile con il codice esistente.
     *
     * @param name Il nome della categoria.
     * @param imageUrl L'URL dell'immagine associata.
     * @param iconPath Il percorso o URL dell'icona.
     */
    public Category(String name, String imageUrl, String iconPath) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.iconPath = iconPath;
    }

    /**
     * Costruttore completo che include l'ID e la descrizione.
     *
     * @param id L'ID univoco della categoria.
     * @param name Il nome della categoria.
     * @param description La descrizione della categoria.
     * @param imageUrl L'URL dell'immagine.
     * @param iconPath Il percorso o URL dell'icona.
     */
    public Category(Long id, String name, String description, String imageUrl, String iconPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.iconPath = iconPath;
    }

    // Getters

    /**
     * Restituisce il nome della categoria.
     * @return Il nome della categoria.
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce l'URL dell'immagine associata.
     * @return L'URL dell'immagine.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Restituisce il percorso o URL dell'icona associata.
     * @return Il percorso dell'icona.
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * Restituisce l'ID della categoria.
     * @return L'ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Restituisce la descrizione della categoria.
     * @return La descrizione.
     */
    public String getDescription() {
        return description;
    }

    // Setters

    /**
     * Imposta l'ID della categoria.
     * @param id Il nuovo ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Imposta la descrizione della categoria.
     * @param description La nuova descrizione.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Genera un nome di file per un'icona basato sul nome della categoria.
     * <p>
     * Il nome viene pulito da caratteri speciali e convertito in minuscolo,
     * con l'aggiunta dell'estensione `.png`. Questo metodo è utile per
     * l'associazione con icone locali.
     * </p>
     * @return Il nome del file generato, o un nome di default se il nome della categoria non è valido.
     */
    public String generateIconFileName() {
        if (name != null && !name.trim().isEmpty()) {
            String cleanName = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (cleanName.length() > 0) {
                return cleanName + ".png";
            }
        }
        return "default_category.png";
    }

    /**
     * Restituisce il nome del file dell'icona per il caricamento sicuro.
     * <p>
     * Se il campo `iconPath` non è un URL (`http`), viene restituito direttamente.
     * Altrimenti, viene generato un nome di file locale utilizzando `generateIconFileName()`.
     * </p>
     * @return Il nome del file dell'icona.
     */
    public String getSafeIconFileName() {
        if (iconPath != null && !iconPath.startsWith("http") && !iconPath.trim().isEmpty()) {
            return iconPath;
        }
        return generateIconFileName();
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto `Category`.
     * @return La rappresentazione in stringa.
     */
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", iconPath='" + iconPath + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    /**
     * Confronta questo oggetto `Category` con un altro per verificarne l'uguaglianza.
     * <p>
     * La verifica è basata sull'ID se disponibile, altrimenti sul nome della categoria.
     * </p>
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;

        if (id != null && category.id != null) {
            return id.equals(category.id);
        }
        return name != null ? name.equals(category.name) : category.name == null;
    }

    /**
     * Restituisce il valore di hash per questo oggetto.
     * <p>
     * Il valore di hash è calcolato sull'ID se disponibile, altrimenti sul nome.
     * </p>
     * @return Il valore di hash.
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return name != null ? name.hashCode() : 0;
    }
}