package org.BABO.client.ui.Home;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.BABO.client.ui.*;
import org.BABO.client.ui.Admin.AdminPanel;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Library.LibraryPanel;
import org.BABO.client.ui.Search.AdvancedSearchPanel;
import org.BABO.client.ui.Popup.PopupManager;
import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import org.BABO.client.service.LibraryService;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Gestisce la finestra principale dell'applicazione
 * Coordina tutti i componenti UI principali
 */
public class MainWindow {

    private final BookService bookService;
    private final LibraryService libraryService;
    private final boolean serverAvailable;
    private List<Book> cachedBooks = new ArrayList<>();
    private StackPane mainRoot;
    private Sidebar sidebar;
    private Header header;
    private ContentArea contentArea;
    private AuthenticationManager authManager;
    private ExploreIntegration exploreIntegration;
    private boolean homeContentLoaded = false;

    public MainWindow(BookService bookService, boolean serverAvailable) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.libraryService = new LibraryService();
        this.authManager = new AuthenticationManager();

        // Inizializza l'autenticazione
        initializeAuthentication();
    }

    /**
     * Inizializza il sistema di autenticazione e configura i callback
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
     * Inizializza l'integrazione per la sezione Esplora
     */
    private void initializeExploreIntegration() {
        exploreIntegration = new ExploreIntegration(bookService, serverAvailable);

        exploreIntegration.setContainer(mainRoot);

        Consumer<Book> bookClickHandler = selectedBook -> {
            System.out.println("📖 Click libro via Esplora: " + selectedBook.getTitle());
            AppleBooksClient.openBookDetails(
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
     * Carica la home page in modo centralizzato e controllato
     */
    public void loadHomeContent() {
        System.out.println("🏠 MainWindow: Richiesta caricamento home (già caricata: " + homeContentLoaded + ")");

        // Carica sempre la home, ma il ContentArea decide se rigenerare o meno
        if (contentArea != null) {
            contentArea.loadInitialContentOnce();
        }

        // Aggiorna flag
        homeContentLoaded = true;
        System.out.println("✅ MainWindow: Home caricata, flag impostato");
    }

    /**
     * Forza il ricaricamento completo della home
     */
    public void forceReloadHome() {
        System.out.println("🔄 MainWindow: Forzatura ricaricamento home");
        homeContentLoaded = false;
        loadHomeContent();
    }

    /**
     * Pulisce lo stato quando si cambia sezione
     */
    public void clearHomeState() {
        System.out.println("🧹 MainWindow: Reset stato home");
        homeContentLoaded = false;
    }

    public StackPane createMainLayout() {
        System.out.println("🎨 Creazione layout principale...");

        mainRoot = new StackPane();
        BorderPane appRoot = new BorderPane();

        // ===== CREAZIONE COMPONENTI PRINCIPALI =====

        // Crea sidebar
        System.out.println("📋 Creazione Sidebar...");
        sidebar = new Sidebar(serverAvailable, authManager, this);

        // Passa BookService e mainRoot all'Header per popup
        System.out.println("🔍 Creazione Header con BookService...");
        header = new Header(bookService, mainRoot);

        // Crea content area
        System.out.println("📄 Creazione ContentArea...");
        contentArea = new ContentArea(bookService, serverAvailable, authManager);

        // ===== CONFIGURAZIONE HANDLERS =====

        // Handler per i click sui libri
        Consumer<Book> bookClickHandler = selectedBook -> {
            System.out.println("📖 Click libro via MainWindow: " + selectedBook.getTitle());
            AppleBooksClient.openBookDetails(
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
                AppleBooksClient.openBookDetails(
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

        // ===== ASSEMBLAGGIO LAYOUT =====

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

        // ===== INIZIALIZZAZIONE POPUP MANAGER =====

        // Inizializza PopupManager DOPO aver creato mainRoot
        Platform.runLater(() -> {
            System.out.println("🚀 Inizializzazione PopupManager...");
            PopupManager.getInstance().initialize(mainRoot);
            System.out.println("✅ PopupManager inizializzato con mainRoot");

            addDebugKeyBindings();

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

        // ===== CARICAMENTO CONTENUTO INIZIALE =====

        System.out.println("📚 Caricamento contenuto iniziale...");
        contentArea.loadInitialContent();

        System.out.println("✅ Layout principale creato con successo");
        return mainRoot;
    }

    public void showAuthPanel() {
        authManager.showAuthPanel(mainRoot);
    }

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
     * Mostra la sezione Esplora (chiamato dalla Sidebar)
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
     * Metodo ricerca avanzata ora delega all'Header
     */
    public void showAdvancedSearch() {
        System.out.println("🔍 Richiesta ricerca avanzata - delegata all'Header");

        if (header != null && header.isFullyInitialized()) {
            // L'Header gestisce già l'apertura della ricerca avanzata tramite il pulsante ⚙️
            System.out.println("💡 La ricerca avanzata è disponibile tramite il pulsante ⚙️ nell'header");
        } else {
            System.err.println("❌ Header non inizializzato correttamente");

            showAdvancedSearchFallback();
        }
    }

    /**
     * Metodo fallback per ricerca avanzata
     */
    private void showAdvancedSearchFallback() {
        System.out.println("🔄 Uso fallback per ricerca avanzata");

        AdvancedSearchPanel searchPanel = new AdvancedSearchPanel(bookService);

        // Configura il callback per i risultati di ricerca
        searchPanel.setOnSearchExecuted(searchResult -> {
            PopupManager.getInstance().closeAllPopups();

            // Mostra i risultati nell'area contenuti
            if (contentArea != null) {
                contentArea.showAdvancedSearchResults(searchResult);
            }
        });

        // Crea overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.getChildren().add(searchPanel);
        StackPane.setAlignment(searchPanel, Pos.CENTER);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                PopupManager.getInstance().closeAllPopups();
            }
        });

        // Previeni chiusura cliccando sul pannello
        searchPanel.setOnMouseClicked(e -> e.consume());

        // ESC per chiudere
        overlay.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                PopupManager.getInstance().closeAllPopups();
            }
        });

        overlay.setFocusTraversable(true);
        Platform.runLater(() -> overlay.requestFocus());

        mainRoot.getChildren().add(overlay);
        System.out.println("🔍 Pannello ricerca avanzata fallback aperto");
    }

    /**
     * showLibraryPanel con gestione corretta PopupManager
     */
    public void showLibraryPanel() {
        if (!authManager.isAuthenticated()) {
            showAlert("🔒 Accesso Richiesto", "Devi effettuare l'accesso per gestire le tue librerie");
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
                System.out.println("🚪 Pannello librerie chiuso");
            });

            // Configura callback per click sui libri
            libraryPanel.setOnBookClick(selectedBook -> {
                System.out.println("📖 Click libro da libreria: " + selectedBook.getTitle());
                AppleBooksClient.openBookDetails(
                        selectedBook,
                        cachedBooks.isEmpty() ? List.of(selectedBook) : cachedBooks,
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
                        System.out.println("🚪 Pannello librerie chiuso via PopupManager");
                    }
            );

        } catch (Exception e) {
            System.err.println("❌ Errore nell'apertura del pannello librerie: " + e.getMessage());
            showAlert("❌ Errore", "Impossibile aprire il pannello librerie: " + e.getMessage());
        }
    }

    public void showBookReader(Book book) {
        if (book == null) {
            showAlert("❌ Errore", "Impossibile aprire il libro: libro non valido");
            return;
        }

        System.out.println("📖 Apertura reader per: " + book.getTitle());

        // Simulazione semplice del reader se la classe BookReader non esiste
        Alert readerAlert = new Alert(Alert.AlertType.INFORMATION);
        readerAlert.setTitle("📖 Book Reader");
        readerAlert.setHeaderText("Lettura: " + book.getTitle());
        readerAlert.setContentText("Reader non ancora implementato.\n" +
                "Qui si aprirebbe il lettore per:\n" +
                "Titolo: " + book.getTitle() + "\n" +
                "Autore: " + book.getAuthor());

        // Styling dell'alert
        DialogPane dialogPane = readerAlert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2c2c2e;");

        // Applica stile ai label se esistono
        try {
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
            dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
        } catch (Exception e) {
            // Ignora errori di styling
        }

        // Imposta icona se disponibile
        readerAlert.setOnShowing(e -> {
            try {
                Stage alertStage = (Stage) readerAlert.getDialogPane().getScene().getWindow();
                IconUtils.setStageIcon(alertStage);
            } catch (Exception ex) {
                // Ignora errori di icona
            }
        });

        readerAlert.showAndWait();
    }

    /**
     * ✅ AGGIORNATO: Apre dettagli libro con navigazione
     */
    public void showBookDetails(Book book) {
        if (book == null) {
            System.err.println("❌ Tentativo di aprire dettagli per libro null");
            return;
        }

        System.out.println("📖 Apertura dettagli libro: " + book.getTitle());

        // ✅ USA POPUP MANAGER
        PopupManager.getInstance().showBookDetails(
                book,
                cachedBooks.isEmpty() ? List.of(book) : cachedBooks,
                authManager
        );
    }

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
     * ✅ NUOVO: Debug completo dello stato dell'applicazione
     */
    public void debugFullState() {
        System.out.println("🔍 ===== MAINWINDOW FULL DEBUG =====");
        System.out.println("MainRoot: " + (mainRoot != null ? "✅ OK" : "❌ NULL"));
        System.out.println("BookService: " + (bookService != null ? "✅ OK" : "❌ NULL"));
        System.out.println("AuthManager: " + (authManager != null ? "✅ OK" : "❌ NULL"));
        System.out.println("ServerAvailable: " + serverAvailable);
        System.out.println("CachedBooks: " + cachedBooks.size());

        if (header != null) {
            System.out.println("📍 HEADER STATE:");
            header.debugState();
        } else {
            System.out.println("📍 HEADER: ❌ NULL");
        }

        if (contentArea != null) {
            System.out.println("📍 CONTENT AREA STATE:");
            contentArea.debugCacheState();
        } else {
            System.out.println("📍 CONTENT AREA: ❌ NULL");
        }

        PopupManager.getInstance().debugFullState();
        System.out.println("===================================");
    }

    /**
     * ✅ NUOVO: Verifica se la ricerca avanzata è aperta
     */
    public boolean isAdvancedSearchOpen() {
        return header != null && header.isAdvancedSearchOpen();
    }

    /**
     * ✅ NUOVO: Chiude forzatamente la ricerca avanzata
     */
    public void closeAdvancedSearch() {
        if (header != null) {
            header.closeAdvancedSearch();
        }

        // Fallback: rimuovi overlay se presente
        try {
            mainRoot.getChildren().removeIf(node -> {
                if (node instanceof StackPane) {
                    StackPane stackPane = (StackPane) node;
                    return stackPane.getChildren().stream()
                            .anyMatch(child -> child instanceof AdvancedSearchPanel);
                }
                return false;
            });
        } catch (Exception e) {
            System.err.println("⚠️ Errore nella chiusura forzata ricerca avanzata: " + e.getMessage());
        }
    }

    // =====================================================
    // GETTER E METODI DI UTILITÀ
    // =====================================================

    public StackPane getMainRoot() {
        return mainRoot;
    }

    public BookService getBookService() {
        return bookService;
    }

    public LibraryService getLibraryService() {
        return libraryService;
    }

    public AuthenticationManager getAuthManager() {
        return authManager;
    }

    /**
     * ✅ NUOVO: Getter per ExploreIntegration
     */
    public ExploreIntegration getExploreIntegration() {
        return exploreIntegration;
    }

    public boolean isServerAvailable() {
        return serverAvailable;
    }

    public List<Book> getCachedBooks() {
        return new ArrayList<>(cachedBooks);
    }

    public void setCachedBooks(List<Book> books) {
        this.cachedBooks = new ArrayList<>(books);
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public Header getHeader() {
        return header;
    }

    public ContentArea getContentArea() {
        return contentArea;
    }

    /**
     * ✅ AGGIUNTO: Metodo per refresh completo dell'interfaccia
     */
    public void refreshInterface() {
        System.out.println("🔄 Refresh completo interfaccia");

        // Chiudi tutti i popup
        PopupManager.getInstance().closeAllPopups();

        // Chiudi ricerca avanzata
        closeAdvancedSearch();

        // Refresh sidebar se l'autenticazione è cambiata
        if (sidebar != null) {
            sidebar.refreshAuthSection();
        }

        // Ricarica contenuto
        if (contentArea != null) {
            contentArea.loadInitialContent();
        }

        // Pulisci ricerca
        if (header != null) {
            header.clearSearch();
        }

        System.out.println("✅ Refresh interfaccia completato");
    }

    /**
     * ✅ AGGIUNTO: Metodo per controllo stato popup
     */
    public boolean hasActivePopups() {
        return PopupManager.getInstance().hasActivePopups();
    }

    /**
     * ✅ AGGIUNTO: Metodo per contare popup attivi
     */
    public int getActivePopupsCount() {
        return PopupManager.getInstance().getActivePopupsCount();
    }

    /**
     * ✅ NUOVO: Verifica se tutti i componenti sono inizializzati
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
     * ✅ NUOVO: Cleanup delle risorse
     */
    public void cleanup() {
        System.out.println("🧹 MainWindow: Cleanup risorse");

        // Chiudi tutti i popup
        PopupManager.getInstance().closeAllPopups();

        // Cleanup componenti
        if (contentArea != null) {
            contentArea.cleanup();
        }

        // Pulisci cache
        cachedBooks.clear();

        System.out.println("✅ MainWindow: Cleanup completato");
    }

    /**
     * ✅ NUOVO: Test completo sistema di ricerca
     */
    public void debugSearchSystem() {
        System.out.println("🔧 ===== DEBUG SISTEMA RICERCA =====");

        // Test stato componenti
        System.out.println("Componenti inizializzati:");
        System.out.println("  BookService: " + (bookService != null ? "✅" : "❌"));
        System.out.println("  Header: " + (header != null ? "✅" : "❌"));
        System.out.println("  ContentArea: " + (contentArea != null ? "✅" : "❌"));
        System.out.println("  Server Available: " + serverAvailable);

        // Test BookService
        if (bookService != null) {
            System.out.println("\n🔧 Test BookService...");
            boolean isAvailable = bookService.isServerAvailable();
            System.out.println("Server raggiungibile: " + (isAvailable ? "✅" : "❌"));

            if (isAvailable) {
                // Test chiamata diretta
                System.out.println("🔧 Test chiamata diretta al BookService...");
                bookService.getAllBooksAsync()
                        .thenAccept(books -> {
                            System.out.println("📚 Libri caricati dal server: " + books.size());
                            if (!books.isEmpty()) {
                                System.out.println("📖 Primo libro: " + books.get(0).getTitle());
                            }
                        })
                        .exceptionally(throwable -> {
                            System.err.println("❌ Errore caricamento libri: " + throwable.getMessage());
                            return null;
                        });

                // Test ricerca diretta
                System.out.println("🔧 Test ricerca diretta...");
                bookService.searchBooksAsync("test")
                        .thenAccept(results -> {
                            System.out.println("🔍 Risultati ricerca diretta: " + results.size());
                        })
                        .exceptionally(throwable -> {
                            System.err.println("❌ Errore ricerca diretta: " + throwable.getMessage());
                            return null;
                        });
            }
        }

        // Test Header
        if (header != null) {
            System.out.println("\n🔧 Test Header...");
            header.debugState();

            // Test ricerca tramite header
            System.out.println("🔧 Test ricerca tramite Header...");
            header.testQuickSearch("debug");
        }

        System.out.println("🔧 ===============================");
    }

    /**
     * ✅ MIGLIORATO: Configura searchHandler con debug dettagliato
     */
    private void configureSearchHandler() {
        header.setSearchHandler((query) -> {
            System.out.println("🔍 [MAINWINDOW] SearchHandler ricevuto query: '" + query + "'");

            if (contentArea == null) {
                System.err.println("❌ [MAINWINDOW] ContentArea non inizializzato!");
                return;
            }

            // Usa PopupManager handler invece di bookClickHandler
            Consumer<Book> popupHandler = selectedBook -> {
                System.out.println("📖 [MAINWINDOW] Click libro: " + selectedBook.getTitle());
                AppleBooksClient.openBookDetails(
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

        System.out.println("✅ [MAINWINDOW] SearchHandler configurato con debug");
    }

    /**
     * ✅ NUOVO: Aggiunge binding per test rapidi (solo per debug)
     */
    public void addDebugKeyBindings() {
        if (mainRoot != null) {
            mainRoot.setOnKeyPressed(event -> {
                // Ctrl+F1 = debug sistema ricerca
                if (event.isControlDown() && event.getCode() == KeyCode.F1) {
                    debugSearchSystem();
                    event.consume();
                }
                // Ctrl+F2 = test ricerca "test"
                else if (event.isControlDown() && event.getCode() == KeyCode.F2) {
                    if (header != null) {
                        header.testQuickSearch("test");
                    }
                    event.consume();
                }
                // Ctrl+F3 = test ricerca "eco"
                else if (event.isControlDown() && event.getCode() == KeyCode.F3) {
                    if (header != null) {
                        header.testQuickSearch("eco");
                    }
                    event.consume();
                }
            });

            System.out.println("🔧 Debug key bindings aggiunti:");
            System.out.println("  Ctrl+F1 = Debug sistema ricerca");
            System.out.println("  Ctrl+F2 = Test ricerca 'test'");
            System.out.println("  Ctrl+F3 = Test ricerca 'eco'");
        }
    }

    private void testSearchSystemAfterInit() {
        System.out.println("🧪 ===== TEST POST-INIZIALIZZAZIONE =====");

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
                                System.out.println("📚 Test caricamento: " + books.size() + " libri disponibili");
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

        System.out.println("🧪 ===============================");
    }

    public void showAdminPanel() {
        System.out.println("⚙️ Apertura pannello amministrativo");

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
            System.err.println("❌ ContentArea non disponibile per pannello admin");
        }
    }
}