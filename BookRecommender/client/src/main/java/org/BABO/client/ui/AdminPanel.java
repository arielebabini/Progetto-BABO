package org.BABO.client.ui;

import org.BABO.client.service.AdminService;
import org.BABO.shared.model.Book;
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

    public AdminPanel(AuthenticationManager authManager) {
        this.authManager = authManager;
        this.adminService = new AdminService();
        this.usersData = FXCollections.observableArrayList();
        this.booksData = FXCollections.observableArrayList();
        this.currentContent = new VBox();

        initializeBooksCoversDirectory();
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

        // Contenitore per i pulsanti
        HBox buttonsContainer = new HBox(40);
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

        buttonsContainer.getChildren().addAll(usersCard, booksCard);

        // Info admin
        Label adminInfo = new Label("👑 Connesso come: " + authManager.getCurrentUser().getEmail());
        adminInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        adminInfo.setTextFill(Color.GOLD);
        adminInfo.setAlignment(Pos.CENTER);

        // ✅ RIMOSSO il pulsante di test

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

        // ✅ AGGIORNA L'ORDINE DELLE COLONNE per mettere la copertina per prima
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
                        booksData.clear();
                        if (response.getBooks() != null) {
                            booksData.addAll(response.getBooks());
                        }

                        statusLabel.setText("✅ " + booksData.size() + " libri caricati");
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
                        String targetFileName = saveCoverImage(coverHolder.selectedFile, isbnField.getText());
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
    private String saveCoverImage(File sourceFile, String isbn) throws IOException {
        // Validazione ISBN
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IOException("ISBN non può essere vuoto");
        }

        // Pulisci l'ISBN per il nome file
        String cleanIsbn = isbn.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (cleanIsbn.isEmpty()) {
            throw new IOException("ISBN non contiene caratteri validi");
        }

        String targetFileName = cleanIsbn + ".jpg";
        System.out.println("📸 Salvataggio copertina: " + sourceFile.getName() + " → " + targetFileName);

        // Percorso di destinazione
        String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
        Path targetDir = Paths.get(resourcesPath);

        // Crea la directory se non esiste
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            System.out.println("📁 Creata directory: " + targetDir);
        }

        Path targetPath = targetDir.resolve(targetFileName);

        try {
            // Verifica che il file sorgente esista
            if (!sourceFile.exists()) {
                throw new IOException("File sorgente non trovato: " + sourceFile.getAbsolutePath());
            }

            // Verifica dimensioni del file (max 5MB)
            long fileSize = Files.size(sourceFile.toPath());
            if (fileSize > 5 * 1024 * 1024) {
                throw new IOException("File troppo grande (max 5MB). Dimensione: " + (fileSize / 1024 / 1024) + "MB");
            }

            // Copia il file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Copertina salvata: " + targetPath);

            // Verifica che il file sia stato copiato correttamente
            if (!Files.exists(targetPath)) {
                throw new IOException("Errore durante la copia: file non creato");
            }

            return targetFileName;

        } catch (IOException e) {
            System.err.println("❌ Errore salvataggio copertina: " + e.getMessage());
            throw new IOException("Errore durante il salvataggio della copertina: " + e.getMessage());
        }
    }

    /**
     * Verifica l'integrità della directory books_covers
     */
    private void verifyBooksCoversDirectory() {
        try {
            String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
            Path targetDir = Paths.get(resourcesPath);

            if (!Files.exists(targetDir)) {
                System.out.println("❌ Directory books_covers non esiste");
                return;
            }

            if (!Files.isWritable(targetDir)) {
                System.out.println("❌ Directory books_covers non è scrivibile");
                return;
            }

            // Conta i file nella directory
            long fileCount = Files.list(targetDir)
                    .filter(Files::isRegularFile)
                    .count();

            System.out.println("📁 Directory books_covers: " + fileCount + " file presenti");

        } catch (IOException e) {
            System.err.println("❌ Errore verifica directory: " + e.getMessage());
        }
    }

    /**
     * Valida le proporzioni dell'immagine
     */
    private boolean validateImageProportions(Image image) {
        if (image == null || image.isError()) {
            return false;
        }

        double width = image.getWidth();
        double height = image.getHeight();
        double ratio = height / width;

        // Controllo: altezza deve essere tra 1x e 2x la larghezza (range 100x200)
        return ratio >= 1.0 && ratio <= 2.0;
    }

    /**
     * Crea un'anteprima ridimensionata dell'immagine
     */
    private Image createImagePreview(File imageFile, double maxWidth, double maxHeight) throws IOException {
        try (InputStream inputStream = new FileInputStream(imageFile)) {
            Image image = new Image(inputStream, maxWidth, maxHeight, true, true);

            if (image.isError()) {
                throw new IOException("Impossibile caricare l'immagine");
            }

            return image;
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
        System.out.println("🔄 Passaggio a gestione libri..."); // Debug

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

            // Tabella libri
            createBooksTable();

            container.getChildren().addAll(tableTitle, addBookButton, booksTable);
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
     * Crea un placeholder semplice quando non ci sono immagini
     */
    private void createSimplePlaceholder(ImageView imageView) {
        imageView.setImage(null);
    }

    /**
     * Verifica se esiste una copertina per il libro specificato
     */
    private boolean coverExistsForBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            return false;
        }

        try {
            String cleanIsbn = book.getIsbn().toUpperCase().replaceAll("[^A-Z0-9]", "");
            String coverFileName = cleanIsbn + ".jpg";
            String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
            Path coverPath = Paths.get(resourcesPath, coverFileName);

            return Files.exists(coverPath) && Files.isRegularFile(coverPath);

        } catch (Exception e) {
            return false;
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

        // ✅ DEBUG: Verifica dati utente selezionato
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
}
