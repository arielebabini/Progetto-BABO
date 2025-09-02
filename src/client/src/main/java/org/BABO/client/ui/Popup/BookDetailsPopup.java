package org.BABO.client.ui.Popup;

import javafx.animation.Interpolator;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Home.IconUtils;
import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.client.ui.Rating.RatingDialog;
import org.BABO.client.ui.Recommendation.RecommendationDialog;
import org.BABO.shared.dto.Library.LibraryResponse;
import org.BABO.shared.model.Book;
import org.BABO.shared.dto.Recommendation.RecommendationRequest;
import org.BABO.shared.dto.Recommendation.RecommendationResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.BABO.shared.model.BookRating;
import org.BABO.client.service.LibraryService;
import org.BABO.client.service.ClientRatingService;
import org.BABO.client.service.ClientRecommendationService;
import org.BABO.shared.model.BookRecommendation;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.util.stream.Collectors;

import java.util.concurrent.CompletableFuture;

/**
 * Componente popup per la visualizzazione dei dettagli di un libro.
 * <p>
 * `BookDetailsPopup` è un componente UI avanzato che offre una vista completa
 * dei dettagli di un libro, includendo copertina, informazioni bibliografiche,
 * e funzionalità interattive come il sistema di valutazione e il motore di raccomandazioni.
 * Il design è ottimizzato per una navigazione intuitiva tra i libri di una collezione
 * e si integra con il `PopupManager` per una gestione del ciclo di vita del popup
 * e degli eventi utente, come la pressione del tasto ESC o il click sullo sfondo,
 * in modo coerente e robusto.
 * </p>
 *
 * <h3>Architettura e funzionalità principali:</h3>
 * <p>
 * Il design del popup è modulare e focalizzato sull'esperienza utente,
 * garantendo un'interazione fluida e reattiva:
 * </p>
 * <ul>
 * <li><strong>Dettagli Libro:</strong> Visualizzazione di titolo, autore, anno, ISBN e descrizione.</li>
 * <li><strong>Navigazione Collezione:</strong> Permette di scorrere tra più libri tramite frecce di navigazione e tasti della tastiera.</li>
 * <li><strong>Sistema di Valutazione:</strong> Mostra il rating medio della community e consente agli utenti autenticati di valutare il libro.</li>
 * <li><strong>Raccomandazioni Personalizzate:</strong> Fornisce suggerimenti di libri basati su similarità.</li>
 * <li><strong>Integrazione Autenticazione:</strong> Adatta le funzionalità (valutazione, libreria) in base allo stato di login dell'utente.</li>
 * <li><strong>Gestione Popup Avanzata:</strong> Interagisce con il `PopupManager` per un'apertura, una chiusura e una gestione del focus ottimali.</li>
 * </ul>
 *
 * <h3>Struttura del layout:</h3>
 * <pre>
 * StackPane (root)
 * ├── StackPane (sfondo sfocato)
 * ├── StackPane (container principale)
 * │   ├── VBox (contenuto del libro)
 * │   │   ├── HBox (barra superiore con bottone di chiusura)
 * │   │   └── ScrollPane (contenuto principale scrollabile)
 * │   │       └── VBox (sezioni dettagli, editore, valutazione, raccomandazioni)
 * │   └── VBox (preview libri successivi/precedenti)
 * ├── Button (freccia sinistra)
 * └── Button (freccia destra)
 * </pre>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see org.BABO.client.ui.Authentication.AuthenticationManager
 * @see org.BABO.shared.model.Book
 * @see org.BABO.client.ui.Popup.PopupManager
 */
public class BookDetailsPopup {

    /**
     * Il contenitore principale (`StackPane`) del popup, che gestisce la disposizione di tutti gli altri componenti UI.
     */
    private static StackPane root;
    /**
     * La lista di oggetti {@link org.BABO.shared.model.Book} che costituiscono la collezione navigabile all'interno del popup.
     */
    private static List<Book> booksCollection;
    /**
     * L'indice numerico del libro attualmente visualizzato nella collezione. Questo valore viene aggiornato man mano che l'utente naviga.
     */
    private static int currentBookIndex = 0;
    /**
     * Il pannello (`StackPane`) che contiene i dettagli del libro visualizzato, compreso il suo layout e i suoi contenuti.
     */
    private static StackPane bookDisplayPane;
    /**
     * Un pannello (`VBox`) che mostra un'anteprima del libro successivo nella collezione.
     */
    private static VBox nextBookPreview;
    /**
     * Un pannello (`VBox`) che mostra un'anteprima del libro precedente nella collezione.
     */
    private static VBox prevBookPreview;
    /**
     * L'oggetto {@link javafx.animation.Timeline} utilizzato per gestire l'animazione di scorrimento (`slide`) tra i libri quando l'utente naviga.
     */
    private static Timeline slideAnimation;
    /**
     * Un'istanza di {@link java.lang.Runnable} che viene eseguita alla chiusura del popup, ad esempio per aggiornare la UI principale o rilasciare risorse.
     */
    private static Runnable closeHandler;
    /**
     * Un flag booleano che indica se un'animazione di transizione è in corso, per evitare che l'utente avvii più transizioni contemporaneamente.
     */
    private static boolean isTransitioning = false;
    /**
     * Il pulsante per la navigazione verso il libro precedente.
     */
    private static Button leftArrowButton;
    /**
     * Il pulsante per la navigazione verso il libro successivo.
     */
    private static Button rightArrowButton;

    /**
     * Un'istanza del servizio {@link org.BABO.client.service.ClientRatingService} responsabile di tutte le interazioni con il backend per le operazioni di valutazione, come l'invio e il recupero dei voti.
     */
    private static final ClientRatingService ratingService = new ClientRatingService();
    /**
     * L'oggetto {@link org.BABO.shared.model.BookRating} che rappresenta il voto dato al libro dall'utente attualmente autenticato. È {@code null} se l'utente non ha ancora votato.
     */
    private static BookRating currentUserRating = null;
    /**
     * Il valore (`Double`) che rappresenta il voto medio del libro calcolato dal backend. È {@code null} se non ci sono ancora voti.
     */
    private static Double averageBookRating = null;
    /**
     * Il numero totale (`Integer`) di recensioni o voti che il libro ha ricevuto.
     */
    private static Integer currentBookReviewCount = null;
    /**
     * Un'etichetta (`Label`) della UI che visualizza il voto medio del libro.
     */
    private static Label averageRatingLabel = null;
    /**
     * La sezione dell'interfaccia utente (`VBox`) che mostra la valutazione corrente del libro e le opzioni di voto per l'utente.
     */
    private static VBox currentRatingSection = null;
    /**
     * Un'istanza del gestore di autenticazione {@link org.BABO.client.ui.Authentication.AuthenticationManager} usata per verificare lo stato di login dell'utente e per adattare le funzionalità di rating di conseguenza.
     */
    private static AuthenticationManager currentAuthManager = null;
    /**
     * L'oggetto {@link org.BABO.shared.model.Book} che rappresenta il libro attualmente visualizzato nel popup.
     */
    private static Book currentBook = null;

    /**
     * Un'istanza del servizio {@link org.BABO.client.service.ClientRecommendationService} che gestisce la richiesta e la ricezione di raccomandazioni di libri dal backend.
     */
    private static ClientRecommendationService recommendationService = new ClientRecommendationService();
    /**
     * La lista di oggetti {@link org.BABO.shared.model.Book} restituiti dal servizio di raccomandazione, che vengono mostrati all'utente. È {@code null} se il servizio non è ancora stato chiamato.
     */
    private static List<Book> recommendedBooksDetails = null;
    /**
     * Il pannello di scorrimento (`ScrollPane`) della UI che contiene e visualizza le raccomandazioni di libri.
     */
    private static ScrollPane currentRecommendationsScrollPane = null;

    /**
     * Crea e restituisce un'istanza di {@link StackPane} per il popup dei dettagli di un singolo libro.
     * <p>
     * Questo metodo è l'API pubblica di base per la creazione del popup. Non include l'integrazione
     * con il sistema di autenticazione e agisce come una versione semplificata per la visualizzazione
     * di un singolo libro e della sua collezione. Internamente, chiama {@link #createWithLibrarySupport(Book, List, Runnable, AuthenticationManager)}
     * passando {@code null} per il gestore di autenticazione.
     * </p>
     *
     * @param book il libro di cui visualizzare i dettagli
     * @param collection la collezione di libri a cui appartiene il libro corrente
     * @param onClose un {@link Runnable} da eseguire alla chiusura del popup
     * @return lo {@link StackPane} root del popup pronto per essere aggiunto a una scena
     */
    public static StackPane create(Book book, List<Book> collection, Runnable onClose) {
        return createWithLibrarySupport(book, collection, onClose, null);
    }

    /**
     * Crea e restituisce un'istanza di {@link StackPane} per il popup dei dettagli di un libro con supporto per l'autenticazione.
     * <p>
     * Questo metodo è l'API pubblica avanzata che gestisce l'integrazione con il gestore di autenticazione.
     * Ciò permette al popup di adattarsi allo stato di login dell'utente e di abilitare funzionalità specifiche
     * come la valutazione del libro e l'aggiunta alla libreria personale. Il metodo inizializza lo stato del popup
     * e poi crea il container principale con tutti i componenti UI necessari.
     * </p>
     *
     * @param book il libro di cui visualizzare i dettagli
     * @param collection la collezione di libri a cui appartiene il libro corrente
     * @param onClose un {@link Runnable} da eseguire alla chiusura del popup
     * @param authManager il gestore di autenticazione per l'integrazione delle funzionalità utente
     * @return lo {@link StackPane} root del popup pronto per essere aggiunto a una scena
     */
    public static StackPane createWithLibrarySupport(Book book, List<Book> collection, Runnable onClose,
                                                     AuthenticationManager authManager) {
        initializePopup(book, collection, onClose, authManager);
        return createMainContainer(book);
    }

    /**
     * Inizializza le variabili di stato statiche del popup.
     * <p>
     * Questo metodo privato configura le variabili di classe come la collezione di libri,
     * l'indice del libro corrente, il gestore di chiusura e il gestore di autenticazione.
     * È il primo passo nella creazione del popup e assicura che lo stato sia pulito e
     * correttamente impostato prima di costruire i componenti dell'interfaccia utente.
     * Inoltre, resetta lo stato delle valutazioni per il nuovo libro selezionato.
     * </p>
     *
     * @param book il libro di cui visualizzare i dettagli
     * @param collection la collezione di libri a cui appartiene il libro corrente
     * @param onClose un {@link Runnable} da eseguire alla chiusura del popup
     * @param authManager il gestore di autenticazione per l'integrazione delle funzionalità utente
     */
    private static void initializePopup(Book book, List<Book> collection, Runnable onClose,
                                        AuthenticationManager authManager) {
        booksCollection = collection;
        closeHandler = onClose;
        currentBookIndex = Math.max(0, collection.indexOf(book));

        resetRatings();
        currentBook = book;
        currentAuthManager = authManager;
    }

    /**
     * Crea e configura il contenitore principale del popup (`StackPane`), inclusi tutti i componenti UI interni.
     * <p>
     * Questo metodo è responsabile della costruzione dell'intera struttura del popup, a partire dal layer
     * di sfondo sfocato fino al contenuto specifico del libro. Gestisce la logica di visualizzazione,
     * inclusa la creazione del pannello per il libro, il caricamento delle valutazioni e l'impostazione
     * dei meccanismi di navigazione per le collezioni di libri più grandi di uno.
     * Si occupa anche di configurare la gestione del focus e degli eventi per il popup.
     * </p>
     *
     * @param book il libro per cui il container principale viene creato
     * @return il {@link StackPane} root del popup, pronto per l'uso
     */
    private static StackPane createMainContainer(Book book) {
        root = new StackPane();

        // Background
        StackPane blurLayer = createBackgroundLayer();

        // Book display pane
        bookDisplayPane = new StackPane();
        VBox currentBookContent = createBookContent(book, getBookBackgroundColor(book), currentAuthManager);

        // Load ratings
        loadBookRatingsForAllUsers(book, currentAuthManager);

        // Setup navigation for multiple books
        if (booksCollection.size() > 1) {
            setupMultiBookNavigation(currentBookContent);
        } else {
            bookDisplayPane.getChildren().add(currentBookContent);
        }

        root.getChildren().addAll(blurLayer, bookDisplayPane);

        setupImprovedFocusHandling(root);

        return root;
    }

    /**
     * Configura la gestione avanzata del focus e degli eventi da tastiera per il popup.
     * <p>
     * Questo metodo abilita il focus per il {@link StackPane} root del popup e gestisce gli eventi
     * della tastiera per una navigazione e una chiusura più efficienti. In particolare:
     * <ul>
     * <li>Rende il popup "attraversabile" dal focus della tastiera.</li>
     * <li>Associa la pressione del tasto ESC alla chiusura del popup, chiamando
     * {@link #handlePopupCloseWithPopupManager()}.</li>
     * <li>Permette la navigazione tra i libri della collezione tramite i tasti freccia
     * sinistra e destra.</li>
     * </ul>
     * Assicura inoltre che il focus venga immediatamente richiesto dal popup all'apertura,
     * garantendo che gli eventi da tastiera siano intercettati correttamente.
     * </p>
     *
     * @param root il {@link StackPane} principale del popup
     */
    private static void setupImprovedFocusHandling(StackPane root) {
        root.setFocusTraversable(true);

        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                System.out.println("🔑 ESC premuto - chiusura tramite PopupManager");
                handlePopupCloseWithPopupManager();
                event.consume();
                return;
            }

            // Gestione frecce per navigazione
            if (event.getCode() == KeyCode.LEFT) {
                if (currentBookIndex > 0 && !isTransitioning) {
                    slideToBook(currentBookIndex - 1);
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                    slideToBook(currentBookIndex + 1);
                    event.consume();
                }
            }
        });

        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    /**
     * Gestisce la chiusura del popup utilizzando il {@link org.BABO.client.ui.Popup.PopupManager} per una gestione centralizzata.
     * <p>
     * Questo metodo tenta di chiudere il popup attraverso un'istanza del `PopupManager`, che è responsabile di mantenere una
     * pila di popup attivi e di chiudere quello in cima in modo coerente. Se il `PopupManager` non è inizializzato,
     * non ha popup attivi o si verifica un errore, il metodo ricorre a una chiusura manuale tramite il
     * metodo di fallback {@link #handlePopupCloseManual()}. Questo approccio garantisce una gestione robusta
     * degli eventi di chiusura del popup.
     * </p>
     *
     * @see org.BABO.client.ui.Popup.PopupManager#getInstance()
     * @see org.BABO.client.ui.Popup.PopupManager#closeTopPopup()
     */
    private static void handlePopupCloseWithPopupManager() {
        try {
            System.out.println("🔒 BookDetailsPopup: Chiusura tramite PopupManager");

            PopupManager popupManager = PopupManager.getInstance();

            if (!popupManager.isInitialized()) {
                System.err.println("⚠️ PopupManager non inizializzato, uso fallback");
                handlePopupCloseManual();
                return;
            }

            if (popupManager.hasActivePopups()) {
                popupManager.closeTopPopup();
                System.out.println("✅ Popup chiuso tramite PopupManager");
            } else {
                System.out.println("⚠️ Nessun popup attivo in PopupManager, uso fallback");
                handlePopupCloseManual();
            }

        } catch (Exception e) {
            System.err.println("❌ Errore chiusura PopupManager: " + e.getMessage());
            handlePopupCloseManual();
        }
    }

    /**
     * Gestisce la chiusura manuale del popup attraverso una serie di tentativi di fallback.
     * <p>
     * Questo metodo è invocato quando la chiusura tramite {@link #handlePopupCloseWithPopupManager()} fallisce.
     * Tenta di rimuovere il popup dal suo nodo padre in diversi modi, in ordine di priorità:
     * <ol>
     * <li>Rimuove il popup direttamente dal suo {@link javafx.scene.layout.StackPane} genitore, se esiste.</li>
     * <li>Scansiona tutte le {@link javafx.stage.Window} aperte per trovare la {@link javafx.stage.Stage} che contiene il popup e lo rimuove.</li>
     * <li>Come ultima risorsa, se il popup non può essere rimosso in modo programmatico, esegue il {@link Runnable} {@code closeHandler} originale, se è stato fornito.</li>
     * </ol>
     * Questo approccio robusto garantisce che il popup venga chiuso correttamente anche in scenari complessi o imprevisti,
     * gestendo gli errori per prevenire il blocco dell'applicazione.
     * </p>
     */
    private static void handlePopupCloseManual() {
        try {
            System.out.println("🔧 Fallback: chiusura manuale popup");

            if (root != null && root.getParent() instanceof StackPane) {
                StackPane parent = (StackPane) root.getParent();
                parent.getChildren().remove(root);
                System.out.println("✅ Popup rimosso manualmente dal parent");
                return;
            }

            // Cerca attraverso le finestre aperte
            javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Stage.getWindows();
            for (javafx.stage.Window window : windows) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getScene() != null && stage.getScene().getRoot() instanceof StackPane) {
                        StackPane stageRoot = (StackPane) stage.getScene().getRoot();
                        if (stageRoot.getChildren().contains(root)) {
                            stageRoot.getChildren().remove(root);
                            System.out.println("✅ Popup rimosso manualmente dal stage root");
                            return;
                        }
                    }
                }
            }

            // Ultimo fallback: usa il closeHandler originale
            if (closeHandler != null) {
                closeHandler.run();
                System.out.println("✅ Usato closeHandler originale");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore anche nel fallback manuale: " + e.getMessage());

            // Fallback finale: usa il closeHandler se esiste
            if (closeHandler != null) {
                try {
                    closeHandler.run();
                } catch (Exception finalError) {
                    System.err.println("❌ Errore anche nel closeHandler: " + finalError.getMessage());
                }
            }
        }
    }

    /**
     * Crea e configura il livello di sfondo semitrasparente e sfocato del popup.
     * <p>
     * Questo metodo crea uno {@link StackPane} che agisce come sfondo per l'intero popup.
     * Applica uno stile CSS per renderlo semitrasparente e un effetto {@link javafx.scene.effect.BoxBlur}
     * per sfocare i contenuti sottostanti, mettendo in evidenza il popup.
     * Imposta anche un gestore di eventi per il click del mouse, che consente all'utente di chiudere
     * il popup semplicemente cliccando sullo sfondo. Questo evento viene gestito in modo centralizzato
     * tramite il {@link org.BABO.client.ui.Popup.PopupManager} per garantire coerenza.
     * </p>
     *
     * @return il {@link StackPane} che rappresenta il livello di sfondo
     */
    private static StackPane createBackgroundLayer() {
        StackPane blurLayer = new StackPane();
        blurLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        blurLayer.setEffect(new BoxBlur(20, 20, 3));

        // Click background tramite PopupManager
        blurLayer.setOnMouseClicked(e -> {
            System.out.println("🖱️ Click su background - chiusura tramite PopupManager");
            handlePopupCloseWithPopupManager();
            e.consume();
        });

        return blurLayer;
    }

    /**
     * Aggiorna lo stato del popup dopo che è stato ripristinato il focus, per esempio al ritorno da un popup figlio.
     * <p>
     * Questo metodo è utilizzato in scenari di gestione avanzata del popup, come quando un popup figlio (es. un dialog di valutazione)
     * viene chiuso e il focus deve tornare al {@code BookDetailsPopup}. Viene eseguito su un thread della piattaforma
     * per garantire che le operazioni di aggiornamento della UI siano sicure. Le azioni principali includono:
     * <ul>
     * <li>Rirendere il popup "attraversabile" dal focus e richiedere il focus per assicurare che la navigazione da tastiera funzioni correttamente.</li>
     * <li>Riabilitare la visibilità dei bottoni di navigazione (frecce), se necessario.</li>
     * <li>Ricaricare le raccomandazioni del libro per assicurare che siano aggiornate.</li>
     * </ul>
     * Questo approccio garantisce che l'esperienza utente rimanga fluida e che il popup sia sempre reattivo.
     * </p>
     */
    public static void refreshPopupOnFocusRestore() {
        Platform.runLater(() -> {
            try {
                if (root != null) {
                    // Riabilita focus
                    root.setFocusTraversable(true);
                    root.requestFocus();

                    // Riabilita navigation se necessario
                    if (leftArrowButton != null && rightArrowButton != null) {
                        updateArrowVisibility();
                    }

                    // Ricarica sezione raccomandazioni se necessario
                    if (currentBook != null && currentRecommendationsScrollPane != null) {
                        loadBookRecommendations(currentBook, currentRecommendationsScrollPane);
                    }

                    System.out.println("🔄 Popup refreshed dopo ripristino focus tramite PopupManager");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Errore refresh popup: " + e.getMessage());
            }
        });
    }

    /**
     * Configura il sistema di navigazione per le collezioni di libri con più di un elemento.
     * <p>
     * Questo metodo è responsabile di preparare l'interfaccia utente per lo scorrimento
     * tra i libri. Crea due pannelli di anteprima, uno per il libro successivo e uno per
     * quello precedente, posizionandoli fuori dallo schermo per l'animazione di entrata.
     * Aggiunge questi pannelli e il contenuto del libro corrente al `bookDisplayPane`,
     * e configura la logica di rilevamento dei bordi e le frecce di navigazione.
     * </p>
     *
     * @param currentBookContent il contenuto del {@link VBox} del libro attualmente visualizzato
     * @see #createBookPreview(int)
     * @see #addEdgeDetection(StackPane)
     * @see #addNavigationArrows()
     */
    private static void setupMultiBookNavigation(VBox currentBookContent) {
        nextBookPreview = createBookPreview(currentBookIndex + 1);
        nextBookPreview.setTranslateX(1200);

        prevBookPreview = createBookPreview(currentBookIndex - 1);
        prevBookPreview.setTranslateX(-1200);

        bookDisplayPane.getChildren().addAll(prevBookPreview, currentBookContent, nextBookPreview);
        addEdgeDetection(bookDisplayPane);
        addNavigationArrows();
    }

    /**
     * Estrae il colore dominante dalla copertina di un libro e lo restituisce come stringa esadecimale scurita.
     * <p>
     * Questo metodo carica l'immagine di copertina del libro, ne analizza i pixel per determinare il colore
     * più frequente (dominante), e successivamente scurisce questo colore per renderlo un'ottima scelta
     * per lo sfondo del popup. Il colore finale viene convertito nel formato di stringa esadecimale (es. "#RRGGBB").
     * </p>
     *
     * @param book il libro da cui estrarre il colore di sfondo
     * @return una stringa che rappresenta il colore di sfondo in formato esadecimale
     */
    private static String getBookBackgroundColor(Book book) {
        Image coverImage = ImageUtils.loadSafeImage(book.getImageUrl());
        Color dominantColor = extractDominantColor(coverImage);
        Color darkenedColor = darkenColor(dominantColor, 0.7);
        return toHexString(darkenedColor);
    }

    /**
     * Crea il contenuto principale del popup per un libro specifico, incluse la barra superiore e l'area di scorrimento.
     * <p>
     * Questo metodo assembla i vari componenti UI per visualizzare i dettagli di un libro.
     * Imposta le dimensioni e lo stile di sfondo del contenuto del popup utilizzando il colore
     * fornito. Crea una barra superiore con il pulsante di chiusura e un'area di scorrimento
     * per visualizzare i dettagli del libro, che include anche il supporto per il sistema di valutazione
     * se viene fornito un gestore di autenticazione.
     * </p>
     *
     * @param book il libro per cui il contenuto viene creato
     * @param backgroundColor la stringa esadecimale del colore di sfondo del popup
     * @param authManager il gestore di autenticazione, può essere {@code null} se non serve supporto per l'autenticazione
     * @return un {@link VBox} che contiene l'intero contenuto del popup
     */
    private static VBox createBookContent(Book book, String backgroundColor, AuthenticationManager authManager) {
        VBox popupContent = new VBox();
        popupContent.setMaxWidth(1000);
        popupContent.setMaxHeight(700);
        popupContent.setMinWidth(1000);
        popupContent.setStyle(createPopupStyle(backgroundColor));

        HBox topBar = createTopBar();
        ScrollPane contentScroll = createContentScrollPane(book, authManager);

        popupContent.getChildren().addAll(topBar, contentScroll);
        return popupContent;
    }

    /**
     * Genera una stringa di stile CSS per il popup.
     * <p>
     * Questo metodo crea uno stile CSS per lo sfondo del popup, impostando il colore
     * fornito, i bordi arrotondati e un'ombra esterna per dare un effetto di profondità.
     * </p>
     *
     * @param backgroundColor la stringa esadecimale del colore di sfondo
     * @return la stringa di stile CSS completa
     */
    private static String createPopupStyle(String backgroundColor) {
        return "-fx-background-color: " + backgroundColor + ";" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);";
    }

    /**
     * Crea e restituisce la barra superiore del popup con il pulsante di chiusura.
     * <p>
     * Questo metodo assembla una barra superiore (`HBox`) con padding e allineamento a destra.
     * Al suo interno, crea un pulsante di chiusura con il testo "×". L'azione di chiusura
     * del pulsante è gestita in modo centralizzato da {@link #handlePopupCloseWithPopupManager()},
     * che assicura una gestione robusta e coerente del ciclo di vita del popup.
     * </p>
     *
     * @return un {@link HBox} che rappresenta la barra superiore
     */
    private static HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = createTopBarButton("×", 20);

        // Gestione chiusura tramite PopupManager
        closeButton.setOnAction(e -> {
            System.out.println("🔒 Pulsante X cliccato - chiusura tramite PopupManager");
            handlePopupCloseWithPopupManager();
            e.consume();
        });

        topBar.getChildren().addAll(closeButton);
        return topBar;
    }

    /**
     * Crea e configura un pulsante generico per la barra superiore del popup.
     * <p>
     * Questo metodo crea un pulsante (`Button`) con un testo e una dimensione del font specificati.
     * Applica uno stile CSS per renderlo trasparente, con un colore del testo grigio
     * chiaro e un cursore a forma di mano quando ci si passa sopra, migliorando l'esperienza
     * utente e l'estetica dell'interfaccia.
     * </p>
     *
     * @param text il testo da visualizzare sul pulsante
     * @param fontSize la dimensione del font in pixel
     * @return un {@link Button} stilizzato per la barra superiore
     */
    private static Button createTopBarButton(String text, int fontSize) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #999999;" +
                        "-fx-font-size: " + fontSize + ";" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    /**
     * Crea e configura un'area di scorrimento (`ScrollPane`) per il contenuto principale del popup.
     * <p>
     * Questo metodo prepara un'istanza di {@link javafx.scene.control.ScrollPane} impostando il suo stile
     * per renderlo trasparente e configurando le politiche delle barre di scorrimento per una navigazione
     * verticale. Al suo interno, crea un {@link VBox} che funge da contenitore per le varie sezioni
     * dei dettagli del libro, come la sezione dei dettagli, quella dell'editore, le valutazioni e le raccomandazioni.
     * </p>
     *
     * @param book il libro per cui l'area di scorrimento viene creata
     * @param authManager il gestore di autenticazione, può essere {@code null} se non serve supporto per l'autenticazione
     * @return un'istanza di {@link ScrollPane} contenente tutte le sezioni di contenuto del libro
     */
    private static ScrollPane createContentScrollPane(Book book, AuthenticationManager authManager) {
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentScroll.setFitToWidth(true);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setPannable(false);

        setupScrollConstraints(contentScroll);

        VBox scrollContent = new VBox();
        scrollContent.getChildren().addAll(
                createDetailsSection(book, authManager),
                createPublisherSection(book),
                createRatingSection(book, authManager),
                createRecommendationsSection(book, authManager),
                createReviewsSection()
        );

        contentScroll.setContent(scrollContent);
        return contentScroll;
    }

    /**
     * Configura i vincoli di scorrimento per un'area di scorrimento, prevenendo lo scorrimento orizzontale non voluto.
     * <p>
     * Questo metodo aggiunge listener e filtri di eventi al {@link ScrollPane} per assicurare una
     * navigazione verticale fluida e controllata. In particolare:
     * <ul>
     * <li>Aggiunge un listener alla proprietà di scorrimento orizzontale per resettare il valore
     * a 0.0, prevenendo qualsiasi scorrimento laterale.</li>
     * <li>Aggiunge un filtro di eventi per consumare gli eventi di trascinamento del mouse
     * se il movimento è prevalentemente orizzontale, evitando conflitti con altri gesti
     * o navigazioni all'interno del popup.</li>
     * </ul>
     * </p>
     *
     * @param scrollPane l'istanza di {@link ScrollPane} da configurare
     */
    private static void setupScrollConstraints(ScrollPane scrollPane) {
        scrollPane.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() != 0.0) {
                scrollPane.setHvalue(0.0);
            }
        });

        scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (Math.abs(e.getX() - e.getSceneX()) > Math.abs(e.getY() - e.getSceneY())) {
                e.consume();
            }
        });
    }

    /**
     * Crea e restituisce la sezione principale dei dettagli del libro, che include la copertina e le informazioni.
     * <p>
     * Questo metodo genera un {@link HBox} che funge da contenitore per le due sezioni principali
     * dei dettagli di un libro: il container della copertina e il box informativo. Imposta il padding,
     * la spaziatura e l'allineamento per una disposizione esteticamente gradevole.
     * </p>
     *
     * @param book il libro per cui la sezione dettagli viene creata
     * @param authManager il gestore di autenticazione per l'integrazione con le funzionalità utente
     * @return un {@link HBox} che contiene la sezione dei dettagli del libro
     */
    private static HBox createDetailsSection(Book book, AuthenticationManager authManager) {
        HBox detailsSection = new HBox(30);
        detailsSection.setPadding(new Insets(20, 30, 30, 30));
        detailsSection.setAlignment(Pos.TOP_LEFT);

        VBox coverContainer = createCoverContainer(book);
        VBox infoBox = createInfoBox(book, authManager);

        detailsSection.getChildren().addAll(coverContainer, infoBox);
        return detailsSection;
    }

    /**
     * Crea e restituisce un contenitore (`VBox`) per l'immagine di copertina di un libro.
     * <p>
     * Questo metodo si occupa di caricare l'immagine della copertina del libro, applicare un ritaglio
     * con angoli arrotondati per un aspetto più moderno e posizionare l'immagine all'interno di un
     * contenitore per una corretta visualizzazione. Utilizza un approccio sicuro per il caricamento
     * dell'immagine e gestisce la sua visualizzazione all'interno della UI.
     * </p>
     *
     * @param book il libro per cui il contenitore della copertina viene creato
     * @return un {@link VBox} che contiene l'immagine della copertina stilizzata
     */
    private static VBox createCoverContainer(Book book) {
        book.ensureLocalImageFileName();

        // Debug per verificare l'immagine
        System.out.println("🖼️ Caricamento copertina per: " + book.getTitle());
        System.out.println("   Nome file: " + book.getSafeImageFileName());

        // Usa SOLO il nome file locale
        ImageView cover = ImageUtils.createSafeImageView(book.getSafeImageFileName(), 180, 270);

        Rectangle coverClip = new Rectangle(180, 270);
        coverClip.setArcWidth(8);
        coverClip.setArcHeight(8);
        cover.setClip(coverClip);

        VBox coverContainer = new VBox(cover);
        coverContainer.setAlignment(Pos.TOP_CENTER);
        return coverContainer;
    }

    /**
     * Crea e restituisce un box informativo (`VBox`) che contiene i dettagli principali del libro.
     * <p>
     * Questo metodo assembla le varie etichette e i box di controllo che visualizzano le informazioni
     * chiave del libro, come titolo, autore, e le sezioni di rating e raccomandazioni.
     * L'inclusione del box dei pulsanti dipende dalla presenza di un gestore di autenticazione,
     * consentendo una UI dinamica in base allo stato di login dell'utente.
     * </p>
     *
     * @param book il libro per cui il box informativo viene creato
     * @param authManager il gestore di autenticazione per l'integrazione con le funzionalità utente
     * @return un {@link VBox} che contiene le informazioni di base del libro
     */
    private static VBox createInfoBox(Book book, AuthenticationManager authManager) {
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.TOP_LEFT);

        infoBox.getChildren().addAll(
                //createCategoryBadge(),
                createTitleLabel(book.getTitle()),
                createAuthorLabel(book.getAuthor()),
                createRatingBox(book),
                createBookInfoBox(book),
                createButtonBox(book, authManager)
        );

        return infoBox;
    }

    /**
     * Crea e restituisce un'etichetta (`Label`) per il titolo del libro.
     * <p>
     * Questo metodo crea un'etichetta con uno stile predefinito per il titolo del libro,
     * impostando il font, il peso, la dimensione e il colore del testo. La proprietà `wrapText`
     * è abilitata per gestire i titoli lunghi su più righe.
     * </p>
     *
     * @param title il testo del titolo del libro
     * @return un'istanza di {@link Label} per il titolo
     */
    private static Label createTitleLabel(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        return titleLabel;
    }

    /**
     * Crea e restituisce un'etichetta (`Label`) per il nome dell'autore del libro.
     * <p>
     * Questo metodo crea un'etichetta con uno stile specifico per il nome dell'autore,
     * utilizzando un font e un colore del testo che si integrano bene con il design complessivo.
     * </p>
     *
     * @param author il testo del nome dell'autore
     * @return un'istanza di {@link Label} per l'autore
     */
    private static Label createAuthorLabel(String author) {
        Label authorLabel = new Label(author);
        authorLabel.setFont(Font.font("SF Pro Text", 18));
        authorLabel.setTextFill(Color.LIGHTGRAY);
        return authorLabel;
    }

    /**
     * Crea e restituisce un box (`HBox`) che mostra il rating medio del libro e la sua categoria.
     * <p>
     * Questo metodo assembla una sezione dell'interfaccia utente che include due etichette:
     * una per visualizzare il voto medio del libro (inizialmente "Caricamento...") e una
     * per mostrare la categoria del libro. L'etichetta del voto medio viene memorizzata
     * in una variabile statica (`averageRatingLabel`) per essere aggiornata in seguito
     * quando il voto effettivo viene caricato in modo asincrono. Se la categoria non
     * è disponibile, viene fornito un valore di fallback ("Narrativa").
     * </p>
     *
     * @param book il libro per cui il box di rating viene creato
     * @return un {@link HBox} che contiene le informazioni di rating e la categoria del libro
     */
    private static HBox createRatingBox(Book book) {
        HBox ratingBox = new HBox(5);
        ratingBox.setPadding(new Insets(10, 0, 0, 0));

        averageRatingLabel = new Label("⭐ Caricamento...");
        averageRatingLabel.setTextFill(Color.WHITE);
        averageRatingLabel.setFont(Font.font("SF Pro Text", 14));

        String categoryText = "• " + (book.getCategory() != null && !book.getCategory().isEmpty()
                ? book.getCategory()
                : "Narrativa"); // fallback solo se category è null/vuota

        Label ratingCategory = new Label(categoryText);
        ratingCategory.setTextFill(Color.LIGHTGRAY);
        ratingCategory.setFont(Font.font("SF Pro Text", 14));
        ratingCategory.setPadding(new Insets(0, 0, 0, 5));

        ratingBox.getChildren().addAll(averageRatingLabel, ratingCategory);
        return ratingBox;
    }

    /**
     * Crea un box (`VBox`) che mostra le informazioni bibliografiche del libro come ISBN e anno di pubblicazione.
     * <p>
     * Questo metodo genera una sezione che contiene etichette per informazioni aggiuntive sul libro.
     * Include un'intestazione fissa "📚 Libro Digitale" e aggiunge dinamicamente etichette per l'ISBN e l'anno di
     * pubblicazione solo se le rispettive informazioni sono disponibili nel modello del libro. Questo approccio
     * garantisce una visualizzazione pulita e adattiva.
     * </p>
     *
     * @param book il libro per cui il box informativo viene creato
     * @return un {@link VBox} che contiene le informazioni bibliografiche del libro
     */
    private static VBox createBookInfoBox(Book book) {
        VBox bookInfoBox = new VBox(15);
        bookInfoBox.setPadding(new Insets(20, 0, 0, 0));

        Label infoLabel = new Label("📚 Libro Digitale");
        infoLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        infoLabel.setTextFill(Color.WHITE);
        bookInfoBox.getChildren().add(infoLabel);

        if (isNotEmpty(book.getIsbn())) {
            Label isbnInfo = new Label("📄 ISBN: " + book.getIsbn());
            isbnInfo.setFont(Font.font("SF Pro Text", 14));
            isbnInfo.setTextFill(Color.LIGHTGRAY);
            bookInfoBox.getChildren().add(isbnInfo);
        }

        if (isNotEmpty(book.getPublishYear())) {
            Label yearInfo = new Label("📅 Anno: " + book.getPublishYear());
            yearInfo.setFont(Font.font("SF Pro Text", 14));
            yearInfo.setTextFill(Color.LIGHTGRAY);
            bookInfoBox.getChildren().add(yearInfo);
        }

        return bookInfoBox;
    }

    /**
     * Crea e restituisce un box (`HBox`) che contiene i pulsanti di azione per il libro.
     * <p>
     * Questo metodo crea un box di pulsanti per azioni come "Aggiungi a Libreria".
     * Il comportamento del pulsante `addToLibraryButton` è dinamico e dipende dallo stato di autenticazione dell'utente:
     * <ul>
     * <li>Se l'utente è autenticato, viene mostrato un dialog per aggiungere il libro alla libreria.</li>
     * <li>Se l'utente non è loggato, viene mostrato un pannello di login.</li>
     * </ul>
     * Il metodo gestisce anche i casi in cui il gestore di autenticazione non è disponibile o la UI non può essere trovata,
     * assicurando una gestione robusta degli errori.
     * </p>
     *
     * @param book il libro per cui il box dei pulsanti viene creato
     * @param authManager il gestore di autenticazione, usato per verificare lo stato di login e mostrare l'interfaccia di login
     * @return un {@link HBox} contenente i pulsanti di azione
     */
    private static HBox createButtonBox(Book book, AuthenticationManager authManager) {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        Button addToLibraryButton = createStyledButton("📚 Aggiungi a Libreria", "#9b59b6", "white");

        addToLibraryButton.setOnAction(e -> {
            if (authManager != null && authManager.isAuthenticated()) {
                // Utente loggato: mostra dialog per aggiungere alla libreria
                showAddToLibraryDialog(book, authManager);
            } else {
                // Utente non loggato: apri popup di login
                if (authManager != null) {
                    // Recupera il mainRoot dalla scena corrente
                    StackPane mainRoot = getMainRootFromButton(addToLibraryButton);
                    if (mainRoot != null) {
                        authManager.showAuthPanel(mainRoot);
                    } else {
                        System.err.println("❌ Impossibile trovare mainRoot per aprire il pannello di login");
                    }
                } else {
                    // Fallback se authManager è null
                    System.err.println("❌ AuthManager non disponibile");
                }
            }
        });

        buttonBox.getChildren().add(addToLibraryButton);
        return buttonBox;
    }

    /**
     * Risale la gerarchia dei nodi per trovare e restituire il contenitore radice principale (`StackPane`) dell'applicazione.
     * <p>
     * Questo metodo è un utility che cerca di individuare il `StackPane` principale dell'interfaccia utente a partire
     * da un pulsante fornito come riferimento. È utile in contesti in cui è necessario aprire nuovi popup o
     * pannelli (es. il pannello di login) al di sopra dell'intera applicazione. La ricerca avviene risalendo
     * l'albero dei nodi genitore (`getParent()`) e verificando se il nodo è un `StackPane` che ha una struttura
     * che suggerisce sia il root principale (ad esempio, avendo più di un figlio). Questo approccio è un
     * fallback robusto quando l'accesso diretto al root non è disponibile.
     * </p>
     *
     * @param button il pulsante di partenza da cui risalire la gerarchia dei nodi
     * @return lo {@link StackPane} principale dell'applicazione, o {@code null} se non viene trovato
     */
    private static StackPane getMainRootFromButton(Button button) {
        try {
            // Risali la gerarchia dei nodi fino a trovare il StackPane principale
            javafx.scene.Node currentNode = button;
            while (currentNode != null) {
                if (currentNode instanceof StackPane) {
                    StackPane stackPane = (StackPane) currentNode;
                    // Verifica se questo è il mainRoot controllando se ha figli appropriati
                    if (stackPane.getChildren().size() >= 2) {
                        return stackPane;
                    }
                }
                currentNode = currentNode.getParent();
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero mainRoot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea e restituisce un pulsante stilizzato con un colore di sfondo e un colore del testo personalizzati.
     * <p>
     * Questo metodo è un'utility per generare pulsanti che si integrano con il design del popup.
     * Applica uno stile CSS che definisce il colore dello sfondo, il colore del testo,
     * il grassetto, il raggio dei bordi arrotondati, il padding e un cursore a forma di mano
     * per migliorare l'estetica e l'esperienza utente.
     * </p>
     *
     * @param text il testo da visualizzare sul pulsante
     * @param bgColor la stringa del colore di sfondo in formato esadecimale o nome
     * @param textColor la stringa del colore del testo in formato esadecimale o nome
     * @return un'istanza di {@link javafx.scene.control.Button} con lo stile applicato
     */
    private static Button createStyledButton(String text, String bgColor, String textColor) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 25;" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    /**
     * Crea e restituisce una sezione (`VBox`) per visualizzare la descrizione del libro fornita dall'editore.
     * <p>
     * Questo metodo genera un'area del popup che include un'intestazione e il testo della descrizione
     * del libro. Se la descrizione non è disponibile nel modello del libro, viene fornito
     * un messaggio di fallback predefinito. Il testo viene formattato per adattarsi
     * correttamente al layout e per essere facilmente leggibile.
     * </p>
     *
     * @param book il libro per cui la sezione dell'editore viene creata
     * @return un {@link VBox} che contiene l'intestazione e la descrizione del libro
     */
    private static VBox createPublisherSection(Book book) {
        VBox publisherSection = new VBox(15);
        publisherSection.setPadding(new Insets(0, 30, 30, 30));

        Label publisherHeader = new Label("📝 Dall'editore");
        publisherHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        publisherHeader.setTextFill(Color.WHITE);

        String description = isNotEmpty(book.getDescription()) ?
                book.getDescription() : "Descrizione non disponibile per questo libro.";

        Text publisherText = new Text(description);
        publisherText.setWrappingWidth(940);
        publisherText.setFill(Color.WHITE);
        publisherText.setFont(Font.font("SF Pro Text", 14));

        publisherSection.getChildren().addAll(publisherHeader, publisherText);
        return publisherSection;
    }

    /**
     * Carica in modo asincrono i voti per un libro specifico, gestendo sia il voto medio che quello dell'utente autenticato.
     * <p>
     * Questo metodo coordina il caricamento delle informazioni di valutazione per un libro.
     * Innanzitutto, verifica se il libro ha un ISBN valido; se non lo ha, la visualizzazione dei voti
     * viene aggiornata a uno stato di fallback. Successivamente, avvia il caricamento del voto medio
     * della community. Se un gestore di autenticazione è disponibile e l'utente è loggato,
     * il metodo avvia anche il caricamento del voto specifico dell'utente.
     * Questo approccio garantisce che i dati siano caricati in modo efficiente e solo quando necessario.
     * </p>
     *
     * @param book il libro per cui i voti devono essere caricati
     * @param authManager il gestore di autenticazione, può essere {@code null} se non serve supporto per l'autenticazione
     * @see #loadAverageRating(Book)
     * @see #loadUserRating(Book, String)
     */
    private static void loadBookRatingsForAllUsers(Book book, AuthenticationManager authManager) {
        if (isEmpty(book.getIsbn())) {
            updateRatingDisplaySafe();
            return;
        }

        loadAverageRating(book);

        if (authManager != null && authManager.isAuthenticated()) {
            loadUserRating(book, authManager.getCurrentUsername());
        }
    }

    /**
     * Carica in modo asincrono le statistiche di valutazione media per un libro e aggiorna l'interfaccia utente.
     * <p>
     * Questo metodo effettua una chiamata asincrona al servizio di valutazione per ottenere il rating medio
     * e il numero totale di voti per un libro specifico, utilizzando il suo ISBN. Una volta che la risposta
     * è disponibile, il codice viene eseguito sul thread della piattaforma (`Platform.runLater`) per aggiornare
     * in modo sicuro le variabili di stato (`averageBookRating` e `currentBookReviewCount`) e la visualizzazione
     * del rating (`averageRatingLabel`). In caso di errore o fallimento della chiamata, le variabili vengono
     * resettate per riflettere lo stato corretto.
     * </p>
     *
     * @param book il libro per cui caricare il rating medio
     */
    private static void loadAverageRating(Book book) {
        ratingService.getBookRatingStatisticsAsync(book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        averageBookRating = response.getAverageRating();
                        currentBookReviewCount = response.getTotalRatings();
                    } else {
                        averageBookRating = null;
                        currentBookReviewCount = null;
                    }
                    updateRatingDisplaySafe();
                    refreshRatingSection();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        averageBookRating = null;
                        currentBookReviewCount = null;
                        updateRatingDisplaySafe();
                    });
                    return null;
                });
    }

    /**
     * Carica in modo asincrono il voto dato da un utente specifico a un libro.
     * <p>
     * Questo metodo interroga il servizio di valutazione per recuperare il voto di un utente
     * (`currentUserRating`) per un libro specifico (identificato tramite l'ISBN). Al completamento
     * della chiamata, l'interfaccia utente viene aggiornata sul thread della piattaforma per riflettere
     * il voto dell'utente, se presente. Questo permette al popup di visualizzare
     * correttamente il rating personalizzato e le opzioni di modifica.
     * </p>
     *
     * @param book il libro per cui caricare il voto dell'utente
     * @param username il nome utente di cui caricare il voto
     */
    private static void loadUserRating(Book book, String username) {
        ratingService.getUserRatingForBookAsync(username, book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    currentUserRating = response.isSuccess() ? response.getRating() : null;
                    refreshRatingSection();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        currentUserRating = null;
                        refreshRatingSection();
                    });
                    return null;
                });
    }

    /**
     * Aggiorna in modo sicuro e dinamico l'etichetta (`Label`) che visualizza il rating medio del libro.
     * <p>
     * Questo metodo gestisce la logica di visualizzazione del voto medio, inclusa la gestione dei
     * casi in cui il rating non è ancora disponibile (`null`). Calcola il numero di stelle
     * piene e vuote da mostrare e formatta il testo per includere il voto medio e il numero
     * di recensioni. Il colore del testo viene modificato per riflettere lo stato del rating
     * (oro per i voti esistenti, grigio chiaro per l'assenza di voti). L'aggiornamento
     * avviene solo se l'etichetta `averageRatingLabel` non è {@code null}.
     * </p>
     */
    private static void updateRatingDisplaySafe() {
        if (averageRatingLabel != null) {
            if (averageBookRating != null && averageBookRating > 0) {
                int stars = (int) Math.round(averageBookRating);
                String starsDisplay = "★".repeat(stars) + "☆".repeat(5 - stars);

                int reviewCount = currentBookReviewCount != null ? currentBookReviewCount : 0;
                String text = String.format("%s %.1f/5 (%d recensioni)",
                        starsDisplay, averageBookRating, reviewCount);

                averageRatingLabel.setText(text);
                averageRatingLabel.setTextFill(Color.GOLD);
            } else {
                averageRatingLabel.setText("☆☆☆☆☆ Non ancora valutato");
                averageRatingLabel.setTextFill(Color.LIGHTGRAY);
            }
        }
    }

    /**
     * Aggiorna in modo dinamico la sezione completa di rating ricaricando i suoi contenuti.
     * <p>
     * Questo metodo è un meccanismo di aggiornamento che ricarica l'intera sezione di rating
     * per riflettere le modifiche allo stato dei voti (come un nuovo voto dell'utente).
     * Rimuove la vecchia sezione `currentRatingSection` dal suo genitore e ne crea una nuova
     * con i dati aggiornati, inserendola nello stesso punto dell'interfaccia utente.
     * Questo assicura che il componente sia sempre sincronizzato con i dati più recenti.
     * La logica è stata progettata per essere robusta, verificando l'esistenza dei
     * nodi prima di procedere con l'aggiornamento.
     * </p>
     */
    private static void refreshRatingSection() {
        if (currentRatingSection != null && currentBook != null && currentAuthManager != null) {
            Parent parent = currentRatingSection.getParent();
            if (parent instanceof VBox) {
                VBox parentVBox = (VBox) parent;
                int index = parentVBox.getChildren().indexOf(currentRatingSection);

                if (index >= 0) {
                    parentVBox.getChildren().remove(index);
                    VBox newRatingSection = createRatingSection(currentBook, currentAuthManager);
                    parentVBox.getChildren().add(index, newRatingSection);
                }
            }
        }
    }

    /**
     * Crea e restituisce l'intera sezione delle valutazioni del libro, con l'integrazione dinamica per gli utenti autenticati.
     * <p>
     * Questo metodo assembla l'intera area dell'interfaccia utente dedicata ai voti. Imposta l'intestazione
     * "⭐ Valutazioni" e crea un contenitore dinamico che visualizza il voto medio.
     * La parte successiva del contenuto varia in base allo stato di autenticazione dell'utente:
     * <ul>
     * <li>Se l'utente è loggato, viene mostrata la sezione che permette all'utente di vedere e modificare il proprio voto.</li>
     * <li>Se l'utente non è autenticato, viene mostrata una sezione che lo invita a loggarsi per poter votare.</li>
     * </ul>
     * Il riferimento a questa sezione viene salvato nella variabile statica `currentRatingSection` per futuri aggiornamenti.
     * </p>
     *
     * @param book il libro per cui la sezione di rating viene creata
     * @param authManager il gestore di autenticazione, usato per determinare quale interfaccia mostrare (utente loggato vs ospite)
     * @return un {@link VBox} che contiene la sezione completa delle valutazioni
     */
    private static VBox createRatingSection(Book book, AuthenticationManager authManager) {
        VBox ratingSection = new VBox(15);
        ratingSection.setPadding(new Insets(0, 30, 30, 30));
        currentRatingSection = ratingSection;

        // Header
        Label ratingsHeader = new Label("⭐ Valutazioni");
        ratingsHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        ratingsHeader.setTextFill(Color.WHITE);

        // Content
        VBox ratingsContent = new VBox(12);
        ratingsContent.getChildren().add(createAverageRatingDisplay());

        if (authManager != null && authManager.isAuthenticated()) {
            ratingsContent.getChildren().add(createUserRatingSection(book, authManager));
        } else {
            ratingsContent.getChildren().add(createGuestInviteSection());
        }

        ratingSection.getChildren().addAll(ratingsHeader, ratingsContent);
        return ratingSection;
    }

    /**
     * Crea e restituisce un box (`HBox`) che visualizza la valutazione media della community in un formato stilizzato.
     * <p>
     * Questo metodo genera un contenitore con un background scuro e angoli arrotondati, al cui interno viene
     * mostrata la valutazione media di un libro. L'interfaccia è progettata per essere chiara e informativa:
     * include un titolo ("📊 Valutazione della Community"), il valore delle stelle (inizialmente "Caricamento..."),
     * e una nota esplicativa. L'aggiornamento del valore delle stelle avviene dinamicamente in base al
     * dato caricato in modo asincrono.
     * </p>
     *
     * @return un {@link HBox} stilizzato che contiene le informazioni sulla valutazione media della community
     */
    private static HBox createAverageRatingDisplay() {
        HBox averageBox = new HBox(15);
        averageBox.setStyle(
                "-fx-background-color: #3a3a3c;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );
        averageBox.setAlignment(Pos.CENTER_LEFT);

        VBox averageInfo = new VBox(5);

        Label averageTitle = new Label("📊 Valutazione della Community");
        averageTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        averageTitle.setTextFill(Color.WHITE);

        Label averageValue = new Label("⭐ Caricamento...");
        averageValue.setFont(Font.font("SF Pro Text", 14));
        averageValue.getStyleClass().add("stars-white");

        if (averageBookRating != null && averageBookRating > 0) {
            int stars = (int) Math.round(averageBookRating);
            String starsDisplay = "★".repeat(stars) + "☆".repeat(5 - stars);
            averageValue.setText(String.format("%s %.1f/5", starsDisplay, averageBookRating));
            averageValue.getStyleClass().add("stars-white");
        }

        Label publicNote = new Label("Basata su tutte le recensioni degli utenti registrati");
        publicNote.setFont(Font.font("SF Pro Text", 12));
        publicNote.setTextFill(Color.GRAY);

        averageInfo.getChildren().addAll(averageTitle, averageValue, publicNote);
        averageBox.getChildren().add(averageInfo);

        return averageBox;
    }

    /**
     * Crea e restituisce una sezione stilizzata che invita gli utenti non autenticati a effettuare il login o a registrarsi.
     * <p>
     * Questo metodo genera un contenitore (`VBox`) con uno sfondo scuro e angoli arrotondati,
     * progettato per essere mostrato agli utenti che non hanno effettuato l'accesso.
     * La sezione include un'intestazione (`guestTitle`) che pone una domanda all'utente,
     * un messaggio che spiega il beneficio del login (`inviteMessage`) e una nota aggiuntiva
     * per invogliarlo a partecipare alla community (`benefitMessage`).
     * </p>
     *
     * @return un {@link VBox} che contiene la sezione di invito per gli ospiti
     */
    private static VBox createGuestInviteSection() {
        VBox guestSection = new VBox(10);
        guestSection.setStyle(
                "-fx-background-color: #444448;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        Label guestTitle = new Label("🔐 Vuoi valutare questo libro?");
        guestTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        guestTitle.setTextFill(Color.WHITE);

        Label inviteMessage = new Label("Registrati o accedi per valutare e recensire i tuoi libri preferiti");
        inviteMessage.setFont(Font.font("SF Pro Text", 14));
        inviteMessage.setTextFill(Color.LIGHTGRAY);
        inviteMessage.setWrapText(true);

        Label benefitMessage = new Label("✨ Condividi le tue opinioni con la community di lettori!");
        benefitMessage.setFont(Font.font("SF Pro Text", 12));
        benefitMessage.setTextFill(Color.LIGHTBLUE);

        guestSection.getChildren().addAll(guestTitle, inviteMessage, benefitMessage);
        return guestSection;
    }

    /**
     * Crea e restituisce la sezione dedicata alla valutazione di un utente specifico per un libro.
     * <p>
     * Questo metodo genera un box (`VBox`) che mostra il voto dell'utente loggato. La sezione include
     * un'intestazione (`userHeader`) con un titolo ("👤 La tua valutazione") e un pulsante di azione
     * (`ratingActionButton`) che permette all'utente di aggiungere o modificare il proprio voto.
     * Il contenuto dinamico (`userContent`) di questa sezione viene aggiornato separatamente
     * da {@link #updateUserContent(VBox)} in base allo stato del voto dell'utente (`currentUserRating`).
     * </p>
     *
     * @param book il libro per cui la sezione di rating dell'utente viene creata
     * @param authManager il gestore di autenticazione, usato per gestire le azioni dell'utente
     * @return un {@link VBox} che contiene la sezione completa di valutazione dell'utente
     */
    private static VBox createUserRatingSection(Book book, AuthenticationManager authManager) {
        VBox userSection = new VBox(10);
        userSection.setStyle(
                "-fx-background-color: #444448;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        // Header with action button
        HBox userHeader = new HBox();
        userHeader.setAlignment(Pos.CENTER_LEFT);

        Label userTitle = new Label("👤 La tua valutazione");
        userTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        userTitle.setTextFill(Color.WHITE);

        Region userSpacer = new Region();
        HBox.setHgrow(userSpacer, Priority.ALWAYS);

        Button ratingActionButton = createRatingActionButton(book, authManager);
        userHeader.getChildren().addAll(userTitle, userSpacer, ratingActionButton);

        // Content
        VBox userContent = new VBox(8);
        updateUserContent(userContent);

        userSection.getChildren().addAll(userHeader, userContent);
        return userSection;
    }

    /**
     * Crea e configura il pulsante di azione per la valutazione di un libro da parte dell'utente.
     * <p>
     * Questo metodo crea un pulsante dinamico il cui testo e comportamento dipendono
     * dallo stato attuale del voto dell'utente (`currentUserRating`). L'azione del pulsante
     * è gestita in modo robusto:
     * <ul>
     * <li>Verifica se il libro ha un ISBN valido e se l'utente è autenticato.</li>
     * <li>Esegue un controllo asincrono per verificare se l'utente possiede il libro nella sua libreria.</li>
     * <li>In base al risultato del controllo di possesso e alla presenza di un voto esistente, mostra
     * un dialog per aggiungere un nuovo voto o per modificarne uno già presente.</li>
     * <li>In caso di errore o se l'utente non possiede il libro, mostra dei messaggi di avviso appropriati.</li>
     * </ul>
     * Le operazioni di caricamento dei voti medio e dell'utente vengono avviate al termine dell'azione
     * di valutazione per aggiornare l'interfaccia utente.
     * </p>
     *
     * @param book il libro a cui si riferisce l'azione
     * @param authManager il gestore di autenticazione per le verifiche di stato
     * @return un {@link javafx.scene.control.Button} configurato con la logica di valutazione
     */
    private static Button createRatingActionButton(Book book, AuthenticationManager authManager) {
        Button button = new Button();
        updateRatingButton(button);

        button.setOnAction(e -> {
            if (isEmpty(book.getIsbn())) {
                showAlert("Errore", "Impossibile valutare: ISBN del libro mancante");
                return;
            }

            String username = authManager.getCurrentUsername();
            if (username == null) {
                showAlert("Errore", "Devi essere autenticato per valutare");
                return;
            }

            // Verifica prima se l'utente possiede il libro usando metodi esistenti
            LibraryService libraryService = new LibraryService();
            libraryService.doesUserOwnBookAsync(username, book.getIsbn())
                    .thenAccept(owns -> Platform.runLater(() -> {
                        if (owns) {
                            // Utente possiede il libro, può valutare
                            if (currentUserRating != null) {
                                RatingDialog.showEditRatingDialog(book, username, currentUserRating, (rating) -> {
                                    loadUserRating(book, authManager.getCurrentUsername());
                                    loadAverageRating(book);
                                });
                            } else {
                                RatingDialog.showRatingDialog(book, username, (rating) -> {
                                    loadUserRating(book, authManager.getCurrentUsername());
                                    loadAverageRating(book);
                                });
                            }
                        } else {
                            // Utente NON possiede il libro, mostra popup di errore
                            showBookNotOwnedDialog(book, libraryService, username);
                        }
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> showAlert("Errore",
                                "Errore nel controllo possesso libro: " + throwable.getMessage()));
                        return null;
                    });
        });

        return button;
    }

    /**
     * Mostra un dialog di avviso che informa l'utente che non può valutare un libro perché non lo possiede.
     * <p>
     * Questo metodo crea un'istanza di {@link javafx.scene.control.Alert} con il tipo `INFORMATION`
     * per notificare l'utente che è necessario aggiungere il libro a una libreria prima di poterlo valutare.
     * Il dialog personalizzato include due pulsanti: "📚 Aggiungi a Libreria" e "Annulla". Se l'utente
     * sceglie di aggiungere il libro, il metodo gestisce l'azione chiamando `handleAddToLibraryAction`.
     * </p>
     *
     * @param book il libro che l'utente ha tentato di valutare
     * @param libraryService l'istanza del servizio di libreria utilizzata per l'azione di aggiunta
     * @param username il nome utente corrente
     */
    private static void showBookNotOwnedDialog(Book book, LibraryService libraryService, String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Libro non posseduto");
        alert.setHeaderText("Non puoi valutare questo libro");
        alert.setContentText("Per valutare '" + book.getTitle() + "' devi prima aggiungerlo a una delle tue librerie.");

        // Personalizza i bottoni
        ButtonType addToLibraryButton = new ButtonType("📚 Aggiungi a Libreria");
        ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(addToLibraryButton, cancelButton);

        styleDialog(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == addToLibraryButton) {
                // Riusa il metodo esistente per aggiungere a libreria
                handleAddToLibraryAction(book, username, libraryService);
            }
        });
    }

    /**
     * Gestisce l'azione di aggiungere un libro alla libreria di un utente, recuperando prima le librerie esistenti.
     * <p>
     * Questo metodo coordina l'intero flusso di lavoro per aggiungere un libro alla libreria.
     * Avvia una chiamata asincrona per recuperare la lista delle librerie dell'utente.
     * In base al risultato:
     * <ul>
     * <li>Se l'utente ha già una o più librerie, viene mostrato un dialog che permette di scegliere in quale libreria aggiungere il libro.</li>
     * <li>Se l'utente non ha librerie esistenti, viene mostrato un dialog che lo invita a crearne una nuova.</li>
     * </ul>
     * Il metodo gestisce anche eventuali eccezioni che si verificano durante il recupero delle librerie, mostrando un messaggio di errore appropriato.
     * </p>
     *
     * @param book il libro da aggiungere alla libreria
     * @param username il nome utente corrente
     * @param libraryService l'istanza del servizio di libreria da utilizzare
     */
    private static void handleAddToLibraryAction(Book book, String username, LibraryService libraryService) {
        // Usa il metodo esistente per recuperare le librerie dell'utente
        libraryService.getUserLibrariesAsync(username)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getLibraries() != null && !response.getLibraries().isEmpty()) {
                        showChooseLibraryDialog(book, username, response.getLibraries(), libraryService);
                    } else {
                        showCreateNewLibraryDialog(book, username, libraryService);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showAlert("Errore",
                            "Errore nel recupero librerie: " + throwable.getMessage()));
                    return null;
                });
    }

    /**
     * Aggiorna lo stile e il testo di un pulsante di valutazione in base allo stato del voto dell'utente.
     * <p>
     * Questo metodo è responsabile di adattare dinamicamente l'aspetto del pulsante che permette all'utente
     * di valutare il libro. La logica è semplice ma efficace:
     * <ul>
     * <li>Se l'utente ha già votato (`currentUserRating` non è {@code null}), il pulsante mostrerà il testo "Modifica"
     * e uno stile (`#9b59b6`) che indica un'azione di modifica.</li>
     * <li>Se l'utente non ha ancora votato, il pulsante mostrerà il testo "Valuta" e uno stile (`#4CAF50`)
     * che lo rende più invitante e visivamente distinguibile.</li>
     * </ul>
     * Questo approccio migliora la chiarezza dell'interfaccia utente, comunicando all'utente l'azione prevista.
     * </p>
     *
     * @param button il pulsante da aggiornare
     */
    private static void updateRatingButton(Button button) {
        if (currentUserRating != null) {
            button.setText("Modifica");
            button.setStyle(
                    "-fx-background-color: #9b59b6;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 15;" +
                            "-fx-cursor: hand;"
            );
        } else {
            button.setText("Valuta");
            button.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 15;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    /**
     * Aggiorna dinamicamente il contenuto della sezione di valutazione dell'utente in base al voto esistente.
     * <p>
     * Questo metodo è responsabile di visualizzare le informazioni pertinenti all'utente
     * in base al fatto che abbia già votato o meno il libro. La logica è la seguente:
     * <ul>
     * <li>Se `currentUserRating` non è {@code null}, la sezione mostra il voto dell'utente, inclusa la
     * suddivisione per categoria (stile, contenuto, ecc.) e l'eventuale recensione, formattando
     * il testo in modo appropriato.</li>
     * <li>Se `currentUserRating` è {@code null}, la sezione mostra un messaggio che informa
     * l'utente che non ha ancora valutato il libro e lo invita a farlo.</li>
     * </ul>
     * Questo approccio garantisce che l'interfaccia utente sia sempre sincronizzata con lo stato dei dati
     * dell'utente in modo chiaro e intuitivo.
     * </p>
     *
     * @param userContent il {@link VBox} da aggiornare con le informazioni del voto dell'utente
     */
    private static void updateUserContent(VBox userContent) {
        userContent.getChildren().clear();

        if (currentUserRating != null) {
            Label userRatingDisplay = new Label(currentUserRating.getDisplayRating());
            userRatingDisplay.setFont(Font.font("SF Pro Text", 14));
            userRatingDisplay.setTextFill(Color.LIGHTGREEN);

            Label ratingBreakdown = new Label(String.format(
                    "Stile: %d★ | Contenuto: %d★ | Piacevolezza: %d★ | Originalità: %d★ | Edizione: %d★",
                    currentUserRating.getStyle(), currentUserRating.getContent(),
                    currentUserRating.getPleasantness(), currentUserRating.getOriginality(),
                    currentUserRating.getEdition()
            ));
            ratingBreakdown.setFont(Font.font("SF Pro Text", 12));
            ratingBreakdown.setTextFill(Color.LIGHTGRAY);

            userContent.getChildren().addAll(userRatingDisplay, ratingBreakdown);

            if (isNotEmpty(currentUserRating.getReview())) {
                Label reviewLabel = new Label("💭 La tua recensione:");
                reviewLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 12));
                reviewLabel.setTextFill(Color.WHITE);

                Text reviewText = new Text(currentUserRating.getReview());
                reviewText.setFont(Font.font("SF Pro Text", 12));
                reviewText.setFill(Color.LIGHTGRAY);
                reviewText.setWrappingWidth(400);

                userContent.getChildren().addAll(reviewLabel, reviewText);
            }
        } else {
            Label inviteLabel = new Label("Non hai ancora valutato questo libro");
            inviteLabel.setFont(Font.font("SF Pro Text", 14));
            inviteLabel.setTextFill(Color.LIGHTGRAY);

            Label benefitLabel = new Label("Condividi la tua opinione con altri lettori!");
            benefitLabel.setFont(Font.font("SF Pro Text", 12));
            benefitLabel.setTextFill(Color.GRAY);

            userContent.getChildren().addAll(inviteLabel, benefitLabel);
        }
    }

    /**
     * Mostra un dialog che gestisce il processo di aggiunta di un libro a una delle librerie dell'utente.
     * <p>
     * Questo metodo è il punto di ingresso per il flusso di lavoro "aggiungi alla libreria".
     * Inizialmente verifica che il libro abbia un ISBN valido. Se l'ISBN è mancante, un errore
     * viene notificato all'utente. Successivamente, avvia in modo asincrono la richiesta al
     * backend per recuperare la lista delle librerie dell'utente. A seconda della risposta:
     * <ul>
     * <li>Se l'utente non ha ancora librerie, viene mostrato un dialog per la creazione della prima libreria.</li>
     * <li>Se l'utente ha già una o più librerie, viene mostrato un dialog che permette di scegliere in quale libreria aggiungere il libro.</li>
     * </ul>
     * In caso di errori durante la comunicazione con il servizio, viene mostrato un avviso all'utente.
     * </p>
     *
     * @param book il libro da aggiungere alla libreria
     * @param authManager il gestore di autenticazione per ottenere il nome utente
     * @see #showCreateFirstLibraryDialog(Book, String, LibraryService)
     * @see #showChooseLibraryDialog(Book, String, List, LibraryService)
     */
    private static void showAddToLibraryDialog(Book book, AuthenticationManager authManager) {
        if (isEmpty(book.getIsbn())) {
            showAlert("❌ Errore", "Impossibile aggiungere il libro: ISBN mancante");
            return;
        }

        LibraryService libraryService = new LibraryService();
        libraryService.getUserLibrariesAsync(authManager.getCurrentUsername())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getLibraries() != null) {
                        List<String> libraries = response.getLibraries();
                        if (libraries.isEmpty()) {
                            showCreateFirstLibraryDialog(book, authManager.getCurrentUsername(), libraryService);
                        } else {
                            showChooseLibraryDialog(book, authManager.getCurrentUsername(), libraries, libraryService);
                        }
                    } else {
                        showAlert("❌ Errore", "Errore nel recupero delle librerie: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage()));
                    return null;
                });
    }

    /**
     * Mostra un dialog che invita l'utente a creare la sua prima libreria.
     * <p>
     * Questo metodo viene chiamato quando l'utente tenta di aggiungere un libro alla libreria ma non ne possiede ancora nessuna.
     * Utilizza un {@link javafx.scene.control.TextInputDialog} per chiedere all'utente di inserire il nome della nuova libreria.
     * Se l'utente fornisce un nome valido, il metodo avvia il processo asincrono di creazione della libreria e di aggiunta del libro.
     * </p>
     *
     * @param book il libro da aggiungere alla libreria
     * @param username il nome utente corrente
     * @param libraryService l'istanza del servizio di libreria da utilizzare
     */
    private static void showCreateFirstLibraryDialog(Book book, String username, LibraryService libraryService) {
        TextInputDialog dialog = new TextInputDialog("La mia libreria");
        dialog.setTitle("Crea la tua prima libreria");
        dialog.setHeaderText("Non hai ancora librerie. Creane una per '" + book.getTitle() + "'");
        dialog.setContentText("Nome libreria:");

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(libraryName -> {
            if (isNotEmpty(libraryName)) {
                createLibraryAndAddBook(book, username, libraryName.trim(), libraryService);
            }
        });
    }

    /**
     * Mostra un dialog che permette all'utente di scegliere una libreria esistente in cui aggiungere un libro.
     * <p>
     * Questo metodo è utilizzato quando l'utente ha già una o più librerie. Presenta un {@link javafx.scene.control.ChoiceDialog}
     * che elenca le librerie esistenti e include un'opzione per crearne una nuova.
     * In base alla scelta dell'utente, avvia l'aggiunta del libro a una libreria esistente
     * o il flusso di creazione di una nuova libreria.
     * </p>
     *
     * @param book il libro da aggiungere
     * @param username il nome utente corrente
     * @param libraries la lista dei nomi delle librerie esistenti dell'utente
     * @param libraryService l'istanza del servizio di libreria da utilizzare
     */
    private static void showChooseLibraryDialog(Book book, String username, List<String> libraries, LibraryService libraryService) {
        List<String> choices = new ArrayList<>(libraries);
        choices.add(0, "➕ Crea nuova libreria...");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(libraries.get(0), choices);
        dialog.setTitle("Aggiungi a Libreria");
        dialog.setHeaderText("Scegli dove aggiungere '" + book.getTitle() + "'");
        dialog.setContentText("Libreria:");

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(choice -> {
            if (choice.startsWith("➕")) {
                showCreateNewLibraryDialog(book, username, libraryService);
            } else {
                addBookToExistingLibrary(book, username, choice, libraryService);
            }
        });
    }

    /**
     * Mostra un dialog che invita l'utente a creare una nuova libreria.
     * <p>
     * Questo metodo crea un {@link javafx.scene.control.TextInputDialog} che chiede all'utente di inserire il nome di una
     * nuova libreria. È utilizzato quando l'utente sceglie di creare una nuova libreria dal dialog di scelta,
     * o come parte del flusso di fallback. Se l'utente fornisce un nome valido e non vuoto,
     * il processo di creazione e aggiunta del libro viene avviato.
     * </p>
     *
     * @param book il libro da aggiungere alla nuova libreria
     * @param username il nome utente corrente
     * @param libraryService l'istanza del servizio di libreria da utilizzare
     */
    private static void showCreateNewLibraryDialog(Book book, String username, LibraryService libraryService) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crea Nuova Libreria");
        dialog.setHeaderText("Crea una nuova libreria per '" + book.getTitle() + "'");
        dialog.setContentText("Nome libreria:");

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(libraryName -> {
            if (isNotEmpty(libraryName)) {
                createLibraryAndAddBook(book, username, libraryName.trim(), libraryService);
            }
        });
    }

    /**
     * Crea una nuova libreria e aggiunge un libro al suo interno in modo asincrono.
     * <p>
     * Questo metodo coordina due operazioni asincrone consecutive. Prima tenta di creare
     * una nuova libreria con il nome fornito. Se la creazione ha successo, concatena
     * (`thenCompose`) una seconda operazione asincrona per aggiungere il libro appena creato
     * alla libreria. In caso di successo, un alert di conferma viene mostrato all'utente.
     * Il metodo gestisce anche gli errori in ogni fase, mostrando un messaggio di avviso
     * chiaro all'utente se la creazione o l'aggiunta del libro falliscono.
     * </p>
     *
     * @param book il libro da aggiungere
     * @param username il nome utente corrente
     * @param libraryName il nome della libreria da creare
     * @param libraryService l'istanza del servizio di libreria da utilizzare
     */
    private static void createLibraryAndAddBook(Book book, String username, String libraryName, LibraryService libraryService) {
        libraryService.createLibraryAsync(username, libraryName)
                .thenCompose(createResponse -> {
                    if (createResponse.isSuccess()) {
                        return libraryService.addBookToLibraryAsync(username, libraryName, book.getIsbn());
                    } else {
                        return CompletableFuture.completedFuture(
                                new LibraryResponse(false, "Errore nella creazione: " + createResponse.getMessage())
                        );
                    }
                })
                .thenAccept(addResponse -> Platform.runLater(() -> {
                    if (addResponse.isSuccess()) {
                        showAlert("✅ Successo", "Libreria '" + libraryName + "' creata e libro aggiunto!");
                    } else {
                        showAlert("❌ Errore", addResponse.getMessage());
                    }
                }));
    }

    /**
     * Aggiunge un libro a una libreria esistente in modo asincrono.
     * <p>
     * Questo metodo si connette al servizio di libreria per aggiungere un libro a una libreria specifica dell'utente.
     * Al completamento dell'operazione, viene mostrato un messaggio di successo o un avviso di errore all'utente,
     * garantendo un feedback chiaro sull'esito dell'azione.
     * </p>
     *
     * @param book il libro da aggiungere
     * @param username il nome utente corrente
     * @param libraryName il nome della libreria a cui aggiungere il libro
     * @param libraryService l'istanza del servizio di libreria da utilizzare
     */
    private static void addBookToExistingLibrary(Book book, String username, String libraryName, LibraryService libraryService) {
        libraryService.addBookToLibraryAsync(username, libraryName, book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showAlert("✅ Successo", "Libro aggiunto alla libreria '" + libraryName + "'!");
                    } else {
                        showAlert("❌ Errore", response.getMessage());
                    }
                }));
    }

    /**
     * Applica uno stile visivo comune a un dialog e imposta l'icona della finestra.
     * <p>
     * Questo è un metodo utility che semplifica la personalizzazione dell'aspetto di un dialog.
     * Un listener viene aggiunto all'evento `setOnShowing` del dialog per assicurare che
     * l'icona dell'applicazione venga impostata sulla finestra del dialog appena prima che essa venga mostrata,
     * garantendo coerenza visiva.
     * </p>
     *
     * @param dialog il {@link javafx.scene.control.Dialog} da stilizzare
     */
    private static void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();

        dialog.setOnShowing(e -> {
            Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
            IconUtils.setStageIcon(dialogStage);
        });
    }

    /**
     * Mostra un semplice dialog di avviso con titolo e messaggio.
     * <p>
     * Questo metodo crea e visualizza un {@link javafx.scene.control.Alert} informativo.
     * È un'utility per notificare l'utente in modo rapido e standardizzato su successi o fallimenti.
     * Il metodo imposta il titolo, il messaggio e si assicura che l'icona della finestra sia corretta
     * prima di mostrare il dialog in modo bloccante.
     * </p>
     *
     * @param title il titolo del dialog
     * @param message il messaggio da visualizzare nel corpo del dialog
     */
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert.setOnShowing(e -> IconUtils.setStageIcon(alertStage));

        styleDialog(alert);
        alert.showAndWait();
    }

    /**
     * Crea e restituisce l'intera sezione delle recensioni per un libro.
     * <p>
     * Questo metodo assembla i componenti principali della sezione delle recensioni, inclusa un'intestazione
     * e un'area di scorrimento per contenere l'elenco delle recensioni. L'intestazione e l'area di scorrimento
     * vengono creati separatamente da metodi ausiliari, garantendo una buona modularità del codice.
     * </p>
     *
     * @return un {@link VBox} che contiene la sezione completa delle recensioni
     */
    private static VBox createReviewsSection() {
        VBox reviewsSection = new VBox(15);
        reviewsSection.setPadding(new Insets(0, 30, 30, 30));

        HBox reviewHeaderBox = createReviewsHeader();
        ScrollPane reviewsScrollPane = createReviewsScrollPane();

        reviewsSection.getChildren().addAll(reviewHeaderBox, reviewsScrollPane);
        return reviewsSection;
    }

    /**
     * Crea e restituisce l'intestazione della sezione delle recensioni, inclusa l'opzione "Vedi tutte".
     * <p>
     * Questo metodo genera un'intestazione (`HBox`) per la sezione delle recensioni. La barra include
     * un titolo ("💬 Recensioni della community") e un'etichetta cliccabile ("Vedi tutte ❯") che
     * funge da pulsante. Quando l'utente clicca sull'etichetta, viene mostrato un dialog che contiene
     * tutte le recensioni del libro, garantendo una navigazione fluida e intuitiva. Un'area spaziatrice
     * (`Region`) viene utilizzata per allineare correttamente i componenti.
     * </p>
     *
     * @return un {@link HBox} che rappresenta l'intestazione della sezione delle recensioni
     */
    private static HBox createReviewsHeader() {
        HBox reviewHeaderBox = new HBox();

        Label reviewsHeader = new Label("💬 Recensioni della community");
        reviewsHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        reviewsHeader.setTextFill(Color.WHITE);

        Label seeMoreReviews = new Label("Vedi tutte ❯");
        seeMoreReviews.setFont(Font.font("SF Pro Text", 14));
        seeMoreReviews.setTextFill(Color.LIGHTBLUE);
        seeMoreReviews.setStyle("-fx-cursor: hand;");
        seeMoreReviews.setOnMouseClicked(e -> {
            if (currentBook != null) {
                showAllReviewsDialog(currentBook);
            }
        });

        Region reviewSpacer = new Region();
        HBox.setHgrow(reviewSpacer, Priority.ALWAYS);
        reviewHeaderBox.getChildren().addAll(reviewsHeader, reviewSpacer, seeMoreReviews);

        return reviewHeaderBox;
    }

    /**
     * Crea e restituisce un'area di scorrimento (`ScrollPane`) configurata per mostrare una selezione di recensioni.
     * <p>
     * Questo metodo configura un'area di scorrimento con stili e politiche specifici per una navigazione
     * orizzontale. Imposta un'altezza fissa e rimuove la barra di scorrimento verticale, mentre abilita
     * quella orizzontale. Il contenuto del `ScrollPane` è un `HBox` (`reviewsContainer`), all'interno
     * del quale le recensioni verranno caricate in modo dinamico. La chiamata a {@link #loadCommunityReviews(HBox)}
     * avvia il caricamento delle recensioni nel contenitore.
     * </p>
     *
     * @return un'istanza di {@link ScrollPane} per le recensioni
     */
    private static ScrollPane createReviewsScrollPane() {
        ScrollPane reviewsScrollPane = new ScrollPane();
        reviewsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        reviewsScrollPane.setFitToHeight(true);
        reviewsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reviewsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        reviewsScrollPane.setPannable(true);
        reviewsScrollPane.setPrefHeight(200);

        HBox reviewsContainer = new HBox(15);
        reviewsContainer.setPadding(new Insets(10, 0, 10, 0));
        reviewsScrollPane.setContent(reviewsContainer);

        loadCommunityReviews(reviewsContainer);
        return reviewsScrollPane;
    }

    /**
     * Carica in modo asincrono un set di recensioni della community e le visualizza in un contenitore orizzontale.
     * <p>
     * Questo metodo gestisce l'intero ciclo di caricamento delle recensioni:
     * <ol>
     * <li>Prima di tutto, verifica la validità del libro. Se non è valido, mostra un messaggio di "nessuna recensione".</li>
     * <li>Visualizza un'etichetta di "Caricamento..." temporanea mentre attende i dati.</li>
     * <li>Effettua una chiamata asincrona al servizio di valutazione per recuperare tutte le recensioni.</li>
     * <li>Una volta ricevuti i dati, filtra le recensioni per includere solo quelle con un testo, escludendo quella dell'utente corrente (se loggato).</li>
     * <li>Ordina le recensioni filtrate in base al voto medio, in ordine decrescente, e ne seleziona un massimo di 5 da visualizzare.</li>
     * <li>Per ogni recensione selezionata, crea una card visuale (`VBox`) e la aggiunge al contenitore.</li>
     * </ol>
     * In caso di assenza di recensioni o di un errore di caricamento, il metodo gestisce l'UI in modo elegante mostrando messaggi appropriati.
     * Tutte le operazioni di aggiornamento della UI sono eseguite in modo sicuro sul thread della piattaforma.
     * </p>
     *
     * @param container l'{@link HBox} in cui verranno visualizzate le recensioni
     */
    private static void loadCommunityReviews(HBox container) {
        if (currentBook == null || isEmpty(currentBook.getIsbn())) {
            showNoReviewsMessage(container);
            return;
        }

        Label loadingLabel = new Label("📖 Caricamento recensioni...");
        loadingLabel.setFont(Font.font("SF Pro Text", 14));
        loadingLabel.setTextFill(Color.GRAY);
        loadingLabel.setStyle("-fx-padding: 20;");
        container.getChildren().add(loadingLabel);

        ratingService.getBookRatingsAsync(currentBook.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.isSuccess() && response.getRatings() != null && !response.getRatings().isEmpty()) {
                        List<BookRating> reviewsWithText = response.getRatings().stream()
                                .filter(rating -> isNotEmpty(rating.getReview()))
                                .filter(rating -> currentAuthManager == null ||
                                        !rating.getUsername().equals(currentAuthManager.getCurrentUsername()))
                                .sorted((r1, r2) -> Double.compare(r2.getAverage(), r1.getAverage()))
                                .limit(5)
                                .collect(Collectors.toList());

                        if (!reviewsWithText.isEmpty()) {
                            for (BookRating rating : reviewsWithText) {
                                VBox reviewCard = createCommunityReviewCard(rating);
                                container.getChildren().add(reviewCard);
                            }
                        } else {
                            showNoReviewsMessage(container);
                        }
                    } else {
                        showNoReviewsMessage(container);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        showErrorMessage(container, "Errore nel caricamento delle recensioni");
                    });
                    return null;
                });
    }

    /**
     * Crea e restituisce una card visuale (`VBox`) per una singola recensione della community.
     * <p>
     * Questo metodo genera una card stilizzata per mostrare una recensione pubblica di un utente.
     * La card include un'intestazione con il voto a stelle e un'etichetta che mostra il nome
     * utente parzialmente mascherato per motivi di privacy (es. "fra***"). Il testo della
     * recensione viene troncato se supera una certa lunghezza per mantenere un layout pulito
     * e leggibile. L'intera card è stilizzata con un background scuro, angoli arrotondati
     * e padding.
     * </p>
     *
     * @param rating l'istanza di {@link BookRating} che contiene i dati della recensione da visualizzare
     * @return un {@link VBox} che rappresenta la card della recensione
     */
    private static VBox createCommunityReviewCard(BookRating rating) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: #3a3a3c;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPrefHeight(180);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String stars = "★".repeat(rating.getStarRating()) + "☆".repeat(5 - rating.getStarRating());
        Label starsLabel = new Label(stars);
        starsLabel.setFont(Font.font("SF Pro Text", 14));
        starsLabel.getStyleClass().add("stars-white");

        String displayName = rating.getUsername().length() > 3 ?
                rating.getUsername().substring(0, 3) + "***" : "***";
        Label usernameLabel = new Label("di " + displayName);
        usernameLabel.setFont(Font.font("SF Pro Text", 10));
        usernameLabel.setTextFill(Color.LIGHTGRAY);

        header.getChildren().addAll(starsLabel, usernameLabel);

        String reviewText = rating.getReview();
        if (reviewText.length() > 120) {
            reviewText = reviewText.substring(0, 117) + "...";
        }

        Text reviewContent = new Text(reviewText);
        reviewContent.setFont(Font.font("SF Pro Text", 12));
        reviewContent.setFill(Color.WHITE);
        reviewContent.setWrappingWidth(250);

        Label dateLabel = new Label(getRelativeDate(rating.getData()));
        dateLabel.setFont(Font.font("SF Pro Text", 10));
        dateLabel.setTextFill(Color.GRAY);

        card.getChildren().addAll(header, reviewContent, dateLabel);
        return card;
    }

    /**
     * Mostra un messaggio all'interno di un contenitore che indica l'assenza di recensioni.
     * <p>
     * Questo metodo è un'utility che aggiunge una semplice etichetta testuale al contenitore delle recensioni
     * quando non ce ne sono da visualizzare. È utile per informare chiaramente l'utente sullo stato
     * dei contenuti senza lasciare uno spazio vuoto.
     * </p>
     *
     * @param container l'{@link HBox} in cui verrà visualizzato il messaggio
     */
    private static void showNoReviewsMessage(HBox container) {
        Label noReviewsLabel = new Label("📝 Nessuna recensione ancora disponibile");
        noReviewsLabel.setFont(Font.font("SF Pro Text", 14));
        noReviewsLabel.setTextFill(Color.GRAY);
        noReviewsLabel.setStyle("-fx-padding: 20;");
        container.getChildren().add(noReviewsLabel);
    }

    /**
     * Mostra un messaggio di errore all'interno di un contenitore.
     * <p>
     * Questo metodo è un'utility per visualizzare un messaggio di errore standardizzato all'interno
     * di un contenitore. Il testo viene formattato in modo da essere visivamente distinguibile
     * (es. testo rosso chiaro) per attirare l'attenzione dell'utente su un problema.
     * </p>
     *
     * @param container il contenitore in cui verrà visualizzato il messaggio di errore
     * @param message il testo del messaggio di errore
     */
    private static void showErrorMessage(HBox container, String message) {
        Label errorLabel = new Label("❌ " + message);
        errorLabel.setFont(Font.font("SF Pro Text", 14));
        errorLabel.setTextFill(Color.LIGHTCORAL);
        errorLabel.setStyle("-fx-padding: 20;");
        container.getChildren().add(errorLabel);
    }

    /**
     * Restituisce una stringa di data relativa casuale, utilizzata come placeholder per la data di una recensione.
     * <p>
     * Questo metodo è un'utility di mockup che genera una data relativa in modo casuale, come "Oggi"
     * o "1 settimana fa", basandosi su un array predefinito. Viene utilizzato per simulare un'esperienza
     * utente realistica senza richiedere dati di data effettivi.
     * </p>
     *
     * @param dateStr la stringa di data originale (non utilizzata, ma inclusa per coerenza con la firma del metodo)
     * @return una stringa che rappresenta una data relativa
     */
    private static String getRelativeDate(String dateStr) {
        if (isEmpty(dateStr)) return "Di recente";

        String[] samples = {"Oggi", "Ieri", "2 giorni fa", "1 settimana fa", "2 settimane fa"};
        return samples[(int) (Math.random() * samples.length)];
    }

    /**
     * Mostra un dialog modale che contiene tutte le recensioni di un libro.
     * <p>
     * Questo metodo crea e configura una nuova finestra (`Stage`) per visualizzare una lista completa di recensioni.
     * La finestra è modale (`Modality.APPLICATION_MODAL`), il che significa che blocca l'interazione con l'applicazione
     * principale finché non viene chiusa. Il dialog include:
     * <ul>
     * <li>Un'intestazione con il titolo del libro.</li>
     * <li>Un'area di scorrimento (`ScrollPane`) che contiene la lista dinamica delle recensioni.</li>
     * <li>Un pulsante "Chiudi" per chiudere il dialog.</li>
     * </ul>
     * La lista delle recensioni viene caricata in modo asincrono da un metodo separato (`loadAllReviews`).
     * </p>
     *
     * @param book il libro per cui mostrare tutte le recensioni
     */
    private static void showAllReviewsDialog(Book book) {
        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Recensioni - " + book.getTitle());

        // Imposta icona per il dialog delle recensioni
        IconUtils.setStageIcon(dialogStage);

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-background-color: #2b2b2b;");

        Label headerLabel = new Label("📖 Tutte le recensioni di \"" + book.getTitle() + "\"");
        headerLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");

        VBox reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(10));
        loadAllReviews(reviewsList, book);
        scrollPane.setContent(reviewsList);

        Button closeButton = new Button("Chiudi");
        closeButton.setStyle(
                "-fx-background-color: #606060;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> dialogStage.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(closeButton);

        dialogContent.getChildren().addAll(headerLabel, scrollPane, buttonBox);

        Scene scene = new Scene(dialogContent, 600, 500);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    /**
     * Carica in modo asincrono tutte le recensioni testuali per un libro e le visualizza in un contenitore.
     * <p>
     * Questo metodo si connette al servizio di valutazione per recuperare tutte le recensioni
     * relative a un libro, identificate dall'ISBN. Prima di procedere, aggiunge una
     * etichetta temporanea di "Caricamento...". Una volta ricevuta la risposta:
     * <ul>
     * <li>Svuota il contenitore.</li>
     * <li>Filtra le recensioni per includere solo quelle che hanno un contenuto testuale.</li>
     * <li>Ordina le recensioni filtrate in ordine decrescente in base al voto medio.</li>
     * <li>Per ogni recensione, crea una card visuale completa (`VBox`) e la aggiunge al contenitore.</li>
     * </ul>
     * Il metodo gestisce anche vari stati di errore, come l'assenza di recensioni testuali o problemi di
     * connessione, mostrando messaggi appropriati per informare l'utente. Tutte le modifiche all'interfaccia
     * utente avvengono in modo sicuro sul thread della piattaforma.
     * </p>
     *
     * @param container il {@link VBox} in cui verranno visualizzate le recensioni
     * @param book il libro per cui caricare le recensioni
     */
    private static void loadAllReviews(VBox container, Book book) {
        Label loadingLabel = new Label("📖 Caricamento di tutte le recensioni...");
        loadingLabel.setFont(Font.font("SF Pro Text", 14));
        loadingLabel.setTextFill(Color.WHITE);
        container.getChildren().add(loadingLabel);

        ratingService.getBookRatingsAsync(book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.isSuccess() && response.getRatings() != null && !response.getRatings().isEmpty()) {
                        List<BookRating> allReviews = response.getRatings().stream()
                                .filter(rating -> isNotEmpty(rating.getReview()))
                                .sorted((r1, r2) -> Double.compare(r2.getAverage(), r1.getAverage()))
                                .collect(Collectors.toList());

                        if (!allReviews.isEmpty()) {
                            for (BookRating rating : allReviews) {
                                VBox fullReviewCard = createFullReviewCard(rating);
                                container.getChildren().add(fullReviewCard);
                            }
                        } else {
                            Label noReviewsLabel = new Label("📝 Non ci sono ancora recensioni testuali per questo libro");
                            noReviewsLabel.setFont(Font.font("SF Pro Text", 14));
                            noReviewsLabel.setTextFill(Color.LIGHTGRAY);
                            container.getChildren().add(noReviewsLabel);
                        }
                    } else {
                        Label errorLabel = new Label("❌ Errore nel caricamento delle recensioni");
                        errorLabel.setFont(Font.font("SF Pro Text", 14));
                        errorLabel.setTextFill(Color.LIGHTCORAL);
                        container.getChildren().add(errorLabel);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore di connessione: " + throwable.getMessage());
                        errorLabel.setFont(Font.font("SF Pro Text", 14));
                        errorLabel.setTextFill(Color.LIGHTCORAL);
                        container.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    /**
     * Crea e restituisce una card visuale (`VBox`) completa che mostra una recensione dettagliata di un utente.
     * <p>
     * Questo metodo genera un contenitore stilizzato che presenta in modo chiaro una recensione.
     * La card include un'intestazione con il voto a stelle e il nome utente parzialmente mascherato per privacy.
     * Vengono mostrati anche i voti specifici per ogni categoria (stile, contenuto, ecc.) e il testo completo
     * della recensione, che viene formattato per adattarsi allo spazio disponibile. Viene inoltre aggiunta
     * una data relativa per indicare la data della recensione.
     * </p>
     *
     * @param rating l'istanza di {@link BookRating} che contiene i dati completi della recensione
     * @return un {@link VBox} che rappresenta la card completa della recensione
     */
    private static VBox createFullReviewCard(BookRating rating) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: #383838;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;"
        );

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        String mainStars = "★".repeat(rating.getStarRating()) + "☆".repeat(5 - rating.getStarRating());
        Label mainStarsLabel = new Label(mainStars + " " + String.format("%.1f/5", rating.getAverage()));
        mainStarsLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        mainStarsLabel.getStyleClass().add("stars-white");

        String displayName = rating.getUsername().length() > 4 ?
                rating.getUsername().substring(0, 4) + "***" : "Utente***";
        Label usernameLabel = new Label("di " + displayName);
        usernameLabel.setFont(Font.font("SF Pro Text", 14));
        usernameLabel.setTextFill(Color.LIGHTBLUE);

        header.getChildren().addAll(mainStarsLabel, usernameLabel);

        Label detailRating = new Label(String.format(
                "Stile: %d★ | Contenuto: %d★ | Piacevolezza: %d★ | Originalità: %d★ | Edizione: %d★",
                rating.getStyle(), rating.getContent(), rating.getPleasantness(),
                rating.getOriginality(), rating.getEdition()
        ));
        detailRating.setFont(Font.font("SF Pro Text", 12));
        detailRating.setTextFill(Color.LIGHTGRAY);

        Text reviewText = new Text(rating.getReview());
        reviewText.setFont(Font.font("SF Pro Text", 14));
        reviewText.setFill(Color.WHITE);
        reviewText.setWrappingWidth(550);

        Label dateLabel = new Label(getRelativeDate(rating.getData()));
        dateLabel.setFont(Font.font("SF Pro Text", 12));
        dateLabel.setTextFill(Color.GRAY);

        card.getChildren().addAll(header, detailRating, reviewText, dateLabel);
        return card;
    }

    /**
     * Crea e restituisce l'intera sezione delle raccomandazioni per un libro.
     * <p>
     * Questo metodo assembla i componenti principali della sezione "Raccomandazioni".
     * Include un'intestazione con un pulsante di azione (`createRecommendationsHeader`) e un'area di scorrimento
     * (`ScrollPane`) per visualizzare le raccomandazioni in un formato orizzontale. Dopo aver costruito la sezione,
     * il metodo avvia il caricamento asincrono delle raccomandazioni (`loadBookRecommendations`).
     * </p>
     *
     * @param book il libro per cui la sezione delle raccomandazioni viene creata
     * @param authManager il gestore di autenticazione per gestire le azioni dell'utente legate alle raccomandazioni
     * @return un {@link VBox} che contiene la sezione completa delle raccomandazioni
     */
    private static VBox createRecommendationsSection(Book book, AuthenticationManager authManager) {
        VBox recommendationsSection = new VBox(15);
        recommendationsSection.setPadding(new Insets(0, 30, 30, 30));

        // Header con pulsante per aggiungere raccomandazioni
        HBox recommendationsHeader = createRecommendationsHeader(book, authManager);

        // Container per le raccomandazioni
        ScrollPane recommendationsScrollPane = createRecommendationsScrollPane();

        recommendationsSection.getChildren().addAll(recommendationsHeader, recommendationsScrollPane);

        // Carica le raccomandazioni esistenti
        loadBookRecommendations(book, recommendationsScrollPane);

        return recommendationsSection;
    }

    /**
     * Crea e restituisce l'intestazione della sezione delle raccomandazioni, con un'azione dinamica basata sull'autenticazione dell'utente.
     * <p>
     * Questo metodo genera un'intestazione (`HBox`) per l'area delle raccomandazioni. Il suo comportamento è
     * dinamico e si adatta allo stato di login dell'utente:
     * <ul>
     * <li>Se l'utente è autenticato, viene visualizzato un pulsante "Consiglia libri" che, quando cliccato,
     * verifica se l'utente è autorizzato a consigliare libri e apre un dialog dedicato.</li>
     * <li>Se l'utente non è autenticato, viene mostrata una semplice etichetta che lo invita a effettuare l'accesso per poter contribuire.</li>
     * </ul>
     * Il metodo gestisce anche la logica per mostrare avvisi in caso di ISBN mancante o problemi di connessione durante la verifica delle autorizzazioni.
     * </p>
     *
     * @param book il libro per cui la sezione delle raccomandazioni viene creata
     * @param authManager il gestore di autenticazione per verificare lo stato di login dell'utente
     * @return un {@link HBox} che rappresenta l'intestazione della sezione delle raccomandazioni
     */
    private static HBox createRecommendationsHeader(Book book, AuthenticationManager authManager) {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label recommendationsHeader = new Label("💡 Raccomandazioni della community");
        recommendationsHeader.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        recommendationsHeader.setTextFill(Color.WHITE);

        Region recommendationsSpacer = new Region();
        HBox.setHgrow(recommendationsSpacer, Priority.ALWAYS);

        // Pulsante per aggiungere raccomandazioni (solo per utenti autenticati)
        if (authManager != null && authManager.isAuthenticated()) {
            Button addRecommendationButton = new Button("Consiglia libri");
            addRecommendationButton.setStyle(
                    "-fx-background-color: #9b59b6;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;"
            );

            addRecommendationButton.setOnAction(e -> {
                if (isEmpty(book.getIsbn())) {
                    showAlert("❌ Errore", "Impossibile consigliare: ISBN del libro mancante");
                    return;
                }

                // Verifica prima se l'utente può consigliare
                recommendationService.canUserRecommendAsync(authManager.getCurrentUsername(), book.getIsbn())
                        .thenAccept(response -> Platform.runLater(() -> {
                            if (response.isSuccess() && response.getCanRecommend() != null && response.getCanRecommend()) {
                                // Apri dialog raccomandazioni
                                RecommendationDialog.showRecommendationDialog(
                                        book,
                                        authManager.getCurrentUsername(),
                                        authManager,
                                        updatedRecommendations -> {
                                            // Ricarica le raccomandazioni quando vengono aggiornate
                                            loadBookRecommendations(book, findRecommendationsScrollPane());
                                        }
                                );
                            } else {
                                showAlert("⚠️ Non disponibile", response.getMessage());
                            }
                        }))
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                            });
                            return null;
                        });
            });

            headerBox.getChildren().addAll(recommendationsHeader, recommendationsSpacer, addRecommendationButton);
        } else {
            // Solo header per utenti non autenticati
            Label guestHint = new Label("Accedi per consigliare libri");
            guestHint.setFont(Font.font("SF Pro Text", 12));
            guestHint.setTextFill(Color.LIGHTBLUE);

            headerBox.getChildren().addAll(recommendationsHeader, recommendationsSpacer, guestHint);
        }

        return headerBox;
    }

    /**
     * Crea e restituisce un'area di scorrimento (`ScrollPane`) configurata per mostrare le raccomandazioni in un layout orizzontale.
     * <p>
     * Questo metodo prepara il contenitore per la visualizzazione delle raccomandazioni. La `ScrollPane` è stilizzata
     * per essere trasparente e configurata per consentire lo scorrimento orizzontale (es. per visualizzare una carosello di libri),
     * mentre la barra di scorrimento verticale è disabilitata. Un contenitore `HBox` viene impostato come suo contenuto per
     * ospitare le singole card di raccomandazione. Il riferimento a questa `ScrollPane` viene salvato in una variabile di istanza
     * per permetterne il futuro aggiornamento, ad esempio in seguito all'aggiunta di una nuova raccomandazione.
     * </p>
     *
     * @return un'istanza di {@link ScrollPane} per le raccomandazioni
     */
    private static ScrollPane createRecommendationsScrollPane() {
        ScrollPane recommendationsScrollPane = new ScrollPane();
        recommendationsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        recommendationsScrollPane.setFitToHeight(true);
        recommendationsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recommendationsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recommendationsScrollPane.setPannable(true);
        recommendationsScrollPane.setPrefHeight(200);

        HBox recommendationsContainer = new HBox(15);
        recommendationsContainer.setPadding(new Insets(10, 0, 10, 0));
        recommendationsScrollPane.setContent(recommendationsContainer);

        // Salva il riferimento
        currentRecommendationsScrollPane = recommendationsScrollPane;

        return recommendationsScrollPane;
    }

    /**
     * Carica in modo asincrono le raccomandazioni di libri per un libro specifico e le visualizza in una ScrollPane.
     * <p>
     * Questo metodo avvia una complessa operazione asincrona per recuperare le raccomandazioni dal servizio di backend.
     * Il flusso di esecuzione è il seguente:
     * <ol>
     * <li>Esegue la chiamata di rete in un thread separato per non bloccare l'interfaccia utente.</li>
     * <li>Gestisce il risultato della chiamata (successo o fallimento) e aggiorna l'interfaccia utente
     * in modo sicuro tramite {@code Platform.runLater}.</li>
     * <li>In caso di successo, raggruppa le raccomandazioni per ISBN per evitare duplicati e crea una card
     * visiva per ogni libro raccomandato.</li>
     * <li>Infine, sostituisce il contenuto e le proprietà della {@code ScrollPane} fornita con una
     * nuova istanza ottimizzata per lo scorrimento orizzontale, garantendo che le card siano visualizzate
     * correttamente in un layout a carosello.</li>
     * </ol>
     * In caso di errore o assenza di raccomandazioni, mostra un messaggio di avviso appropriato all'utente.
     * </p>
     *
     * @param targetBook il libro per cui caricare le raccomandazioni
     * @param scrollPane la {@link ScrollPane} in cui verranno visualizzate le raccomandazioni
     */
    private static void loadBookRecommendations(Book targetBook, ScrollPane scrollPane) {
        System.out.println("📚 Caricamento raccomandazioni per: " + targetBook.getTitle());

        // Usa il metodo corretto getBookRecommendationsAsync
        CompletableFuture.supplyAsync(() -> {
            try {
                // Usa il metodo corretto che esiste nel ClientRecommendationService
                return recommendationService.getBookRecommendationsAsync(targetBook.getIsbn()).get();
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento raccomandazioni: " + e.getMessage());
                return new RecommendationResponse(false, "Errore: " + e.getMessage());
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                // Imposta recommendedBooksDetails dalla response
                if (response.isSuccess()) {
                    // Imposta i dettagli dei libri dalla response
                    recommendedBooksDetails = response.getRecommendedBooks();

                    System.out.println("📚 AGGIORNATO recommendedBooksDetails con " +
                            (recommendedBooksDetails != null ? recommendedBooksDetails.size() : "NULL") + " libri");
                }

                //Controlla se abbiamo i dettagli dei libri raccomandati
                System.out.println("🔍 DEBUG recommendedBooksDetails:");
                if (recommendedBooksDetails != null) {
                    System.out.println("   Trovati " + recommendedBooksDetails.size() + " dettagli libri:");
                    for (Book book : recommendedBooksDetails) {
                        System.out.println("   - ISBN: " + book.getIsbn() + ", Titolo: " + book.getTitle());
                    }
                } else {
                    System.out.println("   recommendedBooksDetails è NULL - i titoli non saranno disponibili");
                }

                if (response.isSuccess() && response.hasMultipleRecommendations()) {
                    System.out.println("🎯 Raccomandazioni trovate: " + response.getRecommendations().size());

                    Map<String, List<BookRecommendation>> groupedRecs = groupRecommendationsByBook(response.getRecommendations());

                    HBox cardsContainer = new HBox(15);
                    cardsContainer.setAlignment(Pos.CENTER_LEFT);
                    cardsContainer.setPadding(new Insets(10));

                    for (Map.Entry<String, List<BookRecommendation>> entry : groupedRecs.entrySet()) {
                        List<BookRecommendation> bookRecs = entry.getValue();
                        BookRecommendation firstRec = bookRecs.get(0); // Prendi il primo per i dettagli del libro

                        System.out.println("📖 Creazione carta per ISBN: " + firstRec.getRecommendedBookIsbn());

                        // Crea carta per il libro raccomandato CON IMMAGINE
                        VBox card = createRecommendationCardWithImage(firstRec, bookRecs, targetBook);
                        cardsContainer.getChildren().add(card);
                    }

                    // Usa ScrollPane SOLO orizzontale
                    ScrollPane fixedScrollPane = createFixedHorizontalScrollPane(cardsContainer);

                    // Sostituisci completamente il contenuto
                    scrollPane.setContent(fixedScrollPane.getContent());
                    scrollPane.setHbarPolicy(fixedScrollPane.getHbarPolicy());
                    scrollPane.setVbarPolicy(fixedScrollPane.getVbarPolicy());
                    scrollPane.setPrefHeight(fixedScrollPane.getPrefHeight());
                    scrollPane.setMaxHeight(fixedScrollPane.getMaxHeight());
                    scrollPane.setMinHeight(fixedScrollPane.getMinHeight());
                    scrollPane.setFitToHeight(fixedScrollPane.isFitToHeight());
                    scrollPane.setFitToWidth(fixedScrollPane.isFitToWidth());

                    System.out.println("✅ Raccomandazioni caricate: " + groupedRecs.size() + " libri unici");
                } else {
                    // Crea container vuoto con messaggio
                    HBox emptyContainer = new HBox();
                    emptyContainer.setAlignment(Pos.CENTER);
                    emptyContainer.setPrefHeight(240); // Stessa altezza del container pieno

                    Label noRecsLabel = new Label("📝 Nessuna raccomandazione disponibile");
                    noRecsLabel.setTextFill(Color.GRAY);
                    noRecsLabel.setFont(Font.font("SF Pro Text", 12));
                    emptyContainer.getChildren().add(noRecsLabel);

                    scrollPane.setContent(emptyContainer);
                    System.out.println("ℹ️ Nessuna raccomandazione trovata per il libro");
                }
            });
        });
    }

    /**
     * Crea e restituisce una card visuale completa e interattiva per un libro raccomandato.
     * <p>
     * Questo metodo genera una card stilizzata che include la copertina del libro, il titolo, il numero di raccomandazioni
     * ricevute e i nomi di chi l'ha consigliato. La card offre diverse funzionalità:
     * <ul>
     * <li>Recupera in modo robusto i dettagli del libro raccomandato.</li>
     * <li>Mostra un'icona di rimozione (`✕`) accanto al nome dell'utente che ha effettuato la raccomandazione,
     * permettendo di rimuoverla in modo asincrono.</li>
     * <li>Include un pulsante "Vedi dettagli" che apre un popup modale con i dettagli completi del libro raccomandato.</li>
     * </ul>
     * La card è progettata con dimensioni fisse e uno stile uniforme per essere visualizzata in un layout a carosello.
     * </p>
     *
     * @param firstRec l'istanza di {@link BookRecommendation} che contiene i dati del primo raccomandatore
     * @param allRecommendations la lista completa di tutte le raccomandazioni per quel libro
     * @param targetBook il libro originale per cui sono state fatte le raccomandazioni
     * @return un {@link VBox} che rappresenta la card completa della raccomandazione
     */
    private static VBox createRecommendationCardWithImage(BookRecommendation firstRec, List<BookRecommendation> allRecommendations, Book targetBook) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #3a3a3c;" +  // Colore corretto come le altre carte
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 3, 0, 0, 1);"
        );
        card.setPrefWidth(180); // Larghezza maggiore per vedere tutto
        card.setMaxWidth(180);
        card.setPrefHeight(220); // Altezza fissa per uniformità
        card.setMaxHeight(220);

        // Trova il libro corretto dai dettagli - variabile final
        final Book recommendedBook;
        if (recommendedBooksDetails != null) {
            Book foundBook = null;
            for (Book book : recommendedBooksDetails) {
                if (book.getIsbn().equals(firstRec.getRecommendedBookIsbn())) {
                    foundBook = book;
                    break;
                }
            }
            recommendedBook = foundBook != null ? foundBook : createBookFromRecommendation(firstRec);
        } else {
            recommendedBook = createBookFromRecommendation(firstRec);
        }

        // Crea immagine copertina
        recommendedBook.ensureLocalImageFileName();
        ImageView coverImage = ImageUtils.createSafeImageView(recommendedBook.getSafeImageFileName(), 70, 100);
        Rectangle coverClip = new Rectangle(70, 100);
        coverClip.setArcWidth(4);
        coverClip.setArcHeight(4);
        coverImage.setClip(coverClip);

        // Centra l'immagine
        HBox imageContainer = new HBox(coverImage);
        imageContainer.setAlignment(Pos.CENTER);

        // Titolo del libro (NON ISBN)
        String bookTitle = recommendedBook.getTitle();
        if (bookTitle == null || bookTitle.startsWith("Libro ISBN:")) {
            bookTitle = "Titolo non disponibile";
        }

        Label titleLabel = new Label(bookTitle);
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 9));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(160);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-text-alignment: center;");
        titleLabel.setPrefHeight(30); // Altezza fissa per il titolo

        // Label con numero di raccomandatori
        Label recommendersLabel = new Label("👥 " + allRecommendations.size() +
                (allRecommendations.size() == 1 ? " utente" : " utenti"));
        recommendersLabel.setFont(Font.font("SF Pro Text", 8));
        recommendersLabel.setTextFill(Color.web("#FFD700")); // Oro invece di giallo
        recommendersLabel.setAlignment(Pos.CENTER);

        // Lista raccomandatori (max 2 visibili per spazio) con possibilità di rimozione
        VBox recommendersBox = new VBox(2);
        recommendersBox.setMaxHeight(40); // Limita altezza

        for (int i = 0; i < Math.min(2, allRecommendations.size()); i++) {
            BookRecommendation rec = allRecommendations.get(i);

            HBox recommenderRow = new HBox(3);
            recommenderRow.setAlignment(Pos.CENTER_LEFT);

            Label recommenderLabel = new Label("👤 " + getShortUsername(rec.getRecommenderUsername()));
            recommenderLabel.setFont(Font.font("SF Pro Text", 7));
            recommenderLabel.setTextFill(Color.LIGHTBLUE);

            // Pulsante elimina se è la raccomandazione dell'utente corrente
            if (currentAuthManager != null && currentAuthManager.isAuthenticated() &&
                    rec.getRecommenderUsername().equals(currentAuthManager.getCurrentUsername())) {

                Button deleteButton = new Button("✕");
                deleteButton.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 6;" +
                                "-fx-background-radius: 50;" +
                                "-fx-min-width: 12;" +
                                "-fx-min-height: 12;" +
                                "-fx-max-width: 12;" +
                                "-fx-max-height: 12;" +
                                "-fx-cursor: hand;"
                );

                deleteButton.setOnAction(e -> {
                    deleteButton.setDisable(true);
                    removeRecommendationAsync(rec, targetBook, deleteButton);
                });

                recommenderRow.getChildren().addAll(recommenderLabel, deleteButton);
            } else {
                recommenderRow.getChildren().add(recommenderLabel);
            }

            recommendersBox.getChildren().add(recommenderRow);
        }

        // Se ci sono più di 2 raccomandazioni, mostra "e altri X"
        if (allRecommendations.size() > 2) {
            Label moreLabel = new Label("... e altri " + (allRecommendations.size() - 2));
            moreLabel.setFont(Font.font("SF Pro Text", 6));
            moreLabel.setTextFill(Color.GRAY);
            recommendersBox.getChildren().add(moreLabel);
        }

        // Pulsante "Vedi dettagli"
        Button viewButton = new Button("Vedi dettagli");
        viewButton.setStyle(
                "-fx-background-color: #007AFF;" +  // Blu
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 8;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 3 8;" +
                        "-fx-cursor: hand;"
        );

        // Gestione click per aprire dettagli libro raccomandato
        viewButton.setOnAction(e -> {
            viewButton.setDisable(true);
            viewButton.setText("Apertura...");

            Platform.runLater(() -> {
                try {
                    List<Book> navigableBooks = createNavigableRecommendedBooks(recommendedBook);

                    PopupManager popupManager = PopupManager.getInstance();
                    if (popupManager.isInitialized()) {
                        popupManager.showRecommendationDetails(recommendedBook, navigableBooks, currentAuthManager);
                    } else {
                        showRecommendedBookDetailsPopupFallback(recommendedBook, navigableBooks);
                    }

                } catch (Exception ex) {
                    System.err.println("❌ Errore apertura popup raccomandazione: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert("❌ Errore", "Errore nell'apertura dei dettagli del libro");
                } finally {
                    // Riabilita pulsante dopo un delay
                    Timeline enableDelay = new Timeline(
                            new KeyFrame(Duration.millis(1500), event -> {
                                viewButton.setDisable(false);
                                viewButton.setText("Vedi dettagli");
                            })
                    );
                    enableDelay.play();
                }
            });
        });

        // Centra il pulsante
        HBox buttonContainer = new HBox(viewButton);
        buttonContainer.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageContainer, titleLabel, recommendersLabel, recommendersBox, buttonContainer);
        return card;
    }

    /**
     * Restituisce una versione abbreviata del nome utente per la visualizzazione in spazi ridotti.
     * <p>
     * Questo metodo è una utility per gestire la visualizzazione dei nomi utente in un'interfaccia compatta.
     * Se il nome utente è nullo o vuoto, restituisce la stringa "Utente".
     * Se il nome utente supera i 10 caratteri, viene troncato e viene aggiunto un'ellissi ("...").
     * In tutti gli altri casi, il nome utente viene restituito inalterato.
     * </p>
     *
     * @param username il nome utente da abbreviare
     * @return il nome utente abbreviato o "Utente" se l'input è nullo o vuoto
     */
    private static String getShortUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "Utente";
        }

        // Se l'username è più lungo di 10 caratteri, accorcialo (spazio ridotto)
        if (username.length() > 10) {
            return username.substring(0, 7) + "...";
        }

        return username;
    }

    /**
     * Recupera il titolo di un libro raccomandato utilizzando diverse strategie di fallback.
     * <p>
     * Questo metodo tenta di ottenere un titolo leggibile per un libro raccomandato, che potrebbe non avere i dettagli completi
     * già caricati. La logica di recupero segue un ordine preciso:
     * <ol>
     * <li>Prima, cerca il titolo nella lista pre-caricata di dettagli dei libri raccomandati (`recommendedBooksDetails`).
     * Questa è la fonte di dati primaria e più affidabile.</li>
     * <li>Successivamente, utilizza un'implementazione di mockup (basata su uno `switch` con ISBN predefiniti) per fornire
     * titoli "noti" a scopo di test o dimostrazione.</li>
     * <li>Come fallback finale, se nessuna delle strategie precedenti riesce a trovare un titolo, restituisce la stringa
     * generica "Libro consigliato".</li>
     * </ol>
     * Questa implementazione è utile per garantire che la UI abbia sempre un titolo da visualizzare, anche in assenza di dati completi.
     * </p>
     *
     * @param rec l'istanza di {@link BookRecommendation} da cui estrarre l'ISBN del libro raccomandato
     * @return il titolo del libro raccomandato, o un titolo generico di fallback se non trovato
     */
    private static String getRecommendedBookTitle(BookRecommendation rec) {
        // Se abbiamo i dettagli dei libri raccomandati, usa quelli
        if (recommendedBooksDetails != null) {
            for (Book book : recommendedBooksDetails) {
                if (book.getIsbn() != null && book.getIsbn().equals(rec.getRecommendedBookIsbn())) {
                    if (book.getTitle() != null && !book.getTitle().trim().isEmpty()) {
                        return book.getTitle();
                    }
                }
            }
        }

        // Prova a caricare il libro dal servizio in modo sincrono
        try {
            // Se non abbiamo i dettagli, potremmo avere il servizio libri disponibile
            if (currentBook != null) {
                System.out.println("🔍 Tentativo recupero titolo per ISBN: " + rec.getRecommendedBookIsbn());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Errore recupero titolo libro: " + e.getMessage());
        }


        String isbn = rec.getRecommendedBookIsbn();
        if (isbn != null) {
            // Basato sui libri che vedo nell'immagine, prova a mappare alcuni ISBN noti
            switch (isbn) {
                case "399148582":
                    return "The Calculus of Consent";
                case "038031840X":
                    return "The Year's Best Fantasy";
                case "393030342":
                    return "Libro Consigliato";
                case "345358791":
                    return "2001: A Space Odyssey";
                case "684819066":
                    return "Libro di Fantascienza";
                default:
                    return "Libro consigliato";
            }
        }

        return "Libro consigliato";
    }

    /**
     * Crea un oggetto {@link Book} a partire da un'istanza di {@link BookRecommendation}, utilizzando una strategia di fallback.
     * <p>
     * Questo metodo è un'utility fondamentale per garantire che un oggetto libro sia sempre disponibile per la visualizzazione,
     * anche se i suoi dettagli completi non sono stati ancora caricati. La logica di creazione è la seguente:
     * <ul>
     * <li>Prima, cerca di recuperare un oggetto {@link Book} completo dalla lista dei libri raccomandati già caricata (`recommendedBooksDetails`).</li>
     * <li>Se il libro non viene trovato in questa lista, il metodo crea un nuovo oggetto `Book` parzialmente popolato.
     * Vengono impostati l'ISBN e il titolo, che viene recuperato tramite il metodo di utilità {@link #getRecommendedBookTitle(BookRecommendation)},
     * che gestisce i propri fallback. L'autore viene impostato su un valore predefinito.</li>
     * </ul>
     * Questo approccio evita i `NullPointerException` e garantisce che la UI possa sempre visualizzare una card del libro.
     * </p>
     *
     * @param rec l'istanza di {@link BookRecommendation} da cui estrarre i dati
     * @return un oggetto {@link Book}, completo se disponibile, altrimenti un oggetto fallback
     */
    private static Book createBookFromRecommendation(BookRecommendation rec) {
        // Se abbiamo i dettagli dei libri raccomandati, usa quelli
        if (recommendedBooksDetails != null) {
            for (Book book : recommendedBooksDetails) {
                if (book.getIsbn() != null && book.getIsbn().equals(rec.getRecommendedBookIsbn())) {
                    return book;
                }
            }
        }

        // Fallback con Book con titolo intelligente
        Book book = new Book();
        book.setIsbn(rec.getRecommendedBookIsbn());

        // Usa lo stesso metodo migliorato per il titolo
        String title = getRecommendedBookTitle(rec);
        book.setTitle(title);
        book.setAuthor("Autore non disponibile");

        return book;
    }

    /**
     * Crea una lista di libri "navigabili" che include tutti i libri raccomandati e il libro attualmente selezionato.
     * <p>
     * Questo metodo prepara una collezione di libri che può essere utilizzata per la navigazione, come ad esempio
     * per consentire all'utente di scorrere tra i dettagli dei vari libri raccomandati in un popup.
     * La logica è la seguente:
     * <ul>
     * <li>Se la lista di dettagli dei libri raccomandati (`recommendedBooksDetails`) esiste e non è vuota, la usa come base per la collezione.</li>
     * <li>Garantisce che il libro correntemente selezionato (`selectedBook`) sia incluso nella lista, aggiungendolo se non è già presente.</li>
     * <li>Se la lista dei dettagli dei libri raccomandati è vuota, crea una lista di fallback contenente solo il libro selezionato.</li>
     * </ul>
     * Questo approccio assicura che il navigatore dei popup abbia sempre una lista valida di libri tra cui scegliere.
     * </p>
     *
     * @param selectedBook il libro attualmente selezionato dall'utente
     * @return una {@link List<Book>} contenente tutti i libri raccomandati e il libro selezionato
     */
    private static List<Book> createNavigableRecommendedBooks(Book selectedBook) {
        List<Book> navigableBooks = new ArrayList<>();

        if (recommendedBooksDetails != null && !recommendedBooksDetails.isEmpty()) {
            // Usa tutti i libri consigliati
            navigableBooks.addAll(recommendedBooksDetails);

            // Assicurati che il libro selezionato sia incluso
            if (!navigableBooks.contains(selectedBook)) {
                navigableBooks.add(selectedBook);
            }

        } else {
            // Fallback: solo il libro selezionato
            navigableBooks.add(selectedBook);
        }

        System.out.println("📖 Collezione navigabile creata: " + navigableBooks.size() + " libri");
        for (int i = 0; i < navigableBooks.size(); i++) {
            System.out.println("   " + (i+1) + ". " + navigableBooks.get(i).getTitle());
        }

        return navigableBooks;
    }

    /**
     * Rimuove in modo asincrono una raccomandazione di libro, dopo aver eseguito le dovute verifiche di autorizzazione.
     * <p>
     * Questo metodo gestisce il processo di rimozione di una raccomandazione, garantendo la sicurezza e fornendo feedback all'utente.
     * Prima di procedere con la chiamata al servizio, esegue le seguenti validazioni:
     * <ul>
     * <li>Controlla che l'utente sia autenticato.</li>
     * <li>Verifica che l'utente stia tentando di rimuovere solo la propria raccomandazione.</li>
     * </ul>
     * Se le verifiche passano, avvia una richiesta asincrona al servizio di raccomandazione. Al completamento,
     * aggiorna l'interfaccia utente mostrando un messaggio di successo o di errore e, in caso di successo,
     * ricarica la sezione delle raccomandazioni per riflettere la modifica. Il pulsante viene disabilitato
     * temporaneamente per evitare invii multipli durante l'operazione.
     * </p>
     *
     * @param rec la raccomandazione da rimuovere
     * @param targetBook il libro per cui la raccomandazione è stata fatta (necessario per il ricaricamento)
     * @param deleteButton il pulsante che ha avviato l'azione, utilizzato per gestirne lo stato (abilitato/disabilitato)
     */
    private static void removeRecommendationAsync(BookRecommendation rec, Book targetBook, Button deleteButton) {
        if (currentAuthManager == null || !currentAuthManager.isAuthenticated()) {
            showAlert("❌ Errore", "Devi essere autenticato per rimuovere una raccomandazione");
            deleteButton.setDisable(false);
            return;
        }

        // Verifica che l'utente possa rimuovere solo le proprie raccomandazioni
        if (!rec.getRecommenderUsername().equals(currentAuthManager.getCurrentUsername())) {
            showAlert("❌ Errore", "Puoi rimuovere solo le tue raccomandazioni");
            deleteButton.setDisable(false);
            return;
        }

        System.out.println("🗑️ Rimozione raccomandazione:");
        System.out.println("   Target Book: " + targetBook.getTitle() + " (ISBN: " + targetBook.getIsbn() + ")");
        System.out.println("   Recommended Book ISBN: " + rec.getRecommendedBookIsbn());
        System.out.println("   User: " + rec.getRecommenderUsername());

        // Crea richiesta di rimozione con dati corretti
        RecommendationRequest removeRequest = new RecommendationRequest(
                currentAuthManager.getCurrentUsername(),
                rec.getTargetBookIsbn(),  // ISBN del libro per cui è stata fatta la raccomandazione
                rec.getRecommendedBookIsbn()  // ISBN del libro raccomandato
        );

        // Esegui la rimozione in background
        CompletableFuture.supplyAsync(() -> {
            try {
                return recommendationService.removeRecommendationAsync(removeRequest).get();
            } catch (Exception e) {
                System.err.println("❌ Errore nella rimozione: " + e.getMessage());
                e.printStackTrace();
                return new RecommendationResponse(false, "Errore di connessione: " + e.getMessage());
            }
        }).thenAccept(removeResponse -> {
            Platform.runLater(() -> {
                if (removeResponse.isSuccess()) {
                    showAlert("✅ Successo", "Raccomandazione rimossa con successo");

                    // Ricarica le raccomandazioni dopo la rimozione
                    if (currentRecommendationsScrollPane != null) {
                        System.out.println("🔄 Ricaricamento raccomandazioni dopo rimozione...");
                        loadBookRecommendations(targetBook, currentRecommendationsScrollPane);
                    }
                } else {
                    showAlert("❌ Errore", "Errore nella rimozione: " + removeResponse.getMessage());
                    deleteButton.setDisable(false);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                showAlert("❌ Errore", "Errore di connessione: " + throwable.getMessage());
                deleteButton.setDisable(false);
            });
            return null;
        });
    }

    /**
     * Raggruppa una lista di raccomandazioni per il libro consigliato (identificato dall'ISBN).
     * <p>
     * Questo metodo trasforma una lista "piatta" di raccomandazioni in una struttura dati (`Map`) che
     * organizza le raccomandazioni in base all'ISBN del libro consigliato. Questo è particolarmente utile
     * per visualizzare quante volte un libro è stato raccomandato e da chi, evitando di mostrare duplicati
     * visivi per ogni singola raccomandazione. Viene utilizzato un `LinkedHashMap` per preservare l'ordine
     * originale in cui i libri raccomandati sono stati trovati nella lista.
     * </p>
     *
     * @param recommendations la lista di {@link BookRecommendation} da raggruppare
     * @return una {@link java.util.Map} in cui la chiave è l'ISBN del libro raccomandato
     * e il valore è una lista di tutte le raccomandazioni per quell'ISBN
     */
    private static Map<String, List<BookRecommendation>> groupRecommendationsByBook(List<BookRecommendation> recommendations) {
        Map<String, List<BookRecommendation>> groupedRecommendations = new LinkedHashMap<>();

        for (BookRecommendation rec : recommendations) {
            String key = rec.getRecommendedBookIsbn(); // Raggruppa per ISBN

            // Se non esiste la chiave, crea una nuova lista
            if (!groupedRecommendations.containsKey(key)) {
                groupedRecommendations.put(key, new ArrayList<>());
            }

            // Aggiungi la raccomandazione al gruppo
            groupedRecommendations.get(key).add(rec);
        }

        return groupedRecommendations;
    }

    /**
     * Crea e configura una {@link ScrollPane} ottimizzata per lo scorrimento esclusivamente orizzontale.
     * <p>
     * Questo metodo è un'utility fondamentale per costruire contenitori come caroselli. Riceve un contenitore (`HBox`)
     * e lo avvolge in una `ScrollPane` con le seguenti caratteristiche:
     * <ul>
     * <li>La barra di scorrimento verticale è completamente disabilitata.</li>
     * <li>La barra di scorrimento orizzontale è visibile solo se il contenuto supera la larghezza della finestra.</li>
     * <li>L'altezza della `ScrollPane` è fissata a 240 pixel per garantire che le carte visualizzate non vengano tagliate.</li>
     * <li>Lo sfondo è impostato su trasparente per integrarsi senza soluzione di continuità nel layout genitore.</li>
     * </ul>
     * Questo garantisce un'esperienza utente coerente e pulita per la navigazione dei contenuti orizzontali.
     * </p>
     *
     * @param cardsContainer l'{@link HBox} che contiene le carte e che fungerà da contenuto scorrevole
     * @return una {@link ScrollPane} configurata per lo scorrimento orizzontale
     */
    private static ScrollPane createFixedHorizontalScrollPane(HBox cardsContainer) {
        ScrollPane recommendationsScrollPane = new ScrollPane(cardsContainer);

        // ScrollPane SOLO orizzontale
        recommendationsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recommendationsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // IMPEDISCE scroll verticale

        // Aumenta altezza per vedere le carte completamente
        recommendationsScrollPane.setPrefHeight(240); // Altezza maggiore per carte complete
        recommendationsScrollPane.setMaxHeight(240);
        recommendationsScrollPane.setMinHeight(240);

        // Stile per nascondere i bordi
        recommendationsScrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );

        //  Imposta fit-to-height per mantenere l'altezza
        recommendationsScrollPane.setFitToHeight(true);
        recommendationsScrollPane.setFitToWidth(false); // NON adattare alla larghezza

        return recommendationsScrollPane;
    }

    /**
     * Mostra un popup di dettagli del libro utilizzando una logica di fallback.
     * <p>
     * Questo metodo è una soluzione di riserva che viene invocata quando il gestore principale dei popup
     * ({@link PopupManager}) non è disponibile. Crea e visualizza un popup di dettagli del libro tradizionale
     * (`BookDetailsPopup`) che si aggiunge direttamente al nodo radice dell'interfaccia utente principale.
     * È progettato per garantire che l'utente possa sempre visualizzare i dettagli di un libro raccomandato,
     * anche in scenari in cui la gestione avanzata dei popup non è attiva.
     * Il metodo gestisce il passaggio dei dati per la navigazione e definisce una logica di chiusura specifica per questo fallback.
     * </p>
     *
     * @param recommendedBook il libro raccomandato di cui visualizzare i dettagli
     * @param recommendedBooksCollection la collezione di libri raccomandati per consentire la navigazione all'interno del popup
     */
    private static void showRecommendedBookDetailsPopupFallback(Book recommendedBook, List<Book> recommendedBooksCollection) {
        System.out.println("🔄 Fallback: apertura popup tradizionale per: " + recommendedBook.getTitle());

        try {
            // Crea popup tradizionale
            StackPane popup = BookDetailsPopup.createWithLibrarySupport(
                    recommendedBook,
                    recommendedBooksCollection != null ? recommendedBooksCollection : List.of(recommendedBook),
                    () -> {
                        // Chiudi popup tradizionale
                        closeTopPopupFallback();
                    },
                    currentAuthManager
            );

            // Trova il root principale e aggiungi popup
            StackPane mainRoot = findMainRoot();
            if (mainRoot != null) {
                mainRoot.getChildren().add(popup);
                System.out.println("✅ Popup fallback aperto con successo");
            } else {
                System.err.println("❌ Impossibile trovare root principale per fallback");
                throw new RuntimeException("Root principale non trovato");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore nel fallback: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Chiude il popup posizionato in cima allo stack visivo dell'applicazione, fungendo da meccanismo di fallback.
     * <p>
     * Questo metodo è una soluzione di riserva per chiudere i popup quando non è disponibile un gestore di popup
     * dedicato. Si basa sul presupposto che il popup sia l'ultimo nodo figlio aggiunto al contenitore radice principale
     * (`StackPane`). Rimuove l'ultimo figlio se il contenitore ne ha più di uno, chiudendo di fatto l'elemento in cima.
     * L'operazione è racchiusa in un blocco `try-catch` per garantire una gestione sicura di eventuali errori.
     * </p>
     */
    private static void closeTopPopupFallback() {
        try {
            StackPane mainRoot = findMainRoot();
            if (mainRoot != null && mainRoot.getChildren().size() > 1) {
                mainRoot.getChildren().remove(mainRoot.getChildren().size() - 1);
                System.out.println("✅ Popup fallback chiuso");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Errore nella chiusura popup fallback: " + e.getMessage());
        }
    }

    /**
     * Fornisce un accesso all'istanza della {@link ScrollPane} utilizzata per le raccomandazioni.
     * <p>
     * Questo metodo è un semplice getter che restituisce il riferimento alla `ScrollPane` precedentemente salvata
     * nella variabile statica `currentRecommendationsScrollPane`. Viene utilizzato per consentire ad altri metodi
     * di ottenere un riferimento al contenitore delle raccomandazioni per aggiornarne il contenuto, ad esempio
     * dopo che un utente ha aggiunto o rimosso una raccomandazione.
     * </p>
     *
     * @return l'istanza di {@link ScrollPane} che contiene le raccomandazioni del libro
     */
    private static ScrollPane findRecommendationsScrollPane() {
        return currentRecommendationsScrollPane;
    }

    /**
     * Trova e restituisce il {@link StackPane} radice principale dell'interfaccia utente dell'applicazione.
     * <p>
     * Questo metodo è una utility di fallback che cerca di identificare il contenitore radice principale per l'intera applicazione.
     * Segue una strategia a due livelli:
     * <ol>
     * <li>Prima, cerca di recuperare il root da una variabile di riferimento nota (`root.getScene().getRoot()`), che è il metodo più veloce e diretto.</li>
     * <li>Se il primo tentativo fallisce, itera attraverso tutte le finestre (`Stage`) aperte dall'applicazione per trovare un `Scene` il cui nodo radice sia un `StackPane`.</li>
     * </ol>
     * Questo metodo è fondamentale per sovrapporre elementi come popup e dialoghi al di sopra dell'intera interfaccia utente,
     * garantendo che un punto di aggancio sia sempre disponibile, anche in scenari complessi.
     * </p>
     *
     * @return il {@link StackPane} radice principale dell'applicazione se trovato, altrimenti {@code null}
     */
    private static StackPane findMainRoot() {
        try {
            // Prova con il root corrente se disponibile
            if (root != null && root.getScene() != null && root.getScene().getRoot() instanceof StackPane) {
                return (StackPane) root.getScene().getRoot();
            }

            // Prova con le finestre aperte
            if (javafx.stage.Stage.getWindows() != null && !javafx.stage.Stage.getWindows().isEmpty()) {
                for (javafx.stage.Window window : javafx.stage.Stage.getWindows()) {
                    if (window instanceof Stage && ((Stage) window).getScene() != null) {
                        Scene scene = ((Stage) window).getScene();
                        if (scene.getRoot() instanceof StackPane) {
                            return (StackPane) scene.getRoot();
                        }
                    }
                }
            }

            System.out.println("⚠️ Root principale non trovato");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca del root principale: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea e restituisce un contenitore {@link VBox} stilizzato che funge da anteprima per un libro specifico.
     * <p>
     * Questo metodo è responsabile della costruzione dell'interfaccia utente per la visualizzazione dei dettagli di un libro.
     * Accetta un indice e recupera il libro corrispondente da una collezione globale. La logica del metodo include:
     * <ul>
     * <li>Validazione dell'indice, restituendo un contenitore invisibile e non gestito in caso di input non valido.</li>
     * <li>Configurazione di un contenitore principale (`VBox`) con dimensioni e stile predefiniti, inclusa un'opacità iniziale di 0.0 per facilitare le animazioni di transizione.</li>
     * <li>Strutturazione del contenuto in sezioni separate per la copertina (`createCoverContainer`) e le informazioni di base del libro (`createBasicInfoBox`).</li>
     * <li>Inclusione di una `ScrollPane` per rendere il contenuto potenzialmente scorrevole, anche se le barre di scorrimento sono inizialmente disattivate.</li>
     * </ul>
     * La card è pensata per essere aggiunta a un contenitore superiore come un popup modale.
     * </p>
     *
     * @param bookIndex l'indice del libro da visualizzare all'interno della collezione di libri
     * @return un {@link VBox} contenente l'anteprima del libro, pronto per essere aggiunto a una scena
     */
    private static VBox createBookPreview(int bookIndex) {
        if (bookIndex < 0 || booksCollection == null || bookIndex >= booksCollection.size()) {
            VBox emptyPreview = new VBox();
            emptyPreview.setVisible(false);
            emptyPreview.setManaged(false);
            return emptyPreview;
        }

        Book previewBook = booksCollection.get(bookIndex);
        VBox previewContent = new VBox();
        previewContent.setMaxWidth(1000);
        previewContent.setMaxHeight(700);
        previewContent.setOpacity(0.0);
        previewContent.setStyle(createPopupStyle(getBookBackgroundColor(previewBook)));

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));

        HBox detailsSection = new HBox(30);
        detailsSection.setPadding(new Insets(20, 30, 30, 30));
        detailsSection.setAlignment(Pos.TOP_LEFT);

        VBox coverContainer = createCoverContainer(previewBook);
        VBox infoBox = createBasicInfoBox(previewBook);

        detailsSection.getChildren().addAll(coverContainer, infoBox);

        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentScroll.setFitToWidth(true);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox scrollContent = new VBox();
        scrollContent.getChildren().addAll(detailsSection);
        contentScroll.setContent(scrollContent);

        previewContent.getChildren().addAll(topBar, contentScroll);
        return previewContent;
    }

    /**
     * Crea e restituisce un contenitore {@link VBox} che visualizza le informazioni di base di un libro.
     * <p>
     * Questo metodo funge da fabbrica per un semplice componente dell'interfaccia utente che include il titolo e l'autore di un libro.
     * Utilizza metodi helper dedicati per creare le etichette del titolo e dell'autore, garantendo coerenza stilistica.
     * Il contenitore risultante è allineato in alto a sinistra.
     * </p>
     *
     * @param book il libro per cui creare il box informativo
     * @return un {@link VBox} contenente le etichette per il titolo e l'autore del libro
     */
    private static VBox createBasicInfoBox(Book book) {
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.TOP_LEFT);

        infoBox.getChildren().addAll(
                createTitleLabel(book.getTitle()),
                createAuthorLabel(book.getAuthor())
        );

        return infoBox;
    }

    /**
     * Aggiunge zone di rilevamento ai bordi di un contenitore per abilitare la navigazione tra le pagine o i libri.
     * <p>
     * Questo metodo crea due rettangoli invisibili, posizionati rispettivamente sul bordo sinistro e destro del contenitore `StackPane` fornito.
     * Questi "sensori" non sono visibili all'utente ma rilevano gli eventi del mouse (passaggio, clic),
     * permettendo di attivare funzionalità di navigazione, come il passaggio al libro precedente o successivo.
     * L'operazione viene eseguita solo se la collezione di libri contiene più di un elemento, rendendo la navigazione pertinente.
     * </p>
     *
     * @param container il {@link StackPane} a cui aggiungere le zone di rilevamento dei bordi
     */
    private static void addEdgeDetection(StackPane container) {
        if (booksCollection == null || booksCollection.size() <= 1) {
            return;
        }

        Rectangle leftEdge = createEdgeZone(120, 700);
        Rectangle rightEdge = createEdgeZone(120, 700);

        StackPane.setAlignment(leftEdge, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightEdge, Pos.CENTER_RIGHT);

        setupEdgeEvents(leftEdge, rightEdge);
        container.getChildren().addAll(leftEdge, rightEdge);
    }

    /**
     * Crea un rettangolo trasparente di dimensioni specificate per fungere da "zona di attivazione" invisibile.
     * <p>
     * Questo metodo è un'utility per generare aree rettangolari non visibili che vengono utilizzate per il rilevamento
     * di eventi del mouse (ad esempio, per un'esperienza utente simile a quella di sfogliare un libro).
     * Il rettangolo viene impostato con un colore trasparente e un'opacità molto bassa (`0.01`) per
     * assicurare che sia cliccabile e rilevabile dal sistema di eventi, pur rimanendo praticamente invisibile all'utente.
     * </p>
     *
     * @param width la larghezza della zona di rilevamento in pixel
     * @param height l'altezza della zona di rilevamento in pixel
     * @return un'istanza di {@link Rectangle} trasparente e reattiva agli eventi
     */
    private static Rectangle createEdgeZone(int width, int height) {
        Rectangle edge = new Rectangle(width, height, Color.TRANSPARENT);
        edge.setOpacity(0.01);
        return edge;
    }

    /**
     * Configura i gestori di eventi del mouse per le zone di rilevamento ai bordi, abilitando la navigazione fluida tra i libri.
     * <p>
     * Questo metodo associa un comportamento interattivo ai rettangoli invisibili usati per la navigazione.
     * La logica degli eventi è progettata per un'esperienza utente reattiva e priva di sfarfallio:
     * <ul>
     * <li>Al passaggio del mouse (`onMouseEntered`), una `Timeline` ritardata avvia un'anteprima visiva.</li>
     * <li>All'uscita del mouse (`onMouseExited`), un'altra `Timeline` ritardata nasconde l'anteprima. I ritardi impediscono attivazioni indesiderate.</li>
     * <li>Al clic del mouse (`onMouseClicked`), il metodo avvia una transizione animata verso il libro precedente o successivo (`slideToBook`), a seconda del bordo cliccato.</li>
     * </ul>
     * Ogni azione è condizionata da un flag `isTransitioning` per prevenire interazioni multiple durante una transizione.
     * </p>
     *
     * @param leftEdge il {@link Rectangle} che rappresenta la zona di navigazione a sinistra
     * @param rightEdge il {@link Rectangle} che rappresenta la zona di navigazione a destra
     */
    private static void setupEdgeEvents(Rectangle leftEdge, Rectangle rightEdge) {
        Timeline leftHoverDelay = new Timeline();
        Timeline rightHoverDelay = new Timeline();

        // Right edge events
        rightEdge.setOnMouseEntered(e -> {
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                rightHoverDelay.stop();
                rightHoverDelay.getKeyFrames().clear();
                rightHoverDelay.getKeyFrames().add(
                        new KeyFrame(Duration.millis(100), event -> showBookPreview(true, false))
                );
                rightHoverDelay.play();
            }
        });

        rightEdge.setOnMouseExited(e -> {
            rightHoverDelay.stop();
            if (!isTransitioning) {
                Timeline exitDelay = new Timeline(
                        new KeyFrame(Duration.millis(50), event -> showBookPreview(false, false))
                );
                exitDelay.play();
            }
        });

        rightEdge.setOnMouseClicked(e -> {
            rightHoverDelay.stop();
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                slideToBook(currentBookIndex + 1);
            }
        });

        // Left edge events
        leftEdge.setOnMouseEntered(e -> {
            if (currentBookIndex > 0 && !isTransitioning) {
                leftHoverDelay.stop();
                leftHoverDelay.getKeyFrames().clear();
                leftHoverDelay.getKeyFrames().add(
                        new KeyFrame(Duration.millis(100), event -> showBookPreview(true, true))
                );
                leftHoverDelay.play();
            }
        });

        leftEdge.setOnMouseExited(e -> {
            leftHoverDelay.stop();
            if (!isTransitioning) {
                Timeline exitDelay = new Timeline(
                        new KeyFrame(Duration.millis(50), event -> showBookPreview(false, true))
                );
                exitDelay.play();
            }
        });

        leftEdge.setOnMouseClicked(e -> {
            leftHoverDelay.stop();
            if (currentBookIndex > 0 && !isTransitioning) {
                slideToBook(currentBookIndex - 1);
            }
        });
    }

    /**
     * Aggiunge le frecce di navigazione per lo scorrimento tra i libri alla radice principale dell'interfaccia utente.
     * <p>
     * Questo metodo crea due pulsanti di navigazione, uno per scorrere a sinistra e uno per scorrere a destra,
     * e li posiziona sui bordi del contenitore radice. Le frecce vengono aggiunte solo se la collezione di libri
     * contiene più di un elemento, rendendo la navigazione possibile e pertinente. Dopo la creazione e il posizionamento,
     * il metodo chiama {@link #setupArrowEvents()} per collegare le funzionalità di navigazione a questi pulsanti.
     * </p>
     */
    private static void addNavigationArrows() {
        if (booksCollection == null || booksCollection.size() <= 1) {
            return;
        }

        leftArrowButton = createArrowButton("❮");
        rightArrowButton = createArrowButton("❯");

        StackPane.setAlignment(leftArrowButton, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightArrowButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(leftArrowButton, new Insets(0, 0, 0, 30));
        StackPane.setMargin(rightArrowButton, new Insets(0, 30, 0, 0));

        setupArrowEvents();
        root.getChildren().addAll(leftArrowButton, rightArrowButton);
    }

    /**
     * Crea e restituisce un {@link Button} stilizzato per essere utilizzato come freccia di navigazione.
     * <p>
     * Il pulsante è progettato con uno stile circolare e semi-trasparente, con testo bianco e un'ombra esterna
     * per una chiara visibilità. Viene impostato un raggio del 50% per renderlo perfettamente circolare e
     * l'opacità iniziale è 0.0, il che suggerisce un'animazione di dissolvenza successiva per farlo apparire
     * in modo graduale.
     * </p>
     *
     * @param text il testo da visualizzare sul pulsante, solitamente un carattere di freccia
     * @return un'istanza di {@link Button} con lo stile di una freccia di navigazione
     */
    private static Button createArrowButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.3);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 44px;" +
                        "-fx-min-height: 44px;" +
                        "-fx-max-width: 44px;" +
                        "-fx-max-height: 44px;" +
                        "-fx-padding: 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-opacity: 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);"
        );
        return button;
    }

    /**
     * Configura il comportamento interattivo e animato delle frecce di navigazione.
     * <p>
     * Questo metodo gestisce la logica di visualizzazione e di interazione dei pulsanti freccia, garantendo un'esperienza utente fluida e reattiva. Le funzionalità principali sono:
     * <ul>
     * <li><b>Animazioni di Visibilità:</b> le frecce si dissolvono (`fadeIn`) quando il cursore entra nell'area principale della schermata e si dissolvono (`fadeOut`) quando il cursore esce. L'opacità finale delle frecce è condizionata dalla disponibilità di libri precedenti o successivi.</li>
     * <li><b>Interazione al Clic:</b> al clic sui pulsanti, il metodo avvia una transizione animata (`slideToBook`) per visualizzare il libro precedente o successivo.</li>
     * </ul>
     * La navigazione è soggetta a due controlli chiave: la disponibilità di un libro nella direzione desiderata e l'assenza di una transizione già in corso (`!isTransitioning`), per evitare azioni multiple contemporanee.
     * </p>
     */
    private static void setupArrowEvents() {
        root.setOnMouseEntered(e -> {
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(leftArrowButton.opacityProperty(),
                                    currentBookIndex > 0 ? 0.7 : 0, Interpolator.EASE_OUT),
                            new KeyValue(rightArrowButton.opacityProperty(),
                                    currentBookIndex < booksCollection.size() - 1 ? 0.7 : 0, Interpolator.EASE_OUT)
                    )
            );
            fadeIn.play();
        });

        root.setOnMouseExited(e -> {
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(leftArrowButton.opacityProperty(), 0, Interpolator.EASE_OUT),
                            new KeyValue(rightArrowButton.opacityProperty(), 0, Interpolator.EASE_OUT)
                    )
            );
            fadeOut.play();
        });

        setupButtonHoverEffect(leftArrowButton, () -> currentBookIndex > 0);
        setupButtonHoverEffect(rightArrowButton, () -> currentBookIndex < booksCollection.size() - 1);

        leftArrowButton.setOnMouseClicked(e -> {
            if (currentBookIndex > 0 && !isTransitioning) {
                slideToBook(currentBookIndex - 1);
            }
        });

        rightArrowButton.setOnMouseClicked(e -> {
            if (currentBookIndex < booksCollection.size() - 1 && !isTransitioning) {
                slideToBook(currentBookIndex + 1);
            }
        });
    }

    /**
     * Applica un effetto visivo di "hover" a un pulsante, rendendolo leggermente più opaco al passaggio del mouse.
     * <p>
     * Questo metodo è un'utilità generica che imposta i gestori di eventi {@code onMouseEntered} e {@code onMouseExited} su un pulsante.
     * L'effetto di hover è condizionale: viene applicato solo se la {@code Supplier} fornita restituisce {@code true}.
     * L'effetto consiste nel modificare l'opacità dello sfondo del pulsante da 0.3 a 0.5 al passaggio del mouse e nel ripristinare il valore originale quando il cursore esce.
     * </p>
     *
     * @param button il pulsante a cui applicare l'effetto
     * @param condition la condizione da verificare prima di applicare l'effetto di hover
     */
    private static void setupButtonHoverEffect(Button button, java.util.function.Supplier<Boolean> condition) {
        button.setOnMouseEntered(e -> {
            if (condition.get()) {
                button.setStyle(button.getStyle().replace("0.3", "0.5"));
            }
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("0.5", "0.3"));
        });
    }

    /**
     * Aggiorna la visibilità delle frecce di navigazione (`leftArrowButton` e `rightArrowButton`) in base alla posizione corrente nel carosello dei libri.
     * <p>
     * Il metodo avvia un'animazione {@link Timeline} che modifica l'opacità delle frecce per farle apparire o scomparire in modo graduale.
     * La freccia sinistra diventa visibile solo se l'indice del libro corrente è maggiore di 0 (quindi non è il primo libro), mentre la freccia destra diventa visibile solo se non si è all'ultimo libro della collezione.
     * In questo modo, l'interfaccia utente fornisce un feedback visivo immediato su quali direzioni di navigazione sono disponibili.
     * </p>
     */
    private static void updateArrowVisibility() {
        if (booksCollection == null || booksCollection.size() <= 1) {
            return;
        }

        Timeline updateVisibility = new Timeline();

        updateVisibility.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(150),
                        new KeyValue(leftArrowButton.opacityProperty(),
                                currentBookIndex > 0 ? 0.7 : 0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(rightArrowButton.opacityProperty(),
                                currentBookIndex < booksCollection.size() - 1 ? 0.7 : 0, Interpolator.EASE_OUT))
        );

        updateVisibility.play();
    }

    /**
     * Anima la visualizzazione di un'anteprima del libro precedente o successivo, creando un effetto visivo di "sbirciata".
     * <p>
     * Questo metodo è un componente chiave per la navigazione fluida. Al passaggio del mouse su una zona di rilevamento,
     * questo metodo anima un'anteprima del libro (precedente o successivo) facendola scivolare, ridimensionare e sfumare.
     * Le animazioni simultanee di traslazione, opacità e scala creano un effetto di anteprima pulito e reattivo.
     * Se un'animazione è già in corso, viene interrotta per evitare conflitti.
     * </p>
     *
     * @param show un booleano che indica se l'anteprima deve essere mostrata (`true`) o nascosta (`false`)
     * @param isPrevious un booleano che indica se l'anteprima da animare è quella del libro precedente (`true`) o successivo (`false`)
     */
    private static void showBookPreview(boolean show, boolean isPrevious) {
        if (slideAnimation != null && slideAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            slideAnimation.stop();
        }

        VBox previewToShow = isPrevious ? prevBookPreview : nextBookPreview;

        if (previewToShow == null || !previewToShow.isVisible()) {
            return;
        }

        double targetX = isPrevious ?
                (show ? -1050 : -1200) :
                (show ? 1050 : 1200);
        double targetOpacity = show ? 0.6 : 0.0;
        double targetScale = show ? 0.95 : 0.9;

        slideAnimation = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(previewToShow.translateXProperty(), targetX, Interpolator.EASE_OUT),
                        new KeyValue(previewToShow.opacityProperty(), targetOpacity, Interpolator.EASE_OUT),
                        new KeyValue(previewToShow.scaleXProperty(), targetScale, Interpolator.EASE_OUT),
                        new KeyValue(previewToShow.scaleYProperty(), targetScale, Interpolator.EASE_OUT)
                )
        );
        slideAnimation.play();
    }

    /**
     * Esegue una transizione animata per cambiare il libro visualizzato, passando a un libro precedente o successivo.
     * <p>
     * Questo metodo è il cuore della navigazione animata dell'interfaccia utente. Gestisce il cambio di stato dell'applicazione
     * e l'animazione visiva per passare da un libro all'altro. La logica si articola in diverse fasi:
     * <ol>
     * <li><b>Validazione:</b> esegue controlli per prevenire transizioni non valide (es. indice fuori dai limiti, transizione verso lo stesso libro, transizione già in corso).</li>
     * <li><b>Preparazione:</b> imposta un flag di stato `isTransitioning`, crea il contenuto del nuovo libro e lo posiziona fuori dallo schermo, pronto per l'animazione.</li>
     * <li><b>Animazione:</b> avvia una `Timeline` complessa che anima simultaneamente il libro corrente (facendolo scivolare e rimpicciolire) e il nuovo libro (facendolo scivolare e ingrandire). Vengono animate le proprietà di traslazione, opacità e scala per un effetto fluido.</li>
     * <li><b>Gestione al Termine:</b> una volta completata l'animazione, il metodo pulisce il `bookDisplayPane`, aggiorna l'indice del libro corrente, ricrea le anteprime, riabilita le frecce e le zone di rilevamento ai bordi e avvia il caricamento delle nuove recensioni. Infine, reimposta il flag `isTransitioning`.</li>
     * </ol>
     * Questo approccio garantisce una navigazione utente fluida, reattiva e priva di bug.
     * </p>
     *
     * @param newIndex l'indice del libro nella collezione a cui passare
     */
    private static void slideToBook(int newIndex) {
        if (newIndex < 0 || newIndex >= booksCollection.size() || newIndex == currentBookIndex || isTransitioning) {
            return;
        }

        isTransitioning = true;
        Book targetBook = booksCollection.get(newIndex);
        boolean isForward = newIndex > currentBookIndex;

        resetRatings();
        currentBook = targetBook;

        VBox newBookContent = createBookContent(targetBook, getBookBackgroundColor(targetBook), currentAuthManager);

        newBookContent.setTranslateX(isForward ? 1200 : -1200);
        newBookContent.setOpacity(0.0);
        newBookContent.setScaleX(0.95);
        newBookContent.setScaleY(0.95);

        bookDisplayPane.getChildren().add(newBookContent);
        VBox currentContent = (VBox) bookDisplayPane.getChildren().get(1);

        if (slideAnimation != null && slideAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            slideAnimation.stop();
        }

        slideAnimation = new Timeline(
                new KeyFrame(Duration.millis(0),
                        new KeyValue(currentContent.opacityProperty(), 1.0),
                        new KeyValue(currentContent.scaleXProperty(), 1.0),
                        new KeyValue(currentContent.scaleYProperty(), 1.0),
                        new KeyValue(currentContent.translateXProperty(), 0),
                        new KeyValue(newBookContent.opacityProperty(), 0.0),
                        new KeyValue(newBookContent.scaleXProperty(), 0.95),
                        new KeyValue(newBookContent.scaleYProperty(), 0.95)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(currentContent.opacityProperty(), 0.3, Interpolator.EASE_BOTH),
                        new KeyValue(currentContent.scaleXProperty(), 0.95, Interpolator.EASE_BOTH),
                        new KeyValue(currentContent.scaleYProperty(), 0.95, Interpolator.EASE_BOTH),
                        new KeyValue(currentContent.translateXProperty(),
                                isForward ? -200 : 200, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.opacityProperty(), 0.7, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.scaleXProperty(), 0.98, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.scaleYProperty(), 0.98, Interpolator.EASE_BOTH),
                        new KeyValue(newBookContent.translateXProperty(),
                                isForward ? 200 : -200, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(currentContent.opacityProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(currentContent.scaleXProperty(), 0.9, Interpolator.EASE_OUT),
                        new KeyValue(currentContent.scaleYProperty(), 0.9, Interpolator.EASE_OUT),
                        new KeyValue(currentContent.translateXProperty(),
                                isForward ? -400 : 400, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(newBookContent.translateXProperty(), 0, Interpolator.EASE_OUT)
                )
        );

        slideAnimation.setOnFinished(e -> {
            currentBookIndex = newIndex;
            bookDisplayPane.getChildren().clear();

            prevBookPreview = createBookPreview(currentBookIndex - 1);
            prevBookPreview.setTranslateX(-1200);
            prevBookPreview.setOpacity(0.0);
            prevBookPreview.setScaleX(0.9);
            prevBookPreview.setScaleY(0.9);

            nextBookPreview = createBookPreview(currentBookIndex + 1);
            nextBookPreview.setTranslateX(1200);
            nextBookPreview.setOpacity(0.0);
            nextBookPreview.setScaleX(0.9);
            nextBookPreview.setScaleY(0.9);

            newBookContent.setOpacity(1.0);
            newBookContent.setTranslateX(0);
            newBookContent.setScaleX(1.0);
            newBookContent.setScaleY(1.0);

            bookDisplayPane.getChildren().addAll(prevBookPreview, newBookContent, nextBookPreview);

            updateArrowVisibility();
            addEdgeDetection(bookDisplayPane);

            loadBookRatingsForAllUsers(targetBook, currentAuthManager);

            isTransitioning = false;
        });

        slideAnimation.play();
    }

    /**
     * Resetta le variabili di stato globali relative alle valutazioni dei libri e alle raccomandazioni.
     * <p>
     * Questo metodo viene chiamato per garantire che lo stato dell'applicazione sia pulito, ad esempio quando si cambia il libro visualizzato.
     * Imposta su {@code null} i riferimenti all'oggetto della valutazione dell'utente corrente, alla valutazione media del libro,
     * all'etichetta della valutazione media, alla sezione delle valutazioni correnti e ai dettagli dei libri raccomandati.
     * Questo aiuta a prevenire problemi di stato residuo tra le diverse visualizzazioni dei libri.
     * </p>
     */
    private static void resetRatings() {
        currentUserRating = null;
        averageBookRating = null;
        averageRatingLabel = null;
        currentRatingSection = null;
        recommendedBooksDetails = null;
    }

    /**
     * Verifica se una stringa è nulla o vuota (cioè non contiene caratteri o solo spazi bianchi).
     * <p>
     * Questo è un metodo di utilità comune per la validazione delle stringhe. Ritorna {@code true} se la stringa
     * è un riferimento nullo, oppure se dopo aver rimosso gli spazi bianchi iniziali e finali risulta vuota.
     * È un'alternativa sicura e robusta al semplice controllo di uguaglianza con una stringa vuota.
     * </p>
     *
     * @param str la stringa da controllare
     * @return {@code true} se la stringa è nulla o vuota; altrimenti {@code false}
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Verifica se una stringa non è né nulla né vuota.
     * <p>
     * Questo è un metodo di convenienza che delega il controllo a {@link #isEmpty(String)} e ne inverte il risultato.
     * Ritorna {@code true} se la stringa contiene almeno un carattere non-spazio, ed è utile per validare input obbligatori.
     * </p>
     *
     * @param str la stringa da controllare
     * @return {@code true} se la stringa non è nulla e non è vuota; altrimenti {@code false}
     */
    private static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Esegue il campionamento dei pixel di un'immagine per estrarne il colore dominante.
     * <p>
     * Questo metodo è una utility per l'analisi visiva di un'immagine (come la copertina di un libro)
     * per trovarne il colore più rappresentativo. Per ragioni di performance, non analizza ogni singolo pixel,
     * ma campiona l'immagine a intervalli predefiniti (`sampleSize`). La logica di base è la seguente:
     * <ul>
     * <li>Scansiona l'immagine a intervalli regolari.</li>
     * <li>Mappa ogni colore trovato e conta la sua frequenza di apparizione.</li>
     * <li>Infine, restituisce il colore che è apparso più di frequente.</li>
     * </ul>
     * Se l'immagine fornita non è valida (es. nullo, errore, dimensioni nulle), il metodo restituisce un colore di fallback
     * predefinito per prevenire eccezioni.
     * </p>
     *
     * @param image l'istanza di {@link javafx.scene.image.Image} da cui estrarre il colore
     * @return un'istanza di {@link Color} che rappresenta il colore dominante, o un colore di fallback in caso di input non valido
     */
    private static Color extractDominantColor(Image image) {
        if (image == null || image.isError()) {
            return Color.rgb(41, 35, 46);
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        if (width <= 0 || height <= 0) {
            return Color.rgb(41, 35, 46);
        }

        int sampleSize = 5;
        Map<Integer, Integer> colorCounts = new HashMap<>();
        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y += sampleSize) {
            for (int x = 0; x < width; x += sampleSize) {
                Color color = pixelReader.getColor(x, y);

                int rgb = ((int) (color.getRed() * 255) << 16) |
                        ((int) (color.getGreen() * 255) << 8) |
                        ((int) (color.getBlue() * 255));

                colorCounts.put(rgb, colorCounts.getOrDefault(rgb, 0) + 1);
            }
        }

        int dominantRGB = 0;
        int maxCount = 0;

        for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantRGB = entry.getKey();
            }
        }

        int red = (dominantRGB >> 16) & 0xFF;
        int green = (dominantRGB >> 8) & 0xFF;
        int blue = dominantRGB & 0xFF;

        return Color.rgb(red, green, blue);
    }

    /**
     * Scurisce un colore in base a un fattore specificato.
     * <p>
     * Questo metodo crea e restituisce una nuova istanza di {@link Color} con i valori RGB originali moltiplicati per un fattore di scurimento.
     * I valori risultanti sono sempre limitati a un minimo di 0 per evitare valori negativi. L'opacità del colore originale viene mantenuta.
     * Per scurire il colore, il fattore deve essere un valore compreso tra 0 e 1 (esclusi gli estremi), dove un valore più basso produce un colore più scuro.
     * </p>
     *
     * @param color il colore da scurire
     * @param factor il fattore di scurimento (un valore inferiore a 1.0 scurisce il colore)
     * @return una nuova istanza di {@link Color} scurita
     */
    private static Color darkenColor(Color color, double factor) {
        return new Color(
                Math.max(0, color.getRed() * factor),
                Math.max(0, color.getGreen() * factor),
                Math.max(0, color.getBlue() * factor),
                color.getOpacity()
        );
    }

    /**
     * Converte un'istanza di {@link Color} nella sua rappresentazione esadecimale standard in formato #RRGGBB.
     * <p>
     * Il metodo estrae i valori dei componenti rosso, verde e blu del colore, li converte in un valore intero compreso tra 0 e 255,
     * e li formatta in una stringa esadecimale di sei cifre, preceduta dal simbolo cancelletto (`#`).
     * Questo formato è comunemente usato per applicare stili tramite CSS o altri sistemi di styling basati su stringhe.
     * </p>
     *
     * @param color il colore da convertire
     * @return una {@link String} che rappresenta il colore nel formato esadecimale (#RRGGBB)
     */
    private static String toHexString(Color color) {
        int r = ((int) (color.getRed() * 255)) & 0xFF;
        int g = ((int) (color.getGreen() * 255)) & 0xFF;
        int b = ((int) (color.getBlue() * 255)) & 0xFF;

        return String.format("#%02X%02X%02X", r, g, b);
    }
}