package org.BABO.client.ui.Admin;

import org.BABO.client.service.AdminService;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.BookRating;
import org.BABO.shared.model.Review;
import org.BABO.shared.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Gestisce il pannello di amministrazione per il client dell'applicazione BABO.
 * <p>
 * Questa classe fornisce un'interfaccia utente JavaFX completa e dinamica per
 * le operazioni amministrative, consentendo la gestione di utenti, libri e
 * recensioni. La sua architettura è modulare, con sezioni dedicate per ogni
 * tipo di entità e una gestione robusta delle interazioni con i servizi backend.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 * <li><strong>Gestione Utenti:</strong> Visualizzazione e eliminazione degli utenti registrati.</li>
 * <li><strong>Gestione Libri:</strong> Operazioni CRUD (Creazione, Lettura, Aggiornamento, Eliminazione)
 * sui libri del catalogo, inclusa la gestione delle copertine locali.</li>
 * <li><strong>Gestione Recensioni:</strong> Visualizzazione e moderazione delle recensioni e delle valutazioni.</li>
 * <li><strong>Interfaccia Utente Dinamica:</strong> Navigazione fluida tra le diverse sezioni amministrative.</li>
 * <li><strong>Feedback in tempo reale:</strong> Messaggi di stato dinamici per monitorare l'esito delle operazioni.</li>
 * </ul>
 *
 * <h3>Architettura e Design:</h3>
 * <p>
 * Il pannello è costruito come un componente {@link VBox} che gestisce dinamicamente
 * il suo contenuto in base alla sezione selezionata (utenti, libri, recensioni).
 * L'integrazione con {@link AuthenticationManager} garantisce che solo gli
 * amministratori autenticati possano accedere e operare. Il pannello utilizza
 * {@link ObservableList} per legare i dati ai componenti {@link TableView},
 * assicurando che le tabelle si aggiornino automaticamente quando i dati
 * vengono modificati.
 * </p>
 *
 * <h3>Integrazione con Servizi:</h3>
 * <p>
 * Tutte le operazioni di gestione dati (recupero, eliminazione, modifica, ecc.)
 * sono delegate a {@link AdminService}. Questo assicura che la logica di business
 * e l'accesso al database siano separati dall'interfaccia utente. Le chiamate
 * ai servizi sono asincrone e gestite tramite {@link java.util.concurrent.CompletableFuture}
 * per non bloccare il thread dell'interfaccia utente.
 * </p>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Inizializza il manager di autenticazione e il pannello admin
 * AuthenticationManager authManager = new AuthenticationManager();
 * AdminPanel adminPanel = new AdminPanel(authManager);
 *
 * // Crea la scena e la root
 * StackPane root = new StackPane();
 * Scene scene = new Scene(root, 1024, 768);
 *
 * // Aggiungi il pannello admin alla root della scena
 * root.getChildren().add(adminPanel.createAdminPanel());
 *
 * // Mostra lo stage
 * primaryStage.setScene(scene);
 * primaryStage.show();
 * }</pre>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * Tutti gli aggiornamenti dell'interfaccia utente, in risposta ai risultati
 * delle operazioni asincrone, sono eseguiti in modo sicuro utilizzando
 * {@link Platform#runLater(Runnable)}, garantendo che le modifiche vengano
 * applicate sul JavaFX Application Thread.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see AdminService
 * @see Book
 * @see User
 * @see Review
 */
public class AdminPanel {

    /**
     * Gestisce l'interazione con l'API di autenticazione per la verifica dei permessi.
     */
    private final AuthenticationManager authManager;

    /**
     * Fornisce i metodi per le operazioni CRUD (Create, Read, Update, Delete) sui dati
     * gestiti dal pannello di amministrazione, come utenti, libri e recensioni.
     */
    private final AdminService adminService;

    /**
     * La tabella JavaFX che visualizza i dati degli utenti.
     */
    private TableView<User> usersTable;

    /**
     * Una lista osservabile che contiene i dati degli utenti recuperati dal servizio.
     * È legata a {@link #usersTable} per garantire aggiornamenti dinamici dell'interfaccia.
     */
    private ObservableList<User> usersData;

    /**
     * Un'etichetta JavaFX utilizzata per mostrare messaggi di stato, come l'esito
     * delle operazioni o lo stato del caricamento dei dati.
     */
    private Label statusLabel;

    /**
     * La tabella JavaFX che visualizza i dati dei libri.
     */
    private TableView<Book> booksTable;

    /**
     * Una lista osservabile che contiene i dati dei libri recuperati dal servizio.
     * È legata a {@link #booksTable} per aggiornamenti automatici.
     */
    private ObservableList<Book> booksData;

    /**
     * Il contenitore principale per le diverse viste (utenti, libri, recensioni).
     * Viene aggiornato dinamicamente per mostrare la sezione selezionata.
     */
    private VBox currentContent;

    /**
     * Il contenitore radice che incapsula l'intero layout del pannello di amministrazione.
     */
    private VBox mainAdminPanel;

    /**
     * Una copia completa e non filtrata di tutti i dati dei libri, utilizzata per
     * le operazioni di ricerca e aggiornamento.
     */
    private ObservableList<Book> allBooksData;

    /**
     * Il campo di testo utilizzato per l'inserimento della stringa di ricerca.
     */
    private TextField searchField;

    /**
     * La tabella JavaFX che visualizza i dati delle recensioni.
     */
    private TableView<BookRating> reviewsTable;

    /**
     * Una lista osservabile che contiene i dati delle recensioni recuperati e formattati.
     */
    private ObservableList<BookRating> reviewsData;

    /**
     * Una copia completa di tutte le recensioni, utilizzata per le operazioni di
     * ricerca e filtraggio.
     */
    private ObservableList<Review> allReviewsData;

    /**
     * Il campo di testo utilizzato per la ricerca all'interno della tabella delle recensioni.
     */
    private TextField reviewsSearchField;

    /**
     * Costruisce una nuova istanza di {@code AdminPanel}.
     * <p>
     * Questo costruttore inizializza i componenti principali del pannello di amministrazione,
     * incluse le dipendenze da {@link AuthenticationManager} e {@link AdminService}.
     * Prepara le {@link ObservableList} per la gestione dinamica dei dati di utenti,
     * libri e recensioni, e si assicura che la directory per le copertine dei libri
     * sia pronta per l'uso.
     * </p>
     *
     * @param authManager L'istanza di {@link AuthenticationManager} utilizzata per
     * gestire l'autenticazione e i permessi dell'utente corrente.
     */
    public AdminPanel(AuthenticationManager authManager) {
        this.authManager = authManager;
        this.adminService = new AdminService();
        this.usersData = FXCollections.observableArrayList();
        this.booksData = FXCollections.observableArrayList();
        this.currentContent = new VBox();

        initializeBooksCoversDirectory();

        this.allBooksData = FXCollections.observableArrayList();

        this.reviewsData = FXCollections.observableArrayList();
    }


    /**
     * Crea e restituisce il layout principale del pannello di amministrazione.
     * <p>
     * Questo metodo costruisce la struttura di base dell'interfaccia utente amministrativa,
     * impostando lo stile del contenitore principale ({@link VBox}) e aggiungendo i
     * componenti principali: l'header e il menu di navigazione. È il punto di ingresso
     * per la visualizzazione dell'intera interfaccia.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta il pannello di amministrazione completo.
     * @see #createHeader()
     * @see #createAdminMenu()
     */
    public VBox createAdminPanel() {
        mainAdminPanel = new VBox(20);
        mainAdminPanel.setPadding(new Insets(30));
        mainAdminPanel.setStyle("-fx-background-color: #1e1e1e;");

        VBox header = createHeader();

        VBox menuContainer = createAdminMenu();

        mainAdminPanel.getChildren().addAll(header, menuContainer);

        return mainAdminPanel;
    }

    /**
     * Crea il contenitore del menu principale per il pannello di amministrazione.
     * <p>
     * Questo metodo costruisce la sezione del menu che permette all'amministratore
     * di navigare tra le diverse aree di gestione: utenti, libri e recensioni.
     * Il layout è organizzato in un {@link VBox} centrale che contiene un titolo,
     * un sottotitolo, e una serie di "card" ({@link VBox}) interattive, ognuna
     * che rappresenta una specifica area di gestione. Ogni card è un pulsante
     * stilizzato che, al click, attiva la vista corrispondente. Include anche
     * un'etichetta che mostra l'utente amministratore attualmente connesso.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta il menu di navigazione dell'amministratore.
     * @see #createMenuCard(String, String, String, String, javafx.event.EventHandler)
     * @see #showUsersManagement()
     * @see #showBooksManagement()
     * @see #showReviewsManagement()
     */
    private VBox createAdminMenu() {
        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));

        // Titolo menu
        Label menuTitle = new Label("🔧 Pannello Amministrazione");
        menuTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        menuTitle.setTextFill(Color.WHITE);
        menuTitle.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Scegli cosa vuoi gestire:");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.LIGHTGRAY);
        subtitle.setAlignment(Pos.CENTER);

        HBox buttonsContainer = new HBox(30);
        buttonsContainer.setAlignment(Pos.CENTER);

        // Pulsante gestione utenti
        VBox usersCard = createMenuCard(
                "👥",
                "Gestione Utenti",
                "Visualizza, elimina e gestisci\ngli utenti registrati",
                "#6c5ce7",
                e -> {
                    showUsersManagement();
                }
        );

        // Pulsante gestione libri
        VBox booksCard = createMenuCard(
                "📚",
                "Gestione Libri",
                "Aggiungi, modifica ed elimina\ni libri dal catalogo",
                "#00b894",
                e -> {
                    showBooksManagement();
                }
        );

        // Pulsante gestione recensioni
        VBox reviewsCard = createMenuCard(
                "⭐",
                "Gestione Recensioni",
                "Visualizza e gestisci\nle recensioni dei libri",
                "#fd79a8",
                e -> {
                    showReviewsManagement();
                }
        );

        buttonsContainer.getChildren().addAll(usersCard, booksCard, reviewsCard);

        // Info admin
        Label adminInfo = new Label("👑 Connesso come: " + authManager.getCurrentUser().getEmail());
        adminInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        adminInfo.setTextFill(Color.LIGHTBLUE);

        container.getChildren().addAll(menuTitle, subtitle, buttonsContainer, adminInfo);
        return container;
    }

    /**
     * Crea una card di menu stilizzata e interattiva.
     * <p>
     * Questo metodo di utilità genera un componente {@link VBox} che funge da
     * "card" cliccabile all'interno del menu di amministrazione. La card visualizza
     * un'icona, un titolo e una breve descrizione. Include uno stile CSS in linea
     * per il design (colori, bordi, angoli arrotondati) e gestisce gli effetti
     * visivi al passaggio del mouse (`onMouseEntered`, `onMouseExited`) per
     * fornire un feedback all'utente. Al click, esegue un'azione definita.
     * </p>
     *
     * @param icon Il carattere Unicode o la stringa per l'icona da visualizzare.
     * @param title Il titolo della card.
     * @param description Una breve descrizione delle funzionalità della card.
     * @param color La stringa esadecimale del colore per il bordo e l'icona.
     * @param action Un {@link javafx.event.EventHandler} che definisce l'azione da
     * eseguire al click sulla card.
     * @return Un {@link VBox} che rappresenta la card di menu creata.
     */
    private VBox createMenuCard(String icon, String title, String description, String color, javafx.event.EventHandler<javafx.scene.input.MouseEvent> action) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setPrefWidth(250);
        card.setPrefHeight(180);
        card.setMaxWidth(250);
        card.setMaxHeight(180);

        // Stile base della card
        card.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-cursor: hand;"
        );

        // Icona
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        iconLabel.setTextFill(Color.web(color));

        // Titolo
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setAlignment(Pos.CENTER);

        // Descrizione
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        descLabel.setTextFill(Color.LIGHTGRAY);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        // Effetti hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #3b3b3b;" +
                            "-fx-background-radius: 15px;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-cursor: hand;" +
                            "-fx-scale-x: 1.05;" +
                            "-fx-scale-y: 1.05;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: #2b2b2b;" +
                            "-fx-background-radius: 15px;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-cursor: hand;" +
                            "-fx-scale-x: 1.0;" +
                            "-fx-scale-y: 1.0;"
            );
        });

        card.setOnMouseClicked(e -> {
            if (action != null) {
                action.handle(e);
            } else {
                System.err.println("❌ Action è null per card: " + title);
            }
        });

        return card;
    }

    /**
     * Crea e restituisce l'intestazione standard del pannello di amministrazione.
     * <p>
     * Questo metodo di utilità costruisce la sezione superiore del layout, che include
     * il titolo principale ("Gestione") e un sottotitolo descrittivo. Il layout
     * è semplice, organizzato in un {@link VBox} allineato a sinistra, e serve a
     * dare un'immediata indicazione all'utente che si trova nel pannello
     * amministrativo.
     * </p>
     *
     * @return Un {@link VBox} che rappresenta l'intestazione del pannello.
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⚙️ Gestione");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Pannello di amministrazione.");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.LIGHTGRAY);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    /**
     * ===================================
     * GESTIONE UTENTI
     * ===================================
     */

    /**
     * Crea e configura una {@link TableView} per visualizzare e gestire i dati degli utenti.
     * <p>
     * Questo metodo di utilità costruisce la tabella che elenca tutti gli utenti
     * registrati. Imposta colonne specifiche per ID, nome utente, email, nome completo e
     * stato, associando i dati del modello {@link User} a ciascuna colonna. Lo stile
     * della tabella è definito per integrarsi visivamente con il tema scuro del
     * pannello di amministrazione.
     * </p>
     *
     * <h3>Colonne della tabella:</h3>
     * <ul>
     * <li><b>ID:</b> L'identificatore univoco dell'utente.</li>
     * <li><b>Username:</b> Il nome utente per l'accesso.</li>
     * <li><b>Email:</b> L'indirizzo email dell'utente.</li>
     * <li><b>Nome Completo:</b> Combina nome e cognome dell'utente.</li>
     * <li><b>Stato:</b> Indica lo stato attuale dell'account (es. "Attivo").</li>
     * </ul>
     *
     * @return Un {@link VBox} che contiene la tabella degli utenti con il relativo titolo.
     * @see User
     * @see TableView
     */
    private VBox createUsersTable() {
        VBox container = new VBox(10);

        Label tableTitle = new Label("📋 Utenti Registrati");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.WHITE);

        usersTable = new TableView<>();
        usersTable.setItems(usersData);
        usersTable.setPrefHeight(400);

        // Stile tabella
        usersTable.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #444;" +
                        "-fx-border-width: 1;"
        );

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String id = user.getId();
            return new javafx.beans.property.SimpleStringProperty(id != null ? id : "N/A");
        });
        idCol.setPrefWidth(80);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String username = user.getUsername();
            return new javafx.beans.property.SimpleStringProperty(username != null ? username : "N/A");
        });
        usernameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String email = user.getEmail();
            return new javafx.beans.property.SimpleStringProperty(email != null ? email : "N/A");
        });
        emailCol.setPrefWidth(250);

        TableColumn<User, String> nameCol = new TableColumn<>("Nome Completo");
        nameCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String fullName = "";
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                fullName += user.getName().trim();
            }
            if (user.getSurname() != null && !user.getSurname().trim().isEmpty()) {
                if (!fullName.isEmpty()) fullName += " ";
                fullName += user.getSurname().trim();
            }
            if (fullName.isEmpty()) {
                fullName = "N/A";
            }
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });
        nameCol.setPrefWidth(150);

        TableColumn<User, String> statusCol = new TableColumn<>("Stato");
        statusCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("Attivo");
        });
        statusCol.setPrefWidth(100);

        usersTable.getColumns().addAll(idCol, usernameCol, emailCol, nameCol, statusCol);

        container.getChildren().addAll(tableTitle, usersTable);
        return container;
    }

    /**
     * Orchestra la visualizzazione della sezione di gestione degli utenti.
     * <p>
     * Questo metodo è responsabile di aggiornare il pannello di amministrazione per mostrare
     * l'interfaccia di gestione degli utenti. Esegue i seguenti passaggi:
     * </p>
     * <ol>
     * <li>Cancella il contenuto esistente dal pannello principale.</li>
     * <li>Crea e aggiunge una nuova intestazione specifica per la sezione.</li>
     * <li>Crea e aggiunge una barra degli strumenti dedicata agli utenti.</li>
     * <li>Inizializza il contenitore dei contenuti e la tabella degli utenti.</li>
     * <li>Aggiunge una barra di stato per fornire feedback all'utente.</li>
     * <li>Avvia il caricamento asincrono dei dati degli utenti dal servizio.</li>
     * </ol>
     * <p>
     * Questo metodo garantisce che la transizione tra le diverse sezioni del pannello
     * amministrativo avvenga in modo pulito e strutturato.
     * </p>
     *
     * @see #createHeader()
     * @see #createUsersToolbar()
     * @see #createUsersTable()
     * @see #createStatusBar()
     * @see #loadUsers()
     */
    private void showUsersManagement() {
        System.out.println("🔄 Passaggio a gestione utenti...");

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            // Header
            VBox header = createHeader();

            // Toolbar per utenti
            HBox toolbar = createUsersToolbar();

            // Contenuto utenti
            currentContent = new VBox(20);
            VBox tableContainer = createUsersTable();
            currentContent.getChildren().add(tableContainer);

            // Status bar
            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);

            // Carica dati utenti
            loadUsers();
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Crea e restituisce la barra degli strumenti per la gestione degli utenti.
     * <p>
     * Questo metodo costruisce un {@link HBox} che funge da barra degli strumenti per la
     * sezione di gestione degli utenti. Contiene pulsanti per le azioni comuni come
     * tornare al menu principale, aggiornare la tabella degli utenti ed eliminare
     * un utente selezionato. Ogni pulsante è stilizzato con colori specifici e un
     * gestore di eventi (`setOnAction`) che invoca il metodo appropriato.
     * Il layout include anche uno spacer (`Region`) per allineare gli elementi
     * e un'etichetta che identifica la sezione corrente.
     * </p>
     *
     * @return Un {@link HBox} che rappresenta la barra degli strumenti degli utenti.
     * @see #backToMainMenu()
     * @see #loadUsers()
     * @see #deleteSelectedUser()
     * @see #styleButton(Button, String)
     */
    private HBox createUsersToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button backButton = new Button("⬅️ Torna al Menu");
        styleButton(backButton, "#95a5a6");
        backButton.setOnAction(e -> backToMainMenu());

        Button refreshButton = new Button("🔄 Aggiorna");
        styleButton(refreshButton, "#4a86e8");
        refreshButton.setOnAction(e -> loadUsers());

        Button deleteButton = new Button("🗑️ Elimina Selezionato");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> deleteSelectedUser());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sectionLabel = new Label("👥 Gestione Utenti");
        sectionLabel.setTextFill(Color.WHITE);
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        toolbar.getChildren().addAll(backButton, refreshButton, deleteButton, spacer, sectionLabel);
        return toolbar;
    }

    /**
     * Carica in modo asincrono la lista di tutti gli utenti dal servizio di amministrazione.
     * <p>
     * Questo metodo avvia una richiesta non bloccante al servizio {@link AdminService}
     * per recuperare i dati di tutti gli utenti registrati. L'uso di
     * assicura che il thread dell'interfaccia utente
     * rimanga reattivo durante l'operazione di rete.
     * </p>
     * <p>
     * Il metodo gestisce sia il successo che il fallimento della richiesta.
     * Se la chiamata ha successo, la {@link ObservableList} {@code usersData}
     * viene aggiornata con i nuovi dati e una notifica di successo viene mostrata
     * tramite {@link #statusLabel}. In caso di errore (sia dal servizio che di
     * connessione), un messaggio di errore viene visualizzato all'utente,
     * fornendo un feedback chiaro sul problema.
     * </p>
     *
     * @see AdminService#getAllUsersAsync(String)
     * @see Platform#runLater(Runnable)
     * @see #statusLabel
     * @see #usersData
     * @see #showAlert(String, String)
     */
    private void loadUsers() {
        statusLabel.setText("🔄 Caricamento utenti...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.getAllUsersAsync(adminEmail)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess() && response.getUsers() != null) {
                        usersData.clear();
                        usersData.addAll(response.getUsers());

                        statusLabel.setText("✅ Caricati " + response.getUsers().size() + " utenti");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Errore: " + response.getMessage());
                        statusLabel.setTextFill(Color.RED);

                        showAlert("Errore", "Impossibile caricare gli utenti: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Gestisce il processo di eliminazione di un utente selezionato dalla tabella.
     * <p>
     * Questo metodo esegue una serie di controlli e richiede una conferma prima di procedere
     * con l'eliminazione. I passaggi includono:
     * </p>
     * <ol>
     * <li>Verifica se un utente è stato effettivamente selezionato nella tabella.</li>
     * <li>Mostra una finestra di dialogo di conferma con i dettagli dell'utente per prevenire
     * eliminazioni accidentali.</li>
     * <li>Se l'utente conferma, chiama un metodo privato per eseguire la richiesta di
     * eliminazione al servizio di backend.</li>
     * </ol>
     * <p>
     * Se l'utente non è selezionato o i dati sono incompleti, viene visualizzato un
     * messaggio di avviso per guidare l'amministratore.
     * </p>
     *
     * @see #performDeleteUser(User)
     * @see #showAlert(String, String)
     */
    private void deleteSelectedUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("Attenzione", "Seleziona un utente da eliminare");
            return;
        }

        System.out.println("   ELIMINAZIONE--> Utente selezionato:");
        System.out.println("   ID: " + selectedUser.getId());
        System.out.println("   Username: " + selectedUser.getUsername());
        System.out.println("   Email: " + selectedUser.getEmail());
        System.out.println("   Nome: " + selectedUser.getName());
        System.out.println("   Cognome: " + selectedUser.getSurname());

        // Verifica che l'ID non sia null o vuoto
        if (selectedUser.getId() == null || selectedUser.getId().trim().isEmpty()) {
            showAlert("Errore", "ID utente non valido. Aggiorna la lista e riprova.");
            return;
        }

        // Conferma eliminazione
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminare l'utente selezionato?");
        confirmAlert.setContentText(
                "Stai per eliminare:\n" +
                        "ID: " + selectedUser.getId() + "\n" +
                        "Username: " + selectedUser.getUsername() + "\n" +
                        "Email: " + selectedUser.getEmail() + "\n\n" +
                        "Questa operazione non può essere annullata."
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeleteUser(selectedUser);
        }
    }

    /**
     * Esegue l'operazione di eliminazione di un utente a livello di servizio.
     * <p>
     * Questo metodo invia una richiesta asincrona al servizio di amministrazione per
     * eliminare un utente specifico. Utilizza {@link CompletableFuture} per non bloccare
     * l'interfaccia utente. Dopo la ricezione della risposta, aggiorna la UI
     * con un messaggio di stato appropriato. Se l'eliminazione ha successo,
     * rimuove l'utente dalla {@link ObservableList} locale per aggiornare la tabella.
     * In caso di errore (sia dal servizio che di connessione), visualizza un
     * messaggio di errore chiaro all'utente.
     * </p>
     *
     * @param user L'oggetto {@link User} da eliminare.
     * @see AdminService#deleteUserAsync(String, String)
     * @see #statusLabel
     * @see #usersData
     * @see #showAlert(String, String)
     */
    private void performDeleteUser(User user) {
        statusLabel.setText("🗑️ Eliminazione in corso...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.deleteUserAsync(String.valueOf(user.getId()), adminEmail)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        usersData.remove(user);
                        statusLabel.setText("✅ Utente eliminato con successo");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Eliminazione fallita");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile eliminare l'utente: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * ===================================
     * GESTIONE LIBRI
     * ===================================
     */

    /**
     * Crea e configura una {@link TableView} per visualizzare e gestire i dati dei libri.
     * <p>
     * Questo metodo di utilità costruisce la tabella che elenca tutti i libri nel catalogo.
     * Imposta colonne specifiche per vari dettagli dei libri, inclusa una colonna personalizzata
     * per visualizzare le copertine. Le colonne sono associate ai campi del modello {@link Book}
     * per un'efficace visualizzazione dei dati. Lo stile della tabella è definito per
     * integrarsi con il tema scuro dell'interfaccia di amministrazione.
     * </p>
     *
     * <h3>Colonne della tabella:</h3>
     * <ul>
     * <li><b>Copertina:</b> Anteprima visiva della copertina del libro.</li>
     * <li><b>ISBN:</b> Il codice ISBN univoco del libro.</li>
     * <li><b>Titolo:</b> Il titolo del libro.</li>
     * <li><b>Autore:</b> L'autore del libro.</li>
     * <li><b>Anno:</b> L'anno di pubblicazione.</li>
     * <li><b>Categoria:</b> La categoria di appartenenza del libro.</li>
     * </ul>
     *
     * @return Un {@link VBox} che contiene la tabella dei libri con il relativo titolo.
     * @see Book
     * @see TableView
     * @see #loadCoverPreview(Book, javafx.scene.image.ImageView)
     */
    private void createBooksTable() {
        booksTable = new TableView<>();
        booksTable.setItems(booksData);
        booksTable.setPrefHeight(400);

        // Stile tabella
        booksTable.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #444;" +
                        "-fx-border-width: 1;"
        );

        TableColumn<Book, String> coverCol = new TableColumn<>("Copertina");
        coverCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("preview");
        });
        coverCol.setCellFactory(col -> new TableCell<Book, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(30);
                imageView.setFitHeight(45);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Book book = getTableRow().getItem();
                if (book != null) {
                    loadCoverPreview(book, imageView);
                    setGraphic(imageView);
                }
            }
        });
        coverCol.setPrefWidth(60);
        coverCol.setSortable(false);

        // Colonna ISBN
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String isbn = book.getIsbn();
            return new javafx.beans.property.SimpleStringProperty(isbn != null ? isbn : "N/A");
        });
        isbnCol.setPrefWidth(120);

        // Colonna Titolo
        TableColumn<Book, String> titleCol = new TableColumn<>("Titolo");
        titleCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String title = book.getTitle();
            return new javafx.beans.property.SimpleStringProperty(title != null ? title : "N/A");
        });
        titleCol.setPrefWidth(180);

        // Colonna Autore
        TableColumn<Book, String> authorCol = new TableColumn<>("Autore");
        authorCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String author = book.getAuthor();
            return new javafx.beans.property.SimpleStringProperty(author != null ? author : "N/A");
        });
        authorCol.setPrefWidth(130);

        // Colonna Anno
        TableColumn<Book, String> yearCol = new TableColumn<>("Anno");
        yearCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String year = book.getPublishYear();
            return new javafx.beans.property.SimpleStringProperty(year != null ? year : "N/A");
        });
        yearCol.setPrefWidth(70);

        // Colonna Categoria
        TableColumn<Book, String> categoryCol = new TableColumn<>("Categoria");
        categoryCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            String category = book.getCategory();
            return new javafx.beans.property.SimpleStringProperty(category != null ? category : "N/A");
        });
        categoryCol.setPrefWidth(100);

        booksTable.getColumns().addAll(coverCol, isbnCol, titleCol, authorCol, yearCol, categoryCol);
    }

    /**
     * Carica in modo asincrono l'intero catalogo di libri dal servizio di amministrazione.
     * <p>
     * Questo metodo avvia una richiesta non bloccante al servizio {@link AdminService} per
     * recuperare tutti i libri presenti nel database. L'utilizzo di {@link CompletableFuture}
     * garantisce che l'interfaccia utente rimanga reattiva durante l'operazione di rete.
     * </p>
     * <p>
     * Il metodo gestisce sia il successo che il fallimento della richiesta. In caso di successo,
     * i dati vengono salvati nella lista locale {@code allBooksData} e poi copiati in
     * {@code booksData} per la visualizzazione nella tabella. Infine, aggiorna il
     * messaggio di stato e la UI per riflettere il risultato dell'operazione. Se si verifica
     * un errore, sia dal servizio che di connessione, un messaggio di errore viene
     * visualizzato per fornire un feedback chiaro all'amministratore.
     * </p>
     *
     * @see AdminService#getAllBooksAsync(String)
     * @see Platform#runLater(Runnable)
     * @see #statusLabel
     * @see #booksData
     * @see #allBooksData
     * @see #showAlert(String, String)
     */
    private void loadBooks() {
        statusLabel.setText("📚 Caricamento libri...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.getAllBooksAsync(adminEmail)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        allBooksData.clear();
                        if (response.getBooks() != null) {
                            allBooksData.addAll(response.getBooks());
                        }

                        booksData.clear();
                        booksData.addAll(allBooksData);

                        updateResultsInfo();

                        statusLabel.setText("✅ " + allBooksData.size() + " libri caricati");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Errore: " + response.getMessage());
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile caricare i libri: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Gestisce il processo di eliminazione di un libro selezionato dalla tabella.
     * <p>
     * Questo metodo esegue una serie di controlli per garantire che l'operazione sia valida.
     * I passaggi principali includono:
     * </p>
     * <ol>
     * <li>Verifica se un libro è stato selezionato dall'utente nella tabella.</li>
     * <li>Controlla se l'ISBN del libro selezionato è valido.</li>
     * <li>Mostra una finestra di dialogo di conferma per prevenire l'eliminazione accidentale,
     * fornendo un riepilogo dei dettagli del libro.</li>
     * <li>Se l'utente conferma l'operazione, invoca un metodo per eseguire l'eliminazione
     * a livello di servizio.</li>
     * </ol>
     * <p>
     * Se la selezione è mancante o i dati non sono validi, un messaggio di avviso viene
     * mostrato all'utente.
     * </p>
     *
     * @see #performDeleteBook(Book)
     * @see #showAlert(String, String)
     */
    private void deleteSelectedBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert("Attenzione", "Seleziona un libro da eliminare");
            return;
        }

        if (selectedBook.getIsbn() == null || selectedBook.getIsbn().trim().isEmpty()) {
            showAlert("Errore", "ISBN libro non valido. Aggiorna la lista e riprova.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminare il libro selezionato?");
        confirmAlert.setContentText(
                "Stai per eliminare:\n" +
                        "ISBN: " + selectedBook.getIsbn() + "\n" +
                        "Titolo: " + selectedBook.getTitle() + "\n" +
                        "Autore: " + selectedBook.getAuthor() + "\n\n" +
                        "Questa operazione non può essere annullata."
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeleteBook(selectedBook);
        }
    }

    /**
     * Esegue l'operazione di eliminazione di un libro a livello di servizio.
     * <p>
     * Questo metodo invia una richiesta asincrona al servizio di amministrazione per
     * eliminare un libro specifico. Utilizza {@link java.util.concurrent.CompletableFuture}
     * per non bloccare l'interfaccia utente durante la comunicazione con il backend.
     * </p>
     * <p>
     * Una volta completata la richiesta, il metodo gestisce la risposta, aggiornando
     * la UI con un messaggio di stato appropriato. Se l'eliminazione ha successo,
     * il libro viene rimosso dalla lista locale {@code booksData} per riflettere
     * immediatamente la modifica nella tabella. In caso di errore, sia dal servizio
     * che di connessione, viene visualizzato un avviso all'utente.
     * </p>
     *
     * @param book L'oggetto {@link Book} da eliminare.
     * @see org.BABO.client.service.AdminService#deleteBookAsync(String, String)
     * @see #statusLabel
     * @see #booksData
     * @see #showAlert(String, String)
     */
    private void performDeleteBook(Book book) {
        statusLabel.setText("🗑️ Eliminazione in corso...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        adminService.deleteBookAsync(adminEmail, book.getIsbn())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        booksData.remove(book);
                        statusLabel.setText("✅ Libro eliminato con successo");
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                    } else {
                        statusLabel.setText("❌ Eliminazione fallita");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile eliminare il libro: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Mostra una finestra di dialogo modale per l'aggiunta di un nuovo libro.
     * <p>
     * Questo metodo crea e gestisce un'interfaccia utente interattiva per l'inserimento
     * dei dati di un nuovo libro. La finestra di dialogo include campi di testo per
     * le informazioni principali del libro (ISBN, titolo, autore, descrizione, anno, categoria)
     * e un selettore di file per caricare un'immagine di copertina.
     * </p>
     *
     * <h3>Caratteristiche principali:</h3>
     * <ul>
     * <li><b>Validazione Immagine:</b> Verifica che l'immagine di copertina selezionata sia valida
     * e che le sue proporzioni (larghezza:altezza) siano comprese tra 1:1 e 1:2.</li>
     * <li><b>Anteprima Copertina:</b> Mostra una piccola anteprima dell'immagine selezionata.</li>
     * <li><b>Salvataggio Copertina:</b> Salva l'immagine di copertina nella directory locale
     * con un nome file basato sull'ISBN per un'organizzazione coerente.</li>
     * <li><b>Validazione Dati:</b> Prima di inviare i dati al servizio, verifica che
     * i campi obbligatori (ISBN, titolo, autore) non siano vuoti.</li>
     * <li><b>Gestione Asincrona:</b> L'inserimento del nuovo libro nel database avviene
     * tramite una chiamata asincrona a {@code addNewBook}.</li>
     * </ul>
     *
     * @see #addNewBook(java.util.Map)
     * @see #saveCoverImageWithDebug(java.io.File, String)
     * @see javafx.scene.control.Dialog
     * @see javafx.stage.FileChooser
     */
    private void showAddBookDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Aggiungi Nuovo Libro");
        dialog.setHeaderText("Inserisci i dettagli del nuovo libro");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");
        TextField titleField = new TextField();
        titleField.setPromptText("Titolo");
        TextField authorField = new TextField();
        authorField.setPromptText("Autore");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Descrizione");
        descriptionField.setPrefRowCount(3);
        TextField yearField = new TextField();
        yearField.setPromptText("Anno pubblicazione");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Categoria");

        HBox coverBox = new HBox(10);
        coverBox.setAlignment(Pos.CENTER_LEFT);

        ImageView coverPreview = new ImageView();
        coverPreview.setFitWidth(60);
        coverPreview.setFitHeight(90);
        coverPreview.setPreserveRatio(true);
        coverPreview.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

        Button selectCoverButton = new Button("📁 Seleziona Copertina");
        styleButton(selectCoverButton, "#3498db");

        Label coverStatus = new Label("Nessuna immagine selezionata");
        coverStatus.setTextFill(Color.GRAY);
        coverStatus.setFont(Font.font("System", 10));

        class CoverFileHolder {
            File selectedFile = null;
        }
        final CoverFileHolder coverHolder = new CoverFileHolder();

        selectCoverButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleziona Copertina Libro");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );

            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                try {
                    // Carica e verifica l'immagine
                    Image image = new Image(new FileInputStream(file));

                    if (image.isError()) {
                        showAlert("Errore", "Impossibile caricare l'immagine selezionata.");
                        return;
                    }

                    // Verifica le proporzioni
                    double width = image.getWidth();
                    double height = image.getHeight();
                    double ratio = height / width;

                    // Controllo proporzioni
                    if (ratio < 1.0 || ratio > 2.0) {
                        showAlert("Proporzioni non valide",
                                String.format("L'immagine deve avere proporzioni nel range 1:1 e 1:2 (larghezza:altezza).\n" +
                                        "Proporzioni attuali: %.2f:1\n" +
                                        "Dimensioni: %.0fx%.0f", ratio, width, height));
                        return;
                    }

                    // Aggiorna preview
                    coverPreview.setImage(image);
                    coverHolder.selectedFile = file;
                    coverStatus.setText("✅ " + file.getName());
                    coverStatus.setTextFill(Color.GREEN);

                } catch (Exception ex) {
                    showAlert("Errore", "Errore durante il caricamento dell'immagine: " + ex.getMessage());
                }
            }
        });

        VBox coverContainer = new VBox(5);
        coverContainer.getChildren().addAll(coverBox, coverStatus);
        coverBox.getChildren().addAll(coverPreview, selectCoverButton);

        // Layout grid
        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(isbnField, 1, 0);
        grid.add(new Label("Titolo:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Autore:"), 0, 2);
        grid.add(authorField, 1, 2);
        grid.add(new Label("Descrizione:"), 0, 3);
        grid.add(descriptionField, 1, 3);
        grid.add(new Label("Anno:"), 0, 4);
        grid.add(yearField, 1, 4);
        grid.add(new Label("Categoria:"), 0, 5);
        grid.add(categoryField, 1, 5);
        grid.add(new Label("Copertina:"), 0, 6);
        grid.add(coverContainer, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Pulsanti
        ButtonType addButtonType = new ButtonType("Aggiungi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Validazione e conversione risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("isbn", isbnField.getText());
                result.put("title", titleField.getText());
                result.put("author", authorField.getText());
                result.put("description", descriptionField.getText());
                result.put("year", yearField.getText());
                result.put("category", categoryField.getText());

                if (coverHolder.selectedFile != null) {
                    try {
                        String targetFileName = saveCoverImageWithDebug(coverHolder.selectedFile, isbnField.getText());
                        result.put("coverFileName", targetFileName);
                    } catch (Exception ex) {
                        showAlert("Errore", "Errore durante il salvataggio della copertina: " + ex.getMessage());
                        return null;
                    }
                }

                return result;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();

        result.ifPresent(bookData -> {
            if (bookData.get("isbn").trim().isEmpty() ||
                    bookData.get("title").trim().isEmpty() ||
                    bookData.get("author").trim().isEmpty()) {
                showAlert("Errore", "ISBN, titolo e autore sono obbligatori");
                return;
            }

            addNewBook(bookData);
        });
    }

    /**
     * Salva in modo sicuro un'immagine di copertina del libro nella directory delle risorse.
     * <p>
     * Questo metodo di utilità esegue diverse operazioni critiche per il salvataggio
     * di un'immagine di copertina, garantendo che il file sia valido e che la directory
     * di destinazione esista. Il nome del file di destinazione viene creato a partire
     * dall'ISBN per garantire un'identificazione univoca.
     * </p>
     *
     * <h3>Passaggi operativi:</h3>
     * <ol>
     * <li>Valida l'ISBN per assicurarsi che sia presente e pulito da caratteri non validi.</li>
     * <li>Costruisce il percorso completo di destinazione basato su una directory di risorse predefinita.</li>
     * <li>Crea la directory di destinazione se non esiste già.</li>
     * <li>Esegue una serie di verifiche sul file sorgente, inclusa la sua esistenza e la dimensione massima (5MB).</li>
     * <li>Copia il file sorgente nel percorso di destinazione, sovrascrivendo qualsiasi file esistente con lo stesso nome.</li>
     * <li>Restituisce il nome del file di destinazione per l'uso nel modello dati.</li>
     * </ol>
     *
     * @param sourceFile Il {@link File} sorgente che rappresenta l'immagine di copertina selezionata.
     * @param isbn La stringa ISBN del libro, utilizzata per il nome del file di destinazione.
     * @return Il nome del file dell'immagine salvata (es. "9781234567890.jpg").
     * @throws IOException Se si verifica un errore durante il salvataggio, la verifica del file,
     * o se l'ISBN non è valido, il file sorgente non esiste, o supera il limite di dimensione.
     */
    private String saveCoverImageWithDebug(File sourceFile, String isbn) throws IOException {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IOException("ISBN non può essere vuoto");
        }

        String cleanIsbn = isbn.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        System.out.println("  ISBN pulito: '" + cleanIsbn + "'");

        if (cleanIsbn.isEmpty()) {
            throw new IOException("ISBN non contiene caratteri validi");
        }

        String targetFileName = cleanIsbn + ".jpg";
        System.out.println("  Nome file target: '" + targetFileName + "'");

        String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
        Path targetDir = Paths.get(resourcesPath);
        Path targetPath = targetDir.resolve(targetFileName);

        System.out.println("  Percorso completo: " + targetPath);

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            System.out.println("  ✅ Creata directory: " + targetDir);
        } else {
            System.out.println("  ✅ Directory già esistente");
        }

        try {
            if (!sourceFile.exists()) {
                throw new IOException("File sorgente non trovato: " + sourceFile.getAbsolutePath());
            }
            System.out.println("  ✅ File sorgente verificato");

            long fileSize = Files.size(sourceFile.toPath());
            System.out.println("  📏 Dimensione file: " + (fileSize / 1024) + " KB");

            if (fileSize > 5 * 1024 * 1024) {
                throw new IOException("File troppo grande (max 5MB). Dimensione: " + (fileSize / 1024 / 1024) + "MB");
            }

            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✅ File copiato con successo");

            if (!Files.exists(targetPath)) {
                throw new IOException("Errore durante la copia: file non creato");
            }

            System.out.println("  ✅ Verifica post-copia: file presente");
            System.out.println("  📁 File finale: " + targetPath.toAbsolutePath());

            return targetFileName;

        } catch (IOException e) {
            System.err.println("  ❌ Errore salvataggio copertina: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Errore durante il salvataggio della copertina: " + e.getMessage());
        }
    }

    /**
     * Inizializza la directory locale per la memorizzazione delle copertine dei libri.
     * <p>
     * Questo metodo di utilità garantisce che il percorso di sistema richiesto per
     * salvare e recuperare le immagini di copertina dei libri esista prima che l'applicazione
     * tenti di accedervi. Esegue i seguenti passaggi:
     * </p>
     * <ol>
     * <li>Costruisce il percorso completo della directory {@code books_covers} all'interno
     * della cartella delle risorse del progetto.</li>
     * <li>Verifica se la directory esiste; se non è presente, la crea, insieme a tutte le
     * directory genitrici necessarie.</li>
     * <li>Verifica la presenza di un'immagine segnaposto {@code placeholder.jpg} e
     * stampa un avviso se non viene trovata, poiché potrebbe essere necessaria per
     * gestire i libri senza copertina.</li>
     * </ol>
     * <p>
     * L'operazione è critica per il corretto funzionamento delle funzionalità di gestione dei libri.
     * </p>
     *
     * @throws IOException Se si verifica un errore durante la creazione della directory.
     * @see java.nio.file.Files#createDirectories(Path, java.nio.file.attribute.FileAttribute[])
     * @see java.nio.file.Paths#get(String, String...)
     */
    private void initializeBooksCoversDirectory() {
        try {
            String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
            Path targetDir = Paths.get(resourcesPath);

            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
                System.out.println("📁 Directory books_covers creata: " + targetDir);
            } else {
                System.out.println("📁 Directory books_covers esistente: " + targetDir);
            }

            Path placeholderPath = targetDir.resolve("placeholder.jpg");
            if (!Files.exists(placeholderPath)) {
                System.out.println("⚠️ Warning: placeholder.jpg non trovato in " + targetDir);
            }

        } catch (IOException e) {
            System.err.println("❌ Errore creazione directory books_covers: " + e.getMessage());
        }
    }

    /**
     * Carica un'immagine di copertina per un libro e la imposta su una {@link ImageView}.
     * <p>
     * Questo metodo di utilità gestisce il caricamento delle anteprime delle copertine dei libri
     * dalla directory locale. Funziona in questo modo:
     * </p>
     * <ol>
     * <li>Costruisce il percorso del file immagine basandosi sull'ISBN del libro.</li>
     * <li>Verifica se il file di copertina esiste in locale.</li>
     * <li>Se il file esiste, lo carica e lo imposta sulla {@link ImageView} fornita,
     * assicurandosi che l'immagine sia ridimensionata correttamente.</li>
     * <li>In caso di file non trovato, errore di caricamento o eccezione, il metodo
     * ripiega sul caricamento di un'immagine segnaposto per garantire la coerenza
     * dell'interfaccia utente.</li>
     * </ol>
     * <p>
     * L'uso di questa logica di fallback migliora la robustezza dell'applicazione,
     * evitando errori di visualizzazione se mancano le immagini.
     * </p>
     *
     * @param book L'oggetto {@link Book} per il quale si sta cercando la copertina.
     * @param imageView L'{@link ImageView} di destinazione su cui visualizzare l'immagine.
     * @see #loadPlaceholderImage(ImageView)
     * @see Book#getIsbn()
     */
    private void loadCoverPreview(Book book, ImageView imageView) {
        try {
            String coverFileName = null;
            if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
                String cleanIsbn = book.getIsbn().toUpperCase().replaceAll("[^A-Z0-9]", "");
                coverFileName = cleanIsbn + ".jpg";
            }

            if (coverFileName != null) {
                String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
                Path coverPath = Paths.get(resourcesPath, coverFileName);

                if (Files.exists(coverPath)) {
                    try (InputStream inputStream = Files.newInputStream(coverPath)) {
                        Image coverImage = new Image(inputStream, 30, 45, true, true);
                        if (!coverImage.isError()) {
                            imageView.setImage(coverImage);
                            return;
                        }
                    }
                }
            }

            loadPlaceholderImage(imageView);

        } catch (Exception e) {
            System.err.println("❌ Errore caricamento anteprima copertina: " + e.getMessage());
            loadPlaceholderImage(imageView);
        }
    }

    /**
     * Carica un'immagine segnaposto (placeholder) e la imposta su una {@link ImageView}.
     * <p>
     * Questo metodo di utilità viene utilizzato come fallback quando l'immagine di copertina
     * di un libro non è disponibile o non può essere caricata. Esegue i seguenti passaggi:
     * </p>
     * <ol>
     * <li>Costruisce il percorso del file {@code placeholder.jpg} all'interno della directory
     * delle copertine dei libri.</li>
     * <li>Verifica l'esistenza del file segnaposto.</li>
     * <li>Se il file esiste, lo carica e lo imposta sulla {@link ImageView} fornita,
     * con le dimensioni corrette.</li>
     * <li>Se il file segnaposto non è presente o si verifica un errore durante il caricamento,
     * viene creato un semplice segnaposto a livello di codice per garantire che l'interfaccia
     * utente non rimanga vuota.</li>
     * </ol>
     * <p>
     * Questo approccio migliora la robustezza dell'applicazione, gestendo in modo
     * elegante i casi in cui mancano le immagini.
     * </p>
     *
     * @param imageView L'{@link ImageView} di destinazione su cui visualizzare il segnaposto.
     * @see #createSimplePlaceholder(ImageView)
     */
    private void loadPlaceholderImage(ImageView imageView) {
        try {
            String resourcesPath = System.getProperty("user.dir") + "/client/src/main/resources/books_covers/";
            Path placeholderPath = Paths.get(resourcesPath, "placeholder.jpg");

            if (Files.exists(placeholderPath)) {
                try (InputStream inputStream = Files.newInputStream(placeholderPath)) {
                    Image placeholderImage = new Image(inputStream, 30, 45, true, true);
                    if (!placeholderImage.isError()) {
                        imageView.setImage(placeholderImage);
                        return;
                    }
                }
            }

            createSimplePlaceholder(imageView);

        } catch (Exception e) {
            createSimplePlaceholder(imageView);
        }
    }

    /**
     * Aggiunge un nuovo libro al catalogo tramite una chiamata asincrona al servizio.
     * <p>
     * Questo metodo orchestra il processo di inserimento di un nuovo libro nel database.
     * Avvia un'operazione non bloccante per inviare i dati del libro forniti
     * dalla finestra di dialogo a {@link AdminService}.
     * </p>
     * <p>
     * Il metodo gestisce il feedback all'utente tramite {@link #statusLabel}:
     * </p>
     * <ul>
     * <li>Mostra un messaggio "Aggiunta in corso" e aggiorna lo stato visivo.</li>
     * <li>Se l'operazione ha successo, visualizza un messaggio di conferma e richiama
     * {@link #loadBooks()} per aggiornare automaticamente la tabella dei libri.</li>
     * <li>In caso di errore (sia dal servizio che di connessione), visualizza un messaggio
     * di errore e un avviso.</li>
     * </ul>
     *
     * @param bookData Una {@link Map} contenente i dati del libro da aggiungere,
     * con chiavi come "isbn", "title", "author", ecc.
     * @see AdminService#addBookAsync(String, String, String, String, String, String, String)
     * @see #loadBooks()
     * @see #showAlert(String, String)
     */
    private void addNewBook(Map<String, String> bookData) {
        statusLabel.setText("📚 Aggiunta libro in corso...");
        statusLabel.setTextFill(Color.ORANGE);

        String adminEmail = authManager.getCurrentUser().getEmail();

        final String coverInfo;
        if (bookData.containsKey("coverFileName")) {
            coverInfo = " (con copertina: " + bookData.get("coverFileName") + ")";
        } else {
            coverInfo = "";
        }

        System.out.println("📚 Aggiunta libro: " + bookData.get("title") + coverInfo);

        adminService.addBookAsync(
                        adminEmail,
                        bookData.get("isbn"),
                        bookData.get("title"),
                        bookData.get("author"),
                        bookData.get("description"),
                        bookData.get("year"),
                        bookData.get("category")
                ).thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        statusLabel.setText("✅ Libro aggiunto con successo" + coverInfo);
                        statusLabel.setTextFill(Color.LIGHTGREEN);

                        loadBooks();

                    } else {
                        statusLabel.setText("❌ Aggiunta fallita");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Impossibile aggiungere il libro: " + response.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Errore di connessione");
                        statusLabel.setTextFill(Color.RED);
                        showAlert("Errore", "Errore di connessione: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Crea e restituisce la barra di stato dell'applicazione.
     * <p>
     * Questo metodo di utilità costruisce un {@link HBox} che funge da contenitore
     * per mostrare messaggi di stato all'utente. Contiene una singola etichetta,
     * {@link #statusLabel}, che viene aggiornata dinamicamente per fornire feedback
     * sullo stato delle operazioni in corso (es. caricamento dati, eliminazione
     * di elementi, messaggi di errore).
     * </p>
     * <p>
     * La barra di stato è un componente essenziale per l'esperienza utente,
     * in quanto fornisce un'immediata indicazione visiva del successo o del fallimento
     * di un'azione.
     * </p>
     *
     * @return Un {@link HBox} che rappresenta la barra di stato.
     * @see #statusLabel
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }

    /**
     * Orchestra la visualizzazione della sezione di gestione dei libri.
     * <p>
     * Questo metodo è responsabile di aggiornare il pannello di amministrazione per mostrare
     * l'interfaccia di gestione del catalogo dei libri. Esegue i seguenti passaggi:
     * </p>
     * <ol>
     * <li>Cancella il contenuto esistente dal pannello principale.</li>
     * <li>Crea e aggiunge un'intestazione e una barra degli strumenti dedicate ai libri.</li>
     * <li>Inizializza i componenti principali della gestione dei libri, inclusi un pulsante
     * per aggiungere nuovi libri, una barra di ricerca e la tabella dei libri.</li>
     * <li>Aggiunge una barra di stato per fornire feedback all'utente.</li>
     * <li>Avvia il caricamento asincrono dei dati dei libri per popolare la tabella.</li>
     * </ol>
     * <p>
     * Questo metodo garantisce che la transizione tra le diverse sezioni del pannello
     * amministrativo avvenga in modo pulito e strutturato, caricando i dati necessari
     * solo al momento opportuno.
     * </p>
     *
     * @see #createHeader()
     * @see #createBooksToolbar()
     * @see #createSearchBar()
     * @see #createBooksTable()
     * @see #showAddBookDialog()
     * @see #loadBooks()
     */
    private void showBooksManagement() {
        System.out.println("📄 Passaggio a gestione libri..."); // Debug

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            VBox header = createHeader();

            HBox toolbar = createBooksToolbar();

            currentContent = new VBox(20);
            VBox container = new VBox(10);

            Label tableTitle = new Label("📚 Gestione Libri");
            tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            tableTitle.setTextFill(Color.WHITE);

            Button addBookButton = new Button("➕ Aggiungi Nuovo Libro");
            styleButton(addBookButton, "#27ae60");
            addBookButton.setOnAction(e -> showAddBookDialog());

            HBox searchContainer = createSearchBar();

            createBooksTable();

            container.getChildren().addAll(tableTitle, addBookButton, searchContainer, booksTable);
            currentContent.getChildren().add(container);

            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);

            loadBooks();
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Crea e configura la barra di ricerca per il pannello di gestione dei libri.
     * <p>
     * Questo metodo di utilità costruisce un {@link HBox} che contiene tutti gli elementi
     * necessari per la funzionalità di ricerca: un'icona, un campo di testo, un pulsante
     * per cancellare la ricerca e un'etichetta per visualizzare il numero di risultati.
     * La logica di filtraggio è implementata tramite un listener sulla proprietà del testo
     * del campo di ricerca, che reagisce in tempo reale all'input dell'utente.
     * </p>
     *
     * <h3>Componenti:</h3>
     * <ul>
     * <li><b>Campo di Ricerca ({@link TextField}):</b> Consente all'utente di digitare i criteri
     * di ricerca. Include un listener che attiva il filtraggio dinamico della tabella.</li>
     * <li><b>Pulsante "❌":</b> Resetta il campo di ricerca e la visualizzazione della tabella.</li>
     * <li><b>Etichetta Risultati:</b> Mostra il numero di elementi trovati dopo la ricerca.</li>
     * </ul>
     *
     * @return Un {@link HBox} che rappresenta la barra di ricerca completa.
     * @see #filterBooks(String)
     * @see #updateResultsInfo()
     */
    private HBox createSearchBar() {
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(5, 0, 5, 0));

        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("System", 14));
        searchIcon.setTextFill(Color.LIGHTGRAY);

        searchField = new TextField();
        searchField.setPromptText("Cerca per ISBN, titolo, autore o categoria...");
        searchField.setPrefWidth(400);
        searchField.setStyle(
                "-fx-background-color: #3b3b3b; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #888; " +
                        "-fx-border-color: #555; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 8;"
        );

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBooks(newValue);
        });

        Button clearButton = new Button("❌");
        clearButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 3; " +
                        "-fx-background-radius: 3; " +
                        "-fx-padding: 5 8 5 8;"
        );
        clearButton.setOnAction(e -> {
            searchField.clear();
            filterBooks("");
        });

        Label resultsInfo = new Label();
        resultsInfo.setTextFill(Color.LIGHTGRAY);
        resultsInfo.setFont(Font.font("System", 11));
        updateResultsInfo(resultsInfo, 0, 0);

        searchContainer.getChildren().addAll(searchIcon, searchField, clearButton, resultsInfo);

        return searchContainer;
    }

    /**
     * Filtra la lista dei libri visualizzati nella tabella in base a una stringa di ricerca.
     * <p>
     * Questo metodo di utilità esegue un filtraggio dinamico della {@link ObservableList}
     * {@link #booksData} in base al testo inserito dall'utente nel campo di ricerca.
     * L'operazione è reattiva, attivandosi ogni volta che il testo del campo di ricerca
     * cambia.
     * </p>
     * <p>
     * La logica di filtraggio si basa su un'analisi non sensibile alle maiuscole/minuscole
     * che cerca corrispondenze tra la stringa di ricerca e i campi del libro come ISBN,
     * titolo, autore e categoria. Se la stringa di ricerca è vuota, la tabella viene
     * ripopolata con l'intera lista dei libri originali ({@link #allBooksData}).
     * </p>
     *
     * @param searchText La stringa di testo su cui basare il filtraggio.
     * @see #booksData
     * @see #allBooksData
     * @see #updateResultsInfo()
     */
    private void filterBooks(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            booksData.setAll(allBooksData);
        } else {
            String lowerSearchText = searchText.toLowerCase().trim();

            booksData.setAll(
                    allBooksData.stream()
                            .filter(book -> matchesSearch(book, lowerSearchText))
                            .collect(java.util.stream.Collectors.toList())
            );
        }

        updateResultsInfo();
    }

    /**
     * Verifica se un oggetto {@link Book} corrisponde a una stringa di ricerca.
     * <p>
     * Questo metodo di utilità esegue una ricerca non sensibile alle maiuscole/minuscole
     * attraverso vari campi di un libro. Viene utilizzato dal metodo di filtraggio
     * principale per determinare quali libri includere nei risultati della ricerca.
     * </p>
     *
     * <h3>Campi di ricerca:</h3>
     * <ul>
     * <li>ISBN</li>
     * <li>Titolo</li>
     * <li>Autore</li>
     * <li>Categoria</li>
     * <li>Anno di pubblicazione</li>
     * </ul>
     *
     * @param book L'oggetto {@link Book} da esaminare.
     * @param searchText La stringa di ricerca (già convertita in minuscolo e pulita).
     * @return {@code true} se almeno un campo del libro contiene la stringa di ricerca,
     * {@code false} altrimenti.
     * @see #filterBooks(String)
     */
    private boolean matchesSearch(Book book, String searchText) {
        if (book == null || searchText == null || searchText.isEmpty()) {
            return true;
        }

        // Cerca in ISBN
        if (book.getIsbn() != null &&
                book.getIsbn().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in titolo
        if (book.getTitle() != null &&
                book.getTitle().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in autore
        if (book.getAuthor() != null &&
                book.getAuthor().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in categoria
        if (book.getCategory() != null &&
                book.getCategory().toLowerCase().contains(searchText)) {
            return true;
        }

        // Cerca in anno (convertito a stringa)
        if (book.getPublishYear() != null &&
                book.getPublishYear().toLowerCase().contains(searchText)) {
            return true;
        }

        return false;
    }

    /**
     * Aggiorna in modo dinamico l'etichetta di stato che visualizza il numero di risultati di ricerca.
     * <p>
     * Questo metodo di utilità naviga attraverso la struttura dei nodi dell'interfaccia
     * utente per individuare l'etichetta dei risultati all'interno della barra di ricerca.
     * Una volta trovata, aggiorna il suo testo per mostrare il numero di libri
     * attualmente visualizzati nella tabella (dopo un'operazione di filtraggio)
     * rispetto al numero totale di libri disponibili.
     * </p>
     * <p>
     * Questo metodo garantisce che l'utente abbia sempre un feedback visivo immediato
     * sul risultato di una ricerca o di un aggiornamento.
     * </p>
     *
     * @see #booksData
     * @see #allBooksData
     * @see #createSearchBar()
     */
    private void updateResultsInfo() {
        if (currentContent != null && currentContent.getChildren().size() > 0) {
            VBox container = (VBox) currentContent.getChildren().get(0);
            for (javafx.scene.Node node : container.getChildren()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    if (hbox.getChildren().size() > 3) {
                        javafx.scene.Node lastNode = hbox.getChildren().get(hbox.getChildren().size() - 1);
                        if (lastNode instanceof Label) {
                            Label resultsLabel = (Label) lastNode;
                            updateResultsInfo(resultsLabel, booksData.size(), allBooksData.size());
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Aggiorna il testo di un'etichetta per mostrare il numero di risultati di ricerca.
     * <p>
     * Questo metodo è un'utilità usata per fornire un feedback visivo all'utente
     * dopo un'operazione di filtraggio. Se il numero di elementi mostrati è uguale
     * al numero totale di elementi, visualizza il totale; altrimenti, mostra il numero
     * di elementi filtrati rispetto al totale.
     * </p>
     *
     * @param resultsLabel L'etichetta {@link Label} da aggiornare.
     * @param shown Il numero di elementi attualmente visualizzati.
     * @param total Il numero totale di elementi disponibili.
     */
    private void updateResultsInfo(Label resultsLabel, int shown, int total) {
        if (shown == total) {
            resultsLabel.setText(total + " libri totali");
        } else {
            resultsLabel.setText(shown + " di " + total + " libri");
        }
    }

    /**
     * Crea e imposta un placeholder semplice quando non è disponibile un'immagine.
     * <p>
     * Questo metodo di utilità serve come fallback finale. Quando non è possibile
     * caricare un'immagine di copertina o il file segnaposto {@code placeholder.jpg},
     * questo metodo rimuove qualsiasi immagine esistente dalla {@link ImageView},
     * lasciandola vuota. Questo garantisce che non vengano visualizzate immagini
     * errate o non funzionanti, mantenendo la coerenza dell'interfaccia.
     * </p>
     *
     * @param imageView L'{@link ImageView} di destinazione.
     * @see #loadPlaceholderImage(ImageView)
     */
    private void createSimplePlaceholder(ImageView imageView) {
        imageView.setImage(null);
    }

    /**
     * Crea e restituisce la barra degli strumenti per la gestione dei libri.
     * <p>
     * Questo metodo costruisce un {@link HBox} che funge da barra degli strumenti per la
     * sezione di gestione del catalogo libri. Contiene pulsanti per le azioni comuni come
     * tornare al menu principale, aggiornare la tabella dei libri ed eliminare
     * un libro selezionato. Ogni pulsante è stilizzato con colori specifici e un
     * gestore di eventi (`setOnAction`) che invoca il metodo appropriato.
     * Il layout include anche uno spacer (`Region`) per allineare gli elementi
     * e un'etichetta che identifica la sezione corrente.
     * </p>
     *
     * @return Un {@link HBox} che rappresenta la barra degli strumenti dei libri.
     * @see #backToMainMenu()
     * @see #loadBooks()
     * @see #deleteSelectedBook()
     * @see #styleButton(javafx.scene.control.Button, String)
     */
    private HBox createBooksToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button backButton = new Button("⬅️ Torna al Menu");
        styleButton(backButton, "#95a5a6");
        backButton.setOnAction(e -> backToMainMenu());

        Button refreshButton = new Button("🔄 Aggiorna");
        styleButton(refreshButton, "#4a86e8");
        refreshButton.setOnAction(e -> loadBooks());

        Button deleteButton = new Button("🗑️ Elimina Selezionato");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> deleteSelectedBook());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sectionLabel = new Label("📚 Gestione Libri");
        sectionLabel.setTextFill(Color.WHITE);
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        toolbar.getChildren().addAll(backButton, refreshButton, deleteButton, spacer, sectionLabel);
        return toolbar;
    }

    /**
     * ==========================
     * GESTIONE RECENSIONI
     * ==========================
     */

    /**
     * Orchestra la visualizzazione della sezione di gestione delle recensioni.
     * <p>
     * Questo metodo è responsabile di aggiornare il pannello di amministrazione per mostrare
     * l'interfaccia di gestione delle recensioni. Esegue i seguenti passaggi:
     * </p>
     * <ol>
     * <li>Cancella il contenuto esistente dal pannello principale.</li>
     * <li>Crea e aggiunge un'intestazione e una barra degli strumenti dedicate alle recensioni.</li>
     * <li>Inizializza il contenitore dei contenuti e la tabella delle recensioni.</li>
     * <li>Aggiunge una barra di stato per fornire feedback all'utente.</li>
     * <li>Avvia il caricamento asincrono dei dati delle recensioni per popolare la tabella.</li>
     * </ol>
     * <p>
     * Questo metodo garantisce che la transizione tra le diverse sezioni del pannello
     * amministrativo avvenga in modo pulito e strutturato, caricando i dati necessari
     * solo al momento opportuno.
     * </p>
     *
     * @see #createHeader()
     * @see #createReviewsToolbar()
     * @see #createReviewsSearchBar()
     * @see #createReviewsTable()
     * @see #createStatusBar()
     * @see #loadReviewsData()
     */
    private void showReviewsManagement() {
        System.out.println("📄 Passaggio a gestione recensioni...");

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            VBox header = createHeader();

            HBox toolbar = createReviewsToolbar();

            currentContent = new VBox(20);
            VBox container = new VBox(10);

            Label tableTitle = new Label("⭐ Gestione Recensioni");
            tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            tableTitle.setTextFill(Color.WHITE);

            HBox searchContainer = createReviewsSearchBar();

            createReviewsTable();

            container.getChildren().addAll(tableTitle, searchContainer, reviewsTable);
            currentContent.getChildren().add(container);

            HBox statusBar = createStatusBar();

            mainAdminPanel.getChildren().addAll(header, toolbar, currentContent, statusBar);

            loadReviewsData();
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Crea e configura la barra di ricerca per il pannello di gestione delle recensioni.
     * <p>
     * Questo metodo di utilità costruisce un {@link HBox} che contiene tutti gli elementi
     * necessari per la funzionalità di ricerca: un'icona, un campo di testo reattivo
     * e un pulsante per cancellare la ricerca.
     * </p>
     *
     * <h3>Funzionalità:</h3>
     * <ul>
     * <li><b>Campo di Ricerca ({@link TextField}):</b> Consente all'utente di digitare i criteri
     * di ricerca. Un listener sul testo del campo attiva un filtraggio dinamico
     * della tabella delle recensioni in tempo reale.</li>
     * <li><b>Pulsante di cancellazione ("✕"):</b> Resetta il campo di ricerca e ripristina
     * la visualizzazione completa della tabella.</li>
     * </ul>
     *
     * @return Un {@link HBox} che rappresenta la barra di ricerca completa per le recensioni.
     * @see #filterReviews(String)
     * @see #reviewsSearchField
     * @see #styleButton(javafx.scene.control.Button, String)
     */
    private HBox createReviewsSearchBar() {
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(10, 0, 10, 0));

        Label searchLabel = new Label("🔍");
        searchLabel.setTextFill(Color.LIGHTGRAY);
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        reviewsSearchField = new TextField();
        reviewsSearchField.setPromptText("Cerca per utente, ISBN o contenuto recensione...");
        reviewsSearchField.setPrefWidth(300);
        reviewsSearchField.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #888888; " +
                        "-fx-border-color: #444444; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;"
        );

        reviewsSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReviews(newValue);
        });

        Button clearButton = new Button("✕");
        styleButton(clearButton, "#e74c3c");
        clearButton.setPrefWidth(30);
        clearButton.setOnAction(e -> {
            reviewsSearchField.clear();
            filterReviews("");
        });

        searchContainer.getChildren().addAll(searchLabel, reviewsSearchField, clearButton);
        return searchContainer;
    }


    /**
     * Filtra la lista delle recensioni visualizzate nella tabella in base a una stringa di ricerca.
     * <p>
     * Questo metodo di utilità esegue un filtraggio dinamico della tabella delle recensioni
     * (`reviewsTable`) in base al testo inserito dall'utente. La ricerca non è sensibile
     * alle maiuscole/minuscole e viene eseguita sui seguenti campi di ogni recensione:
     * </p>
     * <ul>
     * <li>Username dell'utente che ha scritto la recensione.</li>
     * <li>ISBN del libro recensito.</li>
     * <li>Contenuto della recensione.</li>
     * </ul>
     * <p>
     * Se la stringa di ricerca è nulla o vuota, la tabella viene ripristinata per mostrare
     * tutti i dati originali.
     * </p>
     *
     * @param searchText La stringa di testo su cui basare il filtraggio.
     * @see #reviewsTable
     * @see #reviewsData
     */
    private void filterReviews(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            reviewsTable.setItems(reviewsData);
        } else {
            ObservableList<BookRating> filteredData = FXCollections.observableArrayList();
            String lowerCaseFilter = searchText.toLowerCase();

            for (BookRating rating : reviewsData) {
                if ((rating.getUsername() != null && rating.getUsername().toLowerCase().contains(lowerCaseFilter)) ||
                        (rating.getIsbn() != null && rating.getIsbn().toLowerCase().contains(lowerCaseFilter)) ||
                        (rating.getReview() != null && rating.getReview().toLowerCase().contains(lowerCaseFilter))) {
                    filteredData.add(rating);
                }
            }
            reviewsTable.setItems(filteredData);
        }
    }

    /**
     * Crea e restituisce la barra degli strumenti per la gestione delle recensioni.
     * <p>
     * Questo metodo costruisce un {@link HBox} che funge da barra degli strumenti per la
     * sezione di gestione delle recensioni. Contiene pulsanti per le azioni comuni come
     * tornare al menu principale, aggiornare la tabella delle recensioni ed eliminare
     * una recensione selezionata. Ogni pulsante è stilizzato con colori specifici e un
     * gestore di eventi (`setOnAction`) che invoca il metodo appropriato.
     * Il layout include anche uno spacer (`Region`) per allineare gli elementi
     * e un'etichetta che identifica la sezione corrente.
     * </p>
     *
     * @return Un {@link HBox} che rappresenta la barra degli strumenti delle recensioni.
     * @see #backToMainMenu()
     * @see #loadReviewsData()
     * @see #deleteSelectedReview()
     * @see #styleButton(javafx.scene.control.Button, String)
     */
    private HBox createReviewsToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button backButton = new Button("⬅️ Torna al Menu");
        styleButton(backButton, "#95a5a6");
        backButton.setOnAction(e -> backToMainMenu());

        Button refreshButton = new Button("🔄 Aggiorna");
        styleButton(refreshButton, "#4a86e8");
        refreshButton.setOnAction(e -> loadReviewsData());

        Button deleteButton = new Button("🗑️ Elimina Selezionata");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> deleteSelectedReview());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sectionLabel = new Label("⭐ Gestione Recensioni");
        sectionLabel.setTextFill(Color.WHITE);
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        toolbar.getChildren().addAll(backButton, refreshButton, deleteButton, spacer, sectionLabel);
        return toolbar;
    }

    /**
     * Crea e configura una {@link TableView} per visualizzare le recensioni dei libri.
     * <p>
     * Questo metodo di utilità costruisce la tabella che elenca le recensioni disponibili.
     * La tabella è altamente personalizzata con celle e formattazione specifiche per
     * ogni colonna, offrendo una visualizzazione chiara e dettagliata delle informazioni.
     * </p>
     *
     * <h3>Colonne della tabella e loro funzionalità:</h3>
     * <ul>
     * <li><b>Utente:</b> Mostra l'username dell'utente che ha scritto la recensione.</li>
     * <li><b>ISBN:</b> Mostra il codice ISBN del libro recensito.</li>
     * <li><b>Voti:</b> Colonna personalizzata che visualizza i voti dettagliati (stile,
     * contenuto, piacevolezza, originalità, edizione). I voti non disponibili sono mostrati come 0.</li>
     * <li><b>Media:</b> Colonna personalizzata che calcola e visualizza la media dei voti,
     * con colori condizionali per evidenziare valutazioni positive, medie o negative.</li>
     * <li><b>Recensione:</b> Mostra un'anteprima del testo della recensione. Un tooltip
     * visualizza il testo completo al passaggio del mouse. Un click sulla cella apre
     * una finestra di dialogo modale con il testo completo.</li>
     * <li><b>Data:</b> Mostra la data della recensione.</li>
     * </ul>
     * <p>
     * La tabella supporta la selezione multipla e ha uno stile predefinito per integrarsi
     * con il tema dell'applicazione.
     * </p>
     *
     * @see #reviewsData
     */
    private void createReviewsTable() {
        reviewsTable = new TableView<>();
        reviewsTable.setItems(reviewsData);
        reviewsTable.setPrefHeight(500);
        reviewsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reviewsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        reviewsTable.setStyle(
                "-fx-selection-bar: #E0E0E0; " +
                        "-fx-selection-bar-non-focused: #F0F0F0; " +
                        "-fx-text-fill: black;"
        );

        // Colonna Username
        TableColumn<BookRating, String> usernameCol = new TableColumn<>("👤 Utente");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        // Colonna ISBN
        TableColumn<BookRating, String> isbnCol = new TableColumn<>("📚 ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(120);

        // Colonna Voti dettagliati
        TableColumn<BookRating, String> votesCol = new TableColumn<>("📊 Voti");
        votesCol.setPrefWidth(100);
        votesCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    BookRating rating = getTableView().getItems().get(getIndex());
                    String votesText = String.format("S:%d C:%d P:%d O:%d E:%d",
                            rating.getStyle() != null ? rating.getStyle() : 0,
                            rating.getContent() != null ? rating.getContent() : 0,
                            rating.getPleasantness() != null ? rating.getPleasantness() : 0,
                            rating.getOriginality() != null ? rating.getOriginality() : 0,
                            rating.getEdition() != null ? rating.getEdition() : 0);
                    setText(votesText);

                    // Colore condizionale per selezione
                    if (!isSelected()) {
                        setTextFill(Color.STEELBLUE);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        // Colonna Media voti
        TableColumn<BookRating, Double> averageCol = new TableColumn<>("⭐ Media");
        averageCol.setCellValueFactory(new PropertyValueFactory<>("average"));
        averageCol.setPrefWidth(80);
        averageCol.setCellFactory(col -> new TableCell<BookRating, Double>() {
            @Override
            protected void updateItem(Double average, boolean empty) {
                super.updateItem(average, empty);
                if (empty || average == null) {
                    setText("");
                } else {
                    setText(String.format("%.1f/5", average));

                    // Colore condizionale in base a valutazione
                    if (!isSelected()) {
                        if (average >= 4.0) {
                            setTextFill(Color.FORESTGREEN);
                        } else if (average >= 3.0) {
                            setTextFill(Color.GOLDENROD);
                        } else {
                            setTextFill(Color.CRIMSON);
                        }
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        // Colonna Recensione
        TableColumn<BookRating, String> reviewCol = new TableColumn<>("💬 Recensione");
        reviewCol.setCellValueFactory(new PropertyValueFactory<>("review"));
        reviewCol.setPrefWidth(250);
        reviewCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String reviewText, boolean empty) {
                super.updateItem(reviewText, empty);
                if (empty || reviewText == null || reviewText.trim().isEmpty()) {
                    setText("");
                    setOnMouseClicked(null);
                } else {
                    // Tronca il testo se è troppo lungo
                    String displayText = reviewText.length() > 40 ?
                            reviewText.substring(0, 37) + "..." : reviewText;
                    setText(displayText);
                    setTextFill(Color.BLACK);

                    setTooltip(new Tooltip(reviewText));

                    setOnMouseClicked(e -> {
                        if (e.getClickCount() == 1) { // Click singolo
                            showFullReviewDialog(reviewText, getTableView().getItems().get(getIndex()));
                        }
                    });

                    setStyle("-fx-cursor: hand;");
                }
            }
        });

        // Colonna Data
        TableColumn<BookRating, String> dateCol = new TableColumn<>("📅 Data");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("data"));
        dateCol.setPrefWidth(120);
        dateCol.setCellFactory(col -> new TableCell<BookRating, String>() {
            @Override
            protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("");
                } else {
                    setText(date.substring(0, Math.min(10, date.length())));
                    setTextFill(Color.BLACK);
                }
            }
        });

        reviewsTable.getColumns().addAll(usernameCol, isbnCol, votesCol, averageCol, reviewCol, dateCol);
    }

    /**
     * Mostra una finestra di dialogo modale con il testo completo di una recensione
     * e i dettagli dei voti.
     * <p>
     * Questo metodo crea e gestisce una finestra di dialogo informativa che si attiva
     * quando un utente clicca su una cella della colonna "Recensione" nella tabella.
     * Fornisce una visualizzazione espansa della recensione completa e include un
     * riepilogo dettagliato di tutti i voti assegnati (stile, contenuto, ecc.),
     * oltre alla media e alla data della recensione.
     * </p>
     * <p>
     * L'interfaccia utente della finestra di dialogo è progettata per essere chiara
     * e leggibile, con un'area di testo non modificabile per la recensione e un
     * riepilogo formattato dei voti.
     * </p>
     *
     * @param reviewText La stringa di testo completa della recensione.
     * @param rating L'oggetto {@link BookRating} che contiene tutti i dati della recensione,
     * inclusi i voti e le informazioni sul libro e sull'utente.
     * @see #createReviewsTable()
     */
    private void showFullReviewDialog(String reviewText, BookRating rating) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("📖 Recensione Completa");
        dialog.setHeaderText("Recensione di " + rating.getUsername() + " per ISBN: " + rating.getIsbn());

        TextArea textArea = new TextArea(reviewText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);

        String additionalInfo = String.format(
                "\n\n📊 Dettaglio Voti:\n" +
                        "• Stile: %d/5\n" +
                        "• Contenuto: %d/5\n" +
                        "• Piacevolezza: %d/5\n" +
                        "• Originalità: %d/5\n" +
                        "• Edizione: %d/5\n" +
                        "• Media: %.1f/5\n" +
                        "• Data: %s",
                rating.getStyle() != null ? rating.getStyle() : 0,
                rating.getContent() != null ? rating.getContent() : 0,
                rating.getPleasantness() != null ? rating.getPleasantness() : 0,
                rating.getOriginality() != null ? rating.getOriginality() : 0,
                rating.getEdition() != null ? rating.getEdition() : 0,
                rating.getAverage() != null ? rating.getAverage() : 0.0,
                rating.getData() != null ? rating.getData() : "N/A"
        );

        textArea.setText(reviewText + additionalInfo);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().setPrefSize(600, 400);

        dialog.showAndWait();
    }

    /**
     * Carica in modo asincrono i dati di tutte le recensioni dal servizio di amministrazione.
     * <p>
     * Questo metodo avvia un'operazione non bloccante per recuperare tutte le recensioni
     * dal backend, utilizzando il servizio {@link AdminService}. L'operazione è gestita
     * in modo asincrono per non bloccare il thread dell'interfaccia utente.
     * </p>
     * <p>
     * Una volta che la risposta dal server viene ricevuta, il metodo verifica l'esito.
     * Se l'operazione ha successo, la lista locale {@code reviewsData} viene aggiornata
     * con i dati delle recensioni, e la {@link #statusLabel} viene aggiornata con un messaggio
     * di successo. In caso di errore (sia di risposta dal server che di connessione),
     * un messaggio di errore viene visualizzato e viene invocato il metodo di fallback
     * {@link #loadFallbackReviewsData()} per gestire il fallimento.
     * </p>
     *
     * @see AdminService#getAllReviewsAsync(String)
     * @see Platform#runLater(Runnable)
     * @see #statusLabel
     * @see #reviewsData
     * @see #loadFallbackReviewsData()
     */
    private void loadReviewsData() {
        System.out.println("🔄 Caricamento recensioni dal database...");

        if (statusLabel != null) {
            statusLabel.setText("🔄 Caricamento recensioni...");
            statusLabel.setTextFill(Color.YELLOW);
        }

        reviewsData.clear();

        // Verifica che l'utente sia autenticato come admin
        String adminEmail = authManager.getCurrentUser() != null ?
                authManager.getCurrentUser().getEmail() : null;

        if (adminEmail == null) {
            if (statusLabel != null) {
                statusLabel.setText("❌ Errore: utente non autenticato");
                statusLabel.setTextFill(Color.RED);
            }
            loadFallbackReviewsData();
            return;
        }

        adminService.getAllReviewsAsync(adminEmail)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response != null && response.isSuccess() && response.getRatings() != null) {
                            List<BookRating> ratings = response.getRatings();

                            reviewsData.clear();
                            reviewsData.addAll(ratings);

                            if (statusLabel != null) {
                                statusLabel.setText("✅ Caricate " + reviewsData.size() + " recensioni");
                                statusLabel.setTextFill(Color.LIGHTGREEN);
                            }

                            System.out.println("✅ Caricate " + reviewsData.size() + " recensioni");
                        } else {
                            String error = response != null ? response.getMessage() : "Risposta nulla dal server";

                            if (statusLabel != null) {
                                statusLabel.setText("❌ Errore: " + error);
                                statusLabel.setTextFill(Color.RED);
                            }
                            System.err.println("❌ Errore nel caricamento recensioni: " + error);
                            loadFallbackReviewsData();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText("❌ Errore di connessione");
                            statusLabel.setTextFill(Color.RED);
                        }
                        System.err.println("❌ Errore di connessione nel caricamento recensioni: " + throwable.getMessage());
                        throwable.printStackTrace();
                        loadFallbackReviewsData();
                    });
                    return null;
                });
    }

    /**
     * Carica dei dati di recensione di esempio per il debug o come fallback.
     * <p>
     * Questo metodo di utilità popola la lista {@link #reviewsData} con un set
     * predefinito di oggetti {@link BookRating}. È inteso per essere utilizzato
     * in scenari in cui il caricamento dei dati reali dal server fallisce,
     * permettendo all'applicazione di visualizzare un'interfaccia non vuota
     * e di continuare a funzionare, facilitando il testing e il debug.
     * </p>
     * <p>
     * Dopo aver aggiunto i dati di esempio, aggiorna la {@link #statusLabel}
     * per informare l'utente che sono stati caricati dati fittizi.
     * </p>
     *
     * @see #reviewsData
     * @see #loadReviewsData()
     * @see #statusLabel
     */
    private void loadFallbackReviewsData() {
        System.out.println("🔄 Caricamento dati di esempio...");

        BookRating rating1 = new BookRating();
        rating1.setUsername("mario.rossi");
        rating1.setIsbn("978-0123456789");
        rating1.setStyle(5);
        rating1.setContent(4);
        rating1.setPleasantness(5);
        rating1.setOriginality(4);
        rating1.setEdition(4);
        rating1.setReview("Un capolavoro assoluto della letteratura italiana.");
        rating1.setData("2025-08-15");

        BookRating rating2 = new BookRating();
        rating2.setUsername("anna.verdi");
        rating2.setIsbn("978-0987654321");
        rating2.setStyle(4);
        rating2.setContent(4);
        rating2.setPleasantness(3);
        rating2.setOriginality(5);
        rating2.setEdition(3);
        rating2.setReview("Inquietante e profetico, un libro che fa riflettere.");
        rating2.setData("2025-08-12");

        BookRating rating3 = new BookRating();
        rating3.setUsername("luca.bianchi");
        rating3.setIsbn("978-1234567890");
        rating3.setStyle(3);
        rating3.setContent(5);
        rating3.setPleasantness(4);
        rating3.setOriginality(3);
        rating3.setEdition(4);
        rating3.setReview("Interessante ma un po' lento all'inizio.");
        rating3.setData("2025-08-10");

        reviewsData.addAll(rating1, rating2, rating3);

        if (statusLabel != null) {
            statusLabel.setText("⚠️ Dati di esempio caricati (" + reviewsData.size() + " recensioni)");
            statusLabel.setTextFill(Color.ORANGE);
        }
    }

    /**
     * Gestisce l'eliminazione di una o più recensioni selezionate dalla tabella.
     * <p>
     * Questo metodo esegue una serie di controlli prima di procedere con l'eliminazione.
     * I passaggi principali sono:
     * </p>
     * <ol>
     * <li>Verifica se almeno una recensione è stata selezionata. Se non lo è, mostra un avviso.</li>
     * <li>Mostra una finestra di dialogo di conferma per prevenire l'eliminazione accidentale.</li>
     * <li>Se l'utente conferma, avvia un'operazione asincrona per eliminare ogni recensione selezionata
     * tramite il servizio {@link AdminService#deleteRatingAsync(String, String, String)}.</li>
     * <li>Per ogni recensione, aggiorna il feedback all'utente e gestisce il successo o il fallimento
     * dell'operazione, rimuovendo la recensione dalla lista locale {@link #reviewsData} in caso di successo.</li>
     * </ol>
     * <p>
     * L'utilizzo di chiamate asincrone garantisce che l'interfaccia utente rimanga reattiva
     * anche durante l'interazione con il server.
     * </p>
     *
     * @see #reviewsTable
     * @see #reviewsData
     * @see #statusLabel
     * @see #showAlert(String, String)
     */
    private void deleteSelectedReview() {
        ObservableList<BookRating> selectedRatings = reviewsTable.getSelectionModel().getSelectedItems();

        if (selectedRatings.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("⚠️ Seleziona almeno una recensione da eliminare");
                statusLabel.setTextFill(Color.ORANGE);
            }
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("🗑️ Conferma Eliminazione");
        confirmAlert.setHeaderText("Eliminare le recensioni selezionate?");
        confirmAlert.setContentText("Vuoi eliminare " + selectedRatings.size() + " recensione/i?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            List<BookRating> toDelete = List.copyOf(selectedRatings);

            if (statusLabel != null) {
                statusLabel.setText("🔄 Eliminazione in corso...");
                statusLabel.setTextFill(Color.YELLOW);
            }

            String adminEmail = authManager.getCurrentUser() != null ?
                    authManager.getCurrentUser().getEmail() : null;

            if (adminEmail == null) {
                if (statusLabel != null) {
                    statusLabel.setText("❌ Errore: utente non autenticato");
                    statusLabel.setTextFill(Color.RED);
                }
                return;
            }

            for (BookRating rating : toDelete) {
                adminService.deleteRatingAsync(adminEmail, rating.getUsername(), rating.getIsbn())
                        .thenAccept(deleteResponse -> {
                            Platform.runLater(() -> {
                                if (deleteResponse.isSuccess()) {
                                    reviewsData.remove(rating);
                                    System.out.println("✅ Eliminata recensione: " + rating.getUsername() + " - " + rating.getIsbn());
                                } else {
                                    String error = deleteResponse.getMessage();
                                    System.err.println("❌ Errore eliminazione: " + error);
                                }
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                System.err.println("❌ Errore eliminazione recensione: " + throwable.getMessage());
                                if (statusLabel != null) {
                                    statusLabel.setText("❌ Errore durante l'eliminazione");
                                    statusLabel.setTextFill(Color.RED);
                                }
                            });
                            return null;
                        });
            }

            if (statusLabel != null) {
                statusLabel.setText("🗑️ Eliminazione completata");
                statusLabel.setTextFill(Color.LIGHTGREEN);
            }
        }
    }

    /**
     * Torna alla schermata del menu principale del pannello di amministrazione.
     * <p>
     * Questo metodo gestisce la transizione dall'interfaccia di gestione corrente (libri o recensioni)
     * al menu principale dell'applicazione. Cancella il contenuto esistente dal pannello principale
     * e lo ripopola con l'intestazione e il menu di navigazione, offrendo all'utente un modo
     * semplice e chiaro per tornare indietro.
     * </p>
     *
     * @see #mainAdminPanel
     * @see #createHeader()
     * @see #createAdminMenu()
     */
    private void backToMainMenu() {
        System.out.println("🔄 Tornando al menu principale...");

        if (mainAdminPanel != null) {
            mainAdminPanel.getChildren().clear();

            VBox header = createHeader();

            VBox menuContainer = createAdminMenu();

            mainAdminPanel.getChildren().addAll(header, menuContainer);
        } else {
            System.err.println("❌ mainAdminPanel è null!");
        }
    }

    /**
     * Applica uno stile CSS personalizzato a un pulsante e aggiunge un effetto hover.
     * <p>
     * Questo metodo di utilità serve per mantenere uno stile uniforme per i pulsanti
     * nell'intera applicazione. Imposta un colore di sfondo, il colore del testo,
     * il peso del carattere, il raggio dei bordi e un cursore a mano per indicare
     * che il pulsante è cliccabile. Aggiunge inoltre un effetto visivo che scurisce
     * leggermente il colore del pulsante al passaggio del mouse.
     * </p>
     *
     * @param button Il pulsante {@link Button} a cui applicare lo stile.
     * @param color Una stringa che rappresenta il codice esadecimale del colore di sfondo (es. "#3498db").
     */
    private void styleButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(color, "derive(" + color + ", 20%)"))
        );
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace("derive(" + color + ", 20%)", color))
        );
    }

    /**
     * Mostra una finestra di dialogo di avviso all'utente.
     * <p>
     * Questo metodo di utilità crea e visualizza una finestra di dialogo modale semplice
     * di tipo {@link Alert.AlertType#INFORMATION}. È utile per comunicare all'utente
     * messaggi importanti come avvisi, errori o conferme in un formato non intrusivo.
     * </p>
     *
     * @param title Il titolo della finestra di dialogo.
     * @param message Il messaggio di testo da visualizzare nella finestra.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}