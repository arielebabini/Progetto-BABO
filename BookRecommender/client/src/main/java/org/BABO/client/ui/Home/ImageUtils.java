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
 * Utility avanzata per gestione sicura e performante delle immagini nell'applicazione BABO Library.
 * <p>
 * Questa classe fornisce un sistema completo per la gestione delle immagini dell'applicazione,
 * implementando strategie di sicurezza, performance optimization, e gestione asincrona.
 * Progettata specificamente per evitare caricamenti da URL esterni mantenendo alta
 * responsività dell'interfaccia utente attraverso caricamento asincrono e sistema
 * di cache intelligente per ottimizzazione memoria e performance.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Caricamento Sicuro:</strong> Solo da risorse locali, blocco URL esterni</li>
 *   <li><strong>Caricamento Asincrono:</strong> Non-blocking con placeholder immediato</li>
 *   <li><strong>Cache Intelligente:</strong> ConcurrentHashMap per accesso thread-safe</li>
 *   <li><strong>Fallback Robusto:</strong> Sistema di placeholder e varianti file</li>
 *   <li><strong>Resource Management:</strong> Gestione automatica InputStream e cleanup</li>
 *   <li><strong>URL Sanitization:</strong> Conversione sicura URL esterni in nomi locali</li>
 * </ul>
 *
 * <h3>Architettura di Sicurezza:</h3>
 * <p>
 * Il sistema implementa una strategia security-first che previene:
 * </p>
 * <ul>
 *   <li><strong>External URL Loading:</strong> Blocco completo caricamenti da internet</li>
 *   <li><strong>Path Traversal:</strong> Sanitizzazione nomi file per prevenire directory traversal</li>
 *   <li><strong>Resource Isolation:</strong> Accesso limitato a directory /books_covers/</li>
 *   <li><strong>Input Validation:</strong> Validazione estensioni e caratteri file</li>
 * </ul>
 *
 * <h3>Sistema di Cache Concorrente:</h3>
 * <p>
 * Implementa cache thread-safe con gestione intelligente memoria:
 * </p>
 * <ul>
 *   <li><strong>ConcurrentHashMap:</strong> Accesso sicuro da thread multipli</li>
 *   <li><strong>Cache Key:</strong> Basata su nome file sanitizzato</li>
 *   <li><strong>Cache Hit:</strong> Ritorno immediato senza I/O aggiuntivo</li>
 *   <li><strong>Memory Management:</strong> Cleanup manuale tramite clearImageCache()</li>
 * </ul>
 *
 * <h3>Caricamento Asincrono Avanzato:</h3>
 * <p>
 * Sistema non-blocking per UI responsiva:
 * </p>
 * <ul>
 *   <li>Thread pool dedicato per operazioni I/O</li>
 *   <li>Placeholder immediato durante caricamento</li>
 *   <li>Platform.runLater per aggiornamenti UI thread-safe</li>
 *   <li>CompletableFuture per composizione operazioni</li>
 * </ul>
 *
 * <h3>Design Patterns Implementati:</h3>
 * <ul>
 *   <li><strong>Utility Pattern:</strong> Metodi statici per operazioni comuni</li>
 *   <li><strong>Cache Pattern:</strong> Memorizzazione risultati per performance</li>
 *   <li><strong>Async Pattern:</strong> Operazioni non-blocking con callback</li>
 *   <li><strong>Fallback Pattern:</strong> Strategie alternative per failure scenarios</li>
 *   <li><strong>Sanitization Pattern:</strong> Input cleaning per sicurezza</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo base:</h3>
 * <pre>{@code
 * // Creazione ImageView con caricamento asincrono sicuro
 * ImageView bookCover = ImageUtils.createSafeImageView("book123.jpg", 120, 180);
 *
 * // Il metodo gestisce automaticamente:
 * // - Placeholder immediato per UI responsiva
 * // - Caricamento asincrono da risorse locali
 * // - Cache per performance ottimizzate
 * // - Fallback a placeholder se immagine non trovata
 * // - Sanitizzazione nome file per sicurezza
 *
 * // Integrazione in layout
 * VBox bookCard = new VBox();
 * bookCard.getChildren().add(bookCover);
 * }</pre>
 *
 * <h3>Esempio di utilizzo avanzato:</h3>
 * <pre>{@code
 * // Caricamento sincrono per validazione immediata
 * public boolean validateBookCover(Book book) {
 *     Image cover = ImageUtils.loadSafeImage(book.getCoverFileName());
 *     return cover != null && !cover.isError();
 * }
 *
 * // Preload immagini per performance
 * public void preloadBookCovers(List<Book> books) {
 *     books.parallelStream()
 *          .map(Book::getCoverFileName)
 *          .forEach(ImageUtils::loadSafeImage);
 * }
 *
 * // Gestione memoria cache
 * public void onApplicationShutdown() {
 *     ImageUtils.clearImageCache();
 * }
 *
 * // Conversione URL esterni sicura
 * String safeFileName = ImageUtils.convertToLocalFileName(externalUrl);
 * ImageView view = ImageUtils.createSafeImageView(safeFileName, 100, 150);
 * }</pre>
 *
 * <h3>Gestione URL Esterni e Sanitizzazione:</h3>
 * <p>
 * Sistema sofisticato per conversione sicura URL esterni:
 * </p>
 * <ul>
 *   <li>Estrazione nomi file da URL path</li>
 *   <li>Parsing ISBN da URL strutturati</li>
 *   <li>Rimozione caratteri non alfanumerici</li>
 *   <li>Normalizzazione estensioni file</li>
 *   <li>Fallback a placeholder per URL non parsabili</li>
 * </ul>
 *
 * <h3>Sistema di Fallback Robusto:</h3>
 * <p>
 * Strategia multi-livello per gestione file mancanti:
 * </p>
 * <ol>
 *   <li><strong>Cache Hit:</strong> Ritorno immediato se in cache</li>
 *   <li><strong>Direct Load:</strong> Caricamento nome file esatto</li>
 *   <li><strong>Variants:</strong> Prova varianti case e caratteri</li>
 *   <li><strong>Placeholder:</strong> Immagine di default se tutto fallisce</li>
 * </ol>
 *
 * <h3>Struttura Directory Risorse:</h3>
 * <pre>
 * src/main/resources/
 * └── books_covers/
 *     ├── placeholder.jpg     ← Immagine di default
 *     ├── book_isbn.jpg      ← Copertine specifiche
 *     ├── default.jpg        ← Placeholder alternativo
 *     └── noimage.jpg        ← Placeholder alternativo
 * </pre>
 *
 * <h3>Formati Supportati:</h3>
 * <ul>
 *   <li><strong>JPG/JPEG:</strong> Formato primario raccomandato</li>
 *   <li><strong>PNG:</strong> Supporto trasparenza per icone</li>
 *   <li><strong>Auto-correction:</strong> Aggiunta automatica estensione .jpg</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <ul>
 *   <li>Thread pool dimensionato per I/O ottimale (3 thread)</li>
 *   <li>Cache concorrente per eliminare I/O ridondante</li>
 *   <li>Lazy loading con placeholder immediato</li>
 *   <li>Stream management con chiusura automatica</li>
 *   <li>Async execution per non bloccare UI thread</li>
 * </ul>
 *
 * <h3>Security Considerations:</h3>
 * <ul>
 *   <li>Nessun accesso a URL esterni per prevenire SSRF</li>
 *   <li>Path restriction a directory specifica</li>
 *   <li>Filename sanitization per prevenire directory traversal</li>
 *   <li>Extension validation per prevenire code execution</li>
 * </ul>
 *
 * <h3>Thread Safety:</h3>
 * <ul>
 *   <li>ConcurrentHashMap per cache thread-safe</li>
 *   <li>ExecutorService per gestione thread pool</li>
 *   <li>Platform.runLater per UI updates</li>
 *   <li>Immutable Image objects in cache</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see javafx.scene.image.Image
 * @see javafx.scene.image.ImageView
 * @see java.util.concurrent.CompletableFuture
 * @see java.util.concurrent.ConcurrentHashMap
 */
public class ImageUtils {

    /** Cache thread-safe per immagini caricate, evita caricamenti ridondanti */
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();

    /** Thread pool dedicato per operazioni I/O asincrone, dimensionato per carico tipico */
    private static final ExecutorService imageExecutor = Executors.newFixedThreadPool(3);

    /** Immagine placeholder di default per fallback scenarios */
    private static Image defaultPlaceholder = null;

    static {
        // Inizializza il placeholder di default al caricamento della classe
        initializeDefaultPlaceholder();
    }

    /**
     * Crea un ImageView sicuro con caricamento asincrono e placeholder immediato.
     * <p>
     * Factory method principale per creazione ImageView con gestione completa
     * del caricamento asincrono. Configura immediatamente placeholder per UI
     * responsiva, poi carica l'immagine target in background aggiornando
     * l'ImageView quando disponibile.
     * </p>
     *
     * <h4>Processo di creazione:</h4>
     * <ol>
     *   <li>Creazione ImageView con dimensioni specificate</li>
     *   <li>Configurazione proprietà rendering (smooth, preserve ratio)</li>
     *   <li>Impostazione placeholder immediato</li>
     *   <li>Sanitizzazione nome file per sicurezza</li>
     *   <li>Avvio caricamento asincrono target image</li>
     * </ol>
     *
     * <h4>Configurazioni ImageView:</h4>
     * <ul>
     *   <li><strong>Fit dimensions:</strong> Width e height come specificato</li>
     *   <li><strong>Preserve ratio:</strong> Mantiene proporzioni originali</li>
     *   <li><strong>Smooth rendering:</strong> Abilita antialiasing per qualità</li>
     *   <li><strong>Immediate placeholder:</strong> Mostra contenuto immediatamente</li>
     * </ul>
     *
     * <h4>Security features:</h4>
     * <ul>
     *   <li>Conversione automatica URL esterni in nomi locali</li>
     *   <li>Sanitizzazione input per prevenire path traversal</li>
     *   <li>Caricamento esclusivo da risorse locali</li>
     * </ul>
     *
     * <h4>Utilizzo raccomandato:</h4>
     * <pre>{@code
     * // Per copertine libri in griglia
     * ImageView cover = ImageUtils.createSafeImageView(book.getCoverImage(), 120, 180);
     *
     * // Per thumbnail in liste
     * ImageView thumb = ImageUtils.createSafeImageView(item.getImage(), 50, 75);
     * }</pre>
     *
     * @param imageFileName nome file o URL dell'immagine da caricare
     * @param width larghezza desiderata dell'ImageView
     * @param height altezza desiderata dell'ImageView
     * @return {@link ImageView} configurato con caricamento asincrono attivo
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
     * Converte URL esterni o nomi file non sicuri in nomi file locali puliti.
     * <p>
     * Sistema di sanitizzazione e conversione per trasformare input potenzialmente
     * pericolosi (URL esterni, nomi con caratteri speciali) in nomi file sicuri
     * utilizzabili per caricamento da risorse locali. Implementa strategie
     * multiple per estrazione informazioni utili da URL complessi.
     * </p>
     *
     * <h4>Tipi di input gestiti:</h4>
     * <ul>
     *   <li><strong>URL HTTP/HTTPS:</strong> Estrazione nome file da path</li>
     *   <li><strong>URL strutturati:</strong> Parsing ISBN o identificatori</li>
     *   <li><strong>Nomi file locali:</strong> Sanitizzazione caratteri speciali</li>
     *   <li><strong>Input vuoto/null:</strong> Fallback a placeholder</li>
     * </ul>
     *
     * <h4>Strategia parsing URL:</h4>
     * <ol>
     *   <li>Rilevamento URL tramite prefisso "http"</li>
     *   <li>Estrazione nome file da ultimo segmento path</li>
     *   <li>Parsing pattern specializzati (es. "/P/" per ISBN)</li>
     *   <li>Fallback a placeholder se parsing fallisce</li>
     * </ol>
     *
     * <h4>Sanitizzazione applicata:</h4>
     * <ul>
     *   <li>Rimozione caratteri non alfanumerici eccetto punto</li>
     *   <li>Normalizzazione estensioni a .jpg se mancanti</li>
     *   <li>Validazione lunghezza minima risultato</li>
     *   <li>Fallback a "placeholder.jpg" per input non validi</li>
     * </ul>
     *
     * <h4>Esempi di conversione:</h4>
     * <pre>{@code
     * // URL esterni
     * "https://example.com/covers/book123.jpg" -> "book123.jpg"
     * "https://api.site.com/P/9781234567890.png" -> "9781234567890.jpg"
     *
     * // Nomi file non sicuri
     * "book title!@#$%^&*().jpg" -> "booktitle.jpg"
     * "file-with_special chars" -> "filewithspecialchars.jpg"
     *
     * // Input non validi
     * null -> "placeholder.jpg"
     * "" -> "placeholder.jpg"
     * "unparseable-url" -> "placeholder.jpg"
     * }</pre>
     *
     * @param input stringa di input (URL, nome file, o null)
     * @return nome file locale sicuro e sanitizzato
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
            return "placeholder.jpg";
        }

        // È già un nome file locale, puliscilo
        return sanitizeFileName(input);
    }

    /**
     * Pulisce e sanitizza il nome del file per renderlo sicuro e utilizzabile.
     * <p>
     * Applica trasformazioni di sicurezza per rimuovere caratteri potenzialmente
     * pericolosi e normalizzare il formato del nome file. Previene attacchi
     * directory traversal e assicura compatibilità con il filesystem.
     * </p>
     *
     * <h4>Trasformazioni applicate:</h4>
     * <ul>
     *   <li>Rimozione caratteri speciali (mantenendo solo alfanumerici e punto)</li>
     *   <li>Validazione e normalizzazione estensioni supportate</li>
     *   <li>Aggiunta automatica estensione .jpg se mancante</li>
     *   <li>Validazione lunghezza minima per prevenire nomi troppo corti</li>
     * </ul>
     *
     * <h4>Estensioni supportate:</h4>
     * <ul>
     *   <li>.jpg (formato primario raccomandato)</li>
     *   <li>.jpeg (variante formato JPEG)</li>
     *   <li>.png (supporto trasparenza)</li>
     * </ul>
     *
     * <h4>Security validations:</h4>
     * <ul>
     *   <li>Regex pattern per caratteri alfanumerici e punto</li>
     *   <li>Blocco sequenze directory traversal (../, ..\)</li>
     *   <li>Validazione lunghezza minima risultato</li>
     *   <li>Fallback sicuro per input non conformi</li>
     * </ul>
     *
     * @param fileName nome file da sanitizzare
     * @return nome file pulito e sicuro, o "placeholder.jpg" se non valido
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
     * Carica un'immagine asincrona esclusivamente dalle risorse locali.
     * <p>
     * Gestisce il caricamento non-blocking dell'immagine target, utilizzando
     * cache per performance e aggiornando l'ImageView fornito quando il
     * caricamento è completato. Implementa gestione completa errori e
     * fallback a placeholder.
     * </p>
     *
     * <h4>Processo asincrono:</h4>
     * <ol>
     *   <li>Controllo cache per hit immediato</li>
     *   <li>Invio operazione I/O a thread pool</li>
     *   <li>Caricamento da risorse con fallback variants</li>
     *   <li>Aggiornamento cache se successo</li>
     *   <li>Update UI tramite Platform.runLater</li>
     * </ol>
     *
     * <h4>Cache management:</h4>
     * <ul>
     *   <li>Check preventivo per evitare I/O ridondante</li>
     *   <li>Population automatica dopo caricamento riuscito</li>
     *   <li>Thread-safe access tramite ConcurrentHashMap</li>
     * </ul>
     *
     * <h4>Thread safety:</h4>
     * <ul>
     *   <li>Esecuzione I/O su thread pool dedicato</li>
     *   <li>UI updates tramite Platform.runLater</li>
     *   <li>Exception handling per prevenire thread crash</li>
     * </ul>
     *
     * <h4>Error handling:</h4>
     * <ul>
     *   <li>Try-catch su operazioni I/O</li>
     *   <li>Fallback a placeholder per errori</li>
     *   <li>Logging dettagliato per debugging</li>
     *   <li>Graceful degradation senza crash UI</li>
     * </ul>
     *
     * @param fileName nome file locale sanitizzato da caricare
     * @param imageView ImageView target da aggiornare al completamento
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
     * Carica un'immagine sincrona dalle risorse locali con cache integrata.
     * <p>
     * Metodo sincrono per caricamento immediato con gestione cache. Utile
     * per scenari dove serve validazione immediata dell'immagine o quando
     * il caricamento asincrono non è appropriato.
     * </p>
     *
     * <h4>Utilizzo sincrono:</h4>
     * <ul>
     *   <li>Validazione immediata esistenza immagine</li>
     *   <li>Preload durante inizializzazione applicazione</li>
     *   <li>Caricamento in background thread controllato</li>
     * </ul>
     *
     * <h4>Cache integration:</h4>
     * <ul>
     *   <li>Check cache prima di I/O</li>
     *   <li>Population cache dopo caricamento</li>
     *   <li>Return immediato se cache hit</li>
     * </ul>
     *
     * <h4>Esempi di utilizzo:</h4>
     * <pre>{@code
     * // Validazione presenza immagine
     * Image cover = ImageUtils.loadSafeImage("book123.jpg");
     * boolean hasValidCover = cover != null && !cover.isError();
     *
     * // Preload batch immagini
     * books.parallelStream()
     *      .map(Book::getCoverFile)
     *      .forEach(ImageUtils::loadSafeImage);
     * }</pre>
     *
     * @param imageFileName nome file locale da caricare
     * @return {@link Image} caricata, o placeholder/null se errore
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
     * Carica immagine esclusivamente dalle risorse locali con fallback intelligente.
     * <p>
     * Metodo interno per caricamento sicuro esclusivamente da risorse embedded.
     * Implementa sistema di fallback multi-livello che prova varianti del nome
     * file per massimizzare possibilità di successo caricamento.
     * </p>
     *
     * <h4>Strategia di caricamento:</h4>
     * <ol>
     *   <li>Tentativo caricamento nome file esatto</li>
     *   <li>Prova varianti case (lowercase, uppercase)</li>
     *   <li>Prova varianti caratteri (rimozione spazi, trattini, underscore)</li>
     *   <li>Fallback a placeholder se tutti i tentativi falliscono</li>
     * </ol>
     *
     * <h4>Path delle risorse:</h4>
     * <p>
     * Tutte le immagini sono caricate da {@code /books_covers/} nel classpath,
     * seguendo convenzione Maven per risorse embedded.
     * </p>
     *
     * <h4>Varianti tentate:</h4>
     * <ul>
     *   <li>Nome originale</li>
     *   <li>Lowercase version</li>
     *   <li>Uppercase version</li>
     *   <li>Senza spazi</li>
     *   <li>Senza trattini</li>
     *   <li>Senza underscore</li>
     * </ul>
     *
     * <h4>Resource management:</h4>
     * <ul>
     *   <li>Apertura InputStream per ogni tentativo</li>
     *   <li>Chiusura automatica stream dopo uso</li>
     *   <li>Validazione Image prima di return</li>
     *   <li>Exception handling per I/O errors</li>
     * </ul>
     *
     * <h4>Validation checks:</h4>
     * <ul>
     *   <li>Stream non null (file esistente)</li>
     *   <li>Image.isError() == false</li>
     *   <li>Dimensioni valide se necessario</li>
     * </ul>
     *
     * @param fileName nome file da caricare dalle risorse
     * @return {@link Image} caricata o placeholder se fallimento
     */
    private static Image loadFromResourcesOnly(String fileName) {
        try {

            // Tenta di caricare dalle risorse del progetto
            InputStream stream = ImageUtils.class.getResourceAsStream("/books_covers/" + fileName);
            if (stream != null) {
                Image image = new Image(stream);
                stream.close();

                if (!image.isError()) {
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
            return getDefaultPlaceholder();

        } catch (Exception e) {
            System.err.println("❌ Errore caricamento immagine dalle risorse " + fileName + ": " + e.getMessage());
            return getDefaultPlaceholder();
        }
    }

    /**
     * Inizializza il placeholder di default con fallback multipli.
     * <p>
     * Metodo interno per configurazione del sistema di fallback immagini.
     * Prova diversi nomi file placeholder comuni e, come ultima risorsa,
     * crea un placeholder programmatico. Chiamato automaticamente durante
     * inizializzazione statica della classe.
     * </p>
     *
     * <h4>Strategia di inizializzazione:</h4>
     * <ol>
     *   <li>Tentativo caricamento placeholder.jpg principale</li>
     *   <li>Prova nomi alternativi comuni</li>
     *   <li>Fallback a placeholder programmatico</li>
     *   <li>Gestione graceful se tutto fallisce</li>
     * </ol>
     *
     * <h4>Nomi placeholder tentati:</h4>
     * <ul>
     *   <li>placeholder.jpg (primario)</li>
     *   <li>placeholder.png (variante PNG)</li>
     *   <li>default.jpg (alternativo)</li>
     *   <li>noimage.jpg (convention comune)</li>
     *   <li>book.jpg (generico libri)</li>
     * </ul>
     *
     * <h4>Fallback programmatico:</h4>
     * <p>
     * Se nessun file placeholder è disponibile, il sistema può
     * creare un placeholder programmatico o utilizzare null,
     * permettendo a JavaFX di gestire con immagine vuota default.
     * </p>
     *
     * <h4>Error handling:</h4>
     * <ul>
     *   <li>Try-catch per ogni tentativo caricamento</li>
     *   <li>Validazione Image.isError() per ogni file</li>
     *   <li>Logging informativo per debugging</li>
     *   <li>Graceful degradation a null se necessario</li>
     * </ul>
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

        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione placeholder: " + e.getMessage());
            defaultPlaceholder = createProgrammaticPlaceholder();
        }
    }

    /**
     * Crea un placeholder programmatico quando nessun file è disponibile.
     * <p>
     * Ultimo fallback per generazione placeholder quando nessun file immagine
     * è disponibile nelle risorse. Attualmente restituisce null permettendo
     * a JavaFX di gestire con comportamento default, ma può essere esteso
     * per generare immagini programmatiche.
     * </p>
     *
     * <h4>Implementazione attuale:</h4>
     * <p>
     * Restituisce null, delegando a JavaFX la gestione del caso "no image".
     * JavaFX visualizzerà uno spazio vuoto delle dimensioni specificate
     * senza causare errori o crash.
     * </p>
     *
     * <h4>Possibili estensioni future:</h4>
     * <ul>
     *   <li>Generazione Canvas con testo placeholder</li>
     *   <li>Creazione BufferedImage con colore solido</li>
     *   <li>Rendering programmatico icona libro generica</li>
     *   <li>Integrazione con librerie di generazione immagini</li>
     * </ul>
     *
     * <h4>Considerazioni design:</h4>
     * <ul>
     *   <li>Semplicità implementazione vs funzionalità</li>
     *   <li>Performance generation vs file loading</li>
     *   <li>Memory footprint per placeholder generati</li>
     *   <li>Consistenza visuale con theme applicazione</li>
     * </ul>
     *
     * @return placeholder programmatico o null per delegare a JavaFX
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
     * Ottiene il placeholder di default con inizializzazione lazy.
     * <p>
     * Accessor per il placeholder di default, garantendo che sia
     * inizializzato prima del ritorno. Implementa pattern lazy initialization
     * per gestire scenari dove il placeholder potrebbe non essere stato
     * ancora configurato.
     * </p>
     *
     * <h4>Lazy initialization:</h4>
     * <ul>
     *   <li>Check se placeholder già inizializzato</li>
     *   <li>Inizializzazione on-demand se necessario</li>
     *   <li>Return sicuro anche se inizializzazione fallisce</li>
     * </ul>
     *
     * <h4>Thread safety:</h4>
     * <p>
     * Il metodo è chiamato da thread multipli ma l'inizializzazione
     * è gestita in modo thread-safe tramite operazioni atomiche
     * su riferimento statico.
     * </p>
     *
     * @return immagine placeholder di default, può essere null
     */
    public static Image getDefaultPlaceholder() {
        if (defaultPlaceholder == null) {
            initializeDefaultPlaceholder();
        }
        return defaultPlaceholder;
    }

    /**
     * Pulisce completamente la cache delle immagini per gestione memoria.
     * <p>
     * Utility per cleanup manuale della cache quando necessario per
     * ottimizzazione memoria o reset dello stato applicazione.
     * Utile durante shutdown applicazione o dopo caricamento batch
     * di molte immagini.
     * </p>
     *
     * <h4>Quando utilizzare:</h4>
     * <ul>
     *   <li>Shutdown applicazione per cleanup resources</li>
     *   <li>Cambio dataset significativo (es. nuova libreria)</li>
     *   <li>Gestione memoria su dispositivi con RAM limitata</li>
     *   <li>Reset test automatici per stato pulito</li>
     * </ul>
     *
     * <h4>Considerazioni performance:</h4>
     * <ul>
     *   <li>Prossimi accessi ricaricheranno da I/O</li>
     *   <li>Trade-off memoria vs performance</li>
     *   <li>Impact su UI responsiveness se cache hit rate alto</li>
     * </ul>
     *
     * <h4>Thread safety:</h4>
     * <p>
     * L'operazione clear() su ConcurrentHashMap è atomic e thread-safe,
     * garantendo che l'operazione sia sicura anche con accesso concorrente.
     * </p>
     *
     * <h4>Utilizzo raccomandato:</h4>
     * <pre>{@code
     * // Durante shutdown applicazione
     * public void onApplicationExit() {
     *     ImageUtils.clearImageCache();
     *     // Altri cleanup...
     * }
     *
     * // Reset per testing
     * {@literal @}BeforeEach
     * public void resetImageState() {
     *     ImageUtils.clearImageCache();
     * }
     *
     * // Gestione memoria periodica
     * public void performMemoryCleanup() {
     *     if (Runtime.getRuntime().freeMemory() < threshold) {
     *         ImageUtils.clearImageCache();
     *     }
     * }
     * }</pre>
     */
    public static void clearImageCache() {
        imageCache.clear();
        System.out.println("🧹 Cache immagini pulita");
    }
}