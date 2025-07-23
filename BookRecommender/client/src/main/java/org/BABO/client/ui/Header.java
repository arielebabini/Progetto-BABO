package org.BABO.client.ui;

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
import java.util.function.Consumer;

/**
 * Header aggiornato con integrazione completa per la ricerca avanzata
 * Mantiene il design esistente ma con migliore gestione del popup
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
        HBox.setHgrow(searchField, Priority.ALWAYS);

        return searchArea;
    }

    /**
     * Crea il campo di ricerca con stile migliorato
     */
    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("🔍 Cerca libri, autori...");
        field.setStyle(
                "-fx-background-color: " + SEARCH_BG + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-background-radius: 20px;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 10px 20px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-min-height: 40px;" +
                        "-fx-max-height: 40px;"
        );

        // Effetti focus
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: " + SEARCH_BG + ";" +
                                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                                "-fx-background-radius: 20px;" +
                                "-fx-border-color: " + ACCENT_COLOR + ";" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 20px;" +
                                "-fx-padding: 8px 18px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-height: 40px;"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: " + SEARCH_BG + ";" +
                                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                                "-fx-background-radius: 20px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-padding: 10px 20px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-height: 40px;"
                );
            }
        });

        // Handler per ENTER
        field.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performSearch();
            }
        });

        return field;
    }

    /**
     * Crea il pulsante per la ricerca avanzata
     */
    private Button createAdvancedSearchButton() {
        Button button = new Button("⚙️");
        button.setTooltip(new Tooltip("Ricerca Avanzata"));
        button.setStyle(
                "-fx-background-color: " + SEARCH_BG + ";" +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-background-radius: 20px;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16px;" +
                        "-fx-min-width: 40px;" +
                        "-fx-min-height: 40px;" +
                        "-fx-max-width: 40px;" +
                        "-fx-max-height: 40px;" +
                        "-fx-padding: 0;"
        );

        // Effetti hover
        button.setOnMouseEntered(e -> {
            if (!isAdvancedSearchOpen) {
                button.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 16px;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-width: 40px;" +
                                "-fx-max-height: 40px;" +
                                "-fx-padding: 0;"
                );
            }
        });

        button.setOnMouseExited(e -> {
            if (!isAdvancedSearchOpen) {
                button.setStyle(
                        "-fx-background-color: " + SEARCH_BG + ";" +
                                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                                "-fx-background-radius: 20px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 16px;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-width: 40px;" +
                                "-fx-max-height: 40px;" +
                                "-fx-padding: 0;"
                );
            }
        });

        // Handler click per aprire ricerca avanzata
        button.setOnAction(e -> showAdvancedSearch());

        return button;
    }

    /**
     * Crea i controlli del lato destro (placeholder per future funzionalità)
     */
    private HBox createRightControls() {
        HBox rightControls = new HBox(10);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        // Placeholder per future funzionalità come:
        // - Pulsante filtri
        // - Pulsante vista
        // - Pulsante profilo utente
        // - etc.

        return rightControls;
    }

    /**
     * Esegue la ricerca normale (dal campo di testo)
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty() && searchHandler != null) {
            System.out.println("🔍 Ricerca normale: " + query);
            searchHandler.accept(query);
        }
    }

    /**
     * Mostra il popup di ricerca avanzata
     */
    private void showAdvancedSearch() {
        if (isAdvancedSearchOpen) {
            System.out.println("⚠️ Popup ricerca avanzata già aperto");
            return;
        }

        if (mainContainer == null) {
            System.err.println("❌ MainContainer non impostato - impossibile aprire ricerca avanzata");
            showSimpleAlert("Errore", "Errore di configurazione: impossibile aprire la ricerca avanzata.");
            return;
        }

        if (bookService == null) {
            System.err.println("❌ BookService non impostato - impossibile aprire ricerca avanzata");
            showSimpleAlert("Errore", "Servizio libri non disponibile.");
            return;
        }

        isAdvancedSearchOpen = true;
        updateAdvancedSearchButtonState(true);

        System.out.println("🔍 Apertura ricerca avanzata...");

        // Crea il pannello di ricerca avanzata
        currentAdvancedSearchPanel = new AdvancedSearchPanel(bookService);

        // Callback per quando viene eseguita una ricerca
        currentAdvancedSearchPanel.setOnSearchExecuted(result -> {
            System.out.println("✅ Ricerca avanzata eseguita: " + result.getBooks().size() + " risultati");
            handleAdvancedSearchResult(result);
            closeAdvancedSearch(); // Chiudi il popup dopo la ricerca
        });

        // Crea overlay per il popup
        StackPane overlay = createPopupOverlay();
        overlay.getChildren().add(currentAdvancedSearchPanel);
        StackPane.setAlignment(currentAdvancedSearchPanel, Pos.CENTER);

        // Aggiungi al container principale
        mainContainer.getChildren().add(overlay);

        System.out.println("✅ Popup ricerca avanzata aperto");
    }

    /**
     * Crea l'overlay per il popup
     */
    private StackPane createPopupOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // Click sul background per chiudere
        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                closeAdvancedSearch();
            }
        });

        // ESC per chiudere
        overlay.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeAdvancedSearch();
            }
        });

        overlay.setFocusTraversable(true);
        Platform.runLater(() -> overlay.requestFocus());

        return overlay;
    }

    /**
     * Chiude la ricerca avanzata
     */
    public void closeAdvancedSearch() {
        if (!isAdvancedSearchOpen) {
            return;
        }

        System.out.println("🔍 Chiusura ricerca avanzata...");

        isAdvancedSearchOpen = false;
        updateAdvancedSearchButtonState(false);

        // Rimuovi l'overlay dal container principale
        if (mainContainer != null && !mainContainer.getChildren().isEmpty()) {
            // Trova e rimuovi l'overlay della ricerca avanzata
            mainContainer.getChildren().removeIf(node -> {
                if (node instanceof StackPane) {
                    StackPane stackPane = (StackPane) node;
                    return stackPane.getChildren().stream()
                            .anyMatch(child -> child instanceof AdvancedSearchPanel);
                }
                return false;
            });
        }

        currentAdvancedSearchPanel = null;
        System.out.println("✅ Ricerca avanzata chiusa");
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
                searchField.setText(searchQuery);
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
            query.append(result.getTitleQuery());
        } else if (result.getSearchType().contains("Autore")) {
            if (!result.getAuthorQuery().isEmpty()) {
                query.append("author:").append(result.getAuthorQuery());
            }

            // ✅ CORREZIONE: Aggiungi filtri anno se specificati
            if (!result.getYearFrom().isEmpty() || !result.getYearTo().isEmpty()) {
                if (query.length() > 0) query.append(" ");
                query.append("year:");

                if (!result.getYearFrom().isEmpty() && !result.getYearTo().isEmpty()) {
                    if (result.getYearFrom().equals(result.getYearTo())) {
                        // Anno singolo
                        query.append(result.getYearFrom());
                    } else {
                        // Range di anni
                        query.append(result.getYearFrom()).append("-").append(result.getYearTo());
                    }
                } else if (!result.getYearFrom().isEmpty()) {
                    query.append(result.getYearFrom()).append("..");
                } else {
                    query.append("..").append(result.getYearTo());
                }
            }
        }

        String finalQuery = query.toString().trim();
        System.out.println("🔧 Query costruita: '" + finalQuery + "'");
        return finalQuery;
    }

    /**
     * Aggiorna lo stato visivo del pulsante ricerca avanzata
     */
    private void updateAdvancedSearchButtonState(boolean isOpen) {
        if (advancedSearchButton == null) return;

        Platform.runLater(() -> {
            if (isOpen) {
                // Stato attivo (popup aperto)
                advancedSearchButton.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 16px;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-width: 40px;" +
                                "-fx-max-height: 40px;" +
                                "-fx-padding: 0;"
                );
            } else {
                // Stato normale
                advancedSearchButton.setStyle(
                        "-fx-background-color: " + SEARCH_BG + ";" +
                                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                                "-fx-background-radius: 20px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 16px;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-width: 40px;" +
                                "-fx-max-height: 40px;" +
                                "-fx-padding: 0;"
                );
            }
        });
    }

    /**
     * Mostra un alert semplice per errori
     */
    private void showSimpleAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Applica stile dark
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle(
                    "-fx-background-color: #1a1a1c;" +
                            "-fx-text-fill: white;"
            );

            alert.showAndWait();
        });
    }

    // =====================================================
    // METODI PUBBLICI PER COMPATIBILITÀ E CONTROLLO
    // =====================================================

    /**
     * Pulisce il campo di ricerca
     */
    public void clearSearch() {
        if (searchField != null) {
            Platform.runLater(() -> {
                searchField.clear();
            });
        }
    }

    /**
     * Imposta il testo nel campo di ricerca
     */
    public void setSearchText(String text) {
        if (searchField != null) {
            Platform.runLater(() -> {
                searchField.setText(text);
            });
        }
    }

    /**
     * Ottiene il testo corrente nel campo di ricerca
     */
    public String getSearchText() {
        return searchField != null ? searchField.getText() : "";
    }

    /**
     * Imposta l'handler per le ricerche
     */
    public void setSearchHandler(Consumer<String> handler) {
        this.searchHandler = handler;
    }

    /**
     * Forza il focus sul campo di ricerca
     */
    public void focusSearchField() {
        if (searchField != null) {
            Platform.runLater(() -> {
                searchField.requestFocus();
            });
        }
    }

    /**
     * Verifica se la ricerca avanzata è attualmente aperta
     */
    public boolean isAdvancedSearchOpen() {
        return isAdvancedSearchOpen;
    }

    /**
     * Ottiene il pannello di ricerca avanzata corrente (se aperto)
     */
    public AdvancedSearchPanel getCurrentAdvancedSearchPanel() {
        return currentAdvancedSearchPanel;
    }

    /**
     * Verifica se tutti i componenti sono inizializzati correttamente
     */
    public boolean isFullyInitialized() {
        return bookService != null && mainContainer != null && searchField != null && advancedSearchButton != null;
    }

    /**
     * Debug delle informazioni di stato
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