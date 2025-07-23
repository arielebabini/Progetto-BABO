package org.BABO.client.ui;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import org.BABO.client.service.LibraryService;
import org.BABO.client.ui.AppleBooksClient;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.input.KeyCode;

/**
 * Gestisce la finestra principale dell'applicazione
 * Coordina tutti i componenti UI principali
 * AGGIORNATO: Con integrazione PopupManager completa e sezione Esplora
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

        // ✅ HANDLER PER I CLICK SUI LIBRI - USA POPUP MANAGER
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

    public StackPane createMainLayout() {
        mainRoot = new StackPane();
        BorderPane appRoot = new BorderPane();

        // Crea i componenti principali
        sidebar = new Sidebar(serverAvailable, authManager, this);
        header = new Header();
        contentArea = new ContentArea(bookService, serverAvailable, authManager);

        // ✅ HANDLER PER I CLICK SUI LIBRI - USA POPUP MANAGER
        Consumer<Book> bookClickHandler = selectedBook -> {
            System.out.println("📖 Click libro via MainWindow: " + selectedBook.getTitle());
            AppleBooksClient.openBookDetails(
                    selectedBook,
                    cachedBooks.isEmpty() ? List.of(selectedBook) : cachedBooks,
                    authManager
            );
        };

        // Configura la ricerca
        header.setSearchHandler((query) -> {
            contentArea.handleSearch(query, bookClickHandler);
        });

        // Configura il content area
        contentArea.setBookClickHandler(bookClickHandler);
        contentArea.setCachedBooksCallback(books -> this.cachedBooks = books);

        // ✅ INIZIALIZZA INTEGRAZIONE ESPLORA
        initializeExploreIntegration();

        // Assembla il layout
        appRoot.setLeft(sidebar.createSidebar());
        VBox centerBox = new VBox();
        centerBox.setStyle("-fx-background-color: #1e1e1e;");
        centerBox.getChildren().addAll(header.createHeader(), contentArea.createContentArea());
        appRoot.setCenter(centerBox);

        // Carica il contenuto DOPO aver creato l'area contenuti
        contentArea.loadInitialContent();

        mainRoot.getChildren().add(appRoot);
        return mainRoot;
    }

    public void showAuthPanel() {
        authManager.showAuthPanel(mainRoot);
    }

    public void showHomePage() {
        System.out.println("🏠 Chiusura popup e ritorno alla home");

        // ✅ USA POPUP MANAGER per chiudere i popup
        PopupManager.getInstance().closeAllPopups();

        // ✅ IMPORTANTE: Forza il ritorno alla vista home nel ContentArea
        if (contentArea != null) {
            contentArea.forceHomeView();
        }

        // Pulisci il campo di ricerca
        if (header != null) {
            header.clearSearch();
        }

        // ✅ AGGIORNATO: Aggiorna la sidebar per evidenziare Home
        if (sidebar != null) {
            sidebar.setHomeActive();
        }

        System.out.println("🏠 Tornato alla home page");
    }

    /**
     * ✅ NUOVO: Mostra la sezione Esplora (chiamato dalla Sidebar)
     */
    public void showExploreSection() {
        System.out.println("🔍 Richiesta apertura sezione Esplora da sidebar");
        if (contentArea != null) {
            contentArea.handleMenuClick(2); // Indice per Esplora nella sidebar
        } else {
            System.err.println("❌ ContentArea non inizializzato per sezione Esplora");
        }
    }

    public void showAdvancedSearch() {
        AdvancedSearchPanel searchPanel = new AdvancedSearchPanel(bookService);

        // Configura il callback per i risultati di ricerca
        searchPanel.setOnSearchExecuted(searchResult -> {
            // ✅ USA POPUP MANAGER per chiudere
            PopupManager.getInstance().closeAllPopups();

            // Mostra i risultati nell'area contenuti
            contentArea.showSearchResults(searchResult);
        });

        // Crea overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.getChildren().add(searchPanel);
        StackPane.setAlignment(searchPanel, javafx.geometry.Pos.CENTER);

        // ✅ CHIUDI tramite PopupManager cliccando sullo sfondo
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                PopupManager.getInstance().closeAllPopups();
            }
        });

        // Previeni chiusura cliccando sul pannello
        searchPanel.setOnMouseClicked(e -> e.consume());

        mainRoot.getChildren().add(overlay);
        System.out.println("🔍 Pannello ricerca avanzata aperto");
    }

    /**
     * METODO AGGIORNATO: showLibraryPanel con gestione corretta PopupManager
     * Sostituisce il metodo esistente in MainWindow.java
     */
    public void showLibraryPanel() {
        if (!authManager.isAuthenticated()) {
            showAlert("🔒 Accesso Richiesto", "Devi effettuare l'accesso per gestire le tue librerie");
            showAuthPanel();
            return;
        }

        String username = authManager.getCurrentUsername();

        // CORRETTO: Ottieni AuthService dall'AuthenticationManager
        LibraryPanel libraryPanel = new LibraryPanel(username, authManager.getAuthService());

        // CORRETTO: Usa setOnClosePanel invece di setOnClose
        libraryPanel.setOnClosePanel(() -> {
            PopupManager.getInstance().closeAllPopups();
            contentArea.loadInitialContent();
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

        // Crea overlay manualmente
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.getChildren().add(libraryPanel);
        StackPane.setAlignment(libraryPanel, javafx.geometry.Pos.CENTER);

        // Chiudi cliccando sullo sfondo
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                PopupManager.getInstance().closeAllPopups();
                contentArea.loadInitialContent();
            }
        });

        // Previeni chiusura cliccando sul pannello
        libraryPanel.setOnMouseClicked(e -> e.consume());

        // CORRETTO: Usa showCustomPopup con 4 parametri
        PopupManager.getInstance().showCustomPopup(
                "📚 Le Tue Librerie",                    // title
                "Gestisci la tua collezione personale",  // subtitle
                overlay,                                  // content
                () -> {                                   // onClose callback
                    contentArea.loadInitialContent();
                    System.out.println("🚪 Pannello librerie chiuso");
                }
        );
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
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");

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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // AGGIUNTO: Imposta icona per l'alert
        alert.setOnShowing(e -> {
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            IconUtils.setStageIcon(alertStage);
        });

        alert.showAndWait();
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
}