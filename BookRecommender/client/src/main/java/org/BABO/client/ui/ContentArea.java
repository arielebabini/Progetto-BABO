package org.BABO.client.ui;

import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * ContentArea corretta per utilizzare PopupManager
 * Risolve i problemi di popup annidati e riferimenti persi
 */
public class ContentArea {

    private final BookService bookService;
    private final boolean serverAvailable;
    private AuthenticationManager authManager;
    private VBox content;
    private Consumer<Book> bookClickHandler;
    private BookSectionFactory sectionFactory;
    private Consumer<List<Book>> cachedBooksCallback;
    private ExploreIntegration exploreIntegration; // ✅ AGGIUNTA VARIABILE MANCANTE

    // Cache per navigazione contestuale
    private List<Book> featuredBooks = new ArrayList<>();
    private List<Book> freeBooks = new ArrayList<>();
    private List<Book> newBooks = new ArrayList<>();
    private List<Book> searchResults = new ArrayList<>();
    private List<Book> advancedSearchResults = new ArrayList<>();

    public ContentArea(BookService bookService, boolean serverAvailable, AuthenticationManager authManager) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.authManager = authManager;
        this.sectionFactory = new BookSectionFactory(bookService, serverAvailable);

        setupContextualNavigation();
    }

    public ContentArea(BookService bookService, boolean serverAvailable) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.sectionFactory = new BookSectionFactory(bookService, serverAvailable);

        setupContextualNavigation();
    }

    public void setAuthManager(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Configura navigazione contestuale con PopupManager
     */
    private void setupContextualNavigation() {
        // Configura callback per salvare libri per sezione
        this.sectionFactory.setFeaturedBooksCallback(books -> {
            this.featuredBooks = new ArrayList<>(books); // Copia difensiva
            System.out.println("📚 Featured books salvati per navigazione: " + books.size());
        });

        this.sectionFactory.setFreeBooksCallback(books -> {
            this.freeBooks = new ArrayList<>(books);
            System.out.println("🆓 Free books salvati per navigazione: " + books.size());
        });

        this.sectionFactory.setNewBooksCallback(books -> {
            this.newBooks = new ArrayList<>(books);
            System.out.println("✨ New books salvati per navigazione: " + books.size());
        });

        this.sectionFactory.setSearchResultsCallback(books -> {
            this.searchResults = new ArrayList<>(books);
            System.out.println("🔍 Search results salvati per navigazione: " + books.size());
        });
    }

    public ScrollPane createContentArea() {
        content = new VBox(20);
        content.setId("content");
        content.setPadding(new Insets(15, 20, 30, 20));
        content.setStyle("-fx-background-color: #1e1e1e;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        return scrollPane;
    }

    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;

        // Usa PopupManager handler invece di gestione diretta
        Consumer<Book> popupManagerHandler = book -> handleBookClickWithPopupManager(book);
        this.sectionFactory.setBookClickHandler(popupManagerHandler);
    }

    /**
     * Gestisce click usando PopupManager correttamente
     */
    private void handleBookClickWithPopupManager(Book book) {
        if (book == null) {
            System.err.println("❌ ContentArea: Libro null nel click handler");
            return;
        }

        System.out.println("📖 ContentArea: Click su '" + book.getTitle() + "'");

        try {
            // Determina contesto di navigazione
            List<Book> navigationBooks = determineNavigationContext(book);
            System.out.println("📚 Navigazione tra " + navigationBooks.size() + " libri");

            // Usa PopupManager
            PopupManager popupManager = PopupManager.getInstance();

            if (!popupManager.isInitialized()) {
                System.err.println("❌ PopupManager non inizializzato! Inizializzazione di emergenza...");

                // Tentativo di inizializzazione di emergenza
                StackPane mainRoot = findMainRootContainer();
                if (mainRoot != null) {
                    popupManager.initialize(mainRoot);
                } else {
                    System.err.println("❌ Impossibile trovare container principale!");
                    showErrorDialog("Errore nell'apertura dei dettagli del libro");
                    return;
                }
            }

            // Usa PopupManager per aprire il popup
            popupManager.showBookDetails(book, navigationBooks, authManager);

        } catch (Exception e) {
            System.err.println("❌ ContentArea: Errore nel click handler: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Errore nell'apertura dei dettagli del libro: " + e.getMessage());
        }
    }

    /**
     * Determina il contesto di navigazione per un libro
     */
    private List<Book> determineNavigationContext(Book book) {
        if (book == null) {
            return new ArrayList<>();
        }

        // Controlla in quale sezione si trova il libro
        if (containsBook(featuredBooks, book)) {
            System.out.println("📚 Libro trovato in Featured Books");
            return new ArrayList<>(featuredBooks);
        }

        if (containsBook(freeBooks, book)) {
            System.out.println("🆓 Libro trovato in Free Books");
            return new ArrayList<>(freeBooks);
        }

        if (containsBook(newBooks, book)) {
            System.out.println("✨ Libro trovato in New Books");
            return new ArrayList<>(newBooks);
        }

        if (containsBook(searchResults, book)) {
            System.out.println("🔍 Libro trovato in Search Results");
            return new ArrayList<>(searchResults);
        }

        if (containsBook(advancedSearchResults, book)) {
            System.out.println("🎯 Libro trovato in Advanced Search Results");
            return new ArrayList<>(advancedSearchResults);
        }

        // Fallback: libro singolo
        System.out.println("📖 Libro non trovato in cache - navigazione singola");
        return List.of(book);
    }

    /**
     * Verifica se un libro è contenuto in una lista
     */
    private boolean containsBook(List<Book> books, Book targetBook) {
        if (books == null || targetBook == null) {
            return false;
        }

        return books.stream().anyMatch(book -> booksEqual(book, targetBook));
    }

    /**
     * Confronta due libri per uguaglianza
     */
    private boolean booksEqual(Book book1, Book book2) {
        if (book1 == book2) return true;
        if (book1 == null || book2 == null) return false;

        // Confronta per ISBN se disponibile
        if (book1.getIsbn() != null && book2.getIsbn() != null) {
            return book1.getIsbn().equals(book2.getIsbn());
        }

        // Fallback: confronta titolo e autore
        return Objects.equals(book1.getTitle(), book2.getTitle()) &&
                Objects.equals(book1.getAuthor(), book2.getAuthor());
    }

    /**
     * Trova il container principale per PopupManager
     */
    private StackPane findMainRootContainer() {
        try {
            if (content != null && content.getScene() != null) {
                javafx.scene.Node root = content.getScene().getRoot();

                if (root instanceof StackPane) {
                    System.out.println("✅ Container principale trovato tramite content");
                    return (StackPane) root;
                }
            }

            // Metodo alternativo: cerca nelle finestre aperte
            javafx.collections.ObservableList<javafx.stage.Window> windows =
                    javafx.stage.Stage.getWindows();

            for (javafx.stage.Window window : windows) {
                if (window instanceof javafx.stage.Stage) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) window;

                    // Trova l'app principale (non popup)
                    if (ApplicationProtection.isMainApplicationStage(stage)) {
                        javafx.scene.Scene scene = stage.getScene();
                        if (scene != null && scene.getRoot() instanceof StackPane) {
                            System.out.println("✅ Container principale trovato tramite finestra principale");
                            return (StackPane) scene.getRoot();
                        }
                    }
                }
            }

            System.err.println("⚠️ Container principale non trovato");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca del container principale: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mostra dialog di errore
     */
    private void showErrorDialog(String message) {
        Platform.runLater(() -> {
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText(null);
                alert.setContentText(message);

                // Stile per tema scuro
                javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");

                alert.showAndWait();

            } catch (Exception e) {
                System.err.println("❌ Errore anche nel mostrare dialog di errore: " + e.getMessage());
            }
        });
    }

    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        this.cachedBooksCallback = callback;

        // Callback che combina tutte le sezioni
        Consumer<List<Book>> combinedCallback = books -> {
            List<Book> allBooks = new ArrayList<>();
            allBooks.addAll(featuredBooks);
            allBooks.addAll(freeBooks);
            allBooks.addAll(newBooks);
            allBooks.addAll(searchResults);
            allBooks.addAll(advancedSearchResults);

            // Rimuovi duplicati
            List<Book> uniqueBooks = allBooks.stream()
                    .distinct()
                    .toList();

            if (callback != null) {
                callback.accept(uniqueBooks);
            }
        };

        this.sectionFactory.setCachedBooksCallback(combinedCallback);
    }

    public void loadInitialContent() {
        // Reset cache
        featuredBooks.clear();
        freeBooks.clear();
        newBooks.clear();
        searchResults.clear();
        advancedSearchResults.clear();

        if (content != null) {
            content.getChildren().clear();

            // Sezioni principali
            content.getChildren().addAll(
                    sectionFactory.createFeaturedSection(),
                    sectionFactory.createBookSection("📚 Libri gratuiti", "free"),
                    sectionFactory.createBookSection("✨ Nuove uscite", "new")
            );

            // Carica categorie async
            sectionFactory.loadCategoriesAsync(content);
        }
    }

    public void handleSearch(String query, Consumer<Book> clickHandler) {
        if (query == null || query.trim().isEmpty()) {
            loadInitialContent();
            return;
        }

        // Reset cache precedenti
        searchResults.clear();
        advancedSearchResults.clear();

        // Usa PopupManager handler invece di quello passato
        Consumer<Book> popupManagerHandler = book -> handleBookClickWithPopupManager(book);

        sectionFactory.performSearch(query, content, popupManagerHandler);
    }

    public void showSearchResults(AdvancedSearchPanel.SearchResult searchResult) {
        if (content == null || searchResult == null) {
            return;
        }

        content.getChildren().clear();

        // Salva risultati ricerca avanzata
        advancedSearchResults = new ArrayList<>(searchResult.getBooks());
        searchResults.clear(); // Pulisci ricerca normale

        System.out.println("🎯 Advanced search results salvati: " + advancedSearchResults.size() + " libri");

        // Usa PopupManager handler
        Consumer<Book> popupManagerHandler = book -> handleBookClickWithPopupManager(book);

        VBox resultsSection = sectionFactory.createAdvancedSearchResultsSection(
                searchResult.getDescription(),
                searchResult.getBooks(),
                popupManagerHandler
        );

        content.getChildren().add(resultsSection);
    }

    /**
     * Debug dello stato delle cache
     */
    public void debugCacheState() {
        System.out.println("🔍 ContentArea Cache State:");
        System.out.println("  Featured books: " + featuredBooks.size());
        System.out.println("  Free books: " + freeBooks.size());
        System.out.println("  New books: " + newBooks.size());
        System.out.println("  Search results: " + searchResults.size());
        System.out.println("  Advanced search results: " + advancedSearchResults.size());

        // Debug PopupManager
        PopupManager.getInstance().debugFullState();
    }

    /**
     * Test dell'integrazione con PopupManager
     */
    public void testPopupManagerIntegration() {
        System.out.println("🧪 Test integrazione ContentArea + PopupManager");

        PopupManager popupManager = PopupManager.getInstance();

        if (!popupManager.isInitialized()) {
            System.out.println("⚠️ PopupManager non inizializzato, tentativo inizializzazione...");

            StackPane mainRoot = findMainRootContainer();
            if (mainRoot != null) {
                popupManager.initialize(mainRoot);
                System.out.println("✅ PopupManager inizializzato con successo");
            } else {
                System.out.println("❌ Impossibile inizializzare PopupManager");
                return;
            }
        }

        // Test con libro di prova
        if (!featuredBooks.isEmpty()) {
            Book testBook = featuredBooks.get(0);
            System.out.println("🧪 Test apertura popup con: " + testBook.getTitle());

            try {
                popupManager.showBookDetails(testBook, featuredBooks, authManager);
                System.out.println("✅ Test popup riuscito");
            } catch (Exception e) {
                System.err.println("❌ Test popup fallito: " + e.getMessage());
            }
        } else {
            System.out.println("📚 Nessun libro disponibile per test");
        }
    }

    /**
     * Cleanup delle risorse
     */
    public void cleanup() {
        System.out.println("🧹 ContentArea: Cleanup risorse");

        // Pulisci cache
        featuredBooks.clear();
        freeBooks.clear();
        newBooks.clear();
        searchResults.clear();
        advancedSearchResults.clear();

        // Reset handlers
        bookClickHandler = null;
        cachedBooksCallback = null;

        if (content != null) {
            content.getChildren().clear();
        }

        System.out.println("✅ ContentArea: Cleanup completato");
    }

    // =====================================================
    // METODI RICHIESTI DA SIDEBAR E MAINWINDOW
    // =====================================================

    /**
     * Imposta l'integrazione per la sezione Esplora
     */
    public void setExploreIntegration(ExploreIntegration integration) {
        this.exploreIntegration = integration;
        if (integration != null) {
            integration.setBookClickHandler(book -> handleBookClickWithPopupManager(book));
            System.out.println("✅ ExploreIntegration configurata con PopupManager handler");
        }
    }

    /**
     * Forza ritorno alla vista home
     */
    public void forceHomeView() {
        System.out.println("🏠 Forzatura ritorno alla vista home");
        loadInitialContent();
    }

    /**
     * Gestisce i click dal menu della sidebar
     */
    public void handleMenuClick(int menuIndex) {
        System.out.println("🎯 ContentArea: Click menu index " + menuIndex);

        switch (menuIndex) {
            case 0: // Home
                showHome();
                break;
            case 1: // Le Mie Librarie
                showMyLibraries();
                break;
            case 2: // Esplora
                showExplore();
                break;
            case 3: // Audiobook Store
                showAudiobookStore();
                break;
            case 4: // Tutti i Libri
                showAllBooks();
                break;
            case 5: // Ricerca Avanzata
                showAdvancedSearchInfo();
                break;
            case 6: // Libri Letti
                showReadBooks();
                break;
            case 7: // PDF
                showPDFBooks();
                break;
            default:
                System.out.println("❓ Menu index non riconosciuto: " + menuIndex);
                break;
        }
    }

    private void showHome() {
        System.out.println("🏠 Caricamento Home");
        loadInitialContent();
    }

    // In ContentArea.java, sostituisci il metodo showExplore() con questo:

    private void showExplore() {
        System.out.println("🔍 Caricamento sezione Esplora");

        if (exploreIntegration != null) {
            try {
                // Pulisci il contenuto corrente (NON l'intero layout!)
                content.getChildren().clear();

                // Crea la vista Esplora
                ScrollPane exploreView = exploreIntegration.createExploreView();

                // IMPORTANTE: Aggiungi la vista al contenuto VBox, non sostituire tutto
                VBox exploreContainer = new VBox();
                exploreContainer.setStyle("-fx-background-color: #1a1a1c;");

                // Estrai il contenuto dallo ScrollPane e aggiungilo al container
                if (exploreView.getContent() instanceof VBox) {
                    VBox exploreContent = (VBox) exploreView.getContent();
                    exploreContainer.getChildren().add(exploreContent);
                } else {
                    exploreContainer.getChildren().add(exploreView.getContent());
                }

                // Aggiungi al content area esistente
                content.getChildren().add(exploreContainer);

                System.out.println("✅ Sezione Esplora caricata correttamente nel content area");

            } catch (Exception e) {
                System.err.println("❌ Errore caricamento Esplora: " + e.getMessage());
                e.printStackTrace();
                showPlaceholderSection("🔍 Esplora", "Errore nel caricamento della sezione Esplora");
            }
        } else {
            System.err.println("❌ ExploreIntegration non inizializzato");
            showPlaceholderSection("🔍 Esplora", "Sezione Esplora non disponibile");
        }
    }

    /**
     * Trova il root container per sostituire le viste
     */
    private StackPane findRootContainer() {
        try {
            if (content != null && content.getScene() != null) {
                javafx.scene.Node root = content.getScene().getRoot();
                if (root instanceof StackPane) {
                    return (StackPane) root;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void showMyLibraries() {
        showPlaceholderSection("📚 Le Mie Librerie", "Gestisci le tue collezioni private di libri");
    }

    private void showAudiobookStore() {
        showPlaceholderSection("🎧 Audiobook Store", "Scopri audiolibri e contenuti audio");
    }

    private void showAllBooks() {
        showPlaceholderSection("📖 Tutti i Libri", "Sfoglia l'intera collezione disponibile");
    }

    private void showAdvancedSearchInfo() {
        showPlaceholderSection("🔍 Ricerca Avanzata", "Utilizza l'icona di ricerca nell'header per accedere alla ricerca avanzata");
    }

    private void showReadBooks() {
        showPlaceholderSection("✅ Libri Letti", "I tuoi libri completati e le recensioni");
    }

    private void showPDFBooks() {
        showPlaceholderSection("📄 Documenti PDF", "Gestisci i tuoi documenti e file PDF");
    }

    private void showPlaceholderSection(String title, String description) {
        if (content != null) {
            content.getChildren().clear();

            javafx.scene.layout.VBox placeholderContainer = new javafx.scene.layout.VBox(20);
            placeholderContainer.setPadding(new Insets(60));
            placeholderContainer.setAlignment(javafx.geometry.Pos.CENTER);
            placeholderContainer.setStyle("-fx-background-color: #1e1e1e;");

            javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
            titleLabel.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 24));
            titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);

            javafx.scene.control.Label descLabel = new javafx.scene.control.Label(description);
            descLabel.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.NORMAL, 16));
            descLabel.setTextFill(javafx.scene.paint.Color.LIGHTGRAY);
            descLabel.setWrapText(true);
            descLabel.setAlignment(javafx.geometry.Pos.CENTER);

            javafx.scene.control.Label comingSoonLabel = new javafx.scene.control.Label("🚧 In arrivo...");
            comingSoonLabel.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.NORMAL, 14));
            comingSoonLabel.setTextFill(javafx.scene.paint.Color.ORANGE);

            placeholderContainer.getChildren().addAll(titleLabel, descLabel, comingSoonLabel);
            content.getChildren().add(placeholderContainer);
        }
    }

    // =====================================================
    // GETTER PER COMPATIBILITÀ
    // =====================================================

    public StackPane getRootContainer() {
        return findRootContainer();
    }

    public BookService getBookService() {
        return bookService;
    }

    public boolean isServerAvailable() {
        return serverAvailable;
    }

    public AuthenticationManager getAuthManager() {
        return authManager;
    }

    public ExploreIntegration getExploreIntegration() {
        return exploreIntegration;
    }

    public List<Book> getFeaturedBooks() {
        return new ArrayList<>(featuredBooks);
    }

    public List<Book> getFreeBooks() {
        return new ArrayList<>(freeBooks);
    }

    public List<Book> getNewBooks() {
        return new ArrayList<>(newBooks);
    }

    public List<Book> getSearchResults() {
        return new ArrayList<>(searchResults);
    }

    public List<Book> getAdvancedSearchResults() {
        return new ArrayList<>(advancedSearchResults);
    }

    public boolean isShowingExplore() {
        if (exploreIntegration == null) {
            return false;
        }
        return exploreIntegration.isViewingCategory();
    }

    public void clearNavigationCache() {
        featuredBooks.clear();
        freeBooks.clear();
        newBooks.clear();
        searchResults.clear();
        advancedSearchResults.clear();
        System.out.println("🗑️ Cache di navigazione pulita");
    }
}