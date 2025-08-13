package org.BABO.client.ui.category;

import javafx.scene.text.TextAlignment;
import org.BABO.shared.model.Category;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder per la creazione delle sezioni categorie
 */
public class CategorySectionBuilder {

    // Handler per i click sulle categorie
    private Consumer<Category> categoryClickHandler;

    /**
     *  Imposta il gestore dei click sulle categorie
     */
    public void setCategoryClickHandler(Consumer<Category> handler) {
        this.categoryClickHandler = handler;
    }

    /**
     * Crea una sezione di categorie in stile Apple Books
     */
    public VBox createCategorySection(String sectionTitle, List<Category> categories) {
        Label title = new Label(sectionTitle);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        Button seeAllBtn = new Button("Vedi tutti");
        seeAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0a84ff; -fx-border-color: transparent; -fx-cursor: hand;");

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, seeAllBtn);

        HBox categoryRow = new HBox(20);
        categoryRow.setPadding(new Insets(10));
        categoryRow.setAlignment(Pos.CENTER_LEFT);

        for (Category category : categories) {
            try {
                VBox categoryCard = createCategoryCard(category);
                categoryRow.getChildren().add(categoryCard);
            } catch (Exception e) {
                System.err.println("Errore caricamento categoria: " + category.getName() + " - " + e.getMessage());
                // Continua con le altre categorie invece di crashare
            }
        }

        ScrollPane scroll = new ScrollPane(categoryRow);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToHeight(true);
        scroll.setPrefHeight(185);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox section = new VBox(10, headerBox, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));
        section.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        return section;
    }

    /**
     * Crea una card di categoria
     */
    private VBox createCategoryCard(Category category) {
        Rectangle colorRect = new Rectangle(290, 155);

        // Colori diversi per categoria
        Color cardColor = getCategoryColor(category.getName());
        colorRect.setFill(cardColor);
        colorRect.setArcWidth(20);
        colorRect.setArcHeight(20);

        Label categoryLabel = new Label(category.getName().replace(" ", "\n"));
        categoryLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        categoryLabel.setTextFill(Color.WHITE);
        categoryLabel.setTextAlignment(TextAlignment.CENTER);
        categoryLabel.setWrapText(true);
        categoryLabel.setMaxWidth(260);

        // Aggiungi ombra al testo per leggibilità
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.BLACK);
        textShadow.setOffsetX(1);
        textShadow.setOffsetY(1);
        textShadow.setRadius(3);
        categoryLabel.setEffect(textShadow);

        StackPane card = new StackPane(colorRect, categoryLabel);
        StackPane.setAlignment(categoryLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(categoryLabel, new Insets(0, 0, 15, 15));
        card.setPrefSize(290, 155);
        card.setMaxSize(290, 155);
        card.setStyle("-fx-cursor: hand;");

        // Aggiungi effetto hover
        card.setOnMouseEntered(e -> {
            colorRect.setFill(cardColor.brighter());
        });

        card.setOnMouseExited(e -> {
            colorRect.setFill(cardColor);
        });

        // Click handler per la categoria
        card.setOnMouseClicked(e -> {
            System.out.println("🎭 Click su categoria: " + category.getName());
            if (categoryClickHandler != null) {
                categoryClickHandler.accept(category);
            } else {
                System.err.println("❌ CategoryClickHandler non impostato!");
            }
        });

        VBox cardContainer = new VBox(card);
        return cardContainer;
    }

    /**
     * Restituisce un colore diverso per ogni categoria
     */
    private Color getCategoryColor(String categoryName) {
        switch (categoryName.toLowerCase()) {
            case "thriller":
                return Color.web("#4a4a4a");
            case "romance":
                return Color.web("#e91e63");
            case "narrativa":
                return Color.web("#2196f3");
            case "saggistica":
                return Color.web("#ff9800");
            case "fantasy":
                return Color.web("#9c27b0");
            default:
                return Color.web("#666666");
        }
    }
}