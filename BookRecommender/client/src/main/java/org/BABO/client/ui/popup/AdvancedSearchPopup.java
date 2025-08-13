package org.BABO.client.ui.popup;

import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import java.util.function.Consumer;

/**
 * Pannello per la ricerca avanzata completamente ridisegnato
 * per integrarsi perfettamente con il design Apple Books dell'app
 */
public class AdvancedSearchPopup {

    // Design system colori (coerenti con il resto dell'app)
    private static final String BG_PRIMARY = "#1a1a1c";
    private static final String BG_SECONDARY = "#2c2c2e";
    private static final String BG_CONTROL = "#3a3a3c";
    private static final String ACCENT_COLOR = "#007aff";
    private static final String ACCENT_HOVER = "#0056d6";
    private static final String TEXT_PRIMARY = "#ffffff";
    private static final String TEXT_SECONDARY = "#8e8e93";
    private static final String BORDER_COLOR = "#48484a";
    private static final String SUCCESS_COLOR = "#34c759";
    private static final String SHADOW_COLOR = "rgba(0,0,0,0.6)";

    private final BookService bookService;
    private ComboBox<String> searchTypeCombo;
    private TextField titleField;
    private TextField authorField;
    private TextField yearFromField;
    private TextField yearToField;
    private VBox dynamicFieldsContainer;
    private Consumer<SearchResult> onSearchExecuted;
    private Runnable onCloseCallback;
    private StackPane popupContainer;

    // Stati per animazioni
    private boolean isClosing = false;

    public static class SearchResult {
        public final String searchType;
        public final String titleQuery;
        public final String authorQuery;
        public final String yearFrom;
        public final String yearTo;

        public SearchResult(String searchType, String titleQuery, String authorQuery,
                            String yearFrom, String yearTo) {
            this.searchType = searchType;
            this.titleQuery = titleQuery;
            this.authorQuery = authorQuery;
            this.yearFrom = yearFrom;
            this.yearTo = yearTo;
        }
    }

    public AdvancedSearchPopup(BookService bookService) {
        this.bookService = bookService;
    }

    public void setOnSearchExecuted(Consumer<SearchResult> callback) {
        this.onSearchExecuted = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    /**
     * Mostra il popup con animazione di apertura
     */
    public void showPopup(StackPane parentContainer) {
        if (isClosing) return;

        Platform.runLater(() -> {
            try {
                // Crea il container del popup
                popupContainer = createPopupContainer();

                // Aggiunge al container principale
                parentContainer.getChildren().add(popupContainer);

                // Anima l'apertura
                animatePopupOpen();

                System.out.println("✅ Popup ricerca avanzata aperto");

            } catch (Exception e) {
                System.err.println("❌ Errore apertura popup ricerca: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Crea il container principale del popup
     */
    private StackPane createPopupContainer() {
        StackPane container = new StackPane();
        container.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // Click sul background per chiudere
        container.setOnMouseClicked(event -> {
            if (event.getTarget() == container) {
                closePopup();
            }
        });

        // ESC per chiudere
        container.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    closePopup();
                    break;
                case ENTER:
                    executeSearch();
                    break;
            }
        });

        container.setFocusTraversable(true);
        container.requestFocus();

        // Crea il content principale
        VBox popupContent = createPopupContent();
        container.getChildren().add(popupContent);

        return container;
    }

    /**
     * Crea il contenuto principale del popup
     */
    private VBox createPopupContent() {
        VBox popup = new VBox(0);
        popup.setMaxWidth(520);
        popup.setMaxHeight(650);
        popup.setAlignment(Pos.TOP_CENTER);
        popup.setStyle(createPopupStyle());

        // Header con titolo e chiudi
        HBox header = createHeader();

        // Contenuto scrollabile
        ScrollPane contentScroll = createContentScrollPane();

        // Footer con pulsanti
        HBox footer = createFooter();

        popup.getChildren().addAll(header, contentScroll, footer);
        return popup;
    }

    /**
     * Stile principale del popup
     */
    private String createPopupStyle() {
        return "-fx-background-color: " + BG_PRIMARY + ";" +
                "-fx-background-radius: 20px;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW_COLOR + ", 25, 0, 0, 10);" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 20px;";
    }

    /**
     * Crea l'header del popup
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(25, 25, 20, 25));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;" +
                "-fx-border-width: 0 0 1 0;");

        // Titolo principale
        Label titleLabel = new Label("🔍 Ricerca Avanzata");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(TEXT_PRIMARY));

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Pulsante chiudi
        Button closeButton = createCloseButton();

        header.getChildren().addAll(titleLabel, spacer, closeButton);
        return header;
    }

    /**
     * Crea il pulsante di chiusura moderno
     */
    private Button createCloseButton() {
        Button closeButton = new Button("✕");
        closeButton.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 32px;" +
                        "-fx-min-height: 32px;" +
                        "-fx-max-width: 32px;" +
                        "-fx-max-height: 32px;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 0;"
        );

        // Effetti hover
        closeButton.setOnMouseEntered(e ->
                closeButton.setStyle(
                        "-fx-background-color: #ff3b30;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 50%;" +
                                "-fx-min-width: 32px;" +
                                "-fx-min-height: 32px;" +
                                "-fx-max-width: 32px;" +
                                "-fx-max-height: 32px;" +
                                "-fx-cursor: hand;" +
                                "-fx-border-color: transparent;" +
                                "-fx-padding: 0;"
                )
        );

        closeButton.setOnMouseExited(e ->
                closeButton.setStyle(
                        "-fx-background-color: " + BG_CONTROL + ";" +
                                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 50%;" +
                                "-fx-min-width: 32px;" +
                                "-fx-min-height: 32px;" +
                                "-fx-max-width: 32px;" +
                                "-fx-max-height: 32px;" +
                                "-fx-cursor: hand;" +
                                "-fx-border-color: transparent;" +
                                "-fx-padding: 0;"
                )
        );

        closeButton.setOnAction(e -> closePopup());
        return closeButton;
    }

    /**
     * Crea l'area di contenuto scrollabile
     */
    private ScrollPane createContentScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;"
        );
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox content = createMainContent();
        scrollPane.setContent(content);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    /**
     * Crea il contenuto principale del form
     */
    private VBox createMainContent() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);

        // Sezione tipo di ricerca
        VBox searchTypeSection = createSearchTypeSection();

        // Container per campi dinamici
        dynamicFieldsContainer = new VBox(20);
        dynamicFieldsContainer.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(searchTypeSection, dynamicFieldsContainer);

        // Inizializza con ricerca per titolo
        updateDynamicFields();

        return content;
    }

    /**
     * Crea la sezione per la selezione del tipo di ricerca
     */
    private VBox createSearchTypeSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("Tipo di ricerca");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll(
                "Ricerca per Titolo",
                "Ricerca per Autore",
                "Ricerca per Autore e Anno"
        );
        searchTypeCombo.setValue("Ricerca per Titolo");
        styleComboBox(searchTypeCombo);

        // Handler per aggiornare i campi dinamici
        searchTypeCombo.setOnAction(e -> updateDynamicFields());

        section.getChildren().addAll(sectionTitle, searchTypeCombo);
        return section;
    }

    /**
     * Aggiorna i campi dinamici in base al tipo di ricerca selezionato
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

        // Animazione di cambio contenuto
        animateContentChange();
    }

    /**
     * Crea i campi per la ricerca per titolo
     */
    private void createTitleSearchFields() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("📖 Cerca per Titolo");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        titleField = new TextField();
        titleField.setPromptText("Inserisci il titolo del libro...");
        styleTextField(titleField);

        Label hintLabel = new Label("💡 Suggerimento: Puoi inserire anche solo una parte del titolo");
        hintLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        hintLabel.setTextFill(Color.web(TEXT_SECONDARY));
        hintLabel.setWrapText(true);

        section.getChildren().addAll(sectionTitle, titleField, hintLabel);
        dynamicFieldsContainer.getChildren().add(section);
    }

    /**
     * Crea i campi per la ricerca per autore
     */
    private void createAuthorSearchFields() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("👤 Cerca per Autore");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        authorField = new TextField();
        authorField.setPromptText("Inserisci il nome dell'autore...");
        styleTextField(authorField);

        Label hintLabel = new Label("💡 Suggerimento: Puoi cercare per nome, cognome o entrambi");
        hintLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        hintLabel.setTextFill(Color.web(TEXT_SECONDARY));
        hintLabel.setWrapText(true);

        section.getChildren().addAll(sectionTitle, authorField, hintLabel);
        dynamicFieldsContainer.getChildren().add(section);
    }

    /**
     * Crea i campi per la ricerca per autore e anno
     */
    private void createAuthorYearSearchFields() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("👤📅 Cerca per Autore e Anno");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        // Campo autore
        authorField = new TextField();
        authorField.setPromptText("Nome dell'autore...");
        styleTextField(authorField);

        // Sezione anno
        Label yearLabel = new Label("Anno di pubblicazione");
        yearLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        yearLabel.setTextFill(Color.web(TEXT_PRIMARY));

        HBox yearBox = new HBox(15);
        yearBox.setAlignment(Pos.CENTER_LEFT);

        yearFromField = new TextField();
        yearFromField.setPromptText("Dal...");
        yearFromField.setMaxWidth(100);
        styleTextField(yearFromField);

        Label toLabel = new Label("a");
        toLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        toLabel.setTextFill(Color.web(TEXT_SECONDARY));

        yearToField = new TextField();
        yearToField.setPromptText("Al...");
        yearToField.setMaxWidth(100);
        styleTextField(yearToField);

        yearBox.getChildren().addAll(yearFromField, toLabel, yearToField);

        Label hintLabel = new Label("💡 Suggerimento: Lascia vuoto l'anno per cercare solo per autore");
        hintLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        hintLabel.setTextFill(Color.web(TEXT_SECONDARY));
        hintLabel.setWrapText(true);

        section.getChildren().addAll(sectionTitle, authorField, yearLabel, yearBox, hintLabel);
        dynamicFieldsContainer.getChildren().add(section);
    }

    /**
     * Stile per ComboBox
     */
    private void styleComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle(
                "-fx-background-color: " + BG_SECONDARY + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-padding: 12px 16px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Stile per TextField
     */
    private void styleTextField(TextField textField) {
        textField.setStyle(
                "-fx-background-color: " + BG_SECONDARY + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-padding: 14px 16px;" +
                        "-fx-font-size: 14px;"
        );

        // Effetti focus
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textField.setStyle(
                        "-fx-background-color: " + BG_SECONDARY + ";" +
                                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                                "-fx-background-radius: 12px;" +
                                "-fx-border-color: " + ACCENT_COLOR + ";" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 12px;" +
                                "-fx-padding: 13px 15px;" +
                                "-fx-font-size: 14px;"
                );
            } else {
                textField.setStyle(
                        "-fx-background-color: " + BG_SECONDARY + ";" +
                                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
                                "-fx-background-radius: 12px;" +
                                "-fx-border-color: " + BORDER_COLOR + ";" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 12px;" +
                                "-fx-padding: 14px 16px;" +
                                "-fx-font-size: 14px;"
                );
            }
        });

        textField.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Crea il footer con i pulsanti di azione
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(20, 25, 25, 25));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-border-color: " + BORDER_COLOR + " transparent transparent transparent;" +
                "-fx-border-width: 1 0 0 0;");

        Button cancelButton = createSecondaryButton("Annulla");
        cancelButton.setOnAction(e -> closePopup());

        Button searchButton = createPrimaryButton("🔍 Cerca");
        searchButton.setOnAction(e -> executeSearch());

        footer.getChildren().addAll(cancelButton, searchButton);
        return footer;
    }

    /**
     * Crea un pulsante primario (azione principale)
     */
    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 12px 24px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-min-width: 120px;"
        );

        // Effetti hover
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: " + ACCENT_HOVER + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 12px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px 24px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: 600;" +
                                "-fx-min-width: 120px;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 12px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px 24px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: 600;" +
                                "-fx-min-width: 120px;"
                )
        );

        return button;
    }

    /**
     * Crea un pulsante secondario (azione secondaria)
     */
    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 12px 24px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-min-width: 120px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;"
        );

        // Effetti hover
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: " + BG_SECONDARY + ";" +
                                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-background-radius: 12px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px 24px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: 600;" +
                                "-fx-min-width: 120px;" +
                                "-fx-border-color: " + BORDER_COLOR + ";" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 12px;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: " + BG_CONTROL + ";" +
                                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-background-radius: 12px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 12px 24px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: 600;" +
                                "-fx-min-width: 120px;" +
                                "-fx-border-color: " + BORDER_COLOR + ";" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 12px;"
                )
        );

        return button;
    }

    /**
     * Esegue la ricerca con i parametri attuali
     */
    private void executeSearch() {
        if (onSearchExecuted == null) return;

        String searchType = searchTypeCombo.getValue();
        String titleQuery = titleField != null ? titleField.getText().trim() : "";
        String authorQuery = authorField != null ? authorField.getText().trim() : "";
        String yearFrom = yearFromField != null ? yearFromField.getText().trim() : "";
        String yearTo = yearToField != null ? yearToField.getText().trim() : "";

        // Validazione base
        boolean hasValidInput = false;
        if (searchType.contains("Titolo") && !titleQuery.isEmpty()) {
            hasValidInput = true;
        } else if (searchType.contains("Autore") && !authorQuery.isEmpty()) {
            hasValidInput = true;
        }

        if (!hasValidInput) {
            showValidationError("Per favore inserisci almeno un criterio di ricerca valido.");
            return;
        }

        SearchResult result = new SearchResult(searchType, titleQuery, authorQuery, yearFrom, yearTo);

        System.out.println("🔍 Esecuzione ricerca avanzata:");
        System.out.println("   Tipo: " + searchType);
        System.out.println("   Titolo: " + titleQuery);
        System.out.println("   Autore: " + authorQuery);
        System.out.println("   Anno da: " + yearFrom);
        System.out.println("   Anno a: " + yearTo);

        onSearchExecuted.accept(result);
        closePopup();
    }

    /**
     * Mostra un messaggio di errore di validazione
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validazione Ricerca");
        alert.setHeaderText("Parametri di ricerca non validi");
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Chiude il popup con animazione
     */
    private void closePopup() {
        if (isClosing) return;
        isClosing = true;

        animatePopupClose(() -> {
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }

            // Rimuovi dal container padre
            if (popupContainer != null && popupContainer.getParent() instanceof Pane) {
                ((Pane) popupContainer.getParent()).getChildren().remove(popupContainer);
            }

            System.out.println("✅ Popup ricerca avanzata chiuso");
        });
    }

    /**
     * Animazione apertura popup
     */
    private void animatePopupOpen() {
        if (popupContainer == null) return;

        // Inizia trasparente e più piccolo
        popupContainer.setOpacity(0);
        popupContainer.setScaleX(0.8);
        popupContainer.setScaleY(0.8);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popupContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Scale in
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), popupContainer);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        ParallelTransition openAnimation = new ParallelTransition(fadeIn, scaleIn);
        openAnimation.setDelay(Duration.millis(50));
        openAnimation.play();
    }

    /**
     * Animazione chiusura popup
     */
    private void animatePopupClose(Runnable onComplete) {
        if (popupContainer == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popupContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Scale out
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), popupContainer);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.9);
        scaleOut.setToY(0.9);

        ParallelTransition closeAnimation = new ParallelTransition(fadeOut, scaleOut);
        closeAnimation.setOnFinished(e -> {
            if (onComplete != null) onComplete.run();
        });
        closeAnimation.play();
    }

    /**
     * Animazione cambio contenuto dinamico
     */
    private void animateContentChange() {
        if (dynamicFieldsContainer == null) return;

        // Breve animazione fade per il cambio contenuto
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), dynamicFieldsContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.8);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), dynamicFieldsContainer);
        fadeIn.setFromValue(0.8);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> fadeIn.play());
        fadeOut.play();
    }

    /**
     * Metodo di utilità per gestire l'integrazione con Header
     * Fornisce un modo semplice per mostrare il popup dalla classe Header
     */
    public static void showFromHeader(StackPane parentContainer, BookService bookService,
                                      Consumer<SearchResult> onSearchExecuted) {
        Platform.runLater(() -> {
            AdvancedSearchPopup popup = new AdvancedSearchPopup(bookService);
            popup.setOnSearchExecuted(onSearchExecuted);
            popup.showPopup(parentContainer);
        });
    }

    /**
     * Getter per testare lo stato del popup
     */
    public boolean isClosing() {
        return isClosing;
    }

    /**
     * Cleanup delle risorse quando il popup viene distrutto
     */
    public void cleanup() {
        onSearchExecuted = null;
        onCloseCallback = null;
        popupContainer = null;
        isClosing = false;
    }
}