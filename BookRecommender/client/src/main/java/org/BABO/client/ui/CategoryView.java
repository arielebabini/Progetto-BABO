package org.BABO.client.ui;

import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
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
 * Vista per mostrare i libri di una categoria specifica
 * VERSIONE SEMPLIFICATA - Usa ricerca generale filtrata per categoria
 */
public class CategoryView {

    private final Category category;
    private final BookService bookService;
    private final Consumer<Book> bookClickHandler;
    private VBox content;
    private boolean isLoading = false;

    private Label backText;
    private Runnable onBackCallback;

    public CategoryView(Category category, BookService bookService, Consumer<Book> bookClickHandler) {
        this.category = category;
        this.bookService = bookService;
        this.bookClickHandler = bookClickHandler;
    }

    // Costruttore alternativo per compatibilità con ContentArea
    public CategoryView(BookService bookService, Category category) {
        this.category = category;
        this.bookService = bookService;
        this.bookClickHandler = null; // Sarà impostato dopo
    }

    public void setOnBackCallback(Runnable callback) {
        this.onBackCallback = callback;

        // Imposta il click handler sul testo indietro se già creato
        if (backText != null) {
            backText.setOnMouseClicked(e -> {
                if (onBackCallback != null) {
                    onBackCallback.run();
                }
            });
        }
    }

    /**
     * Crea la vista della categoria
     */
    public ScrollPane createCategoryView() {
        content = new VBox(20);
        content.setPadding(new Insets(40, 25, 40, 25));
        content.setStyle("-fx-background-color: #1a1a1c;");

        // Header categoria
        createCategoryHeader();

        // Area contenuto libri
        createBooksArea();

        // Carica i libri
        loadCategoryBooks();

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: #1a1a1c; -fx-background: #1a1a1c;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    /**
     * Crea l'header della categoria
     */
    private void createCategoryHeader() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Testo cliccabile per tornare indietro
        backText = new Label("← Torna a Esplora");
        backText.setFont(Font.font("System", FontWeight.NORMAL, 16));
        backText.setTextFill(Color.web("#007AFF"));
        backText.setStyle("-fx-cursor: hand;");

        // Effetti hover
        backText.setOnMouseEntered(e -> {
            backText.setTextFill(Color.web("#0056CC"));
            backText.setStyle("-fx-cursor: hand; -fx-underline: true;");
        });

        backText.setOnMouseExited(e -> {
            backText.setTextFill(Color.web("#007AFF"));
            backText.setStyle("-fx-cursor: hand; -fx-underline: false;");
        });

        // Click handler
        backText.setOnMouseClicked(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
        });

        // Resto dell'header...
        Label categoryTitle = new Label(category.getName());
        categoryTitle.setFont(Font.font("System", FontWeight.BOLD, 36));
        categoryTitle.setTextFill(Color.WHITE);

        String description = category.getDescription();
        if (description == null || description.trim().isEmpty()) {
            description = getCategoryDescription();
        }

        Label categoryDescription = new Label(description);
        categoryDescription.setFont(Font.font("System", FontWeight.NORMAL, 18));
        categoryDescription.setTextFill(Color.web("#8E8E93"));
        categoryDescription.setWrapText(true);

        header.getChildren().addAll(backText, categoryTitle, categoryDescription);
        content.getChildren().add(header);
    }

    /**
     * Ottiene la descrizione della categoria
     */
    private String getCategoryDescription() {
        String name = category.getName().toLowerCase();

        switch (name) {
            case "young adult fiction":
            case "youngadultfiction":
                return "Storie coinvolgenti per giovani lettori tra adolescenza e età adulta";
            case "social science":
            case "socialscience":
                return "Esplorazioni della società, cultura e comportamento umano";
            case "biography & autobiography":
            case "biography and autobiography":
            case "biographyautobiography":
                return "Vite straordinarie raccontate in prima persona o da esperti biografi";
            case "history":
            case "storia":
                return "Viaggia nel tempo attraverso eventi che hanno cambiato il mondo";
            case "juvenile fiction":
            case "juvenilefiction":
                return "Avventure e storie pensate per i lettori più giovani";
            case "humor":
            case "umorismo":
                return "Risate garantite con storie divertenti e umoristico";
            case "religion":
            case "religione":
                return "Spiritualità, fede e riflessioni sui grandi temi dell'esistenza";
            case "business & economics":
            case "businessandeconomics":
            case "business":
            case "economics":
            case "economia":
                return "Strategie aziendali, economia e mondo degli affari";
            case "fiction":
            case "narrativa":
                return "Il meglio della narrativa contemporanea e classica";
            default:
                return "Esplora una selezione curata di libri per questa categoria";
        }
    }

    /**
     * Crea l'area per i libri
     */
    private void createBooksArea() {
        // Separatore
        Region separator = new Region();
        separator.setPrefHeight(20);
        content.getChildren().add(separator);

        // Indicatore di caricamento iniziale
        showLoadingIndicator();
    }

    /**
     * Mostra indicatore di caricamento
     */
    private void showLoadingIndicator() {
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(200);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        Label loadingLabel = new Label("Caricamento libri...");
        loadingLabel.setTextFill(Color.WHITE);
        loadingLabel.setFont(Font.font("System", 16));

        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        content.getChildren().add(loadingBox);
    }

    /**
     * Carica i libri della categoria
     */
    private void loadCategoryBooks() {
        if (isLoading) {
            return;
        }

        isLoading = true;
        System.out.println("🎭 Caricamento libri per categoria: " + category.getName());

        String dbCategoryName = mapCategoryToDbFormat(category.getName());

        bookService.searchBooksByCategoryAsync(dbCategoryName)
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        if (!books.isEmpty()) {
                            displayBooks(books);
                            System.out.println("✅ Caricati " + books.size() + " libri per categoria " + category.getName());
                        } else {
                            // Fallback: prova con ricerca generica se categoria specifica non trova nulla
                            System.out.println("⚠️ Nessun libro per categoria specifica, provo ricerca generica");
                            loadWithGenericSearch();
                        }
                        isLoading = false;
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.err.println("❌ Errore caricamento categoria: " + throwable.getMessage());
                        loadWithGenericSearch(); // Fallback
                        isLoading = false;
                    });
                    return null;
                });
    }

    /**
     * Estrae il termine di ricerca dalla categoria
     */
    private String extractSearchTermFromCategory() {
        String name = category.getName().toLowerCase();

        // Mappa categorie a termini di ricerca più generici
        switch (name) {
            case "narrativa per giovani adulti":
            case "narrativapergiovaniadulti":
                return "giovani adulti";
            case "scienze sociali":
            case "cienzesociali":
                return "sociale";
            case "biografia & autobiografia":
            case "biografia e autobiografia":
            case "biografiaautobiografia":
                return "biografia";
            case "storia":
                return "storia";
            case "narrativa ragazzi":
            case "narrativaragazzi":
                return "ragazzi";
            case "umore":
                return "divertente";
            case "religione":
                return "spirituale";
            case "economia & commercio":
            case "economiaecommercio":
            case "economia":
            case "commercio":
                return "commercio";
            case "narrativa":
                return "narrativa";
            default:
                return category.getName();
        }
    }

    /**
     * Mappa le categorie dei bottoni con quelle del database
     */
    private String mapCategoryToDbFormat(String buttonCategoryName) {
        switch (buttonCategoryName.toLowerCase()) {
            case "young adult fiction":
                return "Young Adult Fiction";
            case "social science":
                return "Social Science";
            case "biography & autobiography":
                return "Biography & Autobiography";
            case "history":
                return "History";
            case "juvenile fiction":
                return "Juvenile Fiction";
            case "humor":
                return "Humor";
            case "religion":
                return "Religion";
            case "business & economics":
                return "Business & Economics";
            case "fiction":
                return "Fiction";
            default:
                return buttonCategoryName; // Usa il nome originale come fallback
        }
    }

    /**
     * Fallback con ricerca generica se categoria specifica non funziona
     */
    private void loadWithGenericSearch() {
        String searchTerm = extractSearchTermFromCategory();
        bookService.searchBooksAsync(searchTerm)
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        List<Book> filteredBooks = filterBooksForCategory(books);

                        if (filteredBooks.size() < 3) {
                            // Se ancora pochi risultati, prova con tutti i libri
                            loadAllBooksAndFilter();
                        } else {
                            displayBooks(filteredBooks);
                        }
                    });
                });
    }

    /**
     * Fallback: carica tutti i libri e filtra
     */
    private void loadAllBooksAndFilter() {
        bookService.getAllBooksAsync()
                .thenAccept(allBooks -> {
                    Platform.runLater(() -> {
                        List<Book> filteredBooks = filterBooksForCategory(allBooks);

                        // Se ancora non trova abbastanza, usa i primi libri disponibili
                        if (filteredBooks.size() < 3) {
                            filteredBooks = allBooks.subList(0, Math.min(12, allBooks.size()));
                        }

                        displayBooks(filteredBooks);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showErrorMessage("Errore caricamento libri: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Filtra i libri per la categoria corrente
     */
    private List<Book> filterBooksForCategory(List<Book> books) {
        List<Book> filtered = new ArrayList<>();
        String categoryName = category.getName().toLowerCase();
        String searchTerm = extractSearchTermFromCategory().toLowerCase();

        for (Book book : books) {
            boolean matches = false;

            // Controlla titolo
            if (book.getTitle() != null &&
                    (book.getTitle().toLowerCase().contains(searchTerm) ||
                            book.getTitle().toLowerCase().contains(categoryName))) {
                matches = true;
            }

            // Controlla descrizione
            if (!matches && book.getDescription() != null &&
                    (book.getDescription().toLowerCase().contains(searchTerm) ||
                            book.getDescription().toLowerCase().contains(categoryName))) {
                matches = true;
            }

            // Controlla autore (per biografie)
            if (!matches && "biografia".equals(categoryName) && book.getAuthor() != null) {
                matches = true; // Include tutti per biografie
            }

            if (matches) {
                filtered.add(book);
            }
        }

        System.out.println("📚 Filtrati " + filtered.size() + " libri per categoria " + categoryName);
        return filtered;
    }

    /**
     * Mostra i libri nella vista
     */
    private void displayBooks(List<Book> books) {
        // Rimuovi indicatore di caricamento
        content.getChildren().removeIf(node ->
                node instanceof VBox &&
                        ((VBox) node).getChildren().stream().anyMatch(child -> child instanceof ProgressIndicator)
        );

        if (books.isEmpty()) {
            showNoResults();
            return;
        }

        // Intestazione risultati
        Label resultsHeader = new Label(books.size() + " libri trovati");
        resultsHeader.setFont(Font.font("System", FontWeight.BOLD, 24));
        resultsHeader.setTextFill(Color.WHITE);
        resultsHeader.setPadding(new Insets(20, 0, 20, 0));

        content.getChildren().add(resultsHeader);

        // Griglia libri
        FlowPane booksGrid = new FlowPane();
        booksGrid.setHgap(20);
        booksGrid.setVgap(25);
        booksGrid.setPrefWrapLength(1000);
        booksGrid.setAlignment(Pos.CENTER_LEFT);

        for (Book book : books) {
            VBox bookCard = createBookCard(book);
            booksGrid.getChildren().add(bookCard);
        }

        content.getChildren().add(booksGrid);
    }

    /**
     * Mostra messaggio quando non ci sono risultati
     */
    private void showNoResults() {
        VBox noResultsBox = new VBox(15);
        noResultsBox.setAlignment(Pos.CENTER);
        noResultsBox.setPrefHeight(200);

        Label noResultsLabel = new Label("📚 Nessun libro trovato per questa categoria");
        noResultsLabel.setTextFill(Color.web("#8E8E93"));
        noResultsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));

        Label suggestionLabel = new Label("Prova a esplorare altre categorie o usa la ricerca");
        suggestionLabel.setTextFill(Color.web("#8E8E93"));
        suggestionLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        noResultsBox.getChildren().addAll(noResultsLabel, suggestionLabel);
        content.getChildren().add(noResultsBox);
    }

    /**
     * Mostra messaggio di errore
     */
    private void showErrorMessage(String message) {
        // Rimuovi indicatore di caricamento
        content.getChildren().removeIf(node ->
                node instanceof VBox &&
                        ((VBox) node).getChildren().stream().anyMatch(child -> child instanceof ProgressIndicator)
        );

        VBox errorBox = new VBox(15);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(200);

        Label errorLabel = new Label("❌ " + message);
        errorLabel.setTextFill(Color.web("#e74c3c"));
        errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        errorLabel.setWrapText(true);

        errorBox.getChildren().add(errorLabel);
        content.getChildren().add(errorBox);
    }

    /**
     * Crea una card per un libro
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        card.setMaxWidth(150);
        card.setPadding(new Insets(10));

        // ✅ CORRETTO: Usa ImageUtils come nelle altre sezioni che già funzionano
        ImageView bookCover = ImageUtils.createSafeImageView(book.getImageUrl(), 100, 140);

        // Applica clip arrotondato
        Rectangle clip = new Rectangle(100, 140);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        bookCover.setClip(clip);

        // Effetto ombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.4));
        shadow.setOffsetX(0);
        shadow.setOffsetY(6);
        shadow.setRadius(12);
        bookCover.setEffect(shadow);

        // Titolo libro
        Label title = new Label(book.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        title.setMaxWidth(150);
        title.setAlignment(Pos.CENTER);

        // Autore
        Label author = new Label(book.getAuthor());
        author.setFont(Font.font("System", FontWeight.NORMAL, 12));
        author.setTextFill(Color.web("#8E8E93"));
        author.setWrapText(true);
        author.setMaxWidth(150);
        author.setAlignment(Pos.CENTER);

        // Anno pubblicazione (se disponibile)
        if (book.getPublishYear() != null && !book.getPublishYear().trim().isEmpty()) {
            Label year = new Label(book.getPublishYear());
            year.setFont(Font.font("System", FontWeight.NORMAL, 10));
            year.setTextFill(Color.web("#8E8E93"));
            card.getChildren().addAll(bookCover, title, author, year);
        } else {
            card.getChildren().addAll(bookCover, title, author);
        }

        // Click handler
        card.setOnMouseClicked(e -> {
            if (bookClickHandler != null) {
                System.out.println("📖 Click libro categoria: " + book.getTitle());
                bookClickHandler.accept(book);
            }
        });

        // Effetti hover
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
            card.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            card.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
        });

        // Stile cursor
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    /**
     * Getter per la categoria
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Verifica se è in caricamento
     */
    public boolean isLoading() {
        return isLoading;
    }
}