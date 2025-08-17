package org.BABO.client.ui.Home;

import org.BABO.client.service.ClientRatingService;
import org.BABO.client.ui.AppleBooksClient;
import org.BABO.client.ui.Category.CategoryView;
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
    private List<Book> mostReviewedBooks = new ArrayList<>();
    private List<Book> topRatedBooks = new ArrayList<>();

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
                            // ✅ SALVA LA LISTA COMPLETA per la navigazione
                            this.mostReviewedBooks = new ArrayList<>(books);

                            populateBookSection(mostReviewedSection, books.subList(0, Math.min(10, books.size())), "mostReviewed");
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
                            // ✅ SALVA LA LISTA COMPLETA per la navigazione
                            this.topRatedBooks = new ArrayList<>(books);

                            populateBookSection(topRatedSection, books.subList(0, Math.min(10, books.size())), "topRated");
                        } else {
                            showErrorInSection(topRatedSection, "Nessun libro ben valutato disponibile");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ DEBUG: Errore rating service migliori valutazioni: " + throwable.getMessage());
                        showErrorInSection(topRatedSection, "Errore caricamento libri meglio valutati");
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
    private void populateBookSection(VBox section, List<Book> books, String sectionType) {
        // Trova il container dei libri (ultimo figlio della sezione)
        if (section.getChildren().size() >= 2 && section.getChildren().get(1) instanceof HBox) {
            HBox booksContainer = (HBox) section.getChildren().get(1);
            booksContainer.getChildren().clear();

            for (int i = 0; i < Math.min(books.size(), 8); i++) {
                Book book = books.get(i);
                VBox bookCard = createBookCard(book, sectionType);
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

    private VBox createBookCard(Book book, String sectionType) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(120);
        card.setMaxWidth(120);

        // Copertina libro
        ImageView bookCover = ImageUtils.createSafeImageView(book.getSafeImageFileName(), 90, 130);

        // Clip arrotondato
        Rectangle clip = new Rectangle(90, 130);
        clip.setArcWidth(6);
        clip.setArcHeight(6);
        bookCover.setClip(clip);

        // Effetto ombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setOffsetY(4);
        shadow.setRadius(8);
        bookCover.setEffect(shadow);

        // Titolo
        Label title = new Label(book.getTitle() != null ? book.getTitle() : "Titolo non disponibile");
        title.setFont(Font.font("System", FontWeight.BOLD, 11));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        title.setMaxWidth(110);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Autore
        Label author = new Label(book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto");
        author.setFont(Font.font("System", FontWeight.NORMAL, 9));
        author.setTextFill(Color.web("#8E8E93"));
        author.setWrapText(true);
        author.setMaxWidth(110);
        author.setAlignment(Pos.CENTER);
        author.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Rating box (se disponibili)
        VBox ratingBox = new VBox(2);
        ratingBox.setAlignment(Pos.CENTER);

        // Controllo più robusto per il rating
        Double rating = book.getAverageRating();
        if (rating != null && rating > 0.0) {
            // Stelle
            Label starsLabel = new Label(createStarString(book.getAverageRating()));
            starsLabel.setFont(Font.font("System", 10));
            starsLabel.setTextFill(Color.web("#FFD700"));

            // Rating numerico
            Label ratingText = new Label(String.format("%.1f", book.getAverageRating()));
            ratingText.setFont(Font.font("System", 8));
            ratingText.setTextFill(Color.web("#8E8E93"));

            ratingBox.getChildren().addAll(starsLabel, ratingText);
        } else if (book.getReviewCount() > 0) {
            // Se ha almeno delle recensioni, mostra il numero
            Label reviewCount = new Label(book.getReviewCount() + " recensioni");
            reviewCount.setFont(Font.font("System", 9));
            reviewCount.setTextFill(Color.web("#8E8E93"));
            ratingBox.getChildren().add(reviewCount);
        }

        // Aggiungi tutti gli elementi alla card
        if (ratingBox.getChildren().isEmpty()) {
            card.getChildren().addAll(bookCover, title, author);
        } else {
            card.getChildren().addAll(bookCover, title, author, ratingBox);
        }

        // Passa lista completa della sezione
        card.setOnMouseClicked(e -> {
            System.out.println("📖 Click libro sezione " + sectionType + ": " + book.getTitle());

            // Determina quale lista usare in base al tipo di sezione
            List<Book> sectionBooks;
            switch (sectionType) {
                case "mostReviewed":
                    sectionBooks = mostReviewedBooks;
                    System.out.println("📚 Usando lista 'Più recensiti' con " + mostReviewedBooks.size() + " libri");
                    break;
                case "topRated":
                    sectionBooks = topRatedBooks;
                    System.out.println("📚 Usando lista 'Migliori valutazioni' con " + topRatedBooks.size() + " libri");
                    break;
                default:
                    sectionBooks = List.of(book); // Fallback
                    System.out.println("⚠️ Tipo sezione sconosciuto: " + sectionType);
                    break;
            }

            // Apri direttamente BookDetailsPopup con la lista completa della sezione
            AppleBooksClient.openBookDetails(book, sectionBooks, null);
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

        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    /**
     * Crea stringa di stelle per il rating
     */
    private String createStarString(double rating) {
        int fullStars = (int) rating;
        boolean halfStar = (rating - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();

        // Stelle piene
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }

        // Stella mezza se necessaria
        if (halfStar && fullStars < 5) {
            stars.append("☆");
            fullStars++;
        }

        // Stelle vuote rimanenti
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }

        return stars.toString();
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
                "Narrativa per giovani adulti",
                "Scienze sociali",
                "Biografia e autobiografia",
                "Storia",
                "Narrativa per ragazzi",
                "Umore",
                "Religione",
                "Economia e Commercio",
                "Narrativa"
        };

        String[] colors = {
                "#E9B29B",
                "#B5BA8C",
                "#52557A",
                "#8F5D5D",
                "#9AAAB4",
                "#F0C57F",
                "#5F797B",
                "#E0875E",
                "#81B29A"
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
        Button button = new Button(category.replace(" ", "\n"));
        button.setPrefSize(200, 100);
        button.setFont(Font.font("System", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);
        button.setWrapText(true);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);" +
                        "-fx-text-alignment: center;" +
                        "-fx-line-spacing: -5px;" // ← AGGIUNTA: riduce spazio tra righe (valore negativo)
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