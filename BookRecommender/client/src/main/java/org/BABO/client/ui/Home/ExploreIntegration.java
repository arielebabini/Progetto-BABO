package org.BABO.client.ui.Home;

import org.BABO.client.service.ClientRatingService;
import org.BABO.client.ui.BooksClient;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Category.CategoryView;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
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
 * Componente di integrazione per la sezione Esplora dell'applicazione BABO.
 * <p>
 * Questa classe implementa un Books Store per l'esplorazione
 * di contenuti, integrando classifiche dinamiche basate su recensioni e valutazioni
 * con una griglia di categorie per la scoperta di libri. Fornisce un'esperienza
 * utente ricca e coinvolgente per la navigazione e scoperta di contenuti.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Classifiche Dinamiche:</strong> Libri più recensiti e meglio valutati</li>
 *   <li><strong>Navigazione Categorie:</strong> Griglia colorata per esplorazione per genere</li>
 *   <li><strong>Vista Categoria:</strong> Transizioni fluide tra esplorazione e categorie specifiche</li>
 *   <li><strong>Rating Integration:</strong> Visualizzazione stelle e valutazioni numeriche</li>
 *   <li><strong>Responsive Design:</strong> Layout adattivo per diverse risoluzioni</li>
 *   <li><strong>Interactive Elements:</strong> Hover effects e transizioni animate</li>
 * </ul>
 *
 * <h3>Architettura delle Classifiche:</h3>
 * <p>
 * Il sistema di classifiche integra dati da {@link ClientRatingService} per creare
 * liste dinamiche e aggiornate:
 * </p>
 * <ul>
 *   <li><strong>Più Recensiti:</strong> Libri ordinati per numero di recensioni</li>
 *   <li><strong>Migliori Valutazioni:</strong> Libri ordinati per rating medio</li>
 *   <li><strong>Cache Intelligente:</strong> Mantenimento dati per navigazione rapida</li>
 *   <li><strong>Fallback Graceful:</strong> Gestione scenari con dati mancanti</li>
 * </ul>
 *
 * <h3>Sistema di Categorie:</h3>
 * <p>
 * La griglia categorie implementa un design moderno con:
 * </p>
 * <ul>
 *   <li>Palette colori curata per ogni categoria</li>
 *   <li>Layout grid 3x3 responsive</li>
 *   <li>Animazioni micro-interazioni</li>
 *   <li>Integrazione con {@link CategoryView} per drill-down</li>
 * </ul>
 *
 * <h3>Gestione Vista Categoria:</h3>
 * <p>
 * Implementa un sistema di overlay per transizioni fluide:
 * </p>
 * <ul>
 *   <li>Stack-based navigation con overlay system</li>
 *   <li>Breadcrumb navigation integrata</li>
 *   <li>State management per back navigation</li>
 *   <li>Container isolation per performance</li>
 * </ul>
 *
 * <h3>Design Patterns Implementati:</h3>
 * <ul>
 *   <li><strong>Observer Pattern:</strong> Callback per eventi book click</li>
 *   <li><strong>Factory Pattern:</strong> Creazione dinamica category objects</li>
 *   <li><strong>Strategy Pattern:</strong> Diverse strategie per rating visualization</li>
 *   <li><strong>State Pattern:</strong> Gestione stati vista (explore vs category)</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Inizializzazione componente
 * BookService bookService = new BookService();
 * ExploreIntegration exploreIntegration = new ExploreIntegration(bookService, true);
 *
 * // Configurazione container principale
 * StackPane mainContainer = new StackPane();
 * exploreIntegration.setContainer(mainContainer);
 *
 * // Configurazione gestori eventi
 * exploreIntegration.setBookClickHandler(book -> {
 *     System.out.println("Libro selezionato: " + book.getTitle());
 *     showBookDetails(book);
 * });
 *
 * // Configurazione autenticazione
 * AuthenticationManager authManager = new AuthenticationManager();
 * exploreIntegration.setAuthManager(authManager);
 *
 * // Creazione vista esplora
 * ScrollPane exploreView = exploreIntegration.createExploreView();
 *
 * // Integrazione in layout principale
 * VBox mainLayout = new VBox();
 * mainLayout.getChildren().add(exploreView);
 *
 * // Il componente gestisce automaticamente:
 * // - Caricamento classifiche da rating service
 * // - Click handling per categorie
 * // - Transizioni tra vista esplora e categorie
 * // - Back navigation con state management
 * }</pre>
 *
 * <h3>Integrazione con Rating System:</h3>
 * <p>
 * Il componente si integra strettamente con il sistema di rating per:
 * </p>
 * <ul>
 *   <li>Caricamento asincrono dati rating</li>
 *   <li>Visualizzazione stelle e rating numerici</li>
 *   <li>Fallback per libri senza rating</li>
 *   <li>Cache per performance ottimizzate</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <ul>
 *   <li>Lazy loading delle categorie e classifiche</li>
 *   <li>Image caching tramite ImageUtils integration</li>
 *   <li>Memory management per liste grandi</li>
 *   <li>Async operations per non bloccare UI thread</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see BookService
 * @see ClientRatingService
 * @see CategoryView
 * @see AuthenticationManager
 */
public class ExploreIntegration {

    /** Servizio per operazioni sui libri */
    private final BookService bookService;

    /** Servizio per gestione rating e recensioni */
    private final ClientRatingService ratingService;

    /** Callback per gestire click sui libri */
    private Consumer<Book> bookClickHandler;

    /** Container principale per overlay di categoria */
    private StackPane containerPane;

    /** Flag che indica se si sta visualizzando una categoria */
    private boolean isViewingCategory = false;

    /** Cache libri più recensiti per navigazione */
    private List<Book> mostReviewedBooks = new ArrayList<>();

    /** Cache libri meglio valutati per navigazione */
    private List<Book> topRatedBooks = new ArrayList<>();

    /** Manager di autenticazione per operazioni protette */
    private AuthenticationManager authManager;

    /**
     * Costruttore del componente di integrazione Esplora.
     * <p>
     * Inizializza i servizi necessari per il funzionamento del componente.
     * Il flag serverAvailable è mantenuto per compatibilità ma non utilizzato
     * nella versione corrente.
     * </p>
     *
     * @param bookService servizio per operazioni sui libri
     * @param serverAvailable flag disponibilità server (legacy)
     * @throws IllegalArgumentException se bookService è {@code null}
     */
    public ExploreIntegration(BookService bookService, boolean serverAvailable) {
        if (bookService == null) {
            throw new IllegalArgumentException("BookService non può essere null");
        }

        this.bookService = bookService;
        this.ratingService = new ClientRatingService();
    }

    /**
     * Configura il container principale per gestione overlay categoria.
     * <p>
     * Il container viene utilizzato per implementare il sistema di navigazione
     * stack-based che permette transizioni fluide tra vista esplora e
     * visualizzazione categorie specifiche.
     * </p>
     *
     * @param container lo StackPane principale per overlay
     */
    public void setContainer(StackPane container) {
        this.containerPane = container;
    }

    /**
     * Configura il gestore per click sui libri.
     * <p>
     * Il callback viene utilizzato per gestire le interazioni utente sui libri
     * nelle classifiche e nelle categorie, delegando la logica di visualizzazione
     * dettagli al componente padre.
     * </p>
     *
     * @param handler callback per gestire click sui libri
     */
    public void setBookClickHandler(Consumer<Book> handler) {
        this.bookClickHandler = handler;
    }

    /**
     * Configura il manager di autenticazione.
     * <p>
     * Necessario per operazioni che richiedono autenticazione utente,
     * come aggiunta ai favoriti o accesso a contenuti premium.
     * </p>
     *
     * @param authManager manager di autenticazione
     */
    public void setAuthManager(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Crea la vista Esplora completa con classifiche e categorie.
     * <p>
     * Factory method principale che costruisce l'intera esperienza di esplorazione,
     * includendo sezioni classifiche dinamiche e griglia categorie interattiva.
     * La vista è ottimizzata per scroll fluido e layout responsive.
     * </p>
     *
     * <h4>Struttura della vista:</h4>
     * <ol>
     *   <li><strong>Header Classifiche:</strong> Titolo e introduzione</li>
     *   <li><strong>Sezioni Classifiche:</strong> Più recensiti e meglio valutati</li>
     *   <li><strong>Header Generi:</strong> Titolo sezione categorie</li>
     *   <li><strong>Griglia Categorie:</strong> Layout 3x3 interattivo</li>
     *   <li><strong>Padding Finale:</strong> Spacing per scroll completo</li>
     * </ol>
     *
     * <h4>Configurazioni scroll:</h4>
     * <ul>
     *   <li>Fit to width per responsività</li>
     *   <li>Vertical scroll only per UX ottimale</li>
     *   <li>Smooth scrolling per transizioni fluide</li>
     * </ul>
     *
     * @return {@link ScrollPane} configurato con vista esplora completa
     */
    public ScrollPane createExploreView() {
        System.out.println("🔍 DEBUG: createExploreView() chiamato!");
        isViewingCategory = false;

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #1a1a1c;");

        // ScrollPane per l'intero contenuto
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: #1a1a1c; -fx-background: #1a1a1c;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox scrollContent = new VBox(0);
        scrollContent.setStyle("-fx-background-color: #1a1a1c;");

        // HEADER CLASSIFICHE
        Label classificheTitle = new Label("Classifiche");
        classificheTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        classificheTitle.setTextFill(Color.WHITE);
        classificheTitle.setPadding(new Insets(40, 25, 20, 25));

        scrollContent.getChildren().add(classificheTitle);

        // SEZIONI CLASSIFICHE
        createClassificheSection(scrollContent);

        // HEADER SCOPRI PER GENERE
        Label genereTitle = new Label("Scopri per genere");
        genereTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        genereTitle.setTextFill(Color.WHITE);
        genereTitle.setPadding(new Insets(50, 25, 20, 25));

        scrollContent.getChildren().add(genereTitle);

        // GRIGLIA CATEGORIE
        createCategoriesGrid(scrollContent);

        // Padding finale
        Region finalPadding = new Region();
        finalPadding.setPrefHeight(40);
        scrollContent.getChildren().add(finalPadding);

        scrollPane.setContent(scrollContent);
        return scrollPane;
    }

    /**
     * Crea le sezioni delle classifiche con caricamento asincrono.
     * <p>
     * Costruisce le sezioni per libri più recensiti e meglio valutati,
     * gestendo il caricamento asincrono dei dati tramite ClientRatingService
     * e implementando fallback appropriati per scenari di errore.
     * </p>
     *
     * <h4>Sezioni create:</h4>
     * <ul>
     *   <li><strong>Più Recensiti:</strong> Top 10 libri per numero recensioni</li>
     *   <li><strong>Migliori Valutazioni:</strong> Top 10 libri per rating medio</li>
     * </ul>
     *
     * <h4>Gestione asincrona:</h4>
     * <p>
     * Ogni sezione viene inizializzata con placeholder e popolata
     * asincrono quando i dati sono disponibili, garantendo UI responsiva.
     * </p>
     *
     * @param parent container VBox dove aggiungere le sezioni
     */
    private void createClassificheSection(VBox parent) {
        // PIÙ RECENSITI
        VBox mostReviewedSection = createChartSection(
                "📊 Più recensiti",
                "I libri con più recensioni dei lettori"
        );

        System.out.println("🔍 DEBUG: Caricamento libri più recensiti...");
        ratingService.getTopRatedBooksAsync()
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        System.out.println("🔍 DEBUG: Ricevuti " + books.size() + " libri più recensiti dal rating service");
                        if (!books.isEmpty()) {
                            this.mostReviewedBooks = new ArrayList<>(books);

                            populateBookSection(mostReviewedSection, books.subList(0, Math.min(10, books.size())), "mostReviewed");
                        } else {
                            showErrorInSection(mostReviewedSection, "Nessun libro recensito disponibile");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ DEBUG: Errore rating service più recensiti: " + throwable.getMessage());
                        showErrorInSection(mostReviewedSection, "Errore caricamento libri più recensiti");
                    });
                    return null;
                });

        parent.getChildren().add(mostReviewedSection);

        // MEGLIO VALUTATI
        VBox topRatedSection = createChartSection(
                "⭐ Migliori valutazioni",
                "I libri con le valutazioni più alte"
        );

        System.out.println("🔍 DEBUG: Caricamento libri meglio valutati...");
        ratingService.getBestRatedBooksAsync()
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        System.out.println("🔍 DEBUG: Ricevuti " + books.size() + " libri meglio valutati dal rating service");
                        if (!books.isEmpty()) {
                            this.topRatedBooks = new ArrayList<>(books);

                            populateBookSection(topRatedSection, books.subList(0, Math.min(10, books.size())), "topRated");
                        } else {
                            showErrorInSection(topRatedSection, "Nessun libro ben valutato disponibile");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ DEBUG: Errore rating service migliori valutazioni: " + throwable.getMessage());
                        showErrorInSection(topRatedSection, "Errore caricamento libri meglio valutati");
                    });
                    return null;
                });

        parent.getChildren().add(topRatedSection);
    }

    /**
     * Crea una sezione classifiche con header e container per libri.
     * <p>
     * Factory method per creare la struttura base di una sezione classifica
     * con titolo, sottotitolo e container per i libri che verrà popolato
     * asincrono quando i dati sono disponibili.
     * </p>
     *
     * @param title titolo principale della sezione
     * @param subtitle descrizione della sezione
     * @return VBox configurato per la sezione classifica
     */
    private VBox createChartSection(String title, String subtitle) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 25, 30, 25));

        // Header sezione
        VBox header = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web("#8E8E93"));

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Container per i libri (sarà popolato async)
        HBox booksContainer = new HBox(15);
        booksContainer.setAlignment(Pos.CENTER_LEFT);

        section.getChildren().addAll(header, booksContainer);
        return section;
    }

    /**
     * Popola una sezione classifiche con i libri forniti.
     * <p>
     * Trova il container dei libri nella sezione e lo popola con le book card
     * generate dai dati forniti. Limita la visualizzazione a 8 libri per
     * ottimizzare l'esperienza scroll orizzontale.
     * </p>
     *
     * @param section la sezione VBox da popolare
     * @param books lista di libri da visualizzare
     * @param sectionType tipo di sezione per identificazione click context
     */
    private void populateBookSection(VBox section, List<Book> books, String sectionType) {
        // Trova il container dei libri (ultimo figlio della sezione)
        if (section.getChildren().size() >= 2 && section.getChildren().get(1) instanceof HBox) {
            HBox booksContainer = (HBox) section.getChildren().get(1);
            booksContainer.getChildren().clear();

            for (int i = 0; i < Math.min(books.size(), 8); i++) {
                Book book = books.get(i);
                VBox bookCard = createBookCard(book, sectionType);
                booksContainer.getChildren().add(bookCard);
            }
        }
    }

    /**
     * Visualizza messaggio di errore in una sezione classifiche.
     * <p>
     * Utility method per gestire scenari di errore nelle sezioni,
     * sostituendo il contenuto con un messaggio di errore appropriato.
     * </p>
     *
     * @param section sezione dove mostrare l'errore
     * @param errorMessage messaggio di errore da visualizzare
     */
    private void showErrorInSection(VBox section, String errorMessage) {
        if (section.getChildren().size() >= 2 && section.getChildren().get(1) instanceof HBox) {
            HBox booksContainer = (HBox) section.getChildren().get(1);
            booksContainer.getChildren().clear();

            Label errorLabel = new Label("❌ " + errorMessage);
            errorLabel.setTextFill(Color.web("#e74c3c"));
            errorLabel.setFont(Font.font("System", 14));
            booksContainer.getChildren().add(errorLabel);
        }
    }

    /**
     * Crea una book card per le classifiche con rating e interattività.
     * <p>
     * Costruisce un componente UI completo per rappresentare un libro nelle
     * classifiche, includendo copertina, metadati, rating visuale, e gestione
     * click con navigazione contestuale. Include hover effects per migliore UX.
     * </p>
     *
     * <h4>Struttura della card:</h4>
     * <ul>
     *   <li>Copertina 90x130px con clipping arrotondato</li>
     *   <li>Titolo in bold con text wrapping</li>
     *   <li>Autore in grigio secondario</li>
     *   <li>Rating box con stelle e valore numerico (se disponibile)</li>
     * </ul>
     *
     * <h4>Features interattive:</h4>
     * <ul>
     *   <li>Click handler con navigazione contestuale</li>
     *   <li>Hover effects con scale animation</li>
     *   <li>Cursor pointer per indicare clickability</li>
     * </ul>
     *
     * @param book il libro da rappresentare
     * @param sectionType tipo di sezione per determinare lista di navigazione
     * @return VBox configurato come book card interattiva
     */
    private VBox createBookCard(Book book, String sectionType) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(120);
        card.setMaxWidth(120);

        // Copertina libro
        ImageView bookCover = ImageUtils.createSafeImageView(book.getSafeImageFileName(), 90, 130);

        // Clip arrotondato
        Rectangle clip = new Rectangle(90, 130);
        clip.setArcWidth(6);
        clip.setArcHeight(6);
        bookCover.setClip(clip);

        // Effetto ombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setOffsetY(4);
        shadow.setRadius(8);
        bookCover.setEffect(shadow);

        // Titolo
        Label title = new Label(book.getTitle() != null ? book.getTitle() : "Titolo non disponibile");
        title.setFont(Font.font("System", FontWeight.BOLD, 11));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        title.setMaxWidth(110);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Autore
        Label author = new Label(book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto");
        author.setFont(Font.font("System", FontWeight.NORMAL, 9));
        author.setTextFill(Color.web("#8E8E93"));
        author.setWrapText(true);
        author.setMaxWidth(110);
        author.setAlignment(Pos.CENTER);
        author.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Rating box (se disponibili)
        VBox ratingBox = new VBox(2);
        ratingBox.setAlignment(Pos.CENTER);

        // Controllo per il rating
        Double rating = book.getAverageRating();
        if (rating != null && rating > 0.0) {
            // Stelle
            Label starsLabel = new Label(createStarString(book.getAverageRating()));
            starsLabel.setFont(Font.font("System", 10));
            starsLabel.getStyleClass().add("stars-white");

            starsLabel.setTextFill(Color.WHITE);

            // Rating numerico
            Label ratingText = new Label(String.format("%.1f", book.getAverageRating()));
            ratingText.setFont(Font.font("System", 8));
            ratingText.setTextFill(Color.web("#8E8E93"));

            ratingBox.getChildren().addAll(starsLabel, ratingText);
        } else if (book.getReviewCount() > 0) {
            // Se ha almeno delle recensioni, mostra il numero
            Label reviewCount = new Label(book.getReviewCount() + " recensioni");
            reviewCount.setFont(Font.font("System", 9));
            reviewCount.setTextFill(Color.web("#8E8E93"));
            ratingBox.getChildren().add(reviewCount);
        }

        // Aggiungi tutti gli elementi alla card
        if (ratingBox.getChildren().isEmpty()) {
            card.getChildren().addAll(bookCover, title, author);
        } else {
            card.getChildren().addAll(bookCover, title, author, ratingBox);
        }

        // Passa lista completa della sezione
        card.setOnMouseClicked(e -> {
            System.out.println("📖 Click libro sezione " + sectionType + ": " + book.getTitle());

            // Determina quale lista usare in base al tipo di sezione
            List<Book> sectionBooks;
            switch (sectionType) {
                case "mostReviewed":
                    sectionBooks = mostReviewedBooks;
                    System.out.println("📚 Usando lista 'Più recensiti' con " + mostReviewedBooks.size() + " libri");
                    break;
                case "topRated":
                    sectionBooks = topRatedBooks;
                    System.out.println("📚 Usando lista 'Migliori valutazioni' con " + topRatedBooks.size() + " libri");
                    break;
                default:
                    sectionBooks = List.of(book); // Fallback
                    System.out.println("⚠️ Tipo sezione sconosciuto: " + sectionType);
                    break;
            }

            BooksClient.openBookDetails(book, sectionBooks, authManager);
        });

        // Effetti hover
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);

            ratingBox.getChildren().forEach(child -> {
                if (child instanceof Label && ((Label) child).getStyleClass().contains("stars-white")) {
                    ((Label) child).setTextFill(Color.WHITE);
                }
            });
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);

            ratingBox.getChildren().forEach(child -> {
                if (child instanceof Label && ((Label) child).getStyleClass().contains("stars-white")) {
                    ((Label) child).setTextFill(Color.WHITE);
                }
            });
        });

        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    /**
     * Genera stringa di stelle per visualizzazione rating.
     * <p>
     * Converte un rating numerico in rappresentazione visuale con stelle
     * piene (★) e vuote (☆), supportando mezze stelle per rating decimali.
     * </p>
     *
     * <h4>Logica di conversione:</h4>
     * <ul>
     *   <li>Parte intera: stelle piene</li>
     *   <li>Decimale >= 0.5: mezza stella aggiuntiva</li>
     *   <li>Rimanenti: stelle vuote fino a 5 totali</li>
     * </ul>
     *
     * @param rating valore numerico del rating (0.0-5.0)
     * @return stringa con rappresentazione stelle
     */
    private String createStarString(double rating) {
        int fullStars = (int) rating;
        boolean halfStar = (rating - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();

        // Stelle piene
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }

        // Stella mezza se necessaria
        if (halfStar && fullStars < 5) {
            stars.append("☆");
            fullStars++;
        }

        // Stelle vuote rimanenti
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }

        return stars.toString();
    }

    /**
     * Crea la griglia delle categorie con design moderno e colorato.
     * <p>
     * Costruisce una griglia 3x3 di pulsanti categoria con palette colori
     * curata e categorie predefinite. Ogni pulsante include hover effects
     * e gestione click per navigazione categoria.
     * </p>
     *
     * <h4>Categorie predefinite:</h4>
     * <ul>
     *   <li>Narrativa per giovani adulti</li>
     *   <li>Scienze sociali</li>
     *   <li>Biografia e autobiografia</li>
     *   <li>Storia</li>
     *   <li>Narrativa per ragazzi</li>
     *   <li>Umore</li>
     *   <li>Religione</li>
     *   <li>Economia e Commercio</li>
     *   <li>Narrativa</li>
     * </ul>
     *
     * @param parent container VBox dove aggiungere la griglia
     */
    private void createCategoriesGrid(VBox parent) {
        GridPane categoriesGrid = new GridPane();
        categoriesGrid.setPadding(new Insets(0, 25, 0, 25));
        categoriesGrid.setHgap(15);
        categoriesGrid.setVgap(15);

        // Categorie predefinite
        String[] categories = {
                "Narrativa per giovani adulti",
                "Scienze sociali",
                "Biografia e autobiografia",
                "Storia",
                "Narrativa per ragazzi",
                "Umore",
                "Religione",
                "Economia e Commercio",
                "Narrativa"
        };

        String[] colors = {
                "#E9B29B",
                "#B5BA8C",
                "#52557A",
                "#8F5D5D",
                "#9AAAB4",
                "#F0C57F",
                "#5F797B",
                "#E0875E",
                "#81B29A"
        };

        int row = 0, col = 0;
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            String color = colors[i % colors.length];

            Button categoryButton = createCategoryButton(category, color);
            categoriesGrid.add(categoryButton, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        parent.getChildren().add(categoriesGrid);
    }

    /**
     * Crea un pulsante categoria con design personalizzato e interattività.
     * <p>
     * Genera un pulsante con styling custom, colore di background specifico,
     * hover effects, e gestione click per navigazione categoria. Il testo
     * viene formattato per multi-line quando necessario.
     * </p>
     *
     * @param category nome della categoria
     * @param color codice colore hex per il background
     * @return Button configurato per la categoria
     */
    private Button createCategoryButton(String category, String color) {
        Button button = new Button(category.replace(" ", "\n"));
        button.setPrefSize(200, 100);
        button.setFont(Font.font("System", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);
        button.setWrapText(true);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);" +
                        "-fx-text-alignment: center;" +
                        "-fx-line-spacing: -5px;" // ← AGGIUNTA: riduce spazio tra righe (valore negativo)
        );

        // Effetti hover
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });

        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        // Click handler
        button.setOnAction(e -> {
            handleCategoryClick(category);
        });

        return button;
    }

    /**
     * Gestisce il click su una categoria avviando navigazione specifica.
     * <p>
     * Crea e visualizza una CategoryView per la categoria selezionata,
     * implementando transizione fluida tramite overlay system.
     * </p>
     *
     * @param category nome della categoria selezionata
     */
    private void handleCategoryClick(String category) {
        System.out.println("🎭 Click categoria: " + category);

        if (containerPane != null) {
            // Crea vista categoria
            CategoryView categoryView = new CategoryView(
                    createCategoryFromString(category),
                    bookService,
                    bookClickHandler
            );

            categoryView.setAuthManager(authManager);

            // Mostra la vista categoria
            showCategoryView(categoryView);
        }
    }

    /**
     * Crea un oggetto Category da stringa nome categoria.
     * <p>
     * Factory method per convertire nomi stringa in oggetti Category
     * validi, rimuovendo caratteri non alfabetici e mantenendo
     * formato pulito.
     * </p>
     *
     * @param categoryString nome categoria come stringa
     * @return oggetto Category configurato
     */
    private Category createCategoryFromString(String categoryString) {
        // Mantieni il formato originale, rimuovi solo eventuali emoji
        String cleanName = categoryString.replaceAll("[^\\p{L}\\p{N}\\s&]", "").trim();


        return new Category(cleanName, "", "");
    }

    /**
     * Visualizza la vista categoria tramite overlay system.
     * <p>
     * Implementa transizione fluida creando overlay StackPane che
     * sostituisce temporaneamente la vista esplora, con sistema
     * di back navigation integrato.
     * </p>
     *
     * @param categoryView vista categoria da visualizzare
     */
    private void showCategoryView(CategoryView categoryView) {
        if (containerPane == null) {
            System.err.println("❌ Container non impostato per mostrare categoria");
            return;
        }

        try {
            isViewingCategory = true;

            // Imposta il callback per tornare indietro nel testo integrato
            categoryView.setOnBackCallback(() -> closeCategoryView());

            // Crea overlay per la categoria
            StackPane categoryOverlay = new StackPane();
            categoryOverlay.setStyle("-fx-background-color: #1a1a1c;");

            // Crea contenuto categoria
            ScrollPane categoryContent = categoryView.createCategoryView();
            categoryOverlay.getChildren().add(categoryContent);

            // Aggiungi al container
            containerPane.getChildren().add(categoryOverlay);

        } catch (Exception e) {
            System.err.println("❌ Errore visualizzazione categoria: " + e.getMessage());
        }
    }

    /**
     * Chiude la vista categoria e ripristina vista esplora.
     * <p>
     * Rimuove l'overlay categoria dal container principale,
     * ripristinando la vista esplora sottostante e aggiornando
     * lo stato di navigazione.
     * </p>
     */
    private void closeCategoryView() {
        if (containerPane != null && isViewingCategory) {
            // Rimuovi l'overlay della categoria
            containerPane.getChildren().removeIf(node ->
                    node instanceof StackPane &&
                            !((StackPane) node).getChildren().isEmpty() &&
                            ((StackPane) node).getChildren().get(0) instanceof ScrollPane
            );
            isViewingCategory = false;
        }
    }
}