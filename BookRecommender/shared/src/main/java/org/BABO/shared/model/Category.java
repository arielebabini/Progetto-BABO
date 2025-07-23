package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe che rappresenta una categoria
 * Condivisa tra client e server per la serializzazione JSON
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
}