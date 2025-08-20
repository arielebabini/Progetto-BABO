package org.BABO.client.ui.Admin;

import org.BABO.client.service.AdminService;
import org.BABO.client.ui.Authentication.AuthenticationManager;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

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


    /**
     * Crea il pannello per admin
     */
    public VBox createAdminPanel() {
        mainAdminPanel = new VBox(20);
        mainAdminPanel.setPadding(new Insets(30));
        mainAdminPanel.setStyle("-fx-background-color: #1e1e1e;");

        VBox header = createHeader();

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

        HBox buttonsContainer = new HBox(30);
        buttonsContainer.setAlignment(Pos.CENTER);

        // Pulsante gestione utenti
        VBox usersCard = createMenuCard(
                "👥",
                "Gestione Utenti",
                "Visualizza, elimina e gestisci\ngli utenti registrati",
                "#6c5ce7",
                e -> {
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
                    showBooksManagement();
                }
        );

        // Pulsante gestione recensioni
        VBox reviewsCard = createMenuCard(
                "⭐",
                "Gestione Recensioni",
                "Visualizza e gestisci\nle recensioni dei libri",
                "#fd79a8",
                e -> {
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

        card.setOnMouseClicked(e -> {
            if (action != null) {
                action.handle(e);
            } else {
                System.err.println("❌ Action è null per card: " + title);
            }
        });

        return card;
    }

    /**
     * Crea header del pannello admin
     */
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

    /**
     * ===================================
     * GESTIONE UTENTI
     * ===================================
     */

    /**
     * Crea tabella di gestione utenti
     */
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

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String id = user.getId();
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
     * Mostra la gestione utenti
     */
    private void showUsersManagement() {
        System.out.println("🔄 Passaggio a gestione utenti...");

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
     * Carica gli utenti
     */
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

    /**
     * Elimina l'utente selezionato
     */
    private void deleteSelectedUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("Attenzione", "Seleziona un utente da eliminare");
            return;
        }

        System.out.println("   ELIMINAZIONE--> Utente selezionato:");
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

    /**
     * Verifica di conferma eliminazione utente
     */
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

    /**
     * ===================================
     * GESTIONE LIBRI
     * ===================================
     */

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
        titleCol.setPrefWidth(180);

        // Colonna Autore
        TableColumn<Book, String> authorCol = new TableColumn<>("Autore");
        authorCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String author = book.getAuthor();
            return new javafx.beans.property.SimpleStringProperty(author != null ? author : "N/A");
        });
        authorCol.setPrefWidth(130);

        // Colonna Anno
        TableColumn<Book, String> yearCol = new TableColumn<>("Anno");
        yearCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String year = book.getPublishYear();
            return new javafx.beans.property.SimpleStringProperty(year != null ? year : "N/A");
        });
        yearCol.setPrefWidth(70);

        // Colonna Categoria
        TableColumn<Book, String> categoryCol = new TableColumn<>("Categoria");
        categoryCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String category = book.getCategory();
            return new javafx.beans.property.SimpleStringProperty(category != null ? category : "N/A");
        });
        categoryCol.setPrefWidth(100);

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
                        allBooksData.clear();
                        if (response.getBooks() != null) {
                            allBooksData.addAll(response.getBooks());
                        }

                        booksData.clear();
                        booksData.addAll(allBooksData);

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

        if (selectedBook.getIsbn() == null || selectedBook.getIsbn().trim().isEmpty()) {
            showAlert("Errore", "ISBN libro non valido. Aggiorna la lista e riprova.");
            return;
        }

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

                    // Verifica le proporzioni
                    double width = image.getWidth();
                    double height = image.getHeight();
                    double ratio = height / width;

                    // Controllo proporzioni
                    if (ratio < 1.0 || ratio > 2.0) {
                        showAlert("Proporzioni non valide",
                                String.format("L'immagine deve avere proporzioni nel range 1:1 e 1:2 (larghezza:altezza).\n" +
                                        "Proporzioni attuali: %.2f:1\n" +
                                        "Dimensioni: %.0fx%.0f", ratio, width, height));
                        return;
                    }

                    // Aggiorna preview
                    coverPreview.setImage(image);
                    coverHolder.selectedFile = file;
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

        Optional<Map<String, String>> result = dialog.showAndWait();

        result.ifPresent(bookData -> {
            if (bookData.get("isbn").trim().isEmpty() ||
                    bookData.get("title").trim().isEmpty() ||
                    bookData.get("author").trim().isEmpty()) {
                showAlert("Errore", "ISBN, titolo e autore sono obbligatori");
                return;
            }

            addNewBook(bookData);
        });
    }

    /**
     * Salva l'immagine di copertina nella cartella books_covers
     */
    private String saveCoverImageWithDebug(File sourceFile, String isbn) throws IOException {
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

        String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
        Path targetDir = Paths.get(resourcesPath);
        Path targetPath = targetDir.resolve(targetFileName);

        System.out.println("  Percorso completo: " + targetPath);

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            System.out.println("  ✅ Creata directory: " + targetDir);
        } else {
            System.out.println("  ✅ Directory già esistente");
        }

        try {
            if (!sourceFile.exists()) {
                throw new IOException("File sorgente non trovato: " + sourceFile.getAbsolutePath());
            }
            System.out.println("  ✅ File sorgente verificato");

            long fileSize = Files.size(sourceFile.toPath());
            System.out.println("  📏 Dimensione file: " + (fileSize / 1024) + " KB");

            if (fileSize > 5 * 1024 * 1024) {
                throw new IOException("File troppo grande (max 5MB). Dimensione: " + (fileSize / 1024 / 1024) + "MB");
            }

            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✅ File copiato con successo");

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
            String coverFileName = null;
            if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
                String cleanIsbn = book.getIsbn().toUpperCase().replaceAll("[^A-Z0-9]", "");
                coverFileName = cleanIsbn + ".jpg";
            }

            if (coverFileName != null) {
                String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
                Path coverPath = Paths.get(resourcesPath, coverFileName);

                if (Files.exists(coverPath)) {
                    try (InputStream inputStream = Files.newInputStream(coverPath)) {
                        Image coverImage = new Image(inputStream, 30, 45, true, true);
                        if (!coverImage.isError()) {
                            imageView.setImage(coverImage);
                            return;
                        }
                    }
                }
            }

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
                        statusLabel.setText("✅ Libro aggiunto con successo" + coverInfo);
                        statusLabel.setTextFill(Color.LIGHTGREEN);

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
     * Mostra la gestione libri
     */
    private void showBooksManagement() {
        System.out.println("📄 Passaggio a gestione libri..."); // Debug

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            VBox header = createHeader();

            HBox toolbar = createBooksToolbar();

            currentContent = new VBox(20);
            VBox container = new VBox(10);

            Label tableTitle = new Label("📚 Gestione Libri");
            tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            tableTitle.setTextFill(Color.WHITE);

            Button addBookButton = new Button("➕ Aggiungi Nuovo Libro");
            styleButton(addBookButton, "#27ae60");
            addBookButton.setOnAction(e -> showAddBookDialog());

            HBox searchContainer = createSearchBar();

            createBooksTable();

            container.getChildren().addAll(tableTitle, addBookButton, searchContainer, booksTable);
            currentContent.getChildren().add(container);

            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);

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

        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("System", 14));
        searchIcon.setTextFill(Color.LIGHTGRAY);

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

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBooks(newValue);
        });

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
            filterBooks("");
        });

        Label resultsInfo = new Label();
        resultsInfo.setTextFill(Color.LIGHTGRAY);
        resultsInfo.setFont(Font.font("System", 11));
        updateResultsInfo(resultsInfo, 0, 0);

        searchContainer.getChildren().addAll(searchIcon, searchField, clearButton, resultsInfo);

        return searchContainer;
    }

    /**
     * Filtra i libri in base al testo di ricerca
     */
    private void filterBooks(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            booksData.setAll(allBooksData);
        } else {
            String lowerSearchText = searchText.toLowerCase().trim();

            booksData.setAll(
                    allBooksData.stream()
                            .filter(book -> matchesSearch(book, lowerSearchText))
                            .collect(java.util.stream.Collectors.toList())
            );
        }

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
        if (currentContent != null && currentContent.getChildren().size() > 0) {
            VBox container = (VBox) currentContent.getChildren().get(0);
            for (javafx.scene.Node node : container.getChildren()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
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
     * ==========================
     * GESTIONE RECENSIONI
     * ==========================
     */

    /**
     * Mostra la gestione recensioni
     */
    private void showReviewsManagement() {
        System.out.println("📄 Passaggio a gestione recensioni...");

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            VBox header = createHeader();

            HBox toolbar = createReviewsToolbar();

            currentContent = new VBox(20);
            VBox container = new VBox(10);

            Label tableTitle = new Label("⭐ Gestione Recensioni");
            tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            tableTitle.setTextFill(Color.WHITE);

            HBox searchContainer = createReviewsSearchBar();

            createReviewsTable();

            container.getChildren().addAll(tableTitle, searchContainer, reviewsTable);
            currentContent.getChildren().add(container);

            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);

            loadReviewsData();
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Barra di ricerca per gestione recensioni
     */
    private HBox createReviewsSearchBar() {
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(10, 0, 10, 0));

        Label searchLabel = new Label("🔍");
        searchLabel.setTextFill(Color.LIGHTGRAY);
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        reviewsSearchField = new TextField();
        reviewsSearchField.setPromptText("Cerca per utente, ISBN o contenuto recensione...");
        reviewsSearchField.setPrefWidth(300);
        reviewsSearchField.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #888888; " +
                        "-fx-border-color: #444444; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;"
        );

        reviewsSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReviews(newValue);
        });

        Button clearButton = new Button("✕");
        styleButton(clearButton, "#e74c3c");
        clearButton.setPrefWidth(30);
        clearButton.setOnAction(e -> {
            reviewsSearchField.clear();
            filterReviews("");
        });

        searchContainer.getChildren().addAll(searchLabel, reviewsSearchField, clearButton);
        return searchContainer;
    }


    /**
     * Filtra le recensioni
     */
    private void filterReviews(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            reviewsTable.setItems(reviewsData);
        } else {
            ObservableList<BookRating> filteredData = FXCollections.observableArrayList();
            String lowerCaseFilter = searchText.toLowerCase();

            for (BookRating rating : reviewsData) {
                if ((rating.getUsername() != null && rating.getUsername().toLowerCase().contains(lowerCaseFilter)) ||
                        (rating.getIsbn() != null && rating.getIsbn().toLowerCase().contains(lowerCaseFilter)) ||
                        (rating.getReview() != null && rating.getReview().toLowerCase().contains(lowerCaseFilter))) {
                    filteredData.add(rating);
                }
            }
            reviewsTable.setItems(filteredData);
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
        refreshButton.setOnAction(e -> loadReviewsData());

        Button deleteButton = new Button("🗑️ Elimina Selezionata");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> deleteSelectedReview());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sectionLabel = new Label("⭐ Gestione Recensioni");
        sectionLabel.setTextFill(Color.WHITE);
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        toolbar.getChildren().addAll(backButton, refreshButton, deleteButton, spacer, sectionLabel);
        return toolbar;
    }

    /**
     * Crea la tabella delle recensioni
     */
    private void createReviewsTable() {
        reviewsTable = new TableView<>();
        reviewsTable.setItems(reviewsData);
        reviewsTable.setPrefHeight(500);
        reviewsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reviewsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        reviewsTable.setStyle(
                "-fx-selection-bar: #E0E0E0; " +
                        "-fx-selection-bar-non-focused: #F0F0F0; " +
                        "-fx-text-fill: black;"
        );

        // Colonna Username
        TableColumn<BookRating, String> usernameCol = new TableColumn<>("👤 Utente");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        // Colonna ISBN
        TableColumn<BookRating, String> isbnCol = new TableColumn<>("📚 ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(120);

        // Colonna Voti dettagliati
        TableColumn<BookRating, String> votesCol = new TableColumn<>("📊 Voti");
        votesCol.setPrefWidth(100);
        votesCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    BookRating rating = getTableView().getItems().get(getIndex());
                    String votesText = String.format("S:%d C:%d P:%d O:%d E:%d",
                            rating.getStyle() != null ? rating.getStyle() : 0,
                            rating.getContent() != null ? rating.getContent() : 0,
                            rating.getPleasantness() != null ? rating.getPleasantness() : 0,
                            rating.getOriginality() != null ? rating.getOriginality() : 0,
                            rating.getEdition() != null ? rating.getEdition() : 0);
                    setText(votesText);

                    // Colore condizionale per selezione
                    if (!isSelected()) {
                        setTextFill(Color.STEELBLUE);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

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

                    // Colore condizionale in base a valutazione
                    if (!isSelected()) {
                        if (average >= 4.0) {
                            setTextFill(Color.FORESTGREEN);
                        } else if (average >= 3.0) {
                            setTextFill(Color.GOLDENROD);
                        } else {
                            setTextFill(Color.CRIMSON);
                        }
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        // Colonna Recensione
        TableColumn<BookRating, String> reviewCol = new TableColumn<>("💬 Recensione");
        reviewCol.setCellValueFactory(new PropertyValueFactory<>("review"));
        reviewCol.setPrefWidth(250);
        reviewCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String reviewText, boolean empty) {
                super.updateItem(reviewText, empty);
                if (empty || reviewText == null || reviewText.trim().isEmpty()) {
                    setText("");
                    setOnMouseClicked(null);
                } else {
                    // Tronca il testo se è troppo lungo
                    String displayText = reviewText.length() > 40 ?
                            reviewText.substring(0, 37) + "..." : reviewText;
                    setText(displayText);
                    setTextFill(Color.BLACK);

                    setTooltip(new Tooltip(reviewText));

                    setOnMouseClicked(e -> {
                        if (e.getClickCount() == 1) { // Click singolo
                            showFullReviewDialog(reviewText, getTableView().getItems().get(getIndex()));
                        }
                    });

                    setStyle("-fx-cursor: hand;");
                }
            }
        });

        // Colonna Data
        TableColumn<BookRating, String> dateCol = new TableColumn<>("📅 Data");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("data"));
        dateCol.setPrefWidth(120);
        dateCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("");
                } else {
                    setText(date.substring(0, Math.min(10, date.length())));
                    setTextFill(Color.BLACK);
                }
            }
        });

        reviewsTable.getColumns().addAll(usernameCol, isbnCol, votesCol, averageCol, reviewCol, dateCol);
    }

    /**
     * Metodo per visualizzazione completa della recensione
     */
    private void showFullReviewDialog(String reviewText, BookRating rating) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("📖 Recensione Completa");
        dialog.setHeaderText("Recensione di " + rating.getUsername() + " per ISBN: " + rating.getIsbn());

        TextArea textArea = new TextArea(reviewText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);

        String additionalInfo = String.format(
                "\n\n📊 Dettaglio Voti:\n" +
                        "• Stile: %d/5\n" +
                        "• Contenuto: %d/5\n" +
                        "• Piacevolezza: %d/5\n" +
                        "• Originalità: %d/5\n" +
                        "• Edizione: %d/5\n" +
                        "• Media: %.1f/5\n" +
                        "• Data: %s",
                rating.getStyle() != null ? rating.getStyle() : 0,
                rating.getContent() != null ? rating.getContent() : 0,
                rating.getPleasantness() != null ? rating.getPleasantness() : 0,
                rating.getOriginality() != null ? rating.getOriginality() : 0,
                rating.getEdition() != null ? rating.getEdition() : 0,
                rating.getAverage() != null ? rating.getAverage() : 0.0,
                rating.getData() != null ? rating.getData() : "N/A"
        );

        textArea.setText(reviewText + additionalInfo);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().setPrefSize(600, 400);

        dialog.showAndWait();
    }

    /**
     * Carica le recensioni
     */
    private void loadReviewsData() {
        System.out.println("🔄 Caricamento recensioni dal database...");

        if (statusLabel != null) {
            statusLabel.setText("🔄 Caricamento recensioni...");
            statusLabel.setTextFill(Color.YELLOW);
        }

        reviewsData.clear();

        // Verifica che l'utente sia autenticato come admin
        String adminEmail = authManager.getCurrentUser() != null ?
                authManager.getCurrentUser().getEmail() : null;

        if (adminEmail == null) {
            if (statusLabel != null) {
                statusLabel.setText("❌ Errore: utente non autenticato");
                statusLabel.setTextFill(Color.RED);
            }
            loadFallbackReviewsData();
            return;
        }

        adminService.getAllReviewsAsync(adminEmail)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response != null && response.isSuccess() && response.getRatings() != null) {
                            List<BookRating> ratings = response.getRatings();

                            reviewsData.clear();
                            reviewsData.addAll(ratings);

                            if (statusLabel != null) {
                                statusLabel.setText("✅ Caricate " + reviewsData.size() + " recensioni");
                                statusLabel.setTextFill(Color.LIGHTGREEN);
                            }

                            System.out.println("✅ Caricate " + reviewsData.size() + " recensioni");
                        } else {
                            String error = response != null ? response.getMessage() : "Risposta nulla dal server";

                            if (statusLabel != null) {
                                statusLabel.setText("❌ Errore: " + error);
                                statusLabel.setTextFill(Color.RED);
                            }
                            System.err.println("❌ Errore nel caricamento recensioni: " + error);
                            loadFallbackReviewsData();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText("❌ Errore di connessione");
                            statusLabel.setTextFill(Color.RED);
                        }
                        System.err.println("❌ Errore di connessione nel caricamento recensioni: " + throwable.getMessage());
                        throwable.printStackTrace();
                        loadFallbackReviewsData();
                    });
                    return null;
                });
    }

    /**
     * Carica delle recensioni di fallback in caso di server offline
     */
    private void loadFallbackReviewsData() {
        System.out.println("🔄 Caricamento dati di esempio...");

        BookRating rating1 = new BookRating();
        rating1.setUsername("mario.rossi");
        rating1.setIsbn("978-0123456789");
        rating1.setStyle(5);
        rating1.setContent(4);
        rating1.setPleasantness(5);
        rating1.setOriginality(4);
        rating1.setEdition(4);
        rating1.setReview("Un capolavoro assoluto della letteratura italiana.");
        rating1.setData("2025-08-15");

        BookRating rating2 = new BookRating();
        rating2.setUsername("anna.verdi");
        rating2.setIsbn("978-0987654321");
        rating2.setStyle(4);
        rating2.setContent(4);
        rating2.setPleasantness(3);
        rating2.setOriginality(5);
        rating2.setEdition(3);
        rating2.setReview("Inquietante e profetico, un libro che fa riflettere.");
        rating2.setData("2025-08-12");

        BookRating rating3 = new BookRating();
        rating3.setUsername("luca.bianchi");
        rating3.setIsbn("978-1234567890");
        rating3.setStyle(3);
        rating3.setContent(5);
        rating3.setPleasantness(4);
        rating3.setOriginality(3);
        rating3.setEdition(4);
        rating3.setReview("Interessante ma un po' lento all'inizio.");
        rating3.setData("2025-08-10");

        reviewsData.addAll(rating1, rating2, rating3);

        if (statusLabel != null) {
            statusLabel.setText("⚠️ Dati di esempio caricati (" + reviewsData.size() + " recensioni)");
            statusLabel.setTextFill(Color.ORANGE);
        }
    }

    /**
     * Elimina recensione selezionata
     */
    private void deleteSelectedReview() {
        ObservableList<BookRating> selectedRatings = reviewsTable.getSelectionModel().getSelectedItems();

        if (selectedRatings.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("⚠️ Seleziona almeno una recensione da eliminare");
                statusLabel.setTextFill(Color.ORANGE);
            }
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("🗑️ Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminare le recensioni selezionate?");
        confirmAlert.setContentText("Vuoi eliminare " + selectedRatings.size() + " recensione/i?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            List<BookRating> toDelete = List.copyOf(selectedRatings);

            if (statusLabel != null) {
                statusLabel.setText("🔄 Eliminazione in corso...");
                statusLabel.setTextFill(Color.YELLOW);
            }

            String adminEmail = authManager.getCurrentUser() != null ?
                    authManager.getCurrentUser().getEmail() : null;

            if (adminEmail == null) {
                if (statusLabel != null) {
                    statusLabel.setText("❌ Errore: utente non autenticato");
                    statusLabel.setTextFill(Color.RED);
                }
                return;
            }

            for (BookRating rating : toDelete) {
                adminService.deleteRatingAsync(adminEmail, rating.getUsername(), rating.getIsbn())
                        .thenAccept(deleteResponse -> {
                            Platform.runLater(() -> {
                                if (deleteResponse.isSuccess()) {
                                    reviewsData.remove(rating);
                                    System.out.println("✅ Eliminata recensione: " + rating.getUsername() + " - " + rating.getIsbn());
                                } else {
                                    String error = deleteResponse.getMessage();
                                    System.err.println("❌ Errore eliminazione: " + error);
                                }
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                System.err.println("❌ Errore eliminazione recensione: " + throwable.getMessage());
                                if (statusLabel != null) {
                                    statusLabel.setText("❌ Errore durante l'eliminazione");
                                    statusLabel.setTextFill(Color.RED);
                                }
                            });
                            return null;
                        });
            }

            if (statusLabel != null) {
                statusLabel.setText("🗑️ Eliminazione completata");
                statusLabel.setTextFill(Color.LIGHTGREEN);
            }
        }
    }

    /**
     * Torna al menu principale
     */
    private void backToMainMenu() {
        System.out.println("🔄 Tornando al menu principale...");

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            VBox header = createHeader();

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

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(color, "derive(" + color + ", 20%)"))
        );
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace("derive(" + color + ", 20%)", color))
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}