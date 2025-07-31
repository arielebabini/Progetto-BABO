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
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Vista dettagliata per una categoria specifica
 * Mostra tutti i libri di una categoria con filtri e ordinamento
 * ✅ AGGIORNATO: Con layout FlowPane per 8 libri per riga
 */
public class CategoryView {

    private final BookService bookService;
    private final Category category;
    private Consumer<Book> bookClickHandler;
    private Consumer<Void> backHandler;
    private VBox mainContainer;
    private FlowPane booksFlowPane; // ✅ CAMBIATO: da GridPane a FlowPane
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

        // ✅ RIPRISTINATO: Usa FlowPane invece di GridPane per layout flessibile
        FlowPane booksFlowPane = new FlowPane();
        booksFlowPane.setHgap(20);
        booksFlowPane.setVgap(25);
        booksFlowPane.setPadding(new Insets(20, 0, 0, 0));
        booksFlowPane.setAlignment(Pos.TOP_LEFT);
        booksFlowPane.setPrefWrapLength(800); // Aiuta con il wrapping per circa 8 libri per riga

        mainContainer.getChildren().add(booksFlowPane);

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

        // FIX: Testo leggibile nelle opzioni del dropdown
        sortComboBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                // Forza il testo bianco nelle opzioni
                setStyle("-fx-text-fill: #ffffff !important; -fx-background-color: #2c2c2e;");
            }
        });

        // FIX: Testo leggibile per l'opzione selezionata
        sortComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                // Forza il testo bianco per l'opzione selezionata
                setStyle("-fx-text-fill: #ffffff !important; -fx-background-color: transparent;");
            }
        });

        sortComboBox.setOnAction(e -> sortBooks());

        filterBar.getChildren().addAll(searchField, sortComboBox);
        return filterBar;
    }

    /**
     * ✅ AGGIORNATO: Carica i libri per questa categoria
     */
    private void loadCategoryBooks() {
        System.out.println("📚 Caricamento libri per categoria: " + category.getName());

        // Trova il FlowPane nel mainContainer
        booksFlowPane = null;
        for (var child : mainContainer.getChildren()) {
            if (child instanceof FlowPane) {
                booksFlowPane = (FlowPane) child;
                break;
            }
        }

        if (booksFlowPane == null) {
            System.err.println("❌ FlowPane non trovato nel container");
            return;
        }

        // Mostra indicatore di caricamento
        Label loadingLabel = new Label("📚 Caricamento libri...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);

        VBox loadingBox = new VBox(loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));

        booksFlowPane.getChildren().clear();
        booksFlowPane.getChildren().add(loadingBox);

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
     * ✅ RIPRISTINATO: Layout FlowPane con circa 8 libri per riga
     */
    private void updateBooksGrid() {
        if (booksFlowPane == null) return;

        booksFlowPane.getChildren().clear();

        if (filteredBooks.isEmpty()) {
            // Non mostrare nessun messaggio se non ci sono libri
            return;
        }

        // Aggiungi tutti i libri al FlowPane - si auto-organizzano in circa 8 per riga
        for (Book book : filteredBooks) {
            VBox bookCard = createBookCard(book);
            booksFlowPane.getChildren().add(bookCard);
        }
    }

    /**
     * ✅ AGGIORNATO: Crea una card per libro con dimensioni ottimizzate per 8 per riga
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(130);   // Dimensioni ottimizzate per circa 8 libri per riga
        card.setMinWidth(130);
        card.setPrefHeight(240);

        // Immagine del libro
        ImageView bookImage = ImageUtils.createSafeImageView(book.getImageUrl(), 120, 180);
        Rectangle clip = new Rectangle(120, 180);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        bookImage.setClip(clip);

        // Titolo
        String titleText = book.getTitle() != null ? book.getTitle() : "Titolo non disponibile";
        if (titleText.length() > 20) {
            titleText = titleText.substring(0, 17) + "...";
        }

        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(false);
        titleLabel.setMaxWidth(120);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-text-overrun: ellipsis; -fx-text-alignment: center;");

        // Autore
        String authorText = book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto";
        if (authorText.length() > 25) {
            authorText = authorText.substring(0, 22) + "...";
        }

        Label authorLabel = new Label(authorText);
        authorLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        authorLabel.setTextFill(Color.LIGHTGRAY);
        authorLabel.setWrapText(false);
        authorLabel.setMaxWidth(120);
        authorLabel.setAlignment(Pos.CENTER);
        authorLabel.setStyle("-fx-text-overrun: ellipsis; -fx-text-alignment: center;");

        card.getChildren().addAll(bookImage, titleLabel, authorLabel);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (bookClickHandler != null) {
                bookClickHandler.accept(book);
            }
        });

        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-cursor: hand;");
            bookImage.setOpacity(0.8);
        });

        card.setOnMouseExited(e -> {
            bookImage.setOpacity(1.0);
        });

        return card;
    }

    /**
     * ✅ AGGIORNATO: Mostra errore di caricamento nel FlowPane
     */
    private void showLoadingError() {
        if (booksFlowPane == null) return;

        Label errorLabel = new Label("❌ Errore nel caricamento");
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        errorLabel.setTextFill(Color.LIGHTCORAL);

        Label retryLabel = new Label("🔄 Riprova più tardi");
        retryLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        retryLabel.setTextFill(Color.GRAY);

        VBox errorBox = new VBox(10, errorLabel, retryLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(50));

        booksFlowPane.getChildren().clear();
        booksFlowPane.getChildren().add(errorBox);
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
            case "thriller":
                return "🔪";
            case "romance":
                return "💕";
            case "narrativa":
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