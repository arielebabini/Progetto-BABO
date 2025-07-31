package org.BABO.client.ui;

import org.BABO.shared.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Popup per mostrare e gestire il profilo dell'utente loggato
 */
public class UserProfilePopup {

    private static final String BG_COLOR = "#1e1e1e";
    private static final String BG_CONTROL = "#2b2b2b";
    private static final String ACCENT_COLOR = "#4a86e8";
    private static final String TEXT_COLOR = "#ffffff";
    private static final String HINT_COLOR = "#9e9e9e";

    private final AuthenticationManager authManager;
    private final Runnable onLogoutCallback;
    private StackPane root;

    public UserProfilePopup(AuthenticationManager authManager, Runnable onLogoutCallback) {
        this.authManager = authManager;
        this.onLogoutCallback = onLogoutCallback;
    }

    public void show(StackPane mainRoot) {
        root = mainRoot;

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("⚠️ Errore", "Nessun utente loggato");
            return;
        }

        // Crea overlay semi-trasparente
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // Crea contenuto popup
        VBox popupContent = createPopupContent(currentUser);

        // Centra il popup
        overlay.getChildren().add(popupContent);
        StackPane.setAlignment(popupContent, Pos.CENTER);

        // Chiudi popup cliccando sullo sfondo
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                closePopup();
            }
        });

        // Previeni chiusura cliccando sul contenuto
        popupContent.setOnMouseClicked(e -> e.consume());

        // Aggiungi al root principale
        mainRoot.getChildren().add(overlay);

        System.out.println("👤 Popup profilo utente aperto per: " + currentUser.getDisplayName());
    }

    private VBox createPopupContent(User user) {
        VBox popup = new VBox(20);
        popup.setMaxWidth(450);
        popup.setMaxHeight(600);
        popup.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 8);" +
                        "-fx-padding: 25px;"
        );

        // Header con chiudi
        HBox header = createHeader();

        // Sezione avatar e info principali
        VBox profileSection = createProfileSection(user);

        // Sezione informazioni dettagliate
        VBox detailsSection = createDetailsSection(user);

        // Sezione statistiche (placeholder)
        VBox statsSection = createStatsSection();

        // Sezione pulsanti azione
        VBox actionsSection = createActionsSection();

        popup.getChildren().addAll(header, profileSection, detailsSection, statsSection, actionsSection);

        return popup;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);

        Label title = new Label("👤 Il Mio Profilo");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(TEXT_COLOR));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #999999;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5px 10px;"
        );
        closeButton.setOnAction(e -> closePopup());

        header.getChildren().addAll(title, spacer, closeButton);
        return header;
    }

    private VBox createProfileSection(User user) {
        VBox profileSection = new VBox(15);
        profileSection.setAlignment(Pos.CENTER);

        // Avatar grande
        StackPane avatar = createLargeAvatar(user);

        // Nome completo
        Label fullName = new Label(user.getDisplayName());
        fullName.setFont(Font.font("System", FontWeight.BOLD, 24));
        fullName.setTextFill(Color.web(TEXT_COLOR));

        // Email
        Label email = new Label(user.getEmail());
        email.setFont(Font.font("System", 16));
        email.setTextFill(Color.web(HINT_COLOR));

        // Username
        Label username = new Label("@" + user.getUsername());
        username.setFont(Font.font("System", 14));
        username.setTextFill(Color.web(ACCENT_COLOR));

        profileSection.getChildren().addAll(avatar, fullName, email, username);
        return profileSection;
    }

    private StackPane createLargeAvatar(User user) {
        StackPane avatar = new StackPane();
        avatar.setMaxSize(80, 80);
        avatar.setMinSize(80, 80);

        // Cerchio di sfondo
        Circle circle = new Circle(40);
        circle.setFill(Color.web(ACCENT_COLOR));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(3);

        // Iniziali
        String initials = getInitials(user);
        Label initialsLabel = new Label(initials);
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 28));

        avatar.getChildren().addAll(circle, initialsLabel);
        return avatar;
    }

    private String getInitials(User user) {
        String name = user.getName();
        String surname = user.getSurname();

        StringBuilder initials = new StringBuilder();

        if (name != null && !name.trim().isEmpty()) {
            initials.append(name.trim().charAt(0));
        }

        if (surname != null && !surname.trim().isEmpty()) {
            initials.append(surname.trim().charAt(0));
        }

        return initials.length() > 0 ? initials.toString().toUpperCase() : "?";
    }

    private VBox createDetailsSection(User user) {
        VBox detailsSection = new VBox(12);

        Label sectionTitle = new Label("📋 Informazioni Account");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_COLOR));

        // Griglia informazioni
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        // Nome
        addInfoRow(grid, 0, "Nome:", user.getName());
        addInfoRow(grid, 1, "Cognome:", user.getSurname());

        // Codice fiscale (se presente)
        if (user.getCf() != null && !user.getCf().trim().isEmpty()) {
            addInfoRow(grid, 2, "Codice Fiscale:", user.getCf());
        }

        addInfoRow(grid, 3, "Email:", user.getEmail());
        addInfoRow(grid, 4, "Username:", user.getUsername());

        detailsSection.getChildren().addAll(sectionTitle, grid);
        return detailsSection;
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 13));
        labelNode.setTextFill(Color.web(HINT_COLOR));

        Label valueNode = new Label(value != null ? value : "Non specificato");
        valueNode.setFont(Font.font("System", 13));
        valueNode.setTextFill(Color.web(TEXT_COLOR));
        valueNode.setWrapText(true);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private VBox createStatsSection() {
        VBox statsSection = new VBox(12);

        Label sectionTitle = new Label("📊 Statistiche Lettura");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_COLOR));

        // Placeholder statistiche
        HBox statsGrid = new HBox(20);
        statsGrid.setAlignment(Pos.CENTER);

        VBox stat1 = createStatCard("📚", "0", "Libri Letti");
        VBox stat2 = createStatCard("⏱️", "0h", "Tempo Lettura");
        VBox stat3 = createStatCard("⭐", "0", "Recensioni");

        statsGrid.getChildren().addAll(stat1, stat2, stat3);

        statsSection.getChildren().addAll(sectionTitle, statsGrid);
        return statsSection;
    }

    private VBox createStatCard(String icon, String value, String label) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 15px 10px;"
        );
        card.setMinWidth(100);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 20));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        valueLabel.setTextFill(Color.web(TEXT_COLOR));

        Label nameLabel = new Label(label);
        nameLabel.setFont(Font.font("System", 10));
        nameLabel.setTextFill(Color.web(HINT_COLOR));
        nameLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }

    private VBox createActionsSection() {
        VBox actionsSection = new VBox(12);

        Label sectionTitle = new Label("⚙️ Azioni Account");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_COLOR));

        // Pulsanti azione
        HBox buttonsRow1 = new HBox(10);
        buttonsRow1.setAlignment(Pos.CENTER);

        Button editButton = createActionButton("✏️ Modifica Profilo", BG_CONTROL);
        editButton.setOnAction(e -> {
            showAlert("🚧 Info", "Funzionalità di modifica profilo sarà implementata presto!");
        });

        Button changePasswordButton = createActionButton("🔐 Cambia Password", BG_CONTROL);
        changePasswordButton.setOnAction(e -> {
            showAlert("🚧 Info", "Funzionalità di cambio password sarà implementata presto!");
        });

        buttonsRow1.getChildren().addAll(editButton, changePasswordButton);

        HBox buttonsRow2 = new HBox(10);
        buttonsRow2.setAlignment(Pos.CENTER);

        Button preferencesButton = createActionButton("📱 Preferenze", BG_CONTROL);
        preferencesButton.setOnAction(e -> {
            showAlert("🚧 Info", "Pannello preferenze sarà implementato presto!");
        });

        Button logoutButton = createActionButton("🚪 Logout", "#dc3545");
        logoutButton.setOnAction(e -> performLogout());

        buttonsRow2.getChildren().addAll(preferencesButton, logoutButton);

        actionsSection.getChildren().addAll(sectionTitle, buttonsRow1, buttonsRow2);
        return actionsSection;
    }

    private Button createActionButton(String text, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10px 15px;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefWidth(150);

        // Hover effect
        String hoverColor = backgroundColor.equals("#dc3545") ? "#c82333" : "derive(" + backgroundColor + ", 20%)";
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle().replace(backgroundColor, hoverColor)));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace(hoverColor, backgroundColor)));

        return button;
    }

    private void performLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("🚪 Conferma Logout");
        confirmAlert.setHeaderText("Sei sicuro di voler uscire?");
        confirmAlert.setContentText("Dovrai effettuare nuovamente il login per accedere al tuo account.");

        // Styling dell'alert
        DialogPane dialogPane = confirmAlert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";"
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("🚪 Logout confermato dall'utente");

                // Esegui logout tramite AuthenticationManager
                authManager.logout();

                // Chiudi popup
                closePopup();

                // Notifica callback per aggiornare sidebar
                if (onLogoutCallback != null) {
                    onLogoutCallback.run();
                }

                // Mostra messaggio di conferma
                Platform.runLater(() -> {
                    showAlert("👋 Arrivederci", "Logout effettuato con successo!");
                });
            }
        });
    }

    private void closePopup() {
        if (root != null && root.getChildren().size() > 1) {
            root.getChildren().remove(root.getChildren().size() - 1);
        }
        System.out.println("🔒 Popup profilo utente chiuso");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styling dell'alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";"
        );

        alert.showAndWait();
    }
}