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
 * ✅ Header AGGIORNATO con FIX per pulsante X cliccabile
 * Mantiene il design esistente ma con gestione corretta dell'overlay
 */
public class Header {

    // Design system colori (coerenti con il resto dell'app)
    private static final String BG_COLOR = "#2c2c2e";
    private static final String SEARCH_BG = "#3a3a3c";
    private static final String ACCENT_COLOR = "#007aff";
    private static final String TEXT_PRIMARY = "#ffffff";
    private static final String TEXT_SECONDARY = "#8e8e93";
    private static final String BORDER_COLOR = "#48484a";

    private TextField searchField;
    private Button advancedSearchButton;
    private Consumer<String> searchHandler;
    private BookService bookService;
    private StackPane mainContainer; // Riferimento al container principale per i popup

    // Stato per gestire il popup di ricerca avanzata
    private boolean isAdvancedSearchOpen = false;
    private AdvancedSearchPanel currentAdvancedSearchPanel;

    public Header() {
        // Costruttore vuoto per compatibilità con codice esistente
    }

    public Header(BookService bookService, StackPane mainContainer) {
        this.bookService = bookService;
        this.mainContainer = mainContainer;
    }

    /**
     * Imposta il BookService (per compatibilità con codice esistente)
     */
    public void setBookService(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Imposta il container principale per i popup
     */
    public void setMainContainer(StackPane mainContainer) {
        this.mainContainer = mainContainer;
    }

    /**
     * Crea l'header della finestra principale
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
     * Crea il titolo dell'applicazione
     */
    private Label createAppTitle() {
        Label title = new Label("📚 BABO Library");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        return title;
    }

    /**
     * Crea l'area di ricerca migliorata
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
     * ✅ NUOVO: Debug completo della ricerca
     */
    public void debugSearch() {
        System.out.println("🔧 ===== DEBUG RICERCA HEADER =====");

        // Test stato componenti
        debugState();

        // Test BookService se disponibile
        if (bookService != null) {
            System.out.println("🔧 Test connessione server...");
            boolean serverOk = bookService.isServerAvailable();
            System.out.println("Server disponibile: " + (serverOk ? "✅ SI" : "❌ NO"));

            if (serverOk) {
                System.out.println("🔧 Test endpoint debug server...");
                try {
                    // Test con BookService client debug methods
                    if (bookService instanceof org.BABO.client.service.BookService) {
                        org.BABO.client.service.BookService clientService =
                                (org.BABO.client.service.BookService) bookService;

                        // Test connessione
                        clientService.testServerConnection();

                        // Debug endpoint
                        //clientService.debugServerEndpoints();

                        // Test ricerca semplice
                        System.out.println("🔧 Test ricerca 'test'...");
                        clientService.searchBooksAsync("test")
                                .thenAccept(results -> {
                                    System.out.println("🔧 Risultati test ricerca: " + results.size());
                                })
                                .exceptionally(throwable -> {
                                    System.err.println("🔧 Errore test ricerca: " + throwable.getMessage());
                                    return null;
                                });
                    }
                } catch (Exception e) {
                    System.err.println("🔧 Errore durante test: " + e.getMessage());
                }
            }
        } else {
            System.err.println("❌ BookService non disponibile per test");
        }

        // Test searchHandler
        if (searchHandler != null) {
            System.out.println("🔧 Test searchHandler con query 'debug'...");
            try {
                searchHandler.accept("debug");
                System.out.println("✅ SearchHandler chiamato correttamente");
            } catch (Exception e) {
                System.err.println("❌ Errore searchHandler: " + e.getMessage());
            }
        } else {
            System.err.println("❌ SearchHandler non configurato");
        }

        System.out.println("🔧 ===============================");
    }

    /**
     * ✅ NUOVO: Test rapido ricerca
     */
    public void testQuickSearch(String testQuery) {
        if (testQuery == null || testQuery.trim().isEmpty()) {
            testQuery = "test";
        }

        System.out.println("⚡ Test rapido ricerca: '" + testQuery + "'");

        if (searchField != null) {
            searchField.setText(testQuery);
        }

        if (searchHandler != null) {
            searchHandler.accept(testQuery);
            System.out.println("✅ Ricerca inviata");
        } else {
            System.err.println("❌ SearchHandler non disponibile");
        }
    }

    /**
     * Crea il campo di ricerca moderno
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
     * Crea il pulsante per la ricerca avanzata
     */
    private Button createAdvancedSearchButton() {
        Button button = new Button("⚙️");
        button.setTooltip(new Tooltip("Ricerca Avanzata"));

        // Rimuovi i puntini
        button.setTextOverrun(OverrunStyle.CLIP);
        button.setEllipsisString("");
        button.setWrapText(false);
        button.setAlignment(Pos.CENTER);

        // ✅ STILE BASE IDENTICO (stesso di hover ma diverso colore)
        button.setStyle(
                "-fx-background-color: " + SEARCH_BG + ";" +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-background-radius: 19px;" +        // ✅ STESSO radius dell'hover
                        "-fx-border-radius: 19px;" +            // ✅ STESSO border-radius dell'hover
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +              // ✅ STESSO border dell'hover
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

        // ✅ HOVER: CAMBIA SOLO I COLORI, NON LA FORMA
        button.setOnMouseEntered(e -> {
            if (!isAdvancedSearchOpen) {
                button.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + ";" +    // ✅ Solo colore diverso
                                "-fx-text-fill: white;" +                          // ✅ Solo colore testo diverso
                                "-fx-background-radius: 19px;" +                   // ✅ IDENTICO al normale
                                "-fx-border-radius: 19px;" +                       // ✅ IDENTICO al normale
                                "-fx-border-color: " + ACCENT_COLOR + ";" +        // ✅ Border stesso colore sfondo
                                "-fx-border-width: 1px;" +                         // ✅ IDENTICO al normale
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 16px;" +                           // ✅ IDENTICO al normale
                                "-fx-font-family: 'Segoe UI Emoji', sans-serif;" +
                                "-fx-min-width: 40px;" +                           // ✅ IDENTICO al normale
                                "-fx-min-height: 40px;" +                          // ✅ IDENTICO al normale
                                "-fx-max-width: 40px;" +                           // ✅ IDENTICO al normale
                                "-fx-max-height: 40px;" +                          // ✅ IDENTICO al normale
                                "-fx-pref-width: 40px;" +                          // ✅ IDENTICO al normale
                                "-fx-pref-height: 40px;" +                         // ✅ IDENTICO al normale
                                "-fx-padding: 0;" +                                // ✅ IDENTICO al normale
                                "-fx-text-overrun: clip;"                          // ✅ IDENTICO al normale
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
     * Gestisce l'apertura/chiusura della ricerca avanzata
     */
    private void toggleAdvancedSearch() {
        if (isAdvancedSearchOpen) {
            closeAdvancedSearch();
        } else {
            openAdvancedSearch();
        }
    }

    /**
     * Apre il popup di ricerca avanzata
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

            // ✅ FIX: Crea overlay CORRETTO che non blocca i click sui controlli interni
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
     * ✅ FIX DEFINITIVO: Crea overlay che NON blocca i click sui controlli interni
     */
    private StackPane createPopupOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // ✅ FIX CRITICO: Permetti ai controlli figli di ricevere eventi mouse
        overlay.setPickOnBounds(false);

        // ✅ Gestione più precisa dei click
        overlay.setOnMouseClicked(event -> {
            System.out.println("🖱️ Click rilevato su overlay");
            System.out.println("   - Target: " + event.getTarget().getClass().getSimpleName());
            System.out.println("   - Source: " + event.getSource().getClass().getSimpleName());

            // ✅ IMPORTANTE: Chiudi solo se il click è DAVVERO sull'overlay di sfondo
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

        // ✅ GESTIONE ESC invariata (funziona già)
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
     * Chiude il popup di ricerca avanzata
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
     * Aggiorna lo stile del pulsante ricerca avanzata
     */
    private void updateAdvancedSearchButtonStyle(boolean isOpen) {
        if (advancedSearchButton == null) return;

        advancedSearchButton.setText("⚙️");

        if (isOpen) {
            advancedSearchButton.setStyle(
                    "-fx-background-color: #ff3b30;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 19px;" +               // ✅ CONSISTENTE
                            "-fx-border-radius: 19px;" +                   // ✅ CONSISTENTE
                            "-fx-border-color: #ff3b30;" +
                            "-fx-border-width: 1px;" +                     // ✅ CONSISTENTE
                            "-fx-font-size: 16px;" +                       // ✅ CONSISTENTE con createAdvancedSearchButton
                            "-fx-font-family: 'Segoe UI Emoji', sans-serif;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-min-width: 40px;" +                       // ✅ CONSISTENTE
                            "-fx-min-height: 40px;" +                      // ✅ CONSISTENTE
                            "-fx-max-width: 40px;" +                       // ✅ CONSISTENTE
                            "-fx-max-height: 40px;" +                      // ✅ CONSISTENTE
                            "-fx-pref-width: 40px;" +                      // ✅ CONSISTENTE
                            "-fx-pref-height: 40px;" +                     // ✅ CONSISTENTE
                            "-fx-padding: 0;" +                            // ✅ CONSISTENTE
                            "-fx-text-overrun: clip;"                      // ✅ CONSISTENTE
            );
            advancedSearchButton.setText("✕");
        } else {
            advancedSearchButton.setStyle(
                    "-fx-background-color: " + SEARCH_BG + ";" +
                            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                            "-fx-background-radius: 19px;" +               // ✅ CONSISTENTE
                            "-fx-border-radius: 19px;" +                   // ✅ CONSISTENTE
                            "-fx-border-color: " + BORDER_COLOR + ";" +
                            "-fx-border-width: 1px;" +                     // ✅ CONSISTENTE
                            "-fx-font-size: 16px;" +                       // ✅ CONSISTENTE con createAdvancedSearchButton
                            "-fx-font-family: 'Segoe UI Emoji', sans-serif;" +
                            "-fx-cursor: hand;" +
                            "-fx-min-width: 40px;" +                       // ✅ CONSISTENTE
                            "-fx-min-height: 40px;" +                      // ✅ CONSISTENTE
                            "-fx-max-width: 40px;" +                       // ✅ CONSISTENTE
                            "-fx-max-height: 40px;" +                      // ✅ CONSISTENTE
                            "-fx-pref-width: 40px;" +                      // ✅ CONSISTENTE
                            "-fx-pref-height: 40px;" +                     // ✅ CONSISTENTE
                            "-fx-padding: 0;" +                            // ✅ CONSISTENTE
                            "-fx-text-overrun: clip;"                      // ✅ CONSISTENTE
            );
            advancedSearchButton.setText("⚙️");
        }
    }

    /**
     * Crea i controlli destri (placeholder per future funzionalità)
     */
    private HBox createRightControls() {
        HBox rightControls = new HBox(10);
        rightControls.setAlignment(Pos.CENTER_RIGHT);
        // Placeholder per futuri controlli (es. filtri, ordinamento, ecc.)
        return rightControls;
    }

    /**
     * Esegue la ricerca normale
     */
    /**
     * ✅ MIGLIORATO: Esegue la ricerca normale con debug
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
     * Gestisce il risultato della ricerca avanzata
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
            // Fallback finale
            if (!searchQuery.isEmpty()) {
                searchHandler.accept(searchQuery);
            }
        }
    }

    /**
     * Interfaccia per handler di ricerca avanzata
     */
    public interface AdvancedSearchHandler extends Consumer<String> {
        void handleAdvancedSearch(AdvancedSearchPanel.SearchResult result);
    }

    /**
     * Costruisce una query di ricerca dai parametri della ricerca avanzata
     */
    private String buildSearchQuery(AdvancedSearchPanel.SearchResult result) {
        StringBuilder query = new StringBuilder();

        if (result.getSearchType().contains("Titolo") && !result.getTitleQuery().isEmpty()) {
            // ✅ SPECIFICA CHE È UNA RICERCA SOLO TITOLO
            query.append("title-only:").append(result.getTitleQuery());

        } else if (result.getSearchType().contains("Autore") && !result.getAuthorQuery().isEmpty()) {
            query.append("author:").append(result.getAuthorQuery());

            // Aggiungi filtro anno se presente
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
     * Mostra un alert semplice
     */
    private void showSimpleAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters e setters per compatibilità
    public void setSearchHandler(Consumer<String> handler) {
        this.searchHandler = handler;
    }

    public Consumer<String> getSearchHandler() {
        return searchHandler;
    }

    public BookService getBookService() {
        return bookService;
    }

    public StackPane getMainContainer() {
        return mainContainer;
    }

    public boolean isAdvancedSearchOpen() {
        return isAdvancedSearchOpen;
    }

    /**
     * ✅ ORIGINALE: Pulisce il campo di ricerca
     */
    public void clearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
    }

    /**
     * ✅ ORIGINALE: Ottiene il testo di ricerca corrente
     */
    public String getSearchText() {
        return searchField != null ? searchField.getText() : "";
    }

    /**
     * ✅ ORIGINALE: Forza il focus sul campo di ricerca
     */
    public void focusSearchField() {
        if (searchField != null) {
            Platform.runLater(() -> {
                searchField.requestFocus();
            });
        }
    }

    /**
     * ✅ ORIGINALE: Ottiene il pannello di ricerca avanzata corrente (se aperto)
     */
    public AdvancedSearchPanel getCurrentAdvancedSearchPanel() {
        return currentAdvancedSearchPanel;
    }

    /**
     * ✅ ORIGINALE: Verifica se tutti i componenti sono inizializzati correttamente
     */
    public boolean isFullyInitialized() {
        return bookService != null && mainContainer != null && searchField != null && advancedSearchButton != null;
    }

    /**
     * ✅ ORIGINALE: Debug delle informazioni di stato
     */
    public void debugState() {
        System.out.println("🔍 ===== HEADER DEBUG STATE =====");
        System.out.println("BookService: " + (bookService != null ? "✅ OK" : "❌ NULL"));
        System.out.println("MainContainer: " + (mainContainer != null ? "✅ OK" : "❌ NULL"));
        System.out.println("SearchField: " + (searchField != null ? "✅ OK" : "❌ NULL"));
        System.out.println("AdvancedSearchButton: " + (advancedSearchButton != null ? "✅ OK" : "❌ NULL"));
        System.out.println("SearchHandler: " + (searchHandler != null ? "✅ OK" : "❌ NULL"));
        System.out.println("IsAdvancedSearchOpen: " + isAdvancedSearchOpen);
        System.out.println("CurrentAdvancedSearchPanel: " + (currentAdvancedSearchPanel != null ? "✅ ACTIVE" : "❌ NULL"));
        if (mainContainer != null) {
            System.out.println("MainContainer Children: " + mainContainer.getChildren().size());
        }
        System.out.println("================================");
    }
}