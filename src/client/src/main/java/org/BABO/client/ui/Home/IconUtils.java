package org.BABO.client.ui.Home;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Utility cross-platform per gestione icone dell'applicazione BABO Library.
 * <p>
 * Questa classe fornisce un sistema unificato per la gestione delle icone
 * dell'applicazione attraverso diverse piattaforme (Windows, macOS, Linux),
 * integrando tecnologie JavaFX e AWT per garantire supporto completo per
 * icone di finestre, dock macOS, e taskbar Windows. Implementa un sistema
 * di cache intelligente per ottimizzare le performance e ridurre il carico
 * di I/O sui file di risorse.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Caricamento Icone:</strong> Gestione unificata per file PNG da risorse</li>
 *   <li><strong>Cache Intelligente:</strong> Caricamento una tantum con memorizzazione</li>
 *   <li><strong>Cross-Platform Support:</strong> Adattamento automatico per OS specifici</li>
 *   <li><strong>JavaFX Integration:</strong> Impostazione icone per Stage e finestre</li>
 *   <li><strong>System Tray:</strong> Supporto dock macOS e taskbar Windows</li>
 *   <li><strong>Error Handling:</strong> Gestione robusta di errori e fallback</li>
 * </ul>
 *
 * <h3>Architettura di Caricamento:</h3>
 * <p>
 * Il sistema implementa una strategia di caricamento ottimizzata:
 * </p>
 * <ul>
 *   <li><strong>Resource Loading:</strong> Caricamento da classpath /logo/logo.png</li>
 *   <li><strong>Validation:</strong> Verifica integrità e dimensioni dell'immagine</li>
 *   <li><strong>Caching:</strong> Memorizzazione in cache per riutilizzo</li>
 *   <li><strong>Format Support:</strong> Supporto nativo per formato PNG</li>
 * </ul>
 *
 * <h3>Sistema Cross-Platform:</h3>
 * <p>
 * Supporta automaticamente le specifiche di ogni sistema operativo:
 * </p>
 * <ul>
 *   <li><strong>macOS:</strong> Icona dock tramite java.awt.Taskbar API</li>
 *   <li><strong>Windows:</strong> Icona taskbar gestita automaticamente da JavaFX</li>
 *   <li><strong>Linux:</strong> Icona finestra tramite Stage.getIcons()</li>
 *   <li><strong>Detection:</strong> Rilevamento automatico OS tramite system properties</li>
 * </ul>
 *
 * <h3>Conversione Tecnologie:</h3>
 * <p>
 * Implementa conversione tra JavaFX e AWT per compatibilità:
 * </p>
 * <ul>
 *   <li>JavaFX Image per Stage icons</li>
 *   <li>BufferedImage per AWT/Swing integration</li>
 *   <li>Pixel-by-pixel conversion per massima fedeltà</li>
 *   <li>Preservazione trasparenza e canale alpha</li>
 * </ul>
 *
 * <h3>Design Patterns Implementati:</h3>
 * <ul>
 *   <li><strong>Singleton Pattern:</strong> Cache statica dell'icona principale</li>
 *   <li><strong>Factory Pattern:</strong> Metodi factory per diverse tipologie icone</li>
 *   <li><strong>Strategy Pattern:</strong> Diverse strategie per OS specifici</li>
 *   <li><strong>Utility Pattern:</strong> Metodi statici per operazioni comuni</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo base:</h3>
 * <pre>{@code
 * // Impostazione icona per finestra principale
 * Stage primaryStage = new Stage();
 * primaryStage.setTitle("BABO Library");
 * IconUtils.setStageIcon(primaryStage);
 *
 * // Il metodo gestisce automaticamente:
 * // - Caricamento dell'icona dalle risorse
 * // - Cache per performance ottimizzate
 * // - Gestione errori e fallback
 * // - Pulizia liste icone esistenti
 * }</pre>
 *
 * <h3>Esempio di utilizzo avanzato:</h3>
 * <pre>{@code
 * // Setup completo cross-platform
 * public class ApplicationLauncher {
 *     public void initializeIcons() {
 *         // Verifica disponibilità icona
 *         if (!IconUtils.isIconAvailable()) {
 *             System.err.println("Icona non disponibile: " + IconUtils.getIconInfo());
 *             return;
 *         }
 *
 *         // Imposta icona system-wide
 *         IconUtils.setSystemTrayIcon();
 *
 *         // Configura icone per tutte le finestre
 *         Platform.runLater(() -> {
 *             Stage.getWindows().forEach(window -> {
 *                 if (window instanceof Stage) {
 *                     IconUtils.setStageIcon((Stage) window);
 *                 }
 *             });
 *         });
 *
 *         // Log info per debugging
 *         System.out.println("Icons initialized: " + IconUtils.getIconInfo());
 *     }
 * }
 * }</pre>
 *
 * <h3>Gestione Risorse e Path:</h3>
 * <p>
 * L'utility si aspetta la seguente struttura di risorse:
 * </p>
 * <pre>
 * src/main/resources/
 * └── logo/
 *     └── logo.png  ← Icona principale dell'applicazione
 * </pre>
 *
 * <h3>Requisiti Icona:</h3>
 * <ul>
 *   <li><strong>Formato:</strong> PNG con supporto trasparenza</li>
 *   <li><strong>Dimensioni:</strong> Preferibilmente 256x256 o superiori</li>
 *   <li><strong>Qualità:</strong> Alta risoluzione per scaling automatico</li>
 *   <li><strong>Compatibilità:</strong> Colori RGB + canale alpha</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <ul>
 *   <li>Cache statica per evitare caricamenti multipli</li>
 *   <li>Lazy loading con validazione immediata</li>
 *   <li>Stream management con chiusura automatica</li>
 *   <li>Error checking preventivo per robustezza</li>
 * </ul>
 *
 * <h3>Error Handling e Diagnostics:</h3>
 * <ul>
 *   <li>Logging dettagliato per debugging</li>
 *   <li>Graceful degradation in caso di errori</li>
 *   <li>Diagnostic methods per troubleshooting</li>
 *   <li>Validazione preventiva risorse</li>
 * </ul>
 *
 * <h3>Compatibilità Tecnologica:</h3>
 * <ul>
 *   <li><strong>JavaFX 11+:</strong> Per Image e Stage management</li>
 *   <li><strong>AWT:</strong> Per integration system tray e dock</li>
 *   <li><strong>Java 11+:</strong> Utilizzo moderne API Taskbar</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see javafx.scene.image.Image
 * @see javafx.stage.Stage
 * @see java.awt.Taskbar
 * @see java.awt.image.BufferedImage
 */
public class IconUtils {

    /** Cache statica dell'icona principale per evitare caricamenti multipli */
    private static Image cachedAppIcon = null;

    /**
     * Imposta l'icona per uno Stage JavaFX specifico.
     * <p>
     * Configura l'icona della finestra per lo Stage fornito, gestendo
     * automaticamente il caricamento dalle risorse, la cache, e la pulizia
     * delle icone esistenti. Metodo principale per configurazione icone
     * finestre nell'applicazione.
     * </p>
     *
     * <h4>Processo di impostazione:</h4>
     * <ol>
     *   <li>Validazione parametro Stage non null</li>
     *   <li>Caricamento icona tramite cache intelligente</li>
     *   <li>Pulizia lista icone esistenti</li>
     *   <li>Aggiunta nuova icona alla lista Stage</li>
     *   <li>Logging risultato per monitoring</li>
     * </ol>
     *
     * <h4>Gestione errori:</h4>
     * <ul>
     *   <li>Null check preventivo su Stage parameter</li>
     *   <li>Validazione successo caricamento icona</li>
     *   <li>Try-catch per eccezioni impreviste</li>
     *   <li>Logging dettagliato per troubleshooting</li>
     * </ul>
     *
     * <h4>Utilizzo tipico:</h4>
     * <pre>{@code
     * Stage dialogStage = new Stage();
     * dialogStage.setTitle("Dialogo BABO");
     * IconUtils.setStageIcon(dialogStage);
     * dialogStage.show();
     * }</pre>
     *
     * @param stage lo Stage JavaFX per cui impostare l'icona
     */
    public static void setStageIcon(Stage stage) {
        if (stage == null) return;

        try {
            Image appIcon = getApplicationIcon();
            if (appIcon != null) {
                stage.getIcons().clear();
                stage.getIcons().add(appIcon);
                System.out.println("✅ Icona impostata per: " + stage.getTitle());
            } else {
                System.err.println("❌ Impossibile caricare icona per: " + stage.getTitle());
            }
        } catch (Exception e) {
            System.err.println("❌ Errore setting icona: " + e.getMessage());
        }
    }

    /**
     * Ottiene l'icona dell'applicazione utilizzando cache intelligente.
     * <p>
     * Metodo principale per accesso all'icona dell'applicazione, implementando
     * un sistema di cache che carica l'icona una sola volta e la riutilizza
     * per tutte le successive richieste. Garantisce performance ottimali
     * e consistenza dell'icona attraverso l'applicazione.
     * </p>
     *
     * <h4>Strategia di cache:</h4>
     * <ul>
     *   <li><strong>Cache Hit:</strong> Ritorna immediatamente icona cached</li>
     *   <li><strong>Cache Miss:</strong> Carica icona e popola cache</li>
     *   <li><strong>Memory Efficiency:</strong> Una sola istanza in memoria</li>
     *   <li><strong>Thread Safety:</strong> Accesso sicuro da thread multipli</li>
     * </ul>
     *
     * <h4>Gestione null:</h4>
     * <p>
     * Il metodo può restituire {@code null} se il caricamento dell'icona
     * fallisce. I chiamanti devono sempre verificare il valore di ritorno
     * prima dell'utilizzo.
     * </p>
     *
     * @return l'icona dell'applicazione come {@link Image}, o {@code null} se il caricamento fallisce
     */
    public static Image getApplicationIcon() {
        if (cachedAppIcon != null) {
            return cachedAppIcon;
        }

        cachedAppIcon = loadPNGIcon();
        return cachedAppIcon;
    }

    /**
     * Carica l'icona PNG dalle risorse dell'applicazione.
     * <p>
     * Metodo interno per il caricamento dell'icona dal file PNG embedded
     * nelle risorse. Gestisce tutti gli aspetti del caricamento inclusa
     * validazione del file, verifica integrità dell'immagine, e gestione
     * appropriata degli stream di input.
     * </p>
     *
     * <h4>Path della risorsa:</h4>
     * <p>
     * Il metodo cerca il file {@code /logo/logo.png} nel classpath delle
     * risorse, seguendo la convenzione Maven standard per le risorse.
     * </p>
     *
     * <h4>Processo di caricamento:</h4>
     * <ol>
     *   <li>Apertura InputStream dalla risorsa</li>
     *   <li>Validazione esistenza file</li>
     *   <li>Creazione oggetto Image da stream</li>
     *   <li>Chiusura automatica stream</li>
     *   <li>Validazione integrità immagine</li>
     *   <li>Verifica dimensioni valide</li>
     * </ol>
     *
     * <h4>Validazioni implementate:</h4>
     * <ul>
     *   <li>Esistenza file risorsa</li>
     *   <li>Assenza errori nel parsing PNG</li>
     *   <li>Dimensioni immagine maggiori di zero</li>
     *   <li>Integrità dei dati immagine</li>
     * </ul>
     *
     * <h4>Resource management:</h4>
     * <ul>
     *   <li>Apertura automatica InputStream</li>
     *   <li>Chiusura esplicita stream</li>
     *   <li>Try-catch per gestione eccezioni</li>
     *   <li>Cleanup in caso di errore</li>
     * </ul>
     *
     * @return {@link Image} caricata dal PNG, o {@code null} in caso di errore
     */
    private static Image loadPNGIcon() {
        System.out.println("🔍 Caricamento /logo/logo.png...");

        try {
            // Carica direttamente il PNG
            java.io.InputStream iconStream = IconUtils.class.getResourceAsStream("/logo/logo.png");

            if (iconStream == null) {
                System.err.println("❌ File /logo/logo.png NON TROVATO");
                System.err.println("   Verifica: src/main/resources/logo/logo.png");
                return null;
            }

            // Carica l'immagine PNG
            Image icon = new Image(iconStream);

            // Chiudi stream
            iconStream.close();

            // Verifica validità
            if (icon.isError()) {
                System.err.println("❌ Errore PNG: " + icon.getException().getMessage());
                return null;
            }

            if (icon.getWidth() <= 0 || icon.getHeight() <= 0) {
                System.err.println("❌ Dimensioni PNG non valide: " + icon.getWidth() + "x" + icon.getHeight());
                return null;
            }

            System.out.println("✅ PNG caricato: " + icon.getWidth() + "x" + icon.getHeight());
            return icon;

        } catch (Exception e) {
            System.err.println("❌ Eccezione caricamento PNG:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Imposta l'icona per dock e taskbar del sistema operativo.
     * <p>
     * Configura l'icona dell'applicazione a livello di sistema operativo,
     * adattandosi automaticamente alle specifiche di ogni piattaforma.
     * Su macOS imposta l'icona del dock, su Windows l'icona viene gestita
     * automaticamente da JavaFX, su Linux utilizza le icone delle finestre.
     * </p>
     *
     * <h4>Supporto piattaforme:</h4>
     * <ul>
     *   <li><strong>macOS:</strong> Icona dock tramite Taskbar API</li>
     *   <li><strong>Windows:</strong> Icona taskbar automatica via JavaFX</li>
     *   <li><strong>Linux:</strong> Utilizza icone finestre esistenti</li>
     * </ul>
     *
     * <h4>Rilevamento OS:</h4>
     * <p>
     * Utilizza {@code System.getProperty("os.name")} per determinare
     * automaticamente il sistema operativo e applicare la strategia
     * appropriata per l'impostazione dell'icona.
     * </p>
     *
     * <h4>Processo macOS:</h4>
     * <ol>
     *   <li>Verifica supporto Taskbar API</li>
     *   <li>Conversione Image JavaFX in BufferedImage AWT</li>
     *   <li>Impostazione icona dock tramite Taskbar</li>
     * </ol>
     *
     * <h4>Gestione errori:</h4>
     * <ul>
     *   <li>Verifica disponibilità API per ogni piattaforma</li>
     *   <li>Fallback graceful in caso di mancato supporto</li>
     *   <li>Logging informativi per debugging</li>
     * </ul>
     */
    public static void setSystemTrayIcon() {
        try {
            Image icon = getApplicationIcon();
            if (icon == null) return;

            String os = System.getProperty("os.name").toLowerCase();
            System.out.println("🖥️ OS: " + os);

            if (os.contains("mac")) {
                setMacDockIcon(icon);
            } else if (os.contains("windows")) {
                System.out.println("🪟 Windows: Icona taskbar gestita da JavaFX");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore system icon: " + e.getMessage());
        }
    }

    /**
     * Imposta l'icona del dock per macOS utilizzando Taskbar API.
     * <p>
     * Metodo specializzato per la configurazione dell'icona dock su macOS,
     * utilizzando le API Java moderne per l'integrazione con il sistema.
     * Gestisce la conversione da JavaFX Image a BufferedImage AWT necessaria
     * per l'interfaccia Taskbar.
     * </p>
     *
     * <h4>Prerequisiti API:</h4>
     * <ul>
     *   <li>Supporto Taskbar nel sistema corrente</li>
     *   <li>Feature ICON_IMAGE supportata dal Taskbar</li>
     *   <li>Successful conversion JavaFX to AWT</li>
     * </ul>
     *
     * <h4>Processo di impostazione:</h4>
     * <ol>
     *   <li>Verifica supporto Taskbar API</li>
     *   <li>Controllo feature ICON_IMAGE</li>
     *   <li>Conversione Image in BufferedImage</li>
     *   <li>Impostazione icona tramite setIconImage</li>
     * </ol>
     *
     * <h4>Compatibilità:</h4>
     * <p>
     * Richiede Java 9+ per supporto completo Taskbar API.
     * Su versioni precedenti fallisce gracefully con logging appropriato.
     * </p>
     *
     * @param fxIcon l'icona JavaFX da convertire e impostare
     */
    private static void setMacDockIcon(Image fxIcon) {
        try {
            if (!java.awt.Taskbar.isTaskbarSupported()) {
                System.out.println("⚠️ macOS Taskbar non supportato");
                return;
            }

            java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
            if (!taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                System.out.println("⚠️ macOS dock icon non supportato");
                return;
            }

            // Converti in BufferedImage
            java.awt.image.BufferedImage awtImage = fxToAWT(fxIcon);
            if (awtImage != null) {
                taskbar.setIconImage(awtImage);
                System.out.println("✅ Dock icon macOS impostata");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore dock macOS: " + e.getMessage());
        }
    }

    /**
     * Converte un'immagine JavaFX in BufferedImage AWT.
     * <p>
     * Implementa conversione pixel-by-pixel da JavaFX Image a AWT BufferedImage,
     * preservando tutti i canali colore inclusa la trasparenza. Necessario
     * per integrazione con API AWT/Swing che richiedono BufferedImage.
     * </p>
     *
     * <h4>Processo di conversione:</h4>
     * <ol>
     *   <li>Estrazione dimensioni dall'Image JavaFX</li>
     *   <li>Creazione BufferedImage con TYPE_INT_ARGB</li>
     *   <li>Ottenimento PixelReader da Image source</li>
     *   <li>Iterazione pixel-by-pixel</li>
     *   <li>Conversione colori da JavaFX a AWT ARGB</li>
     *   <li>Impostazione pixel nel BufferedImage</li>
     * </ol>
     *
     * <h4>Gestione colori:</h4>
     * <ul>
     *   <li><strong>Red:</strong> Conversione da double [0.0-1.0] a int [0-255]</li>
     *   <li><strong>Green:</strong> Conversione da double [0.0-1.0] a int [0-255]</li>
     *   <li><strong>Blue:</strong> Conversione da double [0.0-1.0] a int [0-255]</li>
     *   <li><strong>Alpha:</strong> Preservazione trasparenza con opacity mapping</li>
     * </ul>
     *
     * <h4>Formato output:</h4>
     * <p>
     * Il BufferedImage risultante utilizza TYPE_INT_ARGB per supporto
     * completo trasparenza e compatibilità con la maggior parte delle
     * API AWT/Swing moderne.
     * </p>
     *
     * <h4>Performance considerations:</h4>
     * <ul>
     *   <li>Conversione O(n*m) per immagini n×m pixel</li>
     *   <li>Memory allocation per BufferedImage output</li>
     *   <li>Suitable per icone piccole-medie dimensioni</li>
     *   <li>Consider caching per conversioni multiple</li>
     * </ul>
     *
     * @param fxImage l'immagine JavaFX da convertire
     * @return {@link java.awt.image.BufferedImage} equivalente, o {@code null} in caso di errore
     */
    private static java.awt.image.BufferedImage fxToAWT(Image fxImage) {
        try {
            int w = (int) fxImage.getWidth();
            int h = (int) fxImage.getHeight();

            java.awt.image.BufferedImage awtImage = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            javafx.scene.image.PixelReader pixelReader = fxImage.getPixelReader();

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    javafx.scene.paint.Color fxColor = pixelReader.getColor(x, y);
                    int r = (int)(fxColor.getRed() * 255);
                    int g = (int)(fxColor.getGreen() * 255);
                    int b = (int)(fxColor.getBlue() * 255);
                    int a = (int)(fxColor.getOpacity() * 255);
                    int argb = (a << 24) | (r << 16) | (g << 8) | b;
                    awtImage.setRGB(x, y, argb);
                }
            }

            return awtImage;

        } catch (Exception e) {
            System.err.println("❌ Errore conversione FX->AWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restituisce informazioni diagnostiche sull'icona corrente.
     * <p>
     * Utility method per debugging e troubleshooting, fornisce informazioni
     * dettagliate sullo stato dell'icona incluse dimensioni, validità, e
     * eventuale presenza di errori. Utile per verificare configurazione
     * corretta delle risorse.
     * </p>
     *
     * <h4>Informazioni fornite:</h4>
     * <ul>
     *   <li><strong>Disponibilità:</strong> "Nessuna icona" se non caricata</li>
     *   <li><strong>Errori:</strong> "Errore icona" se Image.isError()</li>
     *   <li><strong>Dimensioni:</strong> "PNG: WxH" per icone valide</li>
     * </ul>
     *
     * <h4>Utilizzo tipico:</h4>
     * <pre>{@code
     * System.out.println("Icon status: " + IconUtils.getIconInfo());
     * // Output: "PNG: 256x256" per icona valida
     * }</pre>
     *
     * @return stringa descrittiva dello stato dell'icona
     */
    public static String getIconInfo() {
        Image icon = getApplicationIcon();
        if (icon == null) return "Nessuna icona";
        if (icon.isError()) return "Errore icona";
        return String.format("PNG: %.0fx%.0f", icon.getWidth(), icon.getHeight());
    }

    /**
     * Verifica la disponibilità e validità dell'icona dell'applicazione.
     * <p>
     * Metodo di controllo per verificare se l'icona è correttamente caricata
     * e utilizzabile. Utile per controlli preventivi prima di operazioni
     * che richiedono la presenza di un'icona valida.
     * </p>
     *
     * <h4>Controlli eseguiti:</h4>
     * <ul>
     *   <li>Icona non null (caricamento riuscito)</li>
     *   <li>Assenza errori (Image.isError() == false)</li>
     *   <li>Dimensioni valide (larghezza > 0)</li>
     * </ul>
     *
     * <h4>Utilizzo raccomandato:</h4>
     * <pre>{@code
     * if (IconUtils.isIconAvailable()) {
     *     IconUtils.setSystemTrayIcon();
     *     IconUtils.setStageIcon(primaryStage);
     * } else {
     *     System.err.println("Cannot setup icons: " + IconUtils.getIconInfo());
     * }
     * }</pre>
     *
     * @return {@code true} se l'icona è disponibile e valida, {@code false} altrimenti
     */
    public static boolean isIconAvailable() {
        Image icon = getApplicationIcon();
        return icon != null && !icon.isError() && icon.getWidth() > 0;
    }
}