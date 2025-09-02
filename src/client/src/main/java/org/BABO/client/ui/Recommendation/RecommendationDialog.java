package org.BABO.client.ui.Recommendation;

import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.BookRecommendation;
import org.BABO.shared.dto.Recommendation.RecommendationRequest;
import org.BABO.shared.dto.Recommendation.RecommendationResponse;
import org.BABO.client.service.ClientRecommendationService;
import org.BABO.client.service.LibraryService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Dialog per consigliare libri ad altri utenti
 * Permette di selezionare fino a 3 libri dalle proprie librerie da raccomandare per un libro target.
 * <p>
 * Questo dialog fornisce un'interfaccia utente interattiva per permettere all'utente di
 * visualizzare le proprie librerie, selezionare libri da esse e inviare raccomandazioni
 * per un libro specifico. Tutte le operazioni di rete sono gestite in modo asincrono
 * per mantenere la reattività dell'interfaccia utente.
 * </p>
 *
 * <h3>Caratteristiche principali:</h3>
 * <ul>
 * <li><strong>Selezione Libri:</strong> L'utente può scegliere fino a un numero predefinito
 * di libri (attualmente 3) dalle proprie librerie da raccomandare per un libro target.</li>
 * <li><strong>Visualizzazione in tempo reale:</strong> La lista dei libri selezionati viene
 * aggiornata dinamicamente.</li>
 * <li><strong>Stato e Feedback:</strong> Vengono forniti messaggi di stato, indicatori di
 * caricamento e alert per informare l'utente sull'andamento delle operazioni.</li>
 * <li><strong>Gestione Asincrona:</strong> L'interazione con i servizi di rete per caricare
 * librerie e inviare raccomandazioni è gestita con {@link CompletableFuture}.</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see org.BABO.client.service.ClientRecommendationService
 * @see org.BABO.client.service.LibraryService
 */
public class RecommendationDialog {

    // Variabili principali
    private Stage dialogStage;
    private Book targetBook;
    private String username;
    private Consumer<List<BookRecommendation>> onRecommendationsSaved;

    // Servizi
    private ClientRecommendationService recommendationService;
    private LibraryService libraryService;

    // UI Components
    private VBox librariesContainer;
    private VBox selectedBooksContainer;
    private Button saveButton;
    private Button cancelButton;
    private Label statusLabel;
    private Label remainingSlotsLabel;

    // Dati
    private List<Book> selectedBooks;
    private List<String> userLibraries;
    private List<BookRecommendation> existingRecommendations;
    private int maxRecommendations = 3;
    private int currentRecommendationsCount = 0;

    /**
     * Costruttore per {@link RecommendationDialog}.
     * <p>
     * Inizializza il dialog con il libro target, l'utente corrente e i servizi
     * necessari. Avvia il caricamento dei dati dell'utente per la raccomandazione.
     * </p>
     *
     * @param targetBook Il libro per cui si stanno creando le raccomandazioni.
     * @param username L'username dell'utente che sta creando le raccomandazioni.
     * @param authManager Il gestore di autenticazione, non usato direttamente ma passato per compatibilità.
     * @param onRecommendationsSaved Un {@link Consumer} che viene chiamato con la lista
     * di {@link BookRecommendation} aggiornata dopo il salvataggio.
     */
    public RecommendationDialog(Book targetBook, String username, AuthenticationManager authManager,
                                Consumer<List<BookRecommendation>> onRecommendationsSaved) {
        this.targetBook = targetBook;
        this.username = username;
        this.onRecommendationsSaved = onRecommendationsSaved;
        this.selectedBooks = new ArrayList<>();
        this.recommendationService = new ClientRecommendationService();
        this.libraryService = new LibraryService();

        initializeDialog();
        createUI();
        loadUserData();
    }

    /**
     * Inizializza le proprietà di base del dialog {@link Stage}.
     * <p>
     * Questo metodo configura il {@link Stage} del dialog impostando lo stile
     * su {@link javafx.stage.StageStyle#TRANSPARENT}, la modalità su
     * {@link javafx.stage.Modality#APPLICATION_MODAL} per bloccare l'interazione
     * con le altre finestre dell'applicazione, imposta il titolo e disabilita
     * il ridimensionamento della finestra.
     * </p>
     */
    private void initializeDialog() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Consiglia Libri");
        dialogStage.setResizable(false);
    }

    /**
     * Crea e configura l'intera interfaccia utente del dialog.
     * <p>
     * Questo metodo costruisce il layout del dialog a partire da un {@link StackPane}
     * di base trasparente. Aggiunge il contenuto principale del dialog, gestisce
     * la chiusura della finestra quando l'utente clicca fuori dal contenuto (utilizzando
     * un controllo sui target degli eventi del mouse) e imposta la {@link Scene}
     * con le dimensioni e il riempimento desiderati per il dialog.
     * </p>
     */
    private void createUI() {
        // Container principale SENZA sfondo scuro
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        // Dialog content
        VBox dialogContent = createDialogContent();
        root.getChildren().add(dialogContent);

        // Permetti ai controlli figli di ricevere eventi mouse
        root.setPickOnBounds(false);

        // Chiudi dialog cliccando fuori
        root.setOnMouseClicked(e -> {
            // Chiudi solo se il click è DAVVERO sullo sfondo
            if (e.getTarget() == root && e.getSource() == root) {
                closeDialog();
            }
        });

        Scene scene = new Scene(root, 1000, 800);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
    }

    /**
     * Crea il contenuto principale del dialog, inclusi header, sezioni e pulsanti.
     * @return Un {@link VBox} che contiene il layout completo del dialog.
     */
    private VBox createDialogContent() {
        VBox content = new VBox(20);
        content.setMaxWidth(900);
        content.setMaxHeight(750);
        content.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 10);"
        );
        content.setAlignment(Pos.TOP_CENTER);

        // Header
        VBox header = createHeader();

        // Status e info
        VBox statusSection = createStatusSection();

        // Contenuto principale in scroll
        ScrollPane mainScroll = createMainContent();

        // Sezione libri selezionati
        VBox selectedSection = createSelectedBooksSection();

        // Bottoni
        HBox buttons = createButtonSection();

        content.getChildren().addAll(header, statusSection, mainScroll, selectedSection, buttons);
        return content;
    }

    /**
     * Crea l'header del dialog, che include il titolo e le informazioni sul libro target.
     * @return Un {@link VBox} che rappresenta l'header.
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        // Titolo
        Label titleLabel = new Label("💡 Consiglia libri per questo titolo");
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // Info libro target
        HBox bookInfo = createTargetBookInfo();

        header.getChildren().addAll(titleLabel, bookInfo);
        return header;
    }

    /**
     * Crea la sezione che mostra la copertina e i dettagli del libro target.
     * @return Un {@link HBox} contenente le informazioni del libro target.
     */
    private HBox createTargetBookInfo() {
        HBox bookInfo = new HBox(15);
        bookInfo.setAlignment(Pos.CENTER);
        bookInfo.setStyle(
                "-fx-background-color: #383838;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        // Copertina
        ImageView coverImage = ImageUtils.createSafeImageView(targetBook.getImageUrl(), 50, 75);
        Rectangle coverClip = new Rectangle(50, 75);
        coverClip.setArcWidth(5);
        coverClip.setArcHeight(5);
        coverImage.setClip(coverClip);

        // Info
        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER_LEFT);

        Label bookTitle = new Label(targetBook.getTitle());
        bookTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        bookTitle.setTextFill(Color.WHITE);
        bookTitle.setWrapText(true);
        bookTitle.setMaxWidth(400);

        Label bookAuthor = new Label("di " + targetBook.getAuthor());
        bookAuthor.setFont(Font.font("SF Pro Text", 14));
        bookAuthor.setTextFill(Color.LIGHTGRAY);

        info.getChildren().addAll(bookTitle, bookAuthor);
        bookInfo.getChildren().addAll(coverImage, info);

        return bookInfo;
    }

    /**
     * Crea e configura la sezione di stato con messaggi e contatori.
     * @return Un {@link VBox} che rappresenta la sezione di stato.
     */
    private VBox createStatusSection() {
        VBox statusSection = new VBox(8);
        statusSection.setAlignment(Pos.CENTER);

        statusLabel = new Label("📚 Caricamento...");
        statusLabel.setFont(Font.font("SF Pro Text", 14));
        statusLabel.setTextFill(Color.LIGHTGRAY);

        remainingSlotsLabel = new Label("");
        remainingSlotsLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        remainingSlotsLabel.setTextFill(Color.YELLOW);

        Label instructionLabel = new Label("Seleziona fino a 3 libri dalle tue librerie da consigliare");
        instructionLabel.setFont(Font.font("SF Pro Text", 12));
        instructionLabel.setTextFill(Color.GRAY);

        statusSection.getChildren().addAll(statusLabel, remainingSlotsLabel, instructionLabel);
        return statusSection;
    }

    /**
     * Crea il container scrollabile principale per la visualizzazione delle librerie.
     * @return Uno {@link ScrollPane} per il contenuto principale.
     */
    private ScrollPane createMainContent() {
        librariesContainer = new VBox(15);
        librariesContainer.setAlignment(Pos.TOP_CENTER);
        librariesContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(librariesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: #2b2b2b;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );
        scrollPane.setPrefHeight(300);

        return scrollPane;
    }

    /**
     * Crea e configura la sezione che mostra i libri selezionati per la raccomandazione.
     * <p>
     * Questo metodo costruisce un {@link VBox} che serve da contenitore per l'elenco dei libri
     * scelti dall'utente. Include un titolo descrittivo e un {@link ScrollPane} per gestire
     * la visualizzazione dei libri, anche se la lista è lunga. Viene anche aggiunta una
     * label di stato iniziale che indica che nessun libro è stato ancora selezionato.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta la sezione completa dei libri selezionati.
     */
    private VBox createSelectedBooksSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.TOP_CENTER);

        Label selectedTitle = new Label("📋 Libri selezionati per la raccomandazione:");
        selectedTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        selectedTitle.setTextFill(Color.WHITE);

        // Container per i libri selezionati con ScrollPane
        selectedBooksContainer = new VBox(8);
        selectedBooksContainer.setAlignment(Pos.TOP_CENTER);
        selectedBooksContainer.setPadding(new Insets(16));

        ScrollPane selectedBooksScrollPane = new ScrollPane(selectedBooksContainer);
        selectedBooksScrollPane.setFitToWidth(true);
        selectedBooksScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        selectedBooksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        selectedBooksScrollPane.setPrefHeight(160);
        selectedBooksScrollPane.setMaxHeight(180);
        selectedBooksScrollPane.setMinHeight(140);
        selectedBooksScrollPane.setStyle(
                "-fx-background: #383838;" +
                        "-fx-background-color: #383838;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 8;"
        );

        // Label per quando non ci sono libri selezionati
        Label emptyLabel = new Label("Nessun libro selezionato");
        emptyLabel.setTextFill(Color.GRAY);
        emptyLabel.setFont(Font.font("SF Pro Text", 12));
        emptyLabel.setStyle("-fx-padding: 30;");
        selectedBooksContainer.getChildren().add(emptyLabel);

        section.getChildren().addAll(selectedTitle, selectedBooksScrollPane);
        return section;
    }

    /**
     * Crea e configura la sezione dei pulsanti "Salva" e "Annulla" per il dialog.
     * <p>
     * Questo metodo costruisce un {@link HBox} per contenere i pulsanti di azione
     * principali del dialog. Vengono creati e stilizzati i pulsanti "Salva" e
     * "Annulla", con un design moderno che include effetti di ombra e hover
     * per migliorare l'esperienza utente. Il pulsante "Salva" viene inizialmente
     * disabilitato e viene abilitato solo quando l'utente seleziona almeno un libro.
     * </p>
     *
     * @return Un {@link HBox} che contiene i pulsanti di salvataggio e annullamento.
     */
    private HBox createButtonSection() {
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));

        // Pulsante Salva
        saveButton = new Button("💾 Salva 3 Raccomandazioni");
        saveButton.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        saveButton.setStyle(
                "-fx-background-color: #4a7c59;" + // Verde scuro invece di verde acceso
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        // Effetto hover
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(
                "-fx-background-color: #5a8c69;" + // Verde leggermente più chiaro al hover
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
        ));

        saveButton.setOnMouseExited(e -> saveButton.setStyle(
                "-fx-background-color: #4a7c59;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        ));

        saveButton.setOnAction(e -> saveRecommendations());
        saveButton.setDisable(true); // Inizialmente disabilitato

        // Pulsante Annulla
        cancelButton = new Button("❌ Annulla");
        cancelButton.setFont(Font.font("SF Pro Text", FontWeight.NORMAL, 14));
        cancelButton.setStyle(
                "-fx-background-color: #6c757d;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                "-fx-background-color: #7c868d;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
        ));

        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                "-fx-background-color: #6c757d;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        ));

        cancelButton.setOnAction(e -> closeDialog());

        buttons.getChildren().addAll(cancelButton, saveButton);
        return buttons;
    }

    /**
     * Carica i dati iniziali dell'utente in modo asincrono, verificando se può
     * raccomandare libri e ottenendo le raccomandazioni esistenti.
     * <p>
     * Questo metodo esegue una serie di operazioni asincrone:
     * <ol>
     * <li>Verifica tramite {@link ClientRecommendationService#canUserRecommendAsync}
     * se l'utente ha il permesso di creare raccomandazioni per il libro target.</li>
     * <li>Se l'utente può raccomandare, aggiorna le variabili interne per il
     * numero di raccomandazioni correnti e massime.</li>
     * <li>Chiama {@link #updateStatusLabels()}, {@link #loadUserLibraries()} e
     * {@link #loadExistingRecommendations()} per caricare i dati e aggiornare l'UI.</li>
     * <li>In caso di errore nella verifica dei permessi o di connessione, mostra un
     * messaggio di errore visivo e disabilita il pulsante di salvataggio.</li>
     * </ol>
     * Le operazioni sono gestite su thread separati per non bloccare l'interfaccia utente
     * di JavaFX, con tutti gli aggiornamenti UI che avvengono su {@link Platform#runLater}.
     * </p>
     */
    private void loadUserData() {
        // Verifica permessi e carica dati
        recommendationService.canUserRecommendAsync(username, targetBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getCanRecommend() != null && response.getCanRecommend()) {
                        currentRecommendationsCount = response.getCurrentRecommendationsCount() != null ?
                                response.getCurrentRecommendationsCount() : 0;
                        maxRecommendations = response.getMaxRecommendations() != null ?
                                response.getMaxRecommendations() : 3;

                        updateStatusLabels();
                        loadUserLibraries();
                        loadExistingRecommendations();
                    } else {
                        statusLabel.setText("❌ " + response.getMessage());
                        statusLabel.setTextFill(Color.LIGHTCORAL);
                        saveButton.setDisable(true);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.LIGHTCORAL);
                    });
                    return null;
                });
    }


    /**
     * Carica le librerie dell'utente corrente e le visualizza.
     * <p>
     * Questo metodo esegue in modo asincrono una chiamata al servizio {@link LibraryService}
     * per ottenere l'elenco delle librerie dell'utente. Una volta ricevuta la risposta,
     * se l'operazione ha avuto successo, aggiorna l'interfaccia utente in modo thread-safe
     * per mostrare un messaggio di successo e visualizzare le librerie. In caso di fallimento,
     * mostra un messaggio di errore.
     * </p>
     */
    private void loadUserLibraries() {
        libraryService.getUserLibrariesAsync(username)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getLibraries() != null) {
                        userLibraries = response.getLibraries();
                        statusLabel.setText("✅ Caricamento completato");
                        statusLabel.setTextFill(Color.LIGHTGREEN);
                        displayLibraries();
                    } else {
                        statusLabel.setText("❌ Errore nel caricamento librerie");
                        statusLabel.setTextFill(Color.LIGHTCORAL);
                    }
                }));
    }

    /**
     * Carica le raccomandazioni esistenti dell'utente per il libro target.
     * <p>
     * Il metodo effettua una richiesta asincrona al {@link ClientRecommendationService}
     * per recuperare tutte le raccomandazioni che l'utente ha già creato per il libro
     * attualmente selezionato. Se la risposta è positiva, aggiorna la lista interna
     * delle raccomandazioni e invoca {@link #updateStatusLabels()} per riflettere
     * lo stato aggiornato nell'interfaccia utente.
     * </p>
     */
    private void loadExistingRecommendations() {
        recommendationService.getUserRecommendationsForBookAsync(username, targetBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getRecommendations() != null) {
                        existingRecommendations = response.getRecommendations();
                        updateStatusLabels();
                    }
                }));
    }

    /**
     * Visualizza le librerie dell'utente corrente all'interno dell'interfaccia.
     * <p>
     * Questo metodo si occupa di:
     * <ul>
     * <li>Pulire il contenitore dei nodi dell'interfaccia utente.</li>
     * <li>Verificare se l'utente ha delle librerie. Se la lista è vuota o null,
     * aggiunge un messaggio di stato che invita l'utente a crearne una.</li>
     * <li>Iterare sulla lista delle librerie dell'utente e, per ciascuna, creare
     * una card visiva ({@link #createLibraryCard}) per la visualizzazione.</li>
     * <li>Aggiungere ogni card creata al contenitore principale delle librerie.</li>
     * </ul>
     * Questo processo assicura che l'interfaccia utente sia sempre sincronizzata
     * con la lista delle librerie disponibili.
     * </p>
     */
    private void displayLibraries() {
        librariesContainer.getChildren().clear();

        if (userLibraries == null || userLibraries.isEmpty()) {
            Label noLibrariesLabel = new Label("📭 Non hai librerie. Crea delle librerie per consigliare libri!");
            noLibrariesLabel.setFont(Font.font("SF Pro Text", 14));
            noLibrariesLabel.setTextFill(Color.LIGHTGRAY);
            librariesContainer.getChildren().add(noLibrariesLabel);
            return;
        }

        for (String libraryName : userLibraries) {
            VBox libraryCard = createLibraryCard(libraryName);
            librariesContainer.getChildren().add(libraryCard);
        }
    }


    /**
     * Crea una card (scheda) per una singola libreria.
     * @param libraryName Il nome della libreria da visualizzare.
     * @return Un {@link VBox} che rappresenta la card della libreria.
     */
    private VBox createLibraryCard(String libraryName) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: #3a3a3c;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );

        // Header libreria
        Label libraryLabel = new Label("📚 " + libraryName);
        libraryLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        libraryLabel.setTextFill(Color.WHITE);

        // Container per i libri
        VBox booksContainer = new VBox(5);

        // Carica libri della libreria
        loadLibraryBooks(libraryName, booksContainer);

        card.getChildren().addAll(libraryLabel, booksContainer);
        return card;
    }

    /**
     * Carica e visualizza i libri all'interno di una specifica libreria.
     * <p>
     * Questo metodo esegue una chiamata asincrona al servizio librerie per ottenere
     * la lista dei libri in una data libreria. Durante il caricamento, viene mostrato
     * un indicatore visivo. Una volta che la richiesta ha successo, il contenitore
     * dei libri viene aggiornato. Se la libreria è vuota, viene mostrato un messaggio.
     * Altrimenti, per ogni libro, viene creata una card ({@link #createBookCard})
     * e aggiunta al contenitore, assicurandosi di escludere il libro target
     * (quello per cui si stanno creando le raccomandazioni) dalla lista.
     * In caso di errore, viene visualizzato un messaggio appropriato.
     * </p>
     *
     * @param libraryName Il nome della libreria da cui caricare i libri.
     * @param booksContainer Il {@link VBox} in cui verranno aggiunte le card dei libri.
     */
    private void loadLibraryBooks(String libraryName, VBox booksContainer) {
        Label loadingLabel = new Label("📖 Caricamento libri...");
        loadingLabel.setFont(Font.font("SF Pro Text", 12));
        loadingLabel.setTextFill(Color.GRAY);
        booksContainer.getChildren().add(loadingLabel);

        libraryService.getBooksInLibraryAsync(username, libraryName)
                .thenAccept(response -> Platform.runLater(() -> {
                    booksContainer.getChildren().clear();

                    if (response.isSuccess() && response.getBooks() != null) {
                        List<Book> books = response.getBooks();

                        if (books.isEmpty()) {
                            Label emptyLabel = new Label("📭 Libreria vuota");
                            emptyLabel.setFont(Font.font("SF Pro Text", 12));
                            emptyLabel.setTextFill(Color.GRAY);
                            booksContainer.getChildren().add(emptyLabel);
                        } else {
                            for (Book book : books) {
                                // Non mostrare il libro target nelle opzioni
                                if (!targetBook.getIsbn().equals(book.getIsbn())) {
                                    HBox bookCard = createBookCard(book);
                                    booksContainer.getChildren().add(bookCard);
                                }
                            }
                        }
                    } else {
                        Label errorLabel = new Label("❌ Errore caricamento: " + response.getMessage());
                        errorLabel.setFont(Font.font("SF Pro Text", 12));
                        errorLabel.setTextFill(Color.LIGHTCORAL);
                        booksContainer.getChildren().add(errorLabel);
                    }
                }));
    }

    /**
     * Crea una card (scheda) per un singolo libro, con la possibilità di selezionarlo.
     * <p>
     * Questo metodo costruisce un {@link HBox} che funge da rappresentazione visiva
     * di un libro. La card include il titolo e l'autore del libro e una {@link CheckBox}
     * che permette all'utente di selezionarlo per la raccomandazione. Il metodo gestisce
     * la logica per impedire la selezione di libri già raccomandati e per limitare
     * il numero di selezioni in base agli slot disponibili. Un evento `setOnAction`
     * è collegato alla checkbox per aggiornare dinamicamente l'interfaccia utente e
     * la lista dei libri selezionati.
     * </p>
     *
     * @param book Il libro da visualizzare nella card.
     * @return Un {@link HBox} che rappresenta la card completa del libro.
     */
    private HBox createBookCard(Book book) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #4a4a4c;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8;"
        );

        // Info libro
        VBox bookInfo = new VBox(3);
        bookInfo.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(300);

        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.setFont(Font.font("SF Pro Text", 10));
        authorLabel.setTextFill(Color.LIGHTGRAY);

        bookInfo.getChildren().addAll(titleLabel, authorLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Checkbox per selezione
        CheckBox selectBox = new CheckBox();
        selectBox.setStyle("-fx-text-fill: white;");

        // Verifica se già selezionato o già raccomandato
        boolean alreadySelected = selectedBooks.contains(book);
        boolean alreadyRecommended = existingRecommendations != null &&
                existingRecommendations.stream().anyMatch(rec -> book.getIsbn().equals(rec.getRecommendedBookIsbn()));

        selectBox.setSelected(alreadySelected);
        selectBox.setDisable(alreadyRecommended);

        if (alreadyRecommended) {
            Label recommendedLabel = new Label("✅ Già consigliato");
            recommendedLabel.setFont(Font.font("SF Pro Text", 10));
            recommendedLabel.setTextFill(Color.LIGHTGREEN);
            card.getChildren().addAll(bookInfo, spacer, recommendedLabel);
        } else {
            selectBox.setOnAction(e -> {
                if (selectBox.isSelected()) {
                    if (selectedBooks.size() < getAvailableSlots()) {
                        selectedBooks.add(book);
                        updateSelectedBooksDisplay();
                        updateSaveButton();
                    } else {
                        selectBox.setSelected(false);
                        showAlert("⚠️ Limite raggiunto",
                                "Puoi selezionare al massimo " + getAvailableSlots() + " libri per le raccomandazioni.");
                    }
                } else {
                    selectedBooks.remove(book);
                    updateSelectedBooksDisplay();
                    updateSaveButton();
                }
            });

            card.getChildren().addAll(bookInfo, spacer, selectBox);
        }

        return card;
    }

    /**
     * Aggiorna la visualizzazione dei libri selezionati nel container apposito.
     * <p>
     * Questo metodo sincronizza l'interfaccia utente con la lista dei libri
     * selezionati dall'utente. Pulisce il contenitore esistente, e se la lista
     * di selezione è vuota, aggiunge un messaggio. Altrimenti, crea e aggiunge una riga
     * per ogni libro selezionato. Infine, aggiorna lo stato del pulsante di salvataggio
     * e la label che indica gli slot rimanenti per la raccomandazione.
     * </p>
     */
    private void updateSelectedBooksDisplay() {
        selectedBooksContainer.getChildren().clear();

        if (selectedBooks.isEmpty()) {
            Label emptyLabel = new Label("Nessun libro selezionato");
            emptyLabel.setTextFill(Color.GRAY);
            emptyLabel.setFont(Font.font("SF Pro Text", 12));
            emptyLabel.setStyle("-fx-padding: 20;");
            selectedBooksContainer.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < selectedBooks.size(); i++) {
                Book book = selectedBooks.get(i);
                HBox bookRow = createSelectedBookRow(book, i + 1);
                selectedBooksContainer.getChildren().add(bookRow);
            }
        }

        // Aggiorna pulsante salva
        saveButton.setDisable(selectedBooks.size() != maxRecommendations);

        // Aggiorna label slot rimanenti
        int remaining = maxRecommendations - selectedBooks.size();
        remainingSlotsLabel.setText("Slot rimanenti: " + remaining + "/" + maxRecommendations);
    }

    /**
     * Aggiorna lo stato e il testo del pulsante di salvataggio.
     * <p>
     * Questo metodo abilita o disabilita il pulsante di salvataggio in base
     * al fatto che ci siano o meno libri selezionati. Modifica anche il testo del
     * pulsante per riflettere il numero di raccomandazioni che verranno salvate.
     * </p>
     */
    private void updateSaveButton() {
        boolean hasSelections = !selectedBooks.isEmpty();
        saveButton.setDisable(!hasSelections);

        if (hasSelections) {
            saveButton.setText("Salva " + selectedBooks.size() + " Raccomandazioni");
        } else {
            saveButton.setText("Salva Raccomandazioni");
        }
    }

    /**
     * Aggiorna il testo e il colore delle label di stato che indicano
     * il numero di raccomandazioni disponibili.
     * <p>
     * Il metodo calcola gli slot di raccomandazione ancora disponibili e
     * imposta il testo e il colore della label `remainingSlotsLabel` in base
     * al risultato. Se non ci sono più slot disponibili, disabilita il
     * pulsante di salvataggio.
     * </p>
     */
    private void updateStatusLabels() {
        int availableSlots = getAvailableSlots();

        if (availableSlots > 0) {
            remainingSlotsLabel.setText("📋 Puoi aggiungere ancora " + availableSlots + " raccomandazioni");
            remainingSlotsLabel.setTextFill(Color.YELLOW);
        } else {
            remainingSlotsLabel.setText("🚫 Hai raggiunto il limite massimo di raccomandazioni");
            remainingSlotsLabel.setTextFill(Color.LIGHTCORAL);
            saveButton.setDisable(true);
        }
    }

    /**
     * Calcola il numero di slot di raccomandazione disponibili.
     * @return Il numero di slot disponibili.
     */
    private int getAvailableSlots() {
        return Math.max(0, maxRecommendations - currentRecommendationsCount);
    }

    /**
     * Salva le raccomandazioni selezionate inviando richieste asincrone al servizio.
     * <p>
     * Questo metodo gestisce il processo di salvataggio delle raccomandazioni in modo
     * non bloccante. Prima di tutto, verifica che almeno un libro sia stato selezionato.
     * Successivamente, disabilita il pulsante di salvataggio per prevenire invii multipli.
     * Crea una richiesta asincrona (tramite {@link CompletableFuture}) per ogni libro
     * selezionato e attende che tutte le richieste siano completate con
     * {@link CompletableFuture#allOf}.
     * Una volta che tutte le richieste sono terminate, il metodo elabora i risultati,
     * conta i successi e raccoglie gli eventuali errori. Infine, aggiorna l'interfaccia
     * utente mostrando un {@link Alert} che riassume l'esito dell'operazione e, se
     * necessario, ricarica le raccomandazioni aggiornate prima di chiudere il dialog.
     * </p>
     */
    private void saveRecommendations() {
        if (selectedBooks.isEmpty()) {
            showAlert("⚠️ Attenzione", "Seleziona almeno un libro da consigliare.");
            return;
        }

        saveButton.setDisable(true);
        saveButton.setText("Salvando...");

        List<CompletableFuture<RecommendationResponse>> futures = new ArrayList<>();

        // Crea una richiesta per ogni libro selezionato
        for (Book selectedBook : selectedBooks) {
            RecommendationRequest request = new RecommendationRequest(
                    username,
                    targetBook.getIsbn(),
                    selectedBook.getIsbn()
            );

            futures.add(recommendationService.addRecommendationAsync(request));
        }

        // Attendi tutte le richieste
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        int successCount = 0;
                        List<String> errors = new ArrayList<>();

                        for (int i = 0; i < futures.size(); i++) {
                            try {
                                RecommendationResponse response = futures.get(i).get();
                                if (response.isSuccess()) {
                                    successCount++;
                                } else {
                                    errors.add("• " + selectedBooks.get(i).getTitle() + ": " + response.getMessage());
                                }
                            } catch (Exception e) {
                                errors.add("• " + selectedBooks.get(i).getTitle() + ": Errore di connessione");
                            }
                        }

                        // Mostra risultati
                        if (successCount == selectedBooks.size()) {
                            showAlert("✅ Successo",
                                    "Tutte le " + successCount + " raccomandazioni sono state salvate con successo!");

                            // Notifica parent e chiudi
                            if (onRecommendationsSaved != null) {
                                // Ricarica le raccomandazioni aggiornate
                                loadUpdatedRecommendations();
                            } else {
                                closeDialog();
                            }
                        } else if (successCount > 0) {
                            String message = "Salvate " + successCount + " raccomandazioni su " + selectedBooks.size() +
                                    ".\n\nErrori:\n" + String.join("\n", errors);
                            showAlert("⚠️ Parzialmente completato", message);

                            if (onRecommendationsSaved != null) {
                                loadUpdatedRecommendations();
                            } else {
                                closeDialog();
                            }
                        } else {
                            String message = "Nessuna raccomandazione salvata.\n\nErrori:\n" + String.join("\n", errors);
                            showAlert("❌ Errore", message);

                            saveButton.setDisable(false);
                            saveButton.setText("Salva Raccomandazioni");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                        saveButton.setDisable(false);
                        saveButton.setText("Salva Raccomandazioni");
                    });
                    return null;
                });
    }

    /**
     * Ricarica la lista di raccomandazioni aggiornate dopo un'operazione di salvataggio.
     * <p>
     * Questo metodo effettua una nuova richiesta asincrona per recuperare la lista
     * completa e aggiornata delle raccomandazioni dell'utente per il libro target.
     * Se la richiesta ha successo e un consumer è stato fornito, notifica il componente
     * genitore con la nuova lista. Infine, chiude il dialog, indipendentemente
     * dall'esito dell'operazione, per garantire la chiusura della finestra.
     * </p>
     */
    private void loadUpdatedRecommendations() {
        recommendationService.getUserRecommendationsForBookAsync(username, targetBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && onRecommendationsSaved != null) {
                        onRecommendationsSaved.accept(response.getRecommendations());
                    }
                    closeDialog();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> closeDialog());
                    return null;
                });
    }

    /**
     * Chiude il dialog.
     */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Mostra un alert di informazione o errore.
     * @param title Il titolo dell'alert.
     * @param message Il messaggio da visualizzare nell'alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Mostra il dialog.
     */
    public void show() {
        if (dialogStage != null) {
            dialogStage.show();
            dialogStage.centerOnScreen();
        }
    }

    /**
     * Mostra il dialog e attende la sua chiusura.
     */
    public void showAndWait() {
        if (dialogStage != null) {
            dialogStage.showAndWait();
        }
    }

    /**
     * Metodo statico per creare e mostrare rapidamente un dialog di raccomandazione.
     *
     * @param targetBook Il libro per cui raccomandare altri titoli.
     * @param username L'username dell'utente.
     * @param authManager Il gestore di autenticazione.
     * @param onRecommendationsSaved Il consumer per la lista di raccomandazioni aggiornata.
     */
    public static void showRecommendationDialog(Book targetBook, String username,
                                                AuthenticationManager authManager,
                                                Consumer<List<BookRecommendation>> onRecommendationsSaved) {
        RecommendationDialog dialog = new RecommendationDialog(targetBook, username, authManager, onRecommendationsSaved);
        dialog.show();
    }

    /**
     * Crea una riga per visualizzare un libro selezionato.
     * @param book Il libro da visualizzare.
     * @param position La posizione del libro nella lista di selezione.
     * @return Un {@link HBox} che rappresenta la riga del libro selezionato.
     */
    private HBox createSelectedBookRow(Book book, int position) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle(
                "-fx-background-color: #4a7c59;" + // Verde scuro sobrio
                        "-fx-background-radius: 6;" +
                        "-fx-min-height: 40;"
        );

        // Numero posizione
        Label positionLabel = new Label(String.valueOf(position));
        positionLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        positionLabel.setTextFill(Color.WHITE);
        positionLabel.setStyle(
                "-fx-background-color: #2d5233;" + // Verde ancora più scuro
                        "-fx-background-radius: 15;" +
                        "-fx-min-width: 25;" +
                        "-fx-min-height: 25;" +
                        "-fx-alignment: center;"
        );

        // Info libro
        VBox bookInfo = new VBox(2);
        bookInfo.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setMaxWidth(400);
        titleLabel.setStyle("-fx-text-overrun: ellipsis;");

        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.setFont(Font.font("SF Pro Text", 10));
        authorLabel.setTextFill(Color.web("#E0E0E0"));
        authorLabel.setMaxWidth(400);
        authorLabel.setStyle("-fx-text-overrun: ellipsis;");

        bookInfo.getChildren().addAll(titleLabel, authorLabel);

        // Pulsante rimuovi
        Button removeButton = new Button("❌");
        removeButton.setFont(Font.font("SF Pro Text", 10));
        removeButton.setStyle(
                "-fx-background-color: #dc3545;" + // Rosso sobrio
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-min-width: 30;" +
                        "-fx-min-height: 30;" +
                        "-fx-cursor: hand;"
        );

        removeButton.setOnMouseEntered(e -> removeButton.setStyle(
                "-fx-background-color: #e85a67;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-min-width: 30;" +
                        "-fx-min-height: 30;" +
                        "-fx-cursor: hand;"
        ));

        removeButton.setOnMouseExited(e -> removeButton.setStyle(
                "-fx-background-color: #dc3545;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-min-width: 30;" +
                        "-fx-min-height: 30;" +
                        "-fx-cursor: hand;"
        ));

        removeButton.setOnAction(e -> {
            selectedBooks.remove(book);
            updateSelectedBooksDisplay();
            // Aggiorna anche i pulsanti di selezione nella libreria
            refreshLibraryDisplay();
        });

        // Spacer per spingere il pulsante remove a destra
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(positionLabel, bookInfo, spacer, removeButton);
        return row;
    }

    /**
     * Forza il ricaricamento della visualizzazione delle librerie.
     */
    private void refreshLibraryDisplay() {
        loadUserLibraries();
    }
}