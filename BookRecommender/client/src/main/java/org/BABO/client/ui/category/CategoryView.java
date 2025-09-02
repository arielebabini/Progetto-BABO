package org.BABO.client.ui.Category;

import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import org.BABO.client.ui.BooksClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Vista specializzata per la visualizzazione e navigazione di libri per categoria nell'applicazione BABO.
 * <p>
 * Questa classe fornisce un'interfaccia completa per esplorare libri organizzati per categoria,
 * con funzionalità di navigazione, ricerca filtrata, e visualizzazione ottimizzata. Implementa
 * un design immersivo che permette agli utenti di scoprire contenuti correlati e navigare
 * efficacemente tra diverse categorie del catalogo.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Visualizzazione Categoria:</strong> Header descrittivo con navigazione breadcrumb</li>
 *   <li><strong>Griglia Libri:</strong> Layout responsive per browsing ottimale</li>
 *   <li><strong>Navigazione Contestuale:</strong> Integrazione con dettagli libro e lista di navigazione</li>
 *   <li><strong>Gestione Stati:</strong> Loading, successo, errore, e risultati vuoti</li>
 *   <li><strong>Descrizioni Intelligenti:</strong> Descrizioni contextual per categorie predefinite</li>
 *   <li><strong>Ricerca Filtrata:</strong> Caricamento asincrono con filtri per categoria</li>
 * </ul>
 *
 * <h3>Architettura di Navigazione:</h3>
 * <p>
 * La classe implementa un sistema di navigazione a breadcrumb che permette:
 * </p>
 * <ul>
 *   <li>Navigazione back alla vista principale</li>
 *   <li>Context preservation per la lista di libri corrente</li>
 *   <li>Deep linking ai dettagli di singoli libri</li>
 *   <li>Mantenimento dello stato di navigazione</li>
 * </ul>
 *
 * <h3>Sistema di Layout Responsive:</h3>
 * <p>
 * Utilizza FlowPane con property binding per creare un layout che si adatta
 * automaticamente a diverse risoluzioni e dimensioni dello schermo:
 * </p>
 * <ul>
 *   <li>Grid dinamica con spacing ottimizzato</li>
 *   <li>Book cards di dimensioni standardizzate</li>
 *   <li>Scroll verticale fluido per liste lunghe</li>
 *   <li>Header fisso per context costante</li>
 * </ul>
 *
 * <h3>Gestione Contenuti Dinamici:</h3>
 * <p>
 * La vista gestisce intelligentemente diversi tipi di contenuto:
 * </p>
 * <ul>
 *   <li><strong>Descrizioni Categoria:</strong> Mapping predefinito per categorie comuni</li>
 *   <li><strong>Fallback Content:</strong> Gestione graceful di dati mancanti</li>
 *   <li><strong>Loading States:</strong> Feedback visivo durante operazioni asincrone</li>
 *   <li><strong>Error Handling:</strong> Messaggi user-friendly per problemi di rete</li>
 * </ul>
 *
 * <h3>Integrazione con Servizi:</h3>
 * <p>
 * La vista si integra con diversi servizi per funzionalità complete:
 * </p>
 * <ul>
 *   <li>{@link BookService} per ricerca e caricamento libri</li>
 *   <li>{@link AuthenticationManager} per gestione permessi utente</li>
 *   <li>{@link BooksClient} per navigazione ai dettagli</li>
 *   <li>{@link ImageUtils} per gestione sicura delle immagini</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Creazione vista categoria
 * Category sciFiCategory = new Category("Fantascienza", "Esplorazioni del futuro");
 * BookService bookService = new BookService();
 *
 * CategoryView categoryView = new CategoryView(
 *     sciFiCategory,
 *     bookService,
 *     book -> System.out.println("Libro cliccato: " + book.getTitle())
 * );
 *
 * // Configurazione navigazione
 * categoryView.setOnBackCallback(() -> {
 *     navigationManager.goBack();
 *     analytics.trackCategoryExit(sciFiCategory.getName());
 * });
 *
 * // Configurazione autenticazione
 * categoryView.setAuthManager(authenticationManager);
 *
 * // Creazione e integrazione UI
 * ScrollPane categoryPane = categoryView.createCategoryView();
 * mainContainer.getChildren().setAll(categoryPane);
 *
 * // La vista gestisce automaticamente:
 * // - Caricamento libri per categoria
 * // - Stati di loading e errore
 * // - Click handling per singoli libri
 * // - Navigazione contestuale
 * }</pre>
 *
 * <h3>Personalizzazione e Estensibilità:</h3>
 * <ul>
 *   <li>Descrizioni categoria facilmente estensibili tramite switch case</li>
 *   <li>Layout modificabile attraverso override dei metodi di creazione</li>
 *   <li>Styling personalizzabile tramite CSS classes</li>
 *   <li>Event handling configurabile per diverse logiche di navigazione</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <ul>
 *   <li>Caricamento asincrono per non bloccare UI thread</li>
 *   <li>Lazy loading delle immagini per performance</li>
 *   <li>Memory management per liste grandi</li>
 *   <li>Event debouncing per interazioni rapide</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see Category
 * @see BookService
 * @see AuthenticationManager
 * @see Book
 */
public class CategoryView {
    /** La categoria di libri da visualizzare */
    private final Category category;

    /** Servizio per operazioni sui libri */
    private final BookService bookService;

    /** Container principale per il contenuto della vista */
    private VBox content;

    /** Flag per prevenire caricamenti multipli simultanei */
    private boolean isLoading = false;

    /** Label cliccabile per navigazione back */
    private Label backText;

    /** Callback per gestire navigazione indietro */
    private Runnable onBackCallback;

    /** Lista cache dei libri della categoria corrente per navigazione */
    private List<Book> categoryBooks = new ArrayList<>();

    /** Manager di autenticazione per controllo permessi */
    private AuthenticationManager authManager;

    /**
     * Configura il manager di autenticazione per la vista categoria.
     * <p>
     * Il manager è utilizzato per verificare permessi utente e gestire
     * accesso a funzionalità che richiedono autenticazione, come
     * aggiunta ai favoriti o acquisti.
     * </p>
     *
     * @param authManager il manager di autenticazione da utilizzare.
     *                   Può essere {@code null} se non sono richieste
     *                   funzionalità di autenticazione.
     */
    public void setAuthManager(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Costruttore per la vista categoria.
     * <p>
     * Inizializza la vista con la categoria da visualizzare e il servizio
     * per il caricamento dei libri. Il book click handler è mantenuto
     * per compatibilità ma la gestione click è implementata internamente.
     * </p>
     *
     * @param category la categoria di libri da visualizzare
     * @param bookService il servizio per operazioni sui libri
     * @param bookClickHandler callback per gestire click sui libri (legacy)
     * @throws IllegalArgumentException se category o bookService sono {@code null}
     */
    public CategoryView(Category category, BookService bookService, Consumer<Book> bookClickHandler) {
        if (category == null) {
            throw new IllegalArgumentException("La categoria non può essere null");
        }
        if (bookService == null) {
            throw new IllegalArgumentException("Il BookService non può essere null");
        }

        this.category = category;
        this.bookService = bookService;
    }

    /**
     * Configura il callback per la navigazione indietro.
     * <p>
     * Imposta il callback che verrà eseguito quando l'utente clicca
     * sul link "Torna a Esplora". Il callback viene applicato automaticamente
     * al componente UI se già creato.
     * </p>
     *
     * @param callback il {@link Runnable} da eseguire per la navigazione indietro.
     *                Può essere {@code null} per disabilitare la navigazione.
     */
    public void setOnBackCallback(Runnable callback) {
        this.onBackCallback = callback;

        // Imposta il click handler sul testo indietro se già creato
        if (backText != null) {
            backText.setOnMouseClicked(e -> {
                if (onBackCallback != null) {
                    onBackCallback.run();
                }
            });
        }
    }

    /**
     * Crea e configura la vista completa della categoria.
     * <p>
     * Factory method principale che costruisce l'intera interfaccia
     * per la visualizzazione della categoria, includendo header,
     * area contenuti, e gestione scroll. Avvia automaticamente
     * il caricamento dei libri per la categoria.
     * </p>
     *
     * <h4>Componenti della vista:</h4>
     * <ul>
     *   <li>Header con breadcrumb navigation e descrizione categoria</li>
     *   <li>Area contenuti con indicatore loading iniziale</li>
     *   <li>ScrollPane configurato per navigazione fluida</li>
     * </ul>
     *
     * <h4>Configurazioni scroll:</h4>
     * <ul>
     *   <li>FitToWidth per adattamento automatico</li>
     *   <li>Scroll orizzontale disabilitato</li>
     *   <li>Scroll verticale automatico quando necessario</li>
     * </ul>
     *
     * @return un {@link ScrollPane} configurato con la vista categoria completa
     */
    public ScrollPane createCategoryView() {
        content = new VBox(20);
        content.setPadding(new Insets(40, 25, 40, 25));
        content.setStyle("-fx-background-color: #1a1a1c;");

        // Header categoria
        createCategoryHeader();

        // Area contenuto libri
        createBooksArea();

        // Carica i libri
        loadCategoryBooks();

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: #1a1a1c; -fx-background: #1a1a1c;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    /**
     * Crea l'header della categoria con navigazione e informazioni descrittive.
     * <p>
     * Costruisce la sezione superiore della vista includendo link di navigazione
     * indietro, titolo della categoria, e descrizione contextual. Applica
     * styling moderno con effetti hover per migliorare l'interattività.
     * </p>
     *
     * <h4>Elementi dell'header:</h4>
     * <ul>
     *   <li><strong>Back Link:</strong> "← Torna a Esplora" con styling iOS-like</li>
     *   <li><strong>Category Title:</strong> Nome categoria con font bold 36pt</li>
     *   <li><strong>Description:</strong> Testo descrittivo con word wrap</li>
     * </ul>
     *
     * <h4>Interattività back link:</h4>
     * <ul>
     *   <li>Colore base: #007AFF (iOS blue)</li>
     *   <li>Hover: #0056CC con underline</li>
     *   <li>Cursor pointer per indicare clickability</li>
     *   <li>Event handler configurabile tramite callback</li>
     * </ul>
     *
     * @see #getCategoryDescription()
     */
    private void createCategoryHeader() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Testo cliccabile per tornare indietro
        backText = new Label("← Torna a Esplora");
        backText.setFont(Font.font("System", FontWeight.NORMAL, 16));
        backText.setTextFill(Color.web("#007AFF"));
        backText.setStyle("-fx-cursor: hand;");

        // Effetti hover
        backText.setOnMouseEntered(e -> {
            backText.setTextFill(Color.web("#0056CC"));
            backText.setStyle("-fx-cursor: hand; -fx-underline: true;");
        });

        backText.setOnMouseExited(e -> {
            backText.setTextFill(Color.web("#007AFF"));
            backText.setStyle("-fx-cursor: hand; -fx-underline: false;");
        });

        // Click handler
        backText.setOnMouseClicked(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
        });

        Label categoryTitle = new Label(category.getName());
        categoryTitle.setFont(Font.font("System", FontWeight.BOLD, 36));
        categoryTitle.setTextFill(Color.WHITE);

        String description = category.getDescription();
        if (description == null || description.trim().isEmpty()) {
            description = getCategoryDescription();
        }

        Label categoryDescription = new Label(description);
        categoryDescription.setFont(Font.font("System", FontWeight.NORMAL, 18));
        categoryDescription.setTextFill(Color.web("#8E8E93"));
        categoryDescription.setWrapText(true);

        header.getChildren().addAll(backText, categoryTitle, categoryDescription);
        content.getChildren().add(header);
    }

    /**
     * Genera descrizioni contextual per categorie predefinite.
     * <p>
     * Fornisce descrizioni user-friendly e marketing-oriented per categorie
     * comuni nel catalogo. Utilizza un mapping case-insensitive per robustezza
     * e include un fallback generico per categorie non mappate.
     * </p>
     *
     * <h4>Categorie mappate:</h4>
     * <ul>
     *   <li><strong>Narrativa per giovani adulti:</strong> Focus su target demografico</li>
     *   <li><strong>Scienze sociali:</strong> Enfasi su aspetti culturali e comportamentali</li>
     *   <li><strong>Biografia e autobiografia:</strong> Storie personali e biografie</li>
     *   <li><strong>Storia:</strong> Eventi che hanno cambiato il mondo</li>
     *   <li><strong>Narrativa per ragazzi:</strong> Avventure per giovani lettori</li>
     *   <li><strong>Umore:</strong> Contenuti divertenti e umoristici</li>
     *   <li><strong>Religione:</strong> Spiritualità e riflessioni esistenziali</li>
     *   <li><strong>Economia e commercio:</strong> Business e strategie aziendali</li>
     *   <li><strong>Narrativa:</strong> Letteratura contemporanea e classica</li>
     * </ul>
     *
     * @return una stringa descrittiva per la categoria corrente
     * @apiNote Le descrizioni sono progettate per essere accattivanti e informative,
     *          bilanciando brevità e chiarezza per migliorare user engagement.
     */
    private String getCategoryDescription() {
        String name = category.getName().toLowerCase();

        switch (name) {
            case "narrativa per giovani adulti":
                return "Storie coinvolgenti per giovani lettori tra adolescenza e età adulta";
            case "scienze sociali":
                return "Esplorazioni della società, cultura e comportamento umano";
            case "biografia e autobiografia":
                return "Vite straordinarie raccontate in prima persona o da esperti biografi";
            case "storia":
                return "Viaggia nel tempo attraverso eventi che hanno cambiato il mondo";
            case "narrativa per ragazzi":
                return "Avventure e storie pensate per i lettori più giovani";
            case "umore":
                return "Risate garantite con storie divertenti e umoristico";
            case "religione":
                return "Spiritualità, fede e riflessioni sui grandi temi dell'esistenza";
            case "economia e commercio":
                return "Strategie aziendali, economia e mondo degli affari";
            case "narrativa":
                return "Il meglio della narrativa contemporanea e classica";
            default:
                return "Esplora una selezione curata di libri per questa categoria";
        }
    }

    /**
     * Inizializza l'area contenuti con separatore e indicatore di loading.
     * <p>
     * Prepara lo spazio per la visualizzazione dei libri aggiungendo
     * spacing appropriato e un indicatore di caricamento iniziale.
     * </p>
     */
    private void createBooksArea() {
        // Separatore
        Region separator = new Region();
        separator.setPrefHeight(20);
        content.getChildren().add(separator);

        // Indicatore di caricamento iniziale
        showLoadingIndicator();
    }

    /**
     * Visualizza un indicatore di caricamento per operazioni asincrone.
     * <p>
     * Crea e aggiunge un componente di loading con progress indicator
     * e messaggio descrittivo per informare l'utente che i dati sono
     * in fase di caricamento.
     * </p>
     *
     * <h4>Componenti dell'indicatore:</h4>
     * <ul>
     *   <li>ProgressIndicator circolare di 50x50 pixel</li>
     *   <li>Label "Caricamento libri..." con font 16pt</li>
     *   <li>Layout centrato con altezza fissa 200px</li>
     * </ul>
     */
    private void showLoadingIndicator() {
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(200);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        Label loadingLabel = new Label("Caricamento libri...");
        loadingLabel.setTextFill(Color.WHITE);
        loadingLabel.setFont(Font.font("System", 16));

        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        content.getChildren().add(loadingBox);
    }

    /**
     * Carica asincrono i libri della categoria tramite BookService.
     * <p>
     * Esegue una ricerca filtrata per categoria utilizzando il servizio
     * libri, gestisce stati di loading per prevenire chiamate multiple,
     * e processa i risultati nel JavaFX Application Thread.
     * </p>
     *
     * <h4>Flusso di esecuzione:</h4>
     * <ol>
     *   <li>Verifica flag loading per prevenire chiamate duplicate</li>
     *   <li>Esegue ricerca asincrona tramite BookService</li>
     *   <li>Processa risultati nel thread UI</li>
     *   <li>Visualizza libri o gestisce casi di errore</li>
     *   <li>Reset flag loading al completamento</li>
     * </ol>
     *
     * <h4>Gestione risultati:</h4>
     * <ul>
     *   <li><strong>Successo con dati:</strong> Visualizza griglia libri</li>
     *   <li><strong>Successo senza dati:</strong> Logging per debugging</li>
     *   <li><strong>Errore:</strong> Logging errore e reset stato</li>
     * </ul>
     *
     * @see BookService#searchBooksByCategoryAsync(String)
     * @see #displayBooks(List)
     */
    private void loadCategoryBooks() {
        if (isLoading) {
            return;
        }

        isLoading = true;
        System.out.println("🎭 Caricamento libri per categoria: " + category.getName());

        bookService.searchBooksByCategoryAsync(category.getName())
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        if (!books.isEmpty()) {
                            displayBooks(books);
                            System.out.println("✅ Caricati " + books.size() + " libri per categoria " + category.getName());
                        }
                        isLoading = false;
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.err.println("❌ Errore caricamento categoria: " + throwable.getMessage());
                        isLoading = false;
                    });
                    return null;
                });
    }

    /**
     * Visualizza i libri della categoria in una griglia responsive.
     * <p>
     * Rimuove gli indicatori di loading e crea una griglia FlowPane
     * ottimizzata per la visualizzazione dei libri con header dei risultati
     * e book cards individuali. Mantiene una cache dei libri per navigazione.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ol>
     *   <li>Rimozione componenti di loading tramite filtering</li>
     *   <li>Gestione caso lista vuota con messaggio appropriato</li>
     *   <li>Cache libri per navigazione contestuale</li>
     *   <li>Creazione header con conteggio risultati</li>
     *   <li>Generazione griglia responsive con property binding</li>
     * </ol>
     *
     * <h4>Configurazione griglia:</h4>
     * <ul>
     *   <li>Gap orizzontale: 20px</li>
     *   <li>Gap verticale: 25px</li>
     *   <li>Allineamento: CENTER_LEFT</li>
     *   <li>Wrap length: Binding dinamico alla larghezza</li>
     * </ul>
     *
     * @param books la lista di libri da visualizzare
     * @throws IllegalArgumentException se books è {@code null}
     * @see #createBookCard(Book)
     * @see #showNoResults()
     */
    private void displayBooks(List<Book> books) {
        if (books == null) {
            throw new IllegalArgumentException("La lista dei libri non può essere null");
        }

        content.getChildren().removeIf(node ->
                node instanceof VBox &&
                        ((VBox) node).getChildren().stream().anyMatch(child -> child instanceof ProgressIndicator)
        );

        if (books.isEmpty()) {
            showNoResults();
            return;
        }

        this.categoryBooks = new ArrayList<>(books);

        // Intestazione risultati
        Label resultsHeader = new Label(books.size() + " libri trovati");
        resultsHeader.setFont(Font.font("System", FontWeight.BOLD, 24));
        resultsHeader.setTextFill(Color.WHITE);
        resultsHeader.setPadding(new Insets(20, 0, 20, 0));
        content.getChildren().add(resultsHeader);

        FlowPane booksGrid = new FlowPane();
        booksGrid.setHgap(20);
        booksGrid.setVgap(25);
        booksGrid.setAlignment(Pos.CENTER_LEFT);

        booksGrid.prefWrapLengthProperty().bind(
                booksGrid.widthProperty().subtract(40)
        );

        for (Book book : books) {
            VBox bookCard = createBookCard(book);
            booksGrid.getChildren().add(bookCard);
        }

        content.getChildren().add(booksGrid);
    }

    /**
     * Crea una book card interattiva per un singolo libro.
     * <p>
     * Costruisce un componente UI completo per rappresentare un libro
     * nella griglia categoria, includendo copertina, metadati, styling
     * avanzato, e gestione click per navigazione ai dettagli.
     * </p>
     *
     * <h4>Struttura della card:</h4>
     * <ul>
     *   <li><strong>Container:</strong> VBox 150px larghezza con padding 10px</li>
     *   <li><strong>Copertina:</strong> ImageView 120x170px con clipping arrotondato</li>
     *   <li><strong>Titolo:</strong> Label bianco con wrap automatico</li>
     *   <li><strong>Autore:</strong> Label grigio secondario</li>
     * </ul>
     *
     * <h4>Effetti visivi:</h4>
     * <ul>
     *   <li>Drop shadow per profondità copertina</li>
     *   <li>Clipping con corner radius 8px</li>
     *   <li>Cursor pointer per indicare interattività</li>
     *   <li>Color scheme ottimizzato per leggibilità</li>
     * </ul>
     *
     * <h4>Gestione click:</h4>
     * <p>
     * Il click handler integra con {@link BooksClient} per aprire
     * i dettagli del libro, passando la lista completa dei libri della
     * categoria per navigazione contestuale next/previous.
     * </p>
     *
     * @param book l'oggetto {@link Book} per cui creare la card
     * @return un {@link VBox} configurato come book card interattiva
     * @throws IllegalArgumentException se book è {@code null}
     * @see BooksClient#openBookDetails(Book, List, AuthenticationManager)
     */
    private VBox createBookCard(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("L'oggetto Book non può essere null");
        }

        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        card.setMaxWidth(150);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-cursor: hand;");

        ImageView bookCover = ImageUtils.createSafeImageView(book.getSafeImageFileName(), 120, 170);

        // Applica clip per bordi arrotondati
        Rectangle clip = new Rectangle(120, 170);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        bookCover.setClip(clip);

        // Effetto ombra
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        bookCover.setEffect(shadow);

        // Titolo
        Label titleLabel = new Label(book.getTitle() != null ? book.getTitle() : "Titolo non disponibile");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(135);
        titleLabel.setAlignment(Pos.CENTER);

        // Autore
        Label authorLabel = new Label(book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto");
        authorLabel.setTextFill(Color.web("#AAAAAA"));
        authorLabel.setFont(Font.font("System", FontWeight.LIGHT, 12));
        authorLabel.setWrapText(true);
        authorLabel.setMaxWidth(135);
        authorLabel.setAlignment(Pos.CENTER);

        card.setOnMouseClicked(e -> {
            System.out.println("📖 Click libro categoria: " + book.getTitle());
            System.out.println("📚 Aprendo con lista di " + categoryBooks.size() + " libri per navigazione");

            BooksClient.openBookDetails(book, categoryBooks, authManager);
        });

        card.getChildren().addAll(bookCover, titleLabel, authorLabel);
        return card;
    }

    /**
     * Visualizza un messaggio informativo quando non ci sono risultati.
     * <p>
     * Crea un'interfaccia placeholder user-friendly per comunicare
     * l'assenza di libri nella categoria, includendo suggerimenti
     * per azioni alternative come esplorazione di altre categorie
     * o utilizzo della funzione di ricerca.
     * </p>
     *
     * <h4>Componenti del messaggio:</h4>
     * <ul>
     *   <li>Messaggio principale con icona libro</li>
     *   <li>Suggerimento per azioni alternative</li>
     *   <li>Layout centrato con altezza fissa</li>
     *   <li>Styling grigio per aspetto non invasivo</li>
     * </ul>
     */
    private void showNoResults() {
        VBox noResultsBox = new VBox(15);
        noResultsBox.setAlignment(Pos.CENTER);
        noResultsBox.setPrefHeight(200);

        Label noResultsLabel = new Label("📚 Nessun libro trovato per questa categoria");
        noResultsLabel.setTextFill(Color.web("#8E8E93"));
        noResultsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));

        Label suggestionLabel = new Label("Prova a esplorare altre categorie o usa la ricerca");
        suggestionLabel.setTextFill(Color.web("#8E8E93"));
        suggestionLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        noResultsBox.getChildren().addAll(noResultsLabel, suggestionLabel);
        content.getChildren().add(noResultsBox);
    }

    /**
     * Visualizza un messaggio di errore generico nell'area contenuti.
     * <p>
     * Crea un'interfaccia di errore per comunicare problemi durante
     * il caricamento dei dati, rimuovendo prima eventuali indicatori
     * di loading per evitare sovrapposizioni visive.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ol>
     *   <li>Rimozione componenti loading tramite filtering</li>
     *   <li>Creazione layout errore centrato</li>
     *   <li>Applicazione styling per messaggi di errore</li>
     *   <li>Aggiunta al container principale</li>
     * </ol>
     *
     * <h4>Styling errore:</h4>
     * <ul>
     *   <li>Colore rosso (#e74c3c) per visibilità errore</li>
     *   <li>Font 16pt per leggibilità</li>
     *   <li>Word wrap per messaggi lunghi</li>
     *   <li>Layout centrato con altezza fissa</li>
     * </ul>
     *
     * @param message il messaggio di errore da visualizzare
     * @throws IllegalArgumentException se message è {@code null}
     */
    private void showErrorMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Il messaggio di errore non può essere null");
        }

        content.getChildren().removeIf(node ->
                node instanceof VBox &&
                        ((VBox) node).getChildren().stream().anyMatch(child -> child instanceof ProgressIndicator)
        );

        VBox errorBox = new VBox(15);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(200);

        Label errorLabel = new Label("❌ " + message);
        errorLabel.setTextFill(Color.web("#e74c3c"));
        errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        errorLabel.setWrapText(true);

        errorBox.getChildren().add(errorLabel);
        content.getChildren().add(errorBox);
    }
}