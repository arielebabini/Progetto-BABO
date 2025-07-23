package org.BABO.client.ui;

import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Pannello per la ricerca avanzata dei libri
 * Supporta ricerca per: Titolo, Autore, Autore-Anno
 */
public class AdvancedSearchPanel extends VBox {

    // Colori e stili principali
    private static final String BG_COLOR = "#1e1e1e";
    private static final String BG_CONTROL = "#2b2b2b";
    private static final String ACCENT_COLOR = "#4a86e8";
    private static final String TEXT_COLOR = "#ffffff";
    private static final String HINT_COLOR = "#9e9e9e";

    private final BookService bookService;
    private ComboBox<String> searchTypeCombo;
    private TextField searchField;
    private TextField authorField;
    private TextField yearField;
    private VBox dynamicFieldsContainer;
    private Consumer<SearchResult> onSearchExecuted;

    public AdvancedSearchPanel(BookService bookService) {
        this.bookService = bookService;
        setupPanel();
        createComponents();
        setupEventHandlers();
    }

    private void setupPanel() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 4);" +
                        "-fx-background-radius: 12px;"
        );

        setMinWidth(400);
        setMinHeight(350);
        setMaxWidth(500);
    }

    private void createComponents() {
        // Titolo del pannello
        Label titleLabel = new Label("🔍 Ricerca Avanzata");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        titleLabel.setPadding(new Insets(0, 0, 15, 0));

        // Tipo di ricerca
        Label searchTypeLabel = new Label("📋 Tipo di ricerca:");
        searchTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        searchTypeLabel.setTextFill(Color.web(TEXT_COLOR));

        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll(
                "🏷️ Per Titolo",
                "👤 Per Autore",
                "👤📅 Per Autore e Anno"
        );
        searchTypeCombo.setValue("🏷️ Per Titolo");
        styleComboBox(searchTypeCombo);

        // Container per i campi dinamici
        dynamicFieldsContainer = new VBox(10);
        dynamicFieldsContainer.setPadding(new Insets(10, 0, 0, 0));

        // Pulsanti
        HBox buttonBox = createButtonBox();

        // Aggiungi tutti i componenti
        getChildren().addAll(
                titleLabel,
                searchTypeLabel,
                searchTypeCombo,
                dynamicFieldsContainer,
                buttonBox
        );

        // Inizializza con ricerca per titolo
        updateDynamicFields();
    }

    private void updateDynamicFields() {
        dynamicFieldsContainer.getChildren().clear();

        String selectedType = searchTypeCombo.getValue();

        if (selectedType.contains("Titolo")) {
            createTitleSearchFields();
        } else if (selectedType.contains("Autore e Anno")) {
            createAuthorYearSearchFields();
        } else if (selectedType.contains("Autore")) {
            createAuthorSearchFields();
        }
    }

    private void createTitleSearchFields() {
        Label titleLabel = new Label("📖 Titolo del libro:");
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));

        searchField = new TextField();
        searchField.setPromptText("Inserisci il titolo del libro...");
        styleTextField(searchField);

        Label hintLabel = new Label("💡 Suggerimento: Puoi inserire anche solo una parte del titolo");
        hintLabel.setFont(Font.font("System", 12));
        hintLabel.setTextFill(Color.web(HINT_COLOR));
        hintLabel.setWrapText(true);

        dynamicFieldsContainer.getChildren().addAll(titleLabel, searchField, hintLabel);
    }

    private void createAuthorSearchFields() {
        Label authorLabel = new Label("👤 Nome dell'autore:");
        authorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        authorLabel.setTextFill(Color.web(TEXT_COLOR));

        authorField = new TextField();
        authorField.setPromptText("Inserisci nome e cognome dell'autore...");
        styleTextField(authorField);

        Label hintLabel = new Label("💡 Suggerimento: Puoi cercare per nome, cognome o entrambi");
        hintLabel.setFont(Font.font("System", 12));
        hintLabel.setTextFill(Color.web(HINT_COLOR));
        hintLabel.setWrapText(true);

        dynamicFieldsContainer.getChildren().addAll(authorLabel, authorField, hintLabel);
    }

    private void createAuthorYearSearchFields() {
        Label authorLabel = new Label("👤 Nome dell'autore:");
        authorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        authorLabel.setTextFill(Color.web(TEXT_COLOR));

        authorField = new TextField();
        authorField.setPromptText("Inserisci nome e cognome dell'autore...");
        styleTextField(authorField);

        Label yearLabel = new Label("📅 Anno di pubblicazione:");
        yearLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        yearLabel.setTextFill(Color.web(TEXT_COLOR));

        yearField = new TextField();
        yearField.setPromptText("YYYY (es. 2023)");
        styleTextField(yearField);

        // Validazione anno
        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,4}")) {
                yearField.setText(oldValue);
            }
        });

        Label hintLabel = new Label("💡 Suggerimento: Trova libri di un autore pubblicati in un anno specifico");
        hintLabel.setFont(Font.font("System", 12));
        hintLabel.setTextFill(Color.web(HINT_COLOR));
        hintLabel.setWrapText(true);

        dynamicFieldsContainer.getChildren().addAll(
                authorLabel, authorField, yearLabel, yearField, hintLabel
        );
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button searchButton = new Button("🔍 CERCA");
        styleActionButton(searchButton, ACCENT_COLOR);
        searchButton.setOnAction(e -> executeSearch());

        Button clearButton = new Button("🧹 PULISCI");
        styleActionButton(clearButton, "#666666");
        clearButton.setOnAction(e -> clearFields());

        buttonBox.getChildren().addAll(searchButton, clearButton);
        return buttonBox;
    }

    private void executeSearch() {
        String searchType = searchTypeCombo.getValue();
        String query = buildSearchQuery(searchType);

        if (query.trim().isEmpty()) {
            showAlert("⚠️ Attenzione", "Inserisci almeno un parametro di ricerca!");
            return;
        }

        // Mostra indicatore di caricamento
        showSearchProgress();

        // Esegui ricerca in base al tipo
        if (searchType.contains("Titolo")) {
            searchByTitle(searchField.getText().trim());
        } else if (searchType.contains("Autore e Anno")) {
            searchByAuthorAndYear(authorField.getText().trim(), yearField.getText().trim());
        } else if (searchType.contains("Autore")) {
            searchByAuthor(authorField.getText().trim());
        }
    }

    private String buildSearchQuery(String searchType) {
        if (searchType.contains("Titolo")) {
            return searchField != null ? searchField.getText().trim() : "";
        } else if (searchType.contains("Autore e Anno")) {
            String author = authorField != null ? authorField.getText().trim() : "";
            String year = yearField != null ? yearField.getText().trim() : "";
            return author + " " + year;
        } else if (searchType.contains("Autore")) {
            return authorField != null ? authorField.getText().trim() : "";
        }
        return "";
    }

    private void searchByTitle(String title) {
        System.out.println("🔍 Ricerca per titolo: \"" + title + "\"");

        // Usa endpoint specifico per titolo
        bookService.searchBooksByTitleAsync(title)
                .thenAccept(books -> {
                    System.out.println("📚 Risultati ricerca titolo: " + books.size() + " libri trovati");
                    Platform.runLater(() -> {
                        hideSearchProgress();
                        if (onSearchExecuted != null) {
                            onSearchExecuted.accept(new SearchResult(
                                    "Ricerca per titolo: \"" + title + "\"",
                                    books,
                                    SearchResult.SearchType.TITLE
                            ));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Errore ricerca titolo: " + throwable.getMessage());
                    Platform.runLater(() -> {
                        hideSearchProgress();
                        showAlert("❌ Errore", "Errore durante la ricerca per titolo: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void searchByAuthor(String author) {
        System.out.println("🔍 Ricerca per autore: \"" + author + "\"");

        // Usa endpoint specifico per autore
        bookService.searchBooksByAuthorAsync(author)
                .thenAccept(books -> {
                    System.out.println("📚 Risultati ricerca autore: " + books.size() + " libri trovati");
                    Platform.runLater(() -> {
                        hideSearchProgress();
                        if (onSearchExecuted != null) {
                            onSearchExecuted.accept(new SearchResult(
                                    "Ricerca per autore: \"" + author + "\"",
                                    books,
                                    SearchResult.SearchType.AUTHOR
                            ));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Errore ricerca autore: " + throwable.getMessage());
                    Platform.runLater(() -> {
                        hideSearchProgress();
                        showAlert("❌ Errore", "Errore durante la ricerca per autore: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void searchByAuthorAndYear(String author, String year) {
        System.out.println("🔍 Ricerca per autore e anno: \"" + author + "\" (" + year + ")");

        if (author.trim().isEmpty()) {
            showAlert("⚠️ Attenzione", "Inserisci almeno il nome dell'autore!");
            hideSearchProgress();
            return;
        }

        // Usa endpoint specifico per autore-anno con parametri separati
        System.out.println("📡 Chiamata endpoint: /search/author-year?author=" + author + "&year=" + year);

        bookService.searchBooksByAuthorAndYearAsync(author.trim(), year.trim())
                .thenAccept(books -> {
                    System.out.println("📚 Risultati ricerca autore-anno: " + books.size() + " libri trovati");

                    Platform.runLater(() -> {
                        hideSearchProgress();
                        if (onSearchExecuted != null) {
                            String description;
                            if (year.trim().isEmpty()) {
                                description = "Ricerca per autore: \"" + author + "\"";
                            } else {
                                description = "Ricerca per autore: \"" + author + "\" nell'anno " + year;
                            }

                            onSearchExecuted.accept(new SearchResult(
                                    description,
                                    books,
                                    SearchResult.SearchType.AUTHOR_YEAR
                            ));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Errore ricerca autore-anno: " + throwable.getMessage());
                    Platform.runLater(() -> {
                        hideSearchProgress();
                        showAlert("❌ Errore", "Errore durante la ricerca autore-anno: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void clearFields() {
        if (searchField != null) searchField.clear();
        if (authorField != null) authorField.clear();
        if (yearField != null) yearField.clear();
    }

    private void showSearchProgress() {
        searchTypeCombo.setDisable(true);
        dynamicFieldsContainer.setDisable(true);
    }

    private void hideSearchProgress() {
        searchTypeCombo.setDisable(false);
        dynamicFieldsContainer.setDisable(false);
    }

    private void setupEventHandlers() {
        searchTypeCombo.setOnAction(e -> updateDynamicFields());

        setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                executeSearch();
            }
        });
    }

    private void styleComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-padding: 8px;" +
                        "-fx-font-size: 14px;"
        );
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-prompt-text-fill: " + HINT_COLOR + ";" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;"
        );
        field.setMaxWidth(Double.MAX_VALUE);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: " + BG_CONTROL + ";" +
                                "-fx-text-fill: " + TEXT_COLOR + ";" +
                                "-fx-prompt-text-fill: " + HINT_COLOR + ";" +
                                "-fx-background-radius: 8px;" +
                                "-fx-border-color: " + ACCENT_COLOR + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-border-width: 2px;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: " + BG_CONTROL + ";" +
                                "-fx-text-fill: " + TEXT_COLOR + ";" +
                                "-fx-prompt-text-fill: " + HINT_COLOR + ";" +
                                "-fx-background-radius: 8px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;"
                );
            }
        });
    }

    private void styleActionButton(Button button, String bgColor) {
        button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 12px 20px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );
        button.setMaxWidth(Double.MAX_VALUE);

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: derive(" + bgColor + ", 20%);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px 20px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: " + bgColor + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px 20px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;"
                )
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";"
        );

        alert.showAndWait();
    }

    public void setOnSearchExecuted(Consumer<SearchResult> callback) {
        this.onSearchExecuted = callback;
    }

    public static class SearchResult {
        public enum SearchType { TITLE, AUTHOR, AUTHOR_YEAR }

        private final String description;
        private final java.util.List<Book> books;
        private final SearchType type;

        public SearchResult(String description, java.util.List<Book> books, SearchType type) {
            this.description = description;
            this.books = books;
            this.type = type;
        }

        public String getDescription() { return description; }
        public java.util.List<Book> getBooks() { return books; }
        public SearchType getType() { return type; }
    }
}