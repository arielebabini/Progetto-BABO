package org.BABO.client.ui.Search;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.BABO.client.ui.BooksClient;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;
import org.BABO.shared.model.Category;

import java.util.ArrayList;
import java.util.List;
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
 * <li><strong>Visualizzazione Categoria:</strong> Header descrittivo con navigazione breadcrumb</li>
 * <li><strong>Griglia Libri:</strong> Layout responsive per browsing ottimale</li>
 * <li><strong>Navigazione Contestuale:</strong> Integrazione con dettagli libro e lista di navigazione</li>
 * <li><strong>Gestione Stati:</strong> Loading, successo, errore, e risultati vuoti</li>
 * <li><strong>Descrizioni Intelligenti:</strong> Descrizioni contextual per categorie predefinite</li>
 * <li><strong>Ricerca Filtrata:</strong> Caricamento asincrono con filtri per categoria</li>
 * </ul>
 *
 * <h3>Architettura di Navigazione:</h3>
 * <p>
 * La classe implementa un sistema di navigazione a breadcrumb che permette:
 * </p>
 * <ul>
 * <li>Navigazione back alla vista principale</li>
 * <li>Context preservation per la lista di libri corrente</li>
 * <li>Deep linking ai dettagli di singoli libri</li>
 * <li>Mantenimento dello stato di navigazione</li>
 * </ul>
 *
 * <h3>Sistema di Layout Responsive:</h3>
 * <p>
 * Utilizza FlowPane con property binding per creare un layout che si adatta
 * automaticamente a diverse risoluzioni e dimensioni dello schermo:
 * </p>
 * <ul>
 * <li>Grid dinamica con spacing ottimizzato</li>
 * <li>Book cards di dimensioni standardizzate</li>
 * <li>Scroll verticale fluido per liste lunghe</li>
 * <li>Header fisso per context costante</li>
 * </ul>
 *
 * <h3>Gestione Contenuti Dinamici:</h3>
 * <p>
 * La vista gestisce intelligentemente diversi tipi di contenuto:
 * </p>
 * <ul>
 * <li><strong>Descrizioni Categoria:</strong> Mapping predefinito per categorie comuni</li>
 * <li><strong>Fallback Content:</strong> Gestione graceful di dati mancanti</li>
 * <li><strong>Loading States:</strong> Feedback visivo durante operazioni asincrone</li>
 * <li><strong>Error Handling:</strong> Messaggi user-friendly per problemi di rete</li>
 * </ul>
 *
 * <h3>Integrazione con Servizi:</h3>
 * <p>
 * La vista si integra con diversi servizi per funzionalità complete:
 * </p>
 * <ul>
 * <li>{@link BookService} per ricerca e caricamento libri</li>
 * <li>{@link AuthenticationManager} per gestione permessi utente</li>
 * <li>{@link BooksClient} per navigazione ai dettagli</li>
 * <li>{@link ImageUtils} per gestione sicura delle immagini</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Creazione vista categoria
 * Category sciFiCategory = new Category("Fantascienza", "Esplorazioni del futuro");
 * BookService bookService = new BookService();
 *
 * CategoryView categoryView = new CategoryView(
 * sciFiCategory,
 * bookService,
 * book -> System.out.println("Libro cliccato: " + book.getTitle())
 * );
 *
 * // Configurazione navigazione
 * categoryView.setOnBackCallback(() -> {
 * navigationManager.goBack();
 * analytics.trackCategoryExit(sciFiCategory.getName());
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
 * <li>Descrizioni categoria facilmente estensibili tramite switch case</li>
 * <li>Layout modificabile attraverso override dei metodi di creazione</li>
 * <li>Styling personalizzabile tramite CSS classes</li>
 * <li>Event handling configurabile per diverse logiche di navigazione</li>
 * </ul>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <ul>
 * <li>Caricamento asincrono per non bloccare UI thread</li>
 * <li>Lazy loading delle immagini per performance</li>
 * <li>Memory management per liste grandi</li>
 * <li>Event debouncing per interazioni rapide</li>
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
public class AdvancedSearchPanel extends VBox {

    // Costanti per i colori
    private static final String BG_SECONDARY = "#2c2c2e";
    private static final String BG_CONTROL = "#3a3a3c";
    private static final String TEXT_PRIMARY = "#ffffff";
    private static final String TEXT_SECONDARY = "#8e8e93";
    private static final String ACCENT_COLOR = "#007aff";
    private static final String BORDER_COLOR = "#38383a";

    // Servizi
    private final BookService bookService;

    // Componenti UI
    private ComboBox<String> searchTypeCombo;
    private TextField titleField;
    private TextField authorField;
    private TextField yearFromField;
    private TextField yearToField;
    private VBox dynamicFieldsContainer;
    private Button searchButton;
    private Button closeButton;

    // Callback
    private Consumer<SearchResult> onSearchExecuted;
    private Runnable onClosePanel;

    // Stati
    private boolean isSearching = false;

    /**
     * Classe per i risultati della ricerca
     */
    public static class SearchResult {
        private final List<Book> books;
        private final String searchType;
        private final String titleQuery;
        private final String authorQuery;
        private final String yearFrom;
        private final String yearTo;
        private final String description;

        /**
         * Costruisce un oggetto {@link SearchResult} che incapsula i risultati di una ricerca avanzata.
         * <p>
         * Questo costruttore inizializza l'oggetto con la lista di libri trovati e tutti i parametri
         * di ricerca utilizzati. Gestisce i casi in cui i parametri sono nulli, assegnando loro
         * valori predefiniti per garantire la stabilità e prevenire eccezioni. Dopo l'inizializzazione
         * dei campi, invoca il metodo {@link #buildDescription()} per generare automaticamente una
         * descrizione testuale della ricerca, utile per fornire un feedback immediato all'utente.
         * </p>
         *
         * @param books       La lista dei libri risultanti dalla ricerca. Può essere null.
         * @param searchType  Il tipo di ricerca eseguito (es. "Ricerca per Titolo"). Può essere null.
         * @param titleQuery  La stringa di ricerca per il titolo del libro. Può essere null.
         * @param authorQuery La stringa di ricerca per l'autore. Può essere null.
         * @param yearFrom    L'anno di inizio del range di ricerca per anno. Può essere null.
         * @param yearTo      L'anno di fine del range di ricerca per anno. Può essere null.
         */
        public SearchResult(List<Book> books, String searchType, String titleQuery,
                            String authorQuery, String yearFrom, String yearTo) {
            this.books = books != null ? books : new ArrayList<>();
            this.searchType = searchType != null ? searchType : "";
            this.titleQuery = titleQuery != null ? titleQuery : "";
            this.authorQuery = authorQuery != null ? authorQuery : "";
            this.yearFrom = yearFrom != null ? yearFrom : "";
            this.yearTo = yearTo != null ? yearTo : "";
            this.description = buildDescription();
        }

        /**
         * Crea una stringa descrittiva e leggibile che riassume i criteri della ricerca avanzata.
         * <p>
         * Questo metodo di utilità costruisce un riassunto dinamico dei parametri di ricerca,
         * come titolo, autore e intervallo di anni, per fornire un feedback immediato all'utente.
         * In assenza di filtri specifici, la descrizione include il tipo di ricerca predefinita.
         * Infine, aggiunge il conteggio totale dei risultati trovati.
         * </p>
         *
         * @return Una stringa formattata che descrive la ricerca eseguita e il numero di risultati.
         */
        private String buildDescription() {
            StringBuilder desc = new StringBuilder("Risultati ricerca avanzata");

            boolean hasFilters = false;

            if (!titleQuery.isEmpty()) {
                desc.append(": Titolo '").append(titleQuery).append("'");
                hasFilters = true;
            }

            if (!authorQuery.isEmpty()) {
                if (hasFilters) {
                    desc.append(", ");
                } else {
                    desc.append(": ");
                }
                desc.append("Autore '").append(authorQuery).append("'");
                hasFilters = true;
            }

            if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
                if (hasFilters) {
                    desc.append(", ");
                } else {
                    desc.append(": ");
                }
                desc.append("Anno ");
                if (!yearFrom.isEmpty() && !yearTo.isEmpty()) {
                    desc.append("dal ").append(yearFrom).append(" al ").append(yearTo);
                } else if (!yearFrom.isEmpty()) {
                    desc.append("dal ").append(yearFrom);
                } else {
                    desc.append("fino al ").append(yearTo);
                }
                hasFilters = true;
            }

            if (!hasFilters) {
                desc.append(" - ").append(searchType);
            }

            desc.append(" (").append(books.size()).append(" risultati)");

            return desc.toString();
        }

        // Getters
        /**
         * Restituisce la lista dei libri trovati nella ricerca.
         *
         * @return Una {@link List} di oggetti {@link Book}.
         */
        public List<Book> getBooks() {
            return books;
        }

        /**
         * Restituisce il tipo di ricerca selezionato.
         *
         * @return Una {@link String} che rappresenta il tipo di ricerca.
         */
        public String getSearchType() {
            return searchType;
        }

        /**
         * Restituisce la stringa di ricerca utilizzata per il titolo.
         *
         * @return Una {@link String} con il criterio di ricerca per il titolo.
         */
        public String getTitleQuery() {
            return titleQuery;
        }

        /**
         * Restituisce la stringa di ricerca utilizzata per l'autore.
         *
         * @return Una {@link String} con il criterio di ricerca per l'autore.
         */
        public String getAuthorQuery() {
            return authorQuery;
        }

        /**
         * Restituisce l'anno di inizio del range di ricerca.
         *
         * @return Una {@link String} che rappresenta l'anno di inizio.
         */
        public String getYearFrom() {
            return yearFrom;
        }

        /**
         * Restituisce l'anno di fine del range di ricerca.
         *
         * @return Una {@link String} che rappresenta l'anno di fine.
         */
        public String getYearTo() {
            return yearTo;
        }

        /**
         * Restituisce la descrizione testuale completa dei risultati di ricerca,
         * utile per l'interfaccia utente.
         * <p>
         * Questo metodo, richiesto da {@code ContentArea}, fornisce un riassunto
         * formattato dei criteri e del numero di risultati.
         * </p>
         *
         * @return Una {@link String} contenente la descrizione dei risultati di ricerca.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Restituisce una rappresentazione in formato stringa di questo oggetto {@link SearchResult}.
         * <p>
         * Questo metodo sovrascrive il comportamento di default di {@code Object.toString()}
         * per fornire una rappresentazione utile e leggibile dei dati contenuti nell'oggetto,
         * inclusi i parametri di ricerca, il numero di risultati e la descrizione generata.
         * </p>
         *
         * @return Una stringa formattata con tutti i dettagli rilevanti del risultato di ricerca.
         */
        @Override
        public String toString() {
            return "SearchResult{" +
                    "searchType='" + searchType + '\'' +
                    ", titleQuery='" + titleQuery + '\'' +
                    ", authorQuery='" + authorQuery + '\'' +
                    ", yearFrom='" + yearFrom + '\'' +
                    ", yearTo='" + yearTo + '\'' +
                    ", results=" + books.size() +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    /**
     * Costruisce una nuova istanza di {@code AdvancedSearchPanel}.
     * <p>
     * Questo costruttore inizializza il pannello di ricerca avanzata,
     * configurando i componenti dell'interfaccia utente, i gestori degli eventi
     * e il servizio di ricerca libri essenziale per il suo funzionamento.
     * </p>
     *
     * @param bookService Il servizio {@link BookService} utilizzato per eseguire le ricerche dei libri.
     * Non deve essere {@code null}.
     * @see #setupUI()
     * @see #setupEventHandlers()
     */
    public AdvancedSearchPanel(BookService bookService) {
        this.bookService = bookService;
        setupUI();
        setupEventHandlers();
    }

    /**
     * Configura l'intera interfaccia utente del pannello di ricerca avanzata.
     * <p>
     * Questo metodo definisce il layout, lo stile e la struttura dei componenti principali del pannello.
     * Imposta l'allineamento, la spaziatura e le dimensioni fisse del pannello,
     * applicando uno sfondo scuro con angoli arrotondati e un'ombra per un aspetto tridimensionale.
     * Aggiunge poi i componenti principali:
     * <ul>
     * <li><strong>Header:</strong> Contiene il pulsante per chiudere il pannello.</li>
     * <li><strong>Area del titolo:</strong> Fornisce un'intestazione descrittiva.</li>
     * <li><strong>ScrollPane del contenuto:</strong> Un'area scorrevole che ospita dinamicamente i campi di ricerca.</li>
     * </ul>
     * </p>
     *
     * @see #createHeader()
     * @see #createTitleArea()
     * @see #createContentScrollPane()
     */
    private void setupUI() {
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(0);
        this.setPadding(new Insets(0));
        this.setMaxWidth(600);
        this.setPrefWidth(600);
        this.setMaxHeight(700);

        // Background principale
        this.setStyle(
                "-fx-background-color: " + BG_SECONDARY + ";" +
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 25, 0, 0, 8);"
        );

        // Header con pulsante chiudi
        HBox header = createHeader();

        // Area titolo
        VBox titleArea = createTitleArea();

        // Contenuto principale
        ScrollPane scrollPane = createContentScrollPane();

        this.getChildren().addAll(header, titleArea, scrollPane);
    }

    /**
     * Crea e configura la barra di intestazione del pannello di ricerca.
     * <p>
     * Questo metodo costruisce un {@link HBox} che funge da intestazione per il pannello.
     * L'header è progettato per ospitare il pulsante di chiusura del pannello,
     * garantendo che sia sempre posizionato a destra e accessibile.
     * Imposta un'altezza fissa e configura il pannello per non bloccare gli eventi
     * del mouse di altri componenti, assicurando che il pulsante di chiusura sia
     * sempre cliccabile.
     * </p>
     *
     * @return Un {@link HBox} che rappresenta la barra di intestazione del pannello.
     * @see #createCloseButton()
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(25, 25, 10, 25));
        header.setPrefHeight(60);
        header.setMinHeight(60);

        // Assicurati che l'header non blocchi gli eventi
        header.setPickOnBounds(false);
        header.setMouseTransparent(false);

        // Crea il pulsante X
        closeButton = createCloseButton();

        // Aggiungi solo il pulsante senza altri elementi che possano interferire
        header.getChildren().add(closeButton);

        System.out.println("✅ Header creato con pulsante X");
        System.out.println("   - Header dimensioni: " + header.getPrefWidth() + "x" + header.getPrefHeight());
        System.out.println("   - Pulsante visibile: " + closeButton.isVisible());
        System.out.println("   - Pulsante gestito: " + closeButton.isManaged());

        return header;
    }

    /**
     * Crea e configura l'area del titolo e sottotitolo per il pannello di ricerca.
     * <p>
     * Questo metodo di utilità costruisce un {@link VBox} che contiene le etichette
     * del titolo ("🔍 Ricerca Avanzata") e del sottotitolo ("Trova i tuoi libri
     * con criteri specifici"). L'area è centralizzata e stilizzata per
     * garantire un aspetto pulito e coerente con il resto dell'interfaccia,
     * migliorando la leggibilità e l'esperienza utente.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta l'area del titolo e sottotitolo.
     */
    private VBox createTitleArea() {
        VBox titleArea = new VBox(5);
        titleArea.setAlignment(Pos.CENTER);
        titleArea.setPadding(new Insets(0, 30, 20, 30));

        Label title = new Label("🔍 Ricerca Avanzata");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(TEXT_PRIMARY));

        Label subtitle = new Label("Trova i tuoi libri con criteri specifici");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web(TEXT_SECONDARY));

        titleArea.getChildren().addAll(title, subtitle);
        return titleArea;
    }

    /**
     * Crea e configura un'area di contenuto scorrevole per ospitare i campi di ricerca.
     * <p>
     * Questo metodo di utilità costruisce un {@link ScrollPane} per garantire che l'interfaccia utente
     * rimanga funzionale anche se il contenuto dinamico dei campi di ricerca superasse
     * lo spazio disponibile. Lo stile del pannello scorrevole è impostato per essere trasparente,
     * mantenendo la coerenza visiva con lo sfondo del pannello principale.
     * Il metodo si assicura che il pannello si adatti automaticamente alla larghezza disponibile
     * e che la barra di scorrimento verticale sia visibile solo quando necessario.
     * </p>
     *
     * @return Un {@link ScrollPane} configurato e pronto per ospitare il contenuto principale.
     * @see #createMainContent()
     */
    private ScrollPane createContentScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox content = createMainContent();
        scrollPane.setContent(content);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    /**
     * Crea e configura il contenitore principale del contenuto del pannello.
     * <p>
     * Questo metodo di utilità costruisce un {@link VBox} che serve da contenitore per tutti
     * i componenti interattivi del pannello di ricerca, come la selezione del tipo di ricerca,
     * i campi di input dinamici e il pulsante di ricerca.
     * Imposta la spaziatura, il padding e l'allineamento per una presentazione pulita
     * e ordinata degli elementi.
     * </p>
     * <p>
     * Il metodo si occupa anche di:
     * <ul>
     * <li>Inizializzare la ComboBox per la selezione del tipo di ricerca.</li>
     * <li>Creare il contenitore per i campi di input che cambiano in base al tipo di ricerca.</li>
     * <li>Creare la sezione del pulsante di ricerca.</li>
     * <li>Inizializzare i campi di ricerca con la configurazione di default (ricerca per titolo).</li>
     * </ul>
     * </p>
     *
     * @return Un {@link VBox} che rappresenta il contenuto principale del pannello.
     * @see #setupSearchTypeCombo()
     * @see #createSearchTypeSection()
     * @see #createButtonSection()
     * @see #updateDynamicFields()
     */
    private VBox createMainContent() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(0, 30, 30, 30));
        content.setAlignment(Pos.TOP_CENTER);

        // Setup ComboBox per tipo ricerca
        setupSearchTypeCombo();

        // Sezione tipo ricerca
        VBox typeSection = createSearchTypeSection();

        // Container per campi dinamici
        dynamicFieldsContainer = new VBox(20);
        dynamicFieldsContainer.setPadding(new Insets(10, 0, 0, 0));

        // Sezione pulsanti
        VBox buttonSection = createButtonSection();

        content.getChildren().addAll(typeSection, dynamicFieldsContainer, buttonSection);

        // Inizializza con ricerca per titolo
        updateDynamicFields();

        return content;
    }

    /**
     * Inizializza e configura la {@link ComboBox} per la selezione del tipo di ricerca.
     * <p>
     * Questo metodo crea il componente {@code ComboBox} e lo popola con le opzioni
     * disponibili per la ricerca avanzata: "Ricerca per Titolo", "Ricerca per Autore",
     * e "Ricerca per Autore e Anno". Applica uno stile CSS personalizzato per
     * allinearlo all'estetica dell'applicazione, inclusi bordi arrotondati e colori
     * specifici.
     * </p>
     * <p>
     * Per migliorare la leggibilità e l'esperienza utente, il metodo personalizza
     * le factory delle celle e del pulsante della combo box per garantire che il testo
     * sia sempre visibile in bianco, indipendentemente dallo stato (selezionato,
     * inattivo, ecc.).
     * </p>
     * <p>
     * Infine, un listener {@code setOnAction} viene aggiunto per invocare il metodo
     * {@link #updateDynamicFields()} ogni volta che l'utente cambia la selezione,
     * assicurando che i campi di input sottostanti si aggiornino in tempo reale.
     * </p>
     *
     * @see #updateDynamicFields()
     */
    private void setupSearchTypeCombo() {
        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll(
                "Ricerca per Titolo",
                "Ricerca per Autore",
                "Ricerca per Autore e Anno"
        );
        searchTypeCombo.setValue("Ricerca per Titolo");
        searchTypeCombo.setMaxWidth(Double.MAX_VALUE);

        searchTypeCombo.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;"
        );

        // Testo leggibile nelle opzioni del dropdown
        searchTypeCombo.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                // Forza il testo bianco nelle opzioni
                setStyle("-fx-text-fill: #ffffff !important; -fx-background-color: " + BG_CONTROL + ";");
            }
        });

        // Testo leggibile per l'opzione selezionata
        searchTypeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                // Forza il testo bianco per l'opzione selezionata
                setStyle("-fx-text-fill: #ffffff !important; -fx-background-color: transparent;");
            }
        });

        searchTypeCombo.setOnAction(e -> updateDynamicFields());
    }

    /**
     * Crea e configura la sezione dell'interfaccia utente dedicata alla selezione del tipo di ricerca.
     * <p>
     * Questo metodo di utilità costruisce un {@link VBox} che raggruppa logicamente
     * l'etichetta del titolo della sezione ("Tipo di Ricerca") e la {@link ComboBox}
     * che permette all'utente di scegliere il criterio di ricerca. L'utilizzo di una sezione
     * dedicata aiuta a organizzare l'interfaccia in modo chiaro e intuitivo, migliorando
     * la leggibilità e l'esperienza utente.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta la sezione del tipo di ricerca.
     */
    private VBox createSearchTypeSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("Tipo di Ricerca");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        section.getChildren().addAll(sectionTitle, searchTypeCombo);
        return section;
    }

    /**
     * Crea e configura la sezione del pannello che contiene il pulsante di ricerca.
     * <p>
     * Questo metodo di utilità costruisce un {@link VBox} che serve da contenitore
     * per il pulsante {@link #searchButton}. La sezione è allineata al centro
     * e aggiunge uno spazio superiore per separare visivamente il pulsante
     * dai campi di input dinamici, migliorando la chiarezza del layout.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta la sezione del pulsante di ricerca.
     * @see #createSearchButton()
     */
    private VBox createButtonSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20, 0, 0, 0));

        // Crea pulsante ricerca
        searchButton = createSearchButton();

        section.getChildren().add(searchButton);
        return section;
    }

    /**
     * Crea e configura il pulsante di chiusura ("✕") per il pannello.
     * <p>
     * Questo metodo di utilità costruisce un {@link Button} con un'etichetta "✕" e lo stile
     * per apparire come un pulsante di chiusura circolare. Il pulsante ha dimensioni
     * fisse ed esplicite e uno stile di base che garantisce la sua visibilità e
     * un'esperienza utente coerente. Include effetti visivi di hover e gestisce
     * tutti i possibili eventi di interazione (mouse click, action event, key press)
     * per assicurare che sia sempre cliccabile e reattivo.
     * </p>
     * <p>
     * Le proprietà critiche come {@code setPickOnBounds}, {@code setMouseTransparent} e {@code setFocusTraversable}
     * sono impostate per garantire che il pulsante sia sempre in grado di ricevere
     * gli eventi, anche se si trova in un contenitore che potrebbe interferire.
     * </p>
     *
     * @return Un {@link Button} configurato per chiudere il pannello.
     * @see #closePanel()
     */
    private Button createCloseButton() {
        Button closeBtn = new Button("✕");

        // Dimensioni fisse e esplicite
        closeBtn.setMinSize(40, 40);
        closeBtn.setPrefSize(40, 40);
        closeBtn.setMaxSize(40, 40);

        // Stile di base SEMPLIFICATO per garantire visibilità
        closeBtn.setStyle(
                "-fx-background-color: #666666;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-width: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-alignment: center;"
        );

        // Proprietà critiche per la cliccabilità
        closeBtn.setPickOnBounds(true);
        closeBtn.setFocusTraversable(true);
        closeBtn.setMouseTransparent(false);
        closeBtn.setDisable(false);
        closeBtn.setVisible(true);
        closeBtn.setManaged(true);

        // TUTTI I POSSIBILI EVENT HANDLER

        // 1. Mouse events
        closeBtn.setOnMouseEntered(e -> {
            System.out.println("✅ MouseEntered - FUNZIONA");
            closeBtn.setStyle(
                    "-fx-background-color: #ff3b30;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 20px;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-width: 0;" +
                            "-fx-padding: 0;" +
                            "-fx-alignment: center;"
            );
        });

        closeBtn.setOnMouseExited(e -> {
            System.out.println("✅ MouseExited - FUNZIONA");
            closeBtn.setStyle(
                    "-fx-background-color: #666666;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 20px;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-width: 0;" +
                            "-fx-padding: 0;" +
                            "-fx-alignment: center;"
            );
        });

        closeBtn.setOnMousePressed(e -> {
            System.out.println("✅ MousePressed - FUNZIONA");
        });

        closeBtn.setOnMouseReleased(e -> {
            System.out.println("✅ MouseReleased - FUNZIONA");
        });

        closeBtn.setOnMouseClicked(e -> {
            System.out.println("✅ MouseClicked - FUNZIONA! Chiudo pannello...");
            e.consume();
            closePanel();
        });

        // 2. Action event
        closeBtn.setOnAction(e -> {
            System.out.println("✅ Action - FUNZIONA! Chiudo pannello...");
            e.consume();
            closePanel();
        });

        // 3. Key events (se mai ricevesse focus)
        closeBtn.setOnKeyPressed(e -> {
            System.out.println("✅ KeyPressed: " + e.getCode());
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                System.out.println("✅ Key ENTER/SPACE - Chiudo pannello...");
                e.consume();
                closePanel();
            }
        });

        System.out.println("✅ Pulsante X creato con dimensioni: " + closeBtn.getPrefWidth() + "x" + closeBtn.getPrefHeight());
        System.out.println("   - Visible: " + closeBtn.isVisible());
        System.out.println("   - Managed: " + closeBtn.isManaged());
        System.out.println("   - Disabled: " + closeBtn.isDisabled());
        System.out.println("   - MouseTransparent: " + closeBtn.isMouseTransparent());
        System.out.println("   - PickOnBounds: " + closeBtn.isPickOnBounds());

        return closeBtn;
    }

    /**
     * Crea e configura il pulsante principale per avviare la ricerca.
     * <p>
     * Questo metodo di utilità costruisce un {@link Button} con un'etichetta
     * "🔍 Cerca Libri". Il pulsante è stilizzato con un colore di accento
     * e un font in grassetto per renderlo visivamente prominente e coerente
     * con il design dell'applicazione. Le dimensioni e il padding sono
     * ottimizzati per un'esperienza utente mobile-friendly, mentre
     * il cursore a mano e lo stile di base ne indicano la cliccabilità.
     * </p>
     *
     * @return Un {@link Button} configurato per avviare l'operazione di ricerca.
     * @see #executeSearch()
     */
    private Button createSearchButton() {
        Button button = new Button("🔍 Cerca Libri");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 15px 30px;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-min-height: 50px;"
        );

        return button;
    }

    /**
     * Configura tutti i principali gestori di eventi per il pannello di ricerca avanzata.
     * <p>
     * Questo metodo stabilisce i listener per le interazioni con l'utente, inclusi gli
     * eventi della tastiera (come `ESC` per chiudere e `ENTER` per cercare) e i click
     * sui pulsanti. La logica è implementata per rispondere direttamente agli eventi,
     * senza l'uso di {@code Platform.runLater}, garantendo una maggiore reattività
     * e un'esperienza utente più fluida.
     * </p>
     * <p>
     * Il metodo si occupa anche della gestione del focus e della prevenzione della
     * propagazione degli eventi del mouse al di fuori del pannello, assicurando che
     * le interazioni siano contenute all'interno della vista di ricerca.
     * </p>
     *
     * @see #closePanel()
     * @see #executeSearch()
     */
    private void setupEventHandlers() {
        System.out.println("🔧 Setup event handlers per AdvancedSearchPanel...");

        // Handler ESC per chiudere SENZA Platform.runLater
        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    System.out.println("⌨️ ESC premuto nel pannello ricerca avanzata");
                    event.consume(); // Consuma l'evento per evitare propagazione
                    closePanel();  // DIRETTO - NO Platform.runLater!
                    break;
                case ENTER:
                    if (!isSearching) {
                        System.out.println("⌨️ ENTER premuto nel pannello - esegui ricerca");
                        event.consume();
                        executeSearch();  // DIRETTO - NO Platform.runLater!
                    }
                    break;
            }
        });

        // Handler pulsante X SENZA Platform.runLater
        closeButton.setOnAction(e -> {
            System.out.println("🖱️ Pulsante X cliccato - chiusura pannello");
            e.consume(); // Consuma l'evento per evitare conflitti
            closePanel();
        });

        // Handler pulsante cerca SENZA Platform.runLater
        searchButton.setOnAction(e -> {
            System.out.println("🖱️ Pulsante cerca cliccato");
            e.consume(); // Consuma l'evento
            if (!isSearching) {
                executeSearch();
            }
        });

        // Focus management
        setFocusTraversable(true);

        // Previeni propagazione eventi mouse sul pannello
        setOnMouseClicked(e -> {
            e.consume(); // Impedisce che il click sul pannello chiuda il popup
            requestFocus(); // Mantiene il focus sul pannello
        });

        System.out.println("✅ Event handlers configurati correttamente");
    }

    /**
     * Aggiorna dinamicamente i campi di input visualizzati nel pannello in base
     * al tipo di ricerca selezionato dall'utente.
     * <p>
     * Questo metodo di utilità è responsabile di svuotare il contenitore dei campi
     * di input (`dynamicFieldsContainer`) e di ricreare i componenti UI
     * appropriati a seconda della selezione corrente della {@link #searchTypeCombo}.
     * I campi vengono aggiornati per riflettere le opzioni di ricerca disponibili,
     * come solo il titolo, solo l'autore, o una combinazione di autore e anno.
     * </p>
     *
     * @see #createTitleSearchFields()
     * @see #createAuthorSearchFields()
     * @see #createAuthorYearSearchFields()
     */
    private void updateDynamicFields() {
        dynamicFieldsContainer.getChildren().clear();

        String selectedType = searchTypeCombo.getValue();

        if (selectedType.contains("Titolo")) {
            createTitleSearchFields();
        } else if (selectedType.contains("Autore e Anno")) {
            createAuthorYearSearchFields();
        } else if (selectedType.contains("Autore")) {
            createAuthorSearchFields();
        }
    }

    /**
     * Crea i campi di input specifici per la ricerca per titolo.
     * <p>
     * Questo metodo di utilità costruisce la sezione dell'interfaccia utente
     * dedicata alla ricerca di un libro per titolo. Crea un'etichetta
     * con il titolo della sezione ("📖 Cerca per Titolo") e un campo di testo
     * stilizzato (`titleField`) dove l'utente può inserire il titolo desiderato.
     * La sezione completa viene poi aggiunta al contenitore dei campi dinamici,
     * rendendo l'interfaccia coerente con il tipo di ricerca selezionato.
     * </p>
     *
     * @see #createStyledTextField(String)
     */
    private void createTitleSearchFields() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("📖 Cerca per Titolo");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        titleField = createStyledTextField("Inserisci il titolo del libro...");

        section.getChildren().addAll(sectionTitle, titleField);
        dynamicFieldsContainer.getChildren().add(section);
    }

    /**
     * Crea i campi di input specifici per la ricerca per autore.
     * <p>
     * Questo metodo di utilità costruisce la sezione dell'interfaccia utente
     * dedicata alla ricerca di un libro per autore. Crea un'etichetta
     * con il titolo della sezione ("👤 Cerca per Autore") e un campo di testo
     * stilizzato (`authorField`) dove l'utente può inserire il nome dell'autore.
     * La sezione completa viene poi aggiunta al contenitore dei campi dinamici,
     * assicurando che l'interfaccia si adatti in base al tipo di ricerca
     * selezionato.
     * </p>
     *
     * @see #createStyledTextField(String)
     */
    private void createAuthorSearchFields() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("👤 Cerca per Autore");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        authorField = createStyledTextField("Inserisci il nome dell'autore...");

        section.getChildren().addAll(sectionTitle, authorField);
        dynamicFieldsContainer.getChildren().add(section);
    }

    /**
     * Crea i campi di input specifici per la ricerca combinata per autore e anno.
     * <p>
     * Questo metodo costruisce la sezione dell'interfaccia utente che permette di cercare
     * libri specificando sia l'autore che un intervallo di anni di pubblicazione.
     * La sezione include un'etichetta di intestazione ("👤📅 Cerca per Autore e Anno"),
     * un campo di testo (`authorField`) per l'autore e una casella orizzontale (`HBox`)
     * che raggruppa due campi di testo (`yearFromField` e `yearToField`) per l'intervallo
     * di anni, separati da una semplice etichetta.
     * </p>
     * <p>
     * Il layout è progettato per essere chiaro e intuitivo, guidando l'utente nell'inserimento
     * dei dati per una ricerca più precisa.
     * </p>
     *
     * @see #createStyledTextField(String)
     * @see #dynamicFieldsContainer
     */
    private void createAuthorYearSearchFields() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("👤📅 Cerca per Autore e Anno");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        authorField = createStyledTextField("Inserisci il nome dell'autore...");

        // Sezione anno
        Label yearLabel = new Label("Anno di pubblicazione");
        yearLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        yearLabel.setTextFill(Color.web(TEXT_PRIMARY));

        HBox yearBox = new HBox(15);
        yearBox.setAlignment(Pos.CENTER_LEFT);

        yearFromField = createStyledTextField("Dal...");
        yearFromField.setPrefWidth(120);
        yearFromField.setMaxWidth(120);

        Label dashLabel = new Label("a");
        dashLabel.setTextFill(Color.web(TEXT_PRIMARY));
        dashLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        yearToField = createStyledTextField("Al...");
        yearToField.setPrefWidth(120);
        yearToField.setMaxWidth(120);

        yearBox.getChildren().addAll(yearFromField, dashLabel, yearToField);

        section.getChildren().addAll(sectionTitle, authorField, yearLabel, yearBox);
        dynamicFieldsContainer.getChildren().add(section);
    }

    /**
     * Crea e configura un campo di testo con uno stile predefinito.
     * <p>
     * Questo metodo di utilità costruisce un {@link TextField} e vi applica uno stile
     * uniforme che si integra con il design dell'applicazione. Lo stile include
     * angoli arrotondati, colori specifici per lo sfondo e il testo, e un bordo
     * per delineare il campo. Viene anche impostato un testo segnaposto (`prompt text`)
     * per guidare l'utente sull'input atteso.
     * </p>
     *
     * @param placeholder La stringa di testo da visualizzare come segnaposto nel campo.
     * @return Un {@link TextField} stilizzato con le proprietà e lo stile definiti.
     */
    private TextField createStyledTextField(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;"
        );

        return field;
    }

    /**
     * Esegue l'operazione di ricerca avanzata in base ai parametri di input.
     * <p>
     * Questo metodo valida l'input dell'utente, imposta lo stato di ricerca per prevenire
     * l'invio di richieste multiple e invoca il metodo {@link #performAdvancedSearch(String, String, String, String, String)}
     * per ottenere i risultati. In caso di successo, un oggetto {@link SearchResult}
     * contenente i dati e i parametri di ricerca viene creato e passato al callback
     * {@link #onSearchExecuted}. La gestione degli errori è inclusa per catturare
     * e notificare eventuali problemi durante il processo di ricerca.
     * </p>
     * <p>
     * Il metodo gestisce anche il feedback visivo all'utente aggiornando lo stato
     * del pulsante di ricerca e ripristinandolo al completamento dell'operazione,
     * sia in caso di successo che di fallimento.
     * </p>
     *
     * @see #validateSearchInput(String, String, String, String, String)
     * @see #performAdvancedSearch(String, String, String, String, String)
     * @see #updateSearchButtonState(boolean)
     * @see #onSearchExecuted
     */
    private void executeSearch() {
        System.out.println("🔍 === INIZIO RICERCA AVANZATA ===");

        if (isSearching) {
            System.out.println("⚠️ Ricerca già in corso - ignoro");
            return;
        }

        String searchType = searchTypeCombo.getValue();
        String titleQuery = titleField != null ? titleField.getText().trim() : "";
        String authorQuery = authorField != null ? authorField.getText().trim() : "";
        String yearFrom = yearFromField != null ? yearFromField.getText().trim() : "";
        String yearTo = yearToField != null ? yearToField.getText().trim() : "";

        System.out.println("📋 Parametri ricerca:");
        System.out.println("   - Tipo: " + searchType);
        System.out.println("   - Titolo: '" + titleQuery + "'");
        System.out.println("   - Autore: '" + authorQuery + "'");
        System.out.println("   - Anno da: '" + yearFrom + "'");
        System.out.println("   - Anno a: '" + yearTo + "'");

        // Validazione completa dell'input
        if (!validateSearchInput(searchType, titleQuery, authorQuery, yearFrom, yearTo)) {
            System.out.println("❌ Validazione fallita - ricerca interrotta");
            return;
        }

        try {
            isSearching = true;
            updateSearchButtonState(true);
            System.out.println("🔄 Stato ricerca impostato su TRUE");

            List<Book> results = performAdvancedSearch(searchType, titleQuery, authorQuery, yearFrom, yearTo);
            System.out.println("📚 Ricerca completata: " + results.size() + " risultati");

            SearchResult searchResult = new SearchResult(results, searchType, titleQuery, authorQuery, yearFrom, yearTo);
            System.out.println("📦 SearchResult creato: " + searchResult.getDescription());

            if (onSearchExecuted != null) {
                System.out.println("📤 Esecuzione callback ricerca...");
                onSearchExecuted.accept(searchResult);
                System.out.println("✅ Callback eseguito con successo");
            } else {
                System.err.println("❌ ERRORE: onSearchExecuted callback è NULL!");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore ricerca avanzata: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Errore durante la ricerca: " + e.getMessage());
        } finally {
            isSearching = false;
            updateSearchButtonState(false);
            System.out.println("🔄 Stato ricerca ripristinato su FALSE");
            System.out.println("🏁 === FINE RICERCA AVANZATA ===");
        }
    }

    /**
     * Chiude il pannello di ricerca avanzata in modo sicuro e pulito.
     * <p>
     * Questo metodo esegue la sequenza di chiusura del pannello. Prima, invoca il
     * callback {@link #onClosePanel} per notificare all'applicazione che la vista sta
     * per essere chiusa. Successivamente, chiama il metodo {@link #cleanup()}
     * per rimuovere tutti gli event handler e rilasciare le risorse, prevenendo
     * potenziali memory leak. Il metodo è racchiuso in un blocco {@code try-catch}
     * per gestire qualsiasi eccezione che possa verificarsi durante il processo di
     * chiusura, garantendo che l'applicazione rimanga stabile.
     * </p>
     *
     * @see #onClosePanel
     * @see #cleanup()
     */
    private void closePanel() {
        try {
            System.out.println("🔒 Chiusura pannello ricerca avanzata...");

            if (onClosePanel != null) {
                System.out.println("🔗 Chiamata callback chiusura Header...");
                onClosePanel.run(); // DIRETTO - chiamata PRIMA del cleanup
            } else {
                System.err.println("❌ ERRORE: onClosePanel callback è NULL!");
            }

            cleanup();

            System.out.println("✅ Pannello chiuso correttamente");

        } catch (Exception ex) {
            System.err.println("❌ Errore chiusura pannello: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Esegue la pulizia completa delle risorse e degli event handler del pannello.
     * <p>
     * Questo metodo viene invocato prima della chiusura del pannello per prevenire memory leak.
     * Rimuove tutti i listener di eventi associati al pannello, ai campi di testo e ai
     * pulsanti. Inoltre, imposta a {@code null} i riferimenti ai callback di ricerca
     * e chiusura per permettere al garbage collector di deallocare correttamente le risorse.
     * </p>
     * <p>
     * Il metodo è implementato in un blocco `try-catch` per garantire che il processo di
     * pulizia si completi anche in caso di errori imprevisti.
     * </p>
     */
    public void cleanup() {
        try {
            System.out.println("🧹 Cleanup AdvancedSearchPanel...");

            // Rimuovi TUTTI gli event handlers
            setOnKeyPressed(null);
            setOnMouseClicked(null);
            setFocusTraversable(false);

            // Cleanup dei campi di testo
            if (titleField != null) {
                titleField.setOnKeyPressed(null);
                titleField.setOnAction(null);
            }

            if (authorField != null) {
                authorField.setOnKeyPressed(null);
                authorField.setOnAction(null);
            }

            if (yearFromField != null) {
                yearFromField.setOnKeyPressed(null);
                yearFromField.setOnAction(null);
            }

            if (yearToField != null) {
                yearToField.setOnKeyPressed(null);
                yearToField.setOnAction(null);
            }

            // Cleanup dei pulsanti
            if (searchButton != null) {
                searchButton.setOnAction(null);
            }

            if (closeButton != null) {
                closeButton.setOnAction(null);
            }

            if (searchTypeCombo != null) {
                searchTypeCombo.setOnAction(null);
            }

            onSearchExecuted = null;
            onClosePanel = null;

            System.out.println("✅ Cleanup completato");

        } catch (Exception e) {
            System.err.println("❌ Errore durante cleanup: " + e.getMessage());
        }
    }

    /**
     * Esegue la logica di ricerca avanzata basata sui parametri forniti.
     * <p>
     * Questo metodo agisce come il core del motore di ricerca avanzata. Seleziona la
     * strategia di ricerca più appropriata (per titolo o per autore) in base al
     * {@code searchType} fornito. In caso di ricerca per titolo, tenta di utilizzare
     * un metodo di ricerca specifico dal {@link BookService} e, in caso di errore,
     * effettua un fallback a una ricerca più generica seguita da un filtraggio
     * lato client. Per la ricerca per autore, si affida sempre a una ricerca
     * generale con un successivo filtraggio.
     * </p>
     * <p>
     * Infine, applica un ulteriore filtro per l'anno di pubblicazione se i parametri
     * {@code yearFrom} e/o {@code yearTo} sono specificati.
     * </p>
     *
     * @param searchType  Il tipo di ricerca selezionato (es. "Ricerca per Titolo").
     * @param titleQuery  La stringa di ricerca per il titolo del libro.
     * @param authorQuery La stringa di ricerca per l'autore.
     * @param yearFrom    L'anno di inizio del range di ricerca per anno.
     * @param yearTo      L'anno di fine del range di ricerca per anno.
     * @return Una {@link List} di oggetti {@link Book} che soddisfano tutti
     * i criteri di ricerca. Restituisce una lista vuota in caso di errore,
     * parametri mancanti o assenza di risultati.
     * @see BookService#searchBooksByTitle(String)
     * @see BookService#searchBooks(String)
     * @see #filterBooksByTitleOnly(List, String)
     * @see #filterBooksByAuthor(List, String)
     * @see #filterBooksByYear(List, String, String)
     */
    private List<Book> performAdvancedSearch(String searchType, String titleQuery,
                                             String authorQuery, String yearFrom, String yearTo) {
        try {
            if (bookService == null) {
                System.err.println("❌ BookService non disponibile");
                return new ArrayList<>();
            }

            List<Book> results = new ArrayList<>();

            if (searchType.contains("Titolo") && !titleQuery.isEmpty()) {
                System.out.println("📖 Ricerca SPECIFICA per titolo: " + titleQuery);

                try {
                    results = bookService.searchBooksByTitle(titleQuery);
                    System.out.println("✅ Ricerca titolo specifica completata: " + results.size() + " risultati");
                } catch (Exception e) {
                    System.err.println("❌ Errore ricerca titolo specifica: " + e.getMessage());
                    // ✅ FALLBACK: usa ricerca generale e filtra lato client
                    System.out.println("🔄 Fallback: ricerca generale con filtro titolo");
                    List<Book> generalResults = bookService.searchBooks(titleQuery);
                    results = filterBooksByTitleOnly(generalResults, titleQuery);
                }

            } else if (searchType.contains("Autore") && !authorQuery.isEmpty()) {
                System.out.println("👤 Ricerca per autore: " + authorQuery);

                // Per autore usa ricerca generale + filtro lato client
                List<Book> generalResults = bookService.searchBooks(authorQuery);
                results = filterBooksByAuthor(generalResults, authorQuery);

            } else {
                System.err.println("❌ Tipo di ricerca non riconosciuto o parametri mancanti");
                System.err.println("   searchType: " + searchType);
                System.err.println("   titleQuery: '" + titleQuery + "'");
                System.err.println("   authorQuery: '" + authorQuery + "'");
                return new ArrayList<>();
            }

            if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
                System.out.println("📅 Applicazione filtro anno: " + yearFrom + "-" + yearTo);
                results = filterBooksByYear(results, yearFrom, yearTo);
            }

            System.out.println("✅ Ricerca avanzata completata: " + results.size() + " risultati finali");
            return results;

        } catch (Exception e) {
            System.err.println("❌ Errore ricerca avanzata: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Esegue la validazione dell'input dell'utente prima di avviare l'operazione di ricerca.
     * <p>
     * Questo metodo verifica che almeno un campo di ricerca sia compilato e, a seconda del
     * tipo di ricerca selezionato, delega la validazione a metodi specifici. La validazione
     * è un passaggio cruciale per prevenire richieste di ricerca vuote o con formati
     * non validi, migliorando l'affidabilità del sistema.
     * </p>
     *
     * @param searchType  Il tipo di ricerca selezionato (es. "Ricerca per Titolo").
     * @param titleQuery  La stringa di ricerca per il titolo del libro.
     * @param authorQuery La stringa di ricerca per l'autore.
     * @param yearFrom    L'anno di inizio del range di ricerca per anno.
     * @param yearTo      L'anno di fine del range di ricerca per anno.
     * @return {@code true} se l'input è valido e la ricerca può procedere, {@code false} altrimenti.
     * @see #validateTitleSearch(String)
     * @see #validateAuthorSearch(String)
     * @see #validateAuthorYearSearch(String, String, String)
     */
    private boolean validateSearchInput(String searchType, String titleQuery, String authorQuery, String yearFrom, String yearTo) {
        System.out.println("🔍 Validazione input ricerca avanzata...");

        // Controllo base: almeno un campo deve essere compilato
        if (titleQuery.isEmpty() && authorQuery.isEmpty() && yearFrom.isEmpty() && yearTo.isEmpty()) {
            showValidationError("Inserisci almeno un criterio di ricerca");
            return false;
        }

        // Validazione specifica per tipo di ricerca
        if (searchType.contains("Titolo")) {
            return validateTitleSearch(titleQuery);
        } else if (searchType.contains("Autore e Anno")) {
            return validateAuthorYearSearch(authorQuery, yearFrom, yearTo);
        } else if (searchType.contains("Autore")) {
            return validateAuthorSearch(authorQuery);
        }

        return true;
    }

    /**
     * Esegue la validazione del campo di ricerca per il titolo.
     * <p>
     * Questo metodo verifica che la stringa del titolo fornita dall'utente rispetti
     * una serie di criteri essenziali prima di procedere con la ricerca. Le validazioni
     * includono:
     * <ul>
     * <li>Il campo non deve essere vuoto.</li>
     * <li>La stringa non deve contenere solo numeri.</li>
     * <li>La lunghezza minima della stringa deve essere di almeno 2 caratteri.</li>
     * <li>La stringa deve contenere solo caratteri validi (lettere, spazi, apostrofi e trattini).</li>
     * </ul>
     * Se una qualsiasi di queste condizioni non è soddisfatta, viene mostrato un
     * messaggio di errore all'utente e il metodo restituisce {@code false}.
     * </p>
     *
     * @param titleQuery La stringa di ricerca per il titolo.
     * @return {@code true} se la stringa è valida, {@code false} altrimenti.
     */
    private boolean validateTitleSearch(String titleQuery) {
        if (titleQuery.isEmpty()) {
            showValidationError("Il campo titolo non può essere vuoto per questo tipo di ricerca");
            return false;
        }

        // Controllo che il titolo non contenga solo numeri
        if (isOnlyNumbers(titleQuery)) {
            showValidationError("Il titolo non può contenere solo numeri.\nInserisci delle lettere per identificare il libro.");
            return false;
        }

        // ontrollo lunghezza minima
        if (titleQuery.length() < 2) {
            showValidationError("Il titolo deve contenere almeno 2 caratteri");
            return false;
        }

        // Controllo caratteri validi (lettere, spazi, apostrofi, trattini)
        if (!isValidTextInput(titleQuery)) {
            showValidationError("Il titolo può contenere solo lettere, spazi, apostrofi e trattini.\nRimuovi numeri e caratteri speciali.");
            return false;
        }

        System.out.println("✅ Validazione titolo: OK");
        return true;
    }

    /**
     * Esegue la validazione del campo di ricerca per l'autore.
     * <p>
     * Questo metodo verifica che la stringa fornita dall'utente nel campo autore rispetti una serie di
     * criteri di validazione per garantire che l'input sia corretto prima di procedere con la ricerca.
     * Le condizioni di validazione sono le seguenti:
     * <ul>
     * <li>Il campo non deve essere vuoto.</li>
     * <li>La stringa non deve contenere solo numeri.</li>
     * <li>La lunghezza minima della stringa deve essere di almeno 2 caratteri.</li>
     * <li>La stringa deve contenere solo caratteri validi per un nome di autore (lettere, spazi,
     * apostrofi e punti).</li>
     * </ul>
     * Se uno di questi controlli fallisce, viene mostrato un messaggio di errore all'utente
     * e il metodo restituisce {@code false}.
     * </p>
     *
     * @param authorQuery La stringa di ricerca per l'autore.
     * @return {@code true} se la stringa è valida, {@code false} altrimenti.
     */
    private boolean validateAuthorSearch(String authorQuery) {
        if (authorQuery.isEmpty()) {
            showValidationError("Il campo autore non può essere vuoto per questo tipo di ricerca");
            return false;
        }

        // Controllo che l'autore non contenga solo numeri
        if (isOnlyNumbers(authorQuery)) {
            showValidationError("Il nome dell'autore non può contenere solo numeri.\nInserisci il nome o cognome dell'autore.");
            return false;
        }

        // Controllo lunghezza minima
        if (authorQuery.length() < 2) {
            showValidationError("Il nome dell'autore deve contenere almeno 2 caratteri");
            return false;
        }

        // Controllo caratteri validi per nomi
        if (!isValidAuthorName(authorQuery)) {
            showValidationError("Il nome dell'autore può contenere solo lettere, spazi, apostrofi e punti.\nRimuovi numeri e caratteri speciali.");
            return false;
        }

        System.out.println("✅ Validazione autore: OK");
        return true;
    }

    /**
     * Esegue la validazione dei campi di ricerca per l'autore e l'anno.
     * <p>
     * Questo metodo di validazione combina i controlli per la ricerca per autore e per anno,
     * garantendo che almeno uno dei due criteri sia specificato dall'utente.
     * Delega la validazione del campo autore al metodo {@link #validateAuthorSearch(String)}
     * e quella dei campi dell'anno al metodo {@link #validateYearRange(String, String)}.
     * L'approccio modulare garantisce che i controlli siano rigorosi e che i messaggi di
     * errore specifici vengano mostrati all'utente in caso di input non valido.
     * </p>
     *
     * @param authorQuery La stringa di ricerca per l'autore.
     * @param yearFrom    L'anno di inizio del range di ricerca.
     * @param yearTo      L'anno di fine del range di ricerca.
     * @return {@code true} se i campi sono validi, {@code false} altrimenti.
     * @see #validateAuthorSearch(String)
     * @see #validateYearRange(String, String)
     */
    private boolean validateAuthorYearSearch(String authorQuery, String yearFrom, String yearTo) {
        // Almeno uno tra autore e anno deve essere specificato
        if (authorQuery.isEmpty() && yearFrom.isEmpty() && yearTo.isEmpty()) {
            showValidationError("Per la ricerca Autore e Anno, specifica almeno l'autore o un anno");
            return false;
        }

        // Se c'è l'autore, validalo
        if (!authorQuery.isEmpty() && !validateAuthorSearch(authorQuery)) {
            return false; // Il messaggio di errore è già mostrato da validateAuthorSearch
        }

        // Validazione anni
        if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
            return validateYearRange(yearFrom, yearTo);
        }

        return true;
    }

    /**
     * Valida i campi di input per l'intervallo di anni.
     * <p>
     * Questo metodo esegue una serie di controlli per garantire che gli anni
     * inseriti dall'utente siano validi. Le validazioni includono:
     * <ul>
     * <li>Ogni anno specificato deve essere un numero intero valido.</li>
     * <li>Gli anni devono essere compresi in un intervallo ragionevole (da 1000 all'anno corrente).</li>
     * <li>Se entrambi i campi sono compilati, l'anno di inizio non può essere maggiore
     * dell'anno di fine.</li>
     * <li>L'intervallo di anni non deve essere eccessivamente ampio (massimo 100 anni),
     * per prevenire ricerche troppo generiche che potrebbero impattare le prestazioni.</li>
     * </ul>
     * In caso di fallimento della validazione, viene visualizzato un messaggio di errore
     * specifico per l'utente.
     * </p>
     *
     * @param yearFrom L'anno di inizio del range di ricerca.
     * @param yearTo   L'anno di fine del range di ricerca.
     * @return {@code true} se il range di anni è valido, {@code false} altrimenti.
     */
    private boolean validateYearRange(String yearFrom, String yearTo) {
        int currentYear = java.time.Year.now().getValue();

        if (!yearFrom.isEmpty()) {
            if (!isValidYear(yearFrom)) {
                showValidationError("Anno di inizio non valido.\nInserisci un anno tra 1000 e " + currentYear);
                return false;
            }
        }

        if (!yearTo.isEmpty()) {
            if (!isValidYear(yearTo)) {
                showValidationError("Anno di fine non valido.\nInserisci un anno tra 1000 e " + currentYear);
                return false;
            }
        }

        // Se entrambi sono specificati, controlla che yearFrom <= yearTo
        if (!yearFrom.isEmpty() && !yearTo.isEmpty()) {
            try {
                int from = Integer.parseInt(yearFrom);
                int to = Integer.parseInt(yearTo);

                if (from > to) {
                    showValidationError("L'anno di inizio (" + from + ") non può essere maggiore dell'anno di fine (" + to + ")");
                    return false;
                }

                // Controllo range ragionevole (massimo 100 anni)
                if (to - from > 100) {
                    showValidationError("Il range di anni è troppo ampio (massimo 100 anni).\nSpecifica un periodo più ristretto.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showValidationError("Formato anno non valido");
                return false;
            }
        }

        System.out.println("✅ Validazione anni: OK");
        return true;
    }

    /**
     * Controlla se una stringa contiene solo caratteri numerici.
     * <p>
     * Questo metodo di utilità rimuove gli spazi bianchi dall'input e verifica se la
     * stringa risultante è composta interamente da cifre (0-9). Restituisce {@code false}
     * se la stringa è vuota o null.
     * </p>
     *
     * @param input La stringa da verificare.
     * @return {@code true} se la stringa contiene solo numeri, {@code false} altrimenti.
     */
    private boolean isOnlyNumbers(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Rimuovi spazi e controlla se rimangono solo cifre
        String cleaned = input.replaceAll("\\s+", "");
        return cleaned.matches("^\\d+$");
    }

    /**
     * Controlla se l'input è valido per i campi di testo generici, come i titoli dei libri.
     * <p>
     * Questo metodo di utilità verifica che la stringa fornita sia composta unicamente
     * da caratteri validi per i titoli, che includono lettere (anche accentate),
     * spazi, apostrofi, trattini, due punti, parentesi e punteggiatura varia.
     * L'utilizzo di un'espressione regolare garantisce un controllo rigoroso sul formato.
     * </p>
     *
     * @param input La stringa da verificare.
     * @return {@code true} se la stringa contiene solo i caratteri validi, {@code false} altrimenti.
     */
    private boolean isValidTextInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Permetti lettere (anche accentate), spazi, apostrofi, trattini, due punti, parentesi
        return input.matches("^[\\p{L}\\s'\\-:().,!?]+$");
    }

    /**
     * Controlla se l'input è valido per i nomi degli autori.
     * <p>
     * Simile al metodo {@code isValidTextInput}, questo metodo usa un'espressione
     * regolare per validare che una stringa sia un nome di autore corretto.
     * Permette lettere, spazi, apostrofi, punti (per le abbreviazioni come "J.R.R.")
     * e trattini.
     * </p>
     *
     * @param input La stringa da verificare.
     * @return {@code true} se la stringa contiene solo i caratteri validi per un nome di autore, {@code false} altrimenti.
     */
    private boolean isValidAuthorName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Permetti lettere, spazi, apostrofi, punti (per abbreviazioni), trattini
        return input.matches("^[\\p{L}\\s'.\\-]+$");
    }

    /**
     * Verifica se una stringa rappresenta un anno valido e ragionevole.
     * <p>
     * Questo metodo converte la stringa di input in un intero e ne verifica la validità
     * in un intervallo predefinito, dal 1000 all'anno corrente. Questo previene l'uso
     * di anni non realistici o formati non numerici. La gestione delle eccezioni
     * {@link NumberFormatException} garantisce che il metodo restituisca {@code false}
     * in caso di input non numerico.
     * </p>
     *
     * @param yearStr La stringa da verificare che rappresenta l'anno.
     * @return {@code true} se la stringa è un anno valido, {@code false} altrimenti.
     */
    private boolean isValidYear(String yearStr) {
        try {
            int year = Integer.parseInt(yearStr.trim());
            int currentYear = java.time.Year.now().getValue();

            // Anno ragionevole: tra 1000 e anno corrente
            return year >= 1000 && year <= currentYear;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Filtra una lista di libri mantenendo solo quelli il cui titolo contiene una
     * determinata stringa di ricerca.
     * <p>
     * Questo metodo esegue un filtraggio lato client, utile come fallback o quando
     * la ricerca non può essere eseguita direttamente sul backend. La ricerca
     * è insensibile alle maiuscole/minuscole per garantire che i risultati
     * corrispondano indipendentemente dalla capitalizzazione. Se la stringa di
     * ricerca è vuota o null, la lista originale viene restituita senza modifiche.
     * </p>
     *
     * @param books        La lista di libri da filtrare.
     * @param targetTitle  La stringa di ricerca da trovare all'interno dei titoli.
     * @return Una {@link List} contenente solo i libri che soddisfano il criterio di filtro.
     */
    private List<Book> filterBooksByTitleOnly(List<Book> books, String targetTitle) {
        if (targetTitle == null || targetTitle.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();
        String searchTitle = targetTitle.toLowerCase().trim();

        for (Book book : books) {
            if (book.getTitle() != null &&
                    book.getTitle().toLowerCase().contains(searchTitle)) {
                filtered.add(book);
            }
        }

        System.out.println("📖 Filtro SOLO titolo '" + targetTitle + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    /**
     * Filtra una lista di libri mantenendo solo quelli il cui autore contiene una
     * determinata stringa di ricerca.
     * <p>
     * Questo metodo esegue un filtraggio lato client, utile per affinare i risultati
     * di una ricerca generale. L'operazione di filtro è insensibile alle maiuscole/minuscole
     * e ignora gli spazi bianchi all'inizio e alla fine della stringa, garantendo
     * corrispondenze accurate e flessibili. Se la stringa dell'autore è vuota o null,
     * il metodo restituisce la lista originale senza applicare alcun filtro.
     * </p>
     *
     * @param books        La lista di libri da filtrare.
     * @param targetAuthor La stringa di ricerca da trovare nel nome degli autori.
     * @return Una {@link List} contenente solo i libri che soddisfano il criterio di filtro.
     */
    private List<Book> filterBooksByAuthor(List<Book> books, String targetAuthor) {
        if (targetAuthor == null || targetAuthor.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();
        String searchAuthor = targetAuthor.toLowerCase().trim();

        for (Book book : books) {
            if (book.getAuthor() != null &&
                    book.getAuthor().toLowerCase().contains(searchAuthor)) {
                filtered.add(book);
            }
        }

        System.out.println("👤 Filtro autore '" + targetAuthor + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    /**
     * Filtra una lista di libri in base a un intervallo di anni di pubblicazione.
     * <p>
     * Questo metodo esegue un filtraggio lato client, esaminando l'anno di pubblicazione
     * di ciascun libro e confrontandolo con il range specificato dai parametri
     * {@code yearFrom} e {@code yearTo}. L'operazione è sicura e gestisce sia il caso
     * di anni non specificati (saltando il controllo) che il caso di anni non validi
     * (ignorando il libro in questione per prevenire eccezioni).
     * Il metodo è robusto e assicura che solo i libri pubblicati all'interno
     * del range specificato vengano inclusi nella lista finale dei risultati.
     * </p>
     *
     * @param books    La lista di libri da filtrare.
     * @param yearFrom La stringa che rappresenta l'anno di inizio del range.
     * Può essere vuota.
     * @param yearTo   La stringa che rappresenta l'anno di fine del range.
     * Può essere vuota.
     * @return Una {@link List} contenente solo i libri che soddisfano il criterio
     * di filtro per anno.
     */
    private List<Book> filterBooksByYear(List<Book> books, String yearFrom, String yearTo) {
        List<Book> filtered = new ArrayList<>();

        for (Book book : books) {
            try {
                String bookYear = book.getPublishYear();
                if (bookYear == null || bookYear.trim().isEmpty()) {
                    continue; // Salta libri senza anno
                }

                int bookYearInt = Integer.parseInt(bookYear.trim());
                boolean inRange = true;

                if (!yearFrom.isEmpty()) {
                    int yearFromInt = Integer.parseInt(yearFrom);
                    if (bookYearInt < yearFromInt) {
                        inRange = false;
                    }
                }

                if (!yearTo.isEmpty() && inRange) {
                    int yearToInt = Integer.parseInt(yearTo);
                    if (bookYearInt > yearToInt) {
                        inRange = false;
                    }
                }

                if (inRange) {
                    filtered.add(book);
                }

            } catch (NumberFormatException e) {
                // Salta libri con anno non numerico
                continue;
            }
        }

        System.out.println("📅 Filtro anno: " + books.size() + " → " + filtered.size() + " risultati");
        return filtered;
    }

    /**
     * Aggiorna l'aspetto e lo stato di interattività del pulsante di ricerca
     * in base allo stato attuale dell'operazione di ricerca.
     * <p>
     * Questo metodo cambia l'etichetta del pulsante per indicare se una ricerca
     * è in corso e disabilita il pulsante per impedire all'utente di inviare
     * più richieste contemporaneamente. Al termine della ricerca, il testo e lo
     * stato di abilitazione vengono ripristinati.
     * </p>
     *
     * @param searching {@code true} se la ricerca è in corso, {@code false} altrimenti.
     */
    private void updateSearchButtonState(boolean searching) {
        if (searchButton == null) return;

        if (searching) {
            searchButton.setText("🔄 Ricerca in corso...");
            searchButton.setDisable(true);
        } else {
            searchButton.setText("🔍 Cerca Libri");
            searchButton.setDisable(false);
        }
    }

    /**
     * Mostra una finestra di dialogo di tipo {@link Alert} per notificare all'utente
     * un errore di validazione dell'input.
     * <p>
     * Questo metodo di utilità crea e visualizza un'allerta con un'icona di avviso,
     * un titolo fisso ("Attenzione") e un messaggio personalizzato che spiega il
     * motivo del fallimento della validazione. È un metodo utile per fornire
     * feedback immediato e chiaro all'utente sui problemi riscontrati nel suo input.
     * </p>
     *
     * @param message La stringa che contiene il messaggio di errore da visualizzare.
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setHeaderText("Criterio di ricerca mancante");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra una finestra di dialogo di tipo {@link Alert} per notificare all'utente
     * un errore generico.
     * <p>
     * Questo metodo di utilità è progettato per gestire errori non di validazione,
     * come problemi di connessione o eccezioni impreviste durante l'operazione di
     * ricerca. Presenta all'utente una finestra di dialogo con un'icona di errore,
     * un titolo fisso ("Errore") e un messaggio dettagliato sull'accaduto.
     * </p>
     *
     * @param message La stringa che contiene il messaggio di errore da visualizzare.
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Errore durante l'operazione");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Imposta il callback da eseguire al completamento di una ricerca riuscita.
     * <p>
     * Questo metodo permette alla vista genitore di registrare una funzione
     * o un'espressione lambda che verrà invocata una volta che l'operazione
     * di ricerca avanzata è stata completata e i risultati sono disponibili.
     * Il callback riceve in input un oggetto {@link SearchResult}.
     * </p>
     *
     * @param callback Un {@link Consumer} che accetta un {@link SearchResult}.
     */
    public void setOnSearchExecuted(Consumer<SearchResult> callback) {
        this.onSearchExecuted = callback;
    }

    /**
     * Imposta il callback da eseguire quando il pannello viene chiuso.
     * <p>
     * Questo metodo consente a una classe esterna di registrare un'azione
     * (un {@link Runnable}) che verrà eseguita quando l'utente chiude il pannello
     * di ricerca avanzata. È utile per la gestione del ciclo di vita della UI,
     * permettendo alla vista genitore di rimuovere il pannello dal suo layout
     * e di aggiornare il proprio stato.
     * </p>
     *
     * @param callback Un {@link Runnable} che incapsula l'azione di chiusura.
     */
    public void setOnClosePanel(Runnable callback) {
        this.onClosePanel = callback;
    }
}