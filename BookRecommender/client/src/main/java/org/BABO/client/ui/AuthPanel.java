package org.BABO.client.ui;

import javafx.event.ActionEvent;
import org.BABO.client.service.AuthService;
import org.BABO.shared.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Pannello principale per l'autenticazione che include sia Login che Registrazione
 * Aggiornato per comunicare con il server tramite AuthService
 */
public class AuthPanel extends VBox {

    // Colori e stili principali
    private static final String BG_COLOR = "#1e1e1e";
    private static final String BG_CONTROL = "#2b2b2b";
    private static final String ACCENT_COLOR = "#4a86e8";
    private static final String TEXT_COLOR = "#ffffff";
    private static final String HINT_COLOR = "#9e9e9e";

    private AuthService authService;
    private Consumer<User> onSuccessfulAuth;
    private Runnable onClosePanel;

    public AuthPanel() {
        this.authService = new AuthService();
        setupPanel();
    }

    public void setOnSuccessfulAuth(Consumer<User> callback) {
        this.onSuccessfulAuth = callback;
    }

    public void setOnClosePanel(Runnable callback) {
        this.onClosePanel = callback;
    }

    private void setupPanel() {
        // Configurazione del pannello principale
        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);
        setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 4);" +
                        "-fx-background-radius: 12px;"
        );

        // Impostazione dimensioni
        setMinWidth(320);
        setMinHeight(460);
        setMaxWidth(420);
        setMaxHeight(730);

        // Logo o titolo dell'applicazione
        Label appTitle = new Label("📚 Apple Books");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        appTitle.setTextFill(Color.web(TEXT_COLOR));
        appTitle.setPadding(new Insets(0, 0, 15, 0));

        // Sistema a tab per navigare tra login e registrazione
        TabPane authTabs = new TabPane();
        authTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        authTabs.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-tab-min-width: 120px;"
        );

        // Tab per il login
        Tab loginTab = new Tab("Accedi");
        loginTab.setContent(createLoginPanel());

        // Tab per la registrazione
        Tab signupTab = new Tab("Registrati");
        signupTab.setContent(createSignupPanel());

        // Aggiunta dei tab al pannello
        authTabs.getTabs().addAll(loginTab, signupTab);

        // Styling dei tabs
        try {
            authTabs.getStylesheets().add(getClass().getResource("/css/auth-tabs.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("⚠️ CSS auth-tabs non disponibile, uso stili inline");
            authTabs.setStyle(
                    "-fx-tab-min-width: 120px; " +
                            "-fx-tab-max-width: 120px; " +
                            "-fx-tab-min-height: 40px;"
            );
        }

        // Aggiunta di tutti gli elementi al pannello principale
        getChildren().addAll(appTitle, authTabs);
    }

    /**
     * Crea il pannello per il login
     */
    private VBox createLoginPanel() {
        VBox loginPanel = new VBox(15);
        loginPanel.setPadding(new Insets(25, 15, 15, 15));
        loginPanel.setAlignment(Pos.TOP_CENTER);

        Label loginTitle = new Label("🔐 Accedi al tuo account");
        loginTitle.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loginTitle.setTextFill(Color.web(TEXT_COLOR));

        TextField emailField = new TextField();
        emailField.setPromptText("📧 Email");
        styleInput(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Password");
        styleInput(passwordField);

        // Link per password dimenticata
        Hyperlink forgotPassword = new Hyperlink("❓ Password dimenticata?");
        forgotPassword.setTextFill(Color.web(ACCENT_COLOR));
        forgotPassword.setOnAction(e -> showPasswordResetDialog());

        HBox forgotBox = new HBox(forgotPassword);
        forgotBox.setAlignment(Pos.CENTER_RIGHT);

        // Box per checkbox e link
        HBox optionsBox = new HBox();
        optionsBox.getChildren().addAll(forgotBox);
        HBox.setHgrow(forgotBox, Priority.ALWAYS);

        // Pulsante di login
        Button loginBtn = new Button("🚀 ACCEDI");
        styleActionButton(loginBtn);

        // Progress indicator per il caricamento
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(30, 30);

        // Aggiungi azione al pulsante
        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert("⚠️ Errore", "Inserisci email e password");
                return;
            }

            // Mostra indicatore di caricamento
            loginBtn.setDisable(true);
            progressIndicator.setVisible(true);

            // Esegui login in background
            authService.loginAsync(email, password)
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            loginBtn.setDisable(false);
                            progressIndicator.setVisible(false);

                            if (response.isSuccess()) {
                                System.out.println("✅ Login riuscito per: " + response.getUser().getDisplayName());
                                showAlert("✅ Successo", "Login effettuato con successo!\nBenvenuto " + response.getUser().getDisplayName());

                                // Notifica il successo dell'autenticazione
                                if (onSuccessfulAuth != null) {
                                    onSuccessfulAuth.accept(response.getUser());
                                }

                                // Chiudi il pannello
                                if (onClosePanel != null) {
                                    onClosePanel.run();
                                }
                            } else {
                                showAlert("❌ Errore", response.getMessage());
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            loginBtn.setDisable(false);
                            progressIndicator.setVisible(false);
                            showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                        });
                        return null;
                    });
        });

        loginPanel.getChildren().addAll(
                loginTitle,
                emailField,
                passwordField,
                optionsBox,
                loginBtn,
                progressIndicator
        );

        return loginPanel;
    }

    /**
     * Crea il pannello per la registrazione
     */
    private ScrollPane createSignupPanel() {
        // Crea il pannello di contenuto originale
        VBox signupPanel = new VBox(15);
        signupPanel.setPadding(new Insets(25, 15, 15, 15));
        signupPanel.setAlignment(Pos.TOP_CENTER);

        Label signupTitle = new Label("✨ Crea un nuovo account");
        signupTitle.setFont(Font.font("System", FontWeight.NORMAL, 16));
        signupTitle.setTextFill(Color.web(TEXT_COLOR));

        TextField nameField = new TextField();
        nameField.setPromptText("👤 Nome");
        styleInput(nameField);

        TextField surnameField = new TextField();
        surnameField.setPromptText("👤 Cognome");
        styleInput(surnameField);

        TextField cfField = new TextField();
        cfField.setPromptText("🆔 Codice Fiscale (opzionale)");
        styleInput(cfField);

        TextField emailField = new TextField();
        emailField.setPromptText("📧 Email");
        styleInput(emailField);

        TextField usernameField = new TextField();
        usernameField.setPromptText("🏷️ Username");
        styleInput(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Password (min 6 caratteri)");
        styleInput(passwordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("🔒 Conferma password");
        styleInput(confirmPasswordField);

        // Accettazione termini
        HBox termsBox = new HBox(10);
        termsBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox termsCheck = new CheckBox("📋 Accetto i termini e le condizioni");
        termsCheck.setTextFill(Color.web(HINT_COLOR));
        termsBox.getChildren().add(termsCheck);

        // Pulsante registrazione
        Button signupBtn = new Button("✨ Registrati");
        styleActionButton(signupBtn);

        // Progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(30, 30);

        // Aggiungi azione al pulsante
        signupBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String cf = cfField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Validazione
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showAlert("⚠️ Errore", "Compila tutti i campi obbligatori");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert("⚠️ Errore", "Le password non corrispondono");
                return;
            }

            if (password.length() < 8) {
                showAlert("⚠️ Errore", "La password deve essere almeno 8 caratteri");
                return;
            }

            if (!termsCheck.isSelected()) {
                showAlert("⚠️ Errore", "Accetta i termini e le condizioni");
                return;
            }

            // Mostra indicatore di caricamento
            signupBtn.setDisable(true);
            progressIndicator.setVisible(true);

            // Esegui registrazione in background
            authService.registerAsync(name, surname, cf.isEmpty() ? null : cf, email, username, password)
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            signupBtn.setDisable(false);
                            progressIndicator.setVisible(false);

                            if (response.isSuccess()) {
                                System.out.println("✅ Registrazione completata per: " + response.getUser().getDisplayName());
                                showAlert("✅ Successo", "Registrazione completata con successo!\nBenvenuto " + response.getUser().getDisplayName());

                                // Notifica il successo dell'autenticazione
                                if (onSuccessfulAuth != null) {
                                    onSuccessfulAuth.accept(response.getUser());
                                }

                                // Chiudi il pannello
                                if (onClosePanel != null) {
                                    onClosePanel.run();
                                }
                            } else {
                                showAlert("❌ Errore", response.getMessage());
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            signupBtn.setDisable(false);
                            progressIndicator.setVisible(false);
                            showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                        });
                        return null;
                    });
        });

        // Aggiungi tutti gli elementi al pannello
        signupPanel.getChildren().addAll(
                signupTitle,
                nameField,
                surnameField,
                cfField,
                emailField,
                usernameField,
                passwordField,
                confirmPasswordField,
                termsBox,
                signupBtn,
                progressIndicator
        );

        ScrollPane scrollPane = new ScrollPane(signupPanel);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    /**
     * Mostra dialog per reset password
     */
    private void showPasswordResetDialog() {
        Alert resetDialog = new Alert(Alert.AlertType.NONE);
        resetDialog.setTitle("🔄 Reimposta Password");
        resetDialog.setHeaderText(null);

        // Crea contenuto personalizzato
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Header personalizzato
        Label titleLabel = new Label("🔄 Reimposta Password");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));

        Label subtitleLabel = new Label("Inserisci email e nuova password");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web(HINT_COLOR));
        subtitleLabel.setPadding(new Insets(0, 0, 10, 0));

        TextField emailField = new TextField();
        emailField.setPromptText("📧 La tua email");
        styleInput(emailField);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("🔒 Nuova password (min 8 caratteri)");
        styleInput(newPasswordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("🔒 Conferma nuova password");
        styleInput(confirmPasswordField);

        content.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                emailField,
                newPasswordField,
                confirmPasswordField
        );

        resetDialog.getDialogPane().setContent(content);

        // Pulsanti
        ButtonType resetButtonType = new ButtonType("🔄 Reimposta", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        resetDialog.getButtonTypes().addAll(resetButtonType, cancelButtonType);

        // Styling
        DialogPane dialogPane = resetDialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #404040;" +
                        "-fx-text-fill: #FFFFFF;"
        );

        // Gestione click pulsante reset
        Button resetButton = (Button) resetDialog.getDialogPane().lookupButton(resetButtonType);
        resetButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume(); // Previeni chiusura automatica

            String email = emailField.getText().trim();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Validazioni
            if (email.isEmpty()) {
                showAlert("⚠️ Errore", "Inserisci la tua email");
                return;
            }

            if (newPassword.length() < 6) {
                showAlert("⚠️ Errore", "La password deve essere almeno 6 caratteri");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("⚠️ Errore", "Le password non coincidono");
                return;
            }

            // Disabilita pulsante durante operazione
            resetButton.setDisable(true);
            resetButton.setText("⏳ Reimpostazione...");

            // Esegui reset password
            authService.resetPasswordAsync(email, newPassword)
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            resetButton.setDisable(false);
                            resetButton.setText("🔄 Reimposta");

                            if (response.isSuccess()) {
                                resetDialog.close();

                                // Crea popup di successo personalizzato
                                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                successAlert.setTitle("✅ Successo");
                                successAlert.setHeaderText(null);
                                successAlert.setContentText("Reset Password effettuato");

                                DialogPane successPane = successAlert.getDialogPane();
                                successPane.setStyle(
                                        "-fx-background-color: #404040;" +
                                                "-fx-text-fill: #ffffff;"
                                );

                                // Forza sfondo anche sul contenuto
                                successPane.lookup(".content").setStyle("-fx-background-color: #404040;");

                                successAlert.showAndWait();
                            } else {
                                showAlert("❌ Errore", response.getMessage());
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            resetButton.setDisable(false);
                            resetButton.setText("🔄 Reimposta");
                            showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                        });
                        return null;
                    });
        });

        resetDialog.showAndWait();
    }

    /**
     * Stile per i campi di input
     */
    private void styleInput(TextField field) {
        field.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-prompt-text-fill: " + HINT_COLOR + ";" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;"
        );
        field.setMaxWidth(Double.MAX_VALUE);

        // Effetto focus
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: " + BG_CONTROL + ";" +
                                "-fx-text-fill: " + TEXT_COLOR + ";" +
                                "-fx-prompt-text-fill: " + HINT_COLOR + ";" +
                                "-fx-background-radius: 8px;" +
                                "-fx-border-color: " + ACCENT_COLOR + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-border-width: 2px;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: " + BG_CONTROL + ";" +
                                "-fx-text-fill: " + TEXT_COLOR + ";" +
                                "-fx-prompt-text-fill: " + HINT_COLOR + ";" +
                                "-fx-background-radius: 8px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;"
                );
            }
        });
    }

    /**
     * Stile per i pulsanti principali
     */
    private void styleActionButton(Button button) {
        button.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );
        button.setMaxWidth(Double.MAX_VALUE);

        // Effetti hover e press
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: derive(" + ACCENT_COLOR + ", 20%);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;"
                )
        );
        button.setOnMousePressed(e ->
                button.setStyle(
                        "-fx-background-color: derive(" + ACCENT_COLOR + ", -20%);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;"
                )
        );
        button.setOnMouseReleased(e ->
                button.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;"
                )
        );
    }

    /**
     * Crea pulsanti per login/signup tramite social
     */
    private Button createSocialButton(String provider) {
        Button button = new Button(provider);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-border-color: " + HINT_COLOR + ";" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8px 12px;"
        );

        // Aggiungi azione placeholder
        button.setOnAction(e -> {
            showAlert("🚧 Info", "Login con " + provider.substring(2) + " sarà implementato in futuro!");
        });

        // Effetti hover e press
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.1);" +
                                "-fx-text-fill: " + TEXT_COLOR + ";" +
                                "-fx-border-color: " + HINT_COLOR + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8px 12px;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: " + TEXT_COLOR + ";" +
                                "-fx-border-color: " + HINT_COLOR + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8px 12px;"
                )
        );

        return button;
    }

    /**
     * Mostra un alert informativo
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styling dell'alert per mantenere coerenza con il tema
        /*DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";"
        );*/

        alert.showAndWait();
    }
}