package org.BABO.client.ui;

import org.BABO.shared.model.Book;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Builder per la creazione del contenuto del libro in evidenza
 * Aggiornato per gestire ISBN e titolo separatamente
 */
public class FeaturedBookBuilder {

    private Consumer<Book> bookClickHandler;

    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
    }

    /**
     * Crea il contenuto per un libro in evidenza
     */
    public VBox createFeaturedBookContent(Book featuredBook) {
        HBox featuredBox = new HBox(20);
        featuredBox.setPadding(new Insets(20));
        featuredBox.setAlignment(Pos.CENTER_LEFT);

        ImageView cover = ImageUtils.createSafeImageView(featuredBook.getImageUrl(), 180, 270);
        Rectangle clip = new Rectangle(180, 270);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        cover.setClip(clip);

        // Rendi cliccabile la copertina in evidenza
        if (bookClickHandler != null) {
            cover.setOnMouseClicked(e -> bookClickHandler.accept(featuredBook));
            cover.setStyle("-fx-cursor: hand;");
        }

        VBox infoBox = createBookInfoBox(featuredBook);
        featuredBox.getChildren().addAll(cover, infoBox);

        VBox descriptionSection = createDescriptionSection(featuredBook);

        VBox content = new VBox();
        content.getChildren().addAll(featuredBox, descriptionSection);

        return content;
    }

    /**
     * Crea la sezione informazioni del libro
     */
    private VBox createBookInfoBox(Book book) {
        VBox infoBox = new VBox(12);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⭐ IN EVIDENZA");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setTextFill(Color.gray(0.7));

        Label bookTitle = new Label(book.getTitle());
        bookTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        bookTitle.setTextFill(Color.WHITE);
        bookTitle.setWrapText(true);

        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.setFont(Font.font("System", 16));
        authorLabel.setTextFill(Color.gray(0.8));

        // Aggiungi informazioni ISBN e anno se disponibili
        VBox additionalInfo = createAdditionalInfoBox(book);

        HBox buttonBox = createButtonBox(book);

        infoBox.getChildren().addAll(title, bookTitle, authorLabel, additionalInfo, buttonBox);
        return infoBox;
    }

    /**
     * Crea la sezione con informazioni aggiuntive (ISBN, anno)
     */
    private VBox createAdditionalInfoBox(Book book) {
        VBox additionalInfo = new VBox(5);
        additionalInfo.setPadding(new Insets(10, 0, 0, 0));

        // Mostra ISBN se disponibile
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            Label isbnLabel = new Label("📄 ISBN: " + book.getIsbn());
            isbnLabel.setFont(Font.font("System", 12));
            isbnLabel.setTextFill(Color.gray(0.6));
            additionalInfo.getChildren().add(isbnLabel);
        }

        // Mostra anno di pubblicazione se disponibile
        if (book.getPublishYear() != null && !book.getPublishYear().trim().isEmpty()) {
            Label yearLabel = new Label("📅 Anno: " + book.getPublishYear());
            yearLabel.setFont(Font.font("System", 12));
            yearLabel.setTextFill(Color.gray(0.6));
            additionalInfo.getChildren().add(yearLabel);
        }

        return additionalInfo;
    }

    /**
     * Crea i pulsanti di azione
     */
    private HBox createButtonBox(Book book) {
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button sampleButton = new Button("👁️ ANTEPRIMA");
        sampleButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 25;" +
                        "-fx-cursor: hand;"
        );

        // Aggiungi azione al pulsante anteprima per aprire il BookDetailsPopup
        if (bookClickHandler != null) {
            sampleButton.setOnAction(e -> bookClickHandler.accept(book));
        }

        buttonBox.getChildren().addAll(sampleButton);
        return buttonBox;
    }

    /**
     * Crea la sezione descrizione
     */
    private VBox createDescriptionSection(Book book) {
        Label previewLabel = new Label("📝 DESCRIZIONE");
        previewLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        previewLabel.setTextFill(Color.gray(0.7));
        previewLabel.setPadding(new Insets(0, 0, 8, 20));

        String descriptionPreview = book.getDescription();
        if (descriptionPreview != null && descriptionPreview.length() > 150) {
            descriptionPreview = descriptionPreview.substring(0, 150) + "...";
        }

        Label description = new Label(descriptionPreview != null ? descriptionPreview : "Descrizione non disponibile");
        description.setWrapText(true);
        description.setTextFill(Color.WHITE);
        description.setPadding(new Insets(0, 20, 20, 20));

        VBox descriptionSection = new VBox();
        descriptionSection.getChildren().addAll(previewLabel, description);
        return descriptionSection;
    }
}