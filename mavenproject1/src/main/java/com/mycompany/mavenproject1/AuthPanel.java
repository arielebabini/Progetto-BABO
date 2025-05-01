package com.mycompany.mavenproject1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Pannello principale per l'autenticazione che include sia Login che Registrazione
 * tramite un sistema a tab per passare facilmente da uno all'altro.
 */
public class AuthPanel extends VBox {
    
    // Colori e stili principali
    private static final String BG_COLOR = "#1e1e1e";
    private static final String BG_CONTROL = "#2b2b2b";
    private static final String ACCENT_COLOR = "#4a86e8";
    private static final String TEXT_COLOR = "#ffffff";
    private static final String HINT_COLOR = "#9e9e9e";
    
    public AuthPanel() {
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
        setMinWidth(350);
        setMinHeight(450);
        setMaxWidth(450);
        
        // Logo o titolo dell'applicazione
        Label appTitle = new Label("MyApp");
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
        authTabs.getStylesheets().add(getClass().getResource("/css/auth-tabs.css").toExternalForm());
        
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
        
        Label loginTitle = new Label("Accedi al tuo account");
        loginTitle.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loginTitle.setTextFill(Color.web(TEXT_COLOR));
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleInput(emailField);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleInput(passwordField);
        
        // Opzione "Ricordami"
        HBox rememberBox = new HBox(10);
        rememberBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox rememberMe = new CheckBox("Ricordami");
        rememberMe.setTextFill(Color.web(HINT_COLOR));
        rememberBox.getChildren().add(rememberMe);
        
        // Link per password dimenticata
        Hyperlink forgotPassword = new Hyperlink("Password dimenticata?");
        forgotPassword.setTextFill(Color.web(ACCENT_COLOR));
        HBox forgotBox = new HBox(forgotPassword);
        forgotBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Box per checkbox e link
        HBox optionsBox = new HBox();
        optionsBox.getChildren().addAll(rememberBox, forgotBox);
        HBox.setHgrow(rememberBox, Priority.ALWAYS);
        HBox.setHgrow(forgotBox, Priority.ALWAYS);
        
        // Pulsante di login
        Button loginBtn = new Button("ACCEDI");
        styleActionButton(loginBtn);
        
        // Separatore
        Separator separator = new Separator();
        separator.setPadding(new Insets(15, 0, 15, 0));
        
        // Login con social
        Label socialLabel = new Label("Oppure accedi con");
        socialLabel.setTextFill(Color.web(HINT_COLOR));
        
        HBox socialBox = new HBox(15);
        socialBox.setAlignment(Pos.CENTER);
        
        Button googleBtn = createSocialButton("Google");
        Button facebookBtn = createSocialButton("Facebook");
        
        socialBox.getChildren().addAll(googleBtn, facebookBtn);
        
        loginPanel.getChildren().addAll(
            loginTitle, 
            emailField, 
            passwordField, 
            optionsBox, 
            loginBtn,
            separator,
            socialLabel,
            socialBox
        );
        
        return loginPanel;
    }
    
    /**
     * Crea il pannello per la registrazione
     */
    private VBox createSignupPanel() {
        VBox signupPanel = new VBox(15);
        signupPanel.setPadding(new Insets(25, 15, 15, 15));
        signupPanel.setAlignment(Pos.TOP_CENTER);
        
        Label signupTitle = new Label("Crea un nuovo account");
        signupTitle.setFont(Font.font("System", FontWeight.NORMAL, 16));
        signupTitle.setTextFill(Color.web(TEXT_COLOR));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Nome completo");
        styleInput(nameField);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleInput(emailField);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleInput(passwordField);
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Conferma password");
        styleInput(confirmPasswordField);
        
        // Accettazione termini
        HBox termsBox = new HBox(10);
        termsBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox termsCheck = new CheckBox("Accetto i termini e le condizioni");
        termsCheck.setTextFill(Color.web(HINT_COLOR));
        termsBox.getChildren().add(termsCheck);
        
        // Pulsante di registrazione
        Button signupBtn = new Button("REGISTRATI");
        styleActionButton(signupBtn);
        
        // Separatore
        Separator separator = new Separator();
        separator.setPadding(new Insets(15, 0, 15, 0));
        
        // Registrazione con social
        Label socialLabel = new Label("Oppure registrati con");
        socialLabel.setTextFill(Color.web(HINT_COLOR));
        
        HBox socialBox = new HBox(15);
        socialBox.setAlignment(Pos.CENTER);
        
        Button googleBtn = createSocialButton("Google");
        Button facebookBtn = createSocialButton("Facebook");
        
        socialBox.getChildren().addAll(googleBtn, facebookBtn);
        
        signupPanel.getChildren().addAll(
            signupTitle, 
            nameField,
            emailField, 
            passwordField, 
            confirmPasswordField,
            termsBox, 
            signupBtn,
            separator,
            socialLabel,
            socialBox
        );
        
        return signupPanel;
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
}