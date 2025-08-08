package org.BABO.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

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

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UserProfilePopup(AuthenticationManager authManager, Runnable onLogoutCallback) {
        this.authManager = authManager;
        this.onLogoutCallback = onLogoutCallback;

        // AGGIUNGERE QUESTE RIGHE (stesso pattern di AuthService e ClientRatingService):
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
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
        VBox popup = new VBox();
        popup.setMaxWidth(420);
        popup.setMinWidth(350);
        popup.setPrefWidth(400);
        popup.setMaxHeight(600);
        popup.setMinHeight(500);
        popup.setPrefHeight(550);

        popup.setStyle(
                "-fx-background-color: transparent;" +  // lo sfondo lo mettiamo dentro scrollContent
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 8);"
        );

        // Contenuto scrollabile
        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(25));
        scrollContent.setFillWidth(true);

        // 🎨 Ripristina il colore scuro del popup
        scrollContent.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-background-radius: 15px;" +
                        "-fx-padding: 25px;"
        );

        // Sezioni
        HBox header = createHeader();
        VBox profileSection = createProfileSection(user);
        VBox detailsSection = createDetailsSection(user);
        VBox statsSection = createStatsSection();
        VBox actionsSection = createActionsSection();

        scrollContent.getChildren().addAll(header, profileSection, detailsSection, statsSection, actionsSection);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        popup.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

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

        // Container per le statistiche che verranno caricate
        HBox statsGrid = new HBox(20);
        statsGrid.setAlignment(Pos.CENTER);

        // Crea le card con valori iniziali
        VBox stat1 = createStatCard("📚", "...", "Libri Salvati");
        VBox stat2 = createStatCard("💡", "...", "Libri Consigliati");
        VBox stat3 = createStatCard("⭐", "...", "Recensioni");

        statsGrid.getChildren().addAll(stat1, stat2, stat3);
        statsSection.getChildren().addAll(sectionTitle, statsGrid);

        // Carica le statistiche reali in background
        loadUserStatistics(stat1, stat2, stat3);

        return statsSection;
    }

    /**
     * Carica le statistiche reali dell'utente
     */
    private void loadUserStatistics(VBox booksCard, VBox recommendationsCard, VBox reviewsCard) {
        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) return;

        String username = currentUser.getUsername();

        // 1. Carica numero libri (stesso pattern di AuthService)
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/library/stats/" + username))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractNumberFromMessage(response.body(), "Libri totali: ");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento statistiche libri: " + e.getMessage());
            }
            return 0;
        }).thenAccept(count -> {
            Platform.runLater(() -> updateStatCard(booksCard, String.valueOf(count)));
        });

        // 2. Carica numero raccomandazioni
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/recommendations/stats/" + username))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractNumberFromMessage(response.body(), "Raccomandazioni totali: ");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento statistiche raccomandazioni: " + e.getMessage());
            }
            return 0;
        }).thenAccept(count -> {
            Platform.runLater(() -> updateStatCard(recommendationsCard, String.valueOf(count)));
        });

        // 3. Carica numero recensioni
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/ratings/stats/" + username))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractNumberFromMessage(response.body(), "Recensioni totali: ");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento statistiche recensioni: " + e.getMessage());
            }
            return 0;
        }).thenAccept(count -> {
            Platform.runLater(() -> updateStatCard(reviewsCard, String.valueOf(count)));
        });
    }

    /**
     * Estrae un numero da un messaggio JSON
     * Cerca il pattern nel messaggio e estrae il numero
     */
    private int extractNumberFromMessage(String jsonResponse, String pattern) {
        try {
            // Parse del JSON per ottenere il messaggio
            if (jsonResponse.contains("\"message\"")) {
                int messageStart = jsonResponse.indexOf("\"message\":\"") + 11;
                int messageEnd = jsonResponse.indexOf("\"", messageStart);
                String message = jsonResponse.substring(messageStart, messageEnd);

                // Cerca il pattern nel messaggio
                if (message.contains(pattern)) {
                    String numberStr = message.substring(message.indexOf(pattern) + pattern.length());
                    return Integer.parseInt(numberStr.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore parsing numero da messaggio: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Aggiorna il valore in una stat card
     */
    private void updateStatCard(VBox card, String newValue) {
        // Il secondo figlio è il Label con il valore
        if (card.getChildren().size() >= 2 && card.getChildren().get(1) instanceof Label) {
            Label valueLabel = (Label) card.getChildren().get(1);
            valueLabel.setText(newValue);
        }
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

        Button editButton = createActionButton("✏️ Modifica Profilo", "#4a86e8");
        editButton.setOnAction(e -> {
            showAlert("🚧 Info", "Funzionalità di modifica profilo sarà implementata presto!");
        });

        Button changePasswordButton = createActionButton("🔐 Cambia Password", "#4a86e8");
        changePasswordButton.setOnAction(e -> showChangePasswordDialog());

        buttonsRow1.getChildren().addAll(editButton, changePasswordButton);

        HBox buttonsRow2 = new HBox(10);
        buttonsRow2.setAlignment(Pos.CENTER);

        Button logoutButton = createActionButton("🚪 Logout", "#dc3545");
        logoutButton.setOnAction(e -> performLogout());

        buttonsRow2.getChildren().addAll(logoutButton);

        actionsSection.getChildren().addAll(sectionTitle, buttonsRow1, buttonsRow2);
        return actionsSection;
    }

    /**
     * Mostra il dialog per cambiare la password
     */
    private void showChangePasswordDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("🔐 Cambia Password");
        dialog.setResizable(false);
        dialog.getIcons().add(new Image("/icons/password.png")); // Opzionale

        // Container principale
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.CENTER);
        container.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;"
        );

        // Titolo
        Label titleLabel = new Label("🔐 Cambia Password");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setAlignment(Pos.CENTER);

        // Descrizione
        Label descLabel = new Label("Per sicurezza, inserisci la tua password attuale e quella nuova");
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web("#bdc3c7"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(350);

        // Container per i campi
        VBox fieldsContainer = new VBox(15);
        fieldsContainer.setAlignment(Pos.CENTER);

        // Campo password attuale
        Label currentLabel = new Label("Password Attuale:");
        currentLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        currentLabel.setTextFill(Color.WHITE);

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Inserisci la password attuale");
        currentPasswordField.setPrefWidth(300);
        currentPasswordField.setPrefHeight(40);
        currentPasswordField.setStyle(
                "-fx-background-color: #34495e;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #7f8c8d;" +
                        "-fx-border-color: #4a86e8;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 14px;"
        );

        // Campo nuova password
        Label newLabel = new Label("Nuova Password:");
        newLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        newLabel.setTextFill(Color.WHITE);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Inserisci la nuova password");
        newPasswordField.setPrefWidth(300);
        newPasswordField.setPrefHeight(40);
        newPasswordField.setStyle(currentPasswordField.getStyle());

        // Campo conferma nuova password
        Label confirmLabel = new Label("Conferma Nuova Password:");
        confirmLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        confirmLabel.setTextFill(Color.WHITE);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Ripeti la nuova password");
        confirmPasswordField.setPrefWidth(300);
        confirmPasswordField.setPrefHeight(40);
        confirmPasswordField.setStyle(currentPasswordField.getStyle());

        // Label per messaggi di errore/info
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(300);
        messageLabel.setVisible(false);

        // Aggiunta campi al container
        fieldsContainer.getChildren().addAll(
                currentLabel, currentPasswordField,
                newLabel, newPasswordField,
                confirmLabel, confirmPasswordField,
                messageLabel
        );

        // Container per i pulsanti
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        // Bottone Annulla
        Button cancelButton = new Button("❌ Annulla");
        cancelButton.setPrefWidth(120);
        cancelButton.setPrefHeight(40);
        cancelButton.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        cancelButton.setOnAction(event -> dialog.close());

        // Bottone Cambia
        Button changeButton = new Button("✅ Cambia Password");
        changeButton.setPrefWidth(150);
        changeButton.setPrefHeight(40);
        changeButton.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        // Azione del bottone Cambia
        changeButton.setOnAction(event -> {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Reset messaggio precedente
            messageLabel.setVisible(false);

            // Validazioni
            if (currentPassword.isEmpty()) {
                showMessage(messageLabel, "❌ Inserisci la password attuale", "#e74c3c");
                currentPasswordField.requestFocus();
                return;
            }

            if (newPassword.isEmpty()) {
                showMessage(messageLabel, "❌ Inserisci la nuova password", "#e74c3c");
                newPasswordField.requestFocus();
                return;
            }

            if (newPassword.length() < 6) {
                showMessage(messageLabel, "❌ La password deve essere di almeno 6 caratteri", "#e74c3c");
                newPasswordField.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showMessage(messageLabel, "❌ Le password non corrispondono", "#e74c3c");
                confirmPasswordField.requestFocus();
                return;
            }

            if (currentPassword.equals(newPassword)) {
                showMessage(messageLabel, "❌ La nuova password deve essere diversa da quella attuale", "#e74c3c");
                newPasswordField.requestFocus();
                return;
            }

            // Cambio password (qui chiamerai il tuo servizio)
            changePassword(currentPassword, newPassword, dialog, messageLabel);
        });

        buttonContainer.getChildren().addAll(cancelButton, changeButton);

        // Assemblaggio finale
        container.getChildren().addAll(
                titleLabel,
                descLabel,
                fieldsContainer,
                buttonContainer
        );

        // Effetti hover sui bottoni
        addHoverEffect(cancelButton, "#e74c3c", "#c0392b");
        addHoverEffect(changeButton, "#27ae60", "#229954");

        // Scene e show
        Scene scene = new Scene(container, 400, 500);
        dialog.setScene(scene);
        dialog.show();

        // Focus iniziale
        Platform.runLater(() -> currentPasswordField.requestFocus());
    }

    /**
     * Mostra un messaggio nel label
     */
    private void showMessage(Label messageLabel, String text, String color) {
        messageLabel.setText(text);
        messageLabel.setTextFill(Color.web(color));
        messageLabel.setVisible(true);
    }

    /**
     * Aggiunge effetto hover ai bottoni
     */
    private void addHoverEffect(Button button, String normalColor, String hoverColor) {
        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(normalColor, hoverColor))
        );
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace(hoverColor, normalColor))
        );
    }

    /**
     * Effettua il cambio password
     */
    private void changePassword(String currentPassword, String newPassword, Stage dialog, Label messageLabel) {
        User currentUser = authManager.getCurrentUser();
        System.out.println("🔐 Tentativo cambio password per utente: " + currentUser.getUsername());

        // Simula validazione password attuale
        // IMPORTANTE: Qui devi verificare la password attuale tramite il tuo servizio

        // Esempio di implementazione:
    /*
    CompletableFuture.supplyAsync(() -> {
        try {
            // Chiama il tuo servizio per cambiare la password
            return userService.changePassword(currentUser.getUsername(), currentPassword, newPassword);
        } catch (Exception e) {
            Platform.runLater(() ->
                showMessage(messageLabel, "❌ Errore: " + e.getMessage(), "#e74c3c")
            );
            return false;
        }
    }).thenAccept(success -> {
        Platform.runLater(() -> {
            if (success) {
                showAlert("✅ Successo", "Password cambiata con successo!");
                dialog.close();
            } else {
                showMessage(messageLabel, "❌ Password attuale non corretta", "#e74c3c");
            }
        });
    });
    */

        // TEMPORANEO - Simula successo dopo 1 secondo
        showMessage(messageLabel, "🔄 Cambio password in corso...", "#f39c12");

        Timeline timeline = new Timeline(new KeyFrame(Duration.Seconds(1), e -> {
            showAlert("✅ Successo", "Password cambiata con successo!");
            dialog.close();
        }));
        timeline.play();
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