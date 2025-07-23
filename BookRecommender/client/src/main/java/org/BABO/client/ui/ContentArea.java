package org.BABO.client.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
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
        if (query.startsWith("author:")) {
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
            // Ricerca normale per titolo
            handleTitleSearch(query, popupManagerHandler);
        }
    }

    /**
     * ✅ SEMPLIFICATO: Gestisce ricerca per autore (solo autore, senza anno)
     */
    private void handleAuthorSearch(String author, Consumer<Book> clickHandler) {
        System.out.println("👤 Ricerca solo per autore: " + author);
        performAuthorSearchFallback(author, content, clickHandler);
    }

    /**
     * ✅ NUOVO: Gestisce ricerca con filtro anno
     */
    private void handleYearFilteredSearch(String query, Consumer<Book> clickHandler) {
        System.out.println("🔍 Ricerca con filtro anno: " + query);

        // Usa sempre il metodo di fallback per evitare errori
        performYearSearchFallback(query, content, clickHandler);
    }

    /**
     * ✅ NUOVO: Gestisce ricerca normale per titolo
     */
    private void handleTitleSearch(String title, Consumer<Book> clickHandler) {
        System.out.println("🔍 Ricerca per titolo: " + title);

        // Usa il metodo di ricerca esistente
        sectionFactory.performSearch(title, content, clickHandler);
    }

    /**
     * ✅ NUOVO: Metodo di fallback per ricerca per autore
     */
    private void performAuthorSearchFallback(String author, VBox content, Consumer<Book> clickHandler) {
        content.getChildren().clear();

        // Mostra indicatore di caricamento
        Label loadingLabel = new Label("🔄 Ricerca per autore: " + author + "...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        // Ricerca asincrona
        CompletableFuture.supplyAsync(() -> {
            try {
                return bookService.searchBooksByAuthor(author);
            } catch (Exception e) {
                System.err.println("❌ Errore ricerca autore: " + e.getMessage());
                return new ArrayList<Book>();
            }
        }).thenAccept(books -> {
            Platform.runLater(() -> {
                content.getChildren().clear();

                if (books.isEmpty()) {
                    Label noResultsLabel = new Label("📚 Nessun libro trovato per l'autore: " + author);
                    noResultsLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
                    noResultsLabel.setTextFill(Color.LIGHTGRAY);
                    content.getChildren().add(noResultsLabel);
                } else {
                    // Salva risultati nella cache
                    searchResults = new ArrayList<>(books);

                    // Crea sezione risultati
                    VBox resultsSection = createSearchResultsSection("Risultati per autore: " + author, books, clickHandler);
                    content.getChildren().add(resultsSection);
                }
            });
        });
    }

    /**
     * ✅ CORRETTO: Metodo di fallback per ricerca con filtro anno
     */
    private void performYearSearchFallback(String query, VBox content, Consumer<Book> clickHandler) {
        content.getChildren().clear();

        // ✅ PARSING CORRETTO della query
        String author = "";
        String yearFrom = "";
        String yearTo = "";

        System.out.println("🔍 Query ricevuta: " + query);

        try {
            // ✅ MIGLIORATO: Parse della query "author:j year:2000-2004" o "j year:2000-2004"
            String[] parts = query.split("\\s+");

            for (String part : parts) {
                if (part.startsWith("author:")) {
                    author = part.substring(7).trim();
                } else if (part.startsWith("year:")) {
                    String yearPart = part.substring(5).trim();

                    if (yearPart.contains("-")) {
                        // Range di anni: "2000-2004"
                        String[] years = yearPart.split("-");
                        if (years.length >= 1) yearFrom = years[0].trim();
                        if (years.length >= 2) yearTo = years[1].trim();
                    } else if (yearPart.contains("..")) {
                        // Range con ".." : "..2004" o "2000.."
                        String[] years = yearPart.split("\\.\\.");
                        if (yearPart.startsWith("..")) {
                            yearTo = years.length > 1 ? years[1].trim() : "";
                        } else if (yearPart.endsWith("..")) {
                            yearFrom = years[0].trim();
                        }
                    } else {
                        // Anno singolo
                        yearFrom = yearPart;
                        yearTo = yearPart;
                    }
                } else if (author.isEmpty() && !part.startsWith("year:")) {
                    // ✅ NUOVO: Se non abbiamo trovato "author:" esplicito,
                    // considera la prima parte come nome autore
                    author = part.trim();
                }
            }

            // ✅ FALLBACK: Se ancora non abbiamo autore, prova a estrarlo diversamente
            if (author.isEmpty() && query.contains("author:")) {
                int authorIndex = query.indexOf("author:");
                int yearIndex = query.indexOf("year:");

                if (yearIndex > authorIndex) {
                    author = query.substring(authorIndex + 7, yearIndex).trim();
                } else {
                    author = query.substring(authorIndex + 7).trim();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nel parsing della query: " + e.getMessage());
        }

        System.out.println("📊 Parametri estratti:");
        System.out.println("   Autore: '" + author + "'");
        System.out.println("   Anno da: '" + yearFrom + "'");
        System.out.println("   Anno a: '" + yearTo + "'");

        // Validazione parametri
        if (author.isEmpty()) {
            content.getChildren().add(createErrorLabel("❌ Parametro autore mancante nella query"));
            return;
        }

        // Mostra indicatore di caricamento
        String searchDescription = buildSearchDescription(author, yearFrom, yearTo);
        Label loadingLabel = new Label("🔄 " + searchDescription + "...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        final String finalAuthor = author;
        final String finalYearFrom = yearFrom;
        final String finalYearTo = yearTo;
        final String finalDescription = searchDescription;

        // ✅ RICERCA MIGLIORATA - prova diverse strategie
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Book> results = new ArrayList<>();

                if (!finalYearFrom.isEmpty() || !finalYearTo.isEmpty()) {
                    System.out.println("🔍 Ricerca con filtro anno...");

                    if (!finalYearFrom.isEmpty() && !finalYearTo.isEmpty() && !finalYearFrom.equals(finalYearTo)) {
                        // Range di anni - cerca per ogni anno nel range
                        try {
                            int fromYear = Integer.parseInt(finalYearFrom);
                            int toYear = Integer.parseInt(finalYearTo);

                            System.out.println("📅 Ricerca range anni: " + fromYear + " - " + toYear);

                            // Cerca per ogni anno nel range
                            for (int year = fromYear; year <= toYear && year <= fromYear + 10; year++) {
                                List<Book> yearResults = bookService.searchBooksByAuthorAndYear(finalAuthor, String.valueOf(year));
                                System.out.println("   Anno " + year + ": " + yearResults.size() + " risultati");

                                for (Book book : yearResults) {
                                    // Evita duplicati
                                    if (!results.stream().anyMatch(b ->
                                            Objects.equals(b.getTitle(), book.getTitle()) &&
                                                    Objects.equals(b.getAuthor(), book.getAuthor()))) {
                                        results.add(book);
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("❌ Errore parsing anni: " + e.getMessage());
                            // Fallback: cerca solo per autore
                            results = bookService.searchBooksByAuthor(finalAuthor);
                        }
                    } else {
                        // Anno singolo
                        String year = !finalYearFrom.isEmpty() ? finalYearFrom : finalYearTo;
                        System.out.println("📅 Ricerca anno singolo: " + year);
                        results = bookService.searchBooksByAuthorAndYear(finalAuthor, year);
                    }
                } else {
                    // Solo ricerca per autore
                    System.out.println("👤 Ricerca solo per autore: " + finalAuthor);
                    results = bookService.searchBooksByAuthor(finalAuthor);
                }

                System.out.println("✅ Ricerca completata: " + results.size() + " risultati totali");
                return results;

            } catch (Exception e) {
                System.err.println("❌ Errore nella ricerca: " + e.getMessage());
                e.printStackTrace();

                // ✅ FALLBACK: Se la ricerca combinata fallisce, prova solo per autore
                try {
                    System.out.println("🔄 Tentativo fallback: ricerca solo per autore");
                    return bookService.searchBooksByAuthor(finalAuthor);
                } catch (Exception fallbackError) {
                    System.err.println("❌ Anche il fallback è fallito: " + fallbackError.getMessage());
                    return new ArrayList<Book>();
                }
            }
        }).thenAccept(books -> {
            Platform.runLater(() -> {
                content.getChildren().clear();

                if (books.isEmpty()) {
                    Label noResultsLabel = new Label("📚 Nessun libro trovato per: " + finalDescription);
                    noResultsLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
                    noResultsLabel.setTextFill(Color.LIGHTGRAY);

                    // Suggerimento per ampliare la ricerca
                    Label suggestionLabel = new Label("💡 Prova a cercare solo l'autore: author:" + finalAuthor);
                    suggestionLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                    suggestionLabel.setTextFill(Color.web("#888888"));
                    suggestionLabel.setWrapText(true);

                    VBox noResultsContainer = new VBox(10);
                    noResultsContainer.setAlignment(Pos.CENTER);
                    noResultsContainer.getChildren().addAll(noResultsLabel, suggestionLabel);

                    content.getChildren().add(noResultsContainer);
                } else {
                    // ✅ FILTRA RISULTATI per anno se specificato (controllo locale aggiuntivo)
                    List<Book> filteredBooks = filterBooksByYear(books, finalYearFrom, finalYearTo);

                    // Salva risultati nella cache
                    searchResults = new ArrayList<>(filteredBooks);

                    // Crea sezione risultati con il nuovo layout
                    VBox resultsSection = createSearchResultsSection(finalDescription, filteredBooks, clickHandler);
                    content.getChildren().add(resultsSection);

                    System.out.println("✅ Mostrati " + filteredBooks.size() + " risultati filtrati");
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                content.getChildren().clear();
                Label errorLabel = new Label("❌ Errore durante la ricerca: " + throwable.getMessage());
                errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
                errorLabel.setTextFill(Color.web("#ff6b6b"));
                content.getChildren().add(errorLabel);
            });
            return null;
        });
    }

    /**
     * ✅ NUOVO: Filtra localmente i libri per anno (controllo aggiuntivo)
     */
    private List<Book> filterBooksByYear(List<Book> books, String yearFrom, String yearTo) {
        if ((yearFrom.isEmpty() && yearTo.isEmpty()) || books.isEmpty()) {
            return books; // Nessun filtro per anno
        }

        List<Book> filtered = new ArrayList<>();

        for (Book book : books) {
            if (book.getPublishYear() == null || book.getPublishYear().trim().isEmpty()) {
                continue; // Salta libri senza anno
            }

            try {
                int bookYear = Integer.parseInt(book.getPublishYear().trim());
                boolean matches = true;

                if (!yearFrom.isEmpty()) {
                    int fromYear = Integer.parseInt(yearFrom);
                    if (bookYear < fromYear) matches = false;
                }

                if (!yearTo.isEmpty()) {
                    int toYear = Integer.parseInt(yearTo);
                    if (bookYear > toYear) matches = false;
                }

                if (matches) {
                    filtered.add(book);
                }

            } catch (NumberFormatException e) {
                // Se l'anno non è parsabile, includi il libro comunque
                filtered.add(book);
            }
        }

        System.out.println("🔍 Filtro locale: " + books.size() + " → " + filtered.size() + " libri");
        return filtered;
    }

    /**
     * ✅ NUOVO: Costruisce descrizione della ricerca
     */
    private String buildSearchDescription(String author, String yearFrom, String yearTo) {
        StringBuilder desc = new StringBuilder("Ricerca per autore: " + author);

        if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
            if (!yearFrom.isEmpty() && !yearTo.isEmpty() && !yearFrom.equals(yearTo)) {
                desc.append(" (dal ").append(yearFrom).append(" al ").append(yearTo).append(")");
            } else if (!yearFrom.isEmpty()) {
                desc.append(" (anno ").append(yearFrom).append(")");
            } else {
                desc.append(" (fino al ").append(yearTo).append(")");
            }
        }

        return desc.toString();
    }

    /**
     * ✅ NUOVO: Crea label di errore
     */
    private Label createErrorLabel(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        errorLabel.setTextFill(Color.web("#ff6b6b"));
        return errorLabel;
    }

    /**
     * ✅ AGGIORNATO: Layout identico alla ricerca per titolo originale
     */
    private VBox createSearchResultsSection(String title, List<Book> books, Consumer<Book> clickHandler) {
        VBox section = new VBox(20);
        section.setPadding(new Insets(30, 30, 30, 30));
        section.setStyle("-fx-background-color: #1e1e1e;");

        // Titolo sezione
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // Contatore risultati
        Label countLabel = new Label("(" + books.size() + ")");
        countLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        countLabel.setTextFill(Color.LIGHTGRAY);

        // ✅ LAYOUT A GRIGLIA come nella ricerca per titolo originale
        GridPane booksGrid = new GridPane();
        booksGrid.setHgap(20); // Spazio orizzontale
        booksGrid.setVgap(25); // Spazio verticale
        booksGrid.setPadding(new Insets(20, 0, 0, 0));

        // ✅ COLONNE FISSE come nell'originale
        int columns = 6; // Come nella prima immagine

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            VBox bookCard = createOriginalStyleBookCard(book, clickHandler);

            int col = i % columns;
            int row = i / columns;
            booksGrid.add(bookCard, col, row);
        }

        section.getChildren().addAll(titleLabel, countLabel, booksGrid);
        return section;
    }

    /**
     * ✅ NUOVO: Card libro identica allo stile originale della ricerca per titolo
     */
    private VBox createOriginalStyleBookCard(Book book, Consumer<Book> clickHandler) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-cursor: hand;");
        card.setPrefWidth(160); // Larghezza come nell'originale
        card.setMaxWidth(160);

        // ✅ ASSICURATI che il libro abbia un nome file immagine locale
        if (book.getImageUrl() == null || book.getImageUrl().trim().isEmpty()) {
            // Genera nome file basato su ISBN o titolo
            String imageFileName = generateImageFileName(book);
            book.setImageUrl(imageFileName);
        }

        // ✅ COPERTINA REALE usando ImageUtils come nella home
        StackPane coverContainer = new StackPane();
        coverContainer.setPrefWidth(140);
        coverContainer.setPrefHeight(190);
        coverContainer.setMaxWidth(140);
        coverContainer.setMaxHeight(190);

        // ✅ USA ImageUtils per caricare l'immagine reale
        ImageView cover = ImageUtils.createSafeImageView(book.getImageUrl(), 140, 190);

        // Clip con angoli arrotondati
        Rectangle clip = new Rectangle(140, 190);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        cover.setClip(clip);

        // Ombra per effetto profondità
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        cover.setEffect(shadow);

        coverContainer.getChildren().add(cover);

        // ✅ TESTO sotto la copertina come nell'originale
        VBox textContainer = new VBox(3);
        textContainer.setAlignment(Pos.TOP_CENTER);
        textContainer.setMaxWidth(140);

        // Anno in piccolo sopra il titolo (se disponibile)
        if (book.getPublishYear() != null && !book.getPublishYear().isEmpty()) {
            Label yearLabel = new Label("(" + book.getPublishYear() + ")");
            yearLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
            yearLabel.setTextFill(Color.web("#888888"));
            yearLabel.setAlignment(Pos.CENTER);
            textContainer.getChildren().add(yearLabel);
        }

        // Titolo
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(140);
        titleLabel.setAlignment(Pos.TOP_CENTER);
        titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Autore
        Label authorLabel = new Label(book.getAuthor());
        authorLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
        authorLabel.setTextFill(Color.web("#CCCCCC"));
        authorLabel.setWrapText(true);
        authorLabel.setMaxWidth(140);
        authorLabel.setAlignment(Pos.TOP_CENTER);
        authorLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        textContainer.getChildren().addAll(titleLabel, authorLabel);

        card.getChildren().addAll(coverContainer, textContainer);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (clickHandler != null) {
                clickHandler.accept(book);
            }
        });

        // ✅ EFFETTI HOVER migliorati con scala dell'immagine
        card.setOnMouseEntered(e -> {
            // Effetto glow sulla copertina
            DropShadow glowShadow = new DropShadow();
            glowShadow.setRadius(15);
            glowShadow.setColor(Color.WHITE.deriveColor(0, 1, 1, 0.4));
            cover.setEffect(glowShadow);

            // Scala leggermente la card
            card.setStyle("-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        card.setOnMouseExited(e -> {
            // Ripristina ombra normale
            DropShadow normalShadow = new DropShadow();
            normalShadow.setRadius(8);
            normalShadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
            cover.setEffect(normalShadow);

            // Ripristina scala normale
            card.setStyle("-fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        return card;
    }

    /**
     * ✅ NUOVO: Genera nome file immagine basato su ISBN o titolo
     */
    private String generateImageFileName(Book book) {
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            // Pulisci l'ISBN e usa quello
            String cleanIsbn = book.getIsbn().replaceAll("[^a-zA-Z0-9]", "");
            if (cleanIsbn.length() > 0) {
                return cleanIsbn + ".jpg";
            }
        }

        if (book.getTitle() != null && !book.getTitle().trim().isEmpty()) {
            // Pulisci il titolo e usa quello
            String cleanTitle = book.getTitle().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (cleanTitle.length() > 20) {
                cleanTitle = cleanTitle.substring(0, 20); // Limita lunghezza
            }
            if (cleanTitle.length() > 0) {
                return cleanTitle + ".jpg";
            }
        }

        return "placeholder.jpg";
    }

    /**
     * ✅ AGGIORNATO: Gestisce i risultati della ricerca avanzata
     */
    public void showAdvancedSearchResults(AdvancedSearchPanel.SearchResult searchResult) {
        if (content == null || searchResult == null) {
            return;
        }

        System.out.println("🎯 Mostrando risultati ricerca avanzata: " + searchResult.getBooks().size() + " libri");

        content.getChildren().clear();

        // Salva risultati ricerca avanzata nella cache
        if (searchResult.getBooks() != null) {
            advancedSearchResults = new ArrayList<>(searchResult.getBooks());
            searchResults.clear(); // Pulisci ricerca normale
        }

        // Usa PopupManager handler
        Consumer<Book> popupManagerHandler = book -> handleBookClickWithPopupManager(book);

        // Usa sempre il metodo di fallback per evitare errori
        VBox resultsSection = createAdvancedSearchResultsSection(
                searchResult.getDescription(),
                searchResult.getBooks(),
                popupManagerHandler
        );
        content.getChildren().add(resultsSection);
    }

    /**
     * ✅ AGGIORNATO: Crea sezione risultati ricerca avanzata UNIFICATA
     */
    private VBox createAdvancedSearchResultsSection(String description, List<Book> books, Consumer<Book> clickHandler) {
        // ✅ USA LO STESSO LAYOUT della ricerca normale
        return createSearchResultsSection(description, books, clickHandler);
    }

    /**
     * ✅ COMPATIBILITÀ: Versione alternativa per backward compatibility
     */
    public void showSearchResults(AdvancedSearchPanel.SearchResult searchResult) {
        showAdvancedSearchResults(searchResult);
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

    private void showExplore() {
        System.out.println("🔍 Caricamento sezione Esplora");

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

            Label comingSoonLabel = new Label("🚧 In arrivo...");
            comingSoonLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            comingSoonLabel.setTextFill(Color.ORANGE);

            placeholderContainer.getChildren().addAll(titleLabel, descLabel, comingSoonLabel);
            content.getChildren().add(placeholderContainer);
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