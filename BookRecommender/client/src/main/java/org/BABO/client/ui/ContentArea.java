package org.BABO.client.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;

/**
 * ContentArea aggiornata con supporto completo per ricerca avanzata
 * e integrazione migliorata con PopupManager
 */
public class ContentArea {

    private final BookService bookService;
    private final boolean serverAvailable;
    private AuthenticationManager authManager;
    private VBox content;
    private Consumer<Book> bookClickHandler;
    private BookSectionFactory sectionFactory;
    private Consumer<List<Book>> cachedBooksCallback;
    private ExploreIntegration exploreIntegration;
    private MainWindow mainWindowRef;

    // Cache per navigazione contestuale
    private List<Book> featuredBooks = new ArrayList<>();
    private List<Book> freeBooks = new ArrayList<>();
    private List<Book> newBooks = new ArrayList<>();
    private List<Book> searchResults = new ArrayList<>();
    private List<Book> advancedSearchResults = new ArrayList<>();

    // ✅ AGGIUNTO: Per gestione categorie
    private CategoryView currentCategoryView = null;

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
     * ✅ AGGIORNATO: Include category click handler
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

        // ✅ AGGIUNTO: Configura il category click handler
        this.sectionFactory.setCategoryClickHandler(category -> handleCategoryClick(category));
        System.out.println("✅ Category click handler configurato");

        //debug
        this.sectionFactory.setFreeBooksCallback(books -> {
            this.freeBooks = new ArrayList<>(books);
            System.out.println("🆓 ✅ CALLBACK: Free books salvati per navigazione: " + books.size());
            // Debug: stampa i titoli per verificare
            for (int i = 0; i < Math.min(books.size(), 3); i++) {
                System.out.println("   " + (i+1) + ". " + books.get(i).getTitle());
            }
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
                    return;
                }
            }

            // Apri il popup
            popupManager.showBookDetails(book, navigationBooks, authManager);

        } catch (Exception e) {
            System.err.println("❌ ContentArea: Errore nel click handler - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ NUOVO: Gestisce il click su una categoria
     */
    private void handleCategoryClick(Category category) {
        System.out.println("🎭 ContentArea: Click su categoria '" + category.getName() + "'");

        try {
            // Crea la vista per la categoria
            currentCategoryView = new CategoryView(category, bookService, book -> handleBookClickWithPopupManager(book));

            // Sostituisci il contenuto corrente con la vista della categoria
            content.getChildren().clear();
            ScrollPane categoryScrollPane = currentCategoryView.createCategoryView();

            // Assicurati che lo ScrollPane si adatti al contenitore
            categoryScrollPane.setStyle("-fx-background-color: #1a1a1c;");

            // Aggiungi la vista della categoria al content
            content.getChildren().add(categoryScrollPane);

            System.out.println("✅ Vista categoria '" + category.getName() + "' caricata");

        } catch (Exception e) {
            System.err.println("❌ Errore nel caricamento della categoria: " + e.getMessage());
            e.printStackTrace();

            // In caso di errore, mostra un messaggio e torna alla home
            showCategoryError(category.getName());
        }
    }

    /**
     * ✅ NUOVO: Mostra un messaggio di errore quando non si riesce a caricare una categoria
     */
    private void showCategoryError(String categoryName) {
        content.getChildren().clear();

        VBox errorContainer = new VBox(20);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.setPadding(new Insets(50));
        errorContainer.setStyle("-fx-background-color: #1a1a1c;");

        Label errorTitle = new Label("❌ Errore nel caricamento");
        errorTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        errorTitle.setTextFill(Color.LIGHTCORAL);

        Label errorMessage = new Label("Non è stato possibile caricare i libri della categoria \"" + categoryName + "\"");
        errorMessage.setFont(Font.font("System", FontWeight.NORMAL, 16));
        errorMessage.setTextFill(Color.LIGHTGRAY);
        errorMessage.setWrapText(true);

        Button backButton = new Button("🏠 Torna alla Home");
        backButton.setStyle(
                "-fx-background-color: #4a86e8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> loadInitialContent());

        errorContainer.getChildren().addAll(errorTitle, errorMessage, backButton);
        content.getChildren().add(errorContainer);
    }

    /**
     * Determina il contesto di navigazione per un libro
     */
    private List<Book> determineNavigationContext(Book book) {
        //debug
        System.out.println("🔍 DEBUG: Determinazione contesto per libro: " + book.getTitle());
        System.out.println("   - Featured books cache: " + featuredBooks.size());
        System.out.println("   - Free books cache: " + freeBooks.size());
        System.out.println("   - New books cache: " + newBooks.size());

        if (featuredBooks.contains(book)) {
            System.out.println("   -> Trovato in FEATURED");
            return featuredBooks;
        } else if (freeBooks.contains(book)) {
            System.out.println("   -> Trovato in FREE ✅");
            return freeBooks;
        } else if (newBooks.contains(book)) {
            System.out.println("   -> Trovato in NEW");
            return newBooks;
        }
        // Controlla in quale sezione si trova il libro
        if (featuredBooks.contains(book)) {
            return featuredBooks;
        } else if (freeBooks.contains(book)) {
            return freeBooks;
        } else if (newBooks.contains(book)) {
            return newBooks;
        } else if (searchResults.contains(book)) {
            return searchResults;
        } else if (advancedSearchResults.contains(book)) {
            return advancedSearchResults;
        } else {
            // Fallback: crea una lista con solo questo libro
            List<Book> singleBookList = new ArrayList<>();
            singleBookList.add(book);
            return singleBookList;
        }
    }

    /**
     * Trova il container principale per PopupManager
     */
    private StackPane findMainRootContainer() {
        try {
            // Cerca nel parent hierarchy
            if (content != null && content.getParent() != null) {
                var parent = content.getParent();
                while (parent != null) {
                    if (parent instanceof StackPane) {
                        return (StackPane) parent;
                    }
                    parent = parent.getParent();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca del main root container: " + e.getMessage());
        }
        return null;
    }

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
            case 1: // Le Mie Librerie
                showMyLibraries();
                break;
            case 2: // Esplora
                showExplore();
                break;
            default:
                System.out.println("❓ Menu index non riconosciuto: " + menuIndex);
                break;
        }
    }

    private void showHome() {
        System.out.println("🏠 ContentArea: Caricamento Home");
        loadInitialContent(); // Torna al comportamento originale
    }

    private void showExplore() {
        System.out.println("🔍 ContentArea: Caricamento sezione Esplora");

        if (exploreIntegration != null) {
            try {
                // Pulisci il contenuto corrente
                content.getChildren().clear();

                // Crea la vista Esplora
                ScrollPane exploreView = exploreIntegration.createExploreView();

                // Crea container per la vista Esplora
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

                System.out.println("✅ Sezione Esplora caricata correttamente");

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
     * NUOVO: Carica il contenuto solo se necessario
     */
    public void loadInitialContentIfNeeded() {
        // Controlla se il content è vuoto o contiene solo placeholder/errori
        boolean needsLoading = content.getChildren().isEmpty() ||
                isContentEmpty();

        if (needsLoading) {
            System.out.println("🏠 ContentArea: Contenuto mancante, caricamento necessario");
            loadInitialContent();
        } else {
            System.out.println("✅ ContentArea: Contenuto già presente, salto il caricamento");
        }
    }

    /**
     * NUOVO: Controlla se il content è "vuoto" (solo placeholder o errori)
     */
    private boolean isContentEmpty() {
        return content.getChildren().stream().allMatch(node -> {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                return vbox.getChildren().stream().anyMatch(child -> {
                    if (child instanceof Label) {
                        String text = ((Label) child).getText();
                        return text.contains("Le Mie Librerie") ||
                                text.contains("Esplora") ||
                                text.contains("Errore") ||
                                text.contains("placeholder") ||
                                text.contains("non disponibile");
                    }
                    return false;
                });
            }
            return false;
        });
    }

    /**
     * NUOVO: Imposta il riferimento al MainWindow
     */
    public void setMainWindowReference(MainWindow mainWindow) {
        this.mainWindowRef = mainWindow;
        System.out.println("✅ ContentArea: Riferimento MainWindow configurato");
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

            VBox placeholderContainer = new VBox(20);
            placeholderContainer.setPadding(new Insets(60));
            placeholderContainer.setAlignment(Pos.CENTER);
            placeholderContainer.setStyle("-fx-background-color: #1e1e1e;");

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            titleLabel.setTextFill(Color.WHITE);

            Label descLabel = new Label(description);
            descLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
            descLabel.setTextFill(Color.LIGHTGRAY);
            descLabel.setWrapText(true);
            descLabel.setAlignment(Pos.CENTER);

            placeholderContainer.getChildren().addAll(titleLabel, descLabel);
            content.getChildren().add(placeholderContainer);
        }
    }


    /**
     * NUOVO: Carica il contenuto iniziale solo se non è già stato caricato
     */
    public void loadInitialContentOnce() {
        // Controlla se il content è vuoto o contiene solo placeholder
        boolean needsLoading = content.getChildren().isEmpty() ||
                content.getChildren().stream().allMatch(node ->
                        node instanceof VBox &&
                                ((VBox) node).getChildren().stream().anyMatch(child ->
                                        child instanceof Label &&
                                                (((Label) child).getText().contains("placeholder") ||
                                                        ((Label) child).getText().contains("Errore") ||
                                                        ((Label) child).getText().contains("Le Mie Librerie") ||
                                                        ((Label) child).getText().contains("Esplora"))
                                )
                );

        if (needsLoading) {
            System.out.println("🏠 ContentArea: Caricamento necessario, eseguo loadInitialContent");
            loadInitialContent();
        } else {
            System.out.println("✅ ContentArea: Home già caricata, salto il caricamento");
        }
    }

    /**
     * Carica il contenuto iniziale della home page
     */
    public void loadInitialContent() {
        System.out.println("🏠 ContentArea: Caricamento contenuto iniziale");

        // Reset cache
        featuredBooks.clear();
        freeBooks.clear();
        newBooks.clear();
        searchResults.clear();
        advancedSearchResults.clear();

        if (content != null) {
            // Pulisci TUTTO il contenuto esistente
            content.getChildren().clear();
            System.out.println("🧹 Content pulito completamente");

            // Sezioni principali
            content.getChildren().addAll(
                    sectionFactory.createFeaturedSection(),
                    sectionFactory.createBookSection("📚 Libri gratuiti", "free"),
                    sectionFactory.createBookSection("✨ Nuove uscite", "new")
            );

            // CORREZIONE: Carica categorie con delay per evitare race condition
            Platform.runLater(() -> {
                System.out.println("🎭 Avvio caricamento categorie con delay...");
                sectionFactory.loadCategoriesAsync(content);
            });
        }
    }

    /**
     * ✅ AGGIORNATO: Gestisce le ricerche con supporto per ricerca avanzata
     */
    public void handleSearch(String query, Consumer<Book> clickHandler) {
        if (query == null || query.trim().isEmpty()) {
            loadInitialContent();
            return;
        }

        // Reset cache precedenti
        searchResults.clear();
        advancedSearchResults.clear();

        System.out.println("🔍 ContentArea handling search: " + query);

        // Usa PopupManager handler invece di quello passato
        Consumer<Book> popupManagerHandler = book -> handleBookClickWithPopupManager(book);

        // ✅ CORREZIONE: Gestisci query speciali per ricerca avanzata
        if (query.startsWith("title-only:")) {
            // Query specifica solo per titolo
            String title = query.substring(11).trim(); // Rimuovi "title-only:"
            handleTitleOnlySearch(title, popupManagerHandler);

        } else if (query.startsWith("author:")) {
            // Query tipo "author:James year:2002-2003"
            if (query.contains("year:")) {
                // Ricerca combinata autore + anno
                handleYearFilteredSearch(query, popupManagerHandler);
            } else {
                // Solo ricerca per autore - estrai solo la parte dopo "author:"
                String author = query.substring(7).trim();
                handleAuthorSearch(author, popupManagerHandler);
            }
        } else if (query.contains("year:")) {
            // Ricerca con filtro anno (dovrebbe sempre avere anche author:)
            handleYearFilteredSearch(query, popupManagerHandler);
        } else {
            // Ricerca normale dalla barra (titolo + autore)
            handleTitleSearch(query, popupManagerHandler);
        }
    }

    private void handleTitleSearch(String query, Consumer<Book> clickHandler) {
        System.out.println("📖 Ricerca GENERALE (titolo + autore): " + query);
        // La ricerca normale dalla barra cerca in titolo E autore
        sectionFactory.performSearch(query, content, clickHandler);
    }

    private void handleTitleOnlySearch(String title, Consumer<Book> clickHandler) {
        System.out.println("📖 Ricerca SPECIFICA solo titolo: " + title);

        content.getChildren().clear();
        Label loadingLabel = new Label("🔍 Ricerca per titolo: " + title + "...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        // ✅ USA CompletableFuture.supplyAsync con metodo sincrono
        CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("🔍 Tentativo ricerca titolo specifica...");
                        return bookService.searchBooksByTitle(title);
                    } catch (Exception e) {
                        System.err.println("❌ Errore ricerca titolo specifica: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        System.out.println("✅ Ricerca titolo completata: " + results.size() + " risultati");
                        this.advancedSearchResults = new ArrayList<>(results);
                        displaySearchResults(results, "📖 Titolo: " + title, clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Errore ricerca titolo: " + throwable.getMessage());

                    // ✅ FALLBACK: ricerca generale + filtro lato client
                    Platform.runLater(() -> {
                        System.out.println("🔄 Fallback: ricerca generale con filtro titolo");
                        handleTitleSearchFallback(title, clickHandler);
                    });
                    return null;
                });
    }

    private void handleTitleSearchFallback(String title, Consumer<Book> clickHandler) {
        System.out.println("🔄 Fallback ricerca titolo con filtro client");

        bookService.searchBooksAsync(title)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        // ✅ FILTRO SOLO PER TITOLO
                        List<Book> titleResults = filterBooksByTitleOnly(results, title);
                        this.advancedSearchResults = new ArrayList<>(titleResults);
                        displaySearchResults(titleResults, "📖 Titolo: " + title + " (filtrato)", clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore ricerca titolo: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    private void handleAuthorSearch(String author, Consumer<Book> clickHandler) {
        System.out.println("👤 Ricerca per autore: " + author);

        content.getChildren().clear();
        Label loadingLabel = new Label("🔍 Ricerca per autore: " + author + "...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        // ✅ FIX: USA SOLO searchBooksAsync() che funziona
        bookService.searchBooksAsync(author)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        // ✅ FILTRO LATO CLIENT per solo autori
                        List<Book> authorResults = filterBooksByAuthor(results, author);
                        this.advancedSearchResults = new ArrayList<>(authorResults);
                        displaySearchResults(authorResults, "👤 Autore: " + author, clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore nella ricerca per autore: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    private List<Book> filterBooksByTitleOnly(List<Book> books, String targetTitle) {
        if (targetTitle == null || targetTitle.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();
        String searchTitle = targetTitle.toLowerCase().trim();

        for (Book book : books) {
            if (book.getTitle() != null &&
                    book.getTitle().toLowerCase().contains(searchTitle)) {
                filtered.add(book);
            }
        }

        System.out.println("📖 Filtro SOLO titolo '" + targetTitle + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    private List<Book> filterBooksByAuthor(List<Book> books, String targetAuthor) {
        if (targetAuthor == null || targetAuthor.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();
        String searchAuthor = targetAuthor.toLowerCase().trim();

        for (Book book : books) {
            if (book.getAuthor() != null &&
                    book.getAuthor().toLowerCase().contains(searchAuthor)) {
                filtered.add(book);
            }
        }

        System.out.println("👤 Filtro autore '" + targetAuthor + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    private void handleYearFilteredSearch(String query, Consumer<Book> clickHandler) {
        System.out.println("📅 Ricerca combinata autore + anno: " + query);

        // Parsing della query "author:James year:2002-2003"
        String author = "";
        String yearRange = "";

        if (query.contains("author:") && query.contains("year:")) {
            String[] parts = query.split("\\s+");
            for (String part : parts) {
                if (part.startsWith("author:")) {
                    author = part.substring(7);
                } else if (part.startsWith("year:")) {
                    yearRange = part.substring(5);
                }
            }
        }

        final String finalAuthor = author;
        final String finalYearRange = yearRange;

        content.getChildren().clear();
        Label loadingLabel = new Label("🔍🗓️ Ricerca avanzata: " + finalAuthor + " (" + finalYearRange + ")...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        // ✅ FIX: USA SOLO searchBooksAsync() che funziona
        bookService.searchBooksAsync(finalAuthor)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        // ✅ FILTRO LATO CLIENT per autore e anno
                        List<Book> authorResults = filterBooksByAuthor(results, finalAuthor);
                        List<Book> filteredResults = filterBooksByYearRange(authorResults, finalYearRange);

                        this.advancedSearchResults = new ArrayList<>(filteredResults);
                        displaySearchResults(filteredResults,
                                "👤📅 " + finalAuthor + " (" + finalYearRange + ")", clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore ricerca avanzata: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    private List<Book> filterBooksByYearRange(List<Book> books, String yearRange) {
        if (yearRange == null || yearRange.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();

        // Parse "2002-2003" o "2002"
        String[] yearParts = yearRange.split("-");
        Integer yearFrom = null;
        Integer yearTo = null;

        try {
            if (yearParts.length >= 1 && !yearParts[0].isEmpty()) {
                yearFrom = Integer.parseInt(yearParts[0].trim());
            }
            if (yearParts.length >= 2 && !yearParts[1].isEmpty()) {
                yearTo = Integer.parseInt(yearParts[1].trim());
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Formato anno non valido: " + yearRange);
            return books; // Restituisci tutti se il formato è sbagliato
        }

        for (Book book : books) {
            try {
                String bookYearStr = book.getPublishYear();
                if (bookYearStr == null || bookYearStr.trim().isEmpty()) {
                    continue;
                }

                int bookYear = Integer.parseInt(bookYearStr.trim());
                boolean inRange = true;

                if (yearFrom != null && bookYear < yearFrom) {
                    inRange = false;
                }
                if (yearTo != null && bookYear > yearTo) {
                    inRange = false;
                }

                if (inRange) {
                    filtered.add(book);
                }

            } catch (NumberFormatException e) {
                // Salta libri con anno non numerico
                continue;
            }
        }

        System.out.println("📅 Filtro anno '" + yearRange + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    private void displaySearchResults(List<Book> results, String searchTitle, Consumer<Book> clickHandler) {
        content.getChildren().clear();

        if (results.isEmpty()) {
            Label noResults = new Label("❌ Nessun risultato trovato per: " + searchTitle);
            noResults.setTextFill(Color.WHITE);
            noResults.setFont(Font.font("System", FontWeight.NORMAL, 18));

            Label suggestion = new Label("💡 Prova con parole chiave diverse");
            suggestion.setTextFill(Color.GRAY);
            suggestion.setFont(Font.font("System", FontWeight.NORMAL, 14));

            VBox noResultsBox = new VBox(10, noResults, suggestion);
            noResultsBox.setAlignment(Pos.CENTER);
            noResultsBox.setPadding(new Insets(50));

            content.getChildren().add(noResultsBox);
        } else {
            // Crea sezione risultati
            Label title = new Label(searchTitle + " (" + results.size() + " risultati)");
            title.setFont(Font.font("System", FontWeight.BOLD, 20));
            title.setTextFill(Color.WHITE);
            title.setPadding(new Insets(0, 0, 15, 0));

            // Usa BookGridBuilder per creare la griglia
            BookGridBuilder gridBuilder = new BookGridBuilder();
            gridBuilder.setBookClickHandler(clickHandler);

            VBox resultsContainer = new VBox(15);
            resultsContainer.setPadding(new Insets(15, 20, 20, 20));
            resultsContainer.getChildren().add(title);

            // Crea griglia ottimizzata
            FlowPane bookGrid = gridBuilder.createOptimizedBookGrid();
            gridBuilder.populateBookGrid(results, bookGrid, null);

            ScrollPane scroll = new ScrollPane(bookGrid);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setPannable(true);
            scroll.setFitToHeight(true);
            scroll.setPrefHeight(400);
            scroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");

            resultsContainer.getChildren().add(scroll);
            content.getChildren().add(resultsContainer);
        }
    }

    // ✅ METODI MANCANTI PER COMPATIBILITÀ

    /**
     * Imposta callback per libri cachati (per MainWindow)
     */
    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        this.cachedBooksCallback = callback;
        if (this.sectionFactory != null) {
            this.sectionFactory.setCachedBooksCallback(callback);
        }
    }

    /**
     * Mostra risultati ricerca avanzata (per MainWindow)
     */
    public void showAdvancedSearchResults(Object searchResult) {
        System.out.println("🔍 Mostra risultati ricerca avanzata: " + searchResult);
        // Per ora semplicemente delega alla gestione ricerca normale
        if (searchResult != null) {
            String query = searchResult.toString();
            handleSearch(query, book -> handleBookClickWithPopupManager(book));
        }
    }

    /**
     * Debug dello stato cache (per MainWindow)
     */
    public void debugCacheState() {
        System.out.println("🔍 ===== CONTENTAREA CACHE DEBUG =====");
        System.out.println("Featured books: " + featuredBooks.size());
        System.out.println("Free books: " + freeBooks.size());
        System.out.println("New books: " + newBooks.size());
        System.out.println("Search results: " + searchResults.size());
        System.out.println("Advanced search results: " + advancedSearchResults.size());
    }

    /**
     * Cleanup delle risorse (per MainWindow)
     */
    public void cleanup() {
        System.out.println("🧹 ContentArea cleanup");
        if (featuredBooks != null) featuredBooks.clear();
        if (freeBooks != null) freeBooks.clear();
        if (newBooks != null) newBooks.clear();
        if (searchResults != null) searchResults.clear();
        if (advancedSearchResults != null) advancedSearchResults.clear();
        currentCategoryView = null;
    }

    // Getter per compatibilità
    public VBox getContent() {
        return content;
    }

    public BookService getBookService() {
        return bookService;
    }

    public boolean isServerAvailable() {
        return serverAvailable;
    }
}