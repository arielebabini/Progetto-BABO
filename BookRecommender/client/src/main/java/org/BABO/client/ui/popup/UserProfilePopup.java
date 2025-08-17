package org.BABO.client.ui.Popup;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.BABO.client.service.AuthService;
import org.BABO.client.ui.Authentication.AuthenticationManager;
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
    private boolean isEmailDialogOpen = false;

    public UserProfilePopup(AuthenticationManager authManager, Runnable onLogoutCallback) {
        this.authManager = authManager;
        this.onLogoutCallback = onLogoutCallback;

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

        Button editButton = createActionButton("✏️ Cambia Email", "#4a86e8");
        editButton.setOnAction(e -> showEditProfileDialog());

        Button changePasswordButton = createActionButton("🔐 Cambia Password", "#4a86e8");
        changePasswordButton.setOnAction(e -> {
            showChangePasswordDialog();
        });

        buttonsRow1.getChildren().addAll(editButton, changePasswordButton);

        HBox buttonsRow2 = new HBox(10);
        buttonsRow2.setAlignment(Pos.CENTER);

        Button logoutButton = createActionButton("🚪 Logout", "#dc3545");
        logoutButton.setOnAction(e -> performLogout());

        buttonsRow2.getChildren().addAll(logoutButton);

        actionsSection.getChildren().addAll(sectionTitle, buttonsRow1, buttonsRow2);
        return actionsSection;
    }

    private void showEditProfileDialog() {
        isEmailDialogOpen = true;
        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("⚠️ Errore", "Nessun utente loggato");
            isEmailDialogOpen = false; // ✅ RESET flag
            return;
        }

        Stage dialog = new Stage();
        dialog.setTitle("✏️ Modifica Profilo");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        dialog.setOnCloseRequest(e -> {
            System.out.println("🔒 Chiusura popup modifica email");
            isEmailDialogOpen = false;
            dialog.close();
        });

        // Container principale
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Titolo e descrizione
        Label titleLabel = new Label("✏️ Modifica Email");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));

        Label descLabel = new Label("Inserisci la nuova email di accesso");
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web(HINT_COLOR));
        descLabel.setPadding(new Insets(0, 0, 10, 0));

        // Container per i campi
        VBox fieldsContainer = new VBox(15);

        // Campo email attuale (solo lettura)
        Label currentEmailLabel = new Label("📧 Email attuale:");
        currentEmailLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        currentEmailLabel.setTextFill(Color.web(TEXT_COLOR));

        TextField currentEmailField = new TextField(currentUser.getEmail());
        currentEmailField.setEditable(false);
        currentEmailField.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + HINT_COLOR + ";" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-opacity: 0.7;"
        );

        // Campo nuova email
        Label newEmailLabel = new Label("📧 Nuova email:");
        newEmailLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        newEmailLabel.setTextFill(Color.web(TEXT_COLOR));

        TextField newEmailField = new TextField();
        newEmailField.setPromptText("Inserisci la nuova email");
        styleInput(newEmailField);

        // Campo conferma email
        Label confirmEmailLabel = new Label("📧 Conferma nuova email:");
        confirmEmailLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        confirmEmailLabel.setTextFill(Color.web(TEXT_COLOR));

        TextField confirmEmailField = new TextField();
        confirmEmailField.setPromptText("Conferma la nuova email");
        styleInput(confirmEmailField);

        // Label per messaggi
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(350);
        messageLabel.setVisible(false);

        fieldsContainer.getChildren().addAll(
                currentEmailLabel, currentEmailField,
                newEmailLabel, newEmailField,
                confirmEmailLabel, confirmEmailField,
                messageLabel
        );

        // Container per i pulsanti
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        // ✅ Bottone Annulla CORRETTO
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

        // ✅ GESTIONE CORRETTA del bottone annulla
        cancelButton.setOnAction(event -> {
            System.out.println("🚫 Annulla modifica email cliccato");
            isEmailDialogOpen = false;
            dialog.close();
        });

        // Bottone Salva
        Button saveButton = new Button("✅ Salva Email");
        saveButton.setPrefWidth(150);
        saveButton.setPrefHeight(40);
        saveButton.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        // ✅ Azione del bottone Salva CORRETTA
        saveButton.setOnAction(event -> {
            String newEmail = newEmailField.getText().trim();
            String confirmEmail = confirmEmailField.getText().trim();

            // Reset messaggio precedente
            messageLabel.setVisible(false);

            // Validazioni
            if (newEmail.isEmpty()) {
                showMessage(messageLabel, "❌ Inserisci la nuova email", "#e74c3c");
                newEmailField.requestFocus();
                return;
            }

            if (!isValidEmail(newEmail)) {
                showMessage(messageLabel, "❌ Formato email non valido", "#e74c3c");
                newEmailField.requestFocus();
                return;
            }

            if (!newEmail.equals(confirmEmail)) {
                showMessage(messageLabel, "❌ Le email non corrispondono", "#e74c3c");
                confirmEmailField.requestFocus();
                return;
            }

            if (newEmail.equals(currentUser.getEmail())) {
                showMessage(messageLabel, "❌ La nuova email deve essere diversa da quella attuale", "#e74c3c");
                newEmailField.requestFocus();
                return;
            }

            changeEmailWithDialog(newEmail, dialog, messageLabel, saveButton);
        });

        buttonContainer.getChildren().addAll(cancelButton, saveButton);

        // Assemblaggio finale
        container.getChildren().addAll(
                titleLabel,
                descLabel,
                fieldsContainer,
                buttonContainer
        );

        Scene scene = new Scene(container, 450, 500);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                System.out.println("⌨️ ESC premuto - chiusura popup");
                isEmailDialogOpen = false;
                dialog.close();
            } else if (e.getCode() == KeyCode.ENTER) {
                System.out.println("⌨️ ENTER premuto - salva email");
                saveButton.fire(); // Simula click su salva
            }
        });

        dialog.setScene(scene);

        // Effetti hover sui bottoni
        addHoverEffect(cancelButton, "#e74c3c", "#c0392b");
        addHoverEffect(saveButton, "#27ae60", "#219a52");

        Platform.runLater(() -> {
            newEmailField.requestFocus();
        });

        dialog.showAndWait();
    }

    private void changeEmailWithDialog(String newEmail, Stage dialog, Label messageLabel, Button saveButton) {
        // Disabilita il pulsante durante l'operazione
        saveButton.setDisable(true);
        saveButton.setText("🔄 Salvataggio...");

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            saveButton.setDisable(false);
            saveButton.setText("✅ Salva Email");
            showMessage(messageLabel, "❌ Errore: utente non trovato", "#e74c3c");
            return;
        }

        AuthService authService = new AuthService();

        authService.updateEmailAsync(String.valueOf(currentUser.getId()), newEmail)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        saveButton.setText("✅ Salva Email");

                        if (response.isSuccess()) {
                            User updatedUser = response.getUser();
                            if (updatedUser != null) {
                                authManager.updateCurrentUser(updatedUser);
                            }

                            updateProfileDisplay();

                            // Chiudi dialog
                            isEmailDialogOpen = false;
                            dialog.close();

                            // Mostra messaggio di successo
                            showAlert("✅ Successo", "Email aggiornata correttamente!");

                        } else {
                            String errorMsg = response.getMessage() != null ? response.getMessage() : "Errore durante l'aggiornamento";
                            showMessage(messageLabel, "❌ " + errorMsg, "#e74c3c");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        saveButton.setText("✅ Salva Email");
                        showMessage(messageLabel, "❌ Errore di connessione: " + throwable.getMessage(), "#e74c3c");
                    });
                    return null;
                });
    }

    /**
     * Ricarica il popup con i dati aggiornati
     */
    private void updateProfileDisplay() {
        if (root != null) {
            Platform.runLater(() -> {
                // Chiudi il popup corrente
                closePopup();

                // Riapri con i dati aggiornati dopo un breve delay
                Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), e -> {
                    show(root);
                }));
                timeline.play();
            });
        }
    }

    private void styleInput(TextField field) {
        field.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-prompt-text-fill: " + HINT_COLOR + ";"
        );
        field.setPrefHeight(45);
        field.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Mostra un messaggio nel label
     */
    private void showMessage(Label messageLabel, String message, String color) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.web(color));
        messageLabel.setVisible(true);
    }

    /**
     * Aggiunge effetto hover ai bottoni
     */
    private void addHoverEffect(Button button, String originalColor, String hoverColor) {
        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(originalColor, hoverColor))
        );
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace(hoverColor, originalColor))
        );
    }

    /**
     * Effettua il cambio password
     */
    private void changePassword(String currentPassword, String newPassword, Stage dialog, Label messageLabel) {
        User currentUser = authManager.getCurrentUser();
        System.out.println("🔐 Tentativo cambio password per utente: " + currentUser.getUsername());

        showMessage(messageLabel, "🔄 Cambio password in corso...", "#f39c12");

        Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(1000), e -> {
            showAlert("✅ Successo", "Password cambiata con successo!");
            dialog.close();
        }));
        timeline.play();
    }

    private void showChangePasswordDialog() {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("🔐 Cambia Password");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Password attuale");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nuova password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Conferma nuova password");

        Label messageLabel = new Label();

        content.getChildren().addAll(
                new Label("Password attuale:"), currentPasswordField,
                new Label("Nuova password:"), newPasswordField,
                new Label("Conferma password:"), confirmPasswordField,
                messageLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();

            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!newPassword.equals(confirmPassword)) {
                messageLabel.setText("❌ Le password non coincidono");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // Simula cambio password
            messageLabel.setText("🔄 Cambio password in corso...");
            messageLabel.setTextFill(Color.ORANGE);

            // Ottieni l'utente corrente per l'ID
            User currentUser = authManager.getCurrentUser();
            if (currentUser == null) {
                messageLabel.setText("❌ Errore: utente non trovato");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // Disabilita il pulsante durante la richiesta
            okButton.setDisable(true);
            messageLabel.setText("🔄 Cambio password in corso...");
            messageLabel.setTextFill(Color.ORANGE);

            // Crea un'istanza di AuthService
            AuthService authService = new AuthService();

            // Chiama il servizio per cambiare la password
            authService.changePasswordAsync(
                    String.valueOf(currentUser.getId()),
                    currentPassword,
                    newPassword
            ).thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        dialog.close();
                        showAlert("✅ Successo", "Password cambiata con successo!");
                    } else {
                        messageLabel.setText("❌ " + response.getMessage());
                        messageLabel.setTextFill(Color.RED);
                        okButton.setDisable(false);
                    }
                });
            }).exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    messageLabel.setText("❌ Errore di connessione");
                    messageLabel.setTextFill(Color.RED);
                    okButton.setDisable(false);
                });
                return null;
            });
        });

        dialog.showAndWait();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    private void changeEmail(String newEmail, Alert dialog, Label messageLabel, Button saveButton) {
        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            showMessage(messageLabel, "❌ Errore: utente non trovato", "#e74c3c");
            return;
        }

        saveButton.setDisable(true);
        showMessage(messageLabel, "🔄 Cambio email in corso...", "#f39c12");

        AuthService authService = new AuthService();

        authService.updateEmailAsync(String.valueOf(currentUser.getId()), newEmail)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            User updatedUser = response.getUser();
                            if (updatedUser != null) {
                                authManager.updateCurrentUser(updatedUser);
                            }

                            isEmailDialogOpen = false;
                            dialog.close();
                            showAlert("✅ Successo", "Email cambiata con successo!");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showMessage(messageLabel, "❌ Errore di connessione", "#e74c3c");
                        saveButton.setDisable(false);
                    });
                    return null;
                });
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

        alert.showAndWait();
    }
}