package org.BABO.client.ui.Home;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.BABO.client.ui.Authentication.AuthenticationManager;
import org.BABO.client.ui.Book.BookGridBuilder;
import org.BABO.client.ui.Book.BookSectionFactory;
import org.BABO.client.ui.Category.CategoryView;
import org.BABO.client.ui.Popup.PopupManager;
import org.BABO.shared.model.Book;
import org.BABO.shared.model.Category;
import org.BABO.client.service.BookService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.geometry.Pos;

/**
 * Componente centrale per la gestione del contenuto principale dell'applicazione BABO.
 * <p>
 * Questa classe funge da controller principale per l'area di contenuto, gestendo la
 * visualizzazione di diverse sezioni dell'applicazione (home, librerie, esplora),
 * la ricerca avanzata, e la navigazione contestuale tra libri. Implementa un sistema
 * sofisticato di caching per ottimizzare le performance e un'architettura modulare
 * per facilitare l'estensione e la manutenzione.
 * </p>
 *
 * <h3>Responsabilità principali:</h3>
 * <ul>
 *   <li><strong>Gestione Sezioni:</strong> Home, Librerie, Esplora con transizioni fluide</li>
 *   <li><strong>Sistema di Ricerca:</strong> Ricerca normale e avanzata con filtri multipli</li>
 *   <li><strong>Navigazione Contestuale:</strong> Cache intelligente per navigazione tra libri</li>
 *   <li><strong>Integrazione Popup:</strong> Coordinamento con PopupManager per dettagli libri</li>
 *   <li><strong>Menu Handling:</strong> Gestione eventi dal menu sidebar principale</li>
 *   <li><strong>State Management:</strong> Mantenimento stato UI e cache tra transizioni</li>
 * </ul>
 *
 * <h3>Architettura di Caching:</h3>
 * <p>
 * Implementa un sistema di cache multi-livello per ottimizzare l'esperienza utente:
 * </p>
 * <ul>
 *   <li><strong>Featured Books Cache:</strong> Libri in evidenza per navigazione rapida</li>
 *   <li><strong>Free Books Cache:</strong> Libri gratuiti e suggerimenti</li>
 *   <li><strong>New Books Cache:</strong> Nuove uscite e novità</li>
 *   <li><strong>Search Results Cache:</strong> Risultati ricerca normale</li>
 *   <li><strong>Advanced Search Cache:</strong> Risultati ricerca avanzata con filtri</li>
 * </ul>
 *
 * <h3>Sistema di Ricerca Avanzata:</h3>
 * <p>
 * Supporta diverse modalità di ricerca con sintassi specializzata:
 * </p>
 * <ul>
 *   <li><strong>Ricerca Normale:</strong> Query libera su titolo e autore</li>
 *   <li><strong>Solo Titolo:</strong> {@code title-only:query} per ricerca specifica</li>
 *   <li><strong>Solo Autore:</strong> {@code author:nome} per filtro autore</li>
 *   <li><strong>Filtro Anno:</strong> {@code year:inizio-fine} per range temporale</li>
 *   <li><strong>Ricerca Combinata:</strong> {@code author:nome year:range} per query complesse</li>
 * </ul>
 *
 * <h3>Integrazione con Componenti:</h3>
 * <p>
 * La classe si integra con diversi componenti del sistema:
 * </p>
 * <ul>
 *   <li>{@link BookSectionFactory} per creazione sezioni di libri</li>
 *   <li>{@link ExploreIntegration} per la sezione esplora</li>
 *   <li>{@link PopupManager} per visualizzazione dettagli</li>
 *   <li>{@link AuthenticationManager} per gestione permessi</li>
 *   <li>{@link CategoryView} per navigazione categorie</li>
 * </ul>
 *
 * <h3>Gestione Eventi Menu:</h3>
 * <p>
 * Implementa un sistema robusto per la gestione degli eventi del menu sidebar:
 * </p>
 * <ul>
 *   <li><strong>Index 0 (Home):</strong> Caricamento contenuto iniziale con sezioni principali</li>
 *   <li><strong>Index 1 (Librerie):</strong> Vista librerie personali utente</li>
 *   <li><strong>Index 2 (Esplora):</strong> Vista esplorazione categorie e contenuti</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo:</h3>
 * <pre>{@code
 * // Inizializzazione componente
 * BookService bookService = new BookService();
 * AuthenticationManager authManager = new AuthenticationManager();
 * ContentArea contentArea = new ContentArea(bookService, true, authManager);
 *
 * // Configurazione navigazione
 * contentArea.setBookClickHandler(book -> {
 *     System.out.println("Libro selezionato: " + book.getTitle());
 * });
 *
 * // Configurazione integrazione esplora
 * ExploreIntegration exploreIntegration = new ExploreIntegration(bookService, true);
 * contentArea.setExploreIntegration(exploreIntegration);
 *
 * // Creazione UI
 * ScrollPane contentPane = contentArea.createContentArea();
 *
 * // Caricamento contenuto iniziale
 * contentArea.loadInitialContent();
 *
 * // Gestione eventi menu
 * menuSidebar.setOnMenuClick(index -> {
 *     contentArea.handleMenuClick(index);
 * });
 *
 * // Gestione ricerca
 * searchBar.setOnSearch(query -> {
 *     contentArea.handleSearch(query, book -> showBookDetails(book));
 * });
 *
 * // Ricerca avanzata con filtri
 * contentArea.handleSearch("author:Tolkien year:1950-1970", null);
 * contentArea.handleSearch("title-only:Hobbit", null);
 * }</pre>
 *
 * <h3>Gestione Stato e Lifecycle:</h3>
 * <ul>
 *   <li>Inizializzazione lazy per performance ottimizzate</li>
 *   <li>Cleanup automatico delle cache per gestione memoria</li>
 *   <li>State preservation durante transizioni sezioni</li>
 *   <li>Error recovery automatico per operazioni fallite</li>
 * </ul>
 *
 * <h3>Thread Safety e Performance:</h3>
 * <ul>
 *   <li>Utilizzo di {@link Platform#runLater(Runnable)} per thread safety</li>
 *   <li>Operazioni asincrone per caricamento dati non-bloccante</li>
 *   <li>Lazy loading e caching intelligente</li>
 *   <li>Fallback automatici per scenari di errore</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see BookSectionFactory
 * @see ExploreIntegration
 * @see PopupManager
 * @see AuthenticationManager
 */
public class ContentArea {

    /** Servizio per operazioni sui libri */
    private final BookService bookService;

    /** Flag indicante disponibilità del server */
    private final boolean serverAvailable;

    /** Manager per autenticazione e permessi utente */
    private AuthenticationManager authManager;

    /** Container principale per il contenuto */
    private VBox content;

    /** Factory per creazione sezioni di libri */
    private BookSectionFactory sectionFactory;

    /** Integrazione per la sezione esplora */
    private ExploreIntegration exploreIntegration;

    /** Cache libri in evidenza per navigazione contestuale */
    private List<Book> featuredBooks = new ArrayList<>();

    /** Cache libri gratuiti per navigazione contestuale */
    private List<Book> freeBooks = new ArrayList<>();

    /** Cache nuovi libri per navigazione contestuale */
    private List<Book> newBooks = new ArrayList<>();

    /** Cache risultati ricerca normale */
    private List<Book> searchResults = new ArrayList<>();

    /** Cache risultati ricerca avanzata */
    private List<Book> advancedSearchResults = new ArrayList<>();

    /** Vista categoria attualmente visualizzata */
    private CategoryView currentCategoryView = null;

    /**
     * Costruttore del componente ContentArea.
     * <p>
     * Inizializza il componente con i servizi necessari e configura
     * automaticamente il sistema di navigazione contestuale. Non esegue
     * caricamento dati ma prepara l'infrastruttura per l'uso.
     * </p>
     *
     * @param bookService servizio per operazioni sui libri
     * @param serverAvailable flag indicante se il server è disponibile
     * @param authManager manager per autenticazione utente
     * @throws IllegalArgumentException se bookService è {@code null}
     */
    public ContentArea(BookService bookService, boolean serverAvailable, AuthenticationManager authManager) {
        if (bookService == null) {
            throw new IllegalArgumentException("BookService non può essere null");
        }

        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.authManager = authManager;
        this.sectionFactory = new BookSectionFactory(bookService, serverAvailable);

        setupContextualNavigation();
    }

    /**
     * Aggiorna il manager di autenticazione.
     * <p>
     * Permette di aggiornare il riferimento al manager di autenticazione
     * durante il runtime, utile per aggiornamenti di configurazione o
     * cambi di stato utente.
     * </p>
     *
     * @param authManager il nuovo manager di autenticazione
     */
    public void setAuthManager(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Configura il sistema di navigazione contestuale con callback per cache.
     * <p>
     * Imposta i callback per le diverse sezioni che permettono di mantenere
     * cache aggiornate per la navigazione tra libri. Il sistema garantisce
     * che ogni sezione mantenga il proprio contesto di navigazione per
     * operazioni next/previous nel popup dei dettagli.
     * </p>
     *
     * <h4>Cache configurate:</h4>
     * <ul>
     *   <li>Featured books per sezione in evidenza</li>
     *   <li>Free books per sezione consigli</li>
     *   <li>New books per sezione novità</li>
     *   <li>Search results per risultati ricerca</li>
     * </ul>
     *
     * <h4>Gestione memoria:</h4>
     * <p>
     * Ogni callback crea copie difensive per evitare modifiche accidentali
     * delle cache e garantire consistency dei dati di navigazione.
     * </p>
     */
    private void setupContextualNavigation() {
        // Configura callback per salvare libri per sezione
        this.sectionFactory.setFeaturedBooksCallback(books -> {
            this.featuredBooks = new ArrayList<>(books); // Copia difensiva
            System.out.println("📚 Featured books salvati per navigazione: " + books.size());
        });

        this.sectionFactory.setFreeBooksCallback(books -> {
            this.freeBooks = new ArrayList<>(books);
            System.out.println("🆓 Free books salvati per navigazione: " + books.size());
        });

        this.sectionFactory.setNewBooksCallback(books -> {
            this.newBooks = new ArrayList<>(books);
            System.out.println("✨ New books salvati per navigazione: " + books.size());
        });

        this.sectionFactory.setSearchResultsCallback(books -> {
            this.searchResults = new ArrayList<>(books);
            System.out.println("🔍 Search results salvati per navigazione: " + books.size());
        });

        //debug
        this.sectionFactory.setFreeBooksCallback(books -> {
            this.freeBooks = new ArrayList<>(books);
            System.out.println("🆓 ✅ CALLBACK: Free books salvati per navigazione: " + books.size());
            // Debug: stampa i titoli per verificare
            for (int i = 0; i < Math.min(books.size(), 3); i++) {
                System.out.println("   " + (i+1) + ". " + books.get(i).getTitle());
            }
        });
    }

    /**
     * Crea e configura l'area di contenuto principale.
     * <p>
     * Factory method che costruisce il container principale per il contenuto
     * con configurazioni ottimizzate per scroll e layout. Il container è
     * pronto per ricevere diverse tipologie di contenuto.
     * </p>
     *
     * @return {@link ScrollPane} configurato per l'area contenuto
     */
    public ScrollPane createContentArea() {
        content = new VBox(20);
        content.setId("content");
        content.setPadding(new Insets(15, 20, 30, 20));
        content.setStyle("-fx-background-color: #1e1e1e;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        return scrollPane;
    }

    /**
     * Configura il gestore per i click sui libri integrato con PopupManager.
     * <p>
     * Imposta il callback per gestire i click sui libri utilizzando
     * PopupManager per la visualizzazione dei dettagli. Il sistema
     * determina automaticamente il contesto di navigazione appropriato.
     * </p>
     *
     * @param handler callback legacy per compatibilità (non utilizzato)
     */
    public void setBookClickHandler(Consumer<Book> handler) {

        // Usa PopupManager handler invece di gestione diretta
        Consumer<Book> popupManagerHandler = book -> handleBookClickWithPopupManager(book);
        this.sectionFactory.setBookClickHandler(popupManagerHandler);
    }

    /**
     * Gestisce i click sui libri utilizzando PopupManager con navigazione contestuale.
     * <p>
     * Implementa la logica centrale per gestire i click sui libri, determinando
     * automaticamente il contesto di navigazione appropriato e delegando a
     * PopupManager per la visualizzazione. Include gestione di errori robusta
     * e inizializzazione di emergenza per PopupManager.
     * </p>
     *
     * <h4>Operazioni eseguite:</h4>
     * <ol>
     *   <li>Validazione parametri input</li>
     *   <li>Determinazione contesto navigazione via cache</li>
     *   <li>Verifica/inizializzazione PopupManager</li>
     *   <li>Apertura popup dettagli con contesto</li>
     * </ol>
     *
     * <h4>Gestione errori:</h4>
     * <ul>
     *   <li>Controllo null safety per parametri</li>
     *   <li>Inizializzazione emergenza PopupManager</li>
     *   <li>Fallback per container principale mancante</li>
     *   <li>Exception handling con logging dettagliato</li>
     * </ul>
     *
     * @param book il libro su cui è stato fatto click
     * @see #determineNavigationContext(Book)
     * @see #findMainRootContainer()
     */
    private void handleBookClickWithPopupManager(Book book) {
        if (book == null) {
            System.err.println("❌ ContentArea: Libro null nel click handler");
            return;
        }

        System.out.println("📖 ContentArea: Click su '" + book.getTitle() + "'");

        try {
            // Determina contesto di navigazione
            List<Book> navigationBooks = determineNavigationContext(book);
            System.out.println("📚 Navigazione tra " + navigationBooks.size() + " libri");

            // Usa PopupManager
            PopupManager popupManager = PopupManager.getInstance();

            if (!popupManager.isInitialized()) {
                System.err.println("❌ PopupManager non inizializzato! Inizializzazione di emergenza...");

                // Tentativo di inizializzazione di emergenza
                StackPane mainRoot = findMainRootContainer();
                if (mainRoot != null) {
                    popupManager.initialize(mainRoot);
                } else {
                    System.err.println("❌ Impossibile trovare container principale!");
                    return;
                }
            }

            // Apri il popup
            popupManager.showBookDetails(book, navigationBooks, authManager);

        } catch (Exception e) {
            System.err.println("❌ ContentArea: Errore nel click handler - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Determina il contesto di navigazione appropriato per un libro specifico.
     * <p>
     * Analizza le cache delle diverse sezioni per identificare in quale
     * contesto si trova il libro e restituisce la lista appropriata per
     * la navigazione contestuale. Include logging dettagliato per debugging.
     * </p>
     *
     * <h4>Priorità di ricerca:</h4>
     * <ol>
     *   <li>Featured books (libri in evidenza)</li>
     *   <li>Free books (consigli gratuiti)</li>
     *   <li>New books (nuove uscite)</li>
     *   <li>Search results (risultati ricerca normale)</li>
     *   <li>Advanced search results (ricerca avanzata)</li>
     *   <li>Fallback: lista singola con solo il libro corrente</li>
     * </ol>
     *
     * @param book il libro per cui determinare il contesto
     * @return lista di libri per navigazione contestuale
     * @throws IllegalArgumentException se book è {@code null}
     */
    private List<Book> determineNavigationContext(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book non può essere null");
        }

        //debug
        System.out.println("🔍 DEBUG: Determinazione contesto per libro: " + book.getTitle());
        System.out.println("   - Featured books cache: " + featuredBooks.size());
        System.out.println("   - Free books cache: " + freeBooks.size());
        System.out.println("   - New books cache: " + newBooks.size());

        if (featuredBooks.contains(book)) {
            System.out.println("   -> Trovato in FEATURED");
            return featuredBooks;
        } else if (freeBooks.contains(book)) {
            System.out.println("   -> Trovato in FREE ✅");
            return freeBooks;
        } else if (newBooks.contains(book)) {
            System.out.println("   -> Trovato in NEW");
            return newBooks;
        }
        // Controlla in quale sezione si trova il libro
        if (featuredBooks.contains(book)) {
            return featuredBooks;
        } else if (freeBooks.contains(book)) {
            return freeBooks;
        } else if (newBooks.contains(book)) {
            return newBooks;
        } else if (searchResults.contains(book)) {
            return searchResults;
        } else if (advancedSearchResults.contains(book)) {
            return advancedSearchResults;
        } else {
            // Fallback: crea una lista con solo questo libro
            List<Book> singleBookList = new ArrayList<>();
            singleBookList.add(book);
            return singleBookList;
        }
    }

    /**
     * Cerca il container principale StackPane risalendo la gerarchia parent.
     * <p>
     * Implementa una ricerca nel parent hierarchy per trovare il container
     * StackPane principale necessario per l'inizializzazione di PopupManager.
     * Utilizzato per scenari di inizializzazione di emergenza.
     * </p>
     *
     * @return StackPane principale se trovato, {@code null} altrimenti
     */
    private StackPane findMainRootContainer() {
        try {
            // Cerca nel parent hierarchy
            if (content != null && content.getParent() != null) {
                var parent = content.getParent();
                while (parent != null) {
                    if (parent instanceof StackPane) {
                        return (StackPane) parent;
                    }
                    parent = parent.getParent();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nella ricerca del main root container: " + e.getMessage());
        }
        return null;
    }

    /**
     * Configura l'integrazione con la sezione Esplora.
     * <p>
     * Imposta il componente ExploreIntegration e configura il gestore click
     * per utilizzare PopupManager. Essenziale per il funzionamento della
     * sezione esplorazione categorie.
     * </p>
     *
     * @param integration componente di integrazione esplora
     */
    public void setExploreIntegration(ExploreIntegration integration) {
        this.exploreIntegration = integration;
        if (integration != null) {
            integration.setBookClickHandler(book -> handleBookClickWithPopupManager(book));
            System.out.println("✅ ExploreIntegration configurata con PopupManager handler");
        }
    }

    /**
     * Forza il ritorno alla vista home ripulendo stato e ricaricando contenuto.
     * <p>
     * Metodo di utilità per forzare il ritorno alla home page, utile per
     * operazioni di reset o navigazione programmatica.
     * </p>
     */
    public void forceHomeView() {
        System.out.println("🏠 Forzatura ritorno alla vista home");
        loadInitialContent();
    }

    /**
     * Gestisce gli eventi di click dal menu della sidebar principale.
     * <p>
     * Dispatcher centrale per la navigazione tra le diverse sezioni
     * dell'applicazione basato sull'index del menu selezionato.
     * </p>
     *
     * <h4>Mapping menu index:</h4>
     * <ul>
     *   <li><strong>0:</strong> Home - Contenuto principale con sezioni</li>
     *   <li><strong>1:</strong> Le Mie Librerie - Gestione librerie personali</li>
     *   <li><strong>2:</strong> Esplora - Navigazione categorie e scoperta</li>
     * </ul>
     *
     * @param menuIndex indice del menu selezionato
     */
    public void handleMenuClick(int menuIndex) {
        System.out.println("🎯 ContentArea: Click menu index " + menuIndex);

        switch (menuIndex) {
            case 0: // Home
                showHome();
                break;
            case 1: // Le Mie Librerie
                showMyLibraries();
                break;
            case 2: // Esplora
                showExplore();
                break;
            default:
                System.out.println("❓ Menu index non riconosciuto: " + menuIndex);
                break;
        }
    }

    /**
     * Visualizza la sezione Home con contenuto principale.
     */
    private void showHome() {
        System.out.println("🏠 ContentArea: Caricamento Home");
        loadInitialContent(); // Torna al comportamento originale
    }

    /**
     * Visualizza la sezione Esplora con integrazione categorie.
     * <p>
     * Carica la vista esplorazione utilizzando ExploreIntegration se disponibile,
     * altrimenti mostra un placeholder. Include gestione errori robusta e
     * fallback per scenari di failure.
     * </p>
     */
    private void showExplore() {
        System.out.println("🔍 ContentArea: Caricamento sezione Esplora");

        if (exploreIntegration != null) {
            try {
                // Pulisci il contenuto corrente
                content.getChildren().clear();

                // Crea la vista Esplora
                ScrollPane exploreView = exploreIntegration.createExploreView();

                // Crea container per la vista Esplora
                VBox exploreContainer = new VBox();
                exploreContainer.setStyle("-fx-background-color: #1a1a1c;");

                // Estrai il contenuto dallo ScrollPane e aggiungilo al container
                if (exploreView.getContent() instanceof VBox) {
                    VBox exploreContent = (VBox) exploreView.getContent();
                    exploreContainer.getChildren().add(exploreContent);
                } else {
                    exploreContainer.getChildren().add(exploreView.getContent());
                }

                // Aggiungi al content area esistente
                content.getChildren().add(exploreContainer);

                System.out.println("✅ Sezione Esplora caricata correttamente");

            } catch (Exception e) {
                System.err.println("❌ Errore caricamento Esplora: " + e.getMessage());
                e.printStackTrace();
                showPlaceholderSection("🔍 Esplora", "Errore nel caricamento della sezione Esplora");
            }
        } else {
            System.err.println("❌ ExploreIntegration non inizializzato");
            showPlaceholderSection("🔍 Esplora", "Sezione Esplora non disponibile");
        }
    }

    /**
     * Visualizza la sezione Le Mie Librerie (placeholder).
     */
    private void showMyLibraries() {
        showPlaceholderSection("📚 Le Mie Librerie", "Gestisci le tue collezioni private di libri");
    }

    /**
     * Visualizza una sezione placeholder per funzionalità in sviluppo.
     * <p>
     * Utility method per creare sezioni placeholder durante lo sviluppo
     * o per gestire scenari di errore con interfaccia user-friendly.
     * </p>
     *
     * @param title titolo della sezione
     * @param description descrizione o messaggio per l'utente
     */
    private void showPlaceholderSection(String title, String description) {
        if (content != null) {
            content.getChildren().clear();

            VBox placeholderContainer = new VBox(20);
            placeholderContainer.setPadding(new Insets(60));
            placeholderContainer.setAlignment(Pos.CENTER);
            placeholderContainer.setStyle("-fx-background-color: #1e1e1e;");

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            titleLabel.setTextFill(Color.WHITE);

            Label descLabel = new Label(description);
            descLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
            descLabel.setTextFill(Color.LIGHTGRAY);
            descLabel.setWrapText(true);
            descLabel.setAlignment(Pos.CENTER);

            placeholderContainer.getChildren().addAll(titleLabel, descLabel);
            content.getChildren().add(placeholderContainer);
        }
    }

    /**
     * Carica il contenuto iniziale solo se necessario (ottimizzazione).
     * <p>
     * Versione ottimizzata che verifica se il contenuto è già stato caricato
     * per evitare ricaricamenti non necessari. Migliora le performance
     * e l'esperienza utente.
     * </p>
     */
    public void loadInitialContentOnce() {
        // Controlla se il content è vuoto o contiene solo placeholder
        boolean needsLoading = content.getChildren().isEmpty() ||
                content.getChildren().stream().allMatch(node ->
                        node instanceof VBox &&
                                ((VBox) node).getChildren().stream().anyMatch(child ->
                                        child instanceof Label &&
                                                (((Label) child).getText().contains("placeholder") ||
                                                        ((Label) child).getText().contains("Errore") ||
                                                        ((Label) child).getText().contains("Le Mie Librerie") ||
                                                        ((Label) child).getText().contains("Esplora"))
                                )
                );

        if (needsLoading) {
            System.out.println("🏠 ContentArea: Caricamento necessario, eseguo loadInitialContent");
            loadInitialContent();
        } else {
            System.out.println("✅ ContentArea: Home già caricata, salto il caricamento");
        }
    }

    /**
     * Carica il contenuto iniziale della home page con reset cache.
     * <p>
     * Ripulisce tutto lo stato esistente e ricarica le sezioni principali
     * dell'applicazione (featured, consigli, nuovi). Include reset completo
     * delle cache per garantire contenuto fresco.
     * </p>
     */
    public void loadInitialContent() {
        System.out.println("🏠 ContentArea: Caricamento contenuto iniziale");

        // Reset cache
        featuredBooks.clear();
        freeBooks.clear();
        newBooks.clear();
        searchResults.clear();
        advancedSearchResults.clear();

        if (content != null) {
            // Pulisci TUTTO il contenuto esistente
            content.getChildren().clear();
            System.out.println("🧹 Content pulito completamente");

            // Sezioni principali
            content.getChildren().addAll(
                    sectionFactory.createFeaturedSection(),
                    sectionFactory.createBookSection("📚 I Nostri Consigli", "free"),
                    sectionFactory.createBookSection("✨ Nuovi", "new")
            );
        }
    }

    /**
     * Gestisce le ricerche con supporto per sintassi avanzata e filtri multipli.
     * <p>
     * Sistema di ricerca completo che supporta diversi tipi di query con
     * sintassi specializzata per ricerche specifiche. Include fallback
     * automatici e gestione errori robusta per garantire sempre un risultato.
     * </p>
     *
     * <h4>Sintassi supportate:</h4>
     * <ul>
     *   <li><strong>Query normale:</strong> Ricerca libera su titolo e autore</li>
     *   <li><strong>title-only:query:</strong> Ricerca specifica solo nel titolo</li>
     *   <li><strong>author:nome:</strong> Ricerca filtrata per autore</li>
     *   <li><strong>year:inizio-fine:</strong> Filtro per range anni pubblicazione</li>
     *   <li><strong>Combinata:</strong> author:nome year:range per query complesse</li>
     * </ul>
     *
     * <h4>Gestione cache:</h4>
     * <p>
     * I risultati vengono salvati nelle cache appropriate (searchResults o
     * advancedSearchResults) per navigazione contestuale tra i risultati.
     * </p>
     *
     * @param query stringa di ricerca con eventuale sintassi speciale
     * @param clickHandler callback per gestire click sui risultati (legacy)
     */
    public void handleSearch(String query, Consumer<Book> clickHandler) {
        if (query == null || query.trim().isEmpty()) {
            loadInitialContent();
            return;
        }

        // Reset cache precedenti
        searchResults.clear();
        advancedSearchResults.clear();

        System.out.println("🔍 ContentArea handling search: " + query);

        // Usa PopupManager handler invece di quello passato
        Consumer<Book> popupManagerHandler = book -> handleBookClickWithPopupManager(book);

        // Gestisci query speciali per ricerca avanzata
        if (query.startsWith("title-only:")) {
            // Query specifica solo per titolo
            String title = query.substring(11).trim();
            handleTitleOnlySearch(title, popupManagerHandler);

        } else if (query.startsWith("author:")) {
            if (query.contains("year:")) {
                // Ricerca combinata autore + anno
                handleYearFilteredSearch(query, popupManagerHandler);
            } else {
                // Solo ricerca per autore
                String author = query.substring(7).trim();
                handleAuthorSearch(author, popupManagerHandler);
            }
        } else if (query.contains("year:")) {
            // Ricerca con filtro anno
            handleYearFilteredSearch(query, popupManagerHandler);
        } else {
            // Ricerca normale dalla barra (titolo + autore)
            handleTitleSearch(query, popupManagerHandler);
        }
    }

    /**
     * Gestisce ricerca normale su titolo e autore.
     */
    private void handleTitleSearch(String query, Consumer<Book> clickHandler) {
        System.out.println("📖 Ricerca GENERALE (titolo + autore): " + query);
        // La ricerca normale dalla barra cerca in titolo E autore
        sectionFactory.performSearch(query, content, clickHandler);
    }

    /**
     * Gestisce ricerca specifica solo nel titolo con fallback automatico.
     * <p>
     * Tenta prima una ricerca specifica per titolo tramite BookService,
     * in caso di fallimento utilizza ricerca generale con filtro lato client.
     * </p>
     */
    private void handleTitleOnlySearch(String title, Consumer<Book> clickHandler) {
        System.out.println("📖 Ricerca SPECIFICA solo titolo: " + title);

        content.getChildren().clear();
        Label loadingLabel = new Label("🔍 Ricerca per titolo: " + title + "...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("🔍 Tentativo ricerca titolo specifica...");
                        return bookService.searchBooksByTitle(title);
                    } catch (Exception e) {
                        System.err.println("❌ Errore ricerca titolo specifica: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        System.out.println("✅ Ricerca titolo completata: " + results.size() + " risultati");
                        this.advancedSearchResults = new ArrayList<>(results);
                        displaySearchResults(results, "📖 Titolo: " + title, clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Errore ricerca titolo: " + throwable.getMessage());

                    // Ricerca generale + filtro lato client
                    Platform.runLater(() -> {
                        System.out.println("🔄 Fallback: ricerca generale con filtro titolo");
                        handleTitleSearchFallback(title, clickHandler);
                    });
                    return null;
                });
    }

    /**
     * Fallback per ricerca titolo usando filtro lato client.
     */
    private void handleTitleSearchFallback(String title, Consumer<Book> clickHandler) {
        System.out.println("🔄 Fallback ricerca titolo con filtro client");

        bookService.searchBooksAsync(title)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        // FILTRO SOLO PER TITOLO
                        List<Book> titleResults = filterBooksByTitleOnly(results, title);
                        this.advancedSearchResults = new ArrayList<>(titleResults);
                        displaySearchResults(titleResults, "📖 Titolo: " + title + " (filtrato)", clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore ricerca titolo: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    /**
     * Gestisce ricerca per autore con filtro lato client.
     */
    private void handleAuthorSearch(String author, Consumer<Book> clickHandler) {
        System.out.println("👤 Ricerca per autore: " + author);

        content.getChildren().clear();
        Label loadingLabel = new Label("🔍 Ricerca per autore: " + author + "...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        bookService.searchBooksAsync(author)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        List<Book> authorResults = filterBooksByAuthor(results, author);
                        this.advancedSearchResults = new ArrayList<>(authorResults);
                        displaySearchResults(authorResults, "👤 Autore: " + author, clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore nella ricerca per autore: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    /**
     * Filtra libri per corrispondenza solo nel titolo.
     * <p>
     * Implementa filtro case-insensitive per ricerche specifiche nel titolo,
     * utilizzato per ricerche avanzate e fallback.
     * </p>
     *
     * @param books lista libri da filtrare
     * @param targetTitle titolo target per il filtro
     * @return lista filtrata di libri corrispondenti
     */
    private List<Book> filterBooksByTitleOnly(List<Book> books, String targetTitle) {
        if (targetTitle == null || targetTitle.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();
        String searchTitle = targetTitle.toLowerCase().trim();

        for (Book book : books) {
            if (book.getTitle() != null &&
                    book.getTitle().toLowerCase().contains(searchTitle)) {
                filtered.add(book);
            }
        }

        System.out.println("📖 Filtro SOLO titolo '" + targetTitle + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    /**
     * Filtra libri per corrispondenza nell'autore.
     *
     * @param books lista libri da filtrare
     * @param targetAuthor autore target per il filtro
     * @return lista filtrata di libri dell'autore
     */
    private List<Book> filterBooksByAuthor(List<Book> books, String targetAuthor) {
        if (targetAuthor == null || targetAuthor.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();
        String searchAuthor = targetAuthor.toLowerCase().trim();

        for (Book book : books) {
            if (book.getAuthor() != null &&
                    book.getAuthor().toLowerCase().contains(searchAuthor)) {
                filtered.add(book);
            }
        }

        System.out.println("👤 Filtro autore '" + targetAuthor + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    /**
     * Gestisce ricerca combinata autore + anno con parsing query complessa.
     */
    private void handleYearFilteredSearch(String query, Consumer<Book> clickHandler) {
        System.out.println("📅 Ricerca combinata autore + anno: " + query);

        String author = "";
        String yearRange = "";

        if (query.contains("author:") && query.contains("year:")) {
            String[] parts = query.split("\\s+");
            for (String part : parts) {
                if (part.startsWith("author:")) {
                    author = part.substring(7);
                } else if (part.startsWith("year:")) {
                    yearRange = part.substring(5);
                }
            }
        }

        final String finalAuthor = author;
        final String finalYearRange = yearRange;

        content.getChildren().clear();
        Label loadingLabel = new Label("🔍🗓️ Ricerca avanzata: " + finalAuthor + " (" + finalYearRange + ")...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        bookService.searchBooksAsync(finalAuthor)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        // FILTRO LATO CLIENT per autore e anno
                        List<Book> authorResults = filterBooksByAuthor(results, finalAuthor);
                        List<Book> filteredResults = filterBooksByYearRange(authorResults, finalYearRange);

                        this.advancedSearchResults = new ArrayList<>(filteredResults);
                        displaySearchResults(filteredResults,
                                "👤📅 " + finalAuthor + " (" + finalYearRange + ")", clickHandler);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore ricerca avanzata: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    /**
     * Filtra libri per range di anni di pubblicazione.
     * <p>
     * Supporta diverse sintassi per range anni:
     * - "1950-1970" per range completo
     * - "1950-" per dal 1950 in poi
     * - "-1970" per fino al 1970
     * </p>
     *
     * @param books lista libri da filtrare
     * @param yearRange stringa range anni (formato: "inizio-fine")
     * @return lista filtrata per range anni
     */
    private List<Book> filterBooksByYearRange(List<Book> books, String yearRange) {
        if (yearRange == null || yearRange.trim().isEmpty()) {
            return books;
        }

        List<Book> filtered = new ArrayList<>();

        String[] yearParts = yearRange.split("-");
        Integer yearFrom = null;
        Integer yearTo = null;

        try {
            if (yearParts.length >= 1 && !yearParts[0].isEmpty()) {
                yearFrom = Integer.parseInt(yearParts[0].trim());
            }
            if (yearParts.length >= 2 && !yearParts[1].isEmpty()) {
                yearTo = Integer.parseInt(yearParts[1].trim());
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Formato anno non valido: " + yearRange);
            return books; // Restituisci tutti se il formato è sbagliato
        }

        for (Book book : books) {
            try {
                String bookYearStr = book.getPublishYear();
                if (bookYearStr == null || bookYearStr.trim().isEmpty()) {
                    continue;
                }

                int bookYear = Integer.parseInt(bookYearStr.trim());
                boolean inRange = true;

                if (yearFrom != null && bookYear < yearFrom) {
                    inRange = false;
                }
                if (yearTo != null && bookYear > yearTo) {
                    inRange = false;
                }

                if (inRange) {
                    filtered.add(book);
                }

            } catch (NumberFormatException e) {
                // Salta libri con anno non numerico
                continue;
            }
        }

        System.out.println("📅 Filtro anno '" + yearRange + "': " + books.size() + " → " + filtered.size());
        return filtered;
    }

    /**
     * Visualizza risultati di ricerca con layout ottimizzato.
     * <p>
     * Crea interfaccia per visualizzare risultati con header informativo,
     * griglia libri responsive, e gestione caso "nessun risultato".
     * </p>
     *
     * @param results lista risultati da visualizzare
     * @param searchTitle titolo descrittivo della ricerca
     * @param clickHandler callback per click sui risultati
     */
    private void displaySearchResults(List<Book> results, String searchTitle, Consumer<Book> clickHandler) {
        content.getChildren().clear();

        if (results.isEmpty()) {
            Label noResults = new Label("❌ Nessun risultato trovato per: " + searchTitle);
            noResults.setTextFill(Color.WHITE);
            noResults.setFont(Font.font("System", FontWeight.NORMAL, 18));

            Label suggestion = new Label("💡 Prova con parole chiave diverse");
            suggestion.setTextFill(Color.GRAY);
            suggestion.setFont(Font.font("System", FontWeight.NORMAL, 14));

            VBox noResultsBox = new VBox(10, noResults, suggestion);
            noResultsBox.setAlignment(Pos.CENTER);
            noResultsBox.setPadding(new Insets(50));

            content.getChildren().add(noResultsBox);
        } else {
            // Crea sezione risultati
            Label title = new Label(searchTitle + " (" + results.size() + " risultati)");
            title.setFont(Font.font("System", FontWeight.BOLD, 20));
            title.setTextFill(Color.WHITE);
            title.setPadding(new Insets(0, 0, 15, 0));

            BookGridBuilder gridBuilder = new BookGridBuilder();
            gridBuilder.setBookClickHandler(clickHandler);

            VBox resultsContainer = new VBox(15);
            resultsContainer.setPadding(new Insets(15, 20, 20, 20));
            resultsContainer.getChildren().add(title);

            FlowPane bookGrid = gridBuilder.createOptimizedBookGrid();
            gridBuilder.populateBookGrid(results, bookGrid, null);

            ScrollPane scroll = new ScrollPane(bookGrid);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: transparent;");

            resultsContainer.getChildren().add(scroll);
            content.getChildren().add(resultsContainer);
        }
    }

    /**
     * Visualizza contenuto personalizzato nell'area principale.
     * <p>
     * Utility method per mostrare contenuto custom, utile per integrazioni
     * esterne o viste specializzate.
     * </p>
     *
     * @param customContent contenuto personalizzato da visualizzare
     */
    public void showCustomContent(VBox customContent) {
        content.getChildren().clear();
        content.getChildren().add(customContent);
        VBox.setVgrow(customContent, Priority.ALWAYS);
    }

    /**
     * Configura callback per gestione cache libri (compatibilità MainWindow).
     */
    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        if (this.sectionFactory != null) {
            this.sectionFactory.setCachedBooksCallback(callback);
        }
    }

    /**
     * Mostra risultati ricerca avanzata (compatibilità MainWindow).
     */
    public void showAdvancedSearchResults(Object searchResult) {
        System.out.println("🔍 Mostra risultati ricerca avanzata: " + searchResult);
        // Per ora semplicemente delega alla gestione ricerca normale
        if (searchResult != null) {
            String query = searchResult.toString();
            handleSearch(query, book -> handleBookClickWithPopupManager(book));
        }
    }

    /**
     * Debug dello stato cache per monitoring e troubleshooting.
     */
    public void debugCacheState() {
        System.out.println("🔍 ===== CONTENTAREA CACHE DEBUG =====");
        System.out.println("Featured books: " + featuredBooks.size());
        System.out.println("Free books: " + freeBooks.size());
        System.out.println("New books: " + newBooks.size());
        System.out.println("Search results: " + searchResults.size());
        System.out.println("Advanced search results: " + advancedSearchResults.size());
    }

    /**
     * Cleanup delle risorse e reset stato per shutdown applicazione.
     */
    public void cleanup() {
        System.out.println("🧹 ContentArea cleanup");
        if (featuredBooks != null) featuredBooks.clear();
        if (freeBooks != null) freeBooks.clear();
        if (newBooks != null) newBooks.clear();
        if (searchResults != null) searchResults.clear();
        if (advancedSearchResults != null) advancedSearchResults.clear();
        currentCategoryView = null;
    }

    /**
     * Ottiene il container VBox principale del contenuto.
     *
     * @return container principale per compatibilità
     */
    public VBox getContent() {
        return content;
    }

    /**
     * Ottiene il servizio libri utilizzato.
     *
     * @return istanza BookService per operazioni sui libri
     */
    public BookService getBookService() {
        return bookService;
    }
}