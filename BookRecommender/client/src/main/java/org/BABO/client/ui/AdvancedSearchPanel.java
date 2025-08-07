package org.BABO.client.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.BABO.shared.model.Book;
import org.BABO.client.service.BookService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ✅ VERSIONE CORRETTA - Pannello di ricerca avanzata senza Platform.runLater
 * Corregge il problema del blocco quando si preme la X
 */
public class AdvancedSearchPanel extends VBox {

    // Costanti per i colori - ESATTO come LibraryPanel
    private static final String BG_PRIMARY = "#1a1a1c";
    private static final String BG_SECONDARY = "#2c2c2e";
    private static final String BG_CONTROL = "#3a3a3c";
    private static final String TEXT_PRIMARY = "#ffffff";
    private static final String TEXT_SECONDARY = "#8e8e93";
    private static final String ACCENT_COLOR = "#007aff";
    private static final String BORDER_COLOR = "#38383a";
    private static final String SUCCESS_COLOR = "#34c759";
    private static final String ERROR_COLOR = "#ff3b30";

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
         * ✅ AGGIUNTO: Costruisce la descrizione dei risultati di ricerca
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
        public List<Book> getBooks() { return books; }
        public String getSearchType() { return searchType; }
        public String getTitleQuery() { return titleQuery; }
        public String getAuthorQuery() { return authorQuery; }
        public String getYearFrom() { return yearFrom; }
        public String getYearTo() { return yearTo; }

        /**
         * ✅ AGGIUNTO: Metodo getDescription() richiesto da ContentArea
         */
        public String getDescription() { return description; }

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

    public AdvancedSearchPanel(BookService bookService) {
        this.bookService = bookService;
        setupUI();
        setupEventHandlers();
    }

    /**
     * ✅ CORREZIONE: Setup UI completo
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
     * ✅ FIX DEFINITIVO: Creazione header con pulsante X garantito cliccabile
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(25, 25, 10, 25));
        header.setPrefHeight(60);
        header.setMinHeight(60);

        // ✅ IMPORTANTE: Assicurati che l'header non blocchi gli eventi
        header.setPickOnBounds(false);
        header.setMouseTransparent(false);

        // Crea il pulsante X
        closeButton = createCloseButton();

        // ✅ IMPORTANTE: Aggiungi solo il pulsante senza altri elementi che possano interferire
        header.getChildren().add(closeButton);

        System.out.println("✅ Header creato con pulsante X");
        System.out.println("   - Header dimensioni: " + header.getPrefWidth() + "x" + header.getPrefHeight());
        System.out.println("   - Pulsante visibile: " + closeButton.isVisible());
        System.out.println("   - Pulsante gestito: " + closeButton.isManaged());

        return header;
    }

    /**
     * ✅ CORREZIONE: Creazione area titolo
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
     * ✅ CORREZIONE: Creazione area contenuto scorrevole
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
     * ✅ CORREZIONE: Creazione contenuto principale
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
     * ✅ CORREZIONE: Setup ComboBox tipo ricerca (SOLO 3 OPZIONI)
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

        // FIX: Testo leggibile nelle opzioni del dropdown
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

        // FIX: Testo leggibile per l'opzione selezionata
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
     * ✅ CORREZIONE: Creazione sezione tipo ricerca
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
     * ✅ CORREZIONE: Creazione sezione pulsanti
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
     * ✅ TEST DEFINITIVO: Pulsante X con TUTTI i possibili event handler
     */
    private Button createCloseButton() {
        Button closeBtn = new Button("✕");

        // ✅ STEP 1: Dimensioni fisse e esplicite
        closeBtn.setMinSize(40, 40);
        closeBtn.setPrefSize(40, 40);
        closeBtn.setMaxSize(40, 40);

        // ✅ STEP 2: Stile di base SEMPLIFICATO per garantire visibilità
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

        // ✅ STEP 3: Proprietà critiche per la cliccabilità
        closeBtn.setPickOnBounds(true);
        closeBtn.setFocusTraversable(true);
        closeBtn.setMouseTransparent(false);
        closeBtn.setDisable(false);
        closeBtn.setVisible(true);
        closeBtn.setManaged(true);

        // ✅ TEST: TUTTI I POSSIBILI EVENT HANDLER

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
     * ✅ CORREZIONE: Creazione pulsante ricerca
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
     * ✅ CORREZIONE CRITICA: Setup event handlers COMPLETAMENTE SENZA Platform.runLater
     */
    private void setupEventHandlers() {
        System.out.println("🔧 Setup event handlers per AdvancedSearchPanel...");

        // ✅ CORREZIONE: Handler ESC per chiudere SENZA Platform.runLater
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

        // ✅ CORREZIONE CRITICA: Handler pulsante X SENZA Platform.runLater
        closeButton.setOnAction(e -> {
            System.out.println("🖱️ Pulsante X cliccato - chiusura pannello");
            e.consume(); // Consuma l'evento per evitare conflitti
            closePanel(); // DIRETTO - NO Platform.runLater che causava il blocco!
        });

        // ✅ CORREZIONE: Handler pulsante cerca SENZA Platform.runLater
        searchButton.setOnAction(e -> {
            System.out.println("🖱️ Pulsante cerca cliccato");
            e.consume(); // Consuma l'evento
            if (!isSearching) {
                executeSearch(); // DIRETTO - NO Platform.runLater!
            }
        });

        // ✅ Focus management
        setFocusTraversable(true);

        // ✅ Previeni propagazione eventi mouse sul pannello
        setOnMouseClicked(e -> {
            e.consume(); // Impedisce che il click sul pannello chiuda il popup
            requestFocus(); // Mantiene il focus sul pannello
        });

        System.out.println("✅ Event handlers configurati correttamente");
    }

    /**
     * ✅ CORREZIONE: Aggiorna campi dinamici in base al tipo di ricerca (SOLO 3 TIPI)
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
     * ✅ CORREZIONE: Creazione campi ricerca per titolo
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
     * ✅ CORREZIONE: Creazione campi ricerca per autore
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
     * ✅ CORREZIONE: Creazione campi ricerca per autore e anno
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
     * ✅ CORREZIONE: Creazione campo di testo stilizzato
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
     * ✅ CORREZIONE: Esecuzione ricerca SENZA Platform.runLater con DEBUG
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

        // Validazione input
        if (titleQuery.isEmpty() && authorQuery.isEmpty() && yearFrom.isEmpty() && yearTo.isEmpty()) {
            System.out.println("❌ Nessun criterio di ricerca inserito");
            showValidationError("Inserisci almeno un criterio di ricerca");
            return;
        }

        try {
            isSearching = true;
            updateSearchButtonState(true);
            System.out.println("🔄 Stato ricerca impostato su TRUE");

            // ✅ IMPORTANTE: Esecuzione ricerca DIRETTA senza Platform.runLater
            List<Book> results = performAdvancedSearch(searchType, titleQuery, authorQuery, yearFrom, yearTo);
            System.out.println("📚 Ricerca completata: " + results.size() + " risultati");

            SearchResult searchResult = new SearchResult(results, searchType, titleQuery, authorQuery, yearFrom, yearTo);
            System.out.println("📦 SearchResult creato: " + searchResult.getDescription());

            // ✅ IMPORTANTE: Callback DIRETTO senza Platform.runLater
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
            showErrorAlert("Errore durante la ricerca");
        } finally {
            isSearching = false;
            updateSearchButtonState(false);
            System.out.println("🔄 Stato ricerca ripristinato su FALSE");
            System.out.println("🔍 === FINE RICERCA AVANZATA ===");
        }
    }

    /**
     * ✅ FIX DEFINITIVO: Chiusura pannello con callback PRIMA del cleanup
     */
    private void closePanel() {
        try {
            System.out.println("🔒 Chiusura pannello ricerca avanzata...");

            // ✅ FIX CRITICO: Chiama il callback PRIMA del cleanup!
            if (onClosePanel != null) {
                System.out.println("🔗 Chiamata callback chiusura Header...");
                onClosePanel.run(); // DIRETTO - chiamata PRIMA del cleanup
            } else {
                System.err.println("❌ ERRORE: onClosePanel callback è NULL!");
            }

            // ✅ Cleanup DOPO aver chiamato il callback
            cleanup();

            System.out.println("✅ Pannello chiuso correttamente");

        } catch (Exception ex) {
            System.err.println("❌ Errore chiusura pannello: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * ✅ CORREZIONE: Cleanup completo per evitare memory leak
     */
    public void cleanup() {
        try {
            System.out.println("🧹 Cleanup AdvancedSearchPanel...");

            // ✅ IMPORTANTE: Rimuovi TUTTI gli event handlers
            setOnKeyPressed(null);
            setOnMouseClicked(null);
            setFocusTraversable(false);

            // ✅ Cleanup dei campi di testo
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

            // ✅ Cleanup dei pulsanti
            if (searchButton != null) {
                searchButton.setOnAction(null);
            }

            if (closeButton != null) {
                closeButton.setOnAction(null);
            }

            if (searchTypeCombo != null) {
                searchTypeCombo.setOnAction(null);
            }

            // ✅ IMPORTANTE: Reset dei callback
            onSearchExecuted = null;
            onClosePanel = null;

            System.out.println("✅ Cleanup completato");

        } catch (Exception e) {
            System.err.println("❌ Errore durante cleanup: " + e.getMessage());
        }
    }

    /**
     * ✅ CORREZIONE: Esecuzione ricerca nel BookService SOLO con 3 metodi
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

                // ✅ USA L'ENDPOINT SPECIFICO per solo titolo
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

            // ✅ FILTRO ANNO se specificato (sempre lato client)
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
     * ✅ CORREZIONE: Filtra i libri per anno di pubblicazione
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
     * ✅ CORREZIONE: Aggiorna stato pulsante ricerca
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
     * ✅ CORREZIONE: Mostra errore di validazione
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setHeaderText("Criterio di ricerca mancante");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * ✅ CORREZIONE: Mostra errore generico
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Errore durante l'operazione");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ CORREZIONE: Setter per i callback
    public void setOnSearchExecuted(Consumer<SearchResult> callback) {
        this.onSearchExecuted = callback;
    }

    public void setOnClosePanel(Runnable callback) {
        this.onClosePanel = callback;
    }
}