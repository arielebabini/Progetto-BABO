package org.BABO.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Gestisce l'header dell'applicazione con ricerca
 */
public class Header {

    private TextField searchField;
    private Consumer<String> searchHandler;

    public HBox createHeader() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #1e1e1e;");

        Label header = new Label("📚 Book Store");
        header.setFont(Font.font("System", FontWeight.BOLD, 28));
        header.setTextFill(Color.WHITE);

        searchField = createSearchField();

        headerBox.getChildren().add(header);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(headerSpacer, searchField);

        return headerBox;
    }

    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("🔍 Cerca libri...");
        field.setPrefWidth(280);
        field.setStyle(
                "-fx-background-color: #3a3a3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: gray;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 12;" +
                        "-fx-font-size: 14px;"
        );

        field.setOnAction(e -> {
            if (searchHandler != null) {
                searchHandler.accept(field.getText());
            }
        });

        return field;
    }

    public void setSearchHandler(Consumer<String> handler) {
        this.searchHandler = handler;
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public void clearSearch() {
        searchField.clear();
    }
}