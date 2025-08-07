package org.BABO.client.ui;

import org.BABO.client.service.ClientRatingService;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import org.BABO.client.ui.ImageUtils;
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
    private final ClientRatingService ratingService;
    private final boolean serverAvailable;
    private Consumer<Book> bookClickHandler;
    private StackPane containerPane;
    private boolean isViewingCategory = false;

    public ExploreIntegration(BookService bookService, boolean serverAvailable) {
        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.ratingService = new ClientRatingService();
        System.out.println("🚀🚀🚀 NUOVO EXPLOREINTEGRATION CREATO! 🚀🚀🚀");
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
        System.out.println("🔍 DEBUG: createExploreView() chiamato!");
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
        classificheTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        classificheTitle.setTextFill(Color.WHITE);
        classificheTitle.setPadding(new Insets(40, 25, 20, 25));

        scrollContent.getChildren().add(classificheTitle);

        // SEZIONI CLASSIFICHE
        createClassificheSection(scrollContent);

        // HEADER SCOPRI PER GENERE
        Label genereTitle = new Label("Scopri per genere");
        genereTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        genereTitle.setTextFill(Color.WHITE);
        genereTitle.setPadding(new Insets(50, 25, 20, 25));

        scrollContent.getChildren().add(genereTitle);

        // GRIGLIA CATEGORIE
        createCategoriesGrid(scrollContent);

        // Padding finale
        Region finalPadding = new Region();
        finalPadding.setPrefHeight(40);
        scrollContent.getChildren().add(finalPadding);

        scrollPane.setContent(scrollContent);
        return scrollPane;
    }

    /**
     * Crea le sezioni delle classifiche - CORRETTE
     */
    private void createClassificheSection(VBox parent) {
        // PIÙ RECENSITI
        VBox mostReviewedSection = createChartSection(
                "📊 Più recensiti",
                "I libri con più recensioni dei lettori"
        );

        System.out.println("🔍 DEBUG: Caricamento libri più recensiti...");
        ratingService.getTopRatedBooksAsync()
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        System.out.println("🔍 DEBUG: Ricevuti " + books.size() + " libri più recensiti dal rating service");
                        if (!books.isEmpty()) {
                            populateBookSection(mostReviewedSection, books.subList(0, Math.min(10, books.size())));
                        } else {
                            showErrorInSection(mostReviewedSection, "Nessun libro recensito disponibile");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ DEBUG: Errore rating service più recensiti: " + throwable.getMessage());
                        showErrorInSection(mostReviewedSection, "Errore caricamento libri più recensiti");
                    });
                    return null;
                });

        parent.getChildren().add(mostReviewedSection);

        // MEGLIO VALUTATI
        VBox topRatedSection = createChartSection(
                "⭐ Migliori valutazioni",
                "I libri con le valutazioni più alte"
        );

        System.out.println("🔍 DEBUG: Caricamento libri meglio valutati...");
        ratingService.getBestRatedBooksAsync()
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        System.out.println("🔍 DEBUG: Ricevuti " + books.size() + " libri meglio valutati dal rating service");
                        if (!books.isEmpty()) {
                            populateBookSection(topRatedSection, books.subList(0, Math.min(10, books.size())));
                        } else {
                            showErrorInSection(topRatedSection, "Nessun libro valutato disponibile");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ DEBUG: Errore rating service meglio valutati: " + throwable.getMessage());
                        showErrorInSection(topRatedSection, "Errore caricamento libri top rated");
                    });
                    return null;
                });

        parent.getChildren().add(topRatedSection);
    }

    /**
     * ✅ METODO MANCANTE: Crea una sezione classifiche
     */
    private VBox createChartSection(String title, String subtitle) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 25, 30, 25));

        // Header sezione
        VBox header = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web("#8E8E93"));

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Container per i libri (sarà popolato async)
        HBox booksContainer = new HBox(15);
        booksContainer.setAlignment(Pos.CENTER_LEFT);

        section.getChildren().addAll(header, booksContainer);
        return section;
    }

    /**
     * Popola una sezione con libri
     */
    private void populateBookSection(VBox section, List<Book> books) {
        // Trova il container dei libri (ultimo figlio della sezione)
        if (section.getChildren().size() >= 2 && section.getChildren().get(1) instanceof HBox) {
            HBox booksContainer = (HBox) section.getChildren().get(1);
            booksContainer.getChildren().clear();

            for (int i = 0; i < Math.min(books.size(), 8); i++) {
                Book book = books.get(i);
                VBox bookCard = createBookCard(book);
                booksContainer.getChildren().add(bookCard);
            }
        }
    }

    /**
     * Mostra errore in una sezione
     */
    private void showErrorInSection(VBox section, String errorMessage) {
        if (section.getChildren().size() >= 2 && section.getChildren().get(1) instanceof HBox) {
            HBox booksContainer = (HBox) section.getChildren().get(1);
            booksContainer.getChildren().clear();

            Label errorLabel = new Label("❌ " + errorMessage);
            errorLabel.setTextFill(Color.web("#e74c3c"));
            errorLabel.setFont(Font.font("System", 14));
            booksContainer.getChildren().add(errorLabel);
        }
    }

    /**
     * ✅ METODO AGGIORNATO: Crea una card per un libro - CON STELLINE!
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(120);
        card.setMaxWidth(120);

        // Immagine libro - USA IMAGEUTILS per caricare immagini vere
        ImageView bookCover = ImageUtils.createSafeImageView(book.getImageUrl(), 80, 120);

        // Applica clip e ombra all'immagine
        Rectangle clip = new Rectangle(80, 120);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        bookCover.setClip(clip);

        // Effetto ombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setOffsetX(0);
        shadow.setOffsetY(4);
        shadow.setRadius(8);
        bookCover.setEffect(shadow);

        // Titolo libro
        Label title = new Label(book.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 12));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        title.setMaxWidth(120);
        title.setAlignment(Pos.CENTER);

        // Autore
        Label author = new Label(book.getAuthor());
        author.setFont(Font.font("System", FontWeight.NORMAL, 10));
        author.setTextFill(Color.web("#8E8E93"));
        author.setWrapText(true);
        author.setMaxWidth(120);
        author.setAlignment(Pos.CENTER);

        // ✅ STELLINE! RATING BOX - container per stelline e valutazione
        HBox ratingBox = new HBox(4);
        ratingBox.setAlignment(Pos.CENTER);

        // Se il libro ha già i dati di rating dal server, mostrarli
        if (book.getAverageRating() > 0.0) {
            double avgRating = book.getAverageRating();
            int stars = (int) Math.round(avgRating);
            String starsDisplay = "★".repeat(stars) + "☆".repeat(5 - stars);

            Label starsLabel = new Label(starsDisplay);
            starsLabel.setFont(Font.font("System", 10));
            starsLabel.setTextFill(Color.web("#FFD700")); // Stelline gialle

            Label ratingText = new Label(String.format("%.1f", avgRating));
            ratingText.setFont(Font.font("System", 9));
            ratingText.setTextFill(Color.web("#8E8E93"));

            ratingBox.getChildren().addAll(starsLabel, ratingText);
        } else if (book.getReviewCount() > 0) {
            // Se ha almeno delle recensioni, mostra il numero
            Label reviewCount = new Label(book.getReviewCount() + " recensioni");
            reviewCount.setFont(Font.font("System", 9));
            reviewCount.setTextFill(Color.web("#8E8E93"));
            ratingBox.getChildren().add(reviewCount);
        }
        // Se non ha rating né recensioni, non mostrare nulla nella ratingBox

        // Aggiungi tutti gli elementi alla card
        if (ratingBox.getChildren().isEmpty()) {
            // Se non ci sono rating, layout senza rating box
            card.getChildren().addAll(bookCover, title, author);
        } else {
            // Layout con rating box
            card.getChildren().addAll(bookCover, title, author, ratingBox);
        }

        // Click handler
        card.setOnMouseClicked(e -> {
            if (bookClickHandler != null) {
                bookClickHandler.accept(book);
            }
        });

        // Effetti hover
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        // Stile cursor
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    /**
     * Crea la griglia delle categorie
     */
    private void createCategoriesGrid(VBox parent) {
        GridPane categoriesGrid = new GridPane();
        categoriesGrid.setPadding(new Insets(0, 25, 0, 25));
        categoriesGrid.setHgap(15);
        categoriesGrid.setVgap(15);

        // Categorie predefinite
        String[] categories = {
                "Young Adult Fiction",
                "Social Science",
                "Biography & Autobiography",
                "History",
                "Juvenile Fiction",
                "Humor",
                "Religion",
                "Business & Economics",
                "Fiction"
        };

        String[] colors = {
                "#FF6B6B",
                "#4ECDC4",
                "#45B7D1",
                "#96CEB4",
                "#FECA57",
                "#FF9FF3",
                "#54A0FF",
                "#5F27CD",
                "#00D2D3"
        };

        int row = 0, col = 0;
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            String color = colors[i % colors.length];

            Button categoryButton = createCategoryButton(category, color);
            categoriesGrid.add(categoryButton, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        parent.getChildren().add(categoriesGrid);
    }

    /**
     * Crea un pulsante categoria
     */
    private Button createCategoryButton(String category, String color) {
        Button button = new Button(category);
        button.setPrefSize(200, 100);
        button.setFont(Font.font("System", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );

        // Effetti hover
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });

        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        // Click handler
        button.setOnAction(e -> {
            handleCategoryClick(category);
        });

        return button;
    }

    /**
     * Gestisce il click su una categoria
     */
    private void handleCategoryClick(String category) {
        System.out.println("🎭 Click categoria: " + category);

        if (containerPane != null) {
            // Crea vista categoria
            CategoryView categoryView = new CategoryView(
                    createCategoryFromString(category),
                    bookService,
                    bookClickHandler
            );

            // Mostra la vista categoria
            showCategoryView(categoryView);
        }
    }

    /**
     * Crea un oggetto Category da una stringa
     */
    private Category createCategoryFromString(String categoryString) {
        // Mantieni il formato originale, rimuovi solo eventuali emoji
        String cleanName = categoryString.replaceAll("[^\\p{L}\\p{N}\\s&]", "").trim();


        return new Category(cleanName, "", "");
    }

    /**
     * Mostra la vista categoria
     */
    private void showCategoryView(CategoryView categoryView) {
        if (containerPane == null) {
            System.err.println("❌ Container non impostato per mostrare categoria");
            return;
        }

        try {
            isViewingCategory = true;

            // Imposta il callback per tornare indietro nel testo integrato
            categoryView.setOnBackCallback(() -> closeCategoryView());

            // Crea overlay per la categoria (SENZA bottone separato)
            StackPane categoryOverlay = new StackPane();
            categoryOverlay.setStyle("-fx-background-color: #1a1a1c;");

            // Crea contenuto categoria
            ScrollPane categoryContent = categoryView.createCategoryView();
            categoryOverlay.getChildren().add(categoryContent);

            // Aggiungi al container
            containerPane.getChildren().add(categoryOverlay);

        } catch (Exception e) {
            System.err.println("❌ Errore visualizzazione categoria: " + e.getMessage());
        }
    }

    /**
     * Chiude la vista categoria
     */
    private void closeCategoryView() {
        if (containerPane != null && isViewingCategory) {
            // Rimuovi l'overlay della categoria
            containerPane.getChildren().removeIf(node ->
                    node instanceof StackPane &&
                            !((StackPane) node).getChildren().isEmpty() &&
                            ((StackPane) node).getChildren().get(0) instanceof ScrollPane
            );
            isViewingCategory = false;
        }
    }

    /**
     * Verifica se è attualmente visualizzata una categoria
     */
    public boolean isViewingCategory() {
        return isViewingCategory;
    }

    /**
     * Torna alla vista principale Esplora
     */
    public void backToExplore() {
        closeCategoryView();
    }
}