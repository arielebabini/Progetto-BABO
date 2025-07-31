package org.BABO.client.ui;

import org.BABO.client.service.BookService;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Factory per creare le varie sezioni di libri
 * AGGIORNATO: Supporta layout fisso a 8 colonne per sezioni "Libri gratuiti" e "Nuove uscite"
 */
public class BookSectionFactory {

    private final BookService bookService;
    private final boolean serverAvailable;
    private Consumer<Book> bookClickHandler;
    private BookGridBuilder gridBuilder;
    private FeaturedBookBuilder featuredBuilder;
    private CategorySectionBuilder categoryBuilder;
    private Consumer<List<Book>> cachedBooksCallback;

    // Callback per sezioni specifiche
    private Consumer<List<Book>> featuredBooksCallback;
    private Consumer<List<Book>> freeBooksCallback;
    private Consumer<List<Book>> newBooksCallback;
    private Consumer<List<Book>> searchResultsCallback;

    // AGGIUNTO: Callback per gestire click su categorie
    private Consumer<Category> categoryClickHandler;

    public BookSectionFactory(BookService bookService, boolean serverAvailable) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.gridBuilder = new BookGridBuilder();
        this.featuredBuilder = new FeaturedBookBuilder();
        this.categoryBuilder = new CategorySectionBuilder();
    }

    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
        this.gridBuilder.setBookClickHandler(handler);
        this.featuredBuilder.setBookClickHandler(handler);
    }

    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        this.cachedBooksCallback = callback;
        this.gridBuilder.setCachedBooksCallback(callback);
    }

    // AGGIUNTO: Setter per callback categoria
    public void setCategoryClickHandler(Consumer<Category> handler) {
        this.categoryClickHandler = handler;
        if (this.categoryBuilder != null) {
            this.categoryBuilder.setCategoryClickHandler(handler);
        }
    }

    // Setter per callback sezioni
    public void setFeaturedBooksCallback(Consumer<List<Book>> callback) {
        this.featuredBooksCallback = callback;
    }

    public void setFreeBooksCallback(Consumer<List<Book>> callback) {
        this.freeBooksCallback = callback;
    }

    public void setNewBooksCallback(Consumer<List<Book>> callback) {
        this.newBooksCallback = callback;
    }

    public void setSearchResultsCallback(Consumer<List<Book>> callback) {
        this.searchResultsCallback = callback;
    }

    /**
     * RIPRISTINATO: Crea sezioni con layout FlowPane originale per tutte le sezioni
     */
    public VBox createBookSection(String sectionTitle, String sectionType) {
        Label title = new Label(sectionTitle);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);

        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(280);

        Label statusLabel = new Label(serverAvailable ?
                "📡 Online" : "📴 Offline");
        statusLabel.setTextFill(serverAvailable ? Color.LIGHTGREEN : Color.ORANGE);
        statusLabel.setFont(Font.font("System", 12));
        loadingBox.getChildren().add(statusLabel);

        // RIPRISTINATO: Usa sempre FlowPane per tutte le sezioni
        ScrollPane scroll = createScrollPane();
        scroll.setContent(loadingBox);

        Button seeAllBtn = createSeeAllButton();
        HBox headerBox = createSectionHeader(title, seeAllBtn);

        VBox section = new VBox(5, headerBox, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));
        section.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        // RIPRISTINATO: Tutte le sezioni usano FlowPane
        loadBooksForSectionFlowPane(sectionType, scroll);

        return section;
    }

    /**
     * RIPRISTINATO: Carica dati per sezioni con layout FlowPane per tutte le sezioni
     */
    private void loadBooksForSectionFlowPane(String sectionType, ScrollPane scroll) {
        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(10));
        bookGrid.setPrefWrapLength(750);

        CompletableFuture<List<Book>> future;
        Consumer<List<Book>> specificCallback;

        switch (sectionType) {
            case "free":
                future = bookService.getFreeBooksAsync(); // Già restituisce 8 libri
                specificCallback = freeBooksCallback;
                break;
            case "new":
                future = bookService.getNewReleasesAsync(); // Già restituisce 8 libri
                specificCallback = newBooksCallback;
                break;
            case "featured":
                future = bookService.getFeaturedBooksAsync();
                specificCallback = featuredBooksCallback;
                break;
            default:
                future = bookService.getAllBooksAsync();
                specificCallback = null;
                break;
        }

        future.thenAccept(books -> {
            Platform.runLater(() -> {
                System.out.println("✅ Caricati " + books.size() + " libri per sezione " + sectionType + " (layout FlowPane adattivo)");

                // Prima popola la griglia visiva
                gridBuilder.populateBookGrid(books, bookGrid, scroll);
                scroll.setContent(bookGrid);

                // CORREZIONE CRITICA: Assicurati che il callback specifico venga chiamato con TUTTI i libri caricati
                if (specificCallback != null) {
                    // Crea una copia difensiva completa di TUTTI i libri ricevuti
                    List<Book> navigationBooks = new ArrayList<>(books);
                    specificCallback.accept(navigationBooks);
                    System.out.println("📋 ✅ Callback specifico chiamato per '" + sectionType + "' con " + navigationBooks.size() + " libri per navigazione");
                } else {
                    System.out.println("⚠️ Callback specifico è null per sezione: " + sectionType);
                }

                // Callback generale per backward compatibility
                if (cachedBooksCallback != null) {
                    cachedBooksCallback.accept(books);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                System.err.println("❌ Errore caricamento sezione " + sectionType + ": " + throwable.getMessage());
                showSectionError(scroll, sectionType);

                // Anche in caso di errore, chiama il callback con lista vuota per evitare inconsistenze
                if (specificCallback != null) {
                    specificCallback.accept(new ArrayList<>());
                    System.out.println("⚠️ Callback specifico chiamato con lista vuota per " + sectionType + " a causa di errore");
                }
            });
            return null;
        });
    }

    /**
     * ORIGINALE: Crea sezione in evidenza (mantiene comportamento originale)
     */
    public VBox createFeaturedSection() {
        VBox container = new VBox();
        container.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3a3a3c, #2c2c2c);" +
                        "-fx-background-radius: 12;"
        );

        // Loading state
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(300);

        Label loadingLabel = new Label("⭐ Caricamento libro in evidenza...");
        loadingLabel.setTextFill(Color.WHITE);
        loadingBox.getChildren().add(loadingLabel);

        container.getChildren().add(loadingBox);

        // Load featured book with callback
        bookService.getFeaturedBooksAsync()
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        // Notifica callback
                        if (featuredBooksCallback != null) {
                            featuredBooksCallback.accept(books);
                        }

                        if (!books.isEmpty()) {
                            VBox featuredContent = featuredBuilder.createFeaturedBookContent(books.get(0));
                            container.getChildren().clear();
                            container.getChildren().add(featuredContent);
                        } else {
                            showNoFeaturedMessage(container);
                        }

                        // Callback generale per backward compatibility
                        if (cachedBooksCallback != null) {
                            cachedBooksCallback.accept(books);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showErrorMessage(container, "❌ Errore caricamento libro in evidenza"));
                    return null;
                });

        return container;
    }

    /**
     * ORIGINALE: Carica categorie in modo asincrono
     */
    public void loadCategoriesAsync(VBox content) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return createDefaultCategories();
            } catch (Exception e) {
                System.err.println("Errore caricamento categorie: " + e.getMessage());
                return new ArrayList<Category>();
            }
        }).thenAccept(categories -> {
            Platform.runLater(() -> {
                if (!categories.isEmpty()) {
                    try {
                        VBox categorySection = categoryBuilder.createCategorySection("🎭 Scopri per genere", categories);
                        content.getChildren().add(categorySection);
                    } catch (Exception e) {
                        System.err.println("Errore creazione sezione categorie: " + e.getMessage());
                    }
                }
            });
        });
    }

    /**
     * Crea sezione risultati ricerca (usa FlowPane)
     */
    public VBox createSearchResultsSection(List<Book> searchResults, Consumer<Book> onBookClick, String query) {
        Label title = new Label("🔍 Risultati per \"" + query + "\" (" + searchResults.size() + ")");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        // Per i risultati di ricerca usa FlowPane
        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(10));
        bookGrid.setPrefWrapLength(750);

        gridBuilder.populateBookGrid(searchResults, bookGrid, null);

        ScrollPane scroll = new ScrollPane(bookGrid);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox section = new VBox(5, title, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));
        section.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        return section;
    }

    /**
     * ORIGINALE: Esegue ricerca con callback
     */
    public void performSearch(String query, VBox content, Consumer<Book> clickHandler) {
        content.getChildren().clear();

        // Loading indicator
        Label loadingLabel = new Label("🔍 Ricerca in corso...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        bookService.searchBooksAsync(query)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();

                        // Notifica callback ricerca
                        if (searchResultsCallback != null) {
                            searchResultsCallback.accept(results);
                        }

                        if (results.isEmpty()) {
                            Label noResults = new Label("❌ Nessun risultato trovato per: " + query);
                            noResults.setFont(Font.font("System", FontWeight.NORMAL, 16));
                            noResults.setTextFill(Color.LIGHTGRAY);
                            content.getChildren().add(noResults);
                        } else {
                            VBox resultsSection = createSearchResultsSection(results, clickHandler, query);
                            content.getChildren().add(resultsSection);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore durante la ricerca: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    // Helper methods
    private ScrollPane createScrollPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(280);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    private Button createSeeAllButton() {
        Button seeAllBtn = new Button("Vedi tutti");
        seeAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0a84ff; -fx-border-color: transparent; -fx-cursor: hand;");
        return seeAllBtn;
    }

    private HBox createSectionHeader(Label title, Button seeAllBtn) {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(title, spacer, seeAllBtn);
        return headerBox;
    }

    /**
     * Mostra errore per sezioni FlowPane
     */
    private void showSectionError(ScrollPane scroll, String sectionType) {
        Label errorLabel = new Label("❌ Errore nel caricamento di " + sectionType);
        errorLabel.setTextFill(Color.web("#FF6B6B"));
        errorLabel.setFont(Font.font("System", 16));

        Button retryButton = new Button("🔄 Riprova");
        retryButton.setStyle(
                "-fx-background-color: #FF6B6B;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        VBox errorBox = new VBox(10, errorLabel, retryButton);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(280);

        scroll.setContent(errorBox);
    }

    /**
     * ORIGINALI: Metodi helper mantenuti dal codice originale
     */
    private void showNoFeaturedMessage(VBox container) {
        Label noFeatured = new Label("📚 Nessun libro in evidenza disponibile");
        noFeatured.setTextFill(Color.GRAY);
        noFeatured.setFont(Font.font("System", 16));

        VBox noFeaturedBox = new VBox(noFeatured);
        noFeaturedBox.setAlignment(Pos.CENTER);
        noFeaturedBox.setPrefHeight(300);

        container.getChildren().clear();
        container.getChildren().add(noFeaturedBox);
    }

    private void showErrorMessage(VBox container, String message) {
        Label errorLabel = new Label(message);
        errorLabel.setTextFill(Color.web("#FF6B6B"));
        errorLabel.setFont(Font.font("System", 16));

        VBox errorBox = new VBox(errorLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(300);

        container.getChildren().clear();
        container.getChildren().add(errorBox);
    }

    /**
     * CORRETTO: Crea categorie di default usando il costruttore corretto
     */
    private List<Category> createDefaultCategories() {
        List<Category> categories = new ArrayList<>();

        // Creo categorie di esempio usando il costruttore corretto:
        // Category(Long id, String name, String description, String imageUrl, String iconPath)
        try {
            categories.add(new Category(1L, "Fiction", "Romanzi e narrativa", "placeholder.jpg", "fiction-icon.png"));
            categories.add(new Category(2L, "Science Fiction", "Fantascienza e fantasy", "placeholder.jpg", "sci-fi-icon.png"));
            categories.add(new Category(3L, "Mystery", "Gialli e thriller", "placeholder.jpg", "mystery-icon.png"));
            categories.add(new Category(4L, "Romance", "Romanzi rosa", "placeholder.jpg", "romance-icon.png"));
            categories.add(new Category(5L, "Biography", "Biografie e autobiografie", "placeholder.jpg", "biography-icon.png"));
        } catch (Exception e) {
            System.err.println("⚠️ Errore creazione categorie default: " + e.getMessage());
            // Ritorna lista vuota se c'è un errore
        }

        return categories;
    }

    /**
     * RIPRISTINATO: Aggiorna una sezione esistente con layout FlowPane per tutte
     */
    public void updateSection(VBox section, List<Book> books, String sectionType) {
        if (section.getChildren().size() < 2) return;

        ScrollPane scroll = (ScrollPane) section.getChildren().get(1);

        // RIPRISTINATO: Usa sempre FlowPane per tutte le sezioni
        FlowPane bookGrid = gridBuilder.createOptimizedBookGrid();
        gridBuilder.populateBookGrid(books, bookGrid, scroll);
        scroll.setContent(bookGrid);
    }
}