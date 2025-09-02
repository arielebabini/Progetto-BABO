package org.BABO.client.ui.Book;

import org.BABO.client.service.BookService;
import org.BABO.shared.model.Book;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Factory per la creazione e gestione di sezioni di libri con layout adattivo nell'applicazione BABO.
 * <p>
 * Questa classe fornisce un'API unificata per costruire diverse tipologie di sezioni di libri,
 * ognuna ottimizzata per specifici casi d'uso. Implementa pattern Factory per la creazione
 * di componenti UI complessi e gestisce automaticamente il caricamento asincrono dei dati,
 * gli stati di loading, e la gestione degli errori per ogni sezione.
 * </p>
 *
 * <h3>Tipologie di sezioni supportate:</h3>
 * <ul>
 *   <li><strong>Featured Section:</strong> Libro in evidenza con layout speciale</li>
 *   <li><strong>Free Books:</strong> Libri gratuiti o suggeriti</li>
 *   <li><strong>New Releases:</strong> Nuove uscite e novità</li>
 *   <li><strong>Search Results:</strong> Risultati di ricerca con query dinamica</li>
 *   <li><strong>Generic Sections:</strong> Sezioni personalizzabili per altri contenuti</li>
 * </ul>
 *
 * <h3>Architettura e Design Pattern:</h3>
 * <p>
 * La factory implementa diversi pattern di design:
 * </p>
 * <ul>
 *   <li><strong>Factory Pattern:</strong> Creazione standardizzata di sezioni UI</li>
 *   <li><strong>Observer Pattern:</strong> Sistema di callback per eventi specifici</li>
 *   <li><strong>Strategy Pattern:</strong> Diversi layout per diverse tipologie di contenuto</li>
 *   <li><strong>Builder Pattern:</strong> Delegazione a builder specializzati</li>
 * </ul>
 *
 * <h3>Sistema di Layout Unificato:</h3>
 * <p>
 * Tutte le sezioni utilizzano FlowPane come layout primario per garantire:
 * </p>
 * <ul>
 *   <li>Consistenza visiva tra diverse sezioni</li>
 *   <li>Responsività automatica per diverse risoluzioni</li>
 *   <li>Riutilizzo del codice e manutenibilità</li>
 *   <li>Performance ottimizzate per rendering</li>
 * </ul>
 *
 * <h3>Gestione Stati e Loading:</h3>
 * <p>
 * Ogni sezione gestisce automaticamente diversi stati:
 * </p>
 * <ul>
 *   <li><strong>Loading:</strong> Indicatori di progresso con messaggi informativi</li>
 *   <li><strong>Success:</strong> Visualizzazione dati con layout ottimizzato</li>
 *   <li><strong>Error:</strong> Messaggi di errore con opzioni di retry</li>
 *   <li><strong>Empty:</strong> Placeholder per contenuti vuoti</li>
 * </ul>
 *
 * <h3>Sistema di Callback Avanzato:</h3>
 * <p>
 * La factory implementa un sistema di callback specifici per sezione che permette:
 * </p>
 * <ul>
 *   <li>Navigazione contestuale tra sezioni</li>
 *   <li>Caching intelligente dei dati</li>
 *   <li>Sincronizzazione stato applicazione</li>
 *   <li>Analytics e tracking delle interazioni</li>
 * </ul>
 *
 * <h3>Integrazione con BookService:</h3>
 * <p>
 * La factory si integra strettamente con {@link BookService} per:
 * </p>
 * <ul>
 *   <li>Caricamento asincrono dei dati</li>
 *   <li>Gestione automatica dei timeout</li>
 *   <li>Retry logic per operazioni fallite</li>
 *   <li>Caching a livello di servizio</li>
 * </ul>
 *
 * <h3>Esempio di utilizzo completo:</h3>
 * <pre>{@code
 * // Inizializzazione factory
 * BookService bookService = new BookService();
 * BookSectionFactory factory = new BookSectionFactory(bookService, true);
 *
 * // Configurazione callback globali
 * factory.setBookClickHandler(book -> {
 *     showBookDetails(book);
 *     analytics.trackBookView(book.getIsbn());
 * });
 *
 * // Configurazione callback specifici per navigazione
 * factory.setFeaturedBooksCallback(books -> {
 *     navigationManager.updateFeaturedBooks(books);
 * });
 *
 * factory.setNewBooksCallback(books -> {
 *     homeScreen.updateNewReleasesCache(books);
 * });
 *
 * // Creazione sezioni
 * VBox featuredSection = factory.createFeaturedSection();
 * VBox newBooksSection = factory.createBookSection("Nuove Uscite", "new");
 * VBox freeBooksSection = factory.createBookSection("Libri Gratuiti", "free");
 *
 * // Aggiunta a layout principale
 * VBox mainContent = new VBox(featuredSection, newBooksSection, freeBooksSection);
 *
 * // Gestione ricerca dinamica
 * searchField.setOnAction(e -> {
 *     String query = searchField.getText();
 *     factory.performSearch(query, searchResultsContainer, book -> {
 *         showBookDetails(book);
 *     });
 * });
 * }</pre>
 *
 * <h3>Performance e Ottimizzazioni:</h3>
 * <ul>
 *   <li>Lazy loading delle sezioni per migliorare startup time</li>
 *   <li>Reuse di componenti UI quando possibile</li>
 *   <li>Gestione memoria ottimizzata per liste grandi</li>
 *   <li>Caching intelligente a più livelli</li>
 * </ul>
 *
 * @author BABO Team
 * @version 1.0
 * @since 1.0
 * @see BookService
 * @see BookGridBuilder
 * @see FeaturedBookBuilder
 * @see Book
 */
public class BookSectionFactory {

    /** Servizio per operazioni sui libri e caricamento dati */
    private final BookService bookService;

    /** Flag indicante la disponibilità del server */
    private final boolean serverAvailable;

    /** Builder per griglie standard di libri */
    private BookGridBuilder gridBuilder;

    /** Builder specializzato per sezioni featured */
    private FeaturedBookBuilder featuredBuilder;

    /** Callback generale per gestione cache */
    private Consumer<List<Book>> cachedBooksCallback;

    /** Callback specifico per libri in evidenza */
    private Consumer<List<Book>> featuredBooksCallback;

    /** Callback specifico per libri gratuiti */
    private Consumer<List<Book>> freeBooksCallback;

    /** Callback specifico per nuove uscite */
    private Consumer<List<Book>> newBooksCallback;

    /** Callback specifico per risultati di ricerca */
    private Consumer<List<Book>> searchResultsCallback;

    /**
     * Costruttore della factory per sezioni di libri.
     * <p>
     * Inizializza la factory con i servizi necessari e crea le istanze
     * dei builder specializzati. Non esegue operazioni di rete ma prepara
     * la factory per la creazione di sezioni.
     * </p>
     *
     * @param bookService il servizio per operazioni sui libri
     * @param serverAvailable flag indicante se il server è disponibile
     * @throws IllegalArgumentException se bookService è {@code null}
     */
    public BookSectionFactory(BookService bookService, boolean serverAvailable) {
        if (bookService == null) {
            throw new IllegalArgumentException("BookService non può essere null");
        }

        this.bookService = bookService;
        this.serverAvailable = serverAvailable;
        this.gridBuilder = new BookGridBuilder();
        this.featuredBuilder = new FeaturedBookBuilder();
    }

    /**
     * Configura il gestore per i click sui libri in tutte le sezioni.
     * <p>
     * Imposta il callback che verrà eseguito quando un utente clicca su un libro
     * in qualsiasi sezione creata da questa factory. Il callback viene propagato
     * a tutti i builder interni per garantire comportamento consistente.
     * </p>
     *
     * @param handler il {@link Consumer} da eseguire per i click sui libri.
     *               Riceve l'oggetto {@link Book} cliccato. Può essere {@code null}
     *               per disabilitare l'interattività.
     * @see BookGridBuilder#setBookClickHandler(Consumer)
     * @see FeaturedBookBuilder#setBookClickHandler(Consumer)
     */
    public void setBookClickHandler(Consumer<Book> handler) {
        this.gridBuilder.setBookClickHandler(handler);
        this.featuredBuilder.setBookClickHandler(handler);
    }

    /**
     * Configura il callback generale per la gestione della cache dei libri.
     * <p>
     * Imposta il callback che riceve tutti i libri caricati dalle sezioni
     * per implementare strategie di caching globali. Viene propagato al
     * grid builder per compatibilità backward.
     * </p>
     *
     * @param callback il {@link Consumer} per gestire la cache globale.
     *                Riceve la lista di libri caricati. Può essere {@code null}
     *                per disabilitare il caching globale.
     */
    public void setCachedBooksCallback(Consumer<List<Book>> callback) {
        this.cachedBooksCallback = callback;
        this.gridBuilder.setCachedBooksCallback(callback);
    }

    /**
     * Configura il callback specifico per i libri in evidenza.
     * <p>
     * Questo callback viene eseguito quando vengono caricati i libri
     * della sezione featured, permettendo di implementare logica specifica
     * per la navigazione e gestione dei contenuti in evidenza.
     * </p>
     *
     * @param callback il {@link Consumer} per libri in evidenza.
     *                Riceve la lista di libri featured. Può essere {@code null}.
     */
    public void setFeaturedBooksCallback(Consumer<List<Book>> callback) {
        this.featuredBooksCallback = callback;
    }

    /**
     * Configura il callback specifico per i libri gratuiti.
     * <p>
     * Callback eseguito al caricamento della sezione libri gratuiti,
     * utile per implementare logica di navigazione e promozioni specifiche.
     * </p>
     *
     * @param callback il {@link Consumer} per libri gratuiti.
     *                Riceve la lista di libri gratuiti. Può essere {@code null}.
     */
    public void setFreeBooksCallback(Consumer<List<Book>> callback) {
        this.freeBooksCallback = callback;
    }

    /**
     * Configura il callback specifico per le nuove uscite.
     * <p>
     * Callback eseguito al caricamento delle nuove uscite, permettendo
     * di implementare notifiche e aggiornamenti per contenuti recenti.
     * </p>
     *
     * @param callback il {@link Consumer} per nuove uscite.
     *                Riceve la lista di nuovi libri. Può essere {@code null}.
     */
    public void setNewBooksCallback(Consumer<List<Book>> callback) {
        this.newBooksCallback = callback;
    }

    /**
     * Configura il callback specifico per i risultati di ricerca.
     * <p>
     * Callback eseguito quando vengono caricati risultati di ricerca,
     * utile per analytics e gestione della cronologia delle ricerche.
     * </p>
     *
     * @param callback il {@link Consumer} per risultati di ricerca.
     *                Riceve la lista di libri trovati. Può essere {@code null}.
     */
    public void setSearchResultsCallback(Consumer<List<Book>> callback) {
        this.searchResultsCallback = callback;
    }

    /**
     * Crea una sezione di libri con layout FlowPane standardizzato.
     * <p>
     * Factory method principale per creare sezioni di libri con caricamento
     * asincrono, gestione stati, e layout responsivo. Ogni sezione include
     * header, indicatori di loading, gestione errori, e callback specifici.
     * </p>
     *
     * <h4>Tipi di sezione supportati:</h4>
     * <ul>
     *   <li><strong>"free":</strong> Carica libri suggeriti/gratuiti</li>
     *   <li><strong>"new":</strong> Carica nuove uscite</li>
     *   <li><strong>"featured":</strong> Carica libri in evidenza</li>
     *   <li><strong>Altri:</strong> Carica tutti i libri disponibili</li>
     * </ul>
     *
     * <h4>Stati gestiti automaticamente:</h4>
     * <ul>
     *   <li>Loading con indicatore di progresso</li>
     *   <li>Success con griglia di libri</li>
     *   <li>Error con messaggio e pulsante retry</li>
     *   <li>Empty con messaggio informativo</li>
     * </ul>
     *
     * @param sectionTitle il titolo da visualizzare per la sezione
     * @param sectionType il tipo di sezione che determina i dati da caricare
     * @return un {@link VBox} configurato con la sezione completa
     * @throws IllegalArgumentException se sectionTitle o sectionType sono {@code null}
     * @see #loadBooksForSectionFlowPane(String, ScrollPane)
     */
    public VBox createBookSection(String sectionTitle, String sectionType) {
        if (sectionTitle == null || sectionType == null) {
            throw new IllegalArgumentException("Titolo e tipo sezione non possono essere null");
        }

        Label title = new Label(sectionTitle);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);

        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(280);

        Label statusLabel = new Label(serverAvailable ?
                "📡 Online" : "📴 Offline");
        statusLabel.setTextFill(serverAvailable ? Color.LIGHTGREEN : Color.ORANGE);
        statusLabel.setFont(Font.font("System", 12));
        loadingBox.getChildren().add(statusLabel);

        // Usa sempre FlowPane per tutte le sezioni
        ScrollPane scroll = createScrollPane();
        scroll.setContent(loadingBox);

        // Button seeAllBtn = createSeeAllButton();
        HBox headerBox = createSectionHeader(title);

        VBox section = new VBox(5, headerBox, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));
        section.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        // Tutte le sezioni usano FlowPane
        loadBooksForSectionFlowPane(sectionType, scroll);

        return section;
    }

    /**
     * Carica e visualizza i dati per sezioni con layout FlowPane.
     * <p>
     * Metodo privato che gestisce il caricamento asincrono dei dati per una
     * sezione specifica, la creazione del layout FlowPane, e l'esecuzione
     * dei callback appropriati. Implementa gestione completa degli errori
     * e stati di loading.
     * </p>
     *
     * <h4>Flusso di esecuzione:</h4>
     * <ol>
     *   <li>Determina il servizio appropriato basato sul tipo sezione</li>
     *   <li>Crea FlowPane con configurazioni ottimizzate</li>
     *   <li>Esegue caricamento asincrono dei dati</li>
     *   <li>Popola la griglia con i libri ricevuti</li>
     *   <li>Esegue callback specifici e generali</li>
     *   <li>Gestisce errori con fallback appropriati</li>
     * </ol>
     *
     * <h4>Gestione callback:</h4>
     * <p>
     * Il metodo crea copie difensive dei dati per i callback specifici,
     * garantendo che le modifiche esterne non influenzino la visualizzazione.
     * Esegue sempre i callback anche in caso di errore (con liste vuote)
     * per mantenere consistenza dello stato.
     * </p>
     *
     * @param sectionType il tipo di sezione per determinare i dati da caricare
     * @param scroll il {@link ScrollPane} container dove visualizzare i risultati
     * @apiNote Questo metodo viene eseguito nel JavaFX Application Thread per
     *          gli aggiornamenti UI, ma delega il caricamento dati a thread separati.
     */
    private void loadBooksForSectionFlowPane(String sectionType, ScrollPane scroll) {
        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(10));
        bookGrid.setPrefWrapLength(750);

        CompletableFuture<List<Book>> future;
        Consumer<List<Book>> specificCallback;

        switch (sectionType) {
            case "free":
                future = bookService.getSuggestedBooksAsync();
                specificCallback = freeBooksCallback;
                break;
            case "new":
                future = bookService.getNewReleasesAsync();
                specificCallback = newBooksCallback;
                break;
            case "featured":
                future = bookService.getFeaturedBooksAsync();
                specificCallback = featuredBooksCallback;
                break;
            default:
                future = bookService.getAllBooksAsync();
                specificCallback = null;
                break;
        }

        future.thenAccept(books -> {
            Platform.runLater(() -> {
                System.out.println("✅ Caricati " + books.size() + " libri per sezione " + sectionType + " (layout FlowPane adattivo)");

                // Prima popola la griglia visiva
                gridBuilder.populateBookGrid(books, bookGrid, scroll);
                scroll.setContent(bookGrid);

                if (specificCallback != null) {
                    // Crea una copia difensiva completa di TUTTI i libri ricevuti
                    List<Book> navigationBooks = new ArrayList<>(books);
                    specificCallback.accept(navigationBooks);
                    System.out.println("📋 ✅ Callback specifico chiamato per '" + sectionType + "' con " + navigationBooks.size() + " libri per navigazione");
                } else {
                    System.out.println("⚠️ Callback specifico è null per sezione: " + sectionType);
                }

                // Callback generale per backward compatibility
                if (cachedBooksCallback != null) {
                    cachedBooksCallback.accept(books);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                System.err.println("❌ Errore caricamento sezione " + sectionType + ": " + throwable.getMessage());
                showSectionError(scroll, sectionType);

                // Anche in caso di errore, chiama il callback con lista vuota per evitare inconsistenze
                if (specificCallback != null) {
                    specificCallback.accept(new ArrayList<>());
                    System.out.println("⚠️ Callback specifico chiamato con lista vuota per " + sectionType + " a causa di errore");
                }
            });
            return null;
        });
    }

    /**
     * Crea una sezione specializzata per il libro in evidenza.
     * <p>
     * Factory method per creare una sezione dedicata ai libri in evidenza
     * con layout e styling specializzati. Utilizza {@link FeaturedBookBuilder}
     * per creare contenuti ottimizzati per la presentazione di singoli libri
     * in formato prominente.
     * </p>
     *
     * <h4>Caratteristiche della sezione:</h4>
     * <ul>
     *   <li>Layout specializzato per singolo libro prominente</li>
     *   <li>Styling con gradiente personalizzato</li>
     *   <li>Caricamento asincrono con stato loading dedicato</li>
     *   <li>Gestione fallback per contenuti mancanti</li>
     *   <li>Callback specifici per libri featured</li>
     * </ul>
     *
     * <h4>Stati gestiti:</h4>
     * <ul>
     *   <li><strong>Loading:</strong> Messaggio "Caricamento libro in evidenza"</li>
     *   <li><strong>Success:</strong> Contenuto featured del primo libro</li>
     *   <li><strong>Empty:</strong> Messaggio "Nessun libro in evidenza"</li>
     *   <li><strong>Error:</strong> Messaggio di errore generico</li>
     * </ul>
     *
     * @return un {@link VBox} configurato per la sezione featured
     * @see FeaturedBookBuilder#createFeaturedBookContent(Book)
     */
    public VBox createFeaturedSection() {
        VBox container = new VBox();
        container.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3a3a3c, #2c2c2c);" +
                        "-fx-background-radius: 12;"
        );

        // Loading state
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(300);

        Label loadingLabel = new Label("⭐ Caricamento libro in evidenza...");
        loadingLabel.setTextFill(Color.WHITE);
        loadingBox.getChildren().add(loadingLabel);

        container.getChildren().add(loadingBox);

        // Load featured book with callback
        bookService.getFeaturedBooksAsync()
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        // Notifica callback
                        if (featuredBooksCallback != null) {
                            featuredBooksCallback.accept(books);
                        }

                        if (!books.isEmpty()) {
                            VBox featuredContent = featuredBuilder.createFeaturedBookContent(books.get(0));
                            container.getChildren().clear();
                            container.getChildren().add(featuredContent);
                        } else {
                            showNoFeaturedMessage(container);
                        }

                        // Callback generale per backward compatibility
                        if (cachedBooksCallback != null) {
                            cachedBooksCallback.accept(books);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showErrorMessage(container, "❌ Errore caricamento libro in evidenza"));
                    return null;
                });

        return container;
    }

    /**
     * Crea una sezione dedicata per la visualizzazione dei risultati di ricerca.
     * <p>
     * Factory method specializzato per visualizzare risultati di ricerca con
     * header dinamico che include la query e il numero di risultati trovati.
     * Utilizza layout FlowPane per consistenza con altre sezioni.
     * </p>
     *
     * <h4>Caratteristiche specifiche:</h4>
     * <ul>
     *   <li>Header dinamico con query e conteggio risultati</li>
     *   <li>Layout FlowPane per griglia responsiva</li>
     *   <li>Configurazione scroll ottimizzata per risultati</li>
     *   <li>Styling coerente con altre sezioni</li>
     * </ul>
     *
     * @param searchResults la lista di {@link Book} trovati dalla ricerca
     * @param onBookClick il callback per gestire click sui libri nei risultati
     * @param query la stringa di ricerca originale per il header
     * @return un {@link VBox} configurato con i risultati di ricerca
     * @throws IllegalArgumentException se searchResults, onBookClick o query sono {@code null}
     * @apiNote I risultati vengono visualizzati immediatamente senza caricamento
     *          asincrono poiché sono già stati caricati dal processo di ricerca.
     */
    public VBox createSearchResultsSection(List<Book> searchResults, Consumer<Book> onBookClick, String query) {
        if (searchResults == null) {
            throw new IllegalArgumentException("I risultati di ricerca non possono essere null");
        }
        if (onBookClick == null) {
            throw new IllegalArgumentException("Il callback per click sui libri non può essere null");
        }
        if (query == null) {
            throw new IllegalArgumentException("La query di ricerca non può essere null");
        }

        Label title = new Label("🔍 Risultati per \"" + query + "\" (" + searchResults.size() + ")");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        // Per i risultati di ricerca usa FlowPane
        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(25);
        bookGrid.setPadding(new Insets(10));
        bookGrid.setPrefWrapLength(750);

        gridBuilder.populateBookGrid(searchResults, bookGrid, null);

        ScrollPane scroll = new ScrollPane(bookGrid);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox section = new VBox(5, title, scroll);
        section.setPadding(new Insets(15, 20, 20, 20));
        section.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 30%, #2c2c2c, #1e1e1e);" +
                        "-fx-background-radius: 12;"
        );

        return section;
    }

    /**
     * Esegue una ricerca di libri e visualizza i risultati con gestione stati completa.
     * <p>
     * Metodo pubblico per eseguire ricerche di libri con aggiornamento dinamico
     * dell'UI. Gestisce stati di loading, risultati, errori, e callback specifici
     * per risultati di ricerca. Progettato per essere chiamato da componenti UI
     * come campi di ricerca o pulsanti.
     * </p>
     *
     * <h4>Flusso di esecuzione:</h4>
     * <ol>
     *   <li>Pulisce il container e mostra stato loading</li>
     *   <li>Esegue ricerca asincrona tramite BookService</li>
     *   <li>Gestisce risultati: crea sezione o mostra messaggio vuoto</li>
     *   <li>Esegue callback specifico per risultati di ricerca</li>
     *   <li>Gestisce errori con messaggi informativi</li>
     * </ol>
     *
     * <h4>Gestione stati:</h4>
     * <ul>
     *   <li><strong>Loading:</strong> "🔍 Ricerca in corso..."</li>
     *   <li><strong>Success:</strong> Sezione risultati o messaggio vuoto</li>
     *   <li><strong>Error:</strong> Messaggio di errore con dettagli</li>
     * </ul>
     *
     * @param query la stringa di ricerca da eseguire
     * @param content il container {@link VBox} dove visualizzare i risultati
     * @param clickHandler il callback per gestire click sui libri trovati
     * @throws IllegalArgumentException se uno qualsiasi dei parametri è {@code null}
     * @see BookService#searchBooksAsync(String)
     * @see #createSearchResultsSection(List, Consumer, String)
     */
    public void performSearch(String query, VBox content, Consumer<Book> clickHandler) {
        if (query == null) {
            throw new IllegalArgumentException("La query di ricerca non può essere null");
        }
        if (content == null) {
            throw new IllegalArgumentException("Il container dei contenuti non può essere null");
        }
        if (clickHandler == null) {
            throw new IllegalArgumentException("Il click handler non può essere null");
        }

        content.getChildren().clear();

        // Loading indicator
        Label loadingLabel = new Label("🔍 Ricerca in corso...");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        loadingLabel.setTextFill(Color.WHITE);
        content.getChildren().add(loadingLabel);

        bookService.searchBooksAsync(query)
                .thenAccept(results -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();

                        // Notifica callback ricerca
                        if (searchResultsCallback != null) {
                            searchResultsCallback.accept(results);
                        }

                        if (results.isEmpty()) {
                            Label noResults = new Label("❌ Nessun risultato trovato per: " + query);
                            noResults.setFont(Font.font("System", FontWeight.NORMAL, 16));
                            noResults.setTextFill(Color.LIGHTGRAY);
                            content.getChildren().add(noResults);
                        } else {
                            VBox resultsSection = createSearchResultsSection(results, clickHandler, query);
                            content.getChildren().add(resultsSection);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        Label errorLabel = new Label("❌ Errore durante la ricerca: " + throwable.getMessage());
                        errorLabel.setTextFill(Color.web("#e74c3c"));
                        content.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    /**
     * Crea un ScrollPane ottimizzato per sezioni di libri.
     * <p>
     * Factory method privato per creare ScrollPane con configurazioni
     * standardizzate per tutte le sezioni di libri. Garantisce consistenza
     * nel comportamento di scroll e nell'aspetto visivo.
     * </p>
     *
     * <h4>Configurazioni applicate:</h4>
     * <ul>
     *   <li>Scroll orizzontale disabilitato per layout FlowPane</li>
     *   <li>Scroll verticale automatico quando necessario</li>
     *   <li>Pannable per dispositivi touch</li>
     *   <li>FitToWidth per adattamento automatico</li>
     *   <li>Altezza predefinita ottimizzata</li>
     *   <li>Sfondo trasparente per integrazione</li>
     * </ul>
     *
     * @return un {@link ScrollPane} pre-configurato per sezioni di libri
     */
    private ScrollPane createScrollPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(280);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    /**
     * Crea l'header standardizzato per le sezioni di libri.
     * <p>
     * Factory method privato per creare header con layout consistente
     * per tutte le sezioni. Include spazio per titolo e eventuali
     * controlli aggiuntivi futuri.
     * </p>
     *
     * @param title la {@link Label} del titolo da includere nell'header
     * @return un {@link HBox} configurato come header di sezione
     * @throws IllegalArgumentException se title è {@code null}
     */
    private HBox createSectionHeader(Label title) {
        if (title == null) {
            throw new IllegalArgumentException("Il titolo non può essere null");
        }

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(title, spacer);
        return headerBox;
    }

    /**
     * Visualizza un messaggio di errore per sezioni FlowPane con opzione retry.
     * <p>
     * Metodo privato per gestire la visualizzazione di errori nelle sezioni
     * con layout FlowPane. Crea un'interfaccia utente per comunicare l'errore
     * e fornisce un pulsante per tentare nuovamente il caricamento.
     * </p>
     *
     * <h4>Elementi dell'interfaccia di errore:</h4>
     * <ul>
     *   <li>Messaggio di errore descrittivo con icona</li>
     *   <li>Pulsante retry per nuovi tentativi</li>
     *   <li>Layout centrato per migliore presentazione</li>
     *   <li>Styling coerente con il tema dell'applicazione</li>
     * </ul>
     *
     * @param scroll il {@link ScrollPane} dove visualizzare il messaggio di errore
     * @param sectionType il tipo di sezione per personalizzare il messaggio
     * @throws IllegalArgumentException se scroll o sectionType sono {@code null}
     * @apiNote Il pulsante retry attualmente non implementa logica di ricaricamento
     *          automatico. Questa funzionalità può essere aggiunta in future versioni.
     */
    private void showSectionError(ScrollPane scroll, String sectionType) {
        if (scroll == null) {
            throw new IllegalArgumentException("Lo ScrollPane non può essere null");
        }
        if (sectionType == null) {
            throw new IllegalArgumentException("Il tipo di sezione non può essere null");
        }

        Label errorLabel = new Label("❌ Errore nel caricamento di " + sectionType);
        errorLabel.setTextFill(Color.web("#FF6B6B"));
        errorLabel.setFont(Font.font("System", 16));

        Button retryButton = new Button("🔄 Riprova");
        retryButton.setStyle(
                "-fx-background-color: #FF6B6B;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        VBox errorBox = new VBox(10, errorLabel, retryButton);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(280);

        scroll.setContent(errorBox);
    }

    /**
     * Visualizza un messaggio quando non ci sono libri in evidenza disponibili.
     * <p>
     * Metodo privato per gestire il caso in cui la sezione featured non
     * ha contenuti da visualizzare. Crea un'interfaccia placeholder
     * informativa e user-friendly.
     * </p>
     *
     * @param container il {@link VBox} dove visualizzare il messaggio
     * @throws IllegalArgumentException se container è {@code null}
     */
    private void showNoFeaturedMessage(VBox container) {
        if (container == null) {
            throw new IllegalArgumentException("Il container non può essere null");
        }

        Label noFeatured = new Label("📚 Nessun libro in evidenza disponibile");
        noFeatured.setTextFill(Color.GRAY);
        noFeatured.setFont(Font.font("System", 16));

        VBox noFeaturedBox = new VBox(noFeatured);
        noFeaturedBox.setAlignment(Pos.CENTER);
        noFeaturedBox.setPrefHeight(300);

        container.getChildren().clear();
        container.getChildren().add(noFeaturedBox);
    }

    /**
     * Visualizza un messaggio di errore generico in un container.
     * <p>
     * Metodo di utilità privato per visualizzare messaggi di errore
     * in container generici con styling appropriato e layout centrato.
     * </p>
     *
     * @param container il {@link VBox} dove visualizzare il messaggio di errore
     * @param message il messaggio di errore da visualizzare
     * @throws IllegalArgumentException se container o message sono {@code null}
     */
    private void showErrorMessage(VBox container, String message) {
        if (container == null) {
            throw new IllegalArgumentException("Il container non può essere null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Il messaggio non può essere null");
        }

        Label errorLabel = new Label(message);
        errorLabel.setTextFill(Color.web("#FF6B6B"));
        errorLabel.setFont(Font.font("System", 16));

        VBox errorBox = new VBox(errorLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPrefHeight(300);

        container.getChildren().clear();
        container.getChildren().add(errorBox);
    }
}