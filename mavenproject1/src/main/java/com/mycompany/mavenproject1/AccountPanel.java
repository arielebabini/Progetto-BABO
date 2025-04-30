package com.mycompany.mavenproject1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AccountPanel extends VBox {

    public AccountPanel() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER_LEFT);
        setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 4);"
        );

        // Imposta dimensioni minime del pannello
        setMinWidth(300);
        setMinHeight(250);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        Label title = new Label("Account");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleInput(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleInput(passwordField);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button loginBtn = new Button("Login");
        Button signupBtn = new Button("Registrati");
        styleButton(loginBtn);
        styleButton(signupBtn);
        HBox.setHgrow(loginBtn, Priority.ALWAYS);
        HBox.setHgrow(signupBtn, Priority.ALWAYS);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        buttonBox.getChildren().addAll(loginBtn, signupBtn);

        getChildren().addAll(title, emailField, passwordField, buttonBox);
    }

    private void styleInput(TextField field) {
        field.setStyle(
            "-fx-background-color: #2b2b2b;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: gray;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 8;"
        );
        field.setMaxWidth(Double.MAX_VALUE);
    }

    private void styleButton(Button button) {
        button.setStyle(
            "-fx-background-color: #3a3a3a;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
    }
}