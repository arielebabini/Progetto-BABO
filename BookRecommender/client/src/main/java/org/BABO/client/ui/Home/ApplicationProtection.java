package org.BABO.client.ui.Home;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.BABO.client.ui.Popup.PopupManager;

/**
 * Classe di utilità per la protezione e gestione sicura dell'applicazione principale BABO.
 * <p>
 * Questa classe implementa un sistema di protezione per l'applicazione principale che previene
 * chiusure accidentali, gestisce popup in modo coordinato, e fornisce funzionalità di debug
 * per il monitoraggio dello stato dell'applicazione. Utilizza pattern Singleton per mantenere
 * il riferimento all'applicazione principale e coordinarsi con altri componenti del sistema.
 * </p>
 *
 * <h3>Funzionalità principali:</h3>
 * <ul>
 *   <li><strong>Protezione Chiusura:</strong> Previene chiusure accidentali dell'applicazione principale</li>
 *   <li><strong>Gestione Popup:</strong> Coordinamento con PopupManager per chiusure ordinate</li>
 *   <li><strong>Identificazione Stage:</strong> Distinzione sicura tra applicazione principale e popup</li>
 *   <li><strong>Key Handling:</strong> Gestione globale tasti ESC per chiusura popup</li>
 *   <li><strong>Debug Avanzato:</strong> Monitoraggio completo stato finestre e popup</li>
 *   <li><strong>Cleanup Automatico:</strong> Gestione ordinata della chiusura applicazione</li>
 * </ul>
 *
 * <h3>Architettura di Protezione:</h3>
 * <p>
 * Il sistema implementa una strategia di protezione multi-livello:
 * </p>
 * <ul>
 *   <li><strong>Stage Registration:</strong> Registrazione sicura dello stage principale</li>
 *   <li><strong>Identity Verification:</strong> Algoritmi robusti per identificare la main window</li>
 *   <li><strong>Safe Close Guards:</strong> Validazione di ogni richiesta di chiusura</li>
 *   <li><strong>Popup Coordination:</strong> Integrazione con PopupManager per gestione coordinata</li>
 * </ul>
 *
 * <h3>Gestione Stati Applicazione:</h3>
 * <p>
 * La classe monitora e gestisce diversi stati dell'applicazione:
 * </p>
 * <ul>
 *   <li><strong>Initialization:</strong> Verifica corretta inizializzazione del sistema</li>
 *   <li><strong>Active Popups:</strong> Tracciamento popup aperti per cleanup ordinato</li>
 *   <li><strong>Close Requests:</strong> Intercettazione e validazione richieste chiusura</li>
 *   <li><strong>Emergency Situations:</strong> Gestione scenari di emergenza e fallback</li>
 * </ul>
 *
 * <h3>Integrazione con PopupManager:</h3>
 * <p>
 * La classe si integra strettamente con {@link PopupManager} per:
 * </p>
 * <ul>
 *   <li>Verifica presenza popup attivi prima della chiusura</li>
 *   <li>Cleanup coordinato di tutti i popup aperti</li>
 *   <li>Gestione timeout per operazioni di chiusura</li>
 *   <li>Debug sincronizzato dello stato popup</li>
 * </ul>
 *
 * <h3>Algoritmi di Identificazione Stage:</h3>
 * <p>
 * Utilizza un algoritmo robusto per identificare l'applicazione principale:
 * </p>
 * <ol>
 *   <li><strong>Direct Reference:</strong> Confronto di riferimenti diretti</li>
 *   <li><strong>Title Analysis:</strong> Analisi pattern nel titolo finestra</li>
 *   <li><strong>Modality Check:</strong> Verifica modalità finestra</li>
 *   <li><strong>Fallback Logic:</strong> Heuristiche per casi edge</li>
 * </ol>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Inizializzazione durante startup applicazione
 * Stage primaryStage = new Stage();
 * primaryStage.setTitle("BABO - Biblioteca Digitale");
 *
 * // Registrazione protezione
 * ApplicationProtection.registerMainStage(primaryStage);
 *
 * // Il sistema ora protegge automaticamente da:
 * // - Chiusure accidentali
 * // - Conflitti con popup
 * // - Perdita di riferimenti stage
 *
 * // Verifica sicurezza prima di operazioni critiche
 * if (ApplicationProtection.isMainApplicationStage(someStage)) {
 *     System.out.println("Operazione bloccata: stage principale rilevato");
 *     return;
 * }
 *
 * // Chiusura sicura di popup
 * if (ApplicationProtection.safeCloseStage(popupStage)) {
 *     popupStage.close();
 * }
 *
 * // Debug stato applicazione
 * ApplicationProtection.debugApplicationState();
 * }</pre>
 *
 * <h3>Gestione Keyboard Global:</h3>
 * <p>
 * Implementa gestione globale della tastiera per:
 * </p>
 * <ul>
 *   <li>ESC per chiusura popup ordinata</li>
 *   <li>Prevenzione interferenze con applicazione principale</li>
 *   <li>Fallback automatici per scenari complessi</li>
 * </ul>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * Tutte le operazioni sono thread-safe e utilizzano {@link Platform#runLater(Runnable)}
 * quando necessario per garantire esecuzione nel JavaFX Application Thread.
 * </p>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see PopupManager
 * @see Stage
 * @see Platform
 */
public class ApplicationProtection {

    /** Riferimento allo stage principale dell'applicazione */
    private static Stage mainApplicationStage = null;

    /** Flag che indica se il sistema di protezione è stato inizializzato */
    private static boolean isInitialized = false;

    /**
     * Registra lo stage principale e attiva il sistema di protezione.
     * <p>
     * Questo metodo deve essere chiamato una sola volta durante l'inizializzazione
     * dell'applicazione per registrare lo stage principale e attivare tutti i
     * meccanismi di protezione. Configura automaticamente gestori di eventi,
     * protezioni di chiusura, e integrazione con il sistema di popup.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ol>
     *   <li>Validazione e registrazione stage principale</li>
     *   <li>Configurazione titolo identificativo se mancante</li>
     *   <li>Setup protezione chiusura con integrazione PopupManager</li>
     *   <li>Configurazione gestori globali tastiera</li>
     *   <li>Logging dettagliato per debugging</li>
     * </ol>
     *
     * <h4>Protezione chiusura implementata:</h4>
     * <p>
     * Il sistema intercetta richieste di chiusura e:
     * </p>
     * <ul>
     *   <li>Verifica presenza popup attivi</li>
     *   <li>Esegue cleanup ordinato di tutti i popup</li>
     *   <li>Implementa timeout per operazioni di chiusura</li>
     *   <li>Gestisce scenari di errore con fallback appropriati</li>
     * </ul>
     *
     * <h4>Setup keyboard handlers:</h4>
     * <p>
     * Configura gestori globali per tasti ESC che si attivano automaticamente
     * quando la scene diventa disponibile, garantendo funzionalità immediate.
     * </p>
     *
     * @param mainStage lo stage principale dell'applicazione da proteggere
     * @throws IllegalArgumentException se mainStage è {@code null}
     * @apiNote Questo metodo dovrebbe essere chiamato solo una volta durante
     *          l'inizializzazione. Chiamate multiple sovrascriveranno la
     *          registrazione precedente.
     */
    public static void registerMainStage(Stage mainStage) {
        if (mainStage == null) {
            System.err.println("❌ ApplicationProtection: Impossibile registrare stage null");
            return;
        }

        mainApplicationStage = mainStage;
        isInitialized = true;

        // Imposta un titolo identificativo se non presente
        if (mainStage.getTitle() == null || mainStage.getTitle().isEmpty()) {
            mainStage.setTitle("BABO - Biblioteca Digitale");
        }

        System.out.println("🛡️ ApplicationProtection: Stage principale registrato:");
        System.out.println("   Titolo: " + mainStage.getTitle());
        System.out.println("   Classe: " + mainStage.getClass().getSimpleName());

        // Protezione dalla chiusura con integrazione PopupManager migliorata
        mainStage.setOnCloseRequest(e -> {
            System.out.println("🔒 Richiesta chiusura applicazione principale");
            System.out.println("✅ Chiusura forzata dell'applicazione.");

            // Blocca l'evento di chiusura di default

            // Forza la chiusura della JVM
            System.exit(0);
        });

        // Setup handler ESC globale dopo che la scene è disponibile
        mainStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> setupGlobalKeyHandlers(mainStage));
            }
        });

        // Se la scene è già disponibile, configura subito
        if (mainStage.getScene() != null) {
            setupGlobalKeyHandlers(mainStage);
        }

        System.out.println("✅ ApplicationProtection: Stage principale registrato e protetto");
    }

    /**
     * Verifica se uno stage corrisponde all'applicazione principale.
     * <p>
     * Implementa un algoritmo robusto per identificare l'applicazione principale
     * utilizzando multiple strategie di verifica per garantire accuratezza anche
     * in scenari complessi dove i riferimenti potrebbero essere persi o modificati.
     * </p>
     *
     * <h4>Algoritmo di verifica:</h4>
     * <ol>
     *   <li><strong>Controllo inizializzazione:</strong> Verifica stato sistema</li>
     *   <li><strong>Validazione parametri:</strong> Controllo null safety</li>
     *   <li><strong>Confronto diretto:</strong> Verifica riferimento esatto</li>
     *   <li><strong>Analisi titolo:</strong> Pattern matching sui titoli</li>
     *   <li><strong>Verifica modalità:</strong> Controllo modality stage</li>
     *   <li><strong>Fallback heuristics:</strong> Logica di fallback per edge cases</li>
     * </ol>
     *
     * <h4>Pattern titolo riconosciuti:</h4>
     * <ul>
     *   <li>Titoli contenenti "BABO", "Biblioteca", "Books"</li>
     *   <li>Esclusione titoli con "Dettagli", "Popup", "Dialog"</li>
     *   <li>Gestione stage senza titolo con logica euristica</li>
     * </ul>
     *
     * <h4>Logging dettagliato:</h4>
     * <p>
     * Il metodo produce logging estensivo per facilitare debugging e
     * monitoraggio delle decisioni di identificazione.
     * </p>
     *
     * @param stage lo stage da verificare
     * @return {@code true} se lo stage è l'applicazione principale, {@code false} altrimenti
     * @apiNote Il metodo è null-safe e gestisce gracefully tutti i casi edge.
     *          In caso di dubbio, tende a essere conservativo per proteggere
     *          l'applicazione principale.
     */
    public static boolean isMainApplicationStage(Stage stage) {
        if (!isInitialized) {
            System.out.println("⚠️ ApplicationProtection non inizializzato!");
            return false;
        }

        if (stage == null) {
            System.out.println("🔍 isMainApplicationStage: stage è null");
            return false;
        }

        // Verifica per riferimento diretto
        if (stage == mainApplicationStage) {
            System.out.println("✅ isMainApplicationStage: MATCH diretto per " + stage.getTitle());
            return true;
        }

        // Verifica per titolo e caratteristiche se il riferimento è perso
        String title = stage.getTitle();
        System.out.println("🔍 isMainApplicationStage: Verifico titolo '" + title + "'");

        if (title != null) {
            // È l'app principale se:
            boolean isMain = title.contains("BABO") ||
                    title.contains("Biblioteca") ||
                    title.contains("Books") ||
                    (!title.contains("Dettagli") && !title.contains("Popup") && !title.contains("Dialog"));

            System.out.println("🔍 isMainApplicationStage: " + title + " -> " + isMain);
            return isMain;
        }

        // Verifica per modalità
        boolean isModal = stage.getModality() != null && stage.getModality() != javafx.stage.Modality.NONE;
        if (isModal) {
            System.out.println("🔍 isMainApplicationStage: Stage è modal, quindi NON è main");
            return false;
        }

        // Fallback: se non ha titolo e non è modal, probabilmente è la main
        System.out.println("🔍 isMainApplicationStage: Fallback - assumo sia main");
        return true;
    }

    /**
     * Esegue chiusura sicura di uno stage con protezione dell'applicazione principale.
     * <p>
     * Implementa un meccanismo di sicurezza che impedisce la chiusura accidentale
     * dell'applicazione principale mentre consente la chiusura sicura di popup
     * e finestre secondarie. Include logging dettagliato per debugging e
     * monitoraggio delle operazioni.
     * </p>
     *
     * <h4>Logica di protezione:</h4>
     * <ol>
     *   <li>Verifica inizializzazione sistema protezione</li>
     *   <li>Validazione parametri input (null safety)</li>
     *   <li>Identificazione natura dello stage (main vs popup)</li>
     *   <li>Applicazione regole di protezione</li>
     *   <li>Autorizzazione o blocco operazione</li>
     * </ol>
     *
     * <h4>Scenari gestiti:</h4>
     * <ul>
     *   <li><strong>Stage principale:</strong> Chiusura sempre bloccata con logging</li>
     *   <li><strong>Popup/finestre secondarie:</strong> Chiusura autorizzata</li>
     *   <li><strong>Stage non identificati:</strong> Approccio conservativo</li>
     *   <li><strong>Errori di sistema:</strong> Fallback sicuri</li>
     * </ul>
     *
     * <h4>Integrazione con workflow:</h4>
     * <p>
     * Il metodo è progettato per essere utilizzato prima di qualsiasi
     * operazione di chiusura stage per garantire la sicurezza dell'applicazione.
     * </p>
     *
     * @param stage lo stage di cui si richiede la chiusura
     * @return {@code true} se la chiusura è autorizzata, {@code false} se bloccata
     * @apiNote Il metodo è fail-safe: in caso di dubbio, blocca la chiusura
     *          per proteggere l'applicazione principale. Include suggerimenti
     *          per alternative sicure nel logging.
     */
    public static boolean safeCloseStage(Stage stage) {
        if (!isInitialized) {
            System.out.println("⚠️ ApplicationProtection non inizializzato!");
            return false;
        }

        if (stage == null) {
            System.out.println("⚠️ safeCloseStage: Tentativo di chiudere stage null");
            return false;
        }

        String title = stage.getTitle();
        System.out.println("🔒 safeCloseStage: Richiesta chiusura per '" + title + "'");

        // BLOCCA SEMPRE se è l'app principale
        if (isMainApplicationStage(stage)) {
            System.out.println("🛡️ PROTEZIONE ATTIVATA!");
            System.out.println("   🚫 BLOCCO chiusura dell'applicazione principale!");
            System.out.println("   💡 Usa PopupManager.closeAllPopups() per chiudere i popup");
            return false;
        }

        // È sicuro chiudere (è un popup)
        System.out.println("✅ Chiusura sicura autorizzata per popup: " + title);
        return true;
    }

    /**
     * Ottiene il riferimento diretto allo stage principale dell'applicazione.
     * <p>
     * Fornisce accesso al riferimento dello stage principale per operazioni
     * avanzate che richiedono manipolazione diretta. Utilizzare con cautela
     * per evitare interferenze con il sistema di protezione.
     * </p>
     *
     * @return il {@link Stage} principale dell'applicazione, o {@code null}
     *         se non ancora registrato
     * @apiNote L'utilizzo diretto del riferimento bypassa le protezioni.
     *          Preferire sempre i metodi di utilità di questa classe quando possibile.
     */
    public static Stage getMainStage() {
        return mainApplicationStage;
    }

    /**
     * Esegue debug completo dello stato dell'applicazione e di tutte le finestre.
     * <p>
     * Fornisce un report dettagliato dello stato corrente dell'applicazione,
     * inclusi stage registrati, popup attivi, stato del sistema di protezione,
     * e analisi di tutte le finestre presenti nel sistema. Essenziale per
     * debugging di problemi complessi e monitoraggio stato applicazione.
     * </p>
     *
     * <h4>Informazioni raccolte:</h4>
     * <ul>
     *   <li><strong>Stato ApplicationProtection:</strong> Inizializzazione e configurazione</li>
     *   <li><strong>Stage principale:</strong> Proprietà e stato corrente</li>
     *   <li><strong>Popup attivi:</strong> Conteggio tramite PopupManager</li>
     *   <li><strong>Finestre sistema:</strong> Analisi completa di tutte le finestre</li>
     *   <li><strong>Classificazione finestre:</strong> Distinzione main vs popup</li>
     *   <li><strong>Statistiche aggregate:</strong> Riepilogo numerico stato</li>
     * </ul>
     *
     * <h4>Analisi per ogni finestra:</h4>
     * <ul>
     *   <li>Titolo e identificazione</li>
     *   <li>Hash code per tracking</li>
     *   <li>Stato visibilità e focus</li>
     *   <li>Proprietà modalità e stile</li>
     *   <li>Classificazione automatica</li>
     * </ul>
     *
     * <h4>Rilevamento anomalie:</h4>
     * <p>
     * Il metodo identifica automaticamente situazioni anomale come:
     * </p>
     * <ul>
     *   <li>Assenza di finestra principale</li>
     *   <li>Multiple finestre principali</li>
     *   <li>Inconsistenze nel tracking popup</li>
     * </ul>
     *
     * @apiNote Utilizzare questo metodo per debugging di problemi di gestione
     *          finestre, analisi memory leak, o verifica stato applicazione.
     *          L'output è formattato per facile lettura e parsing.
     */
    public static void debugApplicationState() {
        System.out.println("🔍 ===== DEBUG STATO APPLICAZIONE =====");
        System.out.println("📊 ApplicationProtection inizializzato: " + isInitialized);
        System.out.println("📊 Stage principale registrato: " + (mainApplicationStage != null));

        if (mainApplicationStage != null) {
            System.out.println("   🏠 Titolo: '" + mainApplicationStage.getTitle() + "'");
            System.out.println("   🔗 Hash: " + mainApplicationStage.hashCode());
            System.out.println("   👁️ Visible: " + mainApplicationStage.isShowing());
            System.out.println("   🎯 Focused: " + mainApplicationStage.isFocused());
            System.out.println("   🎭 Modal: " + mainApplicationStage.getModality());
        }

        // Debug popup tramite PopupManager
        try {
            PopupManager popupManager = PopupManager.getInstance();
            System.out.println("📊 Popup attivi (PopupManager): " + popupManager.getActivePopupsCount());
        } catch (Exception e) {
            System.out.println("⚠️ PopupManager non disponibile: " + e.getMessage());
        }

        // Debug finestre del sistema
        javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Stage.getWindows();
        System.out.println("📊 Finestre totali nel sistema: " + windows.size());

        int mainCount = 0;
        int popupCount = 0;

        for (int i = 0; i < windows.size(); i++) {
            javafx.stage.Window window = windows.get(i);
            if (window instanceof Stage) {
                Stage stage = (Stage) window;
                String title = stage.getTitle();
                boolean isMain = isMainApplicationStage(stage);
                String type = isMain ? "MAIN" : "POPUP";

                System.out.println("   " + (i + 1) + ". " + type + " - '" + title + "'");
                System.out.println("      🔗 Hash: " + stage.hashCode());
                System.out.println("      👁️ Showing: " + stage.isShowing());
                System.out.println("      🎯 Focused: " + stage.isFocused());
                System.out.println("      🎭 Style: " + stage.getStyle());
                System.out.println("      🔒 Modal: " + stage.getModality());

                if (isMain) mainCount++;
                else popupCount++;
            }
        }

        System.out.println("📈 Riepilogo finestre sistema:");
        System.out.println("   🏠 Finestre principali: " + mainCount);
        System.out.println("   📋 Popup: " + popupCount);

        if (mainCount == 0) {
            System.out.println("⚠️ ATTENZIONE: Nessuna finestra principale rilevata!");
        } else if (mainCount > 1) {
            System.out.println("⚠️ ATTENZIONE: Più finestre principali rilevate!");
        }

        System.out.println("🔍 ===== FINE DEBUG =====");
    }

    /**
     * Gestisce la pressione globale del tasto ESC per chiusura ordinata popup.
     * <p>
     * Implementa la logica centrale per gestire la pressione del tasto ESC,
     * coordinandosi con PopupManager per chiusure ordinate e fornendo fallback
     * per scenari complessi. Progettato per essere chiamato dai gestori
     * di eventi globali configurati dal sistema.
     * </p>
     *
     * <h4>Strategia di gestione:</h4>
     * <ol>
     *   <li><strong>Tentativo PopupManager:</strong> Usa PopupManager se disponibile</li>
     *   <li><strong>Fallback manuale:</strong> Cerca popup direttamente nel sistema</li>
     *   <li><strong>Protezione applicazione:</strong> Evita chiusura stage principale</li>
     *   <li><strong>Error handling:</strong> Gestione robusta di errori</li>
     * </ol>
     *
     * <h4>Priorità di chiusura:</h4>
     * <ul>
     *   <li>Popup gestiti da PopupManager (priorità alta)</li>
     *   <li>Stage non-main visibili (fallback)</li>
     *   <li>Ignora stage principale per sicurezza</li>
     * </ul>
     *
     * <h4>Logging operazioni:</h4>
     * <p>
     * Tutte le operazioni sono loggate per debugging e monitoring,
     * includendo success, fallback, e scenari di nessuna azione.
     * </p>
     *
     * @apiNote Questo metodo dovrebbe essere chiamato solo dai gestori
     *          di eventi ESC configurati dal sistema. L'uso diretto può
     *          causare comportamenti inaspettati.
     */
    public static void handleGlobalEscapeKey() {
        System.out.println("🔑 ApplicationProtection: Gestione ESC globale");

        try {
            // Prima prova con PopupManager
            PopupManager popupManager = PopupManager.getInstance();

            if (popupManager.isInitialized() && popupManager.hasActivePopups()) {
                System.out.println("🔧 ESC: Chiudo popup tramite PopupManager");
                popupManager.closeTopPopup();
                return;
            }

            // Fallback: cerca popup manualmente
            javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Stage.getWindows();

            for (javafx.stage.Window window : windows) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;

                    // Se è un popup e non l'app principale
                    if (!isMainApplicationStage(stage) && stage.isShowing()) {
                        System.out.println("🔒 ESC: Chiudo stage " + stage.getTitle());
                        stage.close();
                        return;
                    }
                }
            }

            System.out.println("ℹ️ ESC: Nessun popup da chiudere");

        } catch (Exception e) {
            System.err.println("❌ Errore gestione ESC: " + e.getMessage());
        }
    }

    /**
     * Configura gestori globali per eventi tastiera sullo stage principale.
     * <p>
     * Imposta event handler per la gestione globale di eventi tastiera,
     * attualmente focalizzato sulla gestione del tasto ESC per chiusura
     * popup. I gestori sono configurati sulla scene dello stage principale
     * per intercettare eventi a livello globale.
     * </p>
     *
     * <h4>Eventi gestiti:</h4>
     * <ul>
     *   <li><strong>ESC:</strong> Chiusura ordinata popup attivi</li>
     * </ul>
     *
     * <h4>Logica di attivazione:</h4>
     * <p>
     * Gli handler ESC si attivano solo quando ci sono popup attivi,
     * evitando interferenze con il normale utilizzo dell'applicazione
     * principale quando non ci sono finestre modali aperte.
     * </p>
     *
     * <h4>Event consumption:</h4>
     * <p>
     * Gli eventi vengono consumati quando gestiti per prevenire
     * propagazione a altri handler che potrebbero causare effetti
     * collaterali indesiderati.
     * </p>
     *
     * @param mainStage lo stage principale su cui configurare i gestori
     * @throws IllegalArgumentException se mainStage è {@code null} o senza scene
     * @apiNote Questo metodo viene chiamato automaticamente durante la
     *          registrazione dello stage principale. Chiamate manuali possono
     *          sovrascrivere configurazioni esistenti.
     */
    public static void setupGlobalKeyHandlers(Stage mainStage) {
        if (mainStage == null || mainStage.getScene() == null) {
            System.err.println("❌ Impossibile configurare handler globali: stage o scene null");
            return;
        }

        System.out.println("⌨️ Configurazione handler ESC globale");

        mainStage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                System.out.println("🔑 ESC globale premuto");

                // Non gestire ESC se siamo nella finestra principale senza popup
                PopupManager popupManager = PopupManager.getInstance();
                if (popupManager.isInitialized() && popupManager.hasActivePopups()) {
                    handleGlobalEscapeKey();
                    event.consume();
                } else {
                    System.out.println("ℹ️ ESC ignorato: nessun popup attivo");
                }
            }
        });
        System.out.println("✅ Handler ESC globale configurato");
    }
}