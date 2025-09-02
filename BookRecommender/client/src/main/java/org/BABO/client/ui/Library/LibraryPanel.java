package org.BABO.client.ui.Library;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import org.BABO.client.ui.BooksClient;
import org.BABO.client.ui.Home.ImageUtils;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.BABO.client.service.AuthService;
import org.BABO.client.service.LibraryService;
import org.BABO.shared.model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Panel delle librerie ridisegnato per integrarsi meglio
 * con il design generale dell'applicazione Books.
 * <p>
 * Questo pannello fornisce un'interfaccia utente completa per gestire le librerie
 * personali dell'utente. Permette di creare nuove librerie, visualizzare l'elenco
 * di quelle esistenti, navigare al loro interno per vedere i libri e gestire la
 * loro eliminazione.
 * </p>
 *
 * <h3>Caratteristiche principali:</h3>
 * <ul>
 * <li><strong>Design Moderno:</strong> L'interfaccia è stata completamente ridisegnata
 * per avere uno stile minimale e moderno.</li>
 * <li><strong>Gestione Librerie:</strong> Supporta la creazione e l'eliminazione delle
 * librerie tramite un'interfaccia utente intuitiva.</li>
 * <li><strong>Visualizzazione Libri:</strong> Quando si seleziona una libreria, il
 * pannello passa alla visualizzazione a griglia dei libri al suo interno.</li>
 * <li><strong>Integrazione Asincrona:</strong> Tutte le operazioni di rete (creazione,
 * caricamento, eliminazione) sono gestite in modo asincrono utilizzando {@link CompletableFuture}
 * per garantire una UI reattiva e non bloccante.</li>
 * <li><strong>Stato Vuoto e Messaggi:</strong> Fornisce messaggi di feedback visivi,
 * come indicatori di caricamento, messaggi di successo o di errore e una
 * "empty state" quando non ci sono librerie o libri.</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see org.BABO.client.service.LibraryService
 * @see org.BABO.client.ui.Home.ImageUtils
 */
public class LibraryPanel extends VBox {

    private final LibraryService libraryService;
    private final String username;
    private Consumer<Book> onBookClick;
    private Runnable onClosePanel;

    // UI Components
    private VBox librariesContainer;
    private TextField newLibraryField;
    private Button createLibraryButton;
    private Label titleLabel;
    private ScrollPane scrollPane;

    // Cache per libri della libreria corrente
    private List<Book> currentLibraryBooks;

    /**
     * Costruisce un nuovo {@link LibraryPanel} con un {@link AuthService} fornito.
     * <p>
     * Questo costruttore è utile per l'iniezione del gestore di autenticazione.
     * Inizializza il pannello, configura l'UI e carica le librerie dell'utente.
     * </p>
     * @param username L'username dell'utente.
     * @param authManager Il gestore di autenticazione dell'applicazione.
     */
    public LibraryPanel(String username, AuthService authManager) {
        this.libraryService = new LibraryService();
        this.username = username;
        setupImprovedUI();
        loadUserLibraries();
    }

    /**
     * Configura l'interfaccia utente migliorata per questo pannello.
     * <p>
     * Questo metodo imposta le proprietà di layout e il background del pannello
     * per creare un'interfaccia utente moderna e visivamente accattivante.
     * Le impostazioni includono l'allineamento, lo spazio tra gli elementi e il padding.
     * Il metodo applica un gradiente lineare come sfondo per ottenere un effetto
     * di sfumatura scura e rimuove gli effetti di stile predefiniti
     * per garantire una visualizzazione pulita.
     * </p>
     */
    private void setupImprovedUI() {
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(0);
        this.setPadding(new Insets(0));

        // Background più integrato - sfumatura graduale
        LinearGradient background = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a1a1a")),
                new Stop(0.3, Color.web("#1e1e1e")),
                new Stop(1, Color.web("#242426"))
        );

        this.setBackground(new Background(new BackgroundFill(background,
                new CornerRadii(0), Insets.EMPTY)));

        this.setStyle("-fx-effect: null;");

        setupLayout();
    }

    /**
     * Configura il layout principale del pannello, aggiungendo tutti i componenti
     * UI principali.
     * <p>
     * Questo metodo crea e aggiunge i vari elementi visivi che compongono l'interfaccia
     * utente del pannello della libreria, inclusi:
     * <ul>
     * <li>L'header moderno ({@link #createModernHeader()}) per il titolo e la navigazione.</li>
     * <li>La sezione per la creazione di una nuova libreria ({@link #createNewLibrarySection()}),
     * che è inizialmente nascosta quando si visualizzano i libri.</li>
     * <li>Un separatore elegante ({@link #createElegantSeparator()}) per suddividere
     * visivamente le sezioni del pannello.</li>
     * <li>Il contenitore principale per le librerie, che gestisce lo scorrimento
     * e la visualizzazione dei contenuti.</li>
     * </ul>
     * La disposizione dei componenti segue una logica top-down per strutturare
     * l'interfaccia in modo chiaro e intuitivo.
     * </p>
     */
    private void setupLayout() {
        // Header moderno integrato
        VBox header = createModernHeader();
        this.getChildren().add(header);

        // Sezione creazione nuova libreria (sarà nascosta quando si visualizzano i libri)
        VBox createSection = createNewLibrarySection();
        this.getChildren().add(createSection);

        // Separatore elegante (sarà nascosto quando si visualizzano i libri)
        Region separator = createElegantSeparator();
        this.getChildren().add(separator);

        // Container per le librerie con scroll moderno
        setupLibrariesContainer();
    }

    /**
     * Crea e configura l'header moderno per il pannello delle librerie.
     * <p>
     * Questo metodo costruisce un {@link VBox} che serve come intestazione visiva
     * del pannello. L'header è caratterizzato da:
     * <ul>
     * <li>Uno sfondo con gradiente lineare per un design elegante.</li>
     * <li>Un titolo principale ("Le Tue Librerie") e un sottotitolo descrittivo.</li>
     * <li>Un pulsante di chiusura personalizzato e stilizzato, posizionato a destra
     * del titolo.</li>
     * </ul>
     * La disposizione dei componenti all'interno dell'header è gestita tramite
     * un {@link HBox} per garantire un allineamento corretto del titolo e del
     * pulsante di chiusura.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta l'header completo.
     */
    private VBox createModernHeader() {
        VBox header = new VBox(0);
        header.setPadding(new Insets(40, 60, 30, 60));

        LinearGradient headerBg = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1e1e1e")),
                new Stop(1, Color.web("#2a2a2c"))
        );
        header.setBackground(new Background(new BackgroundFill(headerBg,
                CornerRadii.EMPTY, Insets.EMPTY)));

        titleLabel = new Label("📚 Le Tue Librerie");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Organizza e gestisci la tua collezione personale");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#8E8E93"));
        subtitleLabel.setPadding(new Insets(5, 0, 0, 0));

        Button closeButton = createModernCloseButton();

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleLabel, spacer, closeButton);

        header.getChildren().addAll(titleRow, subtitleLabel);
        return header;
    }

    /**
     * Crea e configura un pulsante di chiusura moderno e stilizzato.
     * <p>
     * Questo metodo genera un {@link Button} personalizzato per chiudere il pannello.
     * Il pulsante ha una forma circolare con un testo "x" centrato e uno stile
     * trasparente con un bordo sottile. Vengono applicati effetti visivi come
     * il cambio di colore e la scalatura al passaggio del mouse per migliorare
     * l'esperienza utente. Un listener di azione è collegato al pulsante per
     * eseguire l'azione di chiusura del pannello quando viene cliccato.
     * </p>
     *
     * @return Un {@link Button} configurato come pulsante di chiusura.
     */
    private Button createModernCloseButton() {
        Button closeButton = new Button("x");
        closeButton.setPrefSize(40, 40);

        closeButton.setAlignment(Pos.CENTER);
        closeButton.setTextAlignment(TextAlignment.CENTER);

        closeButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: rgba(255,255,255,0.2);" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 20;" +
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle(
                    "-fx-background-color: #FF3B30;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #FF3B30;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 20;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
            closeButton.setScaleX(1.1);
            closeButton.setScaleY(1.1);
        });

        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.1);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: rgba(255,255,255,0.2);" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 20;" +
                            "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
            closeButton.setScaleX(1.0);
            closeButton.setScaleY(1.0);
        });

        closeButton.setOnAction(e -> {
            if (onClosePanel != null) {
                onClosePanel.run();
            }
        });

        return closeButton;
    }

    /**
     * Crea la sezione dell'interfaccia utente per la creazione di una nuova libreria.
     * <p>
     * Questo metodo costruisce un {@link VBox} che contiene un form per permettere
     * all'utente di creare una nuova libreria. Il design della sezione è moderno
     * e minimalista, con uno sfondo scuro semitrasparente. La sezione include un
     * campo di testo ({@link TextField}) con effetti visivi per lo stato di focus
     * e un pulsante "Crea Libreria" stilizzato.
     * Il layout utilizza un {@link HBox} per disporre il campo di testo e il
     * pulsante sulla stessa riga, rendendo il form compatto e intuitivo.
     * Vengono anche configurati i gestori di eventi per il pulsante di creazione.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta la sezione completa per la creazione di una libreria.
     */
    private VBox createNewLibrarySection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 60, 20, 60));

        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );

        Label sectionTitle = new Label("Crea Nuova Libreria");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.WHITE);

        newLibraryField = new TextField();
        newLibraryField.setPromptText("Nome della libreria...");
        newLibraryField.setPrefHeight(44);
        newLibraryField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: rgba(255,255,255,0.2);" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #8E8E93;" +
                        "-fx-font-size: 14;" +
                        "-fx-padding: 12;"
        );

        newLibraryField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                newLibraryField.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.12);" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-color: #007AFF;" +
                                "-fx-border-radius: 8;" +
                                "-fx-border-width: 2;" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #8E8E93;" +
                                "-fx-font-size: 14;" +
                                "-fx-padding: 11;"
                );
            } else {
                newLibraryField.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.08);" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-color: rgba(255,255,255,0.2);" +
                                "-fx-border-radius: 8;" +
                                "-fx-border-width: 1;" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #8E8E93;" +
                                "-fx-font-size: 14;" +
                                "-fx-padding: 12;"
                );
            }
        });

        createLibraryButton = new Button("Crea Libreria");
        createLibraryButton.setPrefHeight(44);
        createLibraryButton.setPrefWidth(140);
        createLibraryButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #007AFF, #0051D5);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-cursor: hand;"
        );

        setupButtonHoverEffects(createLibraryButton);

        HBox inputLayout = new HBox(15);
        inputLayout.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(newLibraryField, Priority.ALWAYS);
        inputLayout.getChildren().addAll(newLibraryField, createLibraryButton);

        card.getChildren().addAll(sectionTitle, inputLayout);
        section.getChildren().add(card);

        setupCreateLibraryHandlers();

        return section;
    }

    /**
     * Configura gli effetti visivi di hover per un pulsante.
     * <p>
     * Questo metodo imposta due listener per gli eventi del mouse:
     * <ul>
     * <li>{@code onMouseEntered}: quando il cursore entra nell'area del pulsante,
     * lo stile viene modificato per un feedback visivo, e il pulsante viene leggermente
     * ingrandito per un effetto di hover dinamico.</li>
     * <li>{@code onMouseExited}: quando il cursore esce dall'area del pulsante,
     * lo stile e la dimensione tornano al loro stato originale.</li>
     * </ul>
     * Questa tecnica migliora l'esperienza utente fornendo un'indicazione chiara
     * dell'interattività del pulsante.
     * </p>
     *
     * @param button Il pulsante su cui applicare gli effetti di hover.
     */
    private void setupButtonHoverEffects(Button button) {
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #1E86FF, #0D5FE0);" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: transparent;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14;" +
                            "-fx-cursor: hand;"
            );
            button.setScaleX(1.02);
            button.setScaleY(1.02);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #007AFF, #0051D5);" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: transparent;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14;" +
                            "-fx-cursor: hand;"
            );
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
    }

    /**
     * Crea un separatore elegante con una sfumatura per migliorare la divisione delle sezioni.
     * @return Un {@link Region} che funge da separatore.
     */
    private Region createElegantSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setMaxHeight(1);

        LinearGradient separatorGradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.5, Color.web("#333335")),
                new Stop(1, Color.TRANSPARENT)
        );

        separator.setBackground(new Background(new BackgroundFill(separatorGradient,
                CornerRadii.EMPTY, Insets.EMPTY)));

        VBox.setMargin(separator, new Insets(20, 60, 20, 60));
        return separator;
    }

    /**
     * Configura il contenitore principale per le librerie con funzionalità di scorrimento.
     * <p>
     * Questo metodo inizializza il {@link VBox} che conterrà le card delle librerie
     * e lo avvolge in un {@link ScrollPane}. Il contenitore è ottimizzato per
     * un layout moderno: viene impostato l'allineamento, il padding e viene
     * reso trasparente per integrarsi con lo sfondo del pannello. Il {@link ScrollPane}
     * è configurato per scorrere verticalmente quando necessario e per non mostrare
     * la barra di scorrimento orizzontale. Inoltre, la gestione del focus
     * è disattivata per mantenere l'interfaccia pulita.
     * </p>
     */
    private void setupLibrariesContainer() {
        librariesContainer = new VBox(20);
        librariesContainer.setAlignment(Pos.TOP_CENTER);
        librariesContainer.setPadding(new Insets(0, 60, 40, 60));

        scrollPane = new ScrollPane(librariesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        this.getChildren().add(scrollPane);
    }

    /**
     * Configura i gestori di eventi per il campo di testo e il pulsante di creazione di una libreria.
     * <p>
     * Questo metodo associa le azioni ai componenti UI in modo da automatizzare il flusso di lavoro
     * di creazione di una nuova libreria. In particolare:
     * <ul>
     * <li>Il pulsante {@link #createLibraryButton} e il campo di testo {@link #newLibraryField}
     * eseguono entrambi il metodo {@link #createNewLibrary()} quando attivati
     * (clic del pulsante o pressione di Invio nel campo di testo).</li>
     * <li>Un listener della proprietà del testo del campo {@link #newLibraryField}
     * viene utilizzato per abilitare o disabilitare il pulsante di creazione
     * in tempo reale. Il pulsante è disabilitato se il campo di testo è vuoto
     * o contiene solo spazi, e viene abilitato non appena l'utente digita
     * un testo valido.</li>
     * <li>Vengono gestiti anche gli stili del pulsante per fornire un feedback visivo
     * quando è abilitato o disabilitato.</li>
     * </ul>
     * Il pulsante viene inizialmente impostato come disabilitato per prevenire
     * creazioni indesiderate.
     * </p>
     */
    private void setupCreateLibraryHandlers() {
        createLibraryButton.setOnAction(e -> createNewLibrary());

        newLibraryField.setOnAction(e -> createNewLibrary());

        newLibraryField.textProperty().addListener((obs, oldText, newText) -> {
            boolean hasText = newText != null && !newText.trim().isEmpty();
            createLibraryButton.setDisable(!hasText);

            if (!hasText) {
                createLibraryButton.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.1);" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-color: transparent;" +
                                "-fx-text-fill: #8E8E93;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14;"
                );
            } else {
                createLibraryButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #007AFF, #0051D5);" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-color: transparent;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        createLibraryButton.setDisable(true);
    }

    /**
     * Gestisce la creazione di una nuova libreria, inviando una richiesta asincrona
     * al servizio di gestione librerie.
     * <p>
     * Se l'operazione ha successo, mostra un messaggio di successo e ricarica
     * l'elenco delle librerie. In caso di errore, mostra un messaggio di errore.
     * </p>
     */
    private void createNewLibrary() {
        String libraryName = newLibraryField.getText().trim();
        if (libraryName.isEmpty()) {
            return;
        }

        createLibraryButton.setDisable(true);
        createLibraryButton.setText("Creazione...");

        libraryService.createLibraryAsync(username, libraryName)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        newLibraryField.clear();
                        showSuccessMessage("Libreria '" + libraryName + "' creata con successo!");
                        loadUserLibraries();
                    } else {
                        showErrorMessage("Errore nella creazione: " + response.getMessage());
                    }

                    createLibraryButton.setDisable(false);
                    createLibraryButton.setText("Crea Libreria");
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showErrorMessage("Errore di connessione: " + throwable.getMessage());
                        createLibraryButton.setDisable(false);
                        createLibraryButton.setText("Crea Libreria");
                    });
                    return null;
                });
    }

    /**
     * Mostra un messaggio di successo temporaneo nell'interfaccia utente.
     * <p>
     * Questo metodo crea e visualizza una label personalizzata che contiene un messaggio
     * di successo. La label ha uno stile specifico con colori vivaci e un background
     * leggermente trasparente. Il messaggio viene aggiunto in cima al contenitore
     * delle librerie e viene automaticamente rimosso dopo 3 secondi per non ingombrare
     * l'interfaccia utente. L'operazione di rimozione avviene in un thread separato
     * per evitare di bloccare il thread dell'interfaccia grafica.
     * </p>
     *
     * @param message Il testo del messaggio di successo da visualizzare.
     */
    private void showSuccessMessage(String message) {
        Label successLabel = new Label("✅ " + message);
        successLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        successLabel.setTextFill(Color.web("#34C759"));
        successLabel.setStyle(
                "-fx-background-color: rgba(52,199,89,0.1);" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 16;"
        );

        VBox container = new VBox();
        container.getChildren().add(successLabel);
        container.setPadding(new Insets(10, 0, 0, 0));

        librariesContainer.getChildren().add(0, container);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> librariesContainer.getChildren().remove(container));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Mostra un messaggio di errore temporaneo nell'interfaccia utente.
     * <p>
     * Questo metodo crea e visualizza una label personalizzata per un messaggio
     * di errore. La label è stilizzata con colori e un background che indicano
     * un errore. Viene aggiunta in cima al contenitore delle librerie e viene
     * automaticamente rimossa dopo 5 secondi per evitare di ingombrare
     * l'interfaccia. L'operazione di rimozione avviene in un thread separato
     * per non bloccare l'interfaccia grafica.
     * </p>
     *
     * @param message Il testo del messaggio di errore da visualizzare.
     */
    private void showErrorMessage(String message) {
        Label errorLabel = new Label("❌ " + message);
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        errorLabel.setTextFill(Color.web("#FF3B30"));
        errorLabel.setStyle(
                "-fx-background-color: rgba(255,59,48,0.1);" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 16;"
        );

        VBox container = new VBox();
        container.getChildren().add(errorLabel);
        container.setPadding(new Insets(10, 0, 0, 0));

        librariesContainer.getChildren().add(0, container);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> librariesContainer.getChildren().remove(container));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }


    /**
     * Carica e visualizza l'elenco delle librerie dell'utente.
     * <p>
     * Questo metodo si occupa di:
     * <ul>
     * <li>Mostrare un indicatore di caricamento.</li>
     * <li>Inviare una richiesta asincrona per ottenere le librerie.</li>
     * <li>Visualizzare le librerie o uno stato vuoto in base alla risposta.</li>
     * </ul>
     * </p>
     */
    public void loadUserLibraries() {
        showCreateLibrarySection();

        librariesContainer.getChildren().clear();

        VBox loadingBox = createLoadingIndicator();
        librariesContainer.getChildren().add(loadingBox);

        libraryService.getUserLibrariesAsync(username)
                .thenAccept(response -> Platform.runLater(() -> {
                    librariesContainer.getChildren().remove(loadingBox);

                    if (response.isSuccess() && response.getLibraries() != null) {
                        List<String> libraries = response.getLibraries();
                        if (libraries.isEmpty()) {
                            showEmptyState();
                        } else {
                            displayLibraries(libraries);
                        }
                    } else {
                        showErrorMessage("Errore nel caricamento delle librerie: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        librariesContainer.getChildren().remove(loadingBox);
                        showErrorMessage("Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Crea e configura un indicatore visivo di caricamento.
     * <p>
     * Questo metodo costruisce un {@link VBox} che serve come indicatore di stato
     * per le operazioni asincrone. Include un {@link ProgressIndicator} stilizzato
     * con un colore primario e una label testuale che comunica all'utente che
     * un'operazione di caricamento è in corso. L'indicatore è centrato e ha un
     * padding per essere visualizzato in modo pulito all'interno del layout.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta il componente completo dell'indicatore di caricamento.
     */
    private VBox createLoadingIndicator() {
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(40, 40);
        spinner.setStyle(
                "-fx-progress-color: #007AFF;" +
                        "-fx-control-inner-background: transparent;"
        );

        Label loadingLabel = new Label("Caricamento librerie...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#8E8E93"));

        loadingBox.getChildren().addAll(spinner, loadingLabel);
        return loadingBox;
    }

    /**
     * Mostra uno stato vuoto nell'interfaccia utente quando non sono presenti librerie.
     * <p>
     * Questo metodo crea e visualizza un'interfaccia utente informativa che invita
     * l'utente a creare la sua prima libreria. L'interfaccia di stato vuoto è composta da:
     * <ul>
     * <li>Un'icona a forma di libro.</li>
     * <li>Un titolo principale "Nessuna libreria creata".</li>
     * <li>Un sottotitolo esplicativo che fornisce istruzioni su come procedere.</li>
     * </ul>
     * I componenti sono stilizzati per integrarsi con il tema scuro del pannello e
     * vengono aggiunti al contenitore principale delle librerie.
     * </p>
     */
    private void showEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));

        Label iconLabel = new Label("📚");
        iconLabel.setFont(Font.font(64));
        iconLabel.setOpacity(0.4);

        Label emptyLabel = new Label("Nessuna libreria creata");
        emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        emptyLabel.setTextFill(Color.web("#8E8E93"));

        Label hintLabel = new Label("Crea la tua prima libreria per iniziare a organizzare i tuoi libri");
        hintLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        hintLabel.setTextFill(Color.web("#636366"));
        hintLabel.setWrapText(true);
        hintLabel.setMaxWidth(300);
        hintLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        emptyBox.getChildren().addAll(iconLabel, emptyLabel, hintLabel);
        librariesContainer.getChildren().add(emptyBox);
    }

    /**
     * Crea e visualizza una lista di schede (card) per ogni libreria.
     * @param libraries La lista di nomi delle librerie da visualizzare.
     */
    private void displayLibraries(List<String> libraries) {
        for (String library : libraries) {
            HBox libraryCard = createLibraryCard(library);
            librariesContainer.getChildren().add(libraryCard);
        }
    }

    /**
     * Crea e configura una card (scheda) per una singola libreria.
     * <p>
     * Questo metodo genera un {@link HBox} stilizzato che rappresenta visivamente una libreria.
     * La card include un'icona, il nome della libreria, un sottotitolo, e pulsanti di azione
     * (per visualizzare ed eliminare). Il design è moderno, con angoli arrotondati,
     * bordi sottili e un effetto di cursore che cambia al passaggio del mouse.
     * Vengono anche impostati i gestori di eventi per i pulsanti e la card stessa,
     * collegando le azioni al nome specifico della libreria.
     * </p>
     *
     * @param libraryName Il nome della libreria per cui creare la card.
     * @return Un {@link HBox} che rappresenta la card completa della libreria.
     */
    private HBox createLibraryCard(String libraryName) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setPrefHeight(80);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-cursor: hand;"
        );

        Label iconLabel = new Label("📖");
        iconLabel.setFont(Font.font(24));

        VBox infoBox = new VBox(4);

        Label nameLabel = new Label(libraryName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Tocca per visualizzare");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        subtitleLabel.setTextFill(Color.web("#8E8E93"));

        infoBox.getChildren().addAll(nameLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = createActionButton("👁", "Visualizza");
        Button deleteButton = createActionButton("🗑", "Elimina");
        deleteButton.setStyle(deleteButton.getStyle().replace("#007AFF", "#FF3B30"));

        actionsBox.getChildren().addAll(viewButton, deleteButton);

        card.getChildren().addAll(iconLabel, infoBox, spacer, actionsBox);

        setupLibraryCardEvents(card, libraryName, viewButton, deleteButton);

        return card;
    }


    /**
     * Crea un pulsante di azione con icone e stile specifici.
     * <p>
     * Questo metodo supporta la creazione di pulsanti per "Visualizza" e "Elimina"
     * con effetti di hover personalizzati.
     * </p>
     * @param icon L'icona del pulsante.
     * @param tooltip Il testo del tooltip per il pulsante.
     * @return Un {@link Button} di azione.
     */
    private Button createActionButton(String icon, String tooltip) {
        Button button = new Button(icon);
        button.setPrefSize(40, 40);

        if (icon.equals("🗑")) {

            button.setText("🗑");
            button.setStyle(
                    "-fx-background-color: rgba(255,59,48,0.2);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: rgba(255,59,48,0.5);" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-text-fill: #FF3B30;" +
                            "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );

            button.setOnMouseEntered(e -> {
                button.setStyle(
                        "-fx-background-color: rgba(255,59,48,0.4);" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: #FF3B30;" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 2;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 18;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );
                button.setScaleX(1.15);
                button.setScaleY(1.15);
            });

            button.setOnMouseExited(e -> {
                button.setStyle(
                        "-fx-background-color: rgba(255,59,48,0.2);" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: rgba(255,59,48,0.5);" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-text-fill: #FF3B30;" +
                                "-fx-font-size: 18;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );
                button.setScaleX(1.0);
                button.setScaleY(1.0);
            });

        } else {
            button.setStyle(
                    "-fx-background-color: rgba(0,122,255,0.2);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: rgba(0,122,255,0.4);" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-text-fill: #007AFF;" +
                            "-fx-font-size: 15;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );

            button.setOnMouseEntered(e -> {
                button.setStyle(
                        "-fx-background-color: rgba(0,122,255,0.3);" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: #007AFF;" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 2;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 15;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );
                button.setScaleX(1.1);
                button.setScaleY(1.1);
            });

            button.setOnMouseExited(e -> {
                button.setStyle(
                        "-fx-background-color: rgba(0,122,255,0.2);" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: rgba(0,122,255,0.4);" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-text-fill: #007AFF;" +
                                "-fx-font-size: 15;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );
                button.setScaleX(1.0);
                button.setScaleY(1.0);
            });
        }

        return button;
    }

    /**
     * Configura i gestori di eventi per una card di libreria e i suoi pulsanti.
     * <p>
     * Questo metodo imposta vari listener del mouse e gestori di azioni per una
     * card di libreria, migliorando l'interattività e l'esperienza utente. Le funzionalità includono:
     * <ul>
     * <li><b>Effetti Hover:</b> Al passaggio del mouse, la card cambia stile e si ingrandisce leggermente
     * per fornire un feedback visivo.</li>
     * <li><b>Azione al Clic:</b> Un clic sulla card (che non sia su uno dei pulsanti)
     * attiva la visualizzazione dei libri all'interno di quella libreria.</li>
     * <li><b>Pulsante "Visualizza":</b> L'azione del pulsante "Visualizza"
     * lancia esplicitamente la visualizzazione dei libri della libreria.</li>
     * <li><b>Pulsante "Elimina":</b> L'azione del pulsante "Elimina"
     * avvia il processo di conferma per la cancellazione della libreria.</li>
     * </ul>
     * I metodi {@code consume()} sono utilizzati per prevenire la propagazione degli eventi
     * e garantire che l'azione corretta venga eseguita solo una volta.
     * </p>
     *
     * @param card L'{@link HBox} che rappresenta la card della libreria.
     * @param libraryName Il nome della libreria associata alla card.
     * @param viewButton Il {@link Button} per visualizzare i libri.
     * @param deleteButton Il {@link Button} per eliminare la libreria.
     */
    private void setupLibraryCardEvents(HBox card, String libraryName, Button viewButton, Button deleteButton) {
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.12);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: rgba(255,255,255,0.2);" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-width: 1;" +
                            "-fx-cursor: hand;"
            );
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.08);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: rgba(255,255,255,0.1);" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-width: 1;" +
                            "-fx-cursor: hand;"
            );
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        card.setOnMouseClicked(e -> {
            if (!e.isConsumed()) {
                viewLibraryBooks(libraryName);
            }
        });

        viewButton.setOnAction(e -> {
            e.consume();
            viewLibraryBooks(libraryName);
        });

        deleteButton.setOnAction(e -> {
            e.consume();
            showDeleteLibraryConfirmation(libraryName);
        });
    }

    /**
     * Visualizza i libri all'interno di una specifica libreria.
     * <p>
     * Questo metodo nasconde la sezione di creazione e mostra una griglia con
     * i libri della libreria selezionata. Esegue una chiamata asincrona al servizio
     * per recuperare i dati.
     * </p>
     * @param libraryName Il nome della libreria da visualizzare.
     */
    public void viewLibraryBooks(String libraryName) {

        hideCreateLibrarySection();

        librariesContainer.getChildren().clear();

        VBox loadingBox = createLoadingIndicator();
        librariesContainer.getChildren().add(loadingBox);

        libraryService.getBooksInLibraryAsync(username, libraryName)
                .thenAccept(response -> Platform.runLater(() -> {
                    librariesContainer.getChildren().remove(loadingBox);

                    if (response.isSuccess() && response.getBooks() != null) {
                        List<Book> books = response.getBooks();
                        this.currentLibraryBooks = books;

                        VBox header = createBooksViewHeader(libraryName);
                        librariesContainer.getChildren().add(header);

                        if (books.isEmpty()) {
                            VBox emptyBox = new VBox(20);
                            emptyBox.setAlignment(Pos.CENTER);
                            emptyBox.setPadding(new Insets(40));

                            Label iconLabel = new Label("📚");
                            iconLabel.setFont(Font.font(48));
                            iconLabel.setOpacity(0.4);

                            Label emptyLabel = new Label("Libreria vuota");
                            emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
                            emptyLabel.setTextFill(Color.web("#8E8E93"));

                            Label hintLabel = new Label("Aggiungi libri alla libreria '" + libraryName + "' per iniziare");
                            hintLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                            hintLabel.setTextFill(Color.web("#636366"));
                            hintLabel.setWrapText(true);
                            hintLabel.setMaxWidth(280);
                            hintLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                            emptyBox.getChildren().addAll(iconLabel, emptyLabel, hintLabel);
                            librariesContainer.getChildren().add(emptyBox);
                        } else {
                            createBooksGrid(books);
                        }
                    } else {
                        showErrorMessage("Errore nel caricamento dei libri: " +
                                (response.getMessage() != null ? response.getMessage() : "Risposta vuota dal server"));
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        librariesContainer.getChildren().remove(loadingBox);
                        showErrorMessage("Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Crea l'header per la visualizzazione dei libri all'interno di una libreria.
     * <p>
     * Include un pulsante per tornare all'elenco delle librerie e il titolo della
     * libreria corrente.
     * </p>
     * @param libraryName Il nome della libreria.
     * @return Un {@link VBox} che rappresenta l'header della vista libri.
     */
    private VBox createBooksViewHeader(String libraryName) {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20, 0, 20, 0));

        HBox navBox = new HBox(10);
        navBox.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("← Torna alle librerie");
        backButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: #007AFF;" +
                        "-fx-font-size: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-underline: false;"
        );

        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                    "-fx-background-color: rgba(0,122,255,0.1);" +
                            "-fx-background-radius: 6;" +
                            "-fx-border-color: transparent;" +
                            "-fx-text-fill: #007AFF;" +
                            "-fx-font-size: 14;" +
                            "-fx-cursor: hand;"
            );
        });

        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-color: transparent;" +
                            "-fx-text-fill: #007AFF;" +
                            "-fx-font-size: 14;" +
                            "-fx-cursor: hand;"
            );
        });

        backButton.setOnAction(e -> loadUserLibraries());

        navBox.getChildren().add(backButton);

        Label libraryTitle = new Label("📖 " + libraryName);
        libraryTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        libraryTitle.setTextFill(Color.WHITE);

        header.getChildren().addAll(navBox, libraryTitle);
        return header;
    }

    /**
     * Crea e configura una griglia per visualizzare una lista di libri.
     * <p>
     * Questo metodo costruisce un layout basato su {@link GridPane} per disporre
     * i libri in una griglia visiva. Le caratteristiche principali includono:
     * <ul>
     * <li>Un'etichetta statistica che mostra il numero totale di libri nella libreria.</li>
     * <li>Una griglia con quattro colonne, configurate per ridimensionarsi in base
     * allo spazio disponibile.</li>
     * <li>Per ogni libro nella lista, viene creata una card moderna (utilizzando
     * {@link #createModernBookCard}) e aggiunta alla griglia in modo ordinato.</li>
     * <li>Il layout complessivo è organizzato in un {@link VBox} che include
     * l'etichetta statistica e la griglia stessa.</li>
     * </ul>
     * Questa struttura garantisce una visualizzazione pulita e responsive dei libri.
     * </p>
     *
     * @param books La lista di {@link Book} da visualizzare nella griglia.
     */
    private void createBooksGrid(List<Book> books) {
        VBox booksContainer = new VBox(30);
        booksContainer.setPadding(new Insets(20, 0, 0, 0));

        Label statsLabel = new Label("📊 " + books.size() + " libri nella libreria");
        statsLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        statsLabel.setTextFill(Color.web("#8E8E93"));
        booksContainer.getChildren().add(statsLabel);

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(35);
        grid.setPadding(new Insets(20, 0, 0, 0));

        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            grid.getColumnConstraints().add(column);
        }

        for (int i = 0; i < books.size(); i++) {
            VBox bookCard = createModernBookCard(books.get(i));
            int col = i % 4;
            int row = i / 4;
            grid.add(bookCard, col, row);
            GridPane.setHalignment(bookCard, javafx.geometry.HPos.CENTER);
        }

        booksContainer.getChildren().add(grid);
        librariesContainer.getChildren().add(booksContainer);
    }

    /**
     * Crea e configura una card (scheda) moderna per un singolo libro.
     * <p>
     * Questo metodo costruisce un {@link VBox} che funge da rappresentazione visiva
     * di un libro all'interno di una griglia. La card include la copertina del libro,
     * il titolo e l'autore. La copertina viene gestita in modo sicuro e stilizzata
     * con angoli arrotondati e un'ombra per un aspetto tridimensionale. I metadati
     * del libro (titolo e autore) vengono formattati e allineati per una lettura chiara.
     * <p>
     * Vengono aggiunti gestori di eventi per il mouse che creano effetti visivi
     * (cambio di scala e opacità) al passaggio del cursore, migliorando l'interazione.
     * Inoltre, un gestore di eventi al clic del mouse permette di aprire i dettagli
     * del libro o di eseguire un'azione personalizzata se un consumer è stato fornito.
     * </p>
     *
     * @param book L'oggetto {@link Book} da cui estrarre i dati per la card.
     * @return Un {@link VBox} che rappresenta la card completa del libro.
     */
    private VBox createModernBookCard(Book book) {
        VBox card = new VBox(12);
        card.setMaxWidth(140);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-cursor: hand;");

        ImageView cover = ImageUtils.createSafeImageView(book.getImageUrl(), 120, 170);

        Rectangle clip = new Rectangle(120, 170);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        cover.setClip(clip);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        cover.setEffect(shadow);

        String titleText = book.getTitle() != null ? book.getTitle() : "Titolo non disponibile";
        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(120);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPrefHeight(35);

        String authorText = book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto";
        Label authorLabel = new Label(authorText);
        authorLabel.setFont(Font.font("System", 11));
        authorLabel.setTextFill(Color.web("#999999"));
        authorLabel.setWrapText(false);
        authorLabel.setMaxWidth(120);
        authorLabel.setAlignment(Pos.CENTER);
        authorLabel.setStyle("-fx-text-overrun: ellipsis;");

        card.getChildren().addAll(cover, titleLabel, authorLabel);

        card.setOnMouseClicked(e -> {
            if (onBookClick != null) {
                onBookClick.accept(book);
            } else {
                System.out.println("📖 Click libro libreria: " + book.getTitle());
                System.out.println("📚 Aprendo con lista di " + (currentLibraryBooks != null ? currentLibraryBooks.size() : 0) + " libri della libreria");

                List<Book> libraryBooksList = currentLibraryBooks != null ? currentLibraryBooks : List.of(book);

                BooksClient.openBookDetails(book, libraryBooksList, null);
            }
        });

        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
            cover.setOpacity(0.9);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            cover.setOpacity(1.0);
        });

        return card;
    }

    /**
     * Restituisce la lista dei libri attualmente visualizzati nella libreria.
     * @return Una {@link List} di {@link Book}.
     */
    public List<Book> getCurrentLibraryBooks() {
        return currentLibraryBooks != null ? new ArrayList<>(currentLibraryBooks) : new ArrayList<>();
    }

    /**
     * Mostra un dialog di conferma per l'eliminazione di una libreria.
     * <p>
     * Questo metodo crea un {@link Alert} di tipo {@link javafx.scene.control.Alert.AlertType#CONFIRMATION}
     * che chiede all'utente di confermare l'eliminazione di una libreria specifica.
     * Il dialog avvisa che l'azione è irreversibile e che tutti i libri al suo interno
     * verranno rimossi. L'utente ha la possibilità di confermare l'eliminazione
     * o annullare l'operazione. Se l'utente sceglie di procedere, viene invocato
     * il metodo {@link #deleteLibrary(String)}.
     * </p>
     *
     * @param libraryName Il nome della libreria da eliminare.
     */
    private void showDeleteLibraryConfirmation(String libraryName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Elimina Libreria");
        alert.setHeaderText("Sei sicuro di voler eliminare la libreria '" + libraryName + "'?");
        alert.setContentText("Questa azione non può essere annullata. Tutti i libri nella libreria saranno rimossi dalla raccolta.");

        ButtonType deleteButtonType = new ButtonType("Elimina", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                deleteLibrary(libraryName);
            }
        });
    }

    /**
     * Elimina in modo asincrono una libreria dell'utente.
     * <p>
     * Questo metodo invia una richiesta al servizio di libreria per rimuovere la
     * libreria specificata. L'operazione è gestita in modo asincrono per non
     * bloccare l'interfaccia utente. Una volta completata la richiesta:
     * <ul>
     * <li>Se l'eliminazione ha successo, viene mostrato un messaggio di conferma
     * e la lista delle librerie viene ricaricata per aggiornare l'interfaccia.</li>
     * <li>In caso di errore dal servizio, viene mostrato un messaggio di errore
     * specifico.</li>
     * <li>Se si verifica un'eccezione, come un problema di connessione, viene
     * visualizzato un messaggio di errore generico.</li>
     * </ul>
     * Tutti gli aggiornamenti dell'interfaccia utente avvengono in modo sicuro
     * sul thread di JavaFX tramite {@link Platform#runLater}.
     * </p>
     *
     * @param libraryName Il nome della libreria da eliminare.
     */
    private void deleteLibrary(String libraryName) {
        libraryService.deleteLibraryAsync(username, libraryName)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showSuccessMessage("Libreria '" + libraryName + "' eliminata con successo!");
                        loadUserLibraries();
                    } else {
                        showErrorMessage("Errore nell'eliminazione: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showErrorMessage("Errore di connessione: " + throwable.getMessage()));
                    return null;
                });
    }

    /**
     * Imposta il consumer per l'evento di click su un libro.
     * <p>
     * Questo metodo permette al chiamante di definire un'azione da eseguire
     * quando un utente clicca su un libro all'interno del pannello.
     * </p>
     * @param onBookClick Il consumer che riceve l'oggetto {@link Book} cliccato.
     */
    public void setOnBookClick(Consumer<Book> onBookClick) {
        this.onBookClick = onBookClick;
    }

    /**
     * Imposta l'azione da eseguire alla chiusura del pannello.
     * @param onClosePanel Un {@link Runnable} che viene eseguito quando l'utente
     * clicca il pulsante di chiusura.
     */
    public void setOnClosePanel(Runnable onClosePanel) {
        this.onClosePanel = onClosePanel;
    }

    /**
     * Nasconde la sezione dedicata alla creazione di una nuova libreria.
     * <p>
     * Questo metodo rende invisibile e disabilita la gestione del layout per la
     * sezione di creazione di una libreria e il separatore che la precede. L'operazione
     * è condizionale, verificando che la struttura dei figli del pannello sia
     * sufficientemente grande prima di tentare di nascondere i componenti.
     * Questo approccio è utile quando si passa da una vista a un'altra, come
     * dalla lista delle librerie alla vista dei libri all'interno di una specifica
     * libreria, per ottimizzare lo spazio e mantenere l'interfaccia pulita.
     * </p>
     */
    private void hideCreateLibrarySection() {
        if (this.getChildren().size() >= 3) {
            this.getChildren().get(1).setVisible(false);
            this.getChildren().get(1).setManaged(false);
            this.getChildren().get(2).setVisible(false);
            this.getChildren().get(2).setManaged(false);
        }
    }

    /**
     * Rende visibile e riabilita la sezione dedicata alla creazione di una nuova libreria.
     * <p>
     * Questo metodo, se la struttura del pannello lo permette, imposta la visibilità
     * e riabilita la gestione del layout per la sezione di creazione e il suo
     * separatore. È l'azione opposta a {@link #hideCreateLibrarySection()},
     * utilizzata per tornare alla vista principale delle librerie dopo aver
     * visualizzato i dettagli di una specifica libreria.
     * </p>
     */
    private void showCreateLibrarySection() {
        if (this.getChildren().size() >= 3) {
            this.getChildren().get(1).setVisible(true);
            this.getChildren().get(1).setManaged(true);
            this.getChildren().get(2).setVisible(true);
            this.getChildren().get(2).setManaged(true);
        }
    }
}