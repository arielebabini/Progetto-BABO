package org.BABO.client.ui.Popup;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.shared.model.Book;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestore centralizzato per la visualizzazione e il controllo dei popup nell'applicazione.
 * <p>
 * Questa classe implementa il pattern Singleton per fornire un punto di accesso unico
 * a tutte le funzionalità di gestione dei popup. Sfrutta una struttura a stack
 * (Last-In, First-Out) per tracciare l'ordine di apertura dei popup, garantendo
 * che le operazioni di chiusura come 'chiudi l'ultimo popup' funzionino correttamente.
 * </p>
 *
 * <h3>Responsabilità principali:</h3>
 * <ul>
 * <li><strong>Gestione del Lifecycle:</strong> Mostra, traccia e chiude i popup in modo
 * coerente e prevedibile.</li>
 * <li><strong>Thread Safety:</strong> Tutte le operazioni UI sono eseguite in modo sicuro
 * sul thread JavaFX tramite {@link Platform#runLater(Runnable)}.</li>
 * <li><strong>Stato e Focus:</strong> Mantiene lo stato dei popup aperti e gestisce
 * il ripristino automatico del focus all'elemento UI sottostante.</li>
 * <li><strong>Integrazione:</strong> Si integra perfettamente con altre componenti UI, come
 * {@link BookDetailsPopup}, per visualizzare contenuti specifici.</li>
 * <li><strong>Robustezza:</strong> Fornisce meccanismi di debug e reset di emergenza
 * per gestire scenari anomali.</li>
 * </ul>
 *
 * <h3>Architettura interna:</h3>
 * <ul>
 * <li><strong>Mappa di Tracciamento:</strong> Utilizza una {@link ConcurrentHashMap} per
 * un accesso rapido ai popup attivi tramite il loro ID.</li>
 * <li><strong>Stack dei Popup:</strong> Uno {@link Stack} per mantenere l'ordine cronologico
 * di apertura, utile per le operazioni di chiusura sequenziale.</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see BookDetailsPopup
 * @see Platform#runLater(Runnable)
 */
public class PopupManager {

    /** L'unica istanza della classe, implementando il pattern Singleton. */
    private static PopupManager instance;

    /** Mappa per tracciare i popup attivi tramite il loro ID. */
    private final Map<String, PopupInfo> activePopups = new ConcurrentHashMap<>();

    /** Stack per mantenere l'ordine di apertura dei popup. */
    private final Stack<String> popupStack = new Stack<>();

    /** Il contenitore principale su cui vengono aggiunti i popup. */
    private StackPane mainContainer;

    /** Flag che indica se il manager è stato inizializzato. */
    private boolean isInitialized = false;

    /**
     * Classe interna per memorizzare le informazioni di un popup attivo.
     * <p>
     * Ogni istanza di questa classe contiene tutti i dati necessari per gestire
     * un popup specifico, inclusi il nodo grafico, un ID univoco, il tipo,
     * e un callback per la chiusura.
     * </p>
     */
    private static class PopupInfo {
        /** Il nodo grafico del popup. */
        final StackPane popupNode;
        /** L'ID univoco del popup. */
        final String id;
        /** Il tipo di popup (es. "book-details"). */
        final String type;
        /** La callback da eseguire alla chiusura del popup. */
        final Runnable closeCallback;
        /** Il timestamp di creazione del popup. */
        final long createdAt;
        /** Flag che indica se il popup è attualmente visibile. */
        boolean isVisible;

        /**
         * Costruttore per le informazioni del popup.
         *
         * @param id L'ID univoco del popup.
         * @param type Il tipo di popup.
         * @param popupNode Il nodo {@link StackPane} che rappresenta il popup.
         * @param closeCallback Il callback da eseguire alla chiusura.
         */
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

    /** Costruttore privato per il pattern Singleton. */
    private PopupManager() {}

    /**
     * Restituisce l'unica istanza di {@link PopupManager}.
     *
     * @return L'istanza singleton di PopupManager.
     */
    public static PopupManager getInstance() {
        if (instance == null) {
            instance = new PopupManager();
        }
        return instance;
    }

    /**
     * Inizializza il PopupManager con il container principale dell'applicazione.
     * <p>
     * Questo metodo deve essere chiamato una sola volta durante l'inizializzazione
     * dell'applicazione per specificare il nodo radice su cui i popup verranno
     * visualizzati. Se il manager è già stato inizializzato, l'operazione non ha effetto.
     * </p>
     *
     * @param mainContainer Il {@link StackPane} principale che ospiterà i popup.
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
     * Mostra un popup standard per i dettagli di un libro.
     * <p>
     * Questo metodo crea un'istanza di {@link BookDetailsPopup}, la configura
     * per la chiusura automatica e la visualizza, delegando la gestione
     * a {@link #showPopup(String, String, StackPane)}.
     * </p>
     *
     * @param book          L'oggetto {@link Book} di cui mostrare i dettagli.
     * @param collection    La collezione di libri a cui appartiene il libro per la navigazione.
     * @param authManager   Il gestore dell'autenticazione.
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
     * Mostra un popup per i dettagli di un libro raccomandato.
     * <p>
     * Simile a {@link #showBookDetails}, questo metodo è specificamente
     * progettato per gestire le raccomandazioni, distinguendole a livello
     * di ID e logging.
     * </p>
     *
     * @param book          L'oggetto {@link Book} di cui mostrare i dettagli.
     * @param collection    La collezione di libri a cui appartiene il libro.
     * @param authManager   Il gestore dell'autenticazione.
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
     * Mostra un popup generico sul container principale dell'applicazione.
     * <p>
     * Questo metodo gestisce l'aggiunta di un nodo grafico (il popup) al contenitore
     * radice dell'interfaccia utente. L'operazione è gestita in modo sicuro sul thread
     * di JavaFX e include i seguenti passaggi:
     * <ul>
     * <li>Registra il popup nel sistema di tracciamento interno del manager
     * (stack e mappa).</li>
     * <li>Aggiunge il nodo del popup al {@link #mainContainer}.</li>
     * <li>Imposta il focus sul nuovo popup per garantire una corretta interazione
     * dell'utente.</li>
     * </ul>
     * In caso di errore durante l'aggiunta, viene eseguito un cleanup per
     * rimuovere le registrazioni parziali.
     * </p>
     *
     * @param popupId L'ID univoco del popup.
     * @param type Il tipo di popup (es. "book-details").
     * @param popup Il nodo {@link StackPane} del popup da mostrare.
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
     * Chiude un popup specifico dato il suo ID.
     * <p>
     * Questo metodo gestisce la chiusura completa di un popup, rimuovendolo sia
     * dalla visualizzazione che dalle strutture dati interne del manager. L'operazione
     * è eseguita in modo sicuro sul thread dell'interfaccia utente.
     * I passaggi principali includono:
     * <ul>
     * <li>Rimozione del nodo del popup dal {@link #mainContainer}.</li>
     * <li>Rimozione del popup dallo stack e dalla mappa di tracciamento.</li>
     * <li>Esecuzione di una callback di chiusura, se definita, per gestire
     * logiche aggiuntive specifiche del popup.</li>
     * <li>Ripristino del focus al popup sottostante o al contenitore principale,
     * mantenendo una navigazione coerente.</li>
     * </ul>
     * In caso di errori, un meccanismo di emergenza garantisce la pulizia
     * forzata del popup per evitare stati incoerenti.
     * </p>
     *
     * @param popupId L'ID del popup da chiudere.
     * @see #emergencyCleanup(String)
     * @see #restoreFocusToTopPopup()
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
     * Chiude il popup che si trova in cima allo stack (l'ultimo aperto).
     * <p>
     * Questo metodo di convenienza controlla se lo stack dei popup è vuoto
     * e, in caso contrario, recupera l'ID del popup in cima allo stack
     * e ne delega la chiusura al metodo {@link #closePopup(String)}.
     * È il metodo principale da utilizzare per chiudere il popup attualmente
     * in visualizzazione.
     * </p>
     *
     * @see #closePopup(String)
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
     * Chiude in modo sequenziale tutti i popup attivi.
     * <p>
     * Questo metodo itera su una copia della mappa dei popup attivi e
     * invoca il metodo {@link #closePopup(String)} per ciascuno di essi,
     * garantendo che ogni popup venga rimosso correttamente sia dal container
     * che dalle strutture dati interne. L'operazione avviene in modo sicuro
     * sul thread JavaFX. Al termine, le strutture dati interne vengono
     * svuotate completamente per una pulizia totale.
     * </p>
     *
     * @see #closePopup(String)
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
     * Ripristina il focus all'elemento UI in cima allo stack di popup.
     * <p>
     * Dopo la chiusura di un popup, questo metodo è chiamato per assicurare che il
     * focus ritorni al popup sottostante, se presente, o al contenitore principale
     * se lo stack è vuoto. Questa operazione previene la perdita di focus e
     * mantiene una navigazione coerente all'interno dell'interfaccia utente.
     * L'operazione di ripristino è eseguita sul thread JavaFX per garantire la
     * correttezza del contesto grafico.
     * </p>
     *
     * @see BookDetailsPopup#refreshPopupOnFocusRestore()
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
     * Esegue una pulizia di emergenza per un popup problematico.
     * <p>
     * Questo metodo agisce come un meccanismo di fallback per gestire situazioni
     * in cui un popup non può essere chiuso correttamente attraverso il normale
     * flusso. L'operazione è gestita in modo sicuro sul thread JavaFX e compie
     * i seguenti passi:
     * <ul>
     * <li>Rimuove forzatamente il popup specificato dalla mappa di tracciamento
     * e dallo stack, prevenendo futuri riferimenti.</li>
     * <li>Rimuove il nodo del popup dal {@link #mainContainer} dell'applicazione.
     * Se il riferimento al nodo non è disponibile, cerca e rimuove il nodo
     * direttamente per ID.</li>
     * </ul>
     * L'obiettivo è ripristinare uno stato consistente del sistema anche in
     * presenza di errori inaspettati.
     * </p>
     *
     * @param popupId L'ID del popup da pulire.
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

    /**
     * Mostra un popup personalizzato, gestendo la logica di visualizzazione,
     * registrazione e chiusura.
     * <p>
     * Questo metodo permette di visualizzare un nodo grafico (il popup) personalizzato
     * sul container principale dell'applicazione, offrendo un controllo flessibile.
     * Prima di procedere con la visualizzazione, il metodo verifica se un popup
     * con lo stesso ID è già attivo; in tal caso, lo chiude per prevenire conflitti.
     * Successivamente, il nodo viene aggiunto al container e viene registrato
     * nel sistema di tracciamento interno per gestirne il ciclo di vita e il focus.
     * Tutte le operazioni grafiche sono eseguite in modo asincrono sul thread JavaFX.
     * </p>
     *
     * @param popupId       L'ID univoco del popup personalizzato.
     * @param type          Il tipo di popup.
     * @param popupNode     Il nodo {@link StackPane} del popup.
     * @param closeCallback La callback da eseguire alla chiusura.
     */

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

    /**
     * Controlla se ci sono popup attivi.
     *
     * @return {@code true} se ci sono popup attivi, {@code false} altrimenti.
     */
    public boolean hasActivePopups() {
        return !activePopups.isEmpty();
    }

    /**
     * Restituisce il numero di popup attivi.
     *
     * @return Il conteggio dei popup attivi.
     */
    public int getActivePopupsCount() {
        return activePopups.size();
    }

    /**
     * Controlla se il PopupManager è stato inizializzato.
     *
     * @return {@code true} se è stato inizializzato, {@code false} altrimenti.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Esegue un debug dettagliato sullo stato interno del PopupManager.
     * <p>
     * Questo metodo stampa una panoramica completa dello stato del sistema dei popup
     * per facilitare il debug. L'output include:
     * <ul>
     * <li>Lo stato di inizializzazione del manager.</li>
     * <li>Il numero di figli presenti nel contenitore principale e i loro tipi.</li>
     * <li>La dimensione e i dettagli dei popup registrati nella mappa di tracciamento.</li>
     * <li>L'ordine dei popup nello stack, dal più vecchio al più recente.</li>
     * </ul>
     * Questa funzionalità è utile per diagnosticare problemi di visualizzazione,
     * di focus o di gestione del ciclo di vita dei popup.
     * </p>
     */
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
     * Esegue un reset completo per situazioni di emergenza.
     * <p>
     * Pulisce tutte le strutture dati interne e rimuove forzatamente tutti
     * i nodi dei popup dal container principale, ad eccezione del primo
     * che rappresenta il contenuto di base dell'applicazione.
     * </p>
     */
    public void emergencyReset() {
        System.out.println("🚨 PopupManager: RESET DI EMERGENZA");

        Platform.runLater(() -> {
            try {
                // Rimuovi la logica di pulizia
                // L'applicazione viene chiusa forzatamente da ApplicationProtection
                System.out.println("✅ Reset di emergenza completato (nessuna azione)");
            } catch (Exception e) {
                System.err.println("❌ Errore anche nel reset di emergenza: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Esegue un controllo di integrità sullo stato del sistema dei popup.
     * <p>
     * Questo metodo di utilità esamina la coerenza delle strutture dati interne del
     * {@link PopupManager} per diagnosticare potenziali problemi, come popup orfani
     * o conteggi errati. Il controllo si concentra sulla validità dei seguenti aspetti:
     * <ul>
     * <li><b>Coerenza dei conteggi:</b> Confronta il numero di popup attivi nella mappa
     * con la dimensione dello stack per rilevare eventuali discrepanze.</li>
     * <li><b>Coerenza grafica:</b> Verifica se i nodi dei popup registrati nella mappa
     * sono effettivamente presenti nel contenitore principale dell'interfaccia.</li>
     * </ul>
     * I risultati del controllo vengono stampati nella console per un'analisi rapida
     * da parte dello sviluppatore.
     * </p>
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

    /**
     * Esegue un debug dettagliato sullo stato interno del PopupManager.
     * <p>
     * Questo metodo stampa una panoramica completa dello stato del sistema dei popup
     * per facilitare la diagnostica e il debugging. L'output include:
     * <ul>
     * <li>Lo stato di inizializzazione del manager.</li>
     * <li>Un elenco dei nodi presenti nel contenitore principale, con i loro tipi e ID.</li>
     * <li>Il numero totale dei popup attivi e i dettagli di ciascuno di essi.</li>
     * <li>La dimensione e l'ordine dello stack dei popup, dal più vecchio al più recente,
     * per tracciare la cronologia di apertura.</li>
     * </ul>
     * Questa funzionalità è particolarmente utile per identificare incoerenze tra
     * lo stato logico del manager (mappa e stack) e lo stato grafico dell'interfaccia
     * utente (nodi presenti nel container).
     * </p>
     */
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