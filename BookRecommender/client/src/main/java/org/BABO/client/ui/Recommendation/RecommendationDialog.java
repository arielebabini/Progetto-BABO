package org.BABO.client.ui.Recommendation;

import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.dto.Recommendation.RecommendationRequest;
import org.BABO.shared.dto.Recommendation.RecommendationResponse;
import org.BABO.client.service.ClientRecommendationService;
import org.BABO.client.service.LibraryService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Dialog per gestire le raccomandazioni di libri
 * Permette agli utenti di consigliare libri dalle proprie librerie
 */
public class RecommendationDialog {

    private Stage dialogStage;
    private Book targetBook;
    private String username;
    private AuthenticationManager authManager;
    private Consumer<List<BookRecommendation>> onRecommendationsSaved;

    // Servizi
    private ClientRecommendationService recommendationService;
    private LibraryService libraryService;

    // UI Components
    private VBox librariesContainer;
    private VBox selectedBooksContainer;
    private Button saveButton;
    private Button cancelButton;
    private Label statusLabel;
    private Label remainingSlotsLabel;

    // Dati
    private List<Book> selectedBooks;
    private List<String> userLibraries;
    private List<BookRecommendation> existingRecommendations;
    private int maxRecommendations = 3;
    private int currentRecommendationsCount = 0;

    /**
     * Costruttore
     */
    public RecommendationDialog(Book targetBook, String username, AuthenticationManager authManager,
                                Consumer<List<BookRecommendation>> onRecommendationsSaved) {
        this.targetBook = targetBook;
        this.username = username;
        this.authManager = authManager;
        this.onRecommendationsSaved = onRecommendationsSaved;
        this.selectedBooks = new ArrayList<>();
        this.recommendationService = new ClientRecommendationService();
        this.libraryService = new LibraryService();

        initializeDialog();
        createUI();
        loadUserData();
    }

    private void initializeDialog() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Consiglia Libri");
        dialogStage.setResizable(false);
    }

    private void createUI() {
        // Container principale SENZA sfondo scuro
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;"); // ✅ CAMBIATO: da rgba(0,0,0,0.3) a transparent

        // Dialog content
        VBox dialogContent = createDialogContent();
        root.getChildren().add(dialogContent);

        // ✅ FIX: Permetti ai controlli figli di ricevere eventi mouse
        root.setPickOnBounds(false);

        // Chiudi dialog cliccando fuori - GESTIONE MIGLIORATA
        root.setOnMouseClicked(e -> {
            // Chiudi solo se il click è DAVVERO sullo sfondo
            if (e.getTarget() == root && e.getSource() == root) {
                closeDialog();
            }
        });

        Scene scene = new Scene(root, 1000, 800);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
    }

    private VBox createDialogContent() {
        VBox content = new VBox(20);
        content.setMaxWidth(900);
        content.setMaxHeight(750);
        content.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 10);"
        );
        content.setAlignment(Pos.TOP_CENTER);

        // Header
        VBox header = createHeader();

        // Status e info
        VBox statusSection = createStatusSection();

        // Contenuto principale in scroll
        ScrollPane mainScroll = createMainContent();

        // Sezione libri selezionati
        VBox selectedSection = createSelectedBooksSection();

        // Bottoni
        HBox buttons = createButtonSection();

        content.getChildren().addAll(header, statusSection, mainScroll, selectedSection, buttons);
        return content;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        // Titolo
        Label titleLabel = new Label("💡 Consiglia libri per questo titolo");
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // Info libro target
        HBox bookInfo = createTargetBookInfo();

        header.getChildren().addAll(titleLabel, bookInfo);
        return header;
    }

    private HBox createTargetBookInfo() {
        HBox bookInfo = new HBox(15);
        bookInfo.setAlignment(Pos.CENTER);
        bookInfo.setStyle(
                "-fx-background-color: #383838;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        // Copertina
        ImageView coverImage = ImageUtils.createSafeImageView(targetBook.getImageUrl(), 50, 75);
        Rectangle coverClip = new Rectangle(50, 75);
        coverClip.setArcWidth(5);
        coverClip.setArcHeight(5);
        coverImage.setClip(coverClip);

        // Info
        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER_LEFT);

        Label bookTitle = new Label(targetBook.getTitle());
        bookTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        bookTitle.setTextFill(Color.WHITE);
        bookTitle.setWrapText(true);
        bookTitle.setMaxWidth(400);

        Label bookAuthor = new Label("di " + targetBook.getAuthor());
        bookAuthor.setFont(Font.font("SF Pro Text", 14));
        bookAuthor.setTextFill(Color.LIGHTGRAY);

        info.getChildren().addAll(bookTitle, bookAuthor);
        bookInfo.getChildren().addAll(coverImage, info);

        return bookInfo;
    }

    private VBox createStatusSection() {
        VBox statusSection = new VBox(8);
        statusSection.setAlignment(Pos.CENTER);

        statusLabel = new Label("📚 Caricamento...");
        statusLabel.setFont(Font.font("SF Pro Text", 14));
        statusLabel.setTextFill(Color.LIGHTGRAY);

        remainingSlotsLabel = new Label("");
        remainingSlotsLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        remainingSlotsLabel.setTextFill(Color.YELLOW);

        Label instructionLabel = new Label("Seleziona fino a 3 libri dalle tue librerie da consigliare");
        instructionLabel.setFont(Font.font("SF Pro Text", 12));
        instructionLabel.setTextFill(Color.GRAY);

        statusSection.getChildren().addAll(statusLabel, remainingSlotsLabel, instructionLabel);
        return statusSection;
    }

    private ScrollPane createMainContent() {
        librariesContainer = new VBox(15);
        librariesContainer.setAlignment(Pos.TOP_CENTER);
        librariesContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(librariesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: #2b2b2b;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );
        scrollPane.setPrefHeight(300);

        return scrollPane;
    }

    private VBox createSelectedBooksSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.TOP_CENTER);

        Label selectedTitle = new Label("📋 Libri selezionati per la raccomandazione:");
        selectedTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        selectedTitle.setTextFill(Color.WHITE);

        // Container per i libri selezionati con ScrollPane
        selectedBooksContainer = new VBox(8);
        selectedBooksContainer.setAlignment(Pos.TOP_CENTER);
        selectedBooksContainer.setPadding(new Insets(16));

        ScrollPane selectedBooksScrollPane = new ScrollPane(selectedBooksContainer);
        selectedBooksScrollPane.setFitToWidth(true);
        selectedBooksScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        selectedBooksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        selectedBooksScrollPane.setPrefHeight(160);
        selectedBooksScrollPane.setMaxHeight(180);
        selectedBooksScrollPane.setMinHeight(140);
        selectedBooksScrollPane.setStyle(
                "-fx-background: #383838;" +
                        "-fx-background-color: #383838;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 8;"
        );

        // Label per quando non ci sono libri selezionati
        Label emptyLabel = new Label("Nessun libro selezionato");
        emptyLabel.setTextFill(Color.GRAY);
        emptyLabel.setFont(Font.font("SF Pro Text", 12));
        emptyLabel.setStyle("-fx-padding: 30;");
        selectedBooksContainer.getChildren().add(emptyLabel);

        section.getChildren().addAll(selectedTitle, selectedBooksScrollPane);
        return section;
    }

    private HBox createButtonSection() {
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));

        // Pulsante Salva - COLORE SOBRIO
        saveButton = new Button("💾 Salva 3 Raccomandazioni");
        saveButton.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        saveButton.setStyle(
                "-fx-background-color: #4a7c59;" + // Verde scuro invece di verde acceso
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        // Effetto hover
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(
                "-fx-background-color: #5a8c69;" + // Verde leggermente più chiaro al hover
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
        ));

        saveButton.setOnMouseExited(e -> saveButton.setStyle(
                "-fx-background-color: #4a7c59;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        ));

        saveButton.setOnAction(e -> saveRecommendations());
        saveButton.setDisable(true); // Inizialmente disabilitato

        // Pulsante Annulla - STILE SOBRIO
        cancelButton = new Button("❌ Annulla");
        cancelButton.setFont(Font.font("SF Pro Text", FontWeight.NORMAL, 14));
        cancelButton.setStyle(
                "-fx-background-color: #6c757d;" + // Grigio sobrio
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                "-fx-background-color: #7c868d;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
        ));

        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                "-fx-background-color: #6c757d;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        ));

        cancelButton.setOnAction(e -> closeDialog());

        buttons.getChildren().addAll(cancelButton, saveButton);
        return buttons;
    }

    private void loadUserData() {
        // Verifica permessi e carica dati
        recommendationService.canUserRecommendAsync(username, targetBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getCanRecommend() != null && response.getCanRecommend()) {
                        currentRecommendationsCount = response.getCurrentRecommendationsCount() != null ?
                                response.getCurrentRecommendationsCount() : 0;
                        maxRecommendations = response.getMaxRecommendations() != null ?
                                response.getMaxRecommendations() : 3;

                        updateStatusLabels();
                        loadUserLibraries();
                        loadExistingRecommendations();
                    } else {
                        statusLabel.setText("❌ " + response.getMessage());
                        statusLabel.setTextFill(Color.LIGHTCORAL);
                        saveButton.setDisable(true);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.LIGHTCORAL);
                    });
                    return null;
                });
    }

    private void loadUserLibraries() {
        libraryService.getUserLibrariesAsync(username)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getLibraries() != null) {
                        userLibraries = response.getLibraries();
                        statusLabel.setText("✅ Caricamento completato");
                        statusLabel.setTextFill(Color.LIGHTGREEN);
                        displayLibraries();
                    } else {
                        statusLabel.setText("❌ Errore nel caricamento librerie");
                        statusLabel.setTextFill(Color.LIGHTCORAL);
                    }
                }));
    }

    private void loadExistingRecommendations() {
        recommendationService.getUserRecommendationsForBookAsync(username, targetBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getRecommendations() != null) {
                        existingRecommendations = response.getRecommendations();
                        updateStatusLabels();
                    }
                }));
    }

    private void displayLibraries() {
        librariesContainer.getChildren().clear();

        if (userLibraries == null || userLibraries.isEmpty()) {
            Label noLibrariesLabel = new Label("📭 Non hai librerie. Crea delle librerie per consigliare libri!");
            noLibrariesLabel.setFont(Font.font("SF Pro Text", 14));
            noLibrariesLabel.setTextFill(Color.LIGHTGRAY);
            librariesContainer.getChildren().add(noLibrariesLabel);
            return;
        }

        for (String libraryName : userLibraries) {
            VBox libraryCard = createLibraryCard(libraryName);
            librariesContainer.getChildren().add(libraryCard);
        }
    }

    private VBox createLibraryCard(String libraryName) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: #3a3a3c;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );

        // Header libreria
        Label libraryLabel = new Label("📚 " + libraryName);
        libraryLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        libraryLabel.setTextFill(Color.WHITE);

        // Container per i libri
        VBox booksContainer = new VBox(5);

        // Carica libri della libreria
        loadLibraryBooks(libraryName, booksContainer);

        card.getChildren().addAll(libraryLabel, booksContainer);
        return card;
    }

    private void loadLibraryBooks(String libraryName, VBox booksContainer) {
        Label loadingLabel = new Label("📖 Caricamento libri...");
        loadingLabel.setFont(Font.font("SF Pro Text", 12));
        loadingLabel.setTextFill(Color.GRAY);
        booksContainer.getChildren().add(loadingLabel);

        libraryService.getBooksInLibraryAsync(username, libraryName)
                .thenAccept(response -> Platform.runLater(() -> {
                    booksContainer.getChildren().clear();

                    if (response.isSuccess() && response.getBooks() != null) {
                        List<Book> books = response.getBooks();

                        if (books.isEmpty()) {
                            Label emptyLabel = new Label("📭 Libreria vuota");
                            emptyLabel.setFont(Font.font("SF Pro Text", 12));
                            emptyLabel.setTextFill(Color.GRAY);
                            booksContainer.getChildren().add(emptyLabel);
                        } else {
                            for (Book book : books) {
                                // Non mostrare il libro target nelle opzioni
                                if (!targetBook.getIsbn().equals(book.getIsbn())) {
                                    HBox bookCard = createBookCard(book);
                                    booksContainer.getChildren().add(bookCard);
                                }
                            }
                        }
                    } else {
                        Label errorLabel = new Label("❌ Errore caricamento: " + response.getMessage());
                        errorLabel.setFont(Font.font("SF Pro Text", 12));
                        errorLabel.setTextFill(Color.LIGHTCORAL);
                        booksContainer.getChildren().add(errorLabel);
                    }
                }));
    }

    private HBox createBookCard(Book book) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #4a4a4c;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8;"
        );

        // Info libro
        VBox bookInfo = new VBox(3);
        bookInfo.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(300);

        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.setFont(Font.font("SF Pro Text", 10));
        authorLabel.setTextFill(Color.LIGHTGRAY);

        bookInfo.getChildren().addAll(titleLabel, authorLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Checkbox per selezione
        CheckBox selectBox = new CheckBox();
        selectBox.setStyle("-fx-text-fill: white;");

        // Verifica se già selezionato o già raccomandato
        boolean alreadySelected = selectedBooks.contains(book);
        boolean alreadyRecommended = existingRecommendations != null &&
                existingRecommendations.stream().anyMatch(rec -> book.getIsbn().equals(rec.getRecommendedBookIsbn()));

        selectBox.setSelected(alreadySelected);
        selectBox.setDisable(alreadyRecommended);

        if (alreadyRecommended) {
            Label recommendedLabel = new Label("✅ Già consigliato");
            recommendedLabel.setFont(Font.font("SF Pro Text", 10));
            recommendedLabel.setTextFill(Color.LIGHTGREEN);
            card.getChildren().addAll(bookInfo, spacer, recommendedLabel);
        } else {
            selectBox.setOnAction(e -> {
                if (selectBox.isSelected()) {
                    if (selectedBooks.size() < getAvailableSlots()) {
                        selectedBooks.add(book);
                        updateSelectedBooksDisplay();
                        updateSaveButton();
                    } else {
                        selectBox.setSelected(false);
                        showAlert("⚠️ Limite raggiunto",
                                "Puoi selezionare al massimo " + getAvailableSlots() + " libri per le raccomandazioni.");
                    }
                } else {
                    selectedBooks.remove(book);
                    updateSelectedBooksDisplay();
                    updateSaveButton();
                }
            });

            card.getChildren().addAll(bookInfo, spacer, selectBox);
        }

        return card;
    }

    private void updateSelectedBooksDisplay() {
        selectedBooksContainer.getChildren().clear();

        if (selectedBooks.isEmpty()) {
            Label emptyLabel = new Label("Nessun libro selezionato");
            emptyLabel.setTextFill(Color.GRAY);
            emptyLabel.setFont(Font.font("SF Pro Text", 12));
            emptyLabel.setStyle("-fx-padding: 20;");
            selectedBooksContainer.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < selectedBooks.size(); i++) {
                Book book = selectedBooks.get(i);
                HBox bookRow = createSelectedBookRow(book, i + 1);
                selectedBooksContainer.getChildren().add(bookRow);
            }
        }

        // Aggiorna pulsante salva
        saveButton.setDisable(selectedBooks.size() != maxRecommendations);

        // Aggiorna label slot rimanenti
        int remaining = maxRecommendations - selectedBooks.size();
        remainingSlotsLabel.setText("Slot rimanenti: " + remaining + "/" + maxRecommendations);
    }

    private HBox createSelectedBookCard(Book book, int index) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #4CAF50;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8;"
        );

        Label numberLabel = new Label(String.valueOf(index + 1));
        numberLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        numberLabel.setTextFill(Color.WHITE);
        numberLabel.setMinWidth(20);

        VBox bookInfo = new VBox(3);
        bookInfo.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(300);

        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.setFont(Font.font("SF Pro Text", 10));
        authorLabel.setTextFill(Color.rgb(230, 255, 230));

        bookInfo.getChildren().addAll(titleLabel, authorLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeButton = new Button("✕");
        removeButton.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50;" +
                        "-fx-min-width: 25;" +
                        "-fx-min-height: 25;" +
                        "-fx-max-width: 25;" +
                        "-fx-max-height: 25;" +
                        "-fx-cursor: hand;"
        );
        removeButton.setOnAction(e -> {
            selectedBooks.remove(book);
            updateSelectedBooksDisplay();
            updateSaveButton();
            refreshLibraryCheckboxes();
        });

        card.getChildren().addAll(numberLabel, bookInfo, spacer, removeButton);
        return card;
    }

    private void refreshLibraryCheckboxes() {
        // Aggiorna lo stato delle checkbox quando un libro viene deselezionato
        displayLibraries();
    }

    private void updateSaveButton() {
        boolean hasSelections = !selectedBooks.isEmpty();
        saveButton.setDisable(!hasSelections);

        if (hasSelections) {
            saveButton.setText("Salva " + selectedBooks.size() + " Raccomandazioni");
        } else {
            saveButton.setText("Salva Raccomandazioni");
        }
    }

    private void updateStatusLabels() {
        int availableSlots = getAvailableSlots();

        if (availableSlots > 0) {
            remainingSlotsLabel.setText("📋 Puoi aggiungere ancora " + availableSlots + " raccomandazioni");
            remainingSlotsLabel.setTextFill(Color.YELLOW);
        } else {
            remainingSlotsLabel.setText("🚫 Hai raggiunto il limite massimo di raccomandazioni");
            remainingSlotsLabel.setTextFill(Color.LIGHTCORAL);
            saveButton.setDisable(true);
        }
    }

    private int getAvailableSlots() {
        return Math.max(0, maxRecommendations - currentRecommendationsCount);
    }

    private void saveRecommendations() {
        if (selectedBooks.isEmpty()) {
            showAlert("⚠️ Attenzione", "Seleziona almeno un libro da consigliare.");
            return;
        }

        saveButton.setDisable(true);
        saveButton.setText("Salvando...");

        List<CompletableFuture<RecommendationResponse>> futures = new ArrayList<>();

        // Crea una richiesta per ogni libro selezionato
        for (Book selectedBook : selectedBooks) {
            RecommendationRequest request = new RecommendationRequest(
                    username,
                    targetBook.getIsbn(),
                    selectedBook.getIsbn()
            );

            futures.add(recommendationService.addRecommendationAsync(request));
        }

        // Attendi tutte le richieste
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        int successCount = 0;
                        List<String> errors = new ArrayList<>();

                        for (int i = 0; i < futures.size(); i++) {
                            try {
                                RecommendationResponse response = futures.get(i).get();
                                if (response.isSuccess()) {
                                    successCount++;
                                } else {
                                    errors.add("• " + selectedBooks.get(i).getTitle() + ": " + response.getMessage());
                                }
                            } catch (Exception e) {
                                errors.add("• " + selectedBooks.get(i).getTitle() + ": Errore di connessione");
                            }
                        }

                        // Mostra risultati
                        if (successCount == selectedBooks.size()) {
                            showAlert("✅ Successo",
                                    "Tutte le " + successCount + " raccomandazioni sono state salvate con successo!");

                            // Notifica parent e chiudi
                            if (onRecommendationsSaved != null) {
                                // Ricarica le raccomandazioni aggiornate
                                loadUpdatedRecommendations();
                            } else {
                                closeDialog();
                            }
                        } else if (successCount > 0) {
                            String message = "Salvate " + successCount + " raccomandazioni su " + selectedBooks.size() +
                                    ".\n\nErrori:\n" + String.join("\n", errors);
                            showAlert("⚠️ Parzialmente completato", message);

                            if (onRecommendationsSaved != null) {
                                loadUpdatedRecommendations();
                            } else {
                                closeDialog();
                            }
                        } else {
                            String message = "Nessuna raccomandazione salvata.\n\nErrori:\n" + String.join("\n", errors);
                            showAlert("❌ Errore", message);

                            saveButton.setDisable(false);
                            saveButton.setText("Salva Raccomandazioni");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                        saveButton.setDisable(false);
                        saveButton.setText("Salva Raccomandazioni");
                    });
                    return null;
                });
    }

    private void loadUpdatedRecommendations() {
        recommendationService.getUserRecommendationsForBookAsync(username, targetBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && onRecommendationsSaved != null) {
                        onRecommendationsSaved.accept(response.getRecommendations());
                    }
                    closeDialog();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> closeDialog());
                    return null;
                });
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Mostra il dialog
     */
    public void show() {
        if (dialogStage != null) {
            dialogStage.show();
            dialogStage.centerOnScreen();
        }
    }

    /**
     * Mostra il dialog e attende la chiusura
     */
    public void showAndWait() {
        if (dialogStage != null) {
            dialogStage.showAndWait();
        }
    }

    /**
     * Metodo statico per creare e mostrare rapidamente un dialog di raccomandazione
     */
    public static void showRecommendationDialog(Book targetBook, String username,
                                                AuthenticationManager authManager,
                                                Consumer<List<BookRecommendation>> onRecommendationsSaved) {
        RecommendationDialog dialog = new RecommendationDialog(targetBook, username, authManager, onRecommendationsSaved);
        dialog.show();
    }

    private HBox createSelectedBookRow(Book book, int position) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle(
                "-fx-background-color: #4a7c59;" + // Verde scuro sobrio
                        "-fx-background-radius: 6;" +
                        "-fx-min-height: 40;"
        );

        // Numero posizione
        Label positionLabel = new Label(String.valueOf(position));
        positionLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        positionLabel.setTextFill(Color.WHITE);
        positionLabel.setStyle(
                "-fx-background-color: #2d5233;" + // Verde ancora più scuro
                        "-fx-background-radius: 15;" +
                        "-fx-min-width: 25;" +
                        "-fx-min-height: 25;" +
                        "-fx-alignment: center;"
        );

        // Info libro
        VBox bookInfo = new VBox(2);
        bookInfo.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setMaxWidth(400);
        titleLabel.setStyle("-fx-text-overrun: ellipsis;");

        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.setFont(Font.font("SF Pro Text", 10));
        authorLabel.setTextFill(Color.web("#E0E0E0"));
        authorLabel.setMaxWidth(400);
        authorLabel.setStyle("-fx-text-overrun: ellipsis;");

        bookInfo.getChildren().addAll(titleLabel, authorLabel);

        // Pulsante rimuovi
        Button removeButton = new Button("❌");
        removeButton.setFont(Font.font("SF Pro Text", 10));
        removeButton.setStyle(
                "-fx-background-color: #dc3545;" + // Rosso sobrio
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-min-width: 30;" +
                        "-fx-min-height: 30;" +
                        "-fx-cursor: hand;"
        );

        removeButton.setOnMouseEntered(e -> removeButton.setStyle(
                "-fx-background-color: #e85a67;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-min-width: 30;" +
                        "-fx-min-height: 30;" +
                        "-fx-cursor: hand;"
        ));

        removeButton.setOnMouseExited(e -> removeButton.setStyle(
                "-fx-background-color: #dc3545;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-min-width: 30;" +
                        "-fx-min-height: 30;" +
                        "-fx-cursor: hand;"
        ));

        removeButton.setOnAction(e -> {
            selectedBooks.remove(book);
            updateSelectedBooksDisplay();
            // Aggiorna anche i pulsanti di selezione nella libreria
            refreshLibraryDisplay();
        });

        // Spacer per spingere il pulsante remove a destra
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(positionLabel, bookInfo, spacer, removeButton);
        return row;
    }

    private void refreshLibraryDisplay() {
        loadUserLibraries();
    }
}