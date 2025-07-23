package org.BABO.client.ui;

import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Gestisce la sezione Esplora in stile Apple Books autentico
 * VERSIONE CORRETTA - Layout fisso senza scroll orizzontale
 */
public class ExploreIntegration {

    private final BookService bookService;
    private final boolean serverAvailable;
    private Consumer<Book> bookClickHandler;
    private StackPane containerPane;
    private boolean isViewingCategory = false;

    public ExploreIntegration(BookService bookService, boolean serverAvailable) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
    }

    public void setContainer(StackPane container) {
        this.containerPane = container;
    }

    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
    }

    /**
     * Crea la vista Esplora stile Apple Books
     */
    public ScrollPane createExploreView() {
        isViewingCategory = false;

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #1a1a1c;");

        // ScrollPane per l'intero contenuto
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: #1a1a1c; -fx-background: #1a1a1c;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox scrollContent = new VBox(0);
        scrollContent.setStyle("-fx-background-color: #1a1a1c;");

        // HEADER CLASSIFICHE
        Label classificheTitle = new Label("Classifiche");
        classificheTitle.setFont(Font.font("System", FontWeight.BOLD, 48));
        classificheTitle.setTextFill(Color.WHITE);
        classificheTitle.setPadding(new Insets(40, 60, 30, 60));

        scrollContent.getChildren().add(classificheTitle);

        // CONTENITORE CLASSIFICHE con sfondo grigio
        VBox classificheBackground = new VBox(40);
        classificheBackground.setStyle("-fx-background-color: #242426;");
        classificheBackground.setPadding(new Insets(40, 60, 40, 60));

        // Sezione "Più recensiti"
        VBox mostReviewedSection = createMostReviewedSection();
        classificheBackground.getChildren().add(mostReviewedSection);

        // Divisore
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #333335;");
        classificheBackground.getChildren().add(divider);

        // Sezione "Meglio valutati"
        VBox topRatedSection = createTopRatedSection();
        classificheBackground.getChildren().add(topRatedSection);

        scrollContent.getChildren().add(classificheBackground);

        // SPAZIO TRA SEZIONI
        Region spacer1 = new Region();
        spacer1.setPrefHeight(60);
        scrollContent.getChildren().add(spacer1);

        // HEADER BOOK STORE
        Label bookStoreTitle = new Label("Book Store");
        bookStoreTitle.setFont(Font.font("System", FontWeight.BOLD, 48));
        bookStoreTitle.setTextFill(Color.WHITE);
        bookStoreTitle.setPadding(new Insets(0, 60, 30, 60));

        scrollContent.getChildren().add(bookStoreTitle);

        // CARD PROMOZIONALI
        HBox promoCards = createPromotionalCards();
        promoCards.setPadding(new Insets(0, 60, 40, 60));
        scrollContent.getChildren().add(promoCards);

        // SPAZIO TRA SEZIONI
        Region spacer2 = new Region();
        spacer2.setPrefHeight(40);
        scrollContent.getChildren().add(spacer2);

        // SEZIONE NUOVI E DI TENDENZA con sfondo
        VBox trendsBackground = new VBox(0);
        trendsBackground.setStyle("-fx-background-color: #242426;");
        trendsBackground.setPadding(new Insets(40, 60, 60, 60));

        VBox trendsSection = createTrendsSection();
        trendsBackground.getChildren().add(trendsSection);

        scrollContent.getChildren().add(trendsBackground);

        scrollPane.setContent(scrollContent);
        return scrollPane;
    }

    /**
     * Header "Classifiche" come nell'immagine di Apple Books
     */
    private VBox createClassificheHeader() {
        VBox headerSection = new VBox(8);
        headerSection.setPadding(new Insets(40, 40, 20, 40));

        Label mainTitle = new Label("Classifiche");
        mainTitle.setFont(Font.font("System", FontWeight.BOLD, 48));
        mainTitle.setTextFill(Color.WHITE);

        headerSection.getChildren().add(mainTitle);
        return headerSection;
    }

    /**
     * Sezione "Più recensiti" semplificata
     */
    private VBox createMostReviewedSection() {
        VBox section = new VBox(20);

        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Più recensiti");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label arrow = new Label("\u203A");
        arrow.setFont(Font.font("System", 28));
        arrow.setTextFill(Color.web("#666666"));

        headerBox.getChildren().addAll(title, arrow);
        headerBox.setStyle("-fx-cursor: hand;");

        // Griglia libri
        GridPane booksGrid = new GridPane();
        booksGrid.setHgap(30);
        booksGrid.setVgap(20);

        loadMostReviewedBooksGrid(booksGrid);

        section.getChildren().addAll(headerBox, booksGrid);
        return section;
    }

    /**
     * Sezione "Meglio valutati" semplificata
     */
    private VBox createTopRatedSection() {
        VBox section = new VBox(20);

        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Meglio valutati");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label arrow = new Label("\u203A");
        arrow.setFont(Font.font("System", 28));
        arrow.setTextFill(Color.web("#666666"));

        headerBox.getChildren().addAll(title, arrow);
        headerBox.setStyle("-fx-cursor: hand;");

        // Griglia libri
        GridPane booksGrid = new GridPane();
        booksGrid.setHgap(30);
        booksGrid.setVgap(20);

        loadTopRatedBooksGrid(booksGrid);

        section.getChildren().addAll(headerBox, booksGrid);
        return section;
    }

    /**
     * Carica libri più recensiti in GRIGLIA FISSA
     */
    private void loadMostReviewedBooksGrid(GridPane grid) {
        Label loadingLabel = new Label("Caricamento classifiche...");
        loadingLabel.setFont(Font.font("System", 14));
        loadingLabel.setTextFill(Color.web("#666666"));
        grid.add(loadingLabel, 0, 0);

        bookService.getFeaturedBooksAsync().thenAccept(books -> {
            Platform.runLater(() -> {
                grid.getChildren().clear();
                if (!books.isEmpty()) {
                    // Configura le colonne per occupare tutto lo spazio
                    grid.getColumnConstraints().clear();
                    for (int i = 0; i < 3; i++) {
                        javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
                        column.setPercentWidth(33.33);
                        column.setHgrow(Priority.ALWAYS);
                        column.setFillWidth(true);
                        grid.getColumnConstraints().add(column);
                    }

                    // Popola griglia 3x2
                    for (int i = 0; i < Math.min(6, books.size()); i++) {
                        HBox bookCard = createCompactRankedBookCard(books.get(i), i + 1, true);
                        bookCard.setMaxWidth(Double.MAX_VALUE);
                        int col = i % 3;
                        int row = i / 3;
                        grid.add(bookCard, col, row);
                        GridPane.setHgrow(bookCard, Priority.ALWAYS);
                        GridPane.setFillWidth(bookCard, true);
                    }
                }
            });
        }).exceptionally(throwable -> {
            System.err.println("❌ Errore caricamento libri più recensiti: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Carica libri meglio valutati in GRIGLIA FISSA
     */
    private void loadTopRatedBooksGrid(GridPane grid) {
        Label loadingLabel = new Label("Caricamento libri meglio valutati...");
        loadingLabel.setFont(Font.font("System", 14));
        loadingLabel.setTextFill(Color.web("#666666"));
        grid.add(loadingLabel, 0, 0);

        bookService.getNewReleasesAsync().thenAccept(books -> {
            Platform.runLater(() -> {
                grid.getChildren().clear();
                if (!books.isEmpty()) {
                    // Configura le colonne per occupare tutto lo spazio
                    grid.getColumnConstraints().clear();
                    for (int i = 0; i < 3; i++) {
                        javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
                        column.setPercentWidth(33.33);
                        column.setHgrow(Priority.ALWAYS);
                        column.setFillWidth(true);
                        grid.getColumnConstraints().add(column);
                    }

                    // Popola griglia 3x2
                    for (int i = 0; i < Math.min(6, books.size()); i++) {
                        HBox bookCard = createCompactRankedBookCard(books.get(i), i + 1, false);
                        bookCard.setMaxWidth(Double.MAX_VALUE);
                        int col = i % 3;
                        int row = i / 3;
                        grid.add(bookCard, col, row);
                        GridPane.setHgrow(bookCard, Priority.ALWAYS);
                        GridPane.setFillWidth(bookCard, true);
                    }
                }
            });
        }).exceptionally(throwable -> {
            System.err.println("❌ Errore caricamento libri meglio valutati: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Crea card ESTESA per griglia - stile Apple Books
     */
    private HBox createCompactRankedBookCard(Book book, int rank, boolean showRating) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(90);
        card.setMaxHeight(90);
        card.setPadding(new Insets(12, 20, 12, 12));
        card.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;"
        );

        // Numero classifica più grande
        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        rankLabel.setTextFill(Color.web("#8E8E93"));
        rankLabel.setMinWidth(45);
        rankLabel.setAlignment(Pos.CENTER);

        // Copertina più grande
        ImageView cover = ImageUtils.createSafeImageView(
                book.getImageUrl(),
                55,
                75
        );

        Rectangle clip = new Rectangle(55, 75);
        clip.setArcWidth(6);
        clip.setArcHeight(6);
        cover.setClip(clip);

        // Ombra per profondità
        DropShadow shadow = new DropShadow();
        shadow.setRadius(4);
        shadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        cover.setEffect(shadow);

        // Info libro con più spazio
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.MEDIUM, 16));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(false);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle("-fx-text-overrun: ellipsis;");

        Label authorLabel = new Label(book.getAuthor());
        authorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        authorLabel.setTextFill(Color.web("#8E8E93"));
        authorLabel.setWrapText(false);
        authorLabel.setMaxWidth(Double.MAX_VALUE);
        authorLabel.setStyle("-fx-text-overrun: ellipsis;");

        infoBox.getChildren().addAll(titleLabel, authorLabel);

        // Aggiungi rating se richiesto
        if (showRating) {
            HBox ratingBox = new HBox(5);
            ratingBox.setAlignment(Pos.CENTER_LEFT);

            double rating = 3.5 + (Math.random() * 1.5);
            int stars = (int) Math.round(rating);

            Label starsLabel = new Label("★".repeat(stars) + "☆".repeat(5 - stars));
            starsLabel.setFont(Font.font("System", 12));
            starsLabel.setTextFill(Color.web("#FFD700"));

            Label ratingLabel = new Label(String.format("%.1f", rating));
            ratingLabel.setFont(Font.font("System", 12));
            ratingLabel.setTextFill(Color.web("#8E8E93"));

            ratingBox.getChildren().addAll(starsLabel, ratingLabel);
            infoBox.getChildren().add(ratingBox);
        }

        card.getChildren().addAll(rankLabel, cover, infoBox);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (bookClickHandler != null) {
                bookClickHandler.accept(book);
            }
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.05);" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-cursor: hand;"
            );
        });

        return card;
    }

    /**
     * Crea le card promozionali
     */
    private HBox createPromotionalCards() {
        HBox cardsContainer = new HBox(25);
        cardsContainer.setAlignment(Pos.CENTER);

        VBox card1 = createPromotionalCard(
                "DI TENDENZA",
                "Scopri i romanzi del momento",
                "#4A7C59",
                "#6B9B7B",
                createQuoteGraphic("#FFB6C1", -10)
        );

        VBox card2 = createPromotionalCard(
                "SCOPRI",
                "Allarga gli orizzonti con i saggi più amati del momento",
                "#B85066",
                "#D47385",
                createQuoteGraphic("#B6D0FF", 10)
        );

        VBox card3 = createPromotionalCard(
                "PREZZI SPECIALI",
                "Trova splendidi libri a 3,99 € o meno",
                "#E8B5C7",
                "#F5D4DF",
                createPriceGraphic()
        );

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);

        cardsContainer.getChildren().addAll(card1, card2, card3);
        return cardsContainer;
    }

    /**
     * Crea una card promozionale con dimensioni responsive
     */
    private VBox createPromotionalCard(String header, String title, String color1, String color2, Region graphic) {
        VBox card = new VBox();
        card.setPrefHeight(180);
        card.setMinHeight(150);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + color1 + ", " + color2 + ");" +
                        "-fx-background-radius: 16;" +
                        "-fx-cursor: hand;"
        );
        card.setPadding(new Insets(25));

        // Header label
        Label headerLabel = new Label(header);
        headerLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        headerLabel.setTextFill(Color.WHITE);
        headerLabel.setOpacity(0.9);

        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);

        VBox textContainer = new VBox(8);
        textContainer.getChildren().addAll(headerLabel, titleLabel);

        // Layout con grafica
        HBox cardContent = new HBox();
        cardContent.setAlignment(Pos.CENTER);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        cardContent.getChildren().addAll(textContainer, spacer, graphic);
        card.getChildren().add(cardContent);

        // Effetto hover
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.02);
            card.setScaleY(1.02);
            card.setEffect(new DropShadow(20, Color.BLACK));
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            card.setEffect(null);
        });

        return card;
    }

    /**
     * Crea grafica virgolette per le card
     */
    private Region createQuoteGraphic(String color, double rotation) {
        Label quote = new Label("\u201C");  // Unicode per virgolette aperte
        quote.setFont(Font.font("System", FontWeight.BOLD, 80));
        quote.setTextFill(Color.web(color));
        quote.setOpacity(0.5);
        quote.setRotate(rotation);
        return quote;
    }

    /**
     * Crea grafica prezzo per la card prezzi speciali
     */
    private Region createPriceGraphic() {
        VBox container = new VBox(-5);
        container.setAlignment(Pos.CENTER);

        Label price = new Label("3,99 €");
        price.setFont(Font.font("System", FontWeight.BOLD, 32));
        price.setTextFill(Color.color(1, 1, 0.2, 0.8));
        price.setRotate(-5);

        HBox shapes = new HBox(8);
        shapes.setAlignment(Pos.CENTER);

        Rectangle rect1 = new Rectangle(30, 30);
        rect1.setFill(Color.color(0.5, 0.7, 1, 0.4));
        rect1.setArcWidth(8);
        rect1.setArcHeight(8);
        rect1.setRotate(15);

        Rectangle rect2 = new Rectangle(20, 20);
        rect2.setFill(Color.color(1, 0.8, 0.3, 0.4));
        rect2.setArcWidth(6);
        rect2.setArcHeight(6);
        rect2.setRotate(-20);

        shapes.getChildren().addAll(rect1, rect2);
        container.getChildren().addAll(price, shapes);

        return container;
    }

    /**
     * Sezione "Nuovi e di tendenza" semplificata
     */
    private VBox createTrendsSection() {
        VBox section = new VBox(25);

        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Nuovi e di tendenza");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        Label arrow = new Label("\u203A");
        arrow.setFont(Font.font("System", 28));
        arrow.setTextFill(Color.web("#666666"));

        headerBox.getChildren().addAll(title, arrow);
        headerBox.setStyle("-fx-cursor: hand;");

        Label subtitle = new Label("Ultime uscite e libri al centro dell'attenzione.");
        subtitle.setFont(Font.font("System", 18));
        subtitle.setTextFill(Color.web("#999999"));

        // Griglia libri 4x3
        GridPane booksGrid = new GridPane();
        booksGrid.setHgap(30);
        booksGrid.setVgap(35);
        booksGrid.setPadding(new Insets(20, 0, 0, 0));

        loadTrendingBooks(booksGrid);

        section.getChildren().addAll(headerBox, subtitle, booksGrid);
        return section;
    }

    /**
     * Carica libri reali dal server
     */
    private void loadTrendingBooks(GridPane grid) {
        // Carica libri in evidenza
        bookService.getFeaturedBooksAsync().thenAccept(books -> {
            Platform.runLater(() -> {
                if (!books.isEmpty()) {
                    grid.getChildren().clear();
                    populateGrid(grid, books, 0, 8);
                }
            });
        }).exceptionally(throwable -> {
            System.err.println("❌ Errore caricamento Featured Books: " + throwable.getMessage());
            return null;
        });

        // Carica nuove uscite per completare la griglia
        bookService.getNewReleasesAsync().thenAccept(books -> {
            Platform.runLater(() -> {
                if (!books.isEmpty()) {
                    populateGrid(grid, books, 8, 4);
                }
            });
        }).exceptionally(throwable -> {
            System.err.println("❌ Errore caricamento New Releases: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Popola griglia con libri - layout fisso 4 colonne
     */
    private void populateGrid(GridPane grid, List<Book> books, int startIndex, int maxCount) {
        int columns = 4;
        int added = 0;

        for (int i = 0; i < Math.min(books.size(), maxCount); i++) {
            Book book = books.get(i);
            VBox bookCard = createBookCard(book);

            int totalIndex = startIndex + i;
            int col = totalIndex % columns;
            int row = totalIndex / columns;

            grid.add(bookCard, col, row);
            GridPane.setHalignment(bookCard, javafx.geometry.HPos.CENTER);
            added++;
        }
    }

    /**
     * Crea card per un libro
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setMaxWidth(140);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-cursor: hand;");

        // Immagine copertina
        ImageView cover = ImageUtils.createSafeImageView(book.getImageUrl(), 120, 170);

        Rectangle clip = new Rectangle(120, 170);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        cover.setClip(clip);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        cover.setEffect(shadow);

        // Titolo
        String titleText = book.getTitle() != null ? book.getTitle() : "Titolo non disponibile";
        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(120);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPrefHeight(35);

        // Autore
        String authorText = book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto";
        Label authorLabel = new Label(authorText);
        authorLabel.setFont(Font.font("System", 11));
        authorLabel.setTextFill(Color.web("#999999"));
        authorLabel.setWrapText(false);
        authorLabel.setMaxWidth(120);
        authorLabel.setAlignment(Pos.CENTER);
        authorLabel.setStyle("-fx-text-overrun: ellipsis;");

        card.getChildren().addAll(cover, titleLabel, authorLabel);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (bookClickHandler != null) {
                bookClickHandler.accept(book);
            }
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
            cover.setOpacity(0.9);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            cover.setOpacity(1.0);
        });

        return card;
    }

    public boolean isViewingCategory() {
        return isViewingCategory;
    }
}