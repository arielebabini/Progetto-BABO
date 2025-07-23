package org.BABO.client.ui;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder per la creazione e popolamento delle griglie di libri
 * Aggiornato per gestire ISBN e titolo separatamente
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
     * Popola la griglia dei libri con i dati ricevuti dal server
     */
    public void populateBookGrid(List<Book> books, FlowPane bookGrid, ScrollPane scroll) {
        bookGrid.getChildren().clear();

        if (books.isEmpty()) {
            if (scroll != null) {
                showNoBooksMessage(scroll);
            }
            return;
        }

        System.out.println("📚 Popolamento griglia con " + books.size() + " libri:");

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);

            // Debug delle informazioni del libro
            if (i < 3) { // Log primi 3 libri per debug
                System.out.println("  " + (i+1) + ". Titolo: '" + book.getTitle() +
                        "', ISBN: '" + book.getIsbn() +
                        "', Autore: '" + book.getAuthor() +
                        "', Anno: '" + book.getPublishYear() +
                        "', Immagine: '" + book.getImageUrl() + "'");
            }

            VBox bookBox = createBookCard(book);
            bookGrid.getChildren().add(bookBox);
        }

        // Cache dei libri per uso futuro
        if (!books.isEmpty() && cachedBooksCallback != null) {
            cachedBooksCallback.accept(new ArrayList<>(books));
        }

        if (scroll != null) {
            scroll.setContent(bookGrid);
        }

        System.out.println("✅ Griglia popolata con successo");
    }

    /**
     * Crea una card per un singolo libro con formattazione migliorata
     */
    private VBox createBookCard(Book book) {
        VBox bookBox = new VBox(6);
        bookBox.setAlignment(Pos.TOP_CENTER);
        bookBox.setMaxWidth(130);
        bookBox.setMinWidth(130);
        bookBox.setPrefHeight(240); // Altezza fissa per uniformità

        // Usa ImageUtils per caricare l'immagine in modo sicuro
        ImageView cover = ImageUtils.createSafeImageView(book.getImageUrl(), 120, 180);

        Rectangle clip = new Rectangle(120, 180);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        cover.setClip(clip);

        // Aggiungi click handler
        if (bookClickHandler != null) {
            cover.setOnMouseClicked(e -> {
                System.out.println("🔍 Click su libro: " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
                bookClickHandler.accept(book);
            });
            cover.setStyle("-fx-cursor: hand;");

            // Effetto hover
            cover.setOnMouseEntered(e -> cover.setOpacity(0.8));
            cover.setOnMouseExited(e -> cover.setOpacity(1.0));
        }

        // Container per le informazioni testuali
        VBox textContainer = new VBox(3);
        textContainer.setAlignment(Pos.TOP_CENTER);
        textContainer.setMaxWidth(120);
        textContainer.setPrefHeight(60); // Altezza fissa per il testo

        // Titolo del libro con troncamento intelligente
        String titleText = book.getTitle() != null ? book.getTitle() : "Titolo non disponibile";
        String formattedTitle = formatBookTitle(titleText);

        Label bookTitle = new Label(formattedTitle);
        bookTitle.setWrapText(false); // Disabilita wrap per controllare meglio il testo
        bookTitle.setMaxWidth(120);
        bookTitle.setTextFill(Color.WHITE);
        bookTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        bookTitle.setStyle(
                "-fx-text-overrun: ellipsis;" +
                        "-fx-text-alignment: center;"
        );

        // Autore del libro con troncamento
        String authorText = book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto";
        String formattedAuthor = formatAuthorName(authorText);

        Label bookAuthor = new Label(formattedAuthor);
        bookAuthor.setWrapText(false);
        bookAuthor.setMaxWidth(120);
        bookAuthor.setTextFill(Color.gray(0.7));
        bookAuthor.setFont(Font.font("System", FontWeight.NORMAL, 11));
        bookAuthor.setStyle(
                "-fx-text-overrun: ellipsis;" +
                        "-fx-text-alignment: center;"
        );

        // Anno di pubblicazione (opzionale, più piccolo)
        if (book.getPublishYear() != null && !book.getPublishYear().trim().isEmpty()) {
            Label yearLabel = new Label("(" + book.getPublishYear() + ")");
            yearLabel.setTextFill(Color.gray(0.5));
            yearLabel.setFont(Font.font("System", FontWeight.NORMAL, 9));
            yearLabel.setStyle("-fx-text-alignment: center;");
            textContainer.getChildren().add(yearLabel);
        }

        textContainer.getChildren().addAll(bookTitle, bookAuthor);
        bookBox.getChildren().addAll(cover, textContainer);

        return bookBox;
    }

    /**
     * Formatta il titolo del libro per la visualizzazione nelle card
     */
    private String formatBookTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Titolo non disponibile";
        }

        // Rimuovi caratteri extra e normalizza
        String cleaned = title.trim().replaceAll("\\s+", " ");

        // Se il titolo è troppo lungo, troncalo intelligentemente
        if (cleaned.length() > 35) {
            // Cerca un punto di interruzione naturale (spazio, due punti, trattino)
            String truncated = cleaned.substring(0, 32);
            int lastSpace = truncated.lastIndexOf(' ');
            int lastColon = truncated.lastIndexOf(':');
            int lastDash = truncated.lastIndexOf('-');

            int breakPoint = Math.max(Math.max(lastSpace, lastColon), lastDash);

            if (breakPoint > 15) { // Se c'è un buon punto di interruzione
                return cleaned.substring(0, breakPoint).trim() + "...";
            } else {
                return truncated.trim() + "...";
            }
        }

        return cleaned;
    }

    /**
     * Formatta il nome dell'autore per la visualizzazione nelle card
     */
    private String formatAuthorName(String author) {
        if (author == null || author.trim().isEmpty()) {
            return "Autore sconosciuto";
        }

        String cleaned = author.trim().replaceAll("\\s+", " ");

        // Se l'autore è troppo lungo, troncalo
        if (cleaned.length() > 25) {
            String truncated = cleaned.substring(0, 22);
            int lastSpace = truncated.lastIndexOf(' ');

            if (lastSpace > 10) {
                return cleaned.substring(0, lastSpace).trim() + "...";
            } else {
                return truncated.trim() + "...";
            }
        }

        return cleaned;
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
     * Crea una griglia di libri con layout ottimizzato
     */
    public FlowPane createOptimizedBookGrid() {
        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20); // Aumentato lo spazio orizzontale
        bookGrid.setVgap(25); // Aumentato lo spazio verticale
        bookGrid.setPadding(new Insets(20));
        bookGrid.setAlignment(Pos.TOP_LEFT);

        // Imposta una larghezza preferita per le righe
        bookGrid.setPrefWrapLength(800); // Aiuta con il wrapping

        return bookGrid;
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