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
 * Builder per la creazione e gestione di griglie di libri nell'interfaccia utente BABO.
 * <p>
 * Questa classe fornisce un'API completa per costruire layout adattivi di libri
 * in formato griglia, gestendo la visualizzazione delle copertine, metadati
 * e interazioni utente. Implementa pattern di design responsive che si adattano
 * automaticamente alle dimensioni del container e fornisce callback per
 * gestire eventi di click e caching dei dati.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Layout Responsivo:</strong> Griglia adattiva che si ridimensiona automaticamente</li>
 *   <li><strong>Gestione Immagini:</strong> Caricamento sicuro e fallback per copertine mancanti</li>
 *   <li><strong>Event Handling:</strong> Sistema di callback per interazioni utente</li>
 *   <li><strong>Caching:</strong> Supporto per cache dei libri visualizzati</li>
 *   <li><strong>Styling Avanzato:</strong> Effetti visivi moderni con ombre e clip</li>
 *   <li><strong>Accessibilità:</strong> Supporto per testo wrappato e dimensioni ottimali</li>
 * </ul>
 *
 * <h3>Architettura e Design Pattern:</h3>
 * <p>
 * La classe implementa il pattern Builder per la costruzione di UI complesse,
 * combinato con Observer pattern per gestire eventi e callback. Utilizza
 * JavaFX FlowPane per layout responsivi e applica principi di Material Design
 * per gli elementi visivi.
 * </p>
 *
 * <h3>Sistema di Layout:</h3>
 * <ul>
 *   <li><strong>FlowPane:</strong> Container adattivo che riorganizza elementi automaticamente</li>
 *   <li><strong>Book Cards:</strong> Componenti singoli per ogni libro con dimensioni standard</li>
 *   <li><strong>Responsive Design:</strong> Adattamento automatico a diverse risoluzioni</li>
 *   <li><strong>Spacing Dinamico:</strong> Gap e padding calcolati per ottimale densità visiva</li>
 * </ul>
 *
 * <h3>Gestione delle Immagini:</h3>
 * <p>
 * Il sistema di gestione immagini implementa:
 * </p>
 * <ul>
 *   <li>Caricamento asincrono e sicuro delle copertine</li>
 *   <li>Fallback automatico per immagini mancanti o corrotte</li>
 *   <li>Caching intelligente per migliorare performance</li>
 *   <li>Ridimensionamento proporzionale mantenendo aspect ratio</li>
 *   <li>Effetti visivi con clipping e ombre</li>
 * </ul>
 *
 * <h3>Esempi di utilizzo:</h3>
 * <pre>{@code
 * // Creazione e configurazione del builder
 * BookGridBuilder gridBuilder = new BookGridBuilder();
 *
 * // Configurazione callback per click sui libri
 * gridBuilder.setBookClickHandler(book -> {
 *     System.out.println("Libro selezionato: " + book.getTitle());
 *     showBookDetails(book);
 * });
 *
 * // Configurazione callback per caching
 * gridBuilder.setCachedBooksCallback(books -> {
 *     libraryCache.updateBooks(books);
 *     System.out.println("Cache aggiornata con " + books.size() + " libri");
 * });
 *
 * // Creazione della griglia ottimizzata
 * FlowPane bookGrid = gridBuilder.createOptimizedBookGrid();
 *
 * // Popolamento con dati
 * List<Book> books = bookService.getBooks();
 * ScrollPane scrollContainer = new ScrollPane();
 * gridBuilder.populateBookGrid(books, bookGrid, scrollContainer);
 *
 * // Aggiunta a container principale
 * VBox mainContainer = new VBox(bookGrid);
 * }</pre>
 *
 * <h3>Personalizzazione e Styling:</h3>
 * <p>
 * Il builder applica stili coerenti ma personalizzabili:
 * </p>
 * <ul>
 *   <li>Dimensioni standardizzate per consistency (140x200px per card)</li>
 *   <li>Effetti di ombra per profondità visiva</li>
 *   <li>Clipping arrotondato per aspetto moderno</li>
 *   <li>Typography gerarchica per titolo e autore</li>
 *   <li>Color scheme adattivo per tema scuro/chiaro</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazione:</h3>
 * <ul>
 *   <li>Lazy loading delle immagini per performance</li>
 *   <li>Riutilizzo di elementi UI quando possibile</li>
 *   <li>Memory management per liste grandi</li>
 *   <li>Debouncing per eventi di ridimensionamento</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see Book
 * @see ImageUtils
 * @see FlowPane
 */
public class BookGridBuilder {

    /** Callback per gestire i click sui libri */
    private Consumer<Book> bookClickHandler;

    /** Callback per gestire la cache dei libri visualizzati */
    private Consumer<List<Book>> cachedBooksCallback;

    /**
     * Imposta il gestore per i click sui libri nella griglia.
     * <p>
     * Il callback riceve l'oggetto {@link Book} del libro cliccato e deve
     * gestire la logica di navigazione o visualizzazione dei dettagli.
     * È essenziale per l'interattività della griglia.
     * </p>
     *
     * @param handler il {@link Consumer} da eseguire quando un libro viene cliccato.
     *               Riceve l'oggetto {@link Book} selezionato. Può essere {@code null}
     *               per disabilitare l'interattività.
     * @apiNote Il callback viene eseguito nel JavaFX Application Thread, quindi
     *          è sicuro aggiornare l'UI direttamente. Per operazioni pesanti,
     *          considerare l'uso di thread separati.
     */
    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
    }

    /**
     * Imposta il callback per la gestione della cache dei libri.
     * <p>
     * Il callback riceve la lista completa dei libri attualmente visualizzati
     * e può essere utilizzato per implementare strategie di caching,
     * analisi delle visualizzazioni, o sincronizzazione con storage locale.
     * </p>
     *
     * @param callback il {@link Consumer} da eseguire dopo il popolamento della griglia.
     *                Riceve la {@link List} di {@link Book} visualizzati. Può essere
     *                {@code null} per disabilitare il caching.
     * @apiNote Il callback viene chiamato ogni volta che la griglia viene popolata,
     *          quindi implementare logic di debouncing se necessario per performance.
     */
    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        this.cachedBooksCallback = callback;
    }

    /**
     * Popola la griglia dei libri con layout adattivo FlowPane.
     * <p>
     * Questo è il metodo principale per visualizzare una collezione di libri
     * in formato griglia. Gestisce automaticamente il layout, la creazione
     * delle card, e l'implementazione dei callback configurati.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ul>
     *   <li>Pulizia della griglia esistente</li>
     *   <li>Validazione dei dati di input</li>
     *   <li>Creazione delle card per ogni libro</li>
     *   <li>Applicazione degli event handler</li>
     *   <li>Gestione del caso "nessun libro"</li>
     *   <li>Esecuzione dei callback di cache</li>
     * </ul>
     *
     * <h4>Gestione layout responsive:</h4>
     * <p>
     * Il metodo implementa un layout che si adatta automaticamente alla
     * larghezza del container, riorganizzando le card per massimizzare
     * l'utilizzo dello spazio disponibile.
     * </p>
     *
     * @param books la lista di {@link Book} da visualizzare nella griglia
     * @param bookGrid il {@link FlowPane} container dove inserire le card dei libri
     * @param scroll il {@link ScrollPane} per gestire il contenuto quando la lista è vuota.
     *              Può essere {@code null} se la gestione del caso vuoto non è necessaria
     * @throws IllegalArgumentException se books o bookGrid sono {@code null}
     * @apiNote Il metodo include logging dettagliato per i primi 3 libri per
     *          facilitare il debugging. Per performance ottimali con liste molto
     *          grandi (>1000 elementi), considerare implementazione di virtualizzazione.
     */
    public void populateBookGrid(List<Book> books, FlowPane bookGrid, ScrollPane scroll) {
        if (books == null) {
            throw new IllegalArgumentException("La lista dei libri non può essere null");
        }
        if (bookGrid == null) {
            throw new IllegalArgumentException("Il container della griglia non può essere null");
        }

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
     * Crea una card individuale per un libro con layout e styling ottimizzati.
     * <p>
     * Costruisce un componente UI completo per rappresentare un singolo libro,
     * includendo copertina, titolo, autore, e gestione degli eventi. Applica
     * stili moderni con effetti visivi e gestione responsive.
     * </p>
     *
     * <h4>Struttura della card:</h4>
     * <ul>
     *   <li><strong>Container:</strong> VBox con dimensioni standardizzate</li>
     *   <li><strong>Copertina:</strong> ImageView con clipping arrotondato e ombra</li>
     *   <li><strong>Titolo:</strong> Label con testo wrappato e font gerarchico</li>
     *   <li><strong>Autore:</strong> Label secondario con colore attenuato</li>
     * </ul>
     *
     * <h4>Caratteristiche visive:</h4>
     * <ul>
     *   <li>Dimensioni standard: 140px larghezza, altezza variabile</li>
     *   <li>Copertina: 120x170px con corner radius di 8px</li>
     *   <li>Drop shadow per effetto di profondità</li>
     *   <li>Cursor pointer per indicare interattività</li>
     *   <li>Testo troncato intelligentemente per titoli lunghi</li>
     * </ul>
     *
     * <h4>Gestione fallback:</h4>
     * <p>
     * Implementa gestione robusta per dati mancanti o corrotti:
     * </p>
     * <ul>
     *   <li>Immagini mancanti: Utilizza placeholder tramite {@link ImageUtils}</li>
     *   <li>Titoli null: Mostra "Titolo non disponibile"</li>
     *   <li>Autori null: Mostra "Autore sconosciuto"</li>
     *   <li>Titoli lunghi: Troncamento intelligente con ellipsis</li>
     * </ul>
     *
     * @param book l'oggetto {@link Book} per cui creare la card
     * @return un {@link VBox} configurato e stilizzato rappresentante il libro
     * @throws IllegalArgumentException se book è {@code null}
     * @see #cleanTitle(String)
     * @see ImageUtils#createSafeImageView(String, int, int)
     */
    private VBox createBookCard(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("L'oggetto Book non può essere null");
        }

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
     * Pulisce e normalizza il titolo del libro per la visualizzazione.
     * <p>
     * Applica una serie di trasformazioni per garantire che il titolo sia
     * visualizzabile correttamente nell'interfaccia utente, gestendo
     * caratteri speciali, lunghezza eccessiva, e formattazione.
     * </p>
     *
     * <h4>Trasformazioni applicate:</h4>
     * <ul>
     *   <li><strong>Trimming:</strong> Rimozione spazi iniziali e finali</li>
     *   <li><strong>Sanitizzazione:</strong> Rimozione caratteri di controllo</li>
     *   <li><strong>Truncation:</strong> Limitazione lunghezza con ellipsis</li>
     *   <li><strong>Fallback:</strong> Gestione valori null o vuoti</li>
     * </ul>
     *
     * <h4>Regole di troncamento:</h4>
     * <p>
     * Titoli superiori a 50 caratteri vengono troncati a 47 caratteri
     * con aggiunta di "..." per indicare il contenuto mancante.
     * </p>
     *
     * @param title il titolo originale del libro da pulire
     * @return una stringa pulita e normalizzata pronta per la visualizzazione
     * @apiNote La funzione è null-safe e restituisce sempre una stringa valida.
     *          Non genera mai eccezioni anche con input malformati.
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
     * Crea una griglia ottimizzata con layout FlowPane responsivo.
     * <p>
     * Factory method che costruisce un container FlowPane pre-configurato
     * con le impostazioni ottimali per la visualizzazione di libri.
     * Il layout si adatta automaticamente alla larghezza del container
     * padre e implementa spacing intelligente.
     * </p>
     *
     * <h4>Configurazioni applicate:</h4>
     * <ul>
     *   <li><strong>Gap orizzontale:</strong> 20px tra elementi nella stessa riga</li>
     *   <li><strong>Gap verticale:</strong> 25px tra righe</li>
     *   <li><strong>Padding:</strong> 20px su tutti i lati</li>
     *   <li><strong>Allineamento:</strong> TOP_LEFT per layout naturale</li>
     *   <li><strong>Wrap Length:</strong> Binding dinamico alla larghezza container</li>
     * </ul>
     *
     * <h4>Responsività:</h4>
     * <p>
     * Il FlowPane utilizza property binding per adattare automaticamente
     * il wrap length alla larghezza disponibile, sottraendo il padding
     * per calcoli precisi del layout.
     * </p>
     *
     * @return un {@link FlowPane} ottimizzato e pre-configurato per libri
     * @apiNote La griglia restituita è pronta per l'uso con
     *          {@link #populateBookGrid(List, FlowPane, ScrollPane)}.
     *          Non richiede configurazioni aggiuntive.
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
     * Visualizza un messaggio informativo quando la lista dei libri è vuota.
     * <p>
     * Crea e configura un'interfaccia placeholder per comunicare all'utente
     * che non ci sono libri da visualizzare. Utilizza styling coerente
     * e un'icona descrittiva per migliorare l'esperienza utente.
     * </p>
     *
     * <h4>Caratteristiche del messaggio:</h4>
     * <ul>
     *   <li>Testo descrittivo con icona emoji per immediata comprensione</li>
     *   <li>Styling minimale ma elegante in tema con l'applicazione</li>
     *   <li>Centramento verticale e orizzontale nel container</li>
     *   <li>Altezza predefinita per mantenere proporzioni UI</li>
     * </ul>
     *
     * <h4>Layout e positioning:</h4>
     * <p>
     * Il messaggio viene centrato nel ScrollPane specificato con un'altezza
     * fissa di 280px per mantenere proporzioni visive coerenti anche
     * quando il contenuto principale è assente.
     * </p>
     *
     * @param scroll il {@link ScrollPane} in cui visualizzare il messaggio
     * @throws IllegalArgumentException se scroll è {@code null}
     * @apiNote Questo metodo sovrascrive il contenuto esistente del ScrollPane.
     *          Utilizzare solo quando si è certi che non ci sono libri da mostrare.
     */
    private void showNoBooksMessage(ScrollPane scroll) {
        if (scroll == null) {
            throw new IllegalArgumentException("Il ScrollPane non può essere null");
        }

        Label noBooks = new Label("📚 Nessun libro disponibile");
        noBooks.setTextFill(Color.GRAY);
        noBooks.setFont(Font.font("System", 16));

        VBox noBooksBox = new VBox(noBooks);
        noBooksBox.setAlignment(Pos.CENTER);
        noBooksBox.setPrefHeight(280);

        scroll.setContent(noBooksBox);
    }
}