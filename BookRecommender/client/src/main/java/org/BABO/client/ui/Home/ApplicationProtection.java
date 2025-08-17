package org.BABO.client.ui.Home;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.BABO.client.ui.Popup.PopupManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Versione migliorata di ApplicationProtection che lavora con il progetto esistente
 */
public class ApplicationProtection {

    private static Stage mainApplicationStage = null;
    private static boolean isInitialized = false;

    /**
     * Registra lo stage principale dell'applicazione
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

            try {
                PopupManager popupManager = PopupManager.getInstance();

                if (popupManager.isInitialized() && popupManager.hasActivePopups()) {
                    System.out.println("⚠️ Ci sono " + popupManager.getActivePopupsCount() + " popup aperti");

                    // Debug stato popup
                    popupManager.debugPopupState();

                    // Chiudi tutti i popup
                    popupManager.emergencyReset();

                    // Posticipa la chiusura
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1000); // Tempo per chiusura popup
                            System.out.println("✅ Popup chiusi, procedendo con chiusura applicazione");
                            Platform.exit();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            Platform.exit();
                        }
                    });

                    e.consume(); // Impedisce la chiusura immediata

                } else {
                    System.out.println("✅ Nessun popup aperto, chiusura applicazione confermata");
                    // Lascia che l'applicazione si chiuda normalmente
                }

            } catch (Exception popupError) {
                System.err.println("⚠️ Errore controllo popup: " + popupError.getMessage());
                System.out.println("✅ Chiusura applicazione (con warning popup)");
            }
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
     * Verifica se uno stage è l'applicazione principale
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

        // Verifica per riferimento diretto (più affidabile)
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
                    title.contains("Apple Books") ||
                    (!title.contains("Dettagli") && !title.contains("Popup") && !title.contains("Dialog"));

            System.out.println("🔍 isMainApplicationStage: " + title + " -> " + isMain);
            return isMain;
        }

        // Verifica per modalità (popup sono spesso modal)
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
     * Chiusura sicura - non chiude mai l'app principale
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
     * Chiude uno stage in modo sicuro usando PopupManager quando possibile
     */
    public static boolean closeStageIfSafe(Stage stage) {
        if (safeCloseStage(stage)) {
            try {
                // Verifica se è gestito da PopupManager
                PopupManager popupManager = PopupManager.getInstance();
                if (popupManager.hasActivePopups()) {
                    System.out.println("📋 Usando PopupManager per chiudere il popup");
                    popupManager.closeTopPopup();
                    return true;
                } else {
                    // Chiusura diretta se non gestito da PopupManager
                    System.out.println("🔒 Chiusura diretta dello stage");
                    stage.close();
                    return true;
                }
            } catch (Exception e) {
                System.err.println("❌ Errore chiusura stage: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Ottiene il riferimento allo stage principale
     */
    public static Stage getMainStage() {
        return mainApplicationStage;
    }

    /**
     * Verifica se ApplicationProtection è inizializzato
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Debug completo dello stato dell'applicazione
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

                System.out.println("   " + (i+1) + ". " + type + " - '" + title + "'");
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
     * Emergency: forza chiusura di tutti i popup usando PopupManager
     */
    public static void emergencyCloseAllPopups() {
        System.out.println("🚨 EMERGENCY: Chiusura forzata di tutti i popup");

        if (!isInitialized) {
            System.out.println("⚠️ ApplicationProtection non inizializzato, chiusura manuale");
            emergencyCloseAllPopupsManual();
            return;
        }

        try {
            // NUOVO: Prima prova con PopupManager
            PopupManager popupManager = PopupManager.getInstance();

            if (popupManager.isInitialized()) {
                System.out.println("🔧 Uso PopupManager per emergency close");
                popupManager.emergencyReset();

                // Attendi un momento e controlla se ci sono ancora popup
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(500);

                        if (popupManager.hasActivePopups()) {
                            System.out.println("⚠️ Popup ancora presenti dopo reset, chiusura manuale");
                            emergencyCloseAllPopupsManual();
                        } else {
                            System.out.println("✅ Tutti i popup chiusi tramite PopupManager");
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        emergencyCloseAllPopupsManual();
                    }
                });

            } else {
                System.out.println("⚠️ PopupManager non inizializzato, uso chiusura manuale");
                emergencyCloseAllPopupsManual();
            }

        } catch (Exception e) {
            System.err.println("❌ Errore emergency close con PopupManager: " + e.getMessage());
            emergencyCloseAllPopupsManual();
        }
    }

    /**
     * Emergency manual: chiude tutti i popup manualmente
     */
    private static void emergencyCloseAllPopupsManual() {
        System.out.println("🔧 Emergency close manuale");

        try {
            javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Stage.getWindows();
            int closedCount = 0;
            int totalWindows = windows.size();

            System.out.println("🔍 Finestre trovate: " + totalWindows);

            // Crea una copia della lista per evitare ConcurrentModificationException
            List<Window> windowsCopy = new ArrayList<>(windows);

            for (javafx.stage.Window window : windowsCopy) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    String title = stage.getTitle();

                    System.out.println("🔍 Esamino finestra: '" + title + "'");

                    // Chiudi solo i popup, non l'applicazione principale
                    if (!isMainApplicationStage(stage)) {
                        System.out.println("🔒 Chiudo popup: " + title);

                        try {
                            stage.close();
                            closedCount++;
                            System.out.println("   ✅ Popup chiuso");
                        } catch (Exception closeError) {
                            System.err.println("   ❌ Errore chiusura: " + closeError.getMessage());
                        }
                    } else {
                        System.out.println("🛡️ Protetto: " + title + " (app principale)");
                    }
                }
            }

            System.out.println("✅ Emergency close completato: " + closedCount + " popup chiusi");

        } catch (Exception e) {
            System.err.println("❌ Errore anche nella chiusura manuale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUOVO: Metodo per gestire ESC globale
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
     * NUOVO: Setup handler ESC globale per la finestra principale
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

    /**
     * Test di integrazione con PopupManager
     */
    public static void testIntegrationWithPopupManager() {
        System.out.println("🧪 TEST INTEGRAZIONE ApplicationProtection + PopupManager");

        debugApplicationState();

        try {
            PopupManager popupManager = PopupManager.getInstance();
            popupManager.debugFullState();
        } catch (Exception e) {
            System.out.println("⚠️ PopupManager non disponibile per test: " + e.getMessage());
        }

        System.out.println("✅ Test integrazione completato");
    }

    /**
     * Reset per test o situazioni di emergenza
     */
    public static void reset() {
        System.out.println("🔄 ApplicationProtection: Reset");
        mainApplicationStage = null;
        isInitialized = false;
        System.out.println("✅ ApplicationProtection: Reset completato");
    }

    // =====================================================
    // METODI DI COMPATIBILITÀ CON APPLICATIONPROTECTION ESISTENTE
    // =====================================================

    /**
     * Alias per compatibilità con ApplicationProtection esistente
     */
    public static void debugStageInfo(Stage stage) {
        if (stage == null) {
            System.out.println("🔍 DEBUG: Stage è null");
            return;
        }

        System.out.println("🔍 DEBUG STAGE INFO:");
        System.out.println("   Titolo: '" + stage.getTitle() + "'");
        System.out.println("   Hash: " + stage.hashCode());
        System.out.println("   Showing: " + stage.isShowing());
        System.out.println("   Focused: " + stage.isFocused());
        System.out.println("   È app principale: " + isMainApplicationStage(stage));

        if (getMainStage() != null) {
            System.out.println("   Main stage hash: " + getMainStage().hashCode());
            System.out.println("   Match diretto: " + (stage == getMainStage()));
        }
    }

    /**
     * Test di chiusura per debug
     */
    public static void testCloseStage(Stage stage) {
        if (stage == null) {
            System.out.println("🧪 TEST: Stage è null");
            return;
        }

        System.out.println("🧪 TEST CHIUSURA STAGE:");
        System.out.println("   Titolo: " + stage.getTitle());
        System.out.println("   Showing: " + stage.isShowing());
        System.out.println("   Focused: " + stage.isFocused());
        System.out.println("   IsMainApp: " + isMainApplicationStage(stage));

        // Test chiusura
        boolean result = safeCloseStage(stage);
        System.out.println("   Risultato: " + result);

        // Verifica post-chiusura
        Platform.runLater(() -> {
            System.out.println("   Post-chiusura Showing: " + stage.isShowing());
        });
    }
}