package org.BABO.client.ui.Popup;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.shared.model.Book;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PopupManager migliorato per gestire popup annidati senza perdere riferimenti
 * Risolve il problema dei popup bloccati dopo la chiusura di popup secondari
 */
public class PopupManager {

    private static PopupManager instance;
    private final Map<String, PopupInfo> activePopups = new ConcurrentHashMap<>();
    private final Stack<String> popupStack = new Stack<>();
    private StackPane mainContainer;
    private boolean isInitialized = false;

    // Classe per memorizzare informazioni sui popup
    private static class PopupInfo {
        final StackPane popupNode;
        final String id;
        final String type;
        final Runnable closeCallback;
        final long createdAt;
        boolean isVisible;

        PopupInfo(String id, String type, StackPane popupNode, Runnable closeCallback) {
            this.id = id;
            this.type = type;
            this.popupNode = popupNode;
            this.closeCallback = closeCallback;
            this.createdAt = System.currentTimeMillis();
            this.isVisible = true;
        }

        @Override
        public String toString() {
            return String.format("PopupInfo{id='%s', type='%s', visible=%s, age=%dms}",
                    id, type, isVisible, System.currentTimeMillis() - createdAt);
        }
    }

    private PopupManager() {}

    public static PopupManager getInstance() {
        if (instance == null) {
            instance = new PopupManager();
        }
        return instance;
    }

    /**
     * Inizializza il PopupManager con il container principale
     */
    public void initialize(StackPane mainContainer) {
        if (mainContainer == null) {
            System.err.println("❌ PopupManager: Container principale è null!");
            return;
        }

        this.mainContainer = mainContainer;
        this.isInitialized = true;

        System.out.println("✅ PopupManager: Inizializzato con container principale");
        System.out.println("   Container: " + mainContainer.getClass().getSimpleName());
        System.out.println("   Children iniziali: " + mainContainer.getChildren().size());
    }

    /**
     * Mostra popup dettagli libro standard
     */
    public void showBookDetails(Book book, List<Book> collection, AuthenticationManager authManager) {
        if (!isInitialized) {
            System.err.println("❌ PopupManager non inizializzato!");
            return;
        }

        String popupId = "book-details-" + System.currentTimeMillis();
        System.out.println("📖 PopupManager: Apertura popup libro - " + popupId);

        StackPane popup = BookDetailsPopup.createWithLibrarySupport(
                book,
                collection,
                () -> closePopup(popupId),
                authManager
        );

        showPopup(popupId, "book-details", popup);
    }

    /**
     * Mostra popup dettagli raccomandazione
     */
    public void showRecommendationDetails(Book book, List<Book> collection, AuthenticationManager authManager) {
        if (!isInitialized) {
            System.err.println("❌ PopupManager non inizializzato!");
            return;
        }

        String popupId = "recommendation-" + System.currentTimeMillis();
        System.out.println("💡 PopupManager: Apertura popup raccomandazione - " + popupId);

        StackPane popup = BookDetailsPopup.createWithLibrarySupport(
                book,
                collection,
                () -> closePopup(popupId),
                authManager
        );

        showPopup(popupId, "recommendation", popup);
    }

    /**
     * Mostra un popup generico
     */
    private void showPopup(String popupId, String type, StackPane popup) {
        if (popup == null) {
            System.err.println("❌ PopupManager: Popup è null per ID " + popupId);
            return;
        }

        Platform.runLater(() -> {
            try {
                // Imposta ID al popup
                popup.setId(popupId);

                // Crea info popup
                PopupInfo popupInfo = new PopupInfo(popupId, type, popup, () -> closePopup(popupId));

                // Aggiungi al tracking
                activePopups.put(popupId, popupInfo);
                popupStack.push(popupId);

                // Aggiungi al container
                mainContainer.getChildren().add(popup);

                // Focus sul nuovo popup
                popup.requestFocus();

                System.out.println("✅ PopupManager: Popup aggiunto - " + popupId);
                System.out.println("   Stack size: " + popupStack.size());
                System.out.println("   Container children: " + mainContainer.getChildren().size());

            } catch (Exception e) {
                System.err.println("❌ PopupManager: Errore nell'aggiunta popup " + popupId + ": " + e.getMessage());
                e.printStackTrace();

                // Cleanup in caso di errore
                activePopups.remove(popupId);
                if (!popupStack.isEmpty() && popupStack.peek().equals(popupId)) {
                    popupStack.pop();
                }
            }
        });
    }

    /**
     * Chiude un popup specifico
     */
    public void closePopup(String popupId) {
        if (popupId == null || !activePopups.containsKey(popupId)) {
            System.out.println("⚠️ PopupManager: Popup " + popupId + " non trovato o già chiuso");
            return;
        }

        System.out.println("🔒 PopupManager: Chiusura popup specifico " + popupId);

        Platform.runLater(() -> {
            try {
                PopupInfo popupInfo = activePopups.get(popupId);

                if (popupInfo != null && popupInfo.isVisible) {
                    // Rimuovi dal container
                    if (mainContainer.getChildren().contains(popupInfo.popupNode)) {
                        mainContainer.getChildren().remove(popupInfo.popupNode);
                        System.out.println("   ✅ Popup rimosso dal container");
                    }

                    // Marca come non visibile
                    popupInfo.isVisible = false;

                    // Rimuovi dallo stack
                    if (!popupStack.isEmpty() && popupStack.peek().equals(popupId)) {
                        popupStack.pop();
                        System.out.println("   ✅ Popup rimosso dalla cima dello stack");
                    } else {
                        popupStack.remove(popupId);
                        System.out.println("   ✅ Popup rimosso dallo stack (non in cima)");
                    }

                    // Esegui callback di chiusura se presente
                    if (popupInfo.closeCallback != null) {
                        try {
                            popupInfo.closeCallback.run();
                        } catch (Exception callbackError) {
                            System.err.println("⚠️ Errore nel callback di chiusura: " + callbackError.getMessage());
                        }
                    }
                }

                // Rimuovi dal tracking
                activePopups.remove(popupId);

                // Ripristina focus al popup sottostante se esiste
                restoreFocusToTopPopup();

                System.out.println("✅ PopupManager: Popup " + popupId + " chiuso completamente");
                System.out.println("   Stack size rimanente: " + popupStack.size());
                System.out.println("   Container children rimanenti: " + mainContainer.getChildren().size());

            } catch (Exception e) {
                System.err.println("❌ PopupManager: Errore nella chiusura popup " + popupId + ": " + e.getMessage());
                e.printStackTrace();

                // Cleanup di emergenza
                emergencyCleanup(popupId);
            }
        });
    }

    /**
     * Chiude il popup in cima allo stack
     */
    public void closeTopPopup() {
        if (popupStack.isEmpty()) {
            System.out.println("⚠️ PopupManager: Stack vuoto, nessun popup da chiudere");
            return;
        }

        String topPopupId = popupStack.peek();
        System.out.println("🔒 PopupManager: Chiusura popup in cima - " + topPopupId);
        closePopup(topPopupId);
    }

    /**
     * Chiude tutti i popup
     */
    public void closeAllPopups() {
        System.out.println("🔒 PopupManager: Chiusura di tutti i popup (" + activePopups.size() + ")");

        // Crea copia della lista per evitare ConcurrentModificationException
        List<String> popupsToClose = new ArrayList<>(activePopups.keySet());

        for (String popupId : popupsToClose) {
            closePopup(popupId);
        }

        // Cleanup finale
        Platform.runLater(() -> {
            activePopups.clear();
            popupStack.clear();
            System.out.println("✅ PopupManager: Tutti i popup chiusi");
        });
    }

    /**
     * Ripristina il focus al popup in cima allo stack
     */
    private void restoreFocusToTopPopup() {
        if (popupStack.isEmpty()) {
            System.out.println("📍 PopupManager: Nessun popup rimanente, focus alla finestra principale");
            return;
        }

        String topPopupId = popupStack.peek();
        PopupInfo topPopup = activePopups.get(topPopupId);

        if (topPopup != null && topPopup.isVisible) {
            Platform.runLater(() -> {
                try {
                    topPopup.popupNode.requestFocus();
                    System.out.println("🎯 PopupManager: Focus ripristinato al popup " + topPopupId);

                    // Notifica al BookDetailsPopup di refreshare se necessario
                    BookDetailsPopup.refreshPopupOnFocusRestore();

                } catch (Exception e) {
                    System.err.println("⚠️ Errore nel ripristino focus: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Cleanup di emergenza per un popup problematico
     */
    private void emergencyCleanup(String popupId) {
        System.out.println("🚨 PopupManager: Cleanup di emergenza per " + popupId);

        Platform.runLater(() -> {
            try {
                // Rimuovi dal tracking
                PopupInfo problematicPopup = activePopups.remove(popupId);

                // Rimuovi dallo stack
                popupStack.remove(popupId);

                // Cerca e rimuovi dal container per ID
                if (problematicPopup != null && problematicPopup.popupNode != null) {
                    mainContainer.getChildren().remove(problematicPopup.popupNode);
                } else {
                    // Cerca per ID nel container
                    mainContainer.getChildren().removeIf(node ->
                            popupId.equals(node.getId()));
                }

                System.out.println("✅ Emergency cleanup completato per " + popupId);

            } catch (Exception e) {
                System.err.println("❌ Errore anche nel cleanup di emergenza: " + e.getMessage());
            }
        });
    }

    public void showCustomPopup(String popupId, String type, StackPane popupNode, Runnable closeCallback) {
        if (!isInitialized) {
            System.err.println("❌ PopupManager non inizializzato!");
            return;
        }

        if (popupId == null || popupNode == null) {
            System.err.println("❌ PopupManager: parametri null per popup personalizzato");
            return;
        }

        System.out.println("🎭 PopupManager: Apertura popup personalizzato - " + popupId + " (tipo: " + type + ")");

        Platform.runLater(() -> {
            try {
                // Rimuovi eventuali popup con lo stesso ID
                if (activePopups.containsKey(popupId)) {
                    System.out.println("⚠️ PopupManager: Popup " + popupId + " già esistente, lo sostituisco");
                    closePopup(popupId);
                }

                // Crea PopupInfo
                PopupInfo popupInfo = new PopupInfo(popupId, type, popupNode, closeCallback);

                // Aggiungi al container principale
                mainContainer.getChildren().add(popupNode);

                // Registra il popup
                activePopups.put(popupId, popupInfo);
                popupStack.push(popupId);

                System.out.println("✅ PopupManager: Popup personalizzato " + popupId + " registrato");
                System.out.println("   Stack size: " + popupStack.size());
                System.out.println("   Container children: " + mainContainer.getChildren().size());

            } catch (Exception e) {
                System.err.println("❌ Errore apertura popup personalizzato: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Metodi di debug e utility
    public boolean hasActivePopups() {
        return !activePopups.isEmpty();
    }

    public int getActivePopupsCount() {
        return activePopups.size();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void debugFullState() {
        System.out.println("🔍 ===== POPUP MANAGER DEBUG DETTAGLIATO =====");
        System.out.println("Inizializzato: " + isInitialized);
        System.out.println("Container principale: " + (mainContainer != null ? "Registrato" : "Non registrato"));

        if (mainContainer != null) {
            System.out.println("Children nel container: " + mainContainer.getChildren().size());
            for (int i = 0; i < mainContainer.getChildren().size(); i++) {
                System.out.println("  Child " + i + ": " + mainContainer.getChildren().get(i).getClass().getSimpleName());
            }
        }

        System.out.println("Stack popup:");
        System.out.println("  Popup totali: " + activePopups.size());
        System.out.println("  Popup attivi: " + popupStack.size());

        if (!activePopups.isEmpty()) {
            System.out.println("  Dettagli popup:");
            for (Map.Entry<String, PopupInfo> entry : activePopups.entrySet()) {
                System.out.println("    " + entry.getValue());
            }
        }

        if (!popupStack.isEmpty()) {
            System.out.println("  Stack order (dal basso):");
            for (int i = 0; i < popupStack.size(); i++) {
                System.out.println("    " + i + ". " + popupStack.get(i));
            }
        }

        System.out.println("🔍 ===== FINE DEBUG =====");
    }

    /**
     * Reset completo per situazioni di emergenza
     */
    public void emergencyReset() {
        System.out.println("🚨 PopupManager: RESET DI EMERGENZA");

        Platform.runLater(() -> {
            try {
                // Salva riferimento al container principale
                StackPane container = mainContainer;

                // Pulisci tutto
                activePopups.clear();
                popupStack.clear();

                if (container != null) {
                    // Rimuovi tutti i children tranne il primo (che dovrebbe essere il contenuto principale)
                    while (container.getChildren().size() > 1) {
                        container.getChildren().remove(container.getChildren().size() - 1);
                    }

                    System.out.println("✅ Container pulito, children rimanenti: " + container.getChildren().size());
                }

                System.out.println("✅ Reset di emergenza completato");

            } catch (Exception e) {
                System.err.println("❌ Errore anche nel reset di emergenza: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Test di integrità del sistema
     */
    public void runIntegrityCheck() {
        System.out.println("🔍 PopupManager: Controllo integrità");

        try {
            int activePopupsCount = activePopups.size();
            int stackSize = popupStack.size();
            int containerChildren = mainContainer != null ? mainContainer.getChildren().size() : 0;

            System.out.println("   Popup attivi: " + activePopupsCount);
            System.out.println("   Stack size: " + stackSize);
            System.out.println("   Container children: " + containerChildren);

            // Verifica coerenza
            if (activePopupsCount != stackSize) {
                System.out.println("⚠️ ATTENZIONE: Incongruenza tra popup attivi e stack");
            }

            if (containerChildren > 0 && activePopupsCount == 0) {
                System.out.println("⚠️ ATTENZIONE: Container ha children ma nessun popup registrato");
            }

            // Verifica popup orfani
            for (String popupId : activePopups.keySet()) {
                PopupInfo popup = activePopups.get(popupId);
                if (popup != null && !mainContainer.getChildren().contains(popup.popupNode)) {
                    System.out.println("⚠️ POPUP ORFANO: " + popupId + " non nel container");
                }
            }

            System.out.println("✅ Controllo integrità completato");

        } catch (Exception e) {
            System.err.println("❌ Errore controllo integrità: " + e.getMessage());
        }
    }

    public void debugPopupState() {
        System.out.println("🔍 ===== POPUP MANAGER DEBUG DETTAGLIATO =====");
        System.out.println("Inizializzato: " + isInitialized);
        System.out.println("Container principale: " + (mainContainer != null ? mainContainer.getClass().getSimpleName() : "null"));

        if (mainContainer != null) {
            System.out.println("Container children: " + mainContainer.getChildren().size());
            for (int i = 0; i < mainContainer.getChildren().size(); i++) {
                System.out.println("  [" + i + "] " + mainContainer.getChildren().get(i).getClass().getSimpleName() +
                        " (ID: " + mainContainer.getChildren().get(i).getId() + ")");
            }
        }

        System.out.println("Popup attivi: " + activePopups.size());
        for (String popupId : activePopups.keySet()) {
            PopupInfo popup = activePopups.get(popupId);
            System.out.println("  " + popup.toString());
        }

        System.out.println("Stack popup: " + popupStack.size());
        for (int i = popupStack.size() - 1; i >= 0; i--) {
            System.out.println("  [" + i + "] " + popupStack.get(i));
        }

        System.out.println("🔍 ===== FINE DEBUG =====");
    }
}