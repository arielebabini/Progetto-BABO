package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe che rappresenta un libro
 * Condivisa tra client e server per la serializzazione JSON
 * ✅ MANTENUTO: Campo imageUrl per uso interno, rimossi solo riferimenti database
 */
@JsonIgnoreProperties(ignoreUnknown = true) // IMPORTANTE: Ignora campi sconosciuti nel JSON
public class Book {

    // Attributi con annotazioni Jackson per JSON
    @JsonProperty("id")
    private Long id;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("description")
    private String description;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("publishYear")
    private String publishYear;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("isFree")
    private Boolean isFree;

    @JsonProperty("isNew")
    private Boolean isNew;

    // CAMPI per gestire response dal server
    @JsonProperty("category")
    private String category;

    @JsonProperty("publisher")
    private String publisher;

    @JsonProperty("language")
    private String language;

    @JsonProperty("pages")
    private Integer pages;

    private int reviewCount = 0;
    private double averageRating = 0.0;

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * Restituisce la valutazione come stringa formattata
     */
    public String getFormattedRating() {
        if (averageRating > 0) {
            return String.format("%.1f", averageRating);
        }
        return "N/A";
    }

    // Costruttori
    public Book() {} // Costruttore vuoto per Jackson

    public Book(String title, String author, String description, String imageUrl) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isFree = true;
        this.isNew = false;
    }

    public Book(Long id, String title, String author, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isFree = true;
        this.isNew = false;
    }

    public Book(Long id, String isbn, String title, String author, String description, String publishYear, String imageUrl) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishYear = publishYear;
        this.imageUrl = imageUrl;
        this.isFree = true;
        this.isNew = false;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPublishYear() {
        return publishYear;
    }

    public Double getPrice() {
        return price;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public String getCategory() {
        return category;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getPages() {
        return pages;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setPublishYear(String publishYear) {
        this.publishYear = publishYear;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    // Metodi di utilità per compatibilità
    public String title() {
        return this.title;
    }

    public String author() {
        return this.author;
    }

    public String imageUrl() {
        return this.imageUrl;
    }

    /**
     * Ottiene il nome del file immagine locale (NON URL)
     * Ignora completamente gli URL esterni
     */
    public String getLocalImageFileName() {
        // Se imageUrl è già un nome file locale (senza http), usalo
        if (imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.trim().isEmpty()) {
            return sanitizeImageFileName(imageUrl);
        }

        // Altrimenti genera il nome file da ISBN o titolo
        return generateImageFileName();
    }

    /**
     * Ottiene il nome del file immagine per il caricamento sicuro
     * SEMPRE locale, mai URL esterni
     */
    public String getSafeImageFileName() {
        String localFileName = getLocalImageFileName();

        // Se è null o vuoto, usa un placeholder
        if (localFileName == null || localFileName.trim().isEmpty()) {
            return "placeholder.jpg";
        }

        return localFileName;
    }

    /**
     * Pulisce il nome del file immagine rimuovendo caratteri non validi
     */
    private String sanitizeImageFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "placeholder.jpg";
        }

        // Rimuovi eventuali path o URL parts
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }

        // Rimuovi caratteri speciali
        fileName = fileName.replaceAll("[^a-zA-Z0-9.]", "");

        // Se non ha estensione, aggiungi .jpg
        if (!fileName.toLowerCase().endsWith(".jpg") &&
                !fileName.toLowerCase().endsWith(".jpeg") &&
                !fileName.toLowerCase().endsWith(".png")) {

            // Rimuovi estensioni esistenti non supportate
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            fileName += ".jpg";
        }

        // Se troppo corto dopo la pulizia, usa placeholder
        if (fileName.length() < 5) {
            return "placeholder.jpg";
        }

        return fileName;
    }

    /**
     * Genera il nome del file immagine basato sull'ISBN
     */
    public String generateImageFileName() {
        if (isbn != null && !isbn.trim().isEmpty()) {
            // Pulisci l'ISBN e usa quello
            String cleanIsbn = isbn.replaceAll("[^a-zA-Z0-9]", "");
            if (cleanIsbn.length() > 0) {
                return cleanIsbn + ".jpg";
            }
        }

        if (title != null && !title.trim().isEmpty()) {
            // Pulisci il titolo e usa quello
            String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (cleanTitle.length() > 20) {
                cleanTitle = cleanTitle.substring(0, 20); // Limita lunghezza
            }
            if (cleanTitle.length() > 0) {
                return cleanTitle + ".jpg";
            }
        }

        return "placeholder.jpg";
    }

    /**
     * Imposta un nome file immagine locale se non presente
     */
    public void ensureLocalImageFileName() {
        if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.startsWith("http")) {
            imageUrl = generateImageFileName();
            System.out.println("📝 Impostato nome file immagine locale per '" + title + "': " + imageUrl);
        }
    }

    /**
     * Verifica se l'immagine è un URL esterno (da ignorare)
     */
    public boolean hasExternalImageUrl() {
        return imageUrl != null && imageUrl.startsWith("http");
    }

    /**
     * Verifica se l'immagine è locale (nelle risorse)
     */
    public boolean hasLocalImageFile() {
        return imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.trim().isEmpty();
    }

    /**
     * Debug per verificare lo stato dell'immagine
     */
    public void debugImageInfo() {
        System.out.println("🔍 DEBUG IMMAGINE LIBRO: " + title);
        System.out.println("  ISBN: " + isbn);
        System.out.println("  ImageURL originale: " + imageUrl);
        System.out.println("  È URL esterno: " + hasExternalImageUrl());
        System.out.println("  Ha file locale: " + hasLocalImageFile());
        System.out.println("  Nome file locale: " + getLocalImageFileName());
        System.out.println("  Nome file sicuro: " + getSafeImageFileName());
        System.out.println("  Nome generato: " + generateImageFileName());
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", publishYear='" + publishYear + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", category='" + category + '\'' +
                ", publisher='" + publisher + '\'' +
                ", language='" + language + '\'' +
                ", pages=" + pages +
                ", price=" + price +
                ", isFree=" + isFree +
                ", isNew=" + isNew +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;

        // Confronta per ISBN se disponibile, altrimenti per ID
        if (isbn != null && book.isbn != null) {
            return isbn.equals(book.isbn);
        }
        return id != null ? id.equals(book.id) : book.id == null;
    }

    @Override
    public int hashCode() {
        // Usa ISBN se disponibile, altrimenti ID
        if (isbn != null) {
            return isbn.hashCode();
        }
        return id != null ? id.hashCode() : 0;
    }
}