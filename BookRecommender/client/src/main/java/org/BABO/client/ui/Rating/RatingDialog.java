package org.BABO.client.ui.Rating;

import org.BABO.client.ui.Home.ImageUtils;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.BookRating;
import org.BABO.shared.dto.Rating.RatingRequest;
import org.BABO.client.service.ClientRatingService;
import javafx.application.Platform;
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

import java.util.function.Consumer;

/**
 * Dialog personalizzato per la creazione e la modifica di una valutazione di un libro.
 * <p>
 * Questo dialog modale offre all'utente un'interfaccia completa per interagire con il sistema di valutazione.
 * Permette di assegnare un punteggio a vari aspetti del libro tramite slider, scrivere una recensione testuale,
 * e visualizzare un'anteprima in tempo reale del punteggio medio. Supporta sia la creazione di nuove
 * valutazioni che la modifica di quelle esistenti.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 * <li><strong>Valutazione a più dimensioni:</strong> L'utente può valutare aspetti specifici come Stile, Contenuto,
 * Piacevolezza, Originalità ed Edizione, ognuno con un punteggio da 1 a 5.</li>
 * <li><strong>Recensione testuale:</strong> Include un'area di testo per scrivere una recensione, con un
 * contatore di caratteri per limitare il testo a 1000 caratteri.</li>
 * <li><strong>Anteprima in tempo reale:</strong> Un'anteprima dinamica calcola e visualizza il punteggio
 * medio e una descrizione qualitativa (es. "Eccellente", "Discreto") man mano che l'utente modifica i voti.</li>
 * <li><strong>Modalità di creazione e modifica:</strong> Il dialog si adatta automaticamente a seconda che
 * si stia creando una nuova valutazione o modificandone una esistente, mostrando i bottoni e i campi
 * rilevanti (es. il pulsante "Elimina" solo in modalità modifica).</li>
 * <li><strong>Integrazione con servizi:</strong> Comunica in modo asincrono con {@link ClientRatingService}
 * per salvare, aggiornare o eliminare la valutazione.</li>
 * <li><strong>Gestione degli eventi:</strong> Include la gestione della chiusura tramite il pulsante "Annulla"
 * o cliccando al di fuori del dialog.</li>
 * </ul>
 *
 * <h3>Integrazione:</h3>
 * <p>
 * La classe si interfaccia con {@link Book} per recuperare i dettagli del libro e con {@link BookRating}
 * per popolare i campi in caso di modifica. Utilizza un {@link Consumer} per notificare il chiamante
 * del salvataggio o dell'eliminazione della valutazione.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see org.BABO.shared.model.BookRating
 * @see org.BABO.client.service.ClientRatingService
 */
public class RatingDialog {

    // Stato interno
    private Stage dialogStage;
    private BookRating currentRating;
    private Book book;
    private String username;
    private Consumer<BookRating> onRatingSaved;
    private ClientRatingService ratingService;

    // Componenti UI
    private Slider styleSlider;
    private Slider contentSlider;
    private Slider pleasantnessSlider;
    private Slider originalitySlider;
    private Slider editionSlider;
    private TextArea reviewTextArea;
    private Label averageLabel;
    private Label qualityLabel;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    // Labels per i valori degli slider
    private Label styleValueLabel;
    private Label contentValueLabel;
    private Label pleasantnessValueLabel;
    private Label originalityValueLabel;
    private Label editionValueLabel;

    /**
     * Costruttore per creare una <b>nuova</b> valutazione.
     * <p>
     * Questo costruttore inizializza il dialog per l'inserimento di una nuova valutazione.
     * Non richiede un oggetto {@link BookRating} esistente.
     * </p>
     * @param book Il libro per cui si sta creando la valutazione.
     * @param username L'username dell'utente che sta creando la valutazione.
     * @param onRatingSaved Il consumer da chiamare quando la valutazione viene salvata con successo.
     */
    public RatingDialog(Book book, String username, Consumer<BookRating> onRatingSaved) {
        this(book, username, null, onRatingSaved);
    }

    /**
     * Costruttore per <b>modificare</b> una valutazione esistente.
     * <p>
     * Questo costruttore inizializza il dialog con i dati di una valutazione pre-esistente.
     * I campi verranno popolati con i valori della valutazione passata come parametro.
     * </p>
     * @param book Il libro a cui si riferisce la valutazione.
     * @param username L'username dell'utente che sta modificando la valutazione.
     * @param existingRating L'oggetto {@link BookRating} esistente da modificare. Può essere {@code null} per una nuova valutazione.
     * @param onRatingSaved Il consumer da chiamare quando la valutazione viene salvata (modifica) o eliminata.
     */
    public RatingDialog(Book book, String username, BookRating existingRating, Consumer<BookRating> onRatingSaved) {
        this.book = book;
        this.username = username;
        this.currentRating = existingRating;
        this.onRatingSaved = onRatingSaved;
        this.ratingService = new ClientRatingService();

        initializeDialog();
        createUI();
        setupEventHandlers();

        if (existingRating != null) {
            populateFields(existingRating);
        }
    }

    /**
     * Inizializza le proprietà di base del dialog, come il suo stile e la modalità.
     * Questo metodo configura lo stage del dialog come una finestra modale, trasparente e non ridimensionabile.
     */
    private void initializeDialog() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle(currentRating != null ? "Modifica Valutazione" : "Nuova Valutazione");
        dialogStage.setResizable(false);
    }

    /**
     * Crea l'interfaccia utente del dialog.
     * <p>
     * Questo metodo costruisce la scena principale, il layout root e il contenuto del dialog.
     * Imposta lo sfondo trasparente e aggiunge la gestione degli eventi per chiudere il dialog
     * con un clic esterno.
     * </p>
     */
    private void createUI() {
        // Container principale SENZA sfondo scuro
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        // Dialog content
        VBox dialogContent = createDialogContent();
        root.getChildren().add(dialogContent);

        root.setPickOnBounds(false);

        // Chiudi dialog cliccando fuori - GESTIONE MIGLIORATA
        root.setOnMouseClicked(e -> {
            // Chiudi solo se il click è DAVVERO sullo sfondo
            if (e.getTarget() == root && e.getSource() == root) {
                closeDialog();
            }
        });

        // Dimensioni maggiori per contenere tutto
        Scene scene = new Scene(root, 900, 800);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
    }


    /**
     * Crea il contenuto principale del dialog, inclusi l'header, le sezioni di valutazione,
     * la recensione, l'anteprima e i bottoni di azione.
     *
     * @return Un {@link VBox} che contiene tutti i componenti UI del dialog.
     */
    private VBox createDialogContent() {
        VBox content = new VBox(20);
        content.setMaxWidth(750);
        content.setMaxHeight(750);
        content.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 10);"
        );
        content.setAlignment(Pos.TOP_CENTER);

        // Header con info libro
        VBox header = createHeader();

        // Sezione valutazioni
        VBox ratingsSection = createRatingsSection();

        // Sezione recensione
        VBox reviewSection = createReviewSection();

        // Anteprima valutazione
        VBox previewSection = createPreviewSection();

        // Bottoni
        HBox buttons = createButtonSection();

        // Aggiungi tutto in un ScrollPane se necessario
        VBox innerContent = new VBox(15);
        innerContent.getChildren().addAll(header, ratingsSection, reviewSection, previewSection, buttons);

        ScrollPane scrollPane = new ScrollPane(innerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        content.getChildren().add(scrollPane);
        return content;
    }


    /**
     * Crea l'header del dialog, che include il titolo e le informazioni sul libro.
     * <p>
     * L'header visualizza la copertina del libro, il titolo, l'autore e l'ISBN.
     * </p>
     * @return Un {@link VBox} che rappresenta l'header.
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        // Titolo dialog
        Label titleLabel = new Label(currentRating != null ? "Modifica la tua valutazione" : "Valuta questo libro");
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // Info libro
        HBox bookInfo = new HBox(15);
        bookInfo.setAlignment(Pos.CENTER);

        // Copertina libro
        ImageView coverImage = ImageUtils.createSafeImageView(book.getImageUrl(), 60, 90);
        Rectangle coverClip = new Rectangle(60, 90);
        coverClip.setArcWidth(5);
        coverClip.setArcHeight(5);
        coverImage.setClip(coverClip);

        // Dettagli libro
        VBox bookDetails = new VBox(5);
        bookDetails.setAlignment(Pos.CENTER_LEFT);

        Label bookTitle = new Label(book.getTitle());
        bookTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        bookTitle.setTextFill(Color.WHITE);
        bookTitle.setWrapText(true);
        bookTitle.setMaxWidth(400);

        Label bookAuthor = new Label("di " + book.getAuthor());
        bookAuthor.setFont(Font.font("SF Pro Text", 14));
        bookAuthor.setTextFill(Color.LIGHTGRAY);

        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            Label isbnLabel = new Label("ISBN: " + book.getIsbn());
            isbnLabel.setFont(Font.font("SF Pro Text", 12));
            isbnLabel.setTextFill(Color.GRAY);
            bookDetails.getChildren().add(isbnLabel);
        }

        bookDetails.getChildren().addAll(bookTitle, bookAuthor);
        bookInfo.getChildren().addAll(coverImage, bookDetails);

        header.getChildren().addAll(titleLabel, bookInfo);
        return header;
    }

    /**
     * Crea la sezione del dialog dedicata alle valutazioni tramite slider.
     * <p>
     * Questa sezione contiene un titolo, un'istruzione e cinque slider per le diverse
     * categorie di valutazione (Stile, Contenuto, Piacevolezza, Originalità, Edizione).
     * </p>
     * @return Un {@link VBox} che contiene la sezione delle valutazioni.
     */
    private VBox createRatingsSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("🌟 La tua valutazione");
        sectionTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.WHITE);

        Label instructionLabel = new Label("Valuta ogni aspetto del libro da 1 a 5 stelle:");
        instructionLabel.setFont(Font.font("SF Pro Text", 14));
        instructionLabel.setTextFill(Color.LIGHTGRAY);

        VBox ratingsContainer = new VBox(12);

        // Crea slider per ogni categoria
        styleSlider = createRatingSlider("Stile di scrittura", "La qualità della prosa e dello stile narrativo");
        styleValueLabel = new Label("0");
        HBox styleBox = createSliderRow("📝", "Stile", styleSlider, styleValueLabel);

        contentSlider = createRatingSlider("Contenuto", "La qualità della trama e dei contenuti");
        contentValueLabel = new Label("0");
        HBox contentBox = createSliderRow("📖", "Contenuto", contentSlider, contentValueLabel);

        pleasantnessSlider = createRatingSlider("Piacevolezza", "Quanto è stata piacevole la lettura");
        pleasantnessValueLabel = new Label("0");
        HBox pleasantnessBox = createSliderRow("😊", "Piacevolezza", pleasantnessSlider, pleasantnessValueLabel);

        originalitySlider = createRatingSlider("Originalità", "L'originalità e innovatività dell'opera");
        originalityValueLabel = new Label("0");
        HBox originalityBox = createSliderRow("💡", "Originalità", originalitySlider, originalityValueLabel);

        editionSlider = createRatingSlider("Edizione", "La qualità dell'edizione e della presentazione");
        editionValueLabel = new Label("0");
        HBox editionBox = createSliderRow("📚", "Edizione", editionSlider, editionValueLabel);

        ratingsContainer.getChildren().addAll(
                styleBox, contentBox, pleasantnessBox, originalityBox, editionBox
        );

        section.getChildren().addAll(sectionTitle, instructionLabel, ratingsContainer);
        return section;
    }

    /**
     * Crea una singola riga di interfaccia per un slider di valutazione.
     * <p>
     * Questa riga include un'icona, un'etichetta testuale, il {@link Slider}, un'etichetta per il valore
     * numerico e una visualizzazione a stelle.
     * </p>
     * @param icon L'icona da visualizzare.
     * @param label L'etichetta testuale per la categoria.
     * @param slider Lo {@link Slider} per la valutazione.
     * @param valueLabel L'etichetta che mostra il valore numerico dello slider.
     * @return Un {@link HBox} che contiene tutti i componenti della riga.
     */
    private HBox createSliderRow(String icon, String label, Slider slider, Label valueLabel) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        // Icona e label
        HBox labelBox = new HBox(8);
        labelBox.setAlignment(Pos.CENTER_LEFT);
        labelBox.setPrefWidth(120);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(16));

        Label textLabel = new Label(label);
        textLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 14));
        textLabel.setTextFill(Color.WHITE);

        labelBox.getChildren().addAll(iconLabel, textLabel);

        // Slider
        slider.setPrefWidth(300);

        // Valore
        valueLabel.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 14));
        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setPrefWidth(30);

        // Stelle visuali
        Label starsLabel = new Label();
        starsLabel.setFont(Font.font(14));
        starsLabel.setPrefWidth(100);

        // Aggiorna stelle quando slider cambia
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            valueLabel.setText(String.valueOf(value));

            if (value > 0) {
                String stars = "★".repeat(value) + "★".repeat(5 - value);
                starsLabel.setText(stars);
                starsLabel.getStyleClass().add("stars-white");
            } else {
                starsLabel.setText("★★★★★");
                starsLabel.getStyleClass().add("stars-white");
            }

            updatePreview();
        });

        row.getChildren().addAll(labelBox, slider, valueLabel, starsLabel);
        return row;
    }

    /**
     * Crea e configura un {@link Slider} per la valutazione.
     * <p>
     * Imposta il range del slider da 0 a 5, con tick marks per ogni valore intero.
     * Aggiunge un tooltip descrittivo per informare l'utente.
     * </p>
     * @param category La categoria della valutazione (es. "Stile di scrittura").
     * @param tooltip Il testo del tooltip descrittivo.
     * @return Lo {@link Slider} configurato.
     */
    private Slider createRatingSlider(String category, String tooltip) {
        Slider slider = new Slider(0, 5, 0);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(false);

        slider.setStyle(
                "-fx-control-inner-background: #404040;" +
                        "-fx-track-color: #606060;" +
                        "-fx-selection-bar: linear-gradient(to right, #ff6b35, #f7931e, #ffd700);"
        );

        Tooltip.install(slider, new Tooltip(tooltip));

        return slider;
    }

    /**
     * Crea la sezione per l'inserimento della recensione.
     * <p>
     * Include un'etichetta per il titolo, istruzioni e un {@link TextArea} per la recensione.
     * Aggiunge un contatore di caratteri dinamico per monitorare la lunghezza del testo.
     * </p>
     * @return Un {@link VBox} che contiene la sezione della recensione.
     */
    private VBox createReviewSection() {
        VBox section = new VBox(10);

        Label sectionTitle = new Label("✍️ Recensione (opzionale)");
        sectionTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.WHITE);

        Label instructionLabel = new Label("Condividi i tuoi pensieri su questo libro:");
        instructionLabel.setFont(Font.font("SF Pro Text", 14));
        instructionLabel.setTextFill(Color.LIGHTGRAY);

        reviewTextArea = new TextArea();
        reviewTextArea.setPromptText("Scrivi qui la tua recensione... (massimo 1000 caratteri)");
        reviewTextArea.setPrefRowCount(4);
        reviewTextArea.setPrefHeight(120);
        reviewTextArea.setMaxHeight(120);
        reviewTextArea.setWrapText(true);
        reviewTextArea.setStyle(
                "-fx-control-inner-background: #404040;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #888888;" +
                        "-fx-background-color: #404040;" +
                        "-fx-border-color: #606060;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

        // Contatore caratteri
        Label characterCount = new Label("0/1000");
        characterCount.setFont(Font.font("SF Pro Text", 12));
        characterCount.setTextFill(Color.GRAY);

        reviewTextArea.textProperty().addListener((obs, oldText, newText) -> {
            int length = newText.length();
            characterCount.setText(length + "/1000");

            if (length > 1000) {
                characterCount.setTextFill(Color.RED);
                reviewTextArea.setText(newText.substring(0, 1000));
            } else if (length > 900) {
                characterCount.setTextFill(Color.ORANGE);
            } else {
                characterCount.setTextFill(Color.GRAY);
            }
        });

        HBox countContainer = new HBox();
        countContainer.setAlignment(Pos.CENTER_RIGHT);
        countContainer.getChildren().add(characterCount);

        section.getChildren().addAll(sectionTitle, instructionLabel, reviewTextArea, countContainer);
        return section;
    }

    /**
     * Crea la sezione di anteprima che mostra il punteggio medio e la descrizione della qualità.
     * <p>
     * Questa sezione viene aggiornata in tempo reale man mano che l'utente modifica gli slider.
     * </p>
     * @return Un {@link VBox} che contiene la sezione di anteprima.
     */
    private VBox createPreviewSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        section.setStyle(
                "-fx-background-color: #383838;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        Label previewTitle = new Label("📊 Anteprima della tua valutazione");
        previewTitle.setFont(Font.font("SF Pro Text", FontWeight.BOLD, 16));
        previewTitle.setTextFill(Color.WHITE);

        averageLabel = new Label("⭐ 0.0/5");
        averageLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        averageLabel.getStyleClass().add("stars-white");

        qualityLabel = new Label("Non valutato");
        qualityLabel.setFont(Font.font("SF Pro Text", 14));
        qualityLabel.setTextFill(Color.LIGHTGRAY);

        section.getChildren().addAll(previewTitle, averageLabel, qualityLabel);
        return section;
    }

    /**
     * Crea la sezione dei bottoni di azione (Salva, Annulla, Elimina).
     * <p>
     * Il pulsante "Elimina" viene visualizzato solo se il dialog è in modalità di modifica.
     * Il pulsante "Salva" è inizialmente disabilitato e si abilita solo quando viene
     * assegnato almeno un voto.
     * </p>
     * @return Un {@link HBox} che contiene i bottoni di azione.
     */
    private HBox createButtonSection() {
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        cancelButton = new Button("Annulla");
        cancelButton.setPrefWidth(120);
        cancelButton.setStyle(
                "-fx-background-color: #606060;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        saveButton = new Button(currentRating != null ? "Aggiorna" : "Salva");
        saveButton.setPrefWidth(120);
        saveButton.setStyle(
                "-fx-background-color: #4CAF50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        saveButton.setDisable(true);

        buttons.getChildren().addAll(cancelButton, saveButton);

        if (currentRating != null) {
            deleteButton = new Button("Elimina");
            deleteButton.setPrefWidth(120);
            deleteButton.setStyle(
                    "-fx-background-color: #f44336;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 10 20;" +
                            "-fx-cursor: hand;"
            );
            buttons.getChildren().add(0, deleteButton);
        }

        return buttons;
    }

    /**
     * Configura i gestori di eventi per i componenti UI.
     * <p>
     * Questo metodo imposta i listener per gli slider, i gestori per i pulsanti
     * (Annulla, Salva, Elimina) e un gestore per la chiusura del dialog tramite
     * il tasto ESC.
     * </p>
     */
    private void setupEventHandlers() {
        styleSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        contentSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        pleasantnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        originalitySlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        editionSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());

        cancelButton.setOnAction(e -> closeDialog());
        saveButton.setOnAction(e -> saveRating());

        if (deleteButton != null) {
            deleteButton.setOnAction(e -> deleteRating());
        }

        dialogStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ESCAPE")) {
                closeDialog();
            }
        });
    }

    /**
     * Aggiorna la sezione di anteprima in base ai valori attuali degli slider.
     * <p>
     * Questo metodo ricalcola il punteggio medio, aggiorna la label del punteggio,
     * e determina la descrizione qualitativa del voto. Abilita o disabilita il
     * pulsante "Salva" in base alla presenza di almeno un voto.
     * </p>
     */
    private void updatePreview() {
        int style = (int) styleSlider.getValue();
        int content = (int) contentSlider.getValue();
        int pleasantness = (int) pleasantnessSlider.getValue();
        int originality = (int) originalitySlider.getValue();
        int edition = (int) editionSlider.getValue();

        boolean hasRating = style > 0 || content > 0 || pleasantness > 0 || originality > 0 || edition > 0;
        saveButton.setDisable(!hasRating);

        if (hasRating) {
            int count = 0;
            double sum = 0;

            if (style > 0) { sum += style; count++; }
            if (content > 0) { sum += content; count++; }
            if (pleasantness > 0) { sum += pleasantness; count++; }
            if (originality > 0) { sum += originality; count++; }
            if (edition > 0) { sum += edition; count++; }

            if (count > 0) {
                double average = sum / count;
                averageLabel.setText(String.format("⭐ %.1f/5", average));

                String quality = getQualityDescription(average);
                qualityLabel.setText(quality);

                if (average >= 4.5) qualityLabel.setTextFill(Color.LIME);
                else if (average >= 4.0) qualityLabel.setTextFill(Color.LIGHTGREEN);
                else if (average >= 3.5) qualityLabel.setTextFill(Color.YELLOW);
                else if (average >= 3.0) qualityLabel.setTextFill(Color.ORANGE);
                else qualityLabel.setTextFill(Color.LIGHTCORAL);
            }
        } else {
            averageLabel.setText("⭐ 0.0/5");
            qualityLabel.setText("Non valutato");
            qualityLabel.setTextFill(Color.LIGHTGRAY);
        }
    }

    /**
     * Restituisce una stringa descrittiva della qualità in base al punteggio medio.
     * @param average Il punteggio medio calcolato.
     * @return Una stringa descrittiva (es. "Eccellente", "Discreto").
     */
    private String getQualityDescription(double average) {
        if (average >= 4.5) return "Eccellente";
        else if (average >= 4.0) return "Molto buono";
        else if (average >= 3.5) return "Buono";
        else if (average >= 3.0) return "Discreto";
        else if (average >= 2.5) return "Sufficiente";
        else if (average >= 2.0) return "Mediocre";
        else return "Scarso";
    }

    /**
     * Popola i campi del dialog con i dati di una valutazione esistente.
     * <p>
     * Viene chiamato solo in modalità di modifica per impostare i valori iniziali
     * degli slider e il testo dell'area di recensione.
     * </p>
     * @param rating La valutazione esistente da cui prendere i dati.
     */
    private void populateFields(BookRating rating) {
        if (rating.getStyle() != null) styleSlider.setValue(rating.getStyle());
        if (rating.getContent() != null) contentSlider.setValue(rating.getContent());
        if (rating.getPleasantness() != null) pleasantnessSlider.setValue(rating.getPleasantness());
        if (rating.getOriginality() != null) originalitySlider.setValue(rating.getOriginality());
        if (rating.getEdition() != null) editionSlider.setValue(rating.getEdition());

        if (rating.getReview() != null) {
            reviewTextArea.setText(rating.getReview());
        }

        updatePreview();
    }

    /**
     * Salva o aggiorna la valutazione.
     * <p>
     * Questo metodo valida la presenza di almeno un voto, disabilita il pulsante di salvataggio
     * e invia una richiesta asincrona al {@link ClientRatingService}. Gestisce la risposta
     * del servizio e notifica il chiamante tramite il consumer {@link #onRatingSaved}.
     * </p>
     */
    private void saveRating() {
        if (!isRatingValid()) {
            showAlert("Errore", "Devi dare almeno una valutazione per salvare.");
            return;
        }

        saveButton.setDisable(true);
        saveButton.setText("Salvando...");

        RatingRequest request = new RatingRequest(
                username,
                book.getIsbn(),
                (int) styleSlider.getValue(),
                (int) contentSlider.getValue(),
                (int) pleasantnessSlider.getValue(),
                (int) originalitySlider.getValue(),
                (int) editionSlider.getValue(),
                reviewTextArea.getText().trim().isEmpty() ? null : reviewTextArea.getText().trim()
        );

        ratingService.addOrUpdateRatingAsync(request)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            if (onRatingSaved != null) {
                                onRatingSaved.accept(response.getRating());
                            }
                            closeDialog();
                            showAlert("Successo", "Valutazione salvata con successo!");
                        } else {
                            showAlert("Errore", "Errore nel salvare la valutazione: " + response.getMessage());
                            saveButton.setDisable(false);
                            saveButton.setText(currentRating != null ? "Aggiorna" : "Salva");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                        saveButton.setDisable(false);
                        saveButton.setText(currentRating != null ? "Aggiorna" : "Salva");
                    });
                    return null;
                });
    }

    /**
     * Elimina una valutazione esistente.
     * <p>
     * Questo metodo mostra un alert di conferma prima di procedere. Se l'utente conferma,
     * invia una richiesta asincrona al {@link ClientRatingService} per eliminare la valutazione.
     * Al successo, notifica il chiamante con un valore {@code null}.
     * </p>
     */
    private void deleteRating() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminare la valutazione?");
        confirmAlert.setContentText("Questa azione non può essere annullata.");

        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                deleteButton.setDisable(true);
                deleteButton.setText("Eliminando...");

                ratingService.deleteRatingAsync(username, book.getIsbn())
                        .thenAccept(response -> {
                            Platform.runLater(() -> {
                                if (response.isSuccess()) {
                                    if (onRatingSaved != null) {
                                        onRatingSaved.accept(null);
                                    }
                                    closeDialog();
                                    showAlert("Successo", "Valutazione eliminata con successo!");
                                } else {
                                    showAlert("Errore", "Errore nell'eliminare la valutazione: " + response.getMessage());
                                    deleteButton.setDisable(false);
                                    deleteButton.setText("Elimina");
                                }
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                                deleteButton.setDisable(false);
                                deleteButton.setText("Elimina");
                            });
                            return null;
                        });
            }
        });
    }

    /**
     * Controlla se almeno uno dei slider ha un valore maggiore di zero.
     * @return {@code true} se almeno un voto è stato dato, altrimenti {@code false}.
     */
    private boolean isRatingValid() {
        return styleSlider.getValue() > 0 ||
                contentSlider.getValue() > 0 ||
                pleasantnessSlider.getValue() > 0 ||
                originalitySlider.getValue() > 0 ||
                editionSlider.getValue() > 0;
    }

    /**
     * Chiude il dialog se non è null.
     */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Mostra un semplice alert informativo all'utente.
     * <p>
     * Questo metodo è una utility per visualizzare messaggi di successo o di errore.
     * </p>
     * @param title Il titolo dell'alert.
     * @param message Il messaggio da mostrare.
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
     * <p>
     * Il dialog viene visualizzato e posizionato al centro dello schermo.
     * </p>
     */
    public void show() {
        if (dialogStage != null) {
            dialogStage.show();
            dialogStage.centerOnScreen();
        }
    }

    /**
     * Mostra il dialog e blocca il thread chiamante fino alla sua chiusura.
     * <p>
     * Questo è utile per i casi in cui si vuole aspettare che l'utente interagisca
     * con il dialog prima di proseguire con l'esecuzione del codice.
     * </p>
     */
    public void showAndWait() {
        if (dialogStage != null) {
            dialogStage.showAndWait();
        }
    }

    /**
     * Metodo statico per creare e mostrare rapidamente un dialog di <b>nuova</b> valutazione.
     * <p>
     * Questo è un metodo di utilità che semplifica l'avvio del dialog per la creazione di
     * una nuova valutazione.
     * </p>
     * @param book Il libro da valutare.
     * @param username L'username dell'utente.
     * @param onRatingSaved Il consumer da chiamare al salvataggio.
     */
    public static void showRatingDialog(Book book, String username, Consumer<BookRating> onRatingSaved) {
        RatingDialog dialog = new RatingDialog(book, username, onRatingSaved);
        dialog.show();
    }

    /**
     * Metodo statico per creare e mostrare rapidamente un dialog di <b>modifica</b> valutazione.
     * <p>
     * Questo metodo di utilità semplifica l'avvio del dialog con i dati di una valutazione
     * esistente.
     * </p>
     * @param book Il libro a cui si riferisce la valutazione.
     * @param username L'username dell'utente.
     * @param existingRating La valutazione esistente da modificare.
     * @param onRatingSaved Il consumer da chiamare al salvataggio o all'eliminazione.
     */
    public static void showEditRatingDialog(Book book, String username, BookRating existingRating, Consumer<BookRating> onRatingSaved) {
        RatingDialog dialog = new RatingDialog(book, username, existingRating, onRatingSaved);
        dialog.show();
    }
}