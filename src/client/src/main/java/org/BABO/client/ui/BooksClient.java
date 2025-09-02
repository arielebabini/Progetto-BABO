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
 * Punto di ingresso principale per l'applicazione client JavaFX.
 * <p>
 * Questa classe estende {@link javafx.application.Application} e funge da
 * coordinatore centrale per l'avvio, la configurazione e la gestione del
 * ciclo di vita dell'applicazione. È responsabile dell'inizializzazione dei
 * servizi di base, della configurazione della finestra principale (Stage),
 * della gestione degli eventi di avvio e chiusura e dell'integrazione con
 * altri componenti UI come {@link MainWindow} e {@link PopupManager}.
 * </p>
 *
 * <h3>Ciclo di vita dell'applicazione:</h3>
 * <p>
 * La classe gestisce le tre fasi principali del ciclo di vita JavaFX:
 * </p>
 * <ul>
 * <li><strong>init():</strong> Inizializzazione non-grafica, come la verifica della
 * connessione al server e la creazione di {@link BookService}.</li>
 * <li><strong>start():</strong> La fase principale in cui viene costruita e mostrata
 * l'interfaccia utente, incluse la finestra principale, l'icona e la scena.</li>
 * <li><strong>stop():</strong> Eseguita al momento della chiusura, per il
 * rilascio delle risorse e la pulizia finale.</li>
 * </ul>
 *
 * <h3>Architettura e integrazioni:</h3>
 * <p>
 * BooksClient non è un semplice "main" ma un gestore di sistema che integra
 * vari moduli per creare un'applicazione coesa. Implementa il pattern "Controller"
 * a livello di sistema, delegando la logica UI a {@link MainWindow} e la gestione
 * dei popup a {@link PopupManager}.
 * </p>
 * <ul>
 * <li><strong>{@link BookService}:</strong> Gestisce la comunicazione con il backend
 * e la logica di business.</li>
 * <li><strong>{@link MainWindow}:</strong> Costruisce l'intera interfaccia utente
 * principale (sidebar, header, content area).</li>
 * <li><strong>{@link PopupManager}:</strong> Un singleton che gestisce in modo
 * centralizzato l'apertura e la chiusura di tutti i popup e le finestre modali.</li>
 * <li><strong>{@link ApplicationProtection}:</strong> Un sistema di protezione
 * che impedisce avvii multipli dell'applicazione.</li>
 * </ul>
 *
 * <h3>Gestione della chiusura e degli errori:</h3>
 * <p>
 * La classe implementa una gestione della chiusura sicura (graceful shutdown) che
 * assicura la corretta terminazione di tutti i componenti attivi, inclusa la chiusura
 * di eventuali popup aperti, prima di uscire dall'applicazione. In caso di errori
 * fatali durante l'avvio, mostra un alert all'utente e termina il processo.
 * </p>
 *
 * <h3>Metodi statici pubblici:</h3>
 * <p>
 * Fornisce metodi statici pubblici per consentire ad altri componenti dell'applicazione
 * di interagire con il sistema in modo centralizzato, ad esempio per aprire
 * un popup dettagliato di un libro.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see org.BABO.client.ui.Home.MainWindow
 * @see org.BABO.client.ui.Popup.PopupManager
 * @see org.BABO.client.service.BookService
 */
public class BooksClient extends Application {

    private BookService bookService;
    private boolean serverAvailable = false;
    private MainWindow mainWindow;

    /**
     * Metodo di inizializzazione dell'applicazione.
     * <p>
     * Questo metodo viene chiamato dal launcher JavaFX prima di {@link #start(Stage)}.
     * Viene utilizzato per preparare le risorse non-grafiche e i servizi essenziali.
     * La principale operazione qui è la creazione e la verifica del {@link BookService}
     * per determinare la disponibilità del server e la modalità operativa (online/offline).
     * </p>
     */
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

    /**
     * Punto di ingresso principale dell'applicazione JavaFX.
     * <p>
     * Questo metodo viene chiamato dopo che {@link #init()} è stato completato.
     * È il luogo in cui viene costruita l'intera interfaccia utente.
     * </p>
     * <h4>Processo di avvio:</h4>
     * <ol>
     * <li>Registrazione dello stage principale per la protezione contro avvii multipli.</li>
     * <li>Configurazione dell'icona dell'applicazione per la finestra e per il sistema (dock/taskbar).</li>
     * <li>Creazione dell'istanza di {@link MainWindow} e del suo layout.</li>
     * <li>Inizializzazione del {@link PopupManager} con il root dell'applicazione.</li>
     * <li>Configurazione della scena, del titolo e delle dimensioni della finestra.</li>
     * <li>Setup dei gestori di eventi per la chiusura della finestra e per gli eventi post-mostrazione.</li>
     * <li>Mostra la finestra all'utente.</li>
     * </ol>
     * @param stage Lo stage primario per questa applicazione, dove la scena dell'applicazione può essere impostata.
     */
    @Override
    public void start(Stage stage) {
        System.out.println("🚀 Avvio BooksClient con PopupManager integrato");

        try {
            // 1. REGISTRA ApplicationProtection
            ApplicationProtection.registerMainStage(stage);
            System.out.println("🛡️ Protezione applicazione attivata");

            // 2. IMPOSTA icona applicazione
            setupApplicationIcon(stage);

            // 3. Crea la finestra principale
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
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/scrollbar.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/auth-tabs.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("⚠️ CSS non trovato, uso stili default");
            }

            stage.setScene(scene);
            stage.setTitle("📚 Books Client " + (serverAvailable ? "🌐" : "📴"));

            stage.setMinWidth(1300);
            stage.setMinHeight(800);

            stage.setWidth(1300);
            stage.setHeight(800);

            // 6. GEstione chiusura
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

            System.out.println("🎉 BooksClient avviato con successo");
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
     * Configura l'icona dell'applicazione per la finestra principale e il sistema operativo.
     * <p>
     * Questo metodo si avvale della classe di utilità {@link IconUtils} per impostare l'icona
     * in modo corretto su diverse piattaforme, garantendo che l'icona sia visibile nella
     * finestra, nella barra delle applicazioni o nel dock del sistema. Esegue anche
     * un controllo di compatibilità per fornire informazioni di debug sul sistema.
     * </p>
     * @param stage Lo stage principale su cui impostare l'icona.
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

    /**
     * Esegue una verifica di compatibilità tra sistemi operativi e fornisce raccomandazioni.
     * <p>
     * Questo metodo interno stampa informazioni diagnostiche sul sistema in esecuzione,
     * inclusi il nome del sistema operativo, la versione di Java e JavaFX. Offre inoltre
     * raccomandazioni specifiche per la gestione delle icone a seconda della piattaforma
     * (es. macOS, Windows, Linux).
     * </p>
     */
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
     * Carica l'icona dell'applicazione dalle risorse integrate.
     * <p>
     * Tenta di caricare un'immagine dal percorso delle risorse (`/logo/logo.png`).
     * Se il caricamento ha successo, restituisce l'oggetto {@link Image} corrispondente.
     * Altrimenti, restituisce `null` e registra l'errore.
     * </p>
     * @return L'oggetto {@link Image} dell'icona se caricato correttamente, altrimenti {@code null}.
     */
    private Image loadApplicationIcon() {
        try {
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
            return null;

        } catch (Exception e) {
            System.err.println("❌ Errore caricamento icona: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gestisce la procedura di chiusura dell'applicazione.
     * <p>
     * Questo metodo viene chiamato quando l'utente tenta di chiudere la finestra principale.
     * Il suo scopo è garantire una chiusura sicura, chiudendo tutti i popup aperti e
     * delegando le operazioni di pulizia finali a {@link #finalizeApplicationClose()}.
     * </p>
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
     * Esegue le operazioni finali di pulizia prima della terminazione dell'applicazione.
     * <p>
     * Questo metodo è responsabile di:
     * <ul>
     * <li>Svuotare la cache delle immagini tramite {@link ImageUtils}.</li>
     * <li>Eseguire la pulizia specifica della {@link MainWindow} e del suo
     * {@link AuthenticationManager}.</li>
     * <li>Terminare l'applicazione tramite {@link Platform#exit()}.</li>
     * </ul>
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

    /**
     * Metodo di stop dell'applicazione.
     * <p>
     * Questo metodo viene chiamato dal sistema quando l'applicazione sta per terminare.
     * È il luogo ideale per rilasciare le risorse che non sono gestite dalla chiusura
     * della finestra principale, come la chiusura dei servizi di business e il reset
     * finale del {@link PopupManager}.
     * </p>
     */
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
     * Esegue un test di integrità del {@link PopupManager}.
     * <p>
     * Questo metodo di debug verifica se l'istanza di {@link PopupManager} è stata
     * inizializzata correttamente e, in caso affermativo, esegue un controllo
     * di integrità interno per validare il suo stato.
     * </p>
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
     * Verifica se l'applicazione è in modalità di debug.
     * <p>
     * Il metodo controlla diverse proprietà di sistema per determinare se l'applicazione
     * è stata avviata con flag di debug attivi, come `-Ddebug=true` o `-Dapp.environment=development`.
     * </p>
     * @return {@code true} se l'applicazione è in modalità di debug, altrimenti {@code false}.
     */
    private boolean isDebugMode() {
        return Boolean.getBoolean("debug") ||
                System.getProperty("app.debug") != null ||
                "development".equals(System.getProperty("app.environment"));
    }

    /**
     * Mostra un alert di errore all'utente in caso di fallimento grave dell'avvio.
     * <p>
     * Questo metodo crea e visualizza una finestra di dialogo modale che informa
     * l'utente di un errore critico e lo informa che l'applicazione verrà chiusa.
     * Viene eseguito sul thread JavaFX per garantire la sicurezza.
     * </p>
     * @param e L'eccezione che ha causato l'errore di avvio.
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
     * Apre un popup con i dettagli di un libro.
     * <p>
     * Questo è un metodo statico che funge da punto di accesso pubblico per l'apertura
     * di un popup contenente i dettagli di un libro specifico. Delega la logica
     * al {@link PopupManager} dopo aver verificato che sia inizializzato correttamente.
     * Fornisce una fallback in caso di errore.
     * </p>
     * @param book L'oggetto {@link Book} di cui mostrare i dettagli.
     * @param collection La collezione di libri a cui appartiene il libro selezionato,
     * per consentire la navigazione nel popup.
     * @param authManager Il gestore di autenticazione necessario per alcune operazioni
     * all'interno del popup.
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
     * Mostra un alert di errore generico all'utente.
     * <p>
     * Un metodo di utilità statico per visualizzare un alert di errore con un messaggio
     * personalizzato. Viene eseguito in modo sicuro sul thread JavaFX.
     * </p>
     * @param message Il messaggio di errore da visualizzare all'utente.
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
    // GETTERS PER ACCESSO AI COMPONENTI
    // =====================================================

    /**
     * Restituisce l'istanza di {@link BookService} utilizzata dall'applicazione.
     * @return Il servizio di gestione dei libri.
     */
    public BookService getBookService() {
        return bookService;
    }
}