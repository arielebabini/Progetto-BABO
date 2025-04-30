package com.mycompany.mavenproject1;

// Classe che rappresenta una categoria
public class Category {
    // Nome della categoria
    private final String name;
    // URL dell'immagine associata alla categoria (può essere un URL o un file locale)
    private final String imageUrl;
    // Percorso assoluto o URL dell'icona associata alla categoria
    private final String iconPath;

    // Costruttore della classe Category che inizializza i campi name, imageUrl e iconPath
    public Category(String name, String imageUrl, String iconPath) {
        this.name = name; // Assegna il nome della categoria
        this.imageUrl = imageUrl; // Assegna l'URL dell'immagine
        this.iconPath = iconPath; // Assegna il percorso o URL dell'icona
    }

    // Metodo per ottenere il nome della categoria
    public String getName() {
        return name;
    }

    // Metodo per ottenere l'URL dell'immagine associata alla categoria
    public String getImageUrl() {
        return imageUrl;
    }
    
    // Metodo per ottenere il percorso o URL dell'icona associata alla categoria
    public String getIconPath() {
        return iconPath;
    }
}