package org.BABO.client.ui.Search;

import org.BABO.client.ui.Book.BookGridBuilder;
import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.function.Consumer;

/**
 * Gestore avanzato per le funzionalità di ricerca
 */
public class SearchManager {

    private final BookService bookService;
    private final BookGridBuilder gridBuilder;
    private String lastSearchQuery = "";
    private boolean isSearching = false;

    public SearchManager(BookService bookService) {
        this.bookService = bookService;
        this.gridBuilder = new BookGridBuilder();
    }

    public void setBookClickHandler(Consumer<Book> handler) {
        this.gridBuilder.setBookClickHandler(handler);
    }

    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        this.gridBuilder.setCachedBooksCallback(callback);
    }

    /**
     * Esegue una ricerca con gestione dello stato
     */
    public void performSearch(String query, VBox content, Consumer<Book> clickHandler) {
        // Evita ricerche duplicate
        if (query.equals(lastSearchQuery) && isSearching) {
            return;
        }

        lastSearchQuery = query;
        isSearching = true;

        // Mostra stato di caricamento
        showSearchLoading(content);

        bookService.searchBooksAsync(query.trim())
                .thenAccept(searchResults -> {
                    Platform.runLater(() -> {
                        isSearching = false;
                        content.getChildren().clear();

                        if (searchResults.isEmpty()) {
                            showNoSearchResults(content, query);
                        } else {
                            VBox searchSection = createSearchResultsSection(searchResults, clickHandler, query);
                            content.getChildren().add(searchSection);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        isSearching = false;
                        showSearchError(content, throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Ricerca con filtri avanzati
     */
    public void performAdvancedSearch(String query, String author, String category, VBox content, Consumer<Book> clickHandler) {
        // TODO: Implementare ricerca avanzata quando il server la supporterà
        // Per ora usa la ricerca normale
        performSearch(query, content, clickHandler);
    }

    /**
     * Mostra lo stato di caricamento della ricerca
     */
    private void showSearchLoading(VBox content) {
        ProgressIndicator searchLoading = new ProgressIndicator();
        searchLoading.setPrefSize(50, 50);

        VBox searchLoadingBox = new VBox(searchLoading);
        searchLoadingBox.setAlignment(Pos.CENTER);
        searchLoadingBox.setPrefHeight(200);

        Label searchingLabel = new Label("🔍 Ricerca in corso...");
        searchingLabel.setTextFill(Color.WHITE);
        searchingLabel.setFont(Font.font("System", 14));
        searchLoadingBox.getChildren().add(searchingLabel);

        content.getChildren().clear();
        content.getChildren().add(searchLoadingBox);
    }

    /**
     * Crea la sezione con i risultati della ricerca
     */
    private VBox createSearchResultsSection(List<Book> searchResults, Consumer<Book> onBookClick, String query) {
        Label title = new Label("🔍 Risultati per \"" + query + "\" (" + searchResults.size() + ")");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 15, 0));

        // Statistiche di ricerca
        Label statsLabel = createSearchStats(searchResults);

        BookGridBuilder gridBuilder = new BookGridBuilder();
        gridBuilder.setBookClickHandler(onBookClick);

        FlowPane bookGrid = gridBuilder.createOptimizedBookGrid();

        gridBuilder.populateBookGrid(searchResults, bookGrid, null);

        VBox resultsContainer = new VBox(10);
        resultsContainer.getChildren().addAll(title, statsLabel, bookGrid);
        resultsContainer.setPadding(new Insets(15, 20, 20, 20));
        resultsContainer.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        return resultsContainer;
    }

    /**
     * Crea le statistiche della ricerca
     */
    private Label createSearchStats(List<Book> results) {
        long uniqueAuthors = results.stream()
                .map(Book::getAuthor)
                .distinct()
                .count();

        String statsText = String.format("📊 %d libri • %d autori", results.size(), uniqueAuthors);

        Label statsLabel = new Label(statsText);
        statsLabel.setTextFill(Color.gray(0.7));
        statsLabel.setFont(Font.font("System", 12));

        return statsLabel;
    }

    /**
     * Mostra messaggio quando non ci sono risultati
     */
    private void showNoSearchResults(VBox content, String query) {
        Label noResults = new Label("❌ Nessun risultato trovato per: \"" + query + "\"");
        noResults.setTextFill(Color.WHITE);
        noResults.setFont(Font.font("System", FontWeight.NORMAL, 18));

        Label suggestion = new Label("💡 Suggerimenti:");
        suggestion.setTextFill(Color.gray(0.7));
        suggestion.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label tips = new Label(
                "• Controlla l'ortografia\n" +
                        "• Prova parole chiave diverse\n" +
                        "• Usa meno parole chiave\n" +
                        "• Prova con il nome dell'autore"
        );
        tips.setTextFill(Color.gray(0.7));
        tips.setFont(Font.font("System", 12));

        VBox noResultsBox = new VBox(15, noResults, suggestion, tips);
        noResultsBox.setAlignment(Pos.CENTER);
        noResultsBox.setPrefHeight(250);
        noResultsBox.setPadding(new Insets(20));
        noResultsBox.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        content.getChildren().add(noResultsBox);
    }

    /**
     * Mostra errore di ricerca
     */
    private void showSearchError(VBox content, String errorMessage) {
        Label errorLabel = new Label("❌ Errore durante la ricerca");
        errorLabel.setTextFill(Color.LIGHTCORAL);
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label errorDetail = new Label("Dettagli: " + errorMessage);
        errorDetail.setTextFill(Color.GRAY);
        errorDetail.setFont(Font.font("System", 12));
        errorDetail.setWrapText(true);

        Label retryLabel = new Label("🔄 Verifica la connessione al server e riprova");
        retryLabel.setTextFill(Color.GRAY);
        retryLabel.setFont(Font.font("System", 12));

        VBox errorBox = new VBox(10, errorLabel, errorDetail, retryLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(200);
        errorBox.setPadding(new Insets(20));
        errorBox.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #3c2c2c, #2e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        content.getChildren().add(errorBox);
    }

    /**
     * Pulisce lo stato della ricerca
     */
    public void clearSearch() {
        lastSearchQuery = "";
        isSearching = false;
    }

    /**
     * Verifica se una ricerca è in corso
     */
    public boolean isSearching() {
        return isSearching;
    }

    /**
     * Ottiene l'ultima query di ricerca
     */
    public String getLastSearchQuery() {
        return lastSearchQuery;
    }
}