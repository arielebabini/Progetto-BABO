package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe che rappresenta una categoria
 * Condivisa tra client e server per la serializzazione JSON
 * ✅ MANTENUTO: Campo imageUrl per uso interno, rimossi solo riferimenti database
 */
public class Category {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private final String name; // Nome della categoria

    @JsonProperty("imageUrl")
    private final String imageUrl; // URL dell'immagine associata alla categoria

    @JsonProperty("iconPath")
    private final String iconPath; // Percorso assoluto o URL dell'icona associata alla categoria

    @JsonProperty("description")
    private String description;

    // Costruttore vuoto per Jackson
    public Category() {
        this.name = null;
        this.imageUrl = null;
        this.iconPath = null;
    }

    // Costruttore della classe Category (compatibile con codice esistente)
    public Category(String name, String imageUrl, String iconPath) {
        this.name = name; // Assegna il nome della categoria
        this.imageUrl = imageUrl; // Assegna l'URL dell'immagine
        this.iconPath = iconPath; // Assegna il percorso o URL dell'icona
    }

    // Costruttore con ID
    public Category(Long id, String name, String description, String imageUrl, String iconPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.iconPath = iconPath;
    }

    // Getters (mantengo compatibilità con codice esistente)
    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getIconPath() {
        return iconPath;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Genera il nome del file icona basato sul nome della categoria
     * Per uso con ImageUtils per caricare icone locali
     */
    public String generateIconFileName() {
        if (name != null && !name.trim().isEmpty()) {
            // Pulisci il nome della categoria e usa quello
            String cleanName = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (cleanName.length() > 0) {
                return cleanName + ".png"; // Le icone sono tipicamente PNG
            }
        }
        return "default_category.png";
    }

    /**
     * Ottiene il nome del file icona per il caricamento sicuro
     * SEMPRE locale, basato sul nome della categoria
     */
    public String getSafeIconFileName() {
        // Se iconPath è già un nome file locale, usalo
        if (iconPath != null && !iconPath.startsWith("http") && !iconPath.trim().isEmpty()) {
            return iconPath;
        }
        // Altrimenti genera dal nome
        return generateIconFileName();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;

        // Confronta per ID se disponibile, altrimenti per nome
        if (id != null && category.id != null) {
            return id.equals(category.id);
        }
        return name != null ? name.equals(category.name) : category.name == null;
    }

    @Override
    public int hashCode() {
        // Usa ID se disponibile, altrimenti nome
        if (id != null) {
            return id.hashCode();
        }
        return name != null ? name.hashCode() : 0;
    }
}