package org.BABO.client.ui;

import org.BABO.client.service.AdminService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Pannello di gestione amministrativa degli utenti
 */
public class AdminPanel {

    private final AuthenticationManager authManager;
    private final AdminService adminService;
    private TableView<User> usersTable;
    private ObservableList<User> usersData;
    private Label statusLabel;

    public AdminPanel(AuthenticationManager authManager) {
        this.authManager = authManager;
        this.adminService = new AdminService();
        this.usersData = FXCollections.observableArrayList();
    }

    public VBox createAdminPanel() {
        VBox adminPanel = new VBox(20);
        adminPanel.setPadding(new Insets(30));
        adminPanel.setStyle("-fx-background-color: #1e1e1e;");

        // Header
        VBox header = createHeader();

        // Toolbar
        HBox toolbar = createToolbar();

        // Tabella utenti
        VBox tableContainer = createUsersTable();

        // Status bar
        HBox statusBar = createStatusBar();

        adminPanel.getChildren().addAll(header, toolbar, tableContainer, statusBar);

        // Carica dati iniziali
        loadUsers();

        return adminPanel;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⚙️ Gestione Utenti");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Pannello di amministrazione per la gestione degli utenti registrati");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.LIGHTGRAY);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button refreshButton = new Button("🔄 Aggiorna");
        styleButton(refreshButton, "#4a86e8");
        refreshButton.setOnAction(e -> loadUsers());

        Button deleteButton = new Button("🗑️ Elimina Selezionato");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> deleteSelectedUser());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminLabel = new Label("👑 Admin: " + authManager.getCurrentUser().getEmail());
        adminLabel.setTextFill(Color.GOLD);
        adminLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        toolbar.getChildren().addAll(refreshButton, deleteButton, spacer, adminLabel);
        return toolbar;
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

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("Pronto");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("System", 12));

        statusBar.getChildren().add(statusLabel);
        return statusBar;
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
