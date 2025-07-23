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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Vista dettagliata per una categoria specifica
 * Mostra tutti i libri di una categoria con filtri e ordinamento
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
                        "-fx-padding: 10;"
        );

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterBooks(newText, sortComboBox.getValue());
        });

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll(
                "Nome A-Z",
                "Nome Z-A",
                "Autore A-Z",
                "Autore Z-A",
                "Anno Pubblicazione ↓",
                "Anno Pubblicazione ↑",
                "Più Votati",
                "Meno Votati"
        );
        sortComboBox.setValue("Nome A-Z");
        sortComboBox.setPrefWidth(180);
        sortComboBox.setStyle(
                "-fx-background-color: #2c2c2e;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;"
        );

        sortComboBox.setOnAction(e -> {
            filterBooks(searchField.getText(), sortComboBox.getValue());
        });

        Label resultCount = new Label();
        resultCount.setFont(Font.font("System", FontWeight.NORMAL, 14));
        resultCount.setTextFill(Color.LIGHTGRAY);
        resultCount.setId("resultCount");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(
                new Label("🔍"), searchField,
                new Label("📊"), sortComboBox,
                spacer, resultCount
        );

        filterBar.getChildren().stream()
                .filter(node -> node instanceof Label && !((Label) node).getId().equals("resultCount"))
                .forEach(label -> {
                    ((Label) label).setTextFill(Color.WHITE);
                    ((Label) label).setFont(Font.font("System", FontWeight.BOLD, 14));
                });

        return filterBar;
    }

    private void loadCategoryBooks() {
        CompletableFuture.supplyAsync(() -> {
            List<Book> allBooks = null;
            try {
                allBooks = bookService.getAllBooks();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return filterBooksByCategory(allBooks, category);
        }).thenAccept(books -> {
            Platform.runLater(() -> {
                this.allCategoryBooks = books;
                this.filteredBooks = new ArrayList<>(books);
                updateBooksDisplay();
                updateResultCount();
            });
        });
    }

    private List<Book> filterBooksByCategory(List<Book> books, Category category) {
        List<Book> categoryBooks = new ArrayList<>();

        for (Book book : books) {
            if (belongsToCategory(book, category)) {
                categoryBooks.add(book);
            }
        }

        return categoryBooks;
    }

    private boolean belongsToCategory(Book book, Category category) {
        String title = book.getTitle().toLowerCase();
        String author = book.getAuthor().toLowerCase();
        String description = book.getDescription().toLowerCase();
        String categoryName = category.getName().toLowerCase();

        switch (categoryName) {
            case "fiction":
                return title.contains("romanzo") || description.contains("storia") ||
                        !title.contains("manuale") && !description.contains("guida");

            case "fantascienza":
                return title.contains("futuro") || description.contains("spazio") ||
                        description.contains("tecnologia") || description.contains("robot") ||
                        author.contains("asimov") || author.contains("dick");

            case "fantasy":
                return description.contains("magia") || description.contains("drago") ||
                        description.contains("regno") || title.contains("signore") ||
                        author.contains("tolkien") || description.contains("avventura");

            case "giallo/thriller":
                return description.contains("mistero") || description.contains("omicidio") ||
                        description.contains("investigatore") || description.contains("crimine");

            case "romance":
                return description.contains("amore") || description.contains("romantico") ||
                        title.contains("amore") || description.contains("cuore");

            case "biografie":
                return description.contains("vita") || title.contains("biografia") ||
                        description.contains("autobiografia") || description.contains("memorie");

            case "storia":
                return description.contains("storico") || description.contains("guerra") ||
                        description.contains("antico") ||
                        (book.getPublishYear() != null && book.getPublishYear().compareTo("1800") < 0);

            case "scienza":
                return description.contains("scienza") || description.contains("ricerca") ||
                        description.contains("scoperta") || title.contains("teoria");

            case "filosofia":
                return description.contains("filosofia") || description.contains("pensiero") ||
                        description.contains("esistenza") || description.contains("etica");

            case "arte":
                return description.contains("arte") || description.contains("pittura") ||
                        description.contains("museo") || description.contains("artista");

            case "cucina":
                return description.contains("cucina") || description.contains("ricetta") ||
                        description.contains("cucinare") || title.contains("chef");

            case "viaggi":
                return description.contains("viaggio") || description.contains("paese") ||
                        description.contains("cultura") || description.contains("esplorare");

            default:
                return book.getId() % 12 == (category.getId() != null ? category.getId() % 12 : 0);
        }
    }

    private void filterBooks(String searchText, String sortBy) {
        List<Book> filtered = new ArrayList<>(allCategoryBooks);

        if (searchText != null && !searchText.trim().isEmpty()) {
            String query = searchText.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(book ->
                            book.getTitle().toLowerCase().contains(query) ||
                                    book.getAuthor().toLowerCase().contains(query) ||
                                    book.getDescription().toLowerCase().contains(query)
                    )
                    .collect(Collectors.toList());
        }

        if (sortBy != null) {
            switch (sortBy) {
                case "Nome A-Z":
                    filtered.sort(Comparator.comparing(Book::getTitle));
                    break;
                case "Nome Z-A":
                    filtered.sort(Comparator.comparing(Book::getTitle).reversed());
                    break;
                case "Autore A-Z":
                    filtered.sort(Comparator.comparing(Book::getAuthor));
                    break;
                case "Autore Z-A":
                    filtered.sort(Comparator.comparing(Book::getAuthor).reversed());
                    break;
                case "Anno Pubblicazione ↓":
                    filtered.sort(Comparator.comparing(Book::getPublishYear,
                            Comparator.nullsLast(String::compareTo)).reversed());
                    break;
                case "Anno Pubblicazione ↑":
                    filtered.sort(Comparator.comparing(Book::getPublishYear,
                            Comparator.nullsLast(String::compareTo)));
                    break;
                case "Più Votati":
                    filtered.sort((b1, b2) -> Double.compare(getSimulatedRating(b2), getSimulatedRating(b1)));
                    break;
                case "Meno Votati":
                    filtered.sort((b1, b2) -> Double.compare(getSimulatedRating(b1), getSimulatedRating(b2)));
                    break;
            }
        }

        this.filteredBooks = filtered;
        updateBooksDisplay();
        updateResultCount();
    }

    private void updateBooksDisplay() {
        booksGrid.getChildren().clear();

        int col = 0;
        int row = 0;
        int maxCols = calculateMaxColumns();

        for (Book book : filteredBooks) {
            VBox bookCard = createBookCard(book);
            booksGrid.add(bookCard, col, row);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }

        if (filteredBooks.isEmpty()) {
            Label noBooks = new Label("📚 Nessun libro trovato per i criteri selezionati");
            noBooks.setFont(Font.font("System", FontWeight.NORMAL, 16));
            noBooks.setTextFill(Color.LIGHTGRAY);
            booksGrid.add(noBooks, 0, 0);
        }
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(12);
        card.setPrefSize(180, 280);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: #2c2c2e;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );

        StackPane coverContainer = new StackPane();
        coverContainer.setPrefSize(150, 200);
        coverContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4a4a4c, #3a3a3c);" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: innershadow(three-pass-box, rgba(0,0,0,0.5), 3, 0, 0, 1);"
        );

        Label coverPlaceholder = new Label("📖");
        coverPlaceholder.setFont(Font.font("System", 48));
        coverPlaceholder.setTextFill(Color.WHITE);
        coverContainer.getChildren().add(coverPlaceholder);

        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER);

        Label title = new Label(book.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        title.setMaxWidth(150);
        title.setAlignment(Pos.CENTER);

        Label author = new Label(book.getAuthor());
        author.setFont(Font.font("System", FontWeight.NORMAL, 11));
        author.setTextFill(Color.LIGHTGRAY);
        author.setWrapText(true);
        author.setMaxWidth(150);
        author.setAlignment(Pos.CENTER);

        HBox ratingBox = createRatingDisplay(getSimulatedRating(book));

        infoBox.getChildren().addAll(title, author, ratingBox);
        card.getChildren().addAll(coverContainer, infoBox);

        setupCardHoverEffects(card);

        card.setOnMouseClicked(e -> {
            if (bookClickHandler != null) {
                bookClickHandler.accept(book);
            }
        });

        return card;
    }

    private HBox createRatingDisplay(double rating) {
        HBox ratingBox = new HBox(2);
        ratingBox.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setFont(Font.font("System", 12));

            if (i <= rating) {
                star.setTextFill(Color.GOLD);
            } else if (i - 0.5 <= rating) {
                star.setTextFill(Color.ORANGE);
            } else {
                star.setTextFill(Color.GRAY);
            }

            ratingBox.getChildren().add(star);
        }

        Label ratingValue = new Label(String.format("%.1f", rating));
        ratingValue.setFont(Font.font("System", FontWeight.NORMAL, 10));
        ratingValue.setTextFill(Color.LIGHTGRAY);
        ratingBox.getChildren().add(ratingValue);

        return ratingBox;
    }

    private void setupCardHoverEffects(VBox card) {
        String originalStyle = card.getStyle();
        String hoverStyle = originalStyle.replace("#2c2c2e", "#3a3a3c");

        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(originalStyle);
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
    }

    private void updateResultCount() {
        Label countLabel = (Label) mainContainer.lookup("#resultCount");
        if (countLabel != null) {
            String text = filteredBooks.size() + " di " + allCategoryBooks.size() + " libri";
            countLabel.setText(text);
        }
    }

    private void updateCategoryStats(Label statsLabel) {
        CompletableFuture.supplyAsync(() -> {
            int totalBooks = allCategoryBooks.size();
            double avgRating = allCategoryBooks.stream()
                    .mapToDouble(this::getSimulatedRating)
                    .average()
                    .orElse(0.0);

            return String.format("📊 %d libri disponibili • ⭐ Media voti: %.1f", totalBooks, avgRating);
        }).thenAccept(stats -> {
            Platform.runLater(() -> statsLabel.setText(stats));
        });
    }

    private int calculateMaxColumns() {
        return 5;
    }

    private String getCategoryIcon(String categoryName) {
        Map<String, String> icons = new HashMap<>();
        icons.put("Fiction", "📚");
        icons.put("Fantascienza", "🚀");
        icons.put("Fantasy", "🐉");
        icons.put("Giallo/Thriller", "🔍");
        icons.put("Romance", "💕");
        icons.put("Biografie", "👤");
        icons.put("Storia", "🏛️");
        icons.put("Scienza", "🔬");
        icons.put("Filosofia", "🤔");
        icons.put("Arte", "🎨");
        icons.put("Cucina", "👨‍🍳");
        icons.put("Viaggi", "✈️");

        return icons.getOrDefault(categoryName, "📖");
    }

    private double getSimulatedRating(Book book) {
        Random random = new Random(book.getId());
        return 2.0 + (random.nextDouble() * 3.0);
    }
}