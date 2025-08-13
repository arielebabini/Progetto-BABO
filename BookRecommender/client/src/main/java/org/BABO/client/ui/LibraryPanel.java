package org.BABO.client.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import org.BABO.client.ui.ImageUtils;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Panel delle librerie ridisegnato per integrarsi meglio
 * con il design generale dell'applicazione Apple Books
 */
public class LibraryPanel extends VBox {

    private final LibraryService libraryService;
    private final String username;
    private Consumer<Book> onBookClick;
    private Runnable onClosePanel;
    private AuthService authManager; // Corretto: usa AuthService invece di AuthenticationManager

    // UI Components
    private VBox librariesContainer;
    private TextField newLibraryField;
    private Button createLibraryButton;
    private Label titleLabel;
    private ScrollPane scrollPane;
    private VBox currentLibraryContent;
    private String currentLibraryName;

    // Cache per libri della libreria corrente
    private List<Book> currentLibraryBooks;

    public LibraryPanel(String username) {
        this.libraryService = new LibraryService();
        this.username = username;
        setupImprovedUI();
        loadUserLibraries();
    }

    public LibraryPanel(String username, AuthService authManager) { // Corretto: usa AuthService
        this.libraryService = new LibraryService();
        this.username = username;
        this.authManager = authManager;
        setupImprovedUI();
        loadUserLibraries();
    }

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

        // Rimuovi bordi e radius per integrare meglio
        this.setStyle("-fx-effect: null;");

        setupLayout();
    }

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

    private VBox createModernHeader() {
        VBox header = new VBox(0);
        header.setPadding(new Insets(40, 60, 30, 60));

        // Background del header con sfumatura
        LinearGradient headerBg = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1e1e1e")),
                new Stop(1, Color.web("#2a2a2c"))
        );
        header.setBackground(new Background(new BackgroundFill(headerBg,
                CornerRadii.EMPTY, Insets.EMPTY)));

        // Titolo con stile Apple Books
        titleLabel = new Label("📚 Le Tue Librerie");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);

        // Sottotitolo descrittivo
        Label subtitleLabel = new Label("Organizza e gestisci la tua collezione personale");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#8E8E93"));
        subtitleLabel.setPadding(new Insets(5, 0, 0, 0));

        // Pulsante chiudi ridisegnato
        Button closeButton = createModernCloseButton();

        // Layout header
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleLabel, spacer, closeButton);

        header.getChildren().addAll(titleRow, subtitleLabel);
        return header;
    }

    private Button createModernCloseButton() {
        Button closeButton = new Button("x");
        closeButton.setPrefSize(40, 40);

        closeButton.setAlignment(Pos.CENTER); // centra il testo
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

    private VBox createNewLibrarySection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 60, 20, 60));

        // Card container per la creazione
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );

        // Titolo sezione
        Label sectionTitle = new Label("Crea Nuova Libreria");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.WHITE);

        // Input field moderno
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

        // Focus effect per input
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

        // Pulsante di creazione moderno
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

        // Effetti hover per pulsante
        setupButtonHoverEffects(createLibraryButton);

        // Layout input
        HBox inputLayout = new HBox(15);
        inputLayout.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(newLibraryField, Priority.ALWAYS);
        inputLayout.getChildren().addAll(newLibraryField, createLibraryButton);

        card.getChildren().addAll(sectionTitle, inputLayout);
        section.getChildren().add(card);

        // Event handlers
        setupCreateLibraryHandlers();

        return section;
    }

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

    private Region createElegantSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setMaxHeight(1);

        // Sfumatura per il separatore
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

    private void setupLibrariesContainer() {
        librariesContainer = new VBox(20);
        librariesContainer.setAlignment(Pos.TOP_CENTER);
        librariesContainer.setPadding(new Insets(0, 60, 40, 60));

        // ScrollPane moderno
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

    private void setupCreateLibraryHandlers() {
        createLibraryButton.setOnAction(e -> createNewLibrary());

        newLibraryField.setOnAction(e -> createNewLibrary());

        // Abilita/disabilita pulsante in base al testo
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

        // Stato iniziale
        createLibraryButton.setDisable(true);
    }

    private void createNewLibrary() {
        String libraryName = newLibraryField.getText().trim();
        if (libraryName.isEmpty()) {
            return;
        }

        // Disabilita pulsante durante creazione
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

        // Rimuovi messaggio dopo 3 secondi
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> librariesContainer.getChildren().remove(container));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

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

        // Rimuovi messaggio dopo 5 secondi
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> librariesContainer.getChildren().remove(container));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void loadUserLibraries() {
        // NUOVO: Mostra di nuovo la sezione creazione quando torniamo alla lista delle librerie
        showCreateLibrarySection();

        librariesContainer.getChildren().clear();

        // Loading indicator moderno
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

    private VBox createLoadingIndicator() {
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));

        // Spinner personalizzato
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

    private void showEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));

        // Icona grande
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

    private void displayLibraries(List<String> libraries) {
        for (String library : libraries) {
            HBox libraryCard = createLibraryCard(library);
            librariesContainer.getChildren().add(libraryCard);
        }
    }

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

        // Icona libreria
        Label iconLabel = new Label("📖");
        iconLabel.setFont(Font.font(24));

        // Info libreria
        VBox infoBox = new VBox(4);

        Label nameLabel = new Label(libraryName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Tocca per visualizzare");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        subtitleLabel.setTextFill(Color.web("#8E8E93"));

        infoBox.getChildren().addAll(nameLabel, subtitleLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Pulsanti azione
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = createActionButton("👁", "Visualizza");
        Button deleteButton = createActionButton("🗑", "Elimina");
        deleteButton.setStyle(deleteButton.getStyle().replace("#007AFF", "#FF3B30"));

        actionsBox.getChildren().addAll(viewButton, deleteButton);

        card.getChildren().addAll(iconLabel, infoBox, spacer, actionsBox);

        // Eventi
        setupLibraryCardEvents(card, libraryName, viewButton, deleteButton);

        return card;
    }

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

    private void setupLibraryCardEvents(HBox card, String libraryName, Button viewButton, Button deleteButton) {
        // Hover effect per la card
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

        // Click sulla card per visualizzare
        card.setOnMouseClicked(e -> {
            if (!e.isConsumed()) {
                viewLibraryBooks(libraryName);
            }
        });

        // Azioni pulsanti
        viewButton.setOnAction(e -> {
            e.consume();
            viewLibraryBooks(libraryName);
        });

        deleteButton.setOnAction(e -> {
            e.consume();
            showDeleteLibraryConfirmation(libraryName);
        });
    }

    public void viewLibraryBooks(String libraryName) {
        this.currentLibraryName = libraryName;

        // NUOVO: Nascondi sezione creazione libreria e separatore quando si visualizzano i libri
        hideCreateLibrarySection();

        librariesContainer.getChildren().clear();

        VBox loadingBox = createLoadingIndicator();
        librariesContainer.getChildren().add(loadingBox);

        // CORRETTO: Usa getBooksInLibraryAsync per ottenere i libri di una libreria specifica
        libraryService.getBooksInLibraryAsync(username, libraryName)
                .thenAccept(response -> Platform.runLater(() -> {
                    librariesContainer.getChildren().remove(loadingBox);

                    if (response.isSuccess() && response.getBooks() != null) {
                        List<Book> books = response.getBooks();
                        this.currentLibraryBooks = books; // Salva i libri in cache

                        // Header per la vista libri
                        VBox header = createBooksViewHeader(libraryName);
                        librariesContainer.getChildren().add(header);

                        if (books.isEmpty()) {
                            // Mostra stato vuoto senza header duplicato
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
                            // Mostra i libri in una griglia
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

    private VBox createBooksViewHeader(String libraryName) {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20, 0, 20, 0));

        // Navigazione back
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

        // Titolo libreria
        Label libraryTitle = new Label("📖 " + libraryName);
        libraryTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        libraryTitle.setTextFill(Color.WHITE);

        header.getChildren().addAll(navBox, libraryTitle);
        return header;
    }

    private void createBooksGrid(List<Book> books) {
        // Container per la griglia di libri - stile simile a ExploreIntegration
        VBox booksContainer = new VBox(30);
        booksContainer.setPadding(new Insets(20, 0, 0, 0));

        // Statistiche della libreria
        Label statsLabel = new Label("📊 " + books.size() + " libri nella libreria");
        statsLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        statsLabel.setTextFill(Color.web("#8E8E93"));
        booksContainer.getChildren().add(statsLabel);

        // Crea griglia di libri 4 per riga come in ExploreIntegration
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(35);
        grid.setPadding(new Insets(20, 0, 0, 0));

        // Configura colonne responsive
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            grid.getColumnConstraints().add(column);
        }

        // Popola la griglia
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

    private VBox createModernBookCard(Book book) {
        // Stile identico a ExploreIntegration.createBookCard()
        VBox card = new VBox(12);
        card.setMaxWidth(140);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-cursor: hand;");

        // CORRETTO: Usa ImageUtils invece del metodo personalizzato
        ImageView cover = ImageUtils.createSafeImageView(book.getImageUrl(), 120, 170);

        Rectangle clip = new Rectangle(120, 170);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        cover.setClip(clip);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        cover.setEffect(shadow);

        // Titolo con stile identico
        String titleText = book.getTitle() != null ? book.getTitle() : "Titolo non disponibile";
        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(120);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPrefHeight(35);

        // Autore con stile identico
        String authorText = book.getAuthor() != null ? book.getAuthor() : "Autore sconosciuto";
        Label authorLabel = new Label(authorText);
        authorLabel.setFont(Font.font("System", 11));
        authorLabel.setTextFill(Color.web("#999999"));
        authorLabel.setWrapText(false);
        authorLabel.setMaxWidth(120);
        authorLabel.setAlignment(Pos.CENTER);
        authorLabel.setStyle("-fx-text-overrun: ellipsis;");

        card.getChildren().addAll(cover, titleLabel, authorLabel);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (onBookClick != null) {
                onBookClick.accept(book);
            }
        });

        // Hover effect identico a ExploreIntegration
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

    // Metodo helper per creare ImageView sicuro (simile a ImageUtils)
    private ImageView createSafeImageView(String imageUrl, double width, double height) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                Image image = new Image(imageUrl, width, height, false, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                // Se l'immagine fallisce, usa placeholder
                setPlaceholderImage(imageView, width, height);
            }
        } else {
            // Nessun URL, usa placeholder
            setPlaceholderImage(imageView, width, height);
        }

        return imageView;
    }

    private void setPlaceholderImage(ImageView imageView, double width, double height) {
        // Crea un placeholder colorato invece dell'immagine
        Rectangle placeholder = new Rectangle(width, height);
        placeholder.setFill(LinearGradient.valueOf("linear-gradient(to bottom, #4A4A4C, #2C2C2E)"));
        placeholder.setArcWidth(8);
        placeholder.setArcHeight(8);

        // Per ora imposta trasparenza totale - potresti sostituire con un'immagine placeholder
        imageView.setOpacity(0);
    }

    private void showDeleteLibraryConfirmation(String libraryName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Elimina Libreria");
        alert.setHeaderText("Sei sicuro di voler eliminare la libreria '" + libraryName + "'?");
        alert.setContentText("Questa azione non può essere annullata. Tutti i libri nella libreria saranno rimossi dalla raccolta.");

        // Styling dell'alert
        /*DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1e1e1e;" +
                        "-fx-text-fill: white;"
        );*/

        // Personalizza pulsanti
        ButtonType deleteButtonType = new ButtonType("Elimina", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                deleteLibrary(libraryName);
            }
        });
    }

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

    // Metodi pubblici per integrazione con l'applicazione principale
    public void setOnBookClick(Consumer<Book> onBookClick) {
        this.onBookClick = onBookClick;
    }

    public void setOnClosePanel(Runnable onClosePanel) {
        this.onClosePanel = onClosePanel;
    }

    public void setAuthenticationManager(AuthService authManager) {
        this.authManager = authManager;
    }

    public void addBookToLibrary(Book book, String libraryName) {
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            showErrorMessage("Impossibile aggiungere il libro: ISBN mancante");
            return;
        }

        libraryService.addBookToLibraryAsync(username, libraryName, book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showSuccessMessage("Libro aggiunto alla libreria '" + libraryName + "'");
                        // CORRETTO: Se stiamo visualizzando quella libreria, ricarica i libri
                        if (libraryName.equals(currentLibraryName)) {
                            viewLibraryBooks(libraryName);
                        }
                    } else {
                        showErrorMessage("Errore nell'aggiunta: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showErrorMessage("Errore di connessione: " + throwable.getMessage()));
                    return null;
                });
    }

    public CompletableFuture<List<String>> getUserLibraries() {
        return libraryService.getUserLibrariesAsync(username)
                .thenApply(response -> {
                    if (response.isSuccess() && response.getLibraries() != null) {
                        return response.getLibraries();
                    }
                    return List.of();
                });
    }

    public void refreshLibraries() {
        loadUserLibraries();
    }

    // NUOVI: Metodi per gestire la visibilità della sezione creazione libreria
    private void hideCreateLibrarySection() {
        // Nascondi la sezione creazione e il separatore quando visualizzi i libri
        if (this.getChildren().size() >= 3) {
            this.getChildren().get(1).setVisible(false);  // Sezione creazione
            this.getChildren().get(1).setManaged(false);
            this.getChildren().get(2).setVisible(false);  // Separatore
            this.getChildren().get(2).setManaged(false);
        }
    }

    private void showCreateLibrarySection() {
        // Mostra di nuovo la sezione creazione e il separatore quando torni alla lista
        if (this.getChildren().size() >= 3) {
            this.getChildren().get(1).setVisible(true);   // Sezione creazione
            this.getChildren().get(1).setManaged(true);
            this.getChildren().get(2).setVisible(true);   // Separatore
            this.getChildren().get(2).setManaged(true);
        }
    }
}