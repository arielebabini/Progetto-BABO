package org.BABO.client.ui.Home;

import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Popup.UserProfilePopup;
import org.BABO.shared.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Componente di navigazione laterale dell'applicazione BABO Library con gestione utenti avanzata.
 * <p>
 * Sidebar rappresenta il pannello di navigazione principale dell'applicazione, fornendo
 * accesso rapido alle sezioni principali e gestione completa dell'autenticazione utente.
 * Implementa un sistema dinamico di menu context-aware che si adatta ai privilegi
 * dell'utente corrente, offrendo funzionalità amministrative per utenti autorizzati
 * e una sezione di gestione profilo personalizzata.
 * </p>
 *
 * <h3>Architettura componente:</h3>
 * <p>
 * Il design segue principi di modularità e responsiveness, adattandosi dinamicamente
 * allo stato dell'applicazione e ai privilegi utente:
 * </p>
 * <ul>
 *   <li><strong>Navigation System:</strong> Menu principale con sezioni core dell'app</li>
 *   <li><strong>Authentication Integration:</strong> Widget utente con gestione sessioni</li>
 *   <li><strong>Privilege Management:</strong> Menu dinamici basati su ruoli utente</li>
 *   <li><strong>Server Status:</strong> Indicatore stato connessione backend</li>
 *   <li><strong>User Profile:</strong> Avatar personalizzato e gestione account</li>
 * </ul>
 *
 * <h3>Struttura layout sidebar:</h3>
 * <pre>
 * VBox (Sidebar Container)
 * ├── Header ("📚 Libreria")
 * ├── Menu Items
 * │   ├── 🏠 Home
 * │   ├── 📚 Le Mie Librerie
 * │   ├── 🔍 Esplora
 * │   └── ⚙️ Gestione (solo admin)
 * ├── Spacer (flexible)
 * ├── Server Status Indicator
 * └── Authentication Section
 *     ├── User Widget (se autenticato)
 *     │   ├── Avatar circolare
 *     │   ├── Nome e email
 *     │   └── Gestisci Account
 *     └── Login/Logout Button
 * </pre>
 *
 * <h3>Sistema di menu dinamico:</h3>
 * <p>
 * Menu items vengono generati dinamicamente basandosi sul contesto utente:
 * </p>
 * <ul>
 *   <li><strong>Menu Base:</strong> Home, Librerie, Esplora (sempre visibili)</li>
 *   <li><strong>Menu Admin:</strong> Gestione (solo per utenti amministratori)</li>
 *   <li><strong>Active State:</strong> Evidenziazione sezione corrente</li>
 *   <li><strong>Hover Effects:</strong> Feedback visivo interazioni</li>
 * </ul>
 *
 * <h3>Gestione privilegi amministrativi:</h3>
 * <p>
 * Sistema robusto per identificazione e gestione utenti amministratori:
 * </p>
 * <ul>
 *   <li><strong>Email Whitelist:</strong> Lista email amministratori autorizzati</li>
 *   <li><strong>Dynamic Menu:</strong> Menu "Gestione" appare solo per admin</li>
 *   <li><strong>Real-time Updates:</strong> Refresh automatico su cambio autenticazione</li>
 *   <li><strong>Security Validation:</strong> Verifica continua privilegi</li>
 * </ul>
 *
 * <h3>Widget utente personalizzato:</h3>
 * <ul>
 *   <li><strong>Avatar Generation:</strong> Avatar circolare con iniziali utente</li>
 *   <li><strong>User Information:</strong> Display nome completo e email</li>
 *   <li><strong>Profile Management:</strong> Accesso diretto gestione account</li>
 *   <li><strong>Visual Feedback:</strong> Stati hover e active personalizzati</li>
 * </ul>
 *
 * <h3>Integrazione autenticazione:</h3>
 * <p>
 * Sistema completamente integrato con AuthenticationManager:
 * </p>
 * <ul>
 *   <li><strong>State Synchronization:</strong> Aggiornamento automatico su login/logout</li>
 *   <li><strong>Callback Integration:</strong> Refresh triggers da auth manager</li>
 *   <li><strong>Session Management:</strong> Gestione sessioni utente</li>
 *   <li><strong>Profile Access:</strong> Popup gestione profilo integrato</li>
 * </ul>
 *
 * <h3>Indicatori di stato sistema:</h3>
 * <ul>
 *   <li><strong>Server Status:</strong> Indicatore visivo connessione backend</li>
 *   <li><strong>Color Coding:</strong> Verde online, arancione offline</li>
 *   <li><strong>Mode Adaptation:</strong> Adattamento funzionalità basato su stato</li>
 * </ul>
 *
 * <h3>Pattern implementati:</h3>
 * <ul>
 *   <li><strong>Observer Pattern:</strong> Callback per aggiornamenti stato auth</li>
 *   <li><strong>Strategy Pattern:</strong> Diversi layout basati su stato utente</li>
 *   <li><strong>State Pattern:</strong> Gestione stati menu attivi</li>
 *   <li><strong>Factory Pattern:</strong> Creazione dinamica componenti UI</li>
 *   <li><strong>Template Method:</strong> Template per creazione elementi menu</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo base:</h3>
 * <pre>{@code
 * // Inizializzazione sidebar
 * AuthenticationManager authManager = new AuthenticationManager();
 * MainWindow mainWindow = new MainWindow(bookService, serverAvailable);
 * Sidebar sidebar = new Sidebar(serverAvailable, authManager, mainWindow);
 *
 * // Creazione layout
 * VBox sidebarLayout = sidebar.createSidebar();
 *
 * // Integrazione in layout principale
 * BorderPane appRoot = new BorderPane();
 * appRoot.setLeft(sidebarLayout);
 *
 * // La sidebar gestisce automaticamente:
 * // - Navigazione tra sezioni
 * // - Stati utente (login/logout)
 * // - Menu amministrativi per utenti privilegiati
 * // - Indicatori stato server
 * }</pre>
 *
 * <h3>Esempio di utilizzo avanzato:</h3>
 * <pre>{@code
 * // Sidebar con gestione callback personalizzati
 * Sidebar sidebar = new Sidebar(true, authManager, mainWindow);
 * VBox layout = sidebar.createSidebar();
 *
 * // Setup callback autenticazione per refresh automatico
 * authManager.setOnAuthStateChanged(() -> {
 *     sidebar.refreshAuthSection();
 *     // Altri aggiornamenti UI...
 * });
 *
 * // Controllo privilegi amministrativi
 * AuthenticationManager auth = sidebar.getAuthManager();
 * if (auth.isAuthenticated()) {
 *     User currentUser = auth.getCurrentUser();
 *     boolean isAdmin = checkAdminPrivileges(currentUser);
 *     // Logica specifica per admin...
 * }
 * }</pre>
 *
 * <h3>Sistema di navigazione eventi:</h3>
 * <p>
 * Ogni menu item ha handler specifici per navigazione:
 * </p>
 * <ul>
 *   <li><strong>Home:</strong> Ritorno pagina principale con reset popup</li>
 *   <li><strong>Librerie:</strong> Apertura pannello gestione librerie personali</li>
 *   <li><strong>Esplora:</strong> Navigazione sezione esplorazione catalogo</li>
 *   <li><strong>Gestione:</strong> Pannello amministrativo (solo admin)</li>
 * </ul>
 *
 * <h3>Gestione avatar e iniziali:</h3>
 * <p>
 * Sistema sofisticato per generazione avatar personalizzati:
 * </p>
 * <ul>
 *   <li><strong>Initials Extraction:</strong> Estrazione iniziali da nome e cognome</li>
 *   <li><strong>Fallback Logic:</strong> Fallback a email se nome non disponibile</li>
 *   <li><strong>Visual Design:</strong> Cerchio colorato con iniziali centrate</li>
 *   <li><strong>Consistent Styling:</strong> Design coerente con tema applicazione</li>
 * </ul>
 *
 * <h3>Refresh dinamico componenti:</h3>
 * <p>
 * Sistema di aggiornamento real-time per cambi stato:
 * </p>
 * <ul>
 *   <li><strong>Auth Section Refresh:</strong> Aggiornamento sezione autenticazione</li>
 *   <li><strong>Menu Items Refresh:</strong> Ricostruzione menu dinamici</li>
 *   <li><strong>Active State Preservation:</strong> Mantenimento stato attivo</li>
 *   <li><strong>Thread Safety:</strong> Aggiornamenti thread-safe UI</li>
 * </ul>
 *
 * <h3>Styling e temi:</h3>
 * <ul>
 *   <li><strong>Dark Theme:</strong> Palette colori dark mode (#2c2c2e background)</li>
 *   <li><strong>Color Coding:</strong> Blu accento (#4a86e8) per elementi interattivi</li>
 *   <li><strong>Hover Effects:</strong> Transizioni smooth per feedback utente</li>
 *   <li><strong>Responsive Design:</strong> Layout adattivo per diverse risoluzioni</li>
 * </ul>
 *
 * <h3>Sicurezza e validazione:</h3>
 * <ul>
 *   <li><strong>Admin Validation:</strong> Whitelist email per privilegi amministrativi</li>
 *   <li><strong>Session Validation:</strong> Verifica continua validità sessioni</li>
 *   <li><strong>Null Safety:</strong> Gestione robusta valori null</li>
 *   <li><strong>Error Recovery:</strong> Fallback graceful per errori UI</li>
 * </ul>
 *
 * <h3>Performance e ottimizzazioni:</h3>
 * <ul>
 *   <li><strong>Lazy Refresh:</strong> Aggiornamento solo quando necessario</li>
 *   <li><strong>Event Efficiency:</strong> Handler eventi ottimizzati</li>
 *   <li><strong>Memory Management:</strong> Cleanup automatico componenti</li>
 *   <li><strong>Rendering Optimization:</strong> Minimizzazione re-rendering</li>
 * </ul>
 *
 * <h3>Accessibilità e UX:</h3>
 * <ul>
 *   <li><strong>Keyboard Navigation:</strong> Supporto navigazione da tastiera</li>
 *   <li><strong>Visual Feedback:</strong> Stati hover e focus chiari</li>
 *   <li><strong>Consistent Behavior:</strong> Comportamento uniforme elementi</li>
 *   <li><strong>Error Communication:</strong> Messaggi errore user-friendly</li>
 * </ul>
 *
 * <h3>Integrazione sistema popup:</h3>
 * <ul>
 *   <li><strong>Profile Popup:</strong> Gestione profilo utente tramite popup</li>
 *   <li><strong>Auth Panel:</strong> Panel login/registrazione integrato</li>
 *   <li><strong>Popup Coordination:</strong> Coordinamento con PopupManager</li>
 * </ul>
 *
 * <h3>Thread safety considerations:</h3>
 * <ul>
 *   <li><strong>UI Thread Updates:</strong> Tutti gli aggiornamenti UI su JavaFX thread</li>
 *   <li><strong>Callback Safety:</strong> Callback thread-safe per auth changes</li>
 *   <li><strong>State Synchronization:</strong> Sincronizzazione sicura stato componenti</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see org.BABO.client.ui.Authentication.AuthenticationManager
 * @see org.BABO.client.ui.Home.MainWindow
 * @see org.BABO.client.ui.Popup.UserProfilePopup
 * @see org.BABO.shared.model.User
 */
public class Sidebar {

    /** Indica la disponibilità del server per adattamento funzionalità */
    private final boolean serverAvailable;

    /** Gestore autenticazione per integrazione sessioni utente */
    private final AuthenticationManager authManager;

    /** Riferimento finestra principale per coordinamento navigazione */
    private final MainWindow mainWindow;

    /** Container dinamico per elementi menu navigazione */
    private VBox menuItemsBox;

    /** Sezione dinamica per gestione autenticazione utente */
    private VBox authSection;

    /** Indice menu attualmente selezionato per gestione stato */
    private int activeMenuIndex = 0;

    /**
     * Costruisce una nuova istanza di Sidebar con integrazione componenti principali.
     * <p>
     * Inizializza il componente sidebar configurando le dipendenze necessarie
     * per il funzionamento integrato con il sistema di autenticazione e
     * la finestra principale dell'applicazione.
     * </p>
     *
     * <h4>Dipendenze configurate:</h4>
     * <ul>
     *   <li><strong>Server Status:</strong> Adattamento funzionalità basato su disponibilità</li>
     *   <li><strong>Authentication Manager:</strong> Integrazione gestione sessioni</li>
     *   <li><strong>MainWindow Reference:</strong> Coordinamento navigazione</li>
     * </ul>
     *
     * @param serverAvailable indica se il server backend è disponibile
     * @param authManager gestore autenticazione per integrazione sessioni
     * @param mainWindow riferimento finestra principale per navigazione
     */
    public Sidebar(boolean serverAvailable, AuthenticationManager authManager, MainWindow mainWindow) {
        this.serverAvailable = serverAvailable;
        this.authManager = authManager;
        this.mainWindow = mainWindow;
    }

    /**
     * Crea il layout completo della sidebar con tutti i componenti integrati.
     * <p>
     * Factory method principale che costruisce l'intera sidebar dell'applicazione
     * includendo header, menu di navigazione, indicatori di stato e sezione
     * autenticazione. Il layout è completamente responsivo e si adatta
     * dinamicamente allo stato dell'utente e del sistema.
     * </p>
     *
     * <h4>Componenti creati:</h4>
     * <ul>
     *   <li><strong>Header:</strong> Titolo sezione "📚 Libreria"</li>
     *   <li><strong>Menu Items:</strong> Navigazione principale dinamica</li>
     *   <li><strong>Spacer:</strong> Spaziatura flessibile per layout</li>
     *   <li><strong>Server Status:</strong> Indicatore connessione backend</li>
     *   <li><strong>Auth Section:</strong> Widget utente o pulsanti login</li>
     * </ul>
     *
     * <h4>Styling applicato:</h4>
     * <ul>
     *   <li>Background color: #2c2c2e (dark theme)</li>
     *   <li>Width: 200px fixed</li>
     *   <li>Spacing: 15px tra componenti</li>
     *   <li>Padding: Specifico per ogni sezione</li>
     * </ul>
     *
     * <h4>Layout structure:</h4>
     * <pre>
     * VBox (Sidebar - 200px width)
     * ├── Label (Header "📚 Libreria")
     * ├── VBox (Menu Items - dynamic)
     * ├── Region (Flexible spacer)
     * ├── Label (Server status indicator)
     * └── VBox (Authentication section)
     * </pre>
     *
     * @return {@link VBox} contenente l'intera sidebar configurata
     */
    public VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(200);
        sidebar.setPrefHeight(700);
        sidebar.setStyle("-fx-background-color: #2c2c2e;");

        // Header
        Label sidebarHeader = new Label("📚 Libreria");
        sidebarHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        sidebarHeader.setTextFill(Color.WHITE);
        sidebarHeader.setPadding(new Insets(20, 0, 5, 20));

        // Menu items
        menuItemsBox = createMenuItems();

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Status server
        Label serverStatus = new Label(serverAvailable ? "🟢 Server Online" : "🔴 Modalità Offline");
        serverStatus.setTextFill(serverAvailable ? Color.LIGHTGREEN : Color.ORANGE);
        serverStatus.setFont(Font.font("System", 12));
        serverStatus.setPadding(new Insets(10, 20, 10, 20));

        // Auth section (dinamica)
        authSection = new VBox(10);
        updateAuthSection();

        sidebar.getChildren().addAll(sidebarHeader, menuItemsBox, spacer, serverStatus, authSection);
        return sidebar;
    }

    /**
     * Crea dinamicamente gli elementi del menu di navigazione basandosi sui privilegi utente.
     * <p>
     * Genera il sistema di menu principale dell'applicazione con elementi dinamici
     * che si adattano ai privilegi dell'utente corrente. Include menu base sempre
     * visibili e menu amministrativi per utenti autorizzati.
     * </p>
     *
     * <h4>Menu base (sempre visibili):</h4>
     * <ul>
     *   <li><strong>🏠 Home:</strong> Pagina principale applicazione</li>
     *   <li><strong>📚 Le Mie Librerie:</strong> Gestione librerie personali</li>
     *   <li><strong>🔍 Esplora:</strong> Esplorazione catalogo</li>
     * </ul>
     *
     * <h4>Menu condizionali:</h4>
     * <ul>
     *   <li><strong>⚙️ Gestione:</strong> Pannello amministrativo (solo admin)</li>
     * </ul>
     *
     * <h4>Event handlers configurati:</h4>
     * <ul>
     *   <li><strong>Home:</strong> Ritorno home con reset UI</li>
     *   <li><strong>Librerie:</strong> Apertura pannello gestione librerie</li>
     *   <li><strong>Esplora:</strong> Navigazione sezione esplorazione</li>
     *   <li><strong>Gestione:</strong> Apertura pannello amministrativo</li>
     * </ul>
     *
     * <h4>Admin privilege checking:</h4>
     * <p>
     * Il sistema verifica i privilegi amministrativi controllando se l'email
     * dell'utente corrente è presente nella whitelist degli amministratori
     * configurata nel sistema.
     * </p>
     *
     * @return {@link VBox} contenente tutti gli elementi menu configurati
     */
    private VBox createMenuItems() {
        VBox menuBox = new VBox(5);
        menuBox.setPadding(new Insets(10, 0, 0, 15));

        // Menu base sempre visibili
        List<String> menuItemsList = new ArrayList<>();
        menuItemsList.add("🏠 Home");
        menuItemsList.add("📚 Le Mie Librerie");
        menuItemsList.add("🔍 Esplora");

        // Controlla se utente è admin per aggiungere menu Gestione
        boolean isAdmin = authManager.isAuthenticated() &&
                authManager.getCurrentUser() != null &&
                isCurrentUserAdmin();

        if (isAdmin) {
            menuItemsList.add("⚙️ Gestione");
            System.out.println("👑 Menu admin attivato per: " + authManager.getCurrentUser().getEmail());
        } else {
            System.out.println("👤 Menu standard per utente normale");
        }

        String[] menuItems = menuItemsList.toArray(new String[0]);

        for (int i = 0; i < menuItems.length; i++) {
            HBox itemBox = createMenuItem(menuItems[i], i == activeMenuIndex);
            final int index = i;

            // Gestori eventi per tutti i menu
            if (menuItems[i].contains("Home")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null) {
                        mainWindow.showHomePage();
                        if (mainWindow.getContentArea() != null) {
                            mainWindow.getContentArea().forceHomeView();
                        }
                    }
                });
            } else if (menuItems[i].contains("Le Mie Librerie")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null) {
                        mainWindow.showLibraryPanel();
                    }
                });
            } else if (menuItems[i].contains("Esplora")) {
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null) {
                        mainWindow.showExploreSection();
                    }
                });
            } else if (menuItems[i].contains("Gestione")) {
                // Menu Gestione per admin
                itemBox.setOnMouseClicked(e -> {
                    updateActiveMenuItem(index);
                    if (mainWindow != null) {
                        System.out.println("⚙️ Apertura pannello admin da sidebar");
                        mainWindow.showAdminPanel();
                    }
                });
            }

            menuBox.getChildren().add(itemBox);
        }

        return menuBox;
    }

    /**
     * Verifica se l'utente corrente possiede privilegi amministrativi.
     * <p>
     * Sistema di validazione privilegi che controlla se l'utente autenticato
     * corrente è autorizzato ad accedere alle funzionalità amministrative.
     * Utilizza una whitelist di email amministratori per la validazione.
     * </p>
     *
     * <h4>Verifica prerequisiti:</h4>
     * <ul>
     *   <li>Utente deve essere autenticato</li>
     *   <li>Oggetto User deve essere valido e non null</li>
     *   <li>Email deve essere presente e valida</li>
     * </ul>
     *
     * <h4>Whitelist amministratori:</h4>
     * <p>
     * La lista email amministratori è sincronizzata con quella del UserService
     * per garantire coerenza tra frontend e backend nella gestione privilegi.
     * </p>
     *
     * <h4>Security considerations:</h4>
     * <ul>
     *   <li><strong>Case Insensitive:</strong> Confronto email case-insensitive</li>
     *   <li><strong>Whitelist Approach:</strong> Solo email esplicitamente autorizzate</li>
     *   <li><strong>Fail Secure:</strong> Default deny se validazione fallisce</li>
     * </ul>
     *
     * @return true se l'utente corrente è amministratore, false altrimenti
     */
    private boolean isCurrentUserAdmin() {
        if (!authManager.isAuthenticated() || authManager.getCurrentUser() == null) {
            return false;
        }

        String email = authManager.getCurrentUser().getEmail();
        System.out.println("🔍 Controllo privilegi admin per: " + email);

        // LISTA EMAIL ADMIN (stessa di UserService)
        String[] adminEmails = {
                "federico@admin.com",
                "ariele@admin.com"
        };

        for (String adminEmail : adminEmails) {
            if (email.equalsIgnoreCase(adminEmail)) {
                System.out.println("👑 Utente riconosciuto come admin: " + email);
                return true;
            }
        }

        System.out.println("👤 Utente normale: " + email);
        return false;
    }

    /**
     * Crea un singolo elemento del menu con styling e stato specificato.
     * <p>
     * Factory method per creazione elementi menu uniformi con gestione
     * stati attivi e effetti hover. Ogni elemento è configurato con
     * styling coerente e feedback visivo appropriato.
     * </p>
     *
     * <h4>Configurazione elemento:</h4>
     * <ul>
     *   <li><strong>Container:</strong> HBox con padding e alignment</li>
     *   <li><strong>Label:</strong> Testo menu con icona</li>
     *   <li><strong>Styling:</strong> Condizionale basato su stato attivo</li>
     *   <li><strong>Hover Effects:</strong> Transizioni smooth mouse over/out</li>
     * </ul>
     *
     * <h4>Stati visivi:</h4>
     * <ul>
     *   <li><strong>Active:</strong> Background #3a3a3c, testo bianco</li>
     *   <li><strong>Inactive:</strong> Background trasparente, testo grigio</li>
     *   <li><strong>Hover:</strong> Background temporaneo, testo bianco</li>
     * </ul>
     *
     * @param text testo da visualizzare nel menu item
     * @param isActive true se questo è l'elemento attualmente selezionato
     * @return {@link HBox} elemento menu configurato con styling
     */
    private HBox createMenuItem(String text, boolean isActive) {
        HBox item = new HBox();
        item.setPrefHeight(35);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(5, 10, 5, 10));

        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.NORMAL, 14));

        if (isActive) {
            item.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 5; -fx-cursor: hand;");
            label.setTextFill(Color.WHITE);
        } else {
            item.setStyle("-fx-cursor: hand;");
            label.setTextFill(Color.LIGHTGRAY);
        }

        // Effetti hover
        item.setOnMouseEntered(e -> {
            if (!isActive) {
                item.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 5; -fx-cursor: hand;");
                label.setTextFill(Color.WHITE);
            }
        });

        item.setOnMouseExited(e -> {
            if (!isActive) {
                item.setStyle("-fx-cursor: hand;");
                label.setTextFill(Color.LIGHTGRAY);
            }
        });

        item.getChildren().add(label);
        return item;
    }

    /**
     * Aggiorna dinamicamente la sezione autenticazione basandosi sullo stato utente.
     * <p>
     * Ricostruisce completamente la sezione autenticazione della sidebar
     * adattandosi allo stato corrente dell'utente. Se autenticato, mostra
     * widget utente personalizzato con avatar e controlli account. Se non
     * autenticato, mostra pulsante di login.
     * </p>
     *
     * <h4>Stato utente autenticato:</h4>
     * <ul>
     *   <li><strong>User Widget:</strong> Avatar, nome, email, gestione account</li>
     *   <li><strong>Logout Button:</strong> Pulsante disconnessione rapida</li>
     * </ul>
     *
     * <h4>Stato utente non autenticato:</h4>
     * <ul>
     *   <li><strong>Login Button:</strong> Pulsante accesso pannello autenticazione</li>
     * </ul>
     *
     * <h4>Dynamic rebuild:</h4>
     * <p>
     * La sezione viene completamente ricostruita ad ogni chiamata per
     * garantire coerenza dello stato e aggiornamento di tutte le informazioni
     * utente visualizzate.
     * </p>
     */
    private void updateAuthSection() {
        authSection.getChildren().clear();

        if (authManager.isAuthenticated()) {
            // Utente autenticato - usa widget utente con avatar
            VBox userWidget = createUserWidget();
            authSection.getChildren().add(userWidget);

            // Pulsante logout
            Button logoutButton = createAuthButton("🚪 Logout", () -> {
                System.out.println("🔓 Logout richiesto");
                authManager.logout();
            });

            authSection.getChildren().add(logoutButton);

        } else {
            // Utente non autenticato
            Button loginButton = createAuthButton("🔑 Accedi", () -> {
                System.out.println("🔑 Login richiesto da sidebar");
                if (mainWindow != null) {
                    mainWindow.showAuthPanel();
                }
            });

            authSection.getChildren().add(loginButton);
        }
    }

    /**
     * Crea il widget personalizzato per l'utente autenticato con avatar e controlli.
     * <p>
     * Costruisce un componente UI complesso che visualizza le informazioni
     * dell'utente corrente in formato compatto ma informativo. Include avatar
     * generato dinamicamente, informazioni utente e controlli per gestione account.
     * </p>
     *
     * <h4>Componenti widget:</h4>
     * <ul>
     *   <li><strong>Avatar Circolare:</strong> Generato con iniziali utente</li>
     *   <li><strong>Display Name:</strong> Nome completo dell'utente</li>
     *   <li><strong>Email Address:</strong> Indirizzo email account</li>
     *   <li><strong>Separator:</strong> Divisore visivo sezioni</li>
     *   <li><strong>Manage Button:</strong> Accesso gestione profilo</li>
     * </ul>
     *
     * <h4>Styling widget:</h4>
     * <ul>
     *   <li>Background: rgba(74, 134, 232, 0.15) con radius 12px</li>
     *   <li>Padding: 12px uniforme</li>
     *   <li>Alignment: Centrato con max width responsive</li>
     *   <li>Cursor: Hand per indicare interattività</li>
     * </ul>
     *
     * <h4>Error handling:</h4>
     * <p>
     * Include gestione robusta per scenari edge case come utente null
     * o informazioni mancanti, fornendo fallback visivi appropriati.
     * </p>
     *
     * <h4>Integration:</h4>
     * <p>
     * Il pulsante "Gestisci Account" apre UserProfilePopup con callback
     * di logout che mantiene sincronizzazione stato applicazione.
     * </p>
     *
     * @return {@link VBox} widget utente completo o widget errore se user null
     */
    private VBox createUserWidget() {
        VBox userWidget = new VBox(8);
        userWidget.setStyle(
                "-fx-background-color: rgba(74, 134, 232, 0.15);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-padding: 12px;" +
                        "-fx-cursor: hand;"
        );
        userWidget.setMaxWidth(Double.MAX_VALUE);
        userWidget.setAlignment(Pos.CENTER);

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            // Fallback se l'utente è null
            Label errorLabel = new Label("⚠️ Errore utente");
            errorLabel.setTextFill(Color.ORANGE);
            userWidget.getChildren().add(errorLabel);
            return userWidget;
        }

        // Avatar circolare con iniziali
        StackPane avatar = createUserAvatar(currentUser);

        // Nome utente
        Label userName = new Label(currentUser.getDisplayName());
        userName.setTextFill(Color.WHITE);
        userName.setFont(Font.font("System", FontWeight.BOLD, 13));
        userName.setWrapText(true);
        userName.setMaxWidth(160);

        // Email utente
        Label userEmail = new Label(currentUser.getEmail());
        userEmail.setTextFill(Color.LIGHTGRAY);
        userEmail.setFont(Font.font("System", 10));
        userEmail.setWrapText(true);
        userEmail.setMaxWidth(160);

        // Separatore
        Separator separator = new Separator();
        separator.setMaxWidth(140);

        // Pulsante gestione account
        Button manageButton = new Button("👤 Gestisci Account");
        manageButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4a86e8;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 11px;"
        );
        manageButton.setOnAction(e -> {
            // Apri popup gestione utente
            UserProfilePopup profilePopup = new UserProfilePopup(authManager, () -> {
                System.out.println("👤 Callback logout da profile popup");
                authManager.logout();
            });
            profilePopup.show(mainWindow.getMainRoot());
        });

        userWidget.getChildren().addAll(avatar, userName, userEmail, separator, manageButton);
        VBox.setMargin(userWidget, new Insets(0, 15, 15, 15));

        return userWidget;
    }

    /**
     * Crea avatar circolare personalizzato con iniziali dell'utente.
     * <p>
     * Genera un avatar visivamente accattivante utilizzando le iniziali
     * dell'utente su sfondo circolare colorato. Sistema completamente
     * automatico che estrae iniziali dal nome utente e le presenta
     * in formato standardizzato.
     * </p>
     *
     * <h4>Design avatar:</h4>
     * <ul>
     *   <li><strong>Dimensions:</strong> Cerchio 40x40 pixel</li>
     *   <li><strong>Background:</strong> Colore blu accento (#4a86e8)</li>
     *   <li><strong>Border:</strong> Contorno bianco 2px</li>
     *   <li><strong>Text:</strong> Iniziali centrate, font bold 14px</li>
     * </ul>
     *
     * <h4>Initials extraction:</h4>
     * <p>
     * Utilizza metodo {@link #getInitials(User)} per estrazione
     * intelligente iniziali con fallback multipli per garantire
     * sempre una rappresentazione visiva valida.
     * </p>
     *
     * @param user oggetto utente per estrazione informazioni
     * @return {@link StackPane} avatar circolare configurato
     */
    private StackPane createUserAvatar(User user) {
        StackPane avatar = new StackPane();
        avatar.setMaxSize(40, 40);
        avatar.setMinSize(40, 40);

        // Cerchio di sfondo
        Circle circle = new Circle(20);
        circle.setFill(Color.web("#4a86e8"));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2);

        // Iniziali
        String initials = getInitials(user);
        Label initialsLabel = new Label(initials);
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        avatar.getChildren().addAll(circle, initialsLabel);
        return avatar;
    }

    /**
     * Estrae iniziali dall'utente con strategia fallback robusta.
     * <p>
     * Sistema intelligente per estrazione iniziali che prova múltiples
     * strategie per garantire sempre un risultato valido. Implementa
     * logica di fallback progressiva che utilizza nome, cognome, email
     * o username secondo disponibilità.
     * </p>
     *
     * <h4>Strategia estrazione:</h4>
     * <ol>
     *   <li><strong>Primary:</strong> Prima lettera nome + prima lettera cognome</li>
     *   <li><strong>Secondary:</strong> Solo nome se cognome non disponibile</li>
     *   <li><strong>Tertiary:</strong> Prima lettera email se nome non disponibile</li>
     *   <li><strong>Fallback:</strong> Prima lettera username</li>
     *   <li><strong>Ultimate:</strong> "?" se tutto fallisce</li>
     * </ol>
     *
     * <h4>Normalization:</h4>
     * <ul>
     *   <li>Trim whitespace automatico</li>
     *   <li>Uppercase conversion per consistenza</li>
     *   <li>Validazione non-empty strings</li>
     * </ul>
     *
     * <h4>Edge cases gestiti:</h4>
     * <ul>
     *   <li>Stringhe null o vuote</li>
     *   <li>Stringhe solo whitespace</li>
     *   <li>Utente con informazioni incomplete</li>
     *   <li>Caratteri speciali in nomi</li>
     * </ul>
     *
     * @param user oggetto utente da cui estrarre iniziali
     * @return stringa contenente 1-2 caratteri uppercase, mai null
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

        if (initials.length() == 0) {
            // Fallback con prima lettera dell'email o username
            String fallback = user.getEmail() != null ? user.getEmail() : user.getUsername();
            if (fallback != null && !fallback.trim().isEmpty()) {
                initials.append(fallback.trim().charAt(0));
            } else {
                initials.append("?");
            }
        }

        return initials.toString().toUpperCase();
    }

    /**
     * Crea pulsanti autenticazione standardizzati con styling coerente.
     * <p>
     * Factory method per creazione pulsanti (login/logout) con design
     * uniforme e comportamento consistente. Applica styling tema
     * applicazione e configura hover effects automaticamente.
     * </p>
     *
     * <h4>Configurazione pulsante:</h4>
     * <ul>
     *   <li><strong>Width:</strong> 170px fixed per consistenza layout</li>
     *   <li><strong>Font:</strong> System 12px normal weight</li>
     *   <li><strong>Colors:</strong> Blu accento su background trasparente</li>
     *   <li><strong>Border:</strong> Bordo blu con radius 8px</li>
     *   <li><strong>Padding:</strong> 8px verticale, 15px orizzontale</li>
     * </ul>
     *
     * <h4>Hover effects:</h4>
     * <p>
     * Applica automaticamente effetti hover standardizzati tramite
     * {@link #setupButtonHoverEffects(Button)} per feedback visivo
     * consistente su tutti i pulsanti autenticazione.
     * </p>
     *
     * <h4>Action integration:</h4>
     * <p>
     * Accetta Runnable per massima flessibilità nell'integrazione
     * con diverse operazioni (login, logout, ecc.).
     * </p>
     *
     * @param text testo da visualizzare sul pulsante
     * @param action azione da eseguire al click
     * @return {@link Button} configurato con styling e azione
     */
    private Button createAuthButton(String text, Runnable action) {
        Button authButton = new Button(text);
        authButton.setFont(Font.font("System", FontWeight.NORMAL, 12));
        authButton.setPrefWidth(170);

        String baseStyle =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4a86e8;" +
                        "-fx-border-color: #4a86e8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-size: 12px;";

        authButton.setStyle(baseStyle);
        authButton.setOnAction(e -> action.run());

        // Hover effects
        setupButtonHoverEffects(authButton);

        VBox.setMargin(authButton, new Insets(0, 15, 15, 15));
        return authButton;
    }

    /**
     * Configura effetti hover standardizzati per pulsanti autenticazione.
     * <p>
     * Applica sistema di hover effects coerente che fornisce feedback
     * visivo appropriato per le interazioni utente. Include stati
     * normal, hover, pressed con transizioni smooth tra stati.
     * </p>
     *
     * <h4>Stati configurati:</h4>
     * <ul>
     *   <li><strong>Normal:</strong> Background trasparente, bordo blu</li>
     *   <li><strong>Hover:</strong> Background blu semi-trasparente</li>
     *   <li><strong>Pressed:</strong> Colori più scuri per feedback immediato</li>
     * </ul>
     *
     * <h4>Event handlers:</h4>
     * <ul>
     *   <li>onMouseEntered: Applica stile hover</li>
     *   <li>onMouseExited: Ripristina stile normale</li>
     *   <li>onMousePressed: Applica stile pressed</li>
     *   <li>onMouseReleased: Ripristina stile normale</li>
     * </ul>
     *
     * @param button pulsante a cui applicare gli effetti hover
     */
    private void setupButtonHoverEffects(Button button) {
        String baseStyle =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4a86e8;" +
                        "-fx-border-color: #4a86e8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-size: 12px;";

        String hoverStyle = baseStyle.replace("transparent", "rgba(74, 134, 232, 0.1)");
        String pressStyle = baseStyle.replace("#4a86e8", "derive(#4a86e8, -20%)");

        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnMousePressed(e -> button.setStyle(pressStyle));
        button.setOnMouseReleased(e -> button.setStyle(baseStyle));
    }

    /**
     * Aggiorna lo stato attivo del menu preservando la coerenza visiva.
     * <p>
     * Gestisce la transizione dello stato attivo tra elementi menu,
     * applicando styling appropriato per evidenziare la sezione corrente
     * e rimuovere evidenziazione da elementi precedentemente attivi.
     * </p>
     *
     * <h4>Processo aggiornamento:</h4>
     * <ol>
     *   <li>Verifica validità menuItemsBox e contenuti</li>
     *   <li>Rimuove stato attivo da tutti gli elementi</li>
     *   <li>Applica stato attivo al nuovo elemento selezionato</li>
     *   <li>Aggiorna indice interno per tracking</li>
     * </ol>
     *
     * <h4>Styling applicato:</h4>
     * <ul>
     *   <li><strong>Active:</strong> Background #3a3a3c, testo bianco</li>
     *   <li><strong>Inactive:</strong> Background trasparente, testo grigio</li>
     * </ul>
     *
     * <h4>Error handling:</h4>
     * <p>
     * Include verifiche di sicurezza per null container e indici
     * fuori range per prevenire errori durante aggiornamenti UI.
     * </p>
     *
     * @param newActiveIndex indice del nuovo elemento da rendere attivo
     */
    private void updateActiveMenuItem(int newActiveIndex) {
        if (menuItemsBox == null || menuItemsBox.getChildren().isEmpty()) {
            return;
        }

        // Rimuovi lo stato attivo da tutti gli elementi
        for (int i = 0; i < menuItemsBox.getChildren().size(); i++) {
            HBox item = (HBox) menuItemsBox.getChildren().get(i);
            Label label = (Label) item.getChildren().get(0);

            if (i == newActiveIndex) {
                item.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 5; -fx-cursor: hand;");
                label.setTextFill(Color.WHITE);
            } else {
                item.setStyle("-fx-cursor: hand;");
                label.setTextFill(Color.LIGHTGRAY);
            }
        }

        activeMenuIndex = newActiveIndex;
        System.out.println("📋 Menu attivo aggiornato: indice " + newActiveIndex);
    }

    /**
     * Aggiorna dinamicamente la sezione autenticazione in risposta a cambi stato utente.
     * <p>
     * Metodo pubblico chiamato dall'AuthenticationManager tramite callback
     * per mantenere sincronizzata l'interfaccia sidebar con lo stato di
     * autenticazione corrente. Gestisce refresh completo di entrambe
     * le sezioni dinamiche (auth e menu).
     * </p>
     *
     * <h4>Operazioni refresh:</h4>
     * <ul>
     *   <li><strong>Auth Section:</strong> Ricostruzione widget/pulsanti utente</li>
     *   <li><strong>Menu Items:</strong> Aggiornamento menu dinamici (admin)</li>
     * </ul>
     *
     * <h4>Thread safety:</h4>
     * <p>
     * Progettato per essere chiamato da thread JavaFX per garantire
     * sicurezza negli aggiornamenti UI. Include logging dettagliato
     * per troubleshooting durante sviluppo.
     * </p>
     *
     * <h4>Error handling:</h4>
     * <p>
     * Include verifiche null safety per prevenire errori durante
     * refresh in scenari edge case o inizializzazione parziale.
     * </p>
     *
     * <h4>Callback integration:</h4>
     * <p>
     * Questo metodo è tipicamente invocato automaticamente dal sistema
     * di callback dell'AuthenticationManager quando lo stato di login
     * cambia, garantendo aggiornamenti UI immediati.
     * </p>
     */
    public void refreshAuthSection() {
        System.out.println("🔄 Refreshing sidebar auth section...");

        // Aggiorna sezione autenticazione
        if (authSection != null) {
            updateAuthSection();
            System.out.println("✅ Sidebar auth section refreshed");
        } else {
            System.err.println("❌ Auth section is null, cannot refresh");
        }

        refreshMenuItems();
    }

    /**
     * Ricostruisce dinamicamente gli elementi menu per adattarsi ai privilegi utente correnti.
     * <p>
     * Gestisce l'aggiornamento completo del sistema di menu per riflettere
     * cambiamenti nei privilegi utente, particolarmente per l'apparizione/
     * scomparsa del menu amministrativo basato sullo stato di autenticazione.
     * </p>
     *
     * <h4>Processo ricostruzione:</h4>
     * <ol>
     *   <li>Salvataggio indice menu attivo corrente</li>
     *   <li>Ricostruzione completa menu items</li>
     *   <li>Sostituzione contenuto menuItemsBox</li>
     *   <li>Ripristino stato attivo o fallback a Home</li>
     * </ol>
     *
     * <h4>State preservation:</h4>
     * <p>
     * Tenta di preservare la sezione attiva corrente durante il refresh,
     * ma include logica di fallback sicura se l'indice precedente non è
     * più valido (es. menu admin rimosso).
     * </p>
     *
     * <h4>Admin menu handling:</h4>
     * <p>
     * Il menu "Gestione" appare/scompare dinamicamente basandosi sui
     * privilegi dell'utente corrente, richiedendo ricostruzione completa
     * del sistema di menu per mantenere coerenza.
     * </p>
     *
     * <h4>Error recovery:</h4>
     * <ul>
     *   <li>Null safety per menuItemsBox</li>
     *   <li>Index validation per stato attivo</li>
     *   <li>Fallback automatico a Home se necessario</li>
     * </ul>
     */
    private void refreshMenuItems() {
        System.out.println("🔄 Refreshing menu items...");

        if (menuItemsBox == null) {
            System.err.println("❌ menuItemsBox is null, cannot refresh");
            return;
        }

        // Salva l'indice attivo attuale
        int currentActiveIndex = activeMenuIndex;

        // Ricostruisci il menu
        VBox newMenuItems = createMenuItems();

        // Sostituisci il contenuto del menuItemsBox
        menuItemsBox.getChildren().clear();
        menuItemsBox.getChildren().addAll(newMenuItems.getChildren());

        // Ripristina lo stato attivo, ma verifica che l'indice sia valido
        if (currentActiveIndex < menuItemsBox.getChildren().size()) {
            updateActiveMenuItem(currentActiveIndex);
        } else {
            updateActiveMenuItem(0); // Torna a Home se l'indice non è più valido
        }

        System.out.println("✅ Menu items refreshed");
    }

    // =====================================================
    // GETTER E METODI DI UTILITÀ
    // =====================================================

    /**
     * Restituisce il gestore autenticazione associato alla sidebar.
     * <p>
     * Accessor per l'AuthenticationManager utilizzato dalla sidebar
     * per verifiche stato utente e integrazione con sistema di
     * autenticazione dell'applicazione.
     * </p>
     *
     * @return {@link AuthenticationManager} gestore autenticazione
     */
    public AuthenticationManager getAuthManager() {
        return authManager;
    }
}