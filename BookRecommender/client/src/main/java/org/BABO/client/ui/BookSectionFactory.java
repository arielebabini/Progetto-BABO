package org.BABO.client.ui;

import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
 */
public class BookSectionFactory {

    private final BookService bookService;
    private final boolean serverAvailable;
    private Consumer<Book> bookClickHandler;
    private BookGridBuilder gridBuilder;
    private FeaturedBookBuilder featuredBuilder;
    private CategorySectionBuilder categoryBuilder;
    private Consumer<List<Book>> cachedBooksCallback;

    // NUOVI: Callback per sezioni specifiche
    private Consumer<List<Book>> featuredBooksCallback;
    private Consumer<List<Book>> freeBooksCallback;
    private Consumer<List<Book>> newBooksCallback;
    private Consumer<List<Book>> searchResultsCallback;

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

    // NUOVI: Setter per callback sezioni
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

    // AGGIORNATO: Metodo createBookSection con callback specifici
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

        Label statusLabel = new Label(serverAvailable ? "📡 Online" : "📴 Offline");
        statusLabel.setTextFill(serverAvailable ? Color.LIGHTGREEN : Color.ORANGE);
        statusLabel.setFont(Font.font("System", 12));
        loadingBox.getChildren().add(statusLabel);

        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(10));
        bookGrid.setPrefWrapLength(750);

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

        // Load books async with callbacks
        loadBooksForSection(sectionType, bookGrid, scroll);

        return section;
    }

    // AGGIORNATO: Metodo createFeaturedSection con callback
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
                        // NUOVO: Notifica callback
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

    // AGGIORNATO: Metodo performSearch con callback
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

                        // NUOVO: Notifica callback ricerca
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

    // AGGIORNATO: Metodo loadBooksForSection con callback specifici
    private void loadBooksForSection(String sectionType, FlowPane bookGrid, ScrollPane scroll) {
        CompletableFuture<List<Book>> future;
        Consumer<List<Book>> specificCallback;

        switch (sectionType) {
            case "free":
                future = bookService.getFreeBooksAsync();
                specificCallback = freeBooksCallback;
                break;
            case "new":
                future = bookService.getNewReleasesAsync();
                specificCallback = newBooksCallback;
                break;
            case "featured":
                future = bookService.getFeaturedBooksAsync();
                specificCallback = featuredBooksCallback;
                break;
            default:
                future = bookService.getAllBooksAsync();
                specificCallback = null; // Categorie non hanno callback specifico
                break;
        }

        future.thenAccept(books -> {
            Platform.runLater(() -> {
                // NUOVO: Notifica callback specifico
                if (specificCallback != null) {
                    specificCallback.accept(books);
                }

                gridBuilder.populateBookGrid(books, bookGrid, scroll);

                // Callback generale
                if (cachedBooksCallback != null) {
                    cachedBooksCallback.accept(books);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                System.err.println("❌ Errore caricamento sezione " + sectionType + ": " + throwable.getMessage());
                showSectionError(scroll, sectionType);
            });
            return null;
        });
    }

    private VBox createSearchResultsSection(List<Book> searchResults, Consumer<Book> onBookClick, String query) {
        Label title = new Label("🔍 Risultati per \"" + query + "\" (" + searchResults.size() + ")");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

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
        headerBox.getChildren().addAll(title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, seeAllBtn);
        return headerBox;
    }

    private List<Category> createDefaultCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Thriller", "", ""));
        categories.add(new Category("Romance", "", ""));
        categories.add(new Category("Narrativa", "", ""));
        categories.add(new Category("Saggistica", "", ""));
        categories.add(new Category("Fantasy", "", ""));
        return categories;
    }

    private void showNoFeaturedMessage(VBox container) {
        Label noFeatured = new Label("❌ Nessun libro in evidenza disponibile");
        noFeatured.setTextFill(Color.WHITE);
        VBox noFeaturedBox = new VBox(noFeatured);
        noFeaturedBox.setAlignment(Pos.CENTER);
        noFeaturedBox.setPrefHeight(200);
        container.getChildren().clear();
        container.getChildren().add(noFeaturedBox);
    }

    private void showErrorMessage(VBox container, String message) {
        Label errorLabel = new Label(message);
        errorLabel.setTextFill(Color.LIGHTCORAL);
        VBox errorBox = new VBox(errorLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(200);
        container.getChildren().clear();
        container.getChildren().add(errorBox);
    }

    private void showSectionError(ScrollPane scroll, String sectionType) {
        Label errorLabel = new Label("❌ Errore di connessione");
        errorLabel.setTextFill(Color.LIGHTCORAL);
        errorLabel.setFont(Font.font("System", 14));

        Label retryLabel = new Label("🔄 Verifica la connessione al server");
        retryLabel.setTextFill(Color.GRAY);
        retryLabel.setFont(Font.font("System", 12));

        VBox errorBox = new VBox(10, errorLabel, retryLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(280);

        scroll.setContent(errorBox);
    }

    private void showNoSearchResults(VBox content, String query) {
        Label noResults = new Label("❌ Nessun risultato trovato per: \"" + query + "\"");
        noResults.setTextFill(Color.WHITE);
        noResults.setFont(Font.font("System", FontWeight.NORMAL, 18));

        Label suggestion = new Label("💡 Prova con parole chiave diverse");
        suggestion.setTextFill(Color.gray(0.7));
        suggestion.setFont(Font.font("System", 14));

        VBox noResultsBox = new VBox(10, noResults, suggestion);
        noResultsBox.setAlignment(Pos.CENTER);
        noResultsBox.setPrefHeight(200);
        content.getChildren().add(noResultsBox);
    }

    private void showSearchError(VBox content) {
        Label errorLabel = new Label("❌ Errore durante la ricerca");
        errorLabel.setTextFill(Color.LIGHTCORAL);
        errorLabel.setFont(Font.font("System", 16));

        Label errorDetail = new Label("Verifica la connessione al server");
        errorDetail.setTextFill(Color.GRAY);
        errorDetail.setFont(Font.font("System", 12));

        VBox errorBox = new VBox(10, errorLabel, errorDetail);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(200);
        content.getChildren().add(errorBox);
    }

    public VBox createAdvancedSearchResultsSection(String description, List<Book> results, Consumer<Book> onBookClick) {
        Label title = new Label(description);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 15, 0));

        if (results.isEmpty()) {
            return createNoAdvancedSearchResults(title);
        }

        // Statistiche di ricerca
        Label statsLabel = createSearchStats(results);

        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(15, 10, 10, 10));
        bookGrid.setPrefWrapLength(750);

        gridBuilder.populateBookGrid(results, bookGrid, null);

        ScrollPane scroll = new ScrollPane(bookGrid);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(500);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox section = new VBox(10, title, statsLabel, scroll);
        section.setPadding(new Insets(20));
        section.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        return section;
    }

    private VBox createNoAdvancedSearchResults(Label title) {
        Label noResults = new Label("❌ Nessun risultato trovato");
        noResults.setTextFill(Color.WHITE);
        noResults.setFont(Font.font("System", FontWeight.NORMAL, 18));

        Label suggestions = new Label(
                "💡 Suggerimenti:\n" +
                        "• Controlla l'ortografia\n" +
                        "• Prova termini di ricerca diversi\n" +
                        "• Usa meno parole chiave\n" +
                        "• Verifica che l'anno sia corretto"
        );
        suggestions.setTextFill(Color.gray(0.7));
        suggestions.setFont(Font.font("System", 12));

        VBox noResultsBox = new VBox(15, title, noResults, suggestions);
        noResultsBox.setAlignment(Pos.CENTER);
        noResultsBox.setPrefHeight(300);
        noResultsBox.setPadding(new Insets(20));
        noResultsBox.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        return noResultsBox;
    }

    private Label createSearchStats(List<Book> results) {
        long uniqueAuthors = results.stream()
                .map(Book::getAuthor)
                .distinct()
                .count();

        String statsText = String.format("📊 %d libri trovati • %d autori diversi", results.size(), uniqueAuthors);

        Label statsLabel = new Label(statsText);
        statsLabel.setTextFill(Color.gray(0.7));
        statsLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        return statsLabel;
    }

    // NUOVO: Metodo createBookCard (necessario per il codice incollato)
    private VBox createBookCard(Book book) {
        // Implementazione semplificata - dovrai adattarla alla tua logica esistente
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 8;");

        Label title = new Label(book.getTitle());
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label author = new Label(book.getAuthor());
        author.setTextFill(Color.LIGHTGRAY);
        author.setFont(Font.font("System", 12));

        card.getChildren().addAll(title, author);

        // Aggiungi click handler se presente
        if (bookClickHandler != null) {
            card.setOnMouseClicked(e -> bookClickHandler.accept(book));
            card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
        }

        return card;
    }
}