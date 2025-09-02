package org.BABO.client.ui.Popup;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.BABO.client.service.AuthService;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.shared.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Gestore del popup per la visualizzazione e la gestione del profilo utente.
 * <p>
 * Questa classe è responsabile della creazione e della gestione dell'interfaccia
 * utente per il profilo dell'utente loggato. Genera un popup dinamico che mostra
 * i dettagli dell'utente, le sue statistiche, e offre opzioni come il logout
 * e la navigazione alla pagina del profilo.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 * <li><strong>Generazione UI:</strong> Crea un layout completo e reattivo per il profilo utente.</li>
 * <li><strong>Visualizzazione Dati:</strong> Popola il popup con i dati dell'utente loggato.</li>
 * <li><strong>Gestione Eventi:</strong> Gestisce le interazioni dell'utente, come il click sui pulsanti di logout e profilo.</li>
 * <li><strong>Integrazione:</strong> Interagisce con l'{@link AuthenticationManager} per accedere ai dati dell'utente e gestire il logout.</li>
 * </ul>
 *
 * <h3>Architettura del Popup:</h3>
 * <p>
 * Il popup è costruito come un overlay su un contenitore {@link StackPane} principale,
 * garantendo che venga visualizzato al di sopra di tutti gli altri contenuti.
 * Il suo layout è composto da una struttura a {@link VBox} scrollabile che contiene
 * diverse sezioni dedicate alle informazioni dell'utente.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see AuthenticationManager
 * @see PopupManager
 */
public class UserProfilePopup {

    private static final String BG_COLOR = "#1e1e1e";
    private static final String BG_CONTROL = "#2b2b2b";
    private static final String ACCENT_COLOR = "#4a86e8";
    private static final String TEXT_COLOR = "#ffffff";
    private static final String HINT_COLOR = "#9e9e9e";

    /** Il gestore dell'autenticazione. */
    private final AuthenticationManager authManager;
    /** La callback da eseguire al momento del logout. */
    private final Runnable onLogoutCallback;
    /** Il nodo radice del popup. */
    private StackPane root;

    /** Il client HTTP per le chiamate API. */
    private final HttpClient httpClient;

    /**
     * Costruttore per il popup del profilo utente.
     * <p>
     * Inizializza il popup con i gestori necessari per l'autenticazione
     * e la gestione degli eventi.
     * </p>
     *
     * @param authManager Il gestore dell'autenticazione.
     * @param onLogoutCallback La callback da eseguire al logout.
     */
    public UserProfilePopup(AuthenticationManager authManager, Runnable onLogoutCallback) {
        this.authManager = authManager;
        this.onLogoutCallback = onLogoutCallback;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Mostra il popup del profilo utente.
     * <p>
     * Questo metodo crea e visualizza il popup come un overlay sul contenitore
     * principale dell'applicazione. Prima di procedere, verifica che un utente
     * sia effettivamente loggato. Il popup è configurato per chiudersi
     * cliccando sullo sfondo semi-trasparente, prevenendo al contempo
     * la chiusura se si clicca direttamente sul suo contenuto.
     * Il layout e gli elementi del popup vengono creati dinamicamente
     * popolando i dati dell'utente corrente.
     * </p>
     *
     * @param mainRoot Il contenitore principale {@link StackPane} dell'interfaccia utente.
     */
    public void show(StackPane mainRoot) {
        root = mainRoot;

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("⚠️ Errore", "Nessun utente loggato");
            return;
        }

        // Crea overlay semi-trasparente
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // Crea contenuto popup
        VBox popupContent = createPopupContent(currentUser);

        // Centra il popup
        overlay.getChildren().add(popupContent);
        StackPane.setAlignment(popupContent, Pos.CENTER);

        // Chiudi popup cliccando sullo sfondo
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                closePopup();
            }
        });

        // Previeni chiusura cliccando sul contenuto
        popupContent.setOnMouseClicked(e -> e.consume());

        // Aggiungi al root principale
        mainRoot.getChildren().add(overlay);

        System.out.println("👤 Popup profilo utente aperto per: " + currentUser.getDisplayName());
    }

    /**
     * Crea il contenuto principale del popup del profilo utente.
     * <p>
     * Questo metodo costruisce l'intera struttura del popup, inclusi i layout,
     * le sezioni informative, le statistiche e i pulsanti di azione. Il contenuto
     * è organizzato all'interno di un {@link VBox} che viene reso scrollabile
     * tramite un {@link ScrollPane} per gestire i casi in cui i dati superano
     * lo spazio disponibile. Il metodo delega la creazione di singole sezioni
     * a metodi privati dedicati per mantenere la modularità del codice.
     * </p>
     *
     * @param user L'oggetto {@link User} i cui dati verranno visualizzati.
     * @return Il nodo {@link VBox} che rappresenta il contenuto completo del popup.
     */
    private VBox createPopupContent(User user) {
        VBox popup = new VBox();
        popup.setMaxWidth(420);
        popup.setMinWidth(350);
        popup.setPrefWidth(400);
        popup.setMaxHeight(600);
        popup.setMinHeight(500);
        popup.setPrefHeight(550);

        popup.setStyle(
                "-fx-background-color: transparent;" +  // lo sfondo lo mettiamo dentro scrollContent
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 8);"
        );

        // Contenuto scrollabile
        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(25));
        scrollContent.setFillWidth(true);

        // 🎨 Ripristina il colore scuro del popup
        scrollContent.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-background-radius: 15px;" +
                        "-fx-padding: 25px;"
        );

        // Sezioni
        HBox header = createHeader();
        VBox profileSection = createProfileSection(user);
        VBox detailsSection = createDetailsSection(user);
        VBox statsSection = createStatsSection();
        VBox actionsSection = createActionsSection();

        scrollContent.getChildren().addAll(header, profileSection, detailsSection, statsSection, actionsSection);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        popup.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return popup;
    }

    /**
     * Crea la sezione dell'intestazione del popup con titolo e pulsante di chiusura.
     * <p>
     * Questo metodo costruisce un nodo {@link HBox} che funge da intestazione
     * per il popup del profilo utente. L'intestazione è composta da:
     * <ul>
     * <li>Un'etichetta {@link Label} per il titolo "Il Mio Profilo".</li>
     * <li>Un {@link Region} che funge da spaziatore per allineare gli elementi.</li>
     * <li>Un pulsante {@link Button} di chiusura che, se premuto, invoca il
     * metodo {@link #closePopup()}.</li>
     * </ul>
     * La sezione è progettata per essere posizionata in cima al popup e fornisce
     * una via chiara per l'utente per chiudere la finestra.
     * </p>
     *
     * @return Il nodo {@link HBox} per l'intestazione del popup.
     * @see #closePopup()
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);

        Label title = new Label("👤 Il Mio Profilo");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(TEXT_COLOR));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #999999;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5px 10px;"
        );
        closeButton.setOnAction(e -> closePopup());

        header.getChildren().addAll(title, spacer, closeButton);
        return header;
    }

    /**
     * Crea la sezione principale del profilo utente con avatar, nome e dettagli di contatto.
     * <p>
     * Questo metodo costruisce un nodo {@link VBox} che organizza le informazioni
     * di base dell'utente in un formato visualmente accattivante. La sezione include:
     * <ul>
     * <li>Un avatar generato in base alle iniziali dell'utente.</li>
     * <li>Il nome completo dell'utente con una formattazione distintiva.</li>
     * <li>L'indirizzo email e l'username, formattati per chiarezza.</li>
     * </ul>
     * Questa sezione funge da intestazione visiva del profilo, fornendo un'identificazione
     * immediata dell'utente all'interno del popup.
     * </p>
     *
     * @param user L'oggetto {@link User} che contiene i dati da visualizzare.
     * @return Il nodo {@link VBox} che rappresenta la sezione del profilo.
     */
    private VBox createProfileSection(User user) {
        VBox profileSection = new VBox(15);
        profileSection.setAlignment(Pos.CENTER);

        // Avatar grande
        StackPane avatar = createLargeAvatar(user);

        // Nome completo
        Label fullName = new Label(user.getDisplayName());
        fullName.setFont(Font.font("System", FontWeight.BOLD, 24));
        fullName.setTextFill(Color.web(TEXT_COLOR));

        // Email
        Label email = new Label(user.getEmail());
        email.setFont(Font.font("System", 16));
        email.setTextFill(Color.web(HINT_COLOR));

        // Username
        Label username = new Label("@" + user.getUsername());
        username.setFont(Font.font("System", 14));
        username.setTextFill(Color.web(ACCENT_COLOR));

        profileSection.getChildren().addAll(avatar, fullName, email, username);
        return profileSection;
    }

    /**
     * Crea un avatar visivo per l'utente, composto da un grande cerchio e le iniziali dell'utente.
     * <p>
     * Questo metodo costruisce un nodo {@link StackPane} che serve come contenitore
     * per l'avatar. Al suo interno vengono aggiunti un cerchio colorato di grandi
     * dimensioni e un'etichetta {@link Label} che visualizza le iniziali dell'utente
     * in maiuscolo. L'avatar è stilizzato con colori e dimensioni predefinite
     * per garantire una rappresentazione coerente del profilo.
     * </p>
     *
     * @param user L'oggetto {@link User} da cui estrarre le iniziali.
     * @return Un nodo {@link StackPane} che rappresenta l'avatar completo.
     */
    private StackPane createLargeAvatar(User user) {
        StackPane avatar = new StackPane();
        avatar.setMaxSize(80, 80);
        avatar.setMinSize(80, 80);

        // Cerchio di sfondo
        Circle circle = new Circle(40);
        circle.setFill(Color.web(ACCENT_COLOR));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(3);

        // Iniziali
        String initials = getInitials(user);
        Label initialsLabel = new Label(initials);
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 28));

        avatar.getChildren().addAll(circle, initialsLabel);
        return avatar;
    }

    /**
     * Estrae e restituisce le iniziali dell'utente.
     * <p>
     * Questo metodo analizza il nome e il cognome dell'utente per creare una stringa
     * di iniziali. Se il nome o il cognome sono presenti, la prima lettera di ciascuno
     * viene estratta e aggiunta alla stringa delle iniziali. Se nessuno dei due è
     * disponibile, viene restituito un punto interrogativo ("?"). Le iniziali sono
     * sempre convertite in maiuscolo.
     * </p>
     *
     * @param user L'oggetto {@link User} contenente il nome e il cognome.
     * @return Una stringa con le iniziali dell'utente o "?" se non disponibili.
     */
    private String getInitials(User user) {
        String name = user.getName();
        String surname = user.getSurname();

        StringBuilder initials = new StringBuilder();

        if (name != null && !name.trim().isEmpty()) {
            initials.append(name.trim().charAt(0));
        }

        if (surname != null && !surname.trim().isEmpty()) {
            initials.append(surname.trim().charAt(0));
        }

        return initials.length() > 0 ? initials.toString().toUpperCase() : "?";
    }

    /**
     * Crea la sezione dei dettagli dell'utente, organizzandoli in una griglia.
     * <p>
     * Questo metodo costruisce un nodo {@link VBox} che raggruppa le informazioni
     * personali dell'utente, come nome, cognome, email e username. Le informazioni
     * sono disposte in una {@link GridPane} per un allineamento chiaro e ordinato.
     * Il metodo verifica anche la presenza del codice fiscale, visualizzandolo solo
     * se disponibile.
     * </p>
     *
     * @param user L'oggetto {@link User} che contiene i dati da visualizzare.
     * @return Il nodo {@link VBox} che rappresenta la sezione dei dettagli dell'account.
     */
    private VBox createDetailsSection(User user) {
        VBox detailsSection = new VBox(12);

        Label sectionTitle = new Label("📋 Informazioni Account");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_COLOR));

        // Griglia informazioni
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        // Nome
        addInfoRow(grid, 0, "Nome:", user.getName());
        addInfoRow(grid, 1, "Cognome:", user.getSurname());

        // Codice fiscale (se presente)
        if (user.getCf() != null && !user.getCf().trim().isEmpty()) {
            addInfoRow(grid, 2, "Codice Fiscale:", user.getCf());
        }

        addInfoRow(grid, 3, "Email:", user.getEmail());
        addInfoRow(grid, 4, "Username:", user.getUsername());

        detailsSection.getChildren().addAll(sectionTitle, grid);
        return detailsSection;
    }

    /**
     * Aggiunge una riga di informazione a una griglia.
     * <p>
     * Questo metodo di utilità crea una coppia di nodi {@link Label} per rappresentare
     * un'informazione (etichetta e valore) e li aggiunge a una {@link GridPane}
     * in una riga specificata. È progettato per semplificare la creazione di layout
     * a griglia per la visualizzazione di dati chiave, come le informazioni del profilo.
     * Se il valore fornito è nullo, viene visualizzato "Non specificato" per
     * garantire un output coerente.
     * </p>
     *
     * @param grid Il nodo {@link GridPane} a cui aggiungere la riga.
     * @param row L'indice della riga in cui inserire la coppia etichetta-valore.
     * @param label L'etichetta descrittiva del dato (es. "Nome:").
     * @param value Il valore da visualizzare (es. "Mario Rossi").
     */
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 13));
        labelNode.setTextFill(Color.web(HINT_COLOR));

        Label valueNode = new Label(value != null ? value : "Non specificato");
        valueNode.setFont(Font.font("System", 13));
        valueNode.setTextFill(Color.web(TEXT_COLOR));
        valueNode.setWrapText(true);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    /**
     * Crea la sezione delle statistiche di lettura dell'utente.
     * <p>
     * Questo metodo costruisce un nodo {@link VBox} che presenta le statistiche
     * di lettura dell'utente in un formato visuale a "card". Vengono inizialmente
     * create delle card segnaposto con valori generici ("...") che vengono poi
     * popolate in modo asincrono con i dati reali richiamando il metodo
     * {@link #loadUserStatistics(VBox, VBox, VBox)}. Questo approccio evita
     * di bloccare l'interfaccia utente durante il caricamento dei dati e
     * migliora la reattività dell'applicazione.
     * </p>
     *
     * @return Il nodo {@link VBox} che rappresenta la sezione delle statistiche.
     * @see #createStatCard(String, String, String)
     * @see #loadUserStatistics(VBox, VBox, VBox)
     */
    private VBox createStatsSection() {
        VBox statsSection = new VBox(12);

        Label sectionTitle = new Label("📊 Statistiche Lettura");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_COLOR));

        // Container per le statistiche che verranno caricate
        HBox statsGrid = new HBox(20);
        statsGrid.setAlignment(Pos.CENTER);

        // Crea le card con valori iniziali
        VBox stat1 = createStatCard("📚", "...", "Libri Salvati");
        VBox stat2 = createStatCard("💡", "...", "Libri Consigliati");
        VBox stat3 = createStatCard("⭐", "...", "Recensioni");

        statsGrid.getChildren().addAll(stat1, stat2, stat3);
        statsSection.getChildren().addAll(sectionTitle, statsGrid);

        // Carica le statistiche reali in background
        loadUserStatistics(stat1, stat2, stat3);

        return statsSection;
    }

    /**
     * Carica in modo asincrono le statistiche di lettura dell'utente dal server.
     * <p>
     * Questo metodo effettua tre chiamate API separate e asincrone per recuperare
     * il numero di libri salvati, raccomandazioni e recensioni dell'utente. Utilizza
     * {@link CompletableFuture} per eseguire ogni chiamata in un thread separato,
     * evitando di bloccare il thread dell'interfaccia utente. Una volta che i dati
     * sono stati recuperati con successo, il metodo {@link Platform#runLater(Runnable)}
     * è utilizzato per aggiornare l'interfaccia grafica in modo sicuro,
     * garantendo che le "card" delle statistiche visualizzino i valori corretti.
     * </p>
     *
     * @param booksCard La {@link VBox} che rappresenta la card dei libri.
     * @param recommendationsCard La {@link VBox} che rappresenta la card delle raccomandazioni.
     * @param reviewsCard La {@link VBox} che rappresenta la card delle recensioni.
     */
    private void loadUserStatistics(VBox booksCard, VBox recommendationsCard, VBox reviewsCard) {
        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) return;

        String username = currentUser.getUsername();

        // 1. Carica numero libri (stesso pattern di AuthService)
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/library/stats/" + username))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractNumberFromMessage(response.body(), "Libri totali: ");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento statistiche libri: " + e.getMessage());
            }
            return 0;
        }).thenAccept(count -> {
            Platform.runLater(() -> updateStatCard(booksCard, String.valueOf(count)));
        });

        // 2. Carica numero raccomandazioni
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/recommendations/stats/" + username))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractNumberFromMessage(response.body(), "Raccomandazioni totali: ");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento statistiche raccomandazioni: " + e.getMessage());
            }
            return 0;
        }).thenAccept(count -> {
            Platform.runLater(() -> updateStatCard(recommendationsCard, String.valueOf(count)));
        });

        // 3. Carica numero recensioni
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/ratings/stats/" + username))
                        .header("Content-Type", "application/json")
                        .GET()
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractNumberFromMessage(response.body(), "Recensioni totali: ");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento statistiche recensioni: " + e.getMessage());
            }
            return 0;
        }).thenAccept(count -> {
            Platform.runLater(() -> updateStatCard(reviewsCard, String.valueOf(count)));
        });
    }

    /**
     * Estrae un valore numerico da una stringa di risposta JSON formattata.
     * <p>
     * Questo metodo di utilità esegue il parsing di una stringa JSON per trovare
     * un messaggio al suo interno e, da questo messaggio, estrae un valore numerico
     * seguendo un pattern specifico. È una soluzione robusta per recuperare dati
     * numerici da risposte API non standardizzate. Il metodo gestisce potenziali
     * eccezioni di parsing e restituisce 0 in caso di errore per garantire la
     * stabilità dell'applicazione.
     * </p>
     *
     * @param jsonResponse La stringa di risposta JSON completa.
     * @param pattern Il prefisso della stringa che precede il numero da estrarre
     * (es. "Recensioni totali: ").
     * @return Il numero intero estratto, o 0 in caso di fallimento del parsing.
     */
    private int extractNumberFromMessage(String jsonResponse, String pattern) {
        try {
            // Parse del JSON per ottenere il messaggio
            if (jsonResponse.contains("\"message\"")) {
                int messageStart = jsonResponse.indexOf("\"message\":\"") + 11;
                int messageEnd = jsonResponse.indexOf("\"", messageStart);
                String message = jsonResponse.substring(messageStart, messageEnd);

                // Cerca il pattern nel messaggio
                if (message.contains(pattern)) {
                    String numberStr = message.substring(message.indexOf(pattern) + pattern.length());
                    return Integer.parseInt(numberStr.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore parsing numero da messaggio: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Aggiorna il valore numerico di una "card" statistica.
     * <p>
     * Questo metodo di utilità trova l'etichetta del valore all'interno di un
     * nodo {@link VBox} che rappresenta una card statistica e aggiorna il
     * suo testo con il nuovo valore. È un metodo sicuro per l'interfaccia utente,
     * in quanto verifica che il nodo esista e sia del tipo corretto prima di
     * procedere con l'aggiornamento.
     * </p>
     *
     * @param card Il nodo {@link VBox} che contiene la card da aggiornare.
     * @param newValue Il nuovo valore da visualizzare nell'etichetta.
     */
    private void updateStatCard(VBox card, String newValue) {
        // Il secondo figlio è il Label con il valore
        if (card.getChildren().size() >= 2 && card.getChildren().get(1) instanceof Label) {
            Label valueLabel = (Label) card.getChildren().get(1);
            valueLabel.setText(newValue);
        }
    }

    /**
     * Crea un nodo {@link VBox} che funge da "card" per visualizzare una singola statistica.
     * <p>
     * Questo metodo costruisce un elemento grafico compatto che mostra un'icona,
     * un valore numerico e una descrizione testuale. La card è stilizzata con
     * colori e spaziature specifiche per integrarsi nel design generale del popup.
     * Le card sono progettate per essere facilmente aggiornabili, come dimostrato
     * dal metodo {@link #updateStatCard(VBox, String)}.
     * </p>
     *
     * @param icon La stringa che rappresenta l'icona (es. un'emoji).
     * @param value La stringa del valore numerico da visualizzare.
     * @param label La descrizione testuale della statistica (es. "Libri Salvati").
     * @return Un nodo {@link VBox} che rappresenta la card completa della statistica.
     * @see #updateStatCard(VBox, String)
     */
    private VBox createStatCard(String icon, String value, String label) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 15px 10px;"
        );
        card.setMinWidth(100);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 20));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        valueLabel.setTextFill(Color.web(TEXT_COLOR));

        Label nameLabel = new Label(label);
        nameLabel.setFont(Font.font("System", 10));
        nameLabel.setTextFill(Color.web(HINT_COLOR));
        nameLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }

    /**
     * Crea la sezione dei pulsanti di azione per il profilo utente.
     * <p>
     * Questo metodo costruisce un nodo {@link VBox} che raggruppa i pulsanti
     * interattivi del popup, come "Cambia Email", "Cambia Password" e "Logout".
     * Ogni pulsante è configurato con uno stile e un'azione specifici,
     * gestendo eventi come l'apertura di finestre di dialogo dedicate o
     * l'esecuzione della logica di logout. La sezione è organizzata in righe
     * per una migliore leggibilità e un'esperienza utente coerente.
     * </p>
     *
     * @return Il nodo {@link VBox} che contiene la sezione delle azioni.
     */
    private VBox createActionsSection() {
        VBox actionsSection = new VBox(12);

        Label sectionTitle = new Label("⚙️ Azioni Account");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(TEXT_COLOR));

        // Pulsanti azione
        HBox buttonsRow1 = new HBox(10);
        buttonsRow1.setAlignment(Pos.CENTER);

        Button editButton = createActionButton("✏️ Cambia Email", "#4a86e8");
        editButton.setOnAction(e -> showEditProfileDialog());

        Button changePasswordButton = createActionButton("🔐 Cambia Password", "#4a86e8");
        changePasswordButton.setOnAction(e -> {
            showChangePasswordDialog();
        });

        buttonsRow1.getChildren().addAll(editButton, changePasswordButton);

        HBox buttonsRow2 = new HBox(10);
        buttonsRow2.setAlignment(Pos.CENTER);

        Button logoutButton = createActionButton("🚪 Logout", "#dc3545");
        logoutButton.setOnAction(e -> performLogout());

        buttonsRow2.getChildren().addAll(logoutButton);

        actionsSection.getChildren().addAll(sectionTitle, buttonsRow1, buttonsRow2);
        return actionsSection;
    }

    /**
     * Mostra un popup modale per la modifica dell'indirizzo email dell'utente.
     * <p>
     * Questo metodo crea e gestisce una finestra di dialogo separata e modale
     * che consente all'utente di aggiornare la propria email. L'interfaccia utente
     * è progettata per essere chiara e guidata, includendo campi per la nuova
     * email e la sua conferma. Il dialogo implementa una logica di validazione
     * dei campi in tempo reale e gestisce le interazioni dell'utente, come la
     * pressione dei tasti "ESC" o "INVIO", per un'esperienza fluida. Le operazioni
     * di salvataggio avvengono tramite una chiamata asincrona al metodo
     * {@link #changeEmailWithDialog(String, Stage, Label, Button)}.
     * </p>
     */
    private void showEditProfileDialog() {
        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("⚠️ Errore", "Nessun utente loggato");
            return;
        }

        Stage dialog = new Stage();
        dialog.setTitle("✏️ Modifica Profilo");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        dialog.setOnCloseRequest(e -> {
            System.out.println("🔒 Chiusura popup modifica email");
            dialog.close();
        });

        // Container principale
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Titolo e descrizione
        Label titleLabel = new Label("✏️ Modifica Email");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));

        Label descLabel = new Label("Inserisci la nuova email di accesso");
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web(HINT_COLOR));
        descLabel.setPadding(new Insets(0, 0, 10, 0));

        // Container per i campi
        VBox fieldsContainer = new VBox(15);

        // Campo email attuale (solo lettura)
        Label currentEmailLabel = new Label("📧 Email attuale:");
        currentEmailLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        currentEmailLabel.setTextFill(Color.web(TEXT_COLOR));

        TextField currentEmailField = new TextField(currentUser.getEmail());
        currentEmailField.setEditable(false);
        currentEmailField.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + HINT_COLOR + ";" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-opacity: 0.7;"
        );

        // Campo nuova email
        Label newEmailLabel = new Label("📧 Nuova email:");
        newEmailLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        newEmailLabel.setTextFill(Color.web(TEXT_COLOR));

        TextField newEmailField = new TextField();
        newEmailField.setPromptText("Inserisci la nuova email");
        styleInput(newEmailField);

        // Campo conferma email
        Label confirmEmailLabel = new Label("📧 Conferma nuova email:");
        confirmEmailLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        confirmEmailLabel.setTextFill(Color.web(TEXT_COLOR));

        TextField confirmEmailField = new TextField();
        confirmEmailField.setPromptText("Conferma la nuova email");
        styleInput(confirmEmailField);

        // Label per messaggi
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(350);
        messageLabel.setVisible(false);

        fieldsContainer.getChildren().addAll(
                currentEmailLabel, currentEmailField,
                newEmailLabel, newEmailField,
                confirmEmailLabel, confirmEmailField,
                messageLabel
        );

        // Container per i pulsanti
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button cancelButton = new Button("❌ Annulla");
        cancelButton.setPrefWidth(120);
        cancelButton.setPrefHeight(40);
        cancelButton.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        cancelButton.setOnAction(event -> {
            System.out.println("🚫 Annulla modifica email cliccato");
            dialog.close();
        });

        // Bottone Salva
        Button saveButton = new Button("✅ Salva Email");
        saveButton.setPrefWidth(150);
        saveButton.setPrefHeight(40);
        saveButton.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        // Azione del bottone Salva CORRETTA
        saveButton.setOnAction(event -> {
            String newEmail = newEmailField.getText().trim();
            String confirmEmail = confirmEmailField.getText().trim();

            // Reset messaggio precedente
            messageLabel.setVisible(false);

            // Validazioni
            if (newEmail.isEmpty()) {
                showMessage(messageLabel, "❌ Inserisci la nuova email", "#e74c3c");
                newEmailField.requestFocus();
                return;
            }

            if (!isValidEmail(newEmail)) {
                showMessage(messageLabel, "❌ Formato email non valido", "#e74c3c");
                newEmailField.requestFocus();
                return;
            }

            if (!newEmail.equals(confirmEmail)) {
                showMessage(messageLabel, "❌ Le email non corrispondono", "#e74c3c");
                confirmEmailField.requestFocus();
                return;
            }

            if (newEmail.equals(currentUser.getEmail())) {
                showMessage(messageLabel, "❌ La nuova email deve essere diversa da quella attuale", "#e74c3c");
                newEmailField.requestFocus();
                return;
            }

            changeEmailWithDialog(newEmail, dialog, messageLabel, saveButton);
        });

        buttonContainer.getChildren().addAll(cancelButton, saveButton);

        // Assemblaggio finale
        container.getChildren().addAll(
                titleLabel,
                descLabel,
                fieldsContainer,
                buttonContainer
        );

        Scene scene = new Scene(container, 450, 500);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                System.out.println("⌨️ ESC premuto - chiusura popup");
                dialog.close();
            } else if (e.getCode() == KeyCode.ENTER) {
                System.out.println("⌨️ ENTER premuto - salva email");
                saveButton.fire(); // Simula click su salva
            }
        });

        dialog.setScene(scene);

        // Effetti hover sui bottoni
        addHoverEffect(cancelButton, "#e74c3c", "#c0392b");
        addHoverEffect(saveButton, "#27ae60", "#219a52");

        Platform.runLater(() -> {
            newEmailField.requestFocus();
        });

        dialog.showAndWait();
    }

    /**
     * Gestisce la logica di aggiornamento dell'email dell'utente, eseguendo una
     * chiamata asincrona all'API del server.
     * <p>
     * Questo metodo orchestra il processo di salvataggio della nuova email.
     * Disabilita temporaneamente il pulsante di salvataggio per prevenire
     * click multipli, quindi invoca il servizio {@link AuthService} per
     * effettuare la richiesta di aggiornamento. Il processo è gestito in modo
     * asincrono per evitare di bloccare il thread dell'interfaccia utente.
     * In base alla risposta del server, l'interfaccia viene aggiornata in modo
     * sicuro tramite {@link Platform#runLater(Runnable)}. In caso di successo,
     * l'utente viene aggiornato e il dialogo viene chiuso; in caso di errore,
     * viene visualizzato un messaggio di feedback appropriato.
     * </p>
     *
     * @param newEmail La nuova email inserita dall'utente.
     * @param dialog La finestra di dialogo {@link Stage} corrente.
     * @param messageLabel L'etichetta {@link Label} per visualizzare i messaggi di stato.
     * @param saveButton Il pulsante {@link Button} di salvataggio.
     * @see AuthService#updateEmailAsync(String, String)
     */
    private void changeEmailWithDialog(String newEmail, Stage dialog, Label messageLabel, Button saveButton) {
        // Disabilita il pulsante durante l'operazione
        saveButton.setDisable(true);
        saveButton.setText("🔄 Salvataggio...");

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            saveButton.setDisable(false);
            saveButton.setText("✅ Salva Email");
            showMessage(messageLabel, "❌ Errore: utente non trovato", "#e74c3c");
            return;
        }

        AuthService authService = new AuthService();

        authService.updateEmailAsync(String.valueOf(currentUser.getId()), newEmail)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        saveButton.setText("✅ Salva Email");

                        if (response.isSuccess()) {
                            User updatedUser = response.getUser();
                            if (updatedUser != null) {
                                authManager.updateCurrentUser(updatedUser);
                            }

                            updateProfileDisplay();

                            // Chiudi dialog
                            dialog.close();

                            // Mostra messaggio di successo
                            showAlert("✅ Successo", "Email aggiornata correttamente!");

                        } else {
                            String errorMsg = response.getMessage() != null ? response.getMessage() : "Errore durante l'aggiornamento";
                            showMessage(messageLabel, "❌ " + errorMsg, "#e74c3c");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        saveButton.setText("✅ Salva Email");
                        showMessage(messageLabel, "❌ Errore di connessione: " + throwable.getMessage(), "#e74c3c");
                    });
                    return null;
                });
    }

    /**
     * Aggiorna la visualizzazione del popup del profilo utente.
     * <p>
     * Questo metodo gestisce la ricarica dell'interfaccia grafica del profilo
     * utente. Lo fa chiudendo il popup corrente e riaprendolo dopo un breve
     * ritardo. Questo approccio garantisce che i dati visualizzati siano
     * aggiornati e previene potenziali problemi grafici legati all'aggiornamento
     * dei componenti dell'interfaccia utente. L'intera operazione è eseguita
     * in modo sicuro sul thread dell'interfaccia utente di JavaFX.
     * </p>
     *
     * @see #closePopup()
     * @see #show(StackPane)
     */
    private void updateProfileDisplay() {
        if (root != null) {
            Platform.runLater(() -> {
                // Chiudi il popup corrente
                closePopup();

                // Riapri con i dati aggiornati dopo un breve delay
                Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), e -> {
                    show(root);
                }));
                timeline.play();
            });
        }
    }

    /**
     * Applica uno stile visivo coerente a un campo di testo.
     * <p>
     * Questo metodo di utilità centralizza la logica di stilizzazione per i campi
     * di testo {@link TextField}, assicurando un design uniforme in tutto il popup.
     * Lo stile include la configurazione di colori, bordi arrotondati, imbottitura
     * e dimensioni, migliorando l'aspetto e la leggibilità dei campi di input.
     * </p>
     *
     * @param field Il campo di testo {@link TextField} da stilizzare.
     */
    private void styleInput(TextField field) {
        field.setStyle(
                "-fx-background-color: " + BG_CONTROL + ";" +
                        "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-prompt-text-fill: " + HINT_COLOR + ";"
        );
        field.setPrefHeight(45);
        field.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Mostra un messaggio di stato in un'etichetta.
     * <p>
     * Questo metodo di utilità semplifica la visualizzazione di messaggi
     * di feedback all'utente. Imposta il testo e il colore dell'etichetta
     * in base ai parametri forniti e la rende visibile. È utile per mostrare
     * avvisi di successo, messaggi di errore o istruzioni.
     * </p>
     *
     * @param messageLabel L'etichetta {@link Label} dove mostrare il messaggio.
     * @param message Il testo del messaggio da visualizzare.
     * @param color Il colore in formato stringa per il testo.
     */
    private void showMessage(Label messageLabel, String message, String color) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.web(color));
        messageLabel.setVisible(true);
    }

    /**
     * Aggiunge un effetto di transizione al passaggio del mouse per un pulsante.
     * <p>
     * Questo metodo di utilità applica un'interattività visuale ai pulsanti,
     * modificandone il colore di sfondo quando il cursore del mouse entra o
     * esce dall'area del pulsante. L'effetto è realizzato tramite la gestione
     * degli eventi {@code onMouseEntered} e {@code onMouseExited}, che
     * aggiornano dinamicamente lo stile del nodo.
     * </p>
     *
     * @param button Il pulsante {@link Button} a cui applicare l'effetto.
     * @param originalColor Il colore di sfondo originale del pulsante in formato esadecimale.
     * @param hoverColor Il colore di sfondo da applicare al passaggio del mouse.
     */
    private void addHoverEffect(Button button, String originalColor, String hoverColor) {
        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(originalColor, hoverColor))
        );
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace(hoverColor, originalColor))
        );
    }

    /**
     * Mostra un popup modale per la gestione della modifica della password.
     * <p>
     * Questo metodo crea e visualizza un'interfaccia di dialogo modale che consente
     * all'utente di cambiare la propria password. Il popup richiede la password
     * attuale, la nuova password e una sua conferma. Il metodo include una logica
     * di validazione per assicurare che la nuova password e la sua conferma
     * coincidano. La richiesta di cambio password viene inviata al server in modo
     * asincrono tramite il servizio {@link AuthService} per non bloccare
     * l'interfaccia utente. Il feedback, sia in caso di successo che di fallimento,
     * è gestito dinamicamente tramite un'etichetta di stato all'interno del dialogo.
     * </p>
     */
    private void showChangePasswordDialog() {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("🔐 Cambia Password");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Password attuale");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nuova password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Conferma nuova password");

        Label messageLabel = new Label();

        content.getChildren().addAll(
                new Label("Password attuale:"), currentPasswordField,
                new Label("Nuova password:"), newPasswordField,
                new Label("Conferma password:"), confirmPasswordField,
                messageLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();

            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!newPassword.equals(confirmPassword)) {
                messageLabel.setText("❌ Le password non coincidono");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // Simula cambio password
            messageLabel.setText("🔄 Cambio password in corso...");
            messageLabel.setTextFill(Color.ORANGE);

            // Ottieni l'utente corrente per l'ID
            User currentUser = authManager.getCurrentUser();
            if (currentUser == null) {
                messageLabel.setText("❌ Errore: utente non trovato");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // Disabilita il pulsante durante la richiesta
            okButton.setDisable(true);
            messageLabel.setText("🔄 Cambio password in corso...");
            messageLabel.setTextFill(Color.ORANGE);

            // Crea un'istanza di AuthService
            AuthService authService = new AuthService();

            // Chiama il servizio per cambiare la password
            authService.changePasswordAsync(
                    String.valueOf(currentUser.getId()),
                    currentPassword,
                    newPassword
            ).thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        dialog.close();
                        showAlert("✅ Successo", "Password cambiata con successo!");
                    } else {
                        messageLabel.setText("❌ " + response.getMessage());
                        messageLabel.setTextFill(Color.RED);
                        okButton.setDisable(false);
                    }
                });
            }).exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    messageLabel.setText("❌ Errore di connessione");
                    messageLabel.setTextFill(Color.RED);
                    okButton.setDisable(false);
                });
                return null;
            });
        });

        dialog.showAndWait();
    }

    /**
     * Valida un indirizzo email utilizzando un'espressione regolare.
     * <p>
     * Questo metodo di utilità verifica che la stringa fornita corrisponda a un
     * formato di indirizzo email valido. L'espressione regolare utilizzata
     * è robusta per la maggior parte dei casi comuni di validazione.
     * </p>
     *
     * @param email La stringa dell'indirizzo email da validare.
     * @return {@code true} se l'email è in un formato valido, {@code false} altrimenti.
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * Crea un pulsante di azione con uno stile predefinito e un effetto hover.
     * <p>
     * Questo metodo di fabbrica genera un nodo {@link Button} con uno stile
     * coerente per i pulsanti di azione all'interno del popup. Il pulsante è
     * personalizzabile con un testo e un colore di sfondo specifici.
     * Applica inoltre un effetto visivo che cambia il colore del pulsante
     * al passaggio del mouse, migliorando l'interattività per l'utente.
     * </p>
     *
     * @param text Il testo da visualizzare sul pulsante.
     * @param backgroundColor Il colore di sfondo del pulsante in formato esadecimale.
     * @return Un nodo {@link Button} completamente stilizzato.
     */
    private Button createActionButton(String text, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10px 15px;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefWidth(150);

        // Hover effect
        String hoverColor = backgroundColor.equals("#dc3545") ? "#c82333" : "derive(" + backgroundColor + ", 20%)";
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle().replace(backgroundColor, hoverColor)));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace(hoverColor, backgroundColor)));

        return button;
    }

    /**
     * Gestisce il processo di disconnessione dell'utente, inclusa la conferma.
     * <p>
     * Questo metodo avvia una finestra di dialogo di conferma per chiedere all'utente
     * di confermare la sua intenzione di effettuare il logout. Se l'utente
     * seleziona "OK", il metodo esegue la logica di disconnessione tramite
     * {@link AuthenticationManager#logout()}, chiude il popup del profilo e
     * invoca la callback di logout per informare gli altri componenti
     * dell'interfaccia utente. Infine, mostra un messaggio di successo.
     * </p>
     */
    private void performLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("🚪 Conferma Logout");
        confirmAlert.setHeaderText("Sei sicuro di voler uscire?");
        confirmAlert.setContentText("Dovrai effettuare nuovamente il login per accedere al tuo account.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("🚪 Logout confermato dall'utente");

                // Esegui logout tramite AuthenticationManager
                authManager.logout();

                // Chiudi popup
                closePopup();

                // Notifica callback per aggiornare sidebar
                if (onLogoutCallback != null) {
                    onLogoutCallback.run();
                }

                // Mostra messaggio di conferma
                Platform.runLater(() -> {
                    showAlert("👋 Arrivederci", "Logout effettuato con successo!");
                });
            }
        });
    }

    /**
     * Chiude il popup del profilo utente.
     * <p>
     * Questo metodo rimuove il nodo che rappresenta il popup dal contenitore radice
     * principale dell'interfaccia utente. La rimozione avviene in modo sicuro
     * verificando che il nodo radice esista e che ci sia più di un elemento
     * (per non rimuovere il contenuto principale dell'applicazione). Un messaggio
     * di debug viene stampato nella console per confermare la chiusura.
     * </p>
     */
    private void closePopup() {
        if (root != null && root.getChildren().size() > 1) {
            root.getChildren().remove(root.getChildren().size() - 1);
        }
        System.out.println("🔒 Popup profilo utente chiuso");
    }

    /**
     * Mostra un semplice alert informativo modale.
     * <p>
     * Questo metodo di utilità crea e visualizza una finestra di dialogo
     * standard {@link Alert} di tipo INFORMATION. Il dialogo è configurato
     * con un titolo e un messaggio specifici, e blocca l'interfaccia utente
     * finché non viene chiuso. È utile per fornire feedback rapido e non
     * critico all'utente.
     * </p>
     *
     * @param title Il titolo da visualizzare nella finestra di dialogo.
     * @param message Il testo del messaggio da mostrare all'utente.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}