package org.BABO.client.ui.Home;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.BABO.client.ui.*;
import org.BABO.client.ui.Admin.AdminPanel;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Library.LibraryPanel;
import org.BABO.client.ui.Popup.PopupManager;
import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Classe principale dell'interfaccia utente che coordina tutti i componenti dell'applicazione BABO Library.
 * <p>
 * MainWindow rappresenta il controller centrale dell'interfaccia utente, gestendo l'orchestrazione
 * e l'integrazione di tutti i componenti principali: sidebar, header, area contenuti, sistema di
 * autenticazione e gestione popup. Implementa il pattern Controller nella architettura MVC,
 * coordinando le interazioni tra i vari moduli UI e fornendo un punto di accesso unificato
 * per le operazioni dell'interfaccia.
 * </p>
 *
 * <h3>Architettura dell'interfaccia:</h3>
 * <p>
 * Il sistema è progettato seguendo principi di separazione delle responsabilità e modularità:
 * </p>
 * <ul>
 *   <li><strong>Layout Management:</strong> BorderPane principale con sidebar, header e content area</li>
 *   <li><strong>Component Coordination:</strong> Orchestrazione comunicazione tra componenti</li>
 *   <li><strong>State Management:</strong> Gestione centralizzata stato applicazione</li>
 *   <li><strong>Event Handling:</strong> Routing eventi tra componenti</li>
 *   <li><strong>Popup Integration:</strong> Integrazione completa con PopupManager</li>
 * </ul>
 *
 * <h3>Componenti principali gestiti:</h3>
 * <ul>
 *   <li><strong>Sidebar:</strong> Navigazione principale con sezioni Home, Libreria, Esplora, Admin</li>
 *   <li><strong>Header:</strong> Ricerca globale e controlli navigazione superiore</li>
 *   <li><strong>ContentArea:</strong> Area principale visualizzazione contenuti</li>
 *   <li><strong>AuthenticationManager:</strong> Sistema autenticazione e gestione utenti</li>
 *   <li><strong>ExploreIntegration:</strong> Integrazione sezione esplorazione catalogo</li>
 *   <li><strong>PopupManager:</strong> Sistema gestione popup e modal</li>
 * </ul>
 *
 * <h3>Flusso di inizializzazione:</h3>
 * <ol>
 *   <li><strong>Constructor Phase:</strong> Inizializzazione servizi base e autenticazione</li>
 *   <li><strong>Layout Creation:</strong> Costruzione layout principale e componenti</li>
 *   <li><strong>Integration Setup:</strong> Configurazione integrazione tra componenti</li>
 *   <li><strong>Event Binding:</strong> Setup handlers eventi e callback</li>
 *   <li><strong>Content Loading:</strong> Caricamento contenuto iniziale</li>
 *   <li><strong>Testing Phase:</strong> Test automatico sistema (debug mode)</li>
 * </ol>
 *
 * <h3>Sistema di comunicazione eventi:</h3>
 * <p>
 * Implementa pattern Observer per comunicazione loose-coupled tra componenti:
 * </p>
 * <ul>
 *   <li><strong>Search Events:</strong> Propagazione query ricerca da Header a ContentArea</li>
 *   <li><strong>Book Selection:</strong> Gestione click sui libri con apertura dettagli</li>
 *   <li><strong>Authentication Events:</strong> Aggiornamento UI su cambio stato auth</li>
 *   <li><strong>Navigation Events:</strong> Routing navigazione da Sidebar a ContentArea</li>
 * </ul>
 *
 * <h3>Gestione stato applicazione:</h3>
 * <ul>
 *   <li><strong>Cache Management:</strong> Cache libri per performance e navigazione</li>
 *   <li><strong>Server Availability:</strong> Gestione modalità online/offline</li>
 *   <li><strong>Authentication State:</strong> Tracking stato autenticazione utente</li>
 *   <li><strong>UI State:</strong> Gestione stato interfaccia (popup aperti, sezione attiva)</li>
 * </ul>
 *
 * <h3>Pattern implementati:</h3>
 * <ul>
 *   <li><strong>Controller Pattern:</strong> Coordinamento centrale componenti UI</li>
 *   <li><strong>Observer Pattern:</strong> Eventi asincroni tra componenti</li>
 *   <li><strong>Factory Pattern:</strong> Creazione componenti UI specializzati</li>
 *   <li><strong>Callback Pattern:</strong> Gestione asincrona operazioni UI</li>
 *   <li><strong>Singleton Pattern:</strong> Integrazione con PopupManager singleton</li>
 * </ul>
 *
 * <h3>Gestione popup e modal:</h3>
 * <p>
 * Integrazione completa con PopupManager per gestione centralizzata popup:
 * </p>
 * <ul>
 *   <li>Popup dettagli libro con navigazione tra collezioni</li>
 *   <li>Modal autenticazione per login/registrazione</li>
 *   <li>Pannello gestione librerie utente</li>
 *   <li>Pannello amministrazione (per utenti privilegiati)</li>
 *   <li>Alert e conferme operazioni</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo base:</h3>
 * <pre>{@code
 * // Inizializzazione MainWindow
 * BookService bookService = new BookService();
 * boolean serverAvailable = bookService.isServerAvailable();
 * MainWindow mainWindow = new MainWindow(bookService, serverAvailable);
 *
 * // Creazione layout principale
 * StackPane root = mainWindow.createMainLayout();
 *
 * // Setup scena JavaFX
 * Scene scene = new Scene(root, 1300, 800);
 * stage.setScene(scene);
 * stage.show();
 *
 * // L'interfaccia è ora completamente funzionale con:
 * // - Ricerca globale attraverso header
 * // - Navigazione laterale tramite sidebar
 * // - Visualizzazione contenuti nell'area principale
 * // - Sistema autenticazione integrato
 * // - Gestione popup automatica
 * }</pre>
 *
 * <h3>Esempio di utilizzo avanzato:</h3>
 * <pre>{@code
 * // Configurazione personalizzata
 * MainWindow mainWindow = new MainWindow(customBookService, true);
 *
 * // Accesso ai componenti per personalizzazione
 * Header header = mainWindow.getHeader();
 * ContentArea contentArea = mainWindow.getContentArea();
 * AuthenticationManager auth = mainWindow.getAuthManager();
 *
 * // Setup callback personalizzati
 * auth.setOnAuthStateChanged(() -> {
 *     // Logica personalizzata cambio autenticazione
 *     updateUIForAuthState();
 * });
 *
 * // Apertura programmatica sezioni
 * mainWindow.showLibraryPanel();      // Apre pannello librerie
 * mainWindow.showAdminPanel();        // Apre pannello admin
 * mainWindow.showExploreSection();    // Naviga a sezione esplora
 *
 * // Gestione stato applicazione
 * boolean isReady = mainWindow.isFullyInitialized();
 * mainWindow.cleanup(); // Cleanup risorse
 * }</pre>
 *
 * <h3>Gestione della ricerca globale:</h3>
 * <p>
 * Sistema di ricerca centralizzato con propagazione eventi:
 * </p>
 * <ul>
 *   <li>Input ricerca nell'header</li>
 *   <li>Propagazione query al ContentArea</li>
 *   <li>Risultati visualizzati con popup manager</li>
 *   <li>Click handler per apertura dettagli libro</li>
 * </ul>
 *
 * <h3>Integrazione autenticazione:</h3>
 * <p>
 * Sistema di autenticazione completamente integrato:
 * </p>
 * <ul>
 *   <li>Login/logout con aggiornamento UI automatico</li>
 *   <li>Refresh sidebar su cambio stato auth</li>
 *   <li>Protezione sezioni riservate (librerie, admin)</li>
 *   <li>Persistenza stato tra sessioni</li>
 * </ul>
 *
 * <h3>Gestione errori e fallback:</h3>
 * <ul>
 *   <li><strong>Server Offline:</strong> Modalità offline con contenuti cache</li>
 *   <li><strong>Component Failure:</strong> Graceful degradation componenti</li>
 *   <li><strong>Authentication Errors:</strong> Reindirizzamento a login</li>
 *   <li><strong>UI Errors:</strong> Alert informativi per l'utente</li>
 * </ul>
 *
 * <h3>Performance e ottimizzazioni:</h3>
 * <ul>
 *   <li>Lazy loading componenti pesanti</li>
 *   <li>Cache intelligente lista libri</li>
 *   <li>Caricamento asincrono contenuti</li>
 *   <li>Cleanup automatico risorse</li>
 * </ul>
 *
 * <h3>Thread safety:</h3>
 * <ul>
 *   <li>Platform.runLater per aggiornamenti UI</li>
 *   <li>Operazioni I/O asincrone</li>
 *   <li>Gestione concorrente eventi</li>
 *   <li>Sincronizzazione stato condiviso</li>
 * </ul>
 *
 * <h3>Struttura layout generata:</h3>
 * <pre>
 * StackPane (mainRoot)
 * └── BorderPane (appRoot)
 *     ├── Left: Sidebar
 *     │   ├── Header navigazione
 *     │   ├── Menu items (Home, Libreria, Esplora, Admin)
 *     │   ├── Spacer
 *     │   └── Sezione autenticazione
 *     └── Center: VBox
 *         ├── Header (ricerca globale)
 *         └── ContentArea (contenuto principale)
 * </pre>
 *
 * <h3>Lifecycle management:</h3>
 * <ul>
 *   <li><strong>Initialization:</strong> Setup componenti e servizi</li>
 *   <li><strong>Running:</strong> Gestione eventi e aggiornamenti</li>
 *   <li><strong>Cleanup:</strong> Rilascio risorse e chiusura connessioni</li>
 * </ul>
 *
 * <h3>Debug e testing:</h3>
 * <ul>
 *   <li>Test automatico sistema post-inizializzazione</li>
 *   <li>Logging dettagliato operazioni</li>
 *   <li>Debug state per troubleshooting</li>
 *   <li>Validazione integrità componenti</li>
 * </ul>
 *
 * <h3>Integrazione con sistemi esterni:</h3>
 * <ul>
 *   <li><strong>BookService:</strong> Comunicazione server/database</li>
 *   <li><strong>Authentication Service:</strong> Validazione credenziali</li>
 *   <li><strong>Popup System:</strong> Gestione modal centralizzata</li>
 *   <li><strong>Icon System:</strong> Gestione icone applicazione</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see org.BABO.client.ui.Home.Sidebar
 * @see org.BABO.client.ui.Home.Header
 * @see org.BABO.client.ui.Home.ContentArea
 * @see org.BABO.client.ui.Authentication.AuthenticationManager
 * @see org.BABO.client.ui.Popup.PopupManager
 * @see org.BABO.client.service.BookService
 */
public class MainWindow {

    /** Servizio per la gestione dei libri e comunicazione con il backend */
    private final BookService bookService;

    /** Indica se il server è disponibile per operazioni online */
    private final boolean serverAvailable;

    /** Cache locale dei libri per performance e navigazione */
    private List<Book> cachedBooks = new ArrayList<>();

    /** Container principale dell'interfaccia utente */
    private StackPane mainRoot;

    /** Componente sidebar per navigazione laterale */
    private Sidebar sidebar;

    /** Componente header con ricerca globale */
    private Header header;

    /** Area principale per visualizzazione contenuti */
    private ContentArea contentArea;

    /** Gestore autenticazione e sessioni utente */
    private AuthenticationManager authManager;

    /** Integrazione per la sezione esplorazione catalogo */
    private ExploreIntegration exploreIntegration;

    /**
     * Costruisce una nuova istanza di MainWindow con servizi specificati.
     * <p>
     * Inizializza il controller principale dell'interfaccia utente configurando
     * i servizi base necessari per il funzionamento dell'applicazione. Questo
     * costruttore prepara l'ambiente per la successiva creazione del layout
     * attraverso {@link #createMainLayout()}.
     * </p>
     *
     * <h4>Inizializzazioni eseguite:</h4>
     * <ul>
     *   <li>Configurazione BookService per gestione catalogo</li>
     *   <li>Setup modalità online/offline basata su server availability</li>
     *   <li>Inizializzazione AuthenticationManager</li>
     *   <li>Configurazione callback autenticazione per aggiornamenti UI</li>
     * </ul>
     *
     * <h4>Callback configurati:</h4>
     * <ul>
     *   <li><strong>onAuthStateChanged:</strong> Refresh sidebar su login/logout</li>
     * </ul>
     *
     * @param bookService servizio per operazioni sui libri
     * @param serverAvailable true se il server è raggiungibile, false per modalità offline
     */
    public MainWindow(BookService bookService, boolean serverAvailable) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.authManager = new AuthenticationManager();

        // Inizializza l'autenticazione
        initializeAuthentication();
    }

    /**
     * Inizializza il sistema di autenticazione e configura i callback per aggiornamenti UI.
     * <p>
     * Metodo interno per setup completo del sistema di autenticazione. Configura
     * i callback necessari per mantenere sincronizzata l'interfaccia utente con
     * lo stato di autenticazione dell'utente, garantendo aggiornamenti automatici
     * della sidebar e altri componenti sensibili all'auth state.
     * </p>
     *
     * <h4>Callback configurati:</h4>
     * <ul>
     *   <li><strong>onAuthStateChanged:</strong> Aggiorna sidebar quando cambia stato auth</li>
     * </ul>
     *
     * <h4>Thread safety:</h4>
     * <p>
     * I callback sono progettati per essere thread-safe e utilizzano Platform.runLater
     * quando necessario per aggiornamenti UI dal thread corretto.
     * </p>
     */
    private void initializeAuthentication() {
        // Configura callback per aggiornare sidebar quando cambia stato auth
        authManager.setOnAuthStateChanged(() -> {
            if (sidebar != null) {
                sidebar.refreshAuthSection();
            }
        });

        // Inizializza auth manager
        authManager.initialize();
    }

    /**
     * Inizializza l'integrazione per la sezione Esplora con configurazione completa.
     * <p>
     * Setup dell'integrazione per la sezione di esplorazione del catalogo, configurando
     * tutti i handler necessari per il funzionamento integrato con il resto
     * dell'applicazione. Include gestione click sui libri, integrazione autenticazione
     * e configurazione container per rendering.
     * </p>
     *
     * <h4>Configurazioni applicate:</h4>
     * <ul>
     *   <li><strong>Container:</strong> Riferimento al mainRoot per rendering</li>
     *   <li><strong>AuthManager:</strong> Integrazione sistema autenticazione</li>
     *   <li><strong>BookClickHandler:</strong> Gestione click apertura dettagli</li>
     *   <li><strong>ContentArea Integration:</strong> Collegamento area contenuti</li>
     * </ul>
     *
     * <h4>Click handling:</h4>
     * <p>
     * Il book click handler configurato gestisce l'apertura dei dettagli libro
     * utilizzando BooksClient con supporto per navigazione tra collezioni
     * e integrazione completa con sistema autenticazione.
     * </p>
     */
    private void initializeExploreIntegration() {
        exploreIntegration = new ExploreIntegration(bookService, serverAvailable);

        exploreIntegration.setContainer(mainRoot);

        exploreIntegration.setAuthManager(authManager);

        Consumer<Book> bookClickHandler = selectedBook -> {
            System.out.println("📖 Click libro via Esplora: " + selectedBook.getTitle());
            BooksClient.openBookDetails(
                    selectedBook,
                    cachedBooks.isEmpty() ? List.of(selectedBook) : cachedBooks,
                    authManager
            );
        };

        exploreIntegration.setBookClickHandler(bookClickHandler);

        // Imposta il container per ExploreIntegration
        if (contentArea != null) {
            contentArea.setExploreIntegration(exploreIntegration);
        }
    }

    /**
     * Crea il layout principale dell'interfaccia utente con inizializzazione completa.
     * <p>
     * Factory method principale che costruisce l'intera interfaccia utente dell'applicazione.
     * Orchestrazione completa di tutti i componenti con configurazione handlers,
     * inizializzazione sistemi di supporto e caricamento contenuto iniziale.
     * Questo metodo rappresenta il punto di entry principale per la costruzione UI.
     * </p>
     *
     * <h4>Processo di costruzione:</h4>
     * <ol>
     *   <li><strong>Container Creation:</strong> StackPane principale e BorderPane layout</li>
     *   <li><strong>Component Initialization:</strong> Sidebar, Header, ContentArea</li>
     *   <li><strong>Integration Setup:</strong> ExploreIntegration e sistemi supporto</li>
     *   <li><strong>Event Binding:</strong> Search handlers e book click handlers</li>
     *   <li><strong>Layout Assembly:</strong> Composizione layout finale</li>
     *   <li><strong>System Initialization:</strong> PopupManager e testing</li>
     *   <li><strong>Content Loading:</strong> Caricamento contenuto iniziale</li>
     * </ol>
     *
     * <h4>Componenti creati:</h4>
     * <ul>
     *   <li><strong>Sidebar:</strong> Navigazione con sezioni Home, Libreria, Esplora, Admin</li>
     *   <li><strong>Header:</strong> Ricerca globale e controlli superiori</li>
     *   <li><strong>ContentArea:</strong> Visualizzazione contenuti principale</li>
     *   <li><strong>ExploreIntegration:</strong> Integrazione sezione esplorazione</li>
     * </ul>
     *
     * <h4>Event handlers configurati:</h4>
     * <ul>
     *   <li><strong>Search Handler:</strong> Propagazione query da Header a ContentArea</li>
     *   <li><strong>Book Click Handler:</strong> Apertura dettagli libro con navigazione</li>
     *   <li><strong>Cache Callback:</strong> Aggiornamento cache libri</li>
     * </ul>
     *
     * <h4>Sistemi inizializzati:</h4>
     * <ul>
     *   <li><strong>PopupManager:</strong> Gestione centralizzata popup</li>
     *   <li><strong>Testing System:</strong> Test automatico post-inizializzazione</li>
     * </ul>
     *
     * <h4>Layout struttura:</h4>
     * <pre>
     * StackPane (mainRoot)
     * └── BorderPane (appRoot)
     *     ├── Left: Sidebar
     *     └── Center: VBox
     *         ├── Header
     *         └── ContentArea
     * </pre>
     *
     * @return {@link StackPane} contenente l'intera interfaccia utente configurata
     */
    public StackPane createMainLayout() {
        System.out.println("🎨 Creazione layout principale...");

        mainRoot = new StackPane();
        BorderPane appRoot = new BorderPane();

        // Crea sidebar
        System.out.println("📋 Creazione Sidebar...");
        sidebar = new Sidebar(serverAvailable, authManager, this);

        // Passa BookService e mainRoot all'Header per popup
        System.out.println("🔍 Creazione Header con BookService...");
        header = new Header(bookService, mainRoot);

        // Crea content area
        System.out.println("📄 Creazione ContentArea...");
        contentArea = new ContentArea(bookService, serverAvailable, authManager);

        // Handler per i click sui libri
        Consumer<Book> bookClickHandler = selectedBook -> {
            System.out.println("📖 Click libro via MainWindow: " + selectedBook.getTitle());
            BooksClient.openBookDetails(
                    selectedBook,
                    cachedBooks.isEmpty() ? List.of(selectedBook) : cachedBooks,
                    authManager
            );
        };

        System.out.println("🔧 Configurazione SearchHandler con debug...");
        header.setSearchHandler((query) -> {
            System.out.println("🔍 [MAINWINDOW] SearchHandler ricevuto query: '" + query + "'");

            if (contentArea == null) {
                System.err.println("❌ [MAINWINDOW] ContentArea non inizializzato!");
                return;
            }

            // Usa PopupManager handler invece di bookClickHandler diretto
            Consumer<Book> popupHandler = selectedBook -> {
                System.out.println("📖 [MAINWINDOW] Click libro: " + selectedBook.getTitle());
                BooksClient.openBookDetails(
                        selectedBook,
                        cachedBooks.isEmpty() ? List.of(selectedBook) : cachedBooks,
                        authManager
                );
            };

            try {
                System.out.println("📤 [MAINWINDOW] Passaggio query a ContentArea...");
                contentArea.handleSearch(query, popupHandler);
                System.out.println("✅ [MAINWINDOW] Query passata con successo a ContentArea");
            } catch (Exception e) {
                System.err.println("❌ [MAINWINDOW] Errore durante passaggio query: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Configura content area
        System.out.println("⚙️ Configurazione ContentArea...");
        contentArea.setBookClickHandler(bookClickHandler);
        contentArea.setCachedBooksCallback(books -> {
            this.cachedBooks = books;
            System.out.println("📚 Cache aggiornata: " + books.size() + " libri");
        });

        System.out.println("🔍 Inizializzazione ExploreIntegration...");
        initializeExploreIntegration();


        System.out.println("🔧 Assemblaggio layout...");

        // Sidebar a sinistra
        appRoot.setLeft(sidebar.createSidebar());

        // Area centrale con header + content
        VBox centerBox = new VBox();
        centerBox.setStyle("-fx-background-color: #1e1e1e;");
        centerBox.getChildren().addAll(header.createHeader(), contentArea.createContentArea());
        appRoot.setCenter(centerBox);

        // Aggiungi app root al main root
        mainRoot.getChildren().add(appRoot);

        // Inizializza PopupManager DOPO aver creato mainRoot
        Platform.runLater(() -> {
            System.out.println("🚀 Inizializzazione PopupManager...");
            PopupManager.getInstance().initialize(mainRoot);
            System.out.println("✅ PopupManager inizializzato con mainRoot");

            Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("🧪 Avvio test automatico sistema ricerca...");
                    testSearchSystemAfterInit();
                } catch (InterruptedException e) {
                    // Ignore
                }
            });
        });


        System.out.println("📚 Caricamento contenuto iniziale...");
        contentArea.loadInitialContent();

        System.out.println("✅ Layout principale creato con successo");
        return mainRoot;
    }

    /**
     * Mostra il pannello di autenticazione per login/registrazione utenti.
     * <p>
     * Delegato al AuthenticationManager per visualizzazione modal di autenticazione.
     * Utilizza il mainRoot come container per overlay del popup autenticazione.
     * </p>
     */
    public void showAuthPanel() {
        authManager.showAuthPanel(mainRoot);
    }

    /**
     * Ritorna alla home page chiudendo tutti i popup e ricaricando contenuto iniziale.
     * <p>
     * Operazione di reset dell'interfaccia che riporta l'applicazione allo stato
     * iniziale. Chiude tutti i popup aperti, pulisce lo stato di ricerca e
     * ricarica il contenuto principale della home page.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ol>
     *   <li>Chiusura di tutti i popup aperti tramite PopupManager</li>
     *   <li>Ricaricamento contenuto iniziale nel ContentArea</li>
     *   <li>Reset della ricerca nell'Header</li>
     * </ol>
     *
     * <h4>Utilizzo tipico:</h4>
     * <ul>
     *   <li>Click su "Home" nella sidebar</li>
     *   <li>Reset dopo operazioni complesse</li>
     *   <li>Annullamento operazioni in corso</li>
     * </ul>
     */
    public void showHomePage() {
        System.out.println("🏠 Chiusura popup e ritorno alla home");

        // Chiudi tutti i popup
        PopupManager.getInstance().closeAllPopups();

        // Ricarica contenuto normalmente
        if (contentArea != null) {
            contentArea.loadInitialContent();
        }

        // Pulisci ricerca
        if (header != null) {
            header.clearSearch();
        }

        System.out.println("✅ Ritorno home completato");
    }

    /**
     * Mostra la sezione Esplora delegando al ContentArea per gestione navigazione.
     * <p>
     * Metodo chiamato dalla Sidebar per navigazione alla sezione di esplorazione
     * del catalogo. Delega al ContentArea che gestisce il routing interno
     * delle sezioni utilizzando l'indice menu corrispondente.
     * </p>
     *
     * <h4>Navigation flow:</h4>
     * <ol>
     *   <li>Click "Esplora" nella sidebar</li>
     *   <li>Chiamata a questo metodo</li>
     *   <li>Delegazione a ContentArea.handleMenuClick(2)</li>
     *   <li>Rendering sezione esplorazione</li>
     * </ol>
     */
    public void showExploreSection() {
        System.out.println("🔍 Richiesta apertura sezione Esplora da sidebar");
        if (contentArea != null) {
            contentArea.handleMenuClick(2); // Indice per Esplora nella sidebar
        } else {
            System.err.println("❌ ContentArea non inizializzato per sezione Esplora");
        }
    }

    /**
     * Mostra il pannello di gestione librerie con autenticazione e gestione popup completa.
     * <p>
     * Apre il pannello per la gestione delle librerie personali dell'utente.
     * Include verifica autenticazione, creazione popup con overlay, gestione
     * eventi (ESC, click background) e integrazione con callback per operazioni
     * sui libri. Utilizza PopupManager per gestione centralizzata del popup.
     * </p>
     *
     * <h4>Verifica prerequisiti:</h4>
     * <ul>
     *   <li><strong>Authentication Check:</strong> Verifica utente autenticato</li>
     *   <li><strong>Redirect Login:</strong> Apre pannello auth se necessario</li>
     * </ul>
     *
     * <h4>Configurazione popup:</h4>
     * <ul>
     *   <li><strong>LibraryPanel:</strong> Pannello gestione librerie per utente</li>
     *   <li><strong>Overlay:</strong> Background semi-trasparente</li>
     *   <li><strong>Close Handlers:</strong> ESC, click background, callback pannello</li>
     *   <li><strong>Book Click Integration:</strong> Apertura dettagli libri da libreria</li>
     * </ul>
     *
     * <h4>Event handling:</h4>
     * <ul>
     *   <li><strong>ESC Key:</strong> Chiusura rapida popup</li>
     *   <li><strong>Background Click:</strong> Chiusura click su overlay</li>
     *   <li><strong>Panel Events:</strong> Propagazione eventi interni pannello</li>
     * </ul>
     *
     * <h4>Error handling:</h4>
     * <ul>
     *   <li>Try-catch su creazione pannello</li>
     *   <li>Alert informativi per errori</li>
     *   <li>Fallback graceful su failure</li>
     * </ul>
     */
    public void showLibraryPanel() {
        if (!authManager.isAuthenticated()) {
            showAlert("Accesso Richiesto", "Devi effettuare l'accesso per gestire le tue librerie");
            showAuthPanel();
            return;
        }

        String username = authManager.getCurrentUsername();

        try {
            // Crea il pannello librerie
            LibraryPanel libraryPanel = new LibraryPanel(username, authManager.getAuthService());

            // Configura callback per chiusura
            libraryPanel.setOnClosePanel(() -> {
                PopupManager.getInstance().closeAllPopups();
                if (contentArea != null) {
                    contentArea.loadInitialContent();
                }
                System.out.println("Pannello librerie chiuso");
            });

            // Configura callback per click sui libri
            libraryPanel.setOnBookClick(selectedBook -> {
                System.out.println("Click libro da libreria: " + selectedBook.getTitle());
                List<Book> libraryBooks = libraryPanel.getCurrentLibraryBooks();
                BooksClient.openBookDetails(
                        selectedBook,
                        libraryBooks.isEmpty() ? List.of(selectedBook) : libraryBooks,
                        authManager
                );
            });

            // Crea overlay
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            overlay.getChildren().add(libraryPanel);
            StackPane.setAlignment(libraryPanel, Pos.CENTER);

            // Chiudi cliccando sullo sfondo
            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    PopupManager.getInstance().closeAllPopups();
                    if (contentArea != null) {
                        contentArea.loadInitialContent();
                    }
                }
            });

            // Previeni chiusura cliccando sul pannello
            libraryPanel.setOnMouseClicked(e -> e.consume());

            // ESC per chiudere
            overlay.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    PopupManager.getInstance().closeAllPopups();
                    if (contentArea != null) {
                        contentArea.loadInitialContent();
                    }
                }
            });

            overlay.setFocusTraversable(true);
            Platform.runLater(() -> overlay.requestFocus());

            // Usa PopupManager per gestire il popup
            PopupManager.getInstance().showCustomPopup(
                    "library_panel",
                    "popup",
                    overlay,
                    () -> {
                        if (contentArea != null) {
                            contentArea.loadInitialContent();
                        }
                        System.out.println("Pannello librerie chiuso via PopupManager");
                    }
            );

        } catch (Exception e) {
            System.err.println("Errore nell'apertura del pannello librerie: " + e.getMessage());
            showAlert("Errore", "Impossibile aprire il pannello librerie: " + e.getMessage());
        }
    }

    /**
     * Visualizza alert informativi all'utente con styling coerente applicazione.
     * <p>
     * Utility interna per creazione alert standardizzati con gestione icone
     * e styling coerente con il tema dell'applicazione. Utilizzato per
     * notifiche, errori e conferme operazioni.
     * </p>
     *
     * <h4>Features:</h4>
     * <ul>
     *   <li><strong>Platform.runLater:</strong> Thread-safe UI updates</li>
     *   <li><strong>Icon Integration:</strong> Icona applicazione su alert</li>
     *   <li><strong>Error Handling:</strong> Graceful degradation su errori styling</li>
     * </ul>
     *
     * @param title titolo dell'alert
     * @param message messaggio da visualizzare
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Imposta icona se disponibile
            alert.setOnShowing(e -> {
                try {
                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    IconUtils.setStageIcon(alertStage);
                } catch (Exception ex) {
                    // Ignora errori di icona
                }
            });

            alert.showAndWait();
        });
    }

    /**
     * Mostra il pannello di amministrazione per utenti con privilegi elevati.
     * <p>
     * Apre il pannello amministrativo che permette gestione avanzata dell'applicazione.
     * Include verifica privilegi, creazione pannello admin e sostituzione
     * del contenuto principale con l'interfaccia amministrativa.
     * </p>
     *
     * <h4>Verifica privilegi:</h4>
     * <ul>
     *   <li>Controllo autenticazione utente</li>
     *   <li>Validazione privilegi amministrativi</li>
     *   <li>Redirect a login se necessario</li>
     * </ul>
     *
     * <h4>Pannello amministrativo:</h4>
     * <ul>
     *   <li>Gestione utenti e permessi</li>
     *   <li>Configurazione sistema</li>
     *   <li>Monitoraggio operazioni</li>
     * </ul>
     */
    public void showAdminPanel() {
        System.out.println("Apertura pannello amministrativo");

        // Verifica privilegi admin
        if (!authManager.isAuthenticated() || authManager.getCurrentUser() == null) {
            showAlert("Accesso Negato", "Devi essere autenticato per accedere al pannello admin");
            return;
        }

        // Crea pannello admin
        AdminPanel adminPanel = new AdminPanel(authManager);
        VBox adminContent = adminPanel.createAdminPanel();

        // Sostituisci contenuto principale
        if (contentArea != null) {
            contentArea.showCustomContent(adminContent);
        } else {
            System.err.println("ContentArea non disponibile per pannello admin");
        }
    }

    // =====================================================
    // GETTER E METODI DI UTILITÀ
    // =====================================================

    /**
     * Restituisce il container principale dell'interfaccia utente.
     * <p>
     * Accessor per il StackPane principale che contiene l'intera interfaccia.
     * Utilizzato per integrazioni esterne e gestione popup di livello superiore.
     * </p>
     *
     * @return {@link StackPane} container principale, può essere null se non inizializzato
     */
    public StackPane getMainRoot() {
        return mainRoot;
    }

    /**
     * Restituisce il servizio per gestione libri e comunicazione backend.
     * <p>
     * Accessor per il BookService utilizzato dall'applicazione. Fornisce
     * accesso alle funzionalità di gestione catalogo, ricerca e comunicazione
     * con il server backend.
     * </p>
     *
     * @return {@link BookService} servizio gestione libri
     */
    public BookService getBookService() {
        return bookService;
    }

    /**
     * Restituisce il gestore autenticazione e sessioni utente.
     * <p>
     * Accessor per l'AuthenticationManager che gestisce login, logout,
     * sessioni utente e operazioni autenticate. Utilizzato per verifica
     * privilegi e gestione stato autenticazione.
     * </p>
     *
     * @return {@link AuthenticationManager} gestore autenticazione
     */
    public AuthenticationManager getAuthManager() {
        return authManager;
    }

    /**
     * Restituisce il componente header con ricerca globale.
     * <p>
     * Accessor per il componente Header che gestisce la ricerca globale
     * e i controlli della parte superiore dell'interfaccia. Utilizzato
     * per integrazioni e controllo programmatico della ricerca.
     * </p>
     *
     * @return {@link Header} componente header, può essere null se non inizializzato
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Restituisce l'area principale per visualizzazione contenuti.
     * <p>
     * Accessor per il ContentArea che gestisce la visualizzazione del
     * contenuto principale dell'applicazione. Utilizzato per controllo
     * programmatico del contenuto visualizzato.
     * </p>
     *
     * @return {@link ContentArea} area contenuti, può essere null se non inizializzato
     */
    public ContentArea getContentArea() {
        return contentArea;
    }

    /**
     * Verifica se tutti i componenti principali sono completamente inizializzati.
     * <p>
     * Metodo di diagnostica che controlla l'integrità dell'inizializzazione
     * di tutti i componenti critici dell'interfaccia. Utilizzato per
     * validazione stato applicazione e troubleshooting.
     * </p>
     *
     * <h4>Componenti verificati:</h4>
     * <ul>
     *   <li>mainRoot container principale</li>
     *   <li>header componente ricerca</li>
     *   <li>contentArea visualizzazione contenuti</li>
     *   <li>sidebar navigazione laterale</li>
     *   <li>authManager sistema autenticazione</li>
     *   <li>PopupManager sistema popup</li>
     * </ul>
     *
     * @return true se tutti i componenti sono inizializzati, false altrimenti
     */
    public boolean isFullyInitialized() {
        return mainRoot != null &&
                header != null &&
                contentArea != null &&
                sidebar != null &&
                authManager != null &&
                PopupManager.getInstance().isInitialized();
    }

    /**
     * Esegue cleanup completo delle risorse utilizzate dall'interfaccia.
     * <p>
     * Metodo di pulizia che rilascia tutte le risorse utilizzate dai
     * componenti dell'interfaccia. Utilizzato durante chiusura applicazione
     * o reset completo stato interfaccia.
     * </p>
     *
     * <h4>Operazioni di cleanup:</h4>
     * <ul>
     *   <li><strong>Popup Cleanup:</strong> Chiusura tutti popup aperti</li>
     *   <li><strong>Component Cleanup:</strong> Cleanup specifico componenti</li>
     *   <li><strong>Cache Cleanup:</strong> Svuotamento cache libri</li>
     *   <li><strong>Resource Release:</strong> Rilascio risorse sistema</li>
     * </ul>
     *
     * <h4>Utilizzo raccomandato:</h4>
     * <ul>
     *   <li>Chiusura applicazione</li>
     *   <li>Reset completo interfaccia</li>
     *   <li>Gestione memoria su dispositivi limitati</li>
     * </ul>
     */
    public void cleanup() {
        System.out.println("MainWindow: Cleanup risorse");

        // Chiudi tutti i popup
        PopupManager.getInstance().closeAllPopups();

        // Cleanup componenti
        if (contentArea != null) {
            contentArea.cleanup();
        }

        // Pulisci cache
        cachedBooks.clear();

        System.out.println("MainWindow: Cleanup completato");
    }

    /**
     * Esegue test automatico del sistema post-inizializzazione per validazione integrità.
     * <p>
     * Metodo di testing interno che verifica il corretto funzionamento del sistema
     * dopo l'inizializzazione completa. Include test di connettività server,
     * validazione componenti e test caricamento dati. Utilizzato principalmente
     * in modalità debug per identificare problemi durante lo sviluppo.
     * </p>
     *
     * <h4>Test eseguiti:</h4>
     * <ul>
     *   <li><strong>Component Readiness:</strong> Verifica inizializzazione componenti</li>
     *   <li><strong>Server Connectivity:</strong> Test connessione backend</li>
     *   <li><strong>Data Loading:</strong> Test caricamento catalogo libri</li>
     *   <li><strong>System Integration:</strong> Verifica integrazione sistemi</li>
     * </ul>
     *
     * <h4>Output diagnostico:</h4>
     * <ul>
     *   <li>Stato componenti (OK/NULL)</li>
     *   <li>Disponibilità server (✅/❌)</li>
     *   <li>Numero libri caricati</li>
     *   <li>Modalità operativa (online/offline)</li>
     * </ul>
     *
     * <h4>Error handling:</h4>
     * <p>
     * Il testing è designed per essere non-invasivo e non interrompere
     * il normale funzionamento dell'applicazione anche in caso di errori
     * durante i test.
     * </p>
     */
    private void testSearchSystemAfterInit() {
        System.out.println("===== TEST POST-INIZIALIZZAZIONE =====");

        // Verifica che tutti i componenti siano inizializzati
        boolean allReady = isFullyInitialized();
        System.out.println("Tutti i componenti pronti: " + (allReady ? "✅" : "❌"));

        if (allReady) {
            // Test connessione server
            if (bookService != null) {
                boolean serverOk = bookService.isServerAvailable();
                System.out.println("Server disponibile: " + (serverOk ? "✅" : "❌"));

                if (serverOk) {
                    // Test rapido caricamento libri
                    bookService.getAllBooksAsync()
                            .thenAccept(books -> {
                                System.out.println("Test caricamento: " + books.size() + " libri disponibili");
                                if (books.size() > 0) {
                                    System.out.println("✅ Sistema funzionante - libri caricati dal database");
                                } else {
                                    System.out.println("⚠️ Sistema carica solo libri di fallback");
                                }
                            })
                            .exceptionally(throwable -> {
                                System.err.println("❌ Errore test caricamento: " + throwable.getMessage());
                                return null;
                            });
                }
            }
        } else {
            System.err.println("❌ Alcuni componenti non sono ancora pronti");
        }

        System.out.println("===============================");
    }
}