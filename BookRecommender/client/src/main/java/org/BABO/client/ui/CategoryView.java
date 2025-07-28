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
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Vista dettagliata per una categoria specifica
 * Mostra tutti i libri di una categoria con filtri e ordinamento
 * ✅ AGGIORNATO: Con caricamento libri per categoria
 */
public class CategoryView {

    private final BookService bookService;
    private final Category category;
    private Consumer<Book> bookClickHandler;
    private Consumer<Void> backHandler;
    private VBox mainContainer;
    private GridPane booksGrid;
    private ComboBox<String> sortComboBox;
    private TextField searchField;
    private List<Book> allCategoryBooks;
    private List<Book> filteredBooks;

    public CategoryView(BookService bookService, Category category) {
        this.bookService = bookService;
        this.category = category;
        this.allCategoryBooks = new ArrayList<>();
        this.filteredBooks = new ArrayList<>();
    }

    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
    }

    public void setBackHandler(Consumer<Void> handler) {
        this.backHandler = handler;
    }

    public Category getCategory() {
        return category;
    }

    public ScrollPane createCategoryView() {
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #1a1a1c;");

        VBox header = createCategoryHeader();
        mainContainer.getChildren().add(header);

        HBox filterBar = createFilterBar();
        mainContainer.getChildren().add(filterBar);

        booksGrid = new GridPane();
        booksGrid.setHgap(20);
        booksGrid.setVgap(20);
        booksGrid.setPadding(new Insets(20, 0, 0, 0));
        mainContainer.getChildren().add(booksGrid);

        loadCategoryBooks();

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a1a1c; -fx-background: #1a1a1c;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private VBox createCategoryHeader() {
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("← Torna a Esplora");
        backButton.setFont(Font.font("System", FontWeight.NORMAL, 14));
        backButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4a86e8;" +
                        "-fx-border-color: #4a86e8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15;"
        );

        backButton.setOnAction(e -> {
            if (backHandler != null) {
                backHandler.accept(null);
            }
        });

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getCategoryIcon(category.getName()));
        icon.setFont(Font.font("System", 36));

        VBox titleAndSubtitle = new VBox(5);
        Label title = new Label(category.getName());
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        String description = category.getDescription();
        if (description == null || description.isEmpty()) {
            description = "Esplora i libri di " + category.getName();
        }
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        descLabel.setTextFill(Color.LIGHTGRAY);
        descLabel.setWrapText(true);

        titleAndSubtitle.getChildren().addAll(title, descLabel);
        titleBox.getChildren().addAll(icon, titleAndSubtitle);

        Label statsLabel = new Label("Caricamento statistiche...");
        statsLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        statsLabel.setTextFill(Color.LIGHTGRAY);

        headerBox.getChildren().addAll(backButton, titleBox, statsLabel);

        updateCategoryStats(statsLabel);

        return headerBox;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(10, 0, 10, 0));

        searchField = new TextField();
        searchField.setPromptText("🔍 Cerca in " + category.getName() + "...");
        searchField.setPrefWidth(300);
        searchField.setStyle(
                "-fx-background-color: #2c2c2e;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #8e8e93;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 8 12;"
        );

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterBooks(newValue);
        });

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll(
                "📅 Più recenti",
                "📅 Più vecchi",
                "🔤 A-Z",
                "🔤 Z-A",
                "👤 Autore A-Z"
        );
        sortComboBox.setValue("📅 Più recenti");
        sortComboBox.setPrefWidth(150);
        sortComboBox.setStyle(
                "-fx-background-color: #2c2c2e;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;"
        );

        sortComboBox.setOnAction(e -> sortBooks());

        filterBar.getChildren().addAll(searchField, sortComboBox);
        return filterBar;
    }

    /**
     * ✅ AGGIORNATO: Carica i libri per questa categoria
     */
    private void loadCategoryBooks() {
        System.out.println("📚 Caricamento libri per categoria: " + category.getName());

        // Mostra indicatore di caricamento
        Label loadingLabel = new Label("📚 Caricamento libri...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);

        VBox loadingBox = new VBox(loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));

        booksGrid.getChildren().clear();
        booksGrid.add(loadingBox, 0, 0);

        // Carica libri asincrono
        bookService.getBooksByCategoryAsync(category.getName())
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        try {
                            this.allCategoryBooks = books != null ? books : new ArrayList<>();
                            this.filteredBooks = new ArrayList<>(this.allCategoryBooks);

                            System.out.println("✅ Caricati " + this.allCategoryBooks.size() + " libri per categoria '" + category.getName() + "'");

                            // Aggiorna la vista
                            updateBooksGrid();
                            updateBookCount();

                        } catch (Exception e) {
                            System.err.println("❌ Errore nell'aggiornamento vista categoria: " + e.getMessage());
                            showLoadingError();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.err.println("❌ Errore nel caricamento libri per categoria: " + throwable.getMessage());
                        showLoadingError();
                    });
                    return null;
                });
    }

    /**
     * ✅ AGGIORNATO: Aggiorna la griglia dei libri
     */
    private void updateBooksGrid() {
        booksGrid.getChildren().clear();

        if (filteredBooks.isEmpty()) {
            showNoBooksMessage();
            return;
        }

        // Crea griglia con i libri
        int columns = 4; // Numero di colonne
        int row = 0;
        int col = 0;

        for (Book book : filteredBooks) {
            VBox bookCard = createBookCard(book);
            booksGrid.add(bookCard, col, row);

            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * ✅ NUOVO: Crea una card per un libro
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(160);
        card.setMaxWidth(160);
        card.setStyle(
                "-fx-background-color: #2c2c2e;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;" +
                        "-fx-cursor: hand;"
        );

        // Immagine del libro
        ImageView bookImage = ImageUtils.createSafeImageView(book.getImageUrl(), 120, 160);
        bookImage.setStyle("-fx-background-radius: 4;");

        // Titolo
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(140);
        titleLabel.setAlignment(Pos.CENTER);

        // Autore
        Label authorLabel = new Label(book.getAuthor());
        authorLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        authorLabel.setTextFill(Color.LIGHTGRAY);
        authorLabel.setWrapText(true);
        authorLabel.setMaxWidth(140);
        authorLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(bookImage, titleLabel, authorLabel);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (bookClickHandler != null) {
                bookClickHandler.accept(book);
            }
        });

        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #3a3a3c;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10;" +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: #2c2c2e;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10;" +
                            "-fx-cursor: hand;"
            );
        });

        return card;
    }

    /**
     * ✅ NUOVO: Mostra messaggio quando non ci sono libri
     */
    private void showNoBooksMessage() {
        Label noBooks = new Label("📚 Nessun libro trovato per questa categoria");
        noBooks.setFont(Font.font("System", FontWeight.NORMAL, 18));
        noBooks.setTextFill(Color.LIGHTGRAY);

        Label suggestion = new Label("💡 Prova a cercare in altre categorie");
        suggestion.setFont(Font.font("System", FontWeight.NORMAL, 14));
        suggestion.setTextFill(Color.GRAY);

        VBox messageBox = new VBox(10, noBooks, suggestion);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(50));

        booksGrid.add(messageBox, 0, 0);
    }

    /**
     * ✅ NUOVO: Mostra errore di caricamento
     */
    private void showLoadingError() {
        Label errorLabel = new Label("❌ Errore nel caricamento");
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        errorLabel.setTextFill(Color.LIGHTCORAL);

        Label retryLabel = new Label("🔄 Riprova più tardi");
        retryLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        retryLabel.setTextFill(Color.GRAY);

        VBox errorBox = new VBox(10, errorLabel, retryLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(50));

        booksGrid.getChildren().clear();
        booksGrid.add(errorBox, 0, 0);
    }

    /**
     * ✅ NUOVO: Aggiorna il conteggio dei libri
     */
    private void updateBookCount() {
        // Questo metodo può essere implementato per aggiornare le statistiche
        // nell'header della categoria, se necessario
    }

    /**
     * ✅ AGGIORNATO: Aggiorna le statistiche della categoria nell'header
     */
    private void updateCategoryStats(Label statsLabel) {
        if (allCategoryBooks != null) {
            int totalBooks = allCategoryBooks.size();
            String statsText = totalBooks > 0 ?
                    "📊 " + totalBooks + " libri disponibili" :
                    "📊 Nessun libro disponibile";
            statsLabel.setText(statsText);
        }
    }

    private void filterBooks(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredBooks = new ArrayList<>(allCategoryBooks);
        } else {
            filteredBooks = allCategoryBooks.stream()
                    .filter(book ->
                            book.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                                    book.getAuthor().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        }

        sortBooks();
        updateBooksGrid();
    }

    private void sortBooks() {
        if (filteredBooks == null || filteredBooks.isEmpty()) {
            return;
        }

        String sortOption = sortComboBox.getValue();

        switch (sortOption) {
            case "📅 Più recenti":
                filteredBooks.sort((b1, b2) -> {
                    try {
                        int year1 = Integer.parseInt(b1.getPublishYear());
                        int year2 = Integer.parseInt(b2.getPublishYear());
                        return Integer.compare(year2, year1); // Decrescente
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });
                break;

            case "📅 Più vecchi":
                filteredBooks.sort((b1, b2) -> {
                    try {
                        int year1 = Integer.parseInt(b1.getPublishYear());
                        int year2 = Integer.parseInt(b2.getPublishYear());
                        return Integer.compare(year1, year2); // Crescente
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });
                break;

            case "🔤 A-Z":
                filteredBooks.sort((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
                break;

            case "🔤 Z-A":
                filteredBooks.sort((b1, b2) -> b2.getTitle().compareToIgnoreCase(b1.getTitle()));
                break;

            case "👤 Autore A-Z":
                filteredBooks.sort((b1, b2) -> b1.getAuthor().compareToIgnoreCase(b2.getAuthor()));
                break;
        }

        updateBooksGrid();
    }

    private String getCategoryIcon(String categoryName) {
        if (categoryName == null) return "📚";

        switch (categoryName.toLowerCase()) {
            case "murder":
                return "🔪";
            case "biography":
                return "💕";
            case "education":
                return "📖";
            case "saggistica":
                return "📄";
            case "fantasy":
                return "🧙‍♂️";
            case "fantascienza":
            case "sci-fi":
                return "🚀";
            case "giallo":
                return "🔍";
            case "horror":
                return "👻";
            case "biografia":
                return "👤";
            case "storia":
                return "🏛️";
            case "cucina":
                return "👨‍🍳";
            case "viaggi":
                return "✈️";
            case "arte":
                return "🎨";
            case "musica":
                return "🎵";
            case "sport":
                return "⚽";
            case "tecnologia":
                return "💻";
            case "scienza":
                return "🔬";
            case "medicina":
                return "⚕️";
            case "bambini":
                return "🧸";
            case "ragazzi":
                return "👦";
            default:
                return "📚";
        }
    }
}