package org.BABO.client.ui.Book;

import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.shared.model.Book;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;

import java.util.List;
import java.util.function.Consumer;

/**
 * Builder per la creazione e popolamento delle griglie di libri
 * RIPRISTINATO: Layout FlowPane adattivo per tutte le sezioni
 */
public class BookGridBuilder {

    private Consumer<Book> bookClickHandler;
    private Consumer<List<Book>> cachedBooksCallback;

    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
    }

    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        this.cachedBooksCallback = callback;
    }

    /**
     * Popola la griglia dei libri con i dati ricevuti dal server (FlowPane adattivo)
     */
    public void populateBookGrid(List<Book> books, FlowPane bookGrid, ScrollPane scroll) {
        bookGrid.getChildren().clear();

        if (books.isEmpty()) {
            if (scroll != null) {
                showNoBooksMessage(scroll);
            }
            return;
        }

        System.out.println("📚 Popolamento griglia con " + books.size() + " libri (layout adattivo):");

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);

            // Debug delle informazioni del libro
            if (i < 3) { // Log primi 3 libri per debug
                System.out.println("  " + (i+1) + ". " + book.getTitle() + " - " + book.getAuthor());
            }

            VBox bookCard = createBookCard(book);
            bookGrid.getChildren().add(bookCard);
        }

        // Callback per cache se impostato
        if (cachedBooksCallback != null) {
            cachedBooksCallback.accept(books);
        }
    }

    /**
     * Crea una card per libro con layout adattivo
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setMaxWidth(140);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-cursor: hand;");

        // Immagine copertina
        ImageView cover = ImageUtils.createSafeImageView(book.getSafeImageFileName(), 120, 170);

        Rectangle clip = new Rectangle(120, 170);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        cover.setClip(clip);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        cover.setEffect(shadow);

        // Titolo
        String titleText = book.getTitle() != null ?
                cleanTitle(book.getTitle()) : "Titolo non disponibile";

        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.NORMAL, 13));
        title.setTextFill(Color.WHITE);
        title.setMaxWidth(135);
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);

        // Autore
        String authorText = book.getAuthor() != null ?
                book.getAuthor() : "Autore sconosciuto";

        Label author = new Label(authorText);
        author.setFont(Font.font("System", FontWeight.LIGHT, 12));
        author.setTextFill(Color.web("#AAAAAA"));
        author.setMaxWidth(135);
        author.setWrapText(true);
        author.setAlignment(Pos.CENTER);

        // Click handler
        if (bookClickHandler != null) {
            card.setOnMouseClicked(e -> bookClickHandler.accept(book));
        }

        card.getChildren().addAll(cover, title, author);
        return card;
    }

    /**
     * Pulisce il titolo del libro
     */
    private String cleanTitle(String title) {
        if (title == null) return "Titolo non disponibile";

        String cleaned = title.trim();

        // Rimuovi eventuali caratteri di controllo o sequenze di escape
        cleaned = cleaned.replaceAll("[\\p{Cntrl}]", " ");

        // Limita la lunghezza del titolo
        if (cleaned.length() > 50) {
            cleaned = cleaned.substring(0, 47) + "...";
        }

        return cleaned;
    }

    /**
     * Crea una griglia di libri con layout ottimizzato FlowPane
     */
    public FlowPane createOptimizedBookGrid() {
        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(20));
        bookGrid.setAlignment(Pos.TOP_LEFT);

        bookGrid.prefWrapLengthProperty().bind(
                bookGrid.widthProperty().subtract(40)
        );

        return bookGrid;
    }

    /**
     * Mostra messaggio quando non ci sono libri
     */
    private void showNoBooksMessage(ScrollPane scroll) {
        Label noBooks = new Label("📚 Nessun libro disponibile");
        noBooks.setTextFill(Color.GRAY);
        noBooks.setFont(Font.font("System", 16));

        VBox noBooksBox = new VBox(noBooks);
        noBooksBox.setAlignment(Pos.CENTER);
        noBooksBox.setPrefHeight(280);

        scroll.setContent(noBooksBox);
    }

    /**
     * Aggiorna la griglia mantenendo la posizione di scroll
     */
    public void updateBookGrid(List<Book> books, FlowPane bookGrid, ScrollPane scroll, double scrollPosition) {
        populateBookGrid(books, bookGrid, scroll);

        // Mantieni la posizione di scroll se specificata
        if (scroll != null && scrollPosition >= 0) {
            scroll.setVvalue(scrollPosition);
        }
    }
}