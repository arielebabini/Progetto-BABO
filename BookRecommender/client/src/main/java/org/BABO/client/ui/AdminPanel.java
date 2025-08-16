package org.BABO.client.ui;

import org.BABO.client.service.AdminService;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.BookRating;
import org.BABO.shared.model.Review;
import org.BABO.shared.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Pannello di gestione amministrativa degli utenti
 */
public class AdminPanel {

    private final AuthenticationManager authManager;
    private final AdminService adminService;
    private TableView<User> usersTable;
    private ObservableList<User> usersData;
    private Label statusLabel;

    private TableView<Book> booksTable;
    private ObservableList<Book> booksData;
    private VBox currentContent;

    private VBox mainAdminPanel;

    private ObservableList<Book> allBooksData;
    private TextField searchField;

    private TableView<BookRating> reviewsTable;
    private ObservableList<BookRating> reviewsData;
    private ObservableList<Review> allReviewsData;
    private TextField reviewsSearchField;

    public AdminPanel(AuthenticationManager authManager) {
        this.authManager = authManager;
        this.adminService = new AdminService();
        this.usersData = FXCollections.observableArrayList();
        this.booksData = FXCollections.observableArrayList();
        this.currentContent = new VBox();

        initializeBooksCoversDirectory();

        this.allBooksData = FXCollections.observableArrayList();

        this.reviewsData = FXCollections.observableArrayList();
        this.allReviewsData = FXCollections.observableArrayList();
    }


    public VBox createAdminPanel() {
        mainAdminPanel = new VBox(20);
        mainAdminPanel.setPadding(new Insets(30));
        mainAdminPanel.setStyle("-fx-background-color: #1e1e1e;");

        // Header
        VBox header = createHeader();

        // Menu di selezione
        VBox menuContainer = createAdminMenu();


        mainAdminPanel.getChildren().addAll(header, menuContainer);

        return mainAdminPanel;
    }

    /**
     * Crea il menu principale di amministrazione
     */
    private VBox createAdminMenu() {
        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));

        // Titolo menu
        Label menuTitle = new Label("🔧 Pannello Amministrazione");
        menuTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        menuTitle.setTextFill(Color.WHITE);
        menuTitle.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Scegli cosa vuoi gestire:");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.LIGHTGRAY);
        subtitle.setAlignment(Pos.CENTER);

        // Contenitore per i pulsanti - MODIFICATO per 3 pulsanti in orizzontale
        HBox buttonsContainer = new HBox(30);
        buttonsContainer.setAlignment(Pos.CENTER);

        // Pulsante gestione utenti
        VBox usersCard = createMenuCard(
                "👥",
                "Gestione Utenti",
                "Visualizza, elimina e gestisci\ngli utenti registrati",
                "#6c5ce7",
                e -> {
                    System.out.println("🖱️ Click rilevato su Gestione Utenti");
                    showUsersManagement();
                }
        );

        // Pulsante gestione libri
        VBox booksCard = createMenuCard(
                "📚",
                "Gestione Libri",
                "Aggiungi, modifica ed elimina\ni libri dal catalogo",
                "#00b894",
                e -> {
                    System.out.println("🖱️ Click rilevato su Gestione Libri");
                    showBooksManagement();
                }
        );

        // NUOVO: Pulsante gestione recensioni
        VBox reviewsCard = createMenuCard(
                "⭐",
                "Gestione Recensioni",
                "Visualizza e gestisci\nle recensioni dei libri",
                "#fd79a8",
                e -> {
                    System.out.println("🖱️ Click rilevato su Gestione Recensioni");
                    showReviewsManagement();
                }
        );

        buttonsContainer.getChildren().addAll(usersCard, booksCard, reviewsCard);

        // Info admin
        Label adminInfo = new Label("👑 Connesso come: " + authManager.getCurrentUser().getEmail());
        adminInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        adminInfo.setTextFill(Color.LIGHTBLUE);

        container.getChildren().addAll(menuTitle, subtitle, buttonsContainer, adminInfo);
        return container;
    }

    /**
     * Mostra la gestione recensioni
     */
    private void showReviewsManagement() {
        System.out.println("🔄 Passaggio a gestione recensioni...");

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            // Header
            VBox header = createHeader();

            // Toolbar semplificato
            HBox toolbar = createReviewsToolbar();

            // Contenuto semplificato
            currentContent = new VBox(20);
            currentContent.setAlignment(Pos.CENTER);
            currentContent.setPadding(new Insets(30));

            // Titolo sezione
            Label titleLabel = new Label("⭐ Gestione Recensioni");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setAlignment(Pos.CENTER);

            // Messaggio stato
            Label statusMessage = new Label("📝 Funzionalità gestione recensioni implementata!\n\n" +
                    "✅ Backend completo con endpoint REST\n" +
                    "✅ Controlli di sicurezza admin\n" +
                    "✅ Database integration\n" +
                    "🔄 Interface utente in sviluppo...\n\n" +
                    "Usa gli endpoint API direttamente per testare le funzionalità.");
            statusMessage.setFont(Font.font("System", FontWeight.NORMAL, 16));
            statusMessage.setTextFill(Color.LIGHTGRAY);
            statusMessage.setAlignment(Pos.CENTER);
            statusMessage.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            // Pulsante per ricaricare (placeholder)
            Button testButton = new Button("🔧 Test API Connection");
            testButton.setPrefWidth(200);
            testButton.setPrefHeight(40);
            styleButton(testButton, "#4a90e2");
            testButton.setOnAction(e -> testReviewsAPI());

            // Container per centrare tutto
            VBox contentContainer = new VBox(30);
            contentContainer.setAlignment(Pos.CENTER);
            contentContainer.getChildren().addAll(titleLabel, statusMessage, testButton);

            currentContent.getChildren().add(contentContainer);

            // Status bar
            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Crea toolbar per gestione recensioni
     */
    private HBox createReviewsToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button backButton = new Button("⬅️ Torna al Menu");
        styleButton(backButton, "#95a5a6");
        backButton.setOnAction(e -> backToMainMenu());

        Button refreshButton = new Button("🔄 Aggiorna");
        styleButton(refreshButton, "#4a86e8");
        refreshButton.setOnAction(e -> {
            statusLabel.setText("🔄 Funzionalità in sviluppo...");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sectionLabel = new Label("⭐ Gestione Recensioni");
        sectionLabel.setTextFill(Color.WHITE);
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        toolbar.getChildren().addAll(backButton, refreshButton, spacer, sectionLabel);
        return toolbar;
    }

    /**
     * Esporta le recensioni (placeholder)
     */
    private void exportReviews() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📊 Esportazione");
        alert.setHeaderText("Funzionalità in sviluppo");
        alert.setContentText("L'esportazione delle recensioni sarà implementata prossimamente.");
        alert.showAndWait();
    }

    /**
     * Crea la barra di ricerca per le recensioni
     */
    /*
    private HBox createReviewsSearchBar() {
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(5, 0, 5, 0));

        // Icona ricerca
        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("System", 14));
        searchIcon.setTextFill(Color.LIGHTGRAY);

        // Campo di ricerca
        reviewsSearchField = new TextField();
        reviewsSearchField.setPromptText("Cerca per utente, libro, rating o testo recensione...");
        reviewsSearchField.setPrefWidth(450);
        reviewsSearchField.setStyle(
                "-fx-background-color: #3b3b3b; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #888; " +
                        "-fx-border-color: #9b59b6; " + // Bordo viola per tema recensioni
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 8;"
        );

        // Listener per ricerca in tempo reale
        reviewsSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReviews(newValue);
        });

        // Pulsante clear
        Button clearButton = new Button("❌");
        clearButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 3; " +
                        "-fx-background-radius: 3; " +
                        "-fx-padding: 5 8 5 8;"
        );
        clearButton.setOnAction(e -> {
            reviewsSearchField.clear();
            filterReviews(""); // Mostra tutte le recensioni
        });

        // Info risultati
        Label resultsInfo = new Label();
        resultsInfo.setTextFill(Color.LIGHTGRAY);
        resultsInfo.setFont(Font.font("System", 11));

        searchContainer.getChildren().addAll(searchIcon, reviewsSearchField, clearButton, resultsInfo);

        return searchContainer;
    }
*/
    /**
     * Crea la tabella delle recensioni
     */
    /*
    private void createReviewsTable() {
        reviewsTable = new TableView<>();
        reviewsTable.setItems(reviewsData);
        reviewsTable.setPrefHeight(500);
        reviewsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Colonna Username
        TableColumn<BookRating, String> usernameCol = new TableColumn<>("👤 Utente");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        // Colonna ISBN
        TableColumn<BookRating, String> isbnCol = new TableColumn<>("📚 ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(130);

        // Colonna Media voti
        TableColumn<BookRating, Double> averageCol = new TableColumn<>("⭐ Media");
        averageCol.setCellValueFactory(new PropertyValueFactory<>("average"));
        averageCol.setPrefWidth(80);
        averageCol.setCellFactory(col -> new TableCell<BookRating, Double>() {
            @Override
            protected void updateItem(Double average, boolean empty) {
                super.updateItem(average, empty);
                if (empty || average == null) {
                    setText("");
                } else {
                    setText(String.format("%.1f/5", average));
                    // Colora in base al voto
                    if (average >= 4.0) {
                        setTextFill(Color.LIGHTGREEN);
                    } else if (average >= 3.0) {
                        setTextFill(Color.YELLOW);
                    } else {
                        setTextFill(Color.LIGHTCORAL);
                    }
                }
            }
        });

        // Colonna Data
        TableColumn<BookRating, String> dateCol = new TableColumn<>("📅 Data");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("data"));
        dateCol.setPrefWidth(150);
        dateCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String dateString, boolean empty) {
                super.updateItem(dateString, empty);
                if (empty || dateString == null) {
                    setText("");
                } else {
                    // Mostra solo la data senza orario
                    try {
                        if (dateString.contains("T")) {
                            setText(dateString.split("T")[0]);
                        } else {
                            setText(dateString);
                        }
                    } catch (Exception e) {
                        setText(dateString);
                    }
                }
            }
        });

        // Colonna Recensione (anteprima)
        TableColumn<BookRating, String> reviewCol = new TableColumn<>("💬 Recensione");
        reviewCol.setCellValueFactory(new PropertyValueFactory<>("review"));
        reviewCol.setPrefWidth(300);
        reviewCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null || review.trim().isEmpty()) {
                    setText("(nessuna recensione)");
                    setTextFill(Color.GRAY);
                } else {
                    // Mostra i primi 100 caratteri
                    String preview = review.length() > 100 ?
                            review.substring(0, 100) + "..." : review;
                    setText(preview);
                    setTextFill(Color.WHITE);
                }
            }
        });

        // Colonna Azioni
        TableColumn<BookRating, Void> actionsCol = new TableColumn<>("🔧 Azioni");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<BookRating, Void>() {
            private final Button viewButton = new Button("👁️ Visualizza");
            private final Button editButton = new Button("✏️ Modifica");
            private final Button deleteButton = new Button("🗑️ Elimina");

            {
                // Stile pulsanti
                styleActionButton(viewButton, "#3498db");
                styleActionButton(editButton, "#f39c12");
                styleActionButton(deleteButton, "#e74c3c");

                // Azioni pulsanti
                viewButton.setOnAction(e -> {
                    BookRating rating = getTableView().getItems().get(getIndex());
                    viewReviewDetails(rating);
                });

                editButton.setOnAction(e -> {
                    BookRating rating = getTableView().getItems().get(getIndex());
                    editReview(rating);
                });

                deleteButton.setOnAction(e -> {
                    BookRating rating = getTableView().getItems().get(getIndex());
                    deleteReview(rating);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.setAlignment(Pos.CENTER);
                    buttons.getChildren().addAll(viewButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        reviewsTable.getColumns().addAll(usernameCol, isbnCol, averageCol, dateCol, reviewCol, actionsCol);

        // Stile tabella
        reviewsTable.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-text-background-color: white;" +
                        "-fx-selection-bar: #4a90e2;" +
                        "-fx-selection-bar-non-focused: #4a90e2;"
        );
    }*/

    private void styleActionButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10px;" +
                        "-fx-padding: 3px 8px;" +
                        "-fx-background-radius: 3;" +
                        "-fx-cursor: hand;"
        );
        button.setPrefWidth(60);
        button.setPrefHeight(25);
    }

    /**
     * Filtra le recensioni
     */


    /**
     * Verifica se una recensione corrisponde al criterio di ricerca
     */
    private boolean matchesReviewSearch(Review review, String searchText) {
        if (review == null || searchText == null || searchText.isEmpty()) {
            return true;
        }

        // Cerca nell'username
        if (review.getUsername() != null &&
                review.getUsername().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca nel titolo del libro
        if (review.getBookTitle() != null &&
                review.getBookTitle().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca nel testo della recensione
        if (review.getReviewText() != null &&
                review.getReviewText().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca nel rating (convertito a stringa)
        if (String.valueOf(review.getRating()).contains(searchText)) {
            return true;
        }

        return false;
    }

    /**
     * Carica le recensioni (placeholder - da implementare con AdminService)
     */
    private void loadReviews() {
        System.out.println("📝 Caricamento recensioni...");

        if (statusLabel != null) {
            statusLabel.setText("⏳ Caricamento recensioni...");
        }

        // Usa AdminService per recuperare tutte le recensioni
        adminService.getAllReviewsAsync()
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.containsKey("success") && (Boolean) response.get("success")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> reviewsMapList = (List<Map<String, Object>>) response.get("reviews");

                            reviewsData.clear();

                            for (Map<String, Object> reviewMap : reviewsMapList) {
                                BookRating rating = mapToBookRating(reviewMap);
                                if (rating != null) {
                                    reviewsData.add(rating);
                                }
                            }

                            if (statusLabel != null) {
                                statusLabel.setText("✅ Caricate " + reviewsData.size() + " recensioni");
                            }

                            System.out.println("✅ Caricate " + reviewsData.size() + " recensioni");
                        } else {
                            String error = (String) response.getOrDefault("message", "Errore sconosciuto");
                            if (statusLabel != null) {
                                statusLabel.setText("❌ Errore: " + error);
                            }
                            System.err.println("❌ Errore nel caricamento recensioni: " + error);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText("❌ Errore di connessione");
                        }
                        System.err.println("❌ Errore di connessione nel caricamento recensioni: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private BookRating mapToBookRating(Map<String, Object> map) {
        try {
            BookRating rating = new BookRating();

            rating.setUsername((String) map.get("username"));
            rating.setIsbn((String) map.get("isbn"));
            rating.setData((String) map.get("data"));
            rating.setReview((String) map.get("review"));

            // Converti i numeri in Integer/Double
            if (map.get("style") != null) rating.setStyle(((Number) map.get("style")).intValue());
            if (map.get("content") != null) rating.setContent(((Number) map.get("content")).intValue());
            if (map.get("pleasantness") != null) rating.setPleasantness(((Number) map.get("pleasantness")).intValue());
            if (map.get("originality") != null) rating.setOriginality(((Number) map.get("originality")).intValue());
            if (map.get("edition") != null) rating.setEdition(((Number) map.get("edition")).intValue());
            if (map.get("average") != null) rating.setAverage(((Number) map.get("average")).doubleValue());

            return rating;
        } catch (Exception e) {
            System.err.println("❌ Errore nella conversione Map->BookRating: " + e.getMessage());
            return null;
        }
    }

    /**
     * Elimina recensione selezionata
     */
    /*
    private void deleteSelectedReview() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();

        if (selectedReview == null) {
            showAlert("Attenzione", "Seleziona una recensione da eliminare");
            return;
        }

        // Conferma eliminazione
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminazione Recensione");
        confirmAlert.setContentText("Sei sicuro di voler eliminare la recensione di " +
                selectedReview.getUsername() + " per \"" +
                selectedReview.getBookTitle() + "\"?\n\n" +
                "Questa azione non può essere annullata.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Procedi con l'eliminazione
            statusLabel.setText("🗑️ Eliminazione recensione in corso...");
            statusLabel.setTextFill(Color.ORANGE);

            String adminEmail = authManager.getCurrentUser().getEmail();

            adminService.deleteReviewAsync(adminEmail, selectedReview.getId())
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            statusLabel.setText("✅ Recensione eliminata con successo");
                            statusLabel.setTextFill(Color.LIGHTGREEN);

                            // Ricarica la lista
                            loadReviews();

                        } else {
                            statusLabel.setText("❌ Errore eliminazione: " + response.getMessage());
                            statusLabel.setTextFill(Color.RED);
                            showAlert("Errore", "Impossibile eliminare la recensione: " + response.getMessage());
                        }
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            statusLabel.setText("❌ Errore di connessione");
                            statusLabel.setTextFill(Color.RED);
                            showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                        });
                        return null;
                    });
        }
    }*/

    /**
     * Elimina tutte le recensioni di un utente
     */
    /*private void deleteAllUserReviews() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();

        if (selectedReview == null) {
            showAlert("Attenzione", "Seleziona una recensione per identificare l'utente");
            return;
        }

        String targetUsername = selectedReview.getUsername();

        // Conta quante recensioni ha l'utente
        long userReviewsCount = allReviewsData.stream()
                .filter(review -> targetUsername.equals(review.getUsername()))
                .count();

        if (userReviewsCount == 0) {
            showAlert("Informazione", "L'utente " + targetUsername + " non ha recensioni da eliminare");
            return;
        }

        // Conferma eliminazione massiva
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Eliminazione Massiva");
        confirmAlert.setHeaderText("Eliminazione Tutte le Recensioni Utente");
        confirmAlert.setContentText("Sei sicuro di voler eliminare TUTTE le " + userReviewsCount +
                " recensioni dell'utente \"" + targetUsername + "\"?\n\n" +
                "Questa azione non può essere annullata e rimuoverà permanentemente " +
                "tutte le recensioni testuali dell'utente.");

        // Aggiungi pulsante personalizzato
        ButtonType eliminaTutteButton = new ButtonType("Elimina Tutte", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(eliminaTutteButton, annullaButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == eliminaTutteButton) {
            // Procedi con l'eliminazione massiva
            statusLabel.setText("🚫 Eliminazione recensioni utente in corso...");
            statusLabel.setTextFill(Color.ORANGE);

            String adminEmail = authManager.getCurrentUser().getEmail();

            adminService.deleteAllUserReviewsAsync(adminEmail, targetUsername)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            statusLabel.setText("✅ " + response.getMessage());
                            statusLabel.setTextFill(Color.LIGHTGREEN);

                            // Ricarica la lista
                            loadReviews();

                        } else {
                            statusLabel.setText("❌ Errore: " + response.getMessage());
                            statusLabel.setTextFill(Color.RED);
                            showAlert("Errore", "Impossibile eliminare le recensioni: " + response.getMessage());
                        }
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            statusLabel.setText("❌ Errore di connessione");
                            statusLabel.setTextFill(Color.RED);
                            showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                        });
                        return null;
                    });
        }
    }
*/
    /**
     * Aggiorna info risultati per recensioni
     */
    /*
    private void updateReviewsResultsInfo(Label resultsLabel, int shown, int total) {
        if (shown == total) {
            resultsLabel.setText(total + " recensioni totali");
        } else {
            resultsLabel.setText(shown + " di " + total + " recensioni");
        }
    }

    private void showReviewDetails() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();

        if (selectedReview == null) {
            showAlert("Attenzione", "Seleziona una recensione per vedere i dettagli");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Dettagli Recensione");
        dialog.setHeaderText("Recensione di " + selectedReview.getUsername());

        // Contenuto del dialog
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        // Informazioni libro
        Label bookInfo = new Label("📚 Libro: " + selectedReview.getBookTitle() +
                " di " + selectedReview.getBookAuthor());
        bookInfo.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Rating
        Label ratingInfo = new Label("⭐ Rating: " + selectedReview.getRatingStars() +
                " (" + selectedReview.getRating() + "/5)");
        ratingInfo.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Data
        Label dateInfo = new Label("📅 Data: " +
                (selectedReview.getCreatedAt() != null ?
                        selectedReview.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) :
                        "N/A"));
        dateInfo.setFont(Font.font("System", FontWeight.NORMAL, 11));

        // Testo recensione
        Label reviewLabel = new Label("📝 Recensione:");
        reviewLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextArea reviewText = new TextArea(selectedReview.getReviewText());
        reviewText.setEditable(false);
        reviewText.setPrefRowCount(8);
        reviewText.setWrapText(true);

        content.getChildren().addAll(bookInfo, ratingInfo, dateInfo, reviewLabel, reviewText);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
*/
    /**
     * Crea una card per il menu amministrativo
     */
    private VBox createMenuCard(String icon, String title, String description, String color, javafx.event.EventHandler<javafx.scene.input.MouseEvent> action) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setPrefWidth(250);
        card.setPrefHeight(180);
        card.setMaxWidth(250);
        card.setMaxHeight(180);

        // Stile base della card
        card.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-cursor: hand;"
        );

        // Icona
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        iconLabel.setTextFill(Color.web(color));

        // Titolo
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setAlignment(Pos.CENTER);

        // Descrizione
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        descLabel.setTextFill(Color.LIGHTGRAY);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        // Effetti hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #3b3b3b;" +
                            "-fx-background-radius: 15px;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-cursor: hand;" +
                            "-fx-scale-x: 1.05;" +
                            "-fx-scale-y: 1.05;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: #2b2b2b;" +
                            "-fx-background-radius: 15px;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-cursor: hand;" +
                            "-fx-scale-x: 1.0;" +
                            "-fx-scale-y: 1.0;"
            );
        });

        // Click handler con debug
        card.setOnMouseClicked(e -> {
            System.out.println("🖱️ Click su card: " + title); // ✅ DEBUG
            if (action != null) {
                action.handle(e);
            } else {
                System.err.println("❌ Action è null per card: " + title);
            }
        });

        return card;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⚙️ Gestione");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Pannello di amministrazione.");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.LIGHTGRAY);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createUsersTable() {
        VBox container = new VBox(10);

        Label tableTitle = new Label("📋 Utenti Registrati");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.WHITE);

        usersTable = new TableView<>();
        usersTable.setItems(usersData);
        usersTable.setPrefHeight(400);

        // Stile tabella
        usersTable.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #444;" +
                        "-fx-border-width: 1;"
        );

        // ✅ FIX: Colonna ID con gestione esplicita del valore
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String id = user.getId();
            System.out.println("🔍 Debug ID utente: " + (id != null ? id : "NULL"));
            return new javafx.beans.property.SimpleStringProperty(id != null ? id : "N/A");
        });
        idCol.setPrefWidth(80);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String username = user.getUsername();
            return new javafx.beans.property.SimpleStringProperty(username != null ? username : "N/A");
        });
        usernameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String email = user.getEmail();
            return new javafx.beans.property.SimpleStringProperty(email != null ? email : "N/A");
        });
        emailCol.setPrefWidth(250);

        TableColumn<User, String> nameCol = new TableColumn<>("Nome Completo");
        nameCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String fullName = "";
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                fullName += user.getName().trim();
            }
            if (user.getSurname() != null && !user.getSurname().trim().isEmpty()) {
                if (!fullName.isEmpty()) fullName += " ";
                fullName += user.getSurname().trim();
            }
            if (fullName.isEmpty()) {
                fullName = "N/A";
            }
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });
        nameCol.setPrefWidth(150);

        TableColumn<User, String> statusCol = new TableColumn<>("Stato");
        statusCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("Attivo");
        });
        statusCol.setPrefWidth(100);

        usersTable.getColumns().addAll(idCol, usernameCol, emailCol, nameCol, statusCol);

        container.getChildren().addAll(tableTitle, usersTable);
        return container;
    }

    /**
     * Crea la tabella per la gestione libri
     */
    private void createBooksTable() {
        booksTable = new TableView<>();
        booksTable.setItems(booksData);
        booksTable.setPrefHeight(400);

        // Stile tabella
        booksTable.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #444;" +
                        "-fx-border-width: 1;"
        );

        // ✅ NUOVA COLONNA: Copertina
        TableColumn<Book, String> coverCol = new TableColumn<>("Copertina");
        coverCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("preview");
        });
        coverCol.setCellFactory(col -> new TableCell<Book, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(30);
                imageView.setFitHeight(45);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Book book = getTableRow().getItem();
                if (book != null) {
                    loadCoverPreview(book, imageView);
                    setGraphic(imageView);
                }
            }
        });
        coverCol.setPrefWidth(60);
        coverCol.setSortable(false);

        // Colonna ISBN
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String isbn = book.getIsbn();
            return new javafx.beans.property.SimpleStringProperty(isbn != null ? isbn : "N/A");
        });
        isbnCol.setPrefWidth(120);

        // Colonna Titolo
        TableColumn<Book, String> titleCol = new TableColumn<>("Titolo");
        titleCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String title = book.getTitle();
            return new javafx.beans.property.SimpleStringProperty(title != null ? title : "N/A");
        });
        titleCol.setPrefWidth(180); // Ridotto per fare spazio alla copertina

        // Colonna Autore
        TableColumn<Book, String> authorCol = new TableColumn<>("Autore");
        authorCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String author = book.getAuthor();
            return new javafx.beans.property.SimpleStringProperty(author != null ? author : "N/A");
        });
        authorCol.setPrefWidth(130); // Ridotto

        // Colonna Anno
        TableColumn<Book, String> yearCol = new TableColumn<>("Anno");
        yearCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String year = book.getPublishYear();
            return new javafx.beans.property.SimpleStringProperty(year != null ? year : "N/A");
        });
        yearCol.setPrefWidth(70); // Ridotto

        // Colonna Categoria
        TableColumn<Book, String> categoryCol = new TableColumn<>("Categoria");
        categoryCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String category = book.getCategory();
            return new javafx.beans.property.SimpleStringProperty(category != null ? category : "N/A");
        });
        categoryCol.setPrefWidth(100); // Ridotto

        booksTable.getColumns().addAll(coverCol, isbnCol, titleCol, authorCol, yearCol, categoryCol);
    }

    /**
     * Carica la lista dei libri
     */
    private void loadBooks() {
        statusLabel.setText("📚 Caricamento libri...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.getAllBooksAsync(adminEmail)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        // ✅ Salva tutti i libri in allBooksData
                        allBooksData.clear();
                        if (response.getBooks() != null) {
                            allBooksData.addAll(response.getBooks());
                        }

                        // ✅ Inizialmente mostra tutti i libri
                        booksData.clear();
                        booksData.addAll(allBooksData);

                        // ✅ Aggiorna info risultati
                        updateResultsInfo();

                        statusLabel.setText("✅ " + allBooksData.size() + " libri caricati");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Errore: " + response.getMessage());
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile caricare i libri: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Elimina il libro selezionato
     */
    private void deleteSelectedBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert("Attenzione", "Seleziona un libro da eliminare");
            return;
        }

        // Verifica che l'ISBN non sia null o vuoto
        if (selectedBook.getIsbn() == null || selectedBook.getIsbn().trim().isEmpty()) {
            showAlert("Errore", "ISBN libro non valido. Aggiorna la lista e riprova.");
            return;
        }

        // Conferma eliminazione
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminare il libro selezionato?");
        confirmAlert.setContentText(
                "Stai per eliminare:\n" +
                        "ISBN: " + selectedBook.getIsbn() + "\n" +
                        "Titolo: " + selectedBook.getTitle() + "\n" +
                        "Autore: " + selectedBook.getAuthor() + "\n\n" +
                        "Questa operazione non può essere annullata."
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeleteBook(selectedBook);
        }
    }

    /**
     * Esegue l'eliminazione del libro
     */
    private void performDeleteBook(Book book) {
        statusLabel.setText("🗑️ Eliminazione in corso...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.deleteBookAsync(adminEmail, book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        booksData.remove(book);
                        statusLabel.setText("✅ Libro eliminato con successo");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Eliminazione fallita");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile eliminare il libro: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Mostra il dialog per aggiungere un nuovo libro
     */
    private void showAddBookDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Aggiungi Nuovo Libro");
        dialog.setHeaderText("Inserisci i dettagli del nuovo libro");

        // Crea i campi del form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");
        TextField titleField = new TextField();
        titleField.setPromptText("Titolo");
        TextField authorField = new TextField();
        authorField.setPromptText("Autore");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Descrizione");
        descriptionField.setPrefRowCount(3);
        TextField yearField = new TextField();
        yearField.setPromptText("Anno pubblicazione");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Categoria");

        // ✅ NUOVO: Campo per caricamento copertina
        HBox coverBox = new HBox(10);
        coverBox.setAlignment(Pos.CENTER_LEFT);

        ImageView coverPreview = new ImageView();
        coverPreview.setFitWidth(60);
        coverPreview.setFitHeight(90);
        coverPreview.setPreserveRatio(true);
        coverPreview.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

        Button selectCoverButton = new Button("📁 Seleziona Copertina");
        styleButton(selectCoverButton, "#3498db");

        Label coverStatus = new Label("Nessuna immagine selezionata");
        coverStatus.setTextFill(Color.GRAY);
        coverStatus.setFont(Font.font("System", 10));

        // ✅ Wrapper class per memorizzare il file selezionato (per evitare problema final)
        class CoverFileHolder {
            File selectedFile = null;
        }
        final CoverFileHolder coverHolder = new CoverFileHolder();

        selectCoverButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleziona Copertina Libro");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );

            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                try {
                    // Carica e verifica l'immagine
                    Image image = new Image(new FileInputStream(file));

                    if (image.isError()) {
                        showAlert("Errore", "Impossibile caricare l'immagine selezionata.");
                        return;
                    }

                    // Verifica le proporzioni (range 100x200)
                    double width = image.getWidth();
                    double height = image.getHeight();
                    double ratio = height / width;

                    // Controllo proporzioni: altezza deve essere tra 1x e 2x la larghezza
                    if (ratio < 1.0 || ratio > 2.0) {
                        showAlert("Proporzioni non valide",
                                String.format("L'immagine deve avere proporzioni nel range 1:1 e 1:2 (larghezza:altezza).\n" +
                                        "Proporzioni attuali: %.2f:1\n" +
                                        "Dimensioni: %.0fx%.0f", ratio, width, height));
                        return;
                    }

                    // Aggiorna preview
                    coverPreview.setImage(image);
                    coverHolder.selectedFile = file;  // ✅ USO WRAPPER CLASS
                    coverStatus.setText("✅ " + file.getName());
                    coverStatus.setTextFill(Color.GREEN);

                } catch (Exception ex) {
                    showAlert("Errore", "Errore durante il caricamento dell'immagine: " + ex.getMessage());
                }
            }
        });

        VBox coverContainer = new VBox(5);
        coverContainer.getChildren().addAll(coverBox, coverStatus);
        coverBox.getChildren().addAll(coverPreview, selectCoverButton);

        // Layout grid
        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(isbnField, 1, 0);
        grid.add(new Label("Titolo:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Autore:"), 0, 2);
        grid.add(authorField, 1, 2);
        grid.add(new Label("Descrizione:"), 0, 3);
        grid.add(descriptionField, 1, 3);
        grid.add(new Label("Anno:"), 0, 4);
        grid.add(yearField, 1, 4);
        grid.add(new Label("Categoria:"), 0, 5);
        grid.add(categoryField, 1, 5);
        grid.add(new Label("Copertina:"), 0, 6);
        grid.add(coverContainer, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Pulsanti
        ButtonType addButtonType = new ButtonType("Aggiungi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Validazione e conversione risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("isbn", isbnField.getText());
                result.put("title", titleField.getText());
                result.put("author", authorField.getText());
                result.put("description", descriptionField.getText());
                result.put("year", yearField.getText());
                result.put("category", categoryField.getText());

                // ✅ GESTIONE COPERTINA con wrapper class
                if (coverHolder.selectedFile != null) {
                    try {
                        String targetFileName = saveCoverImageWithDebug(coverHolder.selectedFile, isbnField.getText());
                        result.put("coverFileName", targetFileName);
                    } catch (Exception ex) {
                        showAlert("Errore", "Errore durante il salvataggio della copertina: " + ex.getMessage());
                        return null;
                    }
                }

                return result;
            }
            return null;
        });

        // Mostra dialog e gestisci risultato
        Optional<Map<String, String>> result = dialog.showAndWait();

        result.ifPresent(bookData -> {
            // Validazione base
            if (bookData.get("isbn").trim().isEmpty() ||
                    bookData.get("title").trim().isEmpty() ||
                    bookData.get("author").trim().isEmpty()) {
                showAlert("Errore", "ISBN, titolo e autore sono obbligatori");
                return;
            }

            // Aggiungi libro
            addNewBook(bookData);
        });
    }

    /**
     * Salva l'immagine di copertina nella cartella books_covers
     */
    private String saveCoverImageWithDebug(File sourceFile, String isbn) throws IOException {
        // Validazione ISBN
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IOException("ISBN non può essere vuoto");
        }

        String cleanIsbn = isbn.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        System.out.println("  ISBN pulito: '" + cleanIsbn + "'");

        if (cleanIsbn.isEmpty()) {
            throw new IOException("ISBN non contiene caratteri validi");
        }

        String targetFileName = cleanIsbn + ".jpg";
        System.out.println("  Nome file target: '" + targetFileName + "'");

        // Percorso di destinazione
        String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
        Path targetDir = Paths.get(resourcesPath);
        Path targetPath = targetDir.resolve(targetFileName);

        System.out.println("  Percorso completo: " + targetPath);

        // Crea la directory se non esiste
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            System.out.println("  ✅ Creata directory: " + targetDir);
        } else {
            System.out.println("  ✅ Directory già esistente");
        }

        try {
            // Verifica che il file sorgente esista
            if (!sourceFile.exists()) {
                throw new IOException("File sorgente non trovato: " + sourceFile.getAbsolutePath());
            }
            System.out.println("  ✅ File sorgente verificato");

            // Verifica dimensioni del file (max 5MB)
            long fileSize = Files.size(sourceFile.toPath());
            System.out.println("  📏 Dimensione file: " + (fileSize / 1024) + " KB");

            if (fileSize > 5 * 1024 * 1024) {
                throw new IOException("File troppo grande (max 5MB). Dimensione: " + (fileSize / 1024 / 1024) + "MB");
            }

            // Copia il file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✅ File copiato con successo");

            // Verifica che il file sia stato copiato correttamente
            if (!Files.exists(targetPath)) {
                throw new IOException("Errore durante la copia: file non creato");
            }

            System.out.println("  ✅ Verifica post-copia: file presente");
            System.out.println("  📁 File finale: " + targetPath.toAbsolutePath());

            return targetFileName;

        } catch (IOException e) {
            System.err.println("  ❌ Errore salvataggio copertina: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Errore durante il salvataggio della copertina: " + e.getMessage());
        }
    }

    /**
     * Inizializza la directory books_covers se non esiste
     */
    private void initializeBooksCoversDirectory() {
        try {
            String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
            Path targetDir = Paths.get(resourcesPath);

            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
                System.out.println("📁 Directory books_covers creata: " + targetDir);
            } else {
                System.out.println("📁 Directory books_covers esistente: " + targetDir);
            }

            // Verifica che ci sia almeno placeholder.jpg
            Path placeholderPath = targetDir.resolve("placeholder.jpg");
            if (!Files.exists(placeholderPath)) {
                System.out.println("⚠️ Warning: placeholder.jpg non trovato in " + targetDir);
            }

        } catch (IOException e) {
            System.err.println("❌ Errore creazione directory books_covers: " + e.getMessage());
        }
    }

    /**
     * Carica l'anteprima della copertina per un libro
     */
    private void loadCoverPreview(Book book, ImageView imageView) {
        try {
            // Genera nome file copertina basato su ISBN
            String coverFileName = null;
            if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
                String cleanIsbn = book.getIsbn().toUpperCase().replaceAll("[^A-Z0-9]", "");
                coverFileName = cleanIsbn + ".jpg";
            }

            if (coverFileName != null) {
                // Percorso copertina
                String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
                Path coverPath = Paths.get(resourcesPath, coverFileName);

                if (Files.exists(coverPath)) {
                    // Carica la copertina personalizzata
                    try (InputStream inputStream = Files.newInputStream(coverPath)) {
                        Image coverImage = new Image(inputStream, 30, 45, true, true);
                        if (!coverImage.isError()) {
                            imageView.setImage(coverImage);
                            return;
                        }
                    }
                }
            }

            // Fallback: carica placeholder
            loadPlaceholderImage(imageView);

        } catch (Exception e) {
            System.err.println("❌ Errore caricamento anteprima copertina: " + e.getMessage());
            loadPlaceholderImage(imageView);
        }
    }

    /**
     * Carica l'immagine placeholder
     */
    private void loadPlaceholderImage(ImageView imageView) {
        try {
            // Prova placeholder locale
            String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
            Path placeholderPath = Paths.get(resourcesPath, "placeholder.jpg");

            if (Files.exists(placeholderPath)) {
                try (InputStream inputStream = Files.newInputStream(placeholderPath)) {
                    Image placeholderImage = new Image(inputStream, 30, 45, true, true);
                    if (!placeholderImage.isError()) {
                        imageView.setImage(placeholderImage);
                        return;
                    }
                }
            }

            // Fallback: crea immagine semplice
            createSimplePlaceholder(imageView);

        } catch (Exception e) {
            createSimplePlaceholder(imageView);
        }
    }

    /**
     * Aggiunge un nuovo libro
     */
    private void addNewBook(Map<String, String> bookData) {
        statusLabel.setText("📚 Aggiunta libro in corso...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        // ✅ RENDI coverInfo FINAL
        final String coverInfo;
        if (bookData.containsKey("coverFileName")) {
            coverInfo = " (con copertina: " + bookData.get("coverFileName") + ")";
        } else {
            coverInfo = "";
        }

        System.out.println("📚 Aggiunta libro: " + bookData.get("title") + coverInfo);

        adminService.addBookAsync(
                        adminEmail,
                        bookData.get("isbn"),
                        bookData.get("title"),
                        bookData.get("author"),
                        bookData.get("description"),
                        bookData.get("year"),
                        bookData.get("category")
                ).thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        statusLabel.setText("✅ Libro aggiunto con successo" + coverInfo); // ✅ ORA È FINAL
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                        // Ricarica la lista libri
                        loadBooks();

                    } else {
                        statusLabel.setText("❌ Aggiunta fallita");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile aggiungere il libro: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }

    /**
     * Mostra la gestione utenti
     */
    private void showUsersManagement() {
        System.out.println("🔄 Passaggio a gestione utenti..."); // Debug

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            // Header
            VBox header = createHeader();

            // Toolbar per utenti
            HBox toolbar = createUsersToolbar();

            // Contenuto utenti
            currentContent = new VBox(20);
            VBox tableContainer = createUsersTable();
            currentContent.getChildren().add(tableContainer);

            // Status bar
            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);

            // Carica dati utenti
            loadUsers();
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Mostra la gestione libri
     */
    private void showBooksManagement() {
        System.out.println("📄 Passaggio a gestione libri..."); // Debug

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            // Header
            VBox header = createHeader();

            // Toolbar per libri
            HBox toolbar = createBooksToolbar();

            // Contenuto libri
            currentContent = new VBox(20);
            VBox container = new VBox(10);

            Label tableTitle = new Label("📚 Gestione Libri");
            tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            tableTitle.setTextFill(Color.WHITE);

            // Pulsante aggiungi libro
            Button addBookButton = new Button("➕ Aggiungi Nuovo Libro");
            styleButton(addBookButton, "#27ae60");
            addBookButton.setOnAction(e -> showAddBookDialog());

            // ✅ NUOVA BARRA DI RICERCA
            HBox searchContainer = createSearchBar();

            // Tabella libri
            createBooksTable();

            container.getChildren().addAll(tableTitle, addBookButton, searchContainer, booksTable);
            currentContent.getChildren().add(container);

            // Status bar
            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);

            // Carica dati libri
            loadBooks();
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Crea la barra di ricerca per i libri
     */
    private HBox createSearchBar() {
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(5, 0, 5, 0));

        // Icona ricerca
        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("System", 14));
        searchIcon.setTextFill(Color.LIGHTGRAY);

        // Campo di ricerca
        searchField = new TextField();
        searchField.setPromptText("Cerca per ISBN, titolo, autore o categoria...");
        searchField.setPrefWidth(400);
        searchField.setStyle(
                "-fx-background-color: #3b3b3b; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #888; " +
                        "-fx-border-color: #555; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 8;"
        );

        // Listener per ricerca in tempo reale
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBooks(newValue);
        });

        // Pulsante clear
        Button clearButton = new Button("❌");
        clearButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 3; " +
                        "-fx-background-radius: 3; " +
                        "-fx-padding: 5 8 5 8;"
        );
        clearButton.setOnAction(e -> {
            searchField.clear();
            filterBooks(""); // Mostra tutti i libri
        });

        // Info risultati
        Label resultsInfo = new Label();
        resultsInfo.setTextFill(Color.LIGHTGRAY);
        resultsInfo.setFont(Font.font("System", 11));
        updateResultsInfo(resultsInfo, 0, 0); // Inizialmente vuoto

        searchContainer.getChildren().addAll(searchIcon, searchField, clearButton, resultsInfo);

        return searchContainer;
    }

    /**
     * Filtra i libri in base al testo di ricerca
     */
    private void filterBooks(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // Mostra tutti i libri
            booksData.setAll(allBooksData);
        } else {
            String lowerSearchText = searchText.toLowerCase().trim();

            // Filtra i libri che contengono il testo di ricerca
            booksData.setAll(
                    allBooksData.stream()
                            .filter(book -> matchesSearch(book, lowerSearchText))
                            .collect(java.util.stream.Collectors.toList())
            );
        }

        // Aggiorna info risultati
        updateResultsInfo();
    }

    /**
     * Verifica se un libro corrisponde al criterio di ricerca
     */
    private boolean matchesSearch(Book book, String searchText) {
        if (book == null || searchText == null || searchText.isEmpty()) {
            return true;
        }

        // Cerca in ISBN
        if (book.getIsbn() != null &&
                book.getIsbn().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in titolo
        if (book.getTitle() != null &&
                book.getTitle().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in autore
        if (book.getAuthor() != null &&
                book.getAuthor().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in categoria
        if (book.getCategory() != null &&
                book.getCategory().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in anno (convertito a stringa)
        if (book.getPublishYear() != null &&
                book.getPublishYear().toLowerCase().contains(searchText)) {
            return true;
        }

        return false;
    }

    /**
     * Aggiorna le informazioni sui risultati di ricerca
     */
    private void updateResultsInfo() {
        // Trova il label dei risultati nel searchContainer
        if (currentContent != null && currentContent.getChildren().size() > 0) {
            VBox container = (VBox) currentContent.getChildren().get(0);
            for (javafx.scene.Node node : container.getChildren()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    // Cerca il label dei risultati (ultimo elemento)
                    if (hbox.getChildren().size() > 3) {
                        javafx.scene.Node lastNode = hbox.getChildren().get(hbox.getChildren().size() - 1);
                        if (lastNode instanceof Label) {
                            Label resultsLabel = (Label) lastNode;
                            updateResultsInfo(resultsLabel, booksData.size(), allBooksData.size());
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Aggiorna il testo delle informazioni sui risultati
     */
    private void updateResultsInfo(Label resultsLabel, int shown, int total) {
        if (shown == total) {
            resultsLabel.setText(total + " libri totali");
        } else {
            resultsLabel.setText(shown + " di " + total + " libri");
        }
    }

    /**
     * Crea un placeholder semplice quando non ci sono immagini
     */
    private void createSimplePlaceholder(ImageView imageView) {
        imageView.setImage(null);
    }

    /**
     * Crea toolbar specifico per gestione utenti
     */
    private HBox createUsersToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button backButton = new Button("⬅️ Torna al Menu");
        styleButton(backButton, "#95a5a6");
        backButton.setOnAction(e -> backToMainMenu());

        Button refreshButton = new Button("🔄 Aggiorna");
        styleButton(refreshButton, "#4a86e8");
        refreshButton.setOnAction(e -> loadUsers());

        Button deleteButton = new Button("🗑️ Elimina Selezionato");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> deleteSelectedUser());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sectionLabel = new Label("👥 Gestione Utenti");
        sectionLabel.setTextFill(Color.WHITE);
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        toolbar.getChildren().addAll(backButton, refreshButton, deleteButton, spacer, sectionLabel);
        return toolbar;
    }

    /**
     * Crea toolbar specifico per gestione libri
     */
    private HBox createBooksToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button backButton = new Button("⬅️ Torna al Menu");
        styleButton(backButton, "#95a5a6");
        backButton.setOnAction(e -> backToMainMenu());

        Button refreshButton = new Button("🔄 Aggiorna");
        styleButton(refreshButton, "#4a86e8");
        refreshButton.setOnAction(e -> loadBooks());

        Button deleteButton = new Button("🗑️ Elimina Selezionato");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> deleteSelectedBook());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sectionLabel = new Label("📚 Gestione Libri");
        sectionLabel.setTextFill(Color.WHITE);
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        toolbar.getChildren().addAll(backButton, refreshButton, deleteButton, spacer, sectionLabel);
        return toolbar;
    }

    /**
     * Torna al menu principale
     */
    private void backToMainMenu() {
        System.out.println("🔄 Tornando al menu principale...");

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            // Header
            VBox header = createHeader();

            // Menu di selezione
            VBox menuContainer = createAdminMenu();

            mainAdminPanel.getChildren().addAll(header, menuContainer);
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    private void styleButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(color, "derive(" + color + ", 20%)"))
        );
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace("derive(" + color + ", 20%)", color))
        );
    }

    private void loadUsers() {
        statusLabel.setText("🔄 Caricamento utenti...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.getAllUsersAsync(adminEmail)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getUsers() != null) {
                        usersData.clear();
                        usersData.addAll(response.getUsers());

                        statusLabel.setText("✅ Caricati " + response.getUsers().size() + " utenti");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Errore: " + response.getMessage());
                        statusLabel.setTextFill(Color.RED);

                        showAlert("Errore", "Impossibile caricare gli utenti: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void deleteSelectedUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("Attenzione", "Seleziona un utente da eliminare");
            return;
        }

        System.out.println("🔍 DEBUG Utente selezionato:");
        System.out.println("   ID: " + selectedUser.getId());
        System.out.println("   Username: " + selectedUser.getUsername());
        System.out.println("   Email: " + selectedUser.getEmail());
        System.out.println("   Nome: " + selectedUser.getName());
        System.out.println("   Cognome: " + selectedUser.getSurname());

        // Verifica che l'ID non sia null o vuoto
        if (selectedUser.getId() == null || selectedUser.getId().trim().isEmpty()) {
            showAlert("Errore", "ID utente non valido. Aggiorna la lista e riprova.");
            return;
        }

        // Conferma eliminazione
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminare l'utente selezionato?");
        confirmAlert.setContentText(
                "Stai per eliminare:\n" +
                        "ID: " + selectedUser.getId() + "\n" +
                        "Username: " + selectedUser.getUsername() + "\n" +
                        "Email: " + selectedUser.getEmail() + "\n\n" +
                        "Questa operazione non può essere annullata."
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeleteUser(selectedUser);
        }
    }

    private void performDeleteUser(User user) {
        statusLabel.setText("🗑️ Eliminazione in corso...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.deleteUserAsync(String.valueOf(user.getId()), adminEmail)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        usersData.remove(user);
                        statusLabel.setText("✅ Utente eliminato con successo");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Eliminazione fallita");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile eliminare l'utente: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Testa la connessione API per le recensioni
     */
    private void testReviewsAPI() {
        if (statusLabel != null) {
            statusLabel.setText("🔄 Test connessione API recensioni...");
        }

        // Test semplice della connessione
        adminService.getAllReviewsAsync()
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.containsKey("success") && (Boolean) response.get("success")) {
                            if (statusLabel != null) {
                                statusLabel.setText("✅ API recensioni funzionante - " +
                                        response.getOrDefault("total", "0") + " recensioni trovate");
                            }

                            // Mostra alert di successo
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("✅ Test API Successful");
                            alert.setHeaderText("Connessione API Recensioni");
                            alert.setContentText("Le API per la gestione recensioni funzionano correttamente!\n\n" +
                                    "Recensioni trovate: " + response.getOrDefault("total", "0"));
                            alert.showAndWait();

                        } else {
                            if (statusLabel != null) {
                                statusLabel.setText("❌ Errore API: " + response.getOrDefault("message", "Errore sconosciuto"));
                            }

                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("⚠️ Test API Failed");
                            alert.setHeaderText("Problema API Recensioni");
                            alert.setContentText("Errore: " + response.getOrDefault("message", "Errore sconosciuto"));
                            alert.showAndWait();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText("❌ Errore di connessione");
                        }

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("❌ Connection Error");
                        alert.setHeaderText("Errore di Connessione");
                        alert.setContentText("Impossibile connettersi al server:\n" + throwable.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

}
