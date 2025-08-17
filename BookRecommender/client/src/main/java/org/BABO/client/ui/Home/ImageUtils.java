package org.BABO.client.ui.Home;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class per la gestione asincrona delle immagini dei libri
 * AGGIORNATO: Solo immagini locali dalla cartella resources
 */
public class ImageUtils {

    // Cache per le immagini caricate
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();

    // Executor per operazioni asincrone
    private static final ExecutorService imageExecutor = Executors.newFixedThreadPool(3);

    // Immagine placeholder di default
    private static Image defaultPlaceholder = null;

    static {
        // Inizializza il placeholder di default
        initializeDefaultPlaceholder();
    }

    /**
     * Crea un ImageView sicuro con caricamento asincrono
     * SOLO DA RISORSE LOCALI
     */
    public static ImageView createSafeImageView(String imageFileName, double width, double height) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Imposta immediatamente il placeholder
        imageView.setImage(getDefaultPlaceholder());

        // Converte URL esterni in nomi file locali
        String localFileName = convertToLocalFileName(imageFileName);

        // Carica l'immagine asincrona SOLO dalle risorse
        loadLocalImageAsync(localFileName, imageView);

        return imageView;
    }

    /**
     * Converte URL esterni o nomi file in nomi file locali puliti
     */
    private static String convertToLocalFileName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "placeholder.jpg";
        }

        // Se è un URL esterno, estrailo dall'URL o ignora
        if (input.startsWith("http")) {
            System.out.println("🔄 Conversione URL esterno in nome file locale: " + input);

            // Estrai il nome file dall'URL se possibile
            if (input.contains("/")) {
                String fileName = input.substring(input.lastIndexOf("/") + 1);
                if (fileName.contains(".")) {
                    // Ha un'estensione, puliscila
                    return sanitizeFileName(fileName);
                }
            }

            // Se l'URL contiene un ISBN riconoscibile, usalo
            if (input.contains("/P/")) {
                try {
                    String part = input.substring(input.indexOf("/P/") + 3);
                    if (part.contains(".")) {
                        String isbn = part.substring(0, part.indexOf("."));
                        return sanitizeFileName(isbn) + ".jpg";
                    }
                } catch (Exception e) {
                    // Ignora errori di parsing
                }
            }

            // Fallback: placeholder
            System.out.println("⚠️ Impossibile estrarre nome file da URL, uso placeholder");
            return "placeholder.jpg";
        }

        // È già un nome file locale, puliscilo
        return sanitizeFileName(input);
    }

    /**
     * Pulisce il nome del file per renderlo sicuro
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "placeholder.jpg";
        }

        // Rimuovi caratteri speciali e mantieni solo alfanumerici
        String clean = fileName.replaceAll("[^a-zA-Z0-9.]", "");

        // Assicurati che abbia un'estensione
        if (!clean.toLowerCase().endsWith(".jpg") &&
                !clean.toLowerCase().endsWith(".jpeg") &&
                !clean.toLowerCase().endsWith(".png")) {

            // Rimuovi estensioni esistenti non supportate
            if (clean.contains(".")) {
                clean = clean.substring(0, clean.lastIndexOf("."));
            }
            clean += ".jpg";
        }

        // Se troppo corto o vuoto dopo pulizia, usa placeholder
        if (clean.length() < 5) { // Almeno "x.jpg"
            return "placeholder.jpg";
        }

        return clean;
    }

    /**
     * Carica un'immagine SOLO dalle risorse locali
     */
    private static void loadLocalImageAsync(String fileName, ImageView imageView) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }

        // Controlla prima la cache
        Image cachedImage = imageCache.get(fileName);
        if (cachedImage != null) {
            Platform.runLater(() -> imageView.setImage(cachedImage));
            return;
        }

        // Carica asincrono SOLO dalle risorse
        CompletableFuture.supplyAsync(() -> {
            try {
                return loadFromResourcesOnly(fileName);
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento asincrono immagine: " + e.getMessage());
                return getDefaultPlaceholder();
            }
        }, imageExecutor).thenAccept(image -> {
            if (image != null && !image.isError()) {
                // Aggiungi alla cache
                imageCache.put(fileName, image);

                // Aggiorna l'UI nel thread principale
                Platform.runLater(() -> {
                    if (imageView != null) {
                        imageView.setImage(image);
                    }
                });
            }
        }).exceptionally(throwable -> {
            System.err.println("❌ Errore nell'aggiornamento immagine UI: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Carica un'immagine SOLO dalle risorse (sincrono)
     */
    public static Image loadSafeImage(String imageFileName) {
        // Converte prima in nome file locale
        String localFileName = convertToLocalFileName(imageFileName);

        // Controlla cache prima
        Image cachedImage = imageCache.get(localFileName);
        if (cachedImage != null) {
            return cachedImage;
        }

        // Carica dalle risorse
        Image image = loadFromResourcesOnly(localFileName);

        // Aggiungi alla cache se valida
        if (image != null && !image.isError()) {
            imageCache.put(localFileName, image);
        }

        return image;
    }

    /**
     * Carica immagine ESCLUSIVAMENTE dalle risorse locali
     */
    private static Image loadFromResourcesOnly(String fileName) {
        try {
            System.out.println("🔄 Caricamento immagine dalle risorse: " + fileName);

            // Tenta di caricare dalle risorse del progetto
            InputStream stream = ImageUtils.class.getResourceAsStream("/books_covers/" + fileName);
            if (stream != null) {
                Image image = new Image(stream);
                stream.close();

                if (!image.isError()) {
                    System.out.println("✅ Immagine caricata dalle risorse: " + fileName);
                    return image;
                }
            }

            // Se non trovata, prova varianti comuni
            String[] variants = {
                    fileName.toLowerCase(),
                    fileName.toUpperCase(),
                    fileName.replace(" ", ""),
                    fileName.replace("-", ""),
                    fileName.replace("_", "")
            };

            for (String variant : variants) {
                if (!variant.equals(fileName)) {
                    InputStream variantStream = ImageUtils.class.getResourceAsStream("/books_covers/" + variant);
                    if (variantStream != null) {
                        Image variantImage = new Image(variantStream);
                        variantStream.close();

                        if (!variantImage.isError()) {
                            System.out.println("✅ Immagine caricata (variante): " + variant);
                            return variantImage;
                        }
                    }
                }
            }

            System.out.println("⚠️ Immagine non trovata nelle risorse: " + fileName + " - uso placeholder");
            return getDefaultPlaceholder();

        } catch (Exception e) {
            System.err.println("❌ Errore caricamento immagine dalle risorse " + fileName + ": " + e.getMessage());
            return getDefaultPlaceholder();
        }
    }

    /**
     * Inizializza il placeholder di default
     */
    private static void initializeDefaultPlaceholder() {
        try {
            // Prova a caricare placeholder dalle risorse
            InputStream placeholderStream = ImageUtils.class.getResourceAsStream("/books_covers/placeholder.jpg");
            if (placeholderStream != null) {
                defaultPlaceholder = new Image(placeholderStream);
                placeholderStream.close();

                if (!defaultPlaceholder.isError()) {
                    System.out.println("✅ Placeholder di default caricato dalle risorse");
                    return;
                }
            }

            // Prova con nomi alternativi
            String[] placeholderNames = {"placeholder.png", "default.jpg", "noimage.jpg", "book.jpg"};
            for (String name : placeholderNames) {
                InputStream stream = ImageUtils.class.getResourceAsStream("/books_covers/" + name);
                if (stream != null) {
                    Image img = new Image(stream);
                    stream.close();
                    if (!img.isError()) {
                        defaultPlaceholder = img;
                        System.out.println("✅ Placeholder alternativo caricato: " + name);
                        return;
                    }
                }
            }

            // Ultima risorsa: crea placeholder programmatico
            defaultPlaceholder = createProgrammaticPlaceholder();
            System.out.println("⚠️ Uso placeholder programmatico");

        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione placeholder: " + e.getMessage());
            defaultPlaceholder = createProgrammaticPlaceholder();
        }
    }

    /**
     * Crea un placeholder programmatico semplice
     */
    private static Image createProgrammaticPlaceholder() {
        try {
            // Crea un'immagine semplice di colore solido
            // Nota: questo è un placeholder molto semplice
            // In alternativa potresti mettere un'immagine placeholder.jpg nelle risorse
            return null; // JavaFX gestirà con un'immagine vuota di default
        } catch (Exception e) {
            System.err.println("❌ Impossibile creare placeholder programmatico: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ottiene il placeholder di default
     */
    public static Image getDefaultPlaceholder() {
        if (defaultPlaceholder == null) {
            initializeDefaultPlaceholder();
        }
        return defaultPlaceholder;
    }

    /**
     * Verifica se un file immagine esiste nelle risorse (asincrono)
     */
    public static CompletableFuture<Boolean> imageExistsAsync(String imageFileName) {
        return CompletableFuture.supplyAsync(() -> {
            String localFileName = convertToLocalFileName(imageFileName);
            return imageExistsInResources(localFileName);
        }, imageExecutor);
    }

    /**
     * Verifica se un file immagine esiste nelle risorse (sincrono)
     */
    public static boolean imageExists(String imageFileName) {
        String localFileName = convertToLocalFileName(imageFileName);
        return imageExistsInResources(localFileName);
    }

    /**
     * Verifica esistenza nelle risorse
     */
    private static boolean imageExistsInResources(String fileName) {
        try {
            InputStream stream = ImageUtils.class.getResourceAsStream("/books_covers/" + fileName);
            if (stream != null) {
                stream.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Genera il nome del file immagine basato su ISBN o titolo
     */
    public static String generateImageFileName(String isbn, String title) {
        if (isbn != null && !isbn.trim().isEmpty()) {
            return sanitizeFileName(isbn) + ".jpg";
        } else if (title != null && !title.trim().isEmpty()) {
            return sanitizeFileName(title) + ".jpg";
        } else {
            return "placeholder.jpg";
        }
    }

    /**
     * Precarica un'immagine nella cache
     */
    public static void preloadImage(String imageFileName) {
        String localFileName = convertToLocalFileName(imageFileName);
        if (localFileName != null && !imageCache.containsKey(localFileName)) {
            CompletableFuture.supplyAsync(() -> loadFromResourcesOnly(localFileName), imageExecutor)
                    .thenAccept(image -> {
                        if (image != null && !image.isError()) {
                            imageCache.put(localFileName, image);
                        }
                    });
        }
    }

    /**
     * Pulisce la cache delle immagini
     */
    public static void clearImageCache() {
        imageCache.clear();
        System.out.println("🧹 Cache immagini pulita");
    }

    /**
     * Ottiene informazioni sulla cache
     */
    public static String getCacheInfo() {
        return String.format("Cache immagini: %d elementi", imageCache.size());
    }

    /**
     * Debug: elenca tutte le immagini caricate
     */
    public static void debugImageCache() {
        System.out.println("🔍 DEBUG CACHE IMMAGINI:");
        imageCache.forEach((fileName, image) -> {
            System.out.println("  " + fileName + " -> " + (image.isError() ? "ERROR" : "OK"));
        });
    }

    /**
     * Shutdown dell'executor (da chiamare alla chiusura dell'app)
     */
    public static void shutdown() {
        imageExecutor.shutdown();
        clearImageCache();
        System.out.println("🔒 ImageUtils chiuso");
    }
}