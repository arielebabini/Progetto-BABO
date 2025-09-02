package org.BABO.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * La classe `Book` rappresenta un libro all'interno del sistema.
 * <p>
 * È un modello unificato, condiviso tra client e server, per garantire una corretta
 * serializzazione e deserializzazione JSON. L'annotazione {@code @JsonIgnoreProperties(ignoreUnknown = true)}
 * è fondamentale per ignorare campi non mappati durante la deserializzazione, assicurando la compatibilità
 * con i dati provenienti da API esterne o da versioni del server più recenti.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {

    /**
     * L'ID univoco del libro.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * L'ISBN (International Standard Book Number) del libro.
     */
    @JsonProperty("isbn")
    private String isbn;

    /**
     * Il titolo del libro.
     */
    @JsonProperty("title")
    private String title;

    /**
     * L'autore del libro.
     */
    @JsonProperty("author")
    private String author;

    /**
     * La descrizione del libro.
     */
    @JsonProperty("description")
    private String description;

    /**
     * L'URL dell'immagine di copertina del libro.
     */
    @JsonProperty("imageUrl")
    private String imageUrl;

    /**
     * L'anno di pubblicazione del libro.
     */
    @JsonProperty("publishYear")
    private String publishYear;

    /**
     * Il prezzo del libro.
     */
    @JsonProperty("price")
    private Double price;

    /**
     * Indica se il libro è disponibile gratuitamente.
     */
    @JsonProperty("isFree")
    private Boolean isFree;

    /**
     * Indica se il libro è una novità.
     */
    @JsonProperty("isNew")
    private Boolean isNew;

    // CAMPI per gestire response dal server

    /**
     * La categoria a cui appartiene il libro.
     */
    @JsonProperty("category")
    private String category;

    /**
     * L'editore del libro.
     */
    @JsonProperty("publisher")
    private String publisher;

    /**
     * La lingua del libro.
     */
    @JsonProperty("language")
    private String language;

    /**
     * Il numero di pagine del libro.
     */
    @JsonProperty("pages")
    private Integer pages;

    /**
     * Il numero di recensioni ricevute dal libro.
     */
    @JsonProperty("reviewCount")
    private int reviewCount = 0;

    /**
     * La valutazione media del libro.
     */
    @JsonProperty("averageRating")
    private double averageRating = 0.0;

    /**
     * Restituisce il numero di recensioni del libro.
     * @return Il numero di recensioni.
     */
    public int getReviewCount() {
        return reviewCount;
    }

    /**
     * Imposta il numero di recensioni del libro.
     * @param reviewCount Il nuovo numero di recensioni.
     */
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    /**
     * Restituisce la valutazione media del libro.
     * @return La valutazione media.
     */
    public double getAverageRating() {
        return averageRating;
    }

    /**
     * Imposta la valutazione media del libro.
     * @param averageRating La nuova valutazione media.
     */
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * Restituisce la valutazione media del libro formattata come stringa.
     * <p>
     * Se la valutazione è maggiore di 0, viene formattata con una cifra decimale.
     * Altrimenti, restituisce "N/A".
     * </p>
     * @return La valutazione formattata.
     */
    public String getFormattedRating() {
        if (averageRating > 0) {
            return String.format("%.1f", averageRating);
        }
        return "N/A";
    }

    // Costruttori

    /**
     * Costruttore di default.
     * <p>
     * Necessario per la deserializzazione JSON da parte di librerie come Jackson.
     * </p>
     */
    public Book() {}

    /**
     * Costruttore per creare un oggetto {@code Book} con i campi principali.
     *
     * @param title Il titolo del libro.
     * @param author L'autore del libro.
     * @param description La descrizione del libro.
     * @param imageUrl L'URL dell'immagine di copertina.
     */
    public Book(String title, String author, String description, String imageUrl) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isFree = true;
        this.isNew = false;
    }

    /**
     * Costruttore per creare un oggetto {@code Book} con ID e campi principali.
     *
     * @param id L'ID del libro.
     * @param title Il titolo del libro.
     * @param author L'autore del libro.
     * @param description La descrizione del libro.
     * @param imageUrl L'URL dell'immagine di copertina.
     */
    public Book(Long id, String title, String author, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isFree = true;
        this.isNew = false;
    }

    /**
     * Costruttore completo per inizializzare i campi essenziali del libro.
     *
     * @param id L'ID del libro.
     * @param isbn L'ISBN del libro.
     * @param title Il titolo del libro.
     * @param author L'autore del libro.
     * @param description La descrizione del libro.
     * @param publishYear L'anno di pubblicazione.
     * @param imageUrl L'URL dell'immagine di copertina.
     */
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

    /**
     * Restituisce il titolo del libro.
     * @return Il titolo.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Restituisce l'autore del libro.
     * @return L'autore.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Restituisce la descrizione del libro.
     * @return La descrizione.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Restituisce l'URL dell'immagine di copertina.
     * @return L'URL dell'immagine.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Restituisce l'ID del libro.
     * @return L'ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Restituisce l'ISBN del libro.
     * @return L'ISBN.
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Restituisce l'anno di pubblicazione del libro.
     * @return L'anno di pubblicazione.
     */
    public String getPublishYear() {
        return publishYear;
    }

    /**
     * Restituisce il prezzo del libro.
     * @return Il prezzo.
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Restituisce lo stato di gratuità del libro.
     * @return {@code true} se è gratuito, {@code false} altrimenti.
     */
    public Boolean getIsFree() {
        return isFree;
    }

    /**
     * Restituisce lo stato di novità del libro.
     * @return {@code true} se è una novità, {@code false} altrimenti.
     */
    public Boolean getIsNew() {
        return isNew;
    }

    /**
     * Restituisce la categoria del libro.
     * @return La categoria.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Restituisce l'editore del libro.
     * @return L'editore.
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Restituisce la lingua del libro.
     * @return La lingua.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Restituisce il numero di pagine del libro.
     * @return Il numero di pagine.
     */
    public Integer getPages() {
        return pages;
    }

    // Setters

    /**
     * Imposta il titolo del libro.
     * @param title Il nuovo titolo.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Imposta l'autore del libro.
     * @param author Il nuovo autore.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Imposta la descrizione del libro.
     * @param description La nuova descrizione.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Imposta l'URL dell'immagine di copertina.
     * @param imageUrl Il nuovo URL dell'immagine.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Imposta l'ID del libro.
     * @param id Il nuovo ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Imposta l'ISBN del libro.
     * @param isbn Il nuovo ISBN.
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Imposta l'anno di pubblicazione.
     * @param publishYear Il nuovo anno di pubblicazione.
     */
    public void setPublishYear(String publishYear) {
        this.publishYear = publishYear;
    }

    /**
     * Imposta il prezzo del libro.
     * @param price Il nuovo prezzo.
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Imposta lo stato di gratuità del libro.
     * @param isFree Lo stato di gratuità.
     */
    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    /**
     * Imposta lo stato di novità del libro.
     * @param isNew Lo stato di novità.
     */
    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Imposta la categoria del libro.
     * @param category La nuova categoria.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Imposta l'editore del libro.
     * @param publisher Il nuovo editore.
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * Imposta la lingua del libro.
     * @param language La nuova lingua.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Imposta il numero di pagine del libro.
     * @param pages Il nuovo numero di pagine.
     */
    public void setPages(Integer pages) {
        this.pages = pages;
    }

    // Metodi di utilità per compatibilità

    /**
     * Restituisce il titolo del libro.
     * <p>
     * Questo metodo è un'alternativa per compatibilità con codice legacy.
     * </p>
     * @return Il titolo del libro.
     */
    public String title() {
        return this.title;
    }

    /**
     * Restituisce l'autore del libro.
     * <p>
     * Questo metodo è un'alternativa per compatibilità con codice legacy.
     * </p>
     * @return L'autore del libro.
     */
    public String author() {
        return this.author;
    }

    /**
     * Restituisce l'URL dell'immagine di copertina.
     * <p>
     * Questo metodo è un'alternativa per compatibilità con codice legacy.
     * </p>
     * @return L'URL dell'immagine.
     */
    public String imageUrl() {
        return this.imageUrl;
    }

    /**
     * Ottiene il nome del file dell'immagine di copertina, assumendo un percorso locale.
     * <p>
     * Questo metodo ignora gli URL esterni e restituisce solo il nome del file
     * se {@code imageUrl} è già un nome di file locale. Altrimenti, genera un nome di file
     * basato su ISBN o titolo.
     * </p>
     * @return Il nome del file locale.
     */
    public String getLocalImageFileName() {
        if (imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.trim().isEmpty()) {
            return sanitizeImageFileName(imageUrl);
        }
        return generateImageFileName();
    }

    /**
     * Ottiene un nome di file immagine sicuro per il caricamento.
     * <p>
     * Questo metodo restituisce sempre un nome di file locale, mai un URL esterno.
     * È utile per garantire che le immagini vengano caricate dalle risorse locali.
     * Se non è disponibile un nome di file, viene restituito un nome di placeholder.
     * </p>
     * @return Un nome di file sicuro.
     */
    public String getSafeImageFileName() {
        String localFileName = getLocalImageFileName();
        if (localFileName == null || localFileName.trim().isEmpty()) {
            return "placeholder.jpg";
        }
        return localFileName;
    }

    /**
     * Pulisce il nome di un file immagine rimuovendo caratteri non validi e aggiungendo
     * l'estensione `.jpg` se mancante.
     *
     * @param fileName Il nome del file da pulire.
     * @return Il nome del file pulito e formattato.
     */
    private String sanitizeImageFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "placeholder.jpg";
        }
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        fileName = fileName.replaceAll("[^a-zA-Z0-9.]", "");
        if (!fileName.toLowerCase().endsWith(".jpg") &&
                !fileName.toLowerCase().endsWith(".jpeg") &&
                !fileName.toLowerCase().endsWith(".png")) {
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            fileName += ".jpg";
        }
        if (fileName.length() < 5) {
            return "placeholder.jpg";
        }
        return fileName;
    }

    /**
     * Genera un nome di file immagine basato sull'ISBN o sul titolo del libro.
     * <p>
     * Se l'ISBN è disponibile, viene utilizzato per generare il nome del file.
     * In alternativa, se è disponibile solo il titolo, viene utilizzato un estratto
     * pulito del titolo. Un nome di placeholder viene restituito se nessuno dei due campi
     * è disponibile.
     * </p>
     * @return Il nome del file generato.
     */
    public String generateImageFileName() {
        if (isbn != null && !isbn.trim().isEmpty()) {
            String cleanIsbn = isbn.replaceAll("[^a-zA-Z0-9]", "");
            if (cleanIsbn.length() > 0) {
                return cleanIsbn + ".jpg";
            }
        }
        if (title != null && !title.trim().isEmpty()) {
            String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (cleanTitle.length() > 20) {
                cleanTitle = cleanTitle.substring(0, 20);
            }
            if (cleanTitle.length() > 0) {
                return cleanTitle + ".jpg";
            }
        }
        return "placeholder.jpg";
    }

    /**
     * Imposta il campo {@code imageUrl} con un nome di file locale se non è già presente.
     * <p>
     * Se {@code imageUrl} è nullo, vuoto o è un URL esterno, viene sovrascritto
     * con un nome di file generato localmente.
     * </p>
     */
    public void ensureLocalImageFileName() {
        if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.startsWith("http")) {
            imageUrl = generateImageFileName();
            System.out.println("📝 Impostato nome file immagine locale per '" + title + "': " + imageUrl);
        }
    }

    /**
     * Verifica se l'immagine di copertina è un URL esterno.
     *
     * @return {@code true} se l'URL dell'immagine è esterno, {@code false} altrimenti.
     */
    public boolean hasExternalImageUrl() {
        return imageUrl != null && imageUrl.startsWith("http");
    }

    /**
     * Verifica se l'immagine di copertina è un file locale.
     *
     * @return {@code true} se l'immagine è un file locale, {@code false} altrimenti.
     */
    public boolean hasLocalImageFile() {
        return imageUrl != null && !imageUrl.startsWith("http") && !imageUrl.trim().isEmpty();
    }

    /**
     * Stampa a console le informazioni di debug relative all'immagine.
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