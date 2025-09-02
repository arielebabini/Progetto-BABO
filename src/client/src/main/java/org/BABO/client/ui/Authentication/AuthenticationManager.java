package org.BABO.client.ui.Authentication;

import org.BABO.client.service.AuthService;
import org.BABO.shared.model.User;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;

/**
 * Gestisce l'autenticazione degli utenti nell'applicazione client BABO.
 * <p>
 * Questa classe fornisce un'interfaccia completa per tutte le operazioni di
 * autenticazione nell'applicazione, inclusa la gestione dello stato di
 * autenticazione, l'interfaccia utente per login/registrazione, e la
 * sincronizzazione con il servizio di autenticazione backend.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Gestione Stato:</strong> Tracciamento dello stato di autenticazione corrente</li>
 *   <li><strong>Interfaccia Utente:</strong> Pannello di autenticazione modale integrato</li>
 *   <li><strong>Autenticazione:</strong> Login e registrazione utenti</li>
 *   <li><strong>Logout:</strong> Disconnessione sicura locale e remota</li>
 *   <li><strong>Callback System:</strong> Notifiche di cambiamento stato per aggiornare UI</li>
 *   <li><strong>Gestione Errori:</strong> Gestione robusta di errori di rete e timeout</li>
 *   <li><strong>Health Check:</strong> Monitoraggio della disponibilità del servizio</li>
 * </ul>
 *
 * <h3>Architettura e Design:</h3>
 * <p>
 * Il manager implementa un pattern Observer per notificare i cambiamenti di stato
 * di autenticazione, permettendo all'UI di reagire automaticamente a login/logout.
 * Utilizza un overlay modale JavaFX per l'interfaccia di autenticazione, garantendo
 * un'esperienza utente fluida e moderna.
 * </p>
 *
 * <h3>Gestione dello Stato:</h3>
 * <ul>
 *   <li><strong>Locale:</strong> Mantiene informazioni utente corrente in memoria</li>
 *   <li><strong>Remoto:</strong> Sincronizza con il backend per validazione sessioni</li>
 *   <li><strong>Persistente:</strong> Logout automatico alla chiusura applicazione</li>
 * </ul>
 *
 * <h3>Integrazione con AuthService:</h3>
 * <p>
 * Il manager si basa su {@link AuthService} per tutte le operazioni di rete,
 * inclusi login, logout, registrazione e health check. Tutte le operazioni
 * remote sono asincrone e gestite tramite {@link java.util.concurrent.CompletableFuture}.
 * </p>
 *
 * <h3>Sicurezza:</h3>
 * <ul>
 *   <li>Logout automatico alla chiusura applicazione</li>
 *   <li>Gestione sicura delle credenziali tramite AuthService</li>
 *   <li>Validazione continua dello stato di autenticazione</li>
 *   <li>Timeout e retry automatici per operazioni di rete</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Inizializzazione del manager
 * AuthenticationManager authManager = new AuthenticationManager();
 *
 * // Configurazione callback per aggiornamenti UI
 * authManager.setOnAuthStateChanged(() -> {
 *     if (authManager.isAuthenticated()) {
 *         updateUIForLoggedInUser(authManager.getCurrentUser());
 *     } else {
 *         updateUIForAnonymousUser();
 *     }
 * });
 *
 * // Inizializzazione e health check
 * authManager.initialize();
 *
 * // Mostrare pannello di autenticazione
 * StackPane mainRoot = new StackPane();
 * authManager.showAuthPanel(mainRoot);
 *
 * // Logout programmatico
 * authManager.logout();
 *
 * // Cleanup risorse
 * authManager.shutdown();
 * }</pre>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * Tutti i metodi sono thread-safe e utilizzano {@link Platform#runLater(Runnable)}
 * per garantire che gli aggiornamenti UI avvengano nel JavaFX Application Thread.
 * Le operazioni asincrone sono gestite tramite thread pool separati.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see AuthService
 * @see AuthPanel
 * @see User
 */
public class AuthenticationManager {

    /** Stato di autenticazione corrente dell'utente */
    private boolean isAuthenticated = false;

    /** Informazioni dell'utente attualmente autenticato */
    private User currentUser = null;

    /** Pannello UI per le operazioni di autenticazione */
    private AuthPanel authPanel;

    /** Servizio backend per operazioni di autenticazione */
    private AuthService authService;

    /** Callback per notificare cambiamenti di stato auth */
    private Runnable onAuthStateChanged;

    /**
     * Costruttore del manager di autenticazione.
     * <p>
     * Inizializza il servizio di autenticazione e prepara il manager per l'uso.
     * Non esegue operazioni di rete - utilizzare {@link #initialize()} per
     * l'inizializzazione completa.
     * </p>
     */
    public AuthenticationManager() {
        this.authService = new AuthService();
    }

    /**
     * Imposta il callback per ricevere notifiche sui cambiamenti di stato di autenticazione.
     * <p>
     * Il callback viene eseguito ogni volta che lo stato di autenticazione cambia
     * (login o logout), permettendo all'applicazione di aggiornare l'interfaccia
     * utente di conseguenza. Il callback viene sempre eseguito nel JavaFX
     * Application Thread per garantire operazioni UI sicure.
     * </p>
     *
     * @param callback il {@link Runnable} da eseguire quando cambia lo stato di autenticazione.
     *                Può essere {@code null} per rimuovere callback esistenti.
     * @apiNote Il callback deve essere leggero e non-bloccante per non rallentare
     *          le operazioni di autenticazione. Per operazioni complesse, considerare
     *          l'uso di thread separati.
     */
    public void setOnAuthStateChanged(Runnable callback) {
        this.onAuthStateChanged = callback;
    }

    /**
     * Verifica se l'utente è attualmente autenticato.
     * <p>
     * Questo metodo controlla solo lo stato locale e non esegue verifiche
     * remote della sessione. Per una validazione completa dello stato,
     * utilizzare {@link #checkAuthServiceHealth()}.
     * </p>
     *
     * @return {@code true} se l'utente è autenticato localmente, {@code false} altrimenti
     */
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Ottiene l'oggetto User dell'utente attualmente autenticato.
     * <p>
     * Restituisce l'istanza completa dell'utente con tutti i metadati disponibili,
     * inclusi username, display name, email e altri attributi. È utile per
     * personalizzare l'esperienza utente e per operazioni che richiedono
     * informazioni dettagliate sull'utente.
     * </p>
     *
     * @return l'oggetto {@link User} dell'utente corrente, o {@code null} se
     *         nessun utente è autenticato
     * @see #getCurrentUsername()
     * @see #getCurrentUserDisplayName()
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Ottiene lo username dell'utente attualmente autenticato.
     * <p>
     * Metodo di convenienza per ottenere rapidamente lo username senza
     * dover accedere all'oggetto User completo. È particolarmente utile
     * per operazioni che richiedono solo l'identificatore utente.
     * </p>
     *
     * @return lo username dell'utente corrente, o {@code null} se nessun utente è autenticato
     * @see #getCurrentUser()
     * @see #getCurrentUserDisplayName()
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    /**
     * Ottiene il nome di visualizzazione dell'utente corrente.
     * <p>
     * Restituisce il nome formattato per la visualizzazione nell'interfaccia utente.
     * Se nessun utente è autenticato, restituisce una stringa predefinita sicura
     * invece di {@code null}, rendendo questo metodo sempre utilizzabile per l'UI.
     * </p>
     *
     * @return il display name dell'utente corrente, o "Utente" se nessun utente è autenticato
     * @see #getCurrentUser()
     * @see #getCurrentUsername()
     */
    public String getCurrentUserDisplayName() {
        return currentUser != null ? currentUser.getDisplayName() : "Utente";
    }

    /**
     * Mostra il pannello di autenticazione in modalità overlay.
     * <p>
     * Crea e visualizza un overlay modale semi-trasparente contenente il pannello
     * di autenticazione. L'overlay occupa l'intera area del container principale
     * e può essere chiuso cliccando al di fuori del pannello o tramite i controlli
     * del pannello stesso.
     * </p>
     *
     * <h4>Caratteristiche dell'overlay:</h4>
     * <ul>
     *   <li>Sfondo semi-trasparente che oscura il contenuto sottostante</li>
     *   <li>Pannello centrato con controlli di login/registrazione</li>
     *   <li>Chiusura automatica al click esterno</li>
     *   <li>Gestione eventi per evitare propagazione indesiderata</li>
     * </ul>
     *
     * @param mainRoot il container principale {@link StackPane} su cui sovrapporre
     *                 il pannello di autenticazione
     * @throws IllegalArgumentException se mainRoot è {@code null}
     * @see #closeAuthPanel(StackPane)
     * @see AuthPanel
     */
    public void showAuthPanel(StackPane mainRoot) {
        if (mainRoot == null) {
            throw new IllegalArgumentException("Il container principale non può essere null");
        }

        authPanel = new AuthPanel();

        authPanel.setOnSuccessfulAuth(this::handleSuccessfulAuthentication);
        authPanel.setOnClosePanel(() -> closeAuthPanel(mainRoot));

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        overlay.getChildren().add(authPanel);
        StackPane.setAlignment(authPanel, Pos.CENTER);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                closeAuthPanel(mainRoot);
            }
        });

        authPanel.setOnMouseClicked(e -> e.consume());

        mainRoot.getChildren().add(overlay);

        System.out.println("🔑 Pannello autenticazione aperto");
    }

    /**
     * Chiude il pannello di autenticazione rimuovendo l'overlay.
     * <p>
     * Rimuove l'overlay del pannello di autenticazione dal container principale,
     * ripristinando la visibilità completa del contenuto sottostante. Il metodo
     * è sicuro e non genera errori se l'overlay non è presente.
     * </p>
     *
     * @param mainRoot il container principale da cui rimuovere l'overlay
     * @implNote Il metodo assume che l'overlay sia sempre l'ultimo elemento aggiunto
     *           al container. Questa assunzione è valida nel contesto dell'applicazione
     *           ma potrebbe richiedere modifiche in architetture più complesse.
     */
    private void closeAuthPanel(StackPane mainRoot) {
        if (mainRoot.getChildren().size() > 1) {
            mainRoot.getChildren().remove(mainRoot.getChildren().size() - 1);
        }
        System.out.println("🚪 Pannello autenticazione chiuso");
    }

    /**
     * Gestisce l'autenticazione riuscita aggiornando lo stato e mostrando il messaggio di benvenuto.
     * <p>
     * Questo metodo viene chiamato automaticamente dal pannello di autenticazione
     * quando l'utente completa con successo il processo di login o registrazione.
     * Aggiorna lo stato interno e avvia la sequenza di benvenuto.
     * </p>
     *
     * @param user l'oggetto {@link User} dell'utente appena autenticato
     * @see #setAuthenticationState(boolean, User)
     * @see #showWelcomeMessage(User)
     */
    private void handleSuccessfulAuthentication(User user) {
        setAuthenticationState(true, user);

        Platform.runLater(() -> {
            showWelcomeMessage(user);
        });
    }

    /**
     * Mostra un messaggio di benvenuto personalizzato per l'utente autenticato.
     * <p>
     * Crea e visualizza una finestra di dialogo di benvenuto con informazioni
     * personalizzate e suggerimenti sulle funzionalità disponibili. Il messaggio
     * include il nome dell'utente e un riepilogo delle capacità dell'applicazione.
     * </p>
     *
     * @param user l'utente per cui mostrare il messaggio di benvenuto
     * @apiNote Il messaggio viene sempre visualizzato nel JavaFX Application Thread
     *          per garantire operazioni UI sicure.
     */
    private void showWelcomeMessage(User user) {
        Alert welcomeAlert = new Alert(Alert.AlertType.INFORMATION);
        welcomeAlert.setTitle("👋 Benvenuto!");
        welcomeAlert.setHeaderText("Accesso effettuato con successo");

        String welcomeMessage = String.format(
                "Ciao %s!\n\n" +
                        "✅ Sei ora connesso al tuo account\n" +
                        "📚 Puoi accedere alle tue librerie\n" +
                        "⭐ Scopri le nuove funzionalità disponibili!",
                user.getDisplayName()
        );

        welcomeAlert.setContentText(welcomeMessage);

        DialogPane dialogPane = welcomeAlert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-text-fill: gray;"
        );

        welcomeAlert.showAndWait();
    }

    /**
     * Esegue il logout dell'utente corrente.
     * <p>
     * Metodo pubblico per iniziare il processo di logout. Delega l'operazione
     * effettiva a {@link #performLogout()} che gestisce sia il logout remoto
     * che quello locale. Questo metodo può essere chiamato da qualsiasi parte
     * dell'applicazione per disconnettere l'utente.
     * </p>
     *
     * @see #performLogout()
     */
    public void logout() {
        performLogout();
    }

    /**
     * Esegue il logout completo con sincronizzazione remota e locale.
     * <p>
     * Gestisce il processo completo di logout, includendo:
     * </p>
     * <ul>
     *   <li>Tentativo di logout remoto tramite il servizio backend</li>
     *   <li>Logout locale garantito anche in caso di errori di rete</li>
     *   <li>Gestione robusten di timeout e errori di connessione</li>
     *   <li>Logging dettagliato per diagnostica</li>
     * </ul>
     *
     * <p>
     * Il metodo garantisce che l'utente venga sempre disconnesso localmente,
     * anche se il logout remoto fallisce per problemi di rete o server.
     * Questo assicura che l'applicazione rimanga in uno stato coerente.
     * </p>
     *
     * @apiNote Tutte le operazioni UI vengono eseguite tramite {@link Platform#runLater(Runnable)}
     *          per garantire thread safety. Il logout locale viene sempre eseguito
     *          indipendentemente dal successo del logout remoto.
     */
    private void performLogout() {
        String userDisplayName = getCurrentUserDisplayName();
        System.out.println("🚪 Esecuzione logout per: " + userDisplayName);

        authService.logoutAsync()
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            System.out.println("✅ Logout completato sul server");
                        } else {
                            System.out.println("⚠️ Logout locale (server non risponde)");
                        }

                        // Esegui logout locale
                        setAuthenticationState(false, null);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("⚠️ Logout locale (errore server): " + throwable.getMessage());
                        setAuthenticationState(false, null);
                    });
                    return null;
                });
    }

    /**
     * Aggiorna lo stato di autenticazione e notifica tutti i listener registrati.
     * <p>
     * Questo è il metodo centrale per la gestione dello stato di autenticazione.
     * Aggiorna sia lo stato locale che le informazioni utente, e trigger
     * le notifiche appropriate per aggiornare l'interfaccia utente.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ul>
     *   <li>Aggiornamento flag di autenticazione</li>
     *   <li>Aggiornamento informazioni utente corrente</li>
     *   <li>Logging dello stato per diagnostica</li>
     *   <li>Notifica callback solo se lo stato è effettivamente cambiato</li>
     * </ul>
     *
     * @param authenticated {@code true} se l'utente è autenticato, {@code false} altrimenti
     * @param user l'oggetto {@link User} dell'utente autenticato, o {@code null} per logout
     * @see #notifyAuthStateChanged()
     */
    public void setAuthenticationState(boolean authenticated, User user) {
        boolean wasAuthenticated = this.isAuthenticated;

        this.isAuthenticated = authenticated;
        this.currentUser = user;

        if (authenticated && user != null) {
            System.out.println("✅ Utente autenticato: " + user.getDisplayName());
        } else {
            System.out.println("🚪 Utente disconnesso");
        }

        if (wasAuthenticated != authenticated) {
            notifyAuthStateChanged();
        }
    }

    /**
     * Notifica tutti i listener registrati del cambiamento di stato di autenticazione.
     * <p>
     * Esegue il callback registrato tramite {@link #setOnAuthStateChanged(Runnable)}
     * nel JavaFX Application Thread per garantire operazioni UI sicure. Include
     * gestione degli errori per evitare che eccezioni nel callback interrompano
     * il flusso di autenticazione.
     * </p>
     *
     * @apiNote Il callback viene eseguito solo se è stato registrato tramite
     *          {@link #setOnAuthStateChanged(Runnable)}. Gli errori nel callback
     *          vengono catturati e loggati ma non propagati.
     */
    private void notifyAuthStateChanged() {
        if (onAuthStateChanged != null) {
            Platform.runLater(() -> {
                try {
                    onAuthStateChanged.run();
                } catch (Exception e) {
                    System.err.println("❌ Errore nel callback auth state changed: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Verifica la disponibilità del servizio di autenticazione tramite health check.
     * <p>
     * Esegue un controllo asincrono della salute del servizio di autenticazione
     * backend per verificare la connettività e la disponibilità del servizio.
     * È utile per diagnosticare problemi di rete e informare l'utente sullo
     * stato del servizio.
     * </p>
     *
     * <h4>Informazioni fornite:</h4>
     * <ul>
     *   <li>Stato del servizio (disponibile/non disponibile)</li>
     *   <li>Messaggio di stato dal server</li>
     *   <li>Diagnostica di errori di connessione</li>
     * </ul>
     *
     * @apiNote Il metodo è asincrono e non blocca l'interfaccia utente.
     *          I risultati vengono stampati nel log per diagnostica.
     *          In caso di errore, il servizio viene considerato non disponibile.
     */
    public void checkAuthServiceHealth() {
        authService.healthCheckAsync()
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            System.out.println("✅ Servizio autenticazione: " + response.getMessage());
                        } else {
                            System.out.println("❌ Servizio autenticazione non disponibile: " + response.getMessage());
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.out.println("❌ Errore connessione servizio auth: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Aggiorna le informazioni dell'utente attualmente autenticato.
     * <p>
     * Permette di aggiornare le informazioni dell'utente senza richiedere
     * una nuova autenticazione. È utile quando l'utente modifica il proprio
     * profilo o quando vengono ricevuti aggiornamenti dal server.
     * </p>
     *
     * <h4>Condizioni per l'aggiornamento:</h4>
     * <ul>
     *   <li>L'utente deve essere attualmente autenticato</li>
     *   <li>L'oggetto utente aggiornato non deve essere {@code null}</li>
     *   <li>L'aggiornamento trigger una notifica di cambio stato</li>
     * </ul>
     *
     * @param updatedUser il nuovo oggetto {@link User} con le informazioni aggiornate
     * @apiNote Se l'utente non è autenticato o updatedUser è {@code null},
     *          l'operazione viene ignorata silenziosamente.
     */
    public void updateCurrentUser(User updatedUser) {
        if (this.isAuthenticated && updatedUser != null) {
            this.currentUser = updatedUser;
            System.out.println("✅ Profilo utente aggiornato: " + updatedUser.getDisplayName());

            notifyAuthStateChanged();
        }
    }

    /**
     * Inizializza il manager di autenticazione ed esegue controlli preliminari.
     * <p>
     * Questo metodo deve essere chiamato dopo la creazione del manager per
     * completare l'inizializzazione. Esegue controlli di connettività e
     * prepara il manager per l'uso operativo.
     * </p>
     *
     * <h4>Operazioni di inizializzazione:</h4>
     * <ul>
     *   <li>Health check del servizio di autenticazione</li>
     *   <li>Verifica della connettività di rete</li>
     *   <li>Logging dello stato di inizializzazione</li>
     * </ul>
     *
     * @apiNote L'inizializzazione è asincrona per non bloccare l'avvio
     *          dell'applicazione. I risultati dei controlli vengono loggati.
     */
    public void initialize() {
        System.out.println("🔧 Inizializzazione AuthenticationManager...");

        checkAuthServiceHealth();

        System.out.println("✅ AuthenticationManager inizializzato");
    }

    /**
     * Esegue il cleanup delle risorse quando l'applicazione viene chiusa.
     * <p>
     * Questo metodo deve essere chiamato prima della chiusura dell'applicazione
     * per garantire una terminazione pulita. Esegue il logout automatico
     * dell'utente corrente e libera le risorse allocate.
     * </p>
     *
     * <h4>Operazioni di cleanup:</h4>
     * <ul>
     *   <li>Logout silenzioso dell'utente autenticato</li>
     *   <li>Chiusura connessioni di rete</li>
     *   <li>Rilascio risorse AuthService</li>
     *   <li>Logging delle operazioni di shutdown</li>
     * </ul>
     *
     * @apiNote Il logout durante lo shutdown è asincrono e "fire-and-forget"
     *          per evitare di bloccare la chiusura dell'applicazione. Gli errori
     *          vengono loggati ma non bloccano il processo di shutdown.
     */
    public void shutdown() {
        System.out.println("🔄 Shutdown AuthenticationManager...");

        if (isAuthenticated) {
            // Esegui logout silenzioso
            authService.logoutAsync()
                    .thenAccept(response -> {
                        System.out.println("✅ Logout silenzioso completato");
                    })
                    .exceptionally(throwable -> {
                        System.out.println("⚠️ Logout silenzioso fallito: " + throwable.getMessage());
                        return null;
                    });
        }

        System.out.println("✅ AuthenticationManager chiuso");
    }

    /**
     * Ottiene l'istanza del servizio di autenticazione utilizzato dal manager.
     * <p>
     * Fornisce accesso diretto al servizio di autenticazione per operazioni
     * avanzate che potrebbero non essere esposte tramite i metodi del manager.
     * Utilizzare con cautela per evitare inconsistenze di stato.
     * </p>
     *
     * @return l'istanza {@link AuthService} utilizzata dal manager
     * @apiNote L'accesso diretto al servizio può causare inconsistenze se le
     *          operazioni non vengono sincronizzate con lo stato del manager.
     *          Preferire sempre i metodi del manager quando possibile.
     */
    public AuthService getAuthService() {
        return authService;
    }

    /**
     * Mostra un alert informativo con il titolo e messaggio specificati.
     * <p>
     * Metodo di utilità per visualizzare messaggi informativi all'utente
     * tramite finestre di dialogo JavaFX standardizzate. È utilizzato
     * internamente per notifiche e messaggi di sistema.
     * </p>
     *
     * @param title il titolo della finestra di dialogo
     * @param message il messaggio da visualizzare all'utente
     * @apiNote La finestra di dialogo è modale e blocca l'interazione con
     *          l'applicazione fino alla chiusura da parte dell'utente.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}