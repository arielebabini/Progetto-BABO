package org.BABO.client.ui.Home;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.KeyCode;
import org.BABO.client.service.BookService;
import org.BABO.client.ui.Search.AdvancedSearchPanel;

import java.util.function.Consumer;

/**
 * Componente header per la finestra principale dell'applicazione BABO Library.
 * <p>
 * Questa classe implementa l'header superiore dell'interfaccia utente principale,
 * integrando funzionalità di ricerca avanzata con un design moderno. Fornisce un'esperienza di ricerca completa e intuitiva
 * con supporto per ricerca rapida e ricerca avanzata con filtri personalizzati.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Ricerca Rapida:</strong> Campo di ricerca principale con auto-completamento</li>
 *   <li><strong>Ricerca Avanzata:</strong> Popup overlay con filtri dettagliati</li>
 *   <li><strong>Gestione Eventi:</strong> Support per Enter key e click handlers</li>
 *   <li><strong>Interfaccia Moderna:</strong> Design consistente</li>
 *   <li><strong>State Management:</strong> Gestione stati apertura/chiusura ricerca avanzata</li>
 *   <li><strong>Responsive Design:</strong> Layout adattivo per diverse risoluzioni</li>
 * </ul>
 *
 * <h3>Architettura di Ricerca:</h3>
 * <p>
 * Il sistema di ricerca implementa una doppia modalità:
 * </p>
 * <ul>
 *   <li><strong>Ricerca Semplice:</strong> Query diretta tramite campo di testo</li>
 *   <li><strong>Ricerca Avanzata:</strong> Pannello popup con filtri strutturati</li>
 *   <li><strong>Query Building:</strong> Conversione parametri avanzati in query string</li>
 *   <li><strong>Handler Unificato:</strong> Interface comune per entrambe le modalità</li>
 * </ul>
 *
 * <h3>Sistema di Overlay:</h3>
 * <p>
 * La ricerca avanzata utilizza un sistema di overlay sofisticato:
 * </p>
 * <ul>
 *   <li>Overlay semi-trasparente con sfondo scuro</li>
 *   <li>Event handling selettivo per click outside</li>
 *   <li>Supporto per ESC key per chiusura rapida</li>
 *   <li>Gestione focus per accessibilità</li>
 *   <li>Memory management per prevenire memory leaks</li>
 * </ul>
 *
 * <h3>Design Pattern Implementati:</h3>
 * <ul>
 *   <li><strong>Observer Pattern:</strong> Callback per eventi di ricerca</li>
 *   <li><strong>State Pattern:</strong> Gestione stati UI (normale/ricerca avanzata)</li>
 *   <li><strong>Builder Pattern:</strong> Costruzione query da parametri avanzati</li>
 *   <li><strong>Strategy Pattern:</strong> Diverse strategie per tipi di ricerca</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo base:</h3>
 * <pre>{@code
 * // Inizializzazione header
 * BookService bookService = new BookService();
 * StackPane mainContainer = new StackPane();
 * Header header = new Header(bookService, mainContainer);
 *
 * // Configurazione search handler
 * header.setSearchHandler(query -> {
 *     System.out.println("Ricerca: " + query);
 *     performBookSearch(query);
 * });
 *
 * // Creazione UI
 * HBox headerUI = header.createHeader();
 *
 * // Integrazione in layout principale
 * VBox mainLayout = new VBox();
 * mainLayout.getChildren().add(headerUI);
 * }</pre>
 *
 * <h3>Esempio di utilizzo con ricerca avanzata:</h3>
 * <pre>{@code
 * // Setup completo con ricerca avanzata
 * Header header = new Header(bookService, mainContainer);
 *
 * // Handler avanzato che supporta entrambe le modalità
 * header.setSearchHandler(new Header.AdvancedSearchHandler() {
 *     {@literal @}Override
 *     public void accept(String query) {
 *         performSimpleSearch(query);
 *     }
 *
 *     {@literal @}Override
 *     public void handleAdvancedSearch(AdvancedSearchPanel.SearchResult result) {
 *         performAdvancedSearch(result);
 *     }
 * });
 *
 * // Il componente gestisce automaticamente:
 * // - Toggle tra modalità normale e avanzata
 * // - Conversione parametri avanzati in query
 * // - Overlay management e cleanup
 * // - Event handling e accessibilità
 * }</pre>
 *
 * <h3>Query Building e Formati:</h3>
 * <p>
 * Il sistema supporta diversi formati di query:
 * </p>
 * <ul>
 *   <li><code>title-only:termine</code> - Ricerca solo nel titolo</li>
 *   <li><code>author:nome</code> - Ricerca per autore</li>
 *   <li><code>year:2020-2023</code> - Filtro range anni</li>
 *   <li><code>termine semplice</code> - Ricerca generale</li>
 * </ul>
 *
 * <h3>Gestione Eventi e Accessibilità:</h3>
 * <ul>
 *   <li>Support per Enter key nel campo ricerca</li>
 *   <li>ESC key per chiusura ricerca avanzata</li>
 *   <li>Click outside per chiusura overlay</li>
 *   <li>Focus management per screen readers</li>
 *   <li>Tooltips informativi sui controlli</li>
 * </ul>
 *
 * <h3>Styling e Temi:</h3>
 * <p>
 * Il componente implementa un design system coerente:
 * </p>
 * <ul>
 *   <li>Palette colori dark theme</li>
 *   <li>Typography hierarchy con font System</li>
 *   <li>Border radius e spacing consistenti</li>
 *   <li>Hover effects e micro-animations</li>
 *   <li>Shadow effects per depth perception</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <ul>
 *   <li>Lazy initialization dei componenti overlay</li>
 *   <li>Event handler cleanup per memory management</li>
 *   <li>Platform.runLater per thread-safe UI updates</li>
 *   <li>Debouncing implicito tramite user action</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see BookService
 * @see AdvancedSearchPanel
 * @see AdvancedSearchHandler
 */
public class Header {
    /** Colore background principale dell'header */
    private static final String BG_COLOR = "#2c2c2e";

    /** Colore background per controlli di ricerca */
    private static final String SEARCH_BG = "#3a3a3c";

    /** Colore accent per elementi attivi */
    private static final String ACCENT_COLOR = "#007aff";

    /** Colore testo primario */
    private static final String TEXT_PRIMARY = "#ffffff";

    /** Colore testo secondario */
    private static final String TEXT_SECONDARY = "#8e8e93";

    /** Colore bordi */
    private static final String BORDER_COLOR = "#48484a";

    /** Campo di ricerca principale */
    private TextField searchField;

    /** Pulsante per attivare ricerca avanzata */
    private Button advancedSearchButton;

    /** Handler per gestire eventi di ricerca */
    private Consumer<String> searchHandler;

    /** Servizio per operazioni sui libri */
    private BookService bookService;

    /** Container principale per gestione overlay */
    private StackPane mainContainer;

    /** Flag che indica se la ricerca avanzata è aperta */
    private boolean isAdvancedSearchOpen = false;

    /** Pannello di ricerca avanzata correntemente attivo */
    private AdvancedSearchPanel currentAdvancedSearchPanel;

    /**
     * Costruttore vuoto per compatibilità con codice esistente.
     * <p>
     * Inizializza tutti i campi a {@code null} o valori di default.
     * Questo costruttore è mantenuto per retrocompatibilità ma richiede
     * configurazione manuale tramite setter methods.
     * </p>
     *
     * @deprecated Utilizzare {@link #Header(BookService, StackPane)} per inizializzazione completa
     */
    public Header() {
        this.bookService = null;
        this.mainContainer = null;
        this.searchField = null;
        this.advancedSearchButton = null;
        this.searchHandler = null;
        this.isAdvancedSearchOpen = false;
        this.currentAdvancedSearchPanel = null;
    }

    /**
     * Costruttore principale con inizializzazione completa.
     * <p>
     * Crea un'istanza Header completamente configurata con servizio libri
     * e container per overlay. Questo è il costruttore raccomandato per
     * nuove implementazioni.
     * </p>
     *
     * @param bookService servizio per operazioni sui libri
     * @param mainContainer container StackPane per gestione overlay
     * @throws IllegalArgumentException se bookService è {@code null}
     */
    public Header(BookService bookService, StackPane mainContainer) {
        if (bookService == null) {
            throw new IllegalArgumentException("BookService non può essere null");
        }
        this.bookService = bookService;
        this.mainContainer = mainContainer;
    }

    /**
     * Imposta il BookService per operazioni sui libri.
     * <p>
     * Metodo di configurazione per istanze create con costruttore vuoto.
     * Necessario per abilitare la funzionalità di ricerca avanzata.
     * </p>
     *
     * @param bookService servizio per operazioni sui libri
     */
    public void setBookService(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Crea l'header completo della finestra principale.
     * <p>
     * Factory method principale che costruisce l'intera interfaccia header,
     * includendo logo dell'applicazione, area di ricerca centralizzata, e
     * spazio per controlli futuri. Il layout è ottimizzato per responsive
     * design e coerenza visuale.
     * </p>
     *
     * <h4>Struttura dell'header:</h4>
     * <ol>
     *   <li><strong>Logo/Titolo:</strong> Brand identification a sinistra</li>
     *   <li><strong>Spacer Flessibile:</strong> Centra l'area di ricerca</li>
     *   <li><strong>Area Ricerca:</strong> Campo principale + pulsante avanzata</li>
     *   <li><strong>Spacer Finale:</strong> Bilancia il layout</li>
     *   <li><strong>Controlli Destri:</strong> Placeholder per funzionalità future</li>
     * </ol>
     *
     * <h4>Styling applicato:</h4>
     * <ul>
     *   <li>Background color con tema dark</li>
     *   <li>Border bottom per separazione visuale</li>
     *   <li>Padding consistente per spacing</li>
     *   <li>Center alignment per elementi principali</li>
     * </ul>
     *
     * @return {@link HBox} configurato come header completo
     */
    public HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER);
        header.setSpacing(20);
        header.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Logo/Titolo dell'app (sinistra)
        Label appTitle = createAppTitle();

        // Spacer centrale
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Area di ricerca (centro)
        HBox searchArea = createSearchArea();

        // Spacer finale
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Eventuali controlli aggiuntivi (destra) - placeholder per future funzionalità
        HBox rightControls = createRightControls();

        header.getChildren().addAll(appTitle, spacer1, searchArea, spacer2, rightControls);
        return header;
    }

    /**
     * Crea il titolo/logo dell'applicazione.
     * <p>
     * Genera il branding principale dell'applicazione con emoji libro
     * e nome "BABO Library". Utilizza font bold e colore primario per
     * massima visibilità e riconoscibilità del brand.
     * </p>
     *
     * @return {@link Label} configurato come titolo applicazione
     */
    private Label createAppTitle() {
        Label title = new Label("📚 BABO Library");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        return title;
    }

    /**
     * Crea l'area di ricerca completa con campo e pulsante avanzata.
     * <p>
     * Costruisce il sistema di ricerca centralizzato includendo campo
     * di ricerca principale e pulsante per ricerca avanzata. Il layout
     * è ottimizzato per usabilità e accessibilità.
     * </p>
     *
     * <h4>Componenti inclusi:</h4>
     * <ul>
     *   <li>Campo ricerca con placeholder informativo</li>
     *   <li>Pulsante ricerca avanzata con tooltip</li>
     *   <li>Spacing consistente tra elementi</li>
     *   <li>Allineamento centrato per bilanciamento visuale</li>
     * </ul>
     *
     * <h4>Configurazioni responsive:</h4>
     * <ul>
     *   <li>Larghezza massima per prevenire stretch eccessivo</li>
     *   <li>Larghezza preferita per layout standard</li>
     *   <li>Spacing proporzionale tra controlli</li>
     * </ul>
     *
     * @return {@link HBox} configurato come area di ricerca
     */
    private HBox createSearchArea() {
        HBox searchArea = new HBox(8);
        searchArea.setAlignment(Pos.CENTER);
        searchArea.setMaxWidth(500);
        searchArea.setPrefWidth(400);

        // Campo di ricerca principale
        searchField = createSearchField();

        // Pulsante ricerca avanzata
        advancedSearchButton = createAdvancedSearchButton();

        searchArea.getChildren().addAll(searchField, advancedSearchButton);

        return searchArea;
    }

    /**
     * Crea il campo di ricerca principale con styling e event handling.
     * <p>
     * Genera il campo di input primario per le ricerche con placeholder
     * informativo, styling dark theme, e gestione evento Enter per
     * ricerca immediata. Include tutte le configurazioni per UX ottimale.
     * </p>
     *
     * <h4>Features implementate:</h4>
     * <ul>
     *   <li>Placeholder text con emoji e descrizione</li>
     *   <li>Enter key handling per ricerca immediata</li>
     *   <li>Dark theme styling consistente</li>
     *   <li>Border radius per aspetto moderno</li>
     *   <li>Padding interno per comfort di typing</li>
     * </ul>
     *
     * <h4>Styling applicato:</h4>
     * <ul>
     *   <li>Background scuro con contrasto leggibile</li>
     *   <li>Border color coordinato con tema</li>
     *   <li>Border radius per corner smussati</li>
     *   <li>Font size ottimizzato per leggibilità</li>
     * </ul>
     *
     * @return {@link TextField} configurato come campo ricerca
     */
    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("🔍 Cerca libri per titolo o autore...");
        field.setPrefWidth(350);
        field.setStyle(
                "-fx-background-color: " + SEARCH_BG + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-background-radius: 20px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 20px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-font-size: 14px;"
        );

        // Gestisce ricerca al press di Enter
        field.setOnAction(e -> performSearch());

        return field;
    }

    /**
     * Crea il pulsante per attivare la ricerca avanzata.
     * <p>
     * Genera un pulsante circolare con emoji ingranaggio per accesso alla
     * ricerca avanzata. Include hover effects, tooltip informativo, e
     * gestione completa degli stati visivi per apertura/chiusura.
     * </p>
     *
     * <h4>Stati del pulsante:</h4>
     * <ul>
     *   <li><strong>Normale:</strong> Emoji ingranaggio con colori neutri</li>
     *   <li><strong>Hover:</strong> Background accent color per feedback</li>
     *   <li><strong>Attivo:</strong> Colore rosso con emoji X per chiusura</li>
     * </ul>
     *
     * <h4>Features interattive:</h4>
     * <ul>
     *   <li>Tooltip descrittivo per accessibilità</li>
     *   <li>Mouse enter/exit handlers per hover effects</li>
     *   <li>Click handler per toggle ricerca avanzata</li>
     *   <li>Dimensioni fisse per consistenza layout</li>
     * </ul>
     *
     * <h4>Configurazioni styling:</h4>
     * <ul>
     *   <li>Shape circolare con border radius</li>
     *   <li>Text clipping per prevenire overflow</li>
     *   <li>Emoji font family per rendering consistente</li>
     *   <li>Cursor hand per indicare clickability</li>
     * </ul>
     *
     * @return {@link Button} configurato per ricerca avanzata
     */
    private Button createAdvancedSearchButton() {
        Button button = new Button("⚙️");
        button.setTooltip(new Tooltip("Ricerca Avanzata"));

        // Rimuovi i puntini
        button.setTextOverrun(OverrunStyle.CLIP);
        button.setEllipsisString("");
        button.setWrapText(false);
        button.setAlignment(Pos.CENTER);

        button.setStyle(
                "-fx-background-color: " + SEARCH_BG + ";" +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-background-radius: 19px;" +
                        "-fx-border-radius: 19px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-family: 'Segoe UI Emoji', sans-serif;" +
                        "-fx-min-width: 40px;" +
                        "-fx-min-height: 40px;" +
                        "-fx-max-width: 40px;" +
                        "-fx-max-height: 40px;" +
                        "-fx-pref-width: 40px;" +
                        "-fx-pref-height: 40px;" +
                        "-fx-padding: 0;" +
                        "-fx-text-overrun: clip;"
        );

        button.setOnMouseEntered(e -> {
            if (!isAdvancedSearchOpen) {
                button.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 19px;" +
                                "-fx-border-radius: 19px;" +
                                "-fx-border-color: " + ACCENT_COLOR + ";" +
                                "-fx-border-width: 1px;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-family: 'Segoe UI Emoji', sans-serif;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-width: 40px;" +
                                "-fx-max-height: 40px;" +
                                "-fx-pref-width: 40px;" +
                                "-fx-pref-height: 40px;" +
                                "-fx-padding: 0;" +
                                "-fx-text-overrun: clip;"
                );
            }
        });

        button.setOnMouseExited(e -> {
            if (!isAdvancedSearchOpen) {
                updateAdvancedSearchButtonStyle(false);
            }
        });

        button.setOnAction(e -> toggleAdvancedSearch());
        return button;
    }

    /**
     * Gestisce il toggle tra apertura e chiusura della ricerca avanzata.
     * <p>
     * Metodo centrale per la gestione dello stato della ricerca avanzata,
     * determinando se aprire o chiudere il pannello basandosi sullo stato
     * corrente. Fornisce un'interfaccia unificata per il controllo del popup.
     * </p>
     */
    private void toggleAdvancedSearch() {
        if (isAdvancedSearchOpen) {
            closeAdvancedSearch();
        } else {
            openAdvancedSearch();
        }
    }

    /**
     * Apre il popup di ricerca avanzata con overlay system.
     * <p>
     * Implementa l'apertura del pannello di ricerca avanzata tramite overlay
     * semi-trasparente, gestendo la creazione del pannello, configurazione
     * callback, e integrazione con il container principale. Include gestione
     * completa degli errori e rollback in caso di fallimento.
     * </p>
     *
     * <h4>Processo di apertura:</h4>
     * <ol>
     *   <li>Verifica prerequisiti (BookService, mainContainer)</li>
     *   <li>Aggiornamento stato e styling pulsante</li>
     *   <li>Creazione AdvancedSearchPanel con callback</li>
     *   <li>Creazione overlay con gestione eventi</li>
     *   <li>Aggiunta al container principale</li>
     * </ol>
     *
     * <h4>Callback configurati:</h4>
     * <ul>
     *   <li><strong>onSearchExecuted:</strong> Gestione risultati ricerca</li>
     *   <li><strong>onClosePanel:</strong> Chiusura pannello da controlli interni</li>
     * </ul>
     *
     * <h4>Event handling overlay:</h4>
     * <ul>
     *   <li>Click outside per chiusura automatica</li>
     *   <li>ESC key per chiusura rapida</li>
     *   <li>Focus management per accessibilità</li>
     * </ul>
     *
     * <h4>Error handling:</h4>
     * <ul>
     *   <li>Validazione prerequisiti con early return</li>
     *   <li>Try-catch per gestione eccezioni</li>
     *   <li>Rollback stato in caso di errore</li>
     *   <li>Logging dettagliato per debugging</li>
     * </ul>
     */
    private void openAdvancedSearch() {
        if (isAdvancedSearchOpen) {
            System.out.println("⚠️ Ricerca avanzata già aperta");
            return;
        }

        if (bookService == null || mainContainer == null) {
            System.err.println("❌ BookService o MainContainer non impostati");
            return;
        }

        System.out.println("🔍 Apertura ricerca avanzata...");

        try {
            // Aggiorna stato
            isAdvancedSearchOpen = true;
            updateAdvancedSearchButtonStyle(true);

            // Crea il pannello di ricerca avanzata
            currentAdvancedSearchPanel = new AdvancedSearchPanel(bookService);

            // Callback per quando viene eseguita una ricerca
            currentAdvancedSearchPanel.setOnSearchExecuted(result -> {
                System.out.println("✅ Ricerca avanzata eseguita: " + result.getBooks().size() + " risultati");
                handleAdvancedSearchResult(result);
                closeAdvancedSearch(); // Chiudi il popup dopo la ricerca
            });

            // Callback per la chiusura del pannello
            currentAdvancedSearchPanel.setOnClosePanel(() -> {
                closeAdvancedSearch();
            });

            // Crea overlay CORRETTO che non blocca i click sui controlli interni
            StackPane overlay = createPopupOverlay();
            overlay.getChildren().add(currentAdvancedSearchPanel);
            StackPane.setAlignment(currentAdvancedSearchPanel, Pos.CENTER);

            // Aggiungi al container principale
            mainContainer.getChildren().add(overlay);

            System.out.println("✅ Popup ricerca avanzata aperto");

        } catch (Exception e) {
            System.err.println("❌ Errore apertura ricerca avanzata: " + e.getMessage());
            e.printStackTrace();

            // Rollback in caso di errore
            isAdvancedSearchOpen = false;
            updateAdvancedSearchButtonStyle(false);
            currentAdvancedSearchPanel = null;
        }
    }

    /**
     * Crea overlay per popup che gestisce correttamente gli eventi mouse.
     * <p>
     * Genera un overlay StackPane semi-trasparente che permette l'interazione
     * con i controlli interni mentre gestisce click outside per chiusura.
     * Implementa event handling sofisticato per distinguere tra click su
     * overlay e click su controlli figli.
     * </p>
     *
     * <h4>Configurazioni overlay:</h4>
     * <ul>
     *   <li>Background semi-trasparente per effetto modal</li>
     *   <li>pickOnBounds false per permettere eventi figli</li>
     *   <li>Focus traversable per gestione ESC key</li>
     * </ul>
     *
     * <h4>Event handling implementato:</h4>
     * <ul>
     *   <li><strong>Mouse Click:</strong> Chiusura solo se click su background</li>
     *   <li><strong>Key Press:</strong> ESC key per chiusura rapida</li>
     *   <li><strong>Focus:</strong> Automatic focus per keyboard navigation</li>
     * </ul>
     *
     * <h4>Logica click detection:</h4>
     * <p>
     * Utilizza confronto tra event.getTarget() e event.getSource() per
     * determinare se il click è effettivamente sull'overlay di background
     * o su un controllo interno, prevenendo chiusure accidentali.
     * </p>
     *
     * @return {@link StackPane} configurato come overlay per popup
     */
    private StackPane createPopupOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // Permetti ai controlli figli di ricevere eventi mouse
        overlay.setPickOnBounds(false);

        overlay.setOnMouseClicked(event -> {
            System.out.println("🖱️ Click rilevato su overlay");
            System.out.println("   - Target: " + event.getTarget().getClass().getSimpleName());
            System.out.println("   - Source: " + event.getSource().getClass().getSimpleName());

            // Chiudi solo se il click è DAVVERO sull'overlay di sfondo
            // e non su un controllo interno (come il pulsante X)
            if (event.getTarget() == overlay && event.getSource() == overlay) {
                System.out.println("🖱️ Click confermato su sfondo overlay - chiusura");
                event.consume();
                Platform.runLater(() -> closeAdvancedSearch());
            } else {
                System.out.println("🖱️ Click su controllo interno - NON chiudo overlay");
                // NON consumare l'evento - lascia che arrivi al controllo di destinazione
            }
        });

        overlay.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                System.out.println("⌨️ ESC premuto - chiusura ricerca avanzata");
                event.consume();
                Platform.runLater(() -> closeAdvancedSearch());
            }
        });

        overlay.setFocusTraversable(true);
        Platform.runLater(() -> overlay.requestFocus());

        return overlay;
    }

    /**
     * Chiude il popup di ricerca avanzata e ripristina stato normale.
     * <p>
     * Implementa la chiusura completa del sistema di ricerca avanzata,
     * gestendo cleanup del pannello, rimozione overlay, aggiornamento
     * stato UI, e prevenzione memory leaks tramite cleanup appropriato
     * degli event handlers.
     * </p>
     *
     * <h4>Processo di chiusura:</h4>
     * <ol>
     *   <li>Verifica stato attuale per evitare operazioni duplicate</li>
     *   <li>Aggiornamento flag stato e styling pulsante</li>
     *   <li>Cleanup pannello ricerca avanzata corrente</li>
     *   <li>Rimozione overlay dal container principale</li>
     *   <li>Reset riferimenti per garbage collection</li>
     * </ol>
     *
     * <h4>Memory management:</h4>
     * <ul>
     *   <li>Cleanup event handlers tramite AdvancedSearchPanel.cleanup()</li>
     *   <li>Rimozione riferimenti per permettere garbage collection</li>
     *   <li>Rimozione selettiva overlay dal container</li>
     * </ul>
     *
     * <h4>Sicurezza operazioni:</h4>
     * <ul>
     *   <li>Early return se ricerca non attiva</li>
     *   <li>Try-catch per gestione eccezioni</li>
     *   <li>Null checks per operazioni sicure</li>
     *   <li>Logging per debugging e monitoring</li>
     * </ul>
     */
    public void closeAdvancedSearch() {
        if (!isAdvancedSearchOpen) {
            return;
        }

        System.out.println("🔍 Chiusura ricerca avanzata...");

        try {
            // 1. Aggiorna stato
            isAdvancedSearchOpen = false;
            updateAdvancedSearchButtonStyle(false);

            // 2. Cleanup del pannello corrente
            if (currentAdvancedSearchPanel != null) {
                // Cleanup degli event handlers per evitare memory leak
                currentAdvancedSearchPanel.cleanup();
                currentAdvancedSearchPanel = null;
            }

            // 3. Rimuovi overlay dal container principale
            if (mainContainer != null) {
                // Rimuovi tutti gli overlay di ricerca avanzata
                mainContainer.getChildren().removeIf(node -> {
                    if (node instanceof StackPane) {
                        StackPane stackPane = (StackPane) node;
                        // Controlla se contiene un AdvancedSearchPanel
                        return stackPane.getChildren().stream()
                                .anyMatch(child -> child instanceof AdvancedSearchPanel);
                    }
                    return false;
                });
            }

            System.out.println("✅ Ricerca avanzata chiusa correttamente");

        } catch (Exception e) {
            System.err.println("❌ Errore nella chiusura ricerca avanzata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Aggiorna lo stile del pulsante ricerca avanzata in base allo stato.
     * <p>
     * Gestisce la transizione visuale del pulsante ricerca avanzata tra
     * stato normale (ingranaggio) e stato attivo (X rossa), applicando
     * styling appropriato per ogni stato e modificando il contenuto
     * testuale per riflettere l'azione disponibile.
     * </p>
     *
     * <h4>Stati supportati:</h4>
     * <ul>
     *   <li><strong>Normale (isOpen=false):</strong> Emoji ingranaggio, colori neutri</li>
     *   <li><strong>Attivo (isOpen=true):</strong> Emoji X, background rosso</li>
     * </ul>
     *
     * <h4>Styling normale:</h4>
     * <ul>
     *   <li>Background colore ricerca coordinato</li>
     *   <li>Testo colore primario per visibilità</li>
     *   <li>Border color neutro per integrazione</li>
     *   <li>Emoji ingranaggio per indicare funzione</li>
     * </ul>
     *
     * <h4>Styling attivo:</h4>
     * <ul>
     *   <li>Background rosso per indicare chiusura</li>
     *   <li>Testo bianco per contrasto massimo</li>
     *   <li>Border color coordinato</li>
     *   <li>Emoji X per indicare azione chiusura</li>
     *   <li>Font weight bold per enfasi</li>
     * </ul>
     *
     * @param isOpen {@code true} se ricerca avanzata è aperta, {@code false} altrimenti
     */
    private void updateAdvancedSearchButtonStyle(boolean isOpen) {
        if (advancedSearchButton == null) return;

        advancedSearchButton.setText("⚙️");

        if (isOpen) {
            advancedSearchButton.setStyle(
                    "-fx-background-color: #ff3b30;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 19px;" +
                            "-fx-border-radius: 19px;" +
                            "-fx-border-color: #ff3b30;" +
                            "-fx-border-width: 1px;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-family: 'Segoe UI Emoji', sans-serif;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-min-width: 40px;" +
                            "-fx-min-height: 40px;" +
                            "-fx-max-width: 40px;" +
                            "-fx-max-height: 40px;" +
                            "-fx-pref-width: 40px;" +
                            "-fx-pref-height: 40px;" +
                            "-fx-padding: 0;" +
                            "-fx-text-overrun: clip;"
            );
            advancedSearchButton.setText("✕");
        } else {
            advancedSearchButton.setStyle(
                    "-fx-background-color: " + SEARCH_BG + ";" +
                            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                            "-fx-background-radius: 19px;" +
                            "-fx-border-radius: 19px;" +
                            "-fx-border-color: " + BORDER_COLOR + ";" +
                            "-fx-border-width: 1px;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-family: 'Segoe UI Emoji', sans-serif;" +
                            "-fx-cursor: hand;" +
                            "-fx-min-width: 40px;" +
                            "-fx-min-height: 40px;" +
                            "-fx-max-width: 40px;" +
                            "-fx-max-height: 40px;" +
                            "-fx-pref-width: 40px;" +
                            "-fx-pref-height: 40px;" +
                            "-fx-padding: 0;" +
                            "-fx-text-overrun: clip;"
            );
            advancedSearchButton.setText("⚙️");
        }
    }

    /**
     * Crea i controlli destri dell'header per funzionalità future.
     * <p>
     * Placeholder per controlli aggiuntivi nell'area destra dell'header.
     * Attualmente restituisce container vuoto ma fornisce struttura per
     * future implementazioni come profilo utente, notifiche, o settings.
     * </p>
     *
     * <h4>Configurazione container:</h4>
     * <ul>
     *   <li>Allineamento a destra per posizionamento corretto</li>
     *   <li>Spacing predefinito per elementi futuri</li>
     *   <li>Layout HBox per disposizione orizzontale</li>
     * </ul>
     *
     * @return {@link HBox} vuoto configurato per controlli destri
     */
    private HBox createRightControls() {
        HBox rightControls = new HBox(10);
        rightControls.setAlignment(Pos.CENTER_RIGHT);
        return rightControls;
    }

    /**
     * Esegue la ricerca basata sul contenuto del campo di input.
     * <p>
     * Metodo principale per l'esecuzione di ricerche semplici, gestendo
     * validazione input, invocazione search handler, e logging per
     * debugging. Include controlli di sicurezza e gestione errori.
     * </p>
     *
     * <h4>Processo di ricerca:</h4>
     * <ol>
     *   <li>Estrazione e pulizia query dal campo input</li>
     *   <li>Validazione query non vuota</li>
     *   <li>Verifica presenza search handler</li>
     *   <li>Invocazione handler con gestione eccezioni</li>
     *   <li>Logging dettagliato per monitoring</li>
     * </ol>
     *
     * <h4>Validazioni implementate:</h4>
     * <ul>
     *   <li>Campo ricerca non null</li>
     *   <li>Query non vuota dopo trim</li>
     *   <li>Search handler configurato</li>
     * </ul>
     *
     * <h4>Error handling:</h4>
     * <ul>
     *   <li>Early return per validazioni fallite</li>
     *   <li>Try-catch per eccezioni handler</li>
     *   <li>Logging errori con stack trace</li>
     * </ul>
     */
    private void performSearch() {
        String query = searchField != null ? searchField.getText().trim() : "";
        System.out.println("🔍 [HEADER] performSearch chiamato con query: '" + query + "'");

        if (query.isEmpty()) {
            System.out.println("⚠️ [HEADER] Query vuota, ricerca non eseguita");
            return;
        }

        if (searchHandler == null) {
            System.err.println("❌ [HEADER] SearchHandler non impostato!");
            return;
        }

        try {
            System.out.println("📤 [HEADER] Invio query a searchHandler...");
            searchHandler.accept(query);
            System.out.println("✅ [HEADER] Query inviata con successo");
        } catch (Exception e) {
            System.err.println("❌ [HEADER] Errore durante invio query: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gestisce il risultato della ricerca avanzata e conversione in query.
     * <p>
     * Processa i risultati del pannello di ricerca avanzata, gestendo sia
     * handler specializzati che fallback a query string. Include aggiornamento
     * campo ricerca per mostrare query utilizzata e gestione errori robusta.
     * </p>
     *
     * <h4>Strategia di handling:</h4>
     * <ol>
     *   <li>Verifica presenza search handler</li>
     *   <li>Costruzione query string dai parametri</li>
     *   <li>Aggiornamento campo ricerca con query</li>
     *   <li>Tentativo handler specializzato AdvancedSearchHandler</li>
     *   <li>Fallback a handler standard con query string</li>
     * </ol>
     *
     * <h4>Handler specializzato:</h4>
     * <p>
     * Se il search handler implementa {@link AdvancedSearchHandler}, viene
     * utilizzato il metodo handleAdvancedSearch per passare direttamente
     * il SearchResult completo, altrimenti si converte in query string.
     * </p>
     *
     * <h4>Fallback strategy:</h4>
     * <ul>
     *   <li>Conversione parametri in query string strutturata</li>
     *   <li>Invocazione handler standard con query convertita</li>
     *   <li>Gestione errori con fallback ulteriore</li>
     * </ul>
     *
     * @param result oggetto SearchResult dal pannello ricerca avanzata
     */
    private void handleAdvancedSearchResult(AdvancedSearchPanel.SearchResult result) {
        if (searchHandler == null) {
            System.err.println("❌ SearchHandler non impostato");
            return;
        }

        // Costruisci una query di ricerca basata sui parametri della ricerca avanzata
        String searchQuery = buildSearchQuery(result);

        if (!searchQuery.isEmpty()) {
            // Aggiorna il campo di ricerca per mostrare cosa è stato cercato
            Platform.runLater(() -> {
                if (searchField != null) {
                    searchField.setText(searchQuery);
                }
            });
        }

        // Passa il risultato direttamente se il searchHandler supporta SearchResult
        // Altrimenti costruisci una query stringa
        try {
            // Prova a chiamare un metodo specifico per i risultati avanzati
            if (searchHandler instanceof AdvancedSearchHandler) {
                ((AdvancedSearchHandler) searchHandler).handleAdvancedSearch(result);
            } else {
                // Fallback: usa la query stringa
                searchHandler.accept(searchQuery);
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nel passare i risultati della ricerca: " + e.getMessage());
            // Fallback
            if (!searchQuery.isEmpty()) {
                searchHandler.accept(searchQuery);
            }
        }
    }

    /**
     * Interfaccia estesa per handler di ricerca avanzata.
     * <p>
     * Estende {@link Consumer}<{@link String}> per fornire supporto aggiuntivo
     * per gestione diretta di risultati ricerca avanzata, permettendo
     * implementazioni più sofisticate che possono processare direttamente
     * i parametri strutturati anziché solo query string.
     * </p>
     *
     * <h4>Utilizzo raccomandato:</h4>
     * <pre>{@code
     * header.setSearchHandler(new Header.AdvancedSearchHandler() {
     *     public void accept(String query) {
     *         // Gestione ricerca semplice
     *         performSimpleSearch(query);
     *     }
     *
     *     public void handleAdvancedSearch(AdvancedSearchPanel.SearchResult result) {
     *         // Gestione ricerca avanzata con accesso a tutti i parametri
     *         performAdvancedSearch(result.getTitleQuery(),
     *                              result.getAuthorQuery(),
     *                              result.getYearFrom(),
     *                              result.getYearTo());
     *     }
     * });
     * }</pre>
     *
     * @see AdvancedSearchPanel.SearchResult
     * @see Consumer
     */
    public interface AdvancedSearchHandler extends Consumer<String> {
        /**
         * Gestisce risultati ricerca avanzata con accesso a parametri strutturati.
         * <p>
         * Metodo specializzato per processare risultati ricerca avanzata,
         * fornendo accesso diretto a tutti i parametri configurati dall'utente
         * nel pannello ricerca avanzata.
         * </p>
         *
         * @param result oggetto contenente parametri e risultati ricerca avanzata
         */
        void handleAdvancedSearch(AdvancedSearchPanel.SearchResult result);
    }

    /**
     * Costruisce una query string dai parametri della ricerca avanzata.
     * <p>
     * Converte i parametri strutturati del SearchResult in una query string
     * utilizzabile dal sistema di ricerca standard, implementando un formato
     * di query specializzato che supporta ricerca per titolo, autore, e
     * filtri temporali.
     * </p>
     *
     * <h4>Formati query supportati:</h4>
     * <ul>
     *   <li><code>title-only:termine</code> - Ricerca esclusivamente nel titolo</li>
     *   <li><code>author:nome year:2020-2023</code> - Ricerca autore con filtro anni</li>
     *   <li><code>author:nome year:2020</code> - Ricerca autore con anno specifico</li>
     * </ul>
     *
     * <h4>Logica di costruzione:</h4>
     * <ol>
     *   <li>Determina tipo ricerca (Titolo vs Autore)</li>
     *   <li>Applica prefisso appropriato (title-only: vs author:)</li>
     *   <li>Aggiunge filtri temporali se specificati</li>
     *   <li>Combina parametri con spazi per parsing backend</li>
     * </ol>
     *
     * <h4>Gestione range anni:</h4>
     * <ul>
     *   <li>Singolo anno: <code>year:2020</code></li>
     *   <li>Range: <code>year:2020-2023</code></li>
     *   <li>Da anno: <code>year:2020-</code> (implementazione futura)</li>
     *   <li>Fino anno: <code>year:-2023</code> (implementazione futura)</li>
     * </ul>
     *
     * @param result oggetto SearchResult contenente parametri ricerca
     * @return query string formattata per il sistema di ricerca
     */
    private String buildSearchQuery(AdvancedSearchPanel.SearchResult result) {
        StringBuilder query = new StringBuilder();

        if (result.getSearchType().contains("Titolo") && !result.getTitleQuery().isEmpty()) {
            query.append("title-only:").append(result.getTitleQuery());

        } else if (result.getSearchType().contains("Autore") && !result.getAuthorQuery().isEmpty()) {
            query.append("author:").append(result.getAuthorQuery());

            // Aggiungi filtro anno
            if (!result.getYearFrom().isEmpty() || !result.getYearTo().isEmpty()) {
                query.append(" year:");
                if (!result.getYearFrom().isEmpty()) {
                    query.append(result.getYearFrom());
                }
                if (!result.getYearTo().isEmpty()) {
                    if (!result.getYearFrom().isEmpty()) {
                        query.append("-");
                    }
                    query.append(result.getYearTo());
                }
            }
        }

        String finalQuery = query.toString();
        System.out.println("🔍 Query costruita: '" + finalQuery + "'");
        return finalQuery;
    }

    /**
     * Configura il gestore per eventi di ricerca.
     * <p>
     * Imposta il callback che verrà invocato quando l'utente esegue una ricerca,
     * sia tramite campo principale che ricerca avanzata (se non implementa
     * AdvancedSearchHandler). Essential per collegare l'header al sistema
     * di ricerca dell'applicazione.
     * </p>
     *
     * @param handler callback per gestire query di ricerca
     */
    public void setSearchHandler(Consumer<String> handler) {
        this.searchHandler = handler;
    }

    /**
     * Restituisce il servizio libri configurato.
     * <p>
     * Getter per accesso al BookService utilizzato dal componente header,
     * utile per debugging e integrazioni avanzate.
     * </p>
     *
     * @return servizio libri configurato, può essere {@code null}
     */
    public BookService getBookService() {
        return bookService;
    }

    /**
     * Pulisce il campo di ricerca rimuovendo il testo corrente.
     * <p>
     * Utility method per reset del campo ricerca, utile dopo esecuzione
     * ricerche o per implementare funzionalità di "nuova ricerca".
     * Include null check per sicurezza operazioni.
     * </p>
     */
    public void clearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
    }
}