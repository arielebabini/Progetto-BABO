package org.BABO.client.ui;

import org.BABO.client.service.BookService;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Home.ApplicationProtection;
import org.BABO.client.ui.Home.IconUtils;
import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.client.ui.Home.MainWindow;
import org.BABO.client.ui.Popup.PopupManager;
import org.BABO.shared.model.Book;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.List;

/**
 * Classe principale dell'applicazione Apple Books Client
 * AGGIORNATO: Include integrazione icona applicazione
 */
public class AppleBooksClient extends Application {

    private BookService bookService;
    private Stage primaryStage;
    private boolean serverAvailable = false;
    private MainWindow mainWindow;

    @Override
    public void init() {
        System.out.println("🔧 Inizializzazione client...");
        bookService = new BookService();

        // Verifica disponibilità server
        serverAvailable = bookService.isServerAvailable();
        if (serverAvailable) {
            System.out.println("✅ Server raggiungibile");
        } else {
            System.out.println("⚠️ Server non raggiungibile - modalità offline");
        }
    }

    @Override
    public void start(Stage stage) {
        System.out.println("🚀 Avvio AppleBooksClient con PopupManager integrato");

        try {
            // 1. REGISTRA ApplicationProtection
            ApplicationProtection.registerMainStage(stage);
            System.out.println("🛡️ Protezione applicazione attivata");

            this.primaryStage = stage;

            // 2. IMPOSTA ICONA APPLICAZIONE
            setupApplicationIcon(stage);

            // 3. Crea la finestra principale (usa la tua MainWindow esistente)
            System.out.println("🎨 Creazione interfaccia utente...");
            mainWindow = new MainWindow(bookService, serverAvailable);
            StackPane root = mainWindow.createMainLayout();

            // 4. INIZIALIZZA PopupManager con il root di MainWindow
            PopupManager popupManager = PopupManager.getInstance();
            popupManager.initialize(root);
            System.out.println("✅ PopupManager inizializzato con MainWindow");

            // 5. Setup scena
            Scene scene = new Scene(root, 1300, 800);

            // Carica CSS se disponibile
            try {
                //scene.getStylesheets().add(getClass().getResource("/css/applebooks.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/scrollbar.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/auth-tabs.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("⚠️ CSS non trovato, uso stili default");
            }

            stage.setScene(scene);
            stage.setTitle("📚 Apple Books Client " + (serverAvailable ? "🌐" : "📴"));

            stage.setMinWidth(1300);  // Larghezza minima assoluta
            stage.setMinHeight(800);  // Altezza minima assoluta

            stage.setWidth(1300);     // Larghezza iniziale
            stage.setHeight(800);     // Altezza iniziale

            // 6. GESTIONE CHIUSURA MIGLIORATA
            stage.setOnCloseRequest(e -> {
                System.out.println("👋 Richiesta chiusura applicazione...");
                handleApplicationClose();
            });

            // 7. Setup eventi post-mostrazione
            stage.setOnShown(e -> {
                System.out.println("✅ Interfaccia avviata con successo!");
                System.out.println("📐 Dimensioni finestra: " + stage.getWidth() + "x" + stage.getHeight());
                System.out.println("📐 Dimensioni minime: 1200x700 (NON riducibili)");

                // Debug iniziale
                if (isDebugMode()) {
                    ApplicationProtection.debugApplicationState();
                    popupManager.debugFullState();
                    testPopupManagerSetup();
                }
            });

            // 8. Mostra applicazione
            stage.show();
            stage.centerOnScreen();

            System.out.println("🎉 AppleBooksClient avviato con successo");
            System.out.println("📐 Finestra configurata: 1300x800 (minimo NON riducibile: 1200x700)");

        } catch (Exception e) {
            System.err.println("❌ Errore fatale nell'avvio: " + e.getMessage());
            e.printStackTrace();

            // Mostra errore all'utente
            showStartupError(e);
            Platform.exit();
        }
    }

    /**
     * Setup dell'icona dell'applicazione
     */
    private void setupApplicationIcon(Stage stage) {
        System.out.println("🎨 Configurazione icona applicazione cross-platform...");

        try {
            // 1. IMPOSTA ICONA PER LA FINESTRA PRINCIPALE
            IconUtils.setStageIcon(stage);

            // 2. IMPOSTA ICONA PER IL SISTEMA (dock/taskbar)
            IconUtils.setSystemTrayIcon();

            // 3. DEBUG INFO
            System.out.println("📄 " + IconUtils.getIconInfo());

            // 4. VERIFICA COMPATIBILITÀ
            debugCrossPlatformCompatibility();

        } catch (Exception e) {
            System.err.println("❌ Errore nel setup icona applicazione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void debugCrossPlatformCompatibility() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String javaVersion = System.getProperty("java.version");

            System.out.println("🖥️ === INFO SISTEMA ===");
            System.out.println("   OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            System.out.println("   Java: " + javaVersion);
            System.out.println("   JavaFX: " + System.getProperty("javafx.version", "Non disponibile"));

            // Test compatibilità icona
            boolean iconWorking = IconUtils.isIconAvailable();
            System.out.println("   Icona funzionante: " + (iconWorking ? "✅ SÌ" : "❌ NO"));

            // Raccomandazioni specifiche per OS
            if (osName.contains("mac")) {
                System.out.println("🍎 macOS rilevato - Raccomandazioni:");
                System.out.println("   • Usa PNG per migliore compatibilità");
                System.out.println("   • Dimensioni consigliate: 16x16, 32x32, 128x128, 512x512");
            } else if (osName.contains("windows")) {
                System.out.println("🪟 Windows rilevato - Raccomandazioni:");
                System.out.println("   • ICO e PNG supportati");
                System.out.println("   • Dimensioni consigliate: 16x16, 32x32, 48x48, 256x256");
            } else if (osName.contains("linux")) {
                System.out.println("🐧 Linux rilevato - Raccomandazioni:");
                System.out.println("   • PNG consigliato");
                System.out.println("   • Dipende dal desktop environment");
            }

            System.out.println("🖥️ === FINE INFO SISTEMA ===");

        } catch (Exception e) {
            System.err.println("⚠️ Errore debug compatibilità: " + e.getMessage());
        }
    }

    /**
     * NUOVO: Carica l'icona dell'applicazione dalle risorse
     */
    private Image loadApplicationIcon() {
        try {
            // Prova prima con il file .ico
            System.out.println("🔍 Tentativo caricamento logo.ico...");
            java.io.InputStream iconStream = getClass().getResourceAsStream("/logo/logo.png");

            if (iconStream != null) {
                Image icon = new Image(iconStream);
                if (!icon.isError()) {
                    System.out.println("✅ Logo.ico caricato con successo (" + icon.getWidth() + "x" + icon.getHeight() + ")");
                    return icon;
                } else {
                    System.out.println("⚠️ Errore nel caricamento logo.ico: " + icon.getException().getMessage());
                }
            }

            // Fallback: prova con formati alternativi nella stessa cartella
            String[] alternativeFormats = {"/logo1/logo.png", "/logo1/logo.jpg", "/logo1/logo.jpeg"};

            for (String format : alternativeFormats) {
                System.out.println("🔍 Tentativo caricamento " + format + "...");
                java.io.InputStream altStream = getClass().getResourceAsStream(format);

                if (altStream != null) {
                    Image icon = new Image(altStream);
                    if (!icon.isError()) {
                        System.out.println("✅ Icona alternativa caricata: " + format);
                        return icon;
                    }
                }
            }

            // Ultimo fallback: icona generica dalla root delle risorse
            System.out.println("🔍 Tentativo caricamento /logo.ico dalla root...");
            java.io.InputStream rootStream = getClass().getResourceAsStream("/logo.ico");
            if (rootStream != null) {
                Image icon = new Image(rootStream);
                if (!icon.isError()) {
                    System.out.println("✅ Logo.ico caricato dalla root delle risorse");
                    return icon;
                }
            }

            System.out.println("⚠️ Nessuna icona trovata nelle risorse");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Errore caricamento icona: " + e.getMessage());
            return null;
        }
    }

    /**
     * NUOVO: Converte JavaFX Image in BufferedImage per AWT (macOS dock)
     */
    private java.awt.image.BufferedImage convertToBufferedImage(Image fxImage) {
        try {
            int width = (int) fxImage.getWidth();
            int height = (int) fxImage.getHeight();

            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                    width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);

            // Usa PixelReader per copiare i pixel
            javafx.scene.image.PixelReader pixelReader = fxImage.getPixelReader();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    javafx.scene.paint.Color fxColor = pixelReader.getColor(x, y);

                    int red = (int) (fxColor.getRed() * 255);
                    int green = (int) (fxColor.getGreen() * 255);
                    int blue = (int) (fxColor.getBlue() * 255);
                    int alpha = (int) (fxColor.getOpacity() * 255);

                    int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    bufferedImage.setRGB(x, y, argb);
                }
            }

            return bufferedImage;

        } catch (Exception e) {
            System.err.println("❌ Errore conversione immagine: " + e.getMessage());
            return null;
        }
    }

    /**
     * NUOVO: Crea icona generata programmaticamente se non trovata
     */
    private Image createFallbackIcon() {
        try {
            System.out.println("🎨 Creazione icona fallback programmatica...");

            // Crea un canvas per disegnare un'icona semplice
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(64, 64);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

            // Sfondo blu
            gc.setFill(javafx.scene.paint.Color.web("#007AFF"));
            gc.fillRoundRect(0, 0, 64, 64, 12, 12);

            // Icona libro stilizzata
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillRoundRect(16, 12, 32, 40, 2, 2);

            // Linee del libro
            gc.setStroke(javafx.scene.paint.Color.web("#007AFF"));
            gc.setLineWidth(1);
            gc.strokeLine(20, 20, 44, 20);
            gc.strokeLine(20, 26, 44, 26);
            gc.strokeLine(20, 32, 44, 32);
            gc.strokeLine(20, 38, 36, 38);

            // Converte il canvas in immagine
            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(javafx.scene.paint.Color.TRANSPARENT);

            Image icon = canvas.snapshot(params, null);
            System.out.println("✅ Icona fallback creata programmaticamente");

            return icon;

        } catch (Exception e) {
            System.err.println("❌ Errore creazione icona fallback: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gestisce la chiusura dell'applicazione
     */
    private void handleApplicationClose() {
        try {
            System.out.println("🔒 Inizio procedura chiusura...");

            // 1. Chiudi tutti i popup tramite PopupManager
            PopupManager popupManager = PopupManager.getInstance();
            if (popupManager.hasActivePopups()) {
                System.out.println("🔒 Chiusura popup aperti...");
                popupManager.closeAllPopups();

                // Attendi un momento per la chiusura
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    finalizeApplicationClose();
                });
            } else {
                finalizeApplicationClose();
            }

        } catch (Exception e) {
            System.err.println("⚠️ Errore durante chiusura: " + e.getMessage());
            finalizeApplicationClose();
        }
    }

    /**
     * Finalizza la chiusura dell'applicazione
     */
    private void finalizeApplicationClose() {
        try {
            // Cleanup cache immagini
            ImageUtils.clearImageCache();

            // Cleanup MainWindow se ha metodi di cleanup
            if (mainWindow != null) {
                // MainWindow ha AuthenticationManager che può fare cleanup
                if (mainWindow.getAuthManager() != null) {
                    mainWindow.getAuthManager().shutdown();
                }
                System.out.println("🧹 MainWindow cleanup completato");
            }

            System.out.println("✅ Chiusura completata correttamente");
            Platform.exit();

        } catch (Exception e) {
            System.err.println("⚠️ Errore nella finalizzazione: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        System.out.println("🛑 Stop applicazione...");

        try {
            // Cleanup servizi
            if (bookService != null) {
                bookService.shutdown();
            }

            // Cleanup finale PopupManager
            PopupManager.getInstance().emergencyReset();

        } catch (Exception e) {
            System.err.println("⚠️ Errore durante stop: " + e.getMessage());
        }

        System.out.println("✅ Stop completato");
    }

    /**
     * Test del setup PopupManager
     */
    private void testPopupManagerSetup() {
        System.out.println("🧪 Test setup PopupManager");

        try {
            PopupManager popupManager = PopupManager.getInstance();

            if (popupManager.isInitialized()) {
                System.out.println("✅ PopupManager correttamente inizializzato");
                popupManager.runIntegrityCheck();
            } else {
                System.err.println("❌ PopupManager NON inizializzato!");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore test PopupManager: " + e.getMessage());
        }
    }

    /**
     * Verifica se siamo in modalità debug
     */
    private boolean isDebugMode() {
        return Boolean.getBoolean("debug") ||
                System.getProperty("app.debug") != null ||
                "development".equals(System.getProperty("app.environment"));
    }

    /**
     * Mostra errore di avvio
     */
    private void showStartupError(Exception e) {
        Platform.runLater(() -> {
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Errore Avvio");
                alert.setHeaderText("Errore durante l'avvio dell'applicazione");
                alert.setContentText("Errore: " + e.getMessage() +
                        "\n\nL'applicazione verrà chiusa.");

                // Imposta anche l'icona per l'alert se disponibile
                try {
                    Image appIcon = loadApplicationIcon();
                    if (appIcon != null) {
                        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        alertStage.getIcons().add(appIcon);
                    }
                } catch (Exception iconError) {
                    // Ignora errori dell'icona nell'alert
                }

                alert.showAndWait();
            } catch (Exception alertError) {
                System.err.println("❌ Errore anche nel mostrare alert: " + alertError.getMessage());
            }
        });
    }

    // =====================================================
    // METODI PUBBLICI STATICI PER POPUP MANAGER
    // =====================================================

    /**
     * METODO PRINCIPALE per aprire dettagli libro con PopupManager
     * DA USARE al posto di BookDetailsPopup.create()
     */
    public static void openBookDetails(Book book, List<Book> collection, AuthenticationManager authManager) {
        if (book == null) {
            System.err.println("❌ openBookDetails: libro null");
            return;
        }

        System.out.println("📖 Apertura dettagli libro: " + book.getTitle());

        try {
            PopupManager popupManager = PopupManager.getInstance();

            if (!popupManager.isInitialized()) {
                System.err.println("❌ PopupManager non inizializzato!");
                return;
            }

            popupManager.showBookDetails(book, collection, authManager);
            System.out.println("✅ Popup libro aperto tramite PopupManager");

        } catch (Exception e) {
            System.err.println("❌ Errore apertura popup: " + e.getMessage());
            e.printStackTrace();

            // Fallback: mostra errore
            showError("Errore nell'apertura dei dettagli del libro: " + e.getMessage());
        }
    }

    /**
     * METODO per aprire popup raccomandazione
     */
    public static void openRecommendationDetails(Book book, List<Book> collection, AuthenticationManager authManager) {
        if (book == null) {
            System.err.println("❌ openRecommendationDetails: libro null");
            return;
        }

        System.out.println("💡 Apertura raccomandazione: " + book.getTitle());

        try {
            PopupManager popupManager = PopupManager.getInstance();

            if (!popupManager.isInitialized()) {
                System.err.println("❌ PopupManager non inizializzato!");
                return;
            }

            popupManager.showRecommendationDetails(book, collection, authManager);
            System.out.println("✅ Popup raccomandazione aperto tramite PopupManager");

        } catch (Exception e) {
            System.err.println("❌ Errore apertura popup raccomandazione: " + e.getMessage());
            e.printStackTrace();
            showError("Errore nell'apertura dei dettagli della raccomandazione: " + e.getMessage());
        }
    }

    /**
     * Mostra errore generico
     */
    private static void showError(String message) {
        Platform.runLater(() -> {
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText(null);
                alert.setContentText(message);

                javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");

                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("❌ Errore nel mostrare alert: " + e.getMessage());
            }
        });
    }

    // =====================================================
    // METODI DI DEBUG E UTILITÀ
    // =====================================================

    /**
     * DEBUG completo dello stato dell'applicazione
     */
    public static void debugApplicationState() {
        System.out.println("🔍 ===== DEBUG APPLICAZIONE COMPLETO =====");

        try {
            // Debug ApplicationProtection
            System.out.println("\n--- PROTEZIONE APPLICAZIONE ---");
            ApplicationProtection.debugApplicationState();

            // Debug PopupManager
            System.out.println("\n--- POPUP MANAGER ---");
            PopupManager popupManager = PopupManager.getInstance();
            popupManager.debugPopupState();

        } catch (Exception e) {
            System.err.println("❌ Errore debug applicazione: " + e.getMessage());
        }

        System.out.println("🔍 ===== FINE DEBUG =====");
    }

    /**
     * Chiusura di emergenza di tutti i popup
     */
    public static void emergencyCloseAllPopups() {
        System.out.println("🚨 EMERGENZA: Chiusura di tutti i popup");

        try {
            PopupManager popupManager = PopupManager.getInstance();
            popupManager.emergencyReset();
            System.out.println("✅ Popup di emergenza chiusi");

        } catch (Exception e) {
            System.err.println("❌ Errore chiusura emergenza: " + e.getMessage());
        }
    }

    /**
     * Test dell'integrazione PopupManager
     */
    public static void testPopupManagerIntegration() {
        System.out.println("🧪 Test integrazione PopupManager");

        try {
            PopupManager popupManager = PopupManager.getInstance();

            if (popupManager.isInitialized()) {
                popupManager.runIntegrityCheck();
                System.out.println("✅ Test integrazione superato");
            } else {
                System.err.println("❌ PopupManager non inizializzato per test");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore test integrazione: " + e.getMessage());
        }
    }

    // =====================================================
    // GETTERS PER ACCESSO AI COMPONENTI
    // =====================================================

    /**
     * Ottiene il MainWindow
     */
    public MainWindow getMainWindow() {
        return mainWindow;
    }

    /**
     * Ottiene il BookService
     */
    public BookService getBookService() {
        return bookService;
    }

    /**
     * Verifica se il server è disponibile
     */
    public boolean isServerAvailable() {
        return serverAvailable;
    }

    /**
     * Ottiene lo stage principale
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Ottiene il container principale (tramite MainWindow)
     */
    public StackPane getMainContainer() {
        return mainWindow != null ? mainWindow.getMainRoot() : null;
    }

    /**
     * Verifica se l'applicazione è completamente inizializzata
     */
    public boolean isFullyInitialized() {
        return primaryStage != null &&
                mainWindow != null &&
                PopupManager.getInstance().isInitialized();
    }
}