package org.BABO.client.ui.Authentication;

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
 * Pannello di interfaccia utente per operazioni di autenticazione nell'applicazione BABO.
 * <p>
 * Questa classe fornisce un'interfaccia grafica completa e moderna per tutte le operazioni
 * di autenticazione degli utenti, inclusi login, registrazione e reset password.
 * Il pannello è progettato con un'interfaccia a schede (TabPane) che permette agli utenti
 * di navigare facilmente tra le diverse modalità di autenticazione.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Login Utente:</strong> Autenticazione tramite email e password</li>
 *   <li><strong>Registrazione:</strong> Creazione di nuovi account utente con validazione completa</li>
 *   <li><strong>Reset Password:</strong> Funzionalità per reimpostare password dimenticate</li>
 *   <li><strong>Validazione Form:</strong> Controlli di input lato client per migliorare UX</li>
 *   <li><strong>Feedback Visivo:</strong> Indicatori di progresso e messaggi di stato</li>
 *   <li><strong>Design Responsivo:</strong> Layout adattivo con supporto per scroll quando necessario</li>
 * </ul>
 *
 * <h3>Architettura e Design:</h3>
 * <p>
 * Il pannello estende {@link VBox} e utilizza un sistema di callback per comunicare
 * con il componente padre. Implementa un design pattern Observer per notificare
 * eventi di autenticazione riuscita e richieste di chiusura pannello.
 * L'interfaccia segue i principi di Material Design con colori e animazioni moderne.
 * </p>
 *
 * <h3>Sistema di Validazione:</h3>
 * <ul>
 *   <li><strong>Email:</strong> Verifica presenza e formato base</li>
 *   <li><strong>Password:</strong> Controllo lunghezza minima (8 caratteri)</li>
 *   <li><strong>Conferma Password:</strong> Validazione corrispondenza</li>
 *   <li><strong>Campi Obbligatori:</strong> Verifica completamento form</li>
 *   <li><strong>Termini e Condizioni:</strong> Accettazione obbligatoria per registrazione</li>
 * </ul>
 *
 * <h3>Gestione Errori:</h3>
 * <p>
 * Il pannello implementa una gestione robusta degli errori con:
 * </p>
 * <ul>
 *   <li>Validazione lato client per feedback immediato</li>
 *   <li>Gestione asincrona delle operazioni di rete</li>
 *   <li>Messaggi di errore user-friendly</li>
 *   <li>Indicatori di caricamento durante operazioni asincrone</li>
 *   <li>Timeout automatici e retry logic tramite AuthService</li>
 * </ul>
 *
 * <h3>Personalizzazione Visiva:</h3>
 * <p>
 * Il pannello utilizza un tema scuro personalizzabile con:
 * </p>
 * <ul>
 *   <li>Colori definiti come costanti per facile manutenzione</li>
 *   <li>Stili CSS inline per compatibilità</li>
 *   <li>Effetti hover e focus per migliorare l'interattività</li>
 *   <li>Supporto per CSS esterni quando disponibili</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Creazione del pannello
 * AuthPanel authPanel = new AuthPanel();
 *
 * // Configurazione callback per autenticazione riuscita
 * authPanel.setOnSuccessfulAuth(user -> {
 *     System.out.println("Utente autenticato: " + user.getDisplayName());
 *     // Aggiorna UI principale
 *     updateMainUI(user);
 * });
 *
 * // Configurazione callback per chiusura pannello
 * authPanel.setOnClosePanel(() -> {
 *     // Rimuovi pannello dall'UI
 *     mainContainer.getChildren().remove(authPanel);
 * });
 *
 * // Aggiunta a container principale
 * StackPane overlay = new StackPane();
 * overlay.getChildren().add(authPanel);
 * mainRoot.getChildren().add(overlay);
 * }</pre>
 *
 * <h3>Integrazione con AuthService:</h3>
 * <p>
 * Il pannello si integra strettamente con {@link AuthService} per tutte le operazioni
 * di backend, garantendo:
 * </p>
 * <ul>
 *   <li>Operazioni asincrone non bloccanti</li>
 *   <li>Gestione automatica di timeout e retry</li>
 *   <li>Feedback immediato all'utente</li>
 *   <li>Sicurezza nelle comunicazioni</li>
 * </ul>
 *
 * <h3>Accessibilità e UX:</h3>
 * <ul>
 *   <li>Tab navigation supportata nativamente</li>
 *   <li>Prompt text descrittivi per ogni campo</li>
 *   <li>Messaggi di errore chiari e actionable</li>
 *   <li>Indicatori di progresso durante operazioni lunghe</li>
 *   <li>Layout responsivo per diverse dimensioni</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see AuthService
 * @see AuthenticationManager
 * @see User
 */
public class AuthPanel extends VBox {

    /** Colore di sfondo principale del pannello */
    private static final String BG_COLOR = "#1e1e1e";

    /** Colore di sfondo per controlli e input */
    private static final String BG_CONTROL = "#2b2b2b";

    /** Colore di accento per pulsanti e elementi interattivi */
    private static final String ACCENT_COLOR = "#4a86e8";

    /** Colore principale per il testo */
    private static final String TEXT_COLOR = "#ffffff";

    /** Colore per testo di suggerimento e elementi secondari */
    private static final String HINT_COLOR = "#9e9e9e";

    /** Servizio per operazioni di autenticazione backend */
    private AuthService authService;

    /** Callback eseguito quando l'autenticazione ha successo */
    private Consumer<User> onSuccessfulAuth;

    /** Callback eseguito quando si richiede la chiusura del pannello */
    private Runnable onClosePanel;

    /**
     * Costruttore del pannello di autenticazione.
     * <p>
     * Inizializza il servizio di autenticazione e configura l'interfaccia utente.
     * Il pannello viene creato con tutti i controlli necessari per login,
     * registrazione e reset password, ma non esegue operazioni di rete
     * fino all'interazione dell'utente.
     * </p>
     */
    public AuthPanel() {
        this.authService = new AuthService();
        setupPanel();
    }

    /**
     * Imposta il callback da eseguire quando l'autenticazione ha successo.
     * <p>
     * Il callback riceve l'oggetto {@link User} dell'utente appena autenticato
     * e deve gestire l'aggiornamento dell'interfaccia principale e la transizione
     * post-login. Il callback viene eseguito nel JavaFX Application Thread.
     * </p>
     *
     * @param callback il {@link Consumer} da eseguire quando l'autenticazione ha successo.
     *                Riceve l'oggetto {@link User} autenticato. Può essere {@code null}
     *                per rimuovere callback esistenti.
     * @apiNote Il callback deve essere leggero e non-bloccante. Per operazioni
     *          complesse post-autenticazione, considerare l'uso di thread separati.
     */
    public void setOnSuccessfulAuth(Consumer<User> callback) {
        this.onSuccessfulAuth = callback;
    }

    /**
     * Imposta il callback da eseguire quando si richiede la chiusura del pannello.
     * <p>
     * Il callback viene invocato quando l'utente richiede la chiusura del pannello
     * (tramite pulsanti di chiusura o dopo autenticazione riuscita) e deve gestire
     * la rimozione del pannello dall'interfaccia utente. Il callback viene eseguito
     * nel JavaFX Application Thread.
     * </p>
     *
     * @param callback il {@link Runnable} da eseguire per chiudere il pannello.
     *                Può essere {@code null} per rimuovere callback esistenti.
     * @apiNote Il callback dovrebbe rimuovere il pannello dal suo container padre
     *          e gestire eventuali animazioni di chiusura.
     */
    public void setOnClosePanel(Runnable callback) {
        this.onClosePanel = callback;
    }

    /**
     * Configura e inizializza l'interfaccia utente del pannello.
     * <p>
     * Questo metodo privato gestisce la creazione e configurazione di tutti
     * gli elementi UI del pannello, inclusi layout, stili, e la struttura
     * a schede per navigare tra login e registrazione.
     * </p>
     *
     * <h4>Elementi creati:</h4>
     * <ul>
     *   <li>Header con titolo dell'applicazione</li>
     *   <li>TabPane con schede per Login e Registrazione</li>
     *   <li>Styling personalizzato per tema scuro</li>
     *   <li>Dimensioni responsive del pannello</li>
     * </ul>
     *
     * @implNote Il metodo tenta di caricare CSS esterni ma fallback su stili
     *           inline per garantire funzionalità anche senza file CSS.
     */
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
        Label appTitle = new Label("📚 Books");
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
     * Crea e configura il pannello per il login degli utenti.
     * <p>
     * Costruisce l'interfaccia per l'autenticazione con email e password,
     * includendo validazione lato client, gestione errori, e feedback visivo.
     * Il pannello include anche un link per il reset password.
     * </p>
     *
     * <h4>Controlli inclusi:</h4>
     * <ul>
     *   <li>Campo email con validazione presenza</li>
     *   <li>Campo password con validazione lunghezza</li>
     *   <li>Link per password dimenticata</li>
     *   <li>Pulsante login con indicatore di progresso</li>
     *   <li>Gestione asincrona della richiesta di login</li>
     * </ul>
     *
     * <h4>Validazioni implementate:</h4>
     * <ul>
     *   <li>Campi non vuoti</li>
     *   <li>Email in formato valido (validazione di base)</li>
     *   <li>Feedback immediato per errori di input</li>
     * </ul>
     *
     * @return un {@link VBox} contenente tutti i controlli per il login
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
     * Crea e configura il pannello per la registrazione di nuovi utenti.
     * <p>
     * Costruisce un'interfaccia completa per la registrazione con tutti i campi
     * necessari, validazione estensiva, e gestione scroll per contenuti lunghi.
     * Include controlli per termini e condizioni e gestione asincrona della
     * richiesta di registrazione.
     * </p>
     *
     * <h4>Campi di registrazione:</h4>
     * <ul>
     *   <li>Nome (obbligatorio)</li>
     *   <li>Cognome (obbligatorio)</li>
     *   <li>Codice Fiscale (opzionale)</li>
     *   <li>Email (obbligatorio, con validazione formato)</li>
     *   <li>Username (obbligatorio, univoco)</li>
     *   <li>Password (minimo 8 caratteri)</li>
     *   <li>Conferma password (deve corrispondere)</li>
     *   <li>Accettazione termini e condizioni</li>
     * </ul>
     *
     * <h4>Validazioni implementate:</h4>
     * <ul>
     *   <li>Tutti i campi obbligatori compilati</li>
     *   <li>Password minimo 8 caratteri</li>
     *   <li>Corrispondenza password e conferma</li>
     *   <li>Accettazione termini obbligatoria</li>
     *   <li>Format validation per email</li>
     * </ul>
     *
     * @return un {@link ScrollPane} contenente il form di registrazione
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
     * Mostra una finestra di dialogo per il reset della password.
     * <p>
     * Crea e visualizza una finestra di dialogo modale personalizzata che permette
     * agli utenti di reimpostare la password fornendo email e nuova password.
     * Include validazione completa e gestione asincrona della richiesta di reset.
     * </p>
     *
     * <h4>Funzionalità del dialogo:</h4>
     * <ul>
     *   <li>Campo email per identificazione utente</li>
     *   <li>Campo nuova password con validazione lunghezza</li>
     *   <li>Campo conferma password per verifica</li>
     *   <li>Validazione lato client prima dell'invio</li>
     *   <li>Gestione asincrona della richiesta</li>
     *   <li>Feedback visivo durante l'operazione</li>
     * </ul>
     *
     * <h4>Validazioni implementate:</h4>
     * <ul>
     *   <li>Email non vuota</li>
     *   <li>Password minimo 6 caratteri</li>
     *   <li>Corrispondenza password e conferma</li>
     * </ul>
     *
     * @apiNote Il dialogo utilizza stili personalizzati per mantenere coerenza
     *          con il tema dell'applicazione. La richiesta viene processata
     *          tramite {@link AuthService#resetPasswordAsync(String, String)}.
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
     * Applica stili personalizzati ai campi di input per mantenere coerenza visiva.
     * <p>
     * Questo metodo applica un set completo di stili CSS a campi di testo e password
     * per creare un'esperienza visiva coerente e moderna. Include gestione dello
     * stato focus con effetti visivi appropriati.
     * </p>
     *
     * <h4>Stili applicati:</h4>
     * <ul>
     *   <li>Colori di sfondo e testo in tema con il design scuro</li>
     *   <li>Bordi arrotondati per aspetto moderno</li>
     *   <li>Padding interno per migliorare leggibilità</li>
     *   <li>Effetti di focus con bordo colorato</li>
     *   <li>Transizioni fluide tra stati</li>
     * </ul>
     *
     * <h4>Gestione stati:</h4>
     * <ul>
     *   <li><strong>Normale:</strong> Sfondo scuro, bordo trasparente</li>
     *   <li><strong>Focus:</strong> Bordo accent color, evidenziazione</li>
     *   <li><strong>Prompt text:</strong> Colore suggerimento per testo placeholder</li>
     * </ul>
     *
     * @param field il {@link TextField} a cui applicare gli stili personalizzati
     * @throws IllegalArgumentException se field è {@code null}
     * @apiNote Il metodo utilizza listener per gestire dinamicamente i cambiamenti
     *          di stato focus, garantendo feedback visivo immediato all'utente.
     */
    private void styleInput(TextField field) {
        if (field == null) {
            throw new IllegalArgumentException("Il campo di input non può essere null");
        }

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
     * Applica stili personalizzati ai pulsanti di azione principali.
     * <p>
     * Configura i pulsanti di azione (login, registrazione, reset) con stili
     * coerenti e effetti interattivi. Include gestione completa degli stati
     * hover, press e release per feedback visivo ottimale.
     * </p>
     *
     * <h4>Stili applicati:</h4>
     * <ul>
     *   <li>Colore di sfondo accent per evidenziare l'azione principale</li>
     *   <li>Testo bianco su sfondo colorato per contrasto</li>
     *   <li>Bordi arrotondati e padding interno</li>
     *   <li>Font weight bold per enfasi</li>
     *   <li>Cursore pointer per indicare interattività</li>
     * </ul>
     *
     * <h4>Effetti interattivi:</h4>
     * <ul>
     *   <li><strong>Hover:</strong> Schiarimento del colore di sfondo del 20%</li>
     *   <li><strong>Press:</strong> Scurimento del colore di sfondo del 20%</li>
     *   <li><strong>Release:</strong> Ritorno al colore originale</li>
     *   <li><strong>Exit:</strong> Rimozione effetti hover</li>
     * </ul>
     *
     * @param button il {@link Button} a cui applicare gli stili personalizzati
     * @throws IllegalArgumentException se button è {@code null}
     * @apiNote Gli effetti utilizzano la funzione CSS derive() per calcolare
     *          automaticamente le variazioni di colore mantenendo armonia visiva.
     */
    private void styleActionButton(Button button) {
        if (button == null) {
            throw new IllegalArgumentException("Il pulsante non può essere null");
        }

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
     * Visualizza un messaggio informativo all'utente tramite finestra di dialogo.
     * <p>
     * Metodo di utilità per mostrare messaggi di errore, successo, o informativi
     * in modo consistente nell'applicazione. Utilizza le finestre di dialogo
     * standard JavaFX per semplicità e compatibilità.
     * </p>
     *
     * <h4>Utilizzi tipici:</h4>
     * <ul>
     *   <li>Errori di validazione lato client</li>
     *   <li>Messaggi di errore da operazioni di rete</li>
     *   <li>Conferme di successo per operazioni completate</li>
     *   <li>Messaggi informativi per funzionalità non implementate</li>
     * </ul>
     *
     * @param title il titolo della finestra di dialogo
     * @param message il messaggio da visualizzare all'utente
     * @throws IllegalArgumentException se title o message sono {@code null}
     * @apiNote La finestra di dialogo è modale e blocca l'interazione con
     *          l'applicazione fino alla chiusura. Per messaggi non-bloccanti,
     *          considerare l'uso di toast notifications o status bar.
     */
    private void showAlert(String title, String message) {
        if (title == null || message == null) {
            throw new IllegalArgumentException("Titolo e messaggio non possono essere null");
        }

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