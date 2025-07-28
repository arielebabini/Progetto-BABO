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

    // ✅ AGGIUNTO: Handler per i click delle categorie
    private Consumer<Category> categoryClickHandler;

    public BookSectionFactory(BookService bookService, boolean serverAvailable) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.gridBuilder = new BookGridBuilder();
        this.featuredBuilder = new FeaturedBookBuilder();
        this.categoryBuilder = new CategorySectionBuilder();

        // ✅ AGGIUNTO: Configura il category builder se il handler è già impostato
        if (this.categoryClickHandler != null) {
            this.categoryBuilder.setCategoryClickHandler(this.categoryClickHandler);
        }
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

    // ✅ AGGIUNTO: Setter per category click handler
    public void setCategoryClickHandler(Consumer<Category> handler) {
        this.categoryClickHandler = handler;
        if (this.categoryBuilder != null) {
            this.categoryBuilder.setCategoryClickHandler(handler);
        }
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

        Label statusLabel = new Label(serverAvailable ?
                "🔄 Caricamento..." : "🔌 Modalità offline - Dati limitati");
        statusLabel.setTextFill(serverAvailable ? Color.LIGHTGRAY : Color.ORANGE);
        statusLabel.setFont(Font.font("System", 12));

        VBox container = new VBox(10, title, loadingBox, statusLabel);
        container.setPadding(new Insets(15, 20, 20, 20));

        // Carica i contenuti asincroni
        loadBooksForSection(sectionType, container);

        return container;
    }

    public VBox createFeaturedSection() {
        Label title = new Label("⭐ In evidenza");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 15, 0));

        VBox container = new VBox(10);
        container.setPadding(new Insets(15, 20, 20, 20));
        container.getChildren().add(title);

        // Carica libro in evidenza asincrono
        bookService.getFeaturedBooksAsync()
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        // NUOVO: Notifica callback featured
                        if (featuredBooksCallback != null) {
                            featuredBooksCallback.accept(books);
                        }

                        if (!books.isEmpty()) {
                            VBox featuredCard = featuredBuilder.createFeaturedBookContent(books.get(0));
                            container.getChildren().add(featuredCard);
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
                            Label noResults = new Label("❌ Nessun risultato trovato per: \"" + query + "\"");
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
    private void loadBooksForSection(String sectionType, VBox container) {
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
                specificCallback = null;
                break;
        }

        future.thenAccept(books -> {
            Platform.runLater(() -> {
                // NUOVO: Notifica callback specifico
                if (specificCallback != null) {
                    specificCallback.accept(books);
                }

                container.getChildren().clear();

                // Ricrea il titolo
                String titleText = switch (sectionType) {
                    case "free" -> "📚 Libri gratuiti";
                    case "new" -> "✨ Nuove uscite";
                    default -> "📖 Libri";
                };

                Label title = new Label(titleText);
                title.setFont(Font.font("System", FontWeight.BOLD, 22));
                title.setTextFill(Color.WHITE);
                title.setPadding(new Insets(0, 0, 8, 0));
                container.getChildren().add(title);

                FlowPane bookGrid = gridBuilder.createOptimizedBookGrid();
                gridBuilder.populateBookGrid(books, bookGrid, null);

                ScrollPane scroll = new ScrollPane(bookGrid);
                scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setPannable(true);
                scroll.setFitToHeight(true);
                scroll.setPrefHeight(280);
                scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

                container.getChildren().add(scroll);

                // Callback generale
                if (cachedBooksCallback != null) {
                    cachedBooksCallback.accept(books);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> showSectionError(container, sectionType));
            return null;
        });
    }

    private VBox createSearchResultsSection(List<Book> results, Consumer<Book> clickHandler, String query) {
        Label title = new Label("🔍 Risultati per: \"" + query + "\" (" + results.size() + " trovati)");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 15, 0));

        FlowPane bookGrid = gridBuilder.createOptimizedBookGrid();
        gridBuilder.populateBookGrid(results, bookGrid, null);

        ScrollPane scroll = new ScrollPane(bookGrid);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToHeight(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox section = new VBox(15, title, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));

        return section;
    }

    private HBox createSectionHeader(String sectionTitle) {
        Label title = new Label(sectionTitle);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        Button seeAllBtn = new Button("Vedi tutti");
        seeAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0a84ff; -fx-border-color: transparent; -fx-cursor: hand;");

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
        categories.add(new Category("Murder", "", ""));
        categories.add(new Category("Biography", "", ""));
        categories.add(new Category("Education", "", ""));
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

    private void showSectionError(VBox container, String sectionType) {
        Label errorLabel = new Label("❌ Errore di connessione");
        errorLabel.setTextFill(Color.LIGHTCORAL);
        errorLabel.setFont(Font.font("System", 14));

        Label retryLabel = new Label("🔄 Verifica la connessione al server");
        retryLabel.setTextFill(Color.GRAY);
        retryLabel.setFont(Font.font("System", 12));

        VBox errorBox = new VBox(10, errorLabel, retryLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(280);

        container.getChildren().clear();
        container.getChildren().add(errorBox);
    }

    private void showNoSearchResults(VBox content, String query) {
        Label noResults = new Label("❌ Nessun risultato trovato per: \"" + query + "\"");
        noResults.setTextFill(Color.WHITE);
        noResults.setFont(Font.font("System", FontWeight.NORMAL, 18));

        Label suggestion = new Label("💡 Prova con parole chiave diverse");
        suggestion.setTextFill(Color.GRAY);
        suggestion.setFont(Font.font("System", FontWeight.NORMAL, 14));

        VBox noResultsBox = new VBox(10, noResults, suggestion);
        noResultsBox.setAlignment(Pos.CENTER);
        noResultsBox.setPadding(new Insets(50));

        content.getChildren().add(noResultsBox);
    }
}