package com.mycompany.mavenproject1;

// Classe che rappresenta un libro
public class Book {
    // Attributi della classe Book
    String title; // Titolo del libro
    String author; // Autore del libro
    String description; // Descrizione del libro
    String imageUrl; // URL dell'immagine del libro

    // Costruttore della classe Book
    public Book(String title, String author, String description, String imageUrl) {
        this.title = title; // Inizializza il titolo
        this.author = author; // Inizializza l'autore
        this.description = description; // Inizializza la descrizione
        this.imageUrl = imageUrl; // Inizializza l'URL dell'immagine
    }

    // Metodo per ottenere il titolo del libro
    public String getTitle() {
        return title;
    }

    // Metodo per ottenere l'autore del libro
    public String getAuthor() {
        return author;
    }

    // Metodo per ottenere la descrizione del libro
    public String getDescription() {
        return description;
    }

    // Metodo per ottenere l'URL dell'immagine del libro
    public String getImageUrl() {
        return imageUrl;
    }

    // Metodo per impostare il titolo del libro
    public void setTitle(String title) {
        this.title = title;
    }

    // Metodo per impostare l'autore del libro
    public void setAuthor(String author) {
        this.author = author;
    }

    // Metodo per impostare la descrizione del libro
    public void setDescription(String description) {
        this.description = description;
    }

    // Metodo per impostare l'URL dell'immagine del libro
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}